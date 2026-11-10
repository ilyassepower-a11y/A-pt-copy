package com.example.game.ui

import com.example.R

// Secret code that unlocks the admin skin (change it if you want)
const val ADMIN_CODE = "ZYNOX-ADMIN"

enum class Skin(
    val id: String,
    val nameEn: String,
    val taglineAr: String,
    val price: Int,
    val previewRes: Int,
    val adminOnly: Boolean = false
) {
    CHEF("chef", "Classic Chef", "الطباخ الأصلي", 0, R.drawable.skin_chef),
    HERO_CHEF("hero_chef", "The Hero Chef", "الطباخ بنسخة صفراء وابتسامة ماكرة", 200, R.drawable.skin_hero),
    BRAVE("brave", "The Brave", "شجاع وهادئ بمعطف طويل وكاسكيط", 250, R.drawable.skin_brave),
    ZYNOX("zynox", "Zynox", "سكن الأدمن: شخصيتك الأصلية بستايل الطباخ", 0, R.drawable.skin_zynox, true),
    HEDGEHOG("hedgehog", "The Hedgehog", "قنفذ أزرق سريع بحذاء أحمر", 350, R.drawable.skin_hedgehog),
    SKELETON("skeleton", "Funny Skeleton", "هيكل عظمي مرح بابتسامة دايمة", 400, R.drawable.skin_skeleton)
}
