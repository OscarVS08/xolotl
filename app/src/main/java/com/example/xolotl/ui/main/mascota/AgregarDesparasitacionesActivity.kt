package com.example.xolotl.ui.main.mascota

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.data.models.Desparasitaciones
import com.example.xolotl.data.repository.DesparasitacionRepository
import com.example.xolotl.databinding.ActivityAgregarDesparasitacionBinding
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AgregarDesparasitacionesActivity : AppCompatActivity() {

    // Usamos exclusivamente Binding para evitar el error de UninitializedPropertyAccessException
    private lateinit var binding: ActivityAgregarDesparasitacionBinding

    private val repository = DesparasitacionRepository()
    private val listaNombresMascotas = mutableListOf<String>()
    private val mapaMascotas = mutableMapOf<String, String>() // nombre -> RUAC
    private var ruacSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Inicialización de Binding
        binding = ActivityAgregarDesparasitacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2. Carga de datos y configuraciones iniciales
        cargarMascotasUsuario()
        configurarValidacionesTiempoReal()
        configurarCamposFecha()

        // 3. Configuración del Spinner de Mascotas
        binding.spinnerMascota.apply {
            threshold = 1
            keyListener = null // Forzar selección, no escritura
            setOnItemClickListener { parent, _, position, _ ->
                val nombreMascota = parent.getItemAtPosition(position).toString()
                ruacSeleccionado = mapaMascotas[nombreMascota] ?: ""
                binding.layoutMascota.error = null // Limpiar error al seleccionar
            }
            setOnClickListener { showDropDown() }
        }


        val adapter = ArrayAdapter(
            this,
            R.layout.item_dropdown, // <--- Nuevo layout personalizado
            listaNombresMascotas
        )
        binding.spinnerMascota.setAdapter(adapter)

        // 4. Botón Guardar
        binding.btnGuardar.setOnClickListener {
            validarFormularioCompleto()
        }

        // 5. Botón Home
        binding.btnHome.setOnClickListener {
            finish()
        }
    }

    private fun configurarValidacionesTiempoReal() {
        binding.txtMetodo.addTextChangedListener {
            binding.layoutMetodo.error = if (!ValidationUtils.isValidMetodo(it.toString()))
                "Mínimo 3 letras (máx 30)" else null
        }

        binding.txtNombre.addTextChangedListener {
            binding.layoutNombre.error = if (!ValidationUtils.isValidMedicamento(it.toString()))
                "Nombre de fármaco inválido" else null
        }

        binding.txtMarca.addTextChangedListener {
            binding.layoutMarca.error = if (!ValidationUtils.isValidMarca(it.toString()))
                "Marca inválida" else null
        }

        binding.txtFecha.addTextChangedListener {
            binding.layoutFecha.error = if (it.toString().isEmpty()) "Requerido" else null
        }

        binding.txtProxFecha.addTextChangedListener {
            val prox = it.toString()
            val actual = binding.txtFecha.text.toString()

            binding.layoutProxFecha.error = when {
                !ValidationUtils.isValidProximaFecha(prox) -> "Formato DD/MM/YYYY"
                actual.isNotEmpty() && !ValidationUtils.isFechaPosterior(actual, prox) ->
                    "Debe ser posterior a la fecha de aplicación"
                else -> null
            }
        }

        // Función auxiliar para re-validar la lógica de fechas
        fun validarRelacionFechas() {
            val fechaAplicacion = binding.txtFecha.text.toString()
            val proximaFecha = binding.txtProxFecha.text.toString()

            if (fechaAplicacion.isNotEmpty() && proximaFecha.isNotEmpty()) {
                if (!ValidationUtils.isFechaPosterior(fechaAplicacion, proximaFecha)) {
                    binding.layoutProxFecha.error = "Debe ser posterior a la aplicación"
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
        val metodo = binding.txtMetodo.text.toString().trim()
        val nombre = binding.txtNombre.text.toString().trim()
        val marca = binding.txtMarca.text.toString().trim()
        val fecha = binding.txtFecha.text.toString().trim()
        val proxFecha = binding.txtProxFecha.text.toString().trim()

        // Activar visualización de errores modernos (TextInputLayout)
        binding.layoutMascota.error = if (ruacSeleccionado.isEmpty()) "Selecciona una mascota" else null
        binding.layoutMetodo.error = if (metodo.isEmpty()) "Obligatorio" else null
        binding.layoutNombre.error = if (nombre.isEmpty()) "Obligatorio" else null
        binding.layoutMarca.error = if (marca.isEmpty()) "Obligatorio" else null
        binding.layoutFecha.error = if (fecha.isEmpty()) "Obligatorio" else null
        binding.layoutProxFecha.error = if (proxFecha.isEmpty()) "Obligatorio" else null

        // Verificación de errores
        val tieneErrores = listOf(
            binding.layoutMascota, binding.layoutMetodo, binding.layoutNombre,
            binding.layoutMarca, binding.layoutFecha, binding.layoutProxFecha
        ).any { it.error != null }

        if (tieneErrores) {
            UiUtils.mostrarAlerta(this, "Formulario incompleto", "Por favor revisa los campos en rojo.", SweetAlertDialog.ERROR_TYPE)
            return
        }

        // Confirmación personalizada
        UiUtils.mostrarConfirmacionDesparasitacion(
            activity = this,
            nombre = nombre,
            marca = marca,
            fecha = fecha,
            proximaFecha = proxFecha,
            metodo = metodo,
            onConfirm = { guardarDesparasitacion(nombre, marca, fecha, proxFecha, metodo) },
            onCancel = { }
        )
    }

    private fun guardarDesparasitacion(nombre: String, marca: String, fecha: String, proxFecha: String, metodo: String) {
        val desparasitacion = Desparasitaciones(
            tipo = EncryptionUtils.encrypt(metodo),
            nombre = EncryptionUtils.encrypt(nombre),
            marca = EncryptionUtils.encrypt(marca),
            fecha = EncryptionUtils.encrypt(fecha),
            proximaFecha = EncryptionUtils.encrypt(proxFecha),
            ruacMascota = ruacSeleccionado
        )

        repository.registrarDesparasitacion(ruacSeleccionado, desparasitacion,
            onSuccess = {
                UiUtils.mostrarAlerta(this, "Registro exitoso", "Datos agregados correctamente", SweetAlertDialog.SUCCESS_TYPE) {
                    finish()
                }
            },
            onError = {
                UiUtils.mostrarAlerta(this, "Error", "No se pudo registrar la desparasitación", SweetAlertDialog.ERROR_TYPE)
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
                val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listaNombresMascotas)
                binding.spinnerMascota.setAdapter(adapter)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show()
            }
    }

    private fun configurarCamposFecha() {
        binding.txtFecha.setOnClickListener {
            mostrarDatePicker { binding.txtFecha.setText(it) }
        }
        binding.txtProxFecha.setOnClickListener {
            mostrarDatePicker { binding.txtProxFecha.setText(it) }
        }
    }

    private fun mostrarDatePicker(onFechaSeleccionada: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            val fechaFormateada = String.format("%02d/%02d/%04d", day, month + 1, year)
            onFechaSeleccionada(fechaFormateada)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}