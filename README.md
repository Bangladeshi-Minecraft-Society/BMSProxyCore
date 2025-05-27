# BMSProxyCore

A comprehensive, modular Velocity proxy plugin for staff communication, private messaging, and server management with extensive customization options.

## 🌟 Features

### 📢 Staff Chat System
- **Dedicated Staff Communication**: Private channel for staff members
- **Discord Integration**: Send staff messages to Discord webhooks with player avatars
- **Staff Activity Tracking**: Monitor staff joins, leaves, and server switches
- **Configurable Formatting**: Customize message formats and prefixes
- **Toggle Mode**: Staff can toggle between public and staff chat

### 💬 Private Messaging System
- **Private Messages**: Send direct messages between players
- **Reply System**: Quick reply to the last received message
- **Social Spy**: Monitor all private messages (admin feature)
- **Message Toggle**: Players can disable incoming messages
- **Ignore System**: Block messages from specific players
- **Bypass Permissions**: Staff can bypass toggles and ignores

### 🏠 Lobby Command System
- **Server Teleportation**: Quick teleport to configured lobby server
- **Customizable Commands**: Configure main command and aliases
- **Cooldown System**: Prevent command spam with configurable cooldowns
- **Permission-Based**: Flexible permission system with bypass options

### 🔧 Modular Architecture
- **Module Management**: Enable/disable individual modules
- **Dynamic Reloading**: Change module states without restart
- **Separate Configurations**: Dedicated config files per module
- **Conditional Loading**: Only load enabled modules

### 🔒 Advanced Permission System
- **Granular Control**: Individual permissions for each feature
- **Customizable Permissions**: Configure all permission nodes
- **Bypass Permissions**: Special permissions for staff privileges
- **Role-Based Access**: Different permission levels for different ranks

## 📋 Commands

### Staff Chat Module
| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/staffchat <message>` | `/sc` | Send a message to staff chat | `bmsproxycore.staffchat.use` |
| `/staffchattoggle` | `/sctoggle` | Toggle staff chat mode | `bmsproxycore.staffchat.toggle` |

### Private Messages Module
| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/msg <player> <message>` | `/whisper` | Send a private message | `bmsproxycore.message.send` |
| `/reply <message>` | `/r` | Reply to last message | `bmsproxycore.message.reply` |
| `/socialspy` | - | Toggle social spy mode | `bmsproxycore.message.socialspy` |
| `/msgtoggle` | - | Toggle message acceptance | `bmsproxycore.message.toggle` |
| `/ignore <add\|remove\|list> [player]` | - | Manage ignored players | `bmsproxycore.message.ignore` |

### Lobby Command Module
| Command | Aliases | Description | Permission |
|---------|---------|-------------|------------|
| `/lobby` | `/hub`, `/spawn`, `/main`, `/l` | Teleport to lobby server | `bmsproxycore.lobby.use` |

### Administration
| Command | Description | Permission |
|---------|-------------|------------|
| `/bmsproxycore reload` | Reload all configurations and modules | `bmsproxycore.admin.reload` |
| `/bmsproxycore status` | Show plugin and module status | `bmsproxycore.admin.info` |
| `/bmsproxycore modules` | Show detailed module information | `bmsproxycore.admin.info` |

## 🔑 Permissions

### Staff Chat Module
- `bmsproxycore.staffchat.use` - Use staff chat commands and see messages
- `bmsproxycore.staffchat.toggle` - Toggle staff chat mode
- `bmsproxycore.staffchat.reload` - Reload staff chat configuration
- `bmsproxycore.staffchat.activity` - See staff activity notifications

### Private Messages Module
- `bmsproxycore.message.send` - Send private messages
- `bmsproxycore.message.reply` - Reply to messages
- `bmsproxycore.message.socialspy` - Use social spy feature
- `bmsproxycore.message.toggle` - Toggle message acceptance
- `bmsproxycore.message.ignore` - Use ignore system
- `bmsproxycore.message.bypass.toggle` - Bypass message toggle
- `bmsproxycore.message.bypass.ignore` - Bypass ignore system

### Lobby Command Module
- `bmsproxycore.lobby.use` - Use lobby teleport commands
- `bmsproxycore.lobby.cooldown.bypass` - Bypass command cooldown

