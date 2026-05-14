package com.example.xolotl.ui.main.mascota

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
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
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom

@RunWith(AndroidJUnit4::class)
class MascotasActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val idMascotaPrueba = "mascota_test_ui_123"
    private val nombreMascotaPrueba = "Perro Prueba UI"

    @Before
    fun setUp() {
        Intents.init()

        // 1. Iniciar sesión silenciosamente
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

        // 2. Inyectar una mascota temporal directamente en Firestore para asegurar que haya datos
        val uid = auth.uid!!
        var mascotaCreada = false
        val dummyPet = hashMapOf(
            "nombre" to EncryptionUtils.encrypt(nombreMascotaPrueba),
            "fotoBase64" to "" // Mandamos vacío para que active la lógica del R.drawable.foto_blanco
        )

        db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaPrueba)
            .set(dummyPet)
            .addOnCompleteListener { mascotaCreada = true }

        while (!mascotaCreada) { Thread.sleep(100) }
    }

    @After
    fun tearDown() {
        Intents.release()

        // 3. Limpiar la base de datos: Borramos la mascota temporal
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            var mascotaBorrada = false
            db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaPrueba)
                .delete()
                .addOnCompleteListener { mascotaBorrada = true }

            while (!mascotaBorrada) { Thread.sleep(100) }
        }
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch(MascotasActivity::class.java)

        // Verificamos el título
        onView(withText("Mis Mascotas")).check(matches(isDisplayed()))

        // Damos 2 segundos para que la lista se descargue de Firebase
        Thread.sleep(2000)

        // Verificamos que nuestra mascota inyectada aparezca en la lista
        onView(withText(nombreMascotaPrueba)).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_botonEditar_lanzaActivityCorrecta() {
        ActivityScenario.launch(MascotasActivity::class.java)
        Thread.sleep(2000)

        // Restringimos la búsqueda: El padre en común debe ser EXCLUSIVAMENTE una MaterialCardView
        onView(
            allOf(
                withId(R.id.btnEditarMascota),
                isDescendantOfA(
                    allOf(
                        isAssignableFrom(com.google.android.material.card.MaterialCardView::class.java),
                        hasDescendant(withText(nombreMascotaPrueba))
                    )
                )
            )
        ).perform(scrollTo(), click())

        intended(hasComponent(EditarMascotasActivity::class.java.name))
    }

    @Test
    fun prueba3_botonPdf_lanzaActivityCorrecta() {
        ActivityScenario.launch(MascotasActivity::class.java)
        Thread.sleep(2000)

        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent())
        intending(hasComponent(GenerarPdfActivity::class.java.name)).respondWith(intentResult)

        onView(
            allOf(
                withId(R.id.btnPdfMascota),
                isDescendantOfA(
                    allOf(
                        isAssignableFrom(com.google.android.material.card.MaterialCardView::class.java),
                        hasDescendant(withText(nombreMascotaPrueba))
                    )
                )
            )
        ).perform(scrollTo(), click())

        intended(hasComponent(GenerarPdfActivity::class.java.name))
    }

    @Test
    fun prueba4_botonEliminar_muestraAlertaDeConfirmacion() {
        ActivityScenario.launch(MascotasActivity::class.java)
        Thread.sleep(2000)

        onView(
            allOf(
                withId(R.id.btnEliminarMascota),
                isDescendantOfA(
                    allOf(
                        isAssignableFrom(com.google.android.material.card.MaterialCardView::class.java),
                        hasDescendant(withText(nombreMascotaPrueba))
                    )
                )
            )
        ).perform(scrollTo(), click())

        onView(withText("¿Eliminar a $nombreMascotaPrueba?")).check(matches(isDisplayed()))

        onView(withText("Cancelar")).perform(click())
    }
}