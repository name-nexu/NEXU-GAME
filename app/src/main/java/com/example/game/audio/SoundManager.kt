package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * High-performance, self-contained kid-friendly audio & haptic engine.
 * Generates cheerful synthesized sound effects & background melody using PCM AudioTrack.
 * Guaranteed 100% offline, zero external dependencies, 100% original.
 */
class SoundManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var musicJob: Job? = null

    var soundEnabled: Boolean = true
    var musicEnabled: Boolean = true
    var hapticsEnabled: Boolean = true

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val sampleRate = 22050

    // Play quick synthesized waveform
    private fun playTone(
        frequencies: List<Float>,
        durationMs: Int,
        type: ToneType = ToneType.SINE,
        volume: Float = 0.5f
    ) {
        if (!soundEnabled) return

        scope.launch {
            try {
                val numSamples = (sampleRate * (durationMs / 1000f)).toInt()
                val buffer = ShortArray(numSamples)
                val freqStep = if (frequencies.isNotEmpty()) numSamples / frequencies.size else numSamples

                var currentPhase = 0.0
                for (i in 0 until numSamples) {
                    val freqIndex = (i / freqStep).coerceIn(0, frequencies.size - 1)
                    val freq = frequencies[freqIndex]
                    val envelope = when {
                        i < numSamples * 0.1f -> i / (numSamples * 0.1f) // Attack
                        i > numSamples * 0.7f -> (numSamples - i) / (numSamples * 0.3f) // Decay
                        else -> 1.0f
                    }

                    val sampleValue = when (type) {
                        ToneType.SINE -> sin(currentPhase)
                        ToneType.SQUARE -> if (sin(currentPhase) >= 0) 0.8 else -0.8
                        ToneType.CHIRP -> sin(currentPhase) * (0.8 + 0.2 * sin(currentPhase * 0.5))
                        ToneType.NOISE -> (Math.random() * 2.0 - 1.0) * 0.7
                    }

                    buffer[i] = (sampleValue * Short.MAX_VALUE * volume * envelope).toInt().toShort()
                    currentPhase += 2.0 * PI * freq / sampleRate
                }

                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(buffer.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(buffer, 0, buffer.size)
                audioTrack.play()

                delay(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio hardware transient failures
            }
        }
    }

    enum class ToneType { SINE, SQUARE, CHIRP, NOISE }

    fun playTap() {
        playTone(listOf(900f, 1100f), 35, ToneType.SINE, 0.4f)
        vibrate(15)
    }

    fun playBlockBreak() {
        playTone(listOf(520f, 380f, 220f), 90, ToneType.CHIRP, 0.6f)
        vibrate(30)
    }

    fun playStrongBlockHit() {
        playTone(listOf(300f, 260f), 60, ToneType.SQUARE, 0.5f)
        vibrate(25)
    }

    fun playIceBreak() {
        playTone(listOf(1800f, 2200f, 1600f), 110, ToneType.CHIRP, 0.5f)
        vibrate(35)
    }

    fun playBombBlast() {
        playTone(listOf(180f, 120f, 70f), 200, ToneType.NOISE, 0.7f)
        vibrate(70)
    }

    fun playCoin() {
        playTone(listOf(1318f, 1760f), 90, ToneType.SINE, 0.55f)
        vibrate(20)
    }

    fun playStar() {
        playTone(listOf(987f, 1318f, 1975f), 160, ToneType.SINE, 0.65f)
        vibrate(40)
    }

    fun playPowerUp() {
        playTone(listOf(440f, 660f, 880f, 1320f), 180, ToneType.CHIRP, 0.6f)
        vibrate(45)
    }

    fun playWin() {
        playTone(listOf(523f, 659f, 784f, 1046f, 1318f), 380, ToneType.SINE, 0.7f)
        vibratePattern(longArrayOf(0, 50, 60, 100))
    }

    fun playFail() {
        playTone(listOf(420f, 370f, 310f, 240f), 320, ToneType.SINE, 0.5f)
        vibrate(60)
    }

    private fun vibrate(durationMs: Long) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(timings: LongArray) {
        if (!hapticsEnabled || vibrator == null || !vibrator.hasVibrator()) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }

    // Cheerful, gentle music box melody
    fun startBackgroundMusic() {
        if (!musicEnabled || musicJob?.isActive == true) return

        musicJob = scope.launch {
            // Playful pentatonic notes: C5, D5, E5, G5, A5, C6
            val notes = listOf(
                523.25f, 659.25f, 783.99f, 659.25f,
                587.33f, 783.99f, 880.00f, 1046.50f,
                783.99f, 659.25f, 587.33f, 523.25f,
                659.25f, 587.33f, 523.25f, 392.00f
            )

            while (isActive && musicEnabled) {
                for (note in notes) {
                    if (!isActive || !musicEnabled) break
                    playTone(listOf(note), 140, ToneType.SINE, 0.15f)
                    delay(260)
                }
                delay(600)
            }
        }
    }

    fun stopBackgroundMusic() {
        musicJob?.cancel()
        musicJob = null
    }

    fun setMusic(enabled: Boolean) {
        musicEnabled = enabled
        if (enabled) {
            startBackgroundMusic()
        } else {
            stopBackgroundMusic()
        }
    }
}
