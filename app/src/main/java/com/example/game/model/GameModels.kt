package com.example.game.model

enum class BlockType {
    NORMAL,
    STRONG,
    ICE,
    STAR,
    COIN,
    MAGIC,
    MOVING,
    BOMB,
    RAINBOW
}

enum class BlockColor {
    RED,
    BLUE,
    GREEN,
    YELLOW,
    PURPLE,
    ORANGE,
    PINK,
    RAINBOW,
    NONE
}

data class BlockItem(
    val id: String,
    val x: Int,
    val y: Int,
    val type: BlockType = BlockType.NORMAL,
    val color: BlockColor = BlockColor.RED,
    val hp: Int = 1,
    val maxHp: Int = 1,
    val isBloki: Boolean = false,
    val isGoal: Boolean = false,
    val moveDirection: Int = 1 // For moving blocks (+1 right/down, -1 left/up)
)

sealed class ObjectiveType {
    data class BreakBlocks(val target: Int) : ObjectiveType()
    object RescueBloki : ObjectiveType()
    data class CollectStars(val target: Int) : ObjectiveType()
    data class CollectCoins(val target: Int) : ObjectiveType()
    data class ClearColor(val color: BlockColor, val target: Int) : ObjectiveType()
}

data class ObjectiveProgress(
    val type: ObjectiveType,
    var current: Int = 0,
    val target: Int = 1,
    var completed: Boolean = false
) {
    val description: String
        get() = when (type) {
            is ObjectiveType.BreakBlocks -> "Break Blocks: $current/$target"
            is ObjectiveType.RescueBloki -> if (completed) "Bloki Rescued! ⭐" else "Rescue Bloki!"
            is ObjectiveType.CollectStars -> "Collect Stars: $current/$target"
            is ObjectiveType.CollectCoins -> "Collect Coins: $current/$target"
            is ObjectiveType.ClearColor -> "Clear ${type.color.name.lowercase().replaceFirstChar { it.uppercase() }}: $current/$target"
        }
}

data class LevelDefinition(
    val levelNumber: Int,
    val worldName: String,
    val worldId: Int, // 1 to 5
    val movesAllowed: Int,
    val objectives: List<ObjectiveType>,
    val gridWidth: Int = 6,
    val gridHeight: Int = 8,
    val initialBlocks: List<BlockItem>,
    val tutorialHint: String? = null
)

enum class PowerUpType(
    val title: String,
    val description: String,
    val costCoins: Int
) {
    HAMMER("Hammer", "Tap any block to crush it!", 50),
    ROCKET("Rocket", "Clears entire row and column!", 75),
    RAINBOW("Rainbow", "Clears all blocks of chosen color!", 100),
    MAGIC_WAND("Magic Wand", "Transforms tough blocks into Star/Coin blocks!", 80),
    SHUFFLE("Shuffle", "Shuffles the puzzle when stuck!", 40)
}

data class Costume(
    val id: String,
    val name: String,
    val priceCoins: Int,
    val description: String,
    val iconEmoji: String
)

object CostumeCatalog {
    val allCostumes = listOf(
        Costume("classic", "Classic Bloki", 0, "The friendly bright cube buddy!", "⭐"),
        Costume("superhero", "Superhero Bloki", 100, "Cape and mask to save the day!", "🦸"),
        Costume("prince", "Royal Prince", 150, "Shiny golden royal crown!", "👑"),
        Costume("dino", "Dino Bloki", 200, "Cute green spikes and scales!", "🦖"),
        Costume("astronaut", "Astro Bloki", 250, "Cosmic space explorer helmet!", "🚀"),
        Costume("party", "Party Bloki", 180, "Festive party cone & confetti!", "🎉"),
        Costume("wizard", "Magic Wizard", 300, "Mystical starry wizard hat!", "🧙"),
        Costume("cool", "Cool Shades", 120, "Retro stylish shades!", "😎")
    )
}

data class WorldInfo(
    val id: Int,
    val name: String,
    val emoji: String,
    val startLevel: Int,
    val endLevel: Int,
    val requiredStars: Int,
    val themeKey: String
)

object WorldCatalog {
    val worlds = listOf(
        WorldInfo(1, "Rainbow Garden", "🌈", 1, 20, 0, "rainbow_garden"),
        WorldInfo(2, "Candy Valley", "🍬", 21, 40, 25, "candy_valley"),
        WorldInfo(3, "Cloud Kingdom", "☁️", 41, 60, 55, "cloud_kingdom"),
        WorldInfo(4, "Jungle Blocks", "🌴", 61, 80, 85, "jungle_blocks"),
        WorldInfo(5, "Space Blocks", "🚀", 81, 100, 115, "space_blocks")
    )
}
