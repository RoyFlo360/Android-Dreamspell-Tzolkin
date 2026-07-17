package com.imix.dreamspell_tzolkin

import java.util.Calendar

/**
 * Tzolkin/Dreamspell calendar math. Kin/tone/seal and 13-Moon numbers come from the fixed
 * lookup tables below, so the standard Dreamspell numbers come out exactly.
 *
 * Names/descriptions (in all 9 shipped languages) live in [DreamspellData], parsed from the
 * res/raw XML files.
 *
 * All five Destiny-Oracle relationships are implemented: [guideKin], [analogKin], [antipodeKin]
 * and [occultKin] around the destiny [kinFor] (see [guideKin] for the seal formulas).
 */
object Dreamspell {

    // Year-cycle offset (index = Gregorian year % 52).
    private val yearOffset = intArrayOf(
        232, 77, 182, 27, 132, 237, 82, 187, 32, 137, 242, 87, 192, 37, 142, 247, 92, 197, 42, 147,
        252, 97, 202, 47, 152, 257, 102, 207, 52, 157, 2, 107, 212, 57, 162, 7, 112, 217, 62, 167,
        12, 117, 222, 67, 172, 17, 122, 227, 72, 177, 22, 127
    )

    // Cumulative day-of-year offset mod 260 (index = Calendar.MONTH, 0-based).
    private val monthOffset = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 13, 44, 74)

    // Days per month (used for the 13-Moon calendar below).
    private val daysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

    /** Kin number (1-260) for the given date. */
    fun kinFor(calendar: Calendar): Int {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        var kin = yearOffset[year % 52] + monthOffset[month] + day
        if (day == 29 && month == Calendar.FEBRUARY && hour < 12) kin--
        while (kin > 260) kin -= 260
        while (kin <= 0) kin += 260
        return kin
    }

    /** The 52 "Galactic Activation Portal" kins highlighted on the Tzolkin. */
    val galacticActivationPortalKins = setOf(
        1, 20, 22, 39, 43, 50, 51, 58, 64, 69, 72, 77, 85, 88, 93, 96, 106, 107, 108, 109, 110, 111,
        112, 113, 114, 115, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 165, 168, 173, 176,
        184, 189, 192, 197, 203, 210, 211, 218, 222, 239, 241, 260
    )

    /**
     * Combine kins by summing their kin numbers, wrapped into 1..260:
     * `((Σ kinNumbers − 1) mod 260) + 1`. Order-independent; a single kin maps to itself.
     * Requires a non-empty list (a combination of nothing has no kin).
     */
    fun combineKins(kins: List<Int>): Int {
        require(kins.isNotEmpty()) { "combineKins needs at least one kin" }
        return (kins.sum() - 1).mod(260) + 1
    }

    /** 1-13 */
    fun tone(kin: Int) = (kin - 1) % 13 + 1

    /** 1-20, indexes toneNames/sealNames and the glyphN/toneN drawables (1-based) */
    fun seal(kin: Int) = (kin - 1) % 20 + 1

    /** Which of the 20 wavespells (13-day cycles) this kin falls in, 1-20 */
    fun wavespellNumber(kin: Int) = (kin - 1) / 13 + 1

    /** The seal that names the wavespell (its first, tone-1 kin) */
    fun wavespellSeal(kin: Int) = seal((wavespellNumber(kin) - 1) * 13 + 1)

    /** Mirror mirror across the Tzolkin: 1<->260, 130<->131, etc. */
    fun occultKin(kin: Int) = 261 - kin

    /** Opposite pole, 130 days away */
    fun antipodeKin(kin: Int) = if (kin <= 130) kin + 130 else kin - 130

    /** The unique kin (1-260) with the given seal (1-20) and tone (1-13). */
    fun kinFromSealTone(seal: Int, tone: Int): Int {
        for (kin in 1..260) if (seal(kin) == seal && tone(kin) == tone) return kin
        error("no kin for seal=$seal tone=$tone") // unreachable: every (seal,tone) pair maps to exactly one kin
    }

    // Destiny Oracle "Fifth Force" partners. Seal formulas are computed 0-based then wrapped to
    // 1-based (see wrapSeal). Guide/Analog/Antipode share the destiny tone; Occult is the full
    // mirror (261 - kin).

    private fun wrapSeal(seal0: Int) = ((seal0 % 20) + 20) % 20 + 1 // 0-based -> wrapped 1-based

    fun guideKin(kin: Int): Int {
        val seal0 = seal(kin) - 1
        val guide0 = when (tone(kin) - 1) {
            0, 5, 10 -> seal0
            1, 6, 11 -> seal0 - 8
            2, 7, 12 -> seal0 + 4
            3, 8 -> seal0 - 4
            else -> seal0 + 8 // tone 4, 9
        }
        return kinFromSealTone(wrapSeal(guide0), tone(kin))
    }

    fun analogKin(kin: Int) = kinFromSealTone(wrapSeal(17 - (seal(kin) - 1)), tone(kin))

    data class MoonPosition(val moon: Int, val dayOfMoon: Int, val isDayOutOfTime: Boolean)

    /** 13 Moons of 28 days each (364 days) + 1 Day Out of Time, year starting July 26. */
    fun moonPositionFor(calendar: Calendar): MoonPosition {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        var dayOfYear = 0
        for (m in 0 until month) dayOfYear += daysInMonth[m]
        dayOfYear += day
        if (day == 29 && month == Calendar.FEBRUARY) {
            dayOfYear = if (hour < 12) 59 else 60 // ponytail: Feb 29 leap-day resolves by hour — before noon = day 59, after = 60
        }
        var offset = dayOfYear - 207 // July 26 = day 207 of a non-leap year
        if (offset < 0) offset += 365

        if (offset == 364) return MoonPosition(13, 28, true) // Day Out of Time
        return MoonPosition(offset / 28 + 1, offset % 28 + 1, false)
    }

    // Psi Bank / Psi Chronometry table: [moon 0-12][day-of-moon 0-27] -> the PSI kin for that day.
    private val psiTable = arrayOf(
        intArrayOf(1, 1, 1, 20, 20, 20, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 241, 241, 241, 260, 260, 260),
        intArrayOf(22, 22, 22, 39, 39, 39, 18, 19, 21, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 222, 222, 222, 239, 239, 239),
        intArrayOf(43, 43, 43, 58, 58, 58, 36, 37, 38, 40, 41, 42, 44, 45, 46, 47, 48, 49, 52, 53, 54, 55, 203, 203, 203, 218, 218, 218),
        intArrayOf(50, 50, 50, 51, 51, 51, 56, 57, 59, 60, 61, 62, 63, 65, 66, 67, 68, 70, 71, 73, 74, 75, 210, 210, 210, 211, 211, 211),
        intArrayOf(64, 64, 64, 77, 77, 77, 76, 78, 79, 80, 81, 82, 83, 84, 86, 87, 89, 90, 91, 92, 94, 95, 184, 184, 184, 197, 197, 197),
        intArrayOf(69, 69, 69, 72, 72, 72, 97, 98, 99, 100, 101, 102, 103, 104, 105, 116, 117, 118, 119, 120, 121, 122, 189, 189, 189, 192, 192, 192),
        intArrayOf(85, 85, 85, 96, 96, 96, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 165, 165, 165, 176, 176, 176),
        intArrayOf(88, 88, 88, 93, 93, 93, 139, 140, 141, 142, 143, 144, 145, 156, 157, 158, 159, 160, 161, 162, 163, 164, 168, 168, 168, 173, 173, 173),
        intArrayOf(106, 106, 106, 115, 115, 115, 166, 167, 169, 170, 171, 172, 174, 175, 177, 178, 179, 180, 181, 182, 183, 185, 146, 146, 146, 155, 155, 155),
        intArrayOf(107, 107, 107, 114, 114, 114, 186, 187, 188, 190, 191, 193, 194, 195, 196, 198, 199, 200, 201, 202, 204, 205, 147, 147, 147, 154, 154, 154),
        intArrayOf(108, 108, 108, 113, 113, 113, 206, 207, 208, 209, 212, 213, 214, 215, 216, 217, 219, 220, 221, 223, 224, 225, 148, 148, 148, 153, 153, 153),
        intArrayOf(109, 109, 109, 112, 112, 112, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 240, 242, 243, 149, 149, 149, 152, 152, 152),
        intArrayOf(110, 110, 110, 111, 111, 111, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254, 255, 256, 257, 258, 259, 150, 150, 150, 151, 151, 151)
    )

    /** The Psi Chrono kin for a date, or null on the Day Out of Time. */
    fun psiKinFor(calendar: Calendar): Int? {
        val position = moonPositionFor(calendar)
        if (position.isDayOutOfTime) return null
        return psiTable[position.moon - 1][position.dayOfMoon - 1]
    }

    /** Moon phase angle in degrees (0=new, 180=full): the Moon's true elongation from the Sun.
     *  Low-precision series from Meeus, Astronomical Algorithms — good to ~0.3°, i.e. minutes of
     *  phase time; a mean-cycle count drifts by up to a day and put the 2026-07-14 new moon in the
     *  wrong phase. Evaluated at local noon so the name represents the whole selected day. */
    fun moonPhaseAngle(calendar: Calendar): Double {
        val noon = (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val t = (noon.timeInMillis / 86_400_000.0 + 2440587.5 - 2451545.0) / 36525.0
        val d = Math.toRadians(297.8501921 + 445267.1114034 * t)  // mean elongation
        val m = Math.toRadians(357.5291092 + 35999.0502909 * t)   // Sun mean anomaly
        val mp = Math.toRadians(134.9633964 + 477198.8675055 * t) // Moon mean anomaly
        val elongation = Math.toDegrees(d) +
            6.289 * kotlin.math.sin(mp) - 2.100 * kotlin.math.sin(m) +
            1.274 * kotlin.math.sin(2 * d - mp) + 0.658 * kotlin.math.sin(2 * d) -
            0.214 * kotlin.math.sin(2 * mp) - 0.110 * kotlin.math.sin(d)
        return (elongation % 360.0 + 360.0) % 360.0
    }

    /** The eight named lunar phases, in cycle order from new to waning crescent. */
    enum class MoonPhase { NEW, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS, FULL, WANING_GIBBOUS, LAST_QUARTER, WANING_CRESCENT }

    /**
     * Names a phase from its elongation angle (0=new, 180=full).
     * Pure counterpart of the MoonPhaseFragment when-chain, extracted so the boundaries are
     * unit-testable (the fragment only maps the result to a localized string). Input is normalized
     * to 0..360 first, so any real angle is accepted.
     */
    fun moonPhaseName(angleDeg: Double): MoonPhase {
        val a = (angleDeg % 360.0 + 360.0) % 360.0
        return when {
            a > 353 || a < 7 -> MoonPhase.NEW
            a <= 83 -> MoonPhase.WAXING_CRESCENT
            a < 97 -> MoonPhase.FIRST_QUARTER
            a <= 173 -> MoonPhase.WAXING_GIBBOUS
            a < 187 -> MoonPhase.FULL
            a <= 263 -> MoonPhase.WANING_GIBBOUS
            a < 277 -> MoonPhase.LAST_QUARTER
            else -> MoonPhase.WANING_CRESCENT
        }
    }

    /** True if both calendars fall on the same civil day (same year + day-of-year). */
    fun isSameDay(a: Calendar, b: Calendar): Boolean =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
}
