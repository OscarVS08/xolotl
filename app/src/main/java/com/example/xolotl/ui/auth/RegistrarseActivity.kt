package com.example.xolotl.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.MainActivity
import com.example.xolotl.data.models.User
import com.example.xolotl.data.repository.AuthRepository
import com.example.xolotl.databinding.ActivityRegistrarseBinding
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.utils.updateTextColor

class RegistrarseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarseBinding
    private val authRepo = AuthRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrarseBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.txtNombre.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val nombre = s.toString().trim()
                if (!ValidationUtils.isValidName(nombre)) {
                    binding.txtNombre.error = "Solo letras y máx. 50 caracteres"
                    binding.txtNombre.updateTextColor(false)
                } else {
                    binding.txtNombre.error = null
                    binding.txtNombre.updateTextColor(true)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- APELLIDOS ---
        val apellidoWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val apellidoP = binding.txtApellidoP.text.toString().trim()
                val apellidoM = binding.txtApellidoM.text.toString().trim()

                if (!ValidationUtils.isValidName(apellidoP)) {
                    binding.txtApellidoP.error = "Solo letras y máx. 50 caracteres"
                    binding.txtApellidoP.updateTextColor(false)
                } else {
                    binding.txtApellidoP.error = null
                    binding.txtApellidoP.updateTextColor(true)
                }

                if (!ValidationUtils.isValidName(apellidoM)) {
                    binding.txtApellidoM.error = "Solo letras y máx. 50 caracteres"
                    binding.txtApellidoM.updateTextColor(false)
                } else {
                    binding.txtApellidoM.error = null
                    binding.txtApellidoM.updateTextColor(true)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        binding.txtApellidoP.addTextChangedListener(apellidoWatcher)
        binding.txtApellidoM.addTextChangedListener(apellidoWatcher)

        // --- TELÉFONOS ---
        binding.txtTelefono.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val tel = s.toString().trim()
                if (!ValidationUtils.isValidPhone(tel)) {
                    binding.txtTelefono.error = "10 dígitos requeridos"
                    binding.txtTelefono.updateTextColor(false)
                } else {
                    binding.txtTelefono.error = null
                    binding.txtTelefono.updateTextColor(true)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.txtTelefonoAlt.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val tel = s.toString().trim()
                if (tel.isNotEmpty() && !ValidationUtils.isValidPhone(tel)) {
                    binding.txtTelefonoAlt.error = "10 dígitos requeridos"
                    binding.txtTelefonoAlt.updateTextColor(false)
                } else {
                    binding.txtTelefonoAlt.error = null
                    binding.txtTelefonoAlt.updateTextColor(true)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- DIRECCIÓN ---
        val addressFields = listOf(
            binding.txtCalle to "Calle inválida",
            binding.txtNumero to "Número inválido",
            binding.txtColonia to "Colonia inválida",
            binding.txtAlcaldia to "Alcaldía inválida"
        )
        addressFields.forEach { (field, msg) ->
            field.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val text = s.toString().trim()
                    if (!ValidationUtils.isValidAddressField(text)) {
                        field.error = msg
                        field.updateTextColor(false)
                    } else {
                        field.error = null
                        field.updateTextColor(true)
                    }
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        // --- CÓDIGO POSTAL ---
        binding.txtCodigoPostal.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val cp = s.toString().trim()
                if (!ValidationUtils.isValidPostalCode(cp)) {
                    binding.txtCodigoPostal.error = "5 dígitos requeridos"
                    binding.txtCodigoPostal.updateTextColor(false)
                } else {
                    binding.txtCodigoPostal.error = null
                    binding.txtCodigoPostal.updateTextColor(true)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- CORREO ---
        binding.txtCorreo.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val email = s.toString().trim()
                if (!ValidationUtils.isValidEmail(email)) {
                    binding.txtCorreo.error = "Correo inválido"
                    binding.txtCorreo.updateTextColor(false)
                } else {
                    binding.txtCorreo.error = null
                    binding.txtCorreo.updateTextColor(true)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // --- CONTRASEÑA ---
        binding.txtContrasena.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pass = s.toString()
                val mensaje = when {
                    pass.length < 8 -> "Muy débil ❌"
                    ValidationUtils.isStrongPassword(pass) -> "Fuerte 💪"
                    else -> "Media ⚠️"
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

        // --- BOTÓN CREAR CUENTA ---
        binding.btnCrearCuenta.setOnClickListener { registrarUsuario() }

        // --- OLVIDASTE CURP ---
        binding.txtOlvidasteCurp.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://www.gob.mx/curp/")
            startActivity(intent)
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
                curp, nombre, apellidoP, telefono,
                calle, numero, colonia, alcaldia, codigoPostal, correo, contrasena, confirmarContrasena
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
            !ValidationUtils.isValidName(apellidoM)) {
            UiUtils.showToast(this, "Nombre o apellidos inválidos")
            return
        }

        if (!ValidationUtils.isValidPhone(telefono) ||
            (telefonoAlt.isNotEmpty() && !ValidationUtils.isValidPhone(telefonoAlt))) {
            UiUtils.showToast(this, "Teléfonos inválidos")
            return
        }

        if (!ValidationUtils.isValidAddressField(calle) ||
            !ValidationUtils.isValidAddressField(numero) ||
            !ValidationUtils.isValidAddressField(colonia) ||
            !ValidationUtils.isValidAddressField(alcaldia)) {
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

        // --- Crear usuario ---
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

        // --- Registrar en Firebase ---
        authRepo.registrarUsuario(
            correo,
            contrasena,
            usuario,
            onSuccess = {
                UiUtils.mostrarAlerta(
                    this,
                    "Cuenta creada",
                    "¡Tu cuenta se ha registrado correctamente!",
                    SweetAlertDialog.SUCCESS_TYPE
                ) {
                    // Solo después de OK se va al MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            },
            onError = { error ->
                UiUtils.mostrarAlerta(
                    this,
                    "Error",
                    "No se pudo crear la cuenta: $error",
                    SweetAlertDialog.ERROR_TYPE
                )
            }
        )
    }
}
