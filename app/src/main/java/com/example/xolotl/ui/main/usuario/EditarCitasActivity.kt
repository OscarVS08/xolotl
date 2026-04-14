package com.example.xolotl.ui.main.usuario

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import androidx.core.widget.addTextChangedListener

class EditarCitasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().uid

    private lateinit var txtNombreMascota: TextView
    private lateinit var txtRuac: TextView
    private lateinit var txtServicio: AutoCompleteTextView
    private lateinit var txtFechaHora: EditText
    private lateinit var txtNotas: EditText
    private lateinit var btnGuardar: LinearLayout

    private var idCita = ""
    private var ruacMascota = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_citas)

        // ============================
        // REFERENCIAS
        // ============================
        txtNombreMascota = findViewById(R.id.txtNombreMascota)
        txtRuac = findViewById(R.id.txtRuac)
        txtServicio = findViewById(R.id.txtServicio)
        txtFechaHora = findViewById(R.id.txtFechaHora)
        txtNotas = findViewById(R.id.txtNotas)
        btnGuardar = findViewById(R.id.btnGuardarCambios)

        // ============================
        // RECIBIR DATOS
        // ============================
        idCita = intent.getStringExtra("id") ?: return
        ruacMascota = intent.getStringExtra("ruacMascota") ?: return

        // ============================
        // CONFIGURAR
        // ============================
        configurarServicios()
        configurarFechaHora()
        cargarDatosCita()
        cargarMascota()
        configurarValidacionesTiempoReal()

        btnGuardar.setOnClickListener {

            val servicio = txtServicio.text.toString()
            val fechaHora = txtFechaHora.text.toString()
            val notas = txtNotas.text.toString()

            var hayError = false

            // ============================
            // VALIDAR CAMPOS VACÍOS
            // ============================

            if (servicio.isEmpty()) {
                txtServicio.error = "Campo obligatorio"
                hayError = true
            }

            if (fechaHora.isEmpty()) {
                txtFechaHora.error = "Campo obligatorio"
                hayError = true
            }

            if (notas.isEmpty()) {
                txtNotas.error = "Campo obligatorio"
                hayError = true
            }

            // ============================
            // SI HAY ERRORES
            // ============================

            if (hayError) {
                UiUtils.mostrarAlerta(
                    activity = this,
                    titulo = "Campos incompletos",
                    mensaje = "Por favor llena todos los campos",
                    tipo = cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE
                )
                return@setOnClickListener
            }

            // ============================
            // CONFIRMACIÓN
            // ============================

            UiUtils.mostrarAlertaCerrarSesion(
                activity = this,
                titulo = "Confirmar cambios",
                mensaje = "¿Deseas actualizar la cita?",
                tipo = cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE,
                confirmText = "Actualizar",
                cancelText = "Cancelar",
                onConfirm = {
                    actualizarCita()
                },
                onCancel = { }
            )
        }

        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }
    }

    // ============================
    // CARGAR DATOS
    // ============================
    private fun cargarDatosCita() {

        db.collection("usuarios")
            .document(uid!!)
            .collection("mascotas")
            .document(ruacMascota)
            .collection("citas")
            .document(idCita)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {

                    val servicio = EncryptionUtils.decrypt(doc.getString("servicio") ?: "")
                    val fecha = EncryptionUtils.decrypt(doc.getString("horario") ?: "")
                    val notas = EncryptionUtils.decrypt(doc.getString("notas") ?: "")

                    txtServicio.setText(servicio, false)
                    txtFechaHora.setText(fecha)
                    txtNotas.setText(notas)
                }
            }
    }

    // ============================
    // ACTUALIZAR
    // ============================
    private fun actualizarCita() {

        val servicio = txtServicio.text.toString()
        val fechaHora = txtFechaHora.text.toString()
        val notas = txtNotas.text.toString()

        val datos = mapOf(
            "servicio" to EncryptionUtils.encrypt(servicio),
            "horario" to EncryptionUtils.encrypt(fechaHora),
            "notas" to EncryptionUtils.encrypt(notas)
        )

        db.collection("usuarios")
            .document(uid!!)
            .collection("mascotas")
            .document(ruacMascota)
            .collection("citas")
            .document(idCita)
            .update(datos)
            .addOnSuccessListener {

                UiUtils.mostrarAlerta(
                    this,
                    "Actualizado",
                    "La cita se actualizó correctamente",
                    cn.pedant.SweetAlert.SweetAlertDialog.SUCCESS_TYPE
                ) {
                    finish()
                }
            }
            .addOnFailureListener {

                UiUtils.mostrarAlerta(
                    this,
                    "Error",
                    "No se pudo actualizar la cita",
                    cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE
                )
            }
    }

    // ============================
    // SERVICIOS
    // ============================
    // ============================
    // SERVICIOS (CATÁLOGO)
    // ============================
    private fun configurarServicios() {

        val servicios = listOf(
            "Consulta general",
            "Vacunación",
            "Desparasitación",
            "Esterilización",
            "Baño y estética",
            "Cirugía",
            "Urgencia",
            "Otro"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            servicios
        )

        txtServicio.setAdapter(adapter)

        // Por defecto NO editable
        txtServicio.keyListener = null

        txtServicio.setOnClickListener {
            txtServicio.showDropDown()
        }

        txtServicio.setOnItemClickListener { parent, _, position, _ ->
            val seleccionado = parent.getItemAtPosition(position).toString()

            if (seleccionado == "Otro") {
                // Permitir escribir
                txtServicio.keyListener = android.text.method.TextKeyListener.getInstance()
                txtServicio.setText("")
                txtServicio.requestFocus()
            } else {
                // Bloquear edición
                txtServicio.keyListener = null
            }
        }
    }

    // ============================
    // FECHA + HORA
    // ============================
    private fun configurarFechaHora() {

        txtFechaHora.setOnClickListener {

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

                            txtFechaHora.setText(fechaHora)

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

    private fun cargarMascota() {

        db.collection("usuarios")
            .document(uid!!)
            .collection("mascotas")
            .document(ruacMascota)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {

                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    val ruac = doc.id

                    txtNombreMascota.text = nombre
                    txtRuac.text = ruac
                }
            }
    }

    private fun configurarValidacionesTiempoReal() {

        txtServicio.addTextChangedListener {
            if (it.toString().isNotEmpty()) txtServicio.error = null
        }

        txtFechaHora.addTextChangedListener {
            if (it.toString().isNotEmpty()) txtFechaHora.error = null
        }

        txtNotas.addTextChangedListener {
            if (it.toString().isNotEmpty()) txtNotas.error = null
        }
    }
}