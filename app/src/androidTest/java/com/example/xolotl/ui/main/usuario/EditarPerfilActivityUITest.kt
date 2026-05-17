package com.example.xolotl.ui.main.usuario

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
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
class EditarPerfilActivityUITest {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userPwd = "TuContrasenaFuerte123!"

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

        // Inyectamos un perfil completo para que cargarDatos() llene la pantalla
        val dummyProfile = hashMapOf(
            "curp" to EncryptionUtils.encrypt("CURP1234567890ABCD"),
            "nombre" to EncryptionUtils.encrypt("Usuario"),
            "apellidoP" to EncryptionUtils.encrypt("Prueba"),
            "apellidoM" to EncryptionUtils.encrypt("Test"),
            "telefono" to EncryptionUtils.encrypt("5512345678"),
            "telefonoAlt" to EncryptionUtils.encrypt("5587654321"),
            "calle" to EncryptionUtils.encrypt("Avenida Siempre Viva"),
            "numero" to EncryptionUtils.encrypt("742"),
            "colonia" to EncryptionUtils.encrypt("Springfield"),
            "alcaldia" to EncryptionUtils.encrypt("Norte"),
            "codigoPostal" to EncryptionUtils.encrypt("12345")
        )

        db.collection("usuarios").document(uid).set(dummyProfile)
            .addOnCompleteListener { preparacionTerminada = true }

