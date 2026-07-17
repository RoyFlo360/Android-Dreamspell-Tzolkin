package com.imix.dreamspell_tzolkin

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.imix.dreamspell_tzolkin.controller.views.ZoomStackView
import java.text.DateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Host activity. Navigation is a bottom nav bar over a 4-page **swipeable** ViewPager2 (the primary
 * destinations Home / Oracle / Wavespell / Tzolkin). "More" is click-only: its secondary screens
 * (13-Moon, Moon Phase, Help) and the Codex dialogs open from a bottom sheet, shown over the pager
 * in [secondaryContainer]. Date changes come from the ‹ Today › stepper; the gear menu holds the
 * PSI-layout toggle, Language and What's New. (PSI merged into the Oracle screen; Share removed.)
 */
class OracleActivity : AppCompatActivity() {

    lateinit var calendar: Calendar
        private set
    private lateinit var pager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var todayButton: TextView
    private lateinit var titleView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_oracle)

        calendar = Calendar.getInstance()
        if (savedInstanceState != null) {
            calendar.set(
                savedInstanceState.getInt("mYear"), savedInstanceState.getInt("mMonth"),
                savedInstanceState.getInt("mDay"), savedInstanceState.getInt("mHour"),
                savedInstanceState.getInt("mMinute"), savedInstanceState.getInt("mSecond")
            )
        }

        pager = findViewById(R.id.pager)
        pager.adapter = TabAdapter(this)
        pager.offscreenPageLimit = PRIMARY_COUNT - 1 // keep all primaries resident (refresh() broadcasts to them)
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNav.menu.getItem(position).isChecked = true // keep the nav highlight in sync with swipes
            }
        })

        bottomNav = findViewById(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navHome -> selectPrimary(0)
                R.id.navOracle -> selectPrimary(1)
                R.id.navWavespell -> selectPrimary(2)
                R.id.navTzolkin -> selectPrimary(3)
                R.id.navMore -> { showMoreSheet(); false } // click-only; don't make "More" the selection
                else -> false
            }
        }

        findViewById<View>(R.id.prevDay).setOnClickListener { stepDay(-1) }
        findViewById<View>(R.id.nextDay).setOnClickListener { stepDay(1) }
        todayButton = findViewById(R.id.todayButton)
        todayButton.setOnClickListener { calendar = Calendar.getInstance(); refresh() }

        // Back closes a secondary "More" screen before leaving the app.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (secondaryVisible()) hideSecondary()
                else { isEnabled = false; onBackPressedDispatcher.onBackPressed() }
            }
        })

        // Date replaces the app name as the action-bar title (auto-sizing so the LONG format never
        // gets ellipsized by the action icons).
        titleView = TextView(this).apply {
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_ActionBar_Title)
            maxLines = 1
            androidx.core.widget.TextViewCompat.setAutoSizeTextTypeUniformWithConfiguration(
                this, 12, 20, 1, android.util.TypedValue.COMPLEX_UNIT_SP
            )
        }
        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            setCustomView(
                titleView,
                androidx.appcompat.app.ActionBar.LayoutParams(
                    androidx.appcompat.app.ActionBar.LayoutParams.MATCH_PARENT,
                    androidx.appcompat.app.ActionBar.LayoutParams.WRAP_CONTENT
                )
            )
        }

        updateTitle()
        updateStepper()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("mYear", calendar.get(Calendar.YEAR))
        outState.putInt("mMonth", calendar.get(Calendar.MONTH))
        outState.putInt("mDay", calendar.get(Calendar.DAY_OF_MONTH))
        outState.putInt("mHour", calendar.get(Calendar.HOUR_OF_DAY))
        outState.putInt("mMinute", calendar.get(Calendar.MINUTE))
        outState.putInt("mSecond", calendar.get(Calendar.SECOND))
        super.onSaveInstanceState(outState)
    }

    // ---- navigation ----

    private fun selectPrimary(index: Int): Boolean {
        if (secondaryVisible()) hideSecondary()
        pager.currentItem = index
        return true
    }

    private fun secondaryVisible() = findViewById<View>(R.id.secondaryContainer).visibility == View.VISIBLE

    /** [dateDriven] screens (13-Moon, Moon Phase) keep the ‹ Today › stepper; Help/Kin Combinator hide it. */
    private fun showSecondary(fragment: Fragment, dateDriven: Boolean) {
        supportFragmentManager.beginTransaction().replace(R.id.secondaryContainer, fragment).commit()
        findViewById<View>(R.id.secondaryContainer).visibility = View.VISIBLE
        pager.visibility = View.GONE
        findViewById<View>(R.id.stepperBar).visibility = if (dateDriven) View.VISIBLE else View.GONE
        bottomNav.menu.findItem(R.id.navMore).isChecked = true
    }

    private fun hideSecondary() {
        supportFragmentManager.findFragmentById(R.id.secondaryContainer)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        findViewById<View>(R.id.secondaryContainer).visibility = View.GONE
        pager.visibility = View.VISIBLE
        findViewById<View>(R.id.stepperBar).visibility = View.VISIBLE
        bottomNav.menu.getItem(pager.currentItem).isChecked = true
    }

    /** The click-only "More" menu: secondary destinations + the Codex dialogs. */
    private fun showMoreSheet() {
        val sheet = BottomSheetDialog(this)
        val pad = (16 * resources.displayMetrics.density).toInt()
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad + pad / 2)
            setBackgroundColor(ContextCompat.getColor(this@OracleActivity, R.color.mw_card))
        }
        // The original labels are ALL CAPS; show them Capitalized Per Word in this menu.
        fun titleCase(s: String) = s.lowercase().split(" ").joinToString(" ") { it.replaceFirstChar(Char::uppercase) }
        fun row(titleRes: Int, iconRes: Int, onClick: () -> Unit) {
            container.addView(TextView(this).apply {
                text = titleCase(getString(titleRes))
                textSize = 16f
                setTextColor(ContextCompat.getColor(this@OracleActivity, R.color.mw_cream))
                setCompoundDrawablesRelativeWithIntrinsicBounds(iconRes, 0, 0, 0)
                compoundDrawableTintList = android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(this@OracleActivity, R.color.mw_gold))
                compoundDrawablePadding = pad
                setPadding(pad / 2, pad, pad / 2, pad)
                setOnClickListener { sheet.dismiss(); onClick() }
            })
        }
        row(R.string.strHelp, R.drawable.ic_more_help) { showSecondary(HelpFragment(), dateDriven = false) }
        // Buy Me a Coffee — brand-yellow pill; opens the donation page in the browser.
        container.addView(TextView(this).apply {
            text = getString(R.string.bmcButton)
            textSize = 16f
            setTextColor(0xFF0D0D0D.toInt())
            setBackgroundResource(R.drawable.bmc_button_bg)
            setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.ic_coffee, 0, 0, 0)
            compoundDrawablePadding = pad
            setPadding(pad, pad * 3 / 4, pad, pad * 3 / 4)
            (layoutParams as? LinearLayout.LayoutParams
                ?: LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)).also {
                it.topMargin = pad / 2; it.bottomMargin = pad / 2; layoutParams = it
            }
            setOnClickListener {
                sheet.dismiss()
                startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://buymeacoffee.com/rodrigoflores")))
            }
        })
        row(R.string.kcTitle, R.drawable.ic_more_combinator) { showSecondary(KinCombinatorFragment(), dateDriven = false) }
        row(R.string.strMoonphase, R.drawable.ic_more_moonphase) { showSecondary(MoonPhaseFragment(), dateDriven = true) }
        row(R.string.strThirteenmoon, R.drawable.ic_more_thirteenmoon) { showSecondary(ThirteenMoonFragment(), dateDriven = true) }
        row(R.string.btHolon, R.drawable.ic_more_holon) { showCodexDialog(R.string.btHolon, R.drawable.humanholon_clans_chak, R.drawable.planetholon_fam_chak) }
        row(R.string.btChromatics, R.drawable.ic_more_chromatics) {
            showCodexDialog(R.string.btChromatics, R.drawable.chromatics_clan, R.drawable.chromatics_overtone, R.drawable.chromatics_wisdom)
        }
        row(R.string.btHarmonics, R.drawable.ic_more_harmonics) { showCodexDialog(R.string.btHarmonics, R.drawable.harmonics) }
        sheet.setContentView(container)
        sheet.show()
    }

    // ---- date stepping ----

    private fun stepDay(delta: Int) {
        calendar.add(Calendar.DAY_OF_YEAR, delta)
        resetTimeOfDay()
        refresh()
    }

    private fun resetTimeOfDay() {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
    }

    private fun refresh() {
        updateTitle()
        updateStepper()
        // Fragments (both the resident pager primaries and any visible secondary) live in the
        // activity's FragmentManager; asking it survives recreation, where createFragment() is
        // never called for restored fragments.
        supportFragmentManager.fragments.forEach { (it as? DreamspellFragment)?.updateDate(calendar) }
    }

    private fun updateTitle() {
        // Nahuatl (nah) / Yucatec (yua) have no ICU date data, so LONG format degrades to "2026 M07 11".
        // Fall back to Spanish month names for them (the standard for Gregorian dates in those languages).
        val loc = Locale.getDefault()
        val dateLoc = if (loc.language in setOf("nah", "yua")) Locale("es") else loc
        titleView.text = DateFormat.getDateInstance(DateFormat.LONG, dateLoc).format(calendar.time)
    }

    /** "Today" is muted when the selection is already today, gold when navigated away. */
    private fun updateStepper() {
        val onToday = Dreamspell.isSameDay(calendar, Calendar.getInstance())
        todayButton.setTextColor(ContextCompat.getColor(this, if (onToday) R.color.mw_cream_dim else R.color.mw_gold))
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                refresh()
            },
            calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ---- top-bar menu (calendar + gear) ----

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pickDate -> { showDatePicker(); return true }
            R.id.selectLanguage -> { showLanguagePicker(); return true }
            R.id.whatsNew -> {
                AlertDialog.Builder(this)
                    .setTitle(R.string.strWhatsNewTitle)
                    .setMessage(R.string.strWhatsNewContent)
                    .setPositiveButton(R.string.btClose, null)
                    .show()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /** Language codes the app ships translations for (see res/raw-* and values-*), including
     *  Nahuatl (nah) and Yucatec Maya (yua) for indigenous-language users in Mexico. */
    private val languageCodes = listOf("en", "fr", "es", "ru", "zh-CN", "zh-TW", "nl", "nah", "yua")

    /** Autonyms for languages the JDK can't name on its own (ISO-639-3 tags have no built-in display name). */
    private val languageAutonyms = mapOf("nah" to "Nāhuatl", "yua" to "Màaya t'àan")

    private fun showLanguagePicker() {
        val names = languageCodes.map { code ->
            languageAutonyms[code] ?: Locale.forLanguageTag(code).let { locale ->
                locale.getDisplayName(locale).replaceFirstChar { it.uppercase(locale) }
            }
        }.toTypedArray()

        val currentTag = AppCompatDelegate.getApplicationLocales().toLanguageTags()
        val checked = languageCodes.indexOfFirst { it.equals(currentTag, ignoreCase = true) }

        AlertDialog.Builder(this)
            .setTitle(R.string.btChangeLanguage)
            .setSingleChoiceItems(names, checked) { dialog, which ->
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageCodes[which]))
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    /** Codex images fullscreen with pinch-zoom; the X top-right closes. Reusing the activity theme
     *  keeps the dialog non-floating (fullscreen) and resolves the icon tint. */
    private fun showCodexDialog(titleRes: Int, vararg imageRes: Int) {
        val dialog = android.app.Dialog(this, R.style.DreamspellTheme)
        dialog.setContentView(R.layout.codex_zoom)
        dialog.findViewById<TextView>(R.id.codexTitle).setText(titleRes)
        dialog.findViewById<ZoomStackView>(R.id.codexZoom).setImages(imageRes.toList())
        dialog.findViewById<View>(R.id.codexClose).setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private class TabAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount() = PRIMARY_COUNT

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> HomeFragment()
            1 -> OracleFragment()
            2 -> WavespellFragment()
            else -> TzolkinFragment()
        }
    }

    companion object {
        /** Swipeable primary destinations: Home, Oracle, Wavespell, Tzolkin. */
        private const val PRIMARY_COUNT = 4
    }
}
