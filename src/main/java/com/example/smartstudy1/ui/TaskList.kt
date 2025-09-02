package com.example.smartstudy1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.smartstudy1.Task
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip


@Composable
fun TaskList(
    tasks: List<Task>,
    onCheckedChange: (Task, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        tasks.forEach { task ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Checkbox(
                    checked = task.isDone,
                    onCheckedChange = { checked -> onCheckedChange(task, checked) }
                )
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
