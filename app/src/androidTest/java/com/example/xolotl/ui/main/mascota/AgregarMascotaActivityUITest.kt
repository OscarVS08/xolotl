package com.example.xolotl.ui.main.mascota

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.R
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AgregarMascotaActivityUITest {

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(AgregarMascotaActivity::class.java)

        onView(withId(R.id.txtRuac)).check(matches(isDisplayed()))
        onView(withId(R.id.txtNombreMascota)).check(matches(isDisplayed()))

        // Hacemos scroll para ver los botones del fondo
        onView(withId(R.id.btnGuardarMascota)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_enlacesExternos_abrenNavegadorCorrecto() {
        ActivityScenario.launch(AgregarMascotaActivity::class.java)

        // Simular que el navegador responderá bien para no salir de la app
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent())
        intending(hasAction(Intent.ACTION_VIEW)).respondWith(intentResult)

        // Clic en RUAC
        onView(withId(R.id.txtTramitarRuac)).perform(scrollTo(), click())
        intended(allOf(hasAction(Intent.ACTION_VIEW), hasData("https://www.ruac.cdmx.gob.mx/")))
    }

    @Test
    fun prueba3_fechasIncoherentes_muestraErrorEnLayout() {
        val scenario = ActivityScenario.launch(AgregarMascotaActivity::class.java)

        // Inyectamos las fechas directamente
        scenario.onActivity { activity ->
            val txtNac = activity.findViewById<android.widget.AutoCompleteTextView>(R.id.txtFechaNacimiento)
            val txtAdop = activity.findViewById<android.widget.AutoCompleteTextView>(R.id.txtFechaAdopcion)

            txtNac.setText("10/10/2025", false)
            txtAdop.setText("01/01/2025", false)
        }

        // Hacemos clic en guardar. Esto dispara validarFormularioCompleto()
        onView(withId(R.id.btnGuardarMascota)).perform(scrollTo(), click())

        // El SweetAlert de "Formulario incompleto" aparece tapando la pantalla.
        // Le damos clic al botón "OK" para cerrarlo.
        onView(withText("OK")).perform(click())

        // Ahora que la alerta se quitó, buscamos el error específico en el layout
        onView(withText("La adopción no puede ser antes del nacimiento")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba4_camposCondicionales_razaOtro_muestraCamposOcultos() {
        ActivityScenario.launch(AgregarMascotaActivity::class.java)

        // 1. Seleccionar Especie: Perro
        onView(withId(R.id.txtEspecie)).perform(scrollTo(), click())
        onView(withText("Perro")).inRoot(isPlatformPopup()).perform(click())

        // 2. Seleccionar Raza: Otro
        onView(withId(R.id.txtRaza)).perform(scrollTo(), click())
        onView(withText("Otro")).inRoot(isPlatformPopup()).perform(click())

        // 3. Verificamos que el EditText de 'RazaOtro' y el link de 'IdentificarRaza' se hicieron visibles
        onView(withId(R.id.layoutRazaOtro)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.txtIdentificarRaza)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba5_validacionEspecie_pesoGatoExcedido_muestraError() {
        ActivityScenario.launch(AgregarMascotaActivity::class.java)

        // Seleccionar Gato
        onView(withId(R.id.txtEspecie)).perform(scrollTo(), click())
        onView(withText("Gato")).inRoot(isPlatformPopup()).perform(click())

        // Escribir peso de 20kg (el límite de gato es 15kg según tu lógica)
        onView(withId(R.id.txtPeso)).perform(scrollTo(), typeText("20"), closeSoftKeyboard())

        onView(withText("Peso excedido para Gato (máx 15kg)")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba6_formularioCompletoSinFoto_muestraAlertaFaltaFoto() {
        ActivityScenario.launch(AgregarMascotaActivity::class.java)

        // Llenamos los datos básicos para pasar las validaciones de texto
        onView(withId(R.id.txtRuac)).perform(scrollTo(), typeText("ABCDEFGH12"), closeSoftKeyboard())
        onView(withId(R.id.txtNombreMascota)).perform(scrollTo(), typeText("Firulais"), closeSoftKeyboard())

        // Seleccionamos "Desconozco dato" en las fechas para evitar el DatePicker
        onView(withId(R.id.txtFechaNacimiento)).perform(scrollTo(), click())
        onView(withText("Desconozco dato")).inRoot(isPlatformPopup()).perform(click())

        onView(withId(R.id.txtFechaAdopcion)).perform(scrollTo(), click())
        onView(withText("Desconozco dato")).inRoot(isPlatformPopup()).perform(click())

        // Especie, Raza, Color, Sexo
        onView(withId(R.id.txtEspecie)).perform(scrollTo(), click())
        onView(withText("Perro")).inRoot(isPlatformPopup()).perform(click())

        onView(withId(R.id.txtRaza)).perform(scrollTo(), click())
        onView(withText("Pug")).inRoot(isPlatformPopup()).perform(click())

        onView(withId(R.id.txtColor)).perform(scrollTo(), click())
        onView(withText("Negro")).inRoot(isPlatformPopup()).perform(click())

        onView(withId(R.id.txtSexo)).perform(scrollTo(), click())
        onView(withText("Macho")).inRoot(isPlatformPopup()).perform(click())

        // Medidas
        onView(withId(R.id.txtPeso)).perform(scrollTo(), typeText("10"), closeSoftKeyboard())
        onView(withId(R.id.txtEstatura)).perform(scrollTo(), typeText("30"), closeSoftKeyboard())

        // Clic en Guardar (SIN HABER SUBIDO FOTO)
        onView(withId(R.id.btnGuardarMascota)).perform(scrollTo(), click())

        // Verificamos que el SweetAlert de "Falta foto" aparezca.
        // Esto confirma que TODO el formulario pasó las validaciones de texto con éxito.
        onView(withText("Falta foto")).check(matches(isDisplayed()))
        onView(withText("La foto es obligatoria para registrar a la mascota.")).check(matches(isDisplayed()))
    }

    @Test
    fun prueba7_botonGaleria_lanzaIntentCorrecto() {
        ActivityScenario.launch(AgregarMascotaActivity::class.java)

        // Bloqueamos la apertura real de la galería
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent())
        intending(hasAction(Intent.ACTION_PICK)).respondWith(intentResult)

        onView(withId(R.id.btnFoto)).perform(scrollTo(), click())

        // Verificamos que se lanzó la orden de abrir imágenes
        intended(allOf(hasAction(Intent.ACTION_PICK), hasType("image/*")))
    }
}