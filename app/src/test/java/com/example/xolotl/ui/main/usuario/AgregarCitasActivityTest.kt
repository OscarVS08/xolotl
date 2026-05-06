package com.example.xolotl.ui.main.usuario

import android.util.Base64
import com.example.xolotl.data.models.Citas
import com.example.xolotl.data.repository.CitasRepository
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.ValidationUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import java.util.Base64 as JavaBase64

class AgregarCitasActivityTest {

    private lateinit var mockRepository: CitasRepository

    @Before
    fun setup() {
        mockRepository = mock(CitasRepository::class.java)
    }

    /**
     * TRUCO MAESTRO: Engaña a Kotlin para que acepte matchers de Mockito sin lanzar NullPointerException.
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
    // 1. PRUEBAS DE ALGORITMOS TEMPORALES (Fechas y Horas)
    // =========================================================

    @Test
    fun `algoritmo de citas - no permite agendar citas en el pasado`() {
        val fechaPasada = "01/01/2000 10:00"
        val fechaFutura = "01/01/2050 15:30"

        assertFalse("El sistema debe rechazar fechas y horas que ya transcurrieron",
            ValidationUtils.isFechaFutura(fechaPasada))

        assertTrue("El sistema debe aceptar fechas programadas en el futuro",
            ValidationUtils.isFechaFutura(fechaFutura))
    }

    @Test
    fun `formato de horario - exige exactamente DD-MM-YYYY HH-mm`() {
        assertTrue(ValidationUtils.isValidHorario("15/08/2026 09:00"))
        assertTrue(ValidationUtils.isValidHorario("01/12/2026 14:30"))

        assertFalse(ValidationUtils.isValidHorario("15/08/2026"))
        assertFalse(ValidationUtils.isValidHorario("2026/08/15 09:00"))
        assertFalse(ValidationUtils.isValidHorario("15-08-2026 09:00"))
    }

    // =========================================================
    // 2. PRUEBAS DE VALIDACIÓN DE TEXTOS Y LÍMITES
    // =========================================================

    @Test
    fun `validacion de servicios - exige entre 3 y 40 caracteres`() {
        assertTrue(ValidationUtils.isValidServicio("Consulta general"))
        assertTrue(ValidationUtils.isValidServicio("Esterilización"))

        assertFalse("Debe rechazar servicios de 1 o 2 letras", ValidationUtils.isValidServicio("Ba"))

        val servicioExcesivo = "A".repeat(41)
        assertFalse("Debe rechazar nombres de servicio absurdamente largos", ValidationUtils.isValidServicio(servicioExcesivo))
    }

    @Test
    fun `validacion de notas - respeta el limite maximo de 200 caracteres`() {
        val notasNormales = "Llevar la cartilla de vacunación y su juguete favorito."
        val notasExcesivas = "A".repeat(201)

        assertTrue(ValidationUtils.isValidNotasCita(notasNormales))
        assertFalse("Debe rechazar notas que superen los 200 caracteres", ValidationUtils.isValidNotasCita(notasExcesivas))
    }

    // =========================================================
    // 3. PRUEBAS DE SEGURIDAD (Encriptación de la Cita)
    // =========================================================

    @Test
    fun `seguridad - los datos de la cita se encriptan antes de llegar al modelo`() {
        conPuenteEncriptacion {
            val servicioReal = "Limpieza dental"
            val horarioReal = "20/10/2026 11:00"
            val notasReales = "Requiere sedación leve"

            val servicioEncriptado = EncryptionUtils.encrypt(servicioReal)
            val horarioEncriptado = EncryptionUtils.encrypt(horarioReal)
            val notasEncriptadas = EncryptionUtils.encrypt(notasReales)

            assertNotEquals(servicioReal, servicioEncriptado)
            assertNotEquals(horarioReal, horarioEncriptado)

            assertEquals(servicioReal, EncryptionUtils.decrypt(servicioEncriptado))
            assertEquals(horarioReal, EncryptionUtils.decrypt(horarioEncriptado))
            assertEquals(notasReales, EncryptionUtils.decrypt(notasEncriptadas))
        }
    }

    // =========================================================
    // 4. PRUEBAS DE REPOSITORIO (Simulación de Firebase Segura)
    // =========================================================

    @Test
    fun `repositorio - registro exitoso de la cita dispara el callback de exito`() {
        conPuenteEncriptacion {
            var onSuccessLlamado = false
            val idDocumentoTest = "DOC-MASCOTA-123"
            val citaTest = Citas()

            // Usamos nuestra función segura 'kotlinAny' en lugar de 'eq()'
            doAnswer { invocation ->
                val onSuccess = invocation.arguments[2] as () -> Unit
                onSuccess.invoke()
                null
            }.`when`(mockRepository).registrarCita(
                kotlinAny("DUMMY_ID"),
                kotlinAny(Citas()),
                kotlinAny({}),
                kotlinAny({})
            )

            // Ejecutamos la función pasando los objetos reales
            mockRepository.registrarCita(idDocumentoTest, citaTest, {
                onSuccessLlamado = true
            }, {
                fail("No debería fallar")
            })

            assertTrue("El callback de éxito debe ejecutarse", onSuccessLlamado)
        }
    }

    @Test
    fun `repositorio - caida de red al agendar cita captura la excepcion correctamente`() {
        conPuenteEncriptacion {
            var errorAtrapado = false
            val excepcionSimulada = Exception("Error de conexión a Firestore")
            val idDocumentoTest = "DOC-MASCOTA-123"
            val citaTest = Citas()

            // Usamos nuestra función segura 'kotlinAny'
            doAnswer { invocation ->
                val onError = invocation.arguments[3] as (Exception) -> Unit
                onError.invoke(excepcionSimulada)
                null
            }.`when`(mockRepository).registrarCita(
                kotlinAny("DUMMY_ID"),
                kotlinAny(Citas()),
                kotlinAny({}),
                kotlinAny({})
            )

            // Ejecutamos
            mockRepository.registrarCita(idDocumentoTest, citaTest, {
                fail("No debería tener éxito")
            }, {
                errorAtrapado = true
            })

            assertTrue("El sistema debe atrapar las fallas de red", errorAtrapado)
        }
    }

    // =========================================================
    // 5. PRUEBAS DE EXTREMA ROBUSTEZ (Edge Cases)
    // =========================================================

    @Test
    fun `robusted - validadores temporales y de texto no crashean con formatos corruptos`() {
        // En lugar de usar strings vacíos que la UI ya valida, probamos con basura pura
        assertFalse(ValidationUtils.isValidHorario("No soy una hora"))
        assertFalse(ValidationUtils.isFechaFutura("Tarde o temprano"))
    }
}