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

@Composable
fun GameScreen(engine: GameEngine) {
    // ---------- Estados ----------
    var actionDiceResult by remember { mutableStateOf("") }
    var cardDiceResult by remember { mutableStateOf(0) }
    var canDrawCard by remember { mutableStateOf(false) }
    var selectedCardType by remember { mutableStateOf<CardType?>(null) }
    var drawnCard by remember { mutableStateOf<Card?>(null) }

    // Estados del intercambio
    var showExchangeDialog by remember { mutableStateOf(false) }
    var availableExchanges by remember { mutableStateOf<List<Triple<Player, Card, Int>>>(emptyList()) }
    var selectedExchange by remember { mutableStateOf<Triple<Player, Card, Int>?>(null) }
    var currentDeckEmpty by remember { mutableStateOf(false) }
    var exchangeType by remember { mutableStateOf<CardType?>(null) }

    // Estado de victoria
    var winner by remember { mutableStateOf<Player?>(null) }
    var showVictoryScreen by remember { mutableStateOf(false) }

    val currentPlayer = engine.currentPlayer
    val scrollState = rememberScrollState()

    // ---------- Mostrar pantalla de victoria si hay ganador ----------
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
                // 🔹 Aquí podríamos notificar al MainActivity que cambie de pantalla
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

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Dado Acción ----------
            Text("Dado Acción", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val actionDice = ActionDice()
                    actionDiceResult = actionDice.roll()
                    canDrawCard = actionDiceResult == "TOMA"
                    drawnCard = null
                    selectedCardType = null
                    selectedExchange = null
                },
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

                        when {
                            // Si hay cartas en bancas → mostrar diálogo de intercambio
                            exchanges.isNotEmpty() -> {
                                availableExchanges = exchanges
                                showExchangeDialog = true
                            }

                            // Si no hay bancas de ese tipo → tomar del mazo o pasar turno
                            else -> {
                                if (deck.isNotEmpty()) {
                                    drawnCard = engine.drawFromDeck(selectedType)
                                } else {
                                    engine.nextTurn()
                                    resetStates {
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
                    }
                },
                enabled = canDrawCard,
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

            // ---------- Carta tomada ----------
            drawnCard?.let { card ->
                Text("Carta tomada del mazo:", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .width(120.dp)
                        .height(180.dp),
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
                        onClick = {
                            engine.replaceCardInHand(currentPlayer, card)
                            winner = engine.checkVictory()
                            if (winner != null) showVictoryScreen = true
                            engine.nextTurn()
                            resetStates {
                                actionDiceResult = ""
                                cardDiceResult = 0
                                canDrawCard = false
                                selectedCardType = null
                                drawnCard = null
                                selectedExchange = null
                                exchangeType = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("Incluir en mi mano") }

                    Button(
                        onClick = {
                            engine.placeInBank(currentPlayer, card)
                            winner = engine.checkVictory()
                            if (winner != null) showVictoryScreen = true
                            engine.nextTurn()
                            resetStates {
                                actionDiceResult = ""
                                cardDiceResult = 0
                                canDrawCard = false
                                selectedCardType = null
                                drawnCard = null
                                selectedExchange = null
                                exchangeType = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) { Text("Dejar en banca") }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

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
                        modifier = Modifier
                            .size(100.dp)
                            .padding(4.dp)
                    )
                }
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
                        modifier = Modifier
                            .width(100.dp)
                            .height(160.dp),
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
                            if (isCurrent) Color(0xFFD1C4E9)
                            else Color(0xFFF5F5F5),
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
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(120.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(4.dp)
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

            // ---------- Selección del tipo de carta para rotar ----------
            if (actionDiceResult == "DER" || actionDiceResult == "IZQ") {
                Text(
                    text = "Selecciona el tipo de carta para rotar:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    CardType.values().forEach { type ->
                        Button(
                            onClick = { selectedCardType = type },
                            modifier = Modifier
                                .weight(1f)
                                .padding(4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedCardType == type)
                                    Color(0xFFB39DDB) else Color.LightGray
                            )
                        ) {
                            Text(
                                text = type.name,
                                fontSize = 14.sp,
                                color = if (selectedCardType == type)
                                    Color.White else Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(90.dp))
        }

        // ---------- Botón fijo: Rotar Cartas ----------
        Button(
            onClick = {
                selectedCardType?.let { type ->
                    engine.rotateCards(type, actionDiceResult)
                    winner = engine.checkVictory()
                    if (winner != null) showVictoryScreen = true
                    engine.nextTurn()
                    resetStates {
                        actionDiceResult = ""
                        cardDiceResult = 0
                        canDrawCard = false
                        selectedCardType = null
                        drawnCard = null
                        selectedExchange = null
                        exchangeType = null
                    }
                }
            },
            enabled = selectedCardType != null && (actionDiceResult == "DER" || actionDiceResult == "IZQ"),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(0.9f)
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedCardType != null)
                    Color(0xFF512DA8) else Color.LightGray
            )
        ) {
            Text("Rotar Cartas", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // ---------- Diálogo de intercambio ----------
        if (showExchangeDialog) {
            val t = exchangeType
            val currentCard = t?.let { currentPlayer.hand[it] }

            AlertDialog(
                onDismissRequest = { },
                title = { Text("Opción de intercambio") },
                text = {
                    // Scroll + fades
                    val dialogScroll = rememberScrollState()
                    val atTop by remember { derivedStateOf { dialogScroll.value == 0 } }
                    val atBottom by remember {
                        derivedStateOf { dialogScroll.value >= dialogScroll.maxValue - 5 }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 500.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(dialogScroll)
                                .padding(horizontal = 4.dp)
                        ) {
                            Text("Tu carta actual de este tipo:")
                            currentCard?.let {
                                Spacer(modifier = Modifier.height(8.dp))
                                Image(
                                    painter = painterResource(id = it.imageRes),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(90.dp)
                                        .align(Alignment.CenterHorizontally)
                                )
                                Text(
                                    "${it.nation} (${it.type})",
                                    fontSize = 14.sp,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Selecciona una carta de las bancas:")
                            Spacer(modifier = Modifier.height(8.dp))

                            availableExchanges.forEach { (player, card, idx) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp)
                                        .background(
                                            if (selectedExchange?.second == card)
                                                Color(0xFFD1C4E9)
                                            else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(8.dp)
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

                        if (!atTop) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .align(Alignment.TopCenter)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.White.copy(alpha = 0.9f), Color.Transparent)
                                        )
                                    )
                            )
                        }

                        if (!atBottom) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(24.dp)
                                    .align(Alignment.BottomCenter)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.9f))
                                        )
                                    )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            selectedExchange?.let { (player, card, _) ->
                                engine.exchangeWithBank(currentPlayer, player, card)
                                winner = engine.checkVictory()
                                if (winner != null) showVictoryScreen = true
                            }
                            showExchangeDialog = false
                            engine.nextTurn()
                            resetStates {
                                actionDiceResult = ""
                                cardDiceResult = 0
                                canDrawCard = false
                                selectedCardType = null
                                drawnCard = null
                                selectedExchange = null
                                exchangeType = null
                            }
                        },
                        enabled = selectedExchange != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                    ) { Text("Intercambiar") }
                },
                dismissButton = {
                    if (!currentDeckEmpty) {
                        TextButton(onClick = {
                            exchangeType?.let { type ->
                                drawnCard = engine.drawFromDeck(type)
                                winner = engine.checkVictory()
                                if (winner != null) showVictoryScreen = true
                            }
                            showExchangeDialog = false
                            selectedExchange = null
                        }) { Text("Tomar del mazo") }
                    } else {
                        TextButton(onClick = {
                            showExchangeDialog = false
                            engine.nextTurn()
                            resetStates {
                                actionDiceResult = ""
                                cardDiceResult = 0
                                canDrawCard = false
                                selectedCardType = null
                                drawnCard = null
                                selectedExchange = null
                                exchangeType = null
                            }
                        }) { Text("Cancelar") }
                    }
                }
            )
        }
    }
}

private fun resetStates(onReset: () -> Unit) { onReset() }















