package com.example.xolotl.ui.main.usuario

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditarCitasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val uid = FirebaseAuth.getInstance().uid

    // UI - Campos
    private lateinit var txtNombreMascota: TextView
    private lateinit var txtRuac: TextView
    private lateinit var txtServicio: AutoCompleteTextView
    private lateinit var txtFechaHora: EditText
    private lateinit var txtNotas: EditText
    private lateinit var btnGuardar: LinearLayout

    // UI - Layouts para errores (Fix signo rojo)
    private lateinit var layoutServicio: TextInputLayout
    private lateinit var layoutFechaHora: TextInputLayout
    private lateinit var layoutNotas: TextInputLayout

    private var idCita = ""
    private var ruacMascota = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_citas)

        initViews()

        // Recibir datos de la cita
        idCita = intent.getStringExtra("id") ?: ""
        ruacMascota = intent.getStringExtra("ruacMascota") ?: ""

        if (idCita.isEmpty() || ruacMascota.isEmpty()) {
            finish()
            return
        }

        configurarServicios()
        configurarFechaHora()
        cargarDatosCita()
        cargarMascota()
        configurarValidacionesTiempoReal()

        btnGuardar.setOnClickListener {
            if (validarFormularioCompleto()) {
                UiUtils.mostrarAlertaCerrarSesion(
                    this,
                    "Confirmar cambios",
                    "¿Deseas actualizar la cita programada?",
                    SweetAlertDialog.WARNING_TYPE,
                    "Actualizar",
                    "Cancelar",
                    onConfirm = { actualizarCita() }
                )
            }
        }

        findViewById<View>(R.id.btnHome).setOnClickListener { finish() }
    }

    private fun initViews() {
        txtNombreMascota = findViewById(R.id.txtNombreMascota)
        txtRuac = findViewById(R.id.txtRuac)
        txtServicio = findViewById(R.id.txtServicio)
        txtFechaHora = findViewById(R.id.txtFechaHora)
        txtNotas = findViewById(R.id.txtNotas)
        btnGuardar = findViewById(R.id.btnGuardarCambios)

        layoutServicio = findViewById(R.id.layoutServicio)
        layoutFechaHora = findViewById(R.id.layoutFechaHora)
        layoutNotas = findViewById(R.id.layoutNotas)
    }

    private fun configurarValidacionesTiempoReal() {
        bindingFieldValidation(txtServicio, layoutServicio) {
            if (it.isEmpty()) "Selecciona un servicio"
            else if (!ValidationUtils.isValidServicio(it)) "Servicio inválido (3-40 carac.)"
            else null
        }

        bindingFieldValidation(txtFechaHora, layoutFechaHora) {
            if (it.isEmpty()) "Selecciona fecha y hora"
            else if (!ValidationUtils.isValidHorario(it)) "Formato de fecha inválido"
            else if (!ValidationUtils.isFechaFutura(it)) "La cita debe ser en el futuro"
            else null
        }

        bindingFieldValidation(txtNotas, layoutNotas) {
            if (!ValidationUtils.isValidNotasCita(it)) "Notas inválidas o muy largas" else null
        }
    }

    private fun bindingFieldValidation(edit: EditText, layout: TextInputLayout, errorProvider: (String) -> String?) {
        edit.addTextChangedListener {
            layout.error = errorProvider(it.toString().trim())
        }
    }

    private fun validarFormularioCompleto(): Boolean {
        // Limpiar errores previos de EditText (Signo rojo antiguo)
        listOf(txtServicio, txtFechaHora, txtNotas).forEach { it.error = null }

        val serv = txtServicio.text.toString().trim()
        val fecha = txtFechaHora.text.toString().trim()
        val notas = txtNotas.text.toString().trim()

        // Validaciones en Layouts
        layoutServicio.error = if (serv.isEmpty()) "Selecciona un servicio" else null
        layoutFechaHora.error = if (fecha.isEmpty()) "Selecciona fecha y hora" else null

        if (fecha.isNotEmpty() && !ValidationUtils.isFechaFutura(fecha)) {
            layoutFechaHora.error = "La fecha debe ser posterior a hoy"
        }

        val tieneErrores = listOf(layoutServicio, layoutFechaHora, layoutNotas).any { it.error != null }

        if (tieneErrores) {
            UiUtils.mostrarAlerta(this, "Atención", "Revisa los campos marcados en rojo", SweetAlertDialog.ERROR_TYPE)
            return false
        }
        return true
    }

    private fun actualizarCita() {
        val uidSafe = uid ?: return
        val datos = mapOf(
            "servicio" to EncryptionUtils.encrypt(txtServicio.text.toString().trim()),
            "horario" to EncryptionUtils.encrypt(txtFechaHora.text.toString().trim()),
            "notas" to EncryptionUtils.encrypt(txtNotas.text.toString().trim())
        )

        db.collection("usuarios").document(uidSafe)
            .collection("mascotas").document(ruacMascota)
            .collection("citas").document(idCita)
            .update(datos)
            .addOnSuccessListener {
                UiUtils.mostrarAlerta(this, "Actualizado", "La cita se actualizó correctamente", SweetAlertDialog.SUCCESS_TYPE) {
                    finish()
                }
            }
            .addOnFailureListener {
                UiUtils.mostrarAlerta(this, "Error", "No se pudo actualizar la cita", SweetAlertDialog.ERROR_TYPE)
            }
    }

    private fun cargarDatosCita() {
        val uidSafe = uid ?: return
        db.collection("usuarios").document(uidSafe)
            .collection("mascotas").document(ruacMascota)
            .collection("citas").document(idCita)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    txtServicio.setText(EncryptionUtils.decrypt(doc.getString("servicio") ?: ""), false)
                    txtFechaHora.setText(EncryptionUtils.decrypt(doc.getString("horario") ?: ""))
                    txtNotas.setText(EncryptionUtils.decrypt(doc.getString("notas") ?: ""))
                }
            }
    }

    private fun configurarServicios() {
        val servicios = listOf("Consulta general", "Vacunación", "Desparasitación", "Esterilización", "Baño y estética", "Cirugía", "Urgencia", "Otro")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, servicios)
        txtServicio.setAdapter(adapter)
        txtServicio.keyListener = null
        txtServicio.setOnClickListener { txtServicio.showDropDown() }
        txtServicio.setOnItemClickListener { parent, _, position, _ ->
            if (parent.getItemAtPosition(position).toString() == "Otro") {
                txtServicio.keyListener = android.text.method.TextKeyListener.getInstance()
                txtServicio.setText("")
                txtServicio.requestFocus()
            } else {
                txtServicio.keyListener = null
            }
        }
    }

    private fun configurarFechaHora() {
        txtFechaHora.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, y, m, d ->
                TimePickerDialog(this, { _, h, min ->
                    val fechaStr = String.format("%02d/%02d/%04d %02d:%02d", d, m + 1, y, h, min)
                    txtFechaHora.setText(fechaStr)
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show()
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
    }

    private fun cargarMascota() {
        val uidSafe = uid ?: return
        // ruacMascota aquí contiene el ID del documento de Firebase
        db.collection("usuarios")
            .document(uidSafe)
            .collection("mascotas")
            .document(ruacMascota)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    // 1. Desencriptamos el nombre de la mascota
                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")

                    // 2. RECUPERAMOS EL RUAC REAL:
                    // Extraemos el campo "ruac" del modelo Mascotas y lo desencriptamos
                    val ruacCifrado = doc.getString("ruac") ?: ""
                    val ruacReal = EncryptionUtils.decrypt(ruacCifrado)

                    // 3. Mostramos los datos correctos en la UI
                    txtNombreMascota.text = nombre
                    txtRuac.text = if (ruacReal.isNotEmpty()) ruacReal else "Sin RUAC"
                }
            }
            .addOnFailureListener {
                txtRuac.text = "Error al cargar RUAC"
            }
    }
}