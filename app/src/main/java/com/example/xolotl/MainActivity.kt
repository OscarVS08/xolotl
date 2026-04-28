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
import android.app.Dialog
import android.widget.ArrayAdapter
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetSequence

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    //private var menuVisible = false

    // Variables para mostrar las tarjetas de citas
    private lateinit var recyclerCitas: RecyclerView
    private val listaCitas = mutableListOf<Citas>()
    private lateinit var adapter: CitasAdapter
    private var countEasterEgg = 0
    private var lastClickTime: Long = 0
    private val listaCitasCompleta = mutableListOf<Citas>() // Respaldo de TODO
    private var mascotaSeleccionadaFiltro: String = "Todas"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        //cargarNombreUsuario()
        setupMenuButton()
        setupBotonesPrincipales()
        setupCerrarSesion()
        editarMascotas()
        notificacionesUsuario()
        editarPerfilUsuario()
        mostrarMapaNoUrgencias()
        setupEasterEgg()

        // Para las tarjetas de citas
        recyclerCitas = findViewById(R.id.recyclerCitas)
        recyclerCitas.layoutManager = GridLayoutManager(this, 2)
        adapter = CitasAdapter(listaCitas, this)
        recyclerCitas.adapter = adapter

        // ==========================================
        // VALIDAR SI ES LA PRIMERA VEZ EN LA APP
        // ==========================================
        val preferencias = getSharedPreferences("XolotlPrefs", MODE_PRIVATE)
        val esPrimeraVez = preferencias.getBoolean("tourCompletado", true)

        if (esPrimeraVez) {
            // Mostramos el recorrido visual
            mostrarTourVisual()

            // Guardamos que ya lo vio para que no vuelva a salir
            preferencias.edit().putBoolean("tourCompletado", false).apply()
        }

        //cargarCitas()
        // Al tocar el fondo (el ConstraintLayout principal), cerramos menús
        binding.root.setOnClickListener {
            if (binding.cardMenuOpciones.visibility == View.VISIBLE ||
                binding.cardMenuPrincipal.visibility == View.VISIBLE) {
                ocultarMenus()
            }
        }

        // También al tocar el RecyclerView (las citas)
        binding.recyclerCitas.setOnTouchListener { _, _ ->
            ocultarMenus()
            false // false para que el scroll siga funcionando
        }
    }

    override fun onResume() {
        super.onResume()
        cargarNombreUsuario()
        cargarCitas()
        configurarFiltrosUI()
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
    // MENÚ SUPERIOR (HAMBURGUESA)
    // ========================
    private fun setupMenuButton() {
        binding.btnMenu.setOnClickListener {
            // Si el menú principal está abierto, lo cerramos primero
            binding.cardMenuPrincipal.visibility = View.GONE

            // Toggle del menú de opciones
            if (binding.cardMenuOpciones.visibility == View.VISIBLE) {
                binding.cardMenuOpciones.visibility = View.GONE
            } else {
                binding.cardMenuOpciones.visibility = View.VISIBLE
            }
        }
    }

    // ========================
    // BOTÓN CENTRAL Y OPCIONES
    // ========================
// ========================
// BOTÓN CENTRAL (+) Y OPCIONES
// ========================
    private fun setupBotonesPrincipales() {

        // 1. Abrir/cerrar menú del botón + (Lógica de Toggle)
        binding.btnPrincipal.setOnClickListener {
            // Regla de oro: Si el menú de hamburguesa está abierto, lo cerramos
            if (binding.cardMenuOpciones.visibility == View.VISIBLE) {
                binding.cardMenuOpciones.visibility = View.GONE
            }

            // Intercambiamos la visibilidad del menú central
            if (binding.cardMenuPrincipal.visibility == View.VISIBLE) {
                binding.cardMenuPrincipal.visibility = View.GONE
            } else {
                binding.cardMenuPrincipal.visibility = View.VISIBLE
            }
        }

        // 2. Opción: Agregar desparasitación
        binding.btnCentralOpcion1.setOnClickListener {
            binding.cardMenuPrincipal.visibility = View.GONE // Siempre ocultar al picar
            val intent = Intent(this, AgregarDesparasitacionesActivity::class.java)
            startActivity(intent)
        }

        // 3. Opción: Agregar vacunas
        binding.btnCentralOpcion2.setOnClickListener {
            binding.cardMenuPrincipal.visibility = View.GONE
            val intent = Intent(this, AgregarVacunasActivity::class.java)
            startActivity(intent)
        }

        // 4. Opción: Agregar Mascota
        binding.btnCentralOpcion3.setOnClickListener {
            binding.cardMenuPrincipal.visibility = View.GONE
            val intent = Intent(this, AgregarMascotaActivity::class.java)
            startActivity(intent)
        }

        // 5. Opción: Agendar cita
        binding.btnCentralOpcion4.setOnClickListener {
            binding.cardMenuPrincipal.visibility = View.GONE
            val intent = Intent(this, AgregarCitasActivity::class.java)
            startActivity(intent)
        }

        // 6. Botón Emergencia (Mapa Urgencias)
        binding.btnEmergencia.setOnClickListener {
            // Por seguridad, cerramos cualquier menú abierto antes de saltar al mapa
            binding.cardMenuPrincipal.visibility = View.GONE
            binding.cardMenuOpciones.visibility = View.GONE

            val intent = Intent(this, VisualizarMapaUrgenciasActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // OPCIÓN 1 → NOTIFICACIONES
    // ========================
    private fun notificacionesUsuario(){
        binding.btnOpcion1.setOnClickListener {
            binding.cardMenuPrincipal.visibility = View.GONE
            val intent = Intent(this, NotificacionesActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // OPCIÓN 2 → EDITAR PERFIL USUARIO
    // ========================
    private fun editarPerfilUsuario(){
        binding.btnOpcion2.setOnClickListener {
            binding.cardMenuPrincipal.visibility = View.GONE
            val intent = Intent(this, EditarPerfilActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // OPCIÓN 3 → EDITAR PERFIL USUARIO
    // ========================
    private fun mostrarMapaNoUrgencias(){
        binding.btnOpcion3.setOnClickListener {
            binding.cardMenuPrincipal.visibility = View.GONE
            val intent = Intent(this, VisualizarMapaActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // OPCIÓN 4 → EDITAR MASCOTAS
    // ========================
    private fun editarMascotas(){
        binding.btnOpcion4.setOnClickListener {
            binding.cardMenuPrincipal.visibility = View.GONE
            val intent = Intent(this, MascotasActivity::class.java)
            startActivity(intent)
        }
    }

    // ========================
    // OPCIÓN 5 -> CERRAR SESIÓN
    // ========================
    private fun setupCerrarSesion() {
        binding.btnOpcion5.setOnClickListener {
            binding.cardMenuOpciones.visibility = View.GONE
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
        listaCitasCompleta.clear() // Limpiamos el respaldo también

        db.collection("usuarios").document(uid).collection("mascotas").get()
            .addOnSuccessListener { mascotas ->
                if (mascotas.isEmpty) {
                    adapter.notifyDataSetChanged()
                    evaluarVisibilidadFiltro() // <-- CORRECCIÓN: Oculta el filtro si no hay mascotas
                    return@addOnSuccessListener
                }

                var consultasPendientes = mascotas.size()

                for (mascota in mascotas) {
                    val ruac = mascota.id
                    val nombreMascota = EncryptionUtils.decrypt(mascota.getString("nombre") ?: "")

                    db.collection("usuarios").document(uid).collection("mascotas")
                        .document(ruac).collection("citas").get()
                        .addOnSuccessListener { citas ->
                            for (doc in citas) {
                                val servicio = EncryptionUtils.decrypt(doc.getString("servicio") ?: "")
                                val fecha = EncryptionUtils.decrypt(doc.getString("horario") ?: "")

                                val nuevaCita = Citas(
                                    idC = doc.id,
                                    servicio = servicio,
                                    horario = fecha,
                                    notas = "",
                                    ruacMascota = ruac,
                                    nombreMascota = nombreMascota
                                )
                                listaCitasCompleta.add(nuevaCita)
                            }

                            consultasPendientes--

                            if (consultasPendientes == 0) {
                                evaluarVisibilidadFiltro() // <-- CORRECCIÓN: Evalúa cada vez que termina
                                actualizarSpinnerMascotas()
                                aplicarFiltrosYOrden()
                            }
                        }
                        .addOnFailureListener {
                            consultasPendientes--
                            // Por seguridad, si falla una consulta pero terminan las demás
                            if (consultasPendientes == 0) {
                                evaluarVisibilidadFiltro()
                                actualizarSpinnerMascotas()
                                aplicarFiltrosYOrden()
                            }
                        }
                }
            }
    }

    private fun actualizarSpinnerMascotas() {
        val nombres = listaCitasCompleta.map { it.nombreMascota }.distinct().toMutableList()
        nombres.add(0, "Todas")

        // Usamos un layout base de Android para que el texto tenga más espacio al desplegarse
        val adapterFiltro = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, nombres)
        binding.spinnerFiltroMascota.setAdapter(adapterFiltro)
    }

    private fun ocultarMenus() {
        binding.cardMenuOpciones.visibility = View.GONE
        binding.cardMenuPrincipal.visibility = View.GONE
    }

    private fun setupEasterEgg() {
        binding.cardHeader.setOnClickListener {
            val currentTime = System.currentTimeMillis()

            // Si pasan más de 2 segundos entre clics, reiniciamos el contador
            if (currentTime - lastClickTime > 2000) {
                countEasterEgg = 0
            }

            lastClickTime = currentTime
            countEasterEgg++

            // Log opcional para que veas en consola cuántos llevas
            // Log.d("EasterEgg", "Clic número: $countEasterEgg")

            if (countEasterEgg == 5) {
                mostrarDedicatoria()
                countEasterEgg = 0 // Reiniciar tras el éxito
            }
        }
    }

    private fun mostrarDedicatoria() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_agradecimientos)

        // Animación suave de entrada/salida
        dialog.window?.attributes?.windowAnimations = android.R.style.Animation_Dialog

        // Fondo transparente para respetar los bordes redondeados del XML
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // CAMBIO CLAVE: Usamos View en lugar de Button para evitar el ClassCastException
        val btnCerrar = dialog.findViewById<View>(R.id.btnCerrarEasterEggContainer)

        btnCerrar?.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun aplicarFiltrosYOrden() {
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())

        // 1. Filtrar por Mascota
        val listaFiltrada = if (mascotaSeleccionadaFiltro == "Todas" || mascotaSeleccionadaFiltro == "Mascota" || mascotaSeleccionadaFiltro.isEmpty()) {
            listaCitasCompleta.toMutableList()
        } else {
            listaCitasCompleta.filter { it.nombreMascota == mascotaSeleccionadaFiltro }.toMutableList()
        }

        // 2. Ordenar por Fecha (Recuperamos ambas funciones)
        if (binding.chipProximas.isChecked) {
            // Ascendente: las fechas más cercanas
            listaFiltrada.sortBy {
                try { sdf.parse(it.horario)?.time } catch (e: Exception) { Long.MAX_VALUE }
            }
        } else if (binding.chipLejanas.isChecked) {
            // Descendente: las fechas más lejanas
            listaFiltrada.sortByDescending {
                try { sdf.parse(it.horario)?.time } catch (e: Exception) { 0L }
            }
        }

        // 3. Actualizar la lista que ve el RecyclerView
        listaCitas.clear()
        listaCitas.addAll(listaFiltrada)
        adapter.notifyDataSetChanged()
    }

    private fun configurarFiltrosUI() {
        binding.spinnerFiltroMascota.setOnItemClickListener { parent, _, position, _ ->
            mascotaSeleccionadaFiltro = parent.getItemAtPosition(position).toString()
            aplicarFiltrosYOrden()
        }

        binding.chipGroupOrden.setOnCheckedChangeListener { _, _ ->
            aplicarFiltrosYOrden()
        }
    }

    private fun evaluarVisibilidadFiltro() {
        if (listaCitasCompleta.size > 4) {
            binding.cardFiltros.visibility = View.VISIBLE
        } else {
            binding.cardFiltros.visibility = View.GONE

            // Si el filtro se oculta, nos aseguramos de resetearlo internamente
            // para que no se quede "atrapado" mostrando solo una mascota
            mascotaSeleccionadaFiltro = "Todas"
            binding.spinnerFiltroMascota.setText("Todas", false)
        }
    }

    fun recargarCitasDesdeAdapter() {
        // Al llamar a cargarCitas de nuevo, se vuelve a descargar la lista actualizada
        // y se ejecuta evaluarVisibilidadFiltro() automáticamente.
        cargarCitas()
    }

    // ==========================================
    // TOUR VISUAL (ONBOARDING)
    // ==========================================
    private fun mostrarTourVisual() {
        TapTargetSequence(this)
            .targets(
                // 1. Botón Central (Acciones Rápidas)
                TapTarget.forView(binding.btnPrincipal, "Acciones Rápidas", "Toca aquí para agendar citas, agregar mascotas, vacunas y desparasitaciones.")
                    .outerCircleColor(R.color.rectanguloLogo) // Usa el color amarillo/naranja de tu logo
                    .outerCircleAlpha(0.96f)
                    .targetCircleColor(android.R.color.white)
                    .titleTextSize(20)
                    .titleTextColor(android.R.color.black)
                    .descriptionTextSize(14)
                    .descriptionTextColor(android.R.color.black)
                    .cancelable(false) // Falso para obligar al usuario a interactuar
                    .tintTarget(false),

                // 2. Botón de Emergencias
                TapTarget.forView(binding.btnEmergencia, "Urgencias 24/7", "Encuentra clínicas veterinarias de emergencia abiertas cerca de ti.")
                    .outerCircleColor(R.color.botonEmergencia) // Usa el rojo de tu botón de emergencia
                    .outerCircleAlpha(0.96f)
                    .targetCircleColor(android.R.color.white)
                    .titleTextSize(20)
                    .titleTextColor(android.R.color.white)
                    .descriptionTextSize(14)
                    .descriptionTextColor(android.R.color.white)
                    .cancelable(false)
                    .tintTarget(false),

                // 3. Botón de Menú/Perfil
                TapTarget.forView(binding.btnMenu, "Tu Perfil y Ajustes", "Accede a tus notificaciones, edita tu perfil y administra tu cuenta desde aquí.")
                    .outerCircleColor(R.color.moradoSubtitulo) // Usa el morado de tu app
                    .outerCircleAlpha(0.96f)
                    .targetCircleColor(android.R.color.white)
                    .titleTextSize(20)
                    .titleTextColor(android.R.color.white)
                    .descriptionTextSize(14)
                    .descriptionTextColor(android.R.color.white)
                    .cancelable(false)
                    .tintTarget(false)
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    // Qué hacer cuando termine el tour
                    Toast.makeText(this@MainActivity, "¡Estás listo para usar Xólotl!", Toast.LENGTH_SHORT).show()
                }

                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {
                    // Acciones entre pasos (opcional, lo dejamos vacío)
                }

                override fun onSequenceCanceled(lastTarget: TapTarget?) {
                    // Acciones si se cancela el tour
                }
            })
            .start()
    }
}
