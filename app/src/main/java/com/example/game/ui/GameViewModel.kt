package com.example.game.ui

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.SoundManager
import com.example.game.data.GameRepository
import com.example.game.data.GameSaveData
import com.example.game.engine.PizzaEscapeEngine
import com.example.game.haptics.HapticsManager
import com.example.game.model.GameDifficulty
import com.example.game.model.GameState
import com.example.game.model.PursuerState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
    val soundManager = SoundManager(application.applicationContext)
    val hapticsManager = HapticsManager(application.applicationContext)
    val repository = GameRepository(application.applicationContext)
    private val shopRepository = ShopRepository(application.applicationContext)
    private val sfx = SfxPlayer(application.applicationContext)

    val engine = PizzaEscapeEngine()

    private val _saveData = MutableStateFlow(repository.getSaveData())
    val saveData: StateFlow<GameSaveData> = _saveData.asStateFlow()

    // Spendable toppings, bought items and claimed milestones
    private val _shop = MutableStateFlow(shopRepository.load())
    val shop: StateFlow<ShopState> = _shop.asStateFlow()

    private var lastPursuerState = PursuerState.CHASING

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    // Frame trigger state flow for recomposition
    private val _gameTick = MutableStateFlow(0L)
    val gameTick: StateFlow<Long> = _gameTick.asStateFlow()

    // Tells the UI when the screen (menu / running / paused / game over) changes
    private val _gameState = MutableStateFlow(GameState.MENU)
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private var urgentMusic = false

    // Pause the game and the music when the app leaves the screen, restart the music when it returns
    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityStarted(activity: Activity) {
            soundManager.resumeMusic()
        }

        override fun onActivityStopped(activity: Activity) {
            if (engine.gameState == GameState.RUNNING) {
                pauseGame()
            }
            soundManager.suspendMusic()
        }

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
        override fun onActivityResumed(activity: Activity) {}
        override fun onActivityPaused(activity: Activity) {}
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
        override fun onActivityDestroyed(activity: Activity) {}
    }

    init {
        setupEngineCallbacks()
        startGameLoop()
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
        soundManager.startMenuBgm()
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
            sfx.playCollect()
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
            shopRepository.addToppings(engine.stats.toppingsCollected)
            _shop.value = shopRepository.load()
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
            shopRepository.addToppings(engine.stats.toppingsCollected)
            _shop.value = shopRepository.load()
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

                    // Sound when the pursuer gets stunned (parry, hot pepper or freeze item)
                    val pursuerState = engine.pursuer.state
                    if (pursuerState == PursuerState.STUNNED && lastPursuerState != PursuerState.STUNNED) {
                        sfx.playStun()
                    }
                    lastPursuerState = pursuerState

                    // Last 20 seconds: switch to the tense music
                    if (!urgentMusic && engine.gameState == GameState.RUNNING &&
                        engine.stats.timeRemaining < 20f
                    ) {
                        urgentMusic = true
                        soundManager.startEscapeBgm(urgent = true)
                    }
                }
                _gameState.value = engine.gameState

                delay(16) // ~60 FPS
            }
        }
    }

    fun startEscape(difficulty: GameDifficulty) {
        engine.startNewGame(difficulty)
        urgentMusic = false
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
            urgentMusic = engine.stats.timeRemaining < 20f
            soundManager.startEscapeBgm(urgent = urgentMusic)
        }
    }

    fun goToMenu() {
        engine.gameState = GameState.MENU
        soundManager.startMenuBgm()
    }

    fun buyItem(item: PowerUp) {
        if (shopRepository.buy(item)) {
            sfx.playBuy()
            _shop.value = shopRepository.load()
        }
    }

    fun claimMilestone(milestone: Milestone) {
        if (!milestone.isDone(_saveData.value)) return
        if (shopRepository.claim(milestone)) {
            sfx.playMilestone()
            _shop.value = shopRepository.load()
        }
    }

    fun usePowerUp(item: PowerUp) {
        if (engine.gameState != GameState.RUNNING) return
        if (!shopRepository.consume(item)) return
        _shop.value = shopRepository.load()
        sfx.playPowerUp()
        when (item) {
            PowerUp.SHIELD -> engine.player.invincibilityTimer = 5f
            PowerUp.TURBO -> engine.player.hotPepperTimer = 6f
            PowerUp.FREEZE -> {
                engine.pursuer.state = PursuerState.STUNNED
                engine.pursuer.stunTimer = 5f
            }
            PowerUp.HOURGLASS -> {
                engine.stats.timeRemaining = (engine.stats.timeRemaining - 10f).coerceAtLeast(1f)
            }
        }
    }

    fun toggleMute() {
        val newMuted = !_isMuted.value
        _isMuted.value = newMuted
        soundManager.isMuted = newMuted
        sfx.isMuted = newMuted
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
        soundManager.release()
        sfx.release()
    }
}
