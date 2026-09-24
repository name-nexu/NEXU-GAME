package com.example.game.engine

import com.example.game.model.BlockColor
import com.example.game.model.BlockItem
import com.example.game.model.BlockType
import com.example.game.model.LevelDefinition
import com.example.game.model.ObjectiveProgress
import com.example.game.model.ObjectiveType
import com.example.game.model.PowerUpType
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class ParticleStyle {
    CHIP,
    STAR,
    COIN,
    ICE_CRYSTAL,
    SHOCKWAVE,
    SMOKE,
    SPARKLE,
    CONFETTI
}

data class Particle(
    val id: String,
    val x: Float, // relative to grid (column/row float)
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: BlockColor,
    val size: Float,
    val alpha: Float = 1f,
    val isStar: Boolean = false,
    val isCoin: Boolean = false,
    val style: ParticleStyle = if (isStar) ParticleStyle.STAR else if (isCoin) ParticleStyle.COIN else ParticleStyle.CHIP,
    val rotation: Float = 0f,
    val vRot: Float = 0f,
    val gravity: Float = 6f,
    val maxLifeMs: Long = 650L,
    val spawnTimeMs: Long = System.currentTimeMillis()
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
    val currentScore: Int = 0,
    val targetScore: Int = levelDef.targetScore,
    val lastScoreEarned: Int = 0,
    val starsEarned: Int = 0,
    val coinsEarned: Int = 0,
    val particles: List<Particle> = emptyList(),
    val activePowerUp: PowerUpType? = null,
    val blokiRescued: Boolean = false,
    val lastActionDescription: String? = null,
    val comboMultiplier: Int = 1,
    val actionSequence: Long = 0L // Monotonically increasing counter on every tap to trigger UI animations
) {
    val isTargetScoreMet: Boolean get() = currentScore >= targetScore
    val scoreProgress: Float get() = if (targetScore > 0) (currentScore.toFloat() / targetScore.toFloat()).coerceIn(0f, 1.5f) else 1f
}

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
                is ObjectiveType.ReachScore -> obj.targetScore
            }
            ObjectiveProgress(type = obj, target = target)
        }

        return GameState(
            levelDef = level,
            blocks = level.initialBlocks,
            movesRemaining = level.movesAllowed,
            objectives = objProgress,
            status = GameStatus.PLAYING,
            currentScore = 0,
            targetScore = level.targetScore,
            lastScoreEarned = 0,
            coinsEarned = 0,
            starsEarned = 0,
            actionSequence = 0L
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
     * Checks if coordinates fall within the level grid boundary.
     */
    fun isInsideGrid(x: Int, y: Int): Boolean {
        return x in 0 until state.levelDef.gridWidth && y in 0 until state.levelDef.gridHeight
    }

    /**
     * Finds connected matching normal blocks starting from a given block (Cluster Matching).
     */
    fun findConnectedCluster(startBlock: BlockItem, allBlocks: List<BlockItem>): List<BlockItem> {
        if (startBlock.type != BlockType.NORMAL || startBlock.isBloki || startBlock.isGoal || startBlock.color == BlockColor.NONE) {
            return listOf(startBlock)
        }

        val targetColor = startBlock.color
        val blockMap = allBlocks.associateBy { it.x to it.y }
        val visited = mutableSetOf<String>()
        val cluster = mutableListOf<BlockItem>()
        val queue = ArrayDeque<BlockItem>()

        queue.add(startBlock)
        visited.add(startBlock.id)

        while (queue.isNotEmpty()) {
            val curr = queue.removeFirst()
            cluster.add(curr)

            val neighbors = listOf(
                curr.x + 1 to curr.y,
                curr.x - 1 to curr.y,
                curr.x to curr.y + 1,
                curr.x to curr.y - 1
            )

            for ((nx, ny) in neighbors) {
                val neighbor = blockMap[nx to ny]
                if (neighbor != null &&
                    neighbor.type == BlockType.NORMAL &&
                    neighbor.color == targetColor &&
                    !neighbor.isBloki &&
                    !neighbor.isGoal &&
                    !visited.contains(neighbor.id)
                ) {
                    visited.add(neighbor.id)
                    queue.add(neighbor)
                }
            }
        }

        return cluster
    }

    /**
     * Main interaction: tap on a block at (gridX, gridY).
     */
    fun tapBlock(gridX: Int, gridY: Int, onSoundEffect: (String) -> Unit = {}): GameState {
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

        var movesDeducted = 1
        val updatedBlocks = state.blocks.toMutableList()
        val brokenBlocks = mutableListOf<BlockItem>()
        val newParticles = mutableListOf<Particle>()
        var addedCoins = 0
        var addedStars = 0
        var combo = 1
        var addedScore = 0

        when (block.type) {
            BlockType.NORMAL -> {
                val cluster = findConnectedCluster(block, updatedBlocks)
                if (cluster.size > 1) {
                    combo = cluster.size
                    for (b in cluster) {
                        updatedBlocks.remove(b)
                        brokenBlocks.add(b)
                        spawnBreakParticles(b, newParticles)
                    }
                    addedScore += (50 * cluster.size * cluster.size)
                    if (cluster.size >= 4) {
                        addedCoins += 5
                        spawnStarParticles(block, newParticles)
                    }
                    onSoundEffect("break")
                } else {
                    updatedBlocks.remove(block)
                    brokenBlocks.add(block)
                    addedScore += 50
                    onSoundEffect("break")
                    spawnBreakParticles(block, newParticles)
                }
            }

            BlockType.STRONG -> {
                if (block.hp > 1) {
                    val cracked = block.copy(hp = block.hp - 1)
                    val idx = updatedBlocks.indexOf(block)
                    if (idx != -1) updatedBlocks[idx] = cracked
                    addedScore += 50
                    onSoundEffect("hit")
                    spawnHitParticles(block, newParticles)
                } else {
                    updatedBlocks.remove(block)
                    brokenBlocks.add(block)
                    addedScore += 150
                    onSoundEffect("break")
                    spawnBreakParticles(block, newParticles)
                }
            }

            BlockType.ICE -> {
                // Ice cannot be tapped directly without a power-up
                onSoundEffect("tap")
                return state
            }

            BlockType.STAR -> {
                updatedBlocks.remove(block)
                brokenBlocks.add(block)
                addedStars += 1
                addedScore += 250
                onSoundEffect("star")
                spawnStarParticles(block, newParticles)
            }

            BlockType.COIN -> {
                updatedBlocks.remove(block)
                brokenBlocks.add(block)
                addedCoins += 10
                addedScore += 150
                onSoundEffect("coin")
                spawnCoinParticles(block, newParticles)
            }

            BlockType.BOMB -> {
                onSoundEffect("bomb")
                val countBefore = brokenBlocks.size
                triggerBombCascade(block, updatedBlocks, brokenBlocks, newParticles)
                val casualties = (brokenBlocks.size - countBefore).coerceAtLeast(1)
                addedScore += 300 + (casualties * 60)
            }

            BlockType.MAGIC -> {
                onSoundEffect("magic")
                val countBefore = brokenBlocks.size
                triggerMagicCrossBlast(block, updatedBlocks, brokenBlocks, newParticles)
                val casualties = (brokenBlocks.size - countBefore).coerceAtLeast(1)
                addedScore += 400 + (casualties * 60)
            }

            BlockType.RAINBOW -> {
                onSoundEffect("magic")
                val countBefore = brokenBlocks.size
                triggerRainbowClear(block, updatedBlocks, brokenBlocks, newParticles)
                val casualties = (brokenBlocks.size - countBefore).coerceAtLeast(1)
                addedScore += 500 + (casualties * 75)
            }

            BlockType.MOVING -> {
                updatedBlocks.remove(block)
                brokenBlocks.add(block)
                addedScore += 100
                onSoundEffect("break")
                spawnBreakParticles(block, newParticles)
            }
        }

        // Chain Reaction: shatter adjacent ice blocks when neighbors break
        val iceBefore = brokenBlocks.size
        shatterAdjacentIce(brokenBlocks, updatedBlocks, brokenBlocks, newParticles, onSoundEffect)
        val iceBroken = brokenBlocks.size - iceBefore
        if (iceBroken > 0) {
            addedScore += iceBroken * 100
        }

        // Apply physics & gravity to drop unsupported blocks and Bloki
        applyGravity(updatedBlocks)

        // Step moving blocks
        stepMovingBlocks(updatedBlocks)

        // Preliminary score before rescue check
        val runningScore = state.currentScore + addedScore

        // Evaluate objectives
        val remainingMoves = state.movesRemaining - movesDeducted
        val (updatedObjectives, isBlokiRescued) = evaluateObjectives(
            state.objectives,
            brokenBlocks,
            addedStars,
            addedCoins,
            updatedBlocks,
            state.levelDef,
            runningScore
        )

        // If Bloki is rescued, spawn victory confetti and award bonus points!
        if (isBlokiRescued && !state.blokiRescued) {
            addedScore += 1000
            val bloki = updatedBlocks.firstOrNull { it.isBloki }
            if (bloki != null) {
                spawnVictoryParticles(bloki, newParticles)
            }
        }

        val allObjectivesMet = updatedObjectives.all { it.completed }
        val won = allObjectivesMet
        val lost = !won && remainingMoves <= 0

        val currentStatus = when {
            won -> GameStatus.WON
            lost -> GameStatus.LOST
            else -> GameStatus.PLAYING
        }

        // If won, grant bonus points for leftover moves
        val movesBonus = if (won) remainingMoves.coerceAtLeast(0) * 150 else 0
        val finalScore = state.currentScore + addedScore + movesBonus

        val totalStarsEarned = if (won) {
            calculateStarsEarned(finalScore, state.targetScore, remainingMoves, state.levelDef.movesAllowed)
        } else 0

        if (won) onSoundEffect("win")
        else if (lost) onSoundEffect("fail")

        state = state.copy(
            blocks = updatedBlocks,
            movesRemaining = remainingMoves.coerceAtLeast(0),
            objectives = updatedObjectives,
            status = currentStatus,
            currentScore = finalScore,
            lastScoreEarned = addedScore + movesBonus,
            starsEarned = totalStarsEarned,
            coinsEarned = state.coinsEarned + addedCoins + (if (won) 30 else 0),
            particles = newParticles,
            blokiRescued = isBlokiRescued,
            comboMultiplier = combo,
            actionSequence = state.actionSequence + 1
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

        // Chain Reaction: Ice shatters adjacent to any broken block
        val iceBefore = brokenBlocks.size
        shatterAdjacentIce(brokenBlocks, updatedBlocks, brokenBlocks, newParticles, onSoundEffect)
        val iceBroken = brokenBlocks.size - iceBefore

        applyGravity(updatedBlocks)

        val powerUpAddedScore = (brokenBlocks.size * 60) + (iceBroken * 100) + (addedStars * 250) + (addedCoins * 15)
        val runningScore = state.currentScore + powerUpAddedScore

        val (updatedObjectives, isBlokiRescued) = evaluateObjectives(
            state.objectives,
            brokenBlocks,
            addedStars,
            addedCoins,
            updatedBlocks,
            state.levelDef,
            runningScore
        )

        var totalEarned = powerUpAddedScore
        if (isBlokiRescued && !state.blokiRescued) {
            totalEarned += 1000
            val bloki = updatedBlocks.firstOrNull { it.isBloki }
            if (bloki != null) spawnVictoryParticles(bloki, newParticles)
        }

        val won = updatedObjectives.all { it.completed }
        val currentStatus = if (won) GameStatus.WON else state.status
        val movesBonus = if (won) state.movesRemaining.coerceAtLeast(0) * 150 else 0
        val finalScore = state.currentScore + totalEarned + movesBonus

        val stars = if (won) {
            calculateStarsEarned(finalScore, state.targetScore, state.movesRemaining, state.levelDef.movesAllowed)
        } else state.starsEarned

        state = state.copy(
            blocks = updatedBlocks,
            objectives = updatedObjectives,
            activePowerUp = null, // Consume power up selection
            status = currentStatus,
            currentScore = finalScore,
            lastScoreEarned = totalEarned + movesBonus,
            starsEarned = stars,
            particles = newParticles,
            blokiRescued = isBlokiRescued,
            actionSequence = state.actionSequence + 1
        )

        return state
    }

    /**
     * Triggers a cascade of bomb detonations if explosions trigger neighboring bombs.
     */
    private fun triggerBombCascade(
        initialBomb: BlockItem,
        currentBlocks: MutableList<BlockItem>,
        brokenBlocks: MutableList<BlockItem>,
        particles: MutableList<Particle>
    ) {
        val bombQueue = ArrayDeque<BlockItem>()
        bombQueue.add(initialBomb)

        while (bombQueue.isNotEmpty()) {
            val bomb = bombQueue.removeFirst()
            if (!currentBlocks.contains(bomb) && bomb != initialBomb) continue

            currentBlocks.remove(bomb)
            brokenBlocks.add(bomb)
            spawnExplosionParticles(bomb, particles)

            // 3x3 Blast zone
            val surrounding = currentBlocks.filter {
                !it.isGoal && !it.isBloki &&
                    kotlin.math.abs(it.x - bomb.x) <= 1 &&
                    kotlin.math.abs(it.y - bomb.y) <= 1
            }

            for (victim in surrounding) {
                if (victim.type == BlockType.BOMB) {
                    bombQueue.add(victim)
                } else {
                    currentBlocks.remove(victim)
                    brokenBlocks.add(victim)
                    spawnBreakParticles(victim, particles)
                }
            }
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
        spawnCrossBlastParticles(magic, particles)

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
                spawnIceParticles(ice, particles)
                onSound("ice")
            }
        }
    }

    /**
     * Physics: Drops blocks down column-by-column until they hit the bottom or another block.
     * Guaranteed that no movable block is left floating in mid-air.
     */
    fun applyGravity(blocks: MutableList<BlockItem>) {
        val width = state.levelDef.gridWidth
        val height = state.levelDef.gridHeight

        // Process each column independently
        for (x in 0 until width) {
            val colBlocks = blocks.filter { it.x == x }.sortedBy { it.y }.toMutableList()

            // Separate goal blocks (they are static platforms)
            val goals = colBlocks.filter { it.isGoal }
            val movables = colBlocks.filter { !it.isGoal }

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
        levelDef: LevelDefinition,
        currentScore: Int
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

        val effectiveScore = currentScore + (if (blokiRescued) 1000 else 0)

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
                is ObjectiveType.ReachScore -> {
                    current = effectiveScore
                }
            }
            val completed = current >= obj.target
            obj.copy(current = current, completed = completed)
        }

        return Pair(updated, blokiRescued)
    }

    private fun calculateStarsEarned(
        score: Int,
        targetScore: Int,
        remainingMoves: Int,
        totalMoves: Int
    ): Int {
        val moveRatio = if (totalMoves > 0) remainingMoves.toFloat() / totalMoves.toFloat() else 0f
        return when {
            score >= targetScore || (score >= targetScore * 0.85f && moveRatio >= 0.35f) -> 3
            score >= (targetScore * 0.65f) || moveRatio >= 0.15f -> 2
            else -> 1
        }
    }

    // --- Particle Spawning Engine ---

    private fun spawnBreakParticles(block: BlockItem, list: MutableList<Particle>) {
        val count = 10
        for (i in 0 until count) {
            val angle = (i * (360f / count) + Random.nextFloat() * 20f) * (Math.PI.toFloat() / 180f)
            val speed = Random.nextFloat() * 2.5f + 1.2f
            list.add(
                Particle(
                    id = "p_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 1.2f, // upward initial impulse
                    color = block.color,
                    size = Random.nextFloat() * 12f + 8f,
                    style = ParticleStyle.CHIP,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 360f,
                    gravity = 5.5f,
                    maxLifeMs = 600L
                )
            )
        }
        // Add 3 bright sparkle bursts
        for (i in 0..2) {
            list.add(
                Particle(
                    id = "spk_${block.id}_$i",
                    x = block.x.toFloat() + (Random.nextFloat() - 0.5f) * 0.4f,
                    y = block.y.toFloat() + (Random.nextFloat() - 0.5f) * 0.4f,
                    vx = (Random.nextFloat() - 0.5f) * 1.5f,
                    vy = (Random.nextFloat() - 0.5f) * 1.5f,
                    color = BlockColor.NONE,
                    size = 14f,
                    style = ParticleStyle.SPARKLE,
                    gravity = 1f,
                    maxLifeMs = 450L
                )
            )
        }
    }

    private fun spawnHitParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..4) {
            list.add(
                Particle(
                    id = "hit_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 2.0f,
                    vy = (Random.nextFloat() - 0.5f) * 2.0f,
                    color = block.color,
                    size = 7f,
                    style = ParticleStyle.CHIP,
                    rotation = Random.nextFloat() * 180f,
                    vRot = (Random.nextFloat() - 0.5f) * 240f,
                    gravity = 6f,
                    maxLifeMs = 400L
                )
            )
        }
    }

    private fun spawnIceParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..11) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = Random.nextFloat() * 3.0f + 1.0f
            list.add(
                Particle(
                    id = "ice_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 1.5f,
                    color = BlockColor.BLUE,
                    size = Random.nextFloat() * 10f + 6f,
                    style = ParticleStyle.ICE_CRYSTAL,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 400f,
                    gravity = 5f,
                    maxLifeMs = 650L
                )
            )
        }
    }

    private fun spawnExplosionParticles(block: BlockItem, list: MutableList<Particle>) {
        // Central shockwave ring
        list.add(
            Particle(
                id = "sw_${block.id}",
                x = block.x.toFloat(),
                y = block.y.toFloat(),
                vx = 0f,
                vy = 0f,
                color = BlockColor.ORANGE,
                size = 18f,
                style = ParticleStyle.SHOCKWAVE,
                gravity = 0f,
                maxLifeMs = 500L
            )
        )

        // Flying fiery explosion chips
        for (i in 0..15) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = Random.nextFloat() * 4.2f + 1.5f
            val col = if (i % 2 == 0) BlockColor.ORANGE else BlockColor.YELLOW
            list.add(
                Particle(
                    id = "exp_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 1.0f,
                    color = col,
                    size = Random.nextFloat() * 14f + 8f,
                    style = ParticleStyle.CHIP,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 500f,
                    gravity = 6f,
                    maxLifeMs = 700L
                )
            )
        }

        // Puffy smoke clouds
        for (i in 0..4) {
            list.add(
                Particle(
                    id = "smk_${block.id}_$i",
                    x = block.x.toFloat() + (Random.nextFloat() - 0.5f) * 0.3f,
                    y = block.y.toFloat() + (Random.nextFloat() - 0.5f) * 0.3f,
                    vx = (Random.nextFloat() - 0.5f) * 0.8f,
                    vy = -Random.nextFloat() * 1.5f - 0.5f,
                    color = BlockColor.NONE,
                    size = 20f,
                    style = ParticleStyle.SMOKE,
                    gravity = -0.5f,
                    maxLifeMs = 750L
                )
            )
        }
    }

    private fun spawnCrossBlastParticles(magic: BlockItem, list: MutableList<Particle>) {
        // Shockwave at center
        list.add(
            Particle(
                id = "sw_${magic.id}",
                x = magic.x.toFloat(),
                y = magic.y.toFloat(),
                vx = 0f,
                vy = 0f,
                color = BlockColor.PURPLE,
                size = 20f,
                style = ParticleStyle.SHOCKWAVE,
                gravity = 0f,
                maxLifeMs = 450L
            )
        )

        // Directional laser sparkles (Horizontal & Vertical)
        val directions = listOf(
            Pair(1f, 0f), Pair(-1f, 0f), Pair(0f, 1f), Pair(0f, -1f)
        )
        for (d in directions) {
            for (step in 1..4) {
                list.add(
                    Particle(
                        id = "mag_${magic.id}_${d.first}_${d.second}_$step",
                        x = magic.x.toFloat() + d.first * step * 0.8f,
                        y = magic.y.toFloat() + d.second * step * 0.8f,
                        vx = d.first * 4.5f,
                        vy = d.second * 4.5f,
                        color = BlockColor.PURPLE,
                        size = 12f,
                        style = ParticleStyle.SPARKLE,
                        gravity = 0f,
                        maxLifeMs = 500L
                    )
                )
            }
        }
    }

    private fun spawnStarParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..7) {
            list.add(
                Particle(
                    id = "star_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 2.5f,
                    vy = -Random.nextFloat() * 3.0f - 1.0f,
                    color = BlockColor.YELLOW,
                    size = 15f,
                    isStar = true,
                    style = ParticleStyle.STAR,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 200f,
                    gravity = 2.5f,
                    maxLifeMs = 750L
                )
            )
        }
    }

    private fun spawnCoinParticles(block: BlockItem, list: MutableList<Particle>) {
        for (i in 0..6) {
            list.add(
                Particle(
                    id = "coin_${block.id}_$i",
                    x = block.x.toFloat(),
                    y = block.y.toFloat(),
                    vx = (Random.nextFloat() - 0.5f) * 2.0f,
                    vy = -Random.nextFloat() * 3.5f - 1.2f,
                    color = BlockColor.YELLOW,
                    size = 14f,
                    isCoin = true,
                    style = ParticleStyle.COIN,
                    rotation = Random.nextFloat() * 180f,
                    vRot = (Random.nextFloat() - 0.5f) * 250f,
                    gravity = 4.5f,
                    maxLifeMs = 700L
                )
            )
        }
    }

    private fun spawnVictoryParticles(bloki: BlockItem, list: MutableList<Particle>) {
        val rainbowColors = listOf(
            BlockColor.RED, BlockColor.ORANGE, BlockColor.YELLOW,
            BlockColor.GREEN, BlockColor.BLUE, BlockColor.PURPLE, BlockColor.PINK
        )
        for (i in 0..24) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2f
            val speed = Random.nextFloat() * 3.5f + 1.5f
            list.add(
                Particle(
                    id = "vic_${bloki.id}_$i",
                    x = bloki.x.toFloat(),
                    y = bloki.y.toFloat(),
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 2.5f,
                    color = rainbowColors[i % rainbowColors.size],
                    size = Random.nextFloat() * 12f + 8f,
                    style = if (i % 3 == 0) ParticleStyle.STAR else ParticleStyle.CONFETTI,
                    rotation = Random.nextFloat() * 360f,
                    vRot = (Random.nextFloat() - 0.5f) * 360f,
                    gravity = 3.5f,
                    maxLifeMs = 900L
                )
            )
        }
    }
}
