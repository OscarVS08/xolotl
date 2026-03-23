package com.example.xolotl.ui.main.mascota

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.R
import com.example.xolotl.data.models.Desparasitaciones
import com.example.xolotl.data.repository.DesparasitacionRepository
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.app.DatePickerDialog
import com.example.xolotl.utils.UiUtils
import java.util.Calendar
import com.example.xolotl.utils.ValidationUtils
import androidx.core.widget.addTextChangedListener

class AgregarDesparasitacionesActivity : AppCompatActivity() {

    private lateinit var spinnerMascota: AutoCompleteTextView
    private lateinit var txtMetodo: EditText
    private lateinit var txtNombre: EditText
    private lateinit var txtMarca: EditText
    private lateinit var txtFecha: EditText
    private lateinit var txtProxFecha: EditText
    private lateinit var btnGuardar: LinearLayout

    private val repository = DesparasitacionRepository()

    private val listaNombresMascotas = mutableListOf<String>()
    private val mapaMascotas = mutableMapOf<String, String>() // nombre -> RUAC

    private var ruacSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_desparasitacion)

        spinnerMascota = findViewById(R.id.spinnerMascota)
        txtMetodo = findViewById(R.id.txtMetodo)
        txtNombre = findViewById(R.id.txtNombre)
        txtMarca = findViewById(R.id.txtMarca)
        txtFecha = findViewById(R.id.txtFecha)
        txtProxFecha = findViewById(R.id.txtProxFecha)
        btnGuardar = findViewById(R.id.btnGuardarCambios)

        cargarMascotasUsuario()
        configurarValidacionesTiempoReal()

        spinnerMascota.setOnItemClickListener { parent, _, position, _ ->
            val nombreMascota = parent.getItemAtPosition(position).toString()
            ruacSeleccionado = mapaMascotas[nombreMascota] ?: ""
        }

        spinnerMascota.threshold = 1

        spinnerMascota.setOnClickListener {
            spinnerMascota.showDropDown()
        }

        spinnerMascota.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                spinnerMascota.showDropDown()
            }
        }

        spinnerMascota.keyListener = null

        btnGuardar.setOnClickListener {

            val metodo = txtMetodo.text.toString()
            val nombre = txtNombre.text.toString()
            val marca = txtMarca.text.toString()
            val fecha = txtFecha.text.toString()
            val proxFecha = txtProxFecha.text.toString()

            // Validaciones
            var hayError = false

            if (metodo.isEmpty()) {
                txtMetodo.error = "Campo obligatorio"
                hayError = true
            }

            if (nombre.isEmpty()) {
                txtNombre.error = "Campo obligatorio"
                hayError = true
            }

            if (marca.isEmpty()) {
                txtMarca.error = "Campo obligatorio"
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
                    activity = this,
                    titulo = "Falta información",
                    mensaje = "Selecciona una mascota",
                    tipo = cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE
                )
                return@setOnClickListener
            }

            if (hayError) {
                UiUtils.mostrarAlerta(
                    activity = this,
                    titulo = "Campos incompletos",
                    mensaje = "Por favor llena todos los campos",
                    tipo = cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE
                )
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidFechaDesparasitacion(proxFecha)) {
                txtProxFecha.error = "Fecha inválida"
                return@setOnClickListener
            }

            // Alerta de confirmacion de datos
            UiUtils.mostrarConfirmacionDesparasitacion(
                activity = this,
                nombre = nombre,
                marca = marca,
                fecha = fecha,
                proximaFecha = proxFecha,
                metodo = metodo,
                onConfirm = {
                    guardarDesparasitacion()
                },
                onCancel = {
                    // Nos quedamos en el layput
                }
            )
        }

        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }

        configurarCamposFecha()
    }

    // ============================
    // Cargar mascotas del usuario
    // ============================
    private fun cargarMascotasUsuario() {

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { result ->

                listaNombresMascotas.clear()
                mapaMascotas.clear()

                for (doc in result) {

                    val nombreCifrado = doc.getString("nombre") ?: continue
                    val nombre = EncryptionUtils.decrypt(nombreCifrado)
                    val ruac = doc.id

                    listaNombresMascotas.add(nombre)
                    mapaMascotas[nombre] = ruac
                }

                //Toast.makeText(this, "Mascotas encontradas: ${listaNombresMascotas.size}", Toast.LENGTH_LONG).show()

                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_dropdown_item_1line,
                    listaNombresMascotas
                )

                spinnerMascota.setAdapter(adapter)
            }

            .addOnFailureListener {
                Toast.makeText(this, "Error Firestore: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    // ============================
    // Guardar desparasitación
    // ============================
    private fun guardarDesparasitacion() {

        val metodo = txtMetodo.text.toString()
        val nombre = txtNombre.text.toString()
        val marca = txtMarca.text.toString()
        val fecha = txtFecha.text.toString()
        val proxFecha = txtProxFecha.text.toString()

        val desparasitacion = Desparasitaciones(
            tipo = EncryptionUtils.encrypt(metodo),
            nombre = EncryptionUtils.encrypt(nombre),
            marca = EncryptionUtils.encrypt(marca),
            fecha = EncryptionUtils.encrypt(fecha),
            proximaFecha = EncryptionUtils.encrypt(proxFecha),
            ruacMascota = ruacSeleccionado
        )

        repository.registrarDesparasitacion(
            ruacSeleccionado,
            desparasitacion,
            onSuccess = {

                UiUtils.mostrarAlerta(
                    activity = this,
                    titulo = "Registro exitoso",
                    mensaje = "Datos de la desparasitación agregados exitosamente",
                    tipo = cn.pedant.SweetAlert.SweetAlertDialog.SUCCESS_TYPE,
                    onConfirm = {
                        finish() //Regresa al menú principal
                    }
                )
            },

            onError = {

                UiUtils.mostrarAlerta(
                    activity = this,
                    titulo = "Error",
                    mensaje = "No se pudo registrar la desparasitación",
                    tipo = cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE
                )
            }
        )
    }

    private fun configurarCamposFecha() {

        txtFecha.setOnClickListener {
            mostrarDatePicker { fechaSeleccionada ->
                txtFecha.setText(fechaSeleccionada)
            }
        }

        txtProxFecha.setOnClickListener {
            mostrarDatePicker { fechaSeleccionada ->
                txtProxFecha.setText(fechaSeleccionada)
            }
        }
    }

    private fun mostrarDatePicker(onFechaSeleccionada: (String) -> Unit) {

        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->

                // Formato DD/MM/YYYY
                val fechaFormateada = String.format(
                    "%02d/%02d/%04d",
                    selectedDay,
                    selectedMonth + 1,
                    selectedYear
                )

                onFechaSeleccionada(fechaFormateada)

            },
            year,
            month,
            day
        )

        datePicker.show()
    }

    private fun configurarValidacionesTiempoReal() {

        txtMetodo.addTextChangedListener {
            val value = it.toString()
            txtMetodo.error = if (!ValidationUtils.isValidMetodo(value)) "Método inválido" else null
        }

        txtNombre.addTextChangedListener {
            val value = it.toString()
            txtNombre.error = if (!ValidationUtils.isValidMedicamento(value)) "Nombre inválido" else null
        }

        txtMarca.addTextChangedListener {
            val value = it.toString()
            txtMarca.error = if (!ValidationUtils.isValidMarca(value)) "Marca inválida" else null
        }

        txtFecha.addTextChangedListener {
            val value = it.toString()
            txtFecha.error = if (!ValidationUtils.isValidFechaDesparasitacion(value)) "Fecha inválida" else null
        }

        txtProxFecha.addTextChangedListener {
            val value = it.toString()
            txtProxFecha.error = if (!ValidationUtils.isValidFechaDesparasitacion(value)) "Fecha inválida" else null
        }
    }

}