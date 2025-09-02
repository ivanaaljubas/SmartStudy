package com.example.smartstudy1.auth

import com.google.firebase.auth.FirebaseAuth

object AuthManager {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun registerUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Greška pri registraciji") }
    }

    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Greška pri prijavi") }
    }
}
