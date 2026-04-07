package com.example.xolotl.ui.main.mascota

import android.app.DatePickerDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.*
import android.text.TextWatcher
import android.text.Editable
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import cn.pedant.SweetAlert.SweetAlertDialog
import java.util.*

class EditarMascotasActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var mascotaId: String
    private lateinit var userId: String

    // UI Mascota
    private lateinit var txtNombre: TextView
    private lateinit var txtRuac: TextView
    private lateinit var txtEspecie: AutoCompleteTextView
    private lateinit var txtRaza: AutoCompleteTextView
    private lateinit var txtSexo: AutoCompleteTextView
    private lateinit var txtFecha: EditText
    private lateinit var txtColor: AutoCompleteTextView
    private lateinit var txtPeso: EditText
    private lateinit var txtEstatura: EditText
    private lateinit var txtAlergias: EditText
    private lateinit var txtNotas: EditText
    private lateinit var imgFotoMascota: ImageView


    // UI Dueño
    private lateinit var txtDueno: TextView
    private lateinit var txtTelefono: TextView
    private lateinit var txtTelAlt: TextView

    private val especies = listOf("Perro", "Gato")

    private val razasPerro = listOf(
        "Labrador", "Pug", "Chihuahua", "Pastor Alemán", "Pitbull",
        "Golden Retriever", "Husky", "Beagle", "Shih Tzu", "Otro"
    )

    private val razasGato = listOf(
        "Siames", "Persa", "Bombay", "Angora", "Azul Ruso",
        "Bengalí", "Maine Coon", "Siberiano", "Otro"
    )

    private val colores = listOf("Blanco", "Negro", "Café", "Gris", "Atigrado", "Naranja", "Otro")

    private val sexos = listOf("Macho", "Hembra")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_mascotas)

        mascotaId = intent.getStringExtra("docId") ?: return
        userId = FirebaseAuth.getInstance().uid ?: return

        initViews()
        cargarDatos()
        cargarDueno()
        setupValidaciones()
        setupFecha()
        setupDropdowns()

        // Botón Home
        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }

        // Guardar cambios
        findViewById<View>(R.id.btnAceptarMorado).setOnClickListener {
            guardarCambios()
        }
    }

    private fun initViews() {
        txtNombre = findViewById(R.id.txtNombreMascotaTop)
        txtRuac = findViewById(R.id.txtRuacTop)
        txtEspecie = findViewById(R.id.txtEspecie)
        txtRaza = findViewById(R.id.txtRaza)
        txtSexo = findViewById(R.id.txtSexo)
        txtFecha = findViewById(R.id.txtFechaAdopcion)
        txtColor = findViewById(R.id.txtColor)
        txtPeso = findViewById(R.id.txtPeso)
        txtEstatura = findViewById(R.id.txtEstatura)
        txtAlergias = findViewById(R.id.txtAlergias)
        txtNotas = findViewById(R.id.txtNotas)
        imgFotoMascota = findViewById(R.id.imgFotoMascotaTop)


        txtDueno = findViewById(R.id.txtnombreDueno)
        txtTelefono = findViewById(R.id.txtnumeroTelefonoDueno)
        txtTelAlt = findViewById(R.id.txtnumTelAltDuen)
    }

    // ---------------- CARGAR DATOS MASCOTA ----------------
    private fun cargarDatos() {
        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .document(mascotaId)
            .get()
            .addOnSuccessListener { doc ->

                if (doc.exists()) {

                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    val ruac = EncryptionUtils.decrypt(doc.getString("ruac") ?: "")
                    val especie = EncryptionUtils.decrypt(doc.getString("especie") ?: "")
                    val raza = EncryptionUtils.decrypt(doc.getString("raza") ?: "")
                    val sexo = EncryptionUtils.decrypt(doc.getString("sexo") ?: "")
                    val fecha = EncryptionUtils.decrypt(doc.getString("fechaAdopcion") ?: "")
                    val color = EncryptionUtils.decrypt(doc.getString("color") ?: "")
                    val peso = EncryptionUtils.decrypt(doc.getString("peso") ?: "")
                    val estatura = EncryptionUtils.decrypt(doc.getString("estatura") ?: "")
                    val alergias = EncryptionUtils.decrypt(doc.getString("alergias") ?: "")
                    val notas = EncryptionUtils.decrypt(doc.getString("notas") ?: "")

                    // ---------------- SET TEXTOS ----------------
                    txtNombre.setText(nombre)
                    txtRuac.text = ruac

                    txtEspecie.setText(especie, false)
                    txtSexo.setText(sexo, false)
                    txtColor.setText(color, false)

                    txtFecha.setText(fecha)
                    txtPeso.setText(peso)
                    txtEstatura.setText(estatura)
                    txtAlergias.setText(alergias)
                    txtNotas.setText(notas)

                    // ---------------- RAZA DINÁMICA ----------------
                    val razaAdapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        if (especie == "Perro") razasPerro else razasGato
                    )

                    txtRaza.setAdapter(razaAdapter)
                    txtRaza.setText(raza, false)

                    // ---------------- FOTO ----------------
                    val fotoCifrada = doc.getString("fotoBase64") ?: ""

                    if (fotoCifrada.isNotEmpty()) {
                        try {
                            val fotoBase64 = EncryptionUtils.decrypt(fotoCifrada)
                            val bytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imgFotoMascota.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            imgFotoMascota.setImageResource(R.drawable.fondo_logo_circular)
                        }
                    } else {
                        imgFotoMascota.setImageResource(R.drawable.fondo_logo_circular)
                    }
                }
            }
            .addOnFailureListener {
                UiUtils.mostrarAlerta(
                    this,
                    "Error",
                    "No se pudieron cargar los datos",
                    SweetAlertDialog.ERROR_TYPE
                )
            }
    }

    // ---------------- CARGAR DUEÑO ----------------
    private fun cargarDueno() {
        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    val apP = EncryptionUtils.decrypt(doc.getString("apellidoP") ?: "")
                    val apM = EncryptionUtils.decrypt(doc.getString("apellidoM") ?: "")

                    txtDueno.text = "$nombre $apP $apM"
                    txtTelefono.text = EncryptionUtils.decrypt(doc.getString("telefono") ?: "")
                    txtTelAlt.text = EncryptionUtils.decrypt(doc.getString("telefonoAlt") ?: "")
                }
            }
    }

    // ---------------- FECHA ----------------
    private fun setupFecha() {

        txtFecha.isFocusable = false
        txtFecha.isClickable = true

        txtFecha.setOnClickListener {
            val c = Calendar.getInstance()

            val dp = DatePickerDialog(this,
                { _, y, m, d ->
                    val fecha = "%02d/%02d/%04d".format(d, m + 1, y)
                    txtFecha.setText(fecha)
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            )
            dp.show()
        }
    }

    // ---------------- VALIDACIONES ----------------
    private fun setupValidaciones() {
        setup(txtEspecie, ValidationUtils::isValidPetSpecies, "Solo Perro o Gato")
        setup(txtRaza, ValidationUtils::isValidPetRace, "Raza inválida")
        setup(txtSexo, ValidationUtils::isValidPetSex, "Macho o Hembra")
        setup(txtFecha, ValidationUtils::isValidDate, "Formato DD/MM/YYYY")
        setup(txtColor, ValidationUtils::isValidColor, "Color inválido")
        setup(txtPeso, ValidationUtils::isValidNumber, "Peso inválido")
        setup(txtEstatura, ValidationUtils::isValidHeight, "Estatura inválida")
        setup(txtAlergias, ValidationUtils::isValidAlergia, "Texto inválido")
        setup(txtNotas, ValidationUtils::isValidNotas, "Texto inválido")
    }

    private fun setup(editText: EditText, validator: (String) -> Boolean, error: String) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()
                if (text.isNotEmpty() && !validator(text)) {
                    editText.error = error
                } else {
                    editText.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ---------------- VALIDAR ----------------
    private fun validar(): Boolean {
        var valido = true

        fun validarCampo(editText: EditText, condicion: Boolean, error: String) {
            if (!condicion) {
                editText.error = error
                valido = false
            } else {
                editText.error = null
            }
        }

        validarCampo(txtEspecie, ValidationUtils.isValidPetSpecies(txtEspecie.text.toString()), "Solo Perro o Gato")
        validarCampo(txtRaza, ValidationUtils.isValidPetRace(txtRaza.text.toString()), "Raza inválida")
        validarCampo(txtSexo, ValidationUtils.isValidPetSex(txtSexo.text.toString()), "Macho o Hembra")
        validarCampo(txtFecha, ValidationUtils.isValidDate(txtFecha.text.toString()), "Fecha inválida")
        validarCampo(txtColor, ValidationUtils.isValidColor(txtColor.text.toString()), "Color inválido")
        validarCampo(txtPeso, ValidationUtils.isValidNumber(txtPeso.text.toString()), "Peso inválido")
        validarCampo(txtEstatura, ValidationUtils.isValidHeight(txtEstatura.text.toString()), "Estatura inválida")

        if (!valido) {
            UiUtils.mostrarAlerta(
                this,
                "Error",
                "Corrige los campos marcados",
                SweetAlertDialog.ERROR_TYPE
            )
        }

        return valido
    }

    // ---------------- GUARDAR ----------------
    private fun guardarCambios() {

        if (!validar()) return

        val datos = hashMapOf(
            "ruac" to EncryptionUtils.encrypt(txtRuac.text.toString()),
            "nombre" to EncryptionUtils.encrypt(txtNombre.text.toString()),
            "fechaAdopcion" to EncryptionUtils.encrypt(txtFecha.text.toString()),
            "especie" to EncryptionUtils.encrypt(txtEspecie.text.toString()),
            "raza" to EncryptionUtils.encrypt(txtRaza.text.toString()),
            "color" to EncryptionUtils.encrypt(txtColor.text.toString()),
            "sexo" to EncryptionUtils.encrypt(txtSexo.text.toString()),
            "peso" to EncryptionUtils.encrypt(txtPeso.text.toString()),
            "estatura" to EncryptionUtils.encrypt(txtEstatura.text.toString()),
            "alergias" to EncryptionUtils.encrypt(txtAlergias.text.toString()),
            "notas" to EncryptionUtils.encrypt(txtNotas.text.toString())
        )

        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .document(mascotaId)
            .update(datos as Map<String, Any>)
            .addOnSuccessListener {
                UiUtils.mostrarAlerta(
                    this,
                    "Mascota actualizada",
                    "Cambios guardados correctamente",
                    SweetAlertDialog.SUCCESS_TYPE
                ) {
                    finish()
                }
            }
            .addOnFailureListener {
                UiUtils.mostrarAlerta(
                    this,
                    "Error",
                    "No se pudieron guardar los cambios",
                    SweetAlertDialog.ERROR_TYPE
                )
            }
    }

    private fun setupDropdowns() {

        // ---------------- ESPECIE ----------------
        val adapterEspecie = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, especies)
        txtEspecie.setAdapter(adapterEspecie)

        txtEspecie.setOnClickListener { txtEspecie.showDropDown() }

        txtEspecie.setOnItemClickListener { _, _, position, _ ->

            val especieElegida = especies[position]

            val razaAdapter = ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                if (especieElegida == "Perro") razasPerro else razasGato
            )

            txtRaza.setAdapter(razaAdapter)
            txtRaza.setText("") // limpiar cuando cambia especie
        }

        // ---------------- RAZA ----------------
        txtRaza.setOnClickListener { txtRaza.showDropDown() }

        txtRaza.setOnItemClickListener { _, _, _, _ ->
            val raza = txtRaza.text.toString()

            if (raza == "Otro") {
                UiUtils.showToast(this, "Próximamente identificador de razas")
            }
        }

        // ---------------- COLOR ----------------
        val adapterColor = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, colores)
        txtColor.setAdapter(adapterColor)
        txtColor.setOnClickListener { txtColor.showDropDown() }

        // ---------------- SEXO ----------------
        val adapterSexo = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, sexos)
        txtSexo.setAdapter(adapterSexo)
        txtSexo.setOnClickListener { txtSexo.showDropDown() }
    }
}