package com.example.game.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.model.GameState

@Composable
fun PizzaEscapeApp(
    viewModel: GameViewModel = viewModel()
) {
    val engine = viewModel.engine
    val tickState = viewModel.gameTick.collectAsState()
    val gameState by viewModel.gameState.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    val saveData by viewModel.saveData.collectAsState()
    val shop by viewModel.shop.collectAsState()

    var showShop by remember { mutableStateOf(false) }
    var showSkins by remember { mutableStateOf(false) }
    var showMilestones by remember { mutableStateOf(false) }

    // The HUD only needs to refresh ~10 times per second (the canvas redraws every frame)
    val hudTick by remember { derivedStateOf { tickState.value / 100_000_000L } }

    Box(modifier = Modifier.fillMaxSize()) {
        when (gameState) {
            GameState.MENU -> {
                MenuScreen(
                    saveData = saveData,
                    haptics = viewModel.hapticsManager,
                    onStartGame = { diff ->
                        viewModel.startEscape(diff)
                    }
                )

                // Balance, shop and milestones buttons
                val hasClaimable = ALL_MILESTONES.any { it.isDone(saveData) && it.id !in shop.claimed }
                MenuTopBar(
                    shop = shop,
                    hasClaimable = hasClaimable,
                    onShop = { showShop = true },
                    onSkins = { showSkins = true },
                    onMilestones = { showMilestones = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 6.dp, end = 10.dp)
                )
            }
            GameState.RUNNING, GameState.PAUSED, GameState.GAME_OVER, GameState.VICTORY -> {
                // Game Canvas (redraws every frame without recomposing the whole screen)
                GameCanvasView(engine = engine, tick = tickState, skinId = shop.selectedSkin)

                // Overlay HUD & Controls
                GameOverlay(
                    engine = engine,
                    haptics = viewModel.hapticsManager,
                    isMuted = isMuted,
                    onToggleMute = { viewModel.toggleMute() },
                    onPauseClick = { viewModel.pauseGame() },
                    hudTick = hudTick
                )

                // Buy-and-use items (shield, turbo, freeze, hourglass)
                PowerUpBar(
                    shop = shop,
                    enabled = gameState == GameState.RUNNING,
                    onUse = { viewModel.usePowerUp(it) },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 10.dp)
                )

                // Dialogs
                when (gameState) {
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

        if (showShop) {
            ShopDialog(
                shop = shop,
                onBuy = { viewModel.buyItem(it) },
                onDismiss = { showShop = false }
            )
        }
        if (showSkins) {
            SkinShopDialog(
                shop = shop,
                onBuy = { viewModel.buySkin(it) },
                onEquip = { viewModel.equipSkin(it) },
                onAdminCode = { viewModel.unlockAdminSkin(it) },
                onDismiss = { showSkins = false }
            )
        }
        if (showMilestones) {
            MilestonesDialog(
                save = saveData,
                shop = shop,
                onClaim = { viewModel.claimMilestone(it) },
                onDismiss = { showMilestones = false }
            )
        }
    }
}
