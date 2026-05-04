package com.example.xolotl.data.repository

import com.example.xolotl.data.models.Citas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

// INYECCIÓN: Agregamos Firebase al constructor con sus valores por defecto
class CitasRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

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
        val uid = auth.currentUser?.uid
        // CORRECCIÓN: Ahora notificamos el error en lugar de un return silencioso
        if (uid == null) {
            onError(Exception("Usuario no autenticado"))
            return
        }

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .document(ruacMascota)
            .collection("citas")
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Mapeamos los documentos a la lista
                val lista = querySnapshot.map { doc -> doc.toObject(Citas::class.java) }
                onSuccess(lista)
            }
            .addOnFailureListener { onError(it) }
    }
}