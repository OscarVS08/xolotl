package com.example.xolotl.utils

import org.junit.Assert.*
import org.junit.Test

class ValidationUtilsTest {

    // ==========================================
    // 1. PRUEBAS DE AUTENTICACIÓN Y DATOS GENERALES
    // ==========================================

    @Test
    fun `correo valido pasa la validacion`() {
        assertTrue(ValidationUtils.isValidEmail("usuario@valido.com"))
        assertTrue(ValidationUtils.isValidEmail("nombre.apellido@empresa.com.mx"))
    }

    @Test
    fun `correo invalido o vacio falla la validacion`() {
        assertFalse(ValidationUtils.isValidEmail("usuariovalido.com"))
        assertFalse(ValidationUtils.isValidEmail("usuario@.com"))
        assertFalse(ValidationUtils.isValidEmail(""))
        assertFalse(ValidationUtils.isValidEmail("   "))
    }

    @Test
    fun `contrasena fuerte cumple con todos los requisitos`() {
        assertTrue(ValidationUtils.isStrongPassword("Password123!"))
    }

    @Test
    fun `contrasena debil falla si le falta algun requisito`() {
        assertFalse(ValidationUtils.isStrongPassword("password123"))
        assertFalse(ValidationUtils.isStrongPassword("PASSWORD123!"))
        assertFalse(ValidationUtils.isStrongPassword("Password!"))
        assertFalse(ValidationUtils.isStrongPassword("Pass1!"))
    }

    @Test
    fun `curp valida pasa la validacion`() {
        assertTrue(ValidationUtils.isValidCURP("VAAO010101HDFRNS09"))
    }

    @Test
    fun `curp con formato incorrecto o incompleto falla`() {
        assertFalse(ValidationUtils.isValidCURP("VAAO010101HDFRN"))
        assertFalse(ValidationUtils.isValidCURP("1234010101HDFRNS09"))
    }

    @Test
    fun `nombres validan acentos y espacios pero rechazan numeros`() {
        assertTrue(ValidationUtils.isValidName("Oscar René"))
        assertFalse(ValidationUtils.isValidName("Oscar123"))
        assertFalse(ValidationUtils.isValidName(""))
    }

    @Test
    fun `telefonos y codigos postales validan longitudes exactas`() {
        assertTrue(ValidationUtils.isValidPhone("5512345678"))
        assertFalse(ValidationUtils.isValidPhone("12345"))
        assertTrue(ValidationUtils.isValidPostalCode("07899"))
        assertFalse(ValidationUtils.isValidPostalCode("7899"))
    }

    @Test
    fun `isNotEmpty valida correctamente arreglos de strings`() {
        assertTrue(ValidationUtils.isNotEmpty("Texto", "Otro texto"))
        assertFalse(ValidationUtils.isNotEmpty("Texto", "", "Otro texto"))
    }

    // ==========================================
    // 2. PRUEBAS NUEVAS DE DIRECCIONES Y UTILIDADES
    // ==========================================

    @Test
    fun `campos de direccion validan limites de longitud y rechazan vacios`() {
        assertTrue(ValidationUtils.isValidAddressField("Calle Benito Juarez"))
        assertFalse(ValidationUtils.isValidAddressField("   "))
        // Excede 50 caracteres
        assertFalse(ValidationUtils.isValidAddressField("Esta direccion es absurdamente larga y superara los cincuenta caracteres"))
    }

    @Test
    fun `hasMinLength valida longitud minima correctamente`() {
        assertTrue(ValidationUtils.hasMinLength("Hola", 4))
        assertFalse(ValidationUtils.hasMinLength("Hi", 4))
    }

    @Test
    fun `doMatches valida que dos textos sean identicos`() {
        assertTrue(ValidationUtils.doMatches("Texto", "Texto"))
        assertFalse(ValidationUtils.doMatches("Texto", "texto")) // Case sensitive
    }

    @Test
    fun `numero de casa valida longitud maxima de 10`() {
        assertTrue(ValidationUtils.isValidHouseNumber("123-B"))
        assertFalse(ValidationUtils.isValidHouseNumber("   "))
        assertFalse(ValidationUtils.isValidHouseNumber("12345678901")) // 11 chars
    }

    // ==========================================
    // 3. PRUEBAS DE MASCOTAS (RUAC, Raza, Color)
    // ==========================================

    @Test
    fun `validarMascota verifica que los tres campos no esten vacios`() {
        assertTrue(ValidationUtils.validarMascota("Max", "Perro", "Pug"))
        assertFalse(ValidationUtils.validarMascota("", "Perro", "Pug"))
    }

    @Test
    fun `ruac valida exactamente 10 caracteres alfanumericos mayusculas`() {
        assertTrue(ValidationUtils.isValidRuac("ABCDE12345"))
        assertFalse(ValidationUtils.isValidRuac("abcde12345")) // Minúsculas
        assertFalse(ValidationUtils.isValidRuac("ABCD")) // Corto
    }

