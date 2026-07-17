package com.imix.dreamspell_tzolkin

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Tests the pure XML reader [DreamspellData.parseRecords]. Covers the empty/missing/weird-character
 * cases the app's real res/raw content can contain (6 languages incl. CJK & Cyrillic), and confirms
 * malformed input surfaces as an exception rather than silently corrupting data.
 */
class ParseRecordsTest {

    private fun parse(xml: String): List<Map<String, String>> =
        DreamspellData.parseRecords(xml.byteInputStream(), "item") { get ->
            mapOf("a" to get("a"), "b" to get("b"))
        }

    @Test fun reads_multiple_records_and_fields() {
        val out = parse("<root><item><a>hello</a><b>world</b></item><item><a>x</a><b>y</b></item></root>")
        assertEquals(2, out.size)
        assertEquals("hello", out[0]["a"])
        assertEquals("world", out[0]["b"])
        assertEquals("x", out[1]["a"])
    }

    @Test fun missing_field_yields_empty_string_not_crash() {
        val out = parse("<root><item><a>only-a</a></item></root>")
        assertEquals("only-a", out[0]["a"])
        assertEquals("", out[0]["b"])
    }

    @Test fun empty_element_yields_empty_string() {
        val out = parse("<root><item><a></a><b>x</b></item></root>")
        assertEquals("", out[0]["a"])
        assertEquals("x", out[0]["b"])
    }

    @Test fun no_records_yields_empty_list() {
        assertEquals(emptyList<Map<String, String>>(), parse("<root></root>"))
    }

    @Test fun non_ascii_content_survives_roundtrip() {
        val text = "Café Ω 日本語 Кириллица 🌞"
        val out = parse("<root><item><a>$text</a></item></root>")
        assertEquals(text, out[0]["a"])
    }

    @Test fun xml_entities_are_decoded() {
        val out = parse("<root><item><a>Tom &amp; Jerry &lt;3&gt;</a></item></root>")
        assertEquals("Tom & Jerry <3>", out[0]["a"])
    }

    @Test fun cdata_is_read_verbatim() {
        val out = parse("<root><item><a><![CDATA[<raw> & stuff]]></a></item></root>")
        assertEquals("<raw> & stuff", out[0]["a"])
    }

    @Test fun multiline_text_is_preserved() {
        val out = parse("<root><item><a>line1\nline2\nline3</a></item></root>")
        assertTrue(out[0]["a"]!!.contains("line1"))
        assertTrue(out[0]["a"]!!.contains("line3"))
    }

    @Test fun malformed_xml_throws() {
        try {
            parse("<root><item><a>oops</root>")
            fail("expected an exception on malformed XML")
        } catch (e: Exception) {
            // expected: StAX surfaces the well-formedness error rather than returning bad data
        }
    }

    @Test fun empty_input_throws() {
        try {
            parse("")
            fail("expected an exception on empty document")
        } catch (e: Exception) {
            // expected
        }
    }
}
