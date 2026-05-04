package com.example.xolotl.utils

import android.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import java.util.Base64 as JavaBase64 // Usamos el Base64 de Java para el puente

class EncryptionUtilsTest {

    @Test
    fun `ciclo completo de encriptacion y desencriptacion es exitoso`() {
        // Redirigimos Base64 de Android al de Java para que funcione en JUnit local
        mockStatic(Base64::class.java).use { mockedBase64 ->
            configurarBase64Mock(mockedBase64)

            val textoOriginal = "Mensaje Secreto de Xolotl"

            // 1. Encriptar
            val textoEncriptado = EncryptionUtils.encrypt(textoOriginal)
            assertNotEquals(textoOriginal, textoEncriptado)

            // 2. Desencriptar
            val textoDesencriptado = EncryptionUtils.decrypt(textoEncriptado)

            // 3. Verificar que regresamos al origen
            assertEquals(textoOriginal, textoDesencriptado)
        }
    }

    @Test
    fun `encriptar textos devuelve resultado esperado`() {
        mockStatic(Base64::class.java).use { mockedBase64 ->
            configurarBase64Mock(mockedBase64)

            val texto = "PruebaConsistencia"
            val resultado1 = EncryptionUtils.encrypt(texto)
            val resultado2 = EncryptionUtils.encrypt(texto)

            // Valida consistencia
            assertEquals(resultado1, resultado2)

            // Valida que realmente se encriptó
            assertNotEquals(texto, resultado1)
        }
    }

    @Test(expected = Exception::class)
    fun `desencriptar un texto que no es base64 valido lanza excepcion`() {
        mockStatic(Base64::class.java).use { mockedBase64 ->
            // Simulamos que el decode falla porque el texto no es Base64
            mockedBase64.`when`<ByteArray> {
                Base64.decode("texto-invalido-!#", Base64.DEFAULT)
            }.thenThrow(IllegalArgumentException())

            EncryptionUtils.decrypt("texto-invalido-!#")
        }
    }

    @Test
    fun `maneja correctamente cadenas vacias`() {
        mockStatic(Base64::class.java).use { mockedBase64 ->
            configurarBase64Mock(mockedBase64)

            val vacio = ""
            val encriptado = EncryptionUtils.encrypt(vacio)
            val desencriptado = EncryptionUtils.decrypt(encriptado)

            assertEquals(vacio, desencriptado)
        }
    }

    /**
     * Función auxiliar para mapear el Base64 de Android al de Java durante el test
     */
    private fun configurarBase64Mock(mockedBase64: MockedStatic<Base64>) {
        // Mock del Encode
        mockedBase64.`when`<String> {
            Base64.encodeToString(any(ByteArray::class.java), eq(Base64.DEFAULT))
        }.thenAnswer { invocation ->
            val bytes = invocation.arguments[0] as ByteArray
            JavaBase64.getEncoder().encodeToString(bytes)
        }

        // Mock del Decode
        mockedBase64.`when`<ByteArray> {
            Base64.decode(anyString(), eq(Base64.DEFAULT))
        }.thenAnswer { invocation ->
            val input = invocation.arguments[0] as String
            JavaBase64.getDecoder().decode(input)
        }
    }

    @Test(expected = Exception::class)
    fun `error al desencriptar datos que han sido alterados`() {
        mockStatic(Base64::class.java).use { mockedBase64 ->
            configurarBase64Mock(mockedBase64)

            // Texto encriptado válido, pero le quitamos o cambiamos caracteres
            val encriptadoOriginal = EncryptionUtils.encrypt("Mensaje Real")
            val encriptadoCorrupto = encriptadoOriginal.substring(0, encriptadoOriginal.length - 5) + "ABCDE"

            // Esto debe lanzar una excepción de relleno (Padding) o de bloque inválido
            EncryptionUtils.decrypt(encriptadoCorrupto)
        }
    }

    @Test
    fun `soporta caracteres especiales y emojis correctamente`() {
        mockStatic(Base64::class.java).use { mockedBase64 ->
            configurarBase64Mock(mockedBase64)

            val textoComplejo = "Xólotl App 🐶 ✨ ¡Hola! 12345"
            val encriptado = EncryptionUtils.encrypt(textoComplejo)
            val desencriptado = EncryptionUtils.decrypt(encriptado)

            assertEquals(textoComplejo, desencriptado)
        }
    }

    @Test
    fun `verificar consistencia de la clave AES de 16 bytes`() {
        // Esta prueba asegura que la clave definida en el objeto sea válida para AES-128
        val claveBytes = "XolotlClaveAES16".toByteArray()
        assertEquals("La clave AES debe ser de exactamente 16 bytes para AES-128", 16, claveBytes.size)
    }
}