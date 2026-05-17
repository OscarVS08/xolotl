package com.example.xolotl.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.data.models.User
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.databinding.ActivityRegistrarseBinding
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth

// 1. LA CLASE AHORA IMPLEMENTA AuthCallback
class RegistrarseActivity : AppCompatActivity(), AuthCallback {

    private lateinit var binding: ActivityRegistrarseBinding
    private val authRepo = AuthRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var mostrarPassword = false
    private var mostrarConfirmPassword = false

    // Variable global para recordar el correo durante los Callbacks
    private var correoActual = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPasswordToggle()
        configurarValidacionesTiempoReal()

        // Listener para el link de términos
        binding.txtTerminosLink.setOnClickListener {
            val intent = Intent(this, TerminosActivity::class.java)
            startActivity(intent)
        }

        // --- Botón crear cuenta ---
        binding.btnCrearCuenta.setOnClickListener {
            validarFormularioCompleto()
        }

        // --- Olvidaste CURP ---
        binding.txtOlvidasteCurp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.gob.mx/curp/"))
            startActivity(intent)
        }
    }

    private fun setupPasswordToggle() {
        // --- Ojo para Contraseña Principal ---
        binding.btnToggleContrasena.setOnClickListener {
            mostrarPassword = !mostrarPassword

            if (mostrarPassword) {
                binding.txtContrasena.transformationMethod = android.text.method.HideReturnsTransformationMethod.getInstance()
                binding.btnToggleContrasena.setImageResource(R.drawable.ic_eye_open)
            } else {
                binding.txtContrasena.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                binding.btnToggleContrasena.setImageResource(R.drawable.ic_eye_closed)
            }
            binding.txtContrasena.setSelection(binding.txtContrasena.text?.length ?: 0)
        }

        // --- Ojo para Confirmar Contraseña ---
        binding.btnToggleConfirmarContrasena.setOnClickListener {
            mostrarConfirmPassword = !mostrarConfirmPassword

            if (mostrarConfirmPassword) {
                binding.txtConfirmarContrasena.transformationMethod = android.text.method.HideReturnsTransformationMethod.getInstance()
                binding.btnToggleConfirmarContrasena.setImageResource(R.drawable.ic_eye_open)
            } else {
                binding.txtConfirmarContrasena.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                binding.btnToggleConfirmarContrasena.setImageResource(R.drawable.ic_eye_closed)
            }
            binding.txtConfirmarContrasena.setSelection(binding.txtConfirmarContrasena.text?.length ?: 0)
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

        // Guardamos el correo en la variable global para onSuccess
        correoActual = binding.txtCorreo.text.toString().trim()

        val contrasena = binding.txtContrasena.text.toString().trim()
        val confirmarContrasena = binding.txtConfirmarContrasena.text.toString().trim()

        // --- VALIDACIONES FINALES ---
        if (!ValidationUtils.isNotEmpty(
                curp, nombre, apellidoP, telefono, calle, numero,
                colonia, alcaldia, codigoPostal, correoActual, contrasena, confirmarContrasena
            )
        ) {
            UiUtils.showToast(this, "Por favor llena todos los campos obligatorios")
            return
        }

        if (!ValidationUtils.isValidCURP(curp)) {
            UiUtils.showToast(this, "CURP no válida")
            return
        }

        if (!ValidationUtils.isValidName(nombre) || !ValidationUtils.isValidName(apellidoP) || !ValidationUtils.isValidName(apellidoM)) {
            UiUtils.showToast(this, "Nombre o apellidos inválidos")
            return
        }

        if (!ValidationUtils.isValidPhone(telefono) || (telefonoAlt.isNotEmpty() && !ValidationUtils.isValidPhone(telefonoAlt))) {
            UiUtils.showToast(this, "Teléfonos inválidos")
            return
        }

        if (!ValidationUtils.isValidAddressField(calle) || !ValidationUtils.isValidAddressField(numero) ||
            !ValidationUtils.isValidAddressField(colonia) || !ValidationUtils.isValidAddressField(alcaldia)
        ) {
            UiUtils.showToast(this, "Campos de dirección inválidos")
            return
        }

        if (!ValidationUtils.isValidPostalCode(codigoPostal)) {
            UiUtils.showToast(this, "Código postal inválido")
            return
        }

        if (!ValidationUtils.isValidEmail(correoActual)) {
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
            correo = correoActual
        )

        // 2. ADIÓS AL OBJETO ANÓNIMO: Pasamos 'this' como el Callback
        authRepo.registrarUsuario(correoActual, contrasena, usuario, this)
    }

    // --- 3. MÉTODOS DE LA INTERFAZ (Ahora pertenecen a la Actividad) ---
    override fun onSuccess() {
        firebaseAuth.currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                UiUtils.mostrarAlerta(
                    this,
                    "Verifica tu correo",
                    "Se ha enviado un correo de verificación a $correoActual. Por favor verifica antes de iniciar sesión.",
                    SweetAlertDialog.WARNING_TYPE
                ) {
                    firebaseAuth.signOut()
                    startActivity(Intent(this, InicioActivity::class.java))
                    finish()
                }
            } else {
                UiUtils.showToast(this, "Error al enviar correo de verificación")
            }
        }
    }

    override fun onError(errorMessage: String) {
        val mensajeEspanol = when {
            errorMessage.contains("email address is already in use", ignoreCase = true) ->
                "Este correo ya está registrado con otra cuenta. Intenta iniciar sesión."
            errorMessage.contains("email address is badly formatted", ignoreCase = true) ->
                "El formato del correo electrónico no es válido."
            errorMessage.contains("weak password", ignoreCase = true) ->
                "La contraseña es muy débil para los estándares de seguridad."
            errorMessage.contains("network-request-failed", ignoreCase = true) ->
                "Error de conexión. Revisa tu internet e inténtalo de nuevo."
            else -> "No se pudo crear la cuenta: $errorMessage"
        }

        UiUtils.mostrarAlerta(
            this,
            "Error de Registro",
            mensajeEspanol,
            SweetAlertDialog.ERROR_TYPE
        )
    }

    private fun validarFormularioCompleto() {
        val curp = binding.txtCurp.text.toString().trim()
        val nombre = binding.txtNombre.text.toString().trim()
        val apeP = binding.txtApellidoP.text.toString().trim()
        val apeM = binding.txtApellidoM.text.toString().trim()
        val tel = binding.txtTelefono.text.toString().trim()
        val calle = binding.txtCalle.text.toString().trim()
        val num = binding.txtNumero.text.toString().trim()
        val col = binding.txtColonia.text.toString().trim()
        val alc = binding.txtAlcaldia.text.toString().trim()
        val cp = binding.txtCodigoPostal.text.toString().trim()
        val email = binding.txtCorreo.text.toString().trim()
        val pass = binding.txtContrasena.text.toString()
        val confirm = binding.txtConfirmarContrasena.text.toString()

        binding.layoutCurp.error = if (!ValidationUtils.isValidCURP(curp)) "CURP no válida" else null
        binding.layoutNombre.error = if (!ValidationUtils.isValidName(nombre)) "Nombre inválido" else null
        binding.layoutApellidoP.error = if (!ValidationUtils.isValidName(apeP)) "Apellido paterno obligatorio" else null
        binding.layoutApellidoM.error = if (!ValidationUtils.isValidName(apeM)) "Apellido materno obligatorio" else null
        binding.layoutTelefono.error = if (!ValidationUtils.isValidPhone(tel)) "Teléfono debe ser de 10 dígitos" else null

        binding.layoutCalle.error = if (!ValidationUtils.isValidAddressField(calle)) "Calle obligatoria" else null
        binding.layoutNumero.error = if (num.isEmpty()) "Requerido" else null
        binding.layoutColonia.error = if (!ValidationUtils.isValidAddressField(col)) "Colonia obligatoria" else null
        binding.layoutAlcaldia.error = if (!ValidationUtils.isValidAddressField(alc)) "Alcaldía obligatoria" else null
        binding.layoutCodigoPostal.error = if (!ValidationUtils.isValidPostalCode(cp)) "CP de 5 dígitos" else null

        binding.layoutCorreo.error = if (!ValidationUtils.isValidEmail(email)) "Correo electrónico inválido" else null
        binding.layoutContrasena.error = if (!ValidationUtils.isStrongPassword(pass)) "Contraseña muy débil" else null
        binding.layoutConfirmarContrasena.error = if (pass != confirm) "Las contraseñas no coinciden" else null

        if (!binding.checkTerminos.isChecked) {
            UiUtils.mostrarAlerta(
                this,
                "Términos y Condiciones",
                "Debes completar los campos y aceptar los términos y condiciones para crear tu cuenta de Xólotl.",
                SweetAlertDialog.WARNING_TYPE
            )
            return
        }

        val tieneErrores = listOf(
            binding.layoutCurp, binding.layoutNombre, binding.layoutApellidoP,
            binding.layoutApellidoM, binding.layoutTelefono, binding.layoutCalle,
            binding.layoutNumero, binding.layoutColonia, binding.layoutAlcaldia,
            binding.layoutCodigoPostal, binding.layoutCorreo, binding.layoutContrasena,
            binding.layoutConfirmarContrasena
        ).any { it.error != null }

        if (!tieneErrores) {
            registrarUsuario()
        } else {
            UiUtils.mostrarAlerta(
                this,
                "Formulario incompleto",
                "Por favor, revisa los campos marcados en rojo para continuar.",
                SweetAlertDialog.ERROR_TYPE
            )
        }
    }

    private fun configurarValidacionesTiempoReal() {
        binding.txtCurp.addCustomTextWatcher { text ->
            val s = text.trim()
            binding.layoutCurp.error = when {
                s.isEmpty() -> "Campo obligatorio"
                s.length < 18 -> "Faltan ${18 - s.length} caracteres"
                !ValidationUtils.isValidCURP(s) -> "Formato de CURP incorrecto"
                else -> null
            }
        }

        binding.txtNombre.addCustomTextWatcher { text ->
            binding.layoutNombre.error = if (!ValidationUtils.isValidName(text)) "Solo letras (máx 50)" else null
        }

        binding.txtApellidoP.addCustomTextWatcher { text ->
            binding.layoutApellidoP.error = if (!ValidationUtils.isValidName(text)) "Solo letras" else null
        }

        binding.txtTelefono.addCustomTextWatcher { text ->
            binding.layoutTelefono.error = if (text.length < 10) "Deben ser 10 dígitos" else null
        }

        binding.txtCalle.addCustomTextWatcher { text ->
            binding.layoutCalle.error = if (text.trim().isEmpty()) "Requerido" else null
        }

        binding.txtNumero.addCustomTextWatcher { text ->
            binding.layoutNumero.error = if (text.isEmpty()) "Requerido" else null
        }

        binding.txtColonia.addCustomTextWatcher { text ->
            binding.layoutColonia.error = if (text.trim().isEmpty()) "Requerido" else null
        }

        binding.txtAlcaldia.addCustomTextWatcher { text ->
            binding.layoutAlcaldia.error = if (text.trim().isEmpty()) "Requerido" else null
        }

        binding.txtCodigoPostal.addCustomTextWatcher { text ->
            binding.layoutCodigoPostal.error = if (text.length < 5) "CP inválido (5 dígitos)" else null
        }

        binding.txtCorreo.addCustomTextWatcher { text ->
            val email = text.trim()
            binding.layoutCorreo.error = if (email.isNotEmpty() && !ValidationUtils.isValidEmail(email)) "Formato de correo inválido" else null
        }

        binding.txtContrasena.addCustomTextWatcher { text ->
            binding.layoutContrasena.error = when {
                text.isEmpty() -> "Campo obligatorio"
                text.length < 8 -> "Mínimo 8 caracteres"
                !text.any { c -> c.isUpperCase() } -> "Debe incluir una Mayúscula"
                !text.any { c -> c.isDigit() } -> "Debe incluir un Número"
                !text.any { c -> "@#\$%^&+=!¿?*._-".contains(c) } -> "Debe incluir un carácter especial (@#$.-)"
                else -> null
            }

            val confirm = binding.txtConfirmarContrasena.text.toString()
            if (confirm.isNotEmpty()) {
                binding.layoutConfirmarContrasena.error = if (text != confirm) "No coinciden" else null
            }
        }

        binding.txtConfirmarContrasena.addCustomTextWatcher { text ->
            val pass = binding.txtContrasena.text.toString()
            binding.layoutConfirmarContrasena.error = if (text != pass) "Las contraseñas no coinciden" else null
        }
    }
} // <-- AQUÍ SE CIERRA LA CLASE

// 4. LA FUNCIÓN DE EXTENSIÓN AHORA VIVE AFUERA DE LA CLASE
fun android.widget.EditText.addCustomTextWatcher(onTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(s?.toString() ?: "")
        }
        override fun afterTextChanged(s: android.text.Editable?) {}
    })
}