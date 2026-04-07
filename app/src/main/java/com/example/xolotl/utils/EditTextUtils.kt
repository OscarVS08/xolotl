package com.example.xolotl.utils

import android.widget.EditText
import androidx.core.content.ContextCompat

// FUNCIÓN DE EXTENSIÓN PARA CAMBIAR COLOR DEL TEXTO
fun EditText.updateTextColor(isValid: Boolean) {
    setTextColor(
        if (isValid) ContextCompat.getColor(context, android.R.color.black)
        else ContextCompat.getColor(context, android.R.color.darker_gray)
    )
}
