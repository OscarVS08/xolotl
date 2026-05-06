package com.example.xolotl.utils

import android.app.Activity
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Answers
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class UiUtilsTest {

    @Mock
    private lateinit var mockActivity: Activity

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private lateinit var mockDialog: SweetAlertDialog

    @Before
    fun setup() {
        mockActivity = mock(Activity::class.java)

        // 1. Creamos el mock con Deep Stubs
        mockDialog = mock(SweetAlertDialog::class.java, RETURNS_DEEP_STUBS)

        // 2. CONFIGURACIÓN CRÍTICA: Cada método de configuración DEBE devolver el mismo mock
        // Esto evita que la cadena se rompa
        `when`(mockDialog.setTitleText(anyString())).thenReturn(mockDialog)
        `when`(mockDialog.setContentText(anyString())).thenReturn(mockDialog)
        `when`(mockDialog.setConfirmText(anyString())).thenReturn(mockDialog)
        `when`(mockDialog.setCancelText(anyString())).thenReturn(mockDialog)
        `when`(mockDialog.setConfirmClickListener(any())).thenReturn(mockDialog)
        `when`(mockDialog.setCancelClickListener(any())).thenReturn(mockDialog)

        // Inyectamos en la factory
        UiUtils.dialogFactory = { _, _ -> mockDialog }
    }

    @Test
    fun `showToast llama al metodo estatico de Toast correctamente`() {
        val mensaje = "Mensaje de prueba"

        mockStatic(Toast::class.java).use { mockedToast ->
            val mockToastObj = mock(Toast::class.java)
            mockedToast.`when`<Toast> {
                Toast.makeText(eq(mockActivity), eq(mensaje), eq(Toast.LENGTH_SHORT))
            }.thenReturn(mockToastObj)

            UiUtils.showToast(mockActivity, mensaje)

            verify(mockToastObj).show()
        }
    }

    @Test
    fun `mostrarAlerta configura los textos correctamente`() {
        // Ejecución
        UiUtils.mostrarAlerta(
            activity = mockActivity,
            titulo = "Test Titulo",
            mensaje = "Test Mensaje"
        )

        // Verificamos sobre el mock inyectado a través de la factory
        verify(mockDialog).setTitleText("Test Titulo")
        verify(mockDialog).setContentText("Test Mensaje")
        verify(mockDialog).show()
    }

    @Test
    fun `mostrarAlertaCerrarSesion configura botones de confirmacion y cancelacion`() {
        UiUtils.mostrarAlertaCerrarSesion(
            activity = mockActivity,
            titulo = "Cerrar Sesión",
            mensaje = "¿Deseas salir?",
            tipo = SweetAlertDialog.WARNING_TYPE,
            confirmText = "Salir ahora",
            cancelText = "Quedarme"
        )

        verify(mockDialog).setConfirmText("Salir ahora")
        verify(mockDialog).setCancelText("Quedarme")
        verify(mockDialog).show()
    }

    @Test
    fun `mostrarConfirmacionVacuna construye el mensaje con todos los datos`() {
        UiUtils.mostrarConfirmacionVacuna(
            mockActivity, "Rabia", "Nobivac", "1ml", "01/05/2026", "01/05/2027"
        )

        // Validamos que el mensaje final que se le pasa al diálogo contenga la info clave
        verify(mockDialog).setContentText(argThat { mensaje ->
            mensaje.contains("Rabia") &&
                    mensaje.contains("Nobivac") &&
                    mensaje.contains("01/05/2027")
        })
    }

    @Test
    fun `mostrarAlertaPdfGenerado muestra el mensaje de exito esperado`() {
        UiUtils.mostrarAlertaPdfGenerado(mockActivity)

        verify(mockDialog).setTitleText("PDF generado")
        verify(mockDialog).setContentText("Carnet de la mascota generado exitosamente")
        verify(mockDialog).show()
    }

    @Test
    fun `mostrarAlertaPdfError muestra el mensaje de error esperado`() {
        UiUtils.mostrarAlertaPdfError(mockActivity)

        verify(mockDialog).setTitleText("Error")
        verify(mockDialog).setContentText("No se pudo generar el PDF de la mascota")
        verify(mockDialog).show()
    }
}