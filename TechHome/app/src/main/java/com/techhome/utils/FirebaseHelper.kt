package com.techhome.utils

import android.content.Context
import android.content.Intent
import android.util.Patterns
import com.google.firebase.auth.FirebaseAuth
import com.techhome.activities.LoginActivity

object FirebaseHelper {

    // ==================== VALIDACIONES ====================

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

    // ==================== AUTENTICACIÓN ====================

    /**
     * Cierra la sesión del usuario actual y redirige al LoginActivity
     */
    fun logout(context: Context) {
        // Cerrar sesión en Firebase
        FirebaseAuth.getInstance().signOut()

        // Redirigir a LoginActivity y limpiar el stack de actividades
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    /**
     * Obtiene el ID del usuario actual autenticado
     */
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    /**
     * Verifica si hay un usuario autenticado
     */
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    /**
     * Obtiene el email del usuario actual
     */
    fun getCurrentUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }

    // ==================== MANEJO DE ERRORES ====================

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
            "A network error (such as timeout, interrupted connection or unreachable host) has occurred." ->
                "Error de conexión. Verifica tu internet"
            "The user account has been disabled by an administrator." ->
                "Esta cuenta ha sido deshabilitada"
            else -> "Error: ${exception?.message ?: "Desconocido"}"
        }
    }
}