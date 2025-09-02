package com.example.smartstudy1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartstudy1.R

@Composable
fun ProfileScreen(
    ime: String?,
    email: String?,
    brojZavršenih: Int,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Pozadina
        Image(
            painter = painterResource(id = R.drawable.planets),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Tamni overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xCC181A34), Color(0xBB23244A))
                    )
                )
        )

        // Sadržaj za prijavljenog korisnika
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Moj profil",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(32.dp))

            // Profilni "avatar" sa inicijalom
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF47F4FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (ime ?: "?").take(1).uppercase(),
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                text = ime ?: "-",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = email ?: "",
                color = Color(0xFFB4B4CC),
                fontSize = 16.sp
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = "Završeni zadaci: $brojZavršenih",
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onBack,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("Natrag") }

                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Odjavi se", color = Color.White) }
            }
        }
    }
}
