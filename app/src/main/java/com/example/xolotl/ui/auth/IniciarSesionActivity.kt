package com.example.xolotl.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.MainActivity
import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.databinding.ActivityIniciarSesionBinding
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.example.xolotl.utils.updateTextColor

class IniciarSesionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIniciarSesionBinding
    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIniciarSesionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Cambiar color y mostrar guía visual según validez ---
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

            authRepo.iniciarSesion(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        UiUtils.showToast(this, "Inicio de sesión exitoso")
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        UiUtils.showToast(this, task.exception?.localizedMessage ?: "Error al iniciar sesión")
                    }
                }
        }
    }

    override fun onStart() {
        super.onStart()
        if (authRepo.obtenerUsuarioActual() != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
