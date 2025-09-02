@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.smartstudy1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartstudy1.R
import com.example.smartstudy1.Task
import kotlin.math.abs

@Composable
fun CompletedTasksScreen(
    completedTasks: List<Task>,
    onBack: () -> Unit
) {
    val swipeThreshold = 80f
    var consumed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { consumed = false },
                    onDragEnd = { consumed = true },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consumePositionChange()
                        if (consumed) return@detectHorizontalDragGestures
                        if (abs(dragAmount) > swipeThreshold && dragAmount > 0f) {
                            consumed = true
                            onBack()
                        }
                    }
                )
            }
    ) {
        // pozadina + overlay (kao ostali screenovi)
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

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Završeni zadaci",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Nazad",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White
                    )
                )
            }
        ) { innerPadding ->
            if (completedTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Nema završenih zadataka.", color = Color(0xFFB4B4CC))
                }
            } else {
                LazyColumn(
                    contentPadding = innerPadding,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(completedTasks) { task ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xDD25274B)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // MALA IKONA PLANETA (bez tintanja, koristi stvarni asset)
                                Image(
                                    painter = painterResource(id = R.drawable.ic_planet),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    contentScale = ContentScale.Fit
                                )

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = task.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (task.subject.isNotBlank()) {
                                        Text(
                                            text = "Predmet: ${task.subject}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFFB4B4CC)
                                        )
                                    }
                                    if (task.date.isNotBlank()) {
                                        Text(
                                            text = "Datum: ${task.date}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFB4B4CC)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
