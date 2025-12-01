package com.example.xolotl.data.models

data class Mascotas(
    val ruac: String = "",
    val nombre: String = "",
    val fechaAdopcion: String = "",
    val color: String = "",
    val sexo: String = "",        // Hembra / Macho
    val peso: String = "",        // Se recomienda String por Firestore
    val estatura: String = "",
    val alergias: String = "",
    val raza: String = "",
    val especie: String = "",     // Perro / Gato
    val notas: String = "",
    val urlFoto: String = "",     // URL que guardarás en Storage
    val idDueno: String = ""      // UID del usuario dueño
)
