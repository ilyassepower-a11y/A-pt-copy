package com.example.game.ui

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.R
import kotlin.random.Random

// Short sound effects (OGG files in res/raw) played with low latency
class SfxPlayer(context: Context) {
    var isMuted: Boolean = false

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    private val ids = HashMap<Int, Int>()

    init {
        val files = listOf(
            R.raw.sfx_collect,
            R.raw.sfx_stun,
            R.raw.sfx_powerup,
            R.raw.sfx_buy,
            R.raw.sfx_milestone
        )
        for (res in files) {
            ids[res] = pool.load(context, res, 1)
        }
    }

    private fun play(res: Int, volume: Float, rate: Float = 1f) {
        if (isMuted) return
        val id = ids[res] ?: return
        try {
            pool.play(id, volume, volume, 1, 0, rate)
        } catch (e: Exception) {
        }
    }

    fun playCollect() = play(R.raw.sfx_collect, 0.8f, 0.95f + Random.nextFloat() * 0.15f)
    fun playStun() = play(R.raw.sfx_stun, 1f)
    fun playPowerUp() = play(R.raw.sfx_powerup, 0.9f)
    fun playBuy() = play(R.raw.sfx_buy, 0.9f)
    fun playMilestone() = play(R.raw.sfx_milestone, 1f)

    fun release() {
        try {
            pool.release()
        } catch (e: Exception) {
        }
    }
}
