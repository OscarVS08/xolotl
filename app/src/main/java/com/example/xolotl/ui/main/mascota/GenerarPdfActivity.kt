package com.example.xolotl.ui.main.mascota

import android.content.Intent
import android.graphics.Canvas
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
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView


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
    private lateinit var imgFotoMascota: ImageView

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
        imgFotoMascota = findViewById(R.id.imgFotoMascotaTop)



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
                    //txtRuac.text     = EncryptionUtils.decrypt(doc.getString("ruac") ?: "")
                    txtRuac.text     = "PHCXA49024"
                    txtEspecie.text  = EncryptionUtils.decrypt(doc.getString("especie") ?: "")
                    txtRaza.text     = EncryptionUtils.decrypt(doc.getString("raza") ?: "")
                    txtSexo.text     = EncryptionUtils.decrypt(doc.getString("sexo") ?: "")
                    txtFecha.text    = EncryptionUtils.decrypt(doc.getString("fechaAdopcion") ?: "")
                    txtColor.text    = EncryptionUtils.decrypt(doc.getString("color") ?: "")
                    txtPeso.text     = EncryptionUtils.decrypt(doc.getString("peso") ?: "")
                    txtEstatura.text = EncryptionUtils.decrypt(doc.getString("estatura") ?: "")
                    txtAlergias.text = EncryptionUtils.decrypt(doc.getString("alergias") ?: "")
                    txtNotas.text    = EncryptionUtils.decrypt(doc.getString("notas") ?: "")

                    val fotoCifrada = doc.getString("fotoBase64") ?: ""

                    if (fotoCifrada.isNotEmpty()) {
                        try {
                            // 1. Desencriptar para obtener el Base64 original
                            val fotoBase64 = EncryptionUtils.decrypt(fotoCifrada)

                            // 2. Decodificar Base64 → bytes
                            val bytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)

                            // 3. Convertir a bitmap
                            val bitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

                            // 4. Mostrar en ImageView
                            imgFotoMascota.setImageBitmap(bitmap)

                        } catch (e: Exception) {
                            e.printStackTrace()
                            imgFotoMascota.setImageResource(R.drawable.fondo_logo_circular)
                        }
                    } else {
                        imgFotoMascota.setImageResource(R.drawable.fondo_logo_circular)
                    }
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
            val pdf = PdfDocument()

            // Tamaño carta: 612 x 792 pt (tipico PDF)
            val pageWidth = 612
            val pageHeight = 792

            lateinit var page: PdfDocument.Page
            lateinit var canvas: Canvas

            var y = 0f

            fun addNewPage() {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdf.pages.size + 1).create()
                page = pdf.startPage(pageInfo)
                canvas = page.canvas
                y = 40f
            }

            addNewPage()

            // Paints
            val labelPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 14f
                isFakeBoldText = true
            }

            val valuePaint = Paint().apply {
                color = Color.BLACK
                textSize = 16f
            }

            val titlePaint = Paint().apply {
                color = Color.parseColor("#3F51B5")
                textSize = 26f
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
            }

            val dividerPaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 2f
            }

            // Título
            canvas.drawText("Carnet de Mascota", (pageWidth / 2).toFloat(), y, titlePaint)
            y += 40

            // FOTO + NOMBRE + RUAC
            var bottomOfPhoto = 0f
            var photoRightX = 0f

            imgFotoMascota.drawable?.let { drawable ->
                val bitmap = (drawable as BitmapDrawable).bitmap

                val photoWidth = 180f
                val photoHeight = 180f

                val left = 40f
                val top = y

                val dest = RectF(left, top, left + photoWidth, top + photoHeight)
                canvas.drawBitmap(bitmap, null, dest, null)

                bottomOfPhoto = top + photoHeight
                photoRightX = dest.right + 20f

                // Texto a la derecha de la foto
                canvas.drawText("Nombre:", photoRightX, top + 30f, labelPaint)
                canvas.drawText(txtNombre.text.toString(), photoRightX + 120f, top + 30f, valuePaint)


                canvas.drawText("RUAC:", photoRightX, top + 65f, labelPaint)
                //canvas.drawText(txtRuac.text.toString(), photoRightX + 120f, top + 65f, valuePaint)
                canvas.drawText("PHCXA49024", photoRightX + 120f, top + 65f, valuePaint)

                y = bottomOfPhoto + 40f
            }

            fun ensureSpace(extra: Float = 40f) {
                if (y + extra >= pageHeight - 40) {
                    pdf.finishPage(page)
                    addNewPage()
                }
            }

            fun drawField(label: String, value: String) {
                ensureSpace(50f)
                canvas.drawText(label, 40f, y, labelPaint)
                canvas.drawText(value, 200f, y, valuePaint)
                y += 22
                canvas.drawLine(30f, y, pageWidth - 30f, y, dividerPaint)
                y += 20
            }

            fun drawMultiLine(label: String, text: String) {
                ensureSpace(80f)

                canvas.drawText(label, 40f, y, labelPaint)
                y += 25

                val maxWidth = (pageWidth - 80).toFloat()
                val words = text.split(" ")
                var line = ""

                for (w in words) {
                    val test = if (line.isEmpty()) w else "$line $w"
                    if (valuePaint.measureText(test) > maxWidth) {
                        ensureSpace(30f)
                        canvas.drawText(line, 60f, y, valuePaint)
                        y += 22
                        line = w
                    } else {
                        line = test
                    }
                }

                if (line.isNotEmpty()) {
                    ensureSpace(30f)
                    canvas.drawText(line, 60f, y, valuePaint)
                    y += 22
                }

                canvas.drawLine(30f, y, pageWidth - 30f, y, dividerPaint)
                y += 20
            }

            // CAMPOS
            drawField("Especie:", txtEspecie.text.toString())
            drawField("Raza:", txtRaza.text.toString())
            drawField("Sexo:", txtSexo.text.toString())
            drawField("Color:", txtColor.text.toString())
            drawField("Peso:", txtPeso.text.toString())
            drawField("Estatura:", txtEstatura.text.toString())
            drawField("Fecha adopción:", txtFecha.text.toString())

            drawField("Dueño:", txtDueno.text.toString())
            drawField("Tel. dueño:", txtTelefono.text.toString())
            drawField("Tel. alternativo:", txtTelAltU.text.toString())

            drawMultiLine("Alergias:", txtAlergias.text.toString())
            drawMultiLine("Notas:", txtNotas.text.toString())

            pdf.finishPage(page)

            // Guardar PDF
            val file = File(getExternalFilesDir(null), "CarnetMascota.pdf")
            val output = FileOutputStream(file)
            pdf.writeTo(output)
            output.close()
            pdf.close()

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
