package com.example.game.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class SoundManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isMuted: Boolean = false
    private var musicJob: Job? = null
    private val sampleRate = 22050

    fun playJump() {
        if (isMuted) return
        scope.launch {
            // Sweep from 280 Hz to 640 Hz
            playToneSweep(280.0, 640.0, 0.12f, 0.5f)
        }
    }

    fun playMachBoost() {
        if (isMuted) return
        scope.launch {
            // Rising whoosh sound
            playToneSweep(180.0, 520.0, 0.20f, 0.6f)
        }
    }

    fun playWallSmash() {
        if (isMuted) return
        scope.launch {
            // Low crunch impact
            playNoiseCrunch(0.18f, 0.7f)
        }
    }

    fun playCollect() {
        if (isMuted) return
        scope.launch {
            // Quick happy ping
            playTone(880.0, 0.08f, 0.45f)
            playTone(1320.0, 0.10f, 0.45f)
        }
    }

    fun playTauntParry() {
        if (isMuted) return
        scope.launch {
            // Bright parry chime
            playTone(1046.5, 0.06f, 0.6f)
            playTone(1318.5, 0.06f, 0.6f)
            playTone(2093.0, 0.18f, 0.7f)
        }
    }

    fun playBounce() {
        if (isMuted) return
        scope.launch {
            playToneSweep(200.0, 800.0, 0.22f, 0.6f)
        }
    }

    fun playPursuerAlert() {
        if (isMuted) return
        scope.launch {
            // Ominous double buzz
            playTone(140.0, 0.12f, 0.6f)
            delay(50)
            playTone(120.0, 0.16f, 0.7f)
        }
    }

    fun playCaught() {
        if (isMuted) return
        scope.launch {
            playToneSweep(440.0, 90.0, 0.45f, 0.8f)
            playNoiseCrunch(0.35f, 0.8f)
        }
    }

    fun playVictory() {
        if (isMuted) return
        scope.launch {
            val notes = listOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
            for (freq in notes) {
                playTone(freq, 0.12f, 0.6f)
                delay(90)
            }
            playTone(1567.98, 0.4f, 0.7f)
        }
    }

    fun startEscapeBgm(urgent: Boolean) {
        musicJob?.cancel()
        musicJob = scope.launch {
            var step = 0
            val bassFreqs = listOf(110.0, 110.0, 130.8, 146.8, 110.0, 164.8, 146.8, 123.5)
            val urgentBass = listOf(146.8, 146.8, 174.6, 196.0, 220.0, 196.0, 174.6, 164.8)
            while (isActive) {
                if (!isMuted) {
                    val list = if (urgent) urgentBass else bassFreqs
                    val freq = list[step % list.size]
                    val duration = if (urgent) 0.12f else 0.18f
                    playTone(freq, duration, if (urgent) 0.35f else 0.25f)
                    
                    if (step % 2 == 1) {
                        playNoiseCrunch(0.04f, 0.2f)
                    }
                }
                step++
                delay(if (urgent) 140L else 210L)
            }
        }
    }

    fun stopBgm() {
        musicJob?.cancel()
        musicJob = null
    }

    private fun playTone(frequency: Double, durationSeconds: Float, volume: Float) {
        try {
            val numSamples = (durationSeconds * sampleRate).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val time = i.toDouble() / sampleRate
                val envelope = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)
                    i > numSamples * 0.7 -> (numSamples - i) / (numSamples * 0.3)
                    else -> 1.0
                }
                val sample = (sin(2.0 * PI * frequency * time) * Short.MAX_VALUE * volume * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAudio(buffer)
        } catch (_: Exception) {}
    }

    private fun playToneSweep(startFreq: Double, endFreq: Double, durationSeconds: Float, volume: Float) {
        try {
            val numSamples = (durationSeconds * sampleRate).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            var currentPhase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                currentPhase += 2.0 * PI * currentFreq / sampleRate
                val envelope = 1.0 - progress * 0.5
                val sample = (sin(currentPhase) * Short.MAX_VALUE * volume * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAudio(buffer)
        } catch (_: Exception) {}
    }

    private fun playNoiseCrunch(durationSeconds: Float, volume: Float) {
        try {
            val numSamples = (durationSeconds * sampleRate).toInt().coerceAtLeast(1)
            val buffer = ShortArray(numSamples)
            val random = Random(42)
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val envelope = (1.0 - progress) * (1.0 - progress)
                val noise = (random.nextDouble() * 2.0 - 1.0)
                val sample = (noise * Short.MAX_VALUE * volume * envelope).toInt()
                buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            writeAudio(buffer)
        } catch (_: Exception) {}
    }

    private fun writeAudio(buffer: ShortArray) {
        var track: AudioTrack? = null
        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = buffer.size.coerceAtLeast(minBufSize)
            track = AudioTrack.Builder()
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
                .setBufferSizeInBytes(bufferSize * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep((buffer.size.toFloat() / sampleRate * 1000).toLong() + 30)
        } catch (_: Exception) {
        } finally {
            try {
                track?.stop()
                track?.release()
            } catch (_: Exception) {}
        }
    }
}
