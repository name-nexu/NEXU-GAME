package com.example.game.extensions

import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.model.PowerUpType

/**
 * Extension interfaces and clean abstractions for future expansions:
 * - Analytics tracking
 * - Cloud save / sync
 * - Remote configuration
 * - Event scheduling
 *
 * Keeps the offline game 100% functional while providing zero-overhead integration hooks.
 */

interface GameAnalyticsTracker {
    fun trackLevelStarted(level: Int) {}
    fun trackLevelCompleted(level: Int, stars: Int, score: Int, movesUsed: Int) {}
    fun trackLevelFailed(level: Int, score: Int) {}
    fun trackPowerUpUsed(powerUp: PowerUpType, level: Int) {}
    fun trackAchievementUnlocked(achievementId: String) {}
    fun trackDailyChallengeCompleted(daySeed: Long) {}

    companion object {
        val Default: GameAnalyticsTracker = object : GameAnalyticsTracker {}
    }
}

interface CloudSaveProvider {
    suspend fun savePlayerProgress(profile: PlayerProfileEntity): Result<Unit> = Result.success(Unit)
    suspend fun loadPlayerProgress(): Result<PlayerProfileEntity?> = Result.success(null)

    companion object {
        val OfflineOnly: CloudSaveProvider = object : CloudSaveProvider {}
    }
}

interface RemoteConfigProvider {
    fun getInt(key: String, defaultValue: Int): Int = defaultValue
    fun getBoolean(key: String, defaultValue: Boolean): Boolean = defaultValue
    fun getString(key: String, defaultValue: String): String = defaultValue

    companion object {
        val LocalOnly: RemoteConfigProvider = object : RemoteConfigProvider {}
    }
}
