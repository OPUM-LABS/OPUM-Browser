package com.mycompany.app.data.sync

import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource

/**
 * The XBEL bookmark file as the floccus extension writes it for "XBEL in WebDAV" and S3 (2026-11-28): the header and
 * doctype, a comment that carries the highest id given out, then the top-level `<folder>` and `<bookmark>` elements
 * straight under `<xbel>` (the root itself is not an element). A folder is `<folder id="N"><title>…</title>…</folder>`,
 * a bookmark `<bookmark id="N" href="…"><title>…</title></bookmark>`, with floccus' tags in
 * `<info><metadata owner="https://floccus.org"><tag>…</tag></metadata></info>`.
 *
 * Plain Kotlin and the JDK's XML parser, so it is unit tested on the JVM. The doctype is cut off before parsing: it
 * names an external DTD that must never be fetched, and a file with an internal DTD subset is refused.
 */
object Xbel {
    private const val HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<!DOCTYPE xbel PUBLIC \"+//IDN python.org//DTD XML Bookmark Exchange Language 1.0//EN//XML\" " +
        "\"http://pyxml.sourceforge.net/topics/dtds/xbel.dtd\">\n"
    private const val TAG_OWNER = "https://floccus.org"

    class FormatException(message: String) : Exception(message)

    /** Whether [text] looks like an XBEL file (not encrypted, not something else). */
    fun looksLike(text: String): Boolean = text.contains("<xbel")

    fun parse(text: String): SyncFile {
        val start = text.indexOf("<xbel")
        if (start < 0) throw FormatException("no <xbel> element")
        val prolog = text.substring(0, start)
        if (prolog.contains("<!DOCTYPE") && prolog.contains('[')) throw FormatException("internal DTD")
        val highest = HIGHEST.find(text)?.groupValues?.get(1)?.trim()?.toLongOrNull() ?: 0L
        val factory = DocumentBuilderFactory.newInstance()
        try { factory.isExpandEntityReferences = false } catch (_: Exception) {}
        try { factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true) } catch (_: Exception) {}
        val doc = try {
            factory.newDocumentBuilder().parse(InputSource(StringReader(text.substring(start))))
        } catch (e: Exception) {
            throw FormatException(e.message ?: "unreadable XML")
        }
        val root = SyncNode.root()
        var missing = false
        var maxId = highest
        fun read(el: Element, into: SyncNode) {
            var child: Node? = el.firstChild
            while (child != null) {
                if (child is Element) {
                    when (child.tagName) {
                        "folder" -> {
                            val id = child.getAttribute("id").trim()
                            if (id.isEmpty()) missing = true
                            id.toLongOrNull()?.let { if (it > maxId) maxId = it }
                            val node = SyncNode(id, titleOf(child), null)
                            into.children.add(node)
                            read(child, node)
                        }
                        "bookmark" -> {
                            val id = child.getAttribute("id").trim()
                            if (id.isEmpty()) missing = true
                            id.toLongOrNull()?.let { if (it > maxId) maxId = it }
                            into.children.add(SyncNode(id, titleOf(child), child.getAttribute("href"), tagsOf(child)))
                        }
                    }
                }
                child = child.nextSibling
            }
        }
        read(doc.documentElement, root)
        // items without an id (a file written by hand or by another tool) get one; the file is then written again
        if (missing) {
            for (n in root.descendants()) if (n.id.isEmpty()) n.id = (++maxId).toString()
        }
        return SyncFile(root, maxId, missing)
    }

    fun write(root: SyncNode, highestId: Long): String {
        val b = StringBuilder(HEADER)
        b.append("<xbel version=\"1.0\">\n")
        b.append("<!--- highestId :").append(highestId).append(": for Floccus bookmark sync browser extension -->\n")
        fun item(n: SyncNode, depth: Int) {
            val pad = "  ".repeat(depth)
            if (n.isFolder) {
                b.append(pad).append("<folder id=\"").append(esc(n.id)).append("\">\n")
                b.append(pad).append("  <title>").append(esc(n.title)).append("</title>\n")
                for (c in n.children) item(c, depth + 1)
                b.append(pad).append("</folder>\n")
            } else {
                b.append(pad).append("<bookmark href=\"").append(esc(n.url!!)).append("\" id=\"").append(esc(n.id)).append("\">\n")
                b.append(pad).append("  <title>").append(esc(n.title)).append("</title>\n")
                if (n.tags.isNotEmpty()) {
                    b.append(pad).append("  <info>\n").append(pad).append("    <metadata owner=\"").append(TAG_OWNER).append("\">\n")
                    for (t in n.tags) b.append(pad).append("      <tag>").append(esc(t)).append("</tag>\n")
                    b.append(pad).append("    </metadata>\n").append(pad).append("  </info>\n")
                }
                b.append(pad).append("</bookmark>\n")
            }
        }
        for (c in root.children) item(c, 1)
        b.append("</xbel>")
        return b.toString()
    }

    private val HIGHEST = Regex("""highestId\s*:([^:]*):""")

    private fun titleOf(el: Element): String {
        var c: Node? = el.firstChild
        while (c != null) {
            if (c is Element && c.tagName == "title") return c.textContent?.trim().orEmpty()
            c = c.nextSibling
        }
        return ""
    }

    private fun tagsOf(el: Element): List<String> {
        val out = ArrayList<String>()
        val tags = el.getElementsByTagName("tag")
        for (i in 0 until tags.length) {
            val meta = tags.item(i).parentNode as? Element ?: continue
            if (meta.tagName == "metadata" && meta.getAttribute("owner") == TAG_OWNER) {
                tags.item(i).textContent?.trim()?.takeIf { it.isNotEmpty() }?.let { out.add(it) }
            }
        }
        return out
    }

    private fun esc(s: String): String {
        val b = StringBuilder(s.length + 8)
        for (ch in s) {
            when (ch) {
                '&' -> b.append("&amp;")
                '<' -> b.append("&lt;")
                '>' -> b.append("&gt;")
                '"' -> b.append("&quot;")
                '\'' -> b.append("&apos;")
                else -> if (ch < ' ' && ch != '\t' && ch != '\n' && ch != '\r') b.append(' ') else b.append(ch)
            }
        }
        return b.toString()
    }
}
