package com.example.xolotl.ui.main.mascota

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.R
import com.example.xolotl.data.models.Mascotas
import com.example.xolotl.databinding.ActivityMascotasBinding
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class MascotasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMascotasBinding
    private lateinit var containerMascotas: LinearLayout
    private lateinit var txtSinMascotas: TextView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mascotas)

        containerMascotas = findViewById(R.id.containerMascotas)
        txtSinMascotas = findViewById(R.id.txtSinMascotas)

        cargarMascotas()

        // Botón Home
        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }
    }

    private fun cargarMascotas() {
        val userId = FirebaseAuth.getInstance().uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    txtSinMascotas.visibility = View.VISIBLE
                    containerMascotas.visibility = View.GONE
                } else {
                    txtSinMascotas.visibility = View.GONE
                    containerMascotas.visibility = View.VISIBLE

                    for (doc in documents) {
                        val mascota = doc.toObject(Mascotas::class.java)
                        agregarTarjetaMascota(mascota, doc.id)
                    }
                }
            }
            .addOnFailureListener { e ->
                txtSinMascotas.visibility = View.VISIBLE
                containerMascotas.visibility = View.GONE
                txtSinMascotas.text = "Error al cargar mascotas"
                e.printStackTrace()
            }
    }

    private fun agregarTarjetaMascota(mascota: Mascotas, docId: String) {
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.item_mascota, containerMascotas, false)

        val txtNombre = itemView.findViewById<TextView>(R.id.txtNombreMascotaItem)
        val imgMascota = itemView.findViewById<ImageView>(R.id.imgMascota)
        val btnEliminar = itemView.findViewById<ImageButton>(R.id.btnEliminarMascota)
        val btnEditar = itemView.findViewById<ImageButton>(R.id.btnEditarMascota)
        val btnPdf = itemView.findViewById<ImageButton>(R.id.btnPdfMascota)

        // Desencriptar los datos antes de mostrarlos
        txtNombre.text = EncryptionUtils.decrypt(mascota.nombre)

        // FOTO EN BASE64 CIFRADA
        val fotoCifrada = mascota.fotoBase64

        if (fotoCifrada.isNotEmpty()) {
            try {
                // 1. Desencriptar
                val fotoBase64 = EncryptionUtils.decrypt(fotoCifrada)

                // 2. Decodificar Base64 → bytes
                val bytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)

                // 3. Convertir a Bitmap
                val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                imgMascota.setImageBitmap(bitmap)

            } catch (e: Exception) {
                e.printStackTrace()
                imgMascota.setImageResource(R.drawable.fondo_logo_circular)
            }
        } else {
            imgMascota.setImageResource(R.drawable.fondo_logo_circular)
        }

        // Listeners de botones
        btnEliminar.setOnClickListener {
            eliminarMascota(docId)
        }

        btnEditar.setOnClickListener {
            val intent = Intent(this, EditarMascotasActivity::class.java)

            // Enviar el ID de la mascota
            intent.putExtra("docId", docId)

            // Si quieres enviar datos de la mascota desencriptados, también puedes:
            intent.putExtra("nombreMascota", EncryptionUtils.decrypt(mascota.nombre))

            startActivity(intent)
        }

        btnPdf.setOnClickListener {
            val intent = Intent(this, GenerarPdfActivity::class.java)

            // Enviar el ID de la mascota
            intent.putExtra("docId", docId)

            // Si quieres enviar datos de la mascota desencriptados, también puedes:
            intent.putExtra("nombreMascota", EncryptionUtils.decrypt(mascota.nombre))

            startActivity(intent)
        }


        containerMascotas.addView(itemView)
    }

    private fun eliminarMascota(docId: String) {
        val userId = FirebaseAuth.getInstance().uid ?: return

        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .document(docId)
            .delete()
            .addOnSuccessListener {
                containerMascotas.removeAllViews()
                cargarMascotas()
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}
