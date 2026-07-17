package com.imix.dreamspell_tzolkin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import com.imix.dreamspell_tzolkin.controller.views.MoonPhaseView
import java.util.Calendar

/** Base for all date-driven tabs: binds on view creation (pulling the activity's current date,
 *  so restored fragments don't reset to "today") and rebinds on every date change. */
abstract class DreamspellFragment(layoutRes: Int) : Fragment(layoutRes) {
    protected var currentDate: Calendar = Calendar.getInstance()

    fun updateDate(date: Calendar) {
        currentDate = date
        view?.let(::bind)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentDate = (requireActivity() as OracleActivity).calendar
        bind(view)
    }

    protected abstract fun bind(view: View)
}

private fun Fragment.drawableId(name: String) = resources.getIdentifier(name, "drawable", requireContext().packageName)

/** Tap-a-glyph detail popup, using the dreamspell_details.xml layout and the kin's text. */
private fun showKinDetail(fragment: Fragment, kin: Int) {
    val context = fragment.requireContext()
    val tone = Dreamspell.tone(kin)
    val seal = Dreamspell.seal(kin)
    val toneInfo = DreamspellData.tone(context, tone)
    val glyphInfo = DreamspellData.glyph(context, seal)

    val view = LayoutInflater.from(context).inflate(R.layout.dreamspell_details, null)
    view.findViewById<ImageView>(R.id.tonesimage).setImageResource(fragment.drawableId("tone$tone"))
    view.findViewById<TextView>(R.id.tonesname).text = toneInfo.name
    view.findViewById<TextView>(R.id.tonesdescription).text = toneInfo.description
    view.findViewById<TextView>(R.id.tonesexplanation).text = toneInfo.explanation
    view.findViewById<ImageView>(R.id.glyphsimage).setImageResource(fragment.drawableId("glyph$seal"))
    view.findViewById<TextView>(R.id.glyphsname).text = glyphInfo.name
    view.findViewById<TextView>(R.id.glyphscolordescription).text = glyphInfo.colorDescription
    view.findViewById<TextView>(R.id.glyphsdescription).text = glyphInfo.description
    view.findViewById<TextView>(R.id.glyphsexplanation).text = glyphInfo.explanation

    AlertDialog.Builder(context)
        .setTitle("Kin $kin: ${DreamspellData.kin(context, kin).galacticName}")
        .setView(view)
        .setPositiveButton(R.string.btClose, null)
        .show()
}

/** Base for the tabs that show the galactic signature (tone+glyph images, tap for detail). */
abstract class SignatureFragment(layoutRes: Int) : DreamspellFragment(layoutRes) {
    override fun bind(view: View) {
        val kin = Dreamspell.kinFor(currentDate)
        val tone = Dreamspell.tone(kin)
        val seal = Dreamspell.seal(kin)
        view.findViewById<ImageView>(R.id.toneImage).setImageResource(drawableId("tone$tone"))
        view.findViewById<ImageView>(R.id.glyphImage).setImageResource(drawableId("glyph$seal"))
        view.findViewById<TextView>(R.id.galacticName).text =
            "Kin $kin: ${DreamspellData.kin(requireContext(), kin).galacticName}"
        view.findViewById<View>(R.id.ClickableLayout).setOnClickListener { showKinDetail(this, kin) }
    }
}

/** Galactic Signature: tone+glyph plus the kin's oracle affirmation in the bottom box. */
class HomeFragment : SignatureFragment(R.layout.oracle) {
    override fun bind(view: View) {
        super.bind(view)
        val kin = Dreamspell.kinFor(currentDate)
        view.findViewById<TextView>(R.id.oracle).text = DreamspellData.kin(requireContext(), kin).oracle
    }
}

/**
 * Destiny Oracle: the 5-kin "flower" cross (guide/analog/antipode/occult around the destiny kin),
 * plus the PSI Chrono ("Akashic") hidden seal that was formerly its own tab. The PSI card crowns
 * the oracle (layout C); on tablets the whole screen uses layout-sw600dp/flower.xml (A).
 */
class OracleFragment : DreamspellFragment(R.layout.flower) {

