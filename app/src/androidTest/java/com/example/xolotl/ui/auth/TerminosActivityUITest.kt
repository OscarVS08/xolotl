package com.example.xolotl.ui.auth

import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.R
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TerminosActivityUITest {

    @Test
    fun prueba1_elementosVisualesYTextoCarganCorrectamente() {
        ActivityScenario.launch(TerminosActivity::class.java)

        // Verificamos que el botón de regresar y el contenedor de texto estén visibles en pantalla
        onView(withId(R.id.btnBack)).check(matches(isDisplayed()))
        onView(withId(R.id.txtContenidoTerminos)).check(matches(isDisplayed()))

        // Verificamos que el HTML se haya renderizado buscando un fragmento clave del texto
        onView(withId(R.id.txtContenidoTerminos)).check(matches(withText(containsString("Condiciones de servicio"))))
        onView(withId(R.id.txtContenidoTerminos)).check(matches(withText(containsString("soul3.tt@gmail.com"))))
    }

    @Test
    fun prueba2_botonRegresar_cierraLaActividad() {
        val scenario = ActivityScenario.launch(TerminosActivity::class.java)

        // Hacemos clic en el botón de la casita
        onView(withId(R.id.btnBack)).perform(click())

        // Espera estratégica post-click
        Thread.sleep(500)

        // En lugar de intentar entrar a la actividad destruida,
        // le preguntamos directamente al escenario si el ciclo de vida terminó.
        org.junit.Assert.assertEquals(Lifecycle.State.DESTROYED, scenario.state)
    }
}