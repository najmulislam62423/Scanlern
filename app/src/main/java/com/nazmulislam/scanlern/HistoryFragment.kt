package com.nazmulislam.scanlern

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvEmpty: TextView

    private lateinit var emptyStateLayout: android.widget.LinearLayout
    private lateinit var etSearch: android.widget.EditText

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val notesList = mutableListOf<Note>()
    private val allNotes = mutableListOf<Note>()
    private lateinit var adapter: NoteAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        recyclerView = view.findViewById(R.id.recyclerView)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        etSearch = view.findViewById(R.id.etSearch)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NoteAdapter(
            notes = notesList,
            onDeleteClick = { note, position ->
                showDeleteConfirmation(note, position)
            },
            onItemClick = { note ->
                val intent = Intent(requireContext(), EditNoteActivity::class.java)
                intent.putExtra("NOTE_ID", note.id)
                intent.putExtra("NOTE_TEXT", note.text)
                startActivity(intent)
            }
        )
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterNotes(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })

        loadNotes()
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "Please login again", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("notes")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                allNotes.clear()
                for (document in result) {
                    val note = Note(
                        id = document.id,
                        text = document.getString("text") ?: "",
                        timestamp = document.getString("timestamp") ?: "",
                        userId = document.getString("userId") ?: ""
                    )
                    allNotes.add(note)
                }
                allNotes.reverse()
                notesList.clear()
                notesList.addAll(allNotes)
                adapter.notifyDataSetChanged()
                updateEmptyState()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to load notes: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterNotes(query: String) {
        val filtered = if (query.isBlank()) {
            allNotes
        } else {
            allNotes.filter { it.text.contains(query, ignoreCase = true) }
        }
        notesList.clear()
        notesList.addAll(filtered)
        adapter.notifyDataSetChanged()
        updateEmptyState()
    }

    private fun showDeleteConfirmation(note: Note, position: Int) {
        AlertDialog.Builder(requireContext())
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
                Toast.makeText(requireContext(), "Note deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateEmptyState() {
        if (notesList.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}