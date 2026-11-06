package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.game.engine.PizzaEscapeEngine
import com.example.game.model.CollectibleType
import com.example.game.model.ParticleShape
import com.example.game.model.PlatformType
import com.example.game.model.PursuerState
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun GameCanvasView(
    engine: PizzaEscapeEngine,
    tick: State<Long>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Read the frame tick here so only the drawing is redone each frame
        tick.value

        val screenW = size.width
        val screenH = size.height

        // Screen Shake calculation
        val shakeX = if (engine.screenShake > 0f) (Random.nextFloat() - 0.5f) * engine.screenShake * 2f else 0f
        val shakeY = if (engine.screenShake > 0f) (Random.nextFloat() - 0.5f) * engine.screenShake * 2f else 0f

        val camX = engine.cameraX - shakeX
        val camY = engine.cameraY - shakeY

        // 1. Draw Atmospheric Parallax Background
        drawTowerBackground(camX, camY, screenW, screenH, engine.pursuer.state == PursuerState.ENRAGED)

        // 2. World Elements with Camera Transform
        withTransform({
            translate(
                left = -camX + screenW / 2f,
                top = -camY + screenH / 2f
            )
        }) {
            // Platforms
            for (p in engine.platforms) {
                if (p.isDestroyed) continue
                drawPlatform(p)
            }

            // Collectibles
            for (c in engine.collectibles) {
                if (c.isCollected) continue
                drawCollectible(c.x, c.y + c.bobOffset, c.radius, c.type)
            }

            // Ghost Trails
            for (g in engine.ghostTrails) {
                drawGhostTrail(g.x, g.y, g.facingRight, g.machLevel, g.alpha)
            }

            // Particles
            for (pt in engine.particles) {
                drawParticle(pt.x, pt.y, pt.color, pt.size, pt.shape, pt.life / pt.maxLife)
            }

            // Player (Peppino-style Chef)
            drawPlayer(engine)

            // Pursuer (Pizzaface)
            drawPursuer(engine)
        }

        // 3. Proximity Danger Vignette & Offscreen Pursuer Radar
        drawDangerVignette(screenW, screenH, engine.stats.pursuerDistance)
        drawPursuerOffscreenIndicator(screenW, screenH, camX, camY, engine.pursuer.x, engine.pursuer.y)
    }
}

