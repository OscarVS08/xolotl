package com.example.xolotl.ui.main.mascota

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.data.models.Mascotas
import com.example.xolotl.data.repository.MascotaRepository
import com.example.xolotl.databinding.ActivityAgregarMascotaBinding
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AgregarMascotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarMascotaBinding
    private val repo = MascotaRepository()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var imagenEnBase64: String? = null

    // Listas de datos
    private val opcionesFecha = listOf("Seleccionar fecha", "Desconozco dato")
    private val especies = listOf("Perro", "Gato")
    private val razasPerro = listOf("Labrador", "Pug", "Chihuahua", "Pastor Alemán", "Pitbull", "Golden Retriever", "Husky", "Beagle", "Shih Tzu", "Otro")
    private val razasGato = listOf("Siames", "Persa", "Bombay", "Angora", "Azul Ruso", "Bengalí", "Maine Coon", "Siberiano", "Otro")
    private val colores = listOf("Blanco", "Negro", "Café", "Gris", "Atigrado", "Naranja", "Otro")
    private val sexos = listOf("Macho", "Hembra")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarMascotaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Forzar mayusculas en RUAC y cantidad de caracteres
        binding.txtRuac.filters = arrayOf(
            android.text.InputFilter.AllCaps(),
            android.text.InputFilter.LengthFilter(10) // Esto bloquea el teclado en el carácter 11
        )

        // Pagina del RUAC
        binding.txtTramitarRuac.setOnClickListener {
            abrirPaginaRuac()
        }

        setupImagePicker()
        setupDropdowns()
        setupValidacionesTiempoReal()
        validarRelacionFechas()

        binding.btnFoto.setOnClickListener { abrirGaleria() }
        binding.btnHome.setOnClickListener { finish() }
        binding.btnGuardarMascota.setOnClickListener {
            if (validarFormularioCompleto()) {
                registrarMascota()
            }
        }

        // Listener para abrir el link de identificación de raza
        binding.txtIdentificarRaza.setOnClickListener {
            val url = "https://www.snuffalo.com/"
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url))
            startActivity(intent)
        }
    }

    private fun setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    // 1. Mostrar la foto original en la UI
                    binding.imgFotoMascota.setImageURI(uri)

                    // 2. Procesar la imagen para que sea ligera
                    try {
                        val inputStream = contentResolver.openInputStream(uri)
                        val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)

                        // Redimensionar a un tamaño razonable (ej. 500x500)
                        val scaledBitmap = android.graphics.Bitmap.createScaledBitmap(originalBitmap, 500, 500, true)

                        val outputStream = java.io.ByteArrayOutputStream()
                        // Comprimir al 70% de calidad para ahorrar espacio
                        scaledBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)

                        val bytes = outputStream.toByteArray()
                        imagenEnBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)

                        // Limpieza para evitar fugas de memoria
                        originalBitmap.recycle()
                        scaledBitmap.recycle()
                    } catch (e: Exception) {
                        UiUtils.mostrarAlerta(this, "Error", "No se pudo procesar la imagen", SweetAlertDialog.ERROR_TYPE)
                    }
                }
            }
        }
    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        imagePickerLauncher.launch(intent)
    }

    private fun setupDropdowns() {
        val adapterFechas = ArrayAdapter(this, R.layout.item_dropdown, opcionesFecha)
        val adapterColores = ArrayAdapter(this, R.layout.item_dropdown, colores)
        val adapterSexo = ArrayAdapter(this, R.layout.item_dropdown, sexos)
        val adapterEspecie = ArrayAdapter(this, R.layout.item_dropdown, especies)

        // Fecha Nacimiento e Ingreso
// --- FECHA NACIMIENTO ---
        binding.txtFechaNacimiento.setAdapter(adapterFechas)
        binding.txtFechaNacimiento.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) mostrarCalendario(binding.txtFechaNacimiento)
            else {
                binding.txtFechaNacimiento.setText("Desconozco dato", false)
                binding.layoutFechaNacimiento.error = null
            }
            // Ejecutamos después de que el sistema asiente el texto
            binding.txtFechaNacimiento.post { validarRelacionFechas() }
        }

