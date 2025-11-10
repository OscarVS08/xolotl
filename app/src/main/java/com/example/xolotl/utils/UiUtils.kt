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
}
