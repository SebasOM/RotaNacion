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
    var hand: MutableMap<CardType, Card> = mutableMapOf() // mutable para actualizar
)







