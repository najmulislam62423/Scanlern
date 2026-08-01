package com.nazmulislam.scanlern

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FlashcardAdapter(private val flashcards: List<Flashcard>) :
    RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCardLabel: TextView = view.findViewById(R.id.tvCardLabel)
        val tvCardContent: TextView = view.findViewById(R.id.tvCardContent)
        var isShowingAnswer = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val card = flashcards[position]
        holder.isShowingAnswer = false
        holder.tvCardLabel.text = "Question"
        holder.tvCardContent.text = card.question

        holder.itemView.setOnClickListener {
            holder.isShowingAnswer = !holder.isShowingAnswer
            if (holder.isShowingAnswer) {
                holder.tvCardLabel.text = "Answer"
                holder.tvCardContent.text = card.answer
            } else {
                holder.tvCardLabel.text = "Question"
                holder.tvCardContent.text = card.question
            }
        }
    }

    override fun getItemCount(): Int = flashcards.size
}