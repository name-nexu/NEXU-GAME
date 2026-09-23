package com.example.game.engine

import com.example.game.model.BlockColor
import com.example.game.model.BlockItem
import com.example.game.model.BlockType
import com.example.game.model.LevelDefinition
import com.example.game.model.ObjectiveProgress
import com.example.game.model.ObjectiveType
import com.example.game.model.PowerUpType
import kotlin.random.Random

data class Particle(
    val id: String,
    val x: Float, // relative to grid or screen
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: BlockColor,
    val size: Float,
    val alpha: Float = 1f,
    val isStar: Boolean = false,
    val isCoin: Boolean = false
)

enum class GameStatus {
    PLAYING,
    WON,
    LOST
}

data class GameState(
    val levelDef: LevelDefinition,
    val blocks: List<BlockItem>,
    val movesRemaining: Int,
    val objectives: List<ObjectiveProgress>,
    val status: GameStatus = GameStatus.PLAYING,
    val starsEarned: Int = 0,
    val coinsEarned: Int = 0,
    val particles: List<Particle> = emptyList(),
    val activePowerUp: PowerUpType? = null,
    val blokiRescued: Boolean = false,
    val lastActionDescription: String? = null
)

class GameEngine(private val initialLevel: LevelDefinition) {

    var state: GameState = createInitialState(initialLevel)
        private set

    private fun createInitialState(level: LevelDefinition): GameState {
        val objProgress = level.objectives.map { obj ->
            val target = when (obj) {
                is ObjectiveType.BreakBlocks -> obj.target
                is ObjectiveType.CollectStars -> obj.target
                is ObjectiveType.CollectCoins -> obj.target
                is ObjectiveType.ClearColor -> obj.target
                is ObjectiveType.RescueBloki -> 1
            }
            ObjectiveProgress(type = obj, target = target)
        }

        return GameState(
            levelDef = level,
            blocks = level.initialBlocks,
            movesRemaining = level.movesAllowed,
            objectives = objProgress,
            status = GameStatus.PLAYING,
            coinsEarned = 0,
            starsEarned = 0
        )
    }

    fun restart() {
        state = createInitialState(initialLevel)
    }

    fun loadLevel(level: LevelDefinition) {
        state = createInitialState(level)
    }

    fun selectPowerUp(powerUpType: PowerUpType?) {
        state = state.copy(activePowerUp = powerUpType)
    }

