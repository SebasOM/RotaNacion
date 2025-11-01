package com.example.rotanacion.model

// Dado de Acción: muestra texto (DER, IZQ, TOMA)
class ActionDice {
    private val faces = listOf("DER", "IZQ", "TOMA")
    fun roll(): String = faces.random()
}

// Dado de Carta: muestra imágenes (símbolos de reversos)
class CardDice {
    private val faces: List<Int> = listOf(
        com.example.rotanacion.R.drawable.simbolo_pais,
        com.example.rotanacion.R.drawable.simbolo_bandera,
        com.example.rotanacion.R.drawable.simbolo_monumento
    )
    fun roll(): Int = faces.random()
}





