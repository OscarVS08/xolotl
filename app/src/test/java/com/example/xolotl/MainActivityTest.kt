package com.example.xolotl

import android.content.Context
import android.os.Build
import android.util.Base64
import android.view.View
import android.view.ViewGroup
import com.example.xolotl.data.models.Citas
import com.example.xolotl.ui.main.usuario.mapas.VisualizarMapaUrgenciasActivity
import com.google.android.material.chip.Chip
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
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@Ignore("Omitido por falta de RAM en ejecución masiva. Ejecutar individualmente.")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class MainActivityTest {

    private lateinit var firestoreMockStatic: MockedStatic<FirebaseFirestore>
    private lateinit var authMockStatic: MockedStatic<FirebaseAuth>
    private lateinit var base64MockStatic: MockedStatic<Base64>

    @Before
    fun setup() {
        val mockFirestore = mock(FirebaseFirestore::class.java, RETURNS_DEEP_STUBS)
        val mockAuth = mock(FirebaseAuth::class.java, RETURNS_DEEP_STUBS)
        `when`(mockAuth.currentUser?.uid).thenReturn("USUARIO_DUMMY")

        firestoreMockStatic = mockStatic(FirebaseFirestore::class.java)
        authMockStatic = mockStatic(FirebaseAuth::class.java)

        firestoreMockStatic.`when`<FirebaseFirestore> { FirebaseFirestore.getInstance() }.thenReturn(mockFirestore)
        authMockStatic.`when`<FirebaseAuth> { FirebaseAuth.getInstance() }.thenReturn(mockAuth)

        base64MockStatic = mockStatic(Base64::class.java)
        base64MockStatic.`when`<ByteArray> { Base64.decode(anyString() ?: "", anyInt()) }.thenReturn(ByteArray(0))
        base64MockStatic.`when`<String> { Base64.encodeToString(any() ?: ByteArray(0), anyInt()) }.thenReturn("")

        val context = RuntimeEnvironment.getApplication()
        val prefs = context.getSharedPreferences("XolotlPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("tourCompletado", false).commit()
    }

    @After
    fun tearDown() {
        firestoreMockStatic.close()
        authMockStatic.close()
        base64MockStatic.close()
    }

    private fun iniciarActivity(): MainActivity {
        return Robolectric.buildActivity(MainActivity::class.java).create().resume().get()
    }

    private fun inyectarListaCitas(activity: MainActivity, citasFalsas: List<Citas>) {
        val campoListaCompleta = MainActivity::class.java.getDeclaredField("listaCitasCompleta")
        campoListaCompleta.isAccessible = true
        val listaInterna = campoListaCompleta.get(activity) as MutableList<Citas>
        listaInterna.clear()
        listaInterna.addAll(citasFalsas)
    }

    private fun ejecutarAlgoritmoPrivado(activity: MainActivity, nombreMetodo: String) {
        val metodo = MainActivity::class.java.getDeclaredMethod(nombreMetodo)
        metodo.isAccessible = true
        metodo.invoke(activity)
    }

    // =========================================================
    // 1. ALGORITMOS DE ORDENAMIENTO DE FECHAS
    // =========================================================

    @Test
    fun `algoritmo de ordenamiento - proximas y lejanas ordenan las fechas cronologicamente`() {
        val activity = iniciarActivity()

        val citasDesordenadas = listOf(
            Citas(horario = "15/10/2026 10:00", nombreMascota = "A"),
            Citas(horario = "01/10/2026 10:00", nombreMascota = "B"),
            Citas(horario = "30/10/2026 10:00", nombreMascota = "C")
        )
        inyectarListaCitas(activity, citasDesordenadas)

        val chipProximas = activity.findViewById<Chip>(R.id.chipProximas)
        val chipLejanas = activity.findViewById<Chip>(R.id.chipLejanas)

        chipProximas.isChecked = true
        chipLejanas.isChecked = false

        ejecutarAlgoritmoPrivado(activity, "aplicarFiltrosYOrden")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        val campoListaRenderizada = MainActivity::class.java.getDeclaredField("listaCitas")
        campoListaRenderizada.isAccessible = true
        var listaRenderizada = campoListaRenderizada.get(activity) as List<Citas>

        assertEquals("01/10/2026 10:00", listaRenderizada[0].horario)
        assertEquals("15/10/2026 10:00", listaRenderizada[1].horario)

        chipProximas.isChecked = false
        chipLejanas.isChecked = true

        ejecutarAlgoritmoPrivado(activity, "aplicarFiltrosYOrden")
        org.robolectric.shadows.ShadowLooper.idleMainLooper()

        listaRenderizada = campoListaRenderizada.get(activity) as List<Citas>

        assertEquals("30/10/2026 10:00", listaRenderizada[0].horario)
        assertEquals("01/10/2026 10:00", listaRenderizada[2].horario)
    }

    // =========================================================
    // 2. LÓGICA DE MÁQUINA DE ESTADOS (Menús)
    // =========================================================

    @Test
    fun `logica de menus - los botones alternan la visibilidad respetando la exclusividad`() {
        val activity = iniciarActivity()

        val btnPrincipal = activity.findViewById<View>(R.id.btnPrincipal)
        val btnMenu = activity.findViewById<View>(R.id.btnMenu)
        val cardMenuPrincipal = activity.findViewById<View>(R.id.cardMenuPrincipal)
        val cardMenuOpciones = activity.findViewById<View>(R.id.cardMenuOpciones)
        val rootFondo = activity.findViewById<ViewGroup>(android.R.id.content).getChildAt(0)

        btnPrincipal.performClick()
        assertEquals(View.VISIBLE, cardMenuPrincipal.visibility)
        assertEquals(View.GONE, cardMenuOpciones.visibility)

        btnMenu.performClick()
        assertEquals(View.VISIBLE, cardMenuOpciones.visibility)
        assertEquals(View.GONE, cardMenuPrincipal.visibility)

        rootFondo.performClick()
        assertEquals(View.GONE, cardMenuPrincipal.visibility)
        assertEquals(View.GONE, cardMenuOpciones.visibility)
    }

    // =========================================================
    // 3. ALGORITMOS DE FILTRADO (Nuevas Pruebas)
    // =========================================================

    @Test
    fun `algoritmo de filtrado - separa correctamente las citas por el nombre de la mascota`() {
        val activity = iniciarActivity()

        // Creamos lista mixta de mascotas
        val citasMixtas = listOf(
            Citas(nombreMascota = "Firulais", horario = "10/10/2026 10:00"),
            Citas(nombreMascota = "Michi", horario = "11/10/2026 10:00"),
            Citas(nombreMascota = "Firulais", horario = "12/10/2026 10:00")
        )
        inyectarListaCitas(activity, citasMixtas)

        // Usamos reflexión para alterar la variable privada 'mascotaSeleccionadaFiltro'
        val campoFiltro = MainActivity::class.java.getDeclaredField("mascotaSeleccionadaFiltro")
        campoFiltro.isAccessible = true
        val campoListaRenderizada = MainActivity::class.java.getDeclaredField("listaCitas")
        campoListaRenderizada.isAccessible = true

        // PRUEBA 1: Filtrar solo "Firulais"
        campoFiltro.set(activity, "Firulais")
        ejecutarAlgoritmoPrivado(activity, "aplicarFiltrosYOrden")

        var listaRenderizada = campoListaRenderizada.get(activity) as List<Citas>
        assertEquals("Debe haber 2 citas de Firulais", 2, listaRenderizada.size)
        assertTrue("Todas las citas deben ser de Firulais", listaRenderizada.all { it.nombreMascota == "Firulais" })

        // PRUEBA 2: Restablecer filtro a "Todas"
        campoFiltro.set(activity, "Todas")
        ejecutarAlgoritmoPrivado(activity, "aplicarFiltrosYOrden")

        listaRenderizada = campoListaRenderizada.get(activity) as List<Citas>
        assertEquals("Al elegir 'Todas' deben aparecer las 3 citas originales", 3, listaRenderizada.size)
    }

    @Test
    fun `algoritmo frontera - el filtro solo aparece si hay mas de 4 citas registradas`() {
        val activity = iniciarActivity()
        val cardFiltros = activity.findViewById<View>(R.id.cardFiltros)

        val citasPocas = List(4) { Citas(nombreMascota = "Mascota $it") }
        inyectarListaCitas(activity, citasPocas)
        ejecutarAlgoritmoPrivado(activity, "evaluarVisibilidadFiltro")
        assertEquals(View.GONE, cardFiltros.visibility)

        val citasMuchas = List(5) { Citas(nombreMascota = "Mascota $it") }
        inyectarListaCitas(activity, citasMuchas)
        ejecutarAlgoritmoPrivado(activity, "evaluarVisibilidadFiltro")
        assertEquals(View.VISIBLE, cardFiltros.visibility)
    }

    // =========================================================
    // 4. LÓGICA DE TIEMPO DEL EASTER EGG (Nuevas Pruebas)
    // =========================================================

    @Test
    fun `logica secreta - el easter egg se activa exactamente al 5to clic consecutivo`() {
        val activity = iniciarActivity()
        val cardHeader = activity.findViewById<View>(R.id.cardHeader)

        repeat(4) { cardHeader.performClick() }
        assertNull(ShadowDialog.getLatestDialog())

        cardHeader.performClick()
        val dialogSecreto = ShadowDialog.getLatestDialog()
        assertNotNull(dialogSecreto)
    }

    @Test
    fun `logica secreta - el contador se reinicia si pasan mas de 2 segundos entre clics`() {
        val activity = iniciarActivity()
        val cardHeader = activity.findViewById<View>(R.id.cardHeader)

        // Hacemos 4 clics
        repeat(4) { cardHeader.performClick() }

        // Hack de tiempo: Modificamos la variable lastClickTime para simular que el usuario se distrajo por 3 segundos
        val campoTiempo = MainActivity::class.java.getDeclaredField("lastClickTime")
        campoTiempo.isAccessible = true
        val tiempoActual = System.currentTimeMillis()
        campoTiempo.set(activity, tiempoActual - 3000L) // 3000 ms = 3 segundos en el pasado

        // Hacemos el 5to clic. Como pasaron 3 segundos, tu algoritmo debe reiniciar la cuenta a 1.
        cardHeader.performClick()

        assertNull("El easter egg NO debe aparecer porque pasaron más de 2 segundos y se reinició la cuenta", ShadowDialog.getLatestDialog())
    }

    // =========================================================
    // 5. LÓGICA DE NAVEGACIÓN Y ENRUTAMIENTO (Nuevas Pruebas)
    // =========================================================

    @Test
    fun `logica de enrutamiento - el boton de urgencias limpia menus y lanza el intent correcto`() {
        val activity = iniciarActivity()

        // Forzamos los menús a estar abiertos
        activity.findViewById<View>(R.id.cardMenuPrincipal).visibility = View.VISIBLE

        // Hacemos clic en Emergencia
        activity.findViewById<View>(R.id.btnEmergencia).performClick()

        // 1. Verificamos tu regla de seguridad: "Por seguridad, cerramos cualquier menú antes de saltar"
        assertEquals("El menú debe ocultarse por seguridad", View.GONE, activity.findViewById<View>(R.id.cardMenuPrincipal).visibility)

        // 2. Verificamos que se lanzó el Intent hacia la pantalla correcta
        val shadowActivity = shadowOf(activity)
        val intentLanzado = shadowActivity.nextStartedActivity

        assertNotNull("Debe dispararse un Intent", intentLanzado)
        assertEquals("Debe navegar al Mapa de Urgencias", VisualizarMapaUrgenciasActivity::class.java.name, intentLanzado.component?.className)
    }
}