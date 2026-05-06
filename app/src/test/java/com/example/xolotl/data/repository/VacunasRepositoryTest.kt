package com.example.xolotl.data.repository

import com.example.xolotl.data.models.Vacunas
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*

class VacunasRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var repository: VacunasRepository

    @Before
    fun setup() {
        // RETURNS_DEEP_STUBS nos ahorra mockear cada paso de la colección
        mockDb = mock(FirebaseFirestore::class.java, RETURNS_DEEP_STUBS)
        mockAuth = mock(FirebaseAuth::class.java)
        mockUser = mock(FirebaseUser::class.java)

        repository = VacunasRepository(mockDb, mockAuth)
    }

    // ==========================================
    // PRUEBAS DE REGISTRO
    // ==========================================

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `registrarVacuna tiene exito`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("USER_X")

        val vacuna = Vacunas(nombre = "Antirrábica", marca = "Nobivac")
        val mockTask = mock(Task::class.java) as Task<Void>

        `when`(mockDb.collection("usuarios").document("USER_X")
            .collection("mascotas").document("RUAC123")
            .collection("vacunas").document().set(vacuna)
        ).thenReturn(mockTask)

        // Simular éxito
        `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
            (it.arguments[0] as OnSuccessListener<Void>).onSuccess(null)
            mockTask
        }
        `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        var exito = false
        repository.registrarVacuna("RUAC123", vacuna, { exito = true }, {})

        assertTrue(exito)
    }

    @Test
    fun `registrarVacuna falla cuando no hay usuario autenticado`() {
        `when`(mockAuth.currentUser).thenReturn(null)

        var errorMensaje = ""
        repository.registrarVacuna("RUAC123", Vacunas(), {}, { errorMensaje = it.message ?: "" })

        assertEquals("Usuario no autenticado", errorMensaje)
    }

    // ==========================================
    // PRUEBAS DE OBTENCIÓN
    // ==========================================

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `obtenerVacunas devuelve lista correctamente`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("USER_X")

        val mockTask = mock(Task::class.java) as Task<QuerySnapshot>
        val mockSnapshot = mock(QuerySnapshot::class.java)

        // --- ESTA ES LA CORRECCIÓN CLAVE ---
        // Le decimos al snapshot que devuelva un iterador vacío para que el .map { } no truene
        `when`(mockSnapshot.iterator()).thenReturn(mutableListOf<com.google.firebase.firestore.QueryDocumentSnapshot>().iterator())

        `when`(mockDb.collection("usuarios").document("USER_X")
            .collection("mascotas").document("RUAC123")
            .collection("vacunas").get()
        ).thenReturn(mockTask)

        // Simular éxito de la consulta
        `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
            (it.arguments[0] as OnSuccessListener<QuerySnapshot>).onSuccess(mockSnapshot)
            mockTask
        }
        `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        var listaRecibida: List<Vacunas>? = null
        repository.obtenerVacunas("RUAC123", { listaRecibida = it }, {})

        // Ahora el test pasará porque el iterador ya no es null
        assertTrue(listaRecibida != null)
    }

    // ==========================================
    // PRUEBAS DE ELIMINACIÓN
    // ==========================================

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `eliminarVacuna tiene exito`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("USER_X")

        val mockTask = mock(Task::class.java) as Task<Void>

        `when`(mockDb.collection("usuarios").document("USER_X")
            .collection("mascotas").document("RUAC123")
            .collection("vacunas").document("ID_VACUNA").delete()
        ).thenReturn(mockTask)

        `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
            (it.arguments[0] as OnSuccessListener<Void>).onSuccess(null)
            mockTask
        }
        `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        var eliminado = false
        repository.eliminarVacuna("RUAC123", "ID_VACUNA", { eliminado = true }, {})

        assertTrue(eliminado)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `eliminarVacuna falla cuando Firestore da error`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("USER_X")

        val mockTask = mock(Task::class.java) as Task<Void>
        val excepcion = Exception("Error de red")

        `when`(mockDb.collection("usuarios").document("USER_X")
            .collection("mascotas").document("RUAC123")
            .collection("vacunas").document("ID_VACUNA").delete()
        ).thenReturn(mockTask)

        `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        `when`(mockTask.addOnFailureListener(any())).thenAnswer {
            (it.arguments[0] as OnFailureListener).onFailure(excepcion)
            mockTask
        }

        var errorMensaje = ""
        repository.eliminarVacuna("RUAC123", "ID_VACUNA", {}, { errorMensaje = it.message ?: "" })

        assertEquals("Error de red", errorMensaje)
    }
}