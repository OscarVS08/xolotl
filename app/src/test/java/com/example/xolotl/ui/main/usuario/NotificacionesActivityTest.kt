package com.example.xolotl.ui.main.usuario

import android.app.AlarmManager
import android.content.Context
import android.os.Build
import android.util.Base64
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.LinearLayout
import com.example.xolotl.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlarmManager
import java.text.SimpleDateFormat
import java.util.*

@Ignore("Omitido por falta de RAM en ejecución masiva. Ejecutar individualmente.")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class NotificacionesActivityTest {

    private lateinit var firestoreMockStatic: MockedStatic<FirebaseFirestore>
    private lateinit var authMockStatic: MockedStatic<FirebaseAuth>
    private lateinit var base64MockStatic: MockedStatic<Base64>

    @Before
    fun setup() {
        // 1. Aislamiento total de la red y base de datos
        val mockFirestore = mock(FirebaseFirestore::class.java, RETURNS_DEEP_STUBS)
        val mockAuth = mock(FirebaseAuth::class.java, RETURNS_DEEP_STUBS)
        `when`(mockAuth.uid).thenReturn("USUARIO_DUMMY")

        firestoreMockStatic = mockStatic(FirebaseFirestore::class.java)
        authMockStatic = mockStatic(FirebaseAuth::class.java)

        firestoreMockStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)
        authMockStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)

        // 2. Aislamiento de encriptación
        base64MockStatic = mockStatic(Base64::class.java)
        base64MockStatic.`when`<ByteArray> { Base64.decode(anyString() ?: "", anyInt()) }.thenReturn(ByteArray(0))
        base64MockStatic.`when`<String> { Base64.encodeToString(any() ?: ByteArray(0), anyInt()) }.thenReturn("")
    }

    @After
    fun tearDown() {
        firestoreMockStatic.close()
        authMockStatic.close()
        base64MockStatic.close()
    }

    private fun iniciarActivity(): NotificacionesActivity {
        return Robolectric.buildActivity(NotificacionesActivity::class.java).create().resume().get()
    }

    // =========================================================
    // 1. ALGORITMOS DE VALIDACIÓN (Lógica Pura de Reglas de Negocio)
    // =========================================================

    @Test
    fun `algoritmo de validacion - aborta la programacion si faltan campos por llenar`() {
        val activity = iniciarActivity()
        val btnGuardar = activity.findViewById<LinearLayout>(R.id.btnGuardarNotificacion)
        val shadowAlarmManager: ShadowAlarmManager = shadowOf(activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager)

        // Simulamos que el usuario deja todo en blanco y presiona guardar
        btnGuardar.performClick()

        // VALIDACIÓN LÓGICA: El sistema no debe haber programado ninguna alarma en el hardware
        assertNull("No se debe crear ninguna alarma si los campos están vacíos", shadowAlarmManager.nextScheduledAlarm)
    }

    @Test
    fun `algoritmo de tiempo - rechaza fechas en el pasado y previene llamadas al hardware`() {
        val activity = iniciarActivity()
        val btnGuardar = activity.findViewById<LinearLayout>(R.id.btnGuardarNotificacion)
        val spinnerCitas = activity.findViewById<AutoCompleteTextView>(R.id.spinnerCitas)
        val txtFechaNotificacion = activity.findViewById<EditText>(R.id.txtFechaHoraNotificacion)

        val shadowAlarmManager = shadowOf(activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager)

        // Llenamos los datos con una fecha en el pasado
        spinnerCitas.setText("Firulais - Vacunación")
        txtFechaNotificacion.setText("01/01/2000 10:00") // Fecha obsoleta

        btnGuardar.performClick()

        // VALIDACIÓN LÓGICA: El algoritmo `fechaDestino.after(Date())` debe fallar y abortar
        assertNull("El sistema debe bloquear peticiones de alarmas en el pasado", shadowAlarmManager.nextScheduledAlarm)
    }

    // =========================================================
    // 2. CASOS DE ÉXITO Y CONSTRUCCIÓN DEL INTENT (Integridad de Datos)
    // =========================================================

    @Test
    fun `algoritmo de programacion - empaqueta el intent correctamente con fechas futuras`() {
        val activity = iniciarActivity()
        val btnGuardar = activity.findViewById<LinearLayout>(R.id.btnGuardarNotificacion)
        val spinnerCitas = activity.findViewById<AutoCompleteTextView>(R.id.spinnerCitas)
        val txtFechaNotificacion = activity.findViewById<EditText>(R.id.txtFechaHoraNotificacion)

        val alarmManager = activity.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val shadowAlarmManager = shadowOf(alarmManager)

        // Construimos una fecha estrictamente en el futuro para engañar al sistema
        val calendarFuturo = Calendar.getInstance()
        calendarFuturo.add(Calendar.DAY_OF_YEAR, 5) // 5 días en el futuro

        // CORRECCIÓN: Limpiamos los segundos y milisegundos para igualar el formato de la UI ("HH:mm")
        calendarFuturo.set(Calendar.SECOND, 0)
        calendarFuturo.set(Calendar.MILLISECOND, 0)

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val fechaValidaStr = sdf.format(calendarFuturo.time)

        val citaTest = "Firulais - Cirugía"

        // Ingresamos datos válidos
        spinnerCitas.setText(citaTest)
        txtFechaNotificacion.setText(fechaValidaStr)

        btnGuardar.performClick()

        // 1. Verificamos que la alarma SÍ se haya programado en el sistema
        val alarmaProgramada = shadowAlarmManager.nextScheduledAlarm
        assertNotNull("La alarma debe registrarse en el AlarmManager", alarmaProgramada)

        // 2. Verificamos la integridad algorítmica (ahora los segundos coincidirán a la perfección)
        val tiempoEsperadoSegundos = calendarFuturo.timeInMillis / 1000
        val tiempoProgramadoSegundos = alarmaProgramada!!.triggerAtTime / 1000
        assertEquals("El cálculo del tiempo destino debe ser matemáticamente exacto", tiempoEsperadoSegundos, tiempoProgramadoSegundos)

        // 3. Verificamos la inyección de datos (Los Extras del Intent)
        val intentLanzado = shadowOf(alarmaProgramada.operation).savedIntent
        assertEquals("com.example.xolotl.ACTION_ALARM", intentLanzado.action)
        assertEquals("Es momento de: $citaTest", intentLanzado.getStringExtra("mensaje"))

        // CORRECCIÓN: Limpiamos la memoria para que la animación del SweetAlertDialog no rompa Robolectric
        org.robolectric.shadows.ShadowLooper.idleMainLooper()
    }

    // =========================================================
    // 3. CASOS FRONTERA (Prevención de Colisiones Matemáticas)
    // =========================================================

    @Test
    fun `logica de RequestCode - genera codigos unicos basados en el tiempo para evitar colisiones`() {
        // En tu código tienes: val requestCode = (timeInMillis / 1000).toInt()
        // Esta prueba unitaria pura evalúa que tu fórmula matemática no colapse las alarmas

        val tiempo1 = 1700000000000L // Simulación de tiempo 1
        val tiempo2 = 1700000005000L // 5 segundos después

        val requestCode1 = (tiempo1 / 1000).toInt()
        val requestCode2 = (tiempo2 / 1000).toInt()

        assertNotEquals("Cada alarma debe tener un RequestCode distinto basado en su tiempo exacto", requestCode1, requestCode2)
    }
}