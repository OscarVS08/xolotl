package com.example.xolotl.ui.auth

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
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
class InicioActivityUITest {

    @Before
    fun setUp() {
        // Inicializamos el interceptor de Intents (para saber a qué pantalla viajamos)
        Intents.init()

        // POR SEGURIDAD: Cerramos cualquier sesión de Firebase que se haya quedado
        // abierta en el emulador por pruebas anteriores, para no saltar a MainActivity
        FirebaseAuth.getInstance().signOut()
    }

    @After
    fun tearDown() {
        // Limpiamos los Intents al terminar cada prueba
        Intents.release()
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        // 1. Lanzamos la Actividad
        ActivityScenario.launch(InicioActivity::class.java)

        // 2. Verificamos que el título y los botones estén visibles en pantalla
        onView(withId(R.id.txtTituloLogo)).check(matches(isDisplayed()))
        onView(withId(R.id.txtTituloLogo)).check(matches(withText("Xólotl")))

        onView(withId(R.id.btnIniciarSesion)).check(matches(isDisplayed()))
        onView(withId(R.id.btnRegistrarse)).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_botonIniciarSesion_navegaAIniciarSesionActivity() {
        ActivityScenario.launch(InicioActivity::class.java)

        // Simula el clic del usuario en el botón
        onView(withId(R.id.btnIniciarSesion)).perform(click())

        // Verifica que la app intentó abrir la actividad "IniciarSesionActivity"
        intended(hasComponent(IniciarSesionActivity::class.java.name))
    }

    @Test
    fun prueba3_botonRegistrarse_muestraAlertaYPermiteCancelar() {
        ActivityScenario.launch(InicioActivity::class.java)

        // Clic en registrarse
        onView(withId(R.id.btnRegistrarse)).perform(click())

        // Verifica que el diálogo de SweetAlert aparece buscando su texto
        onView(withText("Requisito Importante")).check(matches(isDisplayed()))
        onView(withText("Para usar Xólotl, es obligatorio contar con el RUAC de tu mascota. ¿Deseas continuar?")).check(matches(isDisplayed()))

        // Simula hacer clic en el botón de cancelar
        onView(withText("Cancelar")).perform(click())

        // (El diálogo se cierra y la app se queda en InicioActivity, no se lanza ningún Intent)
        // Espresso automáticamente espera a que termine la animación
    }

    @Test
    fun prueba4_botonRegistrarse_navegaARegistrarseActivity_alConfirmar() {
        ActivityScenario.launch(InicioActivity::class.java)

        // Clic en registrarse
        onView(withId(R.id.btnRegistrarse)).perform(click())

        // Clic en "Sí, lo tengo"
        onView(withText("Sí, lo tengo")).perform(click())

        // IMPORTANTE: Tienes un postDelayed de 300ms en tu código.
        // Aunque Espresso suele esperar automáticamente a la UI, a veces las animaciones
        // de SweetAlert van en otro hilo. Ponemos un pequeño margen de seguridad.
        Thread.sleep(500)

        // Verifica que viajamos a la pantalla de registro
        intended(hasComponent(RegistrarseActivity::class.java.name))
    }
}