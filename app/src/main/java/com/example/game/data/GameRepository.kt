package com.example.game.data

import com.example.game.data.dao.GameDao
import com.example.game.data.entity.AchievementEntity
import com.example.game.data.entity.LevelProgressEntity
import com.example.game.data.entity.PlayerProfileEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GameRepository(private val dao: GameDao) {

    val levelProgressList: Flow<List<LevelProgressEntity>> = dao.getAllLevelProgress()
    val playerProfile: Flow<PlayerProfileEntity?> = dao.getPlayerProfile()
    val achievements: Flow<List<AchievementEntity>> = dao.getAllAchievements()

    suspend fun initializeDefaultsIfNeeded() {
        // Initialize Player Profile if missing
        val currentProfile = dao.getPlayerProfileSync()
        if (currentProfile == null) {
            dao.insertOrUpdateProfile(
                PlayerProfileEntity(
                    id = 1,
                    coins = 200,
                    totalStars = 0,
                    currentLevel = 1,
                    selectedCostume = "classic",
                    unlockedCostumes = "classic",
                    selectedTheme = "rainbow_garden",
                    unlockedThemes = "rainbow_garden",
                    selectedSkin = "jelly",
                    hammerCount = 3,
                    rocketCount = 3,
                    rainbowCount = 2,
                    wandCount = 2,
                    shuffleCount = 3
                )
            )
        }

        // Initialize Level 1 unlocked if no levels exist
        val firstLevel = dao.getLevelProgress(1)
        if (firstLevel == null) {
            dao.insertOrUpdateLevel(
                LevelProgressEntity(
                    levelNumber = 1,
                    stars = 0,
                    highScore = 0,
                    unlocked = true,
                    completed = false
                )
            )
        }

        // Initialize default achievements
        val defaultAchievements = listOf(
            AchievementEntity(
                id = "first_break",
                title = "First Break",
                description = "Break your very first block!",
                rewardCoins = 50,
                progress = 0,
                target = 1,
                iconName = "star"
            ),
            AchievementEntity(
                id = "star_collector",
                title = "Star Collector",
                description = "Collect 20 stars across puzzles.",
                rewardCoins = 100,
                progress = 0,
                target = 20,
                iconName = "stars"
            ),
            AchievementEntity(
                id = "puzzle_master",
                title = "Puzzle Master",
                description = "Complete 10 levels.",
                rewardCoins = 150,
                progress = 0,
                target = 10,
                iconName = "trophy"
            ),
            AchievementEntity(
                id = "coin_hunter",
                title = "Coin Hunter",
                description = "Collect 300 total coins.",
                rewardCoins = 120,
                progress = 0,
                target = 300,
                iconName = "coin"
            ),
            AchievementEntity(
                id = "perfect_player",
                title = "Perfect Player",
                description = "Earn 3 stars on 5 different levels.",
                rewardCoins = 200,
                progress = 0,
                target = 5,
                iconName = "sparkle"
            )
        )
        dao.insertAllAchievements(defaultAchievements)
    }

    suspend fun recordLevelVictory(
        levelNumber: Int,
        earnedStars: Int,
        earnedCoins: Int,
        movesUsed: Int,
        finalScore: Int = 0
    ) {
        val currentLevel = dao.getLevelProgress(levelNumber)
        val prevStars = currentLevel?.stars ?: 0
        val bestStars = maxOf(prevStars, earnedStars)
        val calculatedScore = if (finalScore > 0) finalScore else ((earnedStars * 500) + (earnedCoins * 10))
        val bestScore = maxOf(currentLevel?.highScore ?: 0, calculatedScore)
        val bestMoves = if ((currentLevel?.bestMoves ?: 0) > 0) minOf(currentLevel!!.bestMoves, movesUsed) else movesUsed

        dao.insertOrUpdateLevel(
            LevelProgressEntity(
                levelNumber = levelNumber,
                stars = bestStars,
                highScore = bestScore,
                unlocked = true,
                completed = true,
                bestMoves = bestMoves
            )
        )

        // Unlock next level
        val nextLevelNumber = levelNumber + 1
        val nextLevel = dao.getLevelProgress(nextLevelNumber)
        if (nextLevel == null || !nextLevel.unlocked) {
            dao.insertOrUpdateLevel(
                LevelProgressEntity(
                    levelNumber = nextLevelNumber,
                    stars = nextLevel?.stars ?: 0,
                    unlocked = true,
                    completed = nextLevel?.completed ?: false
                )
            )
        }

        // Update player profile
        val profile = dao.getPlayerProfileSync() ?: PlayerProfileEntity()
        val additionalStars = if (earnedStars > prevStars) earnedStars - prevStars else 0
        val updatedCoins = profile.coins + earnedCoins
        val updatedTotalStars = profile.totalStars + additionalStars
        val nextUnlockedCurrent = maxOf(profile.currentLevel, nextLevelNumber)

        dao.insertOrUpdateProfile(
            profile.copy(
                coins = updatedCoins,
                totalStars = updatedTotalStars,
                currentLevel = nextUnlockedCurrent
            )
        )

        // Check & update achievements
        checkAchievements(updatedTotalStars, updatedCoins)
    }

    private suspend fun checkAchievements(totalStars: Int, coins: Int) {
        val allLevels = dao.getAllLevelProgress().firstOrNull() ?: emptyList()
        val completedCount = allLevels.count { it.completed }
        val threeStarCount = allLevels.count { it.stars == 3 }

        val achievementsList = dao.getAllAchievements().firstOrNull() ?: emptyList()
        for (ach in achievementsList) {
            if (ach.unlocked) continue
            var progress = ach.progress
            when (ach.id) {
                "first_break" -> progress = if (completedCount >= 1) 1 else 0
                "star_collector" -> progress = totalStars
                "puzzle_master" -> progress = completedCount
                "coin_hunter" -> progress = coins
                "perfect_player" -> progress = threeStarCount
            }
            val unlocked = progress >= ach.target
            if (progress != ach.progress || unlocked != ach.unlocked) {
                dao.updateAchievement(ach.copy(progress = progress, unlocked = unlocked))
            }
        }
    }

    suspend fun claimAchievement(achievementId: String) {
        val achievementsList = dao.getAllAchievements().firstOrNull() ?: return
        val ach = achievementsList.firstOrNull { it.id == achievementId } ?: return
        if (ach.unlocked && ach.progress >= ach.target) {
            val profile = dao.getPlayerProfileSync() ?: return
            dao.insertOrUpdateProfile(profile.copy(coins = profile.coins + ach.rewardCoins))
            // Mark target negative so it's claimed
            dao.updateAchievement(ach.copy(rewardCoins = 0))
        }
    }

    suspend fun buyCostume(costumeId: String, cost: Int): Boolean {
        val profile = dao.getPlayerProfileSync() ?: return false
        if (profile.coins >= cost) {
            val unlocked = profile.unlockedCostumes.split(",").toMutableSet()
            unlocked.add(costumeId)
            dao.insertOrUpdateProfile(
                profile.copy(
                    coins = profile.coins - cost,
                    unlockedCostumes = unlocked.joinToString(","),
                    selectedCostume = costumeId
                )
            )
            return true
        }
        return false
    }

    suspend fun equipCostume(costumeId: String) {
        val profile = dao.getPlayerProfileSync() ?: return
        dao.insertOrUpdateProfile(profile.copy(selectedCostume = costumeId))
    }

    suspend fun updateSettings(
        sound: Boolean,
        music: Boolean,
        haptics: Boolean,
        largeText: Boolean,
        reducedAnim: Boolean,
        highContrast: Boolean
    ) {
        val profile = dao.getPlayerProfileSync() ?: return
        dao.insertOrUpdateProfile(
            profile.copy(
                soundEnabled = sound,
                musicEnabled = music,
                hapticsEnabled = haptics,
                largeTextEnabled = largeText,
                reducedAnimEnabled = reducedAnim,
                highContrastEnabled = highContrast
            )
        )
    }

    suspend fun usePowerUp(powerUpType: com.example.game.model.PowerUpType): Boolean {
        val profile = dao.getPlayerProfileSync() ?: return false
        when (powerUpType) {
            com.example.game.model.PowerUpType.HAMMER -> {
                if (profile.hammerCount > 0) {
                    dao.insertOrUpdateProfile(profile.copy(hammerCount = profile.hammerCount - 1))
                    return true
                }
            }
            com.example.game.model.PowerUpType.ROCKET -> {
                if (profile.rocketCount > 0) {
                    dao.insertOrUpdateProfile(profile.copy(rocketCount = profile.rocketCount - 1))
                    return true
                }
            }
            com.example.game.model.PowerUpType.RAINBOW -> {
                if (profile.rainbowCount > 0) {
                    dao.insertOrUpdateProfile(profile.copy(rainbowCount = profile.rainbowCount - 1))
                    return true
                }
            }
            com.example.game.model.PowerUpType.MAGIC_WAND -> {
                if (profile.wandCount > 0) {
                    dao.insertOrUpdateProfile(profile.copy(wandCount = profile.wandCount - 1))
                    return true
                }
            }
            com.example.game.model.PowerUpType.SHUFFLE -> {
                if (profile.shuffleCount > 0) {
                    dao.insertOrUpdateProfile(profile.copy(shuffleCount = profile.shuffleCount - 1))
                    return true
                }
            }
        }
        return false
    }

    suspend fun buyPowerUp(powerUpType: com.example.game.model.PowerUpType): Boolean {
        val profile = dao.getPlayerProfileSync() ?: return false
        if (profile.coins < powerUpType.costCoins) return false

        val newCoins = profile.coins - powerUpType.costCoins
        when (powerUpType) {
            com.example.game.model.PowerUpType.HAMMER -> dao.insertOrUpdateProfile(profile.copy(coins = newCoins, hammerCount = profile.hammerCount + 3))
            com.example.game.model.PowerUpType.ROCKET -> dao.insertOrUpdateProfile(profile.copy(coins = newCoins, rocketCount = profile.rocketCount + 3))
            com.example.game.model.PowerUpType.RAINBOW -> dao.insertOrUpdateProfile(profile.copy(coins = newCoins, rainbowCount = profile.rainbowCount + 2))
            com.example.game.model.PowerUpType.MAGIC_WAND -> dao.insertOrUpdateProfile(profile.copy(coins = newCoins, wandCount = profile.wandCount + 2))
            com.example.game.model.PowerUpType.SHUFFLE -> dao.insertOrUpdateProfile(profile.copy(coins = newCoins, shuffleCount = profile.shuffleCount + 3))
        }
        return true
    }

    // Developer / Debug Tools
    suspend fun debugAddCoins(amount: Int = 500) {
        val profile = dao.getPlayerProfileSync() ?: return
        dao.insertOrUpdateProfile(profile.copy(coins = profile.coins + amount))
    }

    suspend fun debugUnlockAllLevels(maxLevel: Int = 20) {
        val levels = (1..maxLevel).map { lvl ->
            LevelProgressEntity(
                levelNumber = lvl,
                stars = 3,
                unlocked = true,
                completed = true
            )
        }
        dao.insertAllLevels(levels)
        val profile = dao.getPlayerProfileSync() ?: return
        dao.insertOrUpdateProfile(profile.copy(currentLevel = maxLevel, totalStars = maxLevel * 3))
    }

    suspend fun debugResetProgress() {
        dao.clearLevelProgress()
        dao.insertOrUpdateProfile(
            PlayerProfileEntity(
                id = 1,
                coins = 200,
                totalStars = 0,
                currentLevel = 1,
                selectedCostume = "classic",
                unlockedCostumes = "classic",
                selectedTheme = "rainbow_garden",
                hammerCount = 3,
                rocketCount = 3,
                rainbowCount = 2,
                wandCount = 2,
                shuffleCount = 3
            )
        )
        dao.insertOrUpdateLevel(
            LevelProgressEntity(levelNumber = 1, stars = 0, unlocked = true, completed = false)
        )
    }
}
