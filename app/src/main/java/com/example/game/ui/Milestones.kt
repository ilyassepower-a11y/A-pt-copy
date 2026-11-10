package com.example.game.ui

import com.example.R
import com.example.game.data.GameSaveData

data class Milestone(
    val id: String,
    val iconRes: Int,
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
    Milestone("first_escape", R.drawable.ms_sneaker, "أول هروب", "اهرب من المطارد مرة وحدة", 1, 10) { it.totalEscapes },
    Milestone("escape_5", R.drawable.ms_flag, "هارب محترف", "اهرب 5 مرات", 5, 25) { it.totalEscapes },
    Milestone("escape_20", R.drawable.ms_rocket, "أسطورة الهروب", "اهرب 20 مرة", 20, 80) { it.totalEscapes },
    Milestone("toppings_50", R.drawable.ms_slice, "جامع المكونات", "اجمع 50 مكون", 50, 15) { it.totalToppings },
    Milestone("toppings_300", R.drawable.ms_pizza, "تاجر البيتزا", "اجمع 300 مكون", 300, 60) { it.totalToppings },
    Milestone("games_10", R.drawable.ms_gamepad, "لاعب مثابر", "العب 10 جولات", 10, 15) { it.totalGames },
    Milestone("score_3000", R.drawable.ms_star, "صياد النقاط", "حقق 3000 نقطة فجولة وحدة", 3000, 30) { it.highScore },
    Milestone("rank_a", R.drawable.ms_medal_a, "رتبة A", "حقق رتبة A", 1, 40) { if (rankValue(it.bestRank) >= 3) 1 else 0 },
    Milestone("rank_s", R.drawable.ms_crown, "رتبة S", "حقق رتبة S", 1, 80) { if (rankValue(it.bestRank) >= 4) 1 else 0 }
)

fun Milestone.isDone(save: GameSaveData): Boolean = progressOf(save) >= target