    @Test
    fun `nombres de mascota, razas y colores validan longitudes y caracteres`() {
        // Nombres (2 a 30)
        assertTrue(ValidationUtils.isValidPetName("Max"))
        assertFalse(ValidationUtils.isValidPetName(" A ")) // Corto al hacer trim

        // Raza (3 a 30)
        assertTrue(ValidationUtils.isValidPetRace("Pug"))
        assertFalse(ValidationUtils.isValidPetRace("Pu")) // Corto

        // Color (3 a 20)
        assertTrue(ValidationUtils.isValidColor("Cafe claro"))
        assertFalse(ValidationUtils.isValidColor("Ca"))
    }

    @Test
    fun `alergias y notas permiten vacios pero restringen caracteres y longitud`() {
        assertTrue(ValidationUtils.isValidAlergia("")) // Es opcional
        assertTrue(ValidationUtils.isValidAlergia("Polvo, polen"))
        assertTrue(ValidationUtils.isValidNotas("")) // Es opcional
        assertTrue(ValidationUtils.isValidNotas("Muerde zapatos.\nEs jugueton."))
    }

    // ==========================================
    // 4. PRUEBAS DE MEDIDAS Y FECHAS
    // ==========================================

    @Test
    fun `pesos y estaturas por especie respetan sus limites logicos`() {
        assertTrue(ValidationUtils.isValidPesoPorEspecie("30.5", "Perro"))
        assertFalse(ValidationUtils.isValidPesoPorEspecie("150.0", "Perro"))
        assertTrue(ValidationUtils.isValidEstaturaPorEspecie("25.0", "Gato"))
        assertFalse(ValidationUtils.isValidEstaturaPorEspecie("200.0", "Perro"))
    }

    @Test
    fun `fechas de mascotas y adopcion validan logica de tiempo`() {
        assertTrue(ValidationUtils.isValidDate("Desconozco dato"))
        assertTrue(ValidationUtils.esFechaPosterior("01/01/2023", "01/06/2023"))
        assertFalse(ValidationUtils.esFechaPosterior("01/06/2023", "01/01/2023"))
    }

    // ==========================================
    // 5. PRUEBAS DE DESPARASITACIÓN Y VACUNAS
    // ==========================================

    @Test
    fun `metodos, medicamentos y marcas de vacunas validan regex`() {
        assertTrue(ValidationUtils.isValidMetodo("Oral"))
        assertFalse(ValidationUtils.isValidMetodo("Or")) // Corto

        assertTrue(ValidationUtils.isValidMedicamento("Bravecto 123"))
        assertTrue(ValidationUtils.isValidVacunaMarca("Pfizer"))
    }

    @Test
    fun `fechas medicas validan formato exacto o aceptan vacios si son proximas`() {
        assertTrue(ValidationUtils.isValidFechaDesparasitacion("15/10/2023"))
        assertFalse(ValidationUtils.isValidFechaDesparasitacion("")) // Obligatorio

        assertTrue(ValidationUtils.isValidProximaFecha("")) // Opcional
        assertTrue(ValidationUtils.isValidProximaFecha("15/10/2024"))
    }

    @Test
    fun `isFechaPosterior valida cronologia simple de dos fechas`() {
        assertTrue(ValidationUtils.isFechaPosterior("01/01/2023", "02/01/2023"))
        assertFalse(ValidationUtils.isFechaPosterior("02/01/2023", "01/01/2023"))
        assertFalse(ValidationUtils.isFechaPosterior("Formato Raro", "02/01/2023"))
    }

    @Test
    fun `dosis de vacuna acepta enteros y decimales validos`() {
        assertTrue(ValidationUtils.isValidDosis("1"))
        assertTrue(ValidationUtils.isValidDosis("2.5"))
        assertFalse(ValidationUtils.isValidDosis("10.123")) // Más de dos decimales
    }

    // ==========================================
    // 6. PRUEBAS DE CITAS
    // ==========================================

    @Test
    fun `servicios y notas de citas validan sus formatos`() {
        assertTrue(ValidationUtils.isValidServicio("Corte de pelo"))
        assertFalse(ValidationUtils.isValidServicio("Co")) // Corto

        assertTrue(ValidationUtils.isValidNotasCita("")) // Opcional
        assertTrue(ValidationUtils.isValidNotasCita("Llevar cartilla."))
    }

    @Test
    fun `horario de citas valida formato y futuro`() {
        assertTrue(ValidationUtils.isValidHorario("15/10/2026 14:30"))
        assertFalse(ValidationUtils.isValidHorario("15/10/2026")) // Falta hora

        assertTrue(ValidationUtils.isFechaFutura("01/01/2099 10:00"))
        assertFalse(ValidationUtils.isFechaFutura("01/01/1990 10:00"))
    }
}