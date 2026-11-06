package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeMute
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import com.example.game.engine.PizzaEscapeEngine
import com.example.game.haptics.HapticsManager
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun GameOverlay(
    engine: PizzaEscapeEngine,
    haptics: HapticsManager,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onPauseClick: () -> Unit,
    hudTick: Long = 0L,
    modifier: Modifier = Modifier
) {
    val stats = engine.stats
    val timeLeft = stats.timeRemaining
    val isUrgent = timeLeft < 15f

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val urgentScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUrgent) 1.14f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "urgent_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 24.dp)
    ) {
        // --- TOP BAR: TIMER, SCORE, COMBO, PURSUER ALERT ---
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Score & Combo
                Column {
                    Surface(
                        color = Color(0xCC000000),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(10.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "النقاط: ${stats.score}",
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    if (stats.combo > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            color = Color(0xFFD50000),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "COMBO x${stats.combo}!",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Center: Big Pizza Time Escape Countdown Timer!
                val minutes = (timeLeft / 60).toInt()
                val seconds = (timeLeft % 60).toInt()
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                Surface(
                    color = if (isUrgent) Color(0xDDE53935) else Color(0xDD1E1428),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .scale(urgentScale)
                        .border(
                            2.dp,
                            if (isUrgent) Color(0xFFFFD600) else Color(0xFFFF5722),
                            RoundedCornerShape(14.dp)
                        )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Countdown",
                            tint = if (isUrgent) Color(0xFFFFD600) else Color(0xFFFF7043),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeFormatted,
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Right: Controls & Mute
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            haptics.lightTap()
                            onToggleMute()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xAA000000), CircleShape)
                            .testTag("mute_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeMute else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Mute Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = {
                            haptics.lightTap()
                            onPauseClick()
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color(0xAA000000), CircleShape)
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pursuer Distance Threat Meter
            val dist = stats.pursuerDistance
            val (threatColor, threatText) = when {
                dist < 220f -> Color(0xFFFF1744) to "المطارد خلفك مباشرة! (خطر شـديد)"
                dist < 420f -> Color(0xFFFF9100) to "المطارد يقترب! (${(dist / 10).roundToInt()}m)"
                else -> Color(0xFF00E676) to "المطارد بعيد (${(dist / 10).roundToInt()}m)"
            }

            Surface(
                color = Color(0xCC000000),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .border(1.dp, threatColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Threat",
                        tint = threatColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = threatText,
                        color = threatColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // --- MACH METER (BOTTOM-LEFT / CENTER) ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 125.dp)
        ) {
            val mach = engine.player.machLevel
            val machTitle = when (mach) {
                4 -> "MACH 4 MAX!"
                3 -> "MACH 3!!"
                2 -> "MACH 2!"
                1 -> "MACH 1"
                else -> "RUN"
            }
            val machColor = when (mach) {
                4 -> Color(0xFFFF1744)
                3 -> Color(0xFFFF9100)
                2 -> Color(0xFFFFD600)
                1 -> Color(0xFF00E5FF)
                else -> Color.Gray
            }

            Surface(
                color = Color(0xDD000000),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.border(1.5.dp, machColor, RoundedCornerShape(10.dp))
            ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Mach Fire",
                            tint = machColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = machTitle,
                            color = machColor,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (mach / 4f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .width(110.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = machColor,
                        trackColor = Color(0x44FFFFFF)
                    )
                }
            }
        }

        // --- ARCADE TOUCH CONTROLS ---
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Joystick: left / right to run, pull down to slide / slam
            ArcadeJoystick(
                onDirection = { left, right, down ->
                    engine.inputLeft = left
                    engine.inputRight = right
                    engine.inputDown = down
                }
            )

            // Action Buttons (Mach Dash, Taunt Parry, Slide Slam, Jump)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Row: Mach Dash + Taunt
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Taunt / Parry Button
                    ArcadeTapButton(
                        icon = Icons.Default.Star,
                        label = "PARRY",
                        testTag = "btn_taunt",
                        color = Color(0xFFAB47BC),
                        size = 60.dp,
                        onTap = {
                            haptics.lightTap()
                            engine.triggerTaunt()
                        }
                    )

                    // Mach Dash Button (Hold or Tap)
                    ArcadeHoldButton(
                        icon = Icons.Default.Bolt,
                        label = "MACH",
                        testTag = "btn_dash",
                        color = Color(0xFFFF5722),
                        size = 64.dp,
                        onPressChange = { isPressed ->
                            engine.inputDash = isPressed
                            if (isPressed) haptics.lightTap()
                        }
                    )
                }

                // Bottom Row: JUMP
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Big JUMP Button
                    ArcadeTapButton(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "JUMP",
                        testTag = "btn_jump",
                        color = Color(0xFF2E7D32),
                        size = 84.dp,
                        onTap = {
                            haptics.lightTap()
                            engine.jump()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ArcadeHoldButton(
    icon: ImageVector,
    testTag: String,
    color: Color,
    label: String? = null,
    size: androidx.compose.ui.unit.Dp = 60.dp,
    onPressChange: (Boolean) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .scale(if (isPressed) 0.90f else 1f)
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        if (isPressed) color.copy(alpha = 0.9f) else color,
                        Color(0xFF1A1A1A)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 2.5.dp,
                color = if (isPressed) Color.White else color.copy(alpha = 0.8f),
                shape = CircleShape
            )
            .testTag(testTag)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPressChange(true)
                        tryAwaitRelease()
                        isPressed = false
                        onPressChange(false)
                    }
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label ?: testTag,
                tint = Color.White,
                modifier = Modifier.size(size * 0.45f)
            )
            if (label != null) {
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 9.sp
                )
            }
        }
    }
}

