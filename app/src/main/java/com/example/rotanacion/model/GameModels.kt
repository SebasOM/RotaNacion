package com.example.rotanacion.model

import androidx.annotation.DrawableRes

enum class CardType { PAIS, BANDERA, MONUMENTO }

data class Card(
    val nation: String,
    val type: CardType,
    @DrawableRes val imageRes: Int
)

data class Player(
    val id: Int,
    val name: String,
    val isAI: Boolean = false, // 🔹 Nuevo campo: indica si el jugador es controlado por IA
    var hand: MutableMap<CardType, Card> = mutableMapOf()
)








