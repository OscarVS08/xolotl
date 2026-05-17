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
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.hamcrest.Matchers.allOf

@RunWith(AndroidJUnit4::class)
class AgregarDesparasitacionActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val idMascotaDesp = "mascota_desparasitacion_test"
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

        // 2. Inyectamos la mascota para que el Spinner de tu UI tenga qué mostrar
        val dummyPet = hashMapOf(
            "nombre" to EncryptionUtils.encrypt(nombreMascota),
            "ruac" to EncryptionUtils.encrypt(idMascotaDesp) // Usamos el ID como RUAC
        )

        db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaDesp)
            .set(dummyPet)
            .addOnCompleteListener { mascotaCreada = true }

        while (!mascotaCreada) { Thread.sleep(100) }
    }

    @After
    fun tearDown() {
        val uid = FirebaseAuth.getInstance().uid ?: return

        // 3. Limpieza de base de datos
        // Borramos primero la desparasitación creada en la prueba y luego la mascota
        db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaDesp)
            .collection("desparasitaciones").get().addOnSuccessListener { docs ->
                for (doc in docs) { doc.reference.delete() }
                db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaDesp).delete()
            }
        Thread.sleep(2000) // Damos margen para que Firebase limpie
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(AgregarDesparasitacionesActivity::class.java)

        onView(withId(R.id.txtTituloDesparasitacion)).check(matches(isDisplayed()))
        onView(withId(R.id.spinnerMascota)).check(matches(isDisplayed()))
        onView(withId(R.id.btnGuardar)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_formularioVacio_muestraErroresVisualesYAlerta() {
        ActivityScenario.launch(AgregarDesparasitacionesActivity::class.java)

        // Clic directo en guardar sin llenar nada
        onView(withId(R.id.btnGuardar)).perform(scrollTo(), click())

        // Tu SweetAlert aparece
        onView(withText("Formulario incompleto")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click()) // Cerramos el modal

        // Verificamos que el error del spinner de mascota se pintó
        onView(withText("Selecciona una mascota")).perform(scrollTo()).check(matches(isDisplayed()))

        // CORRECCIÓN: Para evitar ambigüedad, le decimos a Espresso que busque el texto
        // "Obligatorio" que sea descendiente específico del layout del método.
        onView(
            allOf(
                withText("Obligatorio"),
                isDescendantOfA(withId(R.id.layoutMetodo))
            )
        ).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba3_fechasIncoherentes_muestraErrorEnTiempoReal() {
        val scenario = ActivityScenario.launch(AgregarDesparasitacionesActivity::class.java)

        // Inyectamos las fechas en desorden (la próxima fecha es ANTES que la aplicación)
        scenario.onActivity { activity ->
            activity.findViewById<EditText>(R.id.txtFecha).setText("10/10/2025")
            activity.findViewById<EditText>(R.id.txtProxFecha).setText("01/01/2025")
        }

        // Verificamos que tu TextWatcher (validarRelacionFechas) capturó el error
        // sin necesidad de darle clic a Guardar
        onView(withText("Debe ser posterior a la aplicación")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba4_registroExitoso_guardaEnFirebase() {
        val scenario = ActivityScenario.launch(AgregarDesparasitacionesActivity::class.java)

        // Esperamos a que tu función cargarMascotasUsuario() traiga la lista de Firebase
        Thread.sleep(2500)

        // 1. Seleccionamos a nuestra mascota en el Spinner
        onView(withId(R.id.spinnerMascota)).perform(scrollTo(), click())
        onView(withText(nombreMascota)).inRoot(isPlatformPopup()).perform(click())

        // 2. Llenamos los textos
        onView(withId(R.id.txtMetodo)).perform(scrollTo(), typeText("Pastilla"), closeSoftKeyboard())
        onView(withId(R.id.txtNombre)).perform(scrollTo(), typeText("Bravecto"), closeSoftKeyboard())
        onView(withId(R.id.txtMarca)).perform(scrollTo(), typeText("MSD"), closeSoftKeyboard())

        // 3. Inyectamos fechas válidas saltando el teclado bloqueado
        scenario.onActivity { activity ->
            activity.findViewById<EditText>(R.id.txtFecha).setText("01/01/2025")
            activity.findViewById<EditText>(R.id.txtProxFecha).setText("01/04/2025")
        }

        // 4. Clic en Guardar
        onView(withId(R.id.btnGuardar)).perform(scrollTo(), click())

        // 5. Modal de Confirmación
        // IMPORTANTE: Busca en UiUtils qué texto usa "mostrarConfirmacionDesparasitacion".
        // Normalmente es "Aceptar", "Confirmar" o "Sí, guardar". Aquí usaremos "Aceptar".
        onView(withText("Registrar")).perform(click())

        // 6. Esperamos a que la red mande el dato a Firebase
        Thread.sleep(3000)

        // 7. Verificamos el modal de Éxito final
        onView(withText("Registro exitoso")).check(matches(isDisplayed()))
    }
}