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
    // Mantenemos dos mapas: uno para lo que ve el usuario y otro para la ruta de Firebase
    private val mapaRuacRealVisual = mutableMapOf<String, String>() // Nombre -> RUAC (Cifrado en DB)
    private val mapaDocIdFirebase = mutableMapOf<String, String>()  // Nombre -> ID del Documento

    private var idMascotaSeleccionada: String = "" // Este será el doc.id para la subcolección

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
        binding.spinnerMascota.apply {
            keyListener = null
            setOnItemClickListener { parent, _, position, _ ->
                val nombre = parent.getItemAtPosition(position).toString()

                // 1. Mostramos el RUAC real desencriptado al usuario
                binding.txtRuac.setText(mapaRuacRealVisual[nombre])

                // 2. Guardamos el ID del documento para la ruta de Firebase
                idMascotaSeleccionada = mapaDocIdFirebase[nombre] ?: ""

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
                mapaRuacRealVisual.clear()
                mapaDocIdFirebase.clear()

                for (doc in result) {
                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    val ruacRealCifrado = doc.getString("ruac") ?: ""
                    val ruacDesencriptado = EncryptionUtils.decrypt(ruacRealCifrado)

                    listaNombresMascotas.add(nombre)

                    // Llenamos los mapas
                    mapaRuacRealVisual[nombre] = ruacDesencriptado
                    mapaDocIdFirebase[nombre] = doc.id // Usamos el ID del documento como referencia
                }

                val adapter = ArrayAdapter(this, R.layout.item_dropdown, listaNombresMascotas)
                binding.spinnerMascota.setAdapter(adapter)
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
        val nombreMascota = binding.spinnerMascota.text.toString()

        binding.layoutMascota.error = if (idMascotaSeleccionada.isEmpty()) "Selecciona una mascota" else null
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
            onConfirm = { guardarCita(servicio, fechaHora, notas, nombreMascota) }
        )
    }

    private fun guardarCita(servicio: String, fechaHora: String, notas: String, nombreMascota: String) {
        val cita = Citas(
            servicio = EncryptionUtils.encrypt(servicio),
            horario = EncryptionUtils.encrypt(fechaHora),
            notas = EncryptionUtils.encrypt(notas),
            ruacMascota = idMascotaSeleccionada, // Guardamos el ID del doc para la ruta
            nombreMascota = EncryptionUtils.encrypt(nombreMascota) // Opcional: Cifrar nombre en la cita
        )

        // Usamos el ID del documento para que el repositorio sepa dónde escribir
        repository.registrarCita(idMascotaSeleccionada, cita,
            onSuccess = {
                UiUtils.mostrarAlerta(this, "¡Éxito!", "Cita registrada", SweetAlertDialog.SUCCESS_TYPE) { finish() }
            },
            onError = {
                UiUtils.mostrarAlerta(this, "Error", "No se pudo agendar", SweetAlertDialog.ERROR_TYPE)
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