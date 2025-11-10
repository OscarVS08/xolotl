package com.example.xolotl

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.databinding.ActivityMainBinding
import com.example.xolotl.ui.auth.InicioActivity
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var menuVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupMenuButton()
        setupBotonesPrincipales()
        setupCerrarSesion()
    }

    private fun setupMenuButton() {
        binding.btnMenu.setOnClickListener {
            menuVisible = !menuVisible
            binding.layoutMenuOpciones.visibility = if (menuVisible) View.VISIBLE else View.GONE
        }
    }

    private fun setupBotonesPrincipales() {
        binding.btnPrincipal.setOnClickListener {
            UiUtils.showToast(this, "Botón principal presionado")
        }

        binding.btnEmergencia.setOnClickListener {
            UiUtils.showToast(this, "Botón de emergencia (sin funcionalidad aún)")
        }
    }

    private fun setupCerrarSesion() {
        binding.btnOpcion5.setOnClickListener {
            binding.layoutMenuOpciones.visibility = View.GONE
            mostrarAlertaCerrarSesion()
        }
    }

    private fun mostrarAlertaCerrarSesion() {
        UiUtils.mostrarAlertaCerrarSesion(
            activity = this,
            titulo = "Cerrar sesión",
            mensaje = "¿Estás seguro de que quieres cerrar sesión?",
            tipo = SweetAlertDialog.WARNING_TYPE,
            confirmText = "Sí, salir",
            cancelText = "Cancelar",
            onConfirm = {
                auth.signOut()
                irAInicio()
            },
            onCancel = { /* solo cerrar alerta */ }
        )
    }

    private fun irAInicio() {
        val intent = Intent(this, InicioActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
