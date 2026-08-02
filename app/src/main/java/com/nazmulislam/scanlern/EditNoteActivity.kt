package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class EditNoteActivity : AppCompatActivity() {

    private lateinit var etEditNoteText: EditText
    private lateinit var btnSaveEditNote: Button
    private lateinit var btnCancelEditNote: Button
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_note)

        firestore = FirebaseFirestore.getInstance()

        etEditNoteText = findViewById(R.id.etEditNoteText)
        btnSaveEditNote = findViewById(R.id.btnSaveEditNote)
        btnCancelEditNote = findViewById(R.id.btnCancelEditNote)

        val noteId = intent.getStringExtra("NOTE_ID") ?: ""
        val noteText = intent.getStringExtra("NOTE_TEXT") ?: ""

        etEditNoteText.setText(noteText)

        btnSaveEditNote.setOnClickListener {
            val updatedText = etEditNoteText.text.toString().trim()
            if (updatedText.isNotEmpty()) {
                saveNote(noteId, updatedText)
            } else {
                Toast.makeText(this, "Note cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancelEditNote.setOnClickListener {
            finish()
        }
    }

    private fun saveNote(noteId: String, newText: String) {
        firestore.collection("notes").document(noteId)
            .update("text", newText)
            .addOnSuccessListener {
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}