    /**
     * Main interaction: tap on a block at (gridX, gridY).
     */
    fun tapBlock(gridX: Int, gridY: Int, onSoundEffect: (String) -> Unit): GameState {
        if (state.status != GameStatus.PLAYING) return state

        val block = state.blocks.firstOrNull { it.x == gridX && it.y == gridY }

        // If using an active power-up
        if (state.activePowerUp != null) {
            return executePowerUpAction(state.activePowerUp!!, gridX, gridY, block, onSoundEffect)
        }

        if (block == null || block.isGoal) return state

        // Bloki himself cannot be broken directly
        if (block.isBloki) {
            onSoundEffect("tap")
            return state
        }

        // Process block tap based on its type
        var movesDeducted = 1
        val updatedBlocks = state.blocks.toMutableList()
        val brokenBlocks = mutableListOf<BlockItem>()
        val newParticles = mutableListOf<Particle>()
        var addedCoins = 0
        var addedStars = 0

        when (block.type) {
            BlockType.NORMAL -> {
                updatedBlocks.remove(block)
                brokenBlocks.add(block)
                onSoundEffect("break")
                spawnBreakParticles(block, newParticles)
            }

            BlockType.STRONG -> {
                if (block.hp > 1) {
                    val cracked = block.copy(hp = block.hp - 1)
                    val idx = updatedBlocks.indexOf(block)
                    if (idx != -1) updatedBlocks[idx] = cracked
                    onSoundEffect("hit")
                    spawnHitParticles(block, newParticles)
                } else {
                    updatedBlocks.remove(block)
                    brokenBlocks.add(block)
                    onSoundEffect("break")
                    spawnBreakParticles(block, newParticles)
                }
            }

            BlockType.ICE -> {
                // Ice doesn't break from a direct tap without power-up unless cracked
                onSoundEffect("tap")
                return state
            }

            BlockType.STAR -> {
                updatedBlocks.remove(block)
                brokenBlocks.add(block)
                addedStars += 1
                onSoundEffect("star")
                spawnStarParticles(block, newParticles)
            }

            BlockType.COIN -> {
                updatedBlocks.remove(block)
                brokenBlocks.add(block)
                addedCoins += 10
                onSoundEffect("coin")
                spawnCoinParticles(block, newParticles)
            }

            BlockType.BOMB -> {
                onSoundEffect("bomb")
                triggerBombExplosion(block, updatedBlocks, brokenBlocks, newParticles)
            }

            BlockType.MAGIC -> {
                onSoundEffect("magic")
                triggerMagicCrossBlast(block, updatedBlocks, brokenBlocks, newParticles)
            }

            BlockType.RAINBOW -> {
                onSoundEffect("magic")
                triggerRainbowClear(block, updatedBlocks, brokenBlocks, newParticles)
            }

            BlockType.MOVING -> {
                updatedBlocks.remove(block)
                brokenBlocks.add(block)
                onSoundEffect("break")
                spawnBreakParticles(block, newParticles)
            }
        }

        // Check adjacent ice blocks to shatter them when neighbors break
        shatterAdjacentIce(brokenBlocks, updatedBlocks, brokenBlocks, newParticles, onSoundEffect)

        // Apply physics & gravity to drop unsupported blocks and Bloki
        applyGravity(updatedBlocks)

        // Step moving blocks
        stepMovingBlocks(updatedBlocks)

        // Update objectives
        val remainingMoves = state.movesRemaining - movesDeducted
        val (updatedObjectives, isBlokiRescued) = evaluateObjectives(
            state.objectives,
            brokenBlocks,
            addedStars,
            addedCoins,
            updatedBlocks,
            state.levelDef
        )

        val allObjectivesMet = updatedObjectives.all { it.completed }
        val won = allObjectivesMet
        val lost = !won && remainingMoves <= 0

        val currentStatus = when {
            won -> GameStatus.WON
            lost -> GameStatus.LOST
            else -> GameStatus.PLAYING
        }

        val totalStarsEarned = if (won) calculateStarsEarned(remainingMoves, state.levelDef.movesAllowed) else 0

        if (won) onSoundEffect("win")
        else if (lost) onSoundEffect("fail")

        state = state.copy(
            blocks = updatedBlocks,
            movesRemaining = remainingMoves.coerceAtLeast(0),
            objectives = updatedObjectives,
            status = currentStatus,
            starsEarned = totalStarsEarned,
            coinsEarned = state.coinsEarned + addedCoins + (if (won) 30 else 0),
            particles = newParticles,
            blokiRescued = isBlokiRescued
        )

        return state
    }

