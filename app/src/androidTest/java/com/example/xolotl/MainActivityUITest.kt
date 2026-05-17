package com.example.xolotl

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.anyIntent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.ui.auth.InicioActivity
import com.example.xolotl.ui.main.mascota.AgregarDesparasitacionesActivity
import com.example.xolotl.ui.main.mascota.AgregarMascotaActivity
import com.example.xolotl.ui.main.mascota.AgregarVacunasActivity
import com.example.xolotl.ui.main.mascota.MascotasActivity
import com.example.xolotl.ui.main.usuario.AgregarCitasActivity
import com.example.xolotl.ui.main.usuario.EditarPerfilActivity
import com.example.xolotl.ui.main.usuario.NotificacionesActivity
import com.example.xolotl.ui.main.usuario.mapas.VisualizarMapaActivity
import com.example.xolotl.ui.main.usuario.mapas.VisualizarMapaUrgenciasActivity
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasToString
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val idMascota = "mascota_main_test"
    private val nombreMascota = "XolotlTest"

    @Before
    fun setUp() {
        if (auth.currentUser == null) {
            var loginTerminado = false
            // ==========================================
            // ⚠️ PON TU CORREO Y CONTRASEÑA DE PRUEBA AQUÍ ⚠️
            // ==========================================
            auth.signInWithEmailAndPassword("vaquerosantososcar@gmail.com", "D#Oscar08")
                .addOnCompleteListener { loginTerminado = true }
            while (!loginTerminado) { Thread.sleep(100) }
        }

        val uid = auth.uid!!
        var preparacionTerminada = false

        // 1. Configuramos el SharedPreferences por defecto a FALSE para no estorbar con el Onboarding
        val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("XolotlPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("tourCompletado", false).commit()

        // 2. Inyectamos Perfil de Usuario para probar cargarNombreUsuario()
        val dummyProfile = hashMapOf(
            "nombre" to EncryptionUtils.encrypt("Dueño"),
            "apellidoP" to EncryptionUtils.encrypt("Feliz")
        )
        db.collection("usuarios").document(uid).set(dummyProfile)

        // 3. Inyectamos 5 citas para FORZAR que se muestre el contenedor de Filtros (evaluarVisibilidadFiltro)
        val mascotaRef = db.collection("usuarios").document(uid).collection("mascotas").document(idMascota)
        mascotaRef.set(hashMapOf("nombre" to EncryptionUtils.encrypt(nombreMascota)))

        for (i in 1..5) {
            val cita = hashMapOf(
                "servicio" to EncryptionUtils.encrypt("Servicio $i"),
                "horario" to EncryptionUtils.encrypt("0$i/01/2030 10:00")
            )
            mascotaRef.collection("citas").document("cita_$i").set(cita)
        }

        // Damos tiempo a Firebase de asimilar la inyección masiva
        Thread.sleep(3000)
        preparacionTerminada = true
        while (!preparacionTerminada) { Thread.sleep(100) }
    }

    @After
    fun tearDown() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val mascotaRef = db.collection("usuarios").document(uid).collection("mascotas").document(idMascota)
        for (i in 1..5) { mascotaRef.collection("citas").document("cita_$i").delete() }
        mascotaRef.delete()
        Thread.sleep(1000)
    }

    @Test
    fun prueba1_onboarding_primeraVezEjecutaTourVisual() {
        // RAMA: if (esPrimeraVez)
        // Alteramos las preferencias para engañar a la app
        val prefs = ApplicationProvider.getApplicationContext<Context>().getSharedPreferences("XolotlPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("tourCompletado", true).commit()

        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1000)

        // Verificamos que la preferencia cambió a false inmediatamente después
        assert(!prefs.getBoolean("tourCompletado", true))

        // Regresamos a false por seguridad
        prefs.edit().putBoolean("tourCompletado", false).commit()
    }

    @Test
    fun prueba2_cargaInicial_saludoYFiltrosVisibles() {
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(3000)

        // Verificamos cargarNombreUsuario()
        onView(withId(R.id.txtTituloLogo)).check(matches(withText("Hola Dueño Feliz")))

        // Verificamos evaluarVisibilidadFiltro() -> Como hay 5 citas, debe ser VISIBLE
        onView(withId(R.id.cardFiltros)).check(matches(isDisplayed()))
    }

    @Test
    fun prueba3_logicaDeMenus_toggleYCierreAlTocarFondo() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            Thread.sleep(2000)

            // ESCUDO ACTIVADO: Si el robot accidentalmente toca un botón del menú al intentar
            // tocar el fondo, bloqueamos la navegación para que no se salga del MainActivity.
            Intents.init()
            intending(anyIntent()).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

            // Forzamos el estado inicial de los menús
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.cardMenuOpciones).visibility = View.GONE
                activity.findViewById<View>(R.id.cardMenuPrincipal).visibility = View.GONE
            }
            Thread.sleep(500)

            // 1. Abrimos menú de opciones (Hamburguesa)
            onView(withId(R.id.btnMenu)).perform(click())
            onView(withId(R.id.cardMenuOpciones)).check(matches(isDisplayed()))

            // 2. Tocamos el botón central (+)
            onView(withId(R.id.btnPrincipal)).perform(click())
            onView(withId(R.id.cardMenuOpciones)).check(matches(withEffectiveVisibility(Visibility.GONE)))
            onView(withId(R.id.cardMenuPrincipal)).check(matches(isDisplayed()))

            // 3. Tocamos el botón de menú de nuevo
            onView(withId(R.id.btnMenu)).perform(click())
            onView(withId(R.id.cardMenuPrincipal)).check(matches(withEffectiveVisibility(Visibility.GONE)))
            onView(withId(R.id.cardMenuOpciones)).check(matches(isDisplayed()))

            // 4. Tocamos el fondo (recycler) para cerrar todo.
            // Gracias al escudo, si Espresso presiona "Notificaciones" por error al centro, no pasa nada.
            onView(withId(R.id.recyclerCitas)).perform(click())
            onView(withId(R.id.cardMenuOpciones)).check(matches(withEffectiveVisibility(Visibility.GONE)))
            onView(withId(R.id.cardMenuPrincipal)).check(matches(withEffectiveVisibility(Visibility.GONE)))

            Intents.release()
        }
    }

    @Test
    fun prueba4_navegacionIntents_botonesCentralesYMapa() {
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1500)

        // INICIAMOS EL INTERCEPTOR DE INTENTS
        Intents.init()
        intending(anyIntent()).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        // Botón Principal 1 (Desparasitaciones)
        onView(withId(R.id.btnPrincipal)).perform(click())
        onView(withId(R.id.btnCentralOpcion1)).perform(click())
        intended(hasComponent(AgregarDesparasitacionesActivity::class.java.name))

        // Botón Principal 2 (Vacunas)
        onView(withId(R.id.btnPrincipal)).perform(click())
        onView(withId(R.id.btnCentralOpcion2)).perform(click())
        intended(hasComponent(AgregarVacunasActivity::class.java.name))

        // Botón Principal 3 (Mascota)
        onView(withId(R.id.btnPrincipal)).perform(click())
        onView(withId(R.id.btnCentralOpcion3)).perform(click())
        intended(hasComponent(AgregarMascotaActivity::class.java.name))

        // Botón Principal 4 (Citas)
        onView(withId(R.id.btnPrincipal)).perform(click())
        onView(withId(R.id.btnCentralOpcion4)).perform(click())
        intended(hasComponent(AgregarCitasActivity::class.java.name))

        // Botón Emergencia (Directo)
        onView(withId(R.id.btnEmergencia)).perform(click())
        intended(hasComponent(VisualizarMapaUrgenciasActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun prueba5_navegacionIntents_menuDeOpciones() {
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1500)

        Intents.init()
        intending(anyIntent()).respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))

        // Opción 1: Notificaciones
        onView(withId(R.id.btnMenu)).perform(click())
        onView(withId(R.id.btnOpcion1)).perform(click())
        intended(hasComponent(NotificacionesActivity::class.java.name))

        // Opción 2: Perfil
        onView(withId(R.id.btnMenu)).perform(click())
        onView(withId(R.id.btnOpcion2)).perform(click())
        intended(hasComponent(EditarPerfilActivity::class.java.name))

        // Opción 3: Mapa Normal
        onView(withId(R.id.btnMenu)).perform(click())
        onView(withId(R.id.btnOpcion3)).perform(click())
        intended(hasComponent(VisualizarMapaActivity::class.java.name))

        // Opción 4: Editar Mascotas
        onView(withId(R.id.btnMenu)).perform(click())
        onView(withId(R.id.btnOpcion4)).perform(click())
        intended(hasComponent(MascotasActivity::class.java.name))

        Intents.release()
    }

    @Test
    fun prueba6_filtrosDeCitas_cambioDeMascotaYOrden() {
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(3000)

        // Filtrar por mascota específica
        onView(withId(R.id.spinnerFiltroMascota)).perform(click())
        Thread.sleep(500)
        onData(hasToString(nombreMascota)).inRoot(isPlatformPopup()).perform(click())

        // Cambiar orden a Lejanas
        onView(withId(R.id.chipLejanas)).perform(click())

        // Regresar orden a Próximas
        onView(withId(R.id.chipProximas)).perform(click())
    }

    @Test
    fun prueba7_easterEgg_muestraDialogo() {
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1500)

        // 5 Clics rápidos al Header
        for (i in 1..5) {
            onView(withId(R.id.cardHeader)).perform(click())
        }

        // Verificamos que se abrió el diálogo
        onView(withId(R.id.btnCerrarEasterEggContainer)).check(matches(isDisplayed()))
        onView(withId(R.id.btnCerrarEasterEggContainer)).perform(click())
    }

    @Test
    fun prueba8_cerrarSesion_limpiaAuthYVuelveAlInicio() {
        ActivityScenario.launch(MainActivity::class.java)
        Thread.sleep(1500)

        Intents.init()

        // Botón Cerrar Sesión
        onView(withId(R.id.btnMenu)).perform(click())
        onView(withId(R.id.btnOpcion5)).perform(click())

        // Modal de confirmación
        onView(withText("Cerrar sesión")).check(matches(isDisplayed()))
        onView(withText("Sí, salir")).perform(click())

        // Verificamos que nos manda al InicioActivity
        intended(hasComponent(InicioActivity::class.java.name))

        Intents.release()
    }
}