    override fun bind(view: View) {
        val kin = Dreamspell.kinFor(currentDate)
        val positions = listOf(
            Triple(R.id.glyphImage, R.id.destinyTone, kin),
            Triple(R.id.guideImage, R.id.guideTone, Dreamspell.guideKin(kin)),
            Triple(R.id.antipodeImage, R.id.antipodeTone, Dreamspell.antipodeKin(kin)),
            Triple(R.id.analogImage, R.id.analogTone, Dreamspell.analogKin(kin)),
            Triple(R.id.occultImage, R.id.occultTone, Dreamspell.occultKin(kin))
        )
        for ((glyphId, toneId, cellKin) in positions) {
            view.findViewById<ImageView>(toneId).setImageResource(drawableId("tone${Dreamspell.tone(cellKin)}"))
            view.findViewById<ImageView>(glyphId).apply {
                setImageResource(drawableId("glyph${Dreamspell.seal(cellKin)}"))
                setOnClickListener { showKinDetail(this@OracleFragment, cellKin) }
            }
        }
        view.findViewById<TextView>(R.id.galacticName).text =
            "Kin $kin: ${DreamspellData.kin(requireContext(), kin).galacticName}"

        bindPsi(view)
    }

    /** The PSI Chrono seal — same math as the old PsiFragment, now rendered in the Oracle's PSI card. */
    private fun bindPsi(view: View) {
        val tone = view.findViewById<ImageView>(R.id.psiTone)
        val glyph = view.findViewById<ImageView>(R.id.psiGlyph)
        val name = view.findViewById<TextView>(R.id.psiName)

        val psiKin = Dreamspell.psiKinFor(currentDate)
        if (psiKin == null) {
            name.text = DreamspellData.moonName(requireContext(), 14) // "Day Out Of Time"
            tone.setImageDrawable(null)
            glyph.setImageResource(R.drawable.bannerofpeace)
            glyph.setOnClickListener(null)
        } else {
            tone.setImageResource(drawableId("tone${Dreamspell.tone(psiKin)}"))
            glyph.setImageResource(drawableId("glyph${Dreamspell.seal(psiKin)}"))
            name.text = "Kin $psiKin: ${DreamspellData.kin(requireContext(), psiKin).galacticName}"
            glyph.setOnClickListener { showKinDetail(this, psiKin) }
        }
    }
}

/** Full 20 (wavespell) x 13 (tone) Tzolkin grid; tap any cell for its detail. */
class TzolkinFragment : DreamspellFragment(R.layout.tzolkin) {
    override fun bind(view: View) {
        val context = requireContext()
        val table = view.findViewById<android.widget.TableLayout>(R.id.tzolkintable)
        table.removeAllViews()
        val todayKin = Dreamspell.kinFor(currentDate)
        view.findViewById<TextView>(R.id.tzCaption).text =
            "Kin $todayKin: ${DreamspellData.kin(context, todayKin).galacticName}"
        // Size cells so the 14-column grid (seal + 13 tones) fills ~90% of the screen width,
        // instead of the original's fixed per-density dimens which left it tiny.
        val cellSize = (resources.displayMetrics.widthPixels * 0.9f / 14).toInt()
        val textSize = cellSize * 0.6f

        // 20 rows (one per seal). Column 0 = seal glyph; columns 1..13 = the tone number of each
        // kin in that seal's column. kin = tone*20 + seal.
        for (seal in 1..20) {
            val row = android.widget.TableRow(context)
            row.addView(ImageView(context).apply {
                setImageResource(drawableId("glyph$seal"))
                setPadding(1, 1, 1, 1)
                layoutParams = android.widget.TableRow.LayoutParams(cellSize, cellSize, 17f)
                    .apply { setMargins(0, 0, 2, 0) }
            })
            for (col in 0 until 13) {
                val kin = col * 20 + seal
                row.addView(TextView(context).apply {
                    text = Dreamspell.tone(kin).toString()
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textSize)
                    gravity = android.view.Gravity.CENTER
                    layoutParams = android.widget.TableRow.LayoutParams(cellSize, cellSize, 17f)
                    setBackgroundResource(
                        when {
                            kin == todayKin -> R.drawable.dw_tz_today
                            kin in Dreamspell.galacticActivationPortalKins -> R.drawable.table_row_field_marked
                            else -> R.drawable.table_row_field
                        }
                    )
                    setOnClickListener { showKinDetail(this@TzolkinFragment, kin) }
                })
            }
            table.addView(row)
        }
    }
}

class WavespellFragment : DreamspellFragment(R.layout.wavespell) {
    override fun bind(view: View) {
        val kin = Dreamspell.kinFor(currentDate)
        val wavespellNum = Dreamspell.wavespellNumber(kin)
        val startKin = (wavespellNum - 1) * 13 + 1
        val currentTone = Dreamspell.tone(kin)

        for (position in 1..13) {
            val posKin = startKin + position - 1
            val seal = Dreamspell.seal(posKin)
            val imageViewId = resources.getIdentifier("glyphImage$position", "id", requireContext().packageName)
            view.findViewById<ImageView>(imageViewId)?.apply {
                setImageResource(drawableId("glyph$seal"))
                alpha = if (position == currentTone) 1.0f else 0.45f
                setOnClickListener { showKinDetail(this@WavespellFragment, posKin) }
            }
        }

        view.findViewById<TextView>(R.id.wavespellName).text =
            DreamspellData.wavespellName(requireContext(), wavespellNum)
    }
}

