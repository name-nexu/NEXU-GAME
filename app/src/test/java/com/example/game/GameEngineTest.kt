package com.example.game

import com.example.game.engine.GameEngine
import com.example.game.engine.GameStatus
import com.example.game.engine.ParticleStyle
import com.example.game.level.LevelCatalog
import com.example.game.model.BlockColor
import com.example.game.model.BlockItem
import com.example.game.model.BlockType
import com.example.game.model.LevelDefinition
import com.example.game.model.ObjectiveType
import com.example.game.model.PowerUpType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameEngineTest {

    @Test
    fun level1_initialization_and_break_block() {
        val level1 = LevelCatalog.getLevel(1)
        val engine = GameEngine(level1)

        assertEquals(GameStatus.PLAYING, engine.state.status)
        assertEquals(10, engine.state.movesRemaining)
        assertEquals(1, engine.state.objectives.size)

        // Find a breakable normal block to tap
        val block = engine.state.blocks.firstOrNull { it.type == BlockType.NORMAL && !it.isGoal && !it.isBloki }
        assertNotNull(block)
        val targetBlock = requireNotNull(block)

        val soundEvents = mutableListOf<String>()
        val updated = engine.tapBlock(targetBlock.x, targetBlock.y) { soundEvents.add(it) }

        // Block should have broken and moves decremented by 1
        assertEquals(9, updated.movesRemaining)
        assertTrue(soundEvents.contains("break"))
    }

    @Test
    fun gravity_drops_unsupported_blocks_downward() {
        // Create custom level with 2 blocks stacked vertically: top at (2, 4), bottom at (2, 5)
        val customLevel = LevelDefinition(
            levelNumber = 901,
            worldName = "Physics Test",
            worldId = 1,
            movesAllowed = 5,
            objectives = listOf(ObjectiveType.BreakBlocks(1)),
            gridWidth = 5,
            gridHeight = 6,
            initialBlocks = listOf(
                BlockItem("top", 2, 4, BlockType.NORMAL, BlockColor.RED),
                BlockItem("bottom", 2, 5, BlockType.NORMAL, BlockColor.BLUE)
            )
        )
        val engine = GameEngine(customLevel)

        // Tap the bottom block (x=2, y=5)
        val updated = engine.tapBlock(2, 5) {}

        // Bottom block is broken, top block should fall down to lowest available Y = gridHeight - 1 = 5
        assertEquals(1, updated.blocks.size)
        val remaining = updated.blocks.first()
        assertEquals("top", remaining.id)
        assertEquals(2, remaining.x)
        assertEquals(5, remaining.y) // Dropped from y=4 to y=5!
    }

    @Test
    fun cluster_matching_breaks_connected_same_color_blocks() {
        // Two connected red blocks
        val customLevel = LevelDefinition(
            levelNumber = 902,
            worldName = "Cluster Test",
            worldId = 1,
            movesAllowed = 5,
            objectives = listOf(ObjectiveType.BreakBlocks(2)),
            gridWidth = 5,
            gridHeight = 6,
            initialBlocks = listOf(
                BlockItem("c1", 1, 4, BlockType.NORMAL, BlockColor.RED),
                BlockItem("c2", 2, 4, BlockType.NORMAL, BlockColor.RED),
                BlockItem("c3", 3, 4, BlockType.NORMAL, BlockColor.BLUE)
            )
        )
        val engine = GameEngine(customLevel)

        // Tap c1 (part of 2-block red cluster)
        val updated = engine.tapBlock(1, 4) {}

        // Both c1 and c2 should be broken together in 1 move
        assertEquals(4, updated.movesRemaining)
        assertEquals(2, updated.comboMultiplier)
        assertTrue(updated.blocks.none { it.id == "c1" || it.id == "c2" })
        assertTrue(updated.blocks.any { it.id == "c3" })
    }

    @Test
    fun tap_spawns_particles_with_physics_properties() {
        val level1 = LevelCatalog.getLevel(1)
        val engine = GameEngine(level1)

        val block = engine.state.blocks.first { it.type == BlockType.NORMAL && !it.isGoal && !it.isBloki }
        val updated = engine.tapBlock(block.x, block.y) {}

        assertTrue("Particles should be spawned", updated.particles.isNotEmpty())
        val p = updated.particles.first()
        assertTrue(p.size > 0f)
        assertTrue(p.gravity > 0f)
        assertTrue(p.style == ParticleStyle.CHIP || p.style == ParticleStyle.SPARKLE)
    }

    @Test
    fun bomb_explosion_and_chain_reaction() {
        // Bomb 1 next to Bomb 2
        val customLevel = LevelDefinition(
            levelNumber = 903,
            worldName = "Bomb Cascade Test",
            worldId = 1,
            movesAllowed = 5,
            objectives = listOf(ObjectiveType.BreakBlocks(3)),
            gridWidth = 5,
            gridHeight = 6,
            initialBlocks = listOf(
                BlockItem("bomb1", 2, 4, BlockType.BOMB, BlockColor.NONE),
                BlockItem("bomb2", 2, 5, BlockType.BOMB, BlockColor.NONE),
                BlockItem("victim", 3, 5, BlockType.NORMAL, BlockColor.BLUE)
            )
        )
        val engine = GameEngine(customLevel)
        val sounds = mutableListOf<String>()

        // Tap bomb1
        val updated = engine.tapBlock(2, 4) { sounds.add(it) }

        // Both bombs and victim should be exploded by chain reaction
        assertTrue(sounds.contains("bomb"))
        assertTrue(updated.blocks.none { it.id == "bomb1" || it.id == "bomb2" || it.id == "victim" })
        assertTrue(updated.particles.any { it.style == ParticleStyle.SHOCKWAVE })
    }

    @Test
    fun handcrafted_levels_1_to_20_are_solvable() {
        for (i in 1..20) {
            val level = LevelCatalog.getLevel(i)
            val isSolvable = LevelCatalog.validateSolvability(level)
            assertTrue("Level $i should be solvable", isSolvable)
        }
    }

    @Test
    fun hammer_powerup_breaks_target_block() {
        val level = LevelCatalog.getLevel(3)
        val engine = GameEngine(level)
        engine.selectPowerUp(PowerUpType.HAMMER)

        val target = engine.state.blocks.first { !it.isBloki && !it.isGoal }
        val sounds = mutableListOf<String>()
        val state = engine.tapBlock(target.x, target.y) { sounds.add(it) }

        assertTrue(sounds.contains("powerup"))
        assertTrue(state.blocks.none { it.id == target.id })
    }

    @Test
    fun procedural_generation_returns_valid_levels() {
        for (lvl in listOf(25, 50, 75, 100, 150)) {
            val levelDef = LevelCatalog.getLevel(lvl)
            assertEquals(lvl, levelDef.levelNumber)
            assertTrue(levelDef.initialBlocks.isNotEmpty())
            assertTrue(levelDef.objectives.isNotEmpty())
            assertTrue(levelDef.movesAllowed > 0)
        }
    }
}
