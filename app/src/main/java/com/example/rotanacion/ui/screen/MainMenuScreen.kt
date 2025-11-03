package com.example.rotanacion.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rotanacion.R

@Composable
fun MainMenuScreen(
    onStartGame: () -> Unit,
    onShowRules: () -> Unit,
    onExit: () -> Unit
) {
    // 🔹 Animación de aparición suave
    var visible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, label = "fade")

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF311B92)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(24.dp)
                .alpha(alpha)
        ) {
            // ---------- Logo o Imagen principal ----------
            Image(
                painter = painterResource(id = R.drawable.logo_rotanacion), // Usa tu logo aquí
                contentDescription = "Logo RotaNación",
                modifier = Modifier
                    .size(160.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "RotaNación",
                fontSize = 36.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(40.dp))

            // ---------- Botón JUGAR ----------
            Button(
                onClick = onStartGame,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2)),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🎮 Jugar", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Botón REGLAS ----------
            Button(
                onClick = onShowRules,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9575CD)),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("📘 Reglas", fontSize = 18.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Botón SALIR ----------
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(55.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("🚪 Salir", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
