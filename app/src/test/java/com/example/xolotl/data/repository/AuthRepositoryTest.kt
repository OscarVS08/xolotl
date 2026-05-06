package com.example.xolotl.data.repository

import com.example.xolotl.data.models.User
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.*
import com.example.xolotl.utils.EncryptionUtils
import org.mockito.Mockito.mockStatic

class AuthRepositoryTest {

    private lateinit var mockAuth: FirebaseAuth
    private lateinit var mockDb: FirebaseFirestore
    private lateinit var mockUser: FirebaseUser

    // Mocks para Firestore
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference

    // Necesitamos dos tipos de tareas porque Firebase devuelve cosas distintas
    private lateinit var mockAuthResultTask: Task<AuthResult>
    private lateinit var mockVoidTask: Task<Void>

    private lateinit var repo: AuthRepository
    private lateinit var mockCallback: AuthCallback

    @Before
    fun setup() {
        mockAuth = mock(FirebaseAuth::class.java)
        mockDb = mock(FirebaseFirestore::class.java)
        mockUser = mock(FirebaseUser::class.java)
        mockCallback = mock(AuthCallback::class.java)

        mockCollection = mock(CollectionReference::class.java)
        mockDocument = mock(DocumentReference::class.java)

        // 1. Simular la tarea de AuthResult (para Iniciar Sesión y Registro)
        @Suppress("UNCHECKED_CAST")
        mockAuthResultTask = mock(Task::class.java) as Task<AuthResult>
        `when`(mockAuthResultTask.addOnSuccessListener(any())).thenReturn(mockAuthResultTask)
        `when`(mockAuthResultTask.addOnFailureListener(any())).thenReturn(mockAuthResultTask)
        `when`(mockAuthResultTask.addOnCompleteListener(any())).thenReturn(mockAuthResultTask)

        // 2. Simular la tarea Void (para Recuperar Contraseña y Firestore)
        @Suppress("UNCHECKED_CAST")
        mockVoidTask = mock(Task::class.java) as Task<Void>
        `when`(mockVoidTask.addOnSuccessListener(any())).thenReturn(mockVoidTask)
        `when`(mockVoidTask.addOnFailureListener(any())).thenReturn(mockVoidTask)
        `when`(mockVoidTask.addOnCompleteListener(any())).thenReturn(mockVoidTask)

        // 3. Simular el comportamiento en cascada de Firestore: db.collection().document().set()
        `when`(mockDb.collection(anyString())).thenReturn(mockCollection)
        `when`(mockCollection.document(anyString())).thenReturn(mockDocument)
        `when`(mockDocument.set(any())).thenReturn(mockVoidTask)

        // Inicializamos el repositorio inyectándole nuestros mocks
        repo = AuthRepository(mockAuth, mockDb)
    }

    @Test
    fun `iniciarSesion llama a Firebase Auth correctamente`() {
        val email = "usuario@valido.com"
        val password = "#Password123"

        // Le decimos a Mockito que responda con la tarea simulada
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(mockAuthResultTask)

        // Ejecutamos la función real de nuestro repositorio
        repo.iniciarSesion(email, password, mockCallback)

        // Verificamos que nuestro repositorio realmente le haya pasado los datos a Firebase
        verify(mockAuth).signInWithEmailAndPassword(email, password)
    }

    @Test
    fun `registrarUsuario llama a Firebase Auth correctamente con el modelo User`() {
        val email = "nuevo@usuario.com"
        val password = "#Password123"

        // Creamos un usuario de prueba usando tu modelo de datos exacto
        val dummyUser = User(
            uid = "", // El UID se asigna después en tu lógica, así que lo dejamos vacío
            curp = "ABCD123456EFGH78",
            nombre = "Oscar",
            apellidoP = "Vaquero",
            apellidoM = "Santos",
            telefono = "5512345678",
            telefonoAlt = "5587654321",
            calle = "Avenida Siempre Viva",
            numero = "100",
            colonia = "Centro",
            alcaldia = "Gustavo A. Madero",
            codigoPostal = "07899",
            correo = email
        )

        // Simulamos la respuesta de Auth
        `when`(mockAuth.createUserWithEmailAndPassword(email, password)).thenReturn(mockAuthResultTask)

        // Ejecutamos la función
        repo.registrarUsuario(email, password, dummyUser, mockCallback)

        // Comprobamos que sí se mandó a llamar la creación del usuario en Firebase Auth
        verify(mockAuth).createUserWithEmailAndPassword(email, password)
    }

    @Test
    fun `obtenerUsuarioActual devuelve el usuario logueado`() {
        `when`(mockAuth.currentUser).thenReturn(mockUser)
        val result = repo.obtenerUsuarioActual()
        assert(result == mockUser)
    }

    @Test
    fun `obtenerUsuarioActual devuelve null si no hay sesion iniciada`() {
        `when`(mockAuth.currentUser).thenReturn(null)
        val result = repo.obtenerUsuarioActual()
        assert(result == null)
    }

