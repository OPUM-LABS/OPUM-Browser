package com.mycompany.app.data.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Fake test logins only. */
class PasswordCsvTest {

    @Test
    fun parsesChromeFormatWithQuotingAndCommas() {
        val csv = "﻿name,url,username,password,note\r\n" +
            "example.com,https://example.com/login,alice,\"p,a\"\"ss\",\"line 1\nline 2\"\r\n" +
            "test.example,https://test.example/,,secret,\r\n" +
            "\r\n" +
            "nopass.example,https://nopass.example/,bob,,\r\n"
        val result = PasswordCsv.parse(csv)
        assertTrue(result.headerFound)
        assertEquals(2, result.entries.size)
        assertEquals(1, result.failed)
        val a = result.entries[0]
        assertEquals("https://example.com/login", a.url)
        assertEquals("alice", a.username)
        assertEquals("p,a\"ss", a.password)
        assertEquals("example.com", a.name)
        assertEquals("line 1\nline 2", a.note)
        assertEquals("", result.entries[1].username)
    }

    @Test
    fun parsesOldChromeFormatWithoutNote() {
        val result = PasswordCsv.parse("name,url,username,password\nsite,https://site.example,u,p\n")
        assertEquals(listOf(PasswordEntry("https://site.example", "u", "p", "site", "")), result.entries)
    }

    @Test
    fun parsesFirefoxFormat() {
        val csv = "\"url\",\"username\",\"password\",\"httpRealm\",\"formActionOrigin\",\"guid\",\"timeCreated\",\"timeLastUsed\",\"timePasswordChanged\"\n" +
            "\"https://fx.example\",\"carol@example.com\",\"x,y \"\"z\"\"\",,\"https://fx.example\",\"{0000-1111}\",\"1700000000000\",\"1700000000000\",\"1700000000000\"\n" +
            "\"https://realm.example\",\"dave\",\"pw\",\"Realm, with comma\",,\"{2222}\",\"1\",\"1\",\"1\"\n"
        val result = PasswordCsv.parse(csv)
        assertEquals(0, result.failed)
        assertEquals(2, result.entries.size)
        assertEquals(PasswordEntry("https://fx.example", "carol@example.com", "x,y \"z\""), result.entries[0])
        assertEquals("pw", result.entries[1].password)
    }

    @Test
    fun unknownHeaderIsNotUsed() {
        val result = PasswordCsv.parse("a,b,c\n1,2,3\n")
        assertFalse(result.headerFound)
        assertTrue(result.entries.isEmpty())
    }

    @Test
    fun writeQuotesOnlyWhereNeededAndRoundTrips() {
        val entries = listOf(
            PasswordEntry("https://a.example/", "alice", "plain", "a.example", ""),
            PasswordEntry("https://b.example/?q=1,2", "bob \"b\"", "co,mma\nnew\r\nline", "b.example", " padded "),
        )
        val csv = PasswordCsv.write(entries)
        val lines = csv.split('\n')
        assertEquals(PasswordCsv.CHROME_HEADER, lines[0])
        assertEquals("a.example,https://a.example/,alice,plain,", lines[1])
        assertTrue(csv.contains("\"bob \"\"b\"\"\""))
        assertTrue(csv.contains("\" padded \""))
        assertEquals(entries, PasswordCsv.parse(csv).entries)
    }

    @Test
    fun rowsWithoutTrailingNewline() {
        assertEquals(listOf(listOf("a", "b"), listOf("1", "")), PasswordCsv.parseRows("a,b\n1,"))
        assertEquals(listOf(listOf("x")), PasswordCsv.parseRows("\"x\""))
    }
}
