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

            // 🔹 Control de navegación
            var currentScreen by remember { mutableStateOf("menu") }

            // 🔹 Crear jugadores y motor
            val player1 = Player(1, "Ana")
            val player2 = Player(2, "Luis")
            val player3 = Player(3, "María")

            val engine = remember { GameEngine(listOf(player1, player2, player3)) }

            // 🔹 Transiciones entre pantallas (fade + slide)
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

                    // 🟣 Pantalla de menú principal
                    "menu" -> MainMenuScreen(
                        onStartGame = {
                            engine.resetGame() // Reinicia mazos y manos
                            currentScreen = "game"
                        },
                        onShowRules = { currentScreen = "rules" },
                        onExit = { finish() }
                    )

                    // 🕹️ Pantalla del juego con manejo del botón “Volver al menú”
                    "game" -> GameScreenWithVictoryMenu(
                        engine = engine,
                        onReturnToMenu = { currentScreen = "menu" }
                    )

                    // 📘 Pantalla de reglas
                    "rules" -> RulesScreen(
                        onBack = { currentScreen = "menu" }
                    )
                }
            }
        }
    }
}

/* -------------------------------------------------------------
   🔁 Funciones auxiliares para manejar el flujo entre
   juego ↔ victoria ↔ menú principal
   ------------------------------------------------------------- */

@Composable
fun GameScreenWithVictoryMenu(engine: GameEngine, onReturnToMenu: () -> Unit) {
    var winner by remember { mutableStateOf<Player?>(null) }
    var showVictoryScreen by remember { mutableStateOf(false) }

    if (showVictoryScreen && winner != null) {
        // 🏆 Mostrar pantalla de victoria
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
        // 🎮 Mostrar pantalla de juego normal
        GameScreenWithVictoryDetection(engine) { winnerFound ->
            winner = winnerFound
            showVictoryScreen = true
        }
    }
}

/* -------------------------------------------------------------
   🧩 GameScreen con callback para detectar la victoria y
   notificar al flujo principal.
   ------------------------------------------------------------- */
@Composable
fun GameScreenWithVictoryDetection(
    engine: GameEngine,
    onVictoryDetected: (Player) -> Unit
) {
    var localWinner by remember { mutableStateOf<Player?>(null) }

    GameScreen(engine = engine)

    // Si detecta un ganador desde GameEngine
    LaunchedEffect(engine.currentPlayer) {
        val winner = engine.checkVictory()
        if (winner != null && winner != localWinner) {
            localWinner = winner
            onVictoryDetected(winner)
        }
    }
}