private fun DrawScope.drawTowerBackground(
    camX: Float,
    camY: Float,
    w: Float,
    h: Float,
    isEnraged: Boolean
) {
    // Deep brick tower backdrop
    val topColor = if (isEnraged) Color(0xFF3E0A0A) else Color(0xFF1E1428)
    val bottomColor = if (isEnraged) Color(0xFF6B1212) else Color(0xFF331C38)
    drawRect(
        brush = Brush.verticalGradient(listOf(topColor, bottomColor)),
        size = Size(w, h)
    )

    // Parallax brick grid
    val parallaxX = (camX * 0.2f) % 80f
    val parallaxY = (camY * 0.2f) % 50f
    val brickColor = if (isEnraged) Color(0x22FF5252) else Color(0x18FFFFFF)

    var y = -parallaxY
    while (y < h + 50f) {
        var x = -parallaxX
        while (x < w + 80f) {
            drawRoundRect(
                color = brickColor,
                topLeft = Offset(x, y),
                size = Size(74f, 44f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            x += 80f
        }
        y += 50f
    }
}

private fun DrawScope.drawPlatform(p: com.example.game.model.Platform) {
    when (p.type) {
        PlatformType.CRUST_SOLID -> {
            // Baked pizza crust with dark border
            drawRoundRect(
                color = Color(0xFFB5651D),
                topLeft = Offset(p.x, p.y),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Cheesy top surface
            drawRoundRect(
                color = Color(0xFFFFB300),
                topLeft = Offset(p.x, p.y),
                size = Size(p.width, 10f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        PlatformType.MELTING_CHEESE -> {
            drawRoundRect(
                color = Color(0xFFFFD54F),
                topLeft = Offset(p.x, p.y),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(8f, 8f)
            )
            // Dripping cheese drips
            var dripX = p.x + 20f
            while (dripX < p.right - 20f) {
                drawCircle(
                    color = Color(0xFFFFCA28),
                    radius = 7f,
                    center = Offset(dripX, p.bottom + 2f)
                )
                dripX += 45f
            }
        }
        PlatformType.BOUNCY_MUSHROOM -> {
            // Mushroom bounce spring pad
            drawRoundRect(
                color = Color(0xFFE91E63),
                topLeft = Offset(p.x, p.y + 6f),
                size = Size(p.width, p.height - 6f),
                cornerRadius = CornerRadius(14f, 14f)
            )
            // White bouncy spots
            drawCircle(Color.White, 7f, Offset(p.x + p.width * 0.3f, p.y + 16f))
            drawCircle(Color.White, 9f, Offset(p.x + p.width * 0.5f, p.y + 14f))
            drawCircle(Color.White, 7f, Offset(p.x + p.width * 0.7f, p.y + 16f))
            // Top spring highlight
            drawRoundRect(
                color = Color(0xFFFF80AB),
                topLeft = Offset(p.x + 8f, p.y),
                size = Size(p.width - 16f, 8f),
                cornerRadius = CornerRadius(6f, 6f)
            )
        }
        PlatformType.CRUST_BREAKABLE -> {
            // Cracked breakable crust
            drawRoundRect(
                color = Color(0xFF8D6E63),
                topLeft = Offset(p.x, p.y),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Crack lines
            drawLine(
                color = Color(0xFF3E2723),
                start = Offset(p.x + 10f, p.y + 10f),
                end = Offset(p.x + p.width - 10f, p.y + p.height - 10f),
                strokeWidth = 3f
            )
            drawLine(
                color = Color(0xFF3E2723),
                start = Offset(p.x + p.width - 15f, p.y + 15f),
                end = Offset(p.x + 15f, p.y + p.height - 15f),
                strokeWidth = 3f
            )
        }
        PlatformType.SPEED_RAIL -> {
            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(p.x, p.y),
                size = Size(p.width, p.height),
                cornerRadius = CornerRadius(6f, 6f)
            )
            // Speed arrows
            var arrowX = p.x + 15f
            while (arrowX < p.right - 15f) {
                drawLine(
                    color = Color.White,
                    start = Offset(arrowX, p.y + 8f),
                    end = Offset(arrowX + 12f, p.y + p.height / 2f),
                    strokeWidth = 4f
                )
                drawLine(
                    color = Color.White,
                    start = Offset(arrowX + 12f, p.y + p.height / 2f),
                    end = Offset(arrowX, p.y + p.height - 8f),
                    strokeWidth = 4f
                )
                arrowX += 35f
            }
        }
    }
}

private fun DrawScope.drawCollectible(x: Float, y: Float, radius: Float, type: CollectibleType) {
    when (type) {
        CollectibleType.PEPPERONI -> {
            drawCircle(Color(0xFFD32F2F), radius, Offset(x, y))
            drawCircle(Color(0xFFB71C1C), radius, Offset(x, y), style = Stroke(3f))
            drawCircle(Color(0xFFFFCDD2), 3f, Offset(x - 4f, y - 4f))
            drawCircle(Color(0xFFFFCDD2), 2.5f, Offset(x + 5f, y + 3f))
        }
        CollectibleType.MUSHROOM -> {
            // Cap
            drawArc(
                color = Color(0xFFD7CCC8),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(x - radius, y - radius),
                size = Size(radius * 2, radius * 2)
            )
            // Stem
            drawRoundRect(
                color = Color(0xFFEFEBE9),
                topLeft = Offset(x - 5f, y),
                size = Size(10f, radius * 0.9f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
        CollectibleType.OLIVE -> {
            drawCircle(Color(0xFF33691E), radius, Offset(x, y))
            drawCircle(Color(0xFF1B5E20), radius, Offset(x, y), style = Stroke(3f))
            drawCircle(Color(0xFF1E1428), radius * 0.4f, Offset(x, y))
        }
        CollectibleType.CHEESE_SLICE -> {
            val path = Path().apply {
                moveTo(x - radius, y + radius * 0.8f)
                lineTo(x + radius, y + radius * 0.8f)
                lineTo(x, y - radius)
                close()
            }
            drawPath(path, Color(0xFFFFD54F))
            drawCircle(Color(0xFFFFA000), 4f, Offset(x - 3f, y + 2f))
            drawCircle(Color(0xFFFFA000), 3f, Offset(x + 5f, y + 5f))
        }
        CollectibleType.HOT_PEPPER -> {
            // Fiery aura
            drawCircle(Color(0x55FF5722), radius * 1.5f, Offset(x, y))
            // Pepper body
            val path = Path().apply {
                moveTo(x - radius * 0.7f, y - radius * 0.8f)
                quadraticTo(x + radius, y, x, y + radius)
                quadraticTo(x - radius, y + radius * 0.3f, x - radius * 0.7f, y - radius * 0.8f)
                close()
            }
            drawPath(path, Color(0xFFFF1744))
            // Green stem
            drawCircle(Color(0xFF4CAF50), 5f, Offset(x - radius * 0.6f, y - radius * 0.8f))
        }
        CollectibleType.PIZZA_CLOCK -> {
            drawCircle(Color(0xFF00E5FF), radius, Offset(x, y))
            drawCircle(Color.White, radius * 0.75f, Offset(x, y))
            drawLine(Color.Black, Offset(x, y), Offset(x, y - radius * 0.5f), strokeWidth = 3f)
            drawLine(Color.Black, Offset(x, y), Offset(x + radius * 0.4f, y), strokeWidth = 3f)
        }
    }
}

private fun DrawScope.drawGhostTrail(
    x: Float,
    y: Float,
    facingRight: Boolean,
    machLevel: Int,
    alpha: Float
) {
    val trailColor = when (machLevel) {
        4 -> Color(0xFFFF1744).copy(alpha = alpha * 0.7f)
        3 -> Color(0xFFFF9100).copy(alpha = alpha * 0.6f)
        else -> Color(0xFF00E5FF).copy(alpha = alpha * 0.5f)
    }

    drawCircle(
        color = trailColor,
        radius = 24f,
        center = Offset(x, y - 28f)
    )
}

private fun DrawScope.drawParticle(
    x: Float,
    y: Float,
    color: Color,
    size: Float,
    shape: ParticleShape,
    lifeRatio: Float
) {
    val alphaColor = color.copy(alpha = lifeRatio.coerceIn(0f, 1f))
    when (shape) {
        ParticleShape.CIRCLE -> {
            drawCircle(alphaColor, size * lifeRatio, Offset(x, y))
        }
        ParticleShape.STAR, ParticleShape.SPARK -> {
            val s = size * lifeRatio
            drawLine(alphaColor, Offset(x - s, y), Offset(x + s, y), strokeWidth = 3f)
            drawLine(alphaColor, Offset(x, y - s), Offset(x, y + s), strokeWidth = 3f)
        }
        ParticleShape.FLAME -> {
            drawCircle(alphaColor, size * lifeRatio * 1.2f, Offset(x, y))
        }
    }
}

private fun DrawScope.drawPlayer(engine: PizzaEscapeEngine) {
    val p = engine.player
    val cx = p.x
    val cy = p.y
    val facing = if (p.facingRight) 1f else -1f

    // Flame aura at Mach 3 / 4 or Hot Pepper
    if (p.machLevel >= 3 || p.hotPepperTimer > 0f) {
        val auraColor = if (p.machLevel == 4) Color(0x66FF1744) else Color(0x66FF9800)
        drawCircle(
            color = auraColor,
            radius = 36f,
            center = Offset(cx, cy - 28f)
        )
    }

    // Taunt Heroic Pose!
    if (p.isTaunting) {
        // Freeze-frame sparkling pose
        drawCircle(Color(0xFFFFF176), 34f, Offset(cx, cy - 28f), style = Stroke(4f))
        // Body
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(cx - 16f, cy - 44f),
            size = Size(32f, 30f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Chef Hat
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(cx - 14f, cy - 62f),
            size = Size(28f, 22f),
            cornerRadius = CornerRadius(10f, 10f)
        )
        // Black mustache & triumphant eyes
        drawCircle(Color.Black, 4f, Offset(cx - 5f, cy - 36f))
        drawCircle(Color.Black, 4f, Offset(cx + 5f, cy - 36f))
        drawLine(Color.Black, Offset(cx - 12f, cy - 30f), Offset(cx + 12f, cy - 30f), strokeWidth = 5f)
        return
    }

    // Normal & Mach Running Chef
    val bob = if (p.isGrounded && Math.abs(p.vx) > 20f) sin(p.runCycle * 2f) * 4f else 0f

    // White Chef Uniform Body
    drawRoundRect(
        color = Color(0xFFFAFAFA),
        topLeft = Offset(cx - 16f, cy - 44f + bob),
        size = Size(32f, 28f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Apron / Overalls
    drawRoundRect(
        color = Color(0xFF212121),
        topLeft = Offset(cx - 14f, cy - 26f + bob),
        size = Size(28f, 16f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Running legs
    val legOffset1 = if (p.isGrounded) sin(p.runCycle) * 10f else 6f
    val legOffset2 = if (p.isGrounded) -sin(p.runCycle) * 10f else -6f
    drawLine(
        color = Color(0xFF212121),
        start = Offset(cx - 6f, cy - 12f + bob),
        end = Offset(cx - 6f + legOffset1 * facing, cy),
        strokeWidth = 7f
    )
    drawLine(
        color = Color(0xFF212121),
        start = Offset(cx + 6f, cy - 12f + bob),
        end = Offset(cx + 6f + legOffset2 * facing, cy),
        strokeWidth = 7f
    )

    // Fluffy Chef Hat (tilted back if dashing!)
    val hatTilt = if (p.isDashing) -facing * 10f else 0f
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - 16f + hatTilt, cy - 64f + bob),
        size = Size(32f, 24f),
        cornerRadius = CornerRadius(12f, 12f)
    )
    drawCircle(Color.White, 12f, Offset(cx - 8f + hatTilt, cy - 62f + bob))
    drawCircle(Color.White, 12f, Offset(cx + 8f + hatTilt, cy - 62f + bob))

    // Expressive Panicked/Speedy Face
    val eyeX = cx + (facing * 4f)
    val eyeY = cy - 38f + bob
    // Big comical whites of eyes
    drawCircle(Color.White, 7f, Offset(eyeX - 5f, eyeY))
    drawCircle(Color.White, 7f, Offset(eyeX + 5f, eyeY))
    // Pupils (looking frantic toward movement or back toward pursuer!)
    val pupilLook = facing * 2f
    drawCircle(Color.Black, 3.5f, Offset(eyeX - 5f + pupilLook, eyeY))
    drawCircle(Color.Black, 3.5f, Offset(eyeX + 5f + pupilLook, eyeY))

    // Bushy Chef Mustache
    val mustacheY = cy - 30f + bob
    drawLine(
        color = Color(0xFF1E1E1E),
        start = Offset(eyeX - 12f, mustacheY),
        end = Offset(eyeX + 12f, mustacheY),
        strokeWidth = 6f
    )

    // Wide open screaming mouth if pursuer is near!
    if (engine.stats.pursuerDistance < 350f) {
        drawCircle(Color(0xFFD32F2F), 5f, Offset(eyeX, mustacheY + 6f))
    }
}

private fun DrawScope.drawPursuer(engine: PizzaEscapeEngine) {
    val pursuer = engine.pursuer
    val cx = pursuer.x
    val cy = pursuer.y
    val r = pursuer.radius

    val isStunned = pursuer.state == PursuerState.STUNNED
    val isEnraged = pursuer.state == PursuerState.ENRAGED

    // Sinister Dark Aura
    val auraColor = when {
        isStunned -> Color(0x33FFEB3B)
        isEnraged -> Color(0x77D50000)
        else -> Color(0x55E65100)
    }
    drawCircle(auraColor, r * 1.4f, Offset(cx, cy))

    // Pizza Face Crust Outer Ring
    drawCircle(Color(0xFFB71C1C), r, Offset(cx, cy))
    // Cheesy face interior
    val cheeseColor = if (isEnraged) Color(0xFFFF7043) else Color(0xFFFFD54F)
    drawCircle(cheeseColor, r * 0.88f, Offset(cx, cy))

    // Menacing Pepperoni Spots
    drawCircle(Color(0xFFB71C1C), 9f, Offset(cx - r * 0.5f, cy - r * 0.2f))
    drawCircle(Color(0xFFB71C1C), 8f, Offset(cx + r * 0.5f, cy - r * 0.3f))
    drawCircle(Color(0xFFB71C1C), 7f, Offset(cx, cy + r * 0.5f))

    if (isStunned) {
        // Dizzy spiral / X eyes
        drawLine(Color.Black, Offset(cx - 20f, cy - 14f), Offset(cx - 8f, cy - 2f), strokeWidth = 4f)
        drawLine(Color.Black, Offset(cx - 8f, cy - 14f), Offset(cx - 20f, cy - 2f), strokeWidth = 4f)
        drawLine(Color.Black, Offset(cx + 8f, cy - 14f), Offset(cx + 20f, cy - 2f), strokeWidth = 4f)
        drawLine(Color.Black, Offset(cx + 20f, cy - 14f), Offset(cx + 8f, cy - 2f), strokeWidth = 4f)

        // Orbiting yellow cartoon stars
        for (i in 0 until 4) {
            val starAngle = pursuer.floatPhase * 3f + (i * PI.toFloat() / 2f)
            val sx = cx + cos(starAngle) * (r + 18f)
            val sy = cy + sin(starAngle) * (r + 14f)
            drawCircle(Color(0xFFFFEB3B), 6f, Offset(sx, sy))
        }
    } else {
        // Glowing Menacing Eyes
        val eyeColor = if (isEnraged) Color(0xFFFF1744) else Color(0xFFFFEB3B)
        val eyeGlowColor = if (isEnraged) Color(0xFFFF5252) else Color(0xFFFFF59D)

        // Eye glow
        drawCircle(eyeGlowColor, 12f, Offset(cx - 14f, cy - 8f))
        drawCircle(eyeGlowColor, 12f, Offset(cx + 14f, cy - 8f))

        // Piercing pupils tracking player
        val pupilAngle = pursuer.angle
        val px = cos(pupilAngle) * 4f
        val py = sin(pupilAngle) * 4f
        drawCircle(Color.Black, 5f, Offset(cx - 14f + px, cy - 8f + py))
        drawCircle(Color.Black, 5f, Offset(cx + 14f + px, cy - 8f + py))

        // Maniacal Grinning Mouth with razor cheese teeth!
        val mouthPath = Path().apply {
            moveTo(cx - 24f, cy + 8f)
            quadraticTo(cx, cy + 28f, cx + 24f, cy + 8f)
            quadraticTo(cx, cy + 14f, cx - 24f, cy + 8f)
            close()
        }
        drawPath(mouthPath, Color(0xFF212121))

        // Razor Sharp Teeth
        for (tx in -16..16 step 8) {
            drawLine(
                color = Color.White,
                start = Offset(cx + tx, cy + 12f),
                end = Offset(cx + tx + 4f, cy + 18f),
                strokeWidth = 3f
            )
        }
    }
}

private fun DrawScope.drawDangerVignette(w: Float, h: Float, distance: Float) {
    if (distance > 500f) return
    val intensity = ((500f - distance) / 500f).coerceIn(0f, 1f)
    val vignetteAlpha = intensity * 0.45f

    // Red warning vignette borders
    val borderThickness = 32f * intensity
    drawRect(
        color = Color(0xFFFF1744).copy(alpha = vignetteAlpha),
        topLeft = Offset(0f, 0f),
        size = Size(w, borderThickness)
    )
    drawRect(
        color = Color(0xFFFF1744).copy(alpha = vignetteAlpha),
        topLeft = Offset(0f, h - borderThickness),
        size = Size(w, borderThickness)
    )
    drawRect(
        color = Color(0xFFFF1744).copy(alpha = vignetteAlpha),
        topLeft = Offset(0f, 0f),
        size = Size(borderThickness, h)
    )
    drawRect(
        color = Color(0xFFFF1744).copy(alpha = vignetteAlpha),
        topLeft = Offset(w - borderThickness, 0f),
        size = Size(borderThickness, h)
    )
}

private fun DrawScope.drawPursuerOffscreenIndicator(
    screenW: Float,
    screenH: Float,
    camX: Float,
    camY: Float,
    pursuerX: Float,
    pursuerY: Float
) {
    // Relative to screen center
    val relX = pursuerX - camX
    val relY = pursuerY - camY

    val halfW = screenW / 2f - 40f
    val halfH = screenH / 2f - 70f

    // Check if pursuer is offscreen
    if (Math.abs(relX) > halfW || Math.abs(relY) > halfH) {
        val angle = atan2(relY, relX)
        val edgeX = (screenW / 2f + cos(angle) * halfW).coerceIn(40f, screenW - 40f)
        val edgeY = (screenH / 2f + sin(angle) * halfH).coerceIn(60f, screenH - 60f)

        // Pulsing warning radar pointer
        drawCircle(Color(0xFFD50000), 18f, Offset(edgeX, edgeY))
        drawCircle(Color(0xFFFFD54F), 10f, Offset(edgeX, edgeY))

        // Exclamation mark
        drawLine(Color.Black, Offset(edgeX, edgeY - 6f), Offset(edgeX, edgeY + 2f), strokeWidth = 3f)
        drawCircle(Color.Black, 1.5f, Offset(edgeX, edgeY + 6f))
    }
}
