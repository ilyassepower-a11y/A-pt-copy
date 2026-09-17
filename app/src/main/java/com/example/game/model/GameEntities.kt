package com.example.game.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class GameDifficulty(
    val titleEn: String,
    val titleAr: String,
    val durationSeconds: Float,
    val pursuerBaseSpeed: Float,
    val descriptionEn: String,
    val descriptionAr: String
) {
    SPRINT(
        "Sprint Escape",
        "هروب سريع",
        45f,
        240f,
        "45 seconds fast survival challenge",
        "تحدي البقاء السريع لمدة 45 ثانية"
    ),
    CLASSIC(
        "Classic Tower",
        "البرج الكلاسيكي",
        60f,
        280f,
        "60 seconds intense pursuit escape",
        "هروب حماسي لمدة 60 ثانية"
    ),
    PANIC(
        "Pizza Panic!",
        "هلع البيتزا الشديد!",
        90f,
        320f,
        "90 seconds nightmare pursuit with furious bursts!",
        "90 ثانية من المطاردة الجنونية والسريعة!"
    )
}

enum class GameState {
    MENU,
    RUNNING,
    PAUSED,
    GAME_OVER,
    VICTORY
}

enum class PlatformType {
    CRUST_SOLID,
    MELTING_CHEESE,
    BOUNCY_MUSHROOM,
    CRUST_BREAKABLE,
    SPEED_RAIL
}

enum class CollectibleType(val points: Int, val color: Color) {
    PEPPERONI(50, Color(0xFFD32F2F)),
    MUSHROOM(100, Color(0xFF8D6E63)),
    OLIVE(75, Color(0xFF2E7D32)),
    CHEESE_SLICE(150, Color(0xFFFFD54F)),
    HOT_PEPPER(300, Color(0xFFFF1744)),
    PIZZA_CLOCK(200, Color(0xFF00E5FF))
}

data class Platform(
    val id: Int,
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val type: PlatformType,
    var isDestroyed: Boolean = false
) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height
}

data class Collectible(
    val id: Int,
    val x: Float,
    val y: Float,
    val radius: Float = 16f,
    val type: CollectibleType,
    var isCollected: Boolean = false,
    var bobOffset: Float = 0f
)

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var size: Float,
    var life: Float,
    val maxLife: Float,
    val shape: ParticleShape = ParticleShape.CIRCLE
)

enum class ParticleShape {
    CIRCLE,
    STAR,
    FLAME,
    SPARK
}

data class GhostTrail(
    val x: Float,
    val y: Float,
    val facingRight: Boolean,
    val machLevel: Int,
    var alpha: Float = 0.6f
)

data class Player(
    var x: Float = 400f,
    var y: Float = 800f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val width: Float = 44f,
    val height: Float = 56f,
    var facingRight: Boolean = true,
    var isGrounded: Boolean = false,
    var machLevel: Int = 0, // 0 to 4
    var machRunTimer: Float = 0f,
    var isDashing: Boolean = false,
    var isSliding: Boolean = false,
    var isTaunting: Boolean = false,
    var tauntTimer: Float = 0f,
    var hotPepperTimer: Float = 0f,
    var invincibilityTimer: Float = 0f,
    var isTouchingWall: Boolean = false,
    var wallDirection: Int = 0, // -1 left wall, +1 right wall
    var runCycle: Float = 0f
) {
    val left: Float get() = x - width / 2
    val right: Float get() = x + width / 2
    val top: Float get() = y - height
    val bottom: Float get() = y
}

enum class PursuerState {
    CHASING,
    ENRAGED,
    STUNNED
}

data class Pursuer(
    var x: Float = 100f,
    var y: Float = 300f,
    var vx: Float = 0f,
    var vy: Float = 0f,
    val radius: Float = 42f,
    var state: PursuerState = PursuerState.CHASING,
    var stunTimer: Float = 0f,
    var floatPhase: Float = 0f,
    var eyeGlow: Float = 0f,
    var speed: Float = 260f,
    var angle: Float = 0f
)

data class GameStats(
    var timeRemaining: Float = 60f,
    var totalTime: Float = 60f,
    var score: Int = 0,
    var combo: Int = 0,
    var comboTimer: Float = 0f,
    var maxCombo: Int = 0,
    var toppingsCollected: Int = 0,
    var blocksDestroyed: Int = 0,
    var tauntsPerformed: Int = 0,
    var pursuerDistance: Float = 1000f,
    var rank: String = "D"
)
