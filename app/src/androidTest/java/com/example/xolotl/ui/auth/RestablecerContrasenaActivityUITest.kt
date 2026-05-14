package com.example.xolotl.ui.auth

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.R
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RestablecerContrasenaActivityUITest {

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(RestablecerContrasenaActivity::class.java)

        // Verificamos que los textos, el campo y el botón existan en pantalla
        onView(withId(R.id.txtDescripcion)).check(matches(isDisplayed()))
        onView(withId(R.id.txtCorreoRestablecer)).check(matches(isDisplayed()))
        onView(withId(R.id.btnEnviarCorreo)).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_campoVacio_muestraAlertaRequerido() {
        ActivityScenario.launch(RestablecerContrasenaActivity::class.java)

        // Clic en enviar sin escribir nada
        onView(withId(R.id.btnEnviarCorreo)).perform(click())

        // Verificamos el SweetAlert de campo vacío
        onView(withText("Campo requerido")).check(matches(isDisplayed()))
        onView(withText("Por favor, escribe tu correo electrónico para continuar.")).check(matches(isDisplayed()))
    }

    @Test
    fun prueba3_validacionTiempoReal_formatoInvalido_muestraError() {
        ActivityScenario.launch(RestablecerContrasenaActivity::class.java)

        // Escribimos un correo mal formateado
        onView(withId(R.id.txtCorreoRestablecer)).perform(typeText("correo_sin_arroba"), closeSoftKeyboard())

        // Verificamos que el TextInputLayout muestre el mensaje dinámico
        onView(withText("Ingresa un correo válido")).check(matches(isDisplayed()))
    }

    /* Prueba descontinuada por función de protección de Firebase que envía mensaje de
    * exito independiente de si el correo existe o no, como medida de seguridad */
    /*
    @Test
    fun prueba4_correoNoExiste_muestraErrorDeFirebase() {
        ActivityScenario.launch(RestablecerContrasenaActivity::class.java)

        // Usamos un correo con formato válido pero que sabemos que NO existe en tu base
        val correoFalso = "no_existo_12345@xolotl.com"

        onView(withId(R.id.txtCorreoRestablecer)).perform(typeText(correoFalso), closeSoftKeyboard())
        onView(withId(R.id.btnEnviarCorreo)).perform(click())

        // Esperamos a que Firebase responda (3 segundos)
        Thread.sleep(3000)

        // Verificamos la traducción del error "user-not-found"
        onView(withText("Error de recuperación")).check(matches(isDisplayed()))
        onView(withText("No existe ninguna cuenta registrada con este correo.")).check(matches(isDisplayed()))
    }*/

    @Test
    fun prueba5_correoValido_muestraExitoYEnviaEnlace() {
        ActivityScenario.launch(RestablecerContrasenaActivity::class.java)

        // ==========================================
        //  CAMBIA ESTE CORREO POR UNO REAL DE TU BASE DE DATOS
        // Firebase enviará un correo real de restablecimiento a esta bandeja.
        // ==========================================
        val correoReal = "vaquerosantososcar@gmail.com"

        onView(withId(R.id.txtCorreoRestablecer)).perform(typeText(correoReal), closeSoftKeyboard())
        onView(withId(R.id.btnEnviarCorreo)).perform(click())

        // Esperamos a que Firebase procese el envío
        Thread.sleep(3000)

        // Verificamos el SweetAlert de éxito
        onView(withText("¡Correo enviado!")).check(matches(isDisplayed()))
    }
}