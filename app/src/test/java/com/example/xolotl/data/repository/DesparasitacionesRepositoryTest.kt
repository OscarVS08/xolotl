package com.example.xolotl.data.repository

import com.example.xolotl.data.models.Desparasitaciones
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

class DesparasitacionRepositoryTest {

    private lateinit var mockDb: FirebaseFirestore
    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockUser: FirebaseUser
    private lateinit var repository: DesparasitacionRepository

    @Before
    fun setup() {
        // Deep stubs permite mockear cadenas largas como db.collection().document().collection()...
        mockDb = mock(FirebaseFirestore::class.java, RETURNS_DEEP_STUBS)
        mockAuth = mock(FirebaseAuth::class.java)
        mockUser = mock(FirebaseUser::class.java)

        repository = DesparasitacionRepository(mockDb, mockAuth)
    }

    // ==========================================
    // PRUEBAS DE REGISTRO
    // ==========================================

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `registrarDesparasitacion tiene exito`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("USER123")

        val desparasitacion = Desparasitaciones(nombre = "Bravecto")
        val mockTask = mock(Task::class.java) as Task<Void>

        `when`(mockDb.collection("usuarios").document("USER123")
            .collection("mascotas").document("RUAC_MAX")
            .collection("desparasitaciones").document().set(desparasitacion)
        ).thenReturn(mockTask)

        `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
            (it.arguments[0] as OnSuccessListener<Void>).onSuccess(null)
            mockTask
        }
        `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        var exito = false
        repository.registrarDesparasitacion("RUAC_MAX", desparasitacion, { exito = true }, {})

        assertTrue(exito)
    }

    // ==========================================
    // PRUEBAS DE OBTENCIÓN
    // ==========================================

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `obtenerDesparasitaciones notifica error si el usuario no esta autenticado`() {
        `when`(mockAuth.currentUser).thenReturn(null)

        var errorMensaje = ""
        repository.obtenerDesparasitaciones("RUAC_MAX", {}, { errorMensaje = it.message ?: "" })

        assertEquals("Usuario no autenticado", errorMensaje)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `obtenerDesparasitaciones falla cuando Firestore da error`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("USER123")

        val mockTask = mock(Task::class.java) as Task<QuerySnapshot>
        val exception = Exception("Error de lectura")

        `when`(mockDb.collection("usuarios").document("USER123")
            .collection("mascotas").document("RUAC_MAX")
            .collection("desparasitaciones").get()
        ).thenReturn(mockTask)

        `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        `when`(mockTask.addOnFailureListener(any())).thenAnswer {
            (it.arguments[0] as OnFailureListener).onFailure(exception)
            mockTask
        }

        var errorMensaje = ""
        repository.obtenerDesparasitaciones("RUAC_MAX", {}, { errorMensaje = it.message ?: "" })

        assertEquals("Error de lectura", errorMensaje)
    }

    // ==========================================
    // PRUEBAS DE ELIMINACIÓN
    // ==========================================

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `eliminarDesparasitacion tiene exito`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("USER123")

        val mockTask = mock(Task::class.java) as Task<Void>

        `when`(mockDb.collection("usuarios").document("USER123")
            .collection("mascotas").document("RUAC_MAX")
            .collection("desparasitaciones").document("ID_DOC").delete()
        ).thenReturn(mockTask)

        `when`(mockTask.addOnSuccessListener(any())).thenAnswer {
            (it.arguments[0] as OnSuccessListener<Void>).onSuccess(null)
            mockTask
        }
        `when`(mockTask.addOnFailureListener(any())).thenReturn(mockTask)

        var exito = false
        repository.eliminarDesparasitacion("RUAC_MAX", "ID_DOC", { exito = true }, {})

        assertTrue(exito)
    }

    @Test
    @Suppress("UNCHECKED_CAST")
    fun `eliminarDesparasitacion falla por falta de permisos`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        `when`(mockUser.uid).thenReturn("USER123")

        val mockTask = mock(Task::class.java) as Task<Void>
        val exception = Exception("Permiso denegado")

        `when`(mockDb.collection("usuarios").document("USER123")
            .collection("mascotas").document("RUAC_MAX")
            .collection("desparasitaciones").document("ID_DOC").delete()
        ).thenReturn(mockTask)

        `when`(mockTask.addOnSuccessListener(any())).thenReturn(mockTask)
        `when`(mockTask.addOnFailureListener(any())).thenAnswer {
            (it.arguments[0] as OnFailureListener).onFailure(exception)
            mockTask
        }

        var errorMensaje = ""
        repository.eliminarDesparasitacion("RUAC_MAX", "ID_DOC", {}, { errorMensaje = it.message ?: "" })

        assertEquals("Permiso denegado", errorMensaje)
    }
}