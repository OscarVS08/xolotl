package com.example.xolotl.ui.main.mascota

import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.xolotl.R
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import android.graphics.Color


class GenerarPdfActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()

    private lateinit var txtNombre: TextView
    private lateinit var txtRuac: TextView
    private lateinit var txtEspecie: TextView
    private lateinit var txtRaza: TextView
    private lateinit var txtSexo: TextView
    private lateinit var txtFecha: TextView
    private lateinit var txtColor: TextView
    private lateinit var txtPeso: TextView
    private lateinit var txtEstatura: TextView
    private lateinit var txtAlergias: TextView
    private lateinit var txtNotas: TextView
    private lateinit var txtDueno: TextView
    private lateinit var txtTelefono: TextView
    private lateinit var txtTelAltU: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_pdf)

        val mascotaId = intent.getStringExtra("docId") ?: return
        val userId = FirebaseAuth.getInstance().uid ?: return

        // Referencias UI
        txtNombre   = findViewById(R.id.txtNombreMascotaTop)
        txtRuac     = findViewById(R.id.txtRuacTop)
        txtEspecie  = findViewById(R.id.txtEspecie)
        txtRaza     = findViewById(R.id.txtRaza)
        txtSexo     = findViewById(R.id.txtSexo)
        txtFecha    = findViewById(R.id.txtFechaAdopcion)
        txtColor    = findViewById(R.id.txtColor)
        txtPeso     = findViewById(R.id.txtPeso)
        txtEstatura = findViewById(R.id.txtEstatura)
        txtAlergias = findViewById(R.id.txtAlergias)
        txtNotas    = findViewById(R.id.txtNotas)

        txtDueno    = findViewById(R.id.txtnombreDueno)
        txtTelefono = findViewById(R.id.txtnumeroTelefonoDueno)
        txtTelAltU  = findViewById(R.id.txtnumTelAltDuen)

        // Botón home
        findViewById<View>(R.id.btnHome).setOnClickListener { finish() }

        // Botón GENERAR PDF
        findViewById<View>(R.id.btnGenerarPdf).setOnClickListener {
            generarPdf()
        }

        // --- Cargar datos de la mascota ---
        db.collection("usuarios")
            .document(userId)
            .collection("mascotas")
            .document(mascotaId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    txtNombre.text   = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    txtRuac.text     = EncryptionUtils.decrypt(doc.getString("ruac") ?: "")
                    txtEspecie.text  = EncryptionUtils.decrypt(doc.getString("especie") ?: "")
                    txtRaza.text     = EncryptionUtils.decrypt(doc.getString("raza") ?: "")
                    txtSexo.text     = EncryptionUtils.decrypt(doc.getString("sexo") ?: "")
                    txtFecha.text    = EncryptionUtils.decrypt(doc.getString("fechaAdopcion") ?: "")
                    txtColor.text    = EncryptionUtils.decrypt(doc.getString("color") ?: "")
                    txtPeso.text     = EncryptionUtils.decrypt(doc.getString("peso") ?: "")
                    txtEstatura.text = EncryptionUtils.decrypt(doc.getString("estatura") ?: "")
                    txtAlergias.text = EncryptionUtils.decrypt(doc.getString("alergias") ?: "")
                    txtNotas.text    = EncryptionUtils.decrypt(doc.getString("notas") ?: "")
                }
            }

        // --- Cargar datos del dueño ---
        db.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val nombre = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    val apP = EncryptionUtils.decrypt(doc.getString("apellidoP") ?: "")
                    val apM = EncryptionUtils.decrypt(doc.getString("apellidoM") ?: "")

                    txtDueno.text = "$nombre $apP $apM"
                    txtTelefono.text = EncryptionUtils.decrypt(doc.getString("telefono") ?: "")
                    txtTelAltU.text = EncryptionUtils.decrypt(doc.getString("telefonoAlt") ?: "")
                }
            }
    }

    // ---------------------------------------------------------
    // ➤ AQUÍ SE GENERA EL PDF Y SE MUESTRA LA ALERTA
    // ---------------------------------------------------------
    private fun generarPdf() {
        try {
            // Documento
            val pdf = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(500, 800, 1).create()
            val page = pdf.startPage(pageInfo)

            val canvas = page.canvas

            // Paints
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 26f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
            }

            val headerPaint = Paint().apply {
                color = Color.parseColor("#3F51B5")  // azul tipo Material
            }

            val cardPaint = Paint().apply {
                color = Color.WHITE
            }

            val labelPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                isFakeBoldText = true
            }

            val valuePaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
            }

            val dividerPaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 2f
            }

            //  Encabezado azul
            canvas.drawRect(0f, 0f, pageInfo.pageWidth.toFloat(), 120f, headerPaint)

            // Título centrado
            canvas.drawText(
                "Carnet de Mascota",
                (pageInfo.pageWidth / 2).toFloat(),
                70f,
                titlePaint
            )

            // Fondo de la tarjeta
            val cardLeft = 30f
            val cardTop = 140f
            val cardRight = pageInfo.pageWidth - 30f
            val cardBottom = 700f

            canvas.drawRoundRect(
                cardLeft,
                cardTop,
                cardRight,
                cardBottom,
                25f,
                25f,
                cardPaint
            )

            // Inserción de campos
            var y = cardTop + 60f
            val xLabel = cardLeft + 30f
            val xValue = cardLeft + 180f

            fun drawField(label: String, value: String) {
                canvas.drawText(label, xLabel, y, labelPaint)
                canvas.drawText(value, xValue, y, valuePaint)
                y += 35
                canvas.drawLine(cardLeft + 20f, y, cardRight - 20f, y, dividerPaint)
                y += 25
            }

            drawField("Nombre:", txtNombre.text.toString())
            drawField("RUAC:", txtRuac.text.toString())
            drawField("Especie:", txtEspecie.text.toString())
            drawField("Raza:", txtRaza.text.toString())
            drawField("Sexo:", txtSexo.text.toString())
            drawField("Color:", txtColor.text.toString())
            drawField("Dueño:", txtDueno.text.toString())

            pdf.finishPage(page)

            // Guardar
            val file = File(getExternalFilesDir(null), "CarnetMascota.pdf")
            val output = FileOutputStream(file)
            pdf.writeTo(output)
            output.close()
            pdf.close()

            // Alerta de éxito → abrir visor
            UiUtils.mostrarAlertaPdfGenerado(this) {
                abrirPdfConVisor(file)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            UiUtils.mostrarAlertaPdfError(this) {}
        }
    }

    private fun abrirPdfConVisor(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(intent)

        } catch (e: Exception) {
            e.printStackTrace()

            UiUtils.mostrarAlertaPdfError(this) {}
        }
    }

}
