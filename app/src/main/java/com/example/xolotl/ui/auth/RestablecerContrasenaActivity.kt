package com.example.xolotl.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.data.repository.AuthCallback
import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.databinding.ActivityRestablecerContrasenaBinding
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils

class RestablecerContrasenaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRestablecerContrasenaBinding
    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRestablecerContrasenaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Activamos la validación moderna en tiempo real
        configurarValidacionesTiempoReal()

        binding.btnEnviarCorreo.setOnClickListener {
            val correo = binding.txtCorreoRestablecer.text.toString().trim()

            // Validación de campo vacío con SweetAlert
            if (correo.isEmpty()) {
                UiUtils.mostrarAlerta(
                    this,
                    "Campo requerido",
                    "Por favor, escribe tu correo electrónico para continuar.",
                    SweetAlertDialog.WARNING_TYPE
                )
                return@setOnClickListener
            }

            // Validación de formato
            if (!ValidationUtils.isValidEmail(correo)) {
                UiUtils.mostrarAlerta(
                    this,
                    "Formato inválido",
                    "El correo ingresado no tiene un formato correcto (ejemplo@correo.com).",
                    SweetAlertDialog.ERROR_TYPE
                )
                return@setOnClickListener
            }

            // Llamada al repositorio
            authRepo.enviarCorreoRecuperacion(correo, object : AuthCallback {
                override fun onSuccess() {
                    UiUtils.mostrarAlerta(
                        this@RestablecerContrasenaActivity,
                        "¡Correo enviado!",
                        "Se ha enviado un enlace de restablecimiento a: $correo",
                        SweetAlertDialog.SUCCESS_TYPE
                    ) {
                        // Al darle OK, cerramos la pantalla para volver al Login
                        finish()
                    }
                }

                override fun onError(errorMessage: String) {
                    // Traducción de errores de Firebase a español
                    val mensajeTraducido = when {
                        errorMessage.contains("user-not-found", true) || errorMessage.contains("no user", true) ->
                            "No existe ninguna cuenta registrada con este correo."
                        errorMessage.contains("network-request-failed", true) ->
                            "Error de red. Revisa tu conexión a internet."
                        else -> "No pudimos procesar la solicitud. Inténtalo más tarde."
                    }

                    UiUtils.mostrarAlerta(
                        this@RestablecerContrasenaActivity,
                        "Error de recuperación",
                        mensajeTraducido,
                        SweetAlertDialog.ERROR_TYPE
                    )
                }
            })
        }
    }

    private fun configurarValidacionesTiempoReal() {
        binding.txtCorreoRestablecer.addTextChangedListener {
            val correo = it.toString().trim()

            // Usamos el layout para mostrar el error abajo sin globito
            if (correo.isEmpty()) {
                binding.layoutCorreoRestablecer.error = null
            } else if (!ValidationUtils.isValidEmail(correo)) {
                binding.layoutCorreoRestablecer.error = "Ingresa un correo válido"
            } else {
                binding.layoutCorreoRestablecer.error = null
            }
        }
    }
}