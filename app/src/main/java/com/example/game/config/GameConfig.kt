package com.example.game.config

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * GameConfig: Centralized configuration for all gameplay values, rewards, prices, and settings.
 *
 * Beginner Note:
 * All numeric balance numbers and constants are organized here.
 * If you want to change how many coins a player gets, power-up prices, or move counts,
 * change them in this file!
 */
object GameConfig {

    /**
     * Rewards given to the player during gameplay.
     */
    object Rewards {
        const val DAILY_CHALLENGE_COINS = 50
        const val LEVEL_WIN_BASE_COINS = 30
        const val COIN_BLOCK_COINS = 10
        const val STAR_COLLECT_POINTS = 250
        const val COIN_BLOCK_POINTS = 150
        const val NORMAL_BLOCK_POINTS = 50
        const val STRONG_BLOCK_POINTS = 100
        const val ICE_BLOCK_POINTS = 100
        const val MOVING_BLOCK_POINTS = 100
        const val BOMB_BASE_POINTS = 300
        const val MAGIC_BASE_POINTS = 400
        const val RAINBOW_BASE_POINTS = 500
        const val POWER_UP_BLOCK_POINTS = 60
        const val BLOKI_RESCUE_BONUS_POINTS = 1000
        const val LEFTOVER_MOVE_BONUS_POINTS = 150
    }

    /**
     * Initial profile values when a new player launches the game for the first time.
     */
    object PlayerDefaults {
        const val INITIAL_COINS = 200
        const val INITIAL_STARS = 0
        const val INITIAL_LEVEL = 1

        // Initial free power-ups given to new players
        const val INITIAL_HAMMERS = 3
        const val INITIAL_ROCKETS = 3
        const val INITIAL_RAINBOWS = 2
        const val INITIAL_WANDS = 2
        const val INITIAL_SHUFFLES = 3

        const val DEFAULT_COSTUME = "classic"
        const val DEFAULT_THEME = "rainbow_garden"
        const val DEFAULT_SKIN = "jelly"
    }

    /**
     * Level progression configuration.
     */
    object Levels {
        const val HANDCRAFTED_COUNT = 20
        const val MAX_LEVELS = 1000
    }

    /**
     * Store and Shop prices for power-up bundles.
     */
    object PowerUps {
        const val HAMMER_PRICE = 50
        const val HAMMER_BUNDLE_SIZE = 3

        const val ROCKET_PRICE = 75
        const val ROCKET_BUNDLE_SIZE = 3

        const val RAINBOW_PRICE = 100
        const val RAINBOW_BUNDLE_SIZE = 2

        const val WAND_PRICE = 80
        const val WAND_BUNDLE_SIZE = 2

        const val SHUFFLE_PRICE = 40
        const val SHUFFLE_BUNDLE_SIZE = 3
    }

    /**
     * Achievements configuration: Targets and coin rewards.
     */
    object Achievements {
        const val FIRST_BREAK_ID = "first_break"
        const val FIRST_BREAK_TARGET = 1
        const val FIRST_BREAK_REWARD = 50

        const val STAR_COLLECTOR_ID = "star_collector"
        const val STAR_COLLECTOR_TARGET = 20
        const val STAR_COLLECTOR_REWARD = 100

        const val PUZZLE_MASTER_ID = "puzzle_master"
        const val PUZZLE_MASTER_TARGET = 10
        const val PUZZLE_MASTER_REWARD = 150

        const val COIN_HUNTER_ID = "coin_hunter"
        const val COIN_HUNTER_TARGET = 300
        const val COIN_HUNTER_REWARD = 120

        const val PERFECT_PLAYER_ID = "perfect_player"
        const val PERFECT_PLAYER_TARGET = 5
        const val PERFECT_PLAYER_REWARD = 200
    }

    /**
     * Returns today's calendar date in YYYY-MM-DD format for persistent daily tracking.
     */
    fun getTodayDateString(): String {
        val calendar = Calendar.getInstance()
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(calendar.time)
    }
}
