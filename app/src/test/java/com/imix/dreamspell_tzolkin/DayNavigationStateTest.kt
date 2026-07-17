package com.imix.dreamspell_tzolkin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * State-transition tests for day stepping (the ‹ Today › control): moving the date by ±1 day and
 * observing kin / moon / PSI transitions, plus the [Dreamspell.isSameDay] predicate that drives the
 * "Today" muted/active state in the stepper.
 */
class DayNavigationStateTest {

    private fun next(c: Calendar) = (c.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    private fun prev(c: Calendar) = (c.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }

    @Test fun stepping_forward_then_back_returns_to_the_same_day() {
        val today = cal(2026, 7, 10)
        val roundTrip = prev(next(today))
        assertTrue(Dreamspell.isSameDay(today, roundTrip))
    }

    @Test fun isSameDay_true_for_same_day_different_time() {
        assertTrue(Dreamspell.isSameDay(cal(2026, 7, 10, hour = 1), cal(2026, 7, 10, hour = 23)))
    }

    @Test fun isSameDay_false_for_adjacent_days() {
        val d = cal(2026, 7, 10)
        assertFalse(Dreamspell.isSameDay(d, next(d)))
    }

    @Test fun isSameDay_false_across_new_year() {
        assertFalse(Dreamspell.isSameDay(cal(2025, 12, 31), cal(2026, 1, 1)))
    }

    @Test fun next_day_advances_kin_by_one_across_year_boundary() {
        val dec31 = cal(2025, 12, 31) // noon: no leap quirk
        assertEquals(Dreamspell.kinFor(dec31) % 260 + 1, Dreamspell.kinFor(next(dec31)))
    }

    @Test fun stepping_into_and_out_of_the_day_out_of_time() {
        // Jul 25 is the Day Out of Time; the day before is in moon 13, the day after starts moon 1.
        val doot = cal(2025, 7, 25)
        assertTrue(Dreamspell.moonPositionFor(doot).isDayOutOfTime)
        assertNull(Dreamspell.psiKinFor(doot))

        val before = prev(doot)
        assertFalse(Dreamspell.moonPositionFor(before).isDayOutOfTime)
        assertEquals(13, Dreamspell.moonPositionFor(before).moon)
        assertNotNull(Dreamspell.psiKinFor(before))

        val after = next(doot)
        val p = Dreamspell.moonPositionFor(after)
        assertEquals(1, p.moon)
        assertEquals(1, p.dayOfMoon)
        assertNotNull(Dreamspell.psiKinFor(after))
    }

    @Test fun wavespell_rolls_over_after_tone_13() {
        // Find a tone-13 kin, confirm the next kin is tone 1 of the next wavespell.
        val kin13 = (1..260).first { Dreamspell.tone(it) == 13 }
        val nextKin = kin13 % 260 + 1
        assertEquals(1, Dreamspell.tone(nextKin))
        assertEquals(Dreamspell.wavespellNumber(kin13) + 1, Dreamspell.wavespellNumber(nextKin))
    }
}
