package com.example.xolotl.ui.main.usuario

import android.content.Context
import android.content.Intent
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.hasToString
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditarCitasActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val idMascota = "mascota_editar_cita_123"
    private val idCita = "cita_editar_123"
    private val nombreMascota = "Perro Edición"
    private val ruacMascotaOriginal = "RUAC_EDIT_001"

    // Intent preparado con los datos obligatorios para que la Activity no se cierre
    private val validIntent: Intent
        get() {
            val context = ApplicationProvider.getApplicationContext<Context>()
            return Intent(context, EditarCitasActivity::class.java).apply {
                putExtra("id", idCita)
                putExtra("ruacMascota", idMascota)
            }
        }

    @Before
    fun setUp() {
        val auth = FirebaseAuth.getInstance()
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

        // 1. Inyectamos la mascota
        val dummyPet = hashMapOf(
            "nombre" to EncryptionUtils.encrypt(nombreMascota),
            "ruac" to EncryptionUtils.encrypt(ruacMascotaOriginal)
        )

        // 2. Inyectamos la cita preexistente a editar
        val dummyCita = hashMapOf(
            "servicio" to EncryptionUtils.encrypt("Vacunación"),
            "horario" to EncryptionUtils.encrypt("31/12/2030 10:00"),
            "notas" to EncryptionUtils.encrypt("Cita original generada por UI Test")
        )

        db.collection("usuarios").document(uid).collection("mascotas").document(idMascota)
            .set(dummyPet)
            .continueWithTask {
                db.collection("usuarios").document(uid)
                    .collection("mascotas").document(idMascota)
                    .collection("citas").document(idCita).set(dummyCita)
            }
            .addOnCompleteListener { preparacionTerminada = true }

        while (!preparacionTerminada) { Thread.sleep(100) }
    }

    @After
    fun tearDown() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        db.collection("usuarios").document(uid).collection("mascotas").document(idMascota)
            .collection("citas").document(idCita).delete()
        db.collection("usuarios").document(uid).collection("mascotas").document(idMascota).delete()
        Thread.sleep(1500)
    }

    @Test
    fun prueba1_intentoInvalido_cierraActivity() {
        // RAMA: if (idCita.isEmpty() || ruacMascota.isEmpty()) { finish() }
        val emptyIntent = Intent(ApplicationProvider.getApplicationContext(), EditarCitasActivity::class.java)
        val scenario = ActivityScenario.launch<EditarCitasActivity>(emptyIntent)

        // Como la actividad se destruye casi instantáneamente en el onCreate,
        // no podemos asomarnos a ella. Simplemente verificamos que su estado
        // final sea "DESTROYED".
        assert(scenario.state == androidx.lifecycle.Lifecycle.State.DESTROYED)
    }

    @Test
    fun prueba2_cargaDeDatosExistentes_llenaFormularioCorrectamente() {
        ActivityScenario.launch<EditarCitasActivity>(validIntent)

        // Esperamos a que Firebase descargue la cita y la mascota
        Thread.sleep(3000)

        // Verificamos que los datos preexistentes cifrados se hayan desencriptado en la UI
        onView(withId(R.id.txtNombreMascota)).perform(scrollTo()).check(matches(withText(nombreMascota)))
        onView(withId(R.id.txtRuac)).perform(scrollTo()).check(matches(withText(ruacMascotaOriginal)))

        onView(withId(R.id.txtServicio)).perform(scrollTo()).check(matches(withText("Vacunación")))
        onView(withId(R.id.txtFechaHora)).perform(scrollTo()).check(matches(withText("31/12/2030 10:00")))
    }

    @Test
    fun prueba3_camposVacios_muestraErroresYAlertaAtencion() {
        ActivityScenario.launch<EditarCitasActivity>(validIntent)
        Thread.sleep(3000)

        // Borramos los datos existentes
        onView(withId(R.id.txtServicio)).perform(scrollTo(), replaceText(""), closeSoftKeyboard())
        onView(withId(R.id.txtFechaHora)).perform(scrollTo(), replaceText(""), closeSoftKeyboard())

        // Intentamos guardar
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo(), click())

        // Verificamos el SweetAlert de "Atención"
        onView(withText("Atención")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click()) // Cerramos el modal de error

        // Verificamos los mensajes de error en los Layouts
        onView(
            allOf(
                withText("Selecciona un servicio"),
                isDescendantOfA(withId(R.id.layoutServicio))
            )
        ).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba4_logicaServicioOtro_habilitaEscrituraLibre() {
        ActivityScenario.launch<EditarCitasActivity>(validIntent)
        Thread.sleep(2500)

        // RAMA: if (parent.getItemAtPosition(position).toString() == "Otro")
        onView(withId(R.id.txtServicio)).perform(scrollTo(), click())
        Thread.sleep(500)
        onData(hasToString("Otro")).inRoot(isPlatformPopup()).perform(click())

        // Como seleccionamos "Otro", el keyListener cambió y ahora podemos escribir libremente
        onView(withId(R.id.txtServicio)).perform(scrollTo(), replaceText("Terapia Física"), closeSoftKeyboard())
        onView(withId(R.id.txtServicio)).check(matches(withText("Terapia Física")))
    }

    @Test
    fun prueba5_actualizacionExitosa_modificaFirebase() {
        val scenario = ActivityScenario.launch<EditarCitasActivity>(validIntent)
        Thread.sleep(3000) // Carga inicial

        // 1. Modificamos el servicio
        onView(withId(R.id.txtServicio)).perform(scrollTo(), click())
        Thread.sleep(500)
        onData(hasToString("Cirugía")).inRoot(isPlatformPopup()).perform(click())
        closeSoftKeyboard()

        // 2. Modificamos la fecha a otra futura
        scenario.onActivity { activity ->
            activity.findViewById<EditText>(R.id.txtFechaHora).setText("01/01/2031 08:00")
        }

        // 3. Modificamos las notas
        onView(withId(R.id.txtNotas)).perform(scrollTo(), replaceText("Llegar en ayunas"), closeSoftKeyboard())

        // 4. Guardar cambios
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo(), click())

        // 5. Modal de Confirmación (Verificamos que aparezca el título y le damos al botón "Actualizar")
        onView(withText("Confirmar cambios")).check(matches(isDisplayed()))
        onView(withText("Actualizar")).perform(click())

        // 6. Esperar la red
        Thread.sleep(3000)

        // 7. Modal de Éxito
        onView(withText("Actualizado")).check(matches(isDisplayed()))
    }
}