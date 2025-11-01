package com.example.rotanacion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.rotanacion.model.*
import com.example.rotanacion.ui.screen.GameScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ---------- Crear mazos ----------
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

        // ---------- Crear jugadores ----------
        val player1 = Player(1, "Ana")
        val player2 = Player(2, "Luis")
        val player3 = Player(3, "María")

        val engine = GameEngine(listOf(player1, player2, player3))
        engine.dealInitialCards(paisCards, banderaCards, monumentoCards)

        // ---------- Mostrar pantalla principal ----------
        setContent {
            GameScreen(engine)
        }
    }
}



