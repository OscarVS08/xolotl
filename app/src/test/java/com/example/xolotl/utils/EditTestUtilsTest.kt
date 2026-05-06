package com.example.xolotl.utils

import android.content.Context
import android.widget.EditText
import androidx.core.content.ContextCompat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class EditTestUtilsTest {

    @Mock
    private lateinit var mockEditText: EditText

    @Mock
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        // Le decimos al EditText que devuelva nuestro contexto mockeado
        `when`(mockEditText.context).thenReturn(mockContext)
    }

    @Test
    fun `updateTextColor cambia a negro cuando el valor es valido`() {
        // Mockeamos la llamada estática a ContextCompat para evitar errores de Android
        mockStatic(ContextCompat::class.java).use { mockedContextCompat ->
            val colorNegro = 0xFF000000.toInt()

            // Cuando pidan el color negro de Android, devolvemos nuestro valor de prueba
            mockedContextCompat.`when`<Int> {
                ContextCompat.getColor(eq(mockContext), eq(android.R.color.black))
            }.thenReturn(colorNegro)

            // EJECUCIÓN: Llamamos a la función de extensión
            mockEditText.updateTextColor(true)

            // VERIFICACIÓN: Comprobamos que se llamó a setTextColor con el color negro
            verify(mockEditText).setTextColor(colorNegro)
        }
    }

    @Test
    fun `updateTextColor cambia a gris oscuro cuando el valor es invalido`() {
        mockStatic(ContextCompat::class.java).use { mockedContextCompat ->
            val colorGris = 0xFF444444.toInt()

            mockedContextCompat.`when`<Int> {
                ContextCompat.getColor(eq(mockContext), eq(android.R.color.darker_gray))
            }.thenReturn(colorGris)

            // EJECUCIÓN
            mockEditText.updateTextColor(false)

            // VERIFICACIÓN
            verify(mockEditText).setTextColor(colorGris)
        }
    }
}