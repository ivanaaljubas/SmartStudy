package com.example.smartstudy1.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartstudy1.Task
import com.google.firebase.database.*

@Composable
fun TaskListScreen(
    userId: String,
    onAddTaskClick: () -> Unit = {}
) {
    var tasks by remember { mutableStateOf(listOf<Task>()) }

    val db = FirebaseDatabase.getInstance(
        "https://smartstudyapp-c078c-default-rtdb.europe-west1.firebasedatabase.app/"
    ).getReference("tasks").child(userId)

    DisposableEffect(userId) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val all = mutableListOf<Task>()
                snapshot.children.forEach { ch ->
                    val key = ch.key ?: return@forEach
                    val title = ch.child("title").getValue(String::class.java) ?: ""
                    val subject = ch.child("subject").getValue(String::class.java) ?: ""
                    val date = ch.child("date").getValue(String::class.java) ?: ""
                    val isDoneDb = ch.child("isDone").getValue(Boolean::class.java) ?: false
                    val doneDb   = ch.child("done").getValue(Boolean::class.java) ?: false
                    val effectiveDone = isDoneDb || doneDb
                    if (isDoneDb != effectiveDone || doneDb != effectiveDone) {
                        db.child(key).updateChildren(mapOf("isDone" to effectiveDone, "done" to effectiveDone))
                    }
                    all += Task(id = key, title = title, subject = subject, date = date, isDone = effectiveDone)
                }

                fun contentKey(t: Task) =
                    "${t.title.trim().lowercase()}|${t.subject.trim().lowercase()}|${t.date.trim()}"
                val groups = all.groupBy(::contentKey)

                val merged = mutableListOf<Task>()
                for ((_, list) in groups) {
                    val anyDone = list.any { it.isDone }
                    merged += list.first().copy(isDone = anyDone)
                    list.forEach { db.child(it.id).updateChildren(mapOf("isDone" to anyDone, "done" to anyDone)) }
                }
                tasks = merged.filter { !it.isDone } // ✔️ prikaz samo neriješenih
            }
            override fun onCancelled(error: DatabaseError) { }
        }
        db.addValueEventListener(listener)
        onDispose { db.removeEventListener(listener) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFB2FEFA), Color(0xFFE0C3FC), Color(0xFFF9F9F9))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {
            Text("Moji zadaci", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(14.dp))

            if (tasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nema zadataka za prikaz.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    items(tasks) { task ->
                        ModernTaskCard(
                            task = task,
                            onCheckedChange = { t, checked ->
                                db.child(t.id).updateChildren(mapOf("isDone" to checked, "done" to checked))
                            }
                        )
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onAddTaskClick,
            containerColor = Color(0xFF22577A),
            contentColor = Color.White,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) { Icon(Icons.Default.Edit, contentDescription = "Dodaj zadatak") }
    }
}

@Composable
fun ModernTaskCard(
    task: Task,
    onCheckedChange: (Task, Boolean) -> Unit = { _, _ -> }
) {
    val cardColor by animateColorAsState(
        if (task.isDone) Color(0xFFB8FFD9) else Color.White, label = "cardColor"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(cardColor)
            .clickable { onCheckedChange(task, !task.isDone) },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (task.isDone) Color(0xFF38A3A5) else Color(0xFFE3F0FF)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (task.isDone) Icons.Default.Check else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title)
                if (task.subject.isNotBlank()) Text(task.subject, fontSize = 14.sp)
                Text(task.date, fontSize = 13.sp)
            }
        }
    }
}
