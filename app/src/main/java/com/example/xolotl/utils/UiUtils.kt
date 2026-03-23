package com.example.xolotl.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog

object UiUtils {

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Muestra un SweetAlertDialog con solo botón de confirmación
     * @param activity: Activity donde se mostrará el alert
     * @param titulo: Título del alert
     * @param mensaje: Contenido del alert
     * @param tipo: Tipo de SweetAlertDialog (SUCCESS_TYPE, ERROR_TYPE, WARNING_TYPE...)
     * @param onConfirm: Acción a ejecutar al presionar OK
     */
    fun mostrarAlerta(
        activity: Activity,
        titulo: String,
        mensaje: String,
        tipo: Int = SweetAlertDialog.SUCCESS_TYPE,
        onConfirm: (() -> Unit)? = null
    ) {
        SweetAlertDialog(activity, tipo)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("OK")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                onConfirm?.invoke()
            }
            .show()
    }

    /**
     * Muestra un SweetAlertDialog con confirmación y cancelación
     * @param activity: Activity donde se mostrará el alert
     * @param titulo: Título del alert
     * @param mensaje: Contenido del alert
     * @param tipo: Tipo de SweetAlertDialog
     * @param confirmText: Texto del botón de confirmación
     * @param cancelText: Texto del botón de cancelación
     * @param onConfirm: Acción al presionar confirm
     * @param onCancel: Acción al presionar cancelar
     */
    fun mostrarAlertaCerrarSesion(
        activity: Activity,
        titulo: String,
        mensaje: String,
        tipo: Int,
        confirmText: String,
        cancelText: String,
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        SweetAlertDialog(activity, tipo)
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText(confirmText)
            .setCancelText(cancelText)
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                onConfirm?.invoke()
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()
                onCancel?.invoke()
            }
            .show()
    }

    /**
     * Muestra una alerta específica cuando el PDF de la mascota ha sido generado
     * @param activity: Activity donde se mostrará el alert
     * @param onConfirm: Acción al presionar "Aceptar"
     */
    fun mostrarAlertaPdfGenerado(
        activity: Activity,
        onConfirm: (() -> Unit)? = null
    ) {
        SweetAlertDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("PDF generado")
            .setContentText("Carnet de la mascota generado exitosamente")
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                onConfirm?.invoke()
            }
            .show()
    }

    /**
     * Muestra una alerta cuando ocurre un error al generar el PDF
     * @param activity: Activity donde se mostrará el alert
     * @param onConfirm: Acción al presionar "Aceptar"
     */
    fun mostrarAlertaPdfError(
        activity: Activity,
        onConfirm: (() -> Unit)? = null
    ) {
        SweetAlertDialog(activity, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error")
            .setContentText("No se pudo generar el PDF de la mascota")
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                onConfirm?.invoke()
            }
            .show()
    }

    fun mostrarConfirmacionDesparasitacion(
        activity: Activity,
        nombre: String,
        marca: String,
        fecha: String,
        proximaFecha: String,
        metodo: String,
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {

        val mensaje =
            "Por favor corrobora los siguientes datos:\n\n" +
                    "• Nombre: $nombre\n" +
                    "• Marca: $marca\n" +
                    "• Fecha: $fecha\n" +
                    "• Próxima cita: $proximaFecha\n" +
                    "• Método: $metodo"

        SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Confirmar registro")
            .setContentText(mensaje)
            .setConfirmText("Registrar")
            .setCancelText("Cancelar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                onConfirm?.invoke()
            }
            .setCancelClickListener { dialog ->
                dialog.dismissWithAnimation()
                onCancel?.invoke()
            }
            .show()
    }
}
