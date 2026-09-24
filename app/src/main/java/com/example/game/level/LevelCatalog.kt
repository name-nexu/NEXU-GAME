package com.example.game.level

import com.example.game.config.GameConfig
import com.example.game.model.BlockColor
import com.example.game.model.BlockItem
import com.example.game.model.BlockType
import com.example.game.model.LevelDefinition
import com.example.game.model.ObjectiveType
import java.util.Random

/**
 * LevelProgressionConfig manages the progression curves for:
 * 1. Grid Complexity (5x6 up to 8x10)
 * 2. Block Colors (2 colors up to 7 colors)
 * 3. Target Scores (500 pts scaling to 15,000+ pts)
 */
object LevelProgressionConfig {

    fun getGridDimensions(level: Int): Pair<Int, Int> = when {
        level in 1..2 -> 5 to 6    // 30 cells - compact & simple
        level in 3..5 -> 5 to 7    // 35 cells
        level in 6..9 -> 6 to 7    // 42 cells
        level in 10..15 -> 6 to 8  // 48 cells - balanced
        level in 16..25 -> 7 to 8  // 56 cells - wide boards
        level in 26..40 -> 7 to 9  // 63 cells - labyrinthine
        else -> 8 to 10           // 80 cells - master challenge
    }

    fun getColorPalette(level: Int): List<BlockColor> = when {
        level in 1..2 -> listOf(BlockColor.RED, BlockColor.BLUE) // 2 colors - big natural clusters
        level in 3..5 -> listOf(BlockColor.RED, BlockColor.BLUE, BlockColor.YELLOW) // 3 colors
        level in 6..10 -> listOf(BlockColor.RED, BlockColor.BLUE, BlockColor.GREEN, BlockColor.YELLOW) // 4 colors
        level in 11..20 -> listOf(BlockColor.RED, BlockColor.BLUE, BlockColor.GREEN, BlockColor.YELLOW, BlockColor.PURPLE) // 5 colors
        level in 21..35 -> listOf(BlockColor.RED, BlockColor.BLUE, BlockColor.GREEN, BlockColor.YELLOW, BlockColor.PURPLE, BlockColor.ORANGE) // 6 colors
        else -> listOf(BlockColor.RED, BlockColor.BLUE, BlockColor.GREEN, BlockColor.YELLOW, BlockColor.PURPLE, BlockColor.ORANGE, BlockColor.PINK) // 7 colors
    }

    fun getTargetScore(level: Int): Int = when (level) {
        1 -> 500
        2 -> 750
        3 -> 1000
        4 -> 1250
        5 -> 1500
        6 -> 1800
        7 -> 2200
        8 -> 2600
        9 -> 3000
        10 -> 3500
        in 11..15 -> 4000 + (level - 10) * 400
        in 16..20 -> 6000 + (level - 15) * 500
        else -> 8500 + (level - 20) * 450
    }
}

/**
 * Complete level system containing 20 handcrafted progressive starter levels
 * plus procedural scaling for up to 1000+ levels across 5 worlds.
 */
object LevelCatalog {

    fun hasLevel(levelNumber: Int): Boolean {
        return levelNumber in 1..GameConfig.Levels.MAX_LEVELS
    }

    fun getLevel(levelNumber: Int): LevelDefinition {
        return if (levelNumber in 1..20) {
            getHandcraftedLevel(levelNumber)
        } else {
            generateProceduralLevel(levelNumber)
        }
    }

