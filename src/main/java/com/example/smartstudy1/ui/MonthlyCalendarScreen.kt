package com.example.smartstudy1.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartstudy1.R
import com.example.smartstudy1.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlinx.coroutines.launch


@Composable
fun MonthlyCalendarScreen(
    tasks: List<Task>,
    onAddTask: (String) -> Unit,
    onNavigateProfile: () -> Unit,
    onNavigateDashboard: () -> Unit,
    onNavigateCompleted: () -> Unit,
    onNavigateCourses: () -> Unit,
    onNavigateNext: () -> Unit,        // swipe LTR
    onNavigatePrev: () -> Unit,        // swipe RTL
    onStartTask: (Task) -> Unit,
    onTaskChecked: (Task, Boolean) -> Unit,
    onTaskDelete: (Task) -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val today = remember { Calendar.getInstance() }
    val (monthMatrix, monthInfo) = rememberMonthMatrix(today)

    val inFmt = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val outFmt = remember { SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()) }
    var selectedDate by remember { mutableStateOf(outFmt.format(today.time)) }

    // mapiranje datuma na zadatke
    val taskDates = remember(tasks) {
        tasks.mapNotNull { t ->
            t.date.split(" ").firstOrNull()?.let { d ->
                runCatching { outFmt.format(inFmt.parse(d)!!) }.getOrNull()
            }
        }.toSet()
    }

    // potvrda brisanja
    var taskToDelete by remember { mutableStateOf<Task?>(null) }

    // SWIPE: ulijevo -> next; udesno -> prev
    val density = LocalDensity.current
    val swipeThreshold = 80f
    var swipeConsumed by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = Color(0xFF23244A)) {
                Spacer(Modifier.height(24.dp))
                Text(
                    "Izbornik",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
                Divider(color = Color.Gray)

                DrawerMenuItem("Moj profil") { scope.launch { drawerState.close(); onNavigateProfile() } }
                DrawerMenuItem("Dodaj zadatak") { scope.launch { drawerState.close(); onAddTask(selectedDate) } }
                DrawerMenuItem("Završeni zadaci") { scope.launch { drawerState.close(); onNavigateCompleted() } }
                DrawerMenuItem("Mjesečni kalendar") { scope.launch { drawerState.close() } }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(drawerState.isOpen) {
                    detectHorizontalDragGestures(
                        onDragStart = { swipeConsumed = false },
                        onDragEnd = { swipeConsumed = true },
                        onHorizontalDrag = { change, dragAmount ->
                            if (drawerState.isOpen) return@detectHorizontalDragGestures
                            change.consumePositionChange()
                            if (swipeConsumed) return@detectHorizontalDragGestures
                            if (abs(dragAmount) > swipeThreshold) {
                                swipeConsumed = true
                                if (dragAmount < 0f) onNavigateNext() else onNavigatePrev()
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
                    .background(Brush.verticalGradient(listOf(Color(0xCC181A34), Color(0xCC23244A))))
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, contentDescription = "Izbornik", tint = Color.White)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Mjesečni kalendar", color = Color(0xFF47F4FF), fontWeight = FontWeight.Bold, fontSize = 22.sp)
                }

                Spacer(Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED").forEach {
                        Text(
                            it,
                            color = Color(0xFFB4B4CC),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f).padding(2.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    monthMatrix.forEach { week ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            week.forEach { dayStr ->
                                if (dayStr.isNotEmpty()) {
                                    val dayInt = dayStr.toInt()
                                    val fullDate = "%02d.%02d.%04d".format(dayInt, monthInfo.month, monthInfo.year)
                                    val selected = selectedDate == fullDate
                                    val hasTask = taskDates.contains(fullDate)

                                    Box(
                                        modifier = Modifier
                                            .weight(1f).aspectRatio(1f).padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    selected -> Color(0xFF47F4FF)
                                                    hasTask -> Color(0xFF313396)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .clickable { selectedDate = fullDate },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                dayStr,
                                                color = if (selected || hasTask) Color.White else Color(0xFFE0E0E0),
                                                fontSize = 18.sp,
                                                fontWeight = if (selected || hasTask) FontWeight.Bold else FontWeight.Medium
                                            )
                                            if (hasTask) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_planet),
                                                    contentDescription = null,
                                                    tint = Color.Unspecified,
                                                    modifier = Modifier.size(15.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                val selectedTasks = remember(tasks, selectedDate) {
                    val inF = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val outF = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    tasks.filter { task ->
                        val dateString = task.date.split(" ").firstOrNull()
                        if (dateString == null) false else runCatching {
                            outF.format(inF.parse(dateString)!!) == selectedDate
                        }.getOrDefault(false)
                    }.filter { !it.isDone }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xDD25274B)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Text("Zadaci za dan: $selectedDate", color = Color.White, fontWeight = FontWeight.Medium, fontSize = 17.sp)

                        if (selectedTasks.isEmpty()) {
                            Text("Nema zadataka za ovaj dan!", color = Color(0xFFB4B4CC))
                        } else {
                            selectedTasks.forEach { task ->
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 6.dp)) {
                                    Checkbox(
                                        checked = task.isDone,
                                        onCheckedChange = { checked -> onTaskChecked(task, checked) },
                                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF47F4FF), uncheckedColor = Color(0xFFB4B4CC))
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(task.title, color = Color.White, fontSize = 16.sp)
                                        val timeRange = task.date.split(" ").getOrNull(1) ?: ""
                                        if (timeRange.isNotBlank()) Text(timeRange, color = Color(0xFFB4B4CC), fontSize = 12.sp)
                                    }
                                    Button(onClick = { onStartTask(task) }) { Text("Započni") }

                                    // 🗑️ Ikona brisanja
                                    IconButton(onClick = { taskToDelete = task }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Obriši", tint = Color(0xFFFF6B6B))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //  Potvrda brisanja
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


private data class MonthInfo(val month: Int, val year: Int)


@Composable
private fun rememberMonthMatrix(baseCal: Calendar): Pair<List<List<String>>, MonthInfo> {
    val keyMonth = baseCal.get(Calendar.MONTH)
    val keyYear = baseCal.get(Calendar.YEAR)
    return remember(keyMonth, keyYear) {
        val cal = (baseCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
        val firstDayDow = cal.get(Calendar.DAY_OF_WEEK) // 1=ned, 2=pon...
        val leadingBlanks = ((firstDayDow + 5) % 7)      // pon=0
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val rows = mutableListOf<List<String>>()
        var day = 1
        for (r in 0 until 6) {
            val week = MutableList(7) { "" }
            for (c in 0 until 7) {
                if (r == 0 && c < leadingBlanks) continue
                if (day > daysInMonth) break
                week[c] = day.toString()
                day++
            }
            rows.add(week)
            if (day > daysInMonth) break
        }
        rows to MonthInfo(month = cal.get(Calendar.MONTH) + 1, year = cal.get(Calendar.YEAR))
    }
}
