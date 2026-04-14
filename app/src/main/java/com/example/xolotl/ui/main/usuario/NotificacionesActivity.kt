package com.example.xolotl.ui.main.usuario

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.Manifest
import android.util.Log


class NotificacionesActivity : AppCompatActivity() {

    private lateinit var spinnerCitas: AutoCompleteTextView
    private lateinit var txtFechaHoraCita: TextView
    private lateinit var txtFechaHoraNotificacion: EditText

    private val listaCitas = mutableListOf<String>()
    private val mapaCitas = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        // ============================
        // REFERENCIAS
        // ============================
        spinnerCitas = findViewById(R.id.spinnerCitas)
        // Por defecto NO editable
        spinnerCitas.keyListener = null
        txtFechaHoraCita = findViewById(R.id.txtFechaHoraCita)
        txtFechaHoraNotificacion = findViewById(R.id.txtFechaHoraNotificacion)
        txtFechaHoraNotificacion.keyListener = null

        // ============================
        // CONFIGURACIÓN
        // ============================
        cargarCitas()
        configurarFechaHoraNotificacion()
        crearCanal()
        pedirPermisoNotificaciones()

        spinnerCitas.setOnClickListener {
            spinnerCitas.showDropDown()
        }

        spinnerCitas.setOnItemClickListener { parent, _, position, _ ->

            val seleccion = parent.getItemAtPosition(position).toString()

            val fechaHora = mapaCitas[seleccion] ?: ""

            txtFechaHoraCita.text = fechaHora
        }

        val btnGuardar = findViewById<LinearLayout>(R.id.btnGuardarNotificacion)

        btnGuardar.setOnClickListener {

            val fechaHora = txtFechaHoraNotificacion.text.toString()

            if (fechaHora.isEmpty()) {
                Toast.makeText(this, "Selecciona fecha y hora", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            programarNotificacion(fechaHora)
        }

        // Botón home
        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }
    }

    // ============================
    // CARGAR CITAS DESDE FIREBASE
    // ============================
    private fun cargarCitas() {

        val uid = FirebaseAuth.getInstance().uid ?: return
        val db = FirebaseFirestore.getInstance()

        listaCitas.clear()
        mapaCitas.clear()

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { mascotas ->

                for (mascota in mascotas) {

                    val ruac = mascota.id
                    val nombreMascota = EncryptionUtils.decrypt(
                        mascota.getString("nombre") ?: ""
                    )

                    db.collection("usuarios")
                        .document(uid)
                        .collection("mascotas")
                        .document(ruac)
                        .collection("citas")
                        .get()
                        .addOnSuccessListener { citas ->

                            for (doc in citas) {

                                val servicio = EncryptionUtils.decrypt(
                                    doc.getString("servicio") ?: ""
                                )

                                val fechaHora = EncryptionUtils.decrypt(
                                    doc.getString("horario") ?: ""
                                )

                                val nombreItem = "$nombreMascota - $servicio"

                                listaCitas.add(nombreItem)
                                mapaCitas[nombreItem] = fechaHora
                            }

                            val adapter = ArrayAdapter(
                                this,
                                android.R.layout.simple_dropdown_item_1line,
                                listaCitas
                            )

                            spinnerCitas.setAdapter(adapter)
                        }
                }
            }
    }

    private fun configurarFechaHoraNotificacion() {

        txtFechaHoraNotificacion.setOnClickListener {

            val calendar = Calendar.getInstance()

            DatePickerDialog(
                this,
                { _, year, month, day ->

                    TimePickerDialog(
                        this,
                        { _, hour, minute ->

                            val fechaHora = String.format(
                                "%02d/%02d/%04d %02d:%02d",
                                day,
                                month + 1,
                                year,
                                hour,
                                minute
                            )

                            txtFechaHoraNotificacion.setText(fechaHora)

                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()

                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun crearCanal() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val canal = NotificationChannel(
                "canal_citas",
                "Recordatorios de citas",
                NotificationManager.IMPORTANCE_HIGH
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }
    }

    private fun programarNotificacion(fechaHora: String) {

        try {

            val fechaHoraLimpia = fechaHora.trim()

            // Validación estricta del formato
            val regex = Regex("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}")
            if (!regex.matches(fechaHoraLimpia)) {
                Toast.makeText(this, "Formato inválido", Toast.LENGTH_SHORT).show()
                return
            }

            val formato = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            formato.isLenient = false // clave

            val fecha = formato.parse(fechaHoraLimpia)

            if (fecha == null) {
                Toast.makeText(this, "Fecha inválida", Toast.LENGTH_SHORT).show()
                return
            }

            // Validar que sea futura
            if (fecha.time <= System.currentTimeMillis()) {
                Toast.makeText(this, "La fecha debe ser futura", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(this, NotificacionReceiver::class.java)
            val citaSeleccionada = spinnerCitas.text.toString()

            intent.putExtra(
                "mensaje",
                "Recordatorio: $citaSeleccionada"
            )

            val requestCode = System.currentTimeMillis().toInt()

            val pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

            // AQUÍ VA LA VALIDACIÓN
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (!alarmManager.canScheduleExactAlarms()) {

                    val intentPermiso = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intentPermiso)

                    Toast.makeText(this, "Activa permisos de alarmas exactas", Toast.LENGTH_LONG).show()
                    return
                }
            }

            // PROGRAMA LA NOTIFICACIÓN
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                fecha.time,
                pendingIntent
            )

            Toast.makeText(this, "Notificación programada", Toast.LENGTH_SHORT).show()
            Log.d("ALARM", "Programando para: ${fecha.time}")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun pedirPermisoNotificaciones() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}