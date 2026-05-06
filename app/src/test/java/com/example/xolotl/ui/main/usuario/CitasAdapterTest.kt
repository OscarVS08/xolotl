package com.example.xolotl.ui.main

import android.content.Intent
import android.os.Build
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.data.models.Citas
import com.example.xolotl.ui.main.usuario.CitasAdapter
import com.example.xolotl.ui.main.usuario.EditarCitasActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@Ignore("Omitido por falta de RAM en ejecución masiva. Ejecutar individualmente.")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class CitasAdapterTest {

    private lateinit var adapter: CitasAdapter
    private lateinit var dummyActivity: AppCompatActivity
    private lateinit var listaCitas: List<Citas>

    @Before
    fun setup() {
        // 1. Levantamos una Activity simulada con Robolectric para que sirva como 'Context'
        dummyActivity = Robolectric.buildActivity(AppCompatActivity::class.java).create().get()

        // 2. Creamos datos falsos para alimentar el adaptador
        val cita1 = Citas(
            idC = "CITA_123",
            ruacMascota = "RUAC_ABC",
            nombreMascota = "Firulais",
            servicio = "Consulta General",
            horario = "15/10/2026 10:00"
        )
        val cita2 = Citas(
            idC = "CITA_456",
            ruacMascota = "RUAC_DEF",
            nombreMascota = "Michi",
            servicio = "Vacunación",
            horario = "20/10/2026 12:00"
        )

        listaCitas = listOf(cita1, cita2)

        // 3. Inicializamos el Adapter
        adapter = CitasAdapter(listaCitas, dummyActivity)
    }

    // =========================================================
    // 1. PRUEBAS DE ESTRUCTURA Y CONTEO
    // =========================================================

    @Test
    fun `getItemCount devuelve el tamano exacto de la lista enviada`() {
        assertEquals("El adaptador debe reportar exactamente 2 elementos", 2, adapter.itemCount)
    }

    // =========================================================
    // 2. PRUEBAS DE BINDING (Mapeo de Datos a la UI)
    // =========================================================

    @Test
    fun `onBindViewHolder mapea correctamente los datos de la cita a los TextViews`() {
        // Simulamos el contenedor padre (RecyclerView)
        val parent = FrameLayout(dummyActivity)

        // Creamos un ViewHolder vacío usando el método real de tu Adapter
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Forzamos el enlazado (Binding) de la primera cita (Posición 0)
        adapter.onBindViewHolder(viewHolder, 0)

        // Validamos que los textos se hayan asignado correctamente a la vista
        assertEquals("Firulais", viewHolder.txtMascota.text.toString())
        assertEquals("Consulta General", viewHolder.txtServicio.text.toString())
        assertEquals("15/10/2026 10:00", viewHolder.txtFecha.text.toString())
    }

    // =========================================================
    // 3. PRUEBAS DE NAVEGACIÓN E INTENTS (Botón Editar)
    // =========================================================

    @Test
    fun `click en boton Editar genera el Intent correcto con todos los extras requeridos`() {
        val parent = FrameLayout(dummyActivity)
        val viewHolder = adapter.onCreateViewHolder(parent, 0)

        // Enlazamos la cita 0 ("Firulais")
        adapter.onBindViewHolder(viewHolder, 0)

        // Simulamos que el usuario presiona el botón de Editar
        viewHolder.btnEditar.performClick()

        // Atrapamos el Intent que el Adapter intentó lanzar
        val shadowActivity = shadowOf(dummyActivity)
        val startedIntent = shadowActivity.nextStartedActivity

        // 1. Validamos que el Intent exista y apunte a la Activity correcta
        assertNotNull("Debería lanzarse un Intent", startedIntent)
        assertEquals(EditarCitasActivity::class.java.name, startedIntent.component?.className)

        // 2. Validamos que TODOS los datos de la cita viajen en los "Extras" del Intent
        assertEquals("CITA_123", startedIntent.getStringExtra("id"))
        assertEquals("RUAC_ABC", startedIntent.getStringExtra("ruacMascota"))
        assertEquals("Consulta General", startedIntent.getStringExtra("servicio"))
        assertEquals("15/10/2026 10:00", startedIntent.getStringExtra("fechaHora"))
        assertEquals("Firulais", startedIntent.getStringExtra("nombreMascota"))
    }
}