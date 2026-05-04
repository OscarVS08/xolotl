package com.example.xolotl.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class DesparasitacionesTest {

    @Test
    fun `modelo Desparasitaciones se inicializa con strings vacios por defecto para Firebase`() {
        // Al instanciar sin pasar argumentos, todos los valores deben ser ""
        val desparasitacionVacia = Desparasitaciones()

        assertEquals("", desparasitacionVacia.tipo)
        assertEquals("", desparasitacionVacia.nombre)
        assertEquals("", desparasitacionVacia.marca)
        assertEquals("", desparasitacionVacia.fecha)
        assertEquals("", desparasitacionVacia.proximaFecha)
        assertEquals("", desparasitacionVacia.ruacMascota)
    }

    @Test
    fun `modelo Desparasitaciones asigna correctamente los valores pasados por parametro`() {
        val desparasitacionLlenada = Desparasitaciones(
            tipo = "Interna",
            nombre = "Bravecto",
            marca = "MSD Animal Health",
            fecha = "01/04/2026",
            proximaFecha = "01/07/2026",
            ruacMascota = "XOLO123456"
        )

        assertEquals("Interna", desparasitacionLlenada.tipo)
        assertEquals("Bravecto", desparasitacionLlenada.nombre)
        assertEquals("MSD Animal Health", desparasitacionLlenada.marca)
        assertEquals("01/04/2026", desparasitacionLlenada.fecha)
        assertEquals("01/07/2026", desparasitacionLlenada.proximaFecha)
        assertEquals("XOLO123456", desparasitacionLlenada.ruacMascota)
    }
}