package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var btnBackHistory: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val notesList = mutableListOf<Note>()
    private lateinit var adapter: NoteAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.recyclerView)
        tvEmpty = findViewById(R.id.tvEmpty)
        btnBackHistory = findViewById(R.id.btnBackHistory)

        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = NoteAdapter(notesList) { note, position ->
            showDeleteConfirmation(note, position)
        }
        recyclerView.adapter = adapter

        loadNotes()

        btnBackHistory.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loadNotes() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("notes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                notesList.clear()
                for (document in result) {
                    val note = Note(
                        id = document.id,
                        text = document.getString("text") ?: "",
                        timestamp = document.getString("timestamp") ?: "",
                        userId = document.getString("userId") ?: ""
                    )
                    notesList.add(note)
                }

                notesList.reverse()
                adapter.notifyDataSetChanged()

                updateEmptyState()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load notes: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showDeleteConfirmation(note: Note, position: Int) {
        AlertDialog.Builder(this)
            .setTitle("Delete Note")
            .setMessage("Are you sure you want to delete this note?")
            .setPositiveButton("Delete") { _, _ ->
                deleteNote(note, position)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNote(note: Note, position: Int) {
        firestore.collection("notes").document(note.id)
            .delete()
            .addOnSuccessListener {
                adapter.removeItem(position)
                updateEmptyState()
                Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEmptyState() {
        if (notesList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}