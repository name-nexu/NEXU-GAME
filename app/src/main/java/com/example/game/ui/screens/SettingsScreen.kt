package com.example.game.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.game.data.entity.PlayerProfileEntity
import com.example.ui.theme.BlokiCyan

@Composable
fun SettingsScreen(
    profile: PlayerProfileEntity?,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleLargeText: (Boolean) -> Unit,
    onToggleHighContrast: (Boolean) -> Unit,
    onOpenDevTools: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val soundEnabled = profile?.soundEnabled ?: true
    val musicEnabled = profile?.musicEnabled ?: true
    val hapticsEnabled = profile?.hapticsEnabled ?: true
    val largeText = profile?.largeTextEnabled ?: false
    val highContrast = profile?.highContrastEnabled ?: false

    var devTapCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA),
                        Color(0xFFECEFF1),
                        Color(0xFFFFF9C4)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White, CircleShape)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF263238)
                )
            }

            Spacer(modifier = Modifier.size(16.dp))

            Text(
                text = "SETTINGS",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF0277BD)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Audio & Haptics Section
        Text(
            text = "AUDIO & HAPTICS",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF546E7A),
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsToggleRow(
                    title = "Sound Effects",
                    subtitle = "Taps, pops, breaks & explosions",
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    checked = soundEnabled,
                    onCheckedChange = onToggleSound,
                    tag = "toggle_sound"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsToggleRow(
                    title = "Cheerful Music",
                    subtitle = "Playful background melody",
                    icon = Icons.Default.MusicNote,
                    checked = musicEnabled,
                    onCheckedChange = onToggleMusic,
                    tag = "toggle_music"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsToggleRow(
                    title = "Vibration & Haptics",
                    subtitle = "Tactile bounce feeling",
                    icon = Icons.Default.Vibration,
                    checked = hapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    tag = "toggle_haptics"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Accessibility Section
        Text(
            text = "ACCESSIBILITY",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF546E7A),
            modifier = Modifier.padding(start = 8.dp, bottom = 6.dp)
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsToggleRow(
                    title = "Large Text",
                    subtitle = "Increases button and label sizes",
                    icon = Icons.Default.Build,
                    checked = largeText,
                    onCheckedChange = onToggleLargeText,
                    tag = "toggle_large_text"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsToggleRow(
                    title = "High Contrast",
                    subtitle = "Sharper borders for block visibility",
                    icon = Icons.Default.Security,
                    checked = highContrast,
                    onCheckedChange = onToggleHighContrast,
                    tag = "toggle_high_contrast"
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Kid Safety Guarantee
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🛡️", fontSize = 32.sp)
                Spacer(modifier = Modifier.size(12.dp))
                Column {
                    Text(
                        text = "100% Kid Safe & Offline",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                    Text(
                        text = "No ads, no tracking, no real-money purchases. Built with love for children 5–12 years old.",
                        fontSize = 12.sp,
                        color = Color(0xFF388E3C)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Version & Developer Tools Trigger (Debug builds only)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = BuildConfig.DEBUG) {
                    devTapCount++
                    if (devTapCount >= 4) {
                        devTapCount = 0
                        onOpenDevTools()
                    }
                }
                .testTag("version_label"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Blocky Buddies v1.0.0",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF78909C)
            )
            if (BuildConfig.DEBUG) {
                Text(
                    text = "(Tap here 4 times for Developer Mode)",
                    fontSize = 11.sp,
                    color = Color(0xFFB0BEC5)
                )
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = BlokiCyan,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF263238)
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFF78909C)
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00E676)),
            modifier = Modifier.testTag(tag)
        )
    }
}
