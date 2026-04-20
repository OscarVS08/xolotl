package com.example.xolotl.utils

import android.util.Patterns

object ValidationUtils {

    // --- EMAIL ---
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // --- CURP ---
    fun isValidCURP(curp: String): Boolean {
        val regex = Regex("^[A-Z]{4}\\d{6}[HM][A-Z]{5}[A-Z0-9]{2}$", RegexOption.IGNORE_CASE)
        return regex.matches(curp)
    }

    // --- NOMBRE / APELLIDOS ---
    fun isValidName(name: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{1,50}$")
        return regex.matches(name)
    }

    // --- TELÉFONO ---
    fun isValidPhone(phone: String): Boolean {
        val regex = Regex("^\\d{10}$")
        return regex.matches(phone)
    }

    // --- CÓDIGO POSTAL ---
    fun isValidPostalCode(cp: String): Boolean {
        val regex = Regex("^\\d{5}$")
        return regex.matches(cp)
    }

    // --- CAMPOS NO VACÍOS ---
    fun isNotEmpty(vararg fields: String): Boolean {
        return fields.all { it.isNotBlank() }
    }

    // --- CONTRASEÑA FUERTE ---
    fun isStrongPassword(password: String): Boolean {
        // Debe tener al menos: 8 caracteres, 1 mayúscula, 1 minúscula, 1 número y 1 carácter especial
        val regex = Regex("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#\$%^&+=!¿?*._-]).{8,}$")
        return regex.matches(password)
    }

    // --- CALLE / NÚMERO / COLONIA / ALCALDÍA ---
    fun isValidAddressField(field: String): Boolean {
        return field.isNotBlank() && field.length <= 50
    }

    // --- NUEVAS VALIDACIONES ---

    // Validar si un campo tiene el mínimo de caracteres (ej. para contraseñas o nombres cortos)
    fun hasMinLength(text: String, min: Int): Boolean = text.length >= min

    // Validar si dos textos coinciden (para confirmar contraseña)
    fun doMatches(text1: String, text2: String): Boolean = text1 == text2

    // Validar Número de casa (que no sea solo espacios)
    fun isValidHouseNumber(number: String): Boolean {
        return number.isNotBlank() && number.length <= 10
    }

    // Validaciones para mascotas
    fun validarMascota(nombre: String, especie: String, raza: String): Boolean {
        return nombre.isNotEmpty() && especie.isNotEmpty() && raza.isNotEmpty()
    }

    // -----------------------------
    // VALIDACIONES PARA MASCOTAS
    // -----------------------------
    fun isValidRuac(ruac: String): Boolean {
        val regex = Regex("^[A-Z0-9]{10}$")
        return regex.matches(ruac)
    }

