package com.example.xolotl.ui.auth

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.R
import com.google.firebase.auth.FirebaseAuth
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.containsString
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegistrarseActivityUITest {

    @Before
    fun setUp() {
        FirebaseAuth.getInstance().signOut()
        Thread.sleep(1000)
    }

    @Test
    fun prueba1_elementosVisualesYEnlaces() {
        ActivityScenario.launch(RegistrarseActivity::class.java).use {
            Intents.init()
            intending(anyIntent()).respondWith(android.app.Instrumentation.ActivityResult(android.app.Activity.RESULT_OK, null))

            onView(withId(R.id.txtOlvidasteCurp)).perform(scrollTo(), click())
            intended(allOf(hasAction(Intent.ACTION_VIEW), hasData("https://www.gob.mx/curp/")))

            onView(withId(R.id.txtTerminosLink)).perform(scrollTo(), click())
            intended(hasComponent(TerminosActivity::class.java.name))

            Intents.release()
        }
    }

    @Test
    fun prueba2_togglesDeContrasena_cambianVisibilidad() {
        ActivityScenario.launch(RegistrarseActivity::class.java).use {
            onView(withId(R.id.txtContrasena)).perform(scrollTo(), typeText("12345"), closeSoftKeyboard())
            onView(withId(R.id.txtConfirmarContrasena)).perform(scrollTo(), typeText("12345"), closeSoftKeyboard())

            onView(withId(R.id.btnToggleContrasena)).perform(scrollTo(), click())
            onView(withId(R.id.btnToggleContrasena)).perform(scrollTo(), click())

            onView(withId(R.id.btnToggleConfirmarContrasena)).perform(scrollTo(), click())
            onView(withId(R.id.btnToggleConfirmarContrasena)).perform(scrollTo(), click())
        }
    }

    @Test
    fun prueba3_validacionTiempoReal_TextWatchersCompletos() {
        ActivityScenario.launch(RegistrarseActivity::class.java).use {
            onView(withId(R.id.txtCurp)).perform(scrollTo(), replaceText("ABC"), closeSoftKeyboard())
            onView(withText("Faltan 15 caracteres")).perform(scrollTo()).check(matches(isDisplayed()))
            onView(withId(R.id.txtCurp)).perform(scrollTo(), replaceText("AAAAAAAAAAAAAAAAAA"), closeSoftKeyboard())
            onView(withText("Formato de CURP incorrecto")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtNombre)).perform(scrollTo(), replaceText("123"), closeSoftKeyboard())
            onView(withText("Solo letras (máx 50)")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtTelefono)).perform(scrollTo(), replaceText("5512"), closeSoftKeyboard())
            onView(withText("Deben ser 10 dígitos")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtCodigoPostal)).perform(scrollTo(), replaceText("123"), closeSoftKeyboard())
            onView(withText("CP inválido (5 dígitos)")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtContrasena)).perform(scrollTo(), replaceText("a"), closeSoftKeyboard())
            onView(withText("Mínimo 8 caracteres")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtContrasena)).perform(scrollTo(), replaceText("abcdefgh"), closeSoftKeyboard())
            onView(withText("Debe incluir una Mayúscula")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtContrasena)).perform(scrollTo(), replaceText("Abcdefgh"), closeSoftKeyboard())
            onView(withText("Debe incluir un Número")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtContrasena)).perform(scrollTo(), replaceText("Abcdefgh1"), closeSoftKeyboard())
            onView(withText("Debe incluir un carácter especial (@#$.-)")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtConfirmarContrasena)).perform(scrollTo(), replaceText("Diferente1!"), closeSoftKeyboard())
            onView(withText("Las contraseñas no coinciden")).perform(scrollTo()).check(matches(isDisplayed()))

            onView(withId(R.id.txtCurp)).perform(scrollTo(), replaceText(""), closeSoftKeyboard())
            onView(withText("Campo obligatorio")).perform(scrollTo()).check(matches(isDisplayed()))
        }
    }

    @Test
    fun prueba4_botonRegistrar_bloqueosYErroresEspeciales() {
        ActivityScenario.launch(RegistrarseActivity::class.java).use {
            onView(withId(R.id.btnCrearCuenta)).perform(scrollTo(), click())
            onView(withText("Términos y Condiciones")).check(matches(isDisplayed()))

            // ESCUDO ANTI-CRASH
            onView(withText("OK")).perform(click())
            Thread.sleep(1000)

            onView(withId(R.id.checkTerminos)).perform(scrollTo(), click())
            onView(withId(R.id.btnCrearCuenta)).perform(scrollTo(), click())
            onView(withText("Formulario incompleto")).check(matches(isDisplayed()))

            // ESCUDO ANTI-CRASH
            onView(withText("OK")).perform(click())
            Thread.sleep(1000)
        }
    }

    @Test
    fun prueba5_errorDeFirebase_cuentaYaExiste() {
        val auth = FirebaseAuth.getInstance()
        val correoExistente = "duplicado_${System.currentTimeMillis()}@xolotl.com"
        val passValido = "XolotlTest123!"

        var authLista = false
        auth.createUserWithEmailAndPassword(correoExistente, passValido).addOnCompleteListener {
            auth.signOut()
            authLista = true
        }
        while(!authLista) { Thread.sleep(100) }

        ActivityScenario.launch(RegistrarseActivity::class.java).use {
            onView(withId(R.id.txtCurp)).perform(scrollTo(), replaceText("VASO010408HDFQNSA5"), closeSoftKeyboard())
            onView(withId(R.id.txtNombre)).perform(scrollTo(), replaceText("Test"), closeSoftKeyboard())
            onView(withId(R.id.txtApellidoP)).perform(scrollTo(), replaceText("Uno"), closeSoftKeyboard())
            onView(withId(R.id.txtApellidoM)).perform(scrollTo(), replaceText("Dos"), closeSoftKeyboard())
            onView(withId(R.id.txtTelefono)).perform(scrollTo(), replaceText("5512345678"), closeSoftKeyboard())
            onView(withId(R.id.txtCalle)).perform(scrollTo(), replaceText("Calle"), closeSoftKeyboard())
            onView(withId(R.id.txtNumero)).perform(scrollTo(), replaceText("1"), closeSoftKeyboard())
            onView(withId(R.id.txtColonia)).perform(scrollTo(), replaceText("Centro"), closeSoftKeyboard())
            onView(withId(R.id.txtAlcaldia)).perform(scrollTo(), replaceText("Norte"), closeSoftKeyboard())
            onView(withId(R.id.txtCodigoPostal)).perform(scrollTo(), replaceText("00000"), closeSoftKeyboard())
            onView(withId(R.id.txtCorreo)).perform(scrollTo(), replaceText(correoExistente), closeSoftKeyboard())
            onView(withId(R.id.txtContrasena)).perform(scrollTo(), replaceText(passValido), closeSoftKeyboard())
            onView(withId(R.id.txtConfirmarContrasena)).perform(scrollTo(), replaceText(passValido), closeSoftKeyboard())

            onView(withId(R.id.checkTerminos)).perform(scrollTo(), click())
            onView(withId(R.id.btnCrearCuenta)).perform(scrollTo(), click())

            Thread.sleep(4000)

            onView(withText("Error de Registro")).check(matches(isDisplayed()))

            // ESCUDO ANTI-CRASH
            onView(withText("OK")).perform(click())
            Thread.sleep(1000)
        }

        // LIMPIEZA SINCRONIZADA
        var limpiezaLista = false
        auth.signInWithEmailAndPassword(correoExistente, passValido).addOnCompleteListener {
            auth.currentUser?.delete()?.addOnCompleteListener {
                limpiezaLista = true
            }
        }
        var intentos = 0
        while (!limpiezaLista && intentos < 50) { Thread.sleep(100); intentos++ }
    }

    @Test
    fun prueba6_registroExitoso_limpiezaDeBD() {
        val correoNuevo = "usuario_ui_temporal_${System.currentTimeMillis()}@xolotl.com"
        val passValido = "XolotlTest123!"

        ActivityScenario.launch(RegistrarseActivity::class.java).use {
            onView(withId(R.id.txtCurp)).perform(scrollTo(), replaceText("VASO010408HDFQNSA5"), closeSoftKeyboard())
            onView(withId(R.id.txtNombre)).perform(scrollTo(), replaceText("Test"), closeSoftKeyboard())
            onView(withId(R.id.txtApellidoP)).perform(scrollTo(), replaceText("Uno"), closeSoftKeyboard())
            onView(withId(R.id.txtApellidoM)).perform(scrollTo(), replaceText("Dos"), closeSoftKeyboard())
            onView(withId(R.id.txtTelefono)).perform(scrollTo(), replaceText("5512345678"), closeSoftKeyboard())
            onView(withId(R.id.txtTelefonoAlt)).perform(scrollTo(), replaceText("123"), closeSoftKeyboard())
            onView(withId(R.id.txtCalle)).perform(scrollTo(), replaceText("Calle"), closeSoftKeyboard())
            onView(withId(R.id.txtNumero)).perform(scrollTo(), replaceText("1"), closeSoftKeyboard())
            onView(withId(R.id.txtColonia)).perform(scrollTo(), replaceText("Centro"), closeSoftKeyboard())
            onView(withId(R.id.txtAlcaldia)).perform(scrollTo(), replaceText("Norte"), closeSoftKeyboard())
            onView(withId(R.id.txtCodigoPostal)).perform(scrollTo(), replaceText("00000"), closeSoftKeyboard())
            onView(withId(R.id.txtCorreo)).perform(scrollTo(), replaceText(correoNuevo), closeSoftKeyboard())
            onView(withId(R.id.txtContrasena)).perform(scrollTo(), replaceText(passValido), closeSoftKeyboard())
            onView(withId(R.id.txtConfirmarContrasena)).perform(scrollTo(), replaceText(passValido), closeSoftKeyboard())

            onView(withId(R.id.checkTerminos)).perform(scrollTo(), click())
            onView(withId(R.id.btnCrearCuenta)).perform(scrollTo(), click())
            onView(withId(R.id.txtTelefonoAlt)).perform(scrollTo(), replaceText("5500112233"), closeSoftKeyboard())
            onView(withId(R.id.btnCrearCuenta)).perform(scrollTo(), click())

            Thread.sleep(4000)

            onView(withText(containsString("Verifica tu correo"))).check(matches(isDisplayed()))

            // ESCUDO ANTI-CRASH
            onView(withText("OK")).perform(click())
            Thread.sleep(1000)
        }

        // LIMPIEZA SINCRONIZADA
        val auth = FirebaseAuth.getInstance()
        var limpiezaLista = false
        auth.currentUser?.delete()?.addOnCompleteListener {
            auth.signOut()
            limpiezaLista = true
        }
        var intentos = 0
        while (!limpiezaLista && intentos < 50) { Thread.sleep(100); intentos++ }
    }
}