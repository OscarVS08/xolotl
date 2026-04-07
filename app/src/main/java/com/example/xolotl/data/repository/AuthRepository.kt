package com.example.xolotl.data.repository

import com.example.xolotl.data.models.User
import com.example.xolotl.utils.EncryptionUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    // Inicio de sesión
    fun iniciarSesion(
        email: String,
        password: String,
        callback: AuthCallback
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { callback.onSuccess() }
            .addOnFailureListener { e ->
                callback.onError(e.localizedMessage ?: "Error al iniciar sesión")
            }
    }

    // Registro de usuario
    fun registrarUsuario(
        email: String,
        password: String,
        user: User,
        callback: AuthCallback
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid
                if (uid == null) {
                    callback.onError("No se obtuvo UID del usuario")
                    return@addOnSuccessListener
                }

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
                    .addOnSuccessListener { callback.onSuccess() }
                    .addOnFailureListener { e ->
                        callback.onError(e.localizedMessage ?: "Error al guardar datos")
                    }
            }
            .addOnFailureListener { e ->
                callback.onError(e.localizedMessage ?: "Error al registrar usuario")
            }
    }

    // Envío de correo de recuperación
    fun enviarCorreoRecuperacion(
        correo: String,
        callback: AuthCallback
    ) {
        auth.sendPasswordResetEmail(correo)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback.onSuccess()
                } else {
                    callback.onError(task.exception?.message ?: "Error al enviar correo")
                }
            }
    }

    fun obtenerUsuarioActual() = auth.currentUser
}
