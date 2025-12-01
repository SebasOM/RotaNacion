package com.example.rotanacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rotanacion.model.Player
import com.example.rotanacion.ui.screen.*
import com.example.rotanacion.viewmodel.GameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    RotaNacionApp()
                }
            }
        }
    }
}

/* -------------------------------------------------------------
   🌍 NAVEGACIÓN PRINCIPAL
   ------------------------------------------------------------- */
@Composable
fun RotaNacionApp() {
    val navController = rememberNavController()
    val gameViewModel: GameViewModel = viewModel()

    NavHost(navController = navController, startDestination = "menu") {
        // ---------- Menú principal ----------
        composable("menu") {
            MainMenuScreen(
                onStartGame = { navController.navigate("nameEntry") },
                onShowRules = { navController.navigate("rules") },
                onExit = { /* Puedes cerrar la app o mostrar un diálogo */ }
            )
        }

        // ---------- Pantalla de nombre ----------
        composable("nameEntry") {
            NameScreen { playerName ->
                val players = listOf(
                    Player(id = 0, name = playerName),
                    Player(id = 1, name = "IA 1", isAI = true),
                    Player(id = 2, name = "IA 2", isAI = true),
                    Player(id = 3, name = "IA 3", isAI = true)
                )
                gameViewModel.startNewGame(players)
                navController.navigate("game")
            }
        }

        // ---------- Pantalla de reglas ----------
        composable("rules") {
            RulesScreen(onBack = { navController.popBackStack() })
        }

        // ---------- Pantalla de juego ----------
        composable("game") {
            GameScreen(viewModel = gameViewModel, navController = navController)
        }
    }
}