@Composable
fun ArcadeTapButton(
    icon: ImageVector,
    label: String,
    testTag: String,
    color: Color,
    size: androidx.compose.ui.unit.Dp = 60.dp,
    onTap: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .scale(if (isPressed) 0.88f else 1f)
            .background(
                brush = Brush.radialGradient(
                    listOf(
                        if (isPressed) color.copy(alpha = 0.9f) else color,
                        Color(0xFF1A1A1A)
                    )
                ),
                shape = CircleShape
            )
            .border(
                width = 2.5.dp,
                color = if (isPressed) Color.White else color.copy(alpha = 0.8f),
                shape = CircleShape
            )
            .testTag(testTag)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onTap() // react the moment the finger touches the button
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(size * 0.44f)
            )
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 9.sp
            )
        }
    }
}

@Composable
fun ArcadeJoystick(
    onDirection: (left: Boolean, right: Boolean, down: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    baseSize: androidx.compose.ui.unit.Dp = 140.dp
) {
    var knob by remember { mutableStateOf(Offset.Zero) }
    var isActive by remember { mutableStateOf(false) }

    // Make sure the character stops if the joystick disappears (game over, pause, menu)
    DisposableEffect(Unit) {
        onDispose { onDirection(false, false, false) }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(baseSize)
            .background(Color(0x66000000), CircleShape)
            .border(2.5.dp, if (isActive) Color.White else Color(0x88FFFFFF), CircleShape)
            .testTag("joystick")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val first = awaitFirstDown(requireUnconsumed = false)
                    first.consume()
                    isActive = true

                    val center = Offset(size.width / 2f, size.height / 2f)
                    // How far the knob can travel (stays inside the round base)
                    val maxTravel = size.width / 2f * 0.6f

                    fun handle(pos: Offset) {
                        var dx = pos.x - center.x
                        var dy = pos.y - center.y
                        val dist = sqrt(dx * dx + dy * dy)
                        if (dist > maxTravel) {
                            dx = dx / dist * maxTravel
                            dy = dy / dist * maxTravel
                        }
                        knob = Offset(dx, dy)
                        onDirection(
                            dx < -maxTravel * 0.3f,
                            dx > maxTravel * 0.3f,
                            dy > maxTravel * 0.6f
                        )
                    }

                    handle(first.position)

                    var pressed = true
                    while (pressed) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == first.id }
                        if (change == null || !change.pressed) {
                            pressed = false
                        } else {
                            handle(change.position)
                            change.consume()
                        }
                    }

                    knob = Offset.Zero
                    isActive = false
                    onDirection(false, false, false)
                }
            }
    ) {
        Box(
            modifier = Modifier
                .offset { IntOffset(knob.x.roundToInt(), knob.y.roundToInt()) }
                .size(baseSize * 0.4f)
                .background(
                    brush = Brush.radialGradient(listOf(Color(0xFFFFD54F), Color(0xFFFF6F00))),
                    shape = CircleShape
                )
                .border(2.dp, Color.White, CircleShape)
        )
    }
}
