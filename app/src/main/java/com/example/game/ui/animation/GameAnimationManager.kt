package com.example.game.ui.animation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.game.model.BlockColor
import com.example.game.model.PowerUpType
import com.example.ui.theme.CoinAmber
import com.example.ui.theme.StarGold
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Visual state for an active power-up animation.
 */
data class ActivePowerUpEffect(
    val id: Long,
    val type: PowerUpType,
    val targetCol: Int,
    val targetRow: Int,
    val progress: Animatable<Float, *>
)

/**
 * Visual state for an active block break pulse/shockwave.
 */
data class ActiveBlockBreakEffect(
    val id: Long,
    val col: Int,
    val row: Int,
    val color: Color,
    val progress: Animatable<Float, *>
)

/**
 * Confetti particle for celebration victory screens.
 */
private class ConfettiParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    val size: Float,
    var rotation: Float,
    val vRot: Float,
    val maxLifeMs: Long,
    var ageMs: Long = 0L
)

/**
 * Centralized, reusable animation manager for game UI.
 * Handles consistent transitions for:
 * 1. Block-breaking shockwaves and cell pulses
 * 2. Power-up activations (Hammer slam, Rocket blast, Rainbow wave, Magic wand glow, Shuffle swirl)
 * 3. Board shakes
 * 4. Level completion celebrations
 *
 * Fully respects reduced-motion accessibility settings.
 */
@Stable
class GameAnimationManager(
    val reducedMotion: Boolean = false,
    private val scope: CoroutineScope
) {
    var boardShakeOffsetX by mutableFloatStateOf(0f)
        private set
    var boardShakeOffsetY by mutableFloatStateOf(0f)
        private set

    val activePowerUps = mutableStateListOf<ActivePowerUpEffect>()
    val activeBlockBreaks = mutableStateListOf<ActiveBlockBreakEffect>()

    private var effectCounter = 0L

    /**
     * Trigger a consistent block-break shockwave animation.
     */
    fun triggerBlockBreak(col: Int, row: Int, blockColor: BlockColor) {
        if (reducedMotion) return

        val color = when (blockColor) {
            BlockColor.RED -> Color(0xFFFF5252)
            BlockColor.BLUE -> Color(0xFF2979FF)
            BlockColor.GREEN -> Color(0xFF00E676)
            BlockColor.YELLOW -> Color(0xFFFFD600)
            BlockColor.PURPLE -> Color(0xFFAA00FF)
            BlockColor.ORANGE -> Color(0xFFFF6D00)
            else -> Color.White
        }

        val effectId = ++effectCounter
        val anim = Animatable(0f)
        val effect = ActiveBlockBreakEffect(effectId, col, row, color, anim)
        activeBlockBreaks.add(effect)

        scope.launch {
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            )
            activeBlockBreaks.removeAll { it.id == effectId }
        }
    }

    /**
     * Trigger a power-up activation visual effect on the board.
     */
    fun triggerPowerUp(type: PowerUpType, targetCol: Int, targetRow: Int) {
        if (reducedMotion) return

        val effectId = ++effectCounter
        val anim = Animatable(0f)
        val effect = ActivePowerUpEffect(effectId, type, targetCol, targetRow, anim)
        activePowerUps.add(effect)

        // Trigger light camera/board rumble for hammer and rocket
        if (type == PowerUpType.HAMMER || type == PowerUpType.ROCKET) {
            triggerBoardShake(intensity = if (type == PowerUpType.HAMMER) 8f else 5f)
        }

        scope.launch {
            val duration = when (type) {
                PowerUpType.HAMMER -> 350
                PowerUpType.ROCKET -> 400
                PowerUpType.RAINBOW -> 500
                PowerUpType.MAGIC_WAND -> 450
                PowerUpType.SHUFFLE -> 350
            }
            anim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = duration, easing = FastOutSlowInEasing)
            )
            activePowerUps.removeAll { it.id == effectId }
        }
    }

    /**
     * Lightweight camera/board shake on explosive impacts.
     */
    fun triggerBoardShake(intensity: Float = 6f) {
        if (reducedMotion) return

        scope.launch {
            val steps = listOf(
                Offset(-intensity, intensity * 0.5f),
                Offset(intensity * 0.8f, -intensity),
                Offset(-intensity * 0.4f, intensity * 0.6f),
                Offset(intensity * 0.2f, -intensity * 0.2f),
                Offset.Zero
            )
            for (step in steps) {
                boardShakeOffsetX = step.x
                boardShakeOffsetY = step.y
                delay(30)
            }
            boardShakeOffsetX = 0f
            boardShakeOffsetY = 0f
        }
    }

    /**
     * Clean up all active animation states.
     */
    fun clearAll() {
        activePowerUps.clear()
        activeBlockBreaks.clear()
        boardShakeOffsetX = 0f
        boardShakeOffsetY = 0f
    }
}

