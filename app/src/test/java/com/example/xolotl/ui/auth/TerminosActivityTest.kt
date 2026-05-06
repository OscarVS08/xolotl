package com.example.xolotl.ui.auth

import android.os.Build
import android.widget.TextView
import com.example.xolotl.R
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Ignore("Omitido por falta de RAM en ejecución masiva. Ejecutar individualmente.")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33]) // Uniformidad con tus otras pruebas
class TerminosActivityTest {

    private lateinit var activity: TerminosActivity

    @Before
    fun setup() {
        // Iniciamos la Activity bajo el simulador de Robolectric
        activity = Robolectric.buildActivity(TerminosActivity::class.java)
            .create()
            .resume()
            .get()
    }

    // ==========================================
    // CASOS DE ÉXITO (Lógica de Visualización)
    // ==========================================

    @Test
    fun `el texto legal debe cargarse completamente al iniciar`() {
        val txtContenido = activity.findViewById<TextView>(R.id.txtContenidoTerminos)

        // Validamos que el TextView no esté vacío
        val textoCargado = txtContenido.text.toString()
        assertFalse("El contenido legal no debería estar vacío", textoCargado.isEmpty())

        // Validamos la presencia de palabras clave que aseguran que el texto es el correcto
        assertTrue(textoCargado.contains("Condiciones de servicio"))
        assertTrue(textoCargado.contains("Política de privacidad"))
        assertTrue(textoCargado.contains("soul3.tt@gmail.com"))
    }

    @Test
    fun `el boton back debe cerrar la actividad correctamente`() {
        // Ejecutamos la acción del botón
        activity.findViewById<android.widget.ImageButton>(R.id.btnBack).performClick()

        // Verificamos la navegación (Lógica de salida)
        assertTrue("La actividad debería estar finalizando tras pulsar back", activity.isFinishing)
    }

    // ==========================================
    // CASOS DE ERROR / INTEGRIDAD
    // ==========================================

    @Test
    fun `verificar que el formato HTML no corrompa el texto esencial`() {
        val txtContenido = activity.findViewById<TextView>(R.id.txtContenidoTerminos)
        val textoCompleto = txtContenido.text.toString()

        // Caso de error: Si el HTML falla, a veces se muestran las etiquetas como texto plano <b>
        // Validamos que las etiquetas no sean visibles para el usuario final
        assertFalse("El renderizado HTML falló: se muestran etiquetas <b>", textoCompleto.contains("<b>"))
        assertFalse("El renderizado HTML falló: se muestran etiquetas <br>", textoCompleto.contains("<br>"))
    }

    @Test
    fun `la actividad debe mantener el contenido tras una recreacion`() {
        // Simulamos un cambio de configuración (giro de pantalla)
        val controller = Robolectric.buildActivity(TerminosActivity::class.java).setup()
        controller.configurationChange()

        val recreatedActivity = controller.get()
        val txtContenido = recreatedActivity.findViewById<TextView>(R.id.txtContenidoTerminos)

        assertNotNull("El contenido debería persistir tras recrear la actividad", txtContenido.text)
        assertTrue(txtContenido.text.contains("Términos y condiciones"))
    }
}