class ThirteenMoonFragment : DreamspellFragment(R.layout.thirteenmoon) {
    override fun bind(view: View) {
        val position = Dreamspell.moonPositionFor(currentDate)
        val moonIndex = if (position.isDayOutOfTime) 14 else position.moon
        view.findViewById<TextView>(R.id.moon_name).text = DreamspellData.moonName(requireContext(), moonIndex)

        for (day in 1..28) {
            val cellId = resources.getIdentifier("moontable_$day", "id", requireContext().packageName)
            val isToday = !position.isDayOutOfTime && day == position.dayOfMoon
            view.findViewById<TextView>(cellId)?.setBackgroundResource(
                if (isToday) R.drawable.table_row_field_selected else R.drawable.table_row_field
            )
        }
    }
}

class MoonPhaseFragment : DreamspellFragment(R.layout.moon_phase) {
    override fun bind(view: View) {
        val angle = Dreamspell.moonPhaseAngle(currentDate)
        view.findViewById<MoonPhaseView>(R.id.moonPhaseView).setPhaseAngle(angle)
        val illumination = Math.round((1 - Math.cos(Math.toRadians(angle))) / 2 * 100).toInt()
        view.findViewById<TextView>(R.id.textMoonInfo).text = getString(R.string.strMoonIllumination, illumination)
        // Boundaries live in Dreamspell.moonPhaseName (unit-tested); here we only localize the name.
        view.findViewById<TextView>(R.id.textMoonPhase).setText(
            when (Dreamspell.moonPhaseName(angle)) {
                Dreamspell.MoonPhase.NEW -> R.string.strNewMoon
                Dreamspell.MoonPhase.WAXING_CRESCENT -> R.string.strWaxingCrescentMoon
                Dreamspell.MoonPhase.FIRST_QUARTER -> R.string.strFirstQuarterMoon
                Dreamspell.MoonPhase.WAXING_GIBBOUS -> R.string.strWaxingGibbousMoon
                Dreamspell.MoonPhase.FULL -> R.string.strFullMoon
                Dreamspell.MoonPhase.WANING_GIBBOUS -> R.string.strWaningGibbousMoon
                Dreamspell.MoonPhase.LAST_QUARTER -> R.string.strLastQuarterMoon
                Dreamspell.MoonPhase.WANING_CRESCENT -> R.string.strWaningCrescentMoon
            }
        )
    }
}

/** Static help content; the four topic cards expand/collapse on header tap. */
class HelpFragment : Fragment(R.layout.help) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // header, body, chevron for each collapsible topic
        val sections = listOf(
            Triple(R.id.helpNavHeader, R.id.helpNavBody, R.id.helpNavChevron),
            Triple(R.id.helpDayHeader, R.id.helpDayBody, R.id.helpDayChevron),
            Triple(R.id.helpTapHeader, R.id.helpTapBody, R.id.helpTapChevron),
            Triple(R.id.helpSetHeader, R.id.helpSetBody, R.id.helpSetChevron),
        )
        for ((headerId, bodyId, chevronId) in sections) {
            val body = view.findViewById<View>(bodyId)
            val chevron = view.findViewById<TextView>(chevronId)
            view.findViewById<View>(headerId).setOnClickListener {
                val show = body.visibility != View.VISIBLE
                body.visibility = if (show) View.VISIBLE else View.GONE
                chevron.text = if (show) "▾" else "▸"
            }
        }
    }
}

/**
 * Kin Combinator (More): build a set of kins — by date or from the Tzolkin grid — and show their
 * combined kin ([Dreamspell.combineKins]). The result renders inline after the list and only pins
 * to the bottom while the inline copy is scrolled off-screen (the "no-gap" behavior).
 */
class KinCombinatorFragment : Fragment(R.layout.kin_combinator) {
    private val kins = mutableListOf<Int>()
    private var pendingKin: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<TextView>(R.id.kcSegDate).setOnClickListener { setMode(view, dateMode = true) }
        view.findViewById<TextView>(R.id.kcSegTzolkin).setOnClickListener { setMode(view, dateMode = false) }
        setMode(view, dateMode = true)

