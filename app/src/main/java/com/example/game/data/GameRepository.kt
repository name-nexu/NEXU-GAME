package com.example.game.data

import com.example.BuildConfig
import com.example.game.config.GameConfig
import com.example.game.data.dao.GameDao
import com.example.game.data.entity.AchievementEntity
import com.example.game.data.entity.LevelProgressEntity
import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.model.PowerUpType
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
                    coins = GameConfig.PlayerDefaults.INITIAL_COINS,
                    totalStars = GameConfig.PlayerDefaults.INITIAL_STARS,
                    currentLevel = GameConfig.PlayerDefaults.INITIAL_LEVEL,
                    selectedCostume = GameConfig.PlayerDefaults.DEFAULT_COSTUME,
                    unlockedCostumes = GameConfig.PlayerDefaults.DEFAULT_COSTUME,
                    selectedTheme = GameConfig.PlayerDefaults.DEFAULT_THEME,
                    unlockedThemes = GameConfig.PlayerDefaults.DEFAULT_THEME,
                    selectedSkin = GameConfig.PlayerDefaults.DEFAULT_SKIN,
                    hammerCount = GameConfig.PlayerDefaults.INITIAL_HAMMERS,
                    rocketCount = GameConfig.PlayerDefaults.INITIAL_ROCKETS,
                    rainbowCount = GameConfig.PlayerDefaults.INITIAL_RAINBOWS,
                    wandCount = GameConfig.PlayerDefaults.INITIAL_WANDS,
                    shuffleCount = GameConfig.PlayerDefaults.INITIAL_SHUFFLES,
                    blocksBrokenTotal = 0
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

        // Initialize default achievements using GameConfig
        val defaultAchievements = listOf(
            AchievementEntity(
                id = GameConfig.Achievements.FIRST_BREAK_ID,
                title = "First Break",
                description = "Break your very first block!",
                rewardCoins = GameConfig.Achievements.FIRST_BREAK_REWARD,
                progress = 0,
                target = GameConfig.Achievements.FIRST_BREAK_TARGET,
                iconName = "star"
            ),
            AchievementEntity(
                id = GameConfig.Achievements.STAR_COLLECTOR_ID,
                title = "Star Collector",
                description = "Collect 20 stars across puzzles.",
                rewardCoins = GameConfig.Achievements.STAR_COLLECTOR_REWARD,
                progress = 0,
                target = GameConfig.Achievements.STAR_COLLECTOR_TARGET,
                iconName = "stars"
            ),
            AchievementEntity(
                id = GameConfig.Achievements.PUZZLE_MASTER_ID,
                title = "Puzzle Master",
                description = "Complete 10 levels.",
                rewardCoins = GameConfig.Achievements.PUZZLE_MASTER_REWARD,
                progress = 0,
                target = GameConfig.Achievements.PUZZLE_MASTER_TARGET,
                iconName = "trophy"
            ),
            AchievementEntity(
                id = GameConfig.Achievements.COIN_HUNTER_ID,
                title = "Coin Hunter",
                description = "Collect 300 total coins.",
                rewardCoins = GameConfig.Achievements.COIN_HUNTER_REWARD,
                progress = 0,
                target = GameConfig.Achievements.COIN_HUNTER_TARGET,
                iconName = "coin"
            ),
            AchievementEntity(
                id = GameConfig.Achievements.PERFECT_PLAYER_ID,
                title = "Perfect Player",
                description = "Earn 3 stars on 5 different levels.",
                rewardCoins = GameConfig.Achievements.PERFECT_PLAYER_REWARD,
                progress = 0,
                target = GameConfig.Achievements.PERFECT_PLAYER_TARGET,
                iconName = "sparkle"
            )
        )
        dao.insertAllAchievements(defaultAchievements)
    }

    /**
     * Records block destruction immediately when blocks are broken in gameplay or power-ups.
     * Unlocks the "first_break" achievement on the very first destroyed block without needing level completion.
     */
    suspend fun recordBlocksBroken(count: Int) {
        if (count <= 0) return
        val profile = dao.getPlayerProfileSync() ?: return
        val newTotal = profile.blocksBrokenTotal + count
        dao.insertOrUpdateProfile(profile.copy(blocksBrokenTotal = newTotal))

        // Check first_break achievement
        val achievementsList = dao.getAllAchievements().firstOrNull() ?: emptyList()
        val firstBreak = achievementsList.firstOrNull { it.id == GameConfig.Achievements.FIRST_BREAK_ID }
        if (firstBreak != null && !firstBreak.unlocked && newTotal >= 1) {
            dao.updateAchievement(firstBreak.copy(progress = 1, unlocked = true))
        }
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
        val existingBestMoves = currentLevel?.bestMoves ?: 0
        val bestMoves = if (existingBestMoves > 0) minOf(existingBestMoves, movesUsed) else movesUsed

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

    /**
     * Checks if today's Daily Challenge reward has already been claimed.
     */
    suspend fun isDailyClaimedToday(): Boolean {
        val profile = dao.getPlayerProfileSync() ?: return false
        val todayStr = GameConfig.getTodayDateString()
        return profile.lastDailyPlayedDate == todayStr
    }

    /**
     * Awards daily challenge coins ONLY ONCE per calendar day.
     * Prevents duplicate claims and persists claim date across app restarts.
     * Returns the number of coins awarded (0 if already claimed today).
     */
    suspend fun recordDailyChallengeVictory(): Int {
        val profile = dao.getPlayerProfileSync() ?: return 0
        val todayStr = GameConfig.getTodayDateString()
        if (profile.lastDailyPlayedDate == todayStr) {
            return 0 // Already claimed today!
        }
        val reward = GameConfig.Rewards.DAILY_CHALLENGE_COINS
        dao.insertOrUpdateProfile(
            profile.copy(
                coins = profile.coins + reward,
                lastDailyPlayedDate = todayStr
            )
        )
        return reward
    }

    private suspend fun checkAchievements(totalStars: Int, coins: Int) {
        val allLevels = dao.getAllLevelProgress().firstOrNull() ?: emptyList()
        val completedCount = allLevels.count { it.completed }
        val threeStarCount = allLevels.count { it.stars == 3 }
        val profile = dao.getPlayerProfileSync()

        val achievementsList = dao.getAllAchievements().firstOrNull() ?: emptyList()
        for (ach in achievementsList) {
            if (ach.unlocked) continue
            var progress = ach.progress
            when (ach.id) {
                GameConfig.Achievements.FIRST_BREAK_ID -> {
                    progress = if ((profile?.blocksBrokenTotal ?: 0) >= 1) 1 else 0
                }
                GameConfig.Achievements.STAR_COLLECTOR_ID -> progress = totalStars
                GameConfig.Achievements.PUZZLE_MASTER_ID -> progress = completedCount
                GameConfig.Achievements.COIN_HUNTER_ID -> progress = coins
                GameConfig.Achievements.PERFECT_PLAYER_ID -> progress = threeStarCount
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
        if (ach.unlocked && ach.progress >= ach.target && ach.rewardCoins > 0) {
            val profile = dao.getPlayerProfileSync() ?: return
            dao.insertOrUpdateProfile(profile.copy(coins = profile.coins + ach.rewardCoins))
            // Mark target 0 so it cannot be claimed twice
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

    /**
     * Decrements a power-up from inventory after successful use.
     * Prevents negative inventory and updates the database immediately.
     */
    suspend fun usePowerUp(powerUpType: PowerUpType): Boolean {
        val profile = dao.getPlayerProfileSync() ?: return false
        val currentCount = when (powerUpType) {
            PowerUpType.HAMMER -> profile.hammerCount
            PowerUpType.ROCKET -> profile.rocketCount
            PowerUpType.RAINBOW -> profile.rainbowCount
            PowerUpType.MAGIC_WAND -> profile.wandCount
            PowerUpType.SHUFFLE -> profile.shuffleCount
        }
        if (currentCount <= 0) return false

        val updatedProfile = when (powerUpType) {
            PowerUpType.HAMMER -> profile.copy(hammerCount = maxOf(0, profile.hammerCount - 1))
            PowerUpType.ROCKET -> profile.copy(rocketCount = maxOf(0, profile.rocketCount - 1))
            PowerUpType.RAINBOW -> profile.copy(rainbowCount = maxOf(0, profile.rainbowCount - 1))
            PowerUpType.MAGIC_WAND -> profile.copy(wandCount = maxOf(0, profile.wandCount - 1))
            PowerUpType.SHUFFLE -> profile.copy(shuffleCount = maxOf(0, profile.shuffleCount - 1))
        }
        dao.insertOrUpdateProfile(updatedProfile)
        return true
    }

    /**
     * Purchases a bundle of power-ups with coins using GameConfig bundle quantities and prices.
     */
    suspend fun buyPowerUp(powerUpType: PowerUpType): Boolean {
        val profile = dao.getPlayerProfileSync() ?: return false
        if (profile.coins < powerUpType.costCoins) return false

        val newCoins = profile.coins - powerUpType.costCoins
        val updated = when (powerUpType) {
            PowerUpType.HAMMER -> profile.copy(
                coins = newCoins,
                hammerCount = profile.hammerCount + GameConfig.PowerUps.HAMMER_BUNDLE_SIZE
            )
            PowerUpType.ROCKET -> profile.copy(
                coins = newCoins,
                rocketCount = profile.rocketCount + GameConfig.PowerUps.ROCKET_BUNDLE_SIZE
            )
            PowerUpType.RAINBOW -> profile.copy(
                coins = newCoins,
                rainbowCount = profile.rainbowCount + GameConfig.PowerUps.RAINBOW_BUNDLE_SIZE
            )
            PowerUpType.MAGIC_WAND -> profile.copy(
                coins = newCoins,
                wandCount = profile.wandCount + GameConfig.PowerUps.WAND_BUNDLE_SIZE
            )
            PowerUpType.SHUFFLE -> profile.copy(
                coins = newCoins,
                shuffleCount = profile.shuffleCount + GameConfig.PowerUps.SHUFFLE_BUNDLE_SIZE
            )
        }
        dao.insertOrUpdateProfile(updated)
        return true
    }

    // Developer / Debug Tools (Only accessible in DEBUG builds)
    suspend fun debugAddCoins(amount: Int = 500) {
        if (!BuildConfig.DEBUG) return
        val profile = dao.getPlayerProfileSync() ?: return
        dao.insertOrUpdateProfile(profile.copy(coins = profile.coins + amount))
    }

    suspend fun debugUnlockAllLevels(maxLevel: Int = 20) {
        if (!BuildConfig.DEBUG) return
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
        if (!BuildConfig.DEBUG) return
        dao.clearLevelProgress()
        dao.insertOrUpdateProfile(
            PlayerProfileEntity(
                id = 1,
                coins = GameConfig.PlayerDefaults.INITIAL_COINS,
                totalStars = GameConfig.PlayerDefaults.INITIAL_STARS,
                currentLevel = GameConfig.PlayerDefaults.INITIAL_LEVEL,
                selectedCostume = GameConfig.PlayerDefaults.DEFAULT_COSTUME,
                unlockedCostumes = GameConfig.PlayerDefaults.DEFAULT_COSTUME,
                selectedTheme = GameConfig.PlayerDefaults.DEFAULT_THEME,
                hammerCount = GameConfig.PlayerDefaults.INITIAL_HAMMERS,
                rocketCount = GameConfig.PlayerDefaults.INITIAL_ROCKETS,
                rainbowCount = GameConfig.PlayerDefaults.INITIAL_RAINBOWS,
                wandCount = GameConfig.PlayerDefaults.INITIAL_WANDS,
                shuffleCount = GameConfig.PlayerDefaults.INITIAL_SHUFFLES,
                blocksBrokenTotal = 0
            )
        )
        dao.insertOrUpdateLevel(
            LevelProgressEntity(levelNumber = 1, stars = 0, unlocked = true, completed = false)
        )
    }
}
