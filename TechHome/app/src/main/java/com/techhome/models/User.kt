package com.techhome.models

data class User(
    val email: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val edad: Int? = null,
    val sexo: String = "",
    val telefono: String = "",
    val biografia: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)