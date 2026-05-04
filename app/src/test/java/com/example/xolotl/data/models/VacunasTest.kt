package com.example.xolotl.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class VacunasTest {

    @Test
    fun `modelo Vacunas se inicializa con strings vacios por defecto para Firebase`() {
        // Al instanciar sin pasar argumentos, todos los valores deben ser ""
        val vacunaVacia = Vacunas()

        assertEquals("", vacunaVacia.nombre)
        assertEquals("", vacunaVacia.marca)
        assertEquals("", vacunaVacia.dosis)
        assertEquals("", vacunaVacia.fecha)
        assertEquals("", vacunaVacia.proximaFecha)
        assertEquals("", vacunaVacia.ruacMascota)
    }

    @Test
    fun `modelo Vacunas asigna correctamente los valores pasados por parametro`() {
        val vacunaLlenada = Vacunas(
            nombre = "Antirrábica",
            marca = "Nobivac",
            dosis = "1.0",
            fecha = "10/05/2025",
            proximaFecha = "10/05/2026",
            ruacMascota = "XOLO987654"
        )

        assertEquals("Antirrábica", vacunaLlenada.nombre)
        assertEquals("Nobivac", vacunaLlenada.marca)
        assertEquals("1.0", vacunaLlenada.dosis)
        assertEquals("10/05/2025", vacunaLlenada.fecha)
        assertEquals("10/05/2026", vacunaLlenada.proximaFecha)
        assertEquals("XOLO987654", vacunaLlenada.ruacMascota)
    }
}