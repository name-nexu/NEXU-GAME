package com.example.game.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import com.example.game.engine.GameState
import com.example.game.engine.Particle
import com.example.game.model.BlockColor
import com.example.game.model.BlockItem
import com.example.game.model.BlockType
import com.example.ui.theme.*
import kotlin.math.min

@Composable
fun PuzzleBoard(
    gameState: GameState,
    costumeId: String = "classic",
    onBlockTapped: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val levelDef = gameState.levelDef
    val gridWidth = levelDef.gridWidth
    val gridHeight = levelDef.gridHeight

    BoxWithConstraints(modifier = modifier.fillMaxSize().testTag("puzzle_board")) {
        val availableWidth = constraints.maxWidth.toFloat()
        val availableHeight = constraints.maxHeight.toFloat()

        // Calculate optimal square cell size that fits within available bounds
        val cellPadding = 6f
        val maxCellWidth = (availableWidth - (gridWidth + 1) * cellPadding) / gridWidth
        val maxCellHeight = (availableHeight - (gridHeight + 1) * cellPadding) / gridHeight
        val cellSize = min(maxCellWidth, maxCellHeight).coerceAtLeast(40f)

        val totalBoardWidth = gridWidth * cellSize + (gridWidth - 1) * cellPadding
        val totalBoardHeight = gridHeight * cellSize + (gridHeight - 1) * cellPadding

        val offsetX = (availableWidth - totalBoardWidth) / 2f
        val offsetY = (availableHeight - totalBoardHeight) / 2f

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(gridWidth, gridHeight, cellSize, offsetX, offsetY) {
                    detectTapGestures { tapOffset ->
                        val relativeX = tapOffset.x - offsetX
                        val relativeY = tapOffset.y - offsetY

                        if (relativeX >= 0 && relativeY >= 0) {
                            val col = (relativeX / (cellSize + cellPadding)).toInt()
                            val row = (relativeY / (cellSize + cellPadding)).toInt()
                            if (col in 0 until gridWidth && row in 0 until gridHeight) {
                                onBlockTapped(col, row)
                            }
                        }
                    }
                }
        ) {
            // Draw grid background frame
            drawRoundRect(
                color = Color(0x18000000),
                topLeft = Offset(offsetX - cellPadding, offsetY - cellPadding),
                size = Size(totalBoardWidth + cellPadding * 2, totalBoardHeight + cellPadding * 2),
                cornerRadius = CornerRadius(24f, 24f)
            )

            // Draw subtle cell slot placeholders
            for (gx in 0 until gridWidth) {
                for (gy in 0 until gridHeight) {
                    val px = offsetX + gx * (cellSize + cellPadding)
                    val py = offsetY + gy * (cellSize + cellPadding)
                    drawRoundRect(
                        color = Color(0x0C000000),
                        topLeft = Offset(px, py),
                        size = Size(cellSize, cellSize),
                        cornerRadius = CornerRadius(cellSize * 0.22f, cellSize * 0.22f)
                    )
                }
            }

            // Draw Active Blocks
            for (block in gameState.blocks) {
                val px = offsetX + block.x * (cellSize + cellPadding)
                val py = offsetY + block.y * (cellSize + cellPadding)
                drawBlock(block, px, py, cellSize, costumeId)
            }

            // Draw Particles
            for (particle in gameState.particles) {
                drawParticle(particle, offsetX, offsetY, cellSize, cellPadding)
            }
        }
    }
}

