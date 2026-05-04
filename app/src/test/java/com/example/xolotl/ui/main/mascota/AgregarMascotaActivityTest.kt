package com.example.xolotl.ui.main.mascota

import android.util.Base64
import com.example.xolotl.data.models.Mascotas
import com.example.xolotl.data.repository.MascotaRepository
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.ValidationUtils
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import java.util.Base64 as JavaBase64

class AgregarMascotaActivityTest {

    private lateinit var mockRepository: MascotaRepository

    @Before
    fun setup() {
        mockRepository = mock(MascotaRepository::class.java)
    }

    /**
     * Puente robusto para Base64 de Android usando Java Base64
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
    // 1. PRUEBAS DE ALGORITMOS CRUZADOS (Especie vs Física)
    // =========================================================

    @Test
    fun `rechaza pesos excesivos dependiendo de la especie`() {
        // Gatos (Máximo 15kg según tu Activity)
        assertTrue(ValidationUtils.isValidPesoPorEspecie("5", "Gato"))
        assertTrue(ValidationUtils.isValidPesoPorEspecie("15", "Gato"))
        assertFalse(ValidationUtils.isValidPesoPorEspecie("16", "Gato")) // Falla

        // Perros (Máximo 100kg según tu Activity)
        assertTrue(ValidationUtils.isValidPesoPorEspecie("40", "Perro"))
        assertTrue(ValidationUtils.isValidPesoPorEspecie("100", "Perro"))
        assertFalse(ValidationUtils.isValidPesoPorEspecie("101", "Perro")) // Falla
    }

    @Test
    fun `rechaza alturas irreales dependiendo de la especie`() {
        // Gatos (Máximo 50cm)
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("30", "Gato"))
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("50", "Gato"))
        assertFalse(ValidationUtils.isValidEstaturaPorEspecie("51", "Gato"))

        // Perros (Máximo 110cm)
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("60", "Perro"))
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("110", "Perro"))
        assertFalse(ValidationUtils.isValidEstaturaPorEspecie("111", "Perro"))
    }

    // =========================================================
    // 2. PRUEBAS DE VALIDACIÓN BÁSICA
    // =========================================================

    @Test
    fun `el RUAC debe aceptar solo 10 caracteres alfanumericos`() {
        assertTrue(ValidationUtils.isValidRuac("ABC1234567")) // 10 chars
        assertFalse(ValidationUtils.isValidRuac("ABC123456"))  // Corto
        assertFalse(ValidationUtils.isValidRuac("ABC12345678"))// Largo
        assertFalse(ValidationUtils.isValidRuac("ABC-123456")) // Carácter no permitido
    }

    @Test
    fun `adopcion no puede ser anterior al nacimiento`() {
        val nacimiento = "01/01/2026"
        val adopcionCorrecta = "02/01/2026"
        val adopcionInvalida = "31/12/2025"

        assertTrue(ValidationUtils.esFechaPosterior(nacimiento, adopcionCorrecta))
        assertFalse(ValidationUtils.esFechaPosterior(nacimiento, adopcionInvalida))
        // La adopción el mismo día del nacimiento es válida
        assertTrue(ValidationUtils.esFechaPosterior(nacimiento, nacimiento))
    }

    // =========================================================
    // 3. PRUEBAS DE SEGURIDAD (Encriptación)
    // =========================================================

    @Test
    fun `los datos de la mascota se encriptan correctamente`() {
        conPuenteEncriptacion {
            val nombreReal = "Firulais"
            val nombreEncriptado = EncryptionUtils.encrypt(nombreReal)

            assertNotEquals("El nombre no debe guardarse en Firebase como texto plano", nombreReal, nombreEncriptado)
            assertEquals("La clave AES debe recuperar el nombre original", nombreReal, EncryptionUtils.decrypt(nombreEncriptado))
        }
    }

    // =========================================================
    // 4. PRUEBAS DE REPOSITORIO (Simulación de Firebase)
    // =========================================================

    @Test
    fun `registro exitoso de mascota dispara callback de exito (repositorio)`() {
        conPuenteEncriptacion {
            var onSuccessLlamado = false

            // Stubbing seguro usando Elvis operator
            doAnswer { invocation ->
                val onSuccess = invocation.arguments[1] as () -> Unit
                onSuccess.invoke()
                null
            }.`when`(mockRepository).registrarMascota(
                any() ?: Mascotas(),
                any() ?: {},
                any() ?: {}
            )

            mockRepository.registrarMascota(Mascotas(), {
                onSuccessLlamado = true
            }, {
                fail("No debería invocar error")
            })

            assertTrue(onSuccessLlamado)
        }
    }

    @Test
    fun `fallo al registrar mascota captura la excepcion correctamente (repositorio)`() {
        conPuenteEncriptacion {
            var mensajeErrorCapturado = ""
            val excepcionSimulada = Exception("Permisos denegados en Firestore")

            doAnswer { invocation ->
                val onError = invocation.arguments[2] as (Exception) -> Unit
                onError.invoke(excepcionSimulada)
                null
            }.`when`(mockRepository).registrarMascota(
                any() ?: Mascotas(),
                any() ?: {},
                any() ?: {}
            )

            mockRepository.registrarMascota(Mascotas(), {
                fail("No debería invocar éxito")
            }, { error ->
                mensajeErrorCapturado = error.message ?: ""
            })

            assertEquals("Permisos denegados en Firestore", mensajeErrorCapturado)
        }
    }

    // =========================================================
    // 5. PRUEBAS DE VALIDACIÓN DE TEXTOS (Edge Cases)
    // =========================================================

    @Test
    fun `rechaza nombres mayores a 30 caracteres o con simbolos`() {
        assertTrue(ValidationUtils.isValidPetName("Manchas"))
        assertTrue(ValidationUtils.isValidPetName("Señor Bigotes")) // Acepta espacios

        // Falla: Más de 30 caracteres
        assertFalse(ValidationUtils.isValidPetName("Este Nombre Es Absurdamente Largo Para Un Perro"))
        // Falla: Símbolos no permitidos (dependiendo de tu implementación)
        assertFalse(ValidationUtils.isValidPetName("Firulais_99!"))
    }

    @Test
    fun `alergias y notas respetan limites`() {
        val textoLargo = "A".repeat(501)

        assertTrue(ValidationUtils.isValidAlergia("Pollo, Res, Picaduras de abeja"))
        assertFalse(ValidationUtils.isValidAlergia(textoLargo)) // Excede límite

        assertTrue(ValidationUtils.isValidNotas("Es muy amigable pero le teme a los truenos"))
        assertFalse(ValidationUtils.isValidNotas(textoLargo)) // Excede límite
    }

    @Test
    fun `campos dinamicos Raza y Color 'Otro' exigen minimo 3 caracteres`() {
        // Simulando lo que ocurre cuando el usuario elige "Otro"
        assertTrue(ValidationUtils.isValidPetRace("Pomerania"))
        assertFalse(ValidationUtils.isValidPetRace("Po")) // Falla: Muy corto

        assertTrue(ValidationUtils.isValidColor("Bicolor"))
        assertFalse(ValidationUtils.isValidColor("Bi")) // Falla: Muy corto
    }
}