/**
 * Creates and remembers a [GameAnimationManager] instance attached to the current coroutine scope.
 */
@Composable
fun rememberGameAnimationManager(reducedMotion: Boolean = false): GameAnimationManager {
    val scope = rememberCoroutineScope()
    return remember(reducedMotion) {
        GameAnimationManager(reducedMotion = reducedMotion, scope = scope)
    }
}

/**
 * Canvas layer that draws active block-break shockwaves and power-up activation animations.
 * Completely decoupled from game physics and optimized for zero allocations during drawing.
 */
@Composable
fun GameAnimationOverlay(
    manager: GameAnimationManager,
    gridWidth: Int,
    gridHeight: Int,
    cellSize: Float,
    cellPadding: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    if (manager.activeBlockBreaks.isEmpty() && manager.activePowerUps.isEmpty()) {
        return
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // 1. Draw block break pulses & expanding rings
        for (breakEffect in manager.activeBlockBreaks) {
            val p = breakEffect.progress.value
            val cellX = offsetX + breakEffect.col * (cellSize + cellPadding)
            val cellY = offsetY + breakEffect.row * (cellSize + cellPadding)
            val center = Offset(cellX + cellSize / 2f, cellY + cellSize / 2f)

            // Flash square
            val flashAlpha = (1f - p) * 0.5f
            drawRoundRect(
                color = breakEffect.color.copy(alpha = flashAlpha),
                topLeft = Offset(cellX, cellY),
                size = Size(cellSize, cellSize),
                cornerRadius = CornerRadius(cellSize * 0.22f, cellSize * 0.22f)
            )

            // Expanding shockwave circle
            val ringRadius = (cellSize * 0.4f) + (cellSize * 0.8f * p)
            val ringAlpha = (1f - p).coerceIn(0f, 1f)
            drawCircle(
                color = breakEffect.color.copy(alpha = ringAlpha),
                radius = ringRadius,
                center = center,
                style = Stroke(width = (4f * (1f - p)).coerceAtLeast(1f))
            )
        }

        // 2. Draw power-up activation visual effects
        for (powerUp in manager.activePowerUps) {
            val p = powerUp.progress.value
            val cellX = offsetX + powerUp.targetCol * (cellSize + cellPadding)
            val cellY = offsetY + powerUp.targetRow * (cellSize + cellPadding)
            val center = Offset(cellX + cellSize / 2f, cellY + cellSize / 2f)

            when (powerUp.type) {
                PowerUpType.HAMMER -> {
                    // Hammer slam shockwave rings
                    val hammerRadius = cellSize * 2.2f * p
                    val alpha = (1f - p).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color(0xFFFFB300).copy(alpha = alpha),
                        radius = hammerRadius,
                        center = center,
                        style = Stroke(width = 6f * (1f - p))
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = alpha * 0.7f),
                        radius = hammerRadius * 0.7f,
                        center = center,
                        style = Stroke(width = 4f * (1f - p))
                    )
                }

                PowerUpType.ROCKET -> {
                    // Vertical blast beam
                    val beamWidth = cellSize * (1f - p * 0.4f)
                    val alpha = (1f - p).coerceIn(0f, 1f)
                    val beamTop = offsetY
                    val beamHeight = gridHeight * (cellSize + cellPadding)
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFFFF5252).copy(alpha = alpha), Color(0xFFFFD600).copy(alpha = alpha * 0.5f))
                        ),
                        topLeft = Offset(center.x - beamWidth / 2f, beamTop),
                        size = Size(beamWidth, beamHeight)
                    )
                }

                PowerUpType.RAINBOW -> {
                    // Expansive chromatic ripple across board
                    val maxBoardDim = (gridWidth + gridHeight) * cellSize
                    val rippleRadius = maxBoardDim * p
                    val alpha = (1f - p).coerceIn(0f, 1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF5252).copy(alpha = alpha * 0.6f),
                                Color(0xFFFFD600).copy(alpha = alpha * 0.6f),
                                Color(0xFF00E676).copy(alpha = alpha * 0.6f),
                                Color(0xFF00B0FF).copy(alpha = alpha * 0.6f),
                                Color(0xFFAA00FF).copy(alpha = 0f)
                            ),
                            center = center,
                            radius = (rippleRadius + 1f).coerceAtLeast(10f)
                        ),
                        radius = rippleRadius,
                        center = center
                    )
                }

                PowerUpType.MAGIC_WAND -> {
                    // Twinkling orbital sparkle burst
                    val wandRadius = cellSize * 1.8f * p
                    val alpha = (1f - p).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color(0xFFE040FB).copy(alpha = alpha * 0.4f),
                        radius = wandRadius,
                        center = center
                    )
                    for (i in 0 until 6) {
                        val angle = (i * 60f + p * 180f) * (Math.PI / 180f)
                        val sx = center.x + (wandRadius * 0.85f) * cos(angle).toFloat()
                        val sy = center.y + (wandRadius * 0.85f) * sin(angle).toFloat()
                        drawCircle(
                            color = Color(0xFFFFD700).copy(alpha = alpha),
                            radius = 5f * (1f - p),
                            center = Offset(sx, sy)
                        )
                    }
                }

                PowerUpType.SHUFFLE -> {
                    // Swirl board ripple
                    val boardCenterX = offsetX + (gridWidth * (cellSize + cellPadding)) / 2f
                    val boardCenterY = offsetY + (gridHeight * (cellSize + cellPadding)) / 2f
                    val swirlRadius = cellSize * 3f * p
                    val alpha = (1f - p).coerceIn(0f, 1f)
                    drawCircle(
                        color = Color(0xFF00B0FF).copy(alpha = alpha * 0.35f),
                        radius = swirlRadius,
                        center = Offset(boardCenterX, boardCenterY),
                        style = Stroke(width = 8f * (1f - p))
                    )
                }
            }
        }
    }
}

