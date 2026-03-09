package com.example.xolotl.ui.main.usuario.mapas

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.xolotl.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class VisualizarMapaUrgenciasActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visualizar_mapa_urgencias)

        // Botón Home
        findViewById<View>(R.id.btnHome).setOnClickListener {
            finish()
        }

        // Cargar el mapa en el contenedor
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Ubicación de prueba (CDMX)
        val ubicacionPrueba = LatLng(19.4326, -99.1332)

        mMap.addMarker(
            MarkerOptions()
                .position(ubicacionPrueba)
                .title("Marcador de prueba")
        )

        mMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(ubicacionPrueba, 15f)
        )
    }
}