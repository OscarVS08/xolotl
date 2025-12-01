package com.example.xolotl.data.repository

import com.example.xolotl.data.models.Mascotas
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class MascotaRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun registrarMascota(
        mascota: Mascotas,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val mascotaRef = db.collection("mascotas").document(mascota.ruac)

        mascotaRef.set(mascota)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun subirFotoMascota(
        idMascota: String,
        bytes: ByteArray,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val ref = storage.reference.child("mascotas/$idMascota.jpg")

        ref.putBytes(bytes)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    onSuccess(url.toString())
                }
            }
            .addOnFailureListener { e -> onError(e) }
    }
}


