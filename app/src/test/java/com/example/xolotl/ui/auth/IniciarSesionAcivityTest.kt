package com.example.xolotl.ui.auth

import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.utils.ValidationUtils
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class IniciarSesionAcivityTest {

    private lateinit var mockAuthRepo: AuthRepository

    @Before
    fun setup() {
        mockAuthRepo = mock(AuthRepository::class.java)
    }

    @Test
    fun `login falla con correo invalido`() {
        val correo = "correo_invalido"
        val contrasena = "123456"

        val esValido = ValidationUtils.isValidEmail(correo)
        assert(!esValido)
    }

    @Test
    fun `login falla con contraseña vacía`() {
        val correo = "correo@valido.com"
        val contrasena = ""

        val esCorreoValido = ValidationUtils.isValidEmail(correo)
        assert(esCorreoValido)
        assert(contrasena.isEmpty())
    }

    @Test
    fun `login exitoso con credenciales válidas`() {
        val correo = "usuario@valido.com"
        val contrasena = "password123"

        val esCorreoValido = ValidationUtils.isValidEmail(correo)
        assert(esCorreoValido)
        assert(contrasena.isNotEmpty())

        // --- Simulamos éxito en el callback ---
        doAnswer { invocation ->
            val callback = invocation.getArgument<AuthCallback>(2)
            callback.onSuccess()
            null
        }.`when`(mockAuthRepo).iniciarSesion(anyString(), anyString(), any(AuthCallback::class.java))
    }

    @Test
    fun `login falla con error del repositorio`() {
        val correo = "usuario@valido.com"
        val contrasena = "incorrecta"

        doAnswer { invocation ->
            val callback = invocation.getArgument<AuthCallback>(2)
            callback.onError("Credenciales inválidas")
            null
        }.`when`(mockAuthRepo).iniciarSesion(anyString(), anyString(), any(AuthCallback::class.java))
    }
}
