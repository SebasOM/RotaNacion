package com.example.rotanacion.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rotanacion.R
import com.example.rotanacion.model.Player
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun VictoryScreen(
    winner: Player,
    onRestart: () -> Unit,
    onReturnToMenu: () -> Unit
) {
    // 🎬 Animaciones de entrada
    var visible by remember { mutableStateOf(false) }
    val fadeAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(1000),
        label = "fadeIn"
    )

    // ✨ Animación de zoom para las cartas
    val zoomAnim = rememberInfiniteTransition(label = "zoomInCards")
    val scale by zoomAnim.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnim"
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF311B92)),
        contentAlignment = Alignment.Center
    ) {
        // 🎊 Fondo de confeti animado
        ConfettiAnimation()

        // 🏆 Contenido principal
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(24.dp)
                .alpha(fadeAlpha)
        ) {
            // 🎉 Título
            Text(
                text = "🎉 ¡Victoria!",
                fontSize = 36.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            val nation = winner.hand.values.first().nation

            // 🌍 Mensaje principal
            Text(
                text = "${winner.name} ha ganado con las cartas de $nation 🌍",
                fontSize = 20.sp,
                color = Color(0xFFEDE7F6),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 🃏 Cartas ganadoras con animación de zoom
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .scale(scale) // Efecto de zoom suave
            ) {
                winner.hand.values.forEach { card ->
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(160.dp)
                            .background(Color(0xFF4527A0), shape = RoundedCornerShape(12.dp))
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = card.imageRes),
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .padding(4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🌎 Nombre del país
            Text(
                text = nation,
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 🔁 Botón: Jugar de nuevo
            Button(
                onClick = onRestart,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🔄 Jugar de nuevo", color = Color.White, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🏠 Botón: Volver al menú
            Button(
                onClick = onReturnToMenu,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🏠 Volver al menú", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}

/* -------------------------------------------------------------
   🎊 Fondo animado de confeti
   ------------------------------------------------------------- */
@Composable
fun ConfettiAnimation() {
    val confettiCount = 60
    val colors = listOf(
        Color(0xFFFFC107),
        Color(0xFF4CAF50),
        Color(0xFF03A9F4),
        Color(0xFFF06292),
        Color(0xFFFFEB3B),
        Color(0xFFE91E63)
    )

    val confettiList = remember {
        List(confettiCount) {
            ConfettiParticle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 8 + 4,
                color = colors.random(),
                speed = Random.nextFloat() * 200 + 100,
                offset = Random.nextFloat() * 1000
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "confettiMove")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        confettiList.forEach { confetti ->
            val newY = (confetti.y * height + (time * confetti.speed + confetti.offset)) % height
            val newX = (confetti.x * width + sin(time * 10 + confetti.offset) * 40) % width

            drawCircle(
                color = confetti.color,
                radius = confetti.radius,
                center = Offset(newX, newY)
            )
        }
    }
}

/* -------------------------------------------------------------
   🎨 Data class de partículas de confeti
   ------------------------------------------------------------- */
data class ConfettiParticle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Color,
    val speed: Float,
    val offset: Float
)



