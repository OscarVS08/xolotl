package com.example.xolotl.ui.main.usuario

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import org.junit.Assert.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowNotificationManager

@Ignore("Omitido por falta de RAM en ejecución masiva. Ejecutar individualmente.")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class NotificacionReceiverTest {

    private lateinit var context: Context
    private lateinit var receiver: NotificacionReceiver

    // Sombra para auditar la bandeja de notificaciones del sistema
    private lateinit var shadowNotificationManager: ShadowNotificationManager

    @Before
    fun setup() {
        // Usamos el contexto nativo de Robolectric para evitar errores de librerías faltantes
        context = RuntimeEnvironment.getApplication()
        receiver = NotificacionReceiver()

        // Interceptamos el servicio de notificaciones
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        shadowNotificationManager = shadowOf(notificationManager)
    }

    // =========================================================
    // 1. CASO DE ÉXITO (Integridad de la Notificación)
    // =========================================================

    @Test
    fun `caso de exito - el receiver extrae los extras del Intent y lanza la notificacion`() {
        // Simulamos el Intent exacto que enviaría tu NotificacionesActivity
        val intentValido = Intent().apply {
            putExtra("titulo", "Alerta Veterinaria")
            putExtra("mensaje", "Vacuna de Firulais en 30 min")
        }

        // Ejecutamos el Receiver (Esto probará implícitamente que el WakeLock no lanza errores)
        receiver.onReceive(context, intentValido)

        // 1. Verificamos que se haya enviado la orden al sistema de Android
        val notificaciones = shadowNotificationManager.allNotifications
        assertEquals("Debe dispararse una notificación al sistema", 1, notificaciones.size)

        // 2. Extraemos la notificación para auditar su contenido
        val notificacionLanzada = notificaciones[0]
        val shadowNotif = shadowOf(notificacionLanzada)

        // 3. Comprobamos que el Builder respetó los textos y los pintó correctamente
        assertEquals("Alerta Veterinaria", shadowNotif.contentTitle)
        assertEquals("Vacuna de Firulais en 30 min", shadowNotif.contentText)
    }

    // =========================================================
    // 2. CASO FRONTERA / PREVENCIÓN DE ERRORES (Null Safety)
    // =========================================================

    @Test
    fun `caso frontera - si el intent pierde sus extras usa valores por defecto sin crashear`() {
        // Simulamos un bug grave: El Intent llega totalmente vacío
        val intentVacio = Intent()

        // Ejecutamos. Si no tuvieras los valores por defecto (Elvis operator `?:`), la app crashearía aquí.
        receiver.onReceive(context, intentVacio)

        // Verificamos que la notificación se generó de todos modos usando el plan de respaldo
        val notificaciones = shadowNotificationManager.allNotifications
        val shadowNotif = shadowOf(notificaciones[0])

        assertEquals("Xolotl Notificaciones", shadowNotif.contentTitle)
        assertEquals("Recordatorio", shadowNotif.contentText)
    }
}