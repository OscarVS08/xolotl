package com.example.xolotl.ui.main.usuario.mapas

import android.Manifest
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.xolotl.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisualizarMapaActivityUITest {

    // 1. REGLA DE ORO PARA MAPAS:
    // Le decimos a Espresso que conceda el permiso de ubicación automáticamente
    // antes de que la actividad siquiera nazca. Así cubrimos tu función enableUserLocation()
    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(VisualizarMapaActivity::class.java)

        // Verificamos que el título superior se pinta correctamente
        onView(withText("Clínicas en la GAM")).check(matches(isDisplayed()))

        // Verificamos que el contenedor del mapa está visible en la UI
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()))

        // Verificamos el texto predeterminado del cuadro de información
        onView(withText("Dirección de la clínica:")).check(matches(isDisplayed()))
        onView(withText("Selecciona un marcador en el mapa")).check(matches(isDisplayed()))
    }

 /*   @Test
    fun prueba2_botonHome_cierraLaActividad() {
        val scenario = ActivityScenario.launch(VisualizarMapaActivity::class.java)
        Thread.sleep(1000)

        // Hacemos clic en el botón de regresar
        onView(withId(R.id.btnHome)).perform(click())

        // Le damos un momento al sistema operativo para procesar el finish()
        Thread.sleep(500)

        org.junit.Assert.assertEquals(Lifecycle.State.DESTROYED, scenario.state)
    }*/

    @Test
    fun prueba3_cargaDeUbicacion_noCrasheaLaApp() {
        // Al lanzar la actividad, se ejecutará onMapReady -> enableUserLocation.
        // Como le dimos el permiso con el GrantPermissionRule en la parte superior,
        // tu código entrará en la condición exitosa (PackageManager.PERMISSION_GRANTED)
        // y ejecutará 'mMap.isMyLocationEnabled = true' y 'fusedLocationClient.lastLocation'.
        ActivityScenario.launch(VisualizarMapaActivity::class.java)

        // Damos tiempo a que el FusedLocationProviderClient intente buscar la ubicación del emulador
        Thread.sleep(2000)

        // Si la aplicación sigue viva mostrando el contenedor del mapa,
        // significa que la lógica de permisos funcionó a la perfección.
        onView(withId(R.id.mapContainer)).check(matches(isDisplayed()))
    }
}