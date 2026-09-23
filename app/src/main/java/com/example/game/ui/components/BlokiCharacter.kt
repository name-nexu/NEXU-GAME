package com.example.game.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class BlokiMood {
    HAPPY,
    EXCITED,
    SAD,
    WINKING
}

@Composable
fun BlokiCharacter(
    costumeId: String = "classic",
    mood: BlokiMood = BlokiMood.HAPPY,
    size: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bloki_anim")

    // Cute breathing / bounce offset
    val bounceOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )

    // Eye blinking
    val eyeScaleY by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val bounce = if (mood == BlokiMood.EXCITED) bounceOffset * 2.2f else bounceOffset

            // Body Cube Drop Shadow
            drawRoundRect(
                color = Color(0x33000000),
                topLeft = Offset(w * 0.1f, h * 0.18f + bounce + 6f),
                size = Size(w * 0.8f, h * 0.72f),
                cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)
            )

            // Bloki Main Cube Body Gradient (Warm Golden Yellow / Amber)
            val bodyBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFFFB300)),
                startY = h * 0.18f + bounce,
                endY = h * 0.90f + bounce
            )
            drawRoundRect(
                brush = bodyBrush,
                topLeft = Offset(w * 0.1f, h * 0.18f + bounce),
                size = Size(w * 0.8f, h * 0.72f),
                cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)
            )

            // Top Highlight Bevel
            drawRoundRect(
                color = Color(0x40FFFFFF),
                topLeft = Offset(w * 0.15f, h * 0.22f + bounce),
                size = Size(w * 0.7f, h * 0.18f),
                cornerRadius = CornerRadius(w * 0.12f, h * 0.12f)
            )

            // Cheeks (Rosy blush)
            drawCircle(
                color = Color(0xFFFF8A80),
                radius = w * 0.08f,
                center = Offset(w * 0.22f, h * 0.62f + bounce)
            )
            drawCircle(
                color = Color(0xFFFF8A80),
                radius = w * 0.08f,
                center = Offset(w * 0.78f, h * 0.62f + bounce)
            )

            // Eyes
            when (mood) {
                BlokiMood.SAD -> {
                    // Sad downward arc eyes
                    val eyePath1 = Path().apply {
                        moveTo(w * 0.30f, h * 0.48f + bounce)
                        quadraticTo(w * 0.38f, h * 0.42f + bounce, w * 0.44f, h * 0.48f + bounce)
                    }
                    val eyePath2 = Path().apply {
                        moveTo(w * 0.56f, h * 0.48f + bounce)
                        quadraticTo(w * 0.62f, h * 0.42f + bounce, w * 0.70f, h * 0.48f + bounce)
                    }
                    drawPath(eyePath1, color = Color(0xFF263238), style = Stroke(width = w * 0.06f))
                    drawPath(eyePath2, color = Color(0xFF263238), style = Stroke(width = w * 0.06f))

                    // Sad mouth
                    val mouthPath = Path().apply {
                        moveTo(w * 0.42f, h * 0.70f + bounce)
                        quadraticTo(w * 0.50f, h * 0.64f + bounce, w * 0.58f, h * 0.70f + bounce)
                    }
                    drawPath(mouthPath, color = Color(0xFF263238), style = Stroke(width = w * 0.05f))
                }

                else -> {
                    // Big cute friendly eyes
                    drawCircle(
                        color = Color(0xFF263238),
                        radius = w * 0.085f,
                        center = Offset(w * 0.35f, h * 0.48f + bounce)
                    )
                    drawCircle(
                        color = Color(0xFF263238),
                        radius = w * 0.085f,
                        center = Offset(w * 0.65f, h * 0.48f + bounce)
                    )

                    // Eye highlights (sparkles)
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.032f,
                        center = Offset(w * 0.32f, h * 0.45f + bounce)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.032f,
                        center = Offset(w * 0.62f, h * 0.45f + bounce)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.015f,
                        center = Offset(w * 0.37f, h * 0.51f + bounce)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = w * 0.015f,
                        center = Offset(w * 0.67f, h * 0.51f + bounce)
                    )

                    // Happy mouth
                    val mouthPath = Path().apply {
                        moveTo(w * 0.40f, h * 0.62f + bounce)
                        quadraticTo(w * 0.50f, h * 0.72f + bounce, w * 0.60f, h * 0.62f + bounce)
                    }
                    drawPath(mouthPath, color = Color(0xFF263238), style = Stroke(width = w * 0.05f))

                    // Little tongue
                    drawCircle(
                        color = Color(0xFFFF5252),
                        radius = w * 0.035f,
                        center = Offset(w * 0.50f, h * 0.66f + bounce)
                    )
                }
            }

            // Draw Costume Accessories
            drawCostume(costumeId, w, h, bounce)
        }
    }
}

