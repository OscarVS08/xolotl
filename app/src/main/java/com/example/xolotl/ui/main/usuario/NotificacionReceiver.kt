package com.example.xolotl.ui.main.usuario

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.xolotl.R

class NotificacionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // --- EL PLUS: WakeLock para despertar el procesador al instante ---
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Xolotl:AlarmWakeLock")
        wl.acquire(3000) // Mantener despierto por 3 segundos

        val mensaje = intent.getStringExtra("mensaje") ?: "Recordatorio"
        val titulo = intent.getStringExtra("titulo") ?: "Xolotl Notificaciones"

        val builder = NotificationCompat.Builder(context, "canal_citas")
            .setSmallIcon(R.drawable.logo_monocromatico)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Cambiado a MAX
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Cambiado a ALARM
            .setAutoCancel(true)
            .setColor(Color.argb(255, 156, 39, 176))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setFullScreenIntent(null, true) // Esto ayuda a saltar restricciones de prioridad

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), builder.build())

        wl.release() // Liberar el procesador
    }
}