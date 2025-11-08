package com.example.rotanacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.example.rotanacion.model.GameEngine
import com.example.rotanacion.model.Player
import com.example.rotanacion.ui.screen.*

@OptIn(ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            // Control de navegación entre pantallas
            var currentScreen by remember { mutableStateOf("name") }

            // Nombre del jugador humano
            var playerName by remember { mutableStateOf("") }

            // Motor del juego
            var engine by remember { mutableStateOf<GameEngine?>(null) }

            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(500)) +
                            slideInVertically(initialOffsetY = { it / 10 }))
                        .togetherWith(fadeOut(animationSpec = tween(300)))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {

                    // 🧍 Pantalla de nombre
                    "name" -> NameScreen(
                        onConfirm = { enteredName ->
                            playerName = if (enteredName.isNotBlank()) enteredName else "Jugador"
                            val player1 = Player(1, playerName, isAI = false)
                            val player2 = Player(2, "Luis", isAI = true)
                            val player3 = Player(3, "María", isAI = true)
                            engine = GameEngine(listOf(player1, player2, player3))
                            currentScreen = "menu"
                        }
                    )

                    // 🟣 Menú principal
                    "menu" -> MainMenuScreen(
                        onStartGame = {
                            if (engine == null) {
                                val player1 = Player(1, playerName.ifBlank { "Jugador" }, isAI = false)
                                val player2 = Player(2, "Luis", isAI = true)
                                val player3 = Player(3, "María", isAI = true)
                                engine = GameEngine(listOf(player1, player2, player3))
                            }
                            engine?.resetGame()
                            currentScreen = "game"
                        },
                        onShowRules = { currentScreen = "rules" },
                        onExit = { finish() }
                    )

                    // 🕹️ Pantalla de juego
                    "game" -> {
                        if (engine == null) {
                            val player1 = Player(1, playerName.ifBlank { "Jugador" }, isAI = false)
                            val player2 = Player(2, "Luis", isAI = true)
                            val player3 = Player(3, "María", isAI = true)
                            engine = GameEngine(listOf(player1, player2, player3))
                            engine!!.resetGame()
                        }

                        GameScreenWithVictoryMenu(
                            engine = engine!!,
                            onReturnToMenu = { currentScreen = "menu" }
                        )
                    }

                    // 📘 Pantalla de reglas
                    "rules" -> RulesScreen(
                        onBack = { currentScreen = "menu" }
                    )
                }
            }
        }
    }
}

/* --------------------------- 🏆 Flujo de victoria --------------------------- */
@Composable
fun GameScreenWithVictoryMenu(engine: GameEngine, onReturnToMenu: () -> Unit) {
    var winner by remember { mutableStateOf<com.example.rotanacion.model.Player?>(null) }
    var showVictoryScreen by remember { mutableStateOf(false) }

    if (showVictoryScreen && winner != null) {
        VictoryScreen(
            winner = winner!!,
            onRestart = {
                engine.resetGame()
                showVictoryScreen = false
                winner = null
            },
            onReturnToMenu = {
                showVictoryScreen = false
                winner = null
                onReturnToMenu()
            }
        )
    } else {
        GameScreenWithVictoryDetection(engine) { winnerFound ->
            winner = winnerFound
            showVictoryScreen = true
        }
    }
}

/* ------------------------ 🧩 Detección de victoria ------------------------ */
@Composable
fun GameScreenWithVictoryDetection(
    engine: GameEngine,
    onVictoryDetected: (com.example.rotanacion.model.Player) -> Unit
) {
    var localWinner by remember { mutableStateOf<com.example.rotanacion.model.Player?>(null) }

    GameScreen(engine = engine)

    LaunchedEffect(engine.currentPlayer) {
        val winner = engine.checkVictory()
        if (winner != null && winner != localWinner) {
            localWinner = winner
            onVictoryDetected(winner)
        }
    }
}








