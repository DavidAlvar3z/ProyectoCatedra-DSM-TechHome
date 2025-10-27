package com.techhome.models

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class User(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val edad: Int? = null,
    val sexo: String = "",
    val telefono: String = "",
    val biografia: String = "",
    val photoUrl: String? = null,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val proveedor: String = "email" // "email" o "google"
) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", "", "", "", null, "", "", "", null, 0L, "email")
}