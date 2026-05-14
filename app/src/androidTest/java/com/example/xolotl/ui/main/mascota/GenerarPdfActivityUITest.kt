package com.example.xolotl.ui.main.mascota

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
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
class GenerarPdfActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val idMascotaPdf = "mascota_pdf_test_123"

    // Intent preparado con el ID de la mascota inyectada
    private val testIntent: Intent
        get() {
            val context = ApplicationProvider.getApplicationContext<Context>()
            return Intent(context, GenerarPdfActivity::class.java).apply {
                putExtra("docId", idMascotaPdf)
            }
        }

    @Before
    fun setUp() {
        Intents.init()

        // 1. Inicio de sesión silencioso
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            var loginTerminado = false
            // ==========================================
            // PON TU CORREO Y CONTRASEÑA DE PRUEBA AQUÍ
            // ==========================================
            auth.signInWithEmailAndPassword("vaquerosantososcar@gmail.com", "D#Oscar08")
                .addOnCompleteListener { loginTerminado = true }
            while (!loginTerminado) { Thread.sleep(100) }
        }

        val uid = auth.uid!!
        var preparacionTerminada = false

        // 2. Inyectamos una mascota temporal con TODOS los datos cifrados
        // Esto garantiza que la función cargarDatosMascotaYTablas() pueda
        // descifrar todo sin lanzar un IllegalBlockSizeException
        val dummyPet = hashMapOf(
            "nombre" to EncryptionUtils.encrypt("Pdf Test Dog"),
            "ruac" to EncryptionUtils.encrypt("RUAC123456"),
            "especie" to EncryptionUtils.encrypt("Perro"),
            "raza" to EncryptionUtils.encrypt("Pug"),
            "sexo" to EncryptionUtils.encrypt("Macho"),
            "fechaNacimiento" to EncryptionUtils.encrypt("01/01/2020"),
            "fechaAdopcion" to EncryptionUtils.encrypt("01/06/2020"),
            "color" to EncryptionUtils.encrypt("Negro"),
            "peso" to EncryptionUtils.encrypt("10"),
            "estatura" to EncryptionUtils.encrypt("30"),
            "alergias" to EncryptionUtils.encrypt("Ninguna"),
            "notas" to EncryptionUtils.encrypt("Mascota de prueba UI"),
            "fotoBase64" to ""
        )

        db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaPdf)
            .set(dummyPet)
            .addOnCompleteListener { preparacionTerminada = true }

        while (!preparacionTerminada) { Thread.sleep(100) }
    }

    @After
    fun tearDown() {
        Intents.release()

        // 3. Limpieza de base de datos
        val uid = FirebaseAuth.getInstance().uid
        if (uid != null) {
            var borrado = false
            db.collection("usuarios").document(uid).collection("mascotas").document(idMascotaPdf)
                .delete()
                .addOnCompleteListener { borrado = true }
            while (!borrado) { Thread.sleep(100) }
        }
    }

    @Test
    fun prueba1_elementosVisualesCarganCorrectamente() {
        ActivityScenario.launch<GenerarPdfActivity>(testIntent)

        // Damos tiempo para que Firestore descargue los datos y los descifre
        Thread.sleep(2000)

        // Verificamos que el nombre descifrado aparezca en el TextView correspondiente
        onView(withId(R.id.txtNombreMascotaTop)).check(matches(isDisplayed()))
        onView(withText("Pdf Test Dog")).perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba2_generarPdf_creaDocumentoYLanzaVisorExterno() {
        ActivityScenario.launch<GenerarPdfActivity>(testIntent)
        Thread.sleep(2000) // Esperamos carga inicial

        // =======================================================
        // BLOQUEO DE INTENT: Interceptamos cualquier intento de
        // abrir un PDF para que la prueba no salga de la app
        // =======================================================
        val intentResult = Instrumentation.ActivityResult(Activity.RESULT_OK, Intent())
        intending(allOf(hasAction(Intent.ACTION_VIEW), hasType("application/pdf"))).respondWith(intentResult)

        // Hacemos clic en el botón de generar PDF
        onView(withId(R.id.btnGenerarPdf)).perform(scrollTo(), click())

        // El procesamiento del Canvas y la escritura del archivo toman tiempo.
        // Esperamos 4 segundos a que se termine de escribir en memoria.
        Thread.sleep(4000)

        // Buscamos el botón de confirmación en tu SweetAlertDialog (UiUtils.mostrarAlertaPdfGenerado)
        // Le damos clic para disparar la función 'abrirPdfConVisor'
        onView(withText("Aceptar")).perform(click())

        // Verificamos matemáticamente que tu código ejecutó el FileProvider correcto
        intended(allOf(
            hasAction(Intent.ACTION_VIEW),
            hasType("application/pdf")
        ))
    }
}