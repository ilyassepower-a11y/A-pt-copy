package com.example.game.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.SoundManager
import com.example.game.data.GameRepository
import com.example.game.data.GameSaveData
import com.example.game.engine.PizzaEscapeEngine
import com.example.game.haptics.HapticsManager
import com.example.game.model.GameDifficulty
import com.example.game.model.GameState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val soundManager = SoundManager()
    val hapticsManager = HapticsManager(application.applicationContext)
    val repository = GameRepository(application.applicationContext)

    val engine = PizzaEscapeEngine()

    private val _saveData = MutableStateFlow(repository.getSaveData())
    val saveData: StateFlow<GameSaveData> = _saveData.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Frame trigger state flow for recomposition
    private val _gameTick = MutableStateFlow(0L)
    val gameTick: StateFlow<Long> = _gameTick.asStateFlow()

    // Tells the UI when the screen (menu / running / paused / game over) changes
    private val _gameState = MutableStateFlow(GameState.MENU)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    init {
        setupEngineCallbacks()
        startGameLoop()
    }

    private fun setupEngineCallbacks() {
        engine.onJumpEvent = {
            soundManager.playJump()
            hapticsManager.lightTap()
        }

        engine.onMachBoostEvent = {
            soundManager.playMachBoost()
            hapticsManager.impactHeavy()
        }

        engine.onWallSmashEvent = {
            soundManager.playWallSmash()
            hapticsManager.impactHeavy()
        }

        engine.onCollectEvent = {
            soundManager.playCollect()
        }

        engine.onTauntParryEvent = {
            soundManager.playTauntParry()
            hapticsManager.impactHeavy()
        }

        engine.onBounceEvent = {
            soundManager.playBounce()
        }

        engine.onPursuerAlertEvent = {
            soundManager.playPursuerAlert()
            hapticsManager.dangerPulse()
        }

        engine.onCaughtEvent = {
            soundManager.stopBgm()
            soundManager.playCaught()
            hapticsManager.impactHeavy()
            repository.recordGameResult(
                score = engine.stats.score,
                rank = engine.stats.rank,
                escaped = false,
                toppings = engine.stats.toppingsCollected
            )
            _saveData.value = repository.getSaveData()
        }

        engine.onVictoryEvent = {
            soundManager.stopBgm()
            soundManager.playVictory()
            hapticsManager.victoryFanfare()
            repository.recordGameResult(
                score = engine.stats.score,
                rank = engine.stats.rank,
                escaped = true,
                toppings = engine.stats.toppingsCollected
            )
            _saveData.value = repository.getSaveData()
        }
    }

    private fun startGameLoop() {
        viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val currentTime = System.nanoTime()
                val realDt = ((currentTime - lastTime) / 1_000_000_000.0).toFloat()
                val dt = realDt.coerceIn(0.001f, 0.04f)
                lastTime = currentTime

                if (engine.gameState == GameState.RUNNING) {
                    engine.update(dt, realDt)
                    _gameTick.value = currentTime
                }
                _gameState.value = engine.gameState

                delay(16) // ~60 FPS
            }
        }
    }

    fun startEscape(difficulty: GameDifficulty) {
        engine.startNewGame(difficulty)
        soundManager.startEscapeBgm(urgent = false)
    }

    fun pauseGame() {
        if (engine.gameState == GameState.RUNNING) {
            engine.gameState = GameState.PAUSED
            soundManager.stopBgm()
        }
    }

    fun resumeGame() {
        if (engine.gameState == GameState.PAUSED) {
            engine.gameState = GameState.RUNNING
            soundManager.startEscapeBgm(urgent = engine.stats.timeRemaining < 20f)
        }
    }

    fun goToMenu() {
        engine.gameState = GameState.MENU
        soundManager.stopBgm()
    }

    fun toggleMute() {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        soundManager.isMuted = newMuted
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.stopBgm()
    }
}
