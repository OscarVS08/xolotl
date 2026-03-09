package com.example.xolotl.ui.main.usuario

import android.os.Bundle
import android.view.View
import android.widget.EditText
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

class EditarPerfilActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().uid

    // UI
    private lateinit var txtNombre: EditText
    private lateinit var txtApellidoP: EditText
    private lateinit var txtApellidoM: EditText
    private lateinit var txtTelefono: EditText
    private lateinit var txtTelefonoAlt: EditText
    private lateinit var txtCalle: EditText
    private lateinit var txtNumero: EditText
    private lateinit var txtColonia: EditText
    private lateinit var txtAlcaldia: EditText
    private lateinit var txtCodigoPostal: EditText
    private lateinit var txtContrasena: EditText
    private lateinit var txtConfirmarContrasena: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil_usuario)

        initViews()
        cargarDatos()
        setupValidaciones()

        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }

        findViewById<View>(R.id.btnGuardarCambios).setOnClickListener {
            guardarCambios()
        }
    }

    private fun initViews() {
        txtNombre = findViewById(R.id.txtNombre)
        txtApellidoP = findViewById(R.id.txtApellidoP)
        txtApellidoM = findViewById(R.id.txtApellidoM)
        txtTelefono = findViewById(R.id.txtTelefono)
        txtTelefonoAlt = findViewById(R.id.txtTelefonoAlt)
        txtCalle = findViewById(R.id.txtCalle)
        txtNumero = findViewById(R.id.txtNumero)
        txtColonia = findViewById(R.id.txtColonia)
        txtAlcaldia = findViewById(R.id.txtAlcaldia)
        txtCodigoPostal = findViewById(R.id.txtCodigoPostal)
        txtContrasena = findViewById(R.id.txtContrasena)
        txtConfirmarContrasena = findViewById(R.id.txtConfirmarContrasena)
    }

    // ---------------- CARGAR DATOS ----------------
    private fun cargarDatos() {
        if (userId == null) return

        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    txtNombre.setText(EncryptionUtils.decrypt(doc.getString("nombre") ?: ""))
                    txtApellidoP.setText(EncryptionUtils.decrypt(doc.getString("apellidoP") ?: ""))
                    txtApellidoM.setText(EncryptionUtils.decrypt(doc.getString("apellidoM") ?: ""))
                    txtTelefono.setText(EncryptionUtils.decrypt(doc.getString("telefono") ?: ""))
                    txtTelefonoAlt.setText(EncryptionUtils.decrypt(doc.getString("telefonoAlt") ?: ""))
                    txtCalle.setText(EncryptionUtils.decrypt(doc.getString("calle") ?: ""))
                    txtNumero.setText(EncryptionUtils.decrypt(doc.getString("numero") ?: ""))
                    txtColonia.setText(EncryptionUtils.decrypt(doc.getString("colonia") ?: ""))
                    txtAlcaldia.setText(EncryptionUtils.decrypt(doc.getString("alcaldia") ?: ""))
                    txtCodigoPostal.setText(EncryptionUtils.decrypt(doc.getString("codigoPostal") ?: ""))
                }
            }
            .addOnFailureListener {
                UiUtils.mostrarAlerta(
                    this,
                    "Error",
                    "No se pudieron cargar tus datos",
                    SweetAlertDialog.ERROR_TYPE
                )
            }
    }

    // ---------------- VALIDACIONES ----------------
    private fun setupValidaciones() {

        setupFieldValidation(txtNombre, ValidationUtils::isValidName, "Nombre inválido")
        setupFieldValidation(txtApellidoP, ValidationUtils::isValidName, "Apellido inválido")
        setupFieldValidation(txtApellidoM, ValidationUtils::isValidName, "Apellido inválido")

        setupFieldValidation(txtTelefono, ValidationUtils::isValidPhone, "Debe tener 10 dígitos")
        setupFieldValidation(txtTelefonoAlt, ValidationUtils::isValidPhone, "Debe tener 10 dígitos")

        setupFieldValidation(txtCalle, ValidationUtils::isValidAddressField, "Texto inválido")
        setupFieldValidation(txtNumero, ValidationUtils::isValidNumber, "Número inválido")
        setupFieldValidation(txtColonia, ValidationUtils::isValidAddressField, "Texto inválido")
        setupFieldValidation(txtAlcaldia, ValidationUtils::isValidAddressField, "Texto inválido")

        setupFieldValidation(txtCodigoPostal, ValidationUtils::isValidPostalCode, "Debe tener 5 dígitos")

        // 🔥 LÍMITES
        limitarLongitud(txtTelefono, 10, "El teléfono no puede exceder 10 dígitos")
        limitarLongitud(txtTelefonoAlt, 10, "El teléfono alterno no puede exceder 10 dígitos")
        limitarLongitud(txtCodigoPostal, 5, "El CP no puede exceder 5 dígitos")

        // 🔥 CONTRASEÑA
        txtConfirmarContrasena.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (txtContrasena.text.toString() != txtConfirmarContrasena.text.toString()) {
                    txtConfirmarContrasena.error = "No coinciden"
                } else {
                    txtConfirmarContrasena.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupFieldValidation(
        editText: EditText,
        validator: (String) -> Boolean,
        errorMessage: String
    ) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                if (text.isNotEmpty() && !validator(text)) {
                    editText.error = errorMessage
                } else {
                    editText.error = null
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun limitarLongitud(editText: EditText, max: Int, mensaje: String) {
        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if ((s?.length ?: 0) > max) {
                    editText.error = mensaje
                    UiUtils.showToast(this@EditarPerfilActivity, mensaje)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // ---------------- VALIDAR ANTES DE GUARDAR ----------------
    private fun validarCampos(): Boolean {

        val nombre = txtNombre.text.toString().trim()
        val apellidoP = txtApellidoP.text.toString().trim()
        val telefono = txtTelefono.text.toString().trim()

        if (!ValidationUtils.isNotEmpty(nombre, apellidoP, telefono)) {
            marcarErrores()
            return false
        }

        if (!ValidationUtils.isValidPhone(telefono)) {
            txtTelefono.error = "Teléfono inválido"
            return false
        }

        val cp = txtCodigoPostal.text.toString().trim()
        if (cp.isNotEmpty() && !ValidationUtils.isValidPostalCode(cp)) {
            txtCodigoPostal.error = "CP inválido"
            return false
        }

        val pass = txtContrasena.text.toString()
        val confirm = txtConfirmarContrasena.text.toString()

        if (pass.isNotEmpty() && pass != confirm) {
            txtConfirmarContrasena.error = "No coinciden"
            return false
        }

        return true
    }

    private fun marcarErrores() {
        val fields = listOf(
            txtNombre to ValidationUtils::isValidName,
            txtApellidoP to ValidationUtils::isValidName,
            txtTelefono to ValidationUtils::isValidPhone
        )

        fields.forEach { (editText, validator) ->
            val text = editText.text.toString().trim()
            if (!validator(text)) {
                editText.error = "Campo obligatorio"
            }
        }
    }

    // ---------------- GUARDAR ----------------
    private fun guardarCambios() {
        if (userId == null) return

        if (!validarCampos()) {
            UiUtils.mostrarAlerta(
                this,
                "Error",
                "Corrige los campos marcados",
                SweetAlertDialog.ERROR_TYPE
            )
            return
        }

        val datos = hashMapOf(
            "nombre" to EncryptionUtils.encrypt(txtNombre.text.toString()),
            "apellidoP" to EncryptionUtils.encrypt(txtApellidoP.text.toString()),
            "apellidoM" to EncryptionUtils.encrypt(txtApellidoM.text.toString()),
            "telefono" to EncryptionUtils.encrypt(txtTelefono.text.toString()),
            "telefonoAlt" to EncryptionUtils.encrypt(txtTelefonoAlt.text.toString()),
            "calle" to EncryptionUtils.encrypt(txtCalle.text.toString()),
            "numero" to EncryptionUtils.encrypt(txtNumero.text.toString()),
            "colonia" to EncryptionUtils.encrypt(txtColonia.text.toString()),
            "alcaldia" to EncryptionUtils.encrypt(txtAlcaldia.text.toString()),
            "codigoPostal" to EncryptionUtils.encrypt(txtCodigoPostal.text.toString())
        )

        val pass = txtContrasena.text.toString()
        if (pass.isNotEmpty()) {
            datos["password"] = EncryptionUtils.encrypt(pass)
        }

        db.collection("usuarios")
            .document(userId)
            .update(datos as Map<String, Any>)
            .addOnSuccessListener {
                UiUtils.mostrarAlerta(
                    this,
                    "Perfil actualizado",
                    "Tus datos se guardaron correctamente",
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
}