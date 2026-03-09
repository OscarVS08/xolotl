package com.example.xolotl.ui.main.usuario

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.R

class NotificacionesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        // Botón Home
        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }
    }
}
