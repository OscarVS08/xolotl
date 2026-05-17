package com.example.xolotl.ui.main.usuario

import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isPlatformPopup
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.Espresso.onData
import org.hamcrest.Matchers.hasToString
import org.hamcrest.Matchers.allOf

@RunWith(AndroidJUnit4::class)
class AgregarCitasActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val idMascotaCita = "mascota_cita_test_ui"
    private val nombreMascota = "Perro Prueba UI"

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
        var mascotaCreada = false

        // Inyectamos mascota para que los diccionarios (mapaRuacRealVisual y mapaDocIdFirebase) se llenen
        val dummyPet = hashMapOf(
            "nombre" to EncryptionUtils.encrypt(nombreMascota),
            "ruac" to EncryptionUtils.encrypt("RUAC_SECRETO_123")
        )

        db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaCita)
            .set(dummyPet)
            .addOnCompleteListener { mascotaCreada = true }

        while (!mascotaCreada) { Thread.sleep(100) }
    }

    @After
    fun tearDown() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        // Limpiamos subcolección de citas y luego el documento principal
        db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaCita)
            .collection("citas").get().addOnSuccessListener { docs ->
                for (doc in docs) { doc.reference.delete() }
                db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaCita).delete()
            }
        Thread.sleep(2000)
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(AgregarCitasActivity::class.java)

        onView(withText("Registro de Citas")).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerMascota)).check(matches(isDisplayed()))
        onView(withId(R.id.txtServicio)).perform(scrollTo()).check(matches(isDisplayed()))

        // El campo "Otro" debe estar oculto al inicio
        onView(withId(R.id.layoutOtroServicio)).check(matches(not(isDisplayed())))
    }

    @Test
    fun prueba2_logicaDinamica_mostrarYOcultarCampoOtroServicio() {
        ActivityScenario.launch(AgregarCitasActivity::class.java)

        // 1. Abrimos menú de servicios y tocamos "Otro"
        onView(withId(R.id.txtServicio)).perform(scrollTo(), click())
        Thread.sleep(500)
        // onData le dice a Espresso "Busca en la lista oculta hasta que encuentres el texto Otro"
        onData(hasToString("Otro")).inRoot(isPlatformPopup()).perform(click())

        // 2. Verificamos que el campo apareció mágicamente (Branch true)
        onView(withId(R.id.layoutOtroServicio)).check(matches(isDisplayed()))
        closeSoftKeyboard() // Tu código abre el teclado automáticamente, lo cerramos

        // 3. Seleccionamos un servicio normal
        onView(withId(R.id.txtServicio)).perform(scrollTo(), click())
        Thread.sleep(500)
        onView(withText("Vacunación")).inRoot(isPlatformPopup()).perform(click())

        // 4. Verificamos que se ocultó (Branch false)
        onView(withId(R.id.layoutOtroServicio)).check(matches(not(isDisplayed())))
    }

    @Test
    fun prueba3_formularioVacio_muestraErroresVisualesYAlerta() {
        ActivityScenario.launch(AgregarCitasActivity::class.java)

        // Clic directo en Guardar
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo(), click())

        // Confirmamos SweetAlert
        onView(withText("Formulario incompleto")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // Verificamos errores usando ancestros para evitar ambigüedades
        onView(withText("Selecciona una mascota")).perform(scrollTo()).check(matches(isDisplayed()))

        onView(
            allOf(
                withText("Campo obligatorio"),
                isDescendantOfA(withId(R.id.layoutServicio))
            )
        ).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba4_fechaEnElPasado_muestraErrorEnTiempoReal() {
        val scenario = ActivityScenario.launch(AgregarCitasActivity::class.java)

        // Inyectamos fecha vieja
        scenario.onActivity { activity ->
            activity.findViewById<EditText>(R.id.txtFechaHora).setText("01/01/2000 12:00")
        }

        // Verificamos respuesta del TextWatcher
        onView(withText("La cita no puede ser en el pasado")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba5_registroExitosoConServicioPersonalizado_guardaEnFirebase() {
        val scenario = ActivityScenario.launch(AgregarCitasActivity::class.java)
        Thread.sleep(3000) // Esperamos a Firebase

        // 1. Seleccionar mascota
        onView(withId(R.id.spinnerMascota)).perform(scrollTo(), click())
        Thread.sleep(500)
        onView(withText(nombreMascota)).inRoot(isPlatformPopup()).perform(click())

        // 2. Comprobar que el RUAC se descifró y se mostró
        onView(withId(R.id.txtRuac)).perform(scrollTo()).check(matches(withText("RUAC_SECRETO_123")))

        // 3. Activar el servicio "Otro"
        onView(withId(R.id.txtServicio)).perform(scrollTo(), click())
        Thread.sleep(500)
        onData(hasToString("Otro")).inRoot(isPlatformPopup()).perform(click())
        closeSoftKeyboard()

        // 4. Llenar campo extra de servicio (usamos replaceText por seguridad)
        onView(withId(R.id.txtOtroServicio)).perform(scrollTo(), replaceText("Acupuntura"), closeSoftKeyboard())

        // 5. Inyectar fecha futura
        scenario.onActivity { activity ->
            activity.findViewById<EditText>(R.id.txtFechaHora).setText("31/12/2030 15:30")
        }

        // 6. Llenar notas
        onView(withId(R.id.txtNotas)).perform(scrollTo(), replaceText("Traer historial clínico"), closeSoftKeyboard())

        // 7. Guardar
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo(), click())

        // 8. Confirmar en el modal de UiUtils
        onView(withText("Agendar")).perform(click())

        Thread.sleep(3000) // Tiempo de subida a Firebase

        // 9. Verificar éxito
        onView(withText("¡Éxito!")).check(matches(isDisplayed()))
    }
}