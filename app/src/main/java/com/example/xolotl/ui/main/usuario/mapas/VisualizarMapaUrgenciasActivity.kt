package com.example.xolotl.ui.main.usuario.mapas

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.xolotl.R
import com.example.xolotl.utils.ClinicasUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class VisualizarMapaUrgenciasActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var infoClinica: TextView
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_mapa_urgencias)

        // Inicializar el cliente de ubicación.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<View>(R.id.btnHome).setOnClickListener { finish() }

        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()

        infoClinica = findViewById(R.id.txtInfoClinica)

        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Configurar el listener de clics en marcadores.
        mMap.setOnMarkerClickListener(this)

        // Obtenemos la lista de clínicas, títulos y etiquetas.
        var lugares = ClinicasUtils.clinicasEmergencia()

        // Agregamos los marcadores.
        for ((posicion, titulo, etiqueta) in lugares) {
            val clinica = mMap.addMarker(
                MarkerOptions()
                    .position(posicion)
                    .title(titulo)
            )
            clinica?.tag = etiqueta
        }

        // Intentar obtener la ubicación del usuario
        enableUserLocation()
    }


    // Este método se dispara cuando se toca un marcador.
    override fun onMarkerClick(marker: Marker): Boolean {
        // Recuperamos la información que guardamos en el tag.
        val informacion = marker.tag as? String

        // Actualizamos el TextView.
        infoClinica.text = informacion ?: "Sin información disponible"

        // Retornar 'false' permite que ocurra el comportamiento por defecto
        // (que se mueva la cámara y se muestre el título pequeño del marcador).
        return false
    }


    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Solicitar permisos si no los tiene.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Si ya tiene permisos, habilitar el marcador en el mapa.
        mMap.isMyLocationEnabled = true

        // Obtener la última ubicación conocida y mover la cámara.
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
            } else {
                Toast.makeText(this, "No se pudo obtener la ubicación actual", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // Manejar la respuesta del usuario al diálogo de permisos, es decir,
    // el usuario no debe reiniciar la app si habilitó los permisos al usar el mapa.
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableUserLocation()
            }
        }
    }
}