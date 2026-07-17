package com.imix.dreamspell_tzolkin

import java.util.Calendar
import java.util.GregorianCalendar

/** Build a Calendar at a fixed local time. Month is 1-based here (Jan=1) for readable tests. */
internal fun cal(year: Int, month1: Int, day: Int, hour: Int = 12): Calendar =
    GregorianCalendar(year, month1 - 1, day, hour, 0, 0)
