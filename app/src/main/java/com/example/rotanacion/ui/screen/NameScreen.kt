package com.example.rotanacion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

@Composable
fun NameScreen(onConfirm: (String) -> Unit) {
    var playerName by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Ingresa tu nombre:",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            TextField(
                value = playerName,
                onValueChange = { playerName = it },
                label = { Text("Tu nombre") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color(0xFF7E57C2),
                    unfocusedIndicatorColor = Color.Gray
                )
            )

            Button(
                onClick = { onConfirm(playerName) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
            ) {
                Text("Confirmar", color = Color.White, fontSize = 18.sp)
            }
        }
    }
}
