package com.example.xolotl.data.repository

import com.example.xolotl.data.models.Citas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CitasRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun registrarCita(
        ruacMascota: String,
        cita: Citas,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuario no autenticado"))
            return
        }

        val ref = db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .document(ruacMascota)
            .collection("citas")
            .document()

        ref.set(cita)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun obtenerCitas(
        ruacMascota: String,
        onSuccess: (List<Citas>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val uid = auth.currentUser?.uid ?: return

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .document(ruacMascota)
            .collection("citas")
            .get()
            .addOnSuccessListener {
                val lista = it.map { doc -> doc.toObject(Citas::class.java) }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }
}