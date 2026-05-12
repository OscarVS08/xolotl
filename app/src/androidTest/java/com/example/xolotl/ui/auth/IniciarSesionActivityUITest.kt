package com.example.xolotl.ui.auth

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.MainActivity
import com.example.xolotl.R
import com.google.firebase.auth.FirebaseAuth
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class IniciarSesionActivityUITest {

    @Before
    fun setUp() {
        Intents.init()
        // Aseguramos que empezamos sin sesiones iniciadas
        FirebaseAuth.getInstance().signOut()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(IniciarSesionActivity::class.java)

        // Verificamos que los inputs y botones existan en la pantalla
        onView(withId(R.id.txtCorreo)).check(matches(isDisplayed()))
        onView(withId(R.id.txtContrasena)).check(matches(isDisplayed()))
        onView(withId(R.id.btnIngresar)).check(matches(isDisplayed()))
        onView(withId(R.id.btnOlvidasteContrasena)).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_camposVacios_muestraAlertaSweetAlert() {
        ActivityScenario.launch(IniciarSesionActivity::class.java)

        // Hacemos clic en ingresar sin escribir nada
        onView(withId(R.id.btnIngresar)).perform(click())

        // Buscamos el título de tu SweetAlert
        onView(withText("Campos incompletos")).check(matches(isDisplayed()))
        onView(withText("Por favor, llena todos los campos del formulario para continuar.")).check(matches(isDisplayed()))
    }

    @Test
    fun prueba3_validacionTiempoReal_correoInvalido_muestraErrorEnLayout() {
        ActivityScenario.launch(IniciarSesionActivity::class.java)

        // Escribimos un correo mal formateado
        onView(withId(R.id.txtCorreo)).perform(typeText("correo_sin_arroba"), closeSoftKeyboard())

        // Tu código usa app:errorEnabled="true" en el layoutCorreo.
        // Espresso buscará el texto de error que se inyecta dinámicamente.
        onView(withText("Ingresa un correo válido")).check(matches(isDisplayed()))
    }

    @Test
    fun prueba4_botonOlvideContrasena_navegaActivityCorrecta() {
        ActivityScenario.launch(IniciarSesionActivity::class.java)

        onView(withId(R.id.btnOlvidasteContrasena)).perform(click())

        intended(hasComponent(RestablecerContrasenaActivity::class.java.name))
    }

    @Test
    fun prueba5_loginExitoso_navegaAMainActivity() {
        ActivityScenario.launch(IniciarSesionActivity::class.java)

        // ==========================================
        // ⚠️ CAMBIA ESTOS DATOS POR UN USUARIO REAL DE TU FIREBASE ⚠️
        // ==========================================
        val correoPrueba = "vaquerosantososcar@gmail.com"
        val passwordPrueba = "D#Oscar08"

        onView(withId(R.id.txtCorreo)).perform(typeText(correoPrueba), closeSoftKeyboard())
        onView(withId(R.id.txtContrasena)).perform(typeText(passwordPrueba), closeSoftKeyboard())

        onView(withId(R.id.btnIngresar)).perform(click())

        // Firebase hace una petición de red que tarda unos milisegundos/segundos.
        // Pausamos la prueba 3 segundos para darle tiempo de responder antes de buscar la nueva pantalla.
        Thread.sleep(3000)

        // Si el login fue exitoso, debimos haber saltado al MainActivity
        intended(hasComponent(MainActivity::class.java.name))
    }
}