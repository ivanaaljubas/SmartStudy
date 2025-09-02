package com.example.smartstudy1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartstudy1.R
import kotlinx.coroutines.delay

@Composable
fun StudySessionScreen(
    title: String,
    durationMinutes: Int,
    onFinished: () -> Unit,
    onGiveUp: () -> Unit
) {
    val totalSeconds = (durationMinutes.coerceAtLeast(1)) * 60
    var elapsed by remember { mutableStateOf(0) }
    val remaining = (totalSeconds - elapsed).coerceAtLeast(0)
    val progress = if (totalSeconds == 0) 1f else elapsed.toFloat() / totalSeconds


    val planetScale = 1f + 0.2f * progress

    LaunchedEffect(totalSeconds) {
        while (elapsed < totalSeconds) {
            delay(1000)
            elapsed++
        }
        onFinished()
    }

    Box(Modifier.fillMaxSize()) {
        // Pozadinska slika + tamni overlay
        Image(
            painter = painterResource(id = R.drawable.planets),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC181A34), Color(0xBB23244A))
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Uči: $title",
                color = Color(0xFF47F4FF),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
            Spacer(Modifier.height(18.dp))

            // Jedna slika planete, kontrolirane veličine
            Image(
                painter = painterResource(id = R.drawable.ic_planet),
                contentDescription = null,
                modifier = Modifier.size(180.dp).scale(planetScale)
            )

            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = Color(0x3325274B))) {
                Column(
                    Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Preostalo vrijeme", color = Color.White.copy(alpha = 0.8f))
                    Text(
                        formatHMS(remaining),
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            val stage = when {
                progress >= 0.95f -> "🌍 Planeta je formirana!"
                progress >= 0.66f -> "🪐 Atmosfera se stvara…"
                progress >= 0.33f -> "☄️ Zrnice prašine se spajaju…"
                else -> "✨ Početak formiranja planete…"
            }
            Text(stage, color = Color.White.copy(alpha = 0.9f))

            Spacer(Modifier.weight(1f))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onGiveUp,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF394264))
                ) { Text("Odustani") }

                Button(
                    onClick = onFinished,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF47F4FF))
                ) { Text("Završeno") }
            }
        }
    }
}

private fun formatHMS(sec: Int): String {
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
