package com.example

import com.example.game.engine.GameEngine
import com.example.game.engine.GameStatus
import com.example.game.level.LevelCatalog
import com.example.game.level.LevelProgressionConfig
import com.example.game.model.ObjectiveType
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testLevelProgressionScaling() {
        // Grid size scales from 5x6 up to 8x10
        val (w1, h1) = LevelProgressionConfig.getGridDimensions(1)
        assertEquals(5, w1)
        assertEquals(6, h1)

        val (w15, h15) = LevelProgressionConfig.getGridDimensions(15)
        assertTrue(w15 >= 6 && h15 >= 7)

        val (w80, h80) = LevelProgressionConfig.getGridDimensions(80)
        assertEquals(8, w80)
        assertTrue(h80 >= 9)

        // Color count expands from 2 colors up to 7
        val colorsLvl1 = LevelProgressionConfig.getColorPalette(1)
        assertEquals(2, colorsLvl1.size)

        val colorsLvl15 = LevelProgressionConfig.getColorPalette(15)
        assertEquals(5, colorsLvl15.size)

        val colorsLvl90 = LevelProgressionConfig.getColorPalette(90)
        assertTrue(colorsLvl90.size >= 6)

        // Target score increases progressively
        val scoreLvl1 = LevelProgressionConfig.getTargetScore(1)
        val scoreLvl10 = LevelProgressionConfig.getTargetScore(10)
        val scoreLvl50 = LevelProgressionConfig.getTargetScore(50)
        val scoreLvl100 = LevelProgressionConfig.getTargetScore(100)

        assertTrue(scoreLvl1 < scoreLvl10)
        assertTrue(scoreLvl10 < scoreLvl50)
        assertTrue(scoreLvl50 < scoreLvl100)
    }

    @Test
    fun testLevelCatalogIntegration() {
        val level1 = LevelCatalog.getLevel(1)
        assertEquals(1, level1.levelNumber)
        assertEquals(5, level1.gridWidth)
        assertEquals(6, level1.gridHeight)
        assertEquals(2, level1.availableColors.size)
        assertTrue(level1.targetScore > 0)
        assertTrue(level1.star3Score > level1.star1Score)

        val level15 = LevelCatalog.getLevel(15)
        assertEquals(15, level15.levelNumber)
        assertTrue(level15.colorCount >= 3)
        assertTrue(level15.targetScore > level1.targetScore)

        val level25 = LevelCatalog.getLevel(25) // Procedural level
        assertEquals(25, level25.levelNumber)
        assertTrue(level25.targetScore >= 3000)
        assertTrue(level25.initialBlocks.isNotEmpty())
    }

    @Test
    fun testGameEngineScoreAccumulation() {
        val level = LevelCatalog.getLevel(1)
        val engine = GameEngine(level)

        val initialScore = engine.state.currentScore
        assertEquals(0, initialScore)
        assertEquals(level.targetScore, engine.state.targetScore)
        assertFalse(engine.state.isTargetScoreMet)

        // Find a valid non-bloki, non-goal block to tap
        val blockToTap = engine.state.blocks.first { !it.isBloki && !it.isGoal }
        engine.tapBlock(blockToTap.x, blockToTap.y)

        assertTrue(engine.state.currentScore > 0)
        assertTrue(engine.state.lastScoreEarned > 0)
    }
}
