package com.example.smartstudy1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.smartstudy1.R

@Composable
fun WelcomeLottieLarge() {
    val composition = rememberLottieComposition(LottieCompositionSpec.Asset("education_animacija.json")).value
    LottieAnimation(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        modifier = Modifier
            .size(240.dp)
            .padding(top = 16.dp, bottom = 0.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Pozadinska slika
        Image(
            painter = painterResource(id = R.drawable.planets),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Overlay
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF4A3CE7).copy(alpha = 0.24f),
                            Color(0xFF23244A),
                            Color.Black.copy(alpha = 0.68f)
                        ),
                        center = Offset(300f, 290f),
                        radius = 650f
                    )
                )
        )

        // Sadržaj
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 30.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Naslov
            Spacer(Modifier.height(14.dp))
            Text(
                text = "Smart",
                style = TextStyle(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF47F4FF),
                    shadow = Shadow(
                        color = Color(0xAA47F4FF),
                        offset = androidx.compose.ui.geometry.Offset(0f, 8f),
                        blurRadius = 22f
                    ),
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
            )
            Text(
                text = "Study",
                style = TextStyle(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF7BFFB5),
                    shadow = Shadow(
                        color = Color(0xAA7BFFB5),
                        offset = androidx.compose.ui.geometry.Offset(0f, 8f),
                        blurRadius = 22f
                    ),
                    letterSpacing = 8.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.offset(y = (-16).dp)
            )

            // Lottie + gumbi
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                WelcomeLottieLarge()
                Spacer(Modifier.height(6.dp))
                Text(
                    "🚀 Ostvari svoju galaksiju znanja!",
                    fontWeight = FontWeight.Medium,
                    fontSize = 19.sp,
                    color = Color(0xFF47F4FF),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))

                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3CE7))
                ) {
                    Text("Prijavi se", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF23244A))
                ) {
                    Text("Registriraj se", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                "Registriraj se i koristi sve mogućnosti aplikacije.",
                color = Color(0xFFB4B4CC),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )
        }
    }
}
