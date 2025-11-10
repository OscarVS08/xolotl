package com.example.xolotl.data.repository

import com.example.xolotl.data.models.User
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun iniciarSesion(email: String, password: String) =
        auth.signInWithEmailAndPassword(email, password)

    fun registrarUsuario(
        email: String,
        password: String,
        user: User,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener onError("No se obtuvo UID")

                val encryptedData = hashMapOf(
                    "uid" to uid,
                    "curp" to EncryptionUtils.encrypt(user.curp),
                    "nombre" to EncryptionUtils.encrypt(user.nombre),
                    "apellidoP" to EncryptionUtils.encrypt(user.apellidoP),
                    "apellidoM" to EncryptionUtils.encrypt(user.apellidoM),
                    "telefono" to EncryptionUtils.encrypt(user.telefono),
                    "telefonoAlt" to EncryptionUtils.encrypt(user.telefonoAlt),
                    "calle" to EncryptionUtils.encrypt(user.calle),
                    "numero" to EncryptionUtils.encrypt(user.numero),
                    "colonia" to EncryptionUtils.encrypt(user.colonia),
                    "alcaldia" to EncryptionUtils.encrypt(user.alcaldia),
                    "codigoPostal" to EncryptionUtils.encrypt(user.codigoPostal),
                    "correo" to EncryptionUtils.encrypt(user.correo)
                )

                db.collection("usuarios").document(uid)
                    .set(encryptedData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onError(it.localizedMessage ?: "Error al guardar datos") }
            }
            .addOnFailureListener { onError(it.localizedMessage ?: "Error al registrar usuario") }
    }

    fun enviarCorreoRecuperacion(
        correo: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(correo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Error al enviar correo")
                }
            }
    }

    fun obtenerUsuarioActual() = auth.currentUser
}
