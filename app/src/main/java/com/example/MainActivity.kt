package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.ui.GameScreen
import com.example.game.ui.GameViewModel
import com.example.game.ui.components.DevToolsDialog
import com.example.game.ui.screens.CostumesScreen
import com.example.game.ui.screens.GamePlayScreen
import com.example.game.ui.screens.HomeScreen
import com.example.game.ui.screens.LevelSelectScreen
import com.example.game.ui.screens.RewardsScreen
import com.example.game.ui.screens.SettingsScreen
import com.example.ui.theme.BlockyBuddiesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockyBuddiesTheme {
                BlockyBuddiesApp()
            }
        }
    }
}

@Composable
fun BlockyBuddiesApp(viewModel: GameViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val profile by viewModel.playerProfile.collectAsState()
    val levelsProgress by viewModel.levelsProgress.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val currentLevelDef by viewModel.currentLevelDef.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val isPaused by viewModel.isPaused.collectAsState()
    val devModeVisible by viewModel.devModeVisible.collectAsState()

    // Handle system back navigation
    BackHandler(enabled = currentScreen != GameScreen.HOME) {
        when (currentScreen) {
            GameScreen.GAMEPLAY -> viewModel.navigateTo(GameScreen.LEVEL_SELECT)
            else -> viewModel.navigateTo(GameScreen.HOME)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                GameScreen.HOME -> {
                    HomeScreen(
                        profile = profile,
                        onNavigate = { viewModel.navigateTo(it) },
                        onPlayNextLevel = { lvl -> viewModel.startLevel(lvl) },
                        onPlayDaily = { viewModel.startDailyChallenge() },
                        onOpenDevTools = { viewModel.setDevModeVisible(true) },
                        onPlayTap = { viewModel.soundManager.playTap() }
                    )
                }

                GameScreen.LEVEL_SELECT -> {
                    LevelSelectScreen(
                        profile = profile,
                        levelsProgress = levelsProgress,
                        onSelectLevel = { lvl -> viewModel.startLevel(lvl) },
                        onBack = { viewModel.navigateTo(GameScreen.HOME) }
                    )
                }

                GameScreen.GAMEPLAY -> {
                    GamePlayScreen(
                        levelDef = currentLevelDef,
                        gameState = gameState,
                        profile = profile,
                        isPaused = isPaused,
                        onBlockTapped = { x, y -> viewModel.onBlockTapped(x, y) },
                        onSelectPowerUp = { powerUp -> viewModel.selectPowerUp(powerUp) },
                        onTogglePause = { viewModel.togglePause() },
                        onRestart = { viewModel.restartCurrentLevel() },
                        onNextLevel = { viewModel.nextLevel() },
                        onBackToLevelSelect = { viewModel.navigateTo(GameScreen.LEVEL_SELECT) }
                    )
                }

                GameScreen.COSTUMES -> {
                    CostumesScreen(
                        profile = profile,
                        onEquipCostume = { id -> viewModel.equipCostume(id) },
                        onBuyCostume = { id, cost -> viewModel.buyCostume(id, cost) },
                        onBack = { viewModel.navigateTo(GameScreen.HOME) }
                    )
                }

                GameScreen.REWARDS -> {
                    RewardsScreen(
                        profile = profile,
                        achievements = achievements,
                        onClaimReward = { id -> viewModel.claimAchievement(id) },
                        onBack = { viewModel.navigateTo(GameScreen.HOME) }
                    )
                }

                GameScreen.SETTINGS -> {
                    SettingsScreen(
                        profile = profile,
                        onToggleSound = { viewModel.toggleSound(it) },
                        onToggleMusic = { viewModel.toggleMusic(it) },
                        onToggleHaptics = { viewModel.toggleHaptics(it) },
                        onToggleLargeText = { viewModel.toggleLargeText(it) },
                        onToggleHighContrast = { viewModel.toggleHighContrast(it) },
                        onOpenDevTools = { viewModel.setDevModeVisible(true) },
                        onBack = { viewModel.navigateTo(GameScreen.HOME) }
                    )
                }
            }

            // Developer Tools Modal
            if (devModeVisible) {
                DevToolsDialog(
                    onDismiss = { viewModel.setDevModeVisible(false) },
                    onJumpToLevel = { lvl -> viewModel.startLevel(lvl) },
                    onAddCoins = { viewModel.devAddCoins(500) },
                    onUnlockAllLevels = { viewModel.devUnlockAllLevels() },
                    onResetProgress = { viewModel.devResetProgress() }
                )
            }
        }
    }
}
