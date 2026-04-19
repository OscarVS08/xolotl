package com.example.xolotl.ui.main.usuario

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.data.models.Citas
import com.example.xolotl.data.repository.CitasRepository
import com.example.xolotl.databinding.ActivityAgregarCitaBinding
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AgregarCitasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarCitaBinding
    private val repository = CitasRepository()
    private val listaNombresMascotas = mutableListOf<String>()
    private val mapaMascotas = mutableMapOf<String, String>()
    private var ruacSeleccionado: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarCitaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Configurar la estructura de los campos (Solo listeners, sin adaptadores aún)
        configurarEstructuraCampos()

        // 2. Configurar validaciones y fechas
        configurarValidacionesTiempoReal()
        configurarCamposFechaHora()

        // 3. Cargar datos de Firebase (Aquí se pondrá el adaptador de mascotas)
        cargarMascotasUsuario()

        // 4. Cargar catálogo de servicios (Aquí se pone el adaptador de servicios)
        configurarMenuServicios()

        binding.btnGuardarCambios.setOnClickListener {
            validarFormularioCompleto()
        }

        binding.btnHome.setOnClickListener { finish() }
    }

    private fun configurarEstructuraCampos() {
        // Configuramos el comportamiento del spinner de mascotas UNA SOLA VEZ
        binding.spinnerMascota.apply {
            threshold = 1
            keyListener = null
            setOnItemClickListener { parent, _, position, _ ->
                val nombre = parent.getItemAtPosition(position).toString()
                ruacSeleccionado = mapaMascotas[nombre] ?: ""
                binding.txtRuac.setText(ruacSeleccionado)
                binding.layoutMascota.error = null
            }
            setOnClickListener { showDropDown() }
        }
    }

    private fun configurarMenuServicios() {
        val servicios = listOf("Consulta general", "Vacunación", "Desparasitación", "Esterilización", "Baño y estética", "Cirugía", "Urgencia", "Otro")
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, servicios)
        binding.txtServicio.setAdapter(adapter)

        binding.txtServicio.setOnItemClickListener { parent, _, position, _ ->
            val seleccionado = parent.getItemAtPosition(position).toString()
            binding.layoutServicio.error = null

            // LA LÓGICA QUE BUSCAMOS:
            if (seleccionado == "Otro") {
                // Mostramos el campo extra con animación simple
                binding.layoutOtroServicio.visibility = android.view.View.VISIBLE
                binding.txtOtroServicio.requestFocus()

                // Abrimos el teclado automáticamente
                val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.showSoftInput(binding.txtOtroServicio, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
            } else {
                // Si elige cualquier otro, escondemos el campo y lo limpiamos
                binding.layoutOtroServicio.visibility = android.view.View.GONE
                binding.txtOtroServicio.setText("")
                binding.layoutOtroServicio.error = null
            }
        }
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

                // CAMBIO CLAVE: Forzamos el layout y desactivamos el filtro
                val adapter = ArrayAdapter(this, R.layout.item_dropdown, listaNombresMascotas)
                binding.spinnerMascota.setAdapter(adapter)

                // Esto evita que se "sobreponga" o se vea remarcado al abrirlo
                binding.spinnerMascota.setText(binding.spinnerMascota.text.toString(), false)
            }
    }

    private fun configurarValidacionesTiempoReal() {
        binding.txtServicio.addTextChangedListener {
            binding.layoutServicio.error = if (!ValidationUtils.isValidServicio(it.toString()))
                "Mínimo 3 letras (máx 40)" else null
        }

        binding.txtFechaHora.addTextChangedListener {
            val fecha = it.toString()
            binding.layoutFechaHora.error = when {
                fecha.isEmpty() -> "Requerido"
                !ValidationUtils.isValidHorario(fecha) -> "Formato DD/MM/YYYY HH:mm"
                !ValidationUtils.isFechaFutura(fecha) -> "La cita no puede ser en el pasado"
                else -> null
            }
        }

        binding.txtNotas.addTextChangedListener {
            binding.layoutNotas.error = if (!ValidationUtils.isValidNotasCita(it.toString()))
                "Máximo 200 caracteres" else null
        }

        // Validación en tiempo real para el campo "Otro"
        binding.txtOtroServicio.addTextChangedListener {
            val texto = it.toString().trim()
            if (binding.layoutOtroServicio.visibility == android.view.View.VISIBLE) {
                binding.layoutOtroServicio.error = if (texto.length < 3) {
                    "Mínimo 3 caracteres"
                } else if (!ValidationUtils.isValidServicio(texto)) {
                    "Formato inválido"
                } else {
                    null
                }
            }
        }
    }

    private fun validarFormularioCompleto() {
        val servicio = binding.txtServicio.text.toString().trim()
        val otroServicio = binding.txtOtroServicio.text.toString().trim()
        val fechaHora = binding.txtFechaHora.text.toString().trim()
        val notas = binding.txtNotas.text.toString().trim()

        binding.layoutMascota.error = if (ruacSeleccionado.isEmpty()) "Selecciona una mascota" else null
        binding.layoutServicio.error = if (servicio.isEmpty()) "Obligatorio" else null
        if (servicio.isEmpty()) {
            binding.layoutServicio.error = "Campo obligatorio"
        } else if (servicio == "Otro") {
            // Validamos el campo extra: obligatorio y mínimo 3 caracteres
            if (otroServicio.isEmpty()) {
                binding.layoutOtroServicio.error = "Especifique el servicio"
            } else if (otroServicio.length < 3) {
                binding.layoutOtroServicio.error = "Mínimo 3 caracteres"
            } else if (!ValidationUtils.isValidServicio(otroServicio)) {
                binding.layoutOtroServicio.error = "Formato inválido"
            }
        }
        binding.layoutFechaHora.error = if (fechaHora.isEmpty()) "Obligatorio" else null

        val tieneErrores = listOf(
            binding.layoutMascota, binding.layoutServicio,
            binding.layoutFechaHora, binding.layoutNotas
        ).any { it.error != null }

        if (tieneErrores) {
            UiUtils.mostrarAlerta(this, "Formulario incompleto", "Por favor revisa los campos en rojo.", SweetAlertDialog.ERROR_TYPE)
            return
        }

        UiUtils.mostrarConfirmacionCita(
            activity = this,
            servicio = servicio,
            fecha = fechaHora,
            onConfirm = { guardarCita(servicio, fechaHora, notas) }
        )
    }

    private fun guardarCita(servicio: String, fechaHora: String, notas: String) {
        val cita = Citas(
            servicio = EncryptionUtils.encrypt(servicio),
            horario = EncryptionUtils.encrypt(fechaHora),
            notas = EncryptionUtils.encrypt(notas),
            ruacMascota = ruacSeleccionado
        )

        repository.registrarCita(ruacSeleccionado, cita,
            onSuccess = {
                UiUtils.mostrarAlerta(this, "¡Éxito!", "Cita registrada correctamente", SweetAlertDialog.SUCCESS_TYPE) {
                    finish()
                }
            },
            onError = {
                UiUtils.mostrarAlerta(this, "Error", "No se pudo agendar la cita", SweetAlertDialog.ERROR_TYPE)
            }
        )
    }

    private fun configurarCamposFechaHora() {
        binding.txtFechaHora.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                TimePickerDialog(this, { _, hour, minute ->
                    val f = String.format("%02d/%02d/%04d %02d:%02d", day, month + 1, year, hour, minute)
                    binding.txtFechaHora.setText(f)
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }
    }
}