    private fun executePowerUpAction(
        powerUp: PowerUpType,
        x: Int,
        y: Int,
        targetBlock: BlockItem?,
        onSoundEffect: (String) -> Unit
    ): GameState {
        val updatedBlocks = state.blocks.toMutableList()
        val brokenBlocks = mutableListOf<BlockItem>()
        val newParticles = mutableListOf<Particle>()
        var addedCoins = 0
        var addedStars = 0

        when (powerUp) {
            PowerUpType.HAMMER -> {
                if (targetBlock != null && !targetBlock.isGoal && !targetBlock.isBloki) {
                    updatedBlocks.remove(targetBlock)
                    brokenBlocks.add(targetBlock)
                    onSoundEffect("powerup")
                    spawnBreakParticles(targetBlock, newParticles)
                }
            }

            PowerUpType.ROCKET -> {
                // Clears entire row and column at (x, y)
                val victims = updatedBlocks.filter {
                    !it.isGoal && !it.isBloki && (it.x == x || it.y == y)
                }
                for (v in victims) {
                    updatedBlocks.remove(v)
                    brokenBlocks.add(v)
                    spawnBreakParticles(v, newParticles)
                }
                onSoundEffect("bomb")
            }

            PowerUpType.RAINBOW -> {
                // Clear all blocks matching target color
                if (targetBlock != null && targetBlock.color != BlockColor.NONE && !targetBlock.isBloki && !targetBlock.isGoal) {
                    val victims = updatedBlocks.filter { it.color == targetBlock.color && !it.isGoal && !it.isBloki }
                    for (v in victims) {
                        updatedBlocks.remove(v)
                        brokenBlocks.add(v)
                        spawnBreakParticles(v, newParticles)
                    }
                    onSoundEffect("magic")
                }
            }

            PowerUpType.MAGIC_WAND -> {
                // Transform tough blocks (Strong or Ice) into Star/Coin blocks
                val toughs = updatedBlocks.filter { it.type == BlockType.STRONG || it.type == BlockType.ICE }
                for (t in toughs) {
                    val idx = updatedBlocks.indexOf(t)
                    if (idx != -1) {
                        val transformType = if (Random.nextBoolean()) BlockType.STAR else BlockType.COIN
                        val transformColor = BlockColor.YELLOW
                        updatedBlocks[idx] = t.copy(type = transformType, color = transformColor, hp = 1, maxHp = 1)
                        spawnStarParticles(t, newParticles)
                    }
                }
                onSoundEffect("magic")
            }

            PowerUpType.SHUFFLE -> {
                // Shuffle block colors & types among non-goal/non-bloki blocks
                val regularBlocks = updatedBlocks.filter { !it.isGoal && !it.isBloki }
                val shuffledColors = regularBlocks.map { it.color }.shuffled()
                val shuffledTypes = regularBlocks.map { it.type }.shuffled()
                regularBlocks.forEachIndexed { index, b ->
                    val idx = updatedBlocks.indexOf(b)
                    if (idx != -1) {
                        updatedBlocks[idx] = b.copy(color = shuffledColors[index], type = shuffledTypes[index])
                    }
                }
                onSoundEffect("powerup")
            }
        }

        applyGravity(updatedBlocks)

        val (updatedObjectives, isBlokiRescued) = evaluateObjectives(
            state.objectives,
            brokenBlocks,
            addedStars,
            addedCoins,
            updatedBlocks,
            state.levelDef
        )

        val won = updatedObjectives.all { it.completed }
        val currentStatus = if (won) GameStatus.WON else state.status

        state = state.copy(
            blocks = updatedBlocks,
            objectives = updatedObjectives,
            activePowerUp = null, // Consume power up selection
            status = currentStatus,
            starsEarned = if (won) calculateStarsEarned(state.movesRemaining, state.levelDef.movesAllowed) else state.starsEarned,
            particles = newParticles,
            blokiRescued = isBlokiRescued
        )

        return state
    }

    private fun triggerBombExplosion(
        bomb: BlockItem,
        currentBlocks: MutableList<BlockItem>,
        brokenBlocks: MutableList<BlockItem>,
        particles: MutableList<Particle>
    ) {
        currentBlocks.remove(bomb)
        brokenBlocks.add(bomb)
        spawnExplosionParticles(bomb, particles)

        // Blast 3x3 surrounding
        val surrounding = currentBlocks.filter {
            !it.isGoal && !it.isBloki &&
                kotlin.math.abs(it.x - bomb.x) <= 1 &&
                kotlin.math.abs(it.y - bomb.y) <= 1
        }

        for (victim in surrounding) {
            currentBlocks.remove(victim)
            brokenBlocks.add(victim)
            spawnBreakParticles(victim, particles)
        }
    }

