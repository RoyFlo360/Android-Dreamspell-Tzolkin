package com.imix.dreamspell_tzolkin.controller.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.imix.dreamspell_tzolkin.R
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Draws the current moon phase: the lunar disc art (res/drawable/moon_full.webp) with the unlit
 * part covered in shadow.
 * [phaseAngle] is 0-360 degrees (0 = new, 180 = full), -1 = nothing to draw.
 */
class MoonPhaseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    private var phaseAngle: Double = -1.0
    /** Night side: the surface multiplied down to a dark blue-grey, so its texture stays faintly visible. */
    private val night = PorterDuffColorFilter(Color.rgb(0x24, 0x27, 0x31), PorterDuff.Mode.MULTIPLY)
    private val moon = ContextCompat.getDrawable(context, R.drawable.moon_full)?.mutate()
    private val shadow = Path()
    private val terminatorOval = Path()

    fun setPhaseAngle(degrees: Double) {
        phaseAngle = degrees
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        val cx = measuredWidth / 2
        val cy = measuredHeight / 2
        val radius = (minOf(cx, cy) * 0.9).toFloat()

        moon?.apply {
            setBounds((cx - radius).toInt(), (cy - radius).toInt(), (cx + radius).toInt(), (cy + radius).toInt())
            draw(canvas)
        }

        if (phaseAngle <= -1.0) return

        // Half the disc is always dark; the terminator is that circle squashed horizontally by
        // cos(phase), and is either lit or dark - its sign says which.
        //
        // The night side is the same artwork redrawn darkened through a clip, rather than a shape
        // painted over it: the limb then comes from the art's own alpha, so no sliver of the bright
        // edge can survive outside the shadow (a circle drawn at `radius` left one on the lower
        // right). Only the terminator is a drawn edge, and that one sits well inside the disc.
        val top = cy - radius
        val bottom = cy + radius
        val darkSide = if (sin(Math.toRadians(phaseAngle)) < 0.0) cx.toFloat() else cx - radius // dark half starts here
        val terminator = (cos(Math.toRadians(phaseAngle)) * radius).toInt()
        val half = abs(terminator).toFloat()

        shadow.reset()
        shadow.addRect(RectF(darkSide, top - 1f, darkSide + radius, bottom + 1f), Path.Direction.CW)
        terminatorOval.reset()
        terminatorOval.addOval(RectF(cx - half, top, cx + half, bottom), Path.Direction.CW)
        shadow.op(terminatorOval, if (terminator < 0) Path.Op.DIFFERENCE else Path.Op.UNION)

        canvas.save()
        canvas.clipPath(shadow)
        moon?.apply {
            colorFilter = night
            draw(canvas)
            colorFilter = null
        }
        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec))
    }
}
