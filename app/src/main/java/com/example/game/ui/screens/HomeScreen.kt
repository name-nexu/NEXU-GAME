package com.example.game.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.ui.GameScreen
import com.example.game.ui.components.BlokiCharacter
import com.example.game.ui.components.BlokiMood
import com.example.ui.theme.BlokiCyan
import com.example.ui.theme.BlokiPink
import com.example.ui.theme.BlokiYellow
import com.example.ui.theme.StarGold

@Composable
fun HomeScreen(
    profile: PlayerProfileEntity?,
    onNavigate: (GameScreen) -> Unit,
    onPlayNextLevel: (Int) -> Unit,
    onPlayDaily: () -> Unit,
    onOpenDevTools: () -> Unit,
    onPlayTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLvl = profile?.currentLevel ?: 1
    val coins = profile?.coins ?: 150
    val stars = profile?.totalStars ?: 0
    val costume = profile?.selectedCostume ?: "classic"

    var mascotMood by remember { mutableStateOf(BlokiMood.HAPPY) }

    val infiniteTransition = rememberInfiniteTransition(label = "home_pulse")
    val playPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA),
                        Color(0xFFFFF9C4),
                        Color(0xFFFFEBEE)
                    )
                )
            )
            .testTag("home_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Status Bar: Coins, Stars, Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Coins & Stars Pill
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusChip(
                        icon = "⭐",
                        count = stars.toString(),
                        bgColor = Color(0xFFFFF8E1),
                        textColor = Color(0xFFF57F17),
                        tag = "star_counter"
                    )
                    StatusChip(
                        icon = "🪙",
                        count = coins.toString(),
                        bgColor = Color(0xFFFFF3E0),
                        textColor = Color(0xFFE65100),
                        tag = "coin_counter"
                    )
                }

                // Settings icon button
                IconButton(
                    onClick = { onNavigate(GameScreen.SETTINGS) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.White, CircleShape)
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF455A64)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Title
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "BLOCKY BUDDIES",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0277BD),
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "PUZZLE ADVENTURE",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BlokiPink,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Interactive Bloki Mascot in center
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clickable {
                        onPlayTap()
                        mascotMood = if (mascotMood == BlokiMood.EXCITED) BlokiMood.HAPPY else BlokiMood.EXCITED
                    }
                    .testTag("bloki_mascot_home"),
                contentAlignment = Alignment.Center
            ) {
                BlokiCharacter(
                    costumeId = costume,
                    mood = mascotMood,
                    size = 150.dp
                )
            }

            Text(
                text = "Tap Bloki to tickle him!",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF78909C)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Giant Play Button
            Button(
                onClick = {
                    onPlayTap()
                    onPlayNextLevel(currentLvl)
                },
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(68.dp)
                    .scale(playPulse)
                    .testTag("play_button"),
                shape = RoundedCornerShape(34.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PLAY LEVEL $currentLvl",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Daily Challenge Card
            Card(
                onClick = {
                    onPlayTap()
                    onPlayDaily()
                },
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .testTag("daily_challenge_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎁", fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Daily Challenge",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF263238)
                            )
                            Text(
                                text = "Special puzzle for +50 coins!",
                                fontSize = 12.sp,
                                color = Color(0xFF689F38)
                            )
                        }
                    }
                    Text(
                        text = "PLAY",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BlokiCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Main Navigation Grid: Level Map, Costumes, Rewards
            Row(
                modifier = Modifier.fillMaxWidth(0.95f),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NavCardButton(
                    title = "Map",
                    icon = Icons.Default.Map,
                    badge = "World 1",
                    color = BlokiCyan,
                    onClick = { onNavigate(GameScreen.LEVEL_SELECT) },
                    tag = "nav_map_button"
                )
                NavCardButton(
                    title = "Costumes",
                    icon = Icons.Default.Face,
                    badge = "Wardrobe",
                    color = BlokiYellow,
                    onClick = { onNavigate(GameScreen.COSTUMES) },
                    tag = "nav_costumes_button"
                )
                NavCardButton(
                    title = "Rewards",
                    icon = Icons.Default.CardGiftcard,
                    badge = "Trophies",
                    color = BlokiPink,
                    onClick = { onNavigate(GameScreen.REWARDS) },
                    tag = "nav_rewards_button"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun StatusChip(
    icon: String,
    count: String,
    bgColor: Color,
    textColor: Color,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        shadowElevation = 2.dp,
        modifier = Modifier.testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = count,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )
        }
    }
}

@Composable
fun NavCardButton(
    title: String,
    icon: ImageVector,
    badge: String,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .size(width = 96.dp, height = 86.dp)
            .testTag(tag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF263238)
            )
        }
    }
}
