package com.example.xolotl.ui.main.usuario

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.xolotl.R

class NotificacionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val mensaje = intent.getStringExtra("mensaje") ?: "Recordatorio"

        val builder = NotificationCompat.Builder(context, "canal_citas")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // 🔥 IMPORTANTE
            .setContentTitle("Notificación")
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val manager = NotificationManagerCompat.from(context)

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU ||
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

            manager.notify(System.currentTimeMillis().toInt(), builder.build())
            Log.d("ALARM", "DISPARADA a: ${System.currentTimeMillis()}")

        } else {
            // Permiso no concedido
        }
    }
}