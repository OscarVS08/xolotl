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
import androidx.core.content.ContextCompat
import com.example.xolotl.R



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

        // --- Validaciones visuales ---
        binding.txtCorreo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val correo = s.toString().trim()
                val isValid = ValidationUtils.isValidEmail(correo)
                binding.txtCorreo.updateTextColor(isValid)
                binding.txtCorreo.error = if (correo.isNotEmpty() && !isValid) "Correo inválido" else null
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.txtContrasena.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                val mensaje = when {
                    pass.isEmpty() -> null
                    pass.length < 6 -> "Muy corta"
                    ValidationUtils.isStrongPassword(pass) -> "Fuerte 💪"
                    else -> "Media ⚠️"
                }
                binding.txtContrasena.updateTextColor(pass.length >= 6)
                binding.txtContrasena.error = mensaje
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnOlvidasteContrasena.setOnClickListener {
            startActivity(Intent(this, RestablecerContrasenaActivity::class.java))
        }

        binding.btnIngresar.setOnClickListener {
            val email = binding.txtCorreo.text.toString().trim()
            val password = binding.txtContrasena.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                UiUtils.showToast(this, "Por favor, llena todos los campos")
                return@setOnClickListener
            }

            authRepo.iniciarSesion(email, password, object : AuthCallback {
                override fun onSuccess() {
                    // --- Validar correo verificado ---
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

                    // --- Usuario verificado, continuar ---
                    UiUtils.showToast(this@IniciarSesionActivity, "Inicio de sesión exitoso")
                    val intent = Intent(this@IniciarSesionActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }

                override fun onError(errorMessage: String) {
                    UiUtils.showToast(this@IniciarSesionActivity, errorMessage)
                }
            })
        }
        setupPasswordToggle()
    }

    private fun setupPasswordToggle() {
        binding.btnToggleContrasena.setOnClickListener {
            mostrarPassword = !mostrarPassword

            // Cambiar tipo de input
            val type = if (mostrarPassword)
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            else
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

            binding.txtContrasena.inputType = type
            binding.txtContrasena.setSelection(binding.txtContrasena.text?.length ?: 0)

            // Cambiar icono del ojo
            val icon = if (mostrarPassword)
                R.drawable.ojocontrasena
            else
                R.drawable.ojocontrasena

            binding.btnToggleContrasena.setImageResource(icon)
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
