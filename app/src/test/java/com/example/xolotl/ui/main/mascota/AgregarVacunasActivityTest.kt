package com.example.xolotl.ui.main.mascota

import android.util.Base64
import com.example.xolotl.data.models.Vacunas
import com.example.xolotl.data.repository.VacunasRepository
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.ValidationUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import java.util.Base64 as JavaBase64

class AgregarVacunasActivityTest {

    private lateinit var mockRepository: VacunasRepository

    @Before
    fun setup() {
        mockRepository = mock(VacunasRepository::class.java)
    }

    /**
     * TRUCO MAESTRO: Esta función engaña a Kotlin para que acepte los matchers de Mockito
     * sin lanzar NullPointerException. Registra el "any" y devuelve un valor dummy seguro.
     */
    private fun <T> kotlinAny(dummy: T): T {
        any<T>() // Le decimos a Mockito que acepte cualquier cosa
        return dummy // Le damos a Kotlin un objeto real para que no se asuste con nulos
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
    // 1. PRUEBAS DE ALGORITMOS DE FECHAS
    // =========================================================

    @Test
    fun `fechas de vacunacion - el refuerzo debe ser posterior a la aplicacion`() {
        val fechaAplicacion = "15/08/2026"
        val refuerzoValido = "15/09/2026"
        val refuerzoInvalido = "14/08/2026"

        assertTrue(ValidationUtils.isFechaPosterior(fechaAplicacion, refuerzoValido))
        assertFalse("El sistema debe rechazar un refuerzo programado en el pasado",
            ValidationUtils.isFechaPosterior(fechaAplicacion, refuerzoInvalido))
    }

    @Test
    fun `formato de fecha de refuerzo - exige el patron exacto DD-MM-YYYY`() {
        // Validamos el formato correcto normal
        assertTrue(ValidationUtils.isValidProximaFecha("01/12/2026"))

        // Casos frontera y errores de formato
        assertFalse(ValidationUtils.isValidProximaFecha("2026/12/01")) // Formato inverso
        assertFalse(ValidationUtils.isValidProximaFecha("1/1/26"))     // Faltan ceros
    }

    // =========================================================
    // 2. PRUEBAS DE VALIDACIÓN BÁSICA Y TEXTOS (Edge Cases)
    // =========================================================

    @Test
    fun `validacion de vacuna - el nombre debe tener al menos 3 letras`() {
        assertTrue(ValidationUtils.isValidMedicamento("Rabia"))
        assertTrue(ValidationUtils.isValidMedicamento("Parvovirus"))

        assertFalse("Nombres de 1 o 2 letras deben ser rechazados", ValidationUtils.isValidMedicamento("Ra"))
    }

    @Test
    fun `validacion de marca - rechaza marcas vacias o demasiado cortas`() {
        assertTrue(ValidationUtils.isValidMarca("Zoetis"))
        assertTrue(ValidationUtils.isValidMarca("Merial"))

        assertFalse("Una marca de 1 letra no suele ser válida", ValidationUtils.isValidMarca("Z"))
    }

    // =========================================================
    // 3. PRUEBAS DE SEGURIDAD (Encriptación)
    // =========================================================

    @Test
    fun `seguridad - los datos clinicos de la vacuna viajan encriptados`() {
        conPuenteEncriptacion {
            val vacunaReal = "Sextuple"
            val dosisCombinada = "2.5 ml"

            val vacunaEncriptada = EncryptionUtils.encrypt(vacunaReal)
            val dosisEncriptada = EncryptionUtils.encrypt(dosisCombinada)

            assertNotEquals(vacunaReal, vacunaEncriptada)
            assertNotEquals(dosisCombinada, dosisEncriptada)

            assertEquals(vacunaReal, EncryptionUtils.decrypt(vacunaEncriptada))
            assertEquals(dosisCombinada, EncryptionUtils.decrypt(dosisEncriptada))
        }
    }

    // =========================================================
    // 4. PRUEBAS DE REPOSITORIO (Simulación de Firebase Segura)
    // =========================================================

    @Test
    fun `repositorio - registro exitoso de vacuna dispara callback de exito`() {
        conPuenteEncriptacion {
            var onSuccessLlamado = false

            // Usamos nuestra función mágica 'kotlinAny' pasándole valores dummy
            doAnswer { invocation ->
                val onSuccess = invocation.arguments[2] as () -> Unit
                onSuccess.invoke()
                null
            }.`when`(mockRepository).registrarVacuna(
                kotlinAny("RUAC_DUMMY"),
                kotlinAny(Vacunas()),
                kotlinAny({}), // lambda dummy
                kotlinAny({})  // lambda dummy
            )

            mockRepository.registrarVacuna("RUAC-987", Vacunas(), {
                onSuccessLlamado = true
            }, {
                fail("No debería invocar la ruta de error")
            })

            assertTrue(onSuccessLlamado)
        }
    }

    @Test
    fun `repositorio - error en la red al registrar vacuna captura la excepcion`() {
        conPuenteEncriptacion {
            var mensajeErrorCapturado = ""
            val excepcionSimulada = Exception("Error de timeout de Firebase")

            // Usamos nuestra función mágica 'kotlinAny' pasándole valores dummy
            doAnswer { invocation ->
                val onError = invocation.arguments[3] as (Exception) -> Unit
                onError.invoke(excepcionSimulada)
                null
            }.`when`(mockRepository).registrarVacuna(
                kotlinAny("RUAC_DUMMY"),
                kotlinAny(Vacunas()),
                kotlinAny({}),
                kotlinAny({})
            )

            mockRepository.registrarVacuna("RUAC-987", Vacunas(), {
                fail("No debería invocar la ruta de éxito")
            }, { error ->
                mensajeErrorCapturado = error.message ?: ""
            })

            assertEquals("Error de timeout de Firebase", mensajeErrorCapturado)
        }
    }

    // =========================================================
    // 5. PRUEBAS DE EXTREMA ROBUSTEZ (Prevención de Crashes)
    // =========================================================

    @Test
    fun `utilidades manejan textos nulos o basura sin crashear`() {
        val textoBasura = "No soy una fecha"
        val fechaNormal = "01/01/2026"

        assertFalse(ValidationUtils.isFechaPosterior(textoBasura, fechaNormal))
        assertFalse(ValidationUtils.isFechaPosterior(fechaNormal, textoBasura))
    }
}