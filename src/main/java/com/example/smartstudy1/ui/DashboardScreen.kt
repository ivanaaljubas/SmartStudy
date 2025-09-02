@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.smartstudy1.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartstudy1.R
import com.example.smartstudy1.Task
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.TextStyle
import androidx.compose.material3.HorizontalDivider


@Composable
fun DashboardScreen(
    imeKorisnika: String,
    datumiIsoUTjednu: List<String>,   // "yyyy-MM-dd"
    selektiraniDatumIso: String,
    zadaciZaDanas: List<Task>,
    userId: String,
    onDanChange: (String) -> Unit,
    naDodajZadatak: () -> Unit,
    naPregledZavršenih: () -> Unit,
    naMojProfil: () -> Unit,
    naMjesecniKalendar: () -> Unit,
    onStartTask: (Task) -> Unit = {},
    onTaskChecked: (Task, Boolean) -> Unit = { _, _ -> },
    onTaskDelete: (Task) -> Unit = {},
    onLogout: () -> Unit = {},

    // ukupni brojevi za napredak/planete
    completedTotal: Int = -1,
    totalTasksAll: Int = -1
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val scroll = rememberScrollState()

    var selectedIso by remember { mutableStateOf(selektiraniDatumIso) }

    val tasksForSelectedDay = remember(zadaciZaDanas, selectedIso) {
        zadaciZaDanas.filter {
            it.date.split(" ").firstOrNull() == selectedIso && !it.isDone
        }
    }

    val doneCount = if (completedTotal >= 0) completedTotal else zadaciZaDanas.count { it.isDone }
    val totalCount = if (totalTasksAll >= 0) totalTasksAll else zadaciZaDanas.size
    val progress = if (totalCount > 0) (doneCount.toFloat() / totalCount).coerceIn(0f, 1f) else 0f

    val orbit = rememberInfiniteTransition()
    val angle by orbit.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(12000, easing = LinearEasing))
    )

    val edgeGuardPx = with(density) { 24.dp.toPx() }
    val swipeThreshold = 80f
    var swipeConsumed by remember { mutableStateOf(false) }
    var startX by remember { mutableStateOf(0f) }

    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFF23244A)) {
                Spacer(Modifier.height(24.dp))
                NeonTitle("Smart Study", fontSize = 22.sp)
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(color = Color(0x33FFFFFF))
                DrawerMenuItem("Moj profil") { naMojProfil(); scope.launch { drawerState.close() } }
                DrawerMenuItem("Dodaj zadatak") { naDodajZadatak(); scope.launch { drawerState.close() } }
                DrawerMenuItem("Završeni zadaci") { naPregledZavršenih(); scope.launch { drawerState.close() } }
                DrawerMenuItem("Mjesečni kalendar") { naMjesecniKalendar(); scope.launch { drawerState.close() } }
                DrawerMenuItem("Odjava") { onLogout(); scope.launch { drawerState.close() } }
            }
        }
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset -> swipeConsumed = false; startX = offset.x },
                        onDragEnd = { swipeConsumed = true },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consumePositionChange()
                            if (swipeConsumed) return@detectHorizontalDragGestures
                            if (startX < edgeGuardPx) return@detectHorizontalDragGestures
                            if (abs(dragAmount) > swipeThreshold && dragAmount < 0f) {
                                swipeConsumed = true
                                naMjesecniKalendar()
                            }
                        }
                    )
                }
        ) {
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
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(vertical = 36.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Izbornik", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    NeonTitle("Smart Study", fontSize = 26.sp)
                }

                Spacer(Modifier.height(10.dp))
                NeonSubTitle("Pozdrav, $imeKorisnika! 👋")

                Spacer(Modifier.height(14.dp))
                WeekCalendar(
                    daysIso = datumiIsoUTjednu,
                    selectedIso = selectedIso,
                    onDaySelected = { selectedIso = it; onDanChange(it) }
                )

                Spacer(Modifier.height(20.dp))

                Card(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD25274B))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        NeonSubTitle("Zadaci za dan")
                        Spacer(Modifier.height(7.dp))

                        if (tasksForSelectedDay.isEmpty()) {
                            Text("Nema zadataka za ovaj dan!", color = Color(0xFFB4B4CC))
                        } else {
                            tasksForSelectedDay.forEach { task ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                ) {
                                    Checkbox(
                                        checked = task.isDone,
                                        onCheckedChange = { checked -> onTaskChecked(task, checked) },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = Color(0xFF47F4FF),
                                            uncheckedColor = Color(0xFFB4B4CC)
                                        )
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(task.title, color = Color.White, fontSize = 16.sp)
                                        val timeRange = task.date.split(" ").getOrNull(1) ?: ""
                                        if (timeRange.isNotBlank())
                                            Text(timeRange, color = Color(0xFFB4B4CC), fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = { onStartTask(task) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3CE7))
                                    ) { Text("Započni") }

                                    IconButton(
                                        onClick = { taskToDelete = task },
                                        modifier = Modifier.padding(start = 6.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Obriši", tint = Color(0xFFFF6B6B))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                NeonSubTitle("Napredak")
                Spacer(Modifier.height(6.dp))
                Text("Završeno zadataka ukupno: $doneCount / $totalCount", color = Color.White, fontSize = 15.sp)

                LinearProgressIndicator(
                    progress = progress,
                    color = Color(0xFF47F4FF),
                    trackColor = Color(0xFF23244A),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .padding(vertical = 8.dp)
                )

                Card(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0x3325274B))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .padding(top = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val radius = 72.dp
                        val radiusPx = with(density) { radius.toPx() }
                        val n = doneCount.coerceAtMost(48)

                        Image(
                            painter = painterResource(id = R.drawable.ic_planet),
                            contentDescription = null,
                            modifier = Modifier
                                .size(42.dp)
                                .align(Alignment.Center)
                                .rotate(angle / 8f),
                            contentScale = ContentScale.Fit
                        )

                        if (n == 0) {
                            Text("Još nema planeta – riješi prvi zadatak! ✨", color = Color(0xFFB4B4CC))
                        } else {
                            repeat(n) { i ->
                                val step = 360f / n
                                val aDeg = angle + i * step
                                val rad = aDeg * (PI / 180f)
                                val x = (cos(rad) * radiusPx).toFloat()
                                val y = (sin(rad) * radiusPx).toFloat()
                                val xDp = with(density) { x.toDp() }
                                val yDp = with(density) { y.toDp() }

                                Image(
                                    painter = painterResource(id = R.drawable.ic_planet),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .offset(x = xDp, y = yDp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = naDodajZadatak,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3CE7)),
                        modifier = Modifier.weight(1f)
                    ) { Text("Dodaj zadatak", color = Color.White) }

                    OutlinedButton(
                        onClick = naPregledZavršenih,
                        border = ButtonDefaults.outlinedButtonBorder,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) { Text("Završeni zadaci") }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }

    // potvrda brisanja
    val toDelete = taskToDelete
    if (toDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            confirmButton = {
                TextButton(onClick = { onTaskDelete(toDelete); taskToDelete = null }) {
                    Text("Obriši", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Odustani") }
            },
            title = { Text("Obriši zadatak?") },
            text = { Text("Ova akcija će ukloniti sve duplikate tog zadatka.") }
        )
    }
}

@Composable
private fun DrawerMenuItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 20.dp)
            .clickable(onClick = onClick)
    )
}


@Composable
private fun NeonTitle(text: String, fontSize: androidx.compose.ui.unit.TextUnit) {
    Text(
        text = text,
        color = Color(0xFF9CF6DC),
        fontWeight = FontWeight.ExtraBold,
        fontSize = fontSize,
        letterSpacing = 1.sp,

        style = TextStyle(
            shadow = Shadow(
                color = Color(0x8039FFE6),
                offset = Offset(0f, 0f),
                blurRadius = 18f
            )
        )
    )
}

@Composable
private fun NeonSubTitle(text: String) {
    Text(
        text = text,
        color = Color(0xFF9CF6DC),
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        style = TextStyle(
            shadow = Shadow(
                color = Color(0x4039FFE6),
                offset = Offset(0f, 0f),
                blurRadius = 10f
            )
        )
    )
}

