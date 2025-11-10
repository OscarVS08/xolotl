package com.example.xolotl.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.databinding.ActivityRestablecerContrasenaBinding
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.example.xolotl.utils.updateTextColor

class RestablecerContrasenaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestablecerContrasenaBinding
    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestablecerContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- Cambiar color y mostrar guía visual según validez del correo ---
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

        binding.btnEnviarCorreo.setOnClickListener {
            val correo = binding.txtCorreo.text.toString().trim()

            // Validación del correo
            if (!ValidationUtils.isValidEmail(correo)) {
                UiUtils.showToast(this, "Por favor ingresa un correo válido")
                return@setOnClickListener
            }

            // Enviar correo de restablecimiento
            authRepo.enviarCorreoRecuperacion(
                correo,
                onSuccess = {
                    UiUtils.showToast(this, "Correo de restablecimiento enviado a $correo")
                    finish()
                },
                onError = {
                    UiUtils.showToast(this, it)
                }
            )
        }
    }
}
