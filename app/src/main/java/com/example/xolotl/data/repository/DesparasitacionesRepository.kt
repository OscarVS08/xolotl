package com.example.xolotl.data.repository

import com.example.xolotl.data.models.Desparasitaciones
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DesparasitacionRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun registrarDesparasitacion(
        ruacMascota: String,
        desparasitacion: Desparasitaciones,
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
            .collection("desparasitaciones")
            .document()

        ref.set(desparasitacion)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun obtenerDesparasitaciones(
        ruacMascota: String,
        onSuccess: (List<Desparasitaciones>) -> Unit,
        onError: (Exception) -> Unit
    ) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuario no autenticado"))
            return
        }

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .document(ruacMascota)
            .collection("desparasitaciones")
            .get()
            .addOnSuccessListener { result ->

                val lista = result.map {
                    it.toObject(Desparasitaciones::class.java)
                }

                onSuccess(lista)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun eliminarDesparasitacion(
        ruacMascota: String,
        id: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {

        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError(Exception("Usuario no autenticado"))
            return
        }

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .document(ruacMascota)
            .collection("desparasitaciones")
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }
}