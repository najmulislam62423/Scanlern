package com.nazmulislam.scanlern

import android.R.attr.text
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import android.speech.tts.TextToSpeech

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream


class ResultActivity : AppCompatActivity() {

    private lateinit var tvExtractedText: TextView
    private lateinit var btnSave: Button
    private lateinit var btnBack: View
    private lateinit var btnSummarize: View
    private lateinit var progressBar: ProgressBar
    private lateinit var btnFlashcards: View
    private lateinit var btnQuiz: View
    private lateinit var btnExplainDoubt: View
    private lateinit var btnCheckUnderstanding: View

    private lateinit var btnMnemonic: View
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var btnListen: TextView
    private var textToSpeech: TextToSpeech? = null
    private var isSpeaking = false
    private lateinit var btnShare: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        tvExtractedText = findViewById(R.id.tvExtractedText)
        btnSave = findViewById(R.id.btnSave)
        btnBack = findViewById(R.id.btnBack)
        btnListen = findViewById(R.id.btnListen)
        btnShare = findViewById(R.id.btnShare)
        btnSummarize = findViewById(R.id.btnSummarize)
        btnFlashcards = findViewById(R.id.btnFlashcards)
        btnQuiz = findViewById(R.id.btnQuiz)
        btnExplainDoubt = findViewById(R.id.btnExplainDoubt)
        btnCheckUnderstanding = findViewById(R.id.btnCheckUnderstanding)
        btnMnemonic = findViewById(R.id.btnMnemonic)
        progressBar = findViewById(R.id.progressBar)


        val extractedText = intent.getStringExtra("EXTRACTED_TEXT") ?: "No text found"
        tvExtractedText.text = extractedText


