package com.example.game.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.entity.AchievementEntity
import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.ui.components.BlokiCharacter
import com.example.game.ui.components.BlokiMood
import com.example.ui.theme.BlokiPink

@Composable
fun RewardsScreen(
    profile: PlayerProfileEntity?,
    achievements: List<AchievementEntity>,
    onClaimReward: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val costumeId = profile?.selectedCostume ?: "classic"
    val coins = profile?.coins ?: 0
    val totalStars = profile?.totalStars ?: 0
    val currentLevel = profile?.currentLevel ?: 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFCE4EC),
                        Color(0xFFEDE7F6),
                        Color(0xFFE8EAF6)
                    )
                )
            )
            .testTag("rewards_screen")
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
                    .testTag("rewards_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF263238)
                )
            }

            Text(
                text = "TROPHIES & REWARDS",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = BlokiPink
            )

            StatusChip(
                icon = "🪙",
                count = coins.toString(),
                bgColor = Color.White,
                textColor = Color(0xFFE65100),
                tag = "rewards_coins"
            )
        }

        // Profile Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BlokiCharacter(
                    costumeId = costumeId,
                    mood = BlokiMood.HAPPY,
                    size = 76.dp
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Puzzle Champion",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF263238)
                    )
                    Text(
                        text = "Current Stage: Level $currentLevel",
                        fontSize = 13.sp,
                        color = Color(0xFF78909C)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column {
                            Text(text = "⭐ Stars", fontSize = 11.sp, color = Color(0xFF90A4AE))
                            Text(text = "$totalStars", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFF57F17))
                        }
                        Column {
                            Text(text = "🪙 Coins", fontSize = 11.sp, color = Color(0xFF90A4AE))
                            Text(text = "$coins", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Achievements",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF37474F),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
        )

        // Achievements List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(achievements.size) { index ->
                val ach = achievements[index]
                val canClaim = ach.unlocked && ach.rewardCoins > 0
                val isClaimed = ach.unlocked && ach.rewardCoins == 0
                val progressFraction = (ach.progress.toFloat() / ach.target.toFloat()).coerceIn(0f, 1f)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("achievement_${ach.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isClaimed) Color(0xFFF1F8E9) else Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = when (ach.iconName) {
                                    "star" -> "⭐"
                                    "stars" -> "🌟"
                                    "trophy" -> "🏆"
                                    "coin" -> "🪙"
                                    else -> "✨"
                                },
                                fontSize = 32.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = ach.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF263238)
                                )
                                Text(
                                    text = ach.description,
                                    fontSize = 12.sp,
                                    color = Color(0xFF78909C)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { progressFraction },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .height(6.dp),
                                    color = Color(0xFF00E676),
                                    trackColor = Color(0xFFECEFF1),
                                )
                                Text(
                                    text = "${ach.progress}/${ach.target}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF90A4AE),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        if (canClaim) {
                            Button(
                                onClick = { onClaimReward(ach.id) },
                                shape = RoundedCornerShape(18.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                                modifier = Modifier.testTag("claim_${ach.id}")
                            ) {
                                Text(
                                    text = "+${ach.rewardCoins} 🪙",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        } else if (isClaimed) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Claimed",
                                tint = Color(0xFF00C853),
                                modifier = Modifier.size(28.dp)
                            )
                        } else {
                            Text(
                                text = "+${ach.rewardCoins} 🪙",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            }
        }
    }
}
