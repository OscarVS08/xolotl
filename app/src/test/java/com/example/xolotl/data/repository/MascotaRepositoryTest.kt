package com.example.xolotl.data.repository

import android.net.Uri
import com.example.xolotl.data.models.Mascotas
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class MascotaRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var mockStorage: FirebaseStorage
    private lateinit var repository: MascotaRepository

    @Before
    fun setup() {
        // Usamos RETURNS_DEEP_STUBS para simplificar el encadenamiento de llamadas
        mockDb = mock(FirebaseFirestore::class.java, RETURNS_DEEP_STUBS)
        mockStorage = mock(FirebaseStorage::class.java, RETURNS_DEEP_STUBS)
        repository = MascotaRepository(mockDb, mockStorage)
    }

    // ==========================================
    // PRUEBAS DE REGISTRO (FIRESTORE)
    // ==========================================

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `registrarMascota tiene exito en Firestore`() {
        val mascota = Mascotas(ruac = "XOLO123", nombre = "Dante")
        val mockTask = mock(Task::class.java) as Task<Void>

        `when`(mockDb.collection("mascotas").document(mascota.ruac).set(mascota))
            .thenReturn(mockTask)

        `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
            (it.arguments[0] as OnSuccessListener<Void>).onSuccess(null)
            mockTask
        }
        `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        var resultadoExitoso = false
        repository.registrarMascota(mascota, { resultadoExitoso = true }, {})

        assertTrue(resultadoExitoso)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `registrarMascota falla cuando Firestore arroja excepcion`() {
        val mascota = Mascotas(ruac = "XOLO123")
        val mockTask = mock(Task::class.java) as Task<Void>
        val excepcionEsperada = Exception("Error de escritura")

        `when`(mockDb.collection("mascotas").document(mascota.ruac).set(mascota))
            .thenReturn(mockTask)

        `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        `when`(mockTask.addOnFailureListener(any())).thenAnswer {
            (it.arguments[0] as OnFailureListener).onFailure(excepcionEsperada)
            mockTask
        }

        var mensajeError = ""
        repository.registrarMascota(mascota, {}, { mensajeError = it.message ?: "" })

        assertEquals("Error de escritura", mensajeError)
    }

    // ==========================================
    // PRUEBAS DE STORAGE (SUBIR FOTO)
    // ==========================================

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `subirFotoMascota tiene exito y devuelve la URL`() {
        val idMascota = "XOLO123"
        val bytes = byteArrayOf(0, 1, 2)
        val mockUploadTask = mock(UploadTask::class.java)
        val mockUriTask = mock(Task::class.java) as Task<Uri>
        val mockUri = mock(Uri::class.java)
        val urlEsperada = "https://firebasestorage.com/dante.jpg"

        val mockRef = mockStorage.reference.child("mascotas/$idMascota.jpg")
        `when`(mockRef.putBytes(bytes)).thenReturn(mockUploadTask)
        `when`(mockUri.toString()).thenReturn(urlEsperada)

        // Simular éxito de la subida (putBytes)
        `when`(mockUploadTask.addOnSuccessListener(any())).thenAnswer {
            (it.arguments[0] as OnSuccessListener<UploadTask.TaskSnapshot>).onSuccess(mock(UploadTask.TaskSnapshot::class.java))
            mockUploadTask
        }
        `when`(mockUploadTask.addOnFailureListener(any())).thenReturn(mockUploadTask)

        // Simular éxito al obtener URL de descarga (downloadUrl)
        `when`(mockRef.downloadUrl).thenReturn(mockUriTask)
        `when`(mockUriTask.addOnSuccessListener(any())).thenAnswer {
            (it.arguments[0] as OnSuccessListener<Uri>).onSuccess(mockUri)
            mockUriTask
        }

        var urlCapturada = ""
        repository.subirFotoMascota(idMascota, bytes, { urlCapturada = it }, {})

        assertEquals(urlEsperada, urlCapturada)
    }

    @Test
    fun `subirFotoMascota falla si Storage rechaza los bytes`() {
        val idMascota = "XOLO123"
        val bytes = byteArrayOf(0)
        val mockUploadTask = mock(UploadTask::class.java)
        val excepcionEsperada = Exception("Error de red en Storage")

        `when`(mockStorage.reference.child("mascotas/$idMascota.jpg").putBytes(bytes))
            .thenReturn(mockUploadTask)

        `when`(mockUploadTask.addOnSuccessListener(any())).thenReturn(mockUploadTask)
        `when`(mockUploadTask.addOnFailureListener(any())).thenAnswer {
            (it.arguments[0] as OnFailureListener).onFailure(excepcionEsperada)
            mockUploadTask
        }

        var mensajeError = ""
        repository.subirFotoMascota(idMascota, bytes, {}, { mensajeError = it.message ?: "" })

        assertEquals("Error de red en Storage", mensajeError)
    }
}