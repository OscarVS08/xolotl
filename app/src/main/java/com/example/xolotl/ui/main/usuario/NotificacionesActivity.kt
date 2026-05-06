package com.example.xolotl.ui.main.usuario

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotificacionesActivity : AppCompatActivity() {

    private lateinit var spinnerCitas: AutoCompleteTextView
    private lateinit var txtFechaHoraCita: TextView
    private lateinit var txtFechaHoraNotificacion: EditText
    private val listaCitas = mutableListOf<String>()
    private val mapaCitas = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        // Inicializar vistas
        spinnerCitas = findViewById(R.id.spinnerCitas)
        txtFechaHoraCita = findViewById(R.id.txtFechaHoraCita)
        txtFechaHoraNotificacion = findViewById(R.id.txtFechaHoraNotificacion)

        // Configuración inicial
        cargarCitas()
        configurarDatePicker()
        crearCanalNotificaciones()
        verificarYPedirPermisos()
        solicitarIgnorarOptimizacionBateria()

        spinnerCitas.setOnItemClickListener { parent, _, position, _ ->
            val seleccion = parent.getItemAtPosition(position).toString()
            txtFechaHoraCita.text = mapaCitas[seleccion] ?: "--"
        }

        findViewById<LinearLayout>(R.id.btnGuardarNotificacion).setOnClickListener {
            validarYProgramar()
        }

        findViewById<View>(R.id.btnHome).setOnClickListener { finish() }
    }

    private fun cargarCitas() {
        val uid = FirebaseAuth.getInstance().uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uid).collection("mascotas").get()
            .addOnSuccessListener { mascotas ->
                listaCitas.clear()
                mapaCitas.clear()
                for (mascota in mascotas) {
                    val nombreM = EncryptionUtils.decrypt(mascota.getString("nombre") ?: "")
                    mascota.reference.collection("citas").get().addOnSuccessListener { citas ->
                        for (doc in citas) {
                            val serv = EncryptionUtils.decrypt(doc.getString("servicio") ?: "")
                            val hor = EncryptionUtils.decrypt(doc.getString("horario") ?: "")
                            val item = "$nombreM - $serv"
                            listaCitas.add(item)
                            mapaCitas[item] = hor
                        }
                        val adapter = ArrayAdapter(this, R.layout.item_dropdown, listaCitas)
                        spinnerCitas.setAdapter(adapter)
                    }
                }
            }
    }

    private fun configurarDatePicker() {
        txtFechaHoraNotificacion.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                TimePickerDialog(this, { _, hh, mm ->
                    val fechaSeleccionada = String.format("%02d/%02d/%04d %02d:%02d", d, m + 1, y, hh, mm)
                    txtFechaHoraNotificacion.setText(fechaSeleccionada)
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun validarYProgramar() {
        val seleccion = spinnerCitas.text.toString()
        val fechaNotifStr = txtFechaHoraNotificacion.text.toString()
        val fechaCitaStr = txtFechaHoraCita.text.toString()

        if (seleccion.isEmpty() || fechaNotifStr.isEmpty() || fechaCitaStr == "--") {
            UiUtils.mostrarAlerta(this, "Campos incompletos", "Selecciona una cita y la hora del recordatorio.", SweetAlertDialog.WARNING_TYPE)
            return
        }

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        try {
            val fechaDestino = sdf.parse(fechaNotifStr)
            val fechaCita = sdf.parse(fechaCitaStr)
            val ahora = Date()

            if (fechaDestino == null || fechaCita == null) return

            // BARRERA 1: ¿La cita ya pasó?
            if (fechaCita.before(ahora)) {
                UiUtils.mostrarAlerta(this, "Cita expirada", "Esta cita ya pasó. No es posible programar un recordatorio.", SweetAlertDialog.ERROR_TYPE)
                return
            }

            // BARRERA 2: ¿El recordatorio está en el pasado absoluto? (Tu lógica original)
            if (fechaDestino.before(ahora)) {
                UiUtils.mostrarAlerta(this, "Fecha inválida", "La fecha del recordatorio debe ser posterior a la hora actual.", SweetAlertDialog.ERROR_TYPE)
                return
            }

            // BARRERA 3: El margen de maniobra (Los famosos "30 segundos")
            // Si superó la barrera 2, significa que es futuro, pero... ¿es un futuro MUY cercano?
            val margenMinimo = Calendar.getInstance().apply {
                time = ahora
                add(Calendar.MINUTE, 2)
            }.time

            if (fechaDestino.before(margenMinimo)) {
                UiUtils.mostrarAlerta(this, "Tiempo insuficiente", "Da un margen de al menos 2 minutos desde este instante para programar la alerta.", SweetAlertDialog.WARNING_TYPE)
                return
            }

            // BARRERA 4: La paradoja temporal (Recordatorio después de la cita)
            if (fechaDestino.after(fechaCita) || fechaDestino.time == fechaCita.time) {
                UiUtils.mostrarAlerta(this, "Fecha ilógica", "El recordatorio debe ser estrictamente antes de la hora de la cita.", SweetAlertDialog.ERROR_TYPE)
                return
            }

            // ÉXITO: Programar la alarma
            programarAlarmaExacta(fechaDestino.time, seleccion)

        } catch (e: Exception) {
            UiUtils.mostrarAlerta(this, "Error", "Formato de fecha incorrecto.", SweetAlertDialog.ERROR_TYPE)
        }
    }

    private fun programarAlarmaExacta(timeInMillis: Long, citaNombre: String) {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        // Verificar permiso de Alarma Exacta (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            UiUtils.mostrarAlerta(this, "Permiso requerido", "Para garantizar puntualidad, activa el permiso de Alarmas Exactas.", SweetAlertDialog.WARNING_TYPE) {
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            }
            return
        }

        val intent = Intent(this, NotificacionReceiver::class.java).apply {
            action = "com.example.xolotl.ACTION_ALARM"
            putExtra("titulo", "Recordatorio de Xolotl")
            putExtra("mensaje", "Es momento de: $citaNombre")
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        // Request code basado en tiempo para evitar colisiones
        val requestCode = (timeInMillis / 1000).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Usar AlarmClockInfo para prioridad máxima (Despierta el dispositivo)
        val info = AlarmManager.AlarmClockInfo(timeInMillis, pendingIntent)
        alarmManager.setAlarmClock(info, pendingIntent)

        UiUtils.mostrarAlerta(
            this,
            "¡Programado!",
            "Aviso listo para el ${txtFechaHoraNotificacion.text}",
            SweetAlertDialog.SUCCESS_TYPE
        )
    }

    private fun crearCanalNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel("canal_citas", "Recordatorios de Citas", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alertas de citas veterinarias de Xolotl"
                enableLights(true)
                lightColor = Color.argb(255, 156, 39, 176)
                enableVibration(true)
                setBypassDnd(true) // Plus: puede saltar "No molestar" si el usuario lo permite
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun verificarYPedirPermisos() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }
    }

    private fun solicitarIgnorarOptimizacionBateria() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                UiUtils.mostrarAlerta(this, "Optimización de batería", "Para recibir avisos sin retrasos, selecciona 'Xolotl' y marca 'Sin restricciones'.", SweetAlertDialog.WARNING_TYPE) {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    startActivity(intent)
                }
            }
        }
    }
}