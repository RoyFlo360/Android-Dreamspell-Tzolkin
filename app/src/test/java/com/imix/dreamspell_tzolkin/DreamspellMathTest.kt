package com.imix.dreamspell_tzolkin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

/**
 * Unit tests for the pure Tzolkin math in [Dreamspell]. Uses invariants/properties rather than
 * magic kin numbers where possible, so the tests validate the whole table's internal consistency
 * without hard-coding an oracle we can't independently verify.
 */
class DreamspellMathTest {

    // ---- tone / seal: boundary-value analysis over kin 1..260 ----

    @Test fun tone_boundaries() {
        assertEquals(1, Dreamspell.tone(1))
        assertEquals(13, Dreamspell.tone(13))
        assertEquals(1, Dreamspell.tone(14))   // wraps
        assertEquals(13, Dreamspell.tone(260)) // last kin
    }

    @Test fun seal_boundaries() {
        assertEquals(1, Dreamspell.seal(1))
        assertEquals(20, Dreamspell.seal(20))
        assertEquals(1, Dreamspell.seal(21))   // wraps
        assertEquals(20, Dreamspell.seal(260))
    }

    @Test fun tone_and_seal_stay_in_range_for_all_kin() {
        for (kin in 1..260) {
            assertTrue(Dreamspell.tone(kin) in 1..13)
            assertTrue(Dreamspell.seal(kin) in 1..20)
        }
    }

    // ---- kinFromSealTone: bijection (every (seal,tone) maps to exactly one kin, invertible) ----

    @Test fun sealTone_roundtrips_for_all_kin() {
        for (kin in 1..260) {
            assertEquals(kin, Dreamspell.kinFromSealTone(Dreamspell.seal(kin), Dreamspell.tone(kin)))
        }
    }

    @Test fun every_seal_tone_pair_is_unique() {
        val seen = HashSet<Pair<Int, Int>>()
        for (kin in 1..260) seen.add(Dreamspell.seal(kin) to Dreamspell.tone(kin))
        assertEquals(260, seen.size)
    }

    // ---- occult / antipode: involutions (applying twice returns the original) ----

    @Test fun occult_is_an_involution() {
        for (kin in 1..260) assertEquals(kin, Dreamspell.occultKin(Dreamspell.occultKin(kin)))
    }

    @Test fun antipode_is_an_involution() {
        for (kin in 1..260) assertEquals(kin, Dreamspell.antipodeKin(Dreamspell.antipodeKin(kin)))
    }

    @Test fun antipode_is_130_away() {
        assertEquals(131, Dreamspell.antipodeKin(1))
        assertEquals(1, Dreamspell.antipodeKin(131))
        assertEquals(260, Dreamspell.antipodeKin(130))
        assertEquals(130, Dreamspell.antipodeKin(260))
    }

    @Test fun occult_mirrors_across_261() {
        assertEquals(260, Dreamspell.occultKin(1))
        assertEquals(131, Dreamspell.occultKin(130))
    }

    // ---- guide / analog: decision-table + shared-tone property ----

    @Test fun guide_and_analog_share_the_source_tone() {
        for (kin in 1..260) {
            assertEquals(Dreamspell.tone(kin), Dreamspell.tone(Dreamspell.guideKin(kin)))
            assertEquals(Dreamspell.tone(kin), Dreamspell.tone(Dreamspell.analogKin(kin)))
        }
    }

    @Test fun tone_one_kin_are_self_guided() {
        // Decision-table row for tones {1,6,11}-index 0,5,10 -> guide seal == source seal;
        // for tone 1 specifically that makes the guide the kin itself.
        for (kin in 1..260) if (Dreamspell.tone(kin) == 1) assertEquals(kin, Dreamspell.guideKin(kin))
    }

    @Test fun analog_is_an_involution() {
        for (kin in 1..260) assertEquals(kin, Dreamspell.analogKin(Dreamspell.analogKin(kin)))
    }

    @Test fun guide_and_analog_always_valid_kin() {
        for (kin in 1..260) {
            assertTrue(Dreamspell.guideKin(kin) in 1..260)
            assertTrue(Dreamspell.analogKin(kin) in 1..260)
        }
    }

    // ---- kinFor: the calendar advances exactly +1 kin per day (mod 260), table continuity ----

    @Test fun kin_advances_by_one_each_day_except_the_repeated_leap_day() {
        // The Tzolkin advances +1/day across month ends, year%52 rollovers and leap years — with one
        // real exception: Feb 29 repeats. At noon, Feb 29 shares Mar 1's kin (the morning<12 branch
        // instead shares Feb 28's), so the Feb-29 -> Mar-1 step is +0. Everything else is +1.
        val c = cal(2000, 1, 1) // noon
        var prev = Dreamspell.kinFor(c)
        repeat(4000) {
            val wasLeapDay = c.get(Calendar.MONTH) == Calendar.FEBRUARY && c.get(Calendar.DAY_OF_MONTH) == 29
            c.add(Calendar.DAY_OF_YEAR, 1)
            val next = Dreamspell.kinFor(c)
            val expected = if (wasLeapDay) prev else prev % 260 + 1
            assertEquals("day ${c.time}", expected, next)
            prev = next
        }
    }

    @Test fun kinFor_always_in_range() {
        val c = cal(1975, 3, 3)
        repeat(3000) {
            assertTrue(Dreamspell.kinFor(c) in 1..260)
            c.add(Calendar.DAY_OF_YEAR, 7)
        }
    }

    @Test fun leap_day_morning_is_one_kin_behind_afternoon() {
        // The documented Feb-29 hour<12 quirk (kinFor L40).
        val morning = Dreamspell.kinFor(cal(2024, 2, 29, hour = 11))
        val afternoon = Dreamspell.kinFor(cal(2024, 2, 29, hour = 13))
        assertEquals(afternoon - 1, morning)
    }

    // ---- 13-Moon calendar + PSI ----

    @Test fun moon_year_starts_on_july_26() {
        val p = Dreamspell.moonPositionFor(cal(2025, 7, 26))
        assertEquals(1, p.moon)
        assertEquals(1, p.dayOfMoon)
        assertTrue(!p.isDayOutOfTime)
    }

    @Test fun july_25_is_the_day_out_of_time() {
        assertTrue(Dreamspell.moonPositionFor(cal(2025, 7, 25)).isDayOutOfTime)
    }

    @Test fun psi_is_null_on_day_out_of_time_and_set_otherwise() {
        assertEquals(null, Dreamspell.psiKinFor(cal(2025, 7, 25)))         // Day Out of Time
        assertEquals(1, Dreamspell.psiKinFor(cal(2025, 7, 26)))           // psiTable[0][0]
    }

    @Test fun wavespell_number_and_seal() {
        assertEquals(1, Dreamspell.wavespellNumber(1))
        assertEquals(1, Dreamspell.wavespellNumber(13))
        assertEquals(2, Dreamspell.wavespellNumber(14))
        assertEquals(20, Dreamspell.wavespellNumber(260))
    }

    // ---- moon phase angle: numeric sanity + determinism ----

    @Test fun moon_phase_angle_is_normalized_and_deterministic() {
        val d = cal(2026, 7, 14)
        val a1 = Dreamspell.moonPhaseAngle(d)
        val a2 = Dreamspell.moonPhaseAngle(d)
        assertEquals(a1, a2, 0.0)                 // deterministic
        assertTrue(a1 in 0.0..360.0)              // normalized
    }
}
