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
import androidx.core.widget.addTextChangedListener

class RegistrarseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarseBinding
    private val authRepo = AuthRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private var mostrarPassword = false
    private var mostrarConfirmPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupPasswordToggle()
        configurarValidacionesTiempoReal()

        // Listener para el link de términos
        binding.txtTerminosLink.setOnClickListener {
            UiUtils.showToast(this, "Abriendo términos y condiciones...")
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
                // MOSTRAR: Texto plano
                binding.txtContrasena.transformationMethod = android.text.method.HideReturnsTransformationMethod.getInstance()
                binding.btnToggleContrasena.setImageResource(R.drawable.ic_eye_open)
            } else {
                // OCULTAR: Puntos/Asteriscos
                binding.txtContrasena.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                binding.btnToggleContrasena.setImageResource(R.drawable.ic_eye_closed)
            }

            // Mantener el cursor al final del texto
            binding.txtContrasena.setSelection(binding.txtContrasena.text?.length ?: 0)
        }

        // --- Ojo para Confirmar Contraseña ---
        binding.btnToggleConfirmarContrasena.setOnClickListener {
            mostrarConfirmPassword = !mostrarConfirmPassword

            if (mostrarConfirmPassword) {
                // MOSTRAR
                binding.txtConfirmarContrasena.transformationMethod = android.text.method.HideReturnsTransformationMethod.getInstance()
                binding.btnToggleConfirmarContrasena.setImageResource(R.drawable.ic_eye_open)
            } else {
                // OCULTAR
                binding.txtConfirmarContrasena.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                binding.btnToggleConfirmarContrasena.setImageResource(R.drawable.ic_eye_closed)
            }

            // Mantener el cursor al final del texto
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
                        UiUtils.showToast(
                            this@RegistrarseActivity,
                            "Error al enviar correo de verificación"
                        )
                    }
                }
            }

            override fun onError(errorMessage: String) {
                // TRADUCCIÓN DE ERRORES:
                val mensajeEspanol = when {
                    // El error que mencionas: Cuenta duplicada
                    errorMessage.contains("email address is already in use", ignoreCase = true) ->
                        "Este correo ya está registrado con otra cuenta. Intenta iniciar sesión."

                    // Error de formato (aunque ya lo validamos, Firebase a veces lo lanza)
                    errorMessage.contains("email address is badly formatted", ignoreCase = true) ->
                        "El formato del correo electrónico no es válido."

                    // Error de contraseña débil (por si Firebase tiene reglas distintas)
                    errorMessage.contains("weak password", ignoreCase = true) ->
                        "La contraseña es muy débil para los estándares de seguridad."

                    // Error de red
                    errorMessage.contains("network-request-failed", ignoreCase = true) ->
                        "Error de conexión. Revisa tu internet e inténtalo de nuevo."

                    // Otros errores
                    else -> "No se pudo crear la cuenta: $errorMessage"
                }

                // Mostramos la SweetAlert con el mensaje en español
                UiUtils.mostrarAlerta(
                    this@RegistrarseActivity,
                    "Error de Registro",
                    mensajeEspanol,
                    SweetAlertDialog.ERROR_TYPE
                )
            }
        })
    }

    private fun validarFormularioCompleto() {
        // 1. Extraer todos los valores
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

        // 2. Activar validación visual en los Layouts (Rojo moderno)
        binding.layoutCurp.error = if (!ValidationUtils.isValidCURP(curp)) "CURP no válida" else null
        binding.layoutNombre.error = if (!ValidationUtils.isValidName(nombre)) "Nombre inválido" else null
        binding.layoutApellidoP.error = if (!ValidationUtils.isValidName(apeP)) "Apellido paterno obligatorio" else null
        binding.layoutApellidoM.error = if (!ValidationUtils.isValidName(apeM)) "Apellido materno obligatorio" else null
        binding.layoutTelefono.error = if (!ValidationUtils.isValidPhone(tel)) "Teléfono debe ser de 10 dígitos" else null

        // Dirección
        binding.layoutCalle.error = if (!ValidationUtils.isValidAddressField(calle)) "Calle obligatoria" else null
        binding.layoutNumero.error = if (num.isEmpty()) "Requerido" else null
        binding.layoutColonia.error = if (!ValidationUtils.isValidAddressField(col)) "Colonia obligatoria" else null
        binding.layoutAlcaldia.error = if (!ValidationUtils.isValidAddressField(alc)) "Alcaldía obligatoria" else null
        binding.layoutCodigoPostal.error = if (!ValidationUtils.isValidPostalCode(cp)) "CP de 5 dígitos" else null

        // Seguridad
        binding.layoutCorreo.error = if (!ValidationUtils.isValidEmail(email)) "Correo electrónico inválido" else null
        binding.layoutContrasena.error = if (!ValidationUtils.isStrongPassword(pass)) "Contraseña muy débil" else null
        binding.layoutConfirmarContrasena.error = if (pass != confirm) "Las contraseñas no coinciden" else null

        // 3. Validar Checkbox de Términos (Sweet Alert si no está marcado)
        if (!binding.checkTerminos.isChecked) {
            UiUtils.mostrarAlerta(
                this,
                "Términos y Condiciones",
                "Debes completar los campos y aceptar los términos y condiciones para crear tu cuenta de Xólotl.",
                SweetAlertDialog.WARNING_TYPE
            )
            return
        }

        // 4. Verificación Final: ¿Hay algún error visible?
        val tieneErrores = listOf(
            binding.layoutCurp, binding.layoutNombre, binding.layoutApellidoP,
            binding.layoutApellidoM, binding.layoutTelefono, binding.layoutCalle,
            binding.layoutNumero, binding.layoutColonia, binding.layoutAlcaldia,
            binding.layoutCodigoPostal, binding.layoutCorreo, binding.layoutContrasena,
            binding.layoutConfirmarContrasena
        ).any { it.error != null }

        if (!tieneErrores) {
            registrarUsuario() // Procedemos al registro en Firebase
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
        // --- DATOS PERSONALES ---
        binding.txtCurp.addTextChangedListener {
            val s = it.toString().trim()
            binding.layoutCurp.error = when {
                s.isEmpty() -> "Campo obligatorio"
                s.length < 18 -> "Faltan ${18 - s.length} caracteres"
                !ValidationUtils.isValidCURP(s) -> "Formato de CURP incorrecto"
                else -> null
            }
        }

        binding.txtNombre.addTextChangedListener {
            binding.layoutNombre.error = if (!ValidationUtils.isValidName(it.toString())) "Solo letras (máx 50)" else null
        }

        binding.txtApellidoP.addTextChangedListener {
            binding.layoutApellidoP.error = if (!ValidationUtils.isValidName(it.toString())) "Solo letras" else null
        }

        binding.txtTelefono.addTextChangedListener {
            val s = it.toString()
            binding.layoutTelefono.error = if (s.length < 10) "Deben ser 10 dígitos" else null
        }

        // --- DIRECCIÓN ---
        binding.txtCalle.addTextChangedListener {
            binding.layoutCalle.error = if (it.toString().trim().isEmpty()) "Requerido" else null
        }

        binding.txtNumero.addTextChangedListener {
            // Solo aceptara números, aquí validamos que no esté vacío
            binding.layoutNumero.error = if (it.toString().isEmpty()) "Requerido" else null
        }

        binding.txtColonia.addTextChangedListener {
            binding.layoutColonia.error = if (it.toString().trim().isEmpty()) "Requerido" else null
        }

        binding.txtAlcaldia.addTextChangedListener {
            binding.layoutAlcaldia.error = if (it.toString().trim().isEmpty()) "Requerido" else null
        }

        binding.txtCodigoPostal.addTextChangedListener {
            val s = it.toString()
            binding.layoutCodigoPostal.error = if (s.length < 5) "CP inválido (5 dígitos)" else null
        }



        // --- SEGURIDAD ---
        binding.txtCorreo.addTextChangedListener {
            val email = it.toString().trim()
            binding.layoutCorreo.error = if (email.isNotEmpty() && !ValidationUtils.isValidEmail(email)) "Formato de correo inválido" else null
        }

        binding.txtContrasena.addTextChangedListener {
            val s = it.toString()
            binding.layoutContrasena.error = when {
                s.isEmpty() -> "Campo obligatorio"
                s.length < 8 -> "Mínimo 8 caracteres"
                !s.any { c -> c.isUpperCase() } -> "Debe incluir una Mayúscula"
                !s.any { c -> c.isDigit() } -> "Debe incluir un Número"
                !s.any { c -> "@#\$%^&+=!¿?*._-".contains(c) } -> "Debe incluir un carácter especial (@#$.-)"
                else -> null
            }
            // Re-validar confirmación si ya se había escrito algo
            val confirm = binding.txtConfirmarContrasena.text.toString()
            if (confirm.isNotEmpty()) {
                binding.layoutConfirmarContrasena.error = if (s != confirm) "No coinciden" else null
            }
        }

        binding.txtConfirmarContrasena.addTextChangedListener {
            val s = it.toString()
            val pass = binding.txtContrasena.text.toString()
            binding.layoutConfirmarContrasena.error = if (s != pass) "Las contraseñas no coinciden" else null
        }
    }

}
