package com.example.rotanacion.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rotanacion.R
import com.example.rotanacion.model.*
import com.example.rotanacion.viewmodel.GameViewModel
import androidx.navigation.NavHostController

@Composable
fun GameScreen(viewModel: GameViewModel, navController: NavHostController) {
    val engine = viewModel.engine
    val currentPlayer = engine.currentPlayer
    val scrollState = rememberScrollState()

    // ---------- Pantalla de victoria ----------
    if (viewModel.showVictoryScreen && viewModel.winner != null) {
        VictoryScreen(
            winner = viewModel.winner!!,
            onRestart = {
                viewModel.startNewGame(engine.players)
                viewModel.showVictoryScreen = false
            },
            onReturnToMenu = {
                viewModel.showVictoryScreen = false
                viewModel.winner = null
                navController.navigate("menu") {
                    popUpTo("menu") { inclusive = true }
                }
            }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 100.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ---------- Turno ----------
            Text(
                text = "Turno de: ${currentPlayer.name}",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            if (viewModel.iaActionMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = viewModel.iaActionMessage,
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Dado Acción ----------
            Text("Dado Acción", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.rollActionDice() },
                enabled = !currentPlayer.isAI,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
            ) { Text("Lanzar", color = Color.White) }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (viewModel.actionDiceResult.isNotEmpty()) viewModel.actionDiceResult else "—",
                fontSize = 36.sp,
                color = Color(0xFF512DA8),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Dado Carta ----------
            Text("Dado Carta", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { viewModel.rollCardDice() },
                enabled = viewModel.canDrawCard && !currentPlayer.isAI,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (viewModel.canDrawCard) Color(0xFF7E57C2) else Color.LightGray
                )
            ) { Text("Lanzar", color = Color.White) }

            Spacer(modifier = Modifier.height(16.dp))
            if (viewModel.cardDiceResult != 0) {
                Image(
                    painter = painterResource(id = viewModel.cardDiceResult),
                    contentDescription = "Resultado dado carta",
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Mazos ----------
            Text("Mazos centrales", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(
                    CardType.PAIS to R.drawable.reverso_pais,
                    CardType.BANDERA to R.drawable.reverso_bandera,
                    CardType.MONUMENTO to R.drawable.reverso_monumento
                ).forEach { (type, imageRes) ->
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = "Mazo ${type.name}",
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Carta tomada ----------
            viewModel.drawnCard?.let { card ->
                Text("Carta tomada del mazo:", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.width(120.dp).height(180.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9))
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = card.imageRes),
                            contentDescription = card.type.name,
                            modifier = Modifier.size(90.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${card.nation}\n${card.type.name}", fontSize = 14.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { viewModel.includeCardInHand() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("Incluir en mi mano") }

                    Button(
                        onClick = { viewModel.placeCardInBank() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) { Text("Dejar en banca") }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------- Rotaciones ----------
            if (viewModel.actionDiceResult in listOf("DER", "IZQ")) {
                Text(
                    text = "Selecciona el tipo de carta para rotar:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CardType.values().forEach { type ->
                        Button(
                            onClick = { viewModel.performRotation(type) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB39DDB))
                        ) {
                            Text(type.name, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ---------- Cartas del jugador ----------
            Text("Tus cartas", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                currentPlayer.hand.forEach { (type, card) ->
                    Card(
                        modifier = Modifier.width(100.dp).height(160.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = card.imageRes),
                                contentDescription = type.name,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${card.nation}\n${type.name}", fontSize = 14.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- Bancas ----------
            Text("Bancas de los jugadores", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(12.dp))
            engine.players.forEach { player ->
                val banca = engine.bankas[player.id] ?: emptyList()
                val isCurrent = player == currentPlayer

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isCurrent) Color(0xFFD1C4E9) else Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(8.dp)
                ) {
                    Text(
                        text = player.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrent) Color(0xFF512DA8) else Color.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (banca.isEmpty()) {
                        Text("Sin cartas en banca", fontSize = 14.sp, color = Color.Gray)
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                        ) {
                            banca.forEach { card ->
                                Card(
                                    modifier = Modifier.width(80.dp).height(120.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = card.imageRes),
                                            contentDescription = card.type.name,
                                            modifier = Modifier.size(60.dp)
                                        )
                                        Text(card.type.name, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ---------- Historial ----------
            Text("Historial de partida", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp)
                    .background(Color(0xFFF3E5F5), shape = RoundedCornerShape(12.dp))
                    .padding(8.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                viewModel.actionHistory.takeLast(50).forEach { action ->
                    Text("• $action", fontSize = 14.sp, color = Color.DarkGray)
                }
            }
        }

        // ---------- Diálogo de intercambio ----------
        if (viewModel.showExchangeDialog && !currentPlayer.isAI) {
            val selectedType = viewModel.selectedCardType ?: viewModel.exchangeType
            val isDeckEmpty = selectedType?.let {
                when (it) {
                    CardType.PAIS -> viewModel.engine.deckPais.isEmpty()
                    CardType.BANDERA -> viewModel.engine.deckBandera.isEmpty()
                    CardType.MONUMENTO -> viewModel.engine.deckMonumento.isEmpty()
                }
            } ?: false

            AlertDialog(
                onDismissRequest = { },
                title = { Text("Opción de intercambio") },
                text = {
                    Column {
                        Text("Tu carta actual de este tipo:")
                        Spacer(modifier = Modifier.height(8.dp))
                        val type = viewModel.selectedCardType
                        val currentCard = type?.let { currentPlayer.hand[it] }
                        currentCard?.let {
                            Image(
                                painter = painterResource(id = it.imageRes),
                                contentDescription = null,
                                modifier = Modifier.size(90.dp)
                            )
                            Text("${it.nation} (${it.type})")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Selecciona una carta de las bancas:")
                        Spacer(modifier = Modifier.height(8.dp))
                        viewModel.availableExchanges.forEach { (player, card, idx) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = card.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier.size(50.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${card.nation} (${card.type}) de ${player.name}")
                                Spacer(modifier = Modifier.weight(1f))
                                RadioButton(
                                    selected = viewModel.selectedExchange?.second == card,
                                    onClick = { viewModel.selectedExchange = Triple(player, card, idx) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.confirmExchange() },
                        enabled = viewModel.selectedExchange != null
                    ) { Text("Intercambiar") }
                },
                dismissButton = {
                    if (!isDeckEmpty) {
                        // 🔹 Caso normal: mazo con cartas
                        TextButton(onClick = {
                            viewModel.takeFromDeckInstead()
                        }) { Text("Tomar del mazo") }
                    } else {
                        // 🔹 Caso especial: mazo vacío
                        TextButton(onClick = {
                            viewModel.actionHistory.add("${viewModel.engine.currentPlayer.name} decidió pasar el turno")
                            viewModel.showExchangeDialog = false
                            viewModel.nextTurn()
                        }) { Text("Pasar turno") }
                    }
                }
            )
        }
    }
}



















