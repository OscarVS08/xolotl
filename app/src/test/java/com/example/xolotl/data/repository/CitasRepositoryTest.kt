package com.example.xolotl.data.repository

import com.example.xolotl.data.models.Citas
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class CitasRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var repository: CitasRepository

    @Before
    fun setup() {
        // Mockeamos la base de datos con "Deep Stubs" para soportar las cadenas largas
        mockDb = mock(FirebaseFirestore::class.java, RETURNS_DEEP_STUBS)
        mockAuth = mock(FirebaseAuth::class.java)
        mockUser = mock(FirebaseUser::class.java)

        // Le inyectamos los Mocks a tu repositorio
        repository = CitasRepository(mockDb, mockAuth)
    }

    // ==========================================
    // PRUEBAS DE REGISTRAR CITA
    // ==========================================

    @Test
    fun `registrarCita falla si el usuario no esta autenticado`() {
        // Simulamos que no hay usuario logueado
        `when`(mockAuth.currentUser).thenReturn(null)

        var errorCapturado = ""
        repository.registrarCita("RUAC123", Citas(),
            onSuccess = { },
            onError = { ex -> errorCapturado = ex.message ?: "" }
        )

        assertEquals("Usuario no autenticado", errorCapturado)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `registrarCita tiene exito al guardar en Firestore`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("UID_123")

        val cita = Citas(servicio = "Baño")
        val mockTask = mock(Task::class.java) as Task<Void>

        // Simulamos toda la ruta de Firestore hasta el set()
        `when`(mockDb.collection("usuarios").document("UID_123")
            .collection("mascotas").document("RUAC123")
            .collection("citas").document().set(cita)
        ).thenReturn(mockTask)

        // Simulamos que la tarea se completa exitosamente
        `when`(mockTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as OnSuccessListener<Void>
            listener.onSuccess(null)
            mockTask
        }
        `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        var exito = false
        repository.registrarCita("RUAC123", cita,
            onSuccess = { exito = true },
            onError = { }
        )

        assertTrue(exito)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `registrarCita notifica error si Firestore falla`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("UID_123")

        val cita = Citas()
        val mockTask = mock(Task::class.java) as Task<Void>
        val excepcionSimulada = Exception("Error de servidor Firebase")

        `when`(mockDb.collection("usuarios").document("UID_123")
            .collection("mascotas").document("RUAC123")
            .collection("citas").document().set(cita)
        ).thenReturn(mockTask)

        // Simulamos que la tarea falla
        `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as OnFailureListener
            listener.onFailure(excepcionSimulada)
            mockTask
        }

        var mensajeError = ""
        repository.registrarCita("RUAC123", cita,
            onSuccess = { },
            onError = { ex -> mensajeError = ex.message ?: "" }
        )

        assertEquals("Error de servidor Firebase", mensajeError)
    }

    // ==========================================
    // PRUEBAS DE OBTENER CITAS
    // ==========================================

    @Test
    fun `obtenerCitas falla si el usuario no esta autenticado`() {
        `when`(mockAuth.currentUser).thenReturn(null)

        var errorCapturado = ""
        repository.obtenerCitas("RUAC123",
            onSuccess = { },
            onError = { ex -> errorCapturado = ex.message ?: "" }
        )

        assertEquals("Usuario no autenticado", errorCapturado)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `obtenerCitas notifica error si falla la lectura de Firestore`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("UID_123")

        val mockTask = mock(Task::class.java) as Task<QuerySnapshot>
        val excepcionSimulada = Exception("Sin permisos de lectura")

        `when`(mockDb.collection("usuarios").document("UID_123")
            .collection("mascotas").document("RUAC123")
            .collection("citas").get()
        ).thenReturn(mockTask)

        // Simulamos fallo
        `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        `when`(mockTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.arguments[0] as OnFailureListener
            listener.onFailure(excepcionSimulada)
            mockTask
        }

        var mensajeError = ""
        repository.obtenerCitas("RUAC123",
            onSuccess = { },
            onError = { ex -> mensajeError = ex.message ?: "" }
        )

        assertEquals("Sin permisos de lectura", mensajeError)
    }
}