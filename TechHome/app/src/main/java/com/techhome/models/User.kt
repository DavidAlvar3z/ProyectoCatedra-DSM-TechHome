package com.techhome.models

data class User(
    val uid: String = "",
    val nombre: String = "",
    val apellido: String = "",
    val email: String = "",
    val fechaRegistro: Long = 0
)