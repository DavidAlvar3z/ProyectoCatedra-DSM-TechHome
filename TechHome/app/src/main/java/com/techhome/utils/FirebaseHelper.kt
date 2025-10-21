package com.techhome.utils

import android.util.Patterns

object FirebaseHelper {

    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun validateLoginInput(email: String, password: String): Boolean {
        return email.isNotEmpty() && password.isNotEmpty()
    }

    fun validateRegisterInput(nombre: String, apellido: String, email: String, password: String): Boolean {
        return nombre.isNotEmpty() && apellido.isNotEmpty() &&
                email.isNotEmpty() && password.isNotEmpty()
    }

    fun getErrorMessage(exception: Exception?): String {
        return when (exception?.message) {
            "The email address is already in use by another account." ->
                "Este correo ya está registrado"
            "The password is invalid or the user does not have a password." ->
                "Contraseña incorrecta"
            "There is no user record corresponding to this identifier." ->
                "Usuario no encontrado"
            "The email address is badly formatted." ->
                "Formato de correo inválido"
            else -> "Error: ${exception?.message ?: "Desconocido"}"
        }
    }
}