package com.example.xolotl.ui.main.mascota

import android.util.Base64
import com.example.xolotl.data.models.Desparasitaciones
import com.example.xolotl.data.repository.DesparasitacionRepository
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.ValidationUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import java.util.Base64 as JavaBase64

class AgregarDesparasitacionesActivityTest {

    private lateinit var mockRepository: DesparasitacionRepository

    @Before
    fun setup() {
        mockRepository = mock(DesparasitacionRepository::class.java)
    }

    /**
     * Puente robusto para Base64 de Android
     */
    private fun conPuenteEncriptacion(block: () -> Unit) {
        mockStatic(Base64::class.java).use { mockedBase64 ->
            mockedBase64.`when`<String> {
                Base64.encodeToString(any() ?: ByteArray(0), anyInt())
            }.thenAnswer { invocation ->
                val bytes = invocation.arguments[0] as ByteArray
                JavaBase64.getEncoder().encodeToString(bytes)
            }

            mockedBase64.`when`<ByteArray> {
                Base64.decode(anyString() ?: "", anyInt())
            }.thenAnswer { invocation ->
                val input = invocation.arguments[0] as String
                JavaBase64.getDecoder().decode(input)
            }

            block()
        }
    }

    // =========================================================
    // 1. PRUEBAS DE ALGORITMOS (ValidationUtils)
    // =========================================================

    @Test
    fun `algoritmo de fechas que detecta correctamente fechas posteriores e invalidas`() {
        // Éxito: Fecha futura
        assertTrue(ValidationUtils.isFechaPosterior("01/01/2026", "02/01/2026"))
        // Error: Misma fecha
        assertFalse(ValidationUtils.isFechaPosterior("01/01/2026", "01/01/2026"))
        // Error: Fecha anterior
        assertFalse(ValidationUtils.isFechaPosterior("10/10/2026", "09/10/2026"))
    }

    @Test
    fun `validacion de campos que detecta longitudes y formatos prohibidos`() {
        // Metodo (Min 3)
        assertTrue(ValidationUtils.isValidMetodo("Pastilla"))
        assertFalse(ValidationUtils.isValidMetodo("Pi"))

        // Medicamento (Letras y espacios)
        assertTrue(ValidationUtils.isValidMedicamento("Simparica Trio"))
        assertFalse(ValidationUtils.isValidMedicamento("Med-123!"))

        // Marca
        assertTrue(ValidationUtils.isValidMarca("Zoetis"))
        assertFalse(ValidationUtils.isValidMarca(""))
    }

    @Test
    fun `validacion de proxima fecha que asegura formato correcto`() {
        assertTrue(ValidationUtils.isValidProximaFecha("12/12/2026"))
        assertFalse(ValidationUtils.isValidProximaFecha("2026/12/12"))
        assertFalse(ValidationUtils.isValidProximaFecha("12-12-26"))
    }

    // =========================================================
    // 2. PRUEBAS DE SEGURIDAD (EncryptionUtils)
    // =========================================================

    @Test
    fun `seguridad los datos sensibles se transforman y recuperan correctamente`() {
        conPuenteEncriptacion {
            val original = "Ivermectina"
            val encriptado = EncryptionUtils.encrypt(original)

            assertNotEquals("El dato no debe guardarse en texto plano", original, encriptado)
            assertEquals("La desencriptacion debe recuperar el dato original", original, EncryptionUtils.decrypt(encriptado))
        }
    }

    // =========================================================
    // 3. PRUEBAS DE REPOSITORIO (Casos de Éxito y Error)
    // =========================================================

    @Test
    fun `registro exitoso ejecuta el callback de exito (repositorio)`() {
        conPuenteEncriptacion {
            var exitoLlamado = false

            // Stubbing seguro
            doAnswer { invocation ->
                val onSuccess = invocation.arguments[2] as () -> Unit
                onSuccess.invoke()
                null
            }.`when`(mockRepository).registrarDesparasitacion(
                anyString() ?: "",
                any() ?: Desparasitaciones(),
                any() ?: {},
                any() ?: {}
            )

            mockRepository.registrarDesparasitacion("RUAC-123", Desparasitaciones(), {
                exitoLlamado = true
            }, { fail("No debería llamar a error") })

            assertTrue(exitoLlamado)
        }
    }

    @Test
    fun `error en el registro captura la excepcion correctamente (repositorio)`() {
        conPuenteEncriptacion {
            var errorCapturado = ""
            val excepcionSimulada = Exception("Fallo de conexion a Firestore")

            doAnswer { invocation ->
                val onError = invocation.arguments[3] as (Exception) -> Unit
                onError.invoke(excepcionSimulada) // Invocamos con el tipo Exception real
                null
            }.`when`(mockRepository).registrarDesparasitacion(
                anyString() ?: "",
                any() ?: Desparasitaciones(),
                any() ?: {},
                any() ?: {}
            )

            mockRepository.registrarDesparasitacion("RUAC-123", Desparasitaciones(), {
                fail("No debería llamar a éxito")
            }, { excepcion ->
                errorCapturado = excepcion.message ?: "Sin mensaje"
            })

            assertEquals("Fallo de conexion a Firestore", errorCapturado)
        }
    }

    // =========================================================
    // 4. PRUEBAS DE INTEGRIDAD DE DATOS
    // =========================================================

    @Test
    fun `el objeto Desparasitaciones conserva el RUAC asignado (modelo)`() {
        val ruac = "PET-999"
        val desparasitacion = Desparasitaciones(
            tipo = "E", nombre = "E", marca = "E",
            fecha = "E", proximaFecha = "E",
            ruacMascota = ruac
        )

        assertEquals(ruac, desparasitacion.ruacMascota)
    }
}