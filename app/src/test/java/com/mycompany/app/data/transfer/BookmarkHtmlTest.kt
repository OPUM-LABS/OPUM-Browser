package com.mycompany.app.data.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BookmarkHtmlTest {

    /** Shape of a Chrome export: a "Bookmarks bar" folder with a nested folder, plus top-level entries. */
    private val chrome = """
        <!DOCTYPE NETSCAPE-Bookmark-file-1>
        <!-- This is an automatically generated file.
             It will be read and overwritten.
             DO NOT EDIT! -->
        <META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=UTF-8">
        <TITLE>Bookmarks</TITLE>
        <H1>Bookmarks</H1>
        <DL><p>
            <DT><H3 ADD_DATE="1700000000" LAST_MODIFIED="1700000001" PERSONAL_TOOLBAR_FOLDER="true">Bookmarks bar</H3>
            <DL><p>
                <DT><A HREF="https://example.com/" ADD_DATE="1700000002" ICON="data:image/png;base64,AAA=">Example</A>
                <DT><H3 ADD_DATE="1700000003">Work &amp; Play</H3>
                <DL><p>
                    <DT><A HREF="https://example.org/a?x=1&amp;y=2">Tom &quot;&lt;b&gt;&quot; &#39;s &#x263A; page</A>
                    <DT><H3>Deep</H3>
                    <DL><p>
                        <DT><A HREF="https://deep.example/">Deep link</A>
                    </DL><p>
                </DL><p>
            </DL><p>
            <DT><A HREF="https://top.example/">Top</A>
        </DL><p>
    """.trimIndent()

    @Test
    fun parsesNestedFoldersAndEscaping() {
        val nodes = BookmarkHtml.parse(chrome)
        assertEquals(2, nodes.size)
        val bar = nodes[0]
        assertTrue(bar.isFolder)
        assertEquals("Bookmarks bar", bar.title)
        assertEquals(1700000000L, bar.addDate)
        assertEquals(2, bar.children.size)
        assertEquals("https://example.com/", bar.children[0].url)
        assertEquals(1700000002L, bar.children[0].addDate)
        val work = bar.children[1]
        assertEquals("Work & Play", work.title)
        assertEquals("https://example.org/a?x=1&y=2", work.children[0].url)
        assertEquals("Tom \"<b>\" 's ☺ page", work.children[0].title)
        val deep = work.children[1]
        assertEquals("Deep", deep.title)
        assertEquals("Deep link", deep.children.single().title)
        assertEquals("Top", nodes[1].title)
        assertNull(nodes[1].children.firstOrNull())
    }

    @Test
    fun parsesFirefoxStyleLowercaseAndDescriptions() {
        val firefox = """
            <!DOCTYPE NETSCAPE-Bookmark-file-1>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
            <h1>Bookmarks Menu</h1>
            <dl><p>
              <dt><h3 add_date="1" last_modified="2">Mozilla Firefox</h3>
              <dl><p>
                <dt><a href="https://support.mozilla.org/" add_date="1690000000" last_modified="3">Get Help</a>
                <dd>Description with <b>tags</b>
                <dt><a href='https://single.example/' >Single quotes</a>
              </dl><p>
              <dt><a href="place:sort=8&maxResults=10">Recent Tags</a>
            </dl>
        """.trimIndent()
        val nodes = BookmarkHtml.parse(firefox)
        assertEquals(2, nodes.size)
        val folder = nodes[0]
        assertEquals("Mozilla Firefox", folder.title)
        assertEquals(listOf("Get Help", "Single quotes"), folder.children.map { it.title })
        assertEquals("https://single.example/", folder.children[1].url)
        assertEquals("place:sort=8&maxResults=10", nodes[1].url)
    }

    @Test
    fun emptyFolderAndMissingClosingTags() {
        val html = "<DL><p><DT><H3>Empty</H3><DL><p></DL><p><DT><H3>Open</H3><DL><DT><A HREF=\"https://x.example\">X</A>"
        val nodes = BookmarkHtml.parse(html)
        assertEquals(2, nodes.size)
        assertTrue(nodes[0].children.isEmpty())
        assertEquals("X", nodes[1].children.single().title)
    }

    @Test
    fun millisecondDatesBecomeSeconds() {
        val nodes = BookmarkHtml.parse("<DL><DT><A HREF=\"https://a\" ADD_DATE=\"1700000000123\">a</A></DL>")
        assertEquals(1700000000L, nodes.single().addDate)
    }

    @Test
    fun writeThenParseRoundTrips() {
        val tree = listOf(
            BookmarkNode(
                "A & <B>", null, 1700000000L,
                listOf(
                    BookmarkNode("Quote \" and 'apos'", "https://q.example/?a=1&b=\"2\"", 1700000001L),
                    BookmarkNode("Inner", null, 0L, listOf(BookmarkNode("Leaf", "https://leaf.example/"))),
                    BookmarkNode("Empty", null),
                ),
            ),
            BookmarkNode("Root link", "https://root.example/"),
        )
        val html = BookmarkHtml.write(tree)
        assertTrue(html.startsWith("<!DOCTYPE NETSCAPE-Bookmark-file-1>"))
        assertTrue(html.contains("<H3 ADD_DATE=\"1700000000\" LAST_MODIFIED=\"1700000000\">A &amp; &lt;B&gt;</H3>"))
        assertTrue(html.contains("HREF=\"https://q.example/?a=1&amp;b=&quot;2&quot;\""))
        assertEquals(tree, BookmarkHtml.parse(html))
    }

    @Test
    fun unescapeLeavesUnknownEntities() {
        assertEquals("a &bogus; b & c", BookmarkHtml.unescape("a &bogus; b &amp; c"))
        assertEquals("😀", BookmarkHtml.unescape("&#128512;"))
    }
}
