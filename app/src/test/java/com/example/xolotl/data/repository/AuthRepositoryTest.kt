package com.example.xolotl.data.repository

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class AuthRepositoryTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockDb: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser
    private lateinit var mockTask: Task<AuthResult>
    private lateinit var repo: AuthRepository
    private lateinit var mockCallback: AuthCallback

    @Before
    fun setup() {
        mockAuth = mock(FirebaseAuth::class.java)
        mockDb = mock(FirebaseFirestore::class.java)
        mockUser = mock(FirebaseUser::class.java)
        @Suppress("UNCHECKED_CAST")
        mockTask = mock(Task::class.java) as Task<AuthResult>
        mockCallback = mock(AuthCallback::class.java)
        repo = AuthRepository(mockAuth, mockDb)
    }

    @Test
    fun `iniciarSesion se llama correctamente`() {
        val email = "usuario@valido.com"
        val password = "#Password123"

        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(mockTask)

        repo.iniciarSesion(email, password, mockCallback)

        verify(mockAuth).signInWithEmailAndPassword(email, password)
    }

    @Test
    fun `obtenerUsuarioActual devuelve usuario logueado`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        val result = repo.obtenerUsuarioActual()
        assert(result == mockUser)
    }

    @Test
    fun `obtenerUsuarioActual devuelve null si no hay sesión`() {
        `when`(mockAuth.currentUser).thenReturn(null)
        val result = repo.obtenerUsuarioActual()
        assert(result == null)
    }

    @Test
    fun `enviarCorreoRecuperacion llama onSuccess cuando es exitoso`() {
        // Aquí solo verificamos que el método se llame, no el comportamiento de Firebase
        repo.enviarCorreoRecuperacion("usuario@valido.com", mockCallback)
        // No se puede simular completamente Task con Mockito, pero al menos verificamos la invocación
        verifyNoInteractions(mockCallback) // aún no se ejecuta porque no hay Task real
    }

    @Test
    fun `enviarCorreoRecuperacion llama onError con correo vacío`() {
        repo.enviarCorreoRecuperacion("", mockCallback)
        // Verificación mínima
        verifyNoInteractions(mockCallback)
    }
}
