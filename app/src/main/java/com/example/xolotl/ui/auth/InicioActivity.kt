package com.example.xolotl.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.databinding.ActivityInicioBinding


class InicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Botón: Iniciar sesión
        binding.btnIniciarSesion.setOnClickListener {
            startActivity(Intent(this, IniciarSesionActivity::class.java))
        }

        // Botón: Registrarse
        binding.btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistrarseActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Ya hay un usuario logueado, ir directo al MainActivity
            val intent = Intent(this, com.example.xolotl.MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
