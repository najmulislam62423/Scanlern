package com.nazmulislam.scanlern

import android.view.View

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import android.widget.TextView

import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts

class ScanActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var btnCapture: View
    private lateinit var scanLine: View
    private var imageCapture: ImageCapture? = null

    private val cameraPermissionCode = 100

    private lateinit var btnGallery: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        previewView = findViewById(R.id.previewView)
        btnCapture = findViewById(R.id.btnCapture)
        btnGallery = findViewById(R.id.btnGallery)
        scanLine = findViewById(R.id.scanLine)
        val btnCloseScan = findViewById<TextView>(R.id.btnCloseScan)
        btnCloseScan.setOnClickListener { finish() }
        startScanLineAnimation()

        // Camera permission check kora
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.CAMERA), cameraPermissionCode
            )
        } else {
            startCamera()
        }

        btnCapture.setOnClickListener {
            captureAndRecognizeText()
        }
        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            recognizeTextFromImage(it)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == cameraPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Camera start failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureAndRecognizeText() {
        val imageCapture = imageCapture ?: return

        val photoFile = File(
            cacheDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@ScanActivity, "Capture failed: ${exc.message}", Toast.LENGTH_SHORT).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    recognizeTextFromImage(photoFile)
                }
            }
        )
    }

    private fun recognizeTextFromImage(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = buildCleanText(visionText)
                    val intent = Intent(this, ResultActivity::class.java)
                    intent.putExtra("EXTRACTED_TEXT", extractedText)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Text recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun recognizeTextFromImage(photoFile: File) {
        try {
            val image = InputImage.fromFilePath(this, android.net.Uri.fromFile(photoFile))
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val extractedText = buildCleanText(visionText)
                    val intent = Intent(this, ResultActivity::class.java)
                    intent.putExtra("EXTRACTED_TEXT", extractedText)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Text recognition failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startScanLineAnimation() {
        val animator = android.animation.ObjectAnimator.ofFloat(scanLine, "translationY", 0f, 380f)
        animator.duration = 1800
        animator.repeatMode = android.animation.ValueAnimator.REVERSE
        animator.repeatCount = android.animation.ValueAnimator.INFINITE
        animator.start()
    }
    private fun buildCleanText(visionText: com.google.mlkit.vision.text.Text): String {
        val paragraphs = mutableListOf<String>()
        for (block in visionText.textBlocks) {
            val lines = block.lines.map { it.text.trim() }
            // একটা block এর ভেতরের সব লাইন স্পেস দিয়ে জোড়া, newline দিয়ে না
            val paragraphText = lines.joinToString(" ")
            if (paragraphText.isNotBlank()) {
                paragraphs.add(paragraphText)
            }
        }
        // আলাদা block/paragraph এর মাঝে একটা ফাঁকা লাইন
        return paragraphs.joinToString("\n\n")
    }
}