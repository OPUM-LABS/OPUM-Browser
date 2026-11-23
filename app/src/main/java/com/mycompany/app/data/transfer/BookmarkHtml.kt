package com.mycompany.app.data.transfer

/**
 * One entry of a Netscape bookmark file: a folder (url == null, with [children]) or a bookmark.
 * [addDate] is in seconds since 1970, as the format stores it (0 when unknown).
 */
data class BookmarkNode(
    val title: String,
    val url: String? = null,
    val addDate: Long = 0L,
    val children: List<BookmarkNode> = emptyList(),
) {
    val isFolder: Boolean get() = url == null
}

/**
 * Reads and writes the Netscape bookmark HTML file ("bookmarks.html") that Chrome, Firefox, Edge, Brave, Safari and
 * most other browsers export and import. Plain Kotlin (no Android, no HTML library), so it is unit tested on the JVM.
 *
 * The reader is a small tolerant tag scanner, not a full HTML parser: the format is `<DT><H3>folder</H3>` followed by
 * a `<DL>` list of its entries, and `<DT><A HREF="…">title</A>` for a bookmark. Unclosed `<DT>`/`<p>` tags, upper or
 * lower case, missing `</DL>` at the end and `<DD>` descriptions are all accepted.
 */
object BookmarkHtml {

    /** Folders and bookmarks at the top level of [html] (the file's own root list is not returned as a folder). */
    fun parse(html: String): List<BookmarkNode> {
        val root = Builder("")
        val stack = ArrayList<Builder>()
        stack.add(root)
        var pendingFolder: Builder? = null
        var pos = 0
        val n = html.length
        while (pos < n) {
            val lt = html.indexOf('<', pos)
            if (lt < 0) break
            if (html.startsWith("<!--", lt)) {
                val end = html.indexOf("-->", lt + 4)
                pos = if (end < 0) n else end + 3
                continue
            }
            val gt = tagEnd(html, lt)
            if (gt < 0) break
            val tag = html.substring(lt + 1, gt)
            pos = gt + 1
            val closing = tag.startsWith("/")
            val name = tagName(if (closing) tag.substring(1) else tag)
            when {
                name == "dl" && !closing -> {
                    // the list that follows a folder's <H3> holds that folder's entries
                    val folder = pendingFolder
                    pendingFolder = null
                    if (folder != null) {
                        stack.add(folder)
                    } else if (stack.size > 1 || root.started) {
                        // a stray list without a heading: keep its entries in the current folder
                        stack.add(stack.last())
                    } else {
                        root.started = true
                    }
                }
                name == "dl" && closing -> {
                    pendingFolder = null
                    if (stack.size > 1) stack.removeAt(stack.size - 1)
                }
                name == "h3" && !closing -> {
                    val end = indexOfIgnoreCase(html, "</h3", pos)
                    val text = if (end < 0) html.substring(pos) else html.substring(pos, end)
                    pos = if (end < 0) n else maxOf(pos, tagEnd(html, end).let { if (it < 0) n else it + 1 })
                    val folder = Builder(cleanText(text), attr(tag, "add_date"))
                    stack.last().children.add(folder)
                    pendingFolder = folder
                }
                name == "a" && !closing -> {
                    val end = indexOfIgnoreCase(html, "</a", pos)
                    val text = if (end < 0) html.substring(pos) else html.substring(pos, end)
                    pos = if (end < 0) n else maxOf(pos, tagEnd(html, end).let { if (it < 0) n else it + 1 })
                    val href = attrText(tag, "href")?.trim()
                    if (!href.isNullOrEmpty()) {
                        stack.last().children.add(Leaf(BookmarkNode(cleanText(text), href, attr(tag, "add_date"))))
                    }
                    pendingFolder = null
                }
            }
        }
        return root.build().children
    }

    /** A complete bookmark file for [nodes] that Chrome and Firefox import (folders nested, text escaped). */
    fun write(nodes: List<BookmarkNode>, title: String = "Bookmarks"): String {
        val sb = StringBuilder()
        sb.append("<!DOCTYPE NETSCAPE-Bookmark-file-1>\n")
        sb.append("<!-- This is an automatically generated file.\n     It will be read and overwritten.\n     DO NOT EDIT! -->\n")
        sb.append("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n")
        sb.append("<TITLE>").append(escape(title)).append("</TITLE>\n")
        sb.append("<H1>").append(escape(title)).append("</H1>\n")
        sb.append("<DL><p>\n")
        writeList(sb, nodes, 1)
        sb.append("</DL><p>\n")
        return sb.toString()
    }

    private fun writeList(sb: StringBuilder, nodes: List<BookmarkNode>, depth: Int) {
        val indent = "    ".repeat(depth)
        for (node in nodes) {
            if (node.isFolder) {
                sb.append(indent).append("<DT><H3")
                if (node.addDate > 0) sb.append(" ADD_DATE=\"").append(node.addDate).append("\" LAST_MODIFIED=\"").append(node.addDate).append('"')
                sb.append('>').append(escape(node.title)).append("</H3>\n")
                sb.append(indent).append("<DL><p>\n")
                writeList(sb, node.children, depth + 1)
                sb.append(indent).append("</DL><p>\n")
            } else {
                sb.append(indent).append("<DT><A HREF=\"").append(escape(node.url!!)).append('"')
                if (node.addDate > 0) sb.append(" ADD_DATE=\"").append(node.addDate).append('"')
                sb.append('>').append(escape(node.title)).append("</A>\n")
            }
        }
    }

