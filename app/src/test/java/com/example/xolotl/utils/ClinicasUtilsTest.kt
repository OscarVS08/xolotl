package com.example.xolotl.utils

import org.junit.Assert.*
import org.junit.Test

class ClinicasUtilsTest {

    @Test
    fun `clinicasNormal devuelve la lista completa y con datos validos`() {
        val lista = ClinicasUtils.clinicasNormal()

        // 1. Verificar que la lista no sea nula ni esté vacía
        assertNotNull(lista)
        assertTrue("La lista de clínicas normales no debería estar vacía", lista.isNotEmpty())

        // 2. Verificar la cantidad exacta de clínicas (tienes 16 en el código)
        assertEquals(16, lista.size)

        // 3. Validar que ninguna clínica tenga datos vacíos o coordenadas imposibles
        lista.forEach { (posicion, titulo, etiqueta) ->
            assertTrue("El título no debe estar vacío", titulo.isNotBlank())
            assertTrue("La etiqueta no debe estar vacía", etiqueta.isNotBlank())

            // Latitud debe estar entre -90 y 90, Longitud entre -180 y 180
            assertTrue("Latitud fuera de rango en $titulo", posicion.latitude in -90.0..90.0)
            assertTrue("Longitud fuera de rango en $titulo", posicion.longitude in -180.0..180.0)
        }
    }

    @Test
    fun `clinicasEmergencia devuelve la lista completa de hospitales 24 horas`() {
        val lista = ClinicasUtils.clinicasEmergencia()

        // 1. Verificar integridad
        assertNotNull(lista)
        assertTrue(lista.isNotEmpty())

        // 2. Verificar cantidad (tienes 19 en el código)
        assertEquals(19, lista.size)

        // 3. Caso de éxito específico: Verificar que una clínica conocida esté presente
        val contieneUnam = lista.any { it.second.contains("UNAM") }
        assertTrue("La lista de emergencias debería contener el Hospital de la UNAM", contieneUnam)
    }

    @Test
    fun `las clinicas de emergencia son distintas a las normales en su mayoria`() {
        val normales = ClinicasUtils.clinicasNormal()
        val emergencias = ClinicasUtils.clinicasEmergencia()

        // Verificamos que no sean la misma lista por error de dedo
        assertNotEquals(normales, emergencias)
    }

    @Test
    fun `verificar que los titulos de emergencia contengan la palabra Hospital o Clinica`() {
        val emergencias = ClinicasUtils.clinicasEmergencia()

        emergencias.forEach { (_, titulo, _) ->
            val tieneIdentificador = titulo.startsWith("Hospital:") || titulo.startsWith("Clínica:")
            assertTrue("El título '$titulo' debe empezar con Hospital: o Clínica:", tieneIdentificador)
        }
    }
}