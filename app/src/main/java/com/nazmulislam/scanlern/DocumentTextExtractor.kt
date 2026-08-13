package com.nazmulislam.scanlern

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

object DocumentTextExtractor {

    private const val MAX_PDF_PAGES = 15

    private fun buildCleanText(visionText: com.google.mlkit.vision.text.Text): String {
        val paragraphs = mutableListOf<String>()
        for (block in visionText.textBlocks) {
            val lines = block.lines.map { it.text.trim() }
            val paragraphText = lines.joinToString(" ")
            if (paragraphText.isNotBlank()) {
                paragraphs.add(paragraphText)
            }
        }
        return paragraphs.joinToString("\n\n")
    }

    fun extractFromImage(
        context: Context,
        uri: Uri,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText -> onResult(buildCleanText(visionText)) }
                .addOnFailureListener { e -> onError(e.message ?: "Failed to read image") }
        } catch (e: Exception) {
            onError(e.message ?: "Failed to read image")
        }
    }

    fun extractFromPdf(
        context: Context,
        uri: Uri,
        onProgress: (current: Int, total: Int) -> Unit,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val pfd: ParcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
                ?: run { onError("Could not open PDF"); return }

            val renderer = PdfRenderer(pfd)
            val totalPages = minOf(renderer.pageCount, MAX_PDF_PAGES)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val extractedParts = StringBuilder()

            fun processPage(index: Int) {
                if (index >= totalPages) {
                    renderer.close()
                    pfd.close()
                    onResult(extractedParts.toString().trim())
                    return
                }

                onProgress(index + 1, totalPages)

                val page = renderer.openPage(index)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()

                val image = InputImage.fromBitmap(bitmap, 0)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        extractedParts.append(buildCleanText(visionText)).append("\n\n")
                        processPage(index + 1)
                    }
                    .addOnFailureListener {
                        // এক page fail করলেও বাকি page গুলো চেষ্টা চালিয়ে যাও
                        processPage(index + 1)
                    }
            }

            processPage(0)
        } catch (e: Exception) {
            onError(e.message ?: "Failed to read PDF")
        }
    }
}