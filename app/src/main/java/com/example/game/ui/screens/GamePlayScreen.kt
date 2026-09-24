package com.example.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rocket
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.engine.GameState
import com.example.game.engine.GameStatus
import com.example.game.model.LevelDefinition
import com.example.game.model.PowerUpType
import com.example.game.ui.components.BlokiCharacter
import com.example.game.ui.components.BlokiMood
import com.example.game.ui.components.PuzzleBoard
import com.example.ui.theme.BlokiCyan
import com.example.ui.theme.BlokiPink
import com.example.ui.theme.BlokiYellow
import com.example.ui.theme.StarGold

@Composable
fun GamePlayScreen(
    levelDef: LevelDefinition?,
    gameState: GameState?,
    profile: PlayerProfileEntity?,
    isPaused: Boolean,
    onBlockTapped: (Int, Int) -> Unit,
    onSelectPowerUp: (PowerUpType) -> Unit,
    onTogglePause: () -> Unit,
    onRestart: () -> Unit,
    onNextLevel: () -> Unit,
    onBackToLevelSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (levelDef == null || gameState == null) return

    val costumeId = profile?.selectedCostume ?: "classic"
    val movesRemaining = gameState.movesRemaining
    val isWin = gameState.status == GameStatus.WON
    val isLost = gameState.status == GameStatus.LOST

    // Dynamic background matching the world
    val bgBrush = when (levelDef.worldId) {
        1 -> Brush.verticalGradient(listOf(Color(0xFFE0F7FA), Color(0xFFF1F8E9), Color(0xFFFFF9C4))) // Rainbow Garden
        2 -> Brush.verticalGradient(listOf(Color(0xFFFCE4EC), Color(0xFFF3E5F5), Color(0xFFEDE7F6))) // Candy Valley
        3 -> Brush.verticalGradient(listOf(Color(0xFFE1F5FE), Color(0xFFE8EAF6), Color(0xFFE0F2F1))) // Cloud Kingdom
        4 -> Brush.verticalGradient(listOf(Color(0xFFE8F5E9), Color(0xFFF9FBE7), Color(0xFFFFF3E0))) // Jungle Blocks
        else -> Brush.verticalGradient(listOf(Color(0xFFECEFF1), Color(0xFFEDE7F6), Color(0xFFE1F5FE))) // Space Blocks
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
            .testTag("gameplay_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToLevelSelect,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .testTag("game_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF263238)
                    )
                }

                // Level & World Badge
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (levelDef.levelNumber >= 9999) "Daily Challenge" else "Level ${levelDef.levelNumber}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0277BD)
                    )
                    Text(
                        text = levelDef.worldName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlokiPink
                    )
                }

                // Remaining Moves Bubble
                MovesCounterBadge(moves = movesRemaining)

                // Pause Button
                IconButton(
                    onClick = onTogglePause,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White, CircleShape)
                        .testTag("pause_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Pause,
                        contentDescription = "Pause",
                        tint = Color(0xFF263238)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Objectives Progress Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                gameState.objectives.forEach { obj ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (obj.completed) Color(0xFFE8F5E9) else Color.White,
                        border = if (obj.completed) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E676)) else null,
                        shadowElevation = 2.dp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (obj.completed) "✅" else "🎯",
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = obj.description,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (obj.completed) Color(0xFF2E7D32) else Color(0xFF37474F)
                            )
                        }
                    }
                }
            }

            // Tutorial Hint if available
            levelDef.tutorialHint?.let { hint ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .padding(vertical = 2.dp)
                        .testTag("tutorial_hint")
                ) {
                    Text(
                        text = "💡 $hint",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF57F17),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            // Target Score & Progression Bar
            TargetScoreProgressBar(
                currentScore = gameState.currentScore,
                targetScore = gameState.targetScore,
                gridWidth = levelDef.gridWidth,
                gridHeight = levelDef.gridHeight,
                colorCount = levelDef.colorCount
            )

            // Center Puzzle Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PuzzleBoard(
                    gameState = gameState,
                    costumeId = costumeId,
                    onBlockTapped = onBlockTapped
                )

                // High Combo Indicator Banner
                if (gameState.comboMultiplier >= 2) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFFFF6D00),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 4.dp)
                    ) {
                        Text(
                            text = "🔥 ${gameState.comboMultiplier}x COMBO!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Bottom Bar: Power-Ups Tray
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("power_ups_tray")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PowerUpButton(
                        type = PowerUpType.HAMMER,
                        icon = Icons.Default.Build,
                        count = profile?.hammerCount ?: 0,
                        isSelected = gameState.activePowerUp == PowerUpType.HAMMER,
                        color = Color(0xFFFF5252),
                        onClick = { onSelectPowerUp(PowerUpType.HAMMER) },
                        tag = "powerup_hammer"
                    )
                    PowerUpButton(
                        type = PowerUpType.ROCKET,
                        icon = Icons.Default.Rocket,
                        count = profile?.rocketCount ?: 0,
                        isSelected = gameState.activePowerUp == PowerUpType.ROCKET,
                        color = Color(0xFF00B0FF),
                        onClick = { onSelectPowerUp(PowerUpType.ROCKET) },
                        tag = "powerup_rocket"
                    )
                    PowerUpButton(
                        type = PowerUpType.RAINBOW,
                        icon = Icons.Default.ColorLens,
                        count = profile?.rainbowCount ?: 0,
                        isSelected = gameState.activePowerUp == PowerUpType.RAINBOW,
                        color = Color(0xFFAB47BC),
                        onClick = { onSelectPowerUp(PowerUpType.RAINBOW) },
                        tag = "powerup_rainbow"
                    )
                    PowerUpButton(
                        type = PowerUpType.MAGIC_WAND,
                        icon = Icons.Default.AutoAwesome,
                        count = profile?.wandCount ?: 0,
                        isSelected = gameState.activePowerUp == PowerUpType.MAGIC_WAND,
                        color = Color(0xFFFFB300),
                        onClick = { onSelectPowerUp(PowerUpType.MAGIC_WAND) },
                        tag = "powerup_wand"
                    )
                    PowerUpButton(
                        type = PowerUpType.SHUFFLE,
                        icon = Icons.Default.Shuffle,
                        count = profile?.shuffleCount ?: 0,
                        isSelected = false,
                        color = Color(0xFF00E676),
                        onClick = { onSelectPowerUp(PowerUpType.SHUFFLE) },
                        tag = "powerup_shuffle"
                    )
                }
            }
        }

        // Win Modal
        if (isWin) {
            val nextLevelDef = if (levelDef.levelNumber < 9999 && com.example.game.level.LevelCatalog.hasLevel(levelDef.levelNumber + 1)) {
                com.example.game.level.LevelCatalog.getLevel(levelDef.levelNumber + 1)
            } else null
            WinDialog(
                starsEarned = gameState.starsEarned,
                coinsEarned = gameState.coinsEarned,
                currentScore = gameState.currentScore,
                targetScore = gameState.targetScore,
                costumeId = costumeId,
                nextLevelDef = nextLevelDef,
                onNext = onNextLevel,
                onReplay = onRestart
            )
        }

        // Fail Modal
        if (isLost) {
            FailDialog(
                costumeId = costumeId,
                onReplay = onRestart,
                onBack = onBackToLevelSelect
            )
        }

        // Pause Modal
        if (isPaused) {
            PauseDialog(
                onResume = onTogglePause,
                onRestart = onRestart,
                onLevelSelect = onBackToLevelSelect
            )
        }
    }
}

