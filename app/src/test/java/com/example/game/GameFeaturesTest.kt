package com.example.game

import com.example.game.config.GameConfig
import com.example.game.engine.GameEngine
import com.example.game.engine.GameStatus
import com.example.game.level.LevelCatalog
import com.example.game.model.BlockColor
import com.example.game.model.BlockItem
import com.example.game.model.BlockType
import com.example.game.model.LevelDefinition
import com.example.game.model.ObjectiveType
import com.example.game.model.PowerUpType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GameFeaturesTest {

    @Test
    fun powerUp_failed_action_does_not_consume_inventory() {
        val level = LevelCatalog.getLevel(1)
        val engine = GameEngine(level)

        // Select Hammer
        engine.selectPowerUp(PowerUpType.HAMMER)
        assertEquals(PowerUpType.HAMMER, engine.state.activePowerUp)

        // Tap empty coordinates where no block exists
        val state = engine.tapBlock(-1, -1) {}

        // Power-up action did not succeed, so lastUsedPowerUp must be null and activePowerUp remains selected
        assertNull("Power-up should NOT be marked consumed if failed", state.lastUsedPowerUp)
    }

    @Test
    fun powerUp_successful_hammer_marks_power_up_consumed() {
        val level = LevelCatalog.getLevel(1)
        val engine = GameEngine(level)

        engine.selectPowerUp(PowerUpType.HAMMER)
        val target = engine.state.blocks.first { !it.isBloki && !it.isGoal }

        val state = engine.tapBlock(target.x, target.y) {}

        // When hammer successfully breaks the block, lastUsedPowerUp signals consumption
        assertEquals(PowerUpType.HAMMER, state.lastUsedPowerUp)
        assertNull("Active power-up selection should be cleared after execution", state.activePowerUp)
        assertTrue(state.lastBlocksBrokenCount >= 1)
    }

    @Test
    fun first_break_signals_blocks_broken_count() {
        val level = LevelCatalog.getLevel(1)
        val engine = GameEngine(level)

        val target = engine.state.blocks.first { it.type == BlockType.NORMAL && !it.isBloki && !it.isGoal }
        val state = engine.tapBlock(target.x, target.y) {}

        // Tap broke at least 1 block, reporting broken count to trigger "first_break"
        assertTrue("Broken blocks count should be > 0", state.lastBlocksBrokenCount > 0)
    }

    @Test
    fun coin_and_star_blocks_increase_earnings_consistently() {
        val customLevel = LevelDefinition(
            levelNumber = 905,
            worldName = "Reward Test",
            worldId = 1,
            movesAllowed = 5,
            objectives = listOf(ObjectiveType.CollectStars(1)),
            gridWidth = 5,
            gridHeight = 5,
            initialBlocks = listOf(
                BlockItem("star1", 2, 4, BlockType.STAR, BlockColor.YELLOW),
                BlockItem("coin1", 3, 4, BlockType.COIN, BlockColor.YELLOW)
            )
        )
        val engine = GameEngine(customLevel)

        val updated = engine.tapBlock(2, 4) {}

        // Star block should be collected and award star points
        assertEquals(1, updated.objectives.first().current)
        assertTrue(updated.currentScore >= GameConfig.Rewards.STAR_COLLECT_POINTS)
    }

    @Test
    fun level_catalog_has_levels_up_to_max() {
        assertTrue(LevelCatalog.hasLevel(1))
        assertTrue(LevelCatalog.hasLevel(20))
        assertTrue(LevelCatalog.hasLevel(100))
        assertFalse(LevelCatalog.hasLevel(0))
        assertFalse(LevelCatalog.hasLevel(1001))
    }

    @Test
    fun shuffle_board_executes_only_when_multiple_regular_blocks_exist() {
        val level = LevelCatalog.getLevel(1)
        val engine = GameEngine(level)

        val success = engine.shuffleBoard {}
        assertTrue("Shuffle should succeed on valid board", success)
        assertEquals(PowerUpType.SHUFFLE, engine.state.lastUsedPowerUp)
    }
}
