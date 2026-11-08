package com.example.game.ui

import com.example.game.data.GameSaveData

data class Milestone(
    val id: String,
    val icon: String,
    val titleAr: String,
    val descAr: String,
    val target: Int,
    val reward: Int,
    val progressOf: (GameSaveData) -> Int
)

private fun rankValue(rank: String): Int = when (rank) {
    "P" -> 5
    "S" -> 4
    "A" -> 3
    "B" -> 2
    "C" -> 1
    "D" -> 0
    else -> -1
}

val ALL_MILESTONES: List<Milestone> = listOf(
    Milestone("first_escape", "🏃", "أول هروب", "اهرب من المطارد مرة وحدة", 1, 10) { it.totalEscapes },
    Milestone("escape_5", "🏃‍♂️", "هارب محترف", "اهرب 5 مرات", 5, 25) { it.totalEscapes },
    Milestone("escape_20", "🦸", "أسطورة الهروب", "اهرب 20 مرة", 20, 80) { it.totalEscapes },
    Milestone("toppings_50", "🍕", "جامع المكونات", "اجمع 50 مكون", 50, 15) { it.totalToppings },
    Milestone("toppings_300", "🧺", "تاجر البيتزا", "اجمع 300 مكون", 300, 60) { it.totalToppings },
    Milestone("games_10", "🎮", "لاعب مثابر", "العب 10 جولات", 10, 15) { it.totalGames },
    Milestone("score_3000", "⭐", "صياد النقاط", "حقق 3000 نقطة فجولة وحدة", 3000, 30) { it.highScore },
    Milestone("rank_a", "🏅", "رتبة A", "حقق رتبة A", 1, 40) { if (rankValue(it.bestRank) >= 3) 1 else 0 },
    Milestone("rank_s", "👑", "رتبة S", "حقق رتبة S", 1, 80) { if (rankValue(it.bestRank) >= 4) 1 else 0 }
)

fun Milestone.isDone(save: GameSaveData): Boolean = progressOf(save) >= target
