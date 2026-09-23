package com.example.game.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.game.data.entity.AchievementEntity
import com.example.game.data.entity.LevelProgressEntity
import com.example.game.data.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    // Level Progress
    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun getAllLevelProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber")
    suspend fun getLevelProgress(levelNumber: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLevel(level: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLevels(levels: List<LevelProgressEntity>)

    // Player Profile
    @Query("SELECT * FROM player_profile WHERE id = 1")
    fun getPlayerProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile WHERE id = 1")
    suspend fun getPlayerProfileSync(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: PlayerProfileEntity)

    // Achievements
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllAchievements(achievements: List<AchievementEntity>)

    @Update
    suspend fun updateAchievement(achievement: AchievementEntity)

    @Query("DELETE FROM level_progress")
    suspend fun clearLevelProgress()
}
