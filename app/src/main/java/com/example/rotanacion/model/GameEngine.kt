package com.example.rotanacion.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

class GameEngine(playersInit: List<Player>) {

    // 🔹 Lista de jugadores
    val players = mutableStateListOf<Player>().apply { addAll(playersInit) }

    // 🔹 Control de turnos
    private val _currentPlayerIndex = mutableStateOf(0)
    val currentPlayerIndex: Int get() = _currentPlayerIndex.value
    val currentPlayer: Player get() = players[currentPlayerIndex]

    // 🔹 Mazos centrales
    val deckPais = mutableStateListOf<Card>()
    val deckBandera = mutableStateListOf<Card>()
    val deckMonumento = mutableStateListOf<Card>()

    // 🔹 Bancas por jugador
    val bankas = mutableMapOf<Int, MutableList<Card>>() // playerId → cartas en banca

    // ---------- Cambiar turno ----------
    fun nextTurn() {
        _currentPlayerIndex.value = (currentPlayerIndex + 1) % players.size
    }

    // ---------- Repartir cartas iniciales ----------
    fun dealInitialCards(
        paisCards: MutableList<Card>,
        banderaCards: MutableList<Card>,
        monumentoCards: MutableList<Card>
    ) {
        paisCards.shuffle()
        banderaCards.shuffle()
        monumentoCards.shuffle()

        for (player in players) {
            player.hand[CardType.PAIS] = paisCards.removeAt(0)
            player.hand[CardType.BANDERA] = banderaCards.removeAt(0)
            player.hand[CardType.MONUMENTO] = monumentoCards.removeAt(0)
            bankas[player.id] = mutableListOf()
        }

        deckPais.addAll(paisCards)
        deckBandera.addAll(banderaCards)
        deckMonumento.addAll(monumentoCards)
    }

    // ---------- Rotar cartas entre jugadores ----------
    fun rotateCards(type: CardType, direction: String) {
        if (players.isEmpty()) return
        val handsSnapshot = players.map { it.hand.toMap() }

        players.forEachIndexed { index, player ->
            val fromIndex = if (direction == "DER")
                (index - 1 + players.size) % players.size
            else
                (index + 1) % players.size

            val newHand = handsSnapshot[index].toMutableMap()
            handsSnapshot[fromIndex][type]?.let { newCard ->
                newHand[type] = newCard
            }
            player.hand = newHand
        }
    }

    // ---------- Tomar carta del mazo central ----------
    fun drawFromDeck(type: CardType): Card? {
        val deck = when (type) {
            CardType.PAIS -> deckPais
            CardType.BANDERA -> deckBandera
            CardType.MONUMENTO -> deckMonumento
        }
        if (deck.isEmpty()) return null
        return deck.removeAt(0)
    }

    // ---------- Dejar carta en banca ----------
    fun placeInBank(player: Player, card: Card) {
        bankas[player.id]?.add(card)
    }

    // ---------- Reemplazar carta en mano ----------
    fun replaceCardInHand(player: Player, newCard: Card) {
        val type = newCard.type
        val oldCard = player.hand[type]
        if (oldCard != null) {
            placeInBank(player, oldCard)
        }
        player.hand[type] = newCard
    }

    // ---------- Obtener cartas disponibles en las bancas para intercambio ----------
    fun getAvailableBankCards(type: CardType): List<Triple<Player, Card, Int>> {
        val exchanges = mutableListOf<Triple<Player, Card, Int>>()
        for (player in players) {
            val banca = bankas[player.id] ?: continue
            banca.forEachIndexed { index, card ->
                if (card.type == type) {
                    exchanges.add(Triple(player, card, index))
                }
            }
        }
        return exchanges
    }

    // ---------- Intercambiar carta con la banca ----------
    fun exchangeWithBank(currentPlayer: Player, otherPlayer: Player, cardFromBank: Card) {
        val type = cardFromBank.type
        val currentCard = currentPlayer.hand[type] ?: return

        // Buscar la carta en la banca del otro jugador
        val banca = bankas[otherPlayer.id] ?: return
        val cardIndex = banca.indexOf(cardFromBank)
        if (cardIndex != -1) {
            // Quitar carta de la banca del otro jugador
            banca.removeAt(cardIndex)
            // Poner la carta actual del jugador en su propia banca
            placeInBank(currentPlayer, currentCard)
            // Reemplazar carta en la mano del jugador
            currentPlayer.hand[type] = cardFromBank
        }
    }
}












