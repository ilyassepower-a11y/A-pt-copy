package com.example.game.data

import android.content.Context
import android.content.SharedPreferences

data class GameSaveData(
    val highScore: Int,
    val bestRank: String,
    val totalEscapes: Int,
    val totalGames: Int,
    val totalToppings: Int
)

class GameRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pizza_escape_prefs", Context.MODE_PRIVATE)

    fun getSaveData(): GameSaveData {
        return GameSaveData(
            highScore = prefs.getInt("high_score", 0),
            bestRank = prefs.getString("best_rank", "-") ?: "-",
            totalEscapes = prefs.getInt("total_escapes", 0),
            totalGames = prefs.getInt("total_games", 0),
            totalToppings = prefs.getInt("total_toppings", 0)
        )
    }

    fun recordGameResult(score: Int, rank: String, escaped: Boolean, toppings: Int) {
        val currentHigh = prefs.getInt("high_score", 0)
        val currentRank = prefs.getString("best_rank", "-") ?: "-"
        val escapes = prefs.getInt("total_escapes", 0)
        val games = prefs.getInt("total_games", 0)
        val totalToppings = prefs.getInt("total_toppings", 0)

        val rankOrder = mapOf("P" to 5, "S" to 4, "A" to 3, "B" to 2, "C" to 1, "D" to 0, "-" to -1)
        val currentVal = rankOrder[currentRank] ?: -1
        val newVal = rankOrder[rank] ?: 0

        val newBestRank = if (newVal > currentVal) rank else currentRank

        prefs.edit()
            .putInt("high_score", maxOf(currentHigh, score))
            .putString("best_rank", newBestRank)
            .putInt("total_escapes", if (escaped) escapes + 1 else escapes)
            .putInt("total_games", games + 1)
            .putInt("total_toppings", totalToppings + toppings)
            .apply()
    }
}
