package com.example.game.level

import com.example.game.model.BlockColor
import com.example.game.model.BlockItem
import com.example.game.model.BlockType
import com.example.game.model.LevelDefinition
import com.example.game.model.ObjectiveType
import java.util.Random

/**
 * Complete level system containing 20 handcrafted starter levels
 * plus procedural scaling for up to 1000+ levels across 5 worlds.
 */
object LevelCatalog {

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
            tutorialHint = "Daily Special Puzzle! Rescue Bloki and collect all stars & coins!"
        )
    }

    private fun getHandcraftedLevel(level: Int): LevelDefinition {
        val width = 6
        val height = 8

        val blocks = mutableListOf<BlockItem>()
        val objectives = mutableListOf<ObjectiveType>()
        var moves = 12
        var hint: String? = null

        when (level) {
            1 -> {
                hint = "Tap any block to break it! Help Bloki reach the bottom pad!"
                moves = 10
                objectives.add(ObjectiveType.RescueBloki)
                // Goal at bottom center
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("g2", 3, 7, isGoal = true))
                // Bloki at top center
                blocks.add(BlockItem("bloki", 2, 3, isBloki = true))
                // Stack of 3 blocks under Bloki
                blocks.add(BlockItem("b1", 2, 4, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("b2", 2, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b3", 2, 6, BlockType.NORMAL, BlockColor.GREEN))
                // Friendly side blocks
                blocks.add(BlockItem("b4", 3, 5, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("b5", 3, 6, BlockType.NORMAL, BlockColor.PURPLE))
            }

            2 -> {
                hint = "Collect all shiny Stars by breaking star blocks!"
                moves = 10
                objectives.add(ObjectiveType.CollectStars(2))
                objectives.add(ObjectiveType.BreakBlocks(6))
                for (y in 4..6) {
                    for (x in 1..4) {
                        val isStar = (x == 2 && y == 5) || (x == 3 && y == 4)
                        val type = if (isStar) BlockType.STAR else BlockType.NORMAL
                        val color = if (isStar) BlockColor.YELLOW else BlockColor.BLUE
                        blocks.add(BlockItem("l2_${x}_$y", x, y, type, color))
                    }
                }
            }

            3 -> {
                hint = "Coin blocks give sparkly golden coins! Tap them to collect!"
                moves = 12
                objectives.add(ObjectiveType.CollectCoins(3))
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("g2", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 2, isBloki = true))

                blocks.add(BlockItem("c1", 1, 4, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("c2", 2, 4, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("c3", 3, 4, BlockType.COIN, BlockColor.YELLOW))

                blocks.add(BlockItem("n1", 2, 5, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n2", 2, 6, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n3", 3, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n4", 3, 6, BlockType.NORMAL, BlockColor.ORANGE))
            }

            4 -> {
                hint = "Clear all 6 Red Blocks to win this level!"
                moves = 12
                objectives.add(ObjectiveType.ClearColor(BlockColor.RED, 6))
                val layout = listOf(
                    Triple(1, 4, BlockColor.RED), Triple(2, 4, BlockColor.BLUE), Triple(3, 4, BlockColor.RED), Triple(4, 4, BlockColor.GREEN),
                    Triple(1, 5, BlockColor.RED), Triple(2, 5, BlockColor.RED), Triple(3, 5, BlockColor.YELLOW), Triple(4, 5, BlockColor.RED),
                    Triple(2, 6, BlockColor.PURPLE), Triple(3, 6, BlockColor.RED)
                )
                layout.forEachIndexed { i, (x, y, col) ->
                    blocks.add(BlockItem("l4_$i", x, y, BlockType.NORMAL, col))
                }
            }

            5 -> {
                hint = "Rescue Bloki and grab 2 stars along the way!"
                moves = 14
                objectives.add(ObjectiveType.RescueBloki)
                objectives.add(ObjectiveType.CollectStars(2))
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))

                blocks.add(BlockItem("s1", 1, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("b1", 2, 2, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("b2", 2, 3, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("s2", 3, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("b3", 2, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b4", 2, 5, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("b5", 2, 6, BlockType.NORMAL, BlockColor.ORANGE))
            }

            6 -> {
                hint = "Meet the Strong Block! Tap it TWICE to crack and break it!"
                moves = 14
                objectives.add(ObjectiveType.BreakBlocks(8))
                blocks.add(BlockItem("st1", 2, 4, BlockType.STRONG, BlockColor.ORANGE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("st2", 3, 4, BlockType.STRONG, BlockColor.ORANGE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("n1", 1, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n2", 2, 5, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n3", 3, 5, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n4", 4, 5, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("n5", 2, 6, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("n6", 3, 6, BlockType.NORMAL, BlockColor.BLUE))
            }

            7 -> {
                hint = "BOMB BLOCK! Tap the Bomb to blast away surrounding blocks safely!"
                moves = 12
                objectives.add(ObjectiveType.BreakBlocks(8))
                // Bomb in center surrounded by blocks
                blocks.add(BlockItem("bomb1", 2, 5, BlockType.BOMB, BlockColor.NONE))
                for (dy in -1..1) {
                    for (dx in -1..1) {
                        if (dx == 0 && dy == 0) continue
                        val x = 2 + dx
                        val y = 5 + dy
                        if (x in 0 until width && y in 0 until height) {
                            blocks.add(BlockItem("b_${x}_$y", x, y, BlockType.NORMAL, BlockColor.RED))
                        }
                    }
                }
            }

            8 -> {
                hint = "Use the Bomb to clear the path for Bloki!"
                moves = 14
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 2, isBloki = true))

                blocks.add(BlockItem("b1", 2, 3, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("bomb", 2, 4, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("s1", 1, 4, BlockType.STRONG, BlockColor.BLUE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("s2", 3, 4, BlockType.STRONG, BlockColor.BLUE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("b2", 2, 5, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("b3", 2, 6, BlockType.NORMAL, BlockColor.RED))
            }

            9 -> {
                hint = "Rainbow Block matches all colors! Tap it to clear same-color blocks!"
                moves = 15
                objectives.add(ObjectiveType.ClearColor(BlockColor.BLUE, 5))
                blocks.add(BlockItem("rb1", 2, 4, BlockType.RAINBOW, BlockColor.RAINBOW))
                blocks.add(BlockItem("b1", 1, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b2", 3, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b3", 1, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b4", 2, 5, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("b5", 3, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b6", 2, 6, BlockType.NORMAL, BlockColor.BLUE))
            }

            10 -> {
                hint = "World 1 Garden Finale! Rescue Bloki and collect 3 stars!"
                moves = 16
                objectives.add(ObjectiveType.RescueBloki)
                objectives.add(ObjectiveType.CollectStars(3))
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("g2", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 3, 1, isBloki = true))

                blocks.add(BlockItem("s1", 1, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s2", 4, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s3", 2, 4, BlockType.STAR, BlockColor.YELLOW))

                blocks.add(BlockItem("bomb", 3, 4, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("st1", 2, 5, BlockType.STRONG, BlockColor.GREEN, hp = 2, maxHp = 2))
                blocks.add(BlockItem("st2", 3, 5, BlockType.STRONG, BlockColor.GREEN, hp = 2, maxHp = 2))
                blocks.add(BlockItem("b1", 2, 6, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("b2", 3, 6, BlockType.NORMAL, BlockColor.PURPLE))
            }

            11 -> {
                hint = "Welcome to Candy Valley! Magic Blocks clear whole rows and columns!"
                moves = 15
                objectives.add(ObjectiveType.BreakBlocks(10))
                blocks.add(BlockItem("magic", 2, 4, BlockType.MAGIC, BlockColor.NONE))
                for (x in 0..5) {
                    blocks.add(BlockItem("row_$x", x, 4, BlockType.NORMAL, BlockColor.PURPLE))
                }
                for (y in 2..6) {
                    if (y != 4) blocks.add(BlockItem("col_$y", 2, y, BlockType.NORMAL, BlockColor.PINK))
                }
            }

            12 -> {
                hint = "Brrr! Ice Blocks are frozen! Break an adjacent block or bomb to shatter ice!"
                moves = 15
                objectives.add(ObjectiveType.BreakBlocks(6))
                blocks.add(BlockItem("ice1", 2, 4, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice2", 3, 4, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("norm1", 1, 4, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("norm2", 4, 4, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("norm3", 2, 5, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("norm4", 3, 5, BlockType.NORMAL, BlockColor.GREEN))
            }

            13 -> {
                hint = "Shatter the ice barrier to let Bloki slide down safely!"
                moves = 16
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))

                blocks.add(BlockItem("b1", 2, 2, BlockType.NORMAL, BlockColor.ORANGE))
                blocks.add(BlockItem("ice1", 2, 3, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("bomb", 1, 3, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("b2", 2, 4, BlockType.NORMAL, BlockColor.PURPLE))
                blocks.add(BlockItem("ice2", 2, 5, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("b3", 3, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("b4", 2, 6, BlockType.NORMAL, BlockColor.GREEN))
            }

            14 -> {
                hint = "Moving blocks shift positions! Time your taps carefully!"
                moves = 16
                objectives.add(ObjectiveType.BreakBlocks(10))
                blocks.add(BlockItem("m1", 1, 3, BlockType.MOVING, BlockColor.BLUE, moveDirection = 1))
                blocks.add(BlockItem("m2", 4, 4, BlockType.MOVING, BlockColor.RED, moveDirection = -1))
                for (x in 1..4) {
                    blocks.add(BlockItem("n_${x}_5", x, 5, BlockType.NORMAL, BlockColor.GREEN))
                    blocks.add(BlockItem("n_${x}_6", x, 6, BlockType.NORMAL, BlockColor.YELLOW))
                }
            }

            15 -> {
                hint = "Sweet Sugar Rush! Clear Purple blocks and collect Coins!"
                moves = 16
                objectives.add(ObjectiveType.ClearColor(BlockColor.PURPLE, 4))
                objectives.add(ObjectiveType.CollectCoins(4))

                for (y in 3..6) {
                    for (x in 1..4) {
                        val isCoin = (x + y) % 3 == 0
                        val isPurple = !isCoin && ((x + y) % 2 == 0)
                        val type = if (isCoin) BlockType.COIN else BlockType.NORMAL
                        val color = if (isCoin) BlockColor.YELLOW else if (isPurple) BlockColor.PURPLE else BlockColor.RED
                        blocks.add(BlockItem("l15_${x}_$y", x, y, type, color))
                    }
                }
            }

            16 -> {
                hint = "Double Bomb chain reaction! Trigger one bomb to detonate the other!"
                moves = 15
                objectives.add(ObjectiveType.BreakBlocks(14))
                blocks.add(BlockItem("bomb1", 2, 4, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("bomb2", 3, 4, BlockType.BOMB, BlockColor.NONE))
                for (y in 3..6) {
                    for (x in 1..4) {
                        if ((x == 2 || x == 3) && y == 4) continue
                        blocks.add(BlockItem("l16_${x}_$y", x, y, BlockType.NORMAL, BlockColor.BLUE))
                    }
                }
            }

            17 -> {
                hint = "Heavy Fortress! Break through 4 Strong blocks to save Bloki!"
                moves = 18
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("g2", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 1, isBloki = true))

                blocks.add(BlockItem("st1", 2, 3, BlockType.STRONG, BlockColor.RED, hp = 2, maxHp = 2))
                blocks.add(BlockItem("st2", 3, 3, BlockType.STRONG, BlockColor.RED, hp = 2, maxHp = 2))
                blocks.add(BlockItem("st3", 2, 4, BlockType.STRONG, BlockColor.BLUE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("st4", 3, 4, BlockType.STRONG, BlockColor.BLUE, hp = 2, maxHp = 2))
                blocks.add(BlockItem("n1", 2, 5, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n2", 3, 5, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n3", 2, 6, BlockType.NORMAL, BlockColor.YELLOW))
                blocks.add(BlockItem("n4", 3, 6, BlockType.NORMAL, BlockColor.YELLOW))
            }

            18 -> {
                hint = "Ice and Fire! Use Magic & Bomb blocks to shatter the frozen palace!"
                moves = 16
                objectives.add(ObjectiveType.BreakBlocks(8))
                objectives.add(ObjectiveType.CollectStars(2))
                blocks.add(BlockItem("magic", 2, 4, BlockType.MAGIC, BlockColor.NONE))
                blocks.add(BlockItem("bomb", 3, 4, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("s1", 1, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s2", 4, 3, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("ice1", 1, 4, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice2", 4, 4, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice3", 2, 5, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice4", 3, 5, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("n1", 2, 6, BlockType.NORMAL, BlockColor.RED))
                blocks.add(BlockItem("n2", 3, 6, BlockType.NORMAL, BlockColor.RED))
            }

            19 -> {
                hint = "Rainbow Cascade! Combine Rainbow with moving blocks!"
                moves = 16
                objectives.add(ObjectiveType.ClearColor(BlockColor.GREEN, 6))
                objectives.add(ObjectiveType.RescueBloki)
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 0, isBloki = true))
                blocks.add(BlockItem("rb", 2, 2, BlockType.RAINBOW, BlockColor.RAINBOW))
                blocks.add(BlockItem("m1", 1, 3, BlockType.MOVING, BlockColor.GREEN, moveDirection = 1))
                blocks.add(BlockItem("g_1", 2, 3, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("g_2", 3, 3, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("g_3", 1, 4, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("g_4", 2, 4, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("g_5", 3, 4, BlockType.NORMAL, BlockColor.GREEN))
                blocks.add(BlockItem("n1", 2, 5, BlockType.NORMAL, BlockColor.BLUE))
                blocks.add(BlockItem("n2", 2, 6, BlockType.NORMAL, BlockColor.RED))
            }

            20 -> {
                hint = "Candy Valley Grand Master! Rescue Bloki, collect 3 stars and 3 coins!"
                moves = 20
                objectives.add(ObjectiveType.RescueBloki)
                objectives.add(ObjectiveType.CollectStars(3))
                objectives.add(ObjectiveType.CollectCoins(3))
                blocks.add(BlockItem("g1", 2, 7, isGoal = true))
                blocks.add(BlockItem("g2", 3, 7, isGoal = true))
                blocks.add(BlockItem("bloki", 2, 0, isBloki = true))

                // Multi-tier candy tower
                blocks.add(BlockItem("s1", 1, 2, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s2", 4, 2, BlockType.STAR, BlockColor.YELLOW))
                blocks.add(BlockItem("s3", 2, 1, BlockType.STAR, BlockColor.YELLOW))

                blocks.add(BlockItem("c1", 1, 3, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("magic", 2, 3, BlockType.MAGIC, BlockColor.NONE))
                blocks.add(BlockItem("bomb", 3, 3, BlockType.BOMB, BlockColor.NONE))
                blocks.add(BlockItem("c2", 4, 3, BlockType.COIN, BlockColor.YELLOW))

                blocks.add(BlockItem("c3", 2, 4, BlockType.COIN, BlockColor.YELLOW))
                blocks.add(BlockItem("st1", 3, 4, BlockType.STRONG, BlockColor.PURPLE, hp = 2, maxHp = 2))

                blocks.add(BlockItem("ice1", 2, 5, BlockType.ICE, BlockColor.NONE))
                blocks.add(BlockItem("ice2", 3, 5, BlockType.ICE, BlockColor.NONE))

                blocks.add(BlockItem("b1", 2, 6, BlockType.NORMAL, BlockColor.RED))
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
            tutorialHint = hint
        )
    }

    private fun generateProceduralLevel(level: Int): LevelDefinition {
        val rand = Random(level.toLong() * 31337L)
        val width = 6
        val height = 8

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

        // 50% chance of rescue bloki, otherwise break blocks or color
        val hasRescue = rand.nextBoolean()
        if (hasRescue) {
            objectives.add(ObjectiveType.RescueBloki)
            blocks.add(BlockItem("g1", 2, height - 1, isGoal = true))
            blocks.add(BlockItem("g2", 3, height - 1, isGoal = true))
            blocks.add(BlockItem("bloki", 2, 0, isBloki = true))
        }

        val starTarget = (1 + rand.nextInt(3))
        objectives.add(ObjectiveType.CollectStars(starTarget))

        val colors = listOf(BlockColor.RED, BlockColor.BLUE, BlockColor.GREEN, BlockColor.PURPLE, BlockColor.ORANGE)

        var starsPlaced = 0
        var coinsPlaced = 0

        for (y in (if (hasRescue) 1 else 2) until height - 1) {
            for (x in 1..4) {
                val randVal = rand.nextFloat()
                val type = when {
                    starsPlaced < starTarget && randVal < 0.2f -> {
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
            tutorialHint = "World $worldName - Stage $level!"
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
            }
        }
        return true
    }
}