// --- FECHA ADOPCION ---
        binding.txtFechaAdopcion.setAdapter(adapterFechas)
        binding.txtFechaAdopcion.setOnItemClickListener { _, _, position, _ ->
            if (position == 0) mostrarCalendario(binding.txtFechaAdopcion)
            else {
                binding.txtFechaAdopcion.setText("Desconozco dato", false)
                binding.layoutFechaAdopcion.error = null
            }
            // Ejecutamos después de que el sistema asiente el texto
            binding.txtFechaAdopcion.post { validarRelacionFechas() }
        }

        // Especie y Raza (Cascada)
        binding.txtEspecie.setAdapter(adapterEspecie)
        binding.txtEspecie.setOnItemClickListener { parent, _, position, _ ->
            val especie = parent.getItemAtPosition(position).toString()
            binding.layoutEspecie.error = null
            binding.txtRaza.setText("", false)
            binding.layoutRazaOtro.visibility = View.GONE

            val razas = if (especie == "Perro") razasPerro else razasGato
            binding.txtRaza.setAdapter(ArrayAdapter(this, R.layout.item_dropdown, razas))
        }

        binding.txtRaza.setOnItemClickListener { parent, _, position, _ ->
            val raza = parent.getItemAtPosition(position).toString()
            binding.layoutRaza.error = null
            if (raza == "Otro") {
                binding.layoutRazaOtro.visibility = View.VISIBLE
                binding.txtIdentificarRaza.visibility = View.VISIBLE
            } else {
                binding.layoutRazaOtro.visibility = View.GONE
                binding.txtIdentificarRaza.visibility = View.GONE
            }
        }

        // Color
        binding.txtColor.setAdapter(adapterColores)
        binding.txtColor.setOnItemClickListener { parent, _, position, _ ->
            val color = parent.getItemAtPosition(position).toString()
            binding.layoutColor.error = null
            if (color == "Otro") binding.layoutOtroColor.visibility = View.VISIBLE
            else binding.layoutOtroColor.visibility = View.GONE
        }

        // Sexo
        binding.txtSexo.setAdapter(adapterSexo)
        binding.txtSexo.setOnItemClickListener { _, _, _, _ -> binding.layoutSexo.error = null }
    }

    private fun mostrarCalendario(textView: AutoCompleteTextView) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            val fecha = "%02d/%02d/%04d".format(d, m + 1, y)
            textView.setText(fecha, false)

            // Llamamos a la validación inmediatamente después de setear la fecha
            validarRelacionFechas()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validarRelacionFechas() {
        // Usamos trim() para evitar que espacios invisibles rompan la comparación
        val nac = binding.txtFechaNacimiento.text.toString().trim()
        val adop = binding.txtFechaAdopcion.text.toString().trim()

        if (nac.isNotEmpty() && adop.isNotEmpty()) {
            // La lógica de esFechaPosterior ya incluye el "mismo día" (!before)
            if (!ValidationUtils.esFechaPosterior(nac, adop)) {
                binding.layoutFechaAdopcion.error = "La adopción no puede ser antes del nacimiento"
            } else {
                // Esto se ejecutará si son iguales o si la adopción es posterior
                binding.layoutFechaAdopcion.error = null
                binding.layoutFechaNacimiento.error = null
            }
        }
    }

    private fun setupValidacionesTiempoReal() {
        // 1. Campos de Texto Simples
        setupFieldWatcher(binding.txtRuac, binding.layoutRuac, ValidationUtils::isValidRuac, "RUAC debe tener 10 caracteres (letras y números)")
        setupFieldWatcher(binding.txtNombreMascota, binding.layoutNombreMascota, ValidationUtils::isValidPetName, "Nombre inválido (máx 30 letras)")

        // 2. Campos Opcionales (validados si el usuario escribe)
        setupFieldWatcher(binding.txtAlergias, binding.layoutAlergias, ValidationUtils::isValidAlergia, "Texto inválido o demasiado largo")
        setupFieldWatcher(binding.txtNotas, binding.layoutNotas, ValidationUtils::isValidNotas, "Texto inválido o demasiado largo")

        // 3. Peso en tiempo real con contexto de especie
        binding.txtPeso.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val especie = binding.txtEspecie.text.toString()
                val peso = s.toString()
                if (peso.isNotEmpty()) {
                    if (!ValidationUtils.isValidPesoPorEspecie(peso, especie)) {
                        val max = if (especie == "Gato") "15kg" else "100kg"
                        binding.layoutPeso.error = "Peso excedido para $especie (máx $max)"
                    } else {
                        binding.layoutPeso.error = null
                    }
                } else {
                    binding.layoutPeso.error = "Campo obligatorio"
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 4. Estatura en tiempo real con contexto de especie
        binding.txtEstatura.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val especie = binding.txtEspecie.text.toString()
                val estatura = s.toString()
                if (estatura.isNotEmpty()) {
                    if (!ValidationUtils.isValidEstaturaPorEspecie(estatura, especie)) {
                        val max = if (especie == "Gato") "50cm" else "110cm"
                        binding.layoutEstatura.error = "Estatura excedida para $especie (máx $max)"
                    } else {
                        binding.layoutEstatura.error = null
                    }
                } else {
                    binding.layoutEstatura.error = "Campo obligatorio"
                }
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
            override fun afterTextChanged(s: Editable?) {
                layout.error = if (!validator(s.toString().trim())) error else null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun validarFormularioCompleto(): Boolean {
        // 1. Extraer la especie actual para validar peso y estatura
        val especieActual = binding.txtEspecie.text.toString()

        // 2. Validaciones de Texto y Selección
        binding.layoutRuac.error = if (!ValidationUtils.isValidRuac(binding.txtRuac.text.toString())) "RUAC inválido" else null
        binding.layoutNombreMascota.error = if (binding.txtNombreMascota.text!!.isEmpty()) "Campo obligatorio" else null
        binding.layoutFechaNacimiento.error = if (binding.txtFechaNacimiento.text.isEmpty()) "Seleccione opción" else null
        binding.layoutFechaAdopcion.error = if (binding.txtFechaAdopcion.text.isEmpty()) "Seleccione opción" else null
        binding.layoutEspecie.error = if (binding.txtEspecie.text.isEmpty()) "Obligatorio" else null
        binding.layoutRaza.error = if (binding.txtRaza.text.isEmpty()) "Obligatorio" else null
        binding.layoutColor.error = if (binding.txtColor.text.isEmpty()) "Obligatorio" else null
        binding.layoutSexo.error = if (binding.txtSexo.text.isEmpty()) "Obligatorio" else null

        // --- 3. Validaciones de Peso y Estatura por Especie ---
        // Validamos Peso
        val pesoStr = binding.txtPeso.text.toString().trim()
        if (pesoStr.isEmpty()) {
            binding.layoutPeso.error = "Campo obligatorio"
        } else {
            val pesoValido = ValidationUtils.isValidPesoPorEspecie(pesoStr, especieActual)
            binding.layoutPeso.error = if (!pesoValido) {
                if (especieActual.isEmpty()) "Peso inválido" else "Peso inválido para $especieActual"
            } else null
        }

        // Validamos Estatura
        val estaturaStr = binding.txtEstatura.text.toString().trim()
        if (estaturaStr.isEmpty()) {
            binding.layoutEstatura.error = "Campo obligatorio"
        } else {
            val estaturaValida = ValidationUtils.isValidEstaturaPorEspecie(estaturaStr, especieActual)
            binding.layoutEstatura.error = if (!estaturaValida) {
                if (especieActual.isEmpty()) "Estatura inválida" else "Estatura inválida para $especieActual"
            } else null
        }

        // 4. Validar Coherencia de Fechas (Bidireccional)
        validarRelacionFechas()

        // 5. Validaciones dinámicas (Raza y Color "Otro")
        if (binding.layoutRazaOtro.visibility == View.VISIBLE) {
            val razaOtro = binding.txtRazaOtro.text.toString().trim()
            binding.layoutRazaOtro.error = when {
                razaOtro.isEmpty() -> "Especifique la raza"
                razaOtro.length < 3 -> "Mínimo 3 caracteres"
                else -> null
            }
        }

        if (binding.layoutOtroColor.visibility == View.VISIBLE) {
            val colorOtro = binding.txtOtroColor.text.toString().trim()
            binding.layoutOtroColor.error = when {
                colorOtro.isEmpty() -> "Especifique el color"
                colorOtro.length < 3 -> "Mínimo 3 caracteres"
                else -> null
            }
        }

        // 6. Recopilar todos los layouts para verificar si hay errores
        val camposAValidar = mutableListOf(
            binding.layoutRuac, binding.layoutNombreMascota, binding.layoutFechaNacimiento,
            binding.layoutFechaAdopcion, binding.layoutEspecie, binding.layoutRaza,
            binding.layoutColor, binding.layoutSexo, binding.layoutPeso, binding.layoutEstatura
        )

        // Agregamos los dinámicos si están visibles
        if (binding.layoutRazaOtro.visibility == View.VISIBLE) camposAValidar.add(binding.layoutRazaOtro)
        if (binding.layoutOtroColor.visibility == View.VISIBLE) camposAValidar.add(binding.layoutOtroColor)

        val tieneErrores = camposAValidar.any { it.error != null }

        // 7. Mostrar alertas finales
        if (tieneErrores) {
            UiUtils.mostrarAlerta(this, "Formulario incompleto", "Revisa los campos marcados en rojo.", SweetAlertDialog.ERROR_TYPE)
            return false
        }

        if (imagenEnBase64 == null) {
            UiUtils.mostrarAlerta(this, "Falta foto", "La foto es obligatoria para registrar a la mascota.", SweetAlertDialog.WARNING_TYPE)
            return false
        }

        return true
    }

    private fun registrarMascota() {
        val idDueno = FirebaseAuth.getInstance().uid ?: return
        val db = FirebaseFirestore.getInstance()

        val razaFinal = if (binding.txtRaza.text.toString() == "Otro") binding.txtRazaOtro.text.toString() else binding.txtRaza.text.toString()
        val colorFinal = if (binding.txtColor.text.toString() == "Otro") binding.txtOtroColor.text.toString() else binding.txtColor.text.toString()

        val mascota = Mascotas(
            ruac = EncryptionUtils.encrypt(binding.txtRuac.text.toString()),
            nombre = EncryptionUtils.encrypt(binding.txtNombreMascota.text.toString()),
            fechaNacimiento = EncryptionUtils.encrypt(binding.txtFechaNacimiento.text.toString()),
            fechaAdopcion = EncryptionUtils.encrypt(binding.txtFechaAdopcion.text.toString()),
            especie = EncryptionUtils.encrypt(binding.txtEspecie.text.toString()),
            raza = EncryptionUtils.encrypt(razaFinal),
            color = EncryptionUtils.encrypt(colorFinal),
            sexo = EncryptionUtils.encrypt(binding.txtSexo.text.toString()),
            peso = EncryptionUtils.encrypt(binding.txtPeso.text.toString()),
            estatura = EncryptionUtils.encrypt(binding.txtEstatura.text.toString()),
            alergias = EncryptionUtils.encrypt(binding.txtAlergias.text.toString()),
            notas = EncryptionUtils.encrypt(binding.txtNotas.text.toString()),
            //fotoBase64 = EncryptionUtils.encrypt(imagenEnBase64!!),
            fotoBase64 = imagenEnBase64!!,
            idDueno = idDueno
        )

        db.collection("usuarios").document(idDueno).collection("mascotas").add(mascota)
            .addOnSuccessListener {
                UiUtils.mostrarAlerta(this, "¡Éxito!", "Mascota registrada", SweetAlertDialog.SUCCESS_TYPE) { finish() }
            }
            .addOnFailureListener { e ->
                UiUtils.mostrarAlerta(this, "Error", "No se pudo registrar: ${e.message}", SweetAlertDialog.ERROR_TYPE)
            }
    }

    private fun abrirPaginaRuac() {
        val url = "https://www.ruac.cdmx.gob.mx/" // URL oficial del RUAC
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = android.net.Uri.parse(url)
        startActivity(intent)
    }
}