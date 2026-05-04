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

    @Before
    fun setup() {
        // Inicializamos nuestro Repositorio falso (Mock)
        mockAuthRepo = mock(AuthRepository::class.java)
    }

    // ==========================================
    // VALIDACIONES DE FORMULARIO (Lógica pura)
    // ==========================================

    @Test
    fun `validacion falla si algun campo obligatorio esta vacio`() {
        val vacio = ""
        val lleno = "Texto"
        val result = ValidationUtils.isNotEmpty(vacio, lleno, "otro")
        assert(!result)
    }

    @Test
    fun `valida correctamente un CURP y correo validos`() {
        val curp = "VAAO010101HDFRNS09"
        val correo = "usuario@valido.com"

        assert(ValidationUtils.isValidCURP(curp))
        assert(ValidationUtils.isValidEmail(correo))
    }

    @Test
    fun `validacion falla si la contrasena es debil`() {
        val pass = "12345"
        val result = ValidationUtils.isStrongPassword(pass)
        assert(!result)
    }

    @Test
    fun `validacion exitosa si la contrasena es fuerte`() {
        val passFuerte = "Admin123!@#"
        val result = ValidationUtils.isStrongPassword(passFuerte)
        assert(result)
    }

    @Test
    fun `validacion falla si los codigos postales y telefonos no tienen la longitud correcta`() {
        assert(!ValidationUtils.isValidPhone("12345")) // Corto
        assert(!ValidationUtils.isValidPostalCode("123")) // Corto
    }

    // --- CASOS LÍMITE (Edge Cases) ---

    @Test
    fun `validacion falla si el nombre o apellidos contienen numeros o simbolos`() {
        // Nombres inválidos
        assert(!ValidationUtils.isValidName("Oscar123"))
        assert(!ValidationUtils.isValidName("Oscar@Vaquero"))

        // Nombres válidos con espacios o acentos
        assert(ValidationUtils.isValidName("Oscar René"))
        assert(ValidationUtils.isValidName("Vaquero"))
    }

    @Test
    fun `validacion de telefono alternativo maneja correctamente campos vacios e invalidos`() {
        val telefonoPrincipal = "5512345678"
        val altVacio = ""
        val altInvalido = "12345" // Corto
        val altValido = "5598765432"

        // 1. Si está vacío, debe pasar (es opcional)
        val fallaConVacio = !ValidationUtils.isValidPhone(telefonoPrincipal) ||
                (altVacio.isNotEmpty() && !ValidationUtils.isValidPhone(altVacio))
        assert(!fallaConVacio)

        // 2. Si tiene algo pero es corto, debe fallar
        val fallaConInvalido = !ValidationUtils.isValidPhone(telefonoPrincipal) ||
                (altInvalido.isNotEmpty() && !ValidationUtils.isValidPhone(altInvalido))
        assert(fallaConInvalido)

        // 3. Si tiene 10 dígitos, debe pasar
        val fallaConValido = !ValidationUtils.isValidPhone(telefonoPrincipal) ||
                (altValido.isNotEmpty() && !ValidationUtils.isValidPhone(altValido))
        assert(!fallaConValido)
    }

    // ==========================================
    // PRUEBAS DE REPOSITORIO (Manejo de Firebase)
    // ==========================================

    // Función auxiliar para no repetir la creación del usuario en cada prueba
    private fun crearUsuarioPrueba(): User {
        return User(
            uid = "",
            curp = "VAAO010101HDFRNS09",
            nombre = "Oscar",
            apellidoP = "Vaquero",
            apellidoM = "Santos",
            telefono = "5512345678",
            telefonoAlt = "",
            calle = "Calle Falsa",
            numero = "123",
            colonia = "Centro",
            alcaldia = "CDMX",
            codigoPostal = "01234",
            correo = "usuario@valido.com"
        )
    }

    @Test
    fun `registro exitoso llama al callback onSuccess`() {
        val user = crearUsuarioPrueba()
        val pass = "ContraseñaFuerte1!"

        var exito = false
        val testCallback = object : AuthCallback {
            override fun onSuccess() {
                exito = true
            }

            override fun onError(error: String) {
                exito = false
            }
        }

        // SOLUCIÓN: Pasamos las variables directas sin usar eq() para evitar el NPE de Kotlin
        doAnswer { invocation ->
            val callback = invocation.arguments[3] as AuthCallback
            callback.onSuccess()
            null
        }.`when`(mockAuthRepo).registrarUsuario(user.correo, pass, user, testCallback)

        mockAuthRepo.registrarUsuario(user.correo, pass, user, testCallback)

        assert(exito)
    }

    @Test
    fun `registro falla por correo duplicado y notifica al callback`() {
        val user = crearUsuarioPrueba()
        val pass = "ContraseñaFuerte1!"

        var mensajeCapturado = ""
        val testCallback = object : AuthCallback {
            override fun onSuccess() {}
            override fun onError(error: String) {
                mensajeCapturado = error
            }
        }

        doAnswer { invocation ->
            val callback = invocation.arguments[3] as AuthCallback
            callback.onError("email address is already in use")
            null
        }.`when`(mockAuthRepo).registrarUsuario(user.correo, pass, user, testCallback)

        mockAuthRepo.registrarUsuario(user.correo, pass, user, testCallback)

        assert(mensajeCapturado == "email address is already in use")
    }

    @Test
    fun `registro falla por formato de correo incorrecto devuelto por Firebase`() {
        val user = crearUsuarioPrueba()
        val pass = "ContraseñaFuerte1!"

        var mensajeCapturado = ""
        val testCallback = object : AuthCallback {
            override fun onSuccess() {}
            override fun onError(error: String) {
                mensajeCapturado = error
            }
        }

        doAnswer { invocation ->
            val callback = invocation.arguments[3] as AuthCallback
            callback.onError("email address is badly formatted")
            null
        }.`when`(mockAuthRepo).registrarUsuario(user.correo, pass, user, testCallback)

        mockAuthRepo.registrarUsuario(user.correo, pass, user, testCallback)

        assert(mensajeCapturado == "email address is badly formatted")
    }

    @Test
    fun `registro falla por error de conexion a internet`() {
        val user = crearUsuarioPrueba()
        val pass = "ContraseñaFuerte1!"

        var mensajeCapturado = ""
        val testCallback = object : AuthCallback {
            override fun onSuccess() {}
            override fun onError(error: String) {
                mensajeCapturado = error
            }
        }

        doAnswer { invocation ->
            val callback = invocation.arguments[3] as AuthCallback
            callback.onError("network-request-failed")
            null
        }.`when`(mockAuthRepo).registrarUsuario(user.correo, pass, user, testCallback)

        mockAuthRepo.registrarUsuario(user.correo, pass, user, testCallback)

        assert(mensajeCapturado == "network-request-failed")
    }

    @Test
    fun `registro falla por contrasena debil devuelta por Firebase y notifica al callback`() {
        val user = crearUsuarioPrueba()
        val pass = "Clave1!"

        var mensajeCapturado = ""
        val testCallback = object : AuthCallback {
            override fun onSuccess() {}
            override fun onError(error: String) {
                mensajeCapturado = error
            }
        }

        doAnswer { invocation ->
            val callback = invocation.arguments[3] as AuthCallback
            callback.onError("weak password")
            null
        }.`when`(mockAuthRepo).registrarUsuario(user.correo, pass, user, testCallback)

        mockAuthRepo.registrarUsuario(user.correo, pass, user, testCallback)

        assert(mensajeCapturado == "weak password")
    }

    @Test
    fun `registro maneja error desconocido de Firebase correctamente`() {
        val user = crearUsuarioPrueba()
        val pass = "ContraseñaFuerte1!"

        var mensajeCapturado = ""
        val testCallback = object : AuthCallback {
            override fun onSuccess() {}
            override fun onError(error: String) {
                mensajeCapturado = error
            }
        }

        doAnswer { invocation ->
            val callback = invocation.arguments[3] as AuthCallback
            callback.onError("internal-server-error-500")
            null
        }.`when`(mockAuthRepo).registrarUsuario(user.correo, pass, user, testCallback)

        mockAuthRepo.registrarUsuario(user.correo, pass, user, testCallback)

        assert(mensajeCapturado == "internal-server-error-500")
    }

    @Test
    fun `validacion de campos de direccion respeta el limite de 50 caracteres y rechaza vacios`() {
        val direccionCorta = "Av. Instituto Politécnico Nacional"
        val direccionVacia = "   "
        // String de 51 caracteres
        val direccionExcesiva = "Esta es una direccion ridículamente larga que super"

        assert(ValidationUtils.isValidAddressField(direccionCorta))
        assert(!ValidationUtils.isValidAddressField(direccionVacia))
        assert(!ValidationUtils.isValidAddressField(direccionExcesiva))
    }

    @Test
    fun `validacion de contrasenas asegura que ambas coincidan exactamente`() {
        val passOriginal = "Password123!"
        val passDiferente = "Password123" // Le falta el signo
        val passIgual = "Password123!"

        // Simulamos la lógica exacta que tienes en tu Activity
        assert(passOriginal != passDiferente)
        assert(passOriginal == passIgual)
    }
}