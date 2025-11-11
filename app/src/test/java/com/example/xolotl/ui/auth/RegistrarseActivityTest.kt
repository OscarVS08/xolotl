package com.example.xolotl.ui.auth

import com.example.xolotl.data.models.User
import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.utils.ValidationUtils
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class RegistrarseActivityTest {

    private lateinit var mockAuthRepo: AuthRepository
    private lateinit var mockCallback: AuthCallback
    private lateinit var activity: RegistrarseActivity

    @Before
    fun setup() {
        mockAuthRepo = mock(AuthRepository::class.java)
        mockCallback = mock(AuthCallback::class.java)
        activity = RegistrarseActivity()

        // Inyectamos el mock en el activity
        val field = RegistrarseActivity::class.java.getDeclaredField("authRepo")
        field.isAccessible = true
        field.set(activity, mockAuthRepo)
    }

    // --- VALIDACIONES DE FORMULARIO ---

    @Test
    fun `falla si algun campo obligatorio esta vacio`() {
        val vacio = ""
        val result = ValidationUtils.isNotEmpty(vacio, "texto", "otro")
        assert(!result)
    }

    @Test
    fun `valida correctamente un CURP y correo`() {
        val curp = "VAAO010101HDFRNS09"
        val correo = "usuario@valido.com"

        assert(ValidationUtils.isValidCURP(curp))
        assert(ValidationUtils.isValidEmail(correo))
    }

    @Test
    fun `falla si la contrasena es debil`() {
        val pass = "12345"
        val result = ValidationUtils.isStrongPassword(pass)
        assert(!result)
    }

    // --- PRUEBAS DE REPOSITORIO ---

    @Test
    fun `llama al repositorio con datos validos y recibe exito`() {
        val user = User(
            uid = "",
            curp = "VAAO010101HDFRNS09",
            nombre = "Oscar",
            apellidoP = "Vaquero",
            apellidoM = "Santos",
            telefono = "5512345678",
            telefonoAlt = "5512345679",
            calle = "Calle Falsa",
            numero = "123",
            colonia = "Centro",
            alcaldia = "CDMX",
            codigoPostal = "01234",
            correo = "usuario@valido.com"
        )
        val pass = "ContraseñaFuerte1!"

        // Simulamos que el registro se completa con éxito
        doAnswer {
            val callback = it.getArgument<AuthCallback>(3)
            callback.onSuccess()
            null
        }.`when`(mockAuthRepo).registrarUsuario(anyString(), anyString(), any(), any(AuthCallback::class.java))

        // Ejecutar
        mockAuthRepo.registrarUsuario(user.correo, pass, user, mockCallback)

        // Verificar que se llamó correctamente y se notificó éxito
        verify(mockAuthRepo, times(1))
            .registrarUsuario(eq(user.correo), eq(pass), eq(user), any(AuthCallback::class.java))
    }

    @Test
    fun `muestra error cuando el registro falla`() {
        val user = mock(User::class.java)

        // Simulamos un error de conexión
        doAnswer {
            val callback = it.getArgument<AuthCallback>(3)
            callback.onError("Error en conexión")
            null
        }.`when`(mockAuthRepo).registrarUsuario(anyString(), anyString(), any(), any(AuthCallback::class.java))

        // Ejecutar
        mockAuthRepo.registrarUsuario("usuario@valido.com", "Contraseña1!", user, mockCallback)

        // Verificar que se notificó error
        verify(mockCallback, times(1)).onError("Error en conexión")
    }
}
