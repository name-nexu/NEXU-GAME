package com.example.game.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_progress")
data class LevelProgressEntity(
    @PrimaryKey val levelNumber: Int,
    val stars: Int = 0,
    val highScore: Int = 0,
    val unlocked: Boolean = false,
    val completed: Boolean = false,
    val bestMoves: Int = 0
)

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 150,
    val totalStars: Int = 0,
    val currentLevel: Int = 1,
    val selectedCostume: String = "classic",
    val unlockedCostumes: String = "classic", // Comma-separated costume ids
    val selectedTheme: String = "rainbow_garden",
    val unlockedThemes: String = "rainbow_garden",
    val selectedSkin: String = "jelly",
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val largeTextEnabled: Boolean = false,
    val reducedAnimEnabled: Boolean = false,
    val highContrastEnabled: Boolean = false,
    val lastDailyPlayedDate: String = "",
    val hammerCount: Int = 3,
    val rocketCount: Int = 3,
    val rainbowCount: Int = 2,
    val wandCount: Int = 2,
    val shuffleCount: Int = 3,
    val blocksBrokenTotal: Int = 0
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean = false,
    val rewardCoins: Int = 50,
    val progress: Int = 0,
    val target: Int = 1,
    val iconName: String = "star"
)
