package com.example.game.ui.screens

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.data.entity.PlayerProfileEntity
import com.example.game.model.CostumeCatalog
import com.example.game.ui.components.BlokiCharacter
import com.example.game.ui.components.BlokiMood
import com.example.ui.theme.BlokiCyan
import com.example.ui.theme.BlokiPink

@Composable
fun CostumesScreen(
    profile: PlayerProfileEntity?,
    onEquipCostume: (String) -> Unit,
    onBuyCostume: (String, Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coins = profile?.coins ?: 0
    val equippedCostume = profile?.selectedCostume ?: "classic"
    val unlockedCostumes = profile?.unlockedCostumes?.split(",")?.toSet() ?: setOf("classic")

    var previewCostume by remember(equippedCostume) { mutableStateOf(equippedCostume) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF9C4),
                        Color(0xFFFFECB3),
                        Color(0xFFFFCC80)
                    )
                )
            )
            .testTag("costumes_screen")
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
                    .testTag("costumes_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF263238)
                )
            }

            Text(
                text = "BLOKI'S WARDROBE",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFE65100)
            )

            StatusChip(
                icon = "🪙",
                count = coins.toString(),
                bgColor = Color.White,
                textColor = Color(0xFFE65100),
                tag = "costumes_coins"
            )
        }

        // Live Bloki Preview Podium
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BlokiCharacter(
                    costumeId = previewCostume,
                    mood = BlokiMood.HAPPY,
                    size = 130.dp
                )

                val selectedCostumeObj = CostumeCatalog.allCostumes.firstOrNull { it.id == previewCostume }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = selectedCostumeObj?.name ?: "Bloki",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF263238)
                )

                Text(
                    text = selectedCostumeObj?.description ?: "",
                    fontSize = 13.sp,
                    color = Color(0xFF78909C),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                val isUnlocked = unlockedCostumes.contains(previewCostume)
                val isEquipped = equippedCostume == previewCostume

                if (isEquipped) {
                    Button(
                        onClick = {},
                        enabled = false,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFE0E0E0))
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF43A047))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "EQUIPPED", color = Color(0xFF43A047), fontWeight = FontWeight.Bold)
                    }
                } else if (isUnlocked) {
                    Button(
                        onClick = { onEquipCostume(previewCostume) },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        modifier = Modifier.testTag("equip_costume_button")
                    ) {
                        Text(text = "EQUIP COSTUME", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    val price = selectedCostumeObj?.priceCoins ?: 100
                    val canAfford = coins >= price
                    Button(
                        onClick = { onBuyCostume(previewCostume, price) },
                        enabled = canAfford,
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9100)),
                        modifier = Modifier.testTag("buy_costume_button")
                    ) {
                        Text(
                            text = "UNLOCK FOR $price 🪙",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Costumes Selection Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(CostumeCatalog.allCostumes.size) { index ->
                val costume = CostumeCatalog.allCostumes[index]
                val isUnlocked = unlockedCostumes.contains(costume.id)
                val isSelected = previewCostume == costume.id
                val isEquipped = equippedCostume == costume.id

                Card(
                    modifier = Modifier
                        .clickable { previewCostume = costume.id }
                        .then(
                            if (isSelected) Modifier.border(3.dp, BlokiCyan, RoundedCornerShape(16.dp))
                            else Modifier
                        )
                        .testTag("costume_item_${costume.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = costume.iconEmoji, fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = costume.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF263238),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        if (isEquipped) {
                            Text(text = "Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                        } else if (isUnlocked) {
                            Text(text = "Owned", fontSize = 10.sp, color = Color(0xFF78909C))
                        } else {
                            Text(text = "${costume.priceCoins} 🪙", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                        }
                    }
                }
            }
        }
    }
}
