package com.example.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.SoundManager
import com.example.game.data.AppDatabase
import com.example.game.data.GameRepository
import com.example.game.data.entity.AchievementEntity
import com.example.game.data.entity.LevelProgressEntity
import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.engine.GameEngine
import com.example.game.engine.GameState
import com.example.game.engine.GameStatus
import com.example.game.level.LevelCatalog
import com.example.game.model.LevelDefinition
import com.example.game.model.PowerUpType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class GameScreen {
    HOME,
    LEVEL_SELECT,
    GAMEPLAY,
    COSTUMES,
    REWARDS,
    SETTINGS
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = GameRepository(db.gameDao())
    val soundManager = SoundManager(application)

    private val _currentScreen = MutableStateFlow(GameScreen.HOME)
    val currentScreen: StateFlow<GameScreen> = _currentScreen.asStateFlow()

    val playerProfile: StateFlow<PlayerProfileEntity?> = repository.playerProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val levelsProgress: StateFlow<List<LevelProgressEntity>> = repository.levelProgressList
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val achievements: StateFlow<List<AchievementEntity>> = repository.achievements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentLevelDef = MutableStateFlow<LevelDefinition?>(null)
    val currentLevelDef: StateFlow<LevelDefinition?> = _currentLevelDef.asStateFlow()

    private var engine: GameEngine? = null
    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _devModeVisible = MutableStateFlow(false)
    val devModeVisible: StateFlow<Boolean> = _devModeVisible.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            // Observe settings to sync with soundManager
            playerProfile.collect { profile ->
                profile?.let {
                    soundManager.soundEnabled = it.soundEnabled
                    soundManager.setMusic(it.musicEnabled)
                    soundManager.hapticsEnabled = it.hapticsEnabled
                }
            }
        }
    }

    fun navigateTo(screen: GameScreen) {
        soundManager.playTap()
        _currentScreen.value = screen
    }

    fun startLevel(levelNumber: Int) {
        val levelDef = LevelCatalog.getLevel(levelNumber)
        _currentLevelDef.value = levelDef
        val newEngine = GameEngine(levelDef)
        engine = newEngine
        _gameState.value = newEngine.state
        _isPaused.value = false
        _currentScreen.value = GameScreen.GAMEPLAY
        soundManager.playTap()
    }

    fun startDailyChallenge() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR).toLong()
        val dailyDef = LevelCatalog.getDailyLevel(today)
        _currentLevelDef.value = dailyDef
        val newEngine = GameEngine(dailyDef)
        engine = newEngine
        _gameState.value = newEngine.state
        _isPaused.value = false
        _currentScreen.value = GameScreen.GAMEPLAY
        soundManager.playTap()
    }

    fun restartCurrentLevel() {
        engine?.restart()
        _gameState.value = engine?.state
        _isPaused.value = false
        soundManager.playTap()
    }

    fun nextLevel() {
        val currentLvl = _currentLevelDef.value?.levelNumber ?: 1
        if (currentLvl >= 9999) { // was daily challenge
            navigateTo(GameScreen.HOME)
        } else {
            startLevel(currentLvl + 1)
        }
    }

    fun onBlockTapped(x: Int, y: Int) {
        val eng = engine ?: return
        val updated = eng.tapBlock(x, y) { soundName ->
            when (soundName) {
                "tap" -> soundManager.playTap()
                "break" -> soundManager.playBlockBreak()
                "hit" -> soundManager.playStrongBlockHit()
                "ice" -> soundManager.playIceBreak()
                "bomb" -> soundManager.playBombBlast()
                "star" -> soundManager.playStar()
                "coin" -> soundManager.playCoin()
                "magic" -> soundManager.playPowerUp()
                "powerup" -> soundManager.playPowerUp()
                "win" -> soundManager.playWin()
                "fail" -> soundManager.playFail()
            }
        }
        _gameState.value = updated
        handleVictoryIfWon(updated)
    }

    private fun handleVictoryIfWon(updated: GameState) {
        if (updated.status == GameStatus.WON) {
            val levelNum = _currentLevelDef.value?.levelNumber ?: 1
            if (levelNum < 9999) {
                viewModelScope.launch {
                    val movesUsed = (_currentLevelDef.value?.movesAllowed ?: 10) - updated.movesRemaining
                    repository.recordLevelVictory(
                        levelNumber = levelNum,
                        earnedStars = updated.starsEarned,
                        earnedCoins = updated.coinsEarned,
                        movesUsed = movesUsed,
                        finalScore = updated.currentScore
                    )
                }
            } else {
                viewModelScope.launch {
                    repository.debugAddCoins(50)
                }
            }
        }
    }

    fun selectPowerUp(type: PowerUpType) {
        viewModelScope.launch {
            val profile = playerProfile.value
            val hasInventory = when (type) {
                PowerUpType.HAMMER -> (profile?.hammerCount ?: 0) > 0
                PowerUpType.ROCKET -> (profile?.rocketCount ?: 0) > 0
                PowerUpType.RAINBOW -> (profile?.rainbowCount ?: 0) > 0
                PowerUpType.MAGIC_WAND -> (profile?.wandCount ?: 0) > 0
                PowerUpType.SHUFFLE -> (profile?.shuffleCount ?: 0) > 0
            }

            if (hasInventory) {
                // For Shuffle, execute immediately
                if (type == PowerUpType.SHUFFLE) {
                    val used = repository.usePowerUp(type)
                    if (used) {
                        val eng = engine ?: return@launch
                        eng.selectPowerUp(PowerUpType.SHUFFLE)
                        val updated = eng.tapBlock(0, 0) { soundManager.playPowerUp() }
                        _gameState.value = updated
                        handleVictoryIfWon(updated)
                    }
                } else {
                    // Toggle selection
                    val current = engine?.state?.activePowerUp
                    val next = if (current == type) null else type
                    engine?.selectPowerUp(next)
                    _gameState.value = engine?.state
                    soundManager.playTap()
                }
            } else {
                // Prompt to buy with coins
                soundManager.playTap()
                val bought = repository.buyPowerUp(type)
                if (bought) {
                    soundManager.playCoin()
                }
            }
        }
    }

    fun togglePause() {
        soundManager.playTap()
        _isPaused.value = !_isPaused.value
    }

    fun toggleSound(enabled: Boolean) {
        val p = playerProfile.value ?: return
        viewModelScope.launch {
            repository.updateSettings(
                sound = enabled,
                music = p.musicEnabled,
                haptics = p.hapticsEnabled,
                largeText = p.largeTextEnabled,
                reducedAnim = p.reducedAnimEnabled,
                highContrast = p.highContrastEnabled
            )
        }
    }

    fun toggleMusic(enabled: Boolean) {
        val p = playerProfile.value ?: return
        viewModelScope.launch {
            repository.updateSettings(
                sound = p.soundEnabled,
                music = enabled,
                haptics = p.hapticsEnabled,
                largeText = p.largeTextEnabled,
                reducedAnim = p.reducedAnimEnabled,
                highContrast = p.highContrastEnabled
            )
        }
    }

    fun toggleHaptics(enabled: Boolean) {
        val p = playerProfile.value ?: return
        viewModelScope.launch {
            repository.updateSettings(
                sound = p.soundEnabled,
                music = p.musicEnabled,
                haptics = enabled,
                largeText = p.largeTextEnabled,
                reducedAnim = p.reducedAnimEnabled,
                highContrast = p.highContrastEnabled
            )
        }
    }

    fun toggleLargeText(enabled: Boolean) {
        val p = playerProfile.value ?: return
        viewModelScope.launch {
            repository.updateSettings(
                sound = p.soundEnabled,
                music = p.musicEnabled,
                haptics = p.hapticsEnabled,
                largeText = enabled,
                reducedAnim = p.reducedAnimEnabled,
                highContrast = p.highContrastEnabled
            )
        }
    }

    fun toggleHighContrast(enabled: Boolean) {
        val p = playerProfile.value ?: return
        viewModelScope.launch {
            repository.updateSettings(
                sound = p.soundEnabled,
                music = p.musicEnabled,
                haptics = p.hapticsEnabled,
                largeText = p.largeTextEnabled,
                reducedAnim = p.reducedAnimEnabled,
                highContrast = enabled
            )
        }
    }

    fun equipCostume(costumeId: String) {
        viewModelScope.launch {
            repository.equipCostume(costumeId)
            soundManager.playTap()
        }
    }

    fun buyCostume(costumeId: String, cost: Int) {
        viewModelScope.launch {
            val success = repository.buyCostume(costumeId, cost)
            if (success) {
                soundManager.playStar()
            }
        }
    }

    fun claimAchievement(id: String) {
        viewModelScope.launch {
            repository.claimAchievement(id)
            soundManager.playCoin()
        }
    }

    fun setDevModeVisible(visible: Boolean) {
        _devModeVisible.value = visible
    }

    fun devAddCoins(amount: Int = 500) {
        viewModelScope.launch {
            repository.debugAddCoins(amount)
            soundManager.playCoin()
        }
    }

    fun devUnlockAllLevels() {
        viewModelScope.launch {
            repository.debugUnlockAllLevels(20)
            soundManager.playWin()
        }
    }

    fun devResetProgress() {
        viewModelScope.launch {
            repository.debugResetProgress()
            soundManager.playTap()
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stopBackgroundMusic()
    }
}
