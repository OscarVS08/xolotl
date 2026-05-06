package com.example.xolotl.ui.main.mascota

import android.util.Base64
import com.example.xolotl.data.models.Mascotas
import com.example.xolotl.utils.EncryptionUtils
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mockito.any
import org.mockito.Mockito.mockStatic
import java.util.Base64 as JavaBase64

class MascotasActivityTest {

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
                // Al descifrar, protegemos contra nulos
                Base64.decode(kotlinAny(""), anyInt())
            }.thenAnswer { invocation ->
                val input = invocation.arguments[0] as String
                try {
                    JavaBase64.getDecoder().decode(input)
                } catch (e: IllegalArgumentException) {
                    // Si la cadena no es Base64 válido (simulando que falló), lanzamos la excepción
                    throw e
                }
            }

            block()
        }
    }

    // =========================================================
    // 1. PRUEBAS DE INTEGRIDAD DEL MODELO
    // =========================================================

    @Test
    fun `modelo Mascotas - inicializa variables vacias requeridas por Firestore`() {
        // Firebase Firestore EXIGE que los modelos tengan un constructor vacío
        // y que sus propiedades no sean nulas al instanciarse.
        val mascotaVacia = Mascotas()

        assertEquals("El nombre debe inicializar vacío", "", mascotaVacia.nombre)
        assertEquals("El RUAC debe inicializar vacío", "", mascotaVacia.ruac)
        assertEquals("La fotoBase64 debe inicializar vacía", "", mascotaVacia.fotoBase64)
    }

    // =========================================================
    // 2. PRUEBAS DE LA LÓGICA HÍBRIDA DE FOTOS (El try-catch del Activity)
    // =========================================================

    @Test
    fun `logica hibrida - si la foto de Firebase esta encriptada, la desencripta correctamente`() {
        conPuenteEncriptacion {
            val base64RealDeLaFoto = "iVBORw0KGgoAAAANSUhEUgAAAAE..."

            // Simulamos que en Firebase se guardó correctamente encriptada
            val fotoEnFirebase = EncryptionUtils.encrypt(base64RealDeLaFoto)

            // ESTA ES LA LÓGICA EXACTA DE TU ACTIVITY
            val base64Limpio = try {
                EncryptionUtils.decrypt(fotoEnFirebase)
            } catch (e: Exception) {
                fotoEnFirebase
            }

            // Validamos que el resultado sea la foto real lista para BitmapFactory
            assertEquals(base64RealDeLaFoto, base64Limpio)
        }
    }

    @Test
    fun `logica hibrida - si la foto NO esta encriptada, el catch salva la app y devuelve la foto original`() {
        conPuenteEncriptacion {
            // Simulamos que por algún error, Firebase guardó el Base64 en texto plano
            // o es una cadena que NO cumple con el formato AES de tu EncryptionUtils.
            val fotoPlanaEnFirebase = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQ..."

            // ESTA ES LA LÓGICA EXACTA DE TU ACTIVITY
            val base64Limpio = try {
                // Esto forzará una excepción porque no es un texto cifrado válido para tu AES
                EncryptionUtils.decrypt(fotoPlanaEnFirebase)
            } catch (e: Exception) {
                // El catch atrapa el crash y devuelve la cadena plana
                fotoPlanaEnFirebase
            }

            // Validamos que el Activity no se crashee y logre recuperar la cadena plana
            assertEquals(
                "El sistema debe sobrevivir a la excepción y usar la cadena original",
                fotoPlanaEnFirebase,
                base64Limpio
            )
        }
    }
}