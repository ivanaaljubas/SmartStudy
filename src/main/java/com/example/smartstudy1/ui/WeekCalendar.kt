package com.example.smartstudy1.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun WeekCalendar(
    daysIso: List<String>,           // "yyyy-MM-dd"
    selectedIso: String,             // "yyyy-MM-dd"
    onDaySelected: (String) -> Unit  // vrati "yyyy-MM-dd"
) {
    val dayNames = listOf("PON", "UTO", "SRI", "ČET", "PET", "SUB", "NED")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        daysIso.forEachIndexed { index, iso ->
            val isSelected = iso == selectedIso
            val dayNumber = iso.substring(8, 10)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onDaySelected(iso) }
            ) {
                Text(
                    text = dayNames.getOrNull(index) ?: "",
                    style = MaterialTheme.typography.bodySmall
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayNumber,
                        color = if (isSelected) Color.White else Color.Black,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