        while (!preparacionTerminada) { Thread.sleep(100) }
    }

    // No usamos @After para borrar el documento del usuario porque rompería otras pruebas
    // que dependan del usuario autenticado.

    @Test
    fun prueba1_cargaDeDatosExistentes_llenaFormularioCorrectamente() {
        ActivityScenario.launch(EditarPerfilActivity::class.java)
        Thread.sleep(3000) // Esperamos a Firebase

        // Verificamos que los datos descifrados estén en la UI
        onView(withId(R.id.txtCurp)).perform(scrollTo()).check(matches(withText("CURP1234567890ABCD")))
        onView(withId(R.id.txtNombre)).perform(scrollTo()).check(matches(withText("Usuario")))
        onView(withId(R.id.txtTelefono)).perform(scrollTo()).check(matches(withText("5512345678")))
        onView(withId(R.id.txtCodigoPostal)).perform(scrollTo()).check(matches(withText("12345")))
    }

    @Test
    fun prueba2_botonesDeVisibilidadDeContrasena_cambianEstado() {
        ActivityScenario.launch(EditarPerfilActivity::class.java)
        Thread.sleep(1500)

        // RAMA: setupPasswordToggle()
        onView(withId(R.id.txtContrasena)).perform(scrollTo(), typeText("12345"), closeSoftKeyboard())

        // Clic para mostrar contraseña
        onView(withId(R.id.btnToggleContrasena)).perform(scrollTo(), click())
        // Clic para ocultar de nuevo
        onView(withId(R.id.btnToggleContrasena)).perform(scrollTo(), click())

        // Lo mismo para confirmar contraseña
        onView(withId(R.id.txtConfirmarContrasena)).perform(scrollTo(), typeText("12345"), closeSoftKeyboard())
        onView(withId(R.id.btnToggleConfirmarContrasena)).perform(scrollTo(), click())
    }

    @Test
    fun prueba3_erroresDeValidacionDinamica_telefonosYCodigos() {
        ActivityScenario.launch(EditarPerfilActivity::class.java)
        Thread.sleep(2000)

        // RAMA: Teléfono incompleto
        onView(withId(R.id.txtTelefono)).perform(scrollTo(), replaceText("55123"), closeSoftKeyboard())
        onView(allOf(withText("Deben ser exactamente 10 dígitos (llevas 5)"), isDescendantOfA(withId(R.id.layoutTelefono))))
            .perform(scrollTo()).check(matches(isDisplayed()))

        // RAMA: CP incompleto
        onView(withId(R.id.txtCodigoPostal)).perform(scrollTo(), replaceText("123"), closeSoftKeyboard())
        onView(allOf(withText("Deben ser 5 dígitos"), isDescendantOfA(withId(R.id.layoutCodigoPostal))))
            .perform(scrollTo()).check(matches(isDisplayed()))

        // RAMA: Calle muy corta
        onView(withId(R.id.txtCalle)).perform(scrollTo(), replaceText("Sol"), closeSoftKeyboard())
        onView(allOf(withText("Calle muy corta"), isDescendantOfA(withId(R.id.layoutCalle))))
            .perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba4_erroresDeContrasena_evaluacionDeFuerza() {
        ActivityScenario.launch(EditarPerfilActivity::class.java)
        Thread.sleep(1500)

        // RAMA: Contraseña débil (Falta símbolo, número, etc)
        onView(withId(R.id.txtContrasena)).perform(scrollTo(), replaceText("debilit"), closeSoftKeyboard())
        onView(allOf(withText("Mínimo 8 caracteres"), isDescendantOfA(withId(R.id.layoutContrasena))))
            .perform(scrollTo()).check(matches(isDisplayed()))

        onView(withId(R.id.txtContrasena)).perform(scrollTo(), replaceText("contrasenalarga"), closeSoftKeyboard())
        onView(allOf(withText("Falta una Mayúscula"), isDescendantOfA(withId(R.id.layoutContrasena))))
            .perform(scrollTo()).check(matches(isDisplayed()))

        // RAMA: Contraseñas no coinciden
        onView(withId(R.id.txtConfirmarContrasena)).perform(scrollTo(), replaceText("otraCosa"), closeSoftKeyboard())
        onView(allOf(withText("Las contraseñas no coinciden"), isDescendantOfA(withId(R.id.layoutConfirmarContrasena))))
            .perform(scrollTo()).check(matches(isDisplayed()))
    }

    @Test
    fun prueba5_formularioVacio_bloqueaGuardado() {
        ActivityScenario.launch(EditarPerfilActivity::class.java)
        Thread.sleep(2000)

        // Borramos un campo vital
        onView(withId(R.id.txtNombre)).perform(scrollTo(), replaceText(""), closeSoftKeyboard())

        // Intentamos guardar
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo(), click())

        // RAMA: Validar formulario completo = false
        onView(withText("Atención")).check(matches(isDisplayed()))
        onView(withText("OK")).perform(click()) // Cerramos el modal
    }

    @Test
    fun prueba6_eliminarCuenta_disparaModalYCancela() {
        ActivityScenario.launch(EditarPerfilActivity::class.java)
        Thread.sleep(1500)

        // RAMA: confirmarEliminarCuenta()
        onView(withId(R.id.btnEliminarCuenta)).perform(scrollTo(), click())

        // Verificamos que se abrió el modal de advertencia extremo
        onView(withText("¿BORRAR CUENTA?")).check(matches(isDisplayed()))

        // ¡SÚPER IMPORTANTE! Le damos a "Cancelar"
        // Si le diéramos a borrar, destruiríamos el usuario de Firebase Auth
        // y romperíamos absolutamente todas las pruebas de la suite que necesiten inicio de sesión.
        onView(withText("Cancelar")).perform(click())
    }

    @Test
    fun prueba7_guardadoExitoso_modificaFirebase() {
        ActivityScenario.launch(EditarPerfilActivity::class.java)
        Thread.sleep(2500)

        // Modificamos un dato
        onView(withId(R.id.txtTelefono)).perform(scrollTo(), replaceText("5500001111"), closeSoftKeyboard())

        // Guardar
        onView(withId(R.id.btnGuardarCambios)).perform(scrollTo(), click())

        // Modal de confirmación
        onView(withText("¿Guardar cambios?")).check(matches(isDisplayed()))
        onView(withText("Guardar")).perform(click())

        Thread.sleep(3000) // Tiempo de red

        // Éxito
        onView(withText("¡Éxito!")).check(matches(isDisplayed()))
    }
}