    fun isValidPetName(name: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s]{1,30}$")
        return regex.matches(name)
    }

    fun isValidPetSpecies(species: String): Boolean {
        return species.equals("Perro", true) || species.equals("Gato", true)
    }

    fun isValidPetRace(race: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{3,30}$")
        return regex.matches(race)
    }

    fun isValidPetSex(sex: String): Boolean {
        return sex.equals("Macho", true) || sex.equals("Hembra", true)
    }

    fun isValidDate(date: String): Boolean {
        val regex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        return regex.matches(date)
    }

    fun isValidNumber(value: String): Boolean {
        val regex = Regex("^\\d{1,3}(\\.\\d{1,2})?\$") // 1–999, con decimales opcionales
        return regex.matches(value)
    }

    fun isValidHeight(value: String): Boolean {
        val regex = Regex("^\\d{1,3}(\\.\\d{1,2})?$")
        return regex.matches(value)
    }

    fun isValidColor(color: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{3,20}$")
        return regex.matches(color)
    }

    fun isValidAlergia(text: String): Boolean {
        if (text.isEmpty()) return true // opcional
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s,.]{0,200}$")
        return regex.matches(text)
    }

    fun isValidNotas(text: String): Boolean {
        if (text.isEmpty()) return true
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s,.\\n]{0,500}$")
        return regex.matches(text)
    }

    fun validarMascotaCompleta(
        ruac: String,
        nombre: String,
        fecha: String,
        especie: String,
        raza: String,
        color: String,
        sexo: String,
        peso: String,
        estatura: String,
        alergias: String,
        notas: String
    ): Boolean {

        return isValidRuac(ruac)
                && isValidPetName(nombre)
                && isValidDate(fecha)
                && isValidPetSpecies(especie)
                && isValidPetRace(raza)
                && isValidColor(color)
                && isValidPetSex(sexo)
                && isValidNumber(peso)
                && isValidHeight(estatura)
                && isValidAlergia(alergias)
                && isValidNotas(notas)
    }

    // -----------------------------
    // VALIDACIONES DESPARASITACIÓN
    // -----------------------------

    fun isValidMetodo(metodo: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{3,30}$")
        return regex.matches(metodo)
    }

    fun isValidMedicamento(nombre: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s]{3,40}$")
        return regex.matches(nombre)
    }

    fun isValidMarca(marca: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s]{2,30}$")
        return regex.matches(marca)
    }

    fun isValidFechaDesparasitacion(fecha: String): Boolean {
        val regex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        return regex.matches(fecha)
    }

    fun isValidProximaFecha(fecha: String): Boolean {
        if (fecha.isEmpty()) return true // opcional
        val regex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        return regex.matches(fecha)
    }

    fun isValidRuacMascota(ruac: String): Boolean {
        return ruac.isNotBlank()
    }

    fun isFechaPosterior(fecha1: String, fecha2: String): Boolean {
        return try {
            val formato = java.text.SimpleDateFormat("dd/MM/yyyy")
            val f1 = formato.parse(fecha1)
            val f2 = formato.parse(fecha2)
            f2.after(f1)
        } catch (e: Exception) {
            false
        }
    }

    // -----------------------------
    // VALIDACIONES VACUNAS
    // -----------------------------

    fun isValidVacunaNombre(nombre: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s]{3,40}$")
        return regex.matches(nombre)
    }

    fun isValidVacunaMarca(marca: String): Boolean {
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s]{2,30}$")
        return regex.matches(marca)
    }

    fun isValidDosis(dosis: String): Boolean {
        val regex = Regex("^\\d{1,3}(\\.\\d{1,2})?$") // Ej: 1, 2.5, 10
        return regex.matches(dosis)
    }

    fun isValidFechaVacuna(fecha: String): Boolean {
        val regex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        return regex.matches(fecha)
    }

    fun isValidProximaFechaVacuna(fecha: String): Boolean {
        if (fecha.isEmpty()) return true // opcional
        val regex = Regex("^\\d{2}/\\d{2}/\\d{4}$")
        return regex.matches(fecha)
    }

    fun validarVacunaCompleta(
        nombre: String,
        marca: String,
        dosis: String,
        fecha: String,
        proximaFecha: String,
        ruac: String
    ): Boolean {

        return isValidVacunaNombre(nombre)
                && isValidVacunaMarca(marca)
                && isValidDosis(dosis)
                && isValidFechaVacuna(fecha)
                && isValidProximaFechaVacuna(proximaFecha)
                && isValidRuacMascota(ruac)
    }

    // -----------------------------
    // VALIDACIONES CITAS
    // -----------------------------

    fun isValidServicio(servicio: String): Boolean {
        // Permite letras, espacios y acentos, entre 3 y 40 caracteres
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ\\s]{3,40}$")
        return regex.matches(servicio)
    }

    fun isValidHorario(horario: String): Boolean {
        // Valida formato DD/MM/YYYY HH:mm
        val regex = Regex("^\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}$")
        return regex.matches(horario)
    }

    fun isFechaFutura(fechaHora: String): Boolean {
        return try {
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            val fechaCita = sdf.parse(fechaHora)
            val ahora = java.util.Date()
            fechaCita?.after(ahora) ?: false
        } catch (e: Exception) {
            false
        }
    }

    fun isValidNotasCita(notas: String): Boolean {
        // Las notas son opcionales (vacío es válido) pero máximo 200 caracteres y sin caracteres raros
        if (notas.isEmpty()) return true
        val regex = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9\\s,.\n]{0,200}$")
        return regex.matches(notas)
    }
}
