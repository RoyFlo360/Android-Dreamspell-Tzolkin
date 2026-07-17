package com.imix.dreamspell_tzolkin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** Kin Combinator math: combinedKin = ((Σ kinNumbers − 1) mod 260) + 1. */
class CombineKinsTest {

    @Test fun single_kin_is_identity() {
        for (kin in 1..260) assertEquals(kin, Dreamspell.combineKins(listOf(kin)))
    }

    @Test fun documented_example() {
        // 214 + 1 + 60 = 275 -> ((275-1) mod 260)+1 = 15
        assertEquals(15, Dreamspell.combineKins(listOf(214, 1, 60)))
    }

    @Test fun order_independent() {
        assertEquals(
            Dreamspell.combineKins(listOf(214, 1, 60)),
            Dreamspell.combineKins(listOf(60, 214, 1))
        )
    }

    @Test fun result_always_in_1_260() {
        // sweep a spread of combinations; every result must be a valid kin
        for (a in 1..260 step 7) for (b in 1..260 step 11) {
            val k = Dreamspell.combineKins(listOf(a, b))
            assert(k in 1..260) { "combine($a,$b)=$k out of range" }
        }
    }

    @Test fun wraps_at_260() {
        assertEquals(260, Dreamspell.combineKins(listOf(260)))          // top edge
        assertEquals(1, Dreamspell.combineKins(listOf(260, 1)))          // 261 -> 1
        assertEquals(2, Dreamspell.combineKins(listOf(260, 2)))          // 262 -> 2
        assertEquals(260, Dreamspell.combineKins(listOf(130, 130)))      // 260 -> 260
        assertEquals(1, Dreamspell.combineKins(listOf(130, 131)))        // 261 -> 1
    }

    @Test fun full_wrap_lands_on_260_not_0() {
        // sum a multiple of 260 must map to kin 260, never 0
        assertEquals(260, Dreamspell.combineKins(listOf(260, 260)))      // 520 -> 260
    }

    @Test fun empty_list_throws() {
        assertThrows(IllegalArgumentException::class.java) {
            Dreamspell.combineKins(emptyList())
        }
    }
}