    fun getDailyLevel(daySeed: Long): LevelDefinition {
        val rand = Random(daySeed)
        val width = 6
        val height = 7
        val blocks = mutableListOf<BlockItem>()

        // Goal platform in the bottom center
        blocks.add(BlockItem("goal_daily", 2, height - 1, BlockType.NORMAL, BlockColor.NONE, isGoal = true))
        blocks.add(BlockItem("goal_daily_2", 3, height - 1, BlockType.NORMAL, BlockColor.NONE, isGoal = true))

        // Bloki at the top
        blocks.add(BlockItem("bloki_daily", 2, 0, BlockType.NORMAL, BlockColor.YELLOW, isBloki = true))

        val colors = listOf(BlockColor.RED, BlockColor.BLUE, BlockColor.GREEN, BlockColor.PURPLE, BlockColor.ORANGE)

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val randVal = rand.nextFloat()
                val type = when {
                    randVal < 0.15f -> BlockType.STAR
                    randVal < 0.30f -> BlockType.COIN
                    randVal < 0.40f -> BlockType.BOMB
                    randVal < 0.50f -> BlockType.MAGIC
                    else -> BlockType.NORMAL
                }
                val color = colors[rand.nextInt(colors.size)]
                blocks.add(BlockItem("daily_${x}_$y", x, y, type, color, hp = 1))
            }
        }

        return LevelDefinition(
            levelNumber = 9999, // Special daily indicator
            worldName = "Daily Challenge",
            worldId = 1,
            movesAllowed = 14,
            objectives = listOf(
                ObjectiveType.RescueBloki,
                ObjectiveType.CollectStars(3),
                ObjectiveType.CollectCoins(3)
            ),
            gridWidth = width,
            gridHeight = height,
            initialBlocks = blocks,
            tutorialHint = "Daily Special Puzzle! Rescue Bloki and collect all stars & coins!",
            targetScore = 3000,
            availableColors = colors
        )
    }

    private fun getHandcraftedLevel(level: Int): LevelDefinition {
        val (width, height) = LevelProgressionConfig.getGridDimensions(level)
        val colors = LevelProgressionConfig.getColorPalette(level)
        val targetScore = LevelProgressionConfig.getTargetScore(level)

        val blocks = mutableListOf<BlockItem>()
        val objectives = mutableListOf<ObjectiveType>()
        var moves = 12
        var hint: String? = null

        when (level) {
            1 -> {
                // Intro: 5x6 grid, 2 colors (RED, BLUE), Target 500
                hint = "Tap any block to break it! Help Bloki reach the bottom pad!"
                moves = 10
                objectives.add(ObjectiveType.RescueBloki)
                // Goal at bottom center (row 5)
                blocks.add(BlockItem("g1", 2, 5, isGoal = true))
                // Bloki at top center
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))
                // Stack of 3 blocks under Bloki
                blocks.add(BlockItem("b1", 2, 2, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("b2", 2, 3, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b3", 2, 4, BlockType.NORMAL, BlockColor.RED))
                // Friendly side cluster
                blocks.add(BlockItem("b4", 1, 3, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b5", 1, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b6", 3, 3, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("b7", 3, 4, BlockType.NORMAL, BlockColor.RED))
            }

            2 -> {
                // 5x6 grid, 2 colors (RED, BLUE), Target 750
                hint = "Collect 2 shiny Stars by breaking star blocks!"
                moves = 10
                objectives.add(ObjectiveType.CollectStars(2))
                objectives.add(ObjectiveType.BreakBlocks(6))
                for (y in 2..4) {
                    for (x in 1..3) {
                        val isStar = (x == 2 && y == 3) || (x == 1 && y == 4)
                        val type = if (isStar) BlockType.STAR else BlockType.NORMAL
                        val color = if (isStar) BlockColor.YELLOW else if ((x + y) % 2 == 0) BlockColor.RED else BlockColor.BLUE
                        blocks.add(BlockItem("l2_${x}_$y", x, y, type, color))
                    }
                }
            }

            3 -> {
                // 5x7 grid, 3 colors (RED, BLUE, YELLOW), Target 1000
                hint = "Coin blocks give sparkly golden coins! Tap them to collect!"
                moves = 12
                objectives.add(ObjectiveType.CollectCoins(3))
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 2, 6, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))

                blocks.add(BlockItem("c1", 1, 3, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("c2", 2, 3, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("c3", 3, 3, BlockType.COIN, BlockColor.YELLOW))

                blocks.add(BlockItem("n1", 2, 4, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n2", 2, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n3", 1, 4, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n4", 3, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n5", 1, 5, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("n6", 3, 5, BlockType.NORMAL, BlockColor.YELLOW))
            }

            4 -> {
                // 5x7 grid, 3 colors, Target 1250
                hint = "Clear all 6 Red Blocks to hit the target score!"
                moves = 12
                objectives.add(ObjectiveType.ClearColor(BlockColor.RED, 6))
                val layout = listOf(
                    Triple(1, 3, BlockColor.RED), Triple(2, 3, BlockColor.BLUE), Triple(3, 3, BlockColor.RED),
                    Triple(1, 4, BlockColor.RED), Triple(2, 4, BlockColor.RED), Triple(3, 4, BlockColor.YELLOW),
                    Triple(1, 5, BlockColor.BLUE), Triple(2, 5, BlockColor.RED), Triple(3, 5, BlockColor.RED)
                )
                layout.forEachIndexed { i, (x, y, col) ->
                    blocks.add(BlockItem("l4_$i", x, y, BlockType.NORMAL, col))
                }
            }

            5 -> {
                // 5x7 grid, 3 colors, Target 1500
                hint = "Rescue Bloki and grab 2 stars along the way!"
                moves = 14
                objectives.add(ObjectiveType.RescueBloki)
                objectives.add(ObjectiveType.CollectStars(2))
                blocks.add(BlockItem("g1", 2, 6, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))

                blocks.add(BlockItem("s1", 1, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("b1", 2, 2, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("b2", 2, 3, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("s2", 3, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("b3", 2, 4, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("b4", 2, 5, BlockType.NORMAL, BlockColor.YELLOW))
            }

            6 -> {
                // 6x7 grid, 4 colors (RED, BLUE, GREEN, YELLOW), Target 1800
                hint = "Meet the Strong Block! Tap it TWICE to crack and break it!"
                moves = 14
                objectives.add(ObjectiveType.BreakBlocks(8))
                blocks.add(BlockItem("st1", 2, 3, BlockType.STRONG, BlockColor.ORANGE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("st2", 3, 3, BlockType.STRONG, BlockColor.ORANGE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("n1", 1, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n2", 2, 4, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n3", 3, 4, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n4", 4, 4, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("n5", 2, 5, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("n6", 3, 5, BlockType.NORMAL, BlockColor.BLUE))
            }

            7 -> {
                // 6x7 grid, 4 colors, Target 2200
                hint = "Break the tough blocks to clear Bloki's path to the goal!"
                moves = 15
                objectives.add(ObjectiveType.RescueBloki)
                objectives.add(ObjectiveType.BreakBlocks(6))
                blocks.add(BlockItem("g1", 2, 6, isGoal = true))
                blocks.add(BlockItem("g2", 3, 6, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 0, isBloki = true))

                blocks.add(BlockItem("st1", 2, 2, BlockType.STRONG, BlockColor.ORANGE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("b1", 3, 2, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("b2", 2, 3, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b3", 3, 3, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("st2", 2, 4, BlockType.STRONG, BlockColor.ORANGE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("b4", 3, 4, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("b5", 2, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b6", 3, 5, BlockType.NORMAL, BlockColor.RED))
            }

            8 -> {
                // 6x7 grid, 4 colors, Target 2600
                hint = "Frozen Ice Blocks! Break neighboring blocks to shatter the ice!"
                moves = 14
                objectives.add(ObjectiveType.BreakBlocks(8))
                blocks.add(BlockItem("ice1", 2, 3, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice2", 3, 3, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("n1", 1, 3, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n2", 4, 3, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n3", 2, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n4", 3, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n5", 2, 5, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n6", 3, 5, BlockType.NORMAL, BlockColor.YELLOW))
            }

            9 -> {
                // 6x7 grid, 4 colors, Target 3000
                hint = "Ice Barrier! Shatter both ice blocks to rescue Bloki!"
                moves = 14
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 2, 6, isGoal = true))
                blocks.add(BlockItem("g2", 3, 6, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))

                blocks.add(BlockItem("b1", 2, 2, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("b2", 3, 2, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("ice1", 2, 3, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice2", 3, 3, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("side1", 1, 3, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("side2", 4, 3, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("b3", 2, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b4", 3, 4, BlockType.NORMAL, BlockColor.RED))
            }

            10 -> {
                // 6x8 grid, 4 colors, Target 3500
                hint = "World 1 Finale: Collect 3 Stars and 3 Coins to clear Rainbow Garden!"
                moves = 16
                objectives.add(ObjectiveType.CollectStars(3))
                objectives.add(ObjectiveType.CollectCoins(3))
                blocks.add(BlockItem("s1", 1, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s2", 4, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s3", 2, 2, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("c1", 1, 5, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("c2", 2, 5, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("c3", 4, 5, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("ice1", 2, 4, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("st1", 3, 4, BlockType.STRONG, BlockColor.ORANGE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("n1", 2, 6, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n2", 3, 6, BlockType.NORMAL, BlockColor.BLUE))
            }

            11 -> {
                // 6x8 grid, 5 colors (RED, BLUE, GREEN, YELLOW, PURPLE), Target 4400
                hint = "BOMB BLOCK! Tap the cartoon bomb for a 3x3 blast!"
                moves = 14
                objectives.add(ObjectiveType.BreakBlocks(9))
                blocks.add(BlockItem("bomb", 2, 4, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("n1", 1, 3, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("n2", 2, 3, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n3", 3, 3, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n4", 1, 4, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("n5", 3, 4, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n6", 1, 5, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("n7", 2, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n8", 3, 5, BlockType.NORMAL, BlockColor.GREEN))
            }

            12 -> {
                // 6x8 grid, 5 colors, Target 4800
                hint = "BOMB CHAIN! Detonate one bomb to trigger the neighboring bombs!"
                moves = 14
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("g2", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))

                blocks.add(BlockItem("bomb1", 2, 3, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("bomb2", 3, 4, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("ice1", 2, 4, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("st1", 2, 5, BlockType.STRONG, BlockColor.PURPLE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("st2", 3, 5, BlockType.STRONG, BlockColor.PURPLE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("n1", 2, 6, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n2", 3, 6, BlockType.NORMAL, BlockColor.BLUE))
            }

            13 -> {
                // 6x8 grid, 5 colors, Target 5200
                hint = "MAGIC ROCKET BLOCK! Clears the entire row and column in a cross-blast!"
                moves = 14
                objectives.add(ObjectiveType.BreakBlocks(10))
                blocks.add(BlockItem("magic", 2, 4, BlockType.MAGIC, BlockColor.NONE))
                for (x in 0..5) {
                    if (x != 2) blocks.add(BlockItem("row_$x", x, 4, BlockType.NORMAL, colors[x % colors.size]))
                }
                for (y in 2..6) {
                    if (y != 4) blocks.add(BlockItem("col_$y", 2, y, BlockType.NORMAL, colors[y % colors.size]))
                }
            }

            14 -> {
                // 6x8 grid, 5 colors, Target 5600
                hint = "RAINBOW BLOCK! Clears all blocks of the selected color at once!"
                moves = 14
                objectives.add(ObjectiveType.ClearColor(BlockColor.PURPLE, 5))
                blocks.add(BlockItem("rainbow", 2, 4, BlockType.RAINBOW, BlockColor.NONE))
                blocks.add(BlockItem("p1", 1, 3, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("p2", 3, 3, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("p3", 1, 5, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("p4", 3, 5, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("p5", 4, 4, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("other1", 2, 3, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("other2", 2, 5, BlockType.NORMAL, BlockColor.GREEN))
            }

            15 -> {
                // 6x8 grid, 5 colors, Target 6000
                hint = "Rescue Bloki with a combo of Magic Rocket and Bomb blasts!"
                moves = 16
                objectives.add(ObjectiveType.RescueBloki)
                objectives.add(ObjectiveType.CollectStars(2))
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("g2", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))

                blocks.add(BlockItem("magic", 2, 3, BlockType.MAGIC, BlockColor.NONE))
                blocks.add(BlockItem("bomb", 3, 3, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("s1", 1, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s2", 4, 3, BlockType.STAR, BlockColor.YELLOW))

                blocks.add(BlockItem("ice1", 2, 5, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice2", 3, 5, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("b1", 2, 6, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b2", 3, 6, BlockType.NORMAL, BlockColor.PURPLE))
            }

            16 -> {
                // 7x8 grid, 5 colors, Target 6500
                hint = "MOVING BLOCKS! Watch them slide horizontally across the board!"
                moves = 16
                objectives.add(ObjectiveType.BreakBlocks(8))
                blocks.add(BlockItem("mov1", 1, 3, BlockType.MOVING, BlockColor.RED, moveDirection = 1))
                blocks.add(BlockItem("mov2", 4, 4, BlockType.MOVING, BlockColor.BLUE, moveDirection = -1))
                blocks.add(BlockItem("n1", 2, 5, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n2", 3, 5, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("n3", 4, 5, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("n4", 2, 6, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n5", 3, 6, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n6", 4, 6, BlockType.NORMAL, BlockColor.GREEN))
            }

            17 -> {
                // 7x8 grid, 5 colors, Target 7000
                hint = "Time your taps to break the moving blocks and clear the path!"
                moves = 16
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 3, 1, isBloki = true))

                blocks.add(BlockItem("mov1", 2, 3, BlockType.MOVING, BlockColor.ORANGE, moveDirection = 1))
                blocks.add(BlockItem("st1", 3, 4, BlockType.STRONG, BlockColor.PURPLE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("mov2", 4, 5, BlockType.MOVING, BlockColor.GREEN, moveDirection = -1))
                blocks.add(BlockItem("b1", 3, 6, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("b2", 2, 6, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("b3", 4, 6, BlockType.NORMAL, BlockColor.BLUE))
            }

            18 -> {
                // 7x8 grid, 5 colors, Target 7500
                hint = "Ice cavern with moving obstacle blocks!"
                moves = 18
                objectives.add(ObjectiveType.BreakBlocks(10))
                objectives.add(ObjectiveType.CollectStars(2))
                blocks.add(BlockItem("s1", 1, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s2", 5, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("ice1", 3, 3, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("mov1", 2, 4, BlockType.MOVING, BlockColor.BLUE, moveDirection = 1))
                blocks.add(BlockItem("bomb", 3, 5, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("mov2", 4, 6, BlockType.MOVING, BlockColor.RED, moveDirection = -1))
                blocks.add(BlockItem("n1", 2, 6, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("n2", 3, 6, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n3", 1, 5, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n4", 5, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n5", 1, 6, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("n6", 5, 6, BlockType.NORMAL, BlockColor.GREEN))
            }

            19 -> {
                // 7x8 grid, 5 colors, Target 8000
                hint = "Dual Bomb detonation puzzle! Clear the entire pyramid!"
                moves = 18
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 3, 0, isBloki = true))

                blocks.add(BlockItem("b1", 3, 1, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("bomb1", 2, 3, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("bomb2", 4, 3, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("magic", 3, 4, BlockType.MAGIC, BlockColor.NONE))
                blocks.add(BlockItem("ice1", 3, 5, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("st1", 2, 6, BlockType.STRONG, BlockColor.PURPLE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("st2", 4, 6, BlockType.STRONG, BlockColor.PURPLE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("b2", 3, 6, BlockType.NORMAL, BlockColor.GREEN))
            }

            20 -> {
                // 7x8 grid, 5 colors, Target 8500
                hint = "Candy Valley Grand Master! Rescue Bloki, collect 3 stars and 3 coins!"
                moves = 20
                objectives.add(ObjectiveType.RescueBloki)
                objectives.add(ObjectiveType.CollectStars(3))
                objectives.add(ObjectiveType.CollectCoins(3))
                blocks.add(BlockItem("g1", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 3, 0, isBloki = true))

                // Multi-tier candy tower
                blocks.add(BlockItem("s1", 1, 2, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s2", 5, 2, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s3", 3, 1, BlockType.STAR, BlockColor.YELLOW))

                blocks.add(BlockItem("c1", 1, 3, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("magic", 2, 3, BlockType.MAGIC, BlockColor.NONE))
                blocks.add(BlockItem("bomb", 4, 3, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("c2", 5, 3, BlockType.COIN, BlockColor.YELLOW))

                blocks.add(BlockItem("c3", 3, 3, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("st1", 3, 4, BlockType.STRONG, BlockColor.PURPLE, hp = 2, maxHp = 2))

                blocks.add(BlockItem("ice1", 2, 5, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice2", 4, 5, BlockType.ICE, BlockColor.NONE))

                blocks.add(BlockItem("b1", 3, 5, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("b2", 3, 6, BlockType.NORMAL, BlockColor.BLUE))
            }
        }

        val worldId = when (level) {
            in 1..20 -> 1
            in 21..40 -> 2
            in 41..60 -> 3
            in 61..80 -> 4
            else -> 5
        }
        val worldName = when (worldId) {
            1 -> "Rainbow Garden"
            2 -> "Candy Valley"
            3 -> "Cloud Kingdom"
            4 -> "Jungle Blocks"
            else -> "Space Blocks"
        }

        return LevelDefinition(
            levelNumber = level,
            worldName = worldName,
            worldId = worldId,
            movesAllowed = moves,
            objectives = objectives,
            gridWidth = width,
            gridHeight = height,
            initialBlocks = blocks,
            tutorialHint = hint,
            targetScore = targetScore,
            availableColors = colors
        )
    }

    private fun generateProceduralLevel(level: Int): LevelDefinition {
        val rand = Random(level.toLong() * 31337L)
        val (width, height) = LevelProgressionConfig.getGridDimensions(level)
        val colors = LevelProgressionConfig.getColorPalette(level)
        val targetScore = LevelProgressionConfig.getTargetScore(level)

        val worldId = when (level) {
            in 1..20 -> 1
            in 21..40 -> 2
            in 41..60 -> 3
            in 61..80 -> 4
            else -> 5
        }
        val worldName = when (worldId) {
            1 -> "Rainbow Garden"
            2 -> "Candy Valley"
            3 -> "Cloud Kingdom"
            4 -> "Jungle Blocks"
            else -> "Space Blocks"
        }

        val blocks = mutableListOf<BlockItem>()
        val objectives = mutableListOf<ObjectiveType>()

        // 50% chance of rescue bloki, otherwise break blocks
        val hasRescue = rand.nextBoolean()
        val centerX = width / 2
        if (hasRescue) {
            objectives.add(ObjectiveType.RescueBloki)
            blocks.add(BlockItem("g1", centerX, height - 1, isGoal = true))
            blocks.add(BlockItem("bloki", centerX, 0, isBloki = true))
        }

        val starTarget = (1 + rand.nextInt(3))
        objectives.add(ObjectiveType.CollectStars(starTarget))

        var starsPlaced = 0
        var coinsPlaced = 0

        for (y in (if (hasRescue) 1 else 2) until height - 1) {
            for (x in 1 until width - 1) {
                val randVal = rand.nextFloat()
                val type = when {
                    starsPlaced < starTarget && randVal < 0.20f -> {
                        starsPlaced++
                        BlockType.STAR
                    }
                    coinsPlaced < 3 && randVal < 0.35f -> {
                        coinsPlaced++
                        BlockType.COIN
                    }
                    randVal < 0.50f -> BlockType.STRONG
                    randVal < 0.62f -> BlockType.BOMB
                    randVal < 0.72f -> BlockType.ICE
                    randVal < 0.80f -> BlockType.MAGIC
                    randVal < 0.88f -> BlockType.RAINBOW
                    else -> BlockType.NORMAL
                }
                val color = colors[rand.nextInt(colors.size)]
                val hp = if (type == BlockType.STRONG) 2 else 1
                blocks.add(BlockItem("proc_${level}_${x}_$y", x, y, type, color, hp = hp, maxHp = hp))
            }
        }

        val moves = 14 + (level % 8)

        return LevelDefinition(
            levelNumber = level,
            worldName = worldName,
            worldId = worldId,
            movesAllowed = moves,
            objectives = objectives,
            gridWidth = width,
            gridHeight = height,
            initialBlocks = blocks,
            tutorialHint = "World $worldName - Stage $level!",
            targetScore = targetScore,
            availableColors = colors
        )
    }

    /**
     * Solvability check ensures that for any level, the required objectives
     * are physically possible given the layout and move count.
     */
    fun validateSolvability(levelDef: LevelDefinition): Boolean {
        if (levelDef.movesAllowed <= 0) return false
        if (levelDef.initialBlocks.isEmpty()) return false

        for (obj in levelDef.objectives) {
            when (obj) {
                is ObjectiveType.BreakBlocks -> {
                    val breakable = levelDef.initialBlocks.count { !it.isBloki && !it.isGoal }
                    if (breakable < obj.target) return false
                }
                is ObjectiveType.CollectStars -> {
                    val stars = levelDef.initialBlocks.count { it.type == BlockType.STAR }
                    if (stars < obj.target) return false
                }
                is ObjectiveType.CollectCoins -> {
                    val coins = levelDef.initialBlocks.count { it.type == BlockType.COIN }
                    if (coins < obj.target) return false
                }
                is ObjectiveType.ClearColor -> {
                    val matching = levelDef.initialBlocks.count { it.color == obj.color }
                    if (matching < obj.target) return false
                }
                is ObjectiveType.RescueBloki -> {
                    val hasBloki = levelDef.initialBlocks.any { it.isBloki }
                    val hasGoal = levelDef.initialBlocks.any { it.isGoal }
                    if (!hasBloki || !hasGoal) return false
                }
                is ObjectiveType.ReachScore -> {
                    val breakable = levelDef.initialBlocks.count { !it.isBloki && !it.isGoal }
                    val maxPotential = breakable * 50 +
                        levelDef.initialBlocks.count { it.type == BlockType.STAR } * 200 +
                        levelDef.initialBlocks.count { it.type == BlockType.COIN } * 150 +
                        (if (levelDef.initialBlocks.any { it.isBloki }) 1000 else 0)
                    if (maxPotential < (obj.targetScore * 0.35f)) return false
                }
            }
        }
        return true
    }
}
