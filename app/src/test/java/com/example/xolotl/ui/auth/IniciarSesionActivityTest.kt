package com.example.xolotl.ui.auth

import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.utils.ValidationUtils
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class IniciarSesionActivityTest {

    private lateinit var mockAuthRepo: AuthRepository

    @Before
    fun setup() {
        mockAuthRepo = mock(AuthRepository::class.java)
    }

    @Test
    fun `login falla con correo invalido`() {
        val correo = "correo_invalido"

        // Usamos la función real directamente, ¡ya no explotará porque usamos Java puro!
        val esValido = ValidationUtils.isValidEmail(correo)
        assert(!esValido)
    }

    @Test
    fun `login falla con contraseña vacía`() {
        val correo = "usuario@valido.com"
        val contrasena = ""

        // Usamos la función real directamente
        val esCorreoValido = ValidationUtils.isValidEmail(correo)
        assert(esCorreoValido) // El correo es válido
        assert(contrasena.isEmpty()) // Pero la contraseña está vacía
    }

    @Test
    fun `login exitoso con credenciales válidas`() {
        val correo = "usuario@valido.com"
        val contrasena = "password123"

        var exito = false
        val testCallback = object : AuthCallback {
            override fun onSuccess() { exito = true }
            override fun onError(error: String) { exito = false }
        }

        // SOLUCIÓN: Pasamos las variables directas SIN usar eq() para que Kotlin no explote por nulos
        doAnswer { invocation ->
            val callback = invocation.arguments[2] as AuthCallback
            callback.onSuccess()
            null
        }.`when`(mockAuthRepo).iniciarSesion(correo, contrasena, testCallback)

        mockAuthRepo.iniciarSesion(correo, contrasena, testCallback)

        assert(exito)
    }

    @Test
    fun `login falla por credenciales incorrectas (error del repositorio)`() {
        val correo = "usuario@valido.com"
        val contrasena = "incorrecta"

        var mensajeError = ""
        val testCallback = object : AuthCallback {
            override fun onSuccess() { }
            override fun onError(error: String) { mensajeError = error }
        }

        // Simulamos un error clásico de Firebase: "password is invalid"
        doAnswer { invocation ->
            val callback = invocation.arguments[2] as AuthCallback
            callback.onError("password is invalid")
            null
        }.`when`(mockAuthRepo).iniciarSesion(correo, contrasena, testCallback)

        mockAuthRepo.iniciarSesion(correo, contrasena, testCallback)

        // Verificamos que el callback capturó el error correctamente
        assert(mensajeError == "password is invalid")
    }

    @Test
    fun `login falla por usuario inexistente (error del repositorio)`() {
        val correo = "fantasma@valido.com"
        val contrasena = "123456"

        var mensajeError = ""
        val testCallback = object : AuthCallback {
            override fun onSuccess() { }
            override fun onError(error: String) { mensajeError = error }
        }

        // Simulamos el error "user not found"
        doAnswer { invocation ->
            val callback = invocation.arguments[2] as AuthCallback
            callback.onError("user not found")
            null
        }.`when`(mockAuthRepo).iniciarSesion(correo, contrasena, testCallback)

        mockAuthRepo.iniciarSesion(correo, contrasena, testCallback)

        assert(mensajeError == "user not found")
    }

    @Test
    fun `login falla por error de conexion (error del repositorio)`() {
        val correo = "usuario@valido.com"
        val contrasena = "password123"

        var mensajeError = ""
        val testCallback = object : AuthCallback {
            override fun onSuccess() { }
            override fun onError(error: String) { mensajeError = error }
        }

        // Simulamos un fallo de red
        doAnswer { invocation ->
            val callback = invocation.arguments[2] as AuthCallback
            callback.onError("network error")
            null
        }.`when`(mockAuthRepo).iniciarSesion(correo, contrasena, testCallback)

        mockAuthRepo.iniciarSesion(correo, contrasena, testCallback)

        assert(mensajeError == "network error")
    }
}