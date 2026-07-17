package com.imix.dreamspell_tzolkin

import android.content.Context
import java.io.InputStream
import java.io.InputStreamReader
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

data class GlyphInfo(val name: String, val description: String, val explanation: String, val colorDescription: String)
data class ToneInfo(val name: String, val description: String, val explanation: String)
data class KinInfo(val galacticName: String, val oracle: String)

/**
 * Loads the Dreamspell content shipped in res/raw/{glyphs,tones,dreamspell,
 * wavespell,thirteenmoon}.xml - full names, descriptions and the per-kin oracle text,
 * translated into 9 languages via the standard raw-fr/raw-es/... resource qualifiers (Android
 * picks the right one for the device locale automatically, same as any other resource).
 *
 * Parsed once per process and cached - these files are a few hundred KB total, not worth reloading.
 */
object DreamspellData {
    private var glyphs: List<GlyphInfo>? = null
    private var tones: List<ToneInfo>? = null
    private var kins: List<KinInfo>? = null
    private var wavespellNames: List<String>? = null
    private var moonNames: List<String>? = null
    private var cacheLocale: String? = null

    /** Drop the cache when the app locale changes, so translations refresh (otherwise stale data shows). */
    private fun checkLocale(context: Context) {
        val tag = context.resources.configuration.locales[0].toLanguageTag()
        if (tag != cacheLocale) {
            glyphs = null; tones = null; kins = null; wavespellNames = null; moonNames = null
            cacheLocale = tag
        }
    }

    fun glyph(context: Context, seal: Int): GlyphInfo { checkLocale(context); return loadGlyphs(context)[seal - 1] }
    fun tone(context: Context, tone: Int): ToneInfo { checkLocale(context); return loadTones(context)[tone - 1] }
    fun kin(context: Context, kin: Int): KinInfo { checkLocale(context); return loadKins(context)[kin - 1] }
    fun wavespellName(context: Context, wavespellNumber: Int): String { checkLocale(context); return loadWavespellNames(context)[wavespellNumber - 1] }
    fun moonName(context: Context, moonIndex: Int): String { checkLocale(context); return loadMoonNames(context)[moonIndex - 1] }

    private fun loadGlyphs(context: Context): List<GlyphInfo> = glyphs ?: parseRecords(context, R.raw.glyphs, "glyph") { get ->
        GlyphInfo(get("name"), get("description"), get("explanation"), get("colordescription"))
    }.also { glyphs = it }

    private fun loadTones(context: Context): List<ToneInfo> = tones ?: parseRecords(context, R.raw.tones, "tone") { get ->
        ToneInfo(get("name"), get("description"), get("explanation"))
    }.also { tones = it }

    private fun loadKins(context: Context): List<KinInfo> = kins ?: parseRecords(context, R.raw.dreamspell, "record") { get ->
        KinInfo(get("galacticname"), get("oracle"))
    }.also { kins = it }

    private fun loadWavespellNames(context: Context): List<String> = wavespellNames
        ?: parseRecords(context, R.raw.wavespell, "record") { get -> get("wavespell") }.also { wavespellNames = it }

    private fun loadMoonNames(context: Context): List<String> = moonNames
        ?: parseRecords(context, R.raw.thirteenmoon, "record") { get -> get("moon") }.also { moonNames = it }

    /** Reads a raw resource, delegating to the pure [parseRecords] below. Closes the stream. */
    private fun <T> parseRecords(context: Context, rawResId: Int, itemTag: String, build: (get: (String) -> String) -> T): List<T> =
        context.resources.openRawResource(rawResId).use { parseRecords(it, itemTag, build) }

    /**
     * Pure reader for this app's `<root><ITEM_TAG><field>text</field>...</ITEM_TAG>...</root>` raw XML.
     * Context-free so it is unit-testable off a plain [InputStream] (empty / malformed / non-ASCII input).
     * Missing fields yield "" (never throws); a record with no matching fields still builds from all-empty
     * getters. Malformed XML propagates the parser's exception to the caller.
     */
    internal fun <T> parseRecords(input: InputStream, itemTag: String, build: (get: (String) -> String) -> T): List<T> {
        val results = mutableListOf<T>()
        var fields = mutableMapOf<String, String>()
        var currentTag: String? = null
        // Feed via a UTF-8 Reader: these files carry no <?xml?> prolog, and Android's Expat-backed
        // SAX otherwise mis-detects the byte encoding (rejects well-formed ASCII as "invalid token").
        val source = InputSource(InputStreamReader(input, Charsets.UTF_8))
        saxFactory.newSAXParser().parse(source, object : DefaultHandler() {
            override fun startElement(uri: String?, localName: String?, qName: String, attributes: Attributes?) {
                if (qName == itemTag) fields = mutableMapOf()
                currentTag = qName
            }
            override fun characters(ch: CharArray, start: Int, length: Int) {
                // Accumulate raw; trim once at build time so text split across chunks
                // (entities, multi-chunk) keeps its internal whitespace.
                currentTag?.let { tag -> fields[tag] = fields[tag].orEmpty() + String(ch, start, length) }
            }
            override fun endElement(uri: String?, localName: String?, qName: String) {
                if (qName == itemTag) results.add(build { key -> fields[key].orEmpty().trim() })
                currentTag = null
            }
        })
        return results
    }

    // SAX (javax.xml.parsers) is bundled on both Android and the JVM; StAX is not on Android.
    // Defaults only: the raw files are app-owned (no XXE trust boundary — see docs/TESTING.md N/A
    // ledger), and Android's Expat-backed SAX rejects the extra hardening features outright.
    private val saxFactory: SAXParserFactory = SAXParserFactory.newInstance()
}
