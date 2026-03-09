package com.example.xolotl.ui.main.mascota

import android.os.Bundle
import android.text.Editable
import android.widget.EditText
import android.text.TextWatcher
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.example.xolotl.databinding.ActivityAgregarMascotaBinding
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.xolotl.data.models.Mascotas
import com.example.xolotl.data.repository.MascotaRepository
import com.example.xolotl.utils.ValidationUtils
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.result.ActivityResultLauncher
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils


class AgregarMascotaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAgregarMascotaBinding
    private val repo = MascotaRepository()
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var imagenEnBase64: String? = null


    // Listas
    private val especies = listOf("Perro", "Gato")

    private val razasPerro = listOf(
        "Labrador", "Pug", "Chihuahua", "Pastor Alemán", "Pitbull", "Golden Retriever",
        "Husky", "Beagle", "Shih Tzu", "Otro"
    )

    private val razasGato = listOf(
        "Siames", "Persa", "Bombay", "Angora", "Azul Ruso", "Bengalí",
        "Maine Coon", "Siberiano", "Otro"
    )

    private val colores = listOf("Blanco", "Negro", "Café", "Gris", "Atigrado", "Naranja", "Otro")

    private val sexos = listOf("Macho", "Hembra")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAgregarMascotaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imagePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {

                    // Mostrar la foto en el ImageView
                    binding.imgFotoMascota.setImageURI(uri)

                    // Convertir a Base64
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream!!.readBytes()
                    imagenEnBase64 = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                }
            }
        }

        setupBotonGuardar()
        setupBotonHome()
        setupCalendario()
        setupDropdowns()


        setupFieldValidation(binding.txtRuac, ValidationUtils::isValidRuac, "RUAC inválido")
        setupFieldValidation(binding.txtNombreMascota, ValidationUtils::isValidPetName, "Nombre inválido")
        setupFieldValidation(binding.txtFechaAdopcion, ValidationUtils::isValidDate, "Formato: DD/MM/YYYY")
        setupFieldValidation(binding.txtEspecie, ValidationUtils::isValidPetSpecies, "Especie inválida")
        setupFieldValidation(binding.txtRaza, ValidationUtils::isValidPetRace, "Raza inválida")
        setupFieldValidation(binding.txtColor, ValidationUtils::isValidColor, "Color inválido")
        setupFieldValidation(binding.txtSexo, ValidationUtils::isValidPetSex, "Debe ser Macho o Hembra")
        setupFieldValidation(binding.txtPeso, ValidationUtils::isValidNumber, "Número inválido")
        setupFieldValidation(binding.txtEstatura, ValidationUtils::isValidHeight, "Número inválido")
        setupFieldValidation(binding.txtAlergias, ValidationUtils::isValidAlergia, "Texto inválido")
        setupFieldValidation(binding.txtNotas, ValidationUtils::isValidNotas, "Texto inválido")

        binding.btnFoto.setOnClickListener {
            abrirGaleria()
        }


    }

    private fun abrirGaleria() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }


    // ---------------- FECHA ---------------------
    private fun setupCalendario() {
        binding.txtFechaAdopcion.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dp = DatePickerDialog(this, { _, y, m, d ->
                val mes = m + 1
                val fecha = "%02d/%02d/%04d".format(d, mes, y)
                binding.txtFechaAdopcion.setText(fecha)
            }, year, month, day)

            dp.show()
        }
    }

    private fun setupDropdowns() {

        // ---------------- ESPECIE ----------------
        val adapterEspecie = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, especies)
        binding.txtEspecie.setAdapter(adapterEspecie)

        // Mostrar lista al tocar
        binding.txtEspecie.setOnClickListener {
            binding.txtEspecie.showDropDown()
        }

        binding.txtEspecie.setOnItemClickListener { _, _, position, _ ->
            val especieElegida = especies[position]

            val razaAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                if (especieElegida == "Perro") razasPerro else razasGato
            )

            binding.txtRaza.setAdapter(razaAdapter)

            binding.txtRazaOtro.visibility = android.view.View.GONE
            binding.txtIdentificarRaza.visibility = android.view.View.GONE
        }

        // ---------------- RAZA ----------------
        binding.txtRaza.setOnClickListener {
            binding.txtRaza.showDropDown()
        }

        binding.txtRaza.setOnItemClickListener { _, _, _, _ ->
            val raza = binding.txtRaza.text.toString()

            if (raza == "Otro") {
                binding.txtRazaOtro.visibility = android.view.View.VISIBLE
                binding.txtIdentificarRaza.visibility = android.view.View.VISIBLE

                binding.txtIdentificarRaza.setOnClickListener {
                    UiUtils.showToast(this, "Próximamente un identificador de razas")
                }
            } else {
                binding.txtRazaOtro.visibility = android.view.View.GONE
                binding.txtIdentificarRaza.visibility = android.view.View.GONE
            }
        }

        // ---------------- COLOR ----------------
        val adapterColor = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, colores)
        binding.txtColor.setAdapter(adapterColor)

        binding.txtColor.setOnClickListener {
            binding.txtColor.showDropDown()
        }

        // ---------------- SEXO ----------------
        val adapterSexo = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sexos)
        binding.txtSexo.setAdapter(adapterSexo)

        binding.txtSexo.setOnClickListener {
            binding.txtSexo.showDropDown()
        }
    }

    private fun setupFieldValidation(
        editText: EditText,
        validator: (String) -> Boolean,
        errorMessage: String
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                if (!validator(text)) {
                    editText.error = errorMessage
                    editText.setTextColor(ContextCompat.getColor(this@AgregarMascotaActivity, R.color.black))
                } else {
                    editText.error = null
                    editText.setTextColor(ContextCompat.getColor(this@AgregarMascotaActivity, R.color.black))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    private fun setupBotonGuardar() {
        binding.btnGuardarMascota.setOnClickListener {
            if (validarCamposObligatorios()) {
                registrarMascota()
            }
        }
    }

    private fun setupBotonHome() {
        binding.btnHome.setOnClickListener {
            finish()
        }
    }

    private fun registrarMascota() {
        val nombre = binding.txtNombreMascota.text.toString()
        val especie = binding.txtEspecie.text.toString()
        val raza = binding.txtRaza.text.toString()

        if (!ValidationUtils.validarMascota(nombre, especie, raza)) {
            UiUtils.showToast(this, "Completa los campos obligatorios")
            return
        }

        if (imagenEnBase64 == null) {
            UiUtils.showToast(this, "Debes seleccionar una foto de la mascota")
            return
        }

        val ruacM = repo.hashCode().toString() + System.currentTimeMillis()
        val idDueno = FirebaseAuth.getInstance().uid ?: return
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        // Crear objeto mascota cifrado
        val mascotaCifrada = Mascotas(
            ruac = EncryptionUtils.encrypt(ruacM),
            nombre = EncryptionUtils.encrypt(nombre),
            especie = EncryptionUtils.encrypt(especie),
            raza = EncryptionUtils.encrypt(raza),
            sexo = EncryptionUtils.encrypt(binding.txtSexo.text.toString()),
            fechaAdopcion = EncryptionUtils.encrypt(binding.txtFechaAdopcion.text.toString()),
            color = EncryptionUtils.encrypt(binding.txtColor.text.toString()),
            peso = EncryptionUtils.encrypt(binding.txtPeso.text.toString()),
            estatura = EncryptionUtils.encrypt(binding.txtEstatura.text.toString()),
            alergias = EncryptionUtils.encrypt(binding.txtAlergias.text.toString()),
            notas = EncryptionUtils.encrypt(binding.txtNotas.text.toString()),
            fotoBase64 = EncryptionUtils.encrypt(imagenEnBase64!!),
            idDueno = idDueno // UID del usuario, no es necesario cifrarlo
        )

        // Guardar en subcolección "mascotas" del usuario
        db.collection("usuarios")
            .document(idDueno)
            .collection("mascotas")
            .add(mascotaCifrada)
            .addOnSuccessListener {
                UiUtils.showToast(this, "Mascota registrada correctamente")
                finish()
            }
            .addOnFailureListener { e ->
                UiUtils.showToast(this, "Error: ${e.message}")
            }
    }

    private fun validarCamposObligatorios(): Boolean {

        val ruac = binding.txtRuac.text.toString().trim()
        val nombre = binding.txtNombreMascota.text.toString().trim()
        val fecha = binding.txtFechaAdopcion.text.toString().trim()
        val especie = binding.txtEspecie.text.toString().trim()
        val raza = binding.txtRaza.text.toString().trim()
        val color = binding.txtColor.text.toString().trim()
        val sexo = binding.txtSexo.text.toString().trim()
        val peso = binding.txtPeso.text.toString().trim()
        val estatura = binding.txtEstatura.text.toString().trim()
        val alergias = binding.txtAlergias.text.toString().trim()
        val notas = binding.txtNotas.text.toString().trim()

        val valido = ValidationUtils.validarMascotaCompleta(
            ruac, nombre, fecha, especie, raza, color, sexo, peso, estatura, alergias, notas
        )

        if (!valido) {
            marcarErrores() // <--- Marcar en rojo lo incorrecto
            Toast.makeText(this, "Por favor, corrige los campos en rojo", Toast.LENGTH_LONG).show()
        }

        return valido
    }

    private fun marcarErrores() {
        val fields = listOf(
            binding.txtRuac to ValidationUtils::isValidRuac,
            binding.txtNombreMascota to ValidationUtils::isValidPetName,
            binding.txtFechaAdopcion to ValidationUtils::isValidDate,
            binding.txtEspecie to ValidationUtils::isValidPetSpecies,
            binding.txtRaza to ValidationUtils::isValidPetRace,
            binding.txtColor to ValidationUtils::isValidColor,
            binding.txtSexo to ValidationUtils::isValidPetSex,
            binding.txtPeso to ValidationUtils::isValidNumber,
            binding.txtEstatura to ValidationUtils::isValidHeight,
            binding.txtAlergias to ValidationUtils::isValidAlergia,
            binding.txtNotas to ValidationUtils::isValidNotas
        )

        fields.forEach { (editText, validator) ->
            val text = editText.text.toString().trim()
            if (!validator(text)) {
                editText.error = "Dato inválido"
                editText.setTextColor(ContextCompat.getColor(this, R.color.black))
            }
        }
    }
}
