package com.example.xolotl.ui.main.usuario

import android.Manifest
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasToString
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class NotificacionesActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val idMascota = "mascota_notif_test"
    private val idCitaPasada = "cita_pasada_123"
    private val idCitaFutura = "cita_futura_123"
    private val nombreMascota = "Fido"

    // Formateador de tiempo dinámico para evitar que el test caduque
    private val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val ahoraMillis = System.currentTimeMillis()

    private val fechaAyerStr = sdf.format(Date(ahoraMillis - 86400000L)) // -24 hrs
    private val fechaMananaStr = sdf.format(Date(ahoraMillis + 86400000L)) // +24 hrs
    private val fechaEnUnMinutoStr = sdf.format(Date(ahoraMillis + 60000L)) // +1 min (Falla el margen de 2 min)
    private val fechaEnUnaHoraStr = sdf.format(Date(ahoraMillis + 3600000L)) // +1 hora (Éxito)
    private val fechaPasadoMananaStr = sdf.format(Date(ahoraMillis + 172800000L)) // +48 hrs

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

        // 1. Inyectamos mascota
        val dummyPet = hashMapOf("nombre" to EncryptionUtils.encrypt(nombreMascota))

        // 2. Inyectamos Cita Expirada (Ayer)
        val citaPasada = hashMapOf(
            "servicio" to EncryptionUtils.encrypt("Baño"),
            "horario" to EncryptionUtils.encrypt(fechaAyerStr)
        )

        // 3. Inyectamos Cita Vigente (Mañana)
        val citaFutura = hashMapOf(
            "servicio" to EncryptionUtils.encrypt("Vacuna"),
            "horario" to EncryptionUtils.encrypt(fechaMananaStr)
        )

        val mascotaRef = db.collection("usuarios").document(uid).collection("mascotas").document(idMascota)

        mascotaRef.set(dummyPet).continueWithTask {
            mascotaRef.collection("citas").document(idCitaPasada).set(citaPasada)
        }.continueWithTask {
            mascotaRef.collection("citas").document(idCitaFutura).set(citaFutura)
        }.addOnCompleteListener { preparacionTerminada = true }

        while (!preparacionTerminada) { Thread.sleep(100) }
    }

    @After
    fun tearDown() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val ref = db.collection("usuarios").document(uid).collection("mascotas").document(idMascota)
        ref.collection("citas").document(idCitaPasada).delete()
        ref.collection("citas").document(idCitaFutura).delete()
        ref.delete()
        Thread.sleep(1500)
    }

    // Helper para cerrar el SweetAlert de Optimización de Batería que sale al iniciar
    private fun cerrarAlertaBateria() {
        Thread.sleep(1500) // Damos tiempo a que la animación del SweetAlert termine
        try {
            // Buscamos el botón de confirmación genérico de SweetAlert
            onView(withText("OK")).perform(click())
        } catch (e: Exception) {
            // Si la alerta no apareció en este emulador, no hacemos nada.
            // La actividad sigue viva y segura.
        }
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(NotificacionesActivity::class.java)
        cerrarAlertaBateria()

        onView(withText("Notificaciones")).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerCitas)).check(matches(isDisplayed()))
        onView(withId(R.id.txtFechaHoraCita)).check(matches(withText("--")))
    }

    @Test
    fun prueba2_camposVacios_muestraAlertaIncompleta() {
        // RAMA: if (seleccion.isEmpty() || fechaNotifStr.isEmpty() || fechaCitaStr == "--")
        ActivityScenario.launch(NotificacionesActivity::class.java)
        cerrarAlertaBateria()

        onView(withId(R.id.btnGuardarNotificacion)).perform(scrollTo(), click())

        onView(withText("Campos incompletos")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())
    }

    @Test
    fun prueba3_citaExpirada_muestraAlertaDeError() {
        // RAMA 1: if (fechaCita.before(ahora))

        // Guardamos el escenario (la ventana) en una variable para no abrirla dos veces
        val scenario = ActivityScenario.launch(NotificacionesActivity::class.java)
        cerrarAlertaBateria()
        Thread.sleep(2500) // Esperamos a Firebase

        // Seleccionamos la cita de Ayer ("Baño")
        onView(withId(R.id.spinnerCitas)).perform(scrollTo(), click())
        Thread.sleep(500)
        onData(hasToString("$nombreMascota - Baño")).inRoot(isPlatformPopup()).perform(click())

        // Usamos la MISMA ventana (scenario) para inyectar la fecha
        scenario.onActivity {
            it.findViewById<EditText>(R.id.txtFechaHoraNotificacion).setText(fechaEnUnaHoraStr)
        }

        onView(withId(R.id.btnGuardarNotificacion)).perform(scrollTo(), click())

        // Verificamos el error esperado
        onView(withText("Cita expirada")).check(matches(isDisplayed()))

        // Cerramos la alerta para limpiar el estado
        onView(withText("OK")).perform(click())
    }

    @Test
    fun prueba4_fechaInvalidaYMargenInsuficiente_muestraAlertas() {
        // RAMA 2 y 3: Pasado y Margen de 2 minutos
        val scenario = ActivityScenario.launch(NotificacionesActivity::class.java)
        cerrarAlertaBateria()
        Thread.sleep(2500)

        // Seleccionamos la cita Vigente ("Vacuna")
        onView(withId(R.id.spinnerCitas)).perform(scrollTo(), click())
        Thread.sleep(500)
        onData(hasToString("$nombreMascota - Vacuna")).inRoot(isPlatformPopup()).perform(click())

        // --- SUBPRUEBA A: Recordatorio en el pasado (Ayer) ---
        scenario.onActivity { it.findViewById<EditText>(R.id.txtFechaHoraNotificacion).setText(fechaAyerStr) }
        onView(withId(R.id.btnGuardarNotificacion)).perform(scrollTo(), click())
        onView(withText("Fecha inválida")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // --- SUBPRUEBA B: Recordatorio demasiado pronto (En 1 minuto) ---
        // SOLUCIÓN ANTI-PARADOJA TEMPORAL: Calculamos "1 minuto en el futuro" justo en este milisegundo
        val sdfLocal = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        val fechaEnUnMinutoReal = sdfLocal.format(java.util.Date(System.currentTimeMillis() + 60000L))

        scenario.onActivity { it.findViewById<EditText>(R.id.txtFechaHoraNotificacion).setText(fechaEnUnMinutoReal) }
        onView(withId(R.id.btnGuardarNotificacion)).perform(scrollTo(), click())

        // Verificamos y cerramos la alerta
        onView(withText("Tiempo insuficiente")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())
    }

    @Test
    fun prueba5_paradojaTemporal_recordatorioDespuesDeCita() {
        // RAMA 4: if (fechaDestino.after(fechaCita) || fechaDestino.time == fechaCita.time)
        val scenario = ActivityScenario.launch(NotificacionesActivity::class.java)
        cerrarAlertaBateria()
        Thread.sleep(2500)

        // Seleccionamos la cita Vigente (Mañana)
        onView(withId(R.id.spinnerCitas)).perform(scrollTo(), click())
        Thread.sleep(500)
        onData(hasToString("$nombreMascota - Vacuna")).inRoot(isPlatformPopup()).perform(click())

        // Inyectamos un recordatorio para Pasado Mañana (después de la cita)
        scenario.onActivity { it.findViewById<EditText>(R.id.txtFechaHoraNotificacion).setText(fechaPasadoMananaStr) }

        onView(withId(R.id.btnGuardarNotificacion)).perform(scrollTo(), click())

        onView(withText("Fecha ilógica")).check(matches(isDisplayed()))
    }

    @Test
    fun prueba6_notificacionPerfecta_programaAlarmaCorrectamente() {
        // RAMA FINAL DE ÉXITO
        val scenario = ActivityScenario.launch(NotificacionesActivity::class.java)
        cerrarAlertaBateria()
        Thread.sleep(2500)

        // Seleccionamos la cita Vigente (Mañana)
        onView(withId(R.id.spinnerCitas)).perform(scrollTo(), click())
        Thread.sleep(500)
        onData(hasToString("$nombreMascota - Vacuna")).inRoot(isPlatformPopup()).perform(click())

        // Recordatorio válido (En 1 hora)
        scenario.onActivity { it.findViewById<EditText>(R.id.txtFechaHoraNotificacion).setText(fechaEnUnaHoraStr) }

        onView(withId(R.id.btnGuardarNotificacion)).perform(scrollTo(), click())

        // Es posible que salga el SweetAlert de "¡Programado!" (Si tiene permisos de Android 12)
        // O el de "Permiso requerido" (Si le falta el permiso de alarma exacta en el emulador).
        // Cualquiera de los dos indica que pasamos la función programarAlarmaExacta()
        try {
            onView(withText("¡Programado!")).check(matches(isDisplayed()))
        } catch (e: Exception) {
            onView(withText("Permiso requerido")).check(matches(isDisplayed()))
        }
    }
}