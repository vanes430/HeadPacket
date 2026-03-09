# HeadPacket
**HeadPacket** is a high-performance, cross-platform Minecraft plugin designed to enhance your server list presence with advanced MOTD customization, custom head rendering, and sophisticated spoofing features.

Created by **vanes430** | [GitHub](https://github.com/vanes430/HeadPacket)

## 🌟 Key Features
- **Universal JAR**: A single JAR file that works seamlessly on both **Paper (1.21+)** and **Velocity (3.x)**.
- **Custom MOTD Heads**: Render high-quality images directly into your MOTD using custom player head textures.
- **Player List Spoofing (Hover)**: Create beautiful, colorized hover messages in the player list with support for **MiniMessage** and **Legacy** color codes.
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
- **MineSkin API Key**: **Mandatory**. This plugin relies entirely on [MineSkin](https://mineskin.org/) to process and host head textures. To respect their hard work and ensure the plugin functions correctly (avoiding strict public rate limits), you **must** provide your own API key. The plugin will not work as intended without a valid key.

## 🚀 Installation
1. Download `headpacket-1.0.0-SNAPSHOT.jar`.
2. Place the JAR into your server's `plugins` folder (Paper or Velocity).
3. Ensure **PacketEvents** is also installed.
4. Restart your server to generate the default configuration.
5. Add your MineSkin API Key to `config.yml`.
6. Add your image (e.g., `motd.png`) to the `plugins/HeadPacket/images/` folder.
7. Run `/headpacket process-motd [percentage]` from the **Console** to generate your heads.

## 🛠️ Configuration
### `config.yml`
```yaml
# ==========================================================
#                  HeadPacket Configuration
#           Created by vanes430 | github.vanes430
# ==========================================================

# MineSkin API Key (MANDATORY)
# Get a free API Key at https://mineskin.org/
# This plugin respects MineSkin's service and requires an API Key to function.
mineskin-api-key: "your_api_key_here"

# Upload Delay (Milliseconds)
# Recommended: 2000 (2 seconds) for public/standard accounts to prevent rate limits.
# Lower this only if you have a special MineSkin plan with higher limits.
mineskin-delay: 2000

# Image Settings
# Name of the folder where you store images (inside the HeadPacket plugin folder).
images-folder: "images"
# Name of the image file to be used as MOTD (Must be inside images-folder).
# Recommended format: PNG (Transparency is supported).
motd-image: "motd.png"

# Debug Mode
# If true, the plugin will show additional information in the console during loading/processing.
debug: true

# Advanced MOTD Settings
# Minimum protocol version to display custom heads in MOTD.
# 773 = Minecraft 1.21.9/10 and above (supports ObjectComponent features).
motd_minimum_protocol: 773

# Fallback MOTD
# This MOTD will be shown to players using versions older than the minimum protocol.
# Supports MiniMessage and Legacy color codes.
fallback-motd:
  line1: "&bHeadPacket &7- &fCustom Heads MOTD"
  line2: "&ePlease use 1.21.9+ for the full experience!"

# Dynamic Slots (Always Plus One)
# If true, the maximum player slots will automatically be set to current online players + 1.
# Example: 5/6, 10/11, etc.
always_plus_one: true

# Bedrock Exclusion (Geyser/Floodgate Support)
# If true, the plugin will NOT send custom MOTD/Heads to Bedrock/MCPE players.
# This is important as certain JSON Head features may cause display issues on Bedrock clients.
ignore-bedrock: true

# Hover Messages (Player List Sample)
# Text that appears when players hover over the player count in the server list.
# Supports MiniMessage (<gradient>, <rainbow>, etc) and Legacy (&a, &b, etc) formats.
hover-messages:
  - "&bHeadPacket &fby vanes430"
  - "&7Status: &aOnline"
  - "&eJoin now!"
```

## ⌨️ Commands & Permissions
| Command | Description | Permission |
| :--- | :--- | :--- |
| `/headpacket reload` | Reloads configuration and mapping. | **Console Only** |
| `/headpacket process-motd [pct]` | Processes and uploads the MOTD image. | **Console Only** |

*Note: You can also use the alias `/hp`.*

## 🏗️ Technical Details
- **Image Limits**: 
  - **Height**: Must be exactly **8** or **16** pixels.
  - **Width**: Must be a **multiple of 8** (Maximum **264** pixels).
- **Architecture**: Modular design with no single file exceeding 100 lines for maximum maintainability.
- **Performance**: Heavy I/O operations (MineSkin API) are handled on **Virtual Threads**.
- **Messaging**: Fully powered by the **Kyori Adventure API** for modern text components.

---
© 2026 vanes430. All rights reserved.
