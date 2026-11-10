package com.example.game.ui

import androidx.compose.ui.graphics.Color
import com.example.R

// Items you can buy with toppings and use during a run
enum class PowerUp(
    val id: String,
    val iconRes: Int,
    val titleAr: String,
    val descAr: String,
    val cost: Int,
    val color: Color
) {
    SHIELD("shield", R.drawable.pu_shield, "درع", "حصانة من المطارد لمدة 5 ثواني", 15, Color(0xFF1E88E5)),
    TURBO("turbo", R.drawable.pu_turbo, "توربو", "سرعة خارقة وحصانة لمدة 6 ثواني", 20, Color(0xFFFF6F00)),
    FREEZE("freeze", R.drawable.pu_freeze, "تجميد", "يشل المطارد لمدة 5 ثواني", 25, Color(0xFF00ACC1)),
    HOURGLASS("hourglass", R.drawable.pu_hourglass, "ساعة رملية", "ينقص 10 ثواني من وقت الهروب", 30, Color(0xFF8E24AA))
}