    private fun triggerMagicCrossBlast(
        magic: BlockItem,
        currentBlocks: MutableList<BlockItem>,
        brokenBlocks: MutableList<BlockItem>,
        particles: MutableList<Particle>
    ) {
        currentBlocks.remove(magic)
        brokenBlocks.add(magic)
        spawnExplosionParticles(magic, particles)

        // Cross blast: entire row and entire column
        val lineVictims = currentBlocks.filter {
            !it.isGoal && !it.isBloki && (it.x == magic.x || it.y == magic.y)
        }

        for (victim in lineVictims) {
            currentBlocks.remove(victim)
            brokenBlocks.add(victim)
            spawnBreakParticles(victim, particles)
        }
    }

    private fun triggerRainbowClear(
        rainbow: BlockItem,
        currentBlocks: MutableList<BlockItem>,
        brokenBlocks: MutableList<BlockItem>,
        particles: MutableList<Particle>
    ) {
        currentBlocks.remove(rainbow)
        brokenBlocks.add(rainbow)
        spawnStarParticles(rainbow, particles)

        // Find most common color among remaining blocks
        val regular = currentBlocks.filter { !it.isGoal && !it.isBloki && it.color != BlockColor.NONE }
        val targetColor = regular.groupBy { it.color }.maxByOrNull { it.value.size }?.key ?: BlockColor.BLUE

        val colorVictims = currentBlocks.filter { it.color == targetColor && !it.isGoal && !it.isBloki }
        for (v in colorVictims) {
            currentBlocks.remove(v)
            brokenBlocks.add(v)
            spawnBreakParticles(v, particles)
        }
    }

    private fun shatterAdjacentIce(
        broken: List<BlockItem>,
        currentBlocks: MutableList<BlockItem>,
        allBroken: MutableList<BlockItem>,
        particles: MutableList<Particle>,
        onSound: (String) -> Unit
    ) {
        val iceToShatter = mutableListOf<BlockItem>()
        for (b in broken) {
            val adjacentIce = currentBlocks.filter {
                it.type == BlockType.ICE &&
                    ((kotlin.math.abs(it.x - b.x) == 1 && it.y == b.y) ||
                        (kotlin.math.abs(it.y - b.y) == 1 && it.x == b.x))
            }
            iceToShatter.addAll(adjacentIce)
        }

        for (ice in iceToShatter.distinct()) {
            if (currentBlocks.remove(ice)) {
                allBroken.add(ice)
                spawnBreakParticles(ice, particles)
                onSound("ice")
            }
        }
    }

    /**
     * Physics: Drops blocks down column-by-column until they hit the bottom or another block.
     */
    private fun applyGravity(blocks: MutableList<BlockItem>) {
        val width = state.levelDef.gridWidth
        val height = state.levelDef.gridHeight

        // Process each column independently
        for (x in 0 until width) {
            // Find all blocks in this column, sorted by y ascending (top to bottom)
            val colBlocks = blocks.filter { it.x == x }.sortedBy { it.y }.toMutableList()

            // Separate goal blocks (they are static at bottom)
            val goals = colBlocks.filter { it.isGoal }
            val movables = colBlocks.filter { !it.isGoal }

            // Drop down from bottom upwards
            var lowestAvailableY = height - 1
            if (goals.isNotEmpty()) {
                val minGoalY = goals.minOf { it.y }
                lowestAvailableY = minGoalY - 1
            }

            for (i in movables.indices.reversed()) {
                val b = movables[i]
                if (b.y < lowestAvailableY) {
                    val updated = b.copy(y = lowestAvailableY)
                    val idx = blocks.indexOf(b)
                    if (idx != -1) blocks[idx] = updated
                    lowestAvailableY--
                } else {
                    lowestAvailableY = b.y - 1
                }
            }
        }
    }

