package com.imix.dreamspell_tzolkin.controller.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the current moon phase as a shaded circle.
 * [phaseAngle] is 0-360 degrees (0 = new, 180 = full), -1 = nothing to draw.
 */
class MoonPhaseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var phaseAngle: Double = -1.0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { strokeWidth = 3f }

    fun setPhaseAngle(degrees: Double) {
        phaseAngle = degrees
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = measuredWidth / 2
        val cy = measuredHeight / 2
        val radius = (minOf(cx, cy) * 0.9).toFloat()

        paint.style = Paint.Style.FILL_AND_STROKE
        paint.color = Color.GRAY
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, paint)

        if (phaseAngle <= -1.0) return

        paint.style = Paint.Style.FILL
        paint.color = Color.DKGRAY
        val top = (cy - radius).toInt().toFloat()
        val bottom = (cy + radius).toInt().toFloat()
        val startAngle = if (sin(Math.toRadians(phaseAngle)) < 0.0) 270f else 90f
        canvas.drawArc(
            RectF((cx - radius).toInt().toFloat(), top, (cx + radius).toInt().toFloat(), bottom),
            startAngle, 180f, false, paint,
        )

        var terminator = (cos(Math.toRadians(phaseAngle)) * radius).toInt()
        if (terminator < 0) {
            paint.color = Color.GRAY
            terminator = -terminator
        } else {
            paint.color = Color.DKGRAY
        }
        canvas.drawOval(RectF((cx - terminator).toFloat(), top, (cx + terminator).toFloat(), bottom), paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }
}
