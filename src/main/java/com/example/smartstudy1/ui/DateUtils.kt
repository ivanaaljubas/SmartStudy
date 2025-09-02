package com.example.smartstudy1.ui

import java.text.SimpleDateFormat
import java.util.*

fun getThisWeekDates(): List<String> {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    // Početak tjedna (ponedjeljak)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    return (0..6).map {
        val date = sdf.format(calendar.time)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        date
    }
}

fun todayAsString(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return sdf.format(Date())
}