private fun DrawScope.drawCostume(costumeId: String, w: Float, h: Float, bounce: Float) {
    when (costumeId) {
        "superhero" -> {
            // Mask over eyes
            drawRoundRect(
                color = Color(0xFF00B0FF),
                topLeft = Offset(w * 0.20f, h * 0.40f + bounce),
                size = Size(w * 0.60f, h * 0.16f),
                cornerRadius = CornerRadius(w * 0.08f, h * 0.08f)
            )
            // Mask Eye Cutouts
            drawCircle(Color(0xFF263238), w * 0.065f, Offset(w * 0.35f, h * 0.48f + bounce))
            drawCircle(Color(0xFF263238), w * 0.065f, Offset(w * 0.65f, h * 0.48f + bounce))
            drawCircle(Color.White, w * 0.025f, Offset(w * 0.33f, h * 0.46f + bounce))
            drawCircle(Color.White, w * 0.025f, Offset(w * 0.63f, h * 0.46f + bounce))

            // Superhero Emblem on chest
            drawCircle(Color(0xFFFF1744), w * 0.06f, Offset(w * 0.50f, h * 0.76f + bounce))
            drawCircle(Color(0xFFFFD600), w * 0.035f, Offset(w * 0.50f, h * 0.76f + bounce))
        }

        "prince" -> {
            // Golden Crown
            val crownPath = Path().apply {
                moveTo(w * 0.28f, h * 0.20f + bounce)
                lineTo(w * 0.22f, h * 0.04f + bounce)
                lineTo(w * 0.38f, h * 0.12f + bounce)
                lineTo(w * 0.50f, h * 0.01f + bounce)
                lineTo(w * 0.62f, h * 0.12f + bounce)
                lineTo(w * 0.78f, h * 0.04f + bounce)
                lineTo(w * 0.72f, h * 0.20f + bounce)
                close()
            }
            drawPath(crownPath, color = Color(0xFFFFD700))
            drawPath(crownPath, color = Color(0xFFFFAB00), style = Stroke(w * 0.02f))
            // Crown jewels
            drawCircle(Color(0xFFFF1744), w * 0.025f, Offset(w * 0.22f, h * 0.05f + bounce))
            drawCircle(Color(0xFF00E5FF), w * 0.03f, Offset(w * 0.50f, h * 0.02f + bounce))
            drawCircle(Color(0xFFFF1744), w * 0.025f, Offset(w * 0.78f, h * 0.05f + bounce))
        }

        "dino" -> {
            // Cute Dino Spikes
            val spikeColor = Color(0xFF00E676)
            for (i in 0..2) {
                val cx = w * (0.35f + i * 0.15f)
                val spikePath = Path().apply {
                    moveTo(cx - w * 0.06f, h * 0.18f + bounce)
                    lineTo(cx, h * 0.06f + bounce)
                    lineTo(cx + w * 0.06f, h * 0.18f + bounce)
                    close()
                }
                drawPath(spikePath, color = spikeColor)
            }
        }

        "astronaut" -> {
            // Space Helmet Glass Visor
            drawRoundRect(
                color = Color(0x7000E5FF),
                topLeft = Offset(w * 0.18f, h * 0.34f + bounce),
                size = Size(w * 0.64f, h * 0.36f),
                cornerRadius = CornerRadius(w * 0.16f, h * 0.16f)
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(w * 0.18f, h * 0.34f + bounce),
                size = Size(w * 0.64f, h * 0.36f),
                cornerRadius = CornerRadius(w * 0.16f, h * 0.16f),
                style = Stroke(w * 0.035f)
            )
            // Visor shine
            drawLine(
                color = Color(0x99FFFFFF),
                start = Offset(w * 0.25f, h * 0.38f + bounce),
                end = Offset(w * 0.45f, h * 0.38f + bounce),
                strokeWidth = w * 0.03f
            )
        }

        "party" -> {
            // Party Cone Hat
            val conePath = Path().apply {
                moveTo(w * 0.32f, h * 0.18f + bounce)
                lineTo(w * 0.50f, h * -0.05f + bounce)
                lineTo(w * 0.68f, h * 0.18f + bounce)
                close()
            }
            drawPath(conePath, color = Color(0xFFFF4081))
            // Pompom on top
            drawCircle(Color(0xFFFFEA00), w * 0.05f, Offset(w * 0.50f, h * -0.05f + bounce))
            // Hat stripes
            drawCircle(Color.White, w * 0.025f, Offset(w * 0.45f, h * 0.05f + bounce))
            drawCircle(Color.White, w * 0.025f, Offset(w * 0.55f, h * 0.10f + bounce))
        }

        "wizard" -> {
            // Wizard Pointy Hat
            val hatPath = Path().apply {
                moveTo(w * 0.15f, h * 0.20f + bounce)
                lineTo(w * 0.50f, h * -0.08f + bounce)
                lineTo(w * 0.85f, h * 0.20f + bounce)
                close()
            }
            drawPath(hatPath, color = Color(0xFF7C4DFF))
            // Hat brim
            drawRoundRect(
                color = Color(0xFF651FFF),
                topLeft = Offset(w * 0.10f, h * 0.17f + bounce),
                size = Size(w * 0.80f, h * 0.06f),
                cornerRadius = CornerRadius(w * 0.03f, h * 0.03f)
            )
            // Star on hat
            drawCircle(Color(0xFFFFEA00), w * 0.04f, Offset(w * 0.50f, h * 0.06f + bounce))
        }

        "cool" -> {
            // Sunglasses
            val glassColor = Color(0xFF212121)
            drawRoundRect(
                color = glassColor,
                topLeft = Offset(w * 0.22f, h * 0.42f + bounce),
                size = Size(w * 0.25f, h * 0.14f),
                cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
            )
            drawRoundRect(
                color = glassColor,
                topLeft = Offset(w * 0.53f, h * 0.42f + bounce),
                size = Size(w * 0.25f, h * 0.14f),
                cornerRadius = CornerRadius(w * 0.04f, h * 0.04f)
            )
            // Bridge
            drawLine(
                color = glassColor,
                start = Offset(w * 0.47f, h * 0.46f + bounce),
                end = Offset(w * 0.53f, h * 0.46f + bounce),
                strokeWidth = w * 0.035f
            )
            // Glare
            drawLine(
                color = Color(0x66FFFFFF),
                start = Offset(w * 0.25f, h * 0.45f + bounce),
                end = Offset(w * 0.32f, h * 0.51f + bounce),
                strokeWidth = w * 0.02f
            )
        }
    }
}
