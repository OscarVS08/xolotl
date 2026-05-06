package com.example.xolotl.ui.main.mascota

import android.util.Base64
import com.example.xolotl.data.models.Citas
import com.example.xolotl.data.models.Desparasitaciones
import com.example.xolotl.data.models.Vacunas
import com.example.xolotl.utils.EncryptionUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.any
import org.mockito.Mockito.mockStatic
import java.util.Base64 as JavaBase64

class GenerarPdfActivityTest {

    /**
     * TRUCO MAESTRO para Kotlin NPEs
     */
    private fun <T> kotlinAny(dummy: T): T {
        any<T>()
        return dummy
    }

    /**
     * Puente robusto para Base64 de Android usando Java Base64
     */
    private fun conPuenteEncriptacion(block: () -> Unit) {
        mockStatic(Base64::class.java).use { mockedBase64 ->
            mockedBase64.`when`<String> {
                Base64.encodeToString(kotlinAny(ByteArray(0)), anyInt())
            }.thenAnswer { invocation ->
                val bytes = invocation.arguments[0] as ByteArray
                JavaBase64.getEncoder().encodeToString(bytes)
            }

            mockedBase64.`when`<ByteArray> {
                Base64.decode(kotlinAny(""), anyInt())
            }.thenAnswer { invocation ->
                val input = invocation.arguments[0] as String
                JavaBase64.getDecoder().decode(input)
            }

            block()
        }
    }

    // =========================================================
    // 1. PRUEBAS DE INTEGRIDAD DE MODELOS (Descifrado para PDF)
    // =========================================================
    // El Activity de PDF necesita "clonar" y descifrar los modelos.
    // Aquí probamos que esa lógica de clonación (copy) funciona correctamente.

    @Test
    fun `modelo desparasitacion - los datos se descifran correctamente para la tabla del PDF`() {
        conPuenteEncriptacion {
            // Simulamos lo que te entrega Firebase
            val modeloFirebase = Desparasitaciones(
                tipo = EncryptionUtils.encrypt("Interna"),
                nombre = EncryptionUtils.encrypt("Drontal"),
                marca = EncryptionUtils.encrypt("Bayer"),
                fecha = EncryptionUtils.encrypt("01/01/2026"),
                proximaFecha = EncryptionUtils.encrypt("01/04/2026"),
                ruacMascota = "RUAC-123" // Este no se encripta
            )

            // Simulamos la lógica exacta que tienes en tu Activity
            val modeloDescifradoParaPDF = modeloFirebase.copy(
                tipo = EncryptionUtils.decrypt(modeloFirebase.tipo),
                nombre = EncryptionUtils.decrypt(modeloFirebase.nombre),
                marca = EncryptionUtils.decrypt(modeloFirebase.marca),
                fecha = EncryptionUtils.decrypt(modeloFirebase.fecha),
                proximaFecha = EncryptionUtils.decrypt(modeloFirebase.proximaFecha)
            )

            // Validamos que la tabla del PDF reciba texto plano
            assertEquals("Interna", modeloDescifradoParaPDF.tipo)
            assertEquals("Drontal", modeloDescifradoParaPDF.nombre)
            assertEquals("Bayer", modeloDescifradoParaPDF.marca)
            assertEquals("01/01/2026", modeloDescifradoParaPDF.fecha)

            // Validamos que el RUAC se conserve en la copia
            assertEquals("RUAC-123", modeloDescifradoParaPDF.ruacMascota)

            // Validamos que no estamos mostrando texto encriptado en el PDF
            assertNotEquals(modeloFirebase.nombre, modeloDescifradoParaPDF.nombre)
        }
    }

    @Test
    fun `modelo vacuna - los datos se descifran correctamente para la tabla del PDF`() {
        conPuenteEncriptacion {
            val modeloFirebase = Vacunas(
                nombre = EncryptionUtils.encrypt("Sextuple"),
                marca = EncryptionUtils.encrypt("Zoetis"),
                dosis = EncryptionUtils.encrypt("2 ml"),
                fecha = EncryptionUtils.encrypt("10/05/2026"),
                proximaFecha = EncryptionUtils.encrypt("10/05/2027"),
                ruacMascota = "RUAC-123"
            )

            // Lógica de clonación de tu Activity
            val modeloDescifradoParaPDF = modeloFirebase.copy(
                nombre = EncryptionUtils.decrypt(modeloFirebase.nombre),
                marca = EncryptionUtils.decrypt(modeloFirebase.marca),
                dosis = EncryptionUtils.decrypt(modeloFirebase.dosis),
                fecha = EncryptionUtils.decrypt(modeloFirebase.fecha),
                proximaFecha = EncryptionUtils.decrypt(modeloFirebase.proximaFecha)
            )

            assertEquals("Sextuple", modeloDescifradoParaPDF.nombre)
            assertEquals("Zoetis", modeloDescifradoParaPDF.marca)
            assertEquals("2 ml", modeloDescifradoParaPDF.dosis)
        }
    }

    @Test
    fun `modelo cita - los datos se descifran correctamente para la tabla del PDF`() {
        conPuenteEncriptacion {
            val modeloFirebase = Citas(
                servicio = EncryptionUtils.encrypt("Corte de pelo"),
                horario = EncryptionUtils.encrypt("15/08/2026 10:00 AM"),
                notas = EncryptionUtils.encrypt("Llevar toalla"),
                ruacMascota = "RUAC-123"
            )

            // Lógica de clonación de tu Activity
            val modeloDescifradoParaPDF = modeloFirebase.copy(
                servicio = EncryptionUtils.decrypt(modeloFirebase.servicio),
                horario = EncryptionUtils.decrypt(modeloFirebase.horario),
                notas = EncryptionUtils.decrypt(modeloFirebase.notas)
            )

            assertEquals("Corte de pelo", modeloDescifradoParaPDF.servicio)
            assertEquals("15/08/2026 10:00 AM", modeloDescifradoParaPDF.horario)
            assertEquals("Llevar toalla", modeloDescifradoParaPDF.notas)
        }
    }

    // =========================================================
    // 2. PRUEBAS DE ROBUSTEZ (Edge Cases)
    // =========================================================

    @Test
    fun `el descifrado de modelos maneja strings vacias sin corromper el objeto`() {
        conPuenteEncriptacion {
            // Simulamos un registro donde algunos datos se omitieron (vacíos)
            val modeloFirebaseIncompleto = Citas(
                servicio = EncryptionUtils.encrypt("Baño"),
                horario = EncryptionUtils.encrypt(""), // Vacío
                notas = EncryptionUtils.encrypt(""),   // Vacío
                ruacMascota = ""
            )

            val modeloDescifradoParaPDF = modeloFirebaseIncompleto.copy(
                servicio = EncryptionUtils.decrypt(modeloFirebaseIncompleto.servicio),
                horario = EncryptionUtils.decrypt(modeloFirebaseIncompleto.horario),
                notas = EncryptionUtils.decrypt(modeloFirebaseIncompleto.notas)
            )

            assertEquals("Baño", modeloDescifradoParaPDF.servicio)
            assertEquals("", modeloDescifradoParaPDF.horario)
            assertEquals("", modeloDescifradoParaPDF.notas)
        }
    }
}