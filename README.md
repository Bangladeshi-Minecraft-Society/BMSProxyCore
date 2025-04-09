# BMSProxyCore

A Velocity proxy plugin that implements a cross-server staff chat system and private messaging system for Minecraft servers.

## Features

- **Staff Chat**: Send and receive messages exclusively among staff members across all connected servers
- **Toggle Mode**: Switch to a mode where all your messages are automatically sent to staff chat
- **Discord Integration**: Send staff chat messages to Discord using webhooks with player avatars
- **Console Support**: View staff chat in console and allow console to send staff chat messages
- **Private Messaging**: Send private messages to players across all connected servers
- **Social Spy**: Allow staff to see private messages between players
- **Message Toggle**: Allow players to toggle whether they receive private messages
- **Ignore System**: Allow players to ignore messages from specific players
- **Configurable**: Customize message formats, colors, and feedback messages
- **Permissions-Based**: Granular permission system for all features

## Commands

### Staff Chat Commands
- `/staffchat <message>` (alias: `/sc <message>`): Send a message to staff chat
- `/staffchattoggle` (alias: `/sctoggle`): Toggle staff chat mode
- `/bmsproxycore reload`: Reload the configuration

### Private Messaging Commands
- `/msg <player> <message>` (alias: `/whisper`): Send a private message to a player
- `/reply <message>` (alias: `/r`): Reply to the last player who messaged you
- `/socialspy`: Toggle social spy mode to see private messages between other players
- `/msgtoggle`: Toggle whether you receive private messages
- `/ignore <add|remove|list> [player]`: Manage your ignore list

## Permissions

### Staff Chat Permissions
- `bmsproxycore.staffchat.use`: Required to send messages via `/sc` and receive staff chat messages
- `bmsproxycore.staffchat.toggle`: Required to use the `/sctoggle` command
- `bmsproxycore.staffchat.reload`: Required to use the `/bmsproxycore reload` command

### Private Messaging Permissions
- `bmsproxycore.message.send`: Required to send private messages with `/msg` and `/whisper`
- `bmsproxycore.message.reply`: Required to reply to messages with `/reply` and `/r`
- `bmsproxycore.message.toggle`: Required to toggle private message reception with `/msgtoggle`
- `bmsproxycore.message.ignore`: Required to use the `/ignore` command
- `bmsproxycore.socialspy.toggle`: Required to toggle social spy mode
- `bmsproxycore.socialspy.view`: Required to see private messages in social spy mode

## Configuration

The plugin configuration file (`config.yml`) allows you to customize various aspects:

```yaml
# Staff chat message formatting
staffchat-prefix: "&b&lStaff &8|"
message-format: "{prefix} &7[{server}] &f{player} &8»&r {message}"

# Toggle command messages
toggle-on-message: "&aStaff chat mode &eenabled&a. Your messages will now go to staff chat."
toggle-off-message: "&aStaff chat mode &cdisabled&a. Your messages will now go to public chat."

# Permission messages
no-permission-message: "&cYou don't have permission to use this command."

# Admin command messages
reload-success-message: "&aConfiguration successfully reloaded."
reload-fail-message: "&cFailed to reload configuration. Check console for errors."

# Discord Webhook Integration
discord:
  enabled: false
  webhook-url: "https://discord.com/api/webhooks/your-webhook-url-here"
  webhook-name: "Staff Chat"
  message-format: "**[{server}] {player}**: {message}"
  # Use Crafatar for avatars - set to false to use webhook default avatar
  use-player-avatar: true
  # Show player avatars using Crafatar (https://crafatar.com)
  avatar-size: 128
  # Apply the helmet overlay to the avatar (shows if player has a hat layer)
  avatar-overlay: true

# Private Messaging System
messaging:
  # Format for the sender
  sender-format: "&8[&7You &8→ &7{receiver}&8] &f{message}"
  # Format for the receiver
  receiver-format: "&8[&7{sender} &8→ &7You&8] &f{message}"
  # Format for social spy
  socialspy-format: "&8[&cSPY&8] &7{sender} &8→ &7{receiver}&8: &f{message}"
  
  # Command messages
  error-player-not-found: "&cPlayer not found."
  error-message-self: "&cYou cannot message yourself."
  error-no-reply-target: "&cYou have nobody to reply to."
  error-invalid-usage-msg: "&cUsage: /msg <player> <message>"
  error-invalid-usage-reply: "&cUsage: /reply <message>"
  
  # Social spy messages
  socialspy-enabled: "&aSocial spy enabled. You will now see private messages."
  socialspy-disabled: "&cSocial spy disabled. You will no longer see private messages."
  
  # Message toggle
  msgtoggle-enabled: "&aYou are now accepting private messages."
  msgtoggle-disabled: "&cYou are no longer accepting private messages."
  error-player-msgtoggle: "&c{player} is not accepting private messages."
  
  # Ignore system
  ignore-added: "&aYou are now ignoring {player}."
  ignore-removed: "&aYou are no longer ignoring {player}."
  error-already-ignoring: "&cYou are already ignoring {player}."
  error-not-ignoring: "&cYou are not ignoring {player}."
  error-player-ignored: "&c{player} is ignoring you."
  error-invalid-usage-ignore: "&cUsage: /ignore <add|remove|list> [player]"
  ignore-list-header: "&6Players you are ignoring:"
  ignore-list-entry: "&7- {player}"
  ignore-list-empty: "&7You are not ignoring any players."
```

## Console Integration

BMSProxyCore provides full console integration:

- All staff chat messages are logged to the console
- Console can send staff chat messages using the `/sc` or `/staffchat` command
- Console messages appear with "Console" as the player and server name
- Console has full permission for staff chat without needing to toggle it

## Discord Integration

BMSProxyCore includes Discord webhook integration that allows staff chat messages to be sent to a Discord channel:

1. Create a webhook in your Discord server (Server Settings → Integrations → Webhooks → New Webhook)
2. Copy the webhook URL
3. Update the `webhook-url` in your config.yml
4. Set `enabled: true` to enable the Discord integration

The plugin uses [Crafatar](https://crafatar.com) to display player avatars in Discord messages. You can customize:
- The webhook display name
- Message format
- Avatar size
- Whether to show the player's helmet/hat layer

## Private Messaging System

The private messaging system allows players to communicate privately across all connected servers:

- **Cross-Server Communication**: Send messages to players regardless of which backend server they're on
- **Tab Completion**: Player names are auto-completed in messaging commands
- **Reply System**: Easily reply to the last player who messaged you
- **Social Spy**: Staff can monitor private messages for moderation purposes
- **Message Toggle**: Players can disable receiving private messages
- **Ignore System**: Players can ignore messages from specific players

## Installation

1. Download the latest release from the releases page
2. Place the JAR file in your Velocity proxy's `plugins` directory
3. Start or restart your Velocity proxy
4. Edit the configuration file in `plugins/bmsproxycore/config.yml` if desired

## Building from source

```bash
./gradlew build
```

The compiled JAR file will be available in the `build/libs` directory.

## License

This project is licensed under the MIT License. # BMSProxyCore
# BMSProxyCore
