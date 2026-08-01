package com.nazmulislam.scanlern

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NoteAdapter(
    private val notes: MutableList<Note>,
    private val onDeleteClick: (Note, Int) -> Unit,
    private val onItemClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {

    class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNoteText: TextView = view.findViewById(R.id.tvNoteText)
        val tvNoteTimestamp: TextView = view.findViewById(R.id.tvNoteTimestamp)
        val btnDeleteNote: ImageButton = view.findViewById(R.id.btnDeleteNote)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.tvNoteText.text = note.text
        holder.tvNoteTimestamp.text = note.timestamp

        holder.btnDeleteNote.setOnClickListener {
            onDeleteClick(note, position)
        }

        holder.itemView.setOnClickListener {
            onItemClick(note)
        }
    }

    override fun getItemCount(): Int = notes.size

    fun removeItem(position: Int) {
        notes.removeAt(position)
        notifyItemRemoved(position)
    }
}