private fun DrawScope.drawBlock(
    block: BlockItem,
    x: Float,
    y: Float,
    size: Float,
    costumeId: String
) {
    val cr = size * 0.22f
    val cornerRadius = CornerRadius(cr, cr)

    // Goal Platform
    if (block.isGoal) {
        val goalBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF00E676), Color(0xFF00B0FF)),
            start = Offset(x, y),
            end = Offset(x + size, y + size)
        )
        drawRoundRect(
            brush = goalBrush,
            topLeft = Offset(x, y + size * 0.45f),
            size = Size(size, size * 0.55f),
            cornerRadius = CornerRadius(size * 0.15f, size * 0.15f)
        )
        // Checkered rescue pattern
        val stripeColor = Color(0x40FFFFFF)
        drawRect(stripeColor, Offset(x + size * 0.2f, y + size * 0.45f), Size(size * 0.2f, size * 0.55f))
        drawRect(stripeColor, Offset(x + size * 0.6f, y + size * 0.45f), Size(size * 0.2f, size * 0.55f))
        // "GOAL" label star
        drawCircle(Color(0xFFFFD700), size * 0.16f, Offset(x + size * 0.5f, y + size * 0.72f))
        return
    }

    // Bloki Himself!
    if (block.isBloki) {
        drawBlokiBlock(x, y, size, costumeId)
        return
    }

    // Colors mapping
    val (primaryColor, highlightColor) = when (block.color) {
        BlockColor.RED -> BlockRed to BlockRedLight
        BlockColor.BLUE -> BlockBlue to BlockBlueLight
        BlockColor.GREEN -> BlockGreen to BlockGreenLight
        BlockColor.YELLOW -> BlockYellow to BlockYellowLight
        BlockColor.PURPLE -> BlockPurple to BlockPurpleLight
        BlockColor.ORANGE -> BlockOrange to BlockOrangeLight
        BlockColor.PINK -> BlokiPink to BlokiPinkLight
        BlockColor.RAINBOW -> Color(0xFFFF4081) to Color(0xFF00E5FF)
        BlockColor.NONE -> Color(0xFF90A4AE) to Color(0xFFCFD8DC)
    }

    // Block Drop Shadow
    drawRoundRect(
        color = Color(0x33000000),
        topLeft = Offset(x, y + 4f),
        size = Size(size, size),
        cornerRadius = cornerRadius
    )

    when (block.type) {
        BlockType.NORMAL -> {
            val brush = Brush.verticalGradient(
                colors = listOf(highlightColor, primaryColor),
                startY = y,
                endY = y + size
            )
            drawRoundRect(brush = brush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            // Top Gloss highlight
            drawRoundRect(
                color = Color(0x40FFFFFF),
                topLeft = Offset(x + size * 0.1f, y + size * 0.08f),
                size = Size(size * 0.8f, size * 0.32f),
                cornerRadius = CornerRadius(cr * 0.6f, cr * 0.6f)
            )
        }

        BlockType.STRONG -> {
            // Reinforced tough block
            val strongBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFB0BEC5), Color(0xFF546E7A)),
                startY = y,
                endY = y + size
            )
            drawRoundRect(brush = strongBrush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            // Outer metal border
            drawRoundRect(
                color = Color(0xFF37474F),
                topLeft = Offset(x, y),
                size = Size(size, size),
                cornerRadius = cornerRadius,
                style = Stroke(size * 0.08f)
            )
            // Rivets on corners
            drawCircle(Color(0xFFECEFF1), size * 0.07f, Offset(x + size * 0.22f, y + size * 0.22f))
            drawCircle(Color(0xFFECEFF1), size * 0.07f, Offset(x + size * 0.78f, y + size * 0.22f))
            drawCircle(Color(0xFFECEFF1), size * 0.07f, Offset(x + size * 0.22f, y + size * 0.78f))
            drawCircle(Color(0xFFECEFF1), size * 0.07f, Offset(x + size * 0.78f, y + size * 0.78f))

            // Center HP or Crack indicator
            if (block.hp > 1) {
                // "2" Shield icon
                drawCircle(Color(0xFFFFB300), size * 0.22f, Offset(x + size * 0.5f, y + size * 0.5f))
                drawCircle(Color.White, size * 0.15f, Offset(x + size * 0.5f, y + size * 0.5f))
            } else {
                // Cracked lines
                val crackPath = Path().apply {
                    moveTo(x + size * 0.2f, y + size * 0.3f)
                    lineTo(x + size * 0.45f, y + size * 0.5f)
                    lineTo(x + size * 0.4f, y + size * 0.7f)
                    lineTo(x + size * 0.75f, y + size * 0.8f)
                }
                drawPath(crackPath, color = Color(0xFF263238), style = Stroke(size * 0.06f))
            }
        }

        BlockType.ICE -> {
            // Icy translucent block
            val iceBrush = Brush.linearGradient(
                colors = listOf(Color(0xE6E0F7FA), Color(0xCC80DEEA), Color(0xCC26C6DA)),
                start = Offset(x, y),
                end = Offset(x + size, y + size)
            )
            drawRoundRect(brush = iceBrush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            // Ice crystal lines
            val crystalPath = Path().apply {
                moveTo(x + size * 0.5f, y + size * 0.15f)
                lineTo(x + size * 0.5f, y + size * 0.85f)
                moveTo(x + size * 0.15f, y + size * 0.5f)
                lineTo(x + size * 0.85f, y + size * 0.5f)
                moveTo(x + size * 0.25f, y + size * 0.25f)
                lineTo(x + size * 0.75f, y + size * 0.75f)
                moveTo(x + size * 0.75f, y + size * 0.25f)
                lineTo(x + size * 0.25f, y + size * 0.75f)
            }
            drawPath(crystalPath, color = Color(0x99FFFFFF), style = Stroke(size * 0.045f))
            // Frost border
            drawRoundRect(
                color = Color(0xB3FFFFFF),
                topLeft = Offset(x, y),
                size = Size(size, size),
                cornerRadius = cornerRadius,
                style = Stroke(size * 0.06f)
            )
        }

        BlockType.STAR -> {
            val starBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF59D), Color(0xFFFFD54F), Color(0xFFFFB300)),
                startY = y,
                endY = y + size
            )
            drawRoundRect(brush = starBrush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            // Big embossed golden star
            drawStar(x + size * 0.5f, y + size * 0.5f, size * 0.32f, Color(0xFFFF8F00))
            drawStar(x + size * 0.5f, y + size * 0.5f, size * 0.26f, Color(0xFFFFF9C4))
        }

        BlockType.COIN -> {
            val coinBrush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFECB3), Color(0xFFFFC107), Color(0xFFFF8F00)),
                startY = y,
                endY = y + size
            )
            drawRoundRect(brush = coinBrush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            // Center embossed coin
            drawCircle(Color(0xFFFF6F00), size * 0.30f, Offset(x + size * 0.5f, y + size * 0.5f))
            drawCircle(Color(0xFFFFD54F), size * 0.25f, Offset(x + size * 0.5f, y + size * 0.5f))
            // Coin dollar/gem symbol
            drawStar(x + size * 0.5f, y + size * 0.5f, size * 0.16f, Color(0xFFFFF8E1))
        }

        BlockType.MAGIC -> {
            val magicBrush = Brush.radialGradient(
                colors = listOf(Color(0xFFEA80FC), Color(0xFFAB47BC), Color(0xFF6A1B9A)),
                center = Offset(x + size * 0.5f, y + size * 0.5f),
                radius = size * 0.7f
            )
            drawRoundRect(brush = magicBrush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            // Magic Diamond Sparkle
            val magicPath = Path().apply {
                moveTo(x + size * 0.5f, y + size * 0.18f)
                lineTo(x + size * 0.82f, y + size * 0.5f)
                lineTo(x + size * 0.5f, y + size * 0.82f)
                lineTo(x + size * 0.18f, y + size * 0.5f)
                close()
            }
            drawPath(magicPath, color = Color(0xFFFFFFFF))
            drawCircle(Color(0xFFEA80FC), size * 0.12f, Offset(x + size * 0.5f, y + size * 0.5f))
        }

        BlockType.MOVING -> {
            val brush = Brush.verticalGradient(
                colors = listOf(highlightColor, primaryColor),
                startY = y,
                endY = y + size
            )
            drawRoundRect(brush = brush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            // Arrow indicators
            val arrowPath = Path().apply {
                if (block.moveDirection > 0) {
                    moveTo(x + size * 0.35f, y + size * 0.35f)
                    lineTo(x + size * 0.65f, y + size * 0.5f)
                    lineTo(x + size * 0.35f, y + size * 0.65f)
                } else {
                    moveTo(x + size * 0.65f, y + size * 0.35f)
                    lineTo(x + size * 0.35f, y + size * 0.5f)
                    lineTo(x + size * 0.65f, y + size * 0.65f)
                }
            }
            drawPath(arrowPath, color = Color.White, style = Stroke(size * 0.09f))
        }

        BlockType.BOMB -> {
            val bombBrush = Brush.radialGradient(
                colors = listOf(Color(0xFF546E7A), Color(0xFF263238), Color(0xFF000000)),
                center = Offset(x + size * 0.45f, y + size * 0.45f),
                radius = size * 0.7f
            )
            drawRoundRect(brush = bombBrush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            // Cartoon Fuse and Spark
            drawLine(Color(0xFFFF9800), Offset(x + size * 0.5f, y + size * 0.25f), Offset(x + size * 0.7f, y + size * 0.12f), size * 0.08f)
            drawCircle(Color(0xFFFFEB3B), size * 0.08f, Offset(x + size * 0.72f, y + size * 0.10f))
            drawCircle(Color(0xFFFF1744), size * 0.04f, Offset(x + size * 0.72f, y + size * 0.10f))
            // Bomb skull/star highlight
            drawCircle(Color(0x33FFFFFF), size * 0.22f, Offset(x + size * 0.45f, y + size * 0.55f))
        }

        BlockType.RAINBOW -> {
            val rainbowBrush = Brush.sweepGradient(
                colors = listOf(
                    Color(0xFFFF1744), Color(0xFFFF9100), Color(0xFFFFEA00),
                    Color(0xFF00E676), Color(0xFF00B0FF), Color(0xFF7C4DFF), Color(0xFFFF1744)
                ),
                center = Offset(x + size * 0.5f, y + size * 0.5f)
            )
            drawRoundRect(brush = rainbowBrush, topLeft = Offset(x, y), size = Size(size, size), cornerRadius = cornerRadius)
            drawStar(x + size * 0.5f, y + size * 0.5f, size * 0.22f, Color.White)
        }
    }
}

private fun DrawScope.drawBlokiBlock(x: Float, y: Float, size: Float, costumeId: String) {
    val cr = size * 0.25f
    // Bloki Body
    val bodyBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFF176), Color(0xFFFFD54F), Color(0xFFFFB300)),
        startY = y,
        endY = y + size
    )
    drawRoundRect(
        brush = bodyBrush,
        topLeft = Offset(x, y),
        size = Size(size, size),
        cornerRadius = CornerRadius(cr, cr)
    )
    // Bevel highlight
    drawRoundRect(
        color = Color(0x4DFFFFFF),
        topLeft = Offset(x + size * 0.1f, y + size * 0.08f),
        size = Size(size * 0.8f, size * 0.25f),
        cornerRadius = CornerRadius(cr * 0.6f, cr * 0.6f)
    )

    // Cheeks
    drawCircle(Color(0xFFFF8A80), size * 0.09f, Offset(x + size * 0.22f, y + size * 0.65f))
    drawCircle(Color(0xFFFF8A80), size * 0.09f, Offset(x + size * 0.78f, y + size * 0.65f))

    // Cute Eyes
    drawCircle(Color(0xFF263238), size * 0.095f, Offset(x + size * 0.35f, y + size * 0.46f))
    drawCircle(Color(0xFF263238), size * 0.095f, Offset(x + size * 0.65f, y + size * 0.46f))
    drawCircle(Color.White, size * 0.035f, Offset(x + size * 0.32f, y + size * 0.43f))
    drawCircle(Color.White, size * 0.035f, Offset(x + size * 0.62f, y + size * 0.43f))

    // Smiling Mouth
    val mouthPath = Path().apply {
        moveTo(x + size * 0.42f, y + size * 0.62f)
        quadraticTo(x + size * 0.50f, y + size * 0.72f, x + size * 0.58f, y + size * 0.62f)
    }
    drawPath(mouthPath, color = Color(0xFF263238), style = Stroke(size * 0.05f))

    // Mini costume accessory if equipped
    when (costumeId) {
        "prince" -> drawStar(x + size * 0.5f, y + size * 0.12f, size * 0.15f, Color(0xFFFFD700))
        "superhero" -> {
            drawRoundRect(
                Color(0xFF00B0FF),
                Offset(x + size * 0.22f, y + size * 0.40f),
                Size(size * 0.56f, size * 0.14f),
                CornerRadius(size * 0.05f, size * 0.05f)
            )
        }
        "party" -> drawStar(x + size * 0.5f, y + size * 0.08f, size * 0.12f, Color(0xFFFF4081))
        "cool" -> {
            drawRoundRect(
                Color(0xFF212121),
                Offset(x + size * 0.22f, y + size * 0.40f),
                Size(size * 0.56f, size * 0.14f),
                CornerRadius(size * 0.04f, size * 0.04f)
            )
        }
        "dino" -> {
            drawCircle(Color(0xFF00E676), size * 0.09f, Offset(x + size * 0.5f, y + size * 0.06f))
        }
    }
}

