package com.example.xolotl.ui.main.usuario

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.xolotl.R
import com.example.xolotl.data.models.Citas
import android.content.Context
import android.content.Intent
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.xolotl.MainActivity
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CitasAdapter(
    private val lista: List<Citas>,
    private val context: Context
) : RecyclerView.Adapter<CitasAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtMascota: TextView = view.findViewById(R.id.txtNombreMascotaCita)
        val txtServicio: TextView = view.findViewById(R.id.txtServicioCita)
        val txtFecha: TextView = view.findViewById(R.id.txtFechaHoraCita)
        val chkAsistencia: CheckBox = view.findViewById(R.id.chkAsistencia)
        val btnEditar: ImageButton = view.findViewById(R.id.btnEditarCita)
        val btnEliminar: ImageButton = view.findViewById(R.id.btnEliminarCita)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cita, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = lista.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = lista[position]

        holder.txtMascota.text = cita.nombreMascota
        //holder.txtMascota.text = cita.ruacMascota
        holder.txtServicio.text = cita.servicio
        holder.txtFecha.text = cita.horario
        // Lógica de Asistencia por defecto
        holder.chkAsistencia.isChecked = true

        // Lógica de Color para citas pasadas
        // Obtenemos el fondo actual del LinearLayout
        val fondoCita = holder.itemView.findViewById<View>(R.id.layout_contenedor_interno).background

        if (esCitaPasada(cita.horario)) {
            // Solo cambiamos el color del fondo (tinte), manteniendo los bordes y el padding originales
            fondoCita.setTint(ContextCompat.getColor(context, R.color.CitaPasada))
        } else {
            // Volvemos al color original (debes tener definido el color del rectangulo en colors.xml)
            fondoCita.setTint(ContextCompat.getColor(context, R.color.rectanguloInicioSesion))
        }

        // Usar el activity para editar citas
        holder.btnEditar.setOnClickListener {

            val intent = Intent(context, EditarCitasActivity::class.java)

            // Envio de datos para poder editar
            intent.putExtra("id", lista[position].idC)
            intent.putExtra("ruacMascota", lista[position].ruacMascota)
            // Envio de datos
            intent.putExtra("servicio", cita.servicio)
            intent.putExtra("fechaHora", cita.horario)
            intent.putExtra("ruacMascota", cita.ruacMascota)
            intent.putExtra("nombreMascota", cita.nombreMascota)

            context.startActivity(intent)
        }

        holder.btnEliminar.setOnClickListener {

            UiUtils.mostrarAlertaCerrarSesion(
                activity = context as AppCompatActivity,
                titulo = "Eliminar cita",
                mensaje = "¿Estás seguro de eliminar esta cita?",
                tipo = cn.pedant.SweetAlert.SweetAlertDialog.WARNING_TYPE,
                confirmText = "Eliminar",
                cancelText = "Cancelar",
                onConfirm = {

                    eliminarCita(lista[position])
                },
                onCancel = { }
            )
        }
    }

    private fun eliminarCita(cita: Citas) {

        val db = FirebaseFirestore.getInstance()
        val uid = FirebaseAuth.getInstance().uid ?: return

        db.collection("usuarios")
            .document(uid)
            .collection("mascotas")
            .document(cita.ruacMascota)
            .collection("citas")
            .document(cita.idC)
            .delete()
            .addOnSuccessListener {

                UiUtils.mostrarAlerta(
                    context as AppCompatActivity,
                    "Eliminado",
                    "La cita fue eliminada correctamente",
                    cn.pedant.SweetAlert.SweetAlertDialog.SUCCESS_TYPE
                )

                // eliminar del listado local
                (lista as MutableList).remove(cita)
                notifyDataSetChanged()

                // Agregamos para que funcione la muestra del filtro
                if (context is MainActivity) {
                    context.recargarCitasDesdeAdapter()
                }
            }
            .addOnFailureListener {

                UiUtils.mostrarAlerta(
                    context as AppCompatActivity,
                    "Error",
                    "No se pudo eliminar la cita",
                    cn.pedant.SweetAlert.SweetAlertDialog.ERROR_TYPE
                )
            }
    }

    private fun esCitaPasada(fechaStr: String): Boolean {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val fechaCita = sdf.parse(fechaStr)
            val ahora = Date()
            fechaCita?.before(ahora) ?: false
        } catch (e: Exception) {
            false
        }
    }
}