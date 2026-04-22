package com.example.xolotl.ui.main.mascota

import android.R.id.italic
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.xolotl.R
import com.example.xolotl.data.models.Desparasitaciones
import com.example.xolotl.data.models.Vacunas
import com.example.xolotl.data.models.Citas // Asegúrate de tener este modelo
import com.example.xolotl.utils.EncryptionUtils
import com.example.xolotl.utils.UiUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream

class GenerarPdfActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var userId: String
    private lateinit var mascotaId: String

    // UI Referencias
    private lateinit var txtNombre: TextView
    private lateinit var txtRuac: TextView
    private lateinit var txtEspecie: TextView
    private lateinit var txtRaza: TextView
    private lateinit var txtSexo: TextView
    private lateinit var txtFechaNac: TextView
    private lateinit var txtFechaAdop: TextView
    private lateinit var txtColor: TextView
    private lateinit var txtPeso: TextView
    private lateinit var txtEstatura: TextView
    private lateinit var txtAlergias: TextView
    private lateinit var txtNotas: TextView
    private lateinit var txtDueno: TextView
    private lateinit var txtTelefono: TextView
    private lateinit var txtTelAltU: TextView
    private lateinit var imgFotoMascota: ImageView

    private val listaDesparasitaciones = mutableListOf<Desparasitaciones>()
    private val listaVacunas = mutableListOf<Vacunas>()
    private val listaCitas = mutableListOf<Citas>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generar_pdf)

        mascotaId = intent.getStringExtra("docId") ?: return
        userId = FirebaseAuth.getInstance().uid ?: return

        initViews()
        cargarDatosMascotaYTablas()

        findViewById<View>(R.id.btnHome).setOnClickListener { finish() }
        findViewById<View>(R.id.btnGenerarPdf).setOnClickListener { generarPdf() }
    }

    private fun initViews() {
        txtNombre = findViewById(R.id.txtNombreMascotaTop)
        txtRuac = findViewById(R.id.txtRuacTop)
        txtEspecie = findViewById(R.id.txtEspecie)
        txtRaza = findViewById(R.id.txtRaza)
        txtSexo = findViewById(R.id.txtSexo)
        txtFechaNac = findViewById(R.id.txtFechaNacimiento)
        txtFechaAdop = findViewById(R.id.txtFechaAdopcion)
        txtColor = findViewById(R.id.txtColor)
        txtPeso = findViewById(R.id.txtPeso)
        txtEstatura = findViewById(R.id.txtEstatura)
        txtAlergias = findViewById(R.id.txtAlergias)
        txtNotas = findViewById(R.id.txtNotas)
        imgFotoMascota = findViewById(R.id.imgFotoMascotaTop)
        txtDueno = findViewById(R.id.txtnombreDueno)
        txtTelefono = findViewById(R.id.txtnumeroTelefonoDueno)
        txtTelAltU = findViewById(R.id.txtnumTelAltDuen)
    }

    private fun cargarDatosMascotaYTablas() {
        // 1. Datos principales
        db.collection("usuarios").document(userId).collection("mascotas").document(mascotaId)
            .get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    txtNombre.text = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                    txtRuac.text = EncryptionUtils.decrypt(doc.getString("ruac") ?: "")
                    txtEspecie.text = EncryptionUtils.decrypt(doc.getString("especie") ?: "")
                    txtRaza.text = EncryptionUtils.decrypt(doc.getString("raza") ?: "")
                    txtSexo.text = EncryptionUtils.decrypt(doc.getString("sexo") ?: "")
                    txtFechaNac.text = EncryptionUtils.decrypt(doc.getString("fechaNacimiento") ?: "Desconocido")
                    txtFechaAdop.text = EncryptionUtils.decrypt(doc.getString("fechaAdopcion") ?: "")
                    txtColor.text = EncryptionUtils.decrypt(doc.getString("color") ?: "")
                    txtPeso.text = EncryptionUtils.decrypt(doc.getString("peso") ?: "") + " kg"
                    txtEstatura.text = EncryptionUtils.decrypt(doc.getString("estatura") ?: "") + " cm"
                    txtAlergias.text = EncryptionUtils.decrypt(doc.getString("alergias") ?: "Ninguna")
                    txtNotas.text = EncryptionUtils.decrypt(doc.getString("notas") ?: "Sin notas")

                    // Lógica Anti-Crash Foto
                    val dataFoto = doc.getString("fotoBase64") ?: ""
                    if (dataFoto.isNotEmpty()) {
                        try {
                            val fotoBase64 = try { EncryptionUtils.decrypt(dataFoto) } catch (e: Exception) { dataFoto }
                            val bytes = android.util.Base64.decode(fotoBase64, android.util.Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            imgFotoMascota.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            imgFotoMascota.setImageResource(R.drawable.foto_blanco)
                        }
                    }
                }
            }

        // 2. Tablas (Vacunas, Desparasitaciones y Citas)
        db.collection("usuarios").document(userId).collection("mascotas").document(mascotaId)
            .collection("desparasitaciones").get()
            .addOnSuccessListener { res ->
                listaDesparasitaciones.clear()
                for (d in res) {
                    val des = d.toObject(Desparasitaciones::class.java)
                    // Creamos una copia con los datos descifrados
                    val desDescifrada = des.copy(
                        tipo = EncryptionUtils.decrypt(des.tipo),
                        nombre = EncryptionUtils.decrypt(des.nombre),
                        marca = EncryptionUtils.decrypt(des.marca),
                        fecha = EncryptionUtils.decrypt(des.fecha),
                        proximaFecha = EncryptionUtils.decrypt(des.proximaFecha)
                    )
                    listaDesparasitaciones.add(desDescifrada)
                }
            }

        db.collection("usuarios").document(userId).collection("mascotas").document(mascotaId)
            .collection("vacunas").get()
            .addOnSuccessListener { res ->
                listaVacunas.clear()
                for (d in res) {
                    val vac = d.toObject(Vacunas::class.java)
                    // Creamos una copia con los datos descifrados
                    val vacDescifrada = vac.copy(
                        nombre = EncryptionUtils.decrypt(vac.nombre),
                        marca = EncryptionUtils.decrypt(vac.marca),
                        dosis = EncryptionUtils.decrypt(vac.dosis),
                        fecha = EncryptionUtils.decrypt(vac.fecha),
                        proximaFecha = EncryptionUtils.decrypt(vac.proximaFecha)
                    )
                    listaVacunas.add(vacDescifrada)
                }
            }
        db.collection("usuarios").document(userId)
            .collection("mascotas").document(mascotaId)
            .collection("citas").get()
            .addOnSuccessListener { res ->
                listaCitas.clear()
                for (d in res) {
                    val cita = d.toObject(Citas::class.java)
                    // Creamos la copia descifrada
                    val citaDescifrada = cita.copy(
                        servicio = EncryptionUtils.decrypt(cita.servicio),
                        horario = EncryptionUtils.decrypt(cita.horario),
                        notas = EncryptionUtils.decrypt(cita.notas)
                    )
                    listaCitas.add(citaDescifrada)
                }
            }

        // 3. Dueño
        db.collection("usuarios").document(userId).get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                val n = EncryptionUtils.decrypt(doc.getString("nombre") ?: "")
                val p = EncryptionUtils.decrypt(doc.getString("apellidoP") ?: "")
                val m = EncryptionUtils.decrypt(doc.getString("apellidoM") ?: "")
                txtDueno.text = "$n $p $m"
                txtTelefono.text = EncryptionUtils.decrypt(doc.getString("telefono") ?: "")
                txtTelAltU.text = EncryptionUtils.decrypt(doc.getString("telefonoAlt") ?: "")
            }
        }
    }

    private fun generarPdf() {
        try {
            val pdf = PdfDocument()
            val pageWidth = 612
            val pageHeight = 792
            var pageCount = 1

            // --- CONFIGURACIÓN DE PINCEL Y COLORES ---
            val mainColor = Color.parseColor("#1A237E") // Azul Profundo
            val accentColor = Color.parseColor("#E8EAF6") // Azul muy claro para fondos
            val dividerColor = Color.parseColor("#D1D1D1")

            val titlePaint = Paint().apply { color = mainColor; textSize = 24f; isFakeBoldText = true; isAntiAlias = true; textAlign = Paint.Align.CENTER }
            val headerPaint = Paint().apply { color = mainColor; textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
            val labelPaint = Paint().apply { color = Color.parseColor("#546E7A"); textSize = 10f; isFakeBoldText = true; isAntiAlias = true }
            val valuePaint = Paint().apply { color = Color.BLACK; textSize = 11f; isAntiAlias = true }
            val footerPaint = Paint().apply { color = Color.GRAY; textSize = 9f; isAntiAlias = true; textAlign = Paint.Align.CENTER }

            lateinit var page: PdfDocument.Page
            lateinit var canvas: Canvas
            var y = 0f

            fun addNewPage() {
                val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageCount++).create()
                page = pdf.startPage(pageInfo)
                canvas = page.canvas

                // Dibujar un borde decorativo fino a toda la página
                val margin = 20f
                val borderPaint = Paint().apply { color = mainColor; style = Paint.Style.STROKE; strokeWidth = 0.5f; alpha = 50 }
                canvas.drawRect(margin, margin, pageWidth - margin, pageHeight - margin, borderPaint)

                y = 60f
            }

            addNewPage()

            // 1. ENCABEZADO ELEGANTE
            canvas.drawText("CARNET DE IDENTIDAD CANINA", (pageWidth / 2).toFloat(), y, titlePaint)
            y += 10
            val linePaint = Paint().apply { color = mainColor; strokeWidth = 2.5f }
            canvas.drawLine(pageWidth * 0.1f, y, pageWidth * 0.9f, y, linePaint)
            y += 40

            // 2. SECCIÓN DE PERFIL (FOTO Y DATOS PRINCIPALES)
            imgFotoMascota.drawable?.let { drawable ->
                val bitmap = (drawable as BitmapDrawable).bitmap

                // Foto con bordes redondeados
                val photoRect = RectF(50f, y, 170f, y + 120f)
                val paintPhoto = Paint().apply { isAntiAlias = true }

                // Dibujar sombra suave bajo la foto
                val shadowPaint = Paint().apply { color = Color.BLACK; alpha = 20; maskFilter = BlurMaskFilter(5f, BlurMaskFilter.Blur.NORMAL) }
                canvas.drawRoundRect(photoRect.left + 3, photoRect.top + 3, photoRect.right + 3, photoRect.bottom + 3, 15f, 15f, shadowPaint)

                canvas.drawBitmap(bitmap, null, photoRect, paintPhoto)

                // Marco de la foto
                val framePaint = Paint().apply { color = mainColor; style = Paint.Style.STROKE; strokeWidth = 1.5f }
                canvas.drawRoundRect(photoRect, 15f, 15f, framePaint)

                val textX = 190f
                // Nombre de la mascota (Grande)
                canvas.drawText("NOMBRE DEL EJEMPLAR", textX, y + 15, labelPaint)
                canvas.drawText(txtNombre.text.toString().uppercase(), textX, y + 40, Paint(valuePaint).apply { textSize = 22f; isFakeBoldText = true; color = mainColor })

                // RUAC
                canvas.drawText("FOLIO RUAC OFICIAL", textX, y + 75, labelPaint)
                canvas.drawText(txtRuac.text.toString(), textX, y + 95, Paint(valuePaint).apply { textSize = 14f; letterSpacing = 0.1f })

                y += 150f
            }

            fun checkNewPage(needed: Float) {
                if (y + needed > pageHeight - 60) {
                    // Dibujar pie de página antes de cerrar
                    canvas.drawText("Página ${pageCount - 1} | Sistema Xolotl v2.0", (pageWidth / 2).toFloat(), pageHeight - 35f, footerPaint)
                    pdf.finishPage(page)
                    addNewPage()
                }
            }

            fun drawInfoGrid(label: String, value: String, xPos: Float, currentY: Float) {
                canvas.drawText(label, xPos, currentY, labelPaint)
                canvas.drawText(value, xPos, currentY + 18, valuePaint)
            }

            // 3. CUADRÍCULA DE INFORMACIÓN (Más limpia que filas largas)
            checkNewPage(120f)
            val bgRect = RectF(40f, y, (pageWidth - 40).toFloat(), y + 110f)
            canvas.drawRoundRect(bgRect, 10f, 10f, Paint().apply { color = accentColor })

            y += 25f
            drawInfoGrid("ESPECIE", txtEspecie.text.toString(), 60f, y)
            drawInfoGrid("RAZA", txtRaza.text.toString(), 220f, y)
            drawInfoGrid("SEXO", txtSexo.text.toString(), 400f, y)

            y += 45f
            drawInfoGrid("NACIMIENTO", txtFechaNac.text.toString(), 60f, y)
            drawInfoGrid("PESO / TALLA", "${txtPeso.text} / ${txtEstatura.text}", 220f, y)
            drawInfoGrid("COLOR", txtColor.text.toString(), 400f, y)

            y += 60f

            // 4. TABLAS PROFESIONALES (CON LÓGICA DE CITAS CORREGIDA)
            fun drawStyledTable(title: String, headers: List<String>, data: List<List<String>>) {
                checkNewPage(100f)
                y += 20f
                canvas.drawText(title, 40f, y, headerPaint)
                y += 10f

                val colWidth = (pageWidth - 80) / headers.size

                // Header fondo redondeado
                val headRect = RectF(40f, y, (pageWidth - 40).toFloat(), y + 22)
                canvas.drawRoundRect(headRect, 5f, 5f, Paint().apply { color = mainColor })

                headers.forEachIndexed { i, h ->
                    val hPaint = Paint(labelPaint).apply { color = Color.WHITE; textSize = 9f }
                    canvas.drawText(h, 50f + (i * colWidth), y + 15, hPaint)
                }

                y += 22f

                if (data.isEmpty()) {
                    y += 20f
                    canvas.drawText("No se registran datos históricos.", 55f, y, Paint(valuePaint).apply {
                        color = Color.GRAY
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                    })
                    y += 10f
                } else {
                    data.forEachIndexed { index, row ->
                        checkNewPage(25f)
                        // Fila cebra (un gris muy leve para alternar)
                        if (index % 2 != 0) {
                            canvas.drawRect(40f, y, (pageWidth - 40).toFloat(), y + 20, Paint().apply { color = accentColor; alpha = 120 })
                        }

                        row.forEachIndexed { i, text ->
                            // it.horario y it.notas se manejan aquí
                            val cleanText = if(text.length > 20) text.take(17) + "..." else text
                            canvas.drawText(cleanText, 50f + (i * colWidth), y + 14, valuePaint)
                        }
                        y += 20f
                    }
                }
                y += 15f
            }

            // --- DIBUJAR LAS SECCIONES ---
            drawStyledTable("HISTORIAL DE VACUNACIÓN", listOf("VACUNA", "MARCA", "DOSIS", "FECHA", "PRÓX."), listaVacunas.map { listOf(it.nombre, it.marca, it.dosis, it.fecha, it.proximaFecha) })

            drawStyledTable("CONTROL DE PARÁSITOS", listOf("TIPO", "FÁRMACO", "MARCA", "FECHA", "PRÓX."), listaDesparasitaciones.map { listOf(it.tipo, it.nombre, it.marca, it.fecha, it.proximaFecha) })

            // Lógica de Citas corregida con it.horario
            drawStyledTable("PRÓXIMAS CITAS Y SEGUIMIENTO", listOf("SERVICIO", "FECHA Y HORA", "NOTAS"), listaCitas.map {
                listOf(it.servicio, it.horario, if(it.notas.isEmpty()) "N/A" else it.notas)
            })

            // Sello de pie de página final
            checkNewPage(60f)
            canvas.drawText("Este documento es generado por la aplicación Xolotl y tiene fines informativos.", (pageWidth / 2).toFloat(), pageHeight - 35f, footerPaint)

            pdf.finishPage(page)

            val file = File(getExternalFilesDir(null), "Carnet_${txtNombre.text}_Oficial.pdf")
            pdf.writeTo(FileOutputStream(file))
            pdf.close()

            UiUtils.mostrarAlertaPdfGenerado(this) { abrirPdfConVisor(file) }

        } catch (e: Exception) {
            e.printStackTrace()
            UiUtils.mostrarAlertaPdfError(this) {}
        }
    }

    private fun abrirPdfConVisor(file: File) {
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(intent)
    }
}