package com.example.xolotl.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.MainActivity
import com.example.xolotl.data.models.User
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.databinding.ActivityRegistrarseBinding
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.example.xolotl.utils.updateTextColor
import com.google.firebase.auth.FirebaseAuth

class RegistrarseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarseBinding
    private val authRepo = AuthRepository()
    private var mostrarPassword = false
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupValidations()
        setupPasswordToggle()

        // --- Botón crear cuenta ---
        binding.btnCrearCuenta.setOnClickListener { registrarUsuario() }

        // --- Olvidaste CURP ---
        binding.txtOlvidasteCurp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gob.mx/curp/"))
            startActivity(intent)
        }
    }

    private fun setupValidations() {
        // --- CURP ---
        binding.txtCurp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val curp = s.toString().trim()
                when {
                    curp.isEmpty() -> {
                        binding.txtCurp.error = "Campo obligatorio"
                        binding.txtCurp.updateTextColor(false)
                    }

                    curp.length < 18 -> {
                        binding.txtCurp.error = "Faltan caracteres"
                        binding.txtCurp.updateTextColor(false)
                    }

                    !ValidationUtils.isValidCURP(curp) -> {
                        binding.txtCurp.error = "Formato inválido"
                        binding.txtCurp.updateTextColor(false)
                    }

                    else -> {
                        binding.txtCurp.error = null
                        binding.txtCurp.updateTextColor(true)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- NOMBRE ---
        binding.txtNombre.addTextChangedListener(simpleValidator(binding.txtNombre) {
            ValidationUtils.isValidName(it)
        })

        // --- APELLIDOS ---
        val apellidoWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                listOf(binding.txtApellidoP, binding.txtApellidoM).forEach { field ->
                    val text = field.text.toString().trim()
                    if (!ValidationUtils.isValidName(text)) {
                        field.error = "Solo letras y máx. 50 caracteres"
                        field.updateTextColor(false)
                    } else {
                        field.error = null
                        field.updateTextColor(true)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.txtApellidoP.addTextChangedListener(apellidoWatcher)
        binding.txtApellidoM.addTextChangedListener(apellidoWatcher)

        // --- TELÉFONOS ---
        binding.txtTelefono.addTextChangedListener(simpleValidator(binding.txtTelefono) {
            ValidationUtils.isValidPhone(it)
        })
        binding.txtTelefonoAlt.addTextChangedListener(simpleValidator(binding.txtTelefonoAlt) {
            it.isEmpty() || ValidationUtils.isValidPhone(it)
        })

        // --- DIRECCIÓN ---
        listOf(
            binding.txtCalle,
            binding.txtNumero,
            binding.txtColonia,
            binding.txtAlcaldia
        ).forEach { field ->
            field.addTextChangedListener(simpleValidator(field) {
                ValidationUtils.isValidAddressField(it)
            })
        }

        // --- CÓDIGO POSTAL ---
        binding.txtCodigoPostal.addTextChangedListener(simpleValidator(binding.txtCodigoPostal) {
            ValidationUtils.isValidPostalCode(it)
        })

        // --- CORREO ---
        binding.txtCorreo.addTextChangedListener(simpleValidator(binding.txtCorreo) {
            ValidationUtils.isValidEmail(it)
        })

        // --- CONTRASEÑA ---
        binding.txtContrasena.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                val mensaje = when {
                    pass.length < 8 -> "Muy débil"
                    ValidationUtils.isStrongPassword(pass) -> "Fuerte"
                    else -> "Media"
                }
                binding.txtContrasena.error = mensaje
                binding.txtContrasena.updateTextColor(ValidationUtils.isStrongPassword(pass))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- CONFIRMAR CONTRASEÑA ---
        binding.txtConfirmarContrasena.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val confirm = s.toString()
                val pass = binding.txtContrasena.text.toString()
                if (confirm != pass) {
                    binding.txtConfirmarContrasena.error = "No coincide"
                    binding.txtConfirmarContrasena.updateTextColor(false)
                } else {
                    binding.txtConfirmarContrasena.error = null
                    binding.txtConfirmarContrasena.updateTextColor(true)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnToggleContrasena.setOnClickListener {
            mostrarPassword = !mostrarPassword
            val type = if (mostrarPassword)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.txtContrasena.inputType = type
            binding.txtContrasena.setSelection(binding.txtContrasena.text?.length ?: 0)
        }

        binding.btnToggleConfirmarContrasena.setOnClickListener {
            mostrarPassword = !mostrarPassword
            val type = if (mostrarPassword)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.txtConfirmarContrasena.inputType = type
            binding.txtConfirmarContrasena.setSelection(binding.txtConfirmarContrasena.text?.length ?: 0)
        }
    }

    // --- Botón para mostrar/ocultar contraseña ---
    private fun setupPasswordToggle() {
        binding.btnToggleContrasena.setOnClickListener {
            mostrarPassword = !mostrarPassword
            val type = if (mostrarPassword)
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.txtContrasena.inputType = type
            binding.txtConfirmarContrasena.inputType = type
            binding.txtContrasena.setSelection(binding.txtContrasena.text?.length ?: 0)
            binding.txtConfirmarContrasena.setSelection(binding.txtConfirmarContrasena.text?.length ?: 0)
        }
    }

    // --- Validar campos vacíos y correo existente antes de registrar ---
    private fun validarYCreaUsuario() {
        val campos = listOf(
            binding.txtCurp, binding.txtNombre, binding.txtApellidoP,
            binding.txtApellidoM, binding.txtTelefono, binding.txtCalle,
            binding.txtNumero, binding.txtColonia, binding.txtAlcaldia,
            binding.txtCodigoPostal, binding.txtCorreo, binding.txtContrasena,
            binding.txtConfirmarContrasena
        )

        var todoValido = true
        campos.forEach { field ->
            if (field.text.toString().trim().isEmpty()) {
                field.error = "Campo obligatorio"
                field.updateTextColor(false)
                todoValido = false
            }
        }

        if (!todoValido) {
            UiUtils.showToast(this, "Por favor llena todos los campos")
            return
        }

        val correo = binding.txtCorreo.text.toString().trim()

        // --- Verificar si el correo ya existe con Firebase ---
        firebaseAuth.fetchSignInMethodsForEmail(correo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList<String>()
                    if (signInMethods.isNotEmpty()) {
                        UiUtils.mostrarAlerta(
                            this,
                            "Correo existente",
                            "Este correo ya está registrado",
                            SweetAlertDialog.WARNING_TYPE
                        )
                    } else {
                        registrarUsuario()
                    }
                } else {
                    UiUtils.showToast(this, "Error al validar el correo")
                }
            }
    }

    private fun registrarUsuario() {
        val curp = binding.txtCurp.text.toString().trim()
        val nombre = binding.txtNombre.text.toString().trim()
        val apellidoP = binding.txtApellidoP.text.toString().trim()
        val apellidoM = binding.txtApellidoM.text.toString().trim()
        val telefono = binding.txtTelefono.text.toString().trim()
        val telefonoAlt = binding.txtTelefonoAlt.text.toString().trim()
        val calle = binding.txtCalle.text.toString().trim()
        val numero = binding.txtNumero.text.toString().trim()
        val colonia = binding.txtColonia.text.toString().trim()
        val alcaldia = binding.txtAlcaldia.text.toString().trim()
        val codigoPostal = binding.txtCodigoPostal.text.toString().trim()
        val correo = binding.txtCorreo.text.toString().trim()
        val contrasena = binding.txtContrasena.text.toString().trim()
        val confirmarContrasena = binding.txtConfirmarContrasena.text.toString().trim()

        // --- VALIDACIONES FINALES ---
        if (!ValidationUtils.isNotEmpty(
                curp,
                nombre,
                apellidoP,
                telefono,
                calle,
                numero,
                colonia,
                alcaldia,
                codigoPostal,
                correo,
                contrasena,
                confirmarContrasena
            )
        ) {
            UiUtils.showToast(this, "Por favor llena todos los campos obligatorios")
            return
        }

        if (!ValidationUtils.isValidCURP(curp)) {
            UiUtils.showToast(this, "CURP no válida")
            return
        }

        if (!ValidationUtils.isValidName(nombre) ||
            !ValidationUtils.isValidName(apellidoP) ||
            !ValidationUtils.isValidName(apellidoM)
        ) {
            UiUtils.showToast(this, "Nombre o apellidos inválidos")
            return
        }

        if (!ValidationUtils.isValidPhone(telefono) ||
            (telefonoAlt.isNotEmpty() && !ValidationUtils.isValidPhone(telefonoAlt))
        ) {
            UiUtils.showToast(this, "Teléfonos inválidos")
            return
        }

        if (!ValidationUtils.isValidAddressField(calle) ||
            !ValidationUtils.isValidAddressField(numero) ||
            !ValidationUtils.isValidAddressField(colonia) ||
            !ValidationUtils.isValidAddressField(alcaldia)
        ) {
            UiUtils.showToast(this, "Campos de dirección inválidos")
            return
        }

        if (!ValidationUtils.isValidPostalCode(codigoPostal)) {
            UiUtils.showToast(this, "Código postal inválido")
            return
        }

        if (!ValidationUtils.isValidEmail(correo)) {
            UiUtils.showToast(this, "Correo no válido")
            return
        }

        if (!ValidationUtils.isStrongPassword(contrasena)) {
            UiUtils.showToast(this, "Contraseña débil")
            return
        }

        if (contrasena != confirmarContrasena) {
            UiUtils.showToast(this, "Las contraseñas no coinciden")
            return
        }

        val usuario = User(
            uid = "",
            curp = curp,
            nombre = nombre,
            apellidoP = apellidoP,
            apellidoM = apellidoM,
            telefono = telefono,
            telefonoAlt = telefonoAlt,
            calle = calle,
            numero = numero,
            colonia = colonia,
            alcaldia = alcaldia,
            codigoPostal = codigoPostal,
            correo = correo
        )

        authRepo.registrarUsuario(correo, contrasena, usuario, object : AuthCallback {
            override fun onSuccess() {
                // Usuario creado, enviar correo de verificación
                firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        UiUtils.mostrarAlerta(
                            this@RegistrarseActivity,
                            "Verifica tu correo",
                            "Se ha enviado un correo de verificación a $correo. Por favor verifica antes de iniciar sesión.",
                            SweetAlertDialog.WARNING_TYPE
                        ) {
                            // Cerrar sesión para obligar a verificar
                            firebaseAuth.signOut()
                            startActivity(
                                Intent(
                                    this@RegistrarseActivity,
                                    InicioActivity::class.java
                                )
                            )
                            finish()
                        }
                    } else {
                        UiUtils.showToast(this@RegistrarseActivity, "Error al enviar correo de verificación")
                    }
                }
            }

            override fun onError(errorMessage: String) {
                UiUtils.mostrarAlerta(
                    this@RegistrarseActivity,
                    "Error",
                    "No se pudo crear la cuenta: $errorMessage",
                    SweetAlertDialog.ERROR_TYPE
                )
            }
        })
    }

    private fun simpleValidator(field: EditText, rule: (String) -> Boolean) =
        object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString().trim()
                if (!rule(text)) {
                    field.error = "Campo inválido"
                    field.updateTextColor(false)
                } else {
                    field.error = null
                    field.updateTextColor(true)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
}