        view.findViewById<TextView>(R.id.kcDateField).setOnClickListener { pickDate(view) }
        view.findViewById<TextView>(R.id.kcAddKin).setOnClickListener {
            pendingKin?.let { kins.add(it); pendingKin = null; hidePreview(view); render(view) }
        }
        view.findViewById<TextView>(R.id.kcOpenTzolkin).setOnClickListener { openTzolkinPicker(view) }
        view.findViewById<TextView>(R.id.kcClear).setOnClickListener { kins.clear(); render(view) }

        view.findViewById<NestedScrollView>(R.id.kcScroll)
            .setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, _, _, _ -> updatePinned(view) })

        render(view)
    }

    private fun color(id: Int) = androidx.core.content.ContextCompat.getColor(requireContext(), id)

    private fun setMode(view: View, dateMode: Boolean) {
        view.findViewById<View>(R.id.kcDateMode).visibility = if (dateMode) View.VISIBLE else View.GONE
        view.findViewById<View>(R.id.kcOpenTzolkin).visibility = if (dateMode) View.GONE else View.VISIBLE
        val date = view.findViewById<TextView>(R.id.kcSegDate)
        val tz = view.findViewById<TextView>(R.id.kcSegTzolkin)
        date.setBackgroundResource(if (dateMode) R.color.mw_amethyst else 0)
        tz.setBackgroundResource(if (dateMode) 0 else R.color.mw_amethyst)
        date.setTextColor(color(if (dateMode) R.color.mw_gold else R.color.mw_cream_dim))
        tz.setTextColor(color(if (dateMode) R.color.mw_cream_dim else R.color.mw_gold))
    }

    private fun kinLabel(kin: Int) = "Kin $kin: ${DreamspellData.kin(requireContext(), kin).galacticName}"

    private fun pickDate(view: View) {
        val c = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, y, m, d ->
            val picked = Calendar.getInstance().apply { set(y, m, d, 12, 0, 0) }
            val kin = Dreamspell.kinFor(picked)
            pendingKin = kin
            view.findViewById<TextView>(R.id.kcDateField).text =
                java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM).format(picked.time)
            view.findViewById<View>(R.id.kcPreview).visibility = View.VISIBLE
            view.findViewById<ImageView>(R.id.kcPreviewTone).setImageResource(drawableId("tone${Dreamspell.tone(kin)}"))
            view.findViewById<ImageView>(R.id.kcPreviewSeal).setImageResource(drawableId("glyph${Dreamspell.seal(kin)}"))
            view.findViewById<TextView>(R.id.kcPreviewName).text = kinLabel(kin)
            view.findViewById<View>(R.id.kcAddKin).visibility = View.VISIBLE
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun hidePreview(view: View) {
        view.findViewById<View>(R.id.kcPreview).visibility = View.GONE
        view.findViewById<View>(R.id.kcAddKin).visibility = View.GONE
        view.findViewById<TextView>(R.id.kcDateField).setText(R.string.kcPickDate)
    }

    /**
     * The full 260-cell Tzolkin grid in a full-screen multi-select dialog (green Galactic Activation
     * Portals shown, same as the Tzolkin tab); confirmed kins are added to the list at once.
     */
    private fun openTzolkinPicker(hostView: View) {
        val context = requireContext()
        val selected = linkedSetOf<Int>()
        // size cells to the full screen width (14 columns) — the same fit as the Tzolkin tab
        val cell = (resources.displayMetrics.widthPixels * 0.9f / 14).toInt()
        val textPx = cell * 0.55f

        val content = LayoutInflater.from(context).inflate(R.layout.kc_tzolkin_picker, null)
        val addBtn = content.findViewById<TextView>(R.id.kcPickerAdd)
        fun refreshAdd() {
            addBtn.isEnabled = selected.isNotEmpty()
            addBtn.alpha = if (selected.isEmpty()) 0.4f else 1f
            addBtn.text = getString(R.string.kcPickerAdd, selected.size)
        }
        refreshAdd()

        val table = android.widget.TableLayout(context)
        for (seal in 1..20) {
            val row = android.widget.TableRow(context)
            row.addView(ImageView(context).apply {
                setImageResource(drawableId("glyph$seal"))
                layoutParams = android.widget.TableRow.LayoutParams(cell, cell).apply { setMargins(0, 0, 2, 2) }
            })
            for (col in 0 until 13) {
                val kin = col * 20 + seal
                val base = if (kin in Dreamspell.galacticActivationPortalKins)
                    R.drawable.table_row_field_marked else R.drawable.table_row_field
                row.addView(TextView(context).apply {
                    text = Dreamspell.tone(kin).toString()
                    setTextColor(color(R.color.mw_cream))
                    setTextSize(android.util.TypedValue.COMPLEX_UNIT_PX, textPx)
                    gravity = android.view.Gravity.CENTER
                    layoutParams = android.widget.TableRow.LayoutParams(cell, cell).apply { setMargins(0, 0, 2, 2) }
                    setBackgroundResource(base)
                    setOnClickListener {
                        if (selected.remove(kin)) setBackgroundResource(base)
                        else { selected.add(kin); setBackgroundResource(R.drawable.dw_tz_today) }
                        refreshAdd()
                    }
                })
            }
            table.addView(row)
        }
        content.findViewById<LinearLayout>(R.id.kcPickerTableHost).addView(table)

        val dialog = android.app.Dialog(context, android.R.style.Theme_Black_NoTitleBar)
        dialog.setContentView(content)
        content.findViewById<TextView>(R.id.kcPickerCancel).setOnClickListener { dialog.dismiss() }
        content.findViewById<TextView>(R.id.kcPickerClose).setOnClickListener { dialog.dismiss() }
        addBtn.setOnClickListener {
            if (selected.isNotEmpty()) { kins.addAll(selected); render(hostView); dialog.dismiss() }
        }
        dialog.show()
    }

    private fun render(view: View) {
        val list = view.findViewById<LinearLayout>(R.id.kcList)
        list.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())
        kins.forEachIndexed { index, kin ->
            val row = inflater.inflate(R.layout.kc_kin_row, list, false)
            row.findViewById<ImageView>(R.id.kcRowTone).setImageResource(drawableId("tone${Dreamspell.tone(kin)}"))
            row.findViewById<ImageView>(R.id.kcRowSeal).setImageResource(drawableId("glyph${Dreamspell.seal(kin)}"))
            row.findViewById<TextView>(R.id.kcRowName).text = kinLabel(kin)
            row.setOnClickListener { showKinDetail(this, kin) }
            row.findViewById<TextView>(R.id.kcRowRemove).setOnClickListener { kins.removeAt(index); render(view) }
            list.addView(row)
        }
        val hasKins = kins.isNotEmpty()
        view.findViewById<View>(R.id.kcEmpty).visibility = if (hasKins) View.GONE else View.VISIBLE
        view.findViewById<View>(R.id.kcClear).visibility = if (hasKins) View.VISIBLE else View.GONE
        view.findViewById<View>(R.id.kcInlineResult).visibility = if (hasKins) View.VISIBLE else View.GONE

        if (hasKins) {
            val combined = Dreamspell.combineKins(kins)
            val cap = resources.getQuantityString(R.plurals.kcResultCount, kins.size, kins.size)
            bindResult(view, R.id.kcInlineTone, R.id.kcInlineSeal, R.id.kcInlineName, R.id.kcInlineCap, combined, cap)
            bindResult(view, R.id.kcPinnedTone, R.id.kcPinnedSeal, R.id.kcPinnedName, R.id.kcPinnedCap, combined, cap)
            view.findViewById<View>(R.id.kcInlineResult).setOnClickListener { showKinDetail(this, combined) }
            view.findViewById<View>(R.id.kcPinnedResult).setOnClickListener { showKinDetail(this, combined) }
        } else {
            view.findViewById<View>(R.id.kcPinnedResult).visibility = View.GONE
        }
        view.post { updatePinned(view) }
    }

    private fun bindResult(view: View, toneId: Int, sealId: Int, nameId: Int, capId: Int, kin: Int, cap: String) {
        view.findViewById<ImageView>(toneId).setImageResource(drawableId("tone${Dreamspell.tone(kin)}"))
        view.findViewById<ImageView>(sealId).setImageResource(drawableId("glyph${Dreamspell.seal(kin)}"))
        view.findViewById<TextView>(nameId).text = kinLabel(kin)
        view.findViewById<TextView>(capId).text = cap
    }

    /** Show the pinned result only while the inline copy is scrolled out of view. */
    private fun updatePinned(view: View) {
        val pinned = view.findViewById<View>(R.id.kcPinnedResult)
        if (kins.isEmpty()) { pinned.visibility = View.GONE; return }
        val scroll = view.findViewById<NestedScrollView>(R.id.kcScroll)
        val inline = view.findViewById<View>(R.id.kcInlineResult)
        val fullyVisible = inline.bottom <= scroll.scrollY + scroll.height
        pinned.visibility = if (fullyVisible) View.GONE else View.VISIBLE
    }
}