    /** Escapes text for both element content and double-quoted attribute values. */
    fun escape(text: String): String {
        val sb = StringBuilder(text.length + 8)
        for (c in text) {
            when (c) {
                '&' -> sb.append("&amp;")
                '<' -> sb.append("&lt;")
                '>' -> sb.append("&gt;")
                '"' -> sb.append("&quot;")
                '\'' -> sb.append("&#39;")
                else -> sb.append(c)
            }
        }
        return sb.toString()
    }

    /** Decodes the HTML character references found in exported bookmark files (named basics and numeric ones). */
    fun unescape(text: String): String {
        if (text.indexOf('&') < 0) return text
        val sb = StringBuilder(text.length)
        var i = 0
        while (i < text.length) {
            val c = text[i]
            if (c == '&') {
                val semi = text.indexOf(';', i + 1)
                if (semi > i + 1 && semi - i <= 10) {
                    val ref = text.substring(i + 1, semi)
                    val decoded = decodeRef(ref)
                    if (decoded != null) {
                        sb.append(decoded)
                        i = semi + 1
                        continue
                    }
                }
            }
            sb.append(c)
            i++
        }
        return sb.toString()
    }

    private fun decodeRef(ref: String): String? {
        when (ref.lowercase()) {
            "amp" -> return "&"
            "lt" -> return "<"
            "gt" -> return ">"
            "quot" -> return "\""
            "apos" -> return "'"
            "nbsp" -> return " "
        }
        if (ref.startsWith("#")) {
            val code = try {
                if (ref.length > 1 && (ref[1] == 'x' || ref[1] == 'X')) ref.substring(2).toInt(16) else ref.substring(1).toInt()
            } catch (_: NumberFormatException) {
                return null
            }
            if (code < 0 || code > 0x10FFFF) return null
            return String(Character.toChars(code))
        }
        return null
    }

    private interface Item { fun build(): BookmarkNode }

    private class Leaf(val node: BookmarkNode) : Item { override fun build() = node }

    private class Builder(val title: String, val addDate: Long = 0L) : Item {
        val children = ArrayList<Item>()
        var started = false
        override fun build() = BookmarkNode(title, null, addDate, children.map { it.build() })
    }

    /** Index of the `>` that ends the tag starting at [lt], skipping `>` inside quoted attribute values. */
    private fun tagEnd(html: String, lt: Int): Int {
        var quote = 0.toChar()
        var i = lt + 1
        while (i < html.length) {
            val c = html[i]
            if (quote != 0.toChar()) {
                if (c == quote) quote = 0.toChar()
            } else if (c == '"' || c == '\'') {
                // only treat it as a quote when it opens an attribute value (after '=')
                var j = i - 1
                while (j > lt && html[j].isWhitespace()) j--
                if (html[j] == '=') quote = c
            } else if (c == '>') {
                return i
            }
            i++
        }
        return -1
    }

    private fun tagName(tag: String): String {
        var end = 0
        while (end < tag.length && (tag[end].isLetterOrDigit())) end++
        return tag.substring(0, end).lowercase()
    }

    private fun indexOfIgnoreCase(text: String, what: String, from: Int): Int {
        var i = from
        val last = text.length - what.length
        while (i <= last) {
            if (text.regionMatches(i, what, 0, what.length, ignoreCase = true)) return i
            i++
        }
        return -1
    }

    /** Raw (unescaped) value of attribute [name] in the tag text, or null. */
    private fun attrText(tag: String, name: String): String? {
        var i = 0
        val lower = tag.lowercase()
        while (true) {
            i = lower.indexOf(name, i)
            if (i < 0) return null
            val before = if (i == 0) ' ' else lower[i - 1]
            var j = i + name.length
            while (j < tag.length && tag[j].isWhitespace()) j++
            if (!before.isWhitespace() || j >= tag.length || tag[j] != '=') {
                i += name.length
                continue
            }
            j++
            while (j < tag.length && tag[j].isWhitespace()) j++
            if (j >= tag.length) return ""
            val q = tag[j]
            return if (q == '"' || q == '\'') {
                val end = tag.indexOf(q, j + 1)
                unescape(if (end < 0) tag.substring(j + 1) else tag.substring(j + 1, end))
            } else {
                var end = j
                while (end < tag.length && !tag[end].isWhitespace()) end++
                unescape(tag.substring(j, end))
            }
        }
    }

    private fun attr(tag: String, name: String): Long = attrText(tag, name)?.trim()?.toLongOrNull()?.let {
        // some exporters write microseconds or milliseconds; the format means seconds
        when {
            it > 100_000_000_000_000L -> it / 1_000_000L
            it > 100_000_000_000L -> it / 1_000L
            else -> it
        }
    } ?: 0L

    /** Element text without inner tags, decoded and with whitespace runs collapsed. */
    private fun cleanText(raw: String): String {
        val noTags = raw.replace(Regex("<[^>]*>"), "")
        return unescape(noTags).replace(Regex("\\s+"), " ").trim()
    }
}
