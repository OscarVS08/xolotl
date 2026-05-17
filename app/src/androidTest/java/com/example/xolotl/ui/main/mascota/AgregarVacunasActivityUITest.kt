package com.example.xolotl.ui.main.mascota

import android.widget.EditText
import androidx.test.core.app.ActivityScenario
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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AgregarVacunasActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val idMascotaVacuna = "mascota_vacuna_test_ui"
    private val nombreMascota = "Perro Prueba UI"

    @Before
    fun setUp() {
        // 1. Inicio de sesión garantizado
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

        // 2. Inyectamos la mascota temporal para el Spinner
        val dummyPet = hashMapOf(
            "nombre" to EncryptionUtils.encrypt(nombreMascota),
            "ruac" to EncryptionUtils.encrypt(idMascotaVacuna),
        )

        db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaVacuna)
            .set(dummyPet)
            .addOnCompleteListener { mascotaCreada = true }

        while (!mascotaCreada) { Thread.sleep(100) }
    }

    @After
    fun tearDown() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        // 3. Limpieza de base de datos
        // Borramos las vacunas creadas y luego la mascota
        db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaVacuna)
            .collection("vacunas").get().addOnSuccessListener { docs ->
                for (doc in docs) { doc.reference.delete() }
                db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaVacuna).delete()
            }
        Thread.sleep(2000)
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(AgregarVacunasActivity::class.java)

        // Verificamos los títulos y los dos spinners
        onView(withText("Registro de Vacunas")).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerMascota)).check(matches(isDisplayed()))
        onView(withId(R.id.autoCompleteUnidad)).perform(scrollTo()).check(matches(isDisplayed()))
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_formularioVacio_muestraErroresVisualesYAlerta() {
        ActivityScenario.launch(AgregarVacunasActivity::class.java)

        // Clic directo en guardar sin llenar nada
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo(), click())

        // SweetAlert de validación
        onView(withText("Formulario incompleto")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click())

        // Verificamos que se pintó el error en los TextLayouts específicos (para evitar ambigüedad)
        onView(withText("Selecciona una mascota")).perform(scrollTo()).check(matches(isDisplayed()))

        onView(
            allOf(
                withText("Obligatorio"),
                isDescendantOfA(withId(R.id.layoutNombre))
            )
        ).perform(scrollTo()).check(matches(isDisplayed()))

        onView(
            allOf(
                withText("Ingresa la cantidad"),
                isDescendantOfA(withId(R.id.layoutDosis))
            )
        ).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba3_fechasIncoherentes_muestraErrorEnTiempoReal() {
        val scenario = ActivityScenario.launch(AgregarVacunasActivity::class.java)

        // Inyectamos las fechas al revés para probar el TextWatcher
        scenario.onActivity { activity ->
            activity.findViewById<EditText>(R.id.txtFecha).setText("10/10/2025")
            activity.findViewById<EditText>(R.id.txtProxFecha).setText("01/01/2025")
        }

        // El error debería aparecer mágicamente sin tocar el botón guardar
        onView(withText("Debe ser posterior a la fecha de aplicación")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba4_registroExitoso_guardaEnFirebase() {
        val scenario = ActivityScenario.launch(AgregarVacunasActivity::class.java)

        // Damos tiempo a Firebase de descargar la mascota inyectada
        Thread.sleep(3000)

        // 1. Seleccionar Mascota
        onView(withId(R.id.spinnerMascota)).perform(scrollTo(), click())

        // PAUSA ESTABILIZADORA: Esperamos que la animación del menú termine de pintar la lista
        Thread.sleep(1000)

        onView(withText(nombreMascota)).inRoot(isPlatformPopup()).perform(click())

        // 2. Llenar Textos (Usando replaceText para evitar problemas con la tilde en 'á')
        onView(withId(R.id.txtNombre)).perform(scrollTo(), replaceText("Antirrábica"), closeSoftKeyboard())
        onView(withId(R.id.txtMarca)).perform(scrollTo(), typeText("Zoetis"), closeSoftKeyboard())
        onView(withId(R.id.txtDosis)).perform(scrollTo(), typeText("2.5"), closeSoftKeyboard())

        // 3. Cambiar el Spinner de Unidades
        onView(withId(R.id.autoCompleteUnidad)).perform(scrollTo(), click())

        // PAUSA ESTABILIZADORA: Para el segundo menú desplegable
        Thread.sleep(500)

        onView(withText("ml")).inRoot(isPlatformPopup()).perform(click())

        // 4. Inyectar fechas
        scenario.onActivity { activity ->
            activity.findViewById<EditText>(R.id.txtFecha).setText("01/01/2025")
            activity.findViewById<EditText>(R.id.txtProxFecha).setText("01/01/2026")
        }

        // 5. Guardar
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo(), click())

        // 6. Modal de Confirmación
        onView(withText("Registrar")).perform(click())

        // 7. Tiempo para que la transacción de Firebase se complete
        Thread.sleep(3000)

        // 8. Verificar éxito
        onView(withText("Registro exitoso")).check(matches(isDisplayed()))
    }
}