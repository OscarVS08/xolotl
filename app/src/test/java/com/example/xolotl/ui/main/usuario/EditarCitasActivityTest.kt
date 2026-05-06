package com.example.xolotl.ui.main.usuario

import android.content.Intent
import android.os.Build
import android.text.method.TextKeyListener
import android.util.Base64
import android.view.View
import android.widget.AdapterView
import android.widget.AutoCompleteTextView
import android.widget.EditText
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
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Base64 as JavaBase64

@Ignore("Omitido por falta de RAM en ejecución masiva. Ejecutar individualmente.")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class EditarCitasActivityTest {

    private lateinit var firestoreMockStatic: MockedStatic<FirebaseFirestore>
    private lateinit var authMockStatic: MockedStatic<FirebaseAuth>
    private lateinit var base64MockStatic: MockedStatic<Base64>
    private var controller: org.robolectric.android.controller.ActivityController<EditarCitasActivity>? = null

    @Before
    fun setup() {
        val mockFirestore = mock(FirebaseFirestore::class.java, RETURNS_DEEP_STUBS)
        val mockAuth = mock(FirebaseAuth::class.java, RETURNS_DEEP_STUBS)
        `when`(mockAuth.uid).thenReturn("USUARIO_DUMMY_ID")

        firestoreMockStatic = mockStatic(FirebaseFirestore::class.java)
        authMockStatic = mockStatic(FirebaseAuth::class.java)

        firestoreMockStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)
        authMockStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)

        base64MockStatic = mockStatic(Base64::class.java)
        base64MockStatic.`when`<ByteArray> { Base64.decode(anyString() ?: "", anyInt()) }.thenReturn(ByteArray(0))
        base64MockStatic.`when`<String> { Base64.encodeToString(any() ?: ByteArray(0), anyInt()) }.thenReturn("")
    }

    @After
    fun tearDown() {
        // 1. Cierre de mocks (lo que ya tenías con seguridad de inicialización)
        if (::firestoreMockStatic.isInitialized) firestoreMockStatic.close()
        if (::authMockStatic.isInitialized) authMockStatic.close()
        if (::base64MockStatic.isInitialized) base64MockStatic.close()

        // 2. AGREGAR ESTO: Reset de UiUtils para liberar memoria estática
        com.example.xolotl.utils.UiUtils.dialogFactory = { context, type ->
            cn.pedant.SweetAlert.SweetAlertDialog(context, type)
        }

        // 3. AGREGAR ESTO: Matar la Activity y limpiar el Looper
        controller?.pause()?.stop()?.destroy()
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
    }

    private fun iniciarActivityConDatos(): EditarCitasActivity {
        val intentValido = Intent().apply {
            putExtra("id", "CITA_123")
            putExtra("ruacMascota", "RUAC_456")
        }
        // Guardamos el controlador para poder matarlo luego
        controller = Robolectric.buildActivity(EditarCitasActivity::class.java, intentValido)
        return controller!!.create().resume().get()
    }

    // =========================================================
    // 1. PRUEBAS DE AUTOPROTECCIÓN Y CICLO DE VIDA (Frontera)
    // =========================================================

    @Test
    fun `autoproteccion - la actividad se suicida si no recibe datos del Intent`() {
        val intentVacio = Intent()
        val activity = Robolectric.buildActivity(EditarCitasActivity::class.java, intentVacio).create().get()

        assertTrue("La actividad debe protegerse cerrándose inmediatamente", activity.isFinishing)
    }

    // =========================================================
    // 2. PRUEBAS DE LÓGICA DE VALIDACIÓN (El "Cerebro" de la UI)
    // =========================================================

    @Test
    fun `logica de formulario - detecta campos vacios y muestra errores en los layouts`() {
        val activity = iniciarActivityConDatos()
        val btnGuardar = activity.findViewById<View>(R.id.btnGuardarCambios)

        val layoutServicio = activity.findViewById<TextInputLayout>(R.id.layoutServicio)
        val layoutFechaHora = activity.findViewById<TextInputLayout>(R.id.layoutFechaHora)

        activity.findViewById<EditText>(R.id.txtServicio).setText("")
        activity.findViewById<EditText>(R.id.txtFechaHora).setText("")

        btnGuardar.performClick()

        assertEquals("Selecciona un servicio", layoutServicio.error?.toString())
        assertEquals("Selecciona fecha y hora", layoutFechaHora.error?.toString())
    }

    @Test
    fun `logica de formulario - rechaza fechas en el pasado al intentar guardar`() {
        val activity = iniciarActivityConDatos()
        val btnGuardar = activity.findViewById<View>(R.id.btnGuardarCambios)

        val txtFechaHora = activity.findViewById<EditText>(R.id.txtFechaHora)
        val layoutFechaHora = activity.findViewById<TextInputLayout>(R.id.layoutFechaHora)

        txtFechaHora.setText("01/01/2000 10:00")

        btnGuardar.performClick()

        assertEquals("La fecha debe ser posterior a hoy", layoutFechaHora.error?.toString())
    }

    // =========================================================
    // 3. PRUEBAS DE LÓGICA DINÁMICA (Interacción de Usuario)
    // =========================================================

    @Test
    fun `logica de servicio - seleccionar 'Otro' desbloquea el teclado en el AutoCompleteTextView`() {
        val activity = iniciarActivityConDatos()
        val txtServicio = activity.findViewById<AutoCompleteTextView>(R.id.txtServicio)

        assertNull("Por defecto, el KeyListener debe ser nulo para evitar escritura manual", txtServicio.keyListener)

        // SOLUCIÓN AL NPE: Creamos un AdapterView fantasma que devuelva "Otro" al consultarlo
        val mockParent = mock(AdapterView::class.java)
        `when`(mockParent.getItemAtPosition(0)).thenReturn("Otro")

        // Simulamos la selección de la opción "Otro" enviándole nuestro mock en lugar de null
        txtServicio.onItemClickListener?.onItemClick(mockParent, null, 0, 0)

        // Verificamos que tu Activity abrió el candado (asignó el KeyListener)
        assertNotNull("Si elige 'Otro', el KeyListener debe activarse para escribir", txtServicio.keyListener)
        assertEquals("El texto debe limpiarse para que el usuario escriba", "", txtServicio.text.toString())
    }
}