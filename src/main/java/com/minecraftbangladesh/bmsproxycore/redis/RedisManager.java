package com.minecraftbangladesh.bmsproxycore.redis;

import com.minecraftbangladesh.bmsproxycore.BMSProxyCore;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Redis connection manager for BMSProxyCore
 * Handles connection pooling, pub/sub, and graceful failure handling
 * Compatible with Redis 6, 7, and 8
 */
public class RedisManager {
    
    private final BMSProxyCore plugin;
    private JedisPool jedisPool;
    private final AtomicBoolean isConnected = new AtomicBoolean(false);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private ExecutorService pubSubExecutor;
    
    public RedisManager(BMSProxyCore plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Initialize Redis connection pool
     * @return true if connection was successful, false otherwise
     */
    public boolean initialize() {
        if (isConnected.get()) {
            plugin.getLogger().warn("Redis manager is already initialized");
            return true;
        }
        
        try {
            // Create pool configuration
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(plugin.getConfigManager().getRedisPoolMaxTotal());
            poolConfig.setMaxIdle(plugin.getConfigManager().getRedisPoolMaxIdle());
            poolConfig.setMinIdle(plugin.getConfigManager().getRedisPoolMinIdle());
            poolConfig.setTestOnBorrow(true);
            poolConfig.setTestOnReturn(true);
            poolConfig.setTestWhileIdle(true);
            poolConfig.setBlockWhenExhausted(true);
            
            // Create connection pool
            String host = plugin.getConfigManager().getRedisHost();
            int port = plugin.getConfigManager().getRedisPort();
            int timeout = plugin.getConfigManager().getRedisPoolTimeout();
            String password = plugin.getConfigManager().getRedisPassword();
            int database = plugin.getConfigManager().getRedisDatabase();
            
            if (password.isEmpty()) {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, null, database);
            } else {
                jedisPool = new JedisPool(poolConfig, host, port, timeout, password, database);
            }
            
            // Test connection
            try (Jedis jedis = jedisPool.getResource()) {
                String response = jedis.ping();
                if (!"PONG".equals(response)) {
                    plugin.getLogger().error("Redis ping test failed. Expected PONG, got: " + response);
                    return false;
                }
            }
            
            // Initialize pub/sub executor
            pubSubExecutor = Executors.newCachedThreadPool(r -> {
                Thread thread = new Thread(r, "BMSProxyCore-Redis-PubSub");
                thread.setDaemon(true);
                return thread;
            });
            
            isConnected.set(true);
            plugin.getLogger().info("Redis connection established successfully");
            plugin.getLogger().info("Connected to Redis " + getRedisVersion() + " at " + host + ":" + port);
            
            return true;
            
        } catch (Exception e) {
            plugin.getLogger().error("Failed to initialize Redis connection", e);
            cleanup();
            return false;
        }
    }
    
    /**
     * Get Redis server version for compatibility logging
     */
    private String getRedisVersion() {
        try (Jedis jedis = jedisPool.getResource()) {
            String info = jedis.info("server");
            for (String line : info.split("\r\n")) {
                if (line.startsWith("redis_version:")) {
                    return line.substring("redis_version:".length());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warn("Could not determine Redis version", e);
        }
        return "unknown";
    }
    
    /**
     * Publish a message to a Redis channel
     * @param channel The channel to publish to
     * @param message The message to publish
     * @return CompletableFuture that completes when the message is published
     */
    public CompletableFuture<Boolean> publishMessage(String channel, String message) {
        return CompletableFuture.supplyAsync(() -> {
            if (!isConnected.get() || isShuttingDown.get()) {
                return false;
            }
            
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.publish(channel, message);
                return true;
            } catch (JedisConnectionException e) {
                plugin.getLogger().error("Failed to publish message to Redis channel: " + channel, e);
                return false;
            }
        });
    }
    
    /**
     * Subscribe to a Redis channel with a custom listener
     * @param listener The pub/sub listener
     * @param channels The channels to subscribe to
     */
    public void subscribe(JedisPubSub listener, String... channels) {
        if (!isConnected.get() || isShuttingDown.get()) {
            plugin.getLogger().warn("Cannot subscribe to Redis channels - not connected");
            return;
        }
        
        pubSubExecutor.submit(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                plugin.getLogger().info("Subscribing to Redis channels: " + String.join(", ", channels));
                jedis.subscribe(listener, channels);
            } catch (JedisConnectionException e) {
                if (!isShuttingDown.get()) {
                    plugin.getLogger().error("Redis subscription failed", e);
                }
            } catch (Exception e) {
                plugin.getLogger().error("Unexpected error in Redis subscription", e);
            }
        });
    }
    
    /**
     * Execute a Redis command with automatic resource management
     * @param command The command to execute
     * @return The result of the command, or null if failed
     */
    public <T> T executeCommand(RedisCommand<T> command) {
        if (!isConnected.get() || isShuttingDown.get()) {
            return null;
        }

        try (Jedis jedis = jedisPool.getResource()) {
            return command.execute(jedis);
        } catch (JedisConnectionException e) {
            plugin.getLogger().error("Redis command execution failed", e);
            return null;
        } catch (Exception e) {
            plugin.getLogger().error("Redis command execution failed with unexpected error", e);
            return null;
        }
    }
    
    /**
     * Check if Redis is connected and available
     */
    public boolean isConnected() {
        return isConnected.get() && jedisPool != null && !jedisPool.isClosed();
    }
    
    /**
     * Shutdown Redis manager and cleanup resources
     */
    public void shutdown() {
        isShuttingDown.set(true);
        plugin.getLogger().info("Shutting down Redis manager...");
        
        cleanup();
        
        plugin.getLogger().info("Redis manager shutdown complete");
    }
    
    private void cleanup() {
        isConnected.set(false);
        
        if (pubSubExecutor != null && !pubSubExecutor.isShutdown()) {
            pubSubExecutor.shutdown();
            pubSubExecutor = null;
        }
        
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            jedisPool = null;
        }
    }
    
    /**
     * Functional interface for Redis commands
     */
    @FunctionalInterface
    public interface RedisCommand<T> {
        T execute(Jedis jedis) throws Exception;
    }
}
