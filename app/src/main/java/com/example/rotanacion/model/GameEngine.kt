package com.example.rotanacion.model

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.rotanacion.R

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

        deckPais.clear()
        deckBandera.clear()
        deckMonumento.clear()
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

        checkVictory()?.let { winner ->
            println("🎉 ¡${winner.name} ha ganado con ${winner.hand.values.first().nation}!")
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
        checkVictory()?.let { winner ->
            println("🎉 ¡${winner.name} ha ganado con ${winner.hand.values.first().nation}!")
        }
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

        val bancaOtro = bankas[otherPlayer.id] ?: return
        val cardIndex = bancaOtro.indexOf(cardFromBank)
        if (cardIndex != -1) {
            bancaOtro.removeAt(cardIndex)
            currentPlayer.hand[type] = cardFromBank
            bankas[otherPlayer.id]?.add(currentCard)
            checkVictory()?.let { winner ->
                println("🎉 ¡${winner.name} ha ganado con ${winner.hand.values.first().nation}!")
            }
        }
    }

    // ---------- Comprobar si hay un ganador ----------
    fun checkVictory(): Player? {
        for (player in players) {
            val hand = player.hand.values
            if (hand.size == 3) {
                val firstNation = hand.first().nation
                if (hand.all { it.nation == firstNation }) {
                    return player
                }
            }
        }
        return null
    }

    // ---------- 🔁 Reiniciar juego ----------
    fun resetGame() {
        // 1️⃣ Limpiar manos y bancas
        players.forEach { player ->
            player.hand.clear()
            bankas[player.id]?.clear()
        }

        // 2️⃣ Crear nuevas barajas
        val paisCards = mutableListOf(
            Card("Colombia", CardType.PAIS, R.drawable.pais_colombia),
            Card("Brasil", CardType.PAIS, R.drawable.pais_brasil),
            Card("USA", CardType.PAIS, R.drawable.pais_usa),
            Card("Japón", CardType.PAIS, R.drawable.pais_japon),
            Card("India", CardType.PAIS, R.drawable.pais_india),
            Card("Jordania", CardType.PAIS, R.drawable.pais_jordania),
            Card("Egipto", CardType.PAIS, R.drawable.pais_egipto),
            Card("Sudáfrica", CardType.PAIS, R.drawable.pais_sudafrica),
            Card("Argelia", CardType.PAIS, R.drawable.pais_argelia),
            Card("Italia", CardType.PAIS, R.drawable.pais_italia),
            Card("Francia", CardType.PAIS, R.drawable.pais_francia),
            Card("Reino Unido", CardType.PAIS, R.drawable.pais_reino_unido)
        )

        val banderaCards = mutableListOf(
            Card("Colombia", CardType.BANDERA, R.drawable.bandera_colombia),
            Card("Brasil", CardType.BANDERA, R.drawable.bandera_brasil),
            Card("USA", CardType.BANDERA, R.drawable.bandera_usa),
            Card("Japón", CardType.BANDERA, R.drawable.bandera_japon),
            Card("India", CardType.BANDERA, R.drawable.bandera_india),
            Card("Jordania", CardType.BANDERA, R.drawable.bandera_jordania),
            Card("Egipto", CardType.BANDERA, R.drawable.bandera_egipto),
            Card("Sudáfrica", CardType.BANDERA, R.drawable.bandera_sudafrica),
            Card("Argelia", CardType.BANDERA, R.drawable.bandera_argelia),
            Card("Italia", CardType.BANDERA, R.drawable.bandera_italia),
            Card("Francia", CardType.BANDERA, R.drawable.bandera_francia),
            Card("Reino Unido", CardType.BANDERA, R.drawable.bandera_reino_unido)
        )

        val monumentoCards = mutableListOf(
            Card("Colombia", CardType.MONUMENTO, R.drawable.monumento_colombia),
            Card("Brasil", CardType.MONUMENTO, R.drawable.monumento_brasil),
            Card("USA", CardType.MONUMENTO, R.drawable.monumento_usa),
            Card("Japón", CardType.MONUMENTO, R.drawable.monumento_japon),
            Card("India", CardType.MONUMENTO, R.drawable.monumento_india),
            Card("Jordania", CardType.MONUMENTO, R.drawable.monumento_jordania),
            Card("Egipto", CardType.MONUMENTO, R.drawable.monumento_egipto),
            Card("Sudáfrica", CardType.MONUMENTO, R.drawable.monumento_sudafrica),
            Card("Argelia", CardType.MONUMENTO, R.drawable.monumento_argelia),
            Card("Italia", CardType.MONUMENTO, R.drawable.monumento_italia),
            Card("Francia", CardType.MONUMENTO, R.drawable.monumento_francia),
            Card("Reino Unido", CardType.MONUMENTO, R.drawable.monumento_reino_unido)
        )

        // 3️⃣ Repartir nuevas cartas
        dealInitialCards(paisCards, banderaCards, monumentoCards)

        // 4️⃣ Reiniciar turno
        _currentPlayerIndex.value = 0
    }
}
















