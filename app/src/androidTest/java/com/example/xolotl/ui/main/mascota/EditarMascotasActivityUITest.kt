package com.example.xolotl.ui.main.mascota

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.R
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditarMascotasActivityUITest {

    // Creamos el Intent "falso" para engañar a la validación de tu onCreate
    private val testIntent: Intent
        get() {
            val context = ApplicationProvider.getApplicationContext<Context>()
            return Intent(context, EditarMascotasActivity::class.java).apply {
                putExtra("docId", "mascota_prueba_123") // Simulamos que seleccionamos una mascota
            }
        }

    @Before
    fun setUp() {
        Intents.init()

        // Para evitar el 'return' por falta de sesión, iniciamos sesión rápidamente por debajo del agua
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            var loginTerminado = false
            // ==========================================
            //  PON TU CORREO Y CONTRASEÑA DE PRUEBA AQUÍ
            // ==========================================
            auth.signInWithEmailAndPassword("vaquerosantososcar@gmail.com", "D#Oscar08")
                .addOnCompleteListener { loginTerminado = true }

            // Pausamos el hilo de la prueba hasta que Firebase responda
            while (!loginTerminado) { Thread.sleep(100) }
        }
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch<EditarMascotasActivity>(testIntent)

        onView(withId(R.id.txtNombreMascotaTop)).check(matches(isDisplayed()))
        onView(withId(R.id.txtnumeroTelefonoDueno)).check(matches(isDisplayed()))

        onView(withId(R.id.btnAceptarMorado)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_fechasIncoherentes_muestraErrorEnLayout() {
        val scenario = ActivityScenario.launch<EditarMascotasActivity>(testIntent)

        // Inyectamos las fechas saltándonos el teclado bloqueado, igual que en AgregarMascota
        scenario.onActivity { activity ->
            val txtNac = activity.findViewById<android.widget.AutoCompleteTextView>(R.id.txtFechaNacimiento)
            val txtAdop = activity.findViewById<android.widget.AutoCompleteTextView>(R.id.txtFechaAdopcion)
            txtNac.setText("10/10/2025", false)
            txtAdop.setText("01/01/2025", false)
        }

        // Al dar clic en guardar, dispara validarFormularioCompleto()
        onView(withId(R.id.btnAceptarMorado)).perform(scrollTo(), click())

        // Aparece SweetAlert de "Atención: Revisa los campos en rojo", le damos OK
        onView(withText("OK")).perform(click())

        // Verificamos el mensaje en rojo de tu layout
        onView(withText("No puede ser adoptado antes de nacer")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba3_validacionEnTiempoReal_telefonoInvalido() {
        ActivityScenario.launch<EditarMascotasActivity>(testIntent)

        // 1. Le damos a Firebase 2 segundos para que descargue y pinte
        // el teléfono real del dueño en la pantalla.
        Thread.sleep(2000)

        // 2. Usamos replaceText en lugar de typeText para borrar el número
        // de la base de datos y forzar un número corto de 4 dígitos.
        onView(withId(R.id.txtnumeroTelefonoDueno)).perform(scrollTo(), replaceText("5512"), closeSoftKeyboard())

        // 3. Verificamos que tu TextWatcher reaccione al nuevo texto inválido
        onView(withText("Se requieren 10 dígitos")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba4_botonFoto_lanzaIntentGaleria() {
        ActivityScenario.launch<EditarMascotasActivity>(testIntent)

        // Bloqueamos la apertura real de la galería para no salir de la app en la prueba
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent())
        intending(hasAction(Intent.ACTION_PICK)).respondWith(intentResult)

        // En tu código le pusiste el listener a la imagen directamente
        onView(withId(R.id.imgFotoMascotaTop)).perform(scrollTo(), click())

        intended(allOf(hasAction(Intent.ACTION_PICK), hasType("image/*")))
    }

    @Test
    fun prueba5_formularioVacio_muestraAlertaDeCamposEnRojo() {
        ActivityScenario.launch<EditarMascotasActivity>(testIntent)

        // Al iniciar, si limpiamos el nombre para provocar un error de validación base
        onView(withId(R.id.txtNombreMascotaTop)).perform(scrollTo(), replaceText(""), closeSoftKeyboard())

        // Hacemos clic en guardar
        onView(withId(R.id.btnAceptarMorado)).perform(scrollTo(), click())

        // Verificamos que tu validación general haya bloqueado el guardado
        onView(withText("Atención")).check(matches(isDisplayed()))
        onView(withText("Revisa los campos en rojo")).check(matches(isDisplayed()))
    }
}