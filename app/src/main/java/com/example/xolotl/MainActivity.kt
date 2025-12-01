package com.example.xolotl

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.databinding.ActivityMainBinding
import com.example.xolotl.ui.auth.InicioActivity
import com.example.xolotl.ui.main.mascota.AgregarMascotaActivity
import com.example.xolotl.ui.main.mascota.MascotasActivity
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.xolotl.utils.EncryptionUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var menuVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        cargarNombreUsuario()
        setupMenuButton()
        setupBotonesPrincipales()
        setupCerrarSesion()
        editarMascotas()
    }

    // ========================
    // CARGAR NOMBRE DEL USUARIO
    // ========================
    private fun cargarNombreUsuario() {
        val uid = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("usuarios").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {

                    val nombreCifrado = doc.getString("nombre") ?: ""
                    val apellidoPCifrado = doc.getString("apellidoP") ?: ""

                    val nombre = EncryptionUtils.decrypt(nombreCifrado)
                    val apellidoP = EncryptionUtils.decrypt(apellidoPCifrado)

                    binding.txtTituloLogo.text = "Hola $nombre $apellidoP"
                }
            }
    }

    // ========================
    // MENÚ SUPERIOR DE OPCIONES
    // ========================
    private fun setupMenuButton() {
        binding.btnMenu.setOnClickListener {
            menuVisible = !menuVisible
            binding.layoutMenuOpciones.visibility =
                if (menuVisible) View.VISIBLE else View.GONE
        }
    }

    // ========================
    // BOTÓN CENTRAL Y OPCIONES
    // ========================
    private fun setupBotonesPrincipales() {

        // Abrir/cerrar menú del botón +
        binding.btnPrincipal.setOnClickListener {
            binding.layoutMenuPrincipal.visibility =
                if (binding.layoutMenuPrincipal.visibility == View.GONE)
                    View.VISIBLE
                else
                    View.GONE
        }

        // Opción 1 → Ir a Agregar Mascota
        binding.btnCentralOpcion1.setOnClickListener {
            // Ocultamos el menú para evitar overlays que causan crashes
            binding.layoutMenuPrincipal.visibility = View.GONE

            val intent = Intent(this, AgregarMascotaActivity::class.java)
            startActivity(intent)
        }

        // Botón emergencia
        binding.btnEmergencia.setOnClickListener {
            UiUtils.showToast(this, "Botón de emergencia (sin funcionalidad aún)")
        }
    }


    // ========================
    // OPCIÓN 4 → EDITAR MASCOTAS
    // ========================
    private fun editarMascotas(){
        binding.btnOpcion4.setOnClickListener {
            binding.layoutMenuOpciones.visibility = View.GONE
            val intent = Intent(this, MascotasActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // CERRAR SESIÓN
    // ========================
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
            onCancel = { }
        )
    }

    private fun irAInicio() {
        val intent = Intent(this, InicioActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
