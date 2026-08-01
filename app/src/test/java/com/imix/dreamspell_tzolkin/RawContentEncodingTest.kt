package com.imix.dreamspell_tzolkin

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards the shipped res/raw* content files themselves, not the parser.
 *
 * They carry no `<?xml?>` prolog, so [DreamspellData.parseRecords] decodes them as UTF-8
 * unconditionally. Two ways that has already broken in the wild:
 *  - a file saved as ISO-8859-1 (raw-es/wavespell.xml) -> accents render as U+FFFD diamonds;
 *  - a file saved with a UTF-8 BOM -> the leading U+FEFF is "content in prolog" and SAX throws.
 */
class RawContentEncodingTest {

    private val rawDirs: List<File> =
        File("src/main/res").listFiles { f: File -> f.isDirectory && f.name.startsWith("raw") }
            .orEmpty().sortedBy { it.name }

    private val rawFiles: List<File> = rawDirs.flatMap { it.listFiles()?.toList().orEmpty() }

    @Test fun res_raw_dirs_were_found() {
        assertTrue("no res/raw* dirs found - check the test working directory", rawDirs.size >= 9)
    }

    @Test fun every_raw_file_is_valid_utf8_without_bom_or_replacement_chars() {
        rawFiles.forEach { f ->
            val bytes = f.readBytes()
            assertTrue("${f.path} starts with a UTF-8 BOM", !bytes.take(3).toByteArray()
                .contentEquals(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())))
            val text = bytes.toString(Charsets.UTF_8)
            assertTrue("${f.path} is not valid UTF-8 (mis-decoded bytes)", '�' !in text)
        }
    }

    @Test fun every_raw_file_parses() {
        rawFiles.forEach { f ->
            val tag = if (f.name == "glyphs.xml") "glyph" else if (f.name == "tones.xml") "tone" else "record"
            val records = f.inputStream().use { DreamspellData.parseRecords(it, tag) { get -> get("name") } }
            assertTrue("${f.path} yielded no records", records.isNotEmpty())
        }
    }

    @Test fun spanish_titles_keep_their_accents() {
        val es = File("src/main/res/raw-es")
        val wavespell = es.resolve("wavespell.xml").readText()
        assertTrue("es wavespell lost its accents", "Onda Encantada del Águila Azul" in wavespell)
        assertTrue("es kin names lost their accents",
            "Águila" in es.resolve("dreamspell.xml").readText())
    }
}
