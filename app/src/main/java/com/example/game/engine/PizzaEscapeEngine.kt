package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.Collectible
import com.example.game.model.CollectibleType
import com.example.game.model.GameDifficulty
import com.example.game.model.GameState
import com.example.game.model.GameStats
import com.example.game.model.GhostTrail
import com.example.game.model.Particle
import com.example.game.model.ParticleShape
import com.example.game.model.Platform
import com.example.game.model.PlatformType
import com.example.game.model.Player
import com.example.game.model.Pursuer
import com.example.game.model.PursuerState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class PizzaEscapeEngine(
    var difficulty: GameDifficulty = GameDifficulty.CLASSIC
) {
    val worldWidth = 2800f
    val worldHeight = 2000f

    val player = Player(x = 350f, y = 1700f)
    val pursuer = Pursuer(x = 100f, y = 800f)
    val stats = GameStats(
        timeRemaining = difficulty.durationSeconds,
        totalTime = difficulty.durationSeconds
    )

    var gameState: GameState = GameState.MENU
    val platforms = mutableListOf<Platform>()
    val collectibles = mutableListOf<Collectible>()
    val particles = mutableListOf<Particle>()
    val ghostTrails = mutableListOf<GhostTrail>()

    // Inputs
    var inputLeft = false
    var inputRight = false
    var inputDash = false
    var inputDown = false

    // Screen Shake & Camera
    var screenShake: Float = 0f
    var cameraX: Float = 0f
    var cameraY: Float = 0f

    // Events to signal to Audio/Haptics
    var onJumpEvent: (() -> Unit)? = null
    var onMachBoostEvent: (() -> Unit)? = null
    var onWallSmashEvent: (() -> Unit)? = null
    var onCollectEvent: (() -> Unit)? = null
    var onTauntParryEvent: (() -> Unit)? = null
    var onBounceEvent: (() -> Unit)? = null
    var onPursuerAlertEvent: (() -> Unit)? = null
    var onCaughtEvent: (() -> Unit)? = null
    var onVictoryEvent: (() -> Unit)? = null

    private var alertSoundCooldown = 0f
    private val random = Random(System.currentTimeMillis())

    init {
        buildWorld()
    }

    fun startNewGame(newDifficulty: GameDifficulty = difficulty) {
        difficulty = newDifficulty
        platforms.clear()
        collectibles.clear()
        particles.clear()
        ghostTrails.clear()
        buildWorld()

        player.x = 350f
        player.y = 1700f
        player.vx = 0f
        player.vy = 0f
        player.machLevel = 0
        player.isDashing = false
        player.isSliding = false
        player.isTaunting = false
        player.tauntTimer = 0f
        player.hotPepperTimer = 0f
        player.invincibilityTimer = 1.5f // brief initial grace
        player.runCycle = 0f

        pursuer.x = 2200f
        pursuer.y = 400f
        pursuer.vx = 0f
        pursuer.vy = 0f
        pursuer.state = PursuerState.CHASING
        pursuer.stunTimer = 0f
        pursuer.speed = difficulty.pursuerBaseSpeed

        stats.totalTime = difficulty.durationSeconds
        stats.timeRemaining = difficulty.durationSeconds
        stats.score = 0
        stats.combo = 0
        stats.comboTimer = 0f
        stats.maxCombo = 0
        stats.toppingsCollected = 0
        stats.blocksDestroyed = 0
        stats.tauntsPerformed = 0
        stats.pursuerDistance = 1000f
        stats.rank = "D"

        screenShake = 0f
        alertSoundCooldown = 0f
        gameState = GameState.RUNNING
    }

    private fun buildWorld() {
        var id = 0

        // Outer boundaries
        platforms.add(Platform(id++, 0f, 1920f, worldWidth, 80f, PlatformType.CRUST_SOLID)) // Floor
        platforms.add(Platform(id++, 0f, 0f, worldWidth, 40f, PlatformType.CRUST_SOLID)) // Ceiling
        platforms.add(Platform(id++, 0f, 0f, 40f, worldHeight, PlatformType.CRUST_SOLID)) // Left Wall
        platforms.add(Platform(id++, worldWidth - 40f, 0f, 40f, worldHeight, PlatformType.CRUST_SOLID)) // Right Wall

        // Floor 1 platforms & obstacles (Y ~ 1550 - 1850)
        platforms.add(Platform(id++, 160f, 1720f, 280f, 32f, PlatformType.CRUST_SOLID))
        platforms.add(Platform(id++, 560f, 1620f, 340f, 32f, PlatformType.CRUST_SOLID))
        platforms.add(Platform(id++, 980f, 1680f, 160f, 32f, PlatformType.BOUNCY_MUSHROOM)) // Bouncy pad
        platforms.add(Platform(id++, 1220f, 1620f, 420f, 32f, PlatformType.SPEED_RAIL)) // Speed rail
        platforms.add(Platform(id++, 1740f, 1620f, 80f, 100f, PlatformType.CRUST_BREAKABLE)) // Breakable wall
        platforms.add(Platform(id++, 1920f, 1680f, 300f, 32f, PlatformType.CRUST_SOLID))
        platforms.add(Platform(id++, 2320f, 1620f, 180f, 32f, PlatformType.BOUNCY_MUSHROOM)) // Super bounce

        // Floor 2 platforms & vertical shafts (Y ~ 1150 - 1450)
        platforms.add(Platform(id++, 240f, 1380f, 420f, 32f, PlatformType.MELTING_CHEESE))
        platforms.add(Platform(id++, 760f, 1320f, 360f, 32f, PlatformType.CRUST_SOLID))
        platforms.add(Platform(id++, 1220f, 1260f, 80f, 180f, PlatformType.CRUST_BREAKABLE)) // Vertical barrier
        platforms.add(Platform(id++, 1400f, 1320f, 400f, 32f, PlatformType.SPEED_RAIL))
        platforms.add(Platform(id++, 1900f, 1280f, 200f, 32f, PlatformType.BOUNCY_MUSHROOM))
        platforms.add(Platform(id++, 2180f, 1340f, 340f, 32f, PlatformType.CRUST_SOLID))

        // Floor 3 platforms (Y ~ 750 - 1050)
        platforms.add(Platform(id++, 140f, 980f, 360f, 32f, PlatformType.CRUST_SOLID))
        platforms.add(Platform(id++, 600f, 920f, 160f, 32f, PlatformType.BOUNCY_MUSHROOM))
        platforms.add(Platform(id++, 860f, 880f, 500f, 32f, PlatformType.SPEED_RAIL))
        platforms.add(Platform(id++, 1460f, 920f, 120f, 120f, PlatformType.CRUST_BREAKABLE))
        platforms.add(Platform(id++, 1680f, 880f, 440f, 32f, PlatformType.MELTING_CHEESE))
        platforms.add(Platform(id++, 2220f, 940f, 340f, 32f, PlatformType.CRUST_SOLID))

        // Floor 4 Spire Balcony & Rooftop chutes (Y ~ 350 - 650)
        platforms.add(Platform(id++, 200f, 580f, 460f, 32f, PlatformType.CRUST_SOLID))
        platforms.add(Platform(id++, 760f, 520f, 180f, 32f, PlatformType.BOUNCY_MUSHROOM))
        platforms.add(Platform(id++, 1040f, 460f, 620f, 32f, PlatformType.SPEED_RAIL))
        platforms.add(Platform(id++, 1760f, 460f, 80f, 140f, PlatformType.CRUST_BREAKABLE))
        platforms.add(Platform(id++, 1940f, 520f, 480f, 32f, PlatformType.CRUST_SOLID))
        platforms.add(Platform(id++, 2500f, 460f, 160f, 32f, PlatformType.BOUNCY_MUSHROOM))

        // Central vertical shaft platforms & towers
        platforms.add(Platform(id++, 1340f, 680f, 220f, 32f, PlatformType.CRUST_SOLID))
        platforms.add(Platform(id++, 1340f, 1080f, 220f, 32f, PlatformType.BOUNCY_MUSHROOM))
        platforms.add(Platform(id++, 1340f, 1480f, 220f, 32f, PlatformType.CRUST_SOLID))

        // Spawn Collectibles
        var cId = 0
        val toppingTypes = listOf(
            CollectibleType.PEPPERONI,
            CollectibleType.MUSHROOM,
            CollectibleType.OLIVE,
            CollectibleType.CHEESE_SLICE
        )

        // Add toppings across platforms
        for (p in platforms) {
            if (p.type != PlatformType.CRUST_BREAKABLE && p.width >= 160f && p.height <= 40f) {
                val count = (p.width / 100f).toInt().coerceIn(1, 4)
                val step = p.width / (count + 1)
                for (i in 1..count) {
                    val type = toppingTypes[random.nextInt(toppingTypes.size)]
                    collectibles.add(
                        Collectible(
                            id = cId++,
                            x = p.x + step * i,
                            y = p.y - 28f,
                            type = type
                        )
                    )
                }
            }
        }

        // Special Hot Peppers for max Mach speed & pursuer stun
        collectibles.add(Collectible(cId++, 1150f, 420f, 20f, CollectibleType.HOT_PEPPER))
        collectibles.add(Collectible(cId++, 2360f, 1300f, 20f, CollectibleType.HOT_PEPPER))
        collectibles.add(Collectible(cId++, 300f, 1340f, 20f, CollectibleType.HOT_PEPPER))

        // Pizza Clocks
        collectibles.add(Collectible(cId++, 1450f, 640f, 22f, CollectibleType.PIZZA_CLOCK))
        collectibles.add(Collectible(cId++, 2040f, 1640f, 22f, CollectibleType.PIZZA_CLOCK))
    }

    fun jump() {
        if (gameState != GameState.RUNNING) return

        if (player.isGrounded) {
            // Normal / Mach jump
            val jumpPower = when (player.machLevel) {
                4 -> -860f
                3 -> -820f
                2 -> -780f
                else -> -730f
            }
            player.vy = jumpPower
            player.isGrounded = false
            spawnJumpParticles()
            onJumpEvent?.invoke()
        } else if (player.isTouchingWall) {
            // Wall Kick / Wall Jump
            player.vy = -800f
            player.vx = -player.wallDirection * 620f
            player.facingRight = player.wallDirection < 0
            player.machLevel = maxOf(player.machLevel, 2)
            spawnWallHitParticles(player.x, player.y - 30f)
            onJumpEvent?.invoke()
        }
    }

    fun triggerTaunt() {
        if (gameState != GameState.RUNNING) return
        if (player.isTaunting) return

        player.isTaunting = true
        player.tauntTimer = 0.35f
        stats.tauntsPerformed++

        // Check if pursuer is close for parry
        val dx = pursuer.x - player.x
        val dy = pursuer.y - player.y
        val dist = sqrt(dx * dx + dy * dy)

        if (dist < 260f) {
            // PARRY SUCCESS!
            pursuer.state = PursuerState.STUNNED
            pursuer.stunTimer = 3.0f
            // Knock pursuer back
            val angle = atan2(dy, dx)
            pursuer.vx = cos(angle) * 450f
            pursuer.vy = sin(angle) * 450f
            pursuer.x += cos(angle) * 120f
            pursuer.y += sin(angle) * 120f

            stats.score += 500
            stats.combo++
            stats.comboTimer = 5f
            screenShake = 14f

            // Parry sparks
            for (i in 0 until 18) {
                val a = random.nextFloat() * 2 * Math.PI.toFloat()
                val spd = random.nextFloat() * 400f + 150f
                particles.add(
                    Particle(
                        x = player.x,
                        y = player.y - 30f,
                        vx = cos(a) * spd,
                        vy = sin(a) * spd,
                        color = Color(0xFFFFEB3B),
                        size = random.nextFloat() * 9f + 5f,
                        life = 0.5f,
                        maxLife = 0.5f,
                        shape = ParticleShape.STAR
                    )
                )
            }
            onTauntParryEvent?.invoke()
        } else {
            // Simple comical taunt pose
            stats.score += 25
            for (i in 0 until 6) {
                particles.add(
                    Particle(
                        x = player.x + (random.nextFloat() - 0.5f) * 40f,
                        y = player.y - 40f,
                        vx = (random.nextFloat() - 0.5f) * 80f,
                        vy = -random.nextFloat() * 120f,
                        color = Color(0xFFFFD54F),
                        size = 6f,
                        life = 0.4f,
                        maxLife = 0.4f,
                        shape = ParticleShape.SPARK
                    )
                )
            }
            onTauntParryEvent?.invoke()
        }
    }

    fun update(dt: Float) {
        if (gameState != GameState.RUNNING) return

        val clampedDt = dt.coerceIn(0.001f, 0.05f)

        // 1. Countdown Timer (عليك الهروب حتى ينفذ الوقت)
        stats.timeRemaining -= clampedDt
        if (stats.timeRemaining <= 0f) {
            stats.timeRemaining = 0f
            triggerVictory()
            return
        }

        // Proximity alert sound & haptics update
        alertSoundCooldown -= clampedDt
        val dxP = pursuer.x - player.x
        val dyP = pursuer.y - player.y
        stats.pursuerDistance = sqrt(dxP * dxP + dyP * dyP)

        if (stats.pursuerDistance < 320f && alertSoundCooldown <= 0f && pursuer.state != PursuerState.STUNNED) {
            onPursuerAlertEvent?.invoke()
            alertSoundCooldown = if (stats.pursuerDistance < 180f) 0.6f else 1.2f
        }

        // Combo timer
        if (stats.comboTimer > 0f) {
            stats.comboTimer -= clampedDt
            if (stats.comboTimer <= 0f) {
                stats.combo = 0
            }
        }
        stats.maxCombo = maxOf(stats.maxCombo, stats.combo)

        // Screen Shake decay
        if (screenShake > 0f) {
            screenShake = (screenShake - clampedDt * 24f).coerceAtLeast(0f)
        }

        // Hot Pepper & Invincibility timer
        if (player.hotPepperTimer > 0f) {
            player.hotPepperTimer -= clampedDt
            player.machLevel = 4
            // Spawn fire particles
            particles.add(
                Particle(
                    x = player.x + (random.nextFloat() - 0.5f) * 30f,
                    y = player.y - 20f + (random.nextFloat() - 0.5f) * 20f,
                    vx = -player.vx * 0.2f + (random.nextFloat() - 0.5f) * 60f,
                    vy = -random.nextFloat() * 120f,
                    color = if (random.nextBoolean()) Color(0xFFFF5722) else Color(0xFFFFD600),
                    size = random.nextFloat() * 8f + 6f,
                    life = 0.35f,
                    maxLife = 0.35f,
                    shape = ParticleShape.FLAME
                )
            )
        }
        if (player.invincibilityTimer > 0f) {
            player.invincibilityTimer -= clampedDt
        }

        // Taunt timer
        if (player.isTaunting) {
            player.tauntTimer -= clampedDt
            if (player.tauntTimer <= 0f) {
                player.isTaunting = false
            }
        }

        // 2. Player Physics & Mach System
        updatePlayerPhysics(clampedDt)

        // 3. Pursuer AI & Movement
        updatePursuer(clampedDt)

        // 4. Collectibles & Bobbing
        updateCollectibles(clampedDt)

        // 5. Particles & Ghost Trails
        updateParticles(clampedDt)

        // 6. Camera Follow with Smooth Lerp
        updateCamera(clampedDt)

        // 7. Calculate Current Rank
        updateRank()
    }

    private fun updatePlayerPhysics(dt: Float) {
        val gravity = 1800f
        val maxFallSpeed = 1200f
        val accel = if (player.isGrounded) 1400f else 900f
        val friction = if (player.isGrounded) 1100f else 400f

        // Mach Dash logic
        player.isDashing = inputDash || player.hotPepperTimer > 0f
        player.isSliding = inputDown && player.isGrounded

        // Target speeds based on Mach level
        val maxSpeed = when (player.machLevel) {
            4 -> 1250f
            3 -> 920f
            2 -> 640f
            1 -> 420f
            else -> 280f
        }

        // Horizontal input
        var moveDir = 0
        if (inputLeft) moveDir -= 1
        if (inputRight) moveDir += 1

        if (moveDir != 0 && !player.isTaunting) {
            player.facingRight = moveDir > 0
            player.vx += moveDir * accel * dt

            // Build Mach Speed if dashing
            if (player.isDashing) {
                player.machRunTimer += dt
                val oldMach = player.machLevel
                if (player.hotPepperTimer > 0f) {
                    player.machLevel = 4
                } else if (player.machRunTimer > 2.6f) {
                    player.machLevel = 4
                } else if (player.machRunTimer > 1.6f) {
                    player.machLevel = 3
                } else if (player.machRunTimer > 0.8f) {
                    player.machLevel = 2
                } else {
                    player.machLevel = 1
                }

                if (player.machLevel > oldMach && player.machLevel >= 2) {
                    onMachBoostEvent?.invoke()
                    screenShake = 6f
                }
            } else {
                player.machRunTimer = 0f
                player.machLevel = 0
            }
        } else {
            // Apply friction
            if (player.vx > 0) {
                player.vx = (player.vx - friction * dt).coerceAtLeast(0f)
            } else if (player.vx < 0) {
                player.vx = (player.vx + friction * dt).coerceAtMost(0f)
            }
            if (!player.isDashing) {
                player.machRunTimer = 0f
                player.machLevel = 0
            }
        }

        // Clamp horizontal velocity
        player.vx = player.vx.coerceIn(-maxSpeed, maxSpeed)

        // Downward slam
        if (inputDown && !player.isGrounded) {
            player.vy = 1200f
        } else {
            player.vy = (player.vy + gravity * dt).coerceAtMost(maxFallSpeed)
        }

        // Ghost trails at Mach 3 / 4
        if (player.machLevel >= 3 && Math.abs(player.vx) > 300f) {
            if (random.nextFloat() < 0.35f) {
                ghostTrails.add(
                    GhostTrail(
                        x = player.x,
                        y = player.y,
                        facingRight = player.facingRight,
                        machLevel = player.machLevel
                    )
                )
            }
        }

        // Run cycle animation
        if (player.isGrounded && Math.abs(player.vx) > 20f) {
            player.runCycle += dt * (Math.abs(player.vx) / 30f)
        }

        // Move Horizontal & Collide
        player.x += player.vx * dt
        player.isTouchingWall = false
        player.wallDirection = 0

        for (p in platforms) {
            if (p.isDestroyed) continue
            if (checkCollision(player.left, player.top, player.width, player.height, p)) {
                // Breakable platform check
                if (p.type == PlatformType.CRUST_BREAKABLE && player.machLevel >= 2) {
                    destroyPlatform(p)
                    continue
                }

                if (player.vx > 0) { // Moving right
                    player.x = p.left - player.width / 2
                    player.isTouchingWall = true
                    player.wallDirection = 1
                } else if (player.vx < 0) { // Moving left
                    player.x = p.right + player.width / 2
                    player.isTouchingWall = true
                    player.wallDirection = -1
                }
                player.vx = 0f
            }
        }

        // Move Vertical & Collide
        player.y += player.vy * dt
        player.isGrounded = false

        for (p in platforms) {
            if (p.isDestroyed) continue
            if (checkCollision(player.left, player.top, player.width, player.height, p)) {
                if (player.vy > 0) { // Falling down onto platform
                    // Slamming through breakable platform
                    if (p.type == PlatformType.CRUST_BREAKABLE && inputDown) {
                        destroyPlatform(p)
                        continue
                    }

                    player.y = p.top
                    player.vy = 0f
                    player.isGrounded = true

                    // Bouncy mushroom platform
                    if (p.type == PlatformType.BOUNCY_MUSHROOM) {
                        player.vy = -1050f
                        player.isGrounded = false
                        spawnBounceParticles(p.x + p.width / 2, p.top)
                        onBounceEvent?.invoke()
                        screenShake = 5f
                    }
                } else if (player.vy < 0) { // Hitting ceiling
                    player.y = p.bottom + player.height
                    player.vy = 0f
                }
            }
        }

        // World Bounds clamp
        if (player.x < 40f + player.width / 2) {
            player.x = 40f + player.width / 2
            player.vx = 0f
        } else if (player.x > worldWidth - 40f - player.width / 2) {
            player.x = worldWidth - 40f - player.width / 2
            player.vx = 0f
        }

        if (player.y > worldHeight - 60f) {
            player.y = worldHeight - 60f
            player.vy = 0f
            player.isGrounded = true
        }
    }

    private fun destroyPlatform(p: Platform) {
        p.isDestroyed = true
        stats.blocksDestroyed++
        stats.score += 150
        stats.combo++
        stats.comboTimer = 4f
        screenShake = 9f
        spawnWallHitParticles(p.x + p.width / 2, p.y + p.height / 2)
        onWallSmashEvent?.invoke()
    }

    private fun updatePursuer(dt: Float) {
        pursuer.floatPhase += dt * 4f

        if (pursuer.state == PursuerState.STUNNED) {
            pursuer.stunTimer -= dt
            // Slow drag
            pursuer.vx *= 0.92f
            pursuer.vy *= 0.92f
            pursuer.x += pursuer.vx * dt
            pursuer.y += pursuer.vy * dt
            if (pursuer.stunTimer <= 0f) {
                pursuer.state = PursuerState.CHASING
            }
            return
        }

        // Pursuer dynamic speed
        val timeRatio = (stats.totalTime - stats.timeRemaining) / stats.totalTime
        val speedBoost = timeRatio * 90f // speeds up as time ticks down!
        val currentSpeed = difficulty.pursuerBaseSpeed + speedBoost

        // Enraged state when timer is low (< 20 seconds) or when player is far away
        if (stats.timeRemaining < 20f || stats.pursuerDistance > 850f) {
            pursuer.state = PursuerState.ENRAGED
            pursuer.speed = currentSpeed * 1.25f
            pursuer.eyeGlow = (sin(pursuer.floatPhase * 2f) + 1f) * 0.5f
        } else {
            pursuer.state = PursuerState.CHASING
            pursuer.speed = currentSpeed
            pursuer.eyeGlow = 0.2f
        }

        // Float toward player
        val dx = player.x - pursuer.x
        val dy = (player.y - 30f) - pursuer.y
        val angle = atan2(dy, dx)
        pursuer.angle = angle

        // Add wavy floating movement
        val floatOffsetY = sin(pursuer.floatPhase) * 60f

        val targetVx = cos(angle) * pursuer.speed
        val targetVy = sin(angle) * pursuer.speed + floatOffsetY

        // Smooth steering
        pursuer.vx += (targetVx - pursuer.vx) * 3.5f * dt
        pursuer.vy += (targetVy - pursuer.vy) * 3.5f * dt

        pursuer.x += pursuer.vx * dt
        pursuer.y += pursuer.vy * dt

        // Check Caught Collision with Player!
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < (pursuer.radius + 24f) && player.invincibilityTimer <= 0f && player.hotPepperTimer <= 0f) {
            triggerGameOver()
        }
    }

    private fun updateCollectibles(dt: Float) {
        val iterator = collectibles.iterator()
        while (iterator.hasNext()) {
            val c = iterator.next()
            if (c.isCollected) continue

            c.bobOffset = sin(pursuer.floatPhase * 1.5f + c.id) * 6f

            // Collision with player
            val dx = player.x - c.x
            val dy = (player.y - 28f) - (c.y + c.bobOffset)
            val dist = sqrt(dx * dx + dy * dy)

            if (dist < (c.radius + 24f)) {
                c.isCollected = true
                stats.toppingsCollected++
                stats.score += c.type.points
                stats.combo++
                stats.comboTimer = 4.5f

                if (c.type == CollectibleType.HOT_PEPPER) {
                    player.hotPepperTimer = 5.0f
                    screenShake = 12f
                    // Stun pursuer immediately
                    pursuer.state = PursuerState.STUNNED
                    pursuer.stunTimer = 2.5f
                } else if (c.type == CollectibleType.PIZZA_CLOCK) {
                    stats.score += 300
                    screenShake = 6f
                }

                spawnCollectParticles(c.x, c.y, c.type.color)
                onCollectEvent?.invoke()
            }
        }
    }

    private fun updateParticles(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.life -= dt
            if (p.life <= 0f) {
                pIter.remove()
                continue
            }
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.vy += 400f * dt // gravity on crumbs
        }

        val gIter = ghostTrails.iterator()
        while (gIter.hasNext()) {
            val g = gIter.next()
            g.alpha -= dt * 2.8f
            if (g.alpha <= 0f) {
                gIter.remove()
            }
        }
    }

    private fun updateCamera(dt: Float) {
        val targetCamX = player.x
        val targetCamY = player.y - 80f
        cameraX += (targetCamX - cameraX) * 7f * dt
        cameraY += (targetCamY - cameraY) * 7f * dt
    }

    private fun updateRank() {
        val baseScore = stats.score + (stats.toppingsCollected * 60) + (stats.blocksDestroyed * 100) + (stats.maxCombo * 200)
        stats.rank = when {
            baseScore >= 4500 && stats.maxCombo >= 8 -> "P" // Perfect Pizza Tower P Rank!
            baseScore >= 3200 -> "S"
            baseScore >= 2200 -> "A"
            baseScore >= 1400 -> "B"
            baseScore >= 700 -> "C"
            else -> "D"
        }
    }

    private fun triggerGameOver() {
        gameState = GameState.GAME_OVER
        screenShake = 22f
        onCaughtEvent?.invoke()
    }

    private fun triggerVictory() {
        gameState = GameState.VICTORY
        screenShake = 10f
        updateRank()
        // Bonus points for surviving!
        stats.score += 2000
        // Victory confetti
        for (i in 0 until 50) {
            val a = random.nextFloat() * 2 * Math.PI.toFloat()
            val spd = random.nextFloat() * 400f + 100f
            val colors = listOf(Color(0xFFFFD54F), Color(0xFFFF5722), Color(0xFF4CAF50), Color(0xFF00E5FF), Color(0xFFE91E63))
            particles.add(
                Particle(
                    x = player.x,
                    y = player.y - 40f,
                    vx = cos(a) * spd,
                    vy = sin(a) * spd - 200f,
                    color = colors[random.nextInt(colors.size)],
                    size = random.nextFloat() * 8f + 5f,
                    life = 1.8f,
                    maxLife = 1.8f,
                    shape = ParticleShape.STAR
                )
            )
        }
        onVictoryEvent?.invoke()
    }

    private fun checkCollision(x: Float, y: Float, w: Float, h: Float, p: Platform): Boolean {
        return x < p.right && x + w > p.left && y < p.bottom && y + h > p.top
    }

    private fun spawnJumpParticles() {
        for (i in 0 until 6) {
            particles.add(
                Particle(
                    x = player.x + (random.nextFloat() - 0.5f) * 30f,
                    y = player.bottom,
                    vx = (random.nextFloat() - 0.5f) * 120f,
                    vy = -random.nextFloat() * 60f,
                    color = Color(0xFFD7CCC8),
                    size = random.nextFloat() * 6f + 3f,
                    life = 0.3f,
                    maxLife = 0.3f
                )
            )
        }
    }

    private fun spawnWallHitParticles(x: Float, y: Float) {
        for (i in 0 until 14) {
            val a = random.nextFloat() * 2 * Math.PI.toFloat()
            val spd = random.nextFloat() * 260f + 80f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(a) * spd,
                    vy = sin(a) * spd,
                    color = if (random.nextBoolean()) Color(0xFFFFB74D) else Color(0xFF8D6E63),
                    size = random.nextFloat() * 7f + 4f,
                    life = 0.45f,
                    maxLife = 0.45f,
                    shape = ParticleShape.SPARK
                )
            )
        }
    }

    private fun spawnBounceParticles(x: Float, y: Float) {
        for (i in 0 until 10) {
            particles.add(
                Particle(
                    x = x + (random.nextFloat() - 0.5f) * 40f,
                    y = y,
                    vx = (random.nextFloat() - 0.5f) * 180f,
                    vy = -random.nextFloat() * 250f - 100f,
                    color = Color(0xFFFF4081),
                    size = random.nextFloat() * 6f + 4f,
                    life = 0.4f,
                    maxLife = 0.4f,
                    shape = ParticleShape.STAR
                )
            )
        }
    }

    private fun spawnCollectParticles(x: Float, y: Float, color: Color) {
        for (i in 0 until 8) {
            val a = random.nextFloat() * 2 * Math.PI.toFloat()
            val spd = random.nextFloat() * 160f + 60f
            particles.add(
                Particle(
                    x = x,
                    y = y,
                    vx = cos(a) * spd,
                    vy = sin(a) * spd,
                    color = color,
                    size = random.nextFloat() * 5f + 3f,
                    life = 0.35f,
                    maxLife = 0.35f,
                    shape = ParticleShape.CIRCLE
                )
            )
        }
    }
}
