package com.example.smartstudy1

@com.google.firebase.database.IgnoreExtraProperties
data class Task(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val date: String = "",     // "yyyy-MM-dd HH:mm-HH:mm"
    val isDone: Boolean = false
)
