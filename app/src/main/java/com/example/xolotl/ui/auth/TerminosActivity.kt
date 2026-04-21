package com.example.xolotl.ui.auth

import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.databinding.ActivityTerminosCondicionesBinding

class TerminosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTerminosCondicionesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTerminosCondicionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón para cerrar la pantalla y volver al registro
        binding.btnBack.setOnClickListener { finish() }

        mostrarTextoIntegro()
    }

    private fun mostrarTextoIntegro() {
        val contenidoLegal = """
            <b>Condiciones de servicio</b><br><br>
            
            <b>1 Términos y condiciones</b><br><br>
            
            Una vez que comience a utilizar la aplicación móvil, los siguientes términos y condiciones de uso aplicarán automáticamente a su persona, dándose por hecho que usted ha leído, comprendido y está de acuerdo con lo que en ellos se comunica, asegúrese de haberlos leído con detenimiento antes de comenzar a hacer uso de la aplicación.<br><br>
            
            <b>Cuenta de usuario</b><br>
            Si usted crea una cuenta para hacer uso de la aplicación, es su completa responsabilidad mantener un acceso seguro tanto a su dispositivo como a la aplicación misma, ya que esta puede solicitar información sensible cuya veracidad no será verificada en ningún momento por nosotros.<br>
            Le recomendamos no remover las restricciones de seguridad incluidas en el sistema operativo de su teléfono, ya que, de hacerlo, su dispositivo podría ser vulnerable/visible ante virus, malware o programas maliciosos, los cuales podrían comprometer la seguridad de las funcionalidades proveídas por la aplicación.<br><br>
            
            <b>Limitaciones de responsabilidad</b><br>
            Nosotros no nos declaramos dueños de ningún tipo de información que usted decida proveer a la aplicación en el curso y efecto de los presentes términos y condiciones de uso, usted tiene la completa responsabilidad de verificar la calidad, exactitud, integridad, legalidad y propiedad intelectual de cualquier tipo de información que decida proporcionar a la aplicación.<br>
            La aplicación móvil tiene la intención, más no la responsabilidad de solicitar, procesar y almacenar la información personal que usted decida proporcionarle, con el objetivo de brindar los servicios para los cuales fue diseñada.<br>
            Nosotros no asumimos la responsabilidad por cualquier daño material, físico o psicológico que el uso de la aplicación o cualquiera de sus funcionalidades puedan provocar de manera directa o indirecta a cualquiera de los usuarios registrados, sus mascotas o una tercera entidad no relacionada con las dos anteriores.<br>
            La aplicación requiere que el dispositivo en el cual esta se ejecute tenga acceso a una conexión estable con la internet, dicha conexión puede establecerse por Wi-Fi o ser proveída por cualquier otro punto de acceso conectado a la internet, nosotros no asumimos ninguna responsabilidad si la aplicación o alguna de sus funcionalidades no opera con normalidad cuanto el dispositivo no cuenta con dicha estabilidad de conexión.<br><br>
            
            <b>Exactitud de la información</b><br>
            Nosotros no nos hacernos responsables de la veracidad, integridad y exactitud de la información proporcionada por la aplicación cuando se hace uso de las funcionalidades “Mapa interactivo” y “Mapa interactivo para emergencias”.<br>
            Nos reservamos el derecho de corregir y/o cambiar cualquier tipo de información proporcionada por la aplicación en todas sus funcionalidades en cualquier momento y sin previo aviso, exceptuando aquellos cambios en la política de privacidad que la ley requiera que sean activamente informados.<br><br>
            
            <b>Servicios de terceros</b><br>
            La aplicación hace uso de servicios de terceros, cuyos proveedores cuentan con sus propios términos y condiciones de uso, así como su propia política de privacidad, a continuación, se muestran los enlaces para dichas políticas de los servicios de terceros activamente utilizados:<br>
            https://firebase.google.com/terms/analytics<br>
            https://firebase.google.com/terms/crashlytics<br>
            Nosotros no nos hacemos responsables por el uso que dichos proveedores de servicios hagan con los datos que les sean facilitados, esto incluye los datos de su cuenta de usuario, así como toda la información que usted decida proporcionar a la aplicación.<br><br>
            
            <b>Seguridad</b><br>
            Nosotros valoramos la confianza que los usuarios depositan en la aplicación al compartir sus datos personales, es por esto por lo que hacemos uso de medidas estandarizadas para proteger su información.<br>
            Sin embargo, es de suma importancia recordar a los usuarios de la aplicación que no existe algún método de transmisión de información a través de la internet o método de almacenamiento local o en línea que sea 100% seguro.<br>
            Nosotros no podemos garantizar la seguridad absoluta de los datos procesados por la aplicación.<br><br>
            
            <b>Enlaces hacia otros recursos</b><br>
            Bajo ciertas interacciones, la aplicación puede enlazarlo con recursos externos, sitios web, nosotros no estamos implicados de manera directa o indirecta con dichos recursos y las acciones y/o interacciones que puedan tener con su persona.<br>
            Nosotros no asumimos la responsabilidad por cualquier tipo de interacción y/o intercambio de información que usted realice con alguno de los proveedores de cualquier recurso externo enlazado por la aplicación.<br><br>
            
            <b>Restricciones al usuario</b><br>
            Queda prohibido para toda persona, física o moral, registrada o no registrada como usuario en la aplicación el hacer uso de esta para:<br>
            • Cualquier propósito fuera del marco legal.<br>
            • Solicitar a otros actuar o participar en actos fuera del marco legal.<br>
            • Infringir o violar la propiedad intelectual de la aplicación o cualquier otra entidad o persona.<br>
            • Acosar, abusar, insultar, dañar, difamar, calumniar, menospreciar, intimidar o discriminar por motivos de género, orientación sexual, religión, etnia, raza, edad, origen, nacionalidad o discapacidad.<br>
            • Difundir información falsa o no fundamentada.<br>
            • Difundir o transmitir virus o cualquier tipo de código malicioso que pueda ser usado de cualquier manera para afectar la funcionalidad u operación de la aplicación, sus servicios o los servicios prestados por terceros.<br>
            • Cualquier propósito obsceno o inmoral.<br>
            Nos reservamos el derecho a terminar con el acceso a la aplicación de cualquier usuario que la utilice para cualquiera de los fines antes mencionados.<br>
            Adicionalmente, usted no tiene autorización para copiar o modificar la aplicación de manera parcial o completa, intentar extraer el código fuente de esta o realizar algún tipo de traducción a algún otro lenguaje no soportado por la misma.<br><br>
            
            <b>Cambios y enmiendas es estos términos y condiciones de uso</b><br>
            Nos reservamos el derecho a modificar de cualquier manera los términos y condiciones presentes en este documento en cualquier momento.<br>
            Si se realizan cambios en los presentes términos y condiciones de uso, se les notificará a los usuarios, que deben aceptar los términos actualizados antes de que puedan seguir utilizando la aplicación con normalidad.<br><br>
            
            <b>Medios de contacto</b><br>
            Si tiene alguna duda o sugerencia referente a los términos de uso antes detallados, puede contactarnos enviando un mensaje a la siguiente dirección de correo electrónico: soul3.tt@gmail.com<br><br>
            
            <hr>
            
            <b>2 Política de privacidad</b><br><br>
            
            El presente apartado tiene como objetivo informar a los usuarios de la aplicación cómo se recolecta, procesa y utiliza su información personal, si usted hace uso de la aplicación se dará por hecho que ha leído, comprendido y está de acuerdo con las políticas aquí detalladas.<br>
            La información personal recolectada por la aplicación es utilizada únicamente para habilitar las funcionalidades de esta, nosotros no compartimos su información con ninguna otra entidad además de las mencionadas en este documento, asegúrese de haberlo leído con detenimiento antes de comenzar a hacer uso de la aplicación.<br><br>
            
            <b>Recolección y uso de la información</b><br>
            Con el fin de habilitar todas las funcionalidades y servicios que ofrece la aplicación se le solicitará un conjunto de datos personales específicos, estos datos serán utilizados para identificarlo como usuario del sistema, así como para asociar los datos de sus mascotas con su perfil de usuario principalmente.<br><br>
            
            <b>Acceso y uso de la ubicación en segundo plano</b><br>
            La aplicación puede solicitar a los usuarios el acceder a su ubicación precisa y mantener ese acceso en un proceso en segundo plano, esta información es solicitada con el fin de habilitar la ubicación rápida del usuario cuando este accede a las funcionalidades de “Mapa interactivo” y “Mapa interactivo para emergencias”.<br>
            Nosotros no utilizamos esta información para ningún otro propósito, la ubicación precisa del dispositivo no es:<br>
            • Vendida, rentada o alquilada a ningún servicio de terceros.<br>
            • Utilizada con fines publicitarios, de marketing o minería de datos.<br>
            Usted tiene el control total de conceder o revocar el acceso a la ubicación de su dispositivo en cualquier momento, permitiendo o denegando este permiso desde el panel de permisos de aplicación intrínseco a su dispositivo móvil.<br><br>
            
            <b>Proveedores de servicios</b><br>
            Nosotros hacemos uso de servicios de terceros con los siguientes fines:<br>
            • Acelerar la provisión de los servicios de la aplicación.<br>
            • Habilitar el guardado de los datos de los usuarios en la nube.<br>
            El servicio de guardado en la nube utilizado por la aplicación tiene acceso a los datos proporcionados por los usuarios, sin embargo, este está obligado a no difundir o utilizar dicha información para fines no establecidos en sus propios términos y condiciones y política de privacidad.<br><br>
            
            <b>Cambios y enmiendas es estas políticas de privacidad</b><br>
            Nos reservamos el derecho a modificar de cualquier manera las políticas de privacidad presentes en este documento en cualquier momento.<br>
            Si se realizan cambios en los presentes políticas, se les notificará a los usuarios, que deben aceptar las políticas actualizadas antes de que puedan seguir utilizando la aplicación con normalidad.<br><br>
            
            <b>Medios de contacto</b><br>
            Si tiene alguna duda o sugerencia referente a las políticas de privacidad antes detalladas, puede contactarnos enviando un mensaje a la siguiente dirección de correo electrónico: soul3.tt@gmail.com
        """.trimIndent()

        // Renderizado del HTML en el TextView
        binding.txtContenidoTerminos.text = Html.fromHtml(contenidoLegal, Html.FROM_HTML_MODE_COMPACT)
    }
}