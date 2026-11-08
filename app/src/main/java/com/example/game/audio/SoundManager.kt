package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.MediaPlayer
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class SoundManager(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    var isMuted: Boolean = false
        set(value) {
            field = value
            applyMusicVolume()
        }
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
        playJingle(R.raw.jingle_gameover)
    }

    fun playVictory() {
        playJingle(R.raw.jingle_victory)
    }

    // ---------------- Soundtrack (OGG files in res/raw) ----------------
    private var music: MediaPlayer? = null
    private var currentMusicRes = 0
    private var jingle: MediaPlayer? = null
    private var musicSuspended = false
    private val musicVolume = 0.55f

    fun startMenuBgm() {
        releaseJingle()
        playMusic(R.raw.music_menu)
    }

    fun startEscapeBgm(urgent: Boolean) {
        releaseJingle()
        playMusic(if (urgent) R.raw.music_urgent else R.raw.music_escape)
    }

    fun stopBgm() {
        releaseMusic()
    }

    // The app went to the background: silence everything
    fun suspendMusic() {
        musicSuspended = true
        try {
            music?.let { if (it.isPlaying) it.pause() }
        } catch (e: Exception) {
        }
        try {
            jingle?.let { if (it.isPlaying) it.pause() }
        } catch (e: Exception) {
        }
    }

    // The app is back on screen
    fun resumeMusic() {
        musicSuspended = false
        applyMusicVolume()
        try {
            jingle?.let { if (!it.isPlaying && !isMuted) it.start() }
        } catch (e: Exception) {
        }
    }

    fun release() {
        releaseMusic()
        releaseJingle()
    }

    private fun playMusic(resId: Int) {
        if (music != null && currentMusicRes == resId) {
            applyMusicVolume()
            return
        }
        releaseMusic()
        try {
            val player = MediaPlayer.create(context, resId) ?: return
            player.isLooping = true
            music = player
            currentMusicRes = resId
            applyMusicVolume()
        } catch (e: Exception) {
            music = null
            currentMusicRes = 0
        }
    }

    private fun applyMusicVolume() {
        val player = music ?: return
        try {
            val volume = if (isMuted) 0f else musicVolume
            player.setVolume(volume, volume)
            if (isMuted || musicSuspended) {
                if (player.isPlaying) player.pause()
            } else if (!player.isPlaying) {
                player.start()
            }
        } catch (e: Exception) {
        }
    }

    private fun playJingle(resId: Int) {
        releaseJingle()
        if (isMuted) return
        try {
            val player = MediaPlayer.create(context, resId) ?: return
            player.setVolume(0.9f, 0.9f)
            player.setOnCompletionListener { finished ->
                try {
                    finished.release()
                } catch (e: Exception) {
                }
                if (jingle === finished) jingle = null
            }
            jingle = player
            if (!musicSuspended) player.start()
        } catch (e: Exception) {
            jingle = null
        }
    }

    private fun releaseMusic() {
        val player = music
        music = null
        currentMusicRes = 0
        if (player != null) {
            try {
                player.stop()
            } catch (e: Exception) {
            }
            try {
                player.release()
            } catch (e: Exception) {
            }
        }
    }

    private fun releaseJingle() {
        val player = jingle
        jingle = null
        if (player != null) {
            try {
                player.stop()
            } catch (e: Exception) {
            }
            try {
                player.release()
            } catch (e: Exception) {
            }
        }
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