private fun DrawScope.drawParticle(
    particle: Particle,
    boardX: Float,
    boardY: Float,
    cellSize: Float,
    cellPadding: Float
) {
    val px = boardX + particle.x * (cellSize + cellPadding) + cellSize * 0.5f
    val py = boardY + particle.y * (cellSize + cellPadding) + cellSize * 0.5f

    val color = when (particle.color) {
        BlockColor.RED -> BlockRed
        BlockColor.BLUE -> BlockBlue
        BlockColor.GREEN -> BlockGreen
        BlockColor.YELLOW -> BlockYellow
        BlockColor.PURPLE -> BlockPurple
        BlockColor.ORANGE -> BlockOrange
        BlockColor.PINK -> BlokiPink
        else -> Color.White
    }

    if (particle.isStar) {
        drawStar(px, py, particle.size, StarGold)
    } else if (particle.isCoin) {
        drawCircle(CoinAmber, particle.size, Offset(px, py))
    } else {
        drawRoundRect(
            color = color,
            topLeft = Offset(px - particle.size * 0.5f, py - particle.size * 0.5f),
            size = Size(particle.size, particle.size),
            cornerRadius = CornerRadius(particle.size * 0.2f, particle.size * 0.2f)
        )
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.45f
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) radius else innerRadius
        val angle = (i * 36.0 - 90.0) * Math.PI / 180.0
        val x = (cx + r * kotlin.math.cos(angle)).toFloat()
        val y = (cy + r * kotlin.math.sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    drawPath(path, color = color)
}
