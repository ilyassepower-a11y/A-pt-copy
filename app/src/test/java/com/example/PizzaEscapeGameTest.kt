package com.example

import com.example.game.engine.PizzaEscapeEngine
import com.example.game.model.GameDifficulty
import com.example.game.model.GameState
import com.example.game.model.PursuerState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PizzaEscapeGameTest {

    private lateinit var engine: PizzaEscapeEngine

    @Before
    fun setup() {
        engine = PizzaEscapeEngine(GameDifficulty.CLASSIC)
    }

    @Test
    fun testStartGameInitializesCorrectly() {
        engine.startNewGame(GameDifficulty.CLASSIC)
        assertEquals(GameState.RUNNING, engine.gameState)
        assertEquals(60f, engine.stats.totalTime)
        assertEquals(60f, engine.stats.timeRemaining)
        assertEquals(0, engine.stats.score)
        assertTrue(engine.platforms.isNotEmpty())
        assertTrue(engine.collectibles.isNotEmpty())
    }

    @Test
    fun testMachDashRamping() {
        engine.startNewGame(GameDifficulty.CLASSIC)
        engine.inputRight = true
        engine.inputDash = true

        // Simulate 1.0 second of dashing
        for (i in 0 until 60) {
            engine.update(1f / 60f)
        }

        assertTrue(engine.player.machLevel >= 2)
        assertTrue(engine.player.vx > 300f)
    }

    @Test
    fun testTauntParryStunsPursuerWhenClose() {
        engine.startNewGame(GameDifficulty.CLASSIC)
        // Position pursuer close to player
        engine.pursuer.x = engine.player.x + 100f
        engine.pursuer.y = engine.player.y

        engine.triggerTaunt()

        assertTrue(engine.player.isTaunting)
        assertEquals(PursuerState.STUNNED, engine.pursuer.state)
        assertTrue(engine.pursuer.stunTimer > 0f)
        assertTrue(engine.stats.score > 0)
    }

    @Test
    fun testSurvivingUntilTimeRunsOutTriggersVictory() {
        engine.startNewGame(GameDifficulty.SPRINT)
        // Set time remaining to nearly 0
        engine.stats.timeRemaining = 0.05f
        engine.player.invincibilityTimer = 10f // avoid collision during test tick

        engine.update(0.1f)

        assertEquals(0f, engine.stats.timeRemaining)
        assertEquals(GameState.VICTORY, engine.gameState)
    }

    @Test
    fun testSprintAndPanicDifficultyTimers() {
        engine.startNewGame(GameDifficulty.SPRINT)
        assertEquals(45f, engine.stats.totalTime)

        engine.startNewGame(GameDifficulty.PANIC)
        assertEquals(90f, engine.stats.totalTime)
    }
}