    private fun stepMovingBlocks(blocks: MutableList<BlockItem>) {
        val width = state.levelDef.gridWidth
        for (i in blocks.indices) {
            val b = blocks[i]
            if (b.type == BlockType.MOVING) {
                var nextX = b.x + b.moveDirection
                var dir = b.moveDirection
                if (nextX < 0 || nextX >= width || blocks.any { it.x == nextX && it.y == b.y }) {
                    dir = -dir
                    nextX = (b.x + dir).coerceIn(0, width - 1)
                }
                if (!blocks.any { it.x == nextX && it.y == b.y }) {
                    blocks[i] = b.copy(x = nextX, moveDirection = dir)
                } else {
                    blocks[i] = b.copy(moveDirection = dir)
                }
            }
        }
    }

    private fun evaluateObjectives(
        currentObjectives: List<ObjectiveProgress>,
        brokenBlocks: List<BlockItem>,
        addedStars: Int,
        addedCoins: Int,
        remainingBlocks: List<BlockItem>,
        levelDef: LevelDefinition
    ): Pair<List<ObjectiveProgress>, Boolean> {
        val bloki = remainingBlocks.firstOrNull { it.isBloki }
        val goals = remainingBlocks.filter { it.isGoal }

        // Bloki is rescued if he is on the bottom-most row or adjacent/above a goal block
        val blokiRescued = if (bloki != null) {
            val isOnGoal = goals.any { it.x == bloki.x && it.y == bloki.y + 1 }
            val isAtBottom = bloki.y >= levelDef.gridHeight - 1 || (goals.isNotEmpty() && bloki.y >= goals.minOf { it.y } - 1)
            isOnGoal || isAtBottom
        } else {
            false
        }

        val updated = currentObjectives.map { obj ->
            var current = obj.current
            when (obj.type) {
                is ObjectiveType.BreakBlocks -> {
                    val count = brokenBlocks.count { !it.isBloki && !it.isGoal }
                    current += count
                }
                is ObjectiveType.CollectStars -> {
                    current += addedStars
                }
                is ObjectiveType.CollectCoins -> {
                    current += (addedCoins / 10)
                }
                is ObjectiveType.ClearColor -> {
                    val count = brokenBlocks.count { it.color == obj.type.color }
                    current += count
                }
                is ObjectiveType.RescueBloki -> {
                    if (blokiRescued) current = 1
                }
            }
            val completed = current >= obj.target
            obj.copy(current = current, completed = completed)
        }

        return Pair(updated, blokiRescued)
    }

    private fun calculateStarsEarned(remainingMoves: Int, totalMoves: Int): Int {
        val ratio = remainingMoves.toFloat() / totalMoves.toFloat()
        return when {
            ratio >= 0.40f -> 3
            ratio >= 0.15f -> 2
            else -> 1
        }
    }

    private fun spawnBreakParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..7) {
            list.add(
                Particle(
                    id = "p_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 2f,
                    vy = (Random.nextFloat() - 0.5f) * 2f,
                    color = block.color,
                    size = Random.nextFloat() * 12f + 6f
                )
            )
        }
    }

    private fun spawnHitParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..3) {
            list.add(
                Particle(
                    id = "hit_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 1.2f,
                    vy = (Random.nextFloat() - 0.5f) * 1.2f,
                    color = block.color,
                    size = 6f
                )
            )
        }
    }

    private fun spawnExplosionParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..14) {
            list.add(
                Particle(
                    id = "exp_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 3.5f,
                    vy = (Random.nextFloat() - 0.5f) * 3.5f,
                    color = BlockColor.ORANGE,
                    size = Random.nextFloat() * 16f + 8f
                )
            )
        }
    }

    private fun spawnStarParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..6) {
            list.add(
                Particle(
                    id = "star_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 2.2f,
                    vy = -Random.nextFloat() * 2f - 0.5f,
                    color = BlockColor.YELLOW,
                    size = 14f,
                    isStar = true
                )
            )
        }
    }

    private fun spawnCoinParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..5) {
            list.add(
                Particle(
                    id = "coin_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 1.8f,
                    vy = -Random.nextFloat() * 2f,
                    color = BlockColor.YELLOW,
                    size = 12f,
                    isCoin = true
                )
            )
        }
    }
}