    @Test
    fun `enviarCorreoRecuperacion llama a sendPasswordResetEmail en Firebase Auth`() {
        val email = "usuario@valido.com"

        `when`(mockAuth.sendPasswordResetEmail(email)).thenReturn(mockVoidTask)

        repo.enviarCorreoRecuperacion(email, mockCallback)

        verify(mockAuth).sendPasswordResetEmail(email)
    }

    @Test
    fun `enviarCorreoRecuperacion no interactua con callback en llamada inicial vacia`() {
        `when`(mockAuth.sendPasswordResetEmail(anyString())).thenReturn(mockVoidTask)

        repo.enviarCorreoRecuperacion("", mockCallback)

        // El listener no se dispara en este test básico, verificamos que no haya falsos positivos
        verifyNoInteractions(mockCallback)
    }

    @Test
    fun `iniciarSesion falla y ejecuta onError del callback`() {
        val email = "usuario@valido.com"
        val password = "passwordIncorrecta"
        val excepcionSimulada = Exception("Error de red simulado")

        // Simulamos que el addOnFailureListener se ejecuta inmediatamente con un error
        `when`(mockAuthResultTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnFailureListener>(0)
            listener.onFailure(excepcionSimulada)
            mockAuthResultTask
        }
        `when`(mockAuth.signInWithEmailAndPassword(email, password)).thenReturn(mockAuthResultTask)

        repo.iniciarSesion(email, password, mockCallback)

        // Verificamos que nuestro callback haya recibido el mensaje de error
        verify(mockCallback).onError("Error de red simulado")
    }

    @Test
    fun `registrarUsuario llama onError si el UID de Firebase es nulo`() {
        val email = "test@test.com"
        val password = "password123"
        val dummyUser = User(correo = email) // Modelo mínimo para la prueba

        // Creamos un AuthResult simulado que devuelva un usuario nulo
        val mockAuthResult = mock(AuthResult::class.java)
        `when`(mockAuthResult.user).thenReturn(null)

        // Simulamos que Firebase responde con "éxito" pero entregando ese usuario nulo
        `when`(mockAuthResultTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnSuccessListener<AuthResult>>(0)
            listener.onSuccess(mockAuthResult)
            mockAuthResultTask
        }
        `when`(mockAuth.createUserWithEmailAndPassword(email, password)).thenReturn(mockAuthResultTask)

        repo.registrarUsuario(email, password, dummyUser, mockCallback)

        // Verificamos que tu validación interna funcionó
        verify(mockCallback).onError("No se obtuvo UID del usuario")
    }

    @Test
    fun `registrarUsuario llama onError si Firestore falla al guardar los datos`() {
        val email = "test@test.com"
        val password = "password123"
        val dummyUser = User(correo = email)
        val excepcionFirestore = Exception("Error de permisos en BD")

        // Simulamos un usuario válido con un UID
        `when`(mockUser.uid).thenReturn("UID_SECRETO_123")
        val mockAuthResult = mock(AuthResult::class.java)
        `when`(mockAuthResult.user).thenReturn(mockUser)

        // Simulamos que Firebase Auth responde con éxito
        `when`(mockAuthResultTask.addOnSuccessListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnSuccessListener<AuthResult>>(0)
            listener.onSuccess(mockAuthResult)
            mockAuthResultTask
        }
        `when`(mockAuth.createUserWithEmailAndPassword(email, password)).thenReturn(mockAuthResultTask)

        // Simulamos que Firestore responde con error
        `when`(mockVoidTask.addOnFailureListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnFailureListener>(0)
            listener.onFailure(excepcionFirestore)
            mockVoidTask
        }

        // ==========================================
        // LA SOLUCIÓN: Interceptamos Base64 de Android
        // ==========================================
        mockStatic(android.util.Base64::class.java).use { mockedBase64 ->
            // Le decimos a Mockito que cuando se intente encriptar a Base64, devuelva un texto falso
            mockedBase64.`when`<String> {
                android.util.Base64.encodeToString(any(ByteArray::class.java), anyInt())
            }.thenReturn("texto_encriptado_falso")

            // Ejecutamos la función dentro de este bloque protegido
            repo.registrarUsuario(email, password, dummyUser, mockCallback)
        }

        // Verificamos que el error de la base de datos llegó a tu callback
        verify(mockCallback).onError("Error de permisos en BD")
    }

    @Test
    fun `enviarCorreoRecuperacion llama onSuccess si la tarea se completa exitosamente`() {
        val email = "usuario@valido.com"

        // Hacemos que el task simule ser exitoso
        `when`(mockVoidTask.isSuccessful).thenReturn(true)

        // Simulamos la ejecución del OnCompleteListener
        `when`(mockVoidTask.addOnCompleteListener(any())).thenAnswer { invocation ->
            val listener = invocation.getArgument<com.google.android.gms.tasks.OnCompleteListener<Void>>(0)
            listener.onComplete(mockVoidTask)
            mockVoidTask
        }
        `when`(mockAuth.sendPasswordResetEmail(email)).thenReturn(mockVoidTask)

        repo.enviarCorreoRecuperacion(email, mockCallback)

        // Comprobamos que sí avisó de éxito
        verify(mockCallback).onSuccess()
    }
}