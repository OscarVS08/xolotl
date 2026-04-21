package com.example.xolotl.ui.main.usuario

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.databinding.ActivityEditarPerfilUsuarioBinding
import com.example.xolotl.ui.auth.InicioActivity
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.example.xolotl.utils.ValidationUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditarPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditarPerfilUsuarioBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.uid

    private var mostrarPassword = false
    private var mostrarConfirmPassword = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditarPerfilUsuarioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cargarDatos()
        setupPasswordToggle()
        configurarValidacionesTiempoReal()

        binding.btnHome.setOnClickListener { finish() }

        binding.btnGuardarCambios.setOnClickListener {
            if (validarFormularioCompleto()) {
                guardarCambios()
            }
        }

        binding.btnEliminarCuenta.setOnClickListener {
            confirmarEliminarCuenta()
        }
    }

    private fun cargarDatos() {
        if (userId == null) return
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    binding.txtCurp.text = EncryptionUtils.decrypt(doc.getString("curp") ?: "")
                    binding.txtNombre.setText(EncryptionUtils.decrypt(doc.getString("nombre") ?: ""))
                    binding.txtApellidoP.setText(EncryptionUtils.decrypt(doc.getString("apellidoP") ?: ""))
                    binding.txtApellidoM.setText(EncryptionUtils.decrypt(doc.getString("apellidoM") ?: ""))
                    binding.txtTelefono.setText(EncryptionUtils.decrypt(doc.getString("telefono") ?: ""))
                    binding.txtTelefonoAlt.setText(EncryptionUtils.decrypt(doc.getString("telefonoAlt") ?: ""))
                    binding.txtCalle.setText(EncryptionUtils.decrypt(doc.getString("calle") ?: ""))
                    binding.txtNumero.setText(EncryptionUtils.decrypt(doc.getString("numero") ?: ""))
                    binding.txtColonia.setText(EncryptionUtils.decrypt(doc.getString("colonia") ?: ""))
                    binding.txtAlcaldia.setText(EncryptionUtils.decrypt(doc.getString("alcaldia") ?: ""))
                    binding.txtCodigoPostal.setText(EncryptionUtils.decrypt(doc.getString("codigoPostal") ?: ""))
                }
            }
    }

    private fun setupPasswordToggle() {
        binding.btnToggleContrasena.setOnClickListener {
            mostrarPassword = !mostrarPassword
            binding.txtContrasena.transformationMethod = if (mostrarPassword)
                HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            binding.btnToggleContrasena.setImageResource(if (mostrarPassword) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            binding.txtContrasena.setSelection(binding.txtContrasena.text.length)
        }

        binding.btnToggleConfirmarContrasena.setOnClickListener {
            mostrarConfirmPassword = !mostrarConfirmPassword
            binding.txtConfirmarContrasena.transformationMethod = if (mostrarConfirmPassword)
                HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            binding.btnToggleConfirmarContrasena.setImageResource(if (mostrarConfirmPassword) R.drawable.ic_eye_open else R.drawable.ic_eye_closed)
            binding.txtConfirmarContrasena.setSelection(binding.txtConfirmarContrasena.text.length)
        }
    }

    private fun configurarValidacionesTiempoReal() {
        // --- NOMBRES ---
        binding.txtNombre.addTextChangedListener {
            val s = it.toString().trim()
            binding.layoutNombre.error = when {
                s.isEmpty() -> "El nombre es obligatorio"
                s.length < 3 -> "Mínimo 3 caracteres"
                !ValidationUtils.isValidName(s) -> "Solo se permiten letras"
                else -> null
            }
        }

        binding.txtApellidoP.addTextChangedListener {
            val s = it.toString().trim()
            binding.layoutApellidoP.error = when {
                s.isEmpty() -> "Apellido paterno obligatorio"
                s.length < 3 -> "Mínimo 3 caracteres"
                !ValidationUtils.isValidName(s) -> "Solo se permiten letras"
                else -> null
            }
        }

        binding.txtApellidoM.addTextChangedListener {
            val s = it.toString().trim()
            binding.layoutApellidoM.error = when {
                s.isEmpty() -> "Apellido materno obligatorio"
                s.length < 3 -> "Mínimo 3 caracteres"
                !ValidationUtils.isValidName(s) -> "Solo se permiten letras"
                else -> null
            }
        }

        // --- TELÉFONOS ---
        binding.txtTelefono.addTextChangedListener {
            val s = it.toString().trim()
            binding.layoutTelefono.error = when {
                s.isEmpty() -> "Teléfono obligatorio"
                s.length != 10 -> "Deben ser exactamente 10 dígitos (llevas ${s.length})"
                !s.all { c -> c.isDigit() } -> "Solo se permiten números"
                else -> null
            }
        }

        binding.txtTelefonoAlt.addTextChangedListener {
            val s = it.toString().trim()
            if (s.isNotEmpty()) {
                binding.layoutTelefonoAlt.error = if (s.length != 10) "Deben ser 10 dígitos" else null
            } else {
                binding.layoutTelefonoAlt.error = null
            }
        }

        // --- DIRECCIÓN ---
        binding.txtCalle.addTextChangedListener {
            val s = it.toString().trim()
            binding.layoutCalle.error = if (s.isEmpty()) "La calle es obligatoria" else if (s.length < 4) "Calle muy corta" else null
        }

        binding.txtNumero.addTextChangedListener {
            binding.layoutNumero.error = if (it.toString().trim().isEmpty()) "Número requerido" else null
        }

        binding.txtColonia.addTextChangedListener {
            val s = it.toString().trim()
            binding.layoutColonia.error = if (s.isEmpty()) "Colonia obligatoria" else if (s.length < 4) "Nombre de colonia inválido" else null
        }

        binding.txtAlcaldia.addTextChangedListener {
            binding.layoutAlcaldia.error = if (it.toString().trim().isEmpty()) "Alcaldía requerida" else null
        }

        binding.txtCodigoPostal.addTextChangedListener {
            val s = it.toString().trim()
            binding.layoutCodigoPostal.error = when {
                s.isEmpty() -> "C.P. obligatorio"
                s.length != 5 -> "Deben ser 5 dígitos"
                else -> null
            }
        }

        // --- SEGURIDAD ---
        binding.txtContrasena.addTextChangedListener {
            val s = it.toString()
            if (s.isNotEmpty()) {
                binding.layoutContrasena.error = when {
                    s.length < 8 -> "Mínimo 8 caracteres"
                    !s.any { c -> c.isUpperCase() } -> "Falta una Mayúscula"
                    !s.any { c -> c.isDigit() } -> "Falta un Número"
                    !s.any { c -> "@#\$%^&+=!¿?*._-".contains(c) } -> "Falta un símbolo (@#$.)"
                    else -> null
                }
            } else {
                binding.layoutContrasena.error = null
            }
        }

        binding.txtConfirmarContrasena.addTextChangedListener {
            val s = it.toString()
            binding.layoutConfirmarContrasena.error = if (s != binding.txtContrasena.text.toString()) "Las contraseñas no coinciden" else null
        }
    }

    private fun validarFormularioCompleto(): Boolean {
        // EXTRAER VALORES
        val nom = binding.txtNombre.text.toString().trim()
        val apeP = binding.txtApellidoP.text.toString().trim()
        val apeM = binding.txtApellidoM.text.toString().trim()
        val tel = binding.txtTelefono.text.toString().trim()
        val calle = binding.txtCalle.text.toString().trim()
        val num = binding.txtNumero.text.toString().trim()
        val col = binding.txtColonia.text.toString().trim()
        val alc = binding.txtAlcaldia.text.toString().trim()
        val cp = binding.txtCodigoPostal.text.toString().trim()
        val pass = binding.txtContrasena.text.toString()
        val conf = binding.txtConfirmarContrasena.text.toString()

        // VALIDACIÓN ESTRICTA EN LOS LAYOUTS (No usamos txtCampo.error para evitar el signo rojo)
        binding.layoutNombre.error = if (nom.isEmpty()) "Requerido" else if (nom.length < 3) "Nombre muy corto" else null
        binding.layoutApellidoP.error = if (apeP.isEmpty()) "Requerido" else null
        binding.layoutApellidoM.error = if (apeM.isEmpty()) "Requerido" else null
        binding.layoutTelefono.error = if (tel.length != 10) "Se requieren 10 dígitos" else null
        binding.layoutCalle.error = if (calle.isEmpty()) "Requerido" else null
        binding.layoutNumero.error = if (num.isEmpty()) "Requerido" else null
        binding.layoutColonia.error = if (col.isEmpty()) "Requerido" else null
        binding.layoutAlcaldia.error = if (alc.isEmpty()) "Requerido" else null
        binding.layoutCodigoPostal.error = if (cp.length != 5) "CP inválido" else null

        if (pass.isNotEmpty()) {
            if (!ValidationUtils.isStrongPassword(pass)) binding.layoutContrasena.error = "Contraseña muy débil"
            if (pass != conf) binding.layoutConfirmarContrasena.error = "No coinciden"
        }

        val layouts = listOf(
            binding.layoutNombre, binding.layoutApellidoP, binding.layoutApellidoM,
            binding.layoutTelefono, binding.layoutCalle, binding.layoutNumero,
            binding.layoutColonia, binding.layoutAlcaldia, binding.layoutCodigoPostal,
            binding.layoutContrasena, binding.layoutConfirmarContrasena
        )

        // FINALMENTE: Si hay algún error en los layouts, no permitir el guardado
        if (layouts.any { it.error != null }) {
            UiUtils.mostrarAlerta(this, "Atención", "Hay errores en el formulario o campos vacíos. Por favor corrígelos.", SweetAlertDialog.ERROR_TYPE)
            return false
        }

        return true
    }

    private fun guardarCambios() {
        UiUtils.mostrarAlertaCerrarSesion(this, "¿Guardar cambios?", "Tu información será actualizada.", SweetAlertDialog.WARNING_TYPE, "Guardar", "Cancelar", onConfirm = {
            val datos = hashMapOf(
                "nombre" to EncryptionUtils.encrypt(binding.txtNombre.text.toString().trim()),
                "apellidoP" to EncryptionUtils.encrypt(binding.txtApellidoP.text.toString().trim()),
                "apellidoM" to EncryptionUtils.encrypt(binding.txtApellidoM.text.toString().trim()),
                "telefono" to EncryptionUtils.encrypt(binding.txtTelefono.text.toString().trim()),
                "telefonoAlt" to EncryptionUtils.encrypt(binding.txtTelefonoAlt.text.toString().trim()),
                "calle" to EncryptionUtils.encrypt(binding.txtCalle.text.toString().trim()),
                "numero" to EncryptionUtils.encrypt(binding.txtNumero.text.toString().trim()),
                "colonia" to EncryptionUtils.encrypt(binding.txtColonia.text.toString().trim()),
                "alcaldia" to EncryptionUtils.encrypt(binding.txtAlcaldia.text.toString().trim()),
                "codigoPostal" to EncryptionUtils.encrypt(binding.txtCodigoPostal.text.toString().trim())
            )

            if (binding.txtContrasena.text.toString().isNotEmpty()) {
                datos["password"] = EncryptionUtils.encrypt(binding.txtContrasena.text.toString())
            }

            db.collection("usuarios").document(userId!!).update(datos as Map<String, Any>)
                .addOnSuccessListener {
                    UiUtils.mostrarAlerta(this, "¡Éxito!", "Perfil actualizado correctamente", SweetAlertDialog.SUCCESS_TYPE) { finish() }
                }
                .addOnFailureListener { e ->
                    UiUtils.mostrarAlerta(this, "Error", "No se pudo actualizar los datos", SweetAlertDialog.ERROR_TYPE)
                }
        })
    }

    private fun confirmarEliminarCuenta() {
        UiUtils.mostrarAlertaCerrarSesion(this, "¿BORRAR CUENTA?", "Esta acción es irreversible. Se perderán todos tus datos.", SweetAlertDialog.WARNING_TYPE, "BORRAR TODO", "Cancelar", onConfirm = {
            eliminarRastroTotal()
        })
    }

    private fun eliminarRastroTotal() {
        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply { titleText = "Borrando..."; show() }
        db.collection("usuarios").document(userId!!).collection("mascotas").get().addOnSuccessListener { mascotas ->
            if (mascotas.isEmpty) borrarDocumentoYAuth(pDialog)
            else {
                var cont = 0
                for (masc in mascotas) {
                    val mRef = masc.reference
                    listOf("vacunas", "desparasitaciones", "citas").forEach { sub ->
                        mRef.collection(sub).get().addOnSuccessListener { docs -> for (d in docs) d.reference.delete() }
                    }
                    mRef.delete().addOnSuccessListener {
                        cont++
                        if (cont == mascotas.size()) borrarDocumentoYAuth(pDialog)
                    }
                }
            }
        }
    }

    private fun borrarDocumentoYAuth(pDialog: SweetAlertDialog) {
        db.collection("usuarios").document(userId!!).delete().addOnSuccessListener {
            auth.currentUser?.delete()?.addOnCompleteListener {
                pDialog.dismissWithAnimation()
                val intent = Intent(this, InicioActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            }
        }
    }
}