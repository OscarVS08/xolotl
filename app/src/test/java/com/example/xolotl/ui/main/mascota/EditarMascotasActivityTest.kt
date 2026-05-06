package com.example.xolotl.ui.main.mascota

import android.util.Base64
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.ValidationUtils
import org.junit.Assert.*
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import java.util.Base64 as JavaBase64

class EditarMascotasActivityTest {

    /**
     * TRUCO MAESTRO: Engaña a Kotlin para que acepte matchers de Mockito sin NPE.
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
    // 1. PRUEBAS DE VALIDACIÓN DE DUEÑO (Nuevas reglas)
    // =========================================================

    @Test
    fun `validacion de telefonos - exige exactamente 10 digitos`() {
        // Éxito: 10 dígitos exactos
        assertTrue(ValidationUtils.isValidPhone("5512345678"))

        // Falla: Longitudes incorrectas
        assertFalse("Debe rechazar teléfonos cortos", ValidationUtils.isValidPhone("551234567"))
        assertFalse("Debe rechazar teléfonos muy largos", ValidationUtils.isValidPhone("55123456789"))

        // Falla: Caracteres no numéricos
        assertFalse("Debe rechazar letras en el teléfono", ValidationUtils.isValidPhone("551234ABCD"))
        assertFalse("Debe rechazar símbolos", ValidationUtils.isValidPhone("55-1234-56"))
    }

    @Test
    fun `validacion de nombre de mascota - limites de 2 a 30 caracteres`() {
        assertTrue(ValidationUtils.isValidPetName("Bo")) // Límite inferior
        assertTrue(ValidationUtils.isValidPetName("Max"))

        assertFalse("Rechaza nombres de 1 letra", ValidationUtils.isValidPetName("A"))
        assertFalse("Rechaza nombres vacíos", ValidationUtils.isValidPetName(""))
    }

    // =========================================================
    // 2. PRUEBAS DE ALGORITMOS CRUZADOS (Especie vs Física)
    // =========================================================

    @Test
    fun `edicion de peso - respeta limites estrictos cruzados con la especie`() {
        // Gato (Máx 15kg)
        assertTrue(ValidationUtils.isValidPesoPorEspecie("4.5", "Gato"))
        assertTrue(ValidationUtils.isValidPesoPorEspecie("15", "Gato"))
        assertFalse(ValidationUtils.isValidPesoPorEspecie("15.1", "Gato"))

        // Perro (Máx 100kg)
        assertTrue(ValidationUtils.isValidPesoPorEspecie("35", "Perro"))
        assertTrue(ValidationUtils.isValidPesoPorEspecie("100", "Perro"))
        assertFalse(ValidationUtils.isValidPesoPorEspecie("105", "Perro"))
    }

    @Test
    fun `edicion de estatura - respeta limites cruzados con la especie`() {
        // Gato (Máx 50cm)
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("25", "Gato"))
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("50", "Gato"))
        assertFalse(ValidationUtils.isValidEstaturaPorEspecie("51", "Gato"))

        // Perro (Máx 110cm)
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("70", "Perro"))
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("110", "Perro"))
        assertFalse(ValidationUtils.isValidEstaturaPorEspecie("115", "Perro"))
    }

    // =========================================================
    // 3. PRUEBAS DE CRONOLOGÍA (Fechas)
    // =========================================================

    @Test
    fun `edicion de fechas - la adopcion no puede preceder al nacimiento`() {
        val nacimiento = "10/05/2023"
        val adopcionPosterior = "15/06/2023"
        val adopcionAnterior = "01/01/2023"

        assertTrue(ValidationUtils.esFechaPosterior(nacimiento, adopcionPosterior))
        assertTrue("La mascota puede ser adoptada el mismo día que nace", ValidationUtils.esFechaPosterior(nacimiento, nacimiento))

        assertFalse("El sistema debe impedir que se adopte antes de nacer", ValidationUtils.esFechaPosterior(nacimiento, adopcionAnterior))
    }

    // =========================================================
    // 4. PRUEBAS DE CAMPOS DINÁMICOS
    // =========================================================

    @Test
    fun `campos personalizados (Otro) - exigen un minimo de 3 caracteres`() {
        assertTrue(ValidationUtils.isValidPetRace("Mestizo"))
        assertFalse("Razas personalizadas menores a 3 letras son inválidas", ValidationUtils.isValidPetRace("Me"))

        assertTrue(ValidationUtils.isValidColor("Tricolor"))
        assertFalse("Colores personalizados menores a 3 letras son inválidos", ValidationUtils.isValidColor("Tr"))
    }

    // =========================================================
    // 5. PRUEBAS DE SEGURIDAD BIDIRECCIONAL (Lectura y Escritura)
    // =========================================================

    @Test
    fun `seguridad - desencriptacion y encriptacion funcionan en ambos sentidos para editar`() {
        conPuenteEncriptacion {
            // Simulamos datos que vienen de Firebase (Encriptados)
            val telefonoReal = "5599887766"
            val telefonoEncriptadoFirebase = EncryptionUtils.encrypt(telefonoReal)

            // El Activity lee de Firebase y desencripta para mostrar en pantalla
            val datoDescargado = EncryptionUtils.decrypt(telefonoEncriptadoFirebase)
            assertEquals("El Activity debe recuperar el teléfono real", telefonoReal, datoDescargado)

            // El usuario edita el dato en la pantalla
            val telefonoEditado = "5511223344"

            // El Activity encripta el nuevo dato para mandarlo a Firebase
            val datoParaSubir = EncryptionUtils.encrypt(telefonoEditado)

            assertNotEquals("Los datos actualizados no deben subir en texto plano", telefonoEditado, datoParaSubir)
            assertEquals("La base de datos debe almacenar el dato encriptado correcto", telefonoEditado, EncryptionUtils.decrypt(datoParaSubir))
        }
    }

    // =========================================================
    // 6. PRUEBAS DE EXTREMA ROBUSTEZ (Prevención de Crashes)
    // =========================================================

    @Test
    fun `robusted - validadores soportan strings vacios o con espacios sin lanzar excepciones`() {
        assertFalse(ValidationUtils.isValidPhone(""))
        assertFalse(ValidationUtils.isValidPhone("          "))
        assertFalse(ValidationUtils.isValidPetName(""))
        assertFalse(ValidationUtils.isValidPetName("   "))
        assertFalse(ValidationUtils.isValidPesoPorEspecie("", "Perro"))
        assertFalse(ValidationUtils.isValidEstaturaPorEspecie("", "Gato"))
        assertFalse(ValidationUtils.esFechaPosterior("", ""))
    }
}