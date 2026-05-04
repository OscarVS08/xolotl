package com.example.xolotl.utils

import android.app.Activity
import android.content.Context
import android.widget.Toast
import cn.pedant.SweetAlert.SweetAlertDialog

object UiUtils {

    // Cambiamos a un tipo que pueda ser nulo para forzar el control en tests
    var dialogFactory: ((Context, Int) -> SweetAlertDialog)? = null

    // Función interna para obtener el diálogo (real o mock)
    private fun getDialog(context: Context, type: Int): SweetAlertDialog {
        return dialogFactory?.invoke(context, type) ?: SweetAlertDialog(context, type)
    }

    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun mostrarAlerta(
        activity: Activity,
        titulo: String,
        mensaje: String,
        tipo: Int = SweetAlertDialog.SUCCESS_TYPE,
        onConfirm: (() -> Unit)? = null
    ) {
        getDialog(activity, tipo) // <--- Usamos el helper
            .setTitleText(titulo)
            .setContentText(mensaje)
            .setConfirmText("OK")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                onConfirm?.invoke()
            }
            .show()
    }

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
        getDialog(activity, tipo)
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

    fun mostrarAlertaPdfGenerado(activity: Activity, onConfirm: (() -> Unit)? = null) {
        getDialog(activity, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("PDF generado")
            .setContentText("Carnet de la mascota generado exitosamente")
            .setConfirmText("Aceptar")
            .setConfirmClickListener { dialog ->
                dialog.dismissWithAnimation()
                onConfirm?.invoke()
            }
            .show()
    }

    fun mostrarAlertaPdfError(activity: Activity, onConfirm: (() -> Unit)? = null) {
        getDialog(activity, SweetAlertDialog.ERROR_TYPE)
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
        val mensaje = "Por favor corrobora los siguientes datos:\n\n" +
                "• Nombre: $nombre\n• Marca: $marca\n• Fecha: $fecha\n" +
                "• Próxima cita: $proximaFecha\n• Método: $metodo"

        getDialog(activity, SweetAlertDialog.WARNING_TYPE)
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

    fun mostrarConfirmacionVacuna(
        activity: Activity,
        nombre: String,
        marca: String,
        dosis: String,
        fecha: String,
        proximaFecha: String,
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        val mensaje = "Por favor corrobora los siguientes datos:\n\n" +
                "• Vacuna: $nombre\n• Marca: $marca\n• Dosis: $dosis\n" +
                "• Fecha: $fecha\n• Próxima cita: $proximaFecha"

        getDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Confirmar vacuna")
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

    fun mostrarConfirmacionCita(
        activity: Activity,
        servicio: String,
        fecha: String,
        onConfirm: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null
    ) {
        val mensaje = "Por favor corrobora los datos de la cita:\n\n" +
                "• Servicio: $servicio\n• Fecha y hora: $fecha\n\n¿Deseas agendar esta cita?"

        getDialog(activity, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Confirmar Cita")
            .setContentText(mensaje)
            .setConfirmText("Agendar")
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