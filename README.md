# HeadPacket
**HeadPacket** is a high-performance, cross-platform Minecraft plugin designed to enhance your server list presence with advanced MOTD customization, custom head rendering, and sophisticated spoofing features.

Created by **vanes430** | [GitHub](https://github.com/vanes430/HeadPacket)

## 🌟 Key Features
- **Universal JAR**: A single JAR file that works seamlessly on both **Paper (1.21+)** and **Velocity (3.x)**.
- **Custom MOTD Heads**: Render high-quality images directly into your MOTD using custom player head textures.
- **Player List Spoofing (Hover)**: Create beautiful, colorized hover messages in the player list with support for **MiniMessage** and **Legacy** color codes.
- **Software Spoofing**: Change the server software/version text (top right corner) to any custom text (e.g., "HeadPacket v1.0").
- **Dynamic Slots**: Optional `always_plus_one` feature to force the maximum player count to always be `online + 1`.
- **Bedrock Exclusion**: Advanced detection to skip custom MOTD processing for Bedrock/MCPE players, ensuring compatibility.
- **Java 21 Virtual Threads**: Optimized image processing and uploading using lightweight virtual threads to prevent server lag.
- **Smart Caching**: Efficient JSON caching system for MOTD data and hover lists to ensure zero performance impact during ping spam.
- **Protocol Filtering**: Set a minimum protocol version required to see advanced MOTD features.
- **Modern Command APIs**:
  - **Paper**: Uses modern Lifecycle Brigadier for native tab-completion.
  - **Velocity**: Uses the latest SimpleCommand API.

## 📋 Requirements
- **Java 21** or higher.
- **PacketEvents**: This plugin requires [PacketEvents](https://modrinth.com/plugin/packetevents) to be installed on your server or proxy.

## 🚀 Installation
1. Download `headpacket-1.0.0-SNAPSHOT.jar`.
2. Place the JAR into your server's `plugins` folder (Paper or Velocity).
3. Ensure **PacketEvents** is also installed.
4. Restart your server to generate the default configuration.
5. Add your image (e.g., `motd.png`) to the `plugins/HeadPacket/images/` folder.
6. Run `/headpacket process-motd` from the **Console** to generate your heads.

## 🛠️ Configuration
### `config.yml`
```yaml
# HeadPacket Configuration
mineskin-api-key: "" # Get one at mineskin.org for higher limits
images-folder: "images"
motd-image: "motd.png"
debug: true

# Advanced MOTD Settings
motd_minimum_protocol: 773 # Only show heads for 1.21.2+ clients

# Software Spoof (The text shown as the server software/version)
software_spoof: "HeadPacket v1.0"
always_plus_one: true # Force max slots to be online + 1
ignore-bedrock: true # Skip custom MOTD for MCPE players (LibreLogin style detection)

# Hover Messages (Player List)
hover-messages:
  - "<gradient:aqua:blue>HeadPacket</gradient> by vanes430"
  - "&7Status: &aOnline"
  - "&eJoin now!"
```

## ⌨️ Commands & Permissions
| Command | Description | Permission |
| :--- | :--- | :--- |
| `/headpacket reload` | Reloads configuration and mapping. | `headpacket.admin` |
| `/headpacket process-motd` | Processes and uploads the MOTD image (Console Only). | `headpacket.admin` |

*Note: You can also use the alias `/hp`.*

## 🏗️ Technical Details
- **Architecture**: Modular design with no single file exceeding 100 lines for maximum maintainability.
- **Performance**: Heavy I/O operations (MineSkin API) are handled on **Virtual Threads**.
- **Messaging**: Fully powered by the **Kyori Adventure API** for modern text components.
- **Build System**: Maven with **Spotless** for consistent code formatting.

---
© 2026 vanes430. All rights reserved.
