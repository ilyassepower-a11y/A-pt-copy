package com.example.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.model.GameState

@Composable
fun PizzaEscapeApp(
    viewModel: GameViewModel = viewModel()
) {
    val engine = viewModel.engine
    val tick by viewModel.gameTick.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val saveData by viewModel.saveData.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (engine.gameState) {
            GameState.MENU -> {
                MenuScreen(
                    saveData = saveData,
                    haptics = viewModel.hapticsManager,
                    onStartGame = { diff ->
                        viewModel.startEscape(diff)
                    }
                )
            }
            GameState.RUNNING, GameState.PAUSED, GameState.GAME_OVER, GameState.VICTORY -> {
                // Read tick to trigger recomposition / frame update
                val currentFrame = tick

                // Game Canvas
                GameCanvasView(engine = engine)

                // Overlay HUD & Controls
                GameOverlay(
                    engine = engine,
                    haptics = viewModel.hapticsManager,
                    isMuted = isMuted,
                    onToggleMute = { viewModel.toggleMute() },
                    onPauseClick = { viewModel.pauseGame() }
                )

                // Dialogs
                when (engine.gameState) {
                    GameState.PAUSED -> {
                        PauseDialog(
                            onResume = { viewModel.resumeGame() },
                            onRestart = { viewModel.startEscape(engine.difficulty) },
                            onMainMenu = { viewModel.goToMenu() },
                            isMuted = isMuted,
                            onToggleMute = { viewModel.toggleMute() }
                        )
                    }
                    GameState.GAME_OVER -> {
                        GameOverDialog(
                            stats = engine.stats,
                            onRetry = { viewModel.startEscape(engine.difficulty) },
                            onMainMenu = { viewModel.goToMenu() }
                        )
                    }
                    GameState.VICTORY -> {
                        VictoryDialog(
                            stats = engine.stats,
                            onPlayAgain = { viewModel.startEscape(engine.difficulty) },
                            onMainMenu = { viewModel.goToMenu() }
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}