@Composable
fun MovesCounterBadge(moves: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "moves_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (moves <= 3) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = when {
            moves <= 3 -> Color(0xFFFF5252)
            moves <= 6 -> Color(0xFFFFB300)
            else -> Color(0xFF00E676)
        },
        shadowElevation = 3.dp,
        modifier = Modifier
            .scale(pulse)
            .testTag("moves_counter")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$moves",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "MOVES",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun PowerUpButton(
    type: PowerUpType,
    icon: ImageVector,
    count: Int,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    color = if (isSelected) color else color.copy(alpha = 0.15f),
                    shape = CircleShape
                )
                .then(
                    if (isSelected) Modifier.border(2.5.dp, Color.White, CircleShape) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.title,
                tint = if (isSelected) Color.White else color,
                modifier = Modifier.size(24.dp)
            )

            // Count badge
            Surface(
                shape = CircleShape,
                color = if (count > 0) Color(0xFF263238) else Color(0xFFFF9100),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(18.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (count > 0) count.toString() else "+",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = type.title,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF455A64)
        )
    }
}

@Composable
fun WinDialog(
    starsEarned: Int,
    coinsEarned: Int,
    currentScore: Int,
    targetScore: Int,
    costumeId: String,
    nextLevelDef: LevelDefinition?,
    onNext: () -> Unit,
    onReplay: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("win_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "LEVEL COMPLETE!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E676)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Jumping Excited Bloki
                BlokiCharacter(
                    costumeId = costumeId,
                    mood = BlokiMood.EXCITED,
                    size = 100.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Star Row Reveal
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..3) {
                        val earned = starsEarned >= i
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star $i",
                            tint = if (earned) StarGold else Color(0xFFCFD8DC),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Score Achievement Banner
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFE8F5E9),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E676)),
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🎉 TARGET CLEARED!",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Score: $currentScore pts (Goal: $targetScore)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Coins earned
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🪙", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+$coinsEarned Coins!",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE65100)
                    )
                }

                // Next Level Preview
                if (nextLevelDef != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE1F5FE),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🚀 Next: Level ${nextLevelDef.levelNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0277BD)
                            )
                            Text(
                                text = "Grid: ${nextLevelDef.gridWidth}×${nextLevelDef.gridHeight} • ${nextLevelDef.colorCount} Colors • Target: ${nextLevelDef.targetScore}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF455A64)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Next Level Button
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(52.dp)
                        .testTag("next_level_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text(
                        text = "NEXT LEVEL",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onReplay,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(44.dp)
                        .testTag("replay_button"),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Text(
                        text = "Replay Level",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF546E7A)
                    )
                }
            }
        }
    }
}

