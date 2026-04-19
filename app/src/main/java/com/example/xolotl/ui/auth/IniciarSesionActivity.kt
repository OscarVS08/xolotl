package com.example.xolotl.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.MainActivity
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.databinding.ActivityIniciarSesionBinding
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.example.xolotl.utils.updateTextColor
import com.google.firebase.auth.FirebaseAuth
import com.example.xolotl.R
import androidx.core.widget.addTextChangedListener

class IniciarSesionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIniciarSesionBinding
    private val authRepo = AuthRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()
    // Variable para controlar mostrar/ocultar contraseña
    private var mostrarPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIniciarSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Asegurar que el icono inicial sea el cerrado
        binding.btnToggleContrasena.setImageResource(R.drawable.ic_eye_closed)

        binding.btnOlvidasteContrasena.setOnClickListener {
            startActivity(Intent(this, RestablecerContrasenaActivity::class.java))
        }

        binding.btnIngresar.setOnClickListener {
            val email = binding.txtCorreo.text.toString().trim()
            val password = binding.txtContrasena.text.toString().trim()

            // CAMBIO: Ahora usamos SweetAlert para campos vacíos
            if (email.isEmpty() || password.isEmpty()) {
                UiUtils.mostrarAlerta(
                    this,
                    "Campos incompletos",
                    "Por favor, llena todos los campos del formulario para continuar.",
                    SweetAlertDialog.WARNING_TYPE
                )
                return@setOnClickListener
            }

            // Validación de formato antes de enviar a Firebase
            if (!ValidationUtils.isValidEmail(email)) {
                UiUtils.mostrarAlerta(
                    this,
                    "Correo inválido",
                    "El formato del correo electrónico no es correcto.",
                    SweetAlertDialog.ERROR_TYPE
                )
                return@setOnClickListener
            }

            authRepo.iniciarSesion(email, password, object : AuthCallback {
                override fun onSuccess() {
                    val user = firebaseAuth.currentUser
                    if (user != null && !user.isEmailVerified) {
                        UiUtils.mostrarAlerta(
                            this@IniciarSesionActivity,
                            "Correo no verificado",
                            "Por favor verifica tu correo antes de iniciar sesión",
                            SweetAlertDialog.WARNING_TYPE
                        )
                        firebaseAuth.signOut()
                        return
                    }

                    //UiUtils.showToast(this@IniciarSesionActivity, "¡Bienvenido a Xólotl!")
                    val intent = Intent(this@IniciarSesionActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

                override fun onError(errorMessage: String) {
                    // Convertimos el mensaje técnico en inglés a uno amigable en español
                    val mensajeEspanol = when {
                        errorMessage.contains("password", ignoreCase = true) -> "La contraseña es incorrecta. Inténtalo de nuevo."
                        errorMessage.contains("user not found", ignoreCase = true) || errorMessage.contains("no user", ignoreCase = true) -> "No existe una cuenta con este correo."
                        errorMessage.contains("badly formatted", ignoreCase = true) -> "El formato del correo no es válido."
                        errorMessage.contains("network error", ignoreCase = true) -> "Error de conexión. Revisa tu internet."
                        errorMessage.contains("too many requests", ignoreCase = true) -> "Demasiados intentos. Intenta más tarde."
                        else -> "Ocurrió un error inesperado. Inténtalo de nuevo."
                    }

                    UiUtils.mostrarAlerta(
                        this@IniciarSesionActivity,
                        "Error de acceso",
                        mensajeEspanol,
                        SweetAlertDialog.ERROR_TYPE
                    )
                }
            })
        }
        setupPasswordToggle()
        configurarValidacionesTiempoReal()
    }

    private fun setupPasswordToggle() {
        binding.btnToggleContrasena.setOnClickListener {
            mostrarPassword = !mostrarPassword

            if (mostrarPassword) {
                // ABRIR OJO: Mostrar contraseña
                // Usamos HideReturnsTransformationMethod para ver el texto plano
                binding.txtContrasena.transformationMethod = android.text.method.HideReturnsTransformationMethod.getInstance()
                binding.btnToggleContrasena.setImageResource(R.drawable.ic_eye_open)
            } else {
                // CERRAR OJO: Ocultar contraseña
                // Usamos PasswordTransformationMethod para ver los puntos
                binding.txtContrasena.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
                binding.btnToggleContrasena.setImageResource(R.drawable.ic_eye_closed)
            }

            // Mover el cursor al final para que no se regrese al inicio al cambiar el icono
            binding.txtContrasena.setSelection(binding.txtContrasena.text?.length ?: 0)
        }
    }

    // FUNCIÓN ADAPTADA: Validaciones en tiempo real para Login
    private fun configurarValidacionesTiempoReal() {
        // --- Validación Correo ---
        binding.txtCorreo.addTextChangedListener {
            val correo = it.toString().trim()
            if (correo.isEmpty()) {
                binding.layoutCorreo.error = null
            } else if (!ValidationUtils.isValidEmail(correo)) {
                binding.layoutCorreo.error = "Ingresa un correo válido"
            } else {
                binding.layoutCorreo.error = null
            }
        }

        // --- Validación Contraseña con Niveles (Corta, Media, Fuerte) ---
        binding.txtContrasena.addTextChangedListener {
            val pass = it.toString()

            // Aplicamos tu lógica de niveles pero al Layout
            val mensajeError = when {
                pass.isEmpty() -> null
                pass.length < 6 -> "Contraseña muy corta"
                ValidationUtils.isStrongPassword(pass) -> "Seguridad: Fuerte"
                else -> "Seguridad: Media (usa mayúsculas y símbolos)"
            }

            // Mostramos el mensaje en el layout de abajo
            binding.layoutContrasena.error = mensajeError

            // Esto cambia el color del texto si es mayor a 6, como ya lo tenías
            binding.txtContrasena.updateTextColor(pass.length >= 6)

            // IMPORTANTE: Si la contraseña es "Fuerte", podemos cambiar el color del mensaje a verde
            // (opcional, si quieres que se vea más pro)
            if (ValidationUtils.isStrongPassword(pass)) {
                binding.layoutContrasena.setErrorTextColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.GREEN))
            } else {
                // Volvemos al rojo estándar si no es fuerte
                binding.layoutContrasena.setErrorTextColor(android.content.res.ColorStateList.valueOf(android.graphics.Color.RED))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val user = authRepo.obtenerUsuarioActual()
        if (user != null && firebaseAuth.currentUser?.isEmailVerified == true) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
