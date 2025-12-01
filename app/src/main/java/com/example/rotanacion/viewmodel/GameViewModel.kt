package com.example.rotanacion.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rotanacion.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.rotanacion.R

enum class GameMode { TURISTA, CONQUISTADOR }

class GameViewModel : ViewModel() {

    // Estado general
    var mode by mutableStateOf(GameMode.TURISTA)
    var engine by mutableStateOf(GameEngine(emptyList()))

    // Estados visibles para la UI
    var actionDiceResult by mutableStateOf("")
    var cardDiceResult by mutableStateOf(0)
    var canDrawCard by mutableStateOf(false)
    var drawnCard by mutableStateOf<Card?>(null)
    var selectedCardType by mutableStateOf<CardType?>(null)
    var showExchangeDialog by mutableStateOf(false)
    var availableExchanges by mutableStateOf<List<Triple<Player, Card, Int>>>(emptyList())
    var selectedExchange by mutableStateOf<Triple<Player, Card, Int>?>(null)
    var winner by mutableStateOf<Player?>(null)
    var showVictoryScreen by mutableStateOf(false)
    var iaActionMessage by mutableStateOf("")
    val actionHistory = mutableStateListOf<String>()

    var exchangeType: CardType? by mutableStateOf(null)


    fun startNewGame(players: List<Player>) {
        engine = GameEngine(players)
        engine.resetGame()
        actionHistory.clear()
        resetTurnState()
        winner = null
        showVictoryScreen = false
    }

    fun rollActionDice() {
        if (engine.currentPlayer.isAI) return
        val dice = ActionDice()
        actionDiceResult = dice.roll()
        actionHistory.add("${engine.currentPlayer.name} lanzó dado de acción: $actionDiceResult")
        canDrawCard = actionDiceResult == "TOMA"
    }

    fun rollCardDice() {
        if (!canDrawCard || engine.currentPlayer.isAI) return
        val dice = CardDice()
        cardDiceResult = dice.roll()
        val type = when (cardDiceResult) {
            R.drawable.simbolo_pais -> CardType.PAIS
            R.drawable.simbolo_bandera -> CardType.BANDERA
            else -> CardType.MONUMENTO
        }
        handleCardDraw(type)
    }

    private fun handleCardDraw(type: CardType) {
        // 🔹 Guardamos el tipo para usarlo después (en el diálogo o en la toma del mazo)
        selectedCardType = type
        exchangeType = type

        val exchanges = engine.getAvailableBankCards(type)
        availableExchanges = exchanges
        val deck = when (type) {
            CardType.PAIS -> engine.deckPais
            CardType.BANDERA -> engine.deckBandera
            CardType.MONUMENTO -> engine.deckMonumento
        }

        if (exchanges.isNotEmpty()) {
            // Se abre diálogo de intercambio
            showExchangeDialog = true
        } else {
            // Si no hay cartas disponibles para intercambio, se toma del mazo
            if (deck.isNotEmpty()) {
                drawnCard = engine.drawFromDeck(type)
                actionHistory.add("${engine.currentPlayer.name} tomó una carta de ${type.name}")
            } else {
                actionHistory.add("No hay más cartas disponibles en el mazo de ${type.name}")
                nextTurn()
            }
        }
    }


    fun includeCardInHand() {
        drawnCard?.let { card ->
            val player = engine.currentPlayer
            engine.replaceCardInHand(player, card)
            actionHistory.add("${player.name} incluyó una carta de ${card.type}")
            drawnCard = null
            checkVictoryOrContinue()
        }
    }

    fun placeCardInBank() {
        drawnCard?.let { card ->
            val player = engine.currentPlayer
            engine.placeInBank(player, card)
            actionHistory.add("${player.name} dejó una carta de ${card.type} en su banca")
            drawnCard = null
            checkVictoryOrContinue()
        }
    }

    fun performRotation(type: CardType) {
        if (actionDiceResult !in listOf("DER", "IZQ")) return
        val direction = actionDiceResult
        engine.rotateCards(type, direction)
        actionHistory.add("${engine.currentPlayer.name} rotó ${type.name} hacia $direction")
        nextTurn()
    }

    fun confirmExchange() {
        selectedExchange?.let { (otherPlayer, card, _) ->
            val current = engine.currentPlayer
            engine.exchangeWithBank(current, otherPlayer, card)
            actionHistory.add("${current.name} intercambió con ${otherPlayer.name}")
        }
        showExchangeDialog = false
        nextTurn()
    }

