package com.imix.dreamspell_tzolkin.controller.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import kotlin.math.min

/**
 * Stacks one or more images vertically (each fit to the view width) and lets the user
 * pinch-zoom and drag-pan, clamped to the content bounds. Used by the fullscreen
 * codex dialogs (Harmonics / Chromatics / Holon).
 */
class ZoomStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var bitmaps: List<Bitmap> = emptyList()
    private var scale = 1f
    private var tx = 0f
    private var ty = 0f
    private val paint = Paint(Paint.FILTER_BITMAP_FLAG)

    fun setImages(drawableIds: List<Int>) {
        bitmaps = drawableIds.map { BitmapFactory.decodeResource(resources, it) }
        scale = 1f; tx = 0f; ty = 0f
        invalidate()
    }

    /** Height of the whole stack at scale 1, i.e. every image fit to the view width. */
    private fun contentHeight() = bitmaps.sumOf { it.height.toDouble() * width / it.width }.toFloat()

    private val scaleDetector = ScaleGestureDetector(
        context,
        object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(d: ScaleGestureDetector): Boolean {
                val factor = (scale * d.scaleFactor).coerceIn(1f, 6f) / scale
                scale *= factor
                // keep the pinch focal point stationary while scaling
                tx = d.focusX - (d.focusX - tx) * factor
                ty = d.focusY - (d.focusY - ty) * factor
                clampPan()
                invalidate()
                return true
            }
        },
    )

    private val panDetector = GestureDetector(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent) = true
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, dx: Float, dy: Float): Boolean {
                tx -= dx; ty -= dy
                clampPan()
                invalidate()
                return true
            }
        },
    )

    private fun clampPan() {
        // centre the axis when the (scaled) content is smaller than the view; otherwise clamp to edges
        val cw = width * scale
        tx = if (cw <= width) (width - cw) / 2f else tx.coerceIn(width - cw, 0f)
        val ch = contentHeight() * scale
        ty = if (ch <= height) (height - ch) / 2f else ty.coerceIn(height - ch, 0f)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        clampPan()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        panDetector.onTouchEvent(event)
        if (event.actionMasked == MotionEvent.ACTION_UP) performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.translate(tx, ty)
        canvas.scale(scale, scale)
        var y = 0f
        for (b in bitmaps) {
            val h = b.height.toFloat() * width / b.width
            canvas.drawBitmap(b, null, RectF(0f, y, width.toFloat(), y + h), paint)
            y += h
        }
    }
}
