package com.example.game.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.level.LevelCatalog

@Composable
fun DevToolsDialog(
    onDismiss: () -> Unit,
    onJumpToLevel: (Int) -> Unit,
    onAddCoins: () -> Unit,
    onUnlockAllLevels: () -> Unit,
    onResetProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputLevelText by remember { mutableStateOf("1") }
    var solvabilityStatus by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = modifier
                .fillMaxWidth()
                .testTag("dev_tools_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🛠️ DEVELOPER TOOLS",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFD32F2F)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Jump to level input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputLevelText,
                        onValueChange = { inputLevelText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Level (1-100+)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dev_level_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val lvl = inputLevelText.toIntOrNull() ?: 1
                            onJumpToLevel(lvl)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0288D1)),
                        modifier = Modifier.testTag("dev_jump_button")
                    ) {
                        Text("Jump", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Solvability check button
                Button(
                    onClick = {
                        val lvl = inputLevelText.toIntOrNull() ?: 1
                        val def = LevelCatalog.getLevel(lvl)
                        val solvable = LevelCatalog.validateSolvability(def)
                        solvabilityStatus = if (solvable) "✅ Level $lvl is SOLVABLE!" else "❌ Level $lvl is NOT solvable!"
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_check_solvability"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2))
                ) {
                    Text("Check Solvability", fontWeight = FontWeight.Bold)
                }

                solvabilityStatus?.let { status ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = status,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (status.startsWith("✅")) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Coins
                Button(
                    onClick = onAddCoins,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_add_coins_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100))
                ) {
                    Text("+500 Coins 🪙", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Unlock All Levels
                Button(
                    onClick = onUnlockAllLevels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_unlock_all_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("Unlock All 20 Levels ⭐", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Reset Progress
                Button(
                    onClick = onResetProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dev_reset_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Reset All Save Data", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Dev Tools", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
