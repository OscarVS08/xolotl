package com.example.xolotl.ui.main.mascota

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.data.models.Mascotas
import com.example.xolotl.databinding.ActivityMascotasBinding
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MascotasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMascotasBinding
    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMascotasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Usamos ViewBinding para los elementos principales
        cargarMascotas()

        binding.btnHome.setOnClickListener { finish() }
    }

    // PUNTO 3: Actualizar al regresar
    // onResume se ejecuta cada vez que la pantalla vuelve a estar al frente
    override fun onResume() {
        super.onResume()
        cargarMascotas()
    }

    private fun cargarMascotas() {
        val uid = userId ?: return

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { documents ->
                binding.containerMascotas.removeAllViews() // Limpiamos para evitar duplicados al recargar

                if (documents.isEmpty) {
                    binding.txtSinMascotas.visibility = View.VISIBLE
                    binding.scrollMascotas.visibility = View.GONE
                } else {
                    binding.txtSinMascotas.visibility = View.GONE
                    binding.scrollMascotas.visibility = View.VISIBLE

                    for (doc in documents) {
                        val mascota = doc.toObject(Mascotas::class.java)
                        agregarTarjetaMascota(mascota, doc.id)
                    }
                }
            }
            .addOnFailureListener { e ->
                binding.txtSinMascotas.text = "Error al cargar mascotas"
                binding.txtSinMascotas.visibility = View.VISIBLE
            }
    }

    private fun agregarTarjetaMascota(mascota: Mascotas, docId: String) {
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.item_mascota, binding.containerMascotas, false)

        val txtNombre = itemView.findViewById<TextView>(R.id.txtNombreMascotaItem)
        val imgMascota = itemView.findViewById<ImageView>(R.id.imgMascota)
        val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarMascota)
        val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditarMascota)
        val btnPdf = itemView.findViewById<ImageButton>(R.id.btnPdfMascota)

        txtNombre.text = EncryptionUtils.decrypt(mascota.nombre)

        // PUNTO 1: Lógica híbrida para fotos (Cifrada o Base64 directo)
        val fotoData = mascota.fotoBase64
        if (fotoData.isNotEmpty()) {
            try {
                // Intentamos descifrar, si falla asumimos que es Base64 puro
                val base64Limpio = try {
                    EncryptionUtils.decrypt(fotoData)
                } catch (e: Exception) {
                    fotoData
                }

                val bytes = android.util.Base64.decode(base64Limpio, android.util.Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgMascota.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imgMascota.setImageResource(R.drawable.foto_blanco)
            }
        }

        // Listeners
        btnEliminar.setOnClickListener {
            confirmarEliminacion(docId, txtNombre.text.toString())
        }

        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarMascotasActivity::class.java)
            intent.putExtra("docId", docId)
            startActivity(intent)
        }

        btnPdf.setOnClickListener {
            val intent = Intent(this, GenerarPdfActivity::class.java)
            intent.putExtra("docId", docId)
            startActivity(intent)
        }

        binding.containerMascotas.addView(itemView)
    }

    // PUNTO 2: Confirmación y Eliminación Recursiva
    private fun confirmarEliminacion(docId: String, nombre: String) {
        UiUtils.mostrarAlertaCerrarSesion(
            this,
            "¿Eliminar a $nombre?",
            "Esta acción borrará permanentemente a la mascota, sus vacunas, citas y desparasitaciones.",
            SweetAlertDialog.WARNING_TYPE,
            "Eliminar",
            "Cancelar",
            onConfirm = {
                eliminarTodoRastroMascota(docId)
            }
        )
    }

    private fun eliminarTodoRastroMascota(docId: String) {
        val uid = userId ?: return
        val mascotaRef = db.collection("usuarios").document(uid).collection("mascotas").document(docId)

        // En Firestore, borrar un documento NO borra sus subcolecciones automáticamente.
        // Hay que borrar las subcolecciones primero o al mismo tiempo.
        val subcolecciones = listOf("vacunas", "desparasitaciones", "citas")

        val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        pDialog.titleText = "Eliminando..."
        pDialog.show()

        var completados = 0
        for (sub in subcolecciones) {
            mascotaRef.collection(sub).get().addOnSuccessListener { docs ->
                for (d in docs) { d.reference.delete() }
                completados++
                if (completados == subcolecciones.size) {
                    // Una vez borradas las subcolecciones, borramos la mascota
                    mascotaRef.delete().addOnSuccessListener {
                        pDialog.dismissWithAnimation()
                        UiUtils.mostrarAlerta(this, "Eliminado", "La mascota y sus datos han sido borrados", SweetAlertDialog.SUCCESS_TYPE)
                        cargarMascotas() // Recargar lista
                    }
                }
            }
        }
    }
}