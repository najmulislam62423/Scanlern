package com.nazmulislam.scanlern

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class CircularTimerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val strokeWidthPx = 18f * resources.displayMetrics.density

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(context, R.color.text_secondary)
        alpha = 60
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = strokeWidthPx
        strokeCap = Paint.Cap.ROUND
        color = ContextCompat.getColor(context, R.color.primary)
    }

    private val arcRect = RectF()
    private var progressFraction = 1f  // 1f = পুরো সময় বাকি, 0f = শেষ

    fun setProgress(fraction: Float) {
        progressFraction = fraction.coerceIn(0f, 1f)
        invalidate()
    }

    fun setProgressColor(colorRes: Int) {
        progressPaint.color = ContextCompat.getColor(context, colorRes)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val inset = strokeWidthPx / 2
        arcRect.set(inset, inset, w - inset, h - inset)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // পুরো track (হালকা background ring)
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)
        // Progress arc, উপর থেকে (-90 ডিগ্রি) শুরু হয়ে ঘড়ির কাঁটার দিকে ঘোরে
        canvas.drawArc(arcRect, -90f, 360f * progressFraction, false, progressPaint)
    }
}