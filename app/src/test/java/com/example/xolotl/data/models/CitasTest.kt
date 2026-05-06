package com.example.xolotl.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class CitasTest {

    @Test
    fun `modelo Citas se inicializa con strings vacios por defecto para compatibilidad con Firebase`() {
        // Al instanciar sin pasar argumentos, todos los valores deben ser ""
        val citaVacia = Citas()

        assertEquals("", citaVacia.idC)
        assertEquals("", citaVacia.servicio)
        assertEquals("", citaVacia.horario)
        assertEquals("", citaVacia.notas)
        assertEquals("", citaVacia.ruacMascota)
        assertEquals("", citaVacia.nombreMascota)
    }

    @Test
    fun `modelo Citas asigna correctamente los valores pasados por parametro`() {
        val citaLlenada = Citas(
            idC = "123",
            servicio = "Consulta General",
            horario = "15/10/2026 14:30",
            notas = "Llevar cartilla",
            ruacMascota = "ABCDE12345",
            nombreMascota = "Max"
        )

        assertEquals("123", citaLlenada.idC)
        assertEquals("Consulta General", citaLlenada.servicio)
        assertEquals("Max", citaLlenada.nombreMascota)
    }
}