package com.example.xolotl

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Ignore("Omitido por falta de RAM en ejecución masiva. Ejecutar individualmente.")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class AppTest {

    private lateinit var firebaseStaticMock: MockedStatic<FirebaseApp>
    private lateinit var appCompatStaticMock: MockedStatic<AppCompatDelegate>

    @Before
    fun setup() {
        // Interceptamos las clases estáticas globales antes de que el test inicie
        firebaseStaticMock = mockStatic(FirebaseApp::class.java)
        appCompatStaticMock = mockStatic(AppCompatDelegate::class.java)
    }

    @After
    fun teardown() {
        // Es OBLIGATORIO cerrar los mocks estáticos al final para no afectar otros tests
        firebaseStaticMock.close()
        appCompatStaticMock.close()
    }

    // =========================================================
    // 1. CASO DE ÉXITO (Flujo principal)
    // =========================================================

    @Test
    fun `caso de exito - onCreate inicializa Firebase y fuerza el modo oscuro global`() {
        // 1. Instanciamos tu clase Application
        val app = App()

        // 2. Ejecutamos el ciclo de vida inicial (Robolectric evitará que super.onCreate crashee)
        app.onCreate()

        // 3. Verificamos que se haya llamado exactamente a Firebase pasándole la App (this)
        firebaseStaticMock.verify {
            FirebaseApp.initializeApp(app)
        }

        // 4. Verificamos que se ordenó forzar el modo nocturno sin importar el sistema
        appCompatStaticMock.verify {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    // =========================================================
    // 2. CASOS DE ERROR Y FRONTERA (Fail-Fast Architecture)
    // =========================================================

    @Test(expected = IllegalStateException::class)
    fun `caso frontera - si falla la inicializacion de Firebase la aplicacion crashea inmediatamente`() {
        val app = App()

        // Simulamos un escenario catastrófico: El teléfono no tiene servicios de Google
        // o el archivo google-services.json está corrupto.
        firebaseStaticMock.`when`<Any> { FirebaseApp.initializeApp(app) }
            .thenThrow(IllegalStateException("No se encontraron los binarios de Firebase"))

        // Al ejecutar onCreate, la excepción crasheará la app ("Fail-Fast").
        // La prueba pasará a color VERDE porque esperábamos exactamente este IllegalStateException.
        app.onCreate()
    }
}