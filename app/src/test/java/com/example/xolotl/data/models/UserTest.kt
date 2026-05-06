package com.example.xolotl.data.models

import org.junit.Assert.assertEquals
import org.junit.Test

class UserTest {

    @Test
    fun `modelo User se inicializa con strings vacios por defecto para Firebase`() {
        // Al instanciar sin pasar argumentos, todos los valores deben ser ""
        val userVacio = User()

        assertEquals("", userVacio.uid)
        assertEquals("", userVacio.curp)
        assertEquals("", userVacio.nombre)
        assertEquals("", userVacio.apellidoP)
        assertEquals("", userVacio.apellidoM)
        assertEquals("", userVacio.telefono)
        assertEquals("", userVacio.telefonoAlt)
        assertEquals("", userVacio.calle)
        assertEquals("", userVacio.numero)
        assertEquals("", userVacio.colonia)
        assertEquals("", userVacio.alcaldia)
        assertEquals("", userVacio.codigoPostal)
        assertEquals("", userVacio.correo)
    }

    @Test
    fun `modelo User asigna correctamente los valores pasados por parametro`() {
        val userLleno = User(
            uid = "FIREBASE_UID_123",
            curp = "VAAO010101HDFRNS09",
            nombre = "Oscar",
            apellidoP = "Vaquero",
            apellidoM = "Santos",
            telefono = "5512345678",
            telefonoAlt = "5512345679",
            calle = "Avenida Siempreviva",
            numero = "742",
            colonia = "Centro",
            alcaldia = "Gustavo A. Madero",
            codigoPostal = "07000",
            correo = "usuario@valido.com"
        )

        assertEquals("FIREBASE_UID_123", userLleno.uid)
        assertEquals("VAAO010101HDFRNS09", userLleno.curp)
        assertEquals("Oscar", userLleno.nombre)
        assertEquals("Vaquero", userLleno.apellidoP)
        assertEquals("Santos", userLleno.apellidoM)
        assertEquals("5512345678", userLleno.telefono)
        assertEquals("5512345679", userLleno.telefonoAlt)
        assertEquals("Avenida Siempreviva", userLleno.calle)
        assertEquals("742", userLleno.numero)
        assertEquals("Centro", userLleno.colonia)
        assertEquals("Gustavo A. Madero", userLleno.alcaldia)
        assertEquals("07000", userLleno.codigoPostal)
        assertEquals("usuario@valido.com", userLleno.correo)
    }
}