@Composable
fun TargetScoreProgressBar(
    currentScore: Int,
    targetScore: Int,
    gridWidth: Int,
    gridHeight: Int,
    colorCount: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (targetScore > 0) (currentScore.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f) else 1f
    val isCleared = currentScore >= targetScore

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCleared) Color(0xFFE8F5E9) else Color.White
        ),
        border = if (isCleared) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF00E676)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("target_score_bar")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isCleared) "⭐ TARGET CLEARED!" else "🎯 TARGET SCORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCleared) Color(0xFF2E7D32) else Color(0xFF0277BD)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$currentScore / $targetScore pts",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF37474F)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFECEFF1)
                ) {
                    Text(
                        text = "${gridWidth}x${gridHeight} • ${colorCount}🎨",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF455A64),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFE0E0E0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = progress)
                        .height(8.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFB300),
                                    if (isCleared) Color(0xFF00E676) else Color(0xFFFF7043)
                                )
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun FailDialog(
    costumeId: String,
    onReplay: () -> Unit,
    onBack: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fail_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TRY AGAIN!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF5252)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sad Bloki
                BlokiCharacter(
                    costumeId = costumeId,
                    mood = BlokiMood.SAD,
                    size = 110.dp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "You ran out of moves!\nDon't worry, you can do it!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF546E7A),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onReplay,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(54.dp)
                        .testTag("retry_level_button"),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Retry",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RETRY",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBack,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(48.dp)
                        .testTag("back_to_map_button"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "Level Map",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF546E7A)
                    )
                }
            }
        }
    }
}

@Composable
fun PauseDialog(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onLevelSelect: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pause_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "PAUSED",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0277BD)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(52.dp)
                        .testTag("resume_button"),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text(
                        text = "RESUME",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp)
                        .testTag("restart_button"),
                    shape = RoundedCornerShape(25.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00B0FF))
                ) {
                    Text(
                        text = "RESTART",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLevelSelect,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(48.dp)
                        .testTag("quit_to_map_button"),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = "LEVEL MAP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF546E7A)
                    )
                }
            }
        }
    }
}
