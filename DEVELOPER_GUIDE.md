# NEXU-GAME / Blocky Buddies — Developer Guide

Welcome to the **NEXU-GAME / Blocky Buddies** developer guide!  
This guide is written especially for beginner and intermediate developers. It gives you clear, step-by-step instructions on where everything is located, how to tweak game settings, how to add new content, and how to build the game.

---

## 1. Project Structure Overview

```text
app/src/main/
├── java/com/example/
│   ├── MainActivity.kt                # App container and screen switcher
│   └── game/
│       ├── config/
│       │   └── GameConfig.kt          # ⭐ CENTRAL CONFIGURATION (Edit all numbers here!)
│       ├── data/
│       │   ├── AppDatabase.kt         # Room local SQLite database setup
│       │   ├── GameDao.kt             # Database queries and transactions
│       │   ├── GameRepository.kt      # Single source of truth for player data & rules
│       │   └── entity/                # Database tables (Player, Levels, Achievements)
│       ├── engine/
│       │   ├── GameEngine.kt          # Core puzzle match-3/collapse state machine & physics
│       │   └── GameModels.kt          # Blocks, colors, objectives, particle definitions
│       ├── level/
│       │   └── LevelCatalog.kt        # 20 handcrafted levels & procedural generation
│       ├── model/
│       │   └── GameModels.kt          # Enums for PowerUpType, BlockColor, Objectives
│       ├── audio/
│       │   └── SoundManager.kt        # Procedural audio generator for sound effects
│       └── ui/
│           ├── GameViewModel.kt       # UI state management, level unlock checks, purchases
│           ├── components/            # Reusable UI widgets (Board, Bloki mascot, Dev dialog)
│           └── screens/               # Main screens: Home, Level Select, Gameplay, Rewards, Settings
```

---

## 2. Quick Editing Cheatsheet (Where to Change What)

Everything you frequently change is centralized in **`app/src/main/java/com/example/game/config/GameConfig.kt`**.

| What you want to edit | Exact File | What to look for |
| :--- | :--- | :--- |
| **Coin rewards & scoring** | `config/GameConfig.kt` | `object Rewards` |
| **New player starting coins/power-ups** | `config/GameConfig.kt` | `object PlayerDefaults` |
| **Power-up shop prices & bundle sizes** | `config/GameConfig.kt` | `object PowerUps` |
| **Achievement targets & rewards** | `config/GameConfig.kt` | `object Achievements` |
| **Daily challenge coin reward** | `config/GameConfig.kt` | `GameConfig.Rewards.DAILY_CHALLENGE_COINS` |
| **Handcrafted levels (1 to 20)** | `level/LevelCatalog.kt` | `getHandcraftedLevel(levelNumber)` |
| **Level difficulty / grid dimensions** | `level/LevelCatalog.kt` | `LevelProgressionConfig` |
| **Costumes and cosmetics** | `data/GameRepository.kt` | `initializeDefaultsIfNeeded()` |
| **Sound / Haptics / Visual toggles** | `config/GameConfig.kt` & `ui/screens/SettingsScreen.kt` | Settings toggles |

---

## 3. How to Edit Coins & Rewards

Open `app/src/main/java/com/example/game/config/GameConfig.kt`:

```kotlin
object Rewards {
    const val DAILY_CHALLENGE_COINS = 50   // Change daily challenge bonus coins
    const val LEVEL_WIN_BASE_COINS = 30    // Change coins won when completing a level
    const val COIN_BLOCK_COINS = 10        // Coins collected per golden coin block
    const val STAR_COLLECT_POINTS = 250    // Score awarded for collecting star blocks
    const val BLOKI_RESCUE_BONUS_POINTS = 1000 // Bonus points for dropping Bloki to goal
    const val LEFTOVER_MOVE_BONUS_POINTS = 150 // Points per leftover move
}
```

---

## 4. How to Edit Power-Up Prices & Quantities

Open `app/src/main/java/com/example/game/config/GameConfig.kt`:

```kotlin
object PowerUps {
    const val HAMMER_PRICE = 50          // Price in coins
    const val HAMMER_BUNDLE_SIZE = 3     // How many hammers the player receives per purchase

    const val ROCKET_PRICE = 75
    const val ROCKET_BUNDLE_SIZE = 3

    const val RAINBOW_PRICE = 100
    const val RAINBOW_BUNDLE_SIZE = 2

    const val WAND_PRICE = 80
    const val WAND_BUNDLE_SIZE = 2

    const val SHUFFLE_PRICE = 40
    const val SHUFFLE_BUNDLE_SIZE = 3
}
```

