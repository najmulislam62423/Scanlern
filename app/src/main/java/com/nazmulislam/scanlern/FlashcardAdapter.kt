package com.nazmulislam.scanlern

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FlashcardAdapter(private val flashcards: List<Flashcard>) :
    RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardContainer: LinearLayout = view.findViewById(R.id.cardContainer)
        val tvCardLabel: TextView = view.findViewById(R.id.tvCardLabel)
        val tvCardContent: TextView = view.findViewById(R.id.tvCardContent)
        var isShowingAnswer = false
        var isAnimating = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val card = flashcards[position]
        holder.isShowingAnswer = false
        holder.tvCardLabel.text = "QUESTION"
        holder.tvCardContent.text = card.question
        holder.cardContainer.rotationY = 0f
        holder.cardContainer.setBackgroundResource(R.drawable.bg_card_accent)

        holder.itemView.setOnClickListener {
            if (!holder.isAnimating) {
                flipCard(holder, card)
            }
        }
    }

    private fun flipCard(holder: FlashcardViewHolder, card: Flashcard) {
        holder.isAnimating = true
        val container = holder.cardContainer
        container.cameraDistance = 12000f

        val flipOut = ObjectAnimator.ofFloat(container, "rotationY", 0f, 90f)
        flipOut.duration = 150

        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                holder.isShowingAnswer = !holder.isShowingAnswer
                if (holder.isShowingAnswer) {
                    holder.tvCardLabel.text = "ANSWER"
                    holder.tvCardContent.text = card.answer
                    container.setBackgroundResource(R.drawable.bg_card)
                } else {
                    holder.tvCardLabel.text = "QUESTION"
                    holder.tvCardContent.text = card.question
                    container.setBackgroundResource(R.drawable.bg_card_accent)
                }

                val flipIn = ObjectAnimator.ofFloat(container, "rotationY", -90f, 0f)
                flipIn.duration = 150
                flipIn.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        holder.isAnimating = false
                    }
                })
                flipIn.start()
            }
        })

        flipOut.start()
    }

    override fun getItemCount(): Int = flashcards.size
}