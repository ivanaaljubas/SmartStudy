package com.example.smartstudy1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.*
import com.example.smartstudy1.R
import com.example.smartstudy1.auth.AuthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun LoginScreen(
    onBack: () -> Unit,
    onLoginSuccess: (FirebaseUser) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

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

        Column(
            Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedAlien()

            Spacer(Modifier.height(16.dp))

            Text(
                "Prijava",
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = MaterialTheme.typography.headlineSmall.fontWeight,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail", color = Color.White) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    cursorColor = Color.White
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Lozinka", color = Color.White) },
                textStyle = LocalTextStyle.current.copy(color = Color.White),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.White,
                    cursorColor = Color.White
                ),
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val icon = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(icon, contentDescription = null, tint = Color.White)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            )

            Spacer(Modifier.height(16.dp))

            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        error = "Molimo ispuni sve podatke."
                        return@Button
                    }

                    isLoading = true
                    error = null

                    AuthManager.loginUser(
                        email.trim(),
                        password,
                        onSuccess = {
                            isLoading = false
                            val user = FirebaseAuth.getInstance().currentUser
                            if (user != null) {
                                onLoginSuccess(user)
                            } else {
                                error = "Greška: korisnik nije pronađen."
                            }
                        },
                        onError = {
                            isLoading = false
                            error = it
                        }
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                else Text("Prijavi se", color = Color.White)
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Natrag", color = Color.White)
            }
        }
    }
}

@Composable
fun AnimatedAlien(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("alien.json"))
    val progress by animateLottieCompositionAsState(
        composition,
        iterations = LottieConstants.IterateForever
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.height(180.dp).width(180.dp)
    )
}
