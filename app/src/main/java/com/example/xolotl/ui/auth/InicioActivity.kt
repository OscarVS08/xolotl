package com.example.xolotl.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.databinding.ActivityInicioBinding
import com.example.xolotl.utils.UiUtils


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
            val dialog = SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)

            dialog.setTitleText("Requisito Importante")
                .setContentText("Para usar Xólotl, es obligatorio contar con el RUAC de tu mascota. ¿Deseas continuar?")
                .setConfirmText("Sí, lo tengo")
                .setCancelText("Cancelar")
                .setConfirmClickListener { sDialog ->
                    // 1. Iniciamos la animación de cierre
                    sDialog.dismissWithAnimation()

                    // 2. Usamos un postDelayed para esperar a que la animación termine
                    // Esto libera el hilo principal antes de lanzar el Intent
                    binding.root.postDelayed({
                        val intent = Intent(this, RegistrarseActivity::class.java)
                        startActivity(intent)
                    }, 300) // 300ms es el tiempo estándar de la animación
                }
                .setCancelClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                }
                .show()
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
