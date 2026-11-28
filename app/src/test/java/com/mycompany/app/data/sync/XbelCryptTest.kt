package com.mycompany.app.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class XbelCryptTest {

    /** As the floccus extension writes it (attributes in its order, tags of a bookmark). */
    private val floccus = """<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xbel PUBLIC "+//IDN python.org//DTD XML Bookmark Exchange Language 1.0//EN//XML" "http://pyxml.sourceforge.net/topics/dtds/xbel.dtd">
<xbel version="1.0">
<!--- highestId :7: for Floccus bookmark sync browser extension -->
<folder id="1">
  <title>Bookmarks bar</title>
  <bookmark href="https://example.com/?a=1&amp;b=2" id="2">
    <title>Tom &amp; "Jerry" &lt;3</title>
    <info><metadata owner="https://floccus.org"><tag>news</tag><tag>daily</tag></metadata></info>
  </bookmark>
  <folder id="3"><title>Deep</title><bookmark href="https://deep.example/" id="4"><title>Deep link</title></bookmark></folder>
</folder>
<bookmark href="https://top.example/" id="5"><title></title></bookmark>
</xbel>"""

    @Test
    fun readsFloccusFile() {
        val f = Xbel.parse(floccus)
        assertEquals(7L, f.highestId)
        assertFalse(f.hadMissingIds)
        assertEquals(2, f.root.children.size)
        val bar = f.root.children[0]
        assertTrue(bar.isFolder)
        assertEquals("1", bar.id)
        assertEquals("Bookmarks bar", bar.title)
        val b = bar.children[0]
        assertEquals("https://example.com/?a=1&b=2", b.url)
        assertEquals("Tom & \"Jerry\" <3", b.title)
        assertEquals(listOf("news", "daily"), b.tags)
        assertEquals("Deep link", bar.children[1].children[0].title)
        assertEquals("", f.root.children[1].title)
    }

    @Test
    fun writeThenReadGivesTheSameTree() {
        val f = Xbel.parse(floccus)
        val again = Xbel.parse(Xbel.write(f.root, f.highestId))
        assertEquals(Xbel.write(f.root, f.highestId), Xbel.write(again.root, again.highestId))
        assertEquals(listOf("news", "daily"), again.root.children[0].children[0].tags)
        assertEquals(7L, again.highestId)
    }

    @Test
    fun missingIdsAreGiven() {
        val f = Xbel.parse("<xbel version=\"1.0\"><folder><title>A</title><bookmark href=\"https://a/\"><title>a</title></bookmark></folder></xbel>")
        assertTrue(f.hadMissingIds)
        val ids = f.root.descendants().map { it.id }
        assertEquals(listOf("1", "2"), ids)
        assertEquals(2L, f.highestId)
    }

    @Test
    fun refusesInternalDtd() {
        try {
            Xbel.parse("<?xml version=\"1.0\"?><!DOCTYPE xbel [<!ENTITY x SYSTEM \"file:///etc/passwd\">]><xbel><bookmark id=\"1\" href=\"&x;\"><title>x</title></bookmark></xbel>")
            fail()
        } catch (_: Xbel.FormatException) {
        }
    }

    /** Made with WebCrypto exactly as floccus' Crypto.ts does it (passphrase "correct horse"). */
    private val floccusEncrypted = """{"ciphertext":"Xef9ZDto+BUZtwLYLCZ+UqoZ0ufZ40QvDxGz8v62dkfKESo7gR38KtUrdpzozR1lPHEGqGBLRsQpacSYljlm45/Wzl1qlscpSTZBikMuvep5jqBsXkXaCX5Vj2hjAvEoRORCiVHB0ddzVyey7ukkcauPqo8y67leIhNdiRL/NCXI8aGSxQQ4E8GtqyyON3UkOXoGtl+WU/ORVP5xG+/IvUrzgJiQPE7WN3BOHl3sop2jSQw5yqm5Igs9U7B4Uz4ULxGfl/G4xCStyU2RE7cZIGhxMm/+2JHd8t1rppSR21h1nP0c39iibgf5wDHAskSgPCvhftI/ohxZvhRdi7dvVba0XVwzulReUBPWQFXvL22ud3E8uTx0tAtUGnZKLCpxVXZBxtHR0w==","salt":"130b118041ecf947a772b0b5ad863209373653f5fa11d3a3b233ae5fc0ced8e66bee143a87ff67313f13e8834e13aa0e5dad3e728224706023d5ff4aca267a25"}"""

    @Test
    fun opensAFloccusEncryptedFile() {
        assertTrue(FloccusCrypt.isEncrypted(floccusEncrypted))
        val key = FloccusCrypt.derive("correct horse", FloccusCrypt.saltOf(floccusEncrypted, "bookmarks.xbel"))
        val plain = FloccusCrypt.decrypt(floccusEncrypted, key)
        val f = Xbel.parse(plain)
        assertEquals("Ex & ample", f.root.children[0].children[0].title)
    }

    @Test
    fun wrongPassphraseIsReported() {
        val key = FloccusCrypt.derive("wrong", FloccusCrypt.saltOf(floccusEncrypted, "bookmarks.xbel"))
        try {
            FloccusCrypt.decrypt(floccusEncrypted, key)
            fail()
        } catch (_: FloccusCrypt.WrongPassphrase) {
        }
    }

    @Test
    fun encryptThenDecrypt() {
        val key = FloccusCrypt.derive("pw", FloccusCrypt.newSalt())
        val text = FloccusCrypt.encrypt("<xbel>ä</xbel>", key)
        assertTrue(FloccusCrypt.isEncrypted(text))
        assertEquals(128, FloccusCrypt.saltOf(text, "x").length)
        val again = FloccusCrypt.derive("pw", FloccusCrypt.saltOf(text, "x"))
        assertEquals("<xbel>ä</xbel>", FloccusCrypt.decrypt(text, again))
    }
}