        btnSummarize.setOnClickListener {
            val currentText = tvExtractedText.text.toString()

            progressBar.visibility = View.VISIBLE

            GroqHelper.summarizeText(
                inputText = currentText,
                onResult = { jsonResult ->
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        btnSummarize.isEnabled = true
                        tvExtractedText.text = jsonResult
                        Toast.makeText(this, "Summary generated!", Toast.LENGTH_SHORT).show()
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        btnSummarize.isEnabled = true
                        androidx.appcompat.app.AlertDialog.Builder(this)
                            .setTitle("Error Details")
                            .setMessage(error)
                            .setPositiveButton("OK", null)
                            .show()
                    }
                }
            )
        }

        btnSave.setOnClickListener {
            saveNoteToFirestore()
        }
        btnFlashcards.setOnClickListener {
            val currentText = tvExtractedText.text.toString()
            val intent = Intent(this, FlashcardActivity::class.java)
            intent.putExtra("EXTRACTED_TEXT", currentText)
            startActivity(intent)
        }

        btnQuiz.setOnClickListener {
            val currentText = tvExtractedText.text.toString()
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("EXTRACTED_TEXT", currentText)
            startActivity(intent)
        }
        btnExplainDoubt.setOnClickListener {
            explainSelectedText()
        }
        btnCheckUnderstanding.setOnClickListener {
            showUnderstandingCheckDialog()
        }
        btnMnemonic.setOnClickListener {
            generateMnemonicTrick()
        }
        btnListen.setOnClickListener {
            val textToRead = tvExtractedText.text.toString()
            if (textToRead.isBlank()) {
                Toast.makeText(this, "Nothing to read", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isSpeaking) {
                textToSpeech?.stop()
                isSpeaking = false
                btnListen.text = "🔊"
            } else {
                textToSpeech?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "noteRead")
                isSpeaking = true
                btnListen.text = "⏸️"
            }
        }
        btnShare.setOnClickListener {
            sharePdf()
        }

        btnBack.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale.US
            }
        }
    }
    override fun onDestroy() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        super.onDestroy()
    }
    private fun sharePdf() {
        val text = tvExtractedText.text.toString()
        if (text.isBlank()) {
            Toast.makeText(this, "Nothing to share", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            paint.textSize = 14f
            paint.color = android.graphics.Color.BLACK

            val margin = 40f
            var y = 60f
            val maxWidth = 595 - (margin * 2)
            val lineHeight = 20f

            val words = text.split(" ")
            var line = ""

            for (word in words) {
                val testLine = if (line.isEmpty()) word else "$line $word"
                val lineWidth = paint.measureText(testLine)

                if (lineWidth > maxWidth) {
                    canvas.drawText(line, margin, y, paint)
                    line = word
                    y += lineHeight

                    if (y > 800) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        y = 60f
                    }
                } else {
                    line = testLine
                }
            }
            if (line.isNotEmpty()) {
                canvas.drawText(line, margin, y, paint)
            }

            pdfDocument.finishPage(page)

            val fileName = "Scanlern_Note_${System.currentTimeMillis()}.pdf"
            val file = File(cacheDir, fileName)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "application/pdf"
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Share Note"))

        } catch (e: Exception) {
            Toast.makeText(this, "Failed to create PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveNoteToFirestore() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        val noteText = tvExtractedText.text.toString()
        if (noteText.isBlank()) {
            Toast.makeText(this, "Nothing to save", Toast.LENGTH_SHORT).show()
            return
        }

        showCategoryDialog(userId, noteText)
    }

    private fun showCategoryDialog(userId: String, noteText: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_category, null)
        val radioGroup = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupCategory)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnCategoryConfirm)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        btnConfirm.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            val selectedRadio = dialogView.findViewById<android.widget.RadioButton>(selectedId)
            val category = selectedRadio.text.toString()

            dialog.dismiss()
            actuallySaveNote(userId, noteText, category)
        }

        dialog.show()
    }

    private fun actuallySaveNote(userId: String, noteText: String, category: String) {
        btnSave.isEnabled = false

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(System.currentTimeMillis())

        val note = hashMapOf(
            "text" to noteText,
            "timestamp" to timestamp,
            "userId" to userId,
            "category" to category
        )

        firestore.collection("notes")
            .add(note)
            .addOnSuccessListener {
                btnSave.isEnabled = true
                Toast.makeText(this, "Note saved successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                btnSave.isEnabled = true
                Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
    private fun generateMnemonicTrick() {
        val currentText = tvExtractedText.text.toString()
        if (currentText.isBlank()) {
            Toast.makeText(this, "Nothing to create a memory trick from", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        GroqHelper.generateMnemonic(
            inputText = currentText,
            onResult = { trick ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("🧠 Memory Trick")
                        .setMessage(trick)
                        .setPositiveButton("Got it", null)
                        .show()
                }
            },
            onError = { error ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }
    private fun explainSelectedText() {
        val start = tvExtractedText.selectionStart
        val end = tvExtractedText.selectionEnd

        if (start < 0 || end < 0 || start == end) {
            Toast.makeText(this, "প্রথমে টেক্সট থেকে কিছু অংশ select করো, তারপর চাপো", Toast.LENGTH_LONG).show()
            return
        }

        val selectedText = tvExtractedText.text.subSequence(
            minOf(start, end),
            maxOf(start, end)
        ).toString().trim()

        if (selectedText.isEmpty()) {
            Toast.makeText(this, "কিছু টেক্সট select করো", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE

        GroqHelper.explainText(
            selectedText = selectedText,
            onResult = { explanation ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    showExplanationDialog(explanation)
                }
            },
            onError = { error ->
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed: $error", Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun showExplanationDialog(explanation: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("💡 Explanation")
            .setMessage(explanation)
            .setPositiveButton("Got it", null)
            .show()
    }private fun showUnderstandingCheckDialog() {
        val originalText = tvExtractedText.text.toString()
        if (originalText.isBlank()) {
            Toast.makeText(this, "Nothing to check understanding on", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_understanding_check, null)
        val etInput = dialogView.findViewById<android.widget.EditText>(R.id.etUnderstandingInput)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.understandingProgressBar)
        val tvResult = dialogView.findViewById<TextView>(R.id.tvUnderstandingResult)
        val btnSubmit = dialogView.findViewById<Button>(R.id.btnSubmitUnderstanding)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setNegativeButton("Close", null)
            .create()

        btnSubmit.setOnClickListener {
            val studentExplanation = etInput.text.toString().trim()
            if (studentExplanation.isEmpty()) {
                Toast.makeText(this, "Write your explanation first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            tvResult.visibility = View.GONE
            btnSubmit.isEnabled = false

            GroqHelper.checkUnderstanding(
                originalText = originalText,
                studentExplanation = studentExplanation,
                onResult = { feedback ->
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        tvResult.visibility = View.VISIBLE
                        tvResult.text = feedback
                    }
                },
                onError = { error ->
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        btnSubmit.isEnabled = true
                        Toast.makeText(this, "Failed: $error", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        dialog.show()
    }
}