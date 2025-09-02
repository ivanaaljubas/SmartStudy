package com.example.smartstudy1.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource          // ✅ DODANO
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartstudy1.R
import com.example.smartstudy1.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Composable
fun AddTaskScreen(
    onBack: () -> Unit,
    onTaskAdded: () -> Unit,
    prefillDate: String = "" // yyyy-MM-dd
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var date by remember {
        mutableStateOf(
            if (prefillDate.isNotBlank()) prefillDate
            else SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        )
    }

    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(7000, easing = LinearEasing)),
        label = "rotateAnim"
    )


    val tfColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black,
        disabledTextColor = Color.Black.copy(alpha = 0.4f),
        focusedBorderColor = Color(0xFF1F233E),
        unfocusedBorderColor = Color(0xFF9AA0B4),
        cursorColor = Color(0xFF1F233E),
        focusedLabelColor = Color(0xFF1F233E),
        unfocusedLabelColor = Color(0xFF5A6174),
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        disabledContainerColor = Color.White
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { inner ->
        Box(Modifier.fillMaxSize().padding(inner)) {
            Image(
                painter = painterResource(id = R.drawable.planets),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xCC181A34),
                                Color(0xBB23244A) // ✅ ispravan ARGB (AA RR GG BB)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Nazad",
                            tint = Color.White
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text("Dodaj novi zadatak", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(14.dp))

                Image(
                    painter = painterResource(id = R.drawable.ic_planet),
                    contentDescription = null,
                    modifier = Modifier.size(120.dp).rotate(angle)
                )

                Spacer(Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD252A3A))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        OutlinedTextField(
                            value = title, onValueChange = { title = it },
                            label = { Text("Naziv zadatka") },
                            enabled = !isLoading, modifier = Modifier.fillMaxWidth(),
                            colors = tfColors, singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = subject, onValueChange = { subject = it },
                            label = { Text("Predmet") },
                            enabled = !isLoading, modifier = Modifier.fillMaxWidth(),
                            colors = tfColors, singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = time, onValueChange = { time = it },
                            label = { Text("Vrijeme (npr. 09:00-10:30)") },
                            enabled = !isLoading, modifier = Modifier.fillMaxWidth(),
                            colors = tfColors, singleLine = true
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = date, onValueChange = { date = it },
                            label = { Text("Datum (yyyy-MM-dd)") },
                            enabled = !isLoading, modifier = Modifier.fillMaxWidth(),
                            colors = tfColors, singleLine = true
                        )

                        Spacer(Modifier.height(18.dp))
                        Button(
                            onClick = {
                                if (title.isBlank() || subject.isBlank() || time.isBlank() || date.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Sva polja su obavezna.") }
                                    return@Button
                                }

                                val user = FirebaseAuth.getInstance().currentUser
                                if (user == null) {
                                    scope.launch { snackbarHostState.showSnackbar("Moraš biti prijavljen(a).") }
                                    return@Button
                                }

                                isLoading = true
                                val uid = user.uid
                                val dbUrl = "https://smartstudyapp-c078c-default-rtdb.europe-west1.firebasedatabase.app/"
                                val ref = FirebaseDatabase.getInstance(dbUrl).getReference("tasks").child(uid)
                                val id = ref.push().key ?: UUID.randomUUID().toString()

                                val newTask = Task(
                                    id = id,
                                    title = title.trim(),
                                    subject = subject.trim(),
                                    date = "${date.trim()} ${time.trim()}",
                                    isDone = false
                                )

                                ref.child(id).setValue(newTask)
                                    .addOnCompleteListener { t ->
                                        isLoading = false
                                        if (t.isSuccessful) {
                                            scope.launch { snackbarHostState.showSnackbar("Zadatak spremljen.") }
                                            onTaskAdded()
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Greška: ${t.exception?.message ?: "nepoznata"}")
                                            }
                                        }
                                    }
                            },
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth().height(52.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Icon(Icons.Filled.Save, contentDescription = "Spremi")
                                Spacer(Modifier.width(8.dp))
                                Text("Spremi")
                            }
                        }
                    }
                }
            }
        }
    }
}