    fun takeFromDeckInstead() {
        showExchangeDialog = false

        // 🔹 Determinar tipo de carta
        val type = selectedCardType ?: exchangeType
        if (type == null) {
            actionHistory.add("⚠️ No se pudo determinar el tipo de carta para tomar del mazo.")
            return
        }

        // 🔹 Intentar tomar carta del mazo
        val newCard = engine.drawFromDeck(type)
        if (newCard != null) {
            drawnCard = newCard
            actionHistory.add("${engine.currentPlayer.name} tomó una carta del mazo de ${type.name}")

            // 🔹 Reset parcial de turno (mantiene decisión del jugador)
            selectedExchange = null
            canDrawCard = true
            actionDiceResult = ""
            cardDiceResult = 0
        } else {
            actionHistory.add("No hay más cartas disponibles en el mazo de ${type.name}")
            nextTurn()
        }
    }



    private fun checkVictoryOrContinue() {
        val winnerFound = engine.checkVictory()
        if (winnerFound != null) {
            winner = winnerFound
            showVictoryScreen = true
        } else {
            nextTurn()
        }
    }

    fun nextTurn() {
        engine.nextTurn()
        resetTurnState()
        if (engine.currentPlayer.isAI) performAITurn()
    }

    private fun resetTurnState() {
        actionDiceResult = ""
        cardDiceResult = 0
        canDrawCard = false
        selectedCardType = null
        selectedExchange = null
        drawnCard = null
        iaActionMessage = ""
    }

    // 🤖 Turno de IA
    private fun performAITurn() {
        viewModelScope.launch {
            val player = engine.currentPlayer
            delay(1000)

            val actionDice = ActionDice()
            val result = actionDice.roll()
            actionDiceResult = result
            actionHistory.add("${player.name} lanzó el dado de acción: $result")
            iaActionMessage = "${player.name} obtuvo $result"
            delay(1000)

            when (result) {
                "DER", "IZQ" -> {
                    val type = CardType.values().random()
                    engine.rotateCards(type, result)
                    actionHistory.add("${player.name} rotó cartas de tipo $type hacia $result")
                    iaActionMessage = "${player.name} rotó cartas $type hacia $result"
                    delay(1000)
                }

                "TOMA" -> {
                    val cardDice = CardDice()
                    val face = cardDice.roll()
                    val type = when (face) {
                        R.drawable.simbolo_pais -> CardType.PAIS
                        R.drawable.simbolo_bandera -> CardType.BANDERA
                        else -> CardType.MONUMENTO
                    }

                    cardDiceResult = face
                    actionHistory.add("${player.name} lanzó el dado de carta: $type")
                    iaActionMessage = "${player.name} lanzó el dado carta: $type"
                    delay(1000)

                    val exchanges = engine.getAvailableBankCards(type)
                    if (exchanges.isNotEmpty()) {
                        val chosen = exchanges.random()
                        engine.exchangeWithBank(player, chosen.first, chosen.second)
                        actionHistory.add("${player.name} intercambió con ${chosen.first.name}")
                        iaActionMessage = "${player.name} intercambió con ${chosen.first.name}"
                        delay(1000)
                    } else {
                        val newCard = engine.drawFromDeck(type)
                        if (newCard != null) {
                            drawnCard = newCard
                            iaActionMessage = "${player.name} tomó una carta de ${type.name}"
                            actionHistory.add("${player.name} tomó una carta de ${type.name}")
                            delay(1000)

                            val action = (0..1).random()
                            if (action == 0) {
                                engine.replaceCardInHand(player, newCard)
                                actionHistory.add("${player.name} incluyó la carta en su mano")
                                iaActionMessage = "${player.name} incluyó la carta en su mano"
                            } else {
                                engine.placeInBank(player, newCard)
                                actionHistory.add("${player.name} dejó la carta en su banca")
                                iaActionMessage = "${player.name} dejó la carta en su banca"
                            }

                            delay(1000)
                            drawnCard = null
                        } else {
                            iaActionMessage = "No había cartas disponibles en el mazo de $type"
                        }
                    }
                }
            }

            val win = engine.checkVictory()
            if (win != null) {
                winner = win
                showVictoryScreen = true
            } else {
                nextTurn()
            }
        }
    }

}
