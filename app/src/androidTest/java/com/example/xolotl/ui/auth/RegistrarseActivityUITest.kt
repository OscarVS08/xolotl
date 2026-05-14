package com.example.xolotl.ui.auth

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.*
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
class RegistrarseActivityUITest {

    @Before
    fun setUp() {
        Intents.init()
        FirebaseAuth.getInstance().signOut()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(RegistrarseActivity::class.java)

        // Verificamos elementos superiores
        onView(withId(R.id.txtCurp)).check(matches(isDisplayed()))

        // Hacemos scroll para verificar elementos inferiores
        onView(withId(R.id.btnCrearCuenta)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_enlaceOlvidasteCurp_abreNavegadorExterno() {
        ActivityScenario.launch(RegistrarseActivity::class.java)

        onView(withId(R.id.txtOlvidasteCurp)).perform(scrollTo(), click())

        // Verificamos que se lanzó un Intent hacia el navegador con la URL correcta
        intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasData("https://www.gob.mx/curp/")
        ))
    }

    @Test
    fun prueba3_enlaceTerminos_navegaATerminosActivity() {
        ActivityScenario.launch(RegistrarseActivity::class.java)

        onView(withId(R.id.txtTerminosLink)).perform(scrollTo(), click())

        intended(hasComponent(TerminosActivity::class.java.name))
    }

    @Test
    fun prueba4_validacionTiempoReal_muestraErroresAlEscribirMal() {
        ActivityScenario.launch(RegistrarseActivity::class.java)

        // Escribimos una CURP corta y revisamos el layout
        onView(withId(R.id.txtCurp)).perform(scrollTo(), typeText("ABC"), closeSoftKeyboard())
        onView(withText("Faltan 15 caracteres")).perform(scrollTo()).check(matches(isDisplayed()))

        // Escribimos una contraseña sin mayúsculas
        onView(withId(R.id.txtContrasena)).perform(scrollTo(), typeText("minusculas123"), closeSoftKeyboard())
        onView(withText("Debe incluir una Mayúscula")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba5_formularioIncompleto_muestraSweetAlert() {
        ActivityScenario.launch(RegistrarseActivity::class.java)

        // Damos clic directo a crear cuenta sin llenar nada
        onView(withId(R.id.btnCrearCuenta)).perform(scrollTo(), click())

        // Tu código primero valida los términos y condiciones si está vacío
        onView(withText("Términos y Condiciones")).check(matches(isDisplayed()))
    }

    @Test
    fun prueba6_registroExitoso_creaUsuarioYLoElimina() {
        ActivityScenario.launch(RegistrarseActivity::class.java)

        val correoPrueba = "test_ui_registro@xolotl.com"
        val passwordPrueba = "XolotlTest123!@"

        // 1. Llenar Datos Personales
        onView(withId(R.id.txtCurp)).perform(scrollTo(), typeText("VASO010408HDFQNSA5"), closeSoftKeyboard())
        onView(withId(R.id.txtNombre)).perform(scrollTo(), typeText("Usuario"), closeSoftKeyboard())
        onView(withId(R.id.txtApellidoP)).perform(scrollTo(), typeText("Prueba"), closeSoftKeyboard())
        onView(withId(R.id.txtApellidoM)).perform(scrollTo(), typeText("Ui"), closeSoftKeyboard())
        onView(withId(R.id.txtTelefono)).perform(scrollTo(), typeText("5512345678"), closeSoftKeyboard())

        // 2. Llenar Dirección
        onView(withId(R.id.txtCalle)).perform(scrollTo(), typeText("Avenida Siempreviva"), closeSoftKeyboard())
        onView(withId(R.id.txtNumero)).perform(scrollTo(), typeText("742"), closeSoftKeyboard())
        onView(withId(R.id.txtColonia)).perform(scrollTo(), typeText("Centro"), closeSoftKeyboard())
        onView(withId(R.id.txtAlcaldia)).perform(scrollTo(), typeText("GAM"), closeSoftKeyboard())
        onView(withId(R.id.txtCodigoPostal)).perform(scrollTo(), typeText("07000"), closeSoftKeyboard())

        // 3. Llenar Seguridad
        onView(withId(R.id.txtCorreo)).perform(scrollTo(), typeText(correoPrueba), closeSoftKeyboard())
        onView(withId(R.id.txtContrasena)).perform(scrollTo(), typeText(passwordPrueba), closeSoftKeyboard())
        onView(withId(R.id.txtConfirmarContrasena)).perform(scrollTo(), typeText(passwordPrueba), closeSoftKeyboard())

        // 4. Aceptar términos y Registrar
        onView(withId(R.id.checkTerminos)).perform(scrollTo(), click())
        onView(withId(R.id.btnCrearCuenta)).perform(scrollTo(), click())

        // Esperamos a que Firebase procese el registro y envíe el correo (4 segundos de margen)
        Thread.sleep(4000)

        // Verificamos que salga el SweetAlert de éxito indicando que se envió el correo
        onView(withText("Verifica tu correo")).check(matches(isDisplayed()))

        // ========================================================
        // LIMPIEZA DE BASE DE DATOS (El toque profesional)
        // Como el registro fue exitoso, Firebase inició sesión automáticamente por debajo.
        // Capturamos a ese usuario actual y le ordenamos a Firebase que lo destruya
        // antes de que la prueba termine y cierre la app.
        // ========================================================
        val usuarioCreado = FirebaseAuth.getInstance().currentUser
        usuarioCreado?.delete()?.addOnCompleteListener {
            FirebaseAuth.getInstance().signOut()
        }
    }
}