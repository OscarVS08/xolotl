package com.example.xolotl.utils

import android.util.Patterns

object ValidationUtils {

    // --- EMAIL ---
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // --- CURP ---
    fun isValidCURP(curp: String): Boolean {
        val regex = Regex("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]{2}$", RegexOption.IGNORE_CASE)
        return regex.matches(curp)
    }

    // --- NOMBRE / APELLIDOS ---
    fun isValidName(name: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{1,50}$")
        return regex.matches(name)
    }

    // --- TELÉFONO ---
    fun isValidPhone(phone: String): Boolean {
        val regex = Regex("^\\d{10}$")
        return regex.matches(phone)
    }

    // --- CÓDIGO POSTAL ---
    fun isValidPostalCode(cp: String): Boolean {
        val regex = Regex("^\\d{5}$")
        return regex.matches(cp)
    }

    // --- CAMPOS NO VACÍOS ---
    fun isNotEmpty(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }

    // --- CONTRASEÑA FUERTE ---
    fun isStrongPassword(password: String): Boolean {
        // Debe tener al menos: 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial
        val regex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!¿?*._-]).{8,}$")
        return regex.matches(password)
    }

    // --- CALLE / NÚMERO / COLONIA / ALCALDÍA ---
    fun isValidAddressField(field: String): Boolean {
        return field.isNotBlank() && field.length <= 50
    }
}