### Administration
- `bmsproxycore.admin.reload` - Reload configurations
- `bmsproxycore.admin.info` - View plugin information

## ⚙️ Configuration

### Main Configuration (`config.yml`)
```yaml
# Module Configuration
modules:
  staffchat:
    enabled: true
  private_messages:
    enabled: true
  lobby_command:
    enabled: true

# Global Messages
global:
  no-permission-message: "&cYou don't have permission to use this command."
  reload-success-message: "&aConfiguration successfully reloaded."
  module-disabled-message: "&cThis feature is currently disabled."
```

### Staff Chat Configuration (`staffchat.yml`)
```yaml
# Permission Configuration
permissions:
  use: "bmsproxycore.staffchat.use"
  toggle: "bmsproxycore.staffchat.toggle"
  activity: "bmsproxycore.staffchat.activity"

# Message Formatting
staffchat-prefix: "&b&lStaff &8|"
message-format: "{prefix} &7[{server}] &f{player} &8»&r {message}"

# Discord Integration
discord:
  enabled: false
  webhook-url: "https://discord.com/api/webhooks/your-webhook-url-here"
  webhook-name: "Staff Chat"
  use-player-avatar: true
```

### Private Messages Configuration (`privatemessages.yml`)
```yaml
# Permission Configuration
permissions:
  send: "bmsproxycore.message.send"
  reply: "bmsproxycore.message.reply"
  socialspy: "bmsproxycore.message.socialspy"
  bypass-toggle: "bmsproxycore.message.bypass.toggle"

# Message Formatting
sender-format: "&8[&7You &8→ &7{receiver}&8] &f{message}"
receiver-format: "&8[&7{sender} &8→ &7You&8] &f{message}"
socialspy-format: "&8[&cSPY&8] &7{sender} &8→ &7{receiver}&8: &f{message}"
```

### Lobby Command Configuration (`lobbycommand.yml`)
```yaml
# Permission Configuration
permissions:
  use: "bmsproxycore.lobby.use"
  bypass-cooldown: "bmsproxycore.lobby.cooldown.bypass"

# Server Configuration
target-server: "lobby"
main-command: "lobby"
aliases: ["hub", "spawn", "main", "l"]

# Cooldown System
cooldown: 3
cooldown-message: "&cYou must wait {time} seconds before using this command again."
```

## 🚀 Installation

1. **Download** the latest release from the releases page
2. **Place** the JAR file in your Velocity `plugins` folder
3. **Restart** your Velocity proxy server
4. **Configure** the plugin in `plugins/bmsproxycore/` directory:
   - Edit `config.yml` to enable/disable modules
   - Configure individual modules in their respective files
   - Set up Discord webhooks if desired

## 📋 Requirements

- **Velocity**: 3.3.0 or higher
- **Java**: 21 or higher
- **Minecraft**: 1.16+ (for full feature compatibility)

## 🔧 Module Management

### Enable/Disable Modules
```yaml
# In config.yml
modules:
  staffchat:
    enabled: true    # Enable staff chat
  private_messages:
    enabled: false   # Disable private messages
  lobby_command:
    enabled: true    # Enable lobby commands
```

### Dynamic Reloading
Use `/bmsproxycore reload` to:
- Reload all configuration files
- Enable/disable modules without restart
- Apply permission changes
- Update message formats

## 🎯 Use Cases

### For Large Networks
- **Staff Coordination**: Cross-server staff communication
- **Moderation**: Social spy for monitoring player interactions
- **Quick Navigation**: Lobby commands for staff efficiency

### For Small Servers
- **Simple Setup**: Enable only needed modules
- **Lightweight**: Minimal resource usage
- **Customizable**: Adapt to your server's needs

## 🔒 Security Features

- **Permission-based access control**
- **Configurable permission nodes**
- **Bypass permissions for staff**
- **Module isolation**
- **Input validation and sanitization**

## 📞 Support

- **Issues**: Report bugs on GitHub Issues
- **Documentation**: Check the wiki for detailed guides
- **Community**: Join our Discord for support and updates

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

---

**Made with ❤️ for the Minecraft community**