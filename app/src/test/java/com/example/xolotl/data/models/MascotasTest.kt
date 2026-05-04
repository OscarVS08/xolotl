package com.example.xolotl.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class MascotasTest {

    @Test
    fun `modelo Mascotas se inicializa con strings vacios por defecto para Firebase`() {
        // Al instanciar sin pasar argumentos, todos los valores deben ser ""
        val mascotaVacia = Mascotas()

        assertEquals("", mascotaVacia.ruac)
        assertEquals("", mascotaVacia.nombre)
        assertEquals("", mascotaVacia.fechaNacimiento)
        assertEquals("", mascotaVacia.fechaAdopcion)
        assertEquals("", mascotaVacia.color)
        assertEquals("", mascotaVacia.sexo)
        assertEquals("", mascotaVacia.peso)
        assertEquals("", mascotaVacia.estatura)
        assertEquals("", mascotaVacia.alergias)
        assertEquals("", mascotaVacia.raza)
        assertEquals("", mascotaVacia.especie)
        assertEquals("", mascotaVacia.notas)
        assertEquals("", mascotaVacia.fotoBase64)
        assertEquals("", mascotaVacia.idDueno)
    }

    @Test
    fun `modelo Mascotas asigna correctamente los valores pasados por parametro`() {
        val mascotaLlenada = Mascotas(
            ruac = "ABCD123456",
            nombre = "Solovino",
            fechaNacimiento = "01/01/2022",
            fechaAdopcion = "15/06/2022",
            color = "Cafe oscuro",
            sexo = "Macho",
            peso = "12.5",
            estatura = "45.0",
            alergias = "Pollo",
            raza = "Mestizo",
            especie = "Perro",
            notas = "Rescatado, le teme a la lluvia",
            fotoBase64 = "data:image/jpeg;base64,/9j/4AAQSkZJ...",
            idDueno = "UID987654321"
        )

        assertEquals("ABCD123456", mascotaLlenada.ruac)
        assertEquals("Solovino", mascotaLlenada.nombre)
        assertEquals("01/01/2022", mascotaLlenada.fechaNacimiento)
        assertEquals("15/06/2022", mascotaLlenada.fechaAdopcion)
        assertEquals("Cafe oscuro", mascotaLlenada.color)
        assertEquals("Macho", mascotaLlenada.sexo)
        assertEquals("12.5", mascotaLlenada.peso)
        assertEquals("45.0", mascotaLlenada.estatura)
        assertEquals("Pollo", mascotaLlenada.alergias)
        assertEquals("Mestizo", mascotaLlenada.raza)
        assertEquals("Perro", mascotaLlenada.especie)
        assertEquals("Rescatado, le teme a la lluvia", mascotaLlenada.notas)
        assertEquals("data:image/jpeg;base64,/9j/4AAQSkZJ...", mascotaLlenada.fotoBase64)
        assertEquals("UID987654321", mascotaLlenada.idDueno)
    }
}