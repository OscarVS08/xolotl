package com.example.xolotl.ui.auth

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.R
import org.hamcrest.Matchers.containsString
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RestablecerContrasenaActivityUITest {

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(RestablecerContrasenaActivity::class.java).use {
            onView(withId(R.id.txtDescripcion)).check(matches(isDisplayed()))
            onView(withId(R.id.txtCorreoRestablecer)).check(matches(isDisplayed()))
            onView(withId(R.id.btnEnviarCorreo)).check(matches(isDisplayed()))
        }
    }

    @Test
    fun prueba2_campoVacio_muestraAlertaRequerido() {
        ActivityScenario.launch(RestablecerContrasenaActivity::class.java).use {
            onView(withId(R.id.btnEnviarCorreo)).perform(click())
            onView(withText("Campo requerido")).check(matches(isDisplayed()))

            // ESCUDO ANTI-CRASH: Clic y Pausa para la animación
            onView(withText("OK")).perform(click())
            Thread.sleep(1000)
        }
    }

    @Test
    fun prueba3_validacionTiempoReal_formatoInvalido_muestraError() {
        ActivityScenario.launch(RestablecerContrasenaActivity::class.java).use {
            onView(withId(R.id.txtCorreoRestablecer)).perform(typeText("a"), closeSoftKeyboard())
            onView(withId(R.id.txtCorreoRestablecer)).perform(replaceText(""), closeSoftKeyboard())

            onView(withId(R.id.txtCorreoRestablecer)).perform(typeText("correo_sin_arroba"), closeSoftKeyboard())
            onView(withText("Ingresa un correo válido")).check(matches(isDisplayed()))

            onView(withId(R.id.btnEnviarCorreo)).perform(click())
            onView(withText("Formato inválido")).check(matches(isDisplayed()))

            // ESCUDO ANTI-CRASH: Clic y Pausa para la animación
            onView(withText("OK")).perform(click())
            Thread.sleep(1000)
        }
    }

    @Test
    fun prueba5_correoValido_muestraExitoYEnviaEnlace() {
        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val correoTemp = "recuperacion_fantasma_${System.currentTimeMillis()}@xolotl.com"
        val passTemp = "XolotlTest123!"

        var authLista = false
        auth.createUserWithEmailAndPassword(correoTemp, passTemp).addOnCompleteListener {
            authLista = true
        }
        while(!authLista) { Thread.sleep(100) }

        ActivityScenario.launch(RestablecerContrasenaActivity::class.java).use {
            onView(withId(R.id.txtCorreoRestablecer)).perform(typeText(correoTemp), closeSoftKeyboard())
            onView(withId(R.id.btnEnviarCorreo)).perform(click())

            Thread.sleep(4000)

            onView(withText(containsString("Correo enviado"))).check(matches(isDisplayed()))

            // ESCUDO ANTI-CRASH: Clic y Pausa para la animación
            onView(withText("OK")).perform(click())
            Thread.sleep(1000)
        }

        // LIMPIEZA SINCRONIZADA
        var limpiezaLista = false
        auth.signInWithEmailAndPassword(correoTemp, passTemp).addOnCompleteListener {
            auth.currentUser?.delete()?.addOnCompleteListener {
                auth.signOut()
                limpiezaLista = true
            }
        }
        var intentos = 0
        while (!limpiezaLista && intentos < 50) { Thread.sleep(100); intentos++ }
    }
}