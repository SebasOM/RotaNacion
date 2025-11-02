package com.example.rotanacion.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rotanacion.model.Player
import com.example.rotanacion.model.Card

@Composable
fun VictoryScreen(
    winner: Player,
    onRestart: () -> Unit
) {
    // 🔹 Animación de resplandor
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "alphaAnim"
    )

    // 🔹 Fondo y contenido
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF311B92).copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(20.dp)
                .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "🏆 ¡Victoria!",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF512DA8)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "El jugador ${winner.name} ha ganado con ${winner.hand.values.first().nation}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 🔹 Mostrar las cartas ganadoras
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                winner.hand.values.forEach { card: Card ->
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
                            .background(Color.White.copy(alpha = glowAlpha)),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFD1C4E9))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = card.imageRes),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.type.name,
                                fontSize = 12.sp,
                                color = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    onRestart()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(60.dp)
            ) {
                Text("🔁 Nueva partida", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
