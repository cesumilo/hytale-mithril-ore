# MithrilOre Plugin

![Java](https://img.shields.io/badge/Java-25-orange?style=for-the-badge&logo=java)
![Hytale](https://img.shields.io/badge/Hytale-Server-green?style=for-the-badge)

**MithrilOre** is a custom Hytale server plugin that introduces the legendary Mithril ore into your world generation. It works by intercepting the standard world generation process and replacing specific vanilla ores with Mithril based on calculated rarity.

## 💎 Features

*   **Seamless Integration**: Hooks directly into Hytale's `ChunkPostGenerationEvent` to modify chunks as they are created.
*   **Rarity System**: Replaces standard ores with Mithril based on a tiered probability system:
    *   **Copper/Iron**: Extremely low chance of conversion.
    *   **Adamantite/Cobalt**: Higher chance of conversion (richer veins yield richer rewards).
*   **Performance Optimized**: Uses efficient block ID caching to minimize generation lag.

---

## 📥 Installation

1.  **Build or Download** the plugin JAR file (e.g., `MithrilOre-1.0.0.jar`).
2.  Navigate to your Hytale Server directory.
3.  Place the JAR file into the `mods/` folder.
4.  Start the server to generate the initial configuration files.

---

## ⚙️ Configuration & Usage

Since this plugin introduces a custom **WorldGen Provider**, you must tell your world to use it. Simply installing the plugin is not enough; you must activate the generator.

### 1. Activate the World Generator
Open your server's `worlds/default/config.json` (or the specific configuration for the dimension you want Mithril in).

Locate the `WorldGen` setting and change the provider to **`MithrilOre`**:

```json
{
  "WorldGen": {
    "Type": "MithrilOre"
  }
}
```

*Note: The ID `"MithrilOre"` corresponds to the ID registered in the plugin's main class.*

### 2. Adjusting Rarity (Developers)
Currently, ore probabilities are defined within the source code for performance. To adjust the rates, modify `MithrilWorldGenProvider.java`:

```java
// Example: Increasing the chance to find Mithril inside Adamantite veins
private static final double ORE_ADAMANTITE_CHANCE = 0.08; // 8% chance
```

---

## 🏗️ Building from Source

If you want to modify the rarity or add new ores, you can build the project using Gradle.

### Prerequisites
*   Java 25 JDK
*   Git

### Build Commands

```bash
# Windows
git clone https://github.com/cesumilo/hytale-mithril-ore.git
cd hytale-mithril-ore
gradlew.bat shadowJar

# Linux / Mac
git clone https://github.com/cesumilo/hytale-mithril-ore.git
cd hytale-mithril-ore
./gradlew shadowJar
```

The compiled plugin will be found in `build/libs/`.

---

## 🧩 Technical Details

This plugin implements the `IWorldGenProvider` interface.

1.  **Initialization**: On startup, it scans the `BlockType` registry to find all IDs associated with Copper, Iron, Gold, Thorium, Cobalt, Adamantite, and Mithril.
2.  **Caching**: These IDs are cached in an Integer Array to prevent expensive string lookups during runtime.
3.  **Generation**: During the `generateChunk` phase, it iterates over the chunk data. If it encounters a target ore, it rolls a random chance. If successful, it swaps the block ID with a cached Mithril ID.

---

## 📝 License

This project is licensed under the MIT License.