---

## 5. How to Edit Daily Challenges

- **Reward amount**: Set `GameConfig.Rewards.DAILY_CHALLENGE_COINS`.
- **Level generator**: In `LevelCatalog.kt`, see `getDailyLevel(daySeed: Long)`. It creates a fresh daily puzzle every day using the day of the year as a seed.
- **Fair Play Rule**: Players can only receive the daily coin reward **once per calendar day**. The completion date is saved persistently in the database (`PlayerProfileEntity.lastDailyPlayedDate`). Subsequent plays on the same day are allowed for fun, but will not duplicate the reward.

---

## 6. How to Edit Achievements

In `GameConfig.kt`:

```kotlin
object Achievements {
    const val FIRST_BREAK_TARGET = 1       // Unlocks immediately when breaking 1st block!
    const val FIRST_BREAK_REWARD = 50

    const val STAR_COLLECTOR_TARGET = 20   // Collect 20 stars
    const val STAR_COLLECTOR_REWARD = 100

    const val PUZZLE_MASTER_TARGET = 10    // Complete 10 levels
    const val PUZZLE_MASTER_REWARD = 150
}
```

---

## 7. How to Add a New Handcrafted Level

1. Open `app/src/main/java/com/example/game/level/LevelCatalog.kt`.
2. Find `getHandcraftedLevel(levelNumber: Int)`.
3. Add a new `when` case:

```kotlin
21 -> LevelDefinition(
    levelNumber = 21,
    worldName = "Sky Fortress",
    worldId = 3,
    movesAllowed = 18,
    objectives = listOf(
        ObjectiveType.RescueBloki,
        ObjectiveType.BreakBlocks(25)
    ),
    gridWidth = 6,
    gridHeight = 8,
    initialBlocks = buildCustomGrid21()
)
```

4. If you expand beyond level 20, update `HANDCRAFTED_COUNT` in `GameConfig.Levels.HANDCRAFTED_COUNT`.

---

## 8. How to Add a New Power-Up

To add a new power-up (e.g. `FREEZE`):
1. **Enum**: In `model/GameModels.kt`, add `FREEZE` to `enum class PowerUpType`.
2. **Config**: In `config/GameConfig.kt`, add `FREEZE_PRICE` and `FREEZE_BUNDLE_SIZE`.
3. **Database Entity**: In `data/entity/PlayerProfileEntity.kt`, add `val freezeCount: Int = 0`.
4. **Repository**: In `data/GameRepository.kt`, add purchase and usage branches.
5. **Execution**: In `engine/GameEngine.kt`, handle `PowerUpType.FREEZE` in `executePowerUpAction`.
6. **UI Button**: The gameplay screen will automatically render the new power-up icon!

---

## 9. Developer Mode & Safety

- Developer tools allow testing:
  - Jumping to any level (even locked ones)
  - Adding test coins (+500)
  - Unlocking all levels
  - Verifying level solvability
  - Resetting progress
- **Production Safety**: Developer mode is strictly protected by `BuildConfig.DEBUG`. In release builds, the UI trigger and methods are completely disabled and inaccessible.

---

## 10. How to Build & Run

### A. Run on Android Emulator or Device
```bash
./gradlew installDebug
```

### B. Build Debug APK
```bash
./gradlew assembleDebug
```
The output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`

On Windows:
```cmd
gradlew.bat assembleDebug
```

### C. Build Release APK / Bundle
Set your signing credentials in environment variables:
```bash
export KEYSTORE_PATH="/path/to/upload-keystore.jks"
export STORE_PASSWORD="your_keystore_password"
export KEY_ALIAS="your_key_alias"
export KEY_PASSWORD="your_key_password"

./gradlew assembleRelease
```
*Note*: If environment variables are not set, debug builds continue to work normally, and release builds will safely warn without breaking compilation.

### D. Run Unit Tests
```bash
./gradlew testDebugUnitTest
```

---

## 11. Important Files That Should NOT Be Edited Manually

- `gradle/wrapper/gradle-wrapper.jar` and `gradle-wrapper.properties` (Build system binary)
- `debug.keystore` and `debug.keystore.base64` (Application identity keystores)
- `AppDatabase_Impl.java` or files inside `build/` (Auto-generated by Room and Kotlin compiler)
- `metadata.json` (Platform metadata managed by AI Studio)