/**
 * Staggered, animated star rating reveal for level completion.
 * Each star drops in with an elastic spring pop.
 */
@Composable
fun AnimatedStarRating(
    starsEarned: Int,
    maxStars: Int = 3,
    starSize: Dp = 38.dp,
    reducedMotion: Boolean = false,
    onStarRevealed: ((Int) -> Unit)? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val isEarned = starsEarned >= i
            val scale = remember { Animatable(if (reducedMotion) (if (isEarned) 1f else 0.85f) else 0f) }

            LaunchedEffect(starsEarned, isEarned) {
                if (reducedMotion) {
                    scale.snapTo(if (isEarned) 1f else 0.85f)
                } else if (isEarned) {
                    // Stagger reveal by star index
                    delay((i * 180L))
                    scale.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    onStarRevealed?.invoke(i)
                } else {
                    delay(100L)
                    scale.animateTo(0.85f, animationSpec = tween(200))
                }
            }

            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Star $i",
                tint = if (isEarned) StarGold else Color(0xFFCFD8DC),
                modifier = Modifier
                    .size(starSize)
                    .scale(scale.value)
            )
        }
    }
}

/**
 * Animated rolling counter for score and coin reveals.
 * Smoothly counts from 0 up to targetValue with easing.
 */
@Composable
fun AnimatedCounter(
    targetValue: Int,
    durationMs: Int = 800,
    prefix: String = "",
    suffix: String = "",
    textStyle: TextStyle = TextStyle.Default,
    reducedMotion: Boolean = false
) {
    var displayValue by remember { mutableIntStateOf(if (reducedMotion) targetValue else 0) }

    LaunchedEffect(targetValue) {
        if (reducedMotion || targetValue <= 0) {
            displayValue = targetValue
            return@LaunchedEffect
        }

        val startTime = System.currentTimeMillis()
        while (true) {
            val elapsed = System.currentTimeMillis() - startTime
            val progress = (elapsed.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
            val easedProgress = FastOutSlowInEasing.transform(progress)
            displayValue = (targetValue * easedProgress).toInt()

            if (progress >= 1f) {
                displayValue = targetValue
                break
            }
            delay(16) // ~60 FPS
        }
    }

    Text(
        text = "$prefix$displayValue$suffix",
        style = textStyle
    )
}

/**
 * High-performance 2D Canvas confetti burst for celebration modals.
 * Self-terminating, zero persistent background overhead.
 */
@Composable
fun VictoryConfetti(
    modifier: Modifier = Modifier,
    particleCount: Int = 30,
    durationMs: Long = 2000L,
    reducedMotion: Boolean = false
) {
    if (reducedMotion) return

    val confetti = remember {
        val rand = Random(System.currentTimeMillis())
        val colors = listOf(
            StarGold,
            CoinAmber,
            Color(0xFF00E676),
            Color(0xFF00B0FF),
            Color(0xFFFF4081),
            Color(0xFF7C4DFF)
        )
        (0 until particleCount).map {
            ConfettiParticle(
                x = rand.nextFloat(),
                y = -0.1f - rand.nextFloat() * 0.3f,
                vx = (rand.nextFloat() - 0.5f) * 0.6f,
                vy = 0.4f + rand.nextFloat() * 0.7f,
                color = colors[rand.nextInt(colors.size)],
                size = 8f + rand.nextFloat() * 10f,
                rotation = rand.nextFloat() * 360f,
                vRot = (rand.nextFloat() - 0.5f) * 360f,
                maxLifeMs = durationMs
            )
        }.toMutableList()
    }

    var isFinished by remember { mutableStateOf(false) }

    if (isFinished) return

    LaunchedEffect(Unit) {
        var lastNano = System.nanoTime()
        while (!isFinished) {
            withFrameNanos { now ->
                val dt = ((now - lastNano) / 1_000_000_000f).coerceIn(0.001f, 0.05f)
                lastNano = now
                val dtMs = (dt * 1000f).toLong()

                var anyAlive = false
                for (p in confetti) {
                    p.ageMs += dtMs
                    if (p.ageMs < p.maxLifeMs) {
                        anyAlive = true
                        p.x += p.vx * dt
                        p.y += p.vy * dt
                        p.vy += 0.9f * dt // gravity
                        p.rotation += p.vRot * dt
                    }
                }
                if (!anyAlive) {
                    isFinished = true
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (p in confetti) {
            if (p.ageMs < p.maxLifeMs) {
                val alpha = (1f - (p.ageMs.toFloat() / p.maxLifeMs.toFloat())).coerceIn(0f, 1f)
                val px = p.x * w
                val py = p.y * h

                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size / 2f,
                    center = Offset(px, py)
                )
            }
        }
    }
}
