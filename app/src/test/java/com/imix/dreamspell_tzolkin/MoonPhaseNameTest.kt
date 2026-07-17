package com.imix.dreamspell_tzolkin

import com.imix.dreamspell_tzolkin.Dreamspell.MoonPhase
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Boundary-value analysis for [Dreamspell.moonPhaseName]. The eight phases are separated by the
 * thresholds 7, 83, 97, 173, 187, 263, 277, 353; each is tested on both sides plus the normalization.
 */
class MoonPhaseNameTest {

    private fun name(a: Double) = Dreamspell.moonPhaseName(a)

    @Test fun new_moon_around_zero() {
        assertEquals(MoonPhase.NEW, name(0.0))
        assertEquals(MoonPhase.NEW, name(6.999))
        assertEquals(MoonPhase.NEW, name(353.001))
        // note: 353.0 is NOT new (needs > 353) — see boundary_353 test
    }

    @Test fun boundary_7_new_to_waxing_crescent() {
        assertEquals(MoonPhase.NEW, name(6.999))
        assertEquals(MoonPhase.WAXING_CRESCENT, name(7.0))
    }

    @Test fun boundary_83_crescent_to_first_quarter() {
        assertEquals(MoonPhase.WAXING_CRESCENT, name(83.0))
        assertEquals(MoonPhase.FIRST_QUARTER, name(83.001))
    }

    @Test fun boundary_97_first_quarter_to_waxing_gibbous() {
        assertEquals(MoonPhase.FIRST_QUARTER, name(96.999))
        assertEquals(MoonPhase.WAXING_GIBBOUS, name(97.0))
    }

    @Test fun boundary_173_gibbous_to_full() {
        assertEquals(MoonPhase.WAXING_GIBBOUS, name(173.0))
        assertEquals(MoonPhase.FULL, name(173.001))
    }

    @Test fun boundary_187_full_to_waning_gibbous() {
        assertEquals(MoonPhase.FULL, name(186.999))
        assertEquals(MoonPhase.WANING_GIBBOUS, name(187.0))
    }

    @Test fun boundary_263_waning_gibbous_to_last_quarter() {
        assertEquals(MoonPhase.WANING_GIBBOUS, name(263.0))
        assertEquals(MoonPhase.LAST_QUARTER, name(263.001))
    }

    @Test fun boundary_277_last_quarter_to_waning_crescent() {
        assertEquals(MoonPhase.LAST_QUARTER, name(276.999))
        assertEquals(MoonPhase.WANING_CRESCENT, name(277.0))
    }

    @Test fun boundary_353_waning_crescent_to_new() {
        assertEquals(MoonPhase.WANING_CRESCENT, name(353.0))
        assertEquals(MoonPhase.NEW, name(353.001))
    }

    @Test fun angles_outside_0_360_are_normalized() {
        assertEquals(MoonPhase.NEW, name(360.0))       // -> 0
        assertEquals(MoonPhase.NEW, name(-5.0))        // -> 355
        assertEquals(MoonPhase.FULL, name(180.0 + 360.0))
    }
}
