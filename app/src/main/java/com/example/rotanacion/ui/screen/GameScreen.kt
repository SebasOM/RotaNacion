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
import kotlinx.coroutines.delay

@Composable
fun GameScreen(engine: GameEngine) {
    // ---------- Estados ----------
    var actionDiceResult by remember { mutableStateOf("") }
    var cardDiceResult by remember { mutableStateOf(0) }
    var canDrawCard by remember { mutableStateOf(false) }
    var selectedCardType by remember { mutableStateOf<CardType?>(null) }
    var drawnCard by remember { mutableStateOf<Card?>(null) }

    var showExchangeDialog by remember { mutableStateOf(false) }
    var availableExchanges by remember { mutableStateOf<List<Triple<Player, Card, Int>>>(emptyList()) }
    var selectedExchange by remember { mutableStateOf<Triple<Player, Card, Int>?>(null) }
    var currentDeckEmpty by remember { mutableStateOf(false) }
    var exchangeType by remember { mutableStateOf<CardType?>(null) }

    var winner by remember { mutableStateOf<Player?>(null) }
    var showVictoryScreen by remember { mutableStateOf(false) }

    var iaActionMessage by remember { mutableStateOf("") }
    val actionHistory = remember { mutableStateListOf<String>() }

    val currentPlayer = engine.currentPlayer
    val scrollState = rememberScrollState()

    // 🧠 --- Comportamiento IA ---
    LaunchedEffect(engine.currentPlayerIndex) {
        val player = engine.currentPlayer
        if (player.isAI) {
            delay(1000)
            val actionDice = ActionDice()
            val result = actionDice.roll()
            actionDiceResult = result
            actionHistory.add("${player.name} lanzó el dado de acción: $result")
            iaActionMessage = "${player.name} obtuvo $result"
            delay(800)

            when (result) {
                "TOMA" -> {
                    val cardDice = CardDice()
                    cardDiceResult = cardDice.roll()
                    val type = when (cardDiceResult) {
                        R.drawable.simbolo_pais -> CardType.PAIS
                        R.drawable.simbolo_bandera -> CardType.BANDERA
                        else -> CardType.MONUMENTO
                    }
                    actionHistory.add("${player.name} lanzó dado carta: $type")
                    iaActionMessage = "${player.name} lanzó dado carta: $type"
                    delay(800)

                    val exchanges = engine.getAvailableBankCards(type)
                    if (exchanges.isNotEmpty()) {
                        val chosen = exchanges.random()
                        engine.exchangeWithBank(player, chosen.first, chosen.second)
                        actionHistory.add("${player.name} intercambió con ${chosen.first.name}")
                        iaActionMessage = "${player.name} intercambió con ${chosen.first.name}"
                    } else {
                        val newCard = engine.drawFromDeck(type)
                        if (newCard != null) {
                            // 🔹 Mostrar carta tomada por IA
                            drawnCard = newCard
                            iaActionMessage = "${player.name} tomó una carta de $type"
                            actionHistory.add("${player.name} tomó una carta de $type")
                            delay(1500)

                            if ((0..1).random() == 0) {
                                engine.replaceCardInHand(player, newCard)
                                actionHistory.add("${player.name} incluyó la carta en su mano")
                                iaActionMessage = "${player.name} incluyó la carta en su mano"
                            } else {
                                engine.placeInBank(player, newCard)
                                actionHistory.add("${player.name} dejó la carta en su banca")
                                iaActionMessage = "${player.name} dejó la carta en su banca"
                            }

                            delay(1500)
                            drawnCard = null
                        }
                    }
                }

                "DER", "IZQ" -> {
                    val type = CardType.values().random()
                    engine.rotateCards(type, result)
                    actionHistory.add("${player.name} rotó cartas de tipo $type hacia $result")
                    iaActionMessage = "${player.name} rotó $type hacia $result"
                }
            }

            val winnerFound = engine.checkVictory()
            if (winnerFound != null) {
                winner = winnerFound
                showVictoryScreen = true
            } else {
                engine.nextTurn()
                iaActionMessage = ""
                resetTurnState {
                    actionDiceResult = ""
                    cardDiceResult = 0
                    canDrawCard = false
                    selectedCardType = null
                    drawnCard = null
                    selectedExchange = null
                    exchangeType = null
                }
            }
        }
    }

    // ---------- Pantalla de victoria ----------
    if (showVictoryScreen && winner != null) {
        VictoryScreen(
            winner = winner!!,
            onRestart = {
                engine.resetGame()
                showVictoryScreen = false
                winner = null
                actionHistory.clear()
            },
            onReturnToMenu = {
                showVictoryScreen = false
                winner = null
            }
        )
        return
    }

    // ---------- Contenedor principal ----------
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

            if (iaActionMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = iaActionMessage, fontSize = 16.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Dado Acción ----------
            Text("Dado Acción", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val actionDice = ActionDice()
                    actionDiceResult = actionDice.roll()
                    actionHistory.add("${currentPlayer.name} lanzó dado de acción: $actionDiceResult")
                    canDrawCard = actionDiceResult == "TOMA"
                    drawnCard = null
                    selectedCardType = null
                    selectedExchange = null
                },
                enabled = !currentPlayer.isAI,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
            ) { Text("Lanzar", color = Color.White) }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (actionDiceResult.isNotEmpty()) actionDiceResult else "—",
                fontSize = 36.sp,
                color = Color(0xFF512DA8),
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Dado Carta ----------
            Text("Dado Carta", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val cardDice = CardDice()
                    cardDiceResult = cardDice.roll()
                    val type = when (cardDiceResult) {
                        R.drawable.simbolo_pais -> CardType.PAIS
                        R.drawable.simbolo_bandera -> CardType.BANDERA
                        R.drawable.simbolo_monumento -> CardType.MONUMENTO
                        else -> null
                    }
                    type?.let { selectedType ->
                        exchangeType = selectedType
                        val exchanges = engine.getAvailableBankCards(selectedType)
                        val deck = when (selectedType) {
                            CardType.PAIS -> engine.deckPais
                            CardType.BANDERA -> engine.deckBandera
                            CardType.MONUMENTO -> engine.deckMonumento
                        }
                        currentDeckEmpty = deck.isEmpty()
                        actionHistory.add("${currentPlayer.name} lanzó dado carta: $selectedType")
                        when {
                            exchanges.isNotEmpty() -> {
                                availableExchanges = exchanges
                                showExchangeDialog = true
                            }
                            else -> {
                                if (deck.isNotEmpty()) {
                                    drawnCard = engine.drawFromDeck(selectedType)
                                } else {
                                    engine.nextTurn()
                                }
                            }
                        }
                    }
                },
                enabled = canDrawCard && !currentPlayer.isAI,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canDrawCard) Color(0xFF7E57C2) else Color.LightGray
                )
            ) { Text("Lanzar", color = Color.White) }

            Spacer(modifier = Modifier.height(16.dp))
            if (cardDiceResult != 0) {
                Image(
                    painter = painterResource(id = cardDiceResult),
                    contentDescription = "Resultado dado carta",
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Mazos centrales ----------
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
                        modifier = Modifier.size(100.dp).padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Carta tomada del mazo ----------
            drawnCard?.let { card ->
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

                if (!currentPlayer.isAI) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                engine.replaceCardInHand(currentPlayer, card)
                                actionHistory.add("${currentPlayer.name} incluyó una carta de ${card.type} en su mano")
                                val winnerFound = engine.checkVictory()
                                if (winnerFound != null) {
                                    winner = winnerFound
                                    showVictoryScreen = true
                                } else {
                                    engine.nextTurn()
                                    resetTurnState {
                                        drawnCard = null
                                        canDrawCard = false
                                        actionDiceResult = ""
                                        cardDiceResult = 0
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                        ) { Text("Incluir en mi mano") }

                        Button(
                            onClick = {
                                engine.placeInBank(currentPlayer, card)
                                actionHistory.add("${currentPlayer.name} dejó una carta de ${card.type} en su banca")
                                val winnerFound = engine.checkVictory()
                                if (winnerFound != null) {
                                    winner = winnerFound
                                    showVictoryScreen = true
                                } else {
                                    engine.nextTurn()
                                    resetTurnState {
                                        drawnCard = null
                                        canDrawCard = false
                                        actionDiceResult = ""
                                        cardDiceResult = 0
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                        ) { Text("Dejar en banca") }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ---------- Botones de rotación ----------
            if (actionDiceResult == "DER" || actionDiceResult == "IZQ") {
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
                            onClick = { selectedCardType = type },
                            modifier = Modifier.weight(1f).padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedCardType == type)
                                    Color(0xFFB39DDB) else Color.LightGray
                            )
                        ) {
                            Text(
                                text = type.name,
                                fontSize = 14.sp,
                                color = if (selectedCardType == type) Color.White else Color.Black
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        selectedCardType?.let { type ->
                            engine.rotateCards(type, actionDiceResult)
                            actionHistory.add("${currentPlayer.name} rotó $type hacia $actionDiceResult")
                            engine.nextTurn()
                            resetTurnState(onReset = {
                                actionDiceResult = ""
                                selectedCardType = null
                                canDrawCard = false
                            })
                        }
                    },
                    enabled = selectedCardType != null && !currentPlayer.isAI,
                    modifier = Modifier.fillMaxWidth(0.8f).height(55.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedCardType != null) Color(0xFF512DA8) else Color.LightGray
                    )
                ) { Text("Rotar Cartas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ---------- Tus cartas ----------
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
                        .background(if (isCurrent) Color(0xFFD1C4E9) else Color(0xFFF5F5F5), shape = RoundedCornerShape(12.dp))
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
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())
                        ) {
                            banca.forEach { card ->
                                Card(
                                    modifier = Modifier.width(80.dp).height(120.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
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
                modifier = Modifier.fillMaxWidth().heightIn(max = 250.dp)
                    .background(Color(0xFFF3E5F5), shape = RoundedCornerShape(12.dp))
                    .padding(8.dp).verticalScroll(rememberScrollState())
            ) {
                actionHistory.takeLast(50).forEach { action ->
                    Text("• $action", fontSize = 14.sp, color = Color.DarkGray)
                }
            }
        }

        // ---------- Diálogo de intercambio ----------
        if (showExchangeDialog && !currentPlayer.isAI) {
            val t = exchangeType
            val currentCard = t?.let { currentPlayer.hand[it] }

            AlertDialog(
                onDismissRequest = { },
                title = { Text("Opción de intercambio") },
                text = {
                    Column {
                        Text("Tu carta actual de este tipo:")
                        Spacer(modifier = Modifier.height(8.dp))
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
                        availableExchanges.forEach { (player, card, idx) ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(4.dp)
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
                                    selected = selectedExchange?.second == card,
                                    onClick = { selectedExchange = Triple(player, card, idx) }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedExchange?.let { (player, card, _) ->
                                engine.exchangeWithBank(currentPlayer, player, card)
                                actionHistory.add("${currentPlayer.name} intercambió con ${player.name}")
                            }
                            showExchangeDialog = false
                            engine.nextTurn()
                            resetTurnState { }
                        },
                        enabled = selectedExchange != null
                    ) { Text("Intercambiar") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        exchangeType?.let { type ->
                            drawnCard = engine.drawFromDeck(type)
                        }
                        showExchangeDialog = false
                    }) { Text("Tomar del mazo") }
                }
            )
        }
    }
}

private fun resetTurnState(onReset: () -> Unit) { onReset() }
















