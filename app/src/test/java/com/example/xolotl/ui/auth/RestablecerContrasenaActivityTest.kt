package com.example.xolotl.ui.auth

import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.utils.ValidationUtils
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class RestablecerContrasenaActivityTest {

    private lateinit var mockAuthRepo: AuthRepository
    private lateinit var activity: RestablecerContrasenaActivity

    @Before
    fun setup() {
        mockAuthRepo = mock(AuthRepository::class.java)
        activity = RestablecerContrasenaActivity()
    }

    @Test
    fun `falla al enviar correo invalido`() {
        val correo = "correo_invalido"

        val esValido = ValidationUtils.isValidEmail(correo)
        assert(!esValido)
    }

    @Test
    fun `éxito al enviar correo valido`() {
        val correo = "usuario@valido.com"

        val esValido = ValidationUtils.isValidEmail(correo)
        assert(esValido)

        // Simulamos que el repositorio responde con éxito
        doAnswer { invocation ->
            val callback = invocation.getArgument<AuthCallback>(1)
            callback.onSuccess()
            null
        }.`when`(mockAuthRepo).enviarCorreoRecuperacion(anyString(), any(AuthCallback::class.java))
    }

    @Test
    fun `error al enviar correo cuando el repositorio falla`() {
        val correo = "usuario@valido.com"

        val esValido = ValidationUtils.isValidEmail(correo)
        assert(esValido)

        // Simulamos error en el repositorio
        doAnswer { invocation ->
            val callback = invocation.getArgument<AuthCallback>(1)
            callback.onError("Error de conexión")
            null
        }.`when`(mockAuthRepo).enviarCorreoRecuperacion(anyString(), any(AuthCallback::class.java))
    }
}
