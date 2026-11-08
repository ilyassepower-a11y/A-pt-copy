package com.example.game.ui

import androidx.compose.ui.graphics.Color

// Items you can buy with toppings and use during a run
enum class PowerUp(
    val id: String,
    val icon: String,
    val titleAr: String,
    val descAr: String,
    val cost: Int,
    val color: Color
) {
    SHIELD("shield", "🛡️", "درع", "حصانة من المطارد لمدة 5 ثواني", 15, Color(0xFF1E88E5)),
    TURBO("turbo", "⚡", "توربو", "سرعة خارقة وحصانة لمدة 6 ثواني", 20, Color(0xFFFF6F00)),
    FREEZE("freeze", "❄️", "تجميد", "يشل المطارد لمدة 5 ثواني", 25, Color(0xFF00ACC1)),
    HOURGLASS("hourglass", "⏳", "ساعة رملية", "ينقص 10 ثواني من وقت الهروب", 30, Color(0xFF8E24AA))
}
