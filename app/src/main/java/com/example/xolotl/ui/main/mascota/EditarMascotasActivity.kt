package com.example.xolotl.ui.main.mascota

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.databinding.ActivityEditarMascotasBinding
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class EditarMascotasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarMascotasBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var mascotaId: String
    private lateinit var userId: String
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var nuevaImagenBase64: String? = null

    private val opcionesFecha = listOf("Seleccionar fecha", "Desconozco dato")
    private val especies = listOf("Perro", "Gato")
    private val razasPerro = listOf("Labrador", "Pug", "Chihuahua", "Pastor Alemán", "Pitbull", "Golden Retriever", "Husky", "Beagle", "Shih Tzu", "Otro")
    private val razasGato = listOf("Siames", "Persa", "Bombay", "Angora", "Azul Ruso", "Bengalí", "Maine Coon", "Siberiano", "Otro")
    private val colores = listOf("Blanco", "Negro", "Café", "Gris", "Atigrado", "Naranja", "Otro")
    private val sexos = listOf("Macho", "Hembra")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarMascotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cantidad de caracteres
        binding.txtnumeroTelefonoDueno.filters = arrayOf(
            android.text.InputFilter.LengthFilter(10) // Esto bloquea el teclado en el carácter 11
        )

        // Cantidad de caracteres
        binding.txtnumTelAltDuen.filters = arrayOf(
            android.text.InputFilter.LengthFilter(10) // Esto bloquea el teclado en el carácter 11
        )

        mascotaId = intent.getStringExtra("docId") ?: return
        userId = FirebaseAuth.getInstance().uid ?: return

        setupImagePicker()
        setupDropdowns()
        setupValidacionesTiempoReal()
        cargarDatosMascota()
        cargarDatosDueno()

        binding.btnHome.setOnClickListener { finish() }

        binding.imgFotoMascotaTop.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            imagePickerLauncher.launch(intent)
        }

        binding.btnAceptarMorado.setOnClickListener {
            if (validarFormularioCompleto()) {
                guardarCambios()
            }
        }
    }

    private fun cargarDatosMascota() {
        db.collection("usuarios").document(userId).collection("mascotas").document(mascotaId)
            .get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    val ruac = EncryptionUtils.decrypt(doc.getString("ruac") ?: "")
                    val especie = EncryptionUtils.decrypt(doc.getString("especie") ?: "")
                    val raza = EncryptionUtils.decrypt(doc.getString("raza") ?: "")
                    val sexo = EncryptionUtils.decrypt(doc.getString("sexo") ?: "")
                    val fechaNac = EncryptionUtils.decrypt(doc.getString("fechaNacimiento") ?: "")
                    val fechaAdop = EncryptionUtils.decrypt(doc.getString("fechaAdopcion") ?: "")
                    val color = EncryptionUtils.decrypt(doc.getString("color") ?: "")
                    val peso = EncryptionUtils.decrypt(doc.getString("peso") ?: "")
                    val estatura = EncryptionUtils.decrypt(doc.getString("estatura") ?: "")
                    val alergias = EncryptionUtils.decrypt(doc.getString("alergias") ?: "")
                    val notas = EncryptionUtils.decrypt(doc.getString("notas") ?: "")

                    binding.txtNombreMascotaTop.setText(nombre)
                    binding.txtRuacTop.text = "$ruac"
                    binding.txtEspecie.setText(especie, false)
                    binding.txtSexo.setText(sexo, false)
                    binding.txtFechaNacimiento.setText(fechaNac, false)
                    binding.txtFechaAdopcion.setText(fechaAdop, false)
                    binding.txtPeso.setText(peso)
                    binding.txtEstatura.setText(estatura)
                    binding.txtAlergias.setText(alergias)
                    binding.txtNotas.setText(notas)

                    actualizarAdapterRaza(especie)
                    binding.txtRaza.setText(raza, false)
                    binding.txtColor.setText(color, false)

                    // Lógica Anti-Crash Foto
                    val dataFoto = doc.getString("fotoBase64") ?: ""
                    if (dataFoto.isNotEmpty()) {
                        try {
                            val fotoBase64 = try { EncryptionUtils.decrypt(dataFoto) } catch (e: Exception) { dataFoto }
                            val bytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            if (bitmap != null) {
                                binding.imgFotoMascotaTop.setImageBitmap(bitmap)
                                nuevaImagenBase64 = fotoBase64
                            }
                        } catch (e: Exception) { e.printStackTrace() }
                    }
                }
            }
    }

    private fun cargarDatosDueno() {
        db.collection("usuarios").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                val apP = EncryptionUtils.decrypt(doc.getString("apellidoP") ?: "")
                val apM = EncryptionUtils.decrypt(doc.getString("apellidoM") ?: "")
                binding.txtnombreDueno.text = "$nombre $apP $apM"
                binding.txtnumeroTelefonoDueno.setText(EncryptionUtils.decrypt(doc.getString("telefono") ?: ""))
                binding.txtnumTelAltDuen.setText(EncryptionUtils.decrypt(doc.getString("telefonoAlt") ?: ""))
            }
        }
    }

    private fun setupDropdowns() {
        val adapterFechas = ArrayAdapter(this, R.layout.item_dropdown, opcionesFecha)
        val adapterEspecie = ArrayAdapter(this, R.layout.item_dropdown, especies)
        val adapterColor = ArrayAdapter(this, R.layout.item_dropdown, colores)
        val adapterSexo = ArrayAdapter(this, R.layout.item_dropdown, sexos)

        binding.txtFechaNacimiento.setAdapter(adapterFechas)
        binding.txtFechaNacimiento.setOnItemClickListener { _, _, pos, _ ->
            if (pos == 0) mostrarCalendario(binding.txtFechaNacimiento)
            else {
                binding.txtFechaNacimiento.setText("Desconozco dato", false)
                binding.layoutFechaNacimiento.error = null
            }
            binding.txtFechaNacimiento.post { validarRelacionFechas() }
        }

        binding.txtFechaAdopcion.setAdapter(adapterFechas)
        binding.txtFechaAdopcion.setOnItemClickListener { _, _, pos, _ ->
            if (pos == 0) mostrarCalendario(binding.txtFechaAdopcion)
            else {
                binding.txtFechaAdopcion.setText("Desconozco dato", false)
                binding.layoutFechaAdopcion.error = null
            }
            binding.txtFechaAdopcion.post { validarRelacionFechas() }
        }

        binding.txtEspecie.setAdapter(adapterEspecie)
        binding.txtEspecie.setOnItemClickListener { parent, _, _, _ ->
            val esp = parent.getItemAtPosition(0).toString()
            actualizarAdapterRaza(esp)
            binding.txtRaza.setText("", false)
            binding.layoutRazaOtro.visibility = View.GONE
        }

        binding.txtRaza.setOnItemClickListener { parent, _, pos, _ ->
            val raza = parent.getItemAtPosition(pos).toString()
            binding.layoutRazaOtro.visibility = if (raza == "Otro") View.VISIBLE else View.GONE
        }

        binding.txtColor.setAdapter(adapterColor)
        binding.txtColor.setOnItemClickListener { parent, _, pos, _ ->
            val col = parent.getItemAtPosition(pos).toString()
            binding.layoutOtroColor.visibility = if (col == "Otro") View.VISIBLE else View.GONE
        }

        binding.txtSexo.setAdapter(adapterSexo)
    }

    private fun actualizarAdapterRaza(especie: String) {
        val razas = if (especie == "Perro") razasPerro else razasGato
        binding.txtRaza.setAdapter(ArrayAdapter(this, R.layout.item_dropdown, razas))
    }

    private fun mostrarCalendario(textView: AutoCompleteTextView) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val fecha = "%02d/%02d/%04d".format(d, m + 1, y)
            textView.setText(fecha, false)
            validarRelacionFechas()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validarRelacionFechas() {
        val nac = binding.txtFechaNacimiento.text.toString().trim()
        val adop = binding.txtFechaAdopcion.text.toString().trim()
        if (nac.isNotEmpty() && adop.isNotEmpty() && nac != "Seleccionar fecha" && adop != "Seleccionar fecha") {
            if (!ValidationUtils.esFechaPosterior(nac, adop)) {
                binding.layoutFechaAdopcion.error = "No puede ser adoptado antes de nacer"
            } else {
                binding.layoutFechaAdopcion.error = null
                binding.layoutFechaNacimiento.error = null
            }
        }
    }

    private fun setupValidacionesTiempoReal() {
        setupFieldWatcher(binding.txtNombreMascotaTop, binding.layoutNombreMascotaTop, ValidationUtils::isValidPetName, "Mínimo 2 letras, máximo 30")
        setupFieldWatcher(binding.txtnumeroTelefonoDueno, binding.layoutTelefonoDueno, ValidationUtils::isValidPhone, "Se requieren 10 dígitos")
        setupFieldWatcher(binding.txtnumTelAltDuen, binding.layoutTelAlt, ValidationUtils::isValidPhone, "Se requieren 10 dígitos")

        binding.txtPeso.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val esp = binding.txtEspecie.text.toString()
                if (!ValidationUtils.isValidPesoPorEspecie(s.toString(), esp)) {
                    val max = if (esp == "Gato") "15kg" else "100kg"
                    binding.layoutPeso.error = "Para $esp: 0.1 a $max"
                } else { binding.layoutPeso.error = null }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.txtEstatura.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val esp = binding.txtEspecie.text.toString()
                if (!ValidationUtils.isValidEstaturaPorEspecie(s.toString(), esp)) {
                    val max = if (esp == "Gato") "50cm" else "110cm"
                    binding.layoutEstatura.error = "Para $esp: 5 a $max cm"
                } else { binding.layoutEstatura.error = null }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 5. Campos Dinámicos: Raza "Otro"
        binding.txtRazaOtro.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.layoutRazaOtro.visibility == android.view.View.VISIBLE) {
                    val texto = s.toString().trim()
                    binding.layoutRazaOtro.error = when {
                        texto.isEmpty() -> "Especifique la raza"
                        texto.length < 3 -> "Mínimo 3 caracteres"
                        !ValidationUtils.isValidPetRace(texto) -> "Formato de raza inválido"
                        else -> null
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 6. Campos Dinámicos: Color "Otro"
        binding.txtOtroColor.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.layoutOtroColor.visibility == android.view.View.VISIBLE) {
                    val texto = s.toString().trim()
                    binding.layoutOtroColor.error = when {
                        texto.isEmpty() -> "Especifique el color"
                        texto.length < 3 -> "Mínimo 3 caracteres"
                        !ValidationUtils.isValidColor(texto) -> "Formato de color inválido"
                        else -> null
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFieldWatcher(edit: EditText, layout: com.google.android.material.textfield.TextInputLayout, validator: (String) -> Boolean, error: String) {
        edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { layout.error = if (!validator(s.toString().trim())) error else null }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validarFormularioCompleto(): Boolean {
        val esp = binding.txtEspecie.text.toString().trim()

        // 1. Validaciones básicas
        binding.layoutNombreMascotaTop.error = if (!ValidationUtils.isValidPetName(binding.txtNombreMascotaTop.text.toString())) "Nombre inválido" else null
        binding.layoutTelefonoDueno.error = if (!ValidationUtils.isValidPhone(binding.txtnumeroTelefonoDueno.text.toString())) "Teléfono inválido" else null
        binding.layoutTelAlt.error = if (!ValidationUtils.isValidPhone(binding.txtnumTelAltDuen.text.toString())) "Teléfono inválido" else null

        // 2. Validaciones de fechas
        val nac = binding.txtFechaNacimiento.text.toString()
        binding.layoutFechaNacimiento.error = if (nac.isEmpty() || nac == "Seleccionar fecha") "Dato obligatorio" else null

        val adop = binding.txtFechaAdopcion.text.toString()
        binding.layoutFechaAdopcion.error = if (adop.isEmpty() || adop == "Seleccionar fecha") "Dato obligatorio" else null

        validarRelacionFechas()

        // 3. Peso y Estatura
        binding.layoutPeso.error = if (!ValidationUtils.isValidPesoPorEspecie(binding.txtPeso.text.toString(), esp)) "Peso inválido" else null
        binding.layoutEstatura.error = if (!ValidationUtils.isValidEstaturaPorEspecie(binding.txtEstatura.text.toString(), esp)) "Estatura inválida" else null

        // 4. Validar campos "Otro" (Solo si están visibles)
        if (binding.layoutRazaOtro.visibility == View.VISIBLE) {
            binding.layoutRazaOtro.error = if (binding.txtRazaOtro.text.toString().trim().isEmpty()) "Campo obligatorio" else null
        } else {
            binding.layoutRazaOtro.error = null
        }

        if (binding.layoutOtroColor.visibility == View.VISIBLE) {
            binding.layoutOtroColor.error = if (binding.txtOtroColor.text.toString().trim().isEmpty()) "Campo obligatorio" else null
        } else {
            binding.layoutOtroColor.error = null
        }

        // 5. REVISIÓN CRÍTICA: Incluir TODOS los campos activos en la lista de errores
        val camposAValidar = mutableListOf(
            binding.layoutNombreMascotaTop,
            binding.layoutTelefonoDueno,
            binding.layoutTelAlt,
            binding.layoutFechaNacimiento,
            binding.layoutFechaAdopcion,
            binding.layoutPeso,
            binding.layoutEstatura
        )

        // Agregamos los campos dinámicos a la lista de revisión final solo si el usuario debe llenarlos
        if (binding.layoutRazaOtro.visibility == View.VISIBLE) camposAValidar.add(binding.layoutRazaOtro)
        if (binding.layoutOtroColor.visibility == View.VISIBLE) camposAValidar.add(binding.layoutOtroColor)

        // Ahora sí, revisamos si alguno de los campos de la lista tiene error
        val tieneErrores = camposAValidar.any { it.error != null }

        if (tieneErrores) {
            UiUtils.mostrarAlerta(this, "Atención", "Revisa los campos en rojo", SweetAlertDialog.ERROR_TYPE)
            return false
        }

        return true
    }

    private fun guardarCambios() {
        // Usamos la función genérica de dos botones para confirmar
        UiUtils.mostrarAlertaCerrarSesion(
            activity = this,
            titulo = "¿Guardar cambios?",
            mensaje = "Se actualizará la información de la mascota en el sistema.",
            tipo = SweetAlertDialog.WARNING_TYPE,
            confirmText = "Sí, guardar",
            cancelText = "Cancelar",
            onConfirm = {
                // --- ESTO SE EJECUTA SOLO SI EL USUARIO CONFIRMA ---

                // Preparamos los datos (incluyendo lógica de "Otro")
                val razaFinal = if (binding.layoutRazaOtro.visibility == View.VISIBLE)
                    binding.txtRazaOtro.text.toString() else binding.txtRaza.text.toString()

                val colorFinal = if (binding.layoutOtroColor.visibility == View.VISIBLE)
                    binding.txtOtroColor.text.toString() else binding.txtColor.text.toString()

                val datos = hashMapOf(
                    "nombre" to EncryptionUtils.encrypt(binding.txtNombreMascotaTop.text.toString()),
                    "fechaNacimiento" to EncryptionUtils.encrypt(binding.txtFechaNacimiento.text.toString()),
                    "fechaAdopcion" to EncryptionUtils.encrypt(binding.txtFechaAdopcion.text.toString()),
                    "especie" to EncryptionUtils.encrypt(binding.txtEspecie.text.toString()),
                    "raza" to EncryptionUtils.encrypt(razaFinal),
                    "color" to EncryptionUtils.encrypt(colorFinal),
                    "sexo" to EncryptionUtils.encrypt(binding.txtSexo.text.toString()),
                    "peso" to EncryptionUtils.encrypt(binding.txtPeso.text.toString()),
                    "estatura" to EncryptionUtils.encrypt(binding.txtEstatura.text.toString()),
                    "alergias" to EncryptionUtils.encrypt(binding.txtAlergias.text.toString()),
                    "notas" to EncryptionUtils.encrypt(binding.txtNotas.text.toString()),
                    "fotoBase64" to (nuevaImagenBase64 ?: "")
                )

                // Ejecutamos la actualización en Firestore
                db.collection("usuarios").document(userId).collection("mascotas").document(mascotaId)
                    .update(datos as Map<String, Any>)
                    .addOnSuccessListener {
                        UiUtils.mostrarAlerta(this, "¡Éxito!", "Mascota actualizada correctamente", SweetAlertDialog.SUCCESS_TYPE) {
                            finish() // Regresar a la pantalla anterior
                        }
                    }
                    .addOnFailureListener { e ->
                        UiUtils.mostrarAlerta(this, "Error", "No se pudo actualizar: ${e.message}", SweetAlertDialog.ERROR_TYPE)
                    }
            },
            onCancel = {
                // No hacemos nada, la alerta se cierra sola y el usuario sigue en el formulario
            }
        )
    }

    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    binding.imgFotoMascotaTop.setImageURI(uri)
                    val inputStream = contentResolver.openInputStream(uri)
                    val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, 500, 500, true)
                    val outputStream = java.io.ByteArrayOutputStream()
                    scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
                    nuevaImagenBase64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.DEFAULT)
                }
            }
        }
    }
}