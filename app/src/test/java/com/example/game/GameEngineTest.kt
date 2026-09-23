package com.example.game

import com.example.game.engine.GameEngine
import com.example.game.engine.GameStatus
import com.example.game.level.LevelCatalog
import com.example.game.model.BlockType
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

        val soundEvents = mutableListOf<String>()
        val updated = engine.tapBlock(block!!.x, block.y) { soundEvents.add(it) }

        // Block should have broken and moves decremented by 1
        assertEquals(9, updated.movesRemaining)
        assertTrue(soundEvents.contains("break"))
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
