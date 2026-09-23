package com.example.game.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.entity.LevelProgressEntity
import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.model.WorldCatalog
import com.example.ui.theme.BlokiCyan
import com.example.ui.theme.BlokiPink
import com.example.ui.theme.StarGold

@Composable
fun LevelSelectScreen(
    profile: PlayerProfileEntity?,
    levelsProgress: List<LevelProgressEntity>,
    onSelectLevel: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalStars = profile?.totalStars ?: 0
    val currentLevel = profile?.currentLevel ?: 1

    var selectedWorldIndex by remember { mutableIntStateOf(0) }
    val worlds = WorldCatalog.worlds
    val currentWorld = worlds[selectedWorldIndex]

    val infiniteTransition = rememberInfiniteTransition(label = "level_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE1F5FE),
                        Color(0xFFF3E5F5),
                        Color(0xFFE8F5E9)
                    )
                )
            )
            .testTag("level_select_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape)
                    .testTag("level_select_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Home",
                    tint = Color(0xFF263238)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "ADVENTURE MAP",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0277BD)
                )
                Text(
                    text = "${currentWorld.emoji} ${currentWorld.name}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlokiPink
                )
            }

            // Total Stars Pill
            StatusChip(
                icon = "⭐",
                count = totalStars.toString(),
                bgColor = Color(0xFFFFF8E1),
                textColor = Color(0xFFF57F17),
                tag = "level_select_stars"
            )
        }

        // World Selector Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedWorldIndex,
            edgePadding = 16.dp,
            containerColor = Color.Transparent,
            divider = {}
        ) {
            worlds.forEachIndexed { index, world ->
                val isWorldUnlocked = totalStars >= world.requiredStars
                Tab(
                    selected = selectedWorldIndex == index,
                    onClick = { selectedWorldIndex = index },
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedWorldIndex == index) BlokiCyan else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = world.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "W${world.id}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedWorldIndex == index) Color.White else Color(0xFF37474F)
                            )
                            if (!isWorldUnlocked) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = if (selectedWorldIndex == index) Color.White else Color(0xFF9E9E9E),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Check if current world is unlocked
        val isCurrentWorldUnlocked = totalStars >= currentWorld.requiredStars
        if (!isCurrentWorldUnlocked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔒", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "${currentWorld.name} is Locked!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF37474F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Collect ${currentWorld.requiredStars} ⭐ to unlock this world!",
                            fontSize = 15.sp,
                            color = Color(0xFF78909C)
                        )
                        Text(
                            text = "(You currently have $totalStars ⭐)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFF57F17)
                        )
                    }
                }
            }
        } else {
            // Level Grid (20 levels per world)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("level_nodes_grid")
            ) {
                val worldLevels = (currentWorld.startLevel..currentWorld.endLevel).toList()
                items(worldLevels.size) { index ->
                    val levelNum = worldLevels[index]
                    val progress = levelsProgress.firstOrNull { it.levelNumber == levelNum }
                    val isUnlocked = progress?.unlocked == true || levelNum == 1 || levelNum <= currentLevel
                    val starsEarned = progress?.stars ?: 0
                    val isNextToPlay = isUnlocked && (progress == null || !progress.completed) && levelNum == currentLevel

                    LevelNode(
                        levelNumber = levelNum,
                        isUnlocked = isUnlocked,
                        starsEarned = starsEarned,
                        isNextToPlay = isNextToPlay,
                        pulseScale = if (isNextToPlay) pulseScale else 1f,
                        onClick = {
                            if (isUnlocked) onSelectLevel(levelNum)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LevelNode(
    levelNumber: Int,
    isUnlocked: Boolean,
    starsEarned: Int,
    isNextToPlay: Boolean,
    pulseScale: Float,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.testTag("level_node_$levelNumber")
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .scale(pulseScale)
                .background(
                    color = when {
                        !isUnlocked -> Color(0xFFCFD8DC)
                        isNextToPlay -> Color(0xFF00E676)
                        starsEarned > 0 -> Color(0xFF00B0FF)
                        else -> Color(0xFFFFD54F)
                    },
                    shape = CircleShape
                )
                .then(
                    if (isNextToPlay) {
                        Modifier.border(3.dp, Color.White, CircleShape)
                    } else Modifier
                )
                .clickable(enabled = isUnlocked, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked Level",
                    tint = Color(0xFF78909C),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = levelNumber.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Star indicator row
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (i in 1..3) {
                val filled = isUnlocked && starsEarned >= i
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Star $i",
                    tint = if (filled) StarGold else Color(0x33000000),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
