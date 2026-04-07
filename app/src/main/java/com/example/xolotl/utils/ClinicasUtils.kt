package com.example.xolotl.utils

import com.google.android.gms.maps.model.LatLng

object ClinicasUtils
{
    // Clínicas para el mapa normal.
    // Lista de Tuplas con los datos (posición, título, etiqueta) para colocar los marcadores.
    fun clinicasNormal(): List<Triple<LatLng, String, String>>
    {
        return listOf(
            Triple(LatLng(19.5164, -99.1347), "Clínica: Lucely", "Calz. Ticomán 1327, La Laguna Ticoman."),
            Triple(LatLng(19.5411, -99.1533), "Clínica: Duran", "Calz. Cuautepec, Hab Chalma la Unión."),
            Triple(LatLng(19.4490, -99.0556),"Clínica: Gavilán", "Ote. 4 mz56 lt1, Cuchilla del Tesoro."),
            Triple(LatLng(19.4583, -99.0545), "Clínica: San Eloy", "Av. 699 165, San Juan de Aragón III."),
            Triple(LatLng(19.4739, -99.1323), "Clínica: Marilú", "Ing. Carlos Daza 172, Guadalupe Insurgentes."),
            Triple(LatLng(19.5036, -99.1564), "Clínica: Plaza Vallejo", "Calz. Vallejo 1111-Local 3, Patera Vallejo I."),
            Triple(LatLng(19.4803, -99.1283), "Hospital: Parques insurgentes", "Av. Insurgentes Nte. 1537, Industrial."),
            Triple(LatLng(19.4701, -99.1184), "Clínica: Gobierno GAM CDMX", "Martha 190, Guadalupe Tepeyac."),
            Triple(LatLng(19.4755, -99.1207), "Clínica: Tepeyac", "Calz de Guadalupe 535, Col. Estrella."),
            Triple(LatLng(19.4705, -99.1197), "Clínica: VET PET TEPEYAC", "Av. Henry Ford 63, Guadalupe Tepeyac."),
            Triple(LatLng(19.4818, -99.1269), "Clínica: Fortuna", "Av Fortuna 196, Tepeyac Insurgentes."),
            Triple(LatLng(19.5392, -99.1400), "Clínica: La Palma", "Av. Guadalupe Victoria 77, Guadalupe Victoria I."),
            Triple(LatLng(19.5002, -99.1258), "Clínica: Cuidados veterinarios", "Matagalpa 1000, Lindavista."),
            Triple(LatLng(19.4952, -99.0793), "Clínica: FYNSA", "Mayor's Office, Ejido 94, San Felipe de Jesús."),
            Triple(LatLng(19.4571, -99.1029), "Hospital: Nuestro Cachorro", "Ote 91 4403, Nueva Tenochtitlan."),
            Triple(LatLng(19.5406, -99.1483), "Hospital: PETS NORTE", "Tecnológico Mz 49-Lote 16, Zona Escolar.")
        )
    }

    // Clínicas para el mapa para emergencias.
    fun clinicasEmergencia(): List<Triple<LatLng, String, String>>
    {
        return listOf(
            Triple(LatLng(19.4401, -99.2077), "Hospital: K-Lev", "Presa Angostura 29, Col. Irrigación, Miguel Hidalgo."),
            Triple(LatLng(19.3296, -99.1772), "Hospital: Facultad UNAM", "Ciudad Universitaria, Av. Universidad #3000, Colonia, C.U., Coyoacán."),
            Triple(LatLng(19.3646, -99.1786), "Hospital: Animal Life", "Barranca del Muerto 30, Crédito Constructor, Benito Juárez."),
            Triple(LatLng(19.2873, -99.2226), "Hospital: Albiter", "IZAMAL 306 ,Halacho ,Tenosique, Héroes de Padierna, Tlalpan."),
            Triple(LatLng(19.5210, -99.2129), "Hospital: Exotic Animal Health", "Viveros de Coyoacán, C. Viveros de Asís Número 47, Hab Viveros de la Loma."),
            Triple(LatLng(19.2906, -99.1256), "Hospital: SEVEPES Especialidades", "Canal de Miramontes 3656, Coapa, Narciso Mendoza, Tlalpan."),
            Triple(LatLng(19.3770, -99.1698), "Hospital: Oftalvet", "Av. Coyoacán 1141, Col del Valle Sur, Benito Juárez."),
            Triple(LatLng(19.4209, -99.1624), "Hospital: Durango", "Durango 127, Roma Nte., Cuauhtémoc."),
            Triple(LatLng(19.2670, -99.1297), "Hospital: Animal Care and Health", "Huitzilopochtli 13, Amp Tepepan, Xochimilco."),
            Triple(LatLng(19.3913, -99.1354), "Hospital: Villa de Canes", "Javier Sorondo 207, Iztaccihuatl, Benito Juárez."),
            Triple(LatLng(19.4650, -99.1700), "Hospital: Peques", "Cda. de Nueces 184, Nueva Santa María, Azcapotzalco."),
            Triple(LatLng(19.4687, -99.1689), "Hospital: Hospetall 24 Hrs.", "C. Membrillo 277, Hogar y Seguridad, Azcapotzalco."),
            Triple(LatLng(19.3457, -99.1494), "Hospital: Parque San Andrés", "América 103, Parque San Andrés, Coyoacán."),
            Triple(LatLng(19.4183, -99.1544), "Hospital: Roma", "Av. Cuauhtémoc 149, Roma Nte., Cuauhtémoc."),
            Triple(LatLng(19.3577, -99.1680), "Hospital: Especialidades Bruselas", "Bruselas 79, Del Carmen, Coyoacán."),
            Triple(LatLng(19.4310, -99.1799), "Hospital: DARWIN", "C. Copérnico 173, Anzures, Miguel Hidalgo."),
            Triple(LatLng(19.3210, -99.2319), "Hospital: San Jerónimo", "Av. San Jerónimo 1431, San Jerónimo Lídice, La Magdalena Contreras."),
            Triple(LatLng(19.4801, -99.1285), "Hospital: Parques insurgentes", "Av. Insurgentes Nte. 1537, Industrial."),
            Triple(LatLng(19.5406, -99.1483), "Hospital: PETS NORTE", "Tecnológico Mz 49-Lote 16, Zona Escolar.")
        )
    }
}