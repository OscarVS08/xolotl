package com.example.xolotl.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthCallbackTest {

    @Test
    fun `AuthCallback se puede implementar y sus metodos transmiten la informacion correctamente`() {
        // 1. Variables para capturar lo que haga la interfaz
        var successFueLlamado = false
        var mensajeDeErrorCapturado = ""

        // 2. Creamos una implementación "ficticia" (Mock manual) de la interfaz
        val callbackDePrueba = object : AuthCallback {
            override fun onSuccess() {
                successFueLlamado = true
            }

            override fun onError(errorMessage: String) {
                mensajeDeErrorCapturado = errorMessage
            }
        }

        // 3. Simulamos que el Repositorio de Firebase fue exitoso
        callbackDePrueba.onSuccess()
        assertTrue("El método onSuccess debería cambiar la variable a true", successFueLlamado)

        // 4. Simulamos que el Repositorio de Firebase falló
        val errorSimulado = "Error de conexión a internet"
        callbackDePrueba.onError(errorSimulado)

        assertEquals(
            "El método onError debería transmitir el mensaje exacto",
            errorSimulado,
            mensajeDeErrorCapturado
        )
    }
}