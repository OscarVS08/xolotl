package com.example.xolotl.ui.main.mascota

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.xolotl.R
import com.example.xolotl.data.models.Vacunas
import com.example.xolotl.data.repository.VacunasRepository
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AgregarVacunasActivity : AppCompatActivity() {

    private lateinit var spinnerMascota: AutoCompleteTextView
    private lateinit var txtNombre: EditText
    private lateinit var txtMarca: EditText
    private lateinit var txtDosis: EditText
    private lateinit var txtFecha: EditText
    private lateinit var txtProxFecha: EditText
    private lateinit var btnGuardar: LinearLayout

    private val repository = VacunasRepository()

    private val listaNombresMascotas = mutableListOf<String>()
    private val mapaMascotas = mutableMapOf<String, String>()

    private var ruacSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_vacuna)

        spinnerMascota = findViewById(R.id.spinnerMascota)
        txtNombre = findViewById(R.id.txtNombre)
        txtMarca = findViewById(R.id.txtMarca)
        txtDosis = findViewById(R.id.txtDosis)
        txtFecha = findViewById(R.id.txtFecha)
        txtProxFecha = findViewById(R.id.txtProxFecha)
        btnGuardar = findViewById(R.id.btnGuardarCambios)

        cargarMascotasUsuario()
        configurarValidacionesTiempoReal()
        configurarCamposFecha()

        spinnerMascota.setOnItemClickListener { parent, _, position, _ ->
            val nombreMascota = parent.getItemAtPosition(position).toString()
            ruacSeleccionado = mapaMascotas[nombreMascota] ?: ""
        }

        spinnerMascota.setOnClickListener { spinnerMascota.showDropDown() }

        btnGuardar.setOnClickListener {

            val nombre = txtNombre.text.toString()
            val marca = txtMarca.text.toString()
            val dosis = txtDosis.text.toString()
            val fecha = txtFecha.text.toString()
            val proxFecha = txtProxFecha.text.toString()

            var hayError = false

            // VALIDACIONES VACÍOS
            if (nombre.isEmpty()) {
                txtNombre.error = "Campo obligatorio"
                hayError = true
            }

            if (marca.isEmpty()) {
                txtMarca.error = "Campo obligatorio"
                hayError = true
            }

            if (dosis.isEmpty()) {
                txtDosis.error = "Campo obligatorio"
                hayError = true
            }

            if (fecha.isEmpty()) {
                txtFecha.error = "Campo obligatorio"
                hayError = true
            }

            if (proxFecha.isEmpty()) {
                txtProxFecha.error = "Campo obligatorio"
                hayError = true
            }

            if (ruacSeleccionado.isEmpty()) {
                UiUtils.mostrarAlerta(
                    this,
                    "Falta información",
                    "Selecciona una mascota",
                    cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE
                )
                return@setOnClickListener
            }

            if (hayError) {
                UiUtils.mostrarAlerta(
                    this,
                    "Campos incompletos",
                    "Por favor llena todos los campos",
                    cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE
                )
                return@setOnClickListener
            }

            // VALIDACIONES LÓGICAS
            if (!ValidationUtils.isValidMedicamento(nombre)) {
                txtNombre.error = "Nombre inválido"
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidMarca(marca)) {
                txtMarca.error = "Marca inválida"
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidNumber(dosis)) {
                txtDosis.error = "Dosis inválida"
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidFechaDesparasitacion(fecha)) {
                txtFecha.error = "Fecha inválida"
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidFechaDesparasitacion(proxFecha)) {
                txtProxFecha.error = "Fecha inválida"
                return@setOnClickListener
            }

            // ALERTA DE CONFIRMACIÓN
            UiUtils.mostrarAlertaCerrarSesion(
                activity = this,
                titulo = "Confirmar registro",
                mensaje = "Verifica los datos:\n\n" +
                        "Nombre: $nombre\n" +
                        "Marca: $marca\n" +
                        "Dosis: $dosis\n" +
                        "Fecha: $fecha\n" +
                        "Próxima: $proxFecha",
                tipo = cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE,
                confirmText = "Registrar",
                cancelText = "Cancelar",
                onConfirm = {
                    guardarVacuna()
                },
                onCancel = {}
            )
        }

        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }
    }

    // ============================
    // GUARDAR VACUNA
    // ============================
    private fun guardarVacuna() {

        val vacuna = Vacunas(
            nombre = EncryptionUtils.encrypt(txtNombre.text.toString()),
            marca = EncryptionUtils.encrypt(txtMarca.text.toString()),
            dosis = EncryptionUtils.encrypt(txtDosis.text.toString()),
            fecha = EncryptionUtils.encrypt(txtFecha.text.toString()),
            proximaFecha = EncryptionUtils.encrypt(txtProxFecha.text.toString()),
            ruacMascota = ruacSeleccionado
        )

        repository.registrarVacuna(
            ruacSeleccionado,
            vacuna,
            onSuccess = {
                UiUtils.mostrarAlerta(
                    this,
                    "Registro exitoso",
                    "Vacuna registrada correctamente",
                    cn.pedant.SweetAlert.SweetAlertDialog.SUCCESS_TYPE
                ) {
                    finish()
                }
            },
            onError = {
                UiUtils.mostrarAlerta(
                    this,
                    "Error",
                    "No se pudo registrar la vacuna",
                    cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE
                )
            }
        )
    }

    // ============================
    // MASCOTAS
    // ============================
    private fun cargarMascotasUsuario() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { result ->

                listaNombresMascotas.clear()
                mapaMascotas.clear()

                for (doc in result) {
                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    listaNombresMascotas.add(nombre)
                    mapaMascotas[nombre] = doc.id
                }

                spinnerMascota.setAdapter(
                    ArrayAdapter(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        listaNombresMascotas
                    )
                )
            }
    }

    // ============================
    // FECHAS
    // ============================
    private fun configurarCamposFecha() {

        txtFecha.setOnClickListener {
            mostrarDatePicker { txtFecha.setText(it) }
        }

        txtProxFecha.setOnClickListener {
            mostrarDatePicker { txtProxFecha.setText(it) }
        }
    }

    private fun mostrarDatePicker(onFecha: (String) -> Unit) {

        val cal = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, y, m, d ->
                onFecha(String.format("%02d/%02d/%04d", d, m + 1, y))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ============================
    // VALIDACIONES EN TIEMPO REAL
    // ============================
    private fun configurarValidacionesTiempoReal() {

        txtNombre.addTextChangedListener {
            txtNombre.error =
                if (!ValidationUtils.isValidMedicamento(it.toString())) "Nombre inválido" else null
        }

        txtMarca.addTextChangedListener {
            txtMarca.error =
                if (!ValidationUtils.isValidMarca(it.toString())) "Marca inválida" else null
        }

        txtDosis.addTextChangedListener {
            txtDosis.error =
                if (!ValidationUtils.isValidNumber(it.toString())) "Dosis inválida" else null
        }

        txtFecha.addTextChangedListener {
            txtFecha.error =
                if (!ValidationUtils.isValidFechaDesparasitacion(it.toString())) "Fecha inválida" else null
        }

        txtProxFecha.addTextChangedListener {
            txtProxFecha.error =
                if (!ValidationUtils.isValidFechaDesparasitacion(it.toString())) "Fecha inválida" else null
        }
    }
}