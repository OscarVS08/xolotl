package com.example.xolotl.utils

import org.junit.Assert.*
import org.junit.Test

class ValidationUtilsTest {

    @Test
    fun `correo válido pasa la validación`() {
        val correo = "usuario@valido.com"
        assertTrue(ValidationUtils.isValidEmail(correo))
    }

    @Test
    fun `correo sin arroba falla la validación`() {
        val correo = "usuariovalido.com"
        assertFalse(ValidationUtils.isValidEmail(correo))
    }

    @Test
    fun `correo vacío falla la validación`() {
        val correo = ""
        assertFalse(ValidationUtils.isValidEmail(correo))
    }

    @Test
    fun `contraseña válida pasa la validación`() {
        val contrasena = "password123"
        assertTrue(ValidationUtils.isStrongPassword(contrasena))
    }

    @Test
    fun `contraseña muy corta falla la validación`() {
        val contrasena = "123"
        assertFalse(ValidationUtils.isStrongPassword(contrasena))
    }

    @Test
    fun `contraseña vacía falla la validación`() {
        val contrasena = ""
        assertFalse(ValidationUtils.isStrongPassword(contrasena))
    }
}
