package com.example.xolotl.ui.main.usuario

import android.os.Build
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.widget.EditText
import android.widget.ImageButton
import com.example.xolotl.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Ignore("Omitido por falta de RAM en ejecución masiva. Ejecutar individualmente.")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class EditarPerfilActivityTest {

    private lateinit var firestoreMockStatic: MockedStatic<FirebaseFirestore>
    private lateinit var authMockStatic: MockedStatic<FirebaseAuth>
    private lateinit var base64MockStatic: MockedStatic<Base64>

    @Before
    fun setup() {
        // 1. ANESTESIAMOS A FIREBASE: Evitamos que el onCreate crashee al buscar al usuario
        val mockFirestore = mock(FirebaseFirestore::class.java, RETURNS_DEEP_STUBS)
        val mockAuth = mock(FirebaseAuth::class.java, RETURNS_DEEP_STUBS)
        `when`(mockAuth.uid).thenReturn("USUARIO_DUMMY_123")

        firestoreMockStatic = mockStatic(FirebaseFirestore::class.java)
        authMockStatic = mockStatic(FirebaseAuth::class.java)

        firestoreMockStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)
        authMockStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)

        // 2. ANESTESIAMOS BASE64: Evitamos crasheos si la UI intenta desencriptar datos vacíos del mock
        base64MockStatic = mockStatic(Base64::class.java)
        base64MockStatic.`when`<ByteArray> { Base64.decode(anyString() ?: "", anyInt()) }.thenReturn(ByteArray(0))
        base64MockStatic.`when`<String> { Base64.encodeToString(any() ?: ByteArray(0), anyInt()) }.thenReturn("")
    }

    @After
    fun tearDown() {
        firestoreMockStatic.close()
        authMockStatic.close()
        base64MockStatic.close()
    }

    private fun iniciarActivity(): EditarPerfilActivity {
        return Robolectric.buildActivity(EditarPerfilActivity::class.java).create().resume().get()
    }

    // =========================================================
    // 1. PRUEBAS DE LÓGICA DE INTERFAZ (Botones Toggle)
    // =========================================================

    @Test
    fun `logica UI - el boton del ojito alterna la visibilidad de la contrasena`() {
        val activity = iniciarActivity()
        val txtContrasena = activity.findViewById<EditText>(R.id.txtContrasena)
        val btnToggle = activity.findViewById<ImageButton>(R.id.btnToggleContrasena)

        // Estado inicial: Por defecto, los EditText tipo "textPassword" en XML usan PasswordTransformationMethod
        // Al interactuar con el botón, la Activity fuerza su estado inicial interno.
        btnToggle.performClick()

        // 1er Clic: La contraseña DEBE SER VISIBLE
        assertTrue(
            "Al hacer clic, el método debe cambiar a HideReturns (Visible)",
            txtContrasena.transformationMethod is HideReturnsTransformationMethod
        )

        // 2do Clic: La contraseña DEBE OCULTARSE
        btnToggle.performClick()
        assertTrue(
            "Al hacer clic de nuevo, el método debe cambiar a PasswordTransformation (Oculto)",
            txtContrasena.transformationMethod is PasswordTransformationMethod
        )
    }

    // =========================================================
    // 2. PRUEBAS DE VALIDACIÓN EN TIEMPO REAL (Límites y Reglas)
    // =========================================================

    @Test
    fun `validacion en tiempo real - detecta nombres muy cortos o con numeros`() {
        val activity = iniciarActivity()
        val txtNombre = activity.findViewById<EditText>(R.id.txtNombre)
        val layoutNombre = activity.findViewById<TextInputLayout>(R.id.layoutNombre)

        // Prueba 1: Nombre muy corto
        txtNombre.setText("Al")
        assertEquals("Mínimo 3 caracteres", layoutNombre.error?.toString())

        // Prueba 2: Nombre con caracteres inválidos
        txtNombre.setText("Oscar123")
        assertEquals("Solo se permiten letras", layoutNombre.error?.toString())

        // Prueba 3: Nombre válido
        txtNombre.setText("Oscar")
        assertNull("El error debe desaparecer si el nombre es válido", layoutNombre.error)
    }

    @Test
    fun `validacion en tiempo real - el telefono exige exactamente 10 digitos`() {
        val activity = iniciarActivity()
        val txtTelefono = activity.findViewById<EditText>(R.id.txtTelefono)
        val layoutTelefono = activity.findViewById<TextInputLayout>(R.id.layoutTelefono)

        // Prueba 1: Teléfono incompleto
        txtTelefono.setText("5512345")
        assertTrue(layoutTelefono.error?.toString()?.contains("exactamente 10 dígitos") == true)

        // Prueba 2: Teléfono válido
        txtTelefono.setText("5512345678")
        assertNull("El error debe desaparecer con 10 dígitos exactos", layoutTelefono.error)
    }

    @Test
    fun `validacion en tiempo real - codigo postal exige exactamente 5 digitos`() {
        val activity = iniciarActivity()
        val txtCP = activity.findViewById<EditText>(R.id.txtCodigoPostal)
        val layoutCP = activity.findViewById<TextInputLayout>(R.id.layoutCodigoPostal)

        txtCP.setText("070")
        assertEquals("Deben ser 5 dígitos", layoutCP.error?.toString())

        txtCP.setText("07000")
        assertNull(layoutCP.error)
    }

    // =========================================================
    // 3. PRUEBAS DE SEGURIDAD (Reglas de Contraseña)
    // =========================================================

    @Test
    fun `validacion de seguridad - evalua la fortaleza y coincidencia de contrasenas`() {
        val activity = iniciarActivity()
        val txtPass = activity.findViewById<EditText>(R.id.txtContrasena)
        val txtConfPass = activity.findViewById<EditText>(R.id.txtConfirmarContrasena)
        val layoutPass = activity.findViewById<TextInputLayout>(R.id.layoutContrasena)
        val layoutConfPass = activity.findViewById<TextInputLayout>(R.id.layoutConfirmarContrasena)

        // Prueba 1: Contraseña corta
        txtPass.setText("1234")
        assertEquals("Mínimo 8 caracteres", layoutPass.error?.toString())

        // Prueba 2: Sin mayúsculas
        txtPass.setText("password123.")
        assertEquals("Falta una Mayúscula", layoutPass.error?.toString())

        // Prueba 3: Contraseña fuerte pero sin confirmar correctamente
        txtPass.setText("PassFuerte123@")
        assertNull("La contraseña fuerte no debe tener errores", layoutPass.error)

        txtConfPass.setText("PassFuerte")
        assertEquals("Las contraseñas no coinciden", layoutConfPass.error?.toString())

        // Prueba 4: Confirmación correcta
        txtConfPass.setText("PassFuerte123@")
        assertNull("El error debe desaparecer al coincidir", layoutConfPass.error)
    }

    // =========================================================
    // 4. PRUEBAS DE CASOS FRONTERA (Botón Guardar)
    // =========================================================

    @Test
    fun `boton guardar - activa errores en los layouts si hay campos vacios`() {
        val activity = iniciarActivity()
        val btnGuardar = activity.findViewById<android.widget.LinearLayout>(R.id.btnGuardarCambios)

        val txtNombre = activity.findViewById<EditText>(R.id.txtNombre)
        val layoutNombre = activity.findViewById<TextInputLayout>(R.id.layoutNombre)
        val layoutCalle = activity.findViewById<TextInputLayout>(R.id.layoutCalle)

        // Forzamos campos críticos a vacío
        txtNombre.setText("")
        activity.findViewById<EditText>(R.id.txtCalle).setText("")

        // Ejecutamos la validación global simulando el clic en guardar
        btnGuardar.performClick()

        // Validamos que la función validarFormularioCompleto() atrapó los campos vacíos
        assertEquals("Requerido", layoutNombre.error?.toString())
        assertEquals("Requerido", layoutCalle.error?.toString())
    }
}