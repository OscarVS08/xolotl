package com.example.xolotl.ui.main.mascota

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.data.models.Vacunas
import com.example.xolotl.data.repository.VacunasRepository
import com.example.xolotl.databinding.ActivityAgregarVacunaBinding
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AgregarVacunasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarVacunaBinding
    private val repository = VacunasRepository()
    private val listaNombresMascotas = mutableListOf<String>()
    private val mapaMascotas = mutableMapOf<String, String>()
    private var ruacSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarVacunaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarMascotasUsuario()
        configurarValidacionesTiempoReal()
        configurarCamposFecha()

        // Configuración Spinner
        binding.spinnerMascota.apply {
            threshold = 1
            keyListener = null
            setOnItemClickListener { parent, _, position, _ ->
                val nombreMascota = parent.getItemAtPosition(position).toString()
                ruacSeleccionado = mapaMascotas[nombreMascota] ?: ""
                binding.layoutMascota.error = null
            }
            setOnClickListener { showDropDown() }
        }

        // Definiendo las unidades para la dosis
        val unidades = listOf("ml", "mg", "Tableta", "Gotas", "Refuerzo")
        // Configuracion del adaptador para el Spinner
        val adapterUnidades = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, unidades)
        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown, // <--- Nuevo layout personalizado
            listaNombresMascotas
        )
        binding.spinnerMascota.setAdapter(adapter)
        binding.autoCompleteUnidad.setAdapter(adapterUnidades)

        binding.btnGuardarCambios.setOnClickListener {
            validarFormularioCompleto()
        }

        binding.btnHome.setOnClickListener { finish() }
    }

    private fun configurarValidacionesTiempoReal() {
        binding.txtNombre.addTextChangedListener {
            binding.layoutNombre.error = if (!ValidationUtils.isValidMedicamento(it.toString())) "Mínimo 3 letras" else null
        }
        binding.txtMarca.addTextChangedListener {
            binding.layoutMarca.error = if (!ValidationUtils.isValidMarca(it.toString())) "Marca inválida" else null
        }
        binding.txtDosis.addTextChangedListener {
            binding.layoutDosis.error = if (it.toString().isEmpty()) "Requerido" else null
        }
        binding.txtFecha.addTextChangedListener {
            binding.layoutFecha.error = if (it.toString().isEmpty()) "Requerido" else null
        }
        binding.txtProxFecha.addTextChangedListener {
            val prox = it.toString()
            val actual = binding.txtFecha.text.toString()
            binding.layoutProxFecha.error = when {
                prox.isEmpty() -> "Requerido"
                actual.isNotEmpty() && !ValidationUtils.isFechaPosterior(actual, prox) -> "Debe ser posterior a la fecha de aplicación"
                else -> null
            }
        }

        // Función auxiliar para re-validar la lógica de fechas
        fun validarRelacionFechas() {
            val fechaAplicacion = binding.txtFecha.text.toString()
            val proximaFecha = binding.txtProxFecha.text.toString()

            if (fechaAplicacion.isNotEmpty() && proximaFecha.isNotEmpty()) {
                if (!ValidationUtils.isFechaPosterior(fechaAplicacion, proximaFecha)) {
                    binding.layoutProxFecha.error = "Debe ser posterior a la fecha de aplicación"
                } else {
                    binding.layoutProxFecha.error = null
                }
            }
        }

        binding.txtFecha.addTextChangedListener {
            binding.layoutFecha.error = if (it.toString().isEmpty()) "Requerido" else null
            // IMPORTANTE: Cuando cambia la fecha de aplicación, validamos la relación
            validarRelacionFechas()
        }

        binding.txtProxFecha.addTextChangedListener {
            val prox = it.toString()
            binding.layoutProxFecha.error = when {
                prox.isEmpty() -> "Requerido"
                !ValidationUtils.isValidProximaFecha(prox) -> "Formato DD/MM/YYYY"
                else -> null
            }
            // También validamos la relación aquí
            validarRelacionFechas()
        }
    }

    private fun validarFormularioCompleto() {
        val nombre = binding.txtNombre.text.toString().trim()
        val marca = binding.txtMarca.text.toString().trim()
        val fecha = binding.txtFecha.text.toString().trim()
        val proxFecha = binding.txtProxFecha.text.toString().trim()
        val cantidad = binding.txtDosis.text.toString().trim()
        val unidad = binding.autoCompleteUnidad.text.toString()
        val dosis = "$cantidad $unidad"

        // 1. Validar Mascota
        binding.layoutMascota.error = if (ruacSeleccionado.isEmpty()) "Selecciona una mascota" else null

        // 2. Validar Fármaco / Vacuna
        binding.layoutNombre.error = when {
            nombre.isEmpty() -> "Obligatorio"
            !ValidationUtils.isValidMedicamento(nombre) -> "Mínimo 3 letras"
            else -> null
        }

        // 3. Validar Marca
        binding.layoutMarca.error = when {
            marca.isEmpty() -> "Obligatorio"
            !ValidationUtils.isValidMarca(marca) -> "Marca inválida"
            else -> null
        }

        // 4. Validar Dosis (Cantidad)
        binding.layoutDosis.error = if (cantidad.isEmpty()) "Ingresa la cantidad" else null

        // 5. Validar Fecha de Aplicación
        binding.layoutFecha.error = when {
            fecha.isEmpty() -> "Obligatorio"
            !ValidationUtils.isValidFechaDesparasitacion(fecha) -> "Formato inválido" // Asumiendo que usas el mismo método
            else -> null
        }

        // 6. Validar Próxima Fecha (LA CORRECCIÓN CLAVE)
        binding.layoutProxFecha.error = when {
            proxFecha.isEmpty() -> "Obligatorio"
            !ValidationUtils.isValidProximaFecha(proxFecha) -> "Formato DD/MM/YYYY"
            fecha.isNotEmpty() && !ValidationUtils.isFechaPosterior(fecha, proxFecha) -> "Debe ser posterior a la fecha de aplicación"
            else -> null
        }

        // 7. Evaluar si algún layout terminó con error
        val tieneErrores = listOf(
            binding.layoutMascota, binding.layoutNombre, binding.layoutMarca,
            binding.layoutDosis, binding.layoutFecha, binding.layoutProxFecha
        ).any { it.error != null }

        if (tieneErrores) {
            UiUtils.mostrarAlerta(this, "Formulario incompleto", "Por favor revisa los campos en rojo.", SweetAlertDialog.ERROR_TYPE)
            return
        }

        // Confirmación
        UiUtils.mostrarConfirmacionVacuna(
            activity = this,
            nombre = nombre,
            marca = marca,
            dosis = dosis,
            fecha = fecha,
            proximaFecha = proxFecha,
            onConfirm = { guardarVacuna(nombre, marca, dosis, fecha, proxFecha) },
            onCancel = { }
        )
    }

    private fun guardarVacuna(nombre: String, marca: String, dosis: String, fecha: String, proxFecha: String) {
        val vacuna = Vacunas(
            nombre = EncryptionUtils.encrypt(nombre),
            marca = EncryptionUtils.encrypt(marca),
            dosis = EncryptionUtils.encrypt(dosis),
            fecha = EncryptionUtils.encrypt(fecha),
            proximaFecha = EncryptionUtils.encrypt(proxFecha),
            ruacMascota = ruacSeleccionado
        )

        repository.registrarVacuna(ruacSeleccionado, vacuna,
            onSuccess = {
                UiUtils.mostrarAlerta(this, "Registro exitoso", "Vacuna registrada", SweetAlertDialog.SUCCESS_TYPE) { finish() }
            },
            onError = {
                UiUtils.mostrarAlerta(this, "Error", "No se pudo registrar", SweetAlertDialog.ERROR_TYPE)
            }
        )
    }

    private fun cargarMascotasUsuario() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("usuarios").document(uid).collection("mascotas").get()
            .addOnSuccessListener { result ->
                listaNombresMascotas.clear()
                mapaMascotas.clear()
                for (doc in result) {
                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    listaNombresMascotas.add(nombre)
                    mapaMascotas[nombre] = doc.id
                }
                binding.spinnerMascota.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNombresMascotas))
            }
    }

    private fun configurarCamposFecha() {
        binding.txtFecha.setOnClickListener { mostrarDatePicker { binding.txtFecha.setText(it) } }
        binding.txtProxFecha.setOnClickListener { mostrarDatePicker { binding.txtProxFecha.setText(it) } }
    }

    private fun mostrarDatePicker(onFecha: (String) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            onFecha(String.format("%02d/%02d/%04d", d, m + 1, y))
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
}