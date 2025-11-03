package com.example.rotanacion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RulesScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEDE7F6)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "📘 Reglas del juego (versión resumida)\n\n" +
                        "1️⃣ Lanza el dado de acción.\n" +
                        "2️⃣ Si sale TOMA, lanza el dado de carta y toma una del mazo.\n" +
                        "3️⃣ Si sale DER o IZQ, rota una carta del tipo que elijas.\n" +
                        "4️⃣ Puedes intercambiar cartas con las bancas.\n" +
                        "5️⃣ Ganas si tienes las tres cartas del mismo país.",
                color = Color.Black,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
            ) {
                Text("⬅️ Volver al menú", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}
