package com.example.xolotl.ui.auth

import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.utils.ValidationUtils
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class RestablecerContrasenaActivityTest {

    private lateinit var mockAuthRepo: AuthRepository

    @Before
    fun setup() {
        // Inicializamos nuestro Repositorio falso (Mock) sin instanciar la Actividad
        mockAuthRepo = mock(AuthRepository::class.java)
    }

    // ==========================================
    // VALIDACIONES DE FORMULARIO (Lógica pura)
    // ==========================================

    @Test
    fun `validacion falla con correo vacio o en blanco`() {
        // Simulamos cuando el usuario le da click al botón sin escribir nada
        assert(!ValidationUtils.isValidEmail(""))
        assert(!ValidationUtils.isValidEmail("   "))
    }

    @Test
    fun `validacion falla con correo sin formato correcto`() {
        // Casos que deben activar la alerta de "Formato inválido"
        assert(!ValidationUtils.isValidEmail("correo_invalido"))
        assert(!ValidationUtils.isValidEmail("usuario@.com"))
        assert(!ValidationUtils.isValidEmail("@dominio.com"))
    }

    @Test
    fun `validacion exitosa con correo valido`() {
        // Casos que deben pasar la validación
        assert(ValidationUtils.isValidEmail("usuario@valido.com"))
        assert(ValidationUtils.isValidEmail("nombre.apellido@empresa.com.mx"))
    }

    // ==========================================
    // PRUEBAS DE REPOSITORIO (Manejo de Firebase)
    // ==========================================

    @Test
    fun `enviar correo exitoso llama al callback onSuccess`() {
        val correo = "usuario@valido.com"
        var exito = false

        val testCallback = object : AuthCallback {
            override fun onSuccess() { exito = true }
            override fun onError(error: String) { exito = false }
        }

        // Pasamos variables exactas, sin eq() ni any()
        doAnswer { invocation ->
            val callback = invocation.arguments[1] as AuthCallback
            callback.onSuccess()
            null
        }.`when`(mockAuthRepo).enviarCorreoRecuperacion(correo, testCallback)

        mockAuthRepo.enviarCorreoRecuperacion(correo, testCallback)

        assert(exito)
    }

    @Test
    fun `falla por usuario no encontrado en Firebase y notifica al callback`() {
        val correo = "fantasma@valido.com"
        var mensajeCapturado = ""

        val testCallback = object : AuthCallback {
            override fun onSuccess() {}
            override fun onError(error: String) { mensajeCapturado = error }
        }

        // Simulamos el "user-not-found" que espera tu Activity
        doAnswer { invocation ->
            val callback = invocation.arguments[1] as AuthCallback
            callback.onError("user-not-found")
            null
        }.`when`(mockAuthRepo).enviarCorreoRecuperacion(correo, testCallback)

        mockAuthRepo.enviarCorreoRecuperacion(correo, testCallback)

        assert(mensajeCapturado == "user-not-found")
    }

    @Test
    fun `falla por error de conexion a internet y notifica al callback`() {
        val correo = "usuario@valido.com"
        var mensajeCapturado = ""

        val testCallback = object : AuthCallback {
            override fun onSuccess() {}
            override fun onError(error: String) { mensajeCapturado = error }
        }

        // Simulamos el error de red
        doAnswer { invocation ->
            val callback = invocation.arguments[1] as AuthCallback
            callback.onError("network-request-failed")
            null
        }.`when`(mockAuthRepo).enviarCorreoRecuperacion(correo, testCallback)

        mockAuthRepo.enviarCorreoRecuperacion(correo, testCallback)

        assert(mensajeCapturado == "network-request-failed")
    }

    @Test
    fun `falla por error desconocido y notifica al callback`() {
        val correo = "usuario@valido.com"
        var mensajeCapturado = ""

        val testCallback = object : AuthCallback {
            override fun onSuccess() {}
            override fun onError(error: String) { mensajeCapturado = error }
        }

        // Simulamos un error raro que activará el "else" en tu Activity
        doAnswer { invocation ->
            val callback = invocation.arguments[1] as AuthCallback
            callback.onError("internal-server-error-500")
            null
        }.`when`(mockAuthRepo).enviarCorreoRecuperacion(correo, testCallback)

        mockAuthRepo.enviarCorreoRecuperacion(correo, testCallback)

        assert(mensajeCapturado == "internal-server-error-500")
    }
}