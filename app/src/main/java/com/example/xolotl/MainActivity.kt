package com.example.xolotl

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.data.models.Citas
import com.example.xolotl.databinding.ActivityMainBinding
import com.example.xolotl.ui.auth.InicioActivity
import com.example.xolotl.ui.main.mascota.AgregarDesparasitacionesActivity
import com.example.xolotl.ui.main.mascota.AgregarMascotaActivity
import com.example.xolotl.ui.main.mascota.AgregarVacunasActivity
import com.example.xolotl.ui.main.mascota.MascotasActivity
import com.example.xolotl.ui.main.usuario.AgregarCitasActivity
import com.example.xolotl.ui.main.usuario.CitasAdapter
import com.example.xolotl.ui.main.usuario.EditarPerfilActivity
import com.example.xolotl.ui.main.usuario.NotificacionesActivity
import com.example.xolotl.ui.main.usuario.mapas.VisualizarMapaActivity
import com.example.xolotl.ui.main.usuario.mapas.VisualizarMapaUrgenciasActivity
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.xolotl.utils.EncryptionUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private var menuVisible = false

    // Variables para mostrar las tarjetas de citas
    private lateinit var recyclerCitas: RecyclerView
    private val listaCitas = mutableListOf<Citas>()
    private lateinit var adapter: CitasAdapter


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
        notificacionesUsuario()
        editarPerfilUsuario()
        mostrarMapaNoUrgencias()

        // Para las tarjetas de citas
        recyclerCitas = findViewById(R.id.recyclerCitas)
        recyclerCitas.layoutManager = GridLayoutManager(this, 2)

        adapter = CitasAdapter(listaCitas, this)
        recyclerCitas.adapter = adapter

        //cargarCitas()
    }

    override fun onResume() {
        super.onResume()
        cargarCitas()
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

        // Opción 1 → Ir a Agregar desparasitaciones
        binding.btnCentralOpcion1.setOnClickListener {
            // Ocultamos el menú para evitar overlays que causan crashes
            binding.layoutMenuPrincipal.visibility = View.GONE

            val intent = Intent(this, AgregarDesparasitacionesActivity::class.java)
            startActivity(intent)
        }

        // Opción 2 → Ir a Agregar vacunas
        binding.btnCentralOpcion2.setOnClickListener {
            // Ocultamos el menú para evitar overlays que causan crashes
            binding.layoutMenuPrincipal.visibility = View.GONE

            val intent = Intent(this, AgregarVacunasActivity::class.java)
            startActivity(intent)
        }

        // Opción 3 → Ir a Agregar Mascota
        binding.btnCentralOpcion3.setOnClickListener {
            // Ocultamos el menú para evitar overlays que causan crashes
            binding.layoutMenuPrincipal.visibility = View.GONE

            val intent = Intent(this, AgregarMascotaActivity::class.java)
            startActivity(intent)
        }

        // Opción 4 → Ir a Agregar citas
        binding.btnCentralOpcion4.setOnClickListener {
            // Ocultamos el menú para evitar overlays que causan crashes
            binding.layoutMenuPrincipal.visibility = View.GONE

            val intent = Intent(this, AgregarCitasActivity::class.java)
            startActivity(intent)
        }

        // Botón emergencia
        binding.btnEmergencia.setOnClickListener {
            binding.layoutMenuOpciones.visibility = View.GONE
            val intent = Intent(this, VisualizarMapaUrgenciasActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // OPCIÓN 1 → NOTIFICACIONES
    // ========================
    private fun notificacionesUsuario(){
        binding.btnOpcion1.setOnClickListener {
            binding.layoutMenuOpciones.visibility = View.GONE
            val intent = Intent(this, NotificacionesActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // OPCIÓN 2 → EDITAR PERFIL USUARIO
    // ========================
    private fun editarPerfilUsuario(){
        binding.btnOpcion2.setOnClickListener {
            binding.layoutMenuOpciones.visibility = View.GONE
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // OPCIÓN 3 → EDITAR PERFIL USUARIO
    // ========================
    private fun mostrarMapaNoUrgencias(){
        binding.btnOpcion3.setOnClickListener {
            binding.layoutMenuOpciones.visibility = View.GONE
            val intent = Intent(this, VisualizarMapaActivity::class.java)
            startActivity(intent)
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
    // OPCIÓN 5 -> CERRAR SESIÓN
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

    // Funcion para mostrar las citas
    private fun cargarCitas() {

        val uid = auth.currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        listaCitas.clear()

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { mascotas ->

                if (mascotas.isEmpty) {
                    // recyclerCitas.adapter = CitasAdapter(listaCitas)
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                var consultasPendientes = mascotas.size()

                for (mascota in mascotas) {

                    val ruac = mascota.id
                    val nombreMascota = EncryptionUtils.decrypt(mascota.getString("nombre") ?: "")

                    db.collection("usuarios")
                        .document(uid)
                        .collection("mascotas")
                        .document(ruac)
                        .collection("citas")
                        .get()
                        .addOnSuccessListener { citas ->

                            for (doc in citas) {

                                val servicio = EncryptionUtils.decrypt(doc.getString("servicio") ?: "")
                                val fecha = EncryptionUtils.decrypt(doc.getString("horario") ?: "")

                                listaCitas.add(
                                    Citas(
                                        idC = doc.id,
                                        servicio = servicio,
                                        horario = fecha,
                                        notas = "",
                                        ruacMascota = ruac,
                                        nombreMascota = nombreMascota
                                    )
                                )
                            }

                            consultasPendientes--

                            // SOLO AQUÍ actualizamos
                            if (consultasPendientes == 0) {
                                Toast.makeText(this, "Citas cargadas: ${listaCitas.size}", Toast.LENGTH_SHORT).show()
                                recyclerCitas.adapter = CitasAdapter(listaCitas, this)
                            }
                        }
                        .addOnFailureListener {
                            consultasPendientes--
                        }
                }
            }
    }
}
