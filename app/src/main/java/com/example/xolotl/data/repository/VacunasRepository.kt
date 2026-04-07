package com.example.xolotl.data.repository


import com.example.xolotl.data.models.Vacunas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VacunasRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ============================
    // REGISTRAR VACUNA
    // ============================
    fun registrarVacuna(
        ruacMascota: String,
        vacuna: Vacunas,
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
            .collection("vacunas")
            .document()

        ref.set(vacuna)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    // ============================
    // OBTENER VACUNAS
    // ============================
    fun obtenerVacunas(
        ruacMascota: String,
        onSuccess: (List<Vacunas>) -> Unit,
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
            .collection("vacunas")
            .get()
            .addOnSuccessListener { result ->

                val lista = result.map {
                    it.toObject(Vacunas::class.java)
                }

                onSuccess(lista)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    // ============================
    // ELIMINAR VACUNA
    // ============================
    fun eliminarVacuna(
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
            .collection("vacunas")
            .document(id)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }
}