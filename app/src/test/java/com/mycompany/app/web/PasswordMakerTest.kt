package com.mycompany.app.web

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PasswordMakerTest {
    @Test
    fun makesSixteenCharactersOfEveryKind() {
        repeat(500) {
            val p = PasswordMaker.make()
            assertEquals(16, p.length)
            assertTrue(p.any { it.isLowerCase() })
            assertTrue(p.any { it.isUpperCase() })
            assertTrue(p.any { it.isDigit() })
            assertTrue(p.any { !it.isLetterOrDigit() })
            assertFalse(p.any { it in "0Oo1lI" })
        }
    }

    @Test
    fun doesNotRepeat() {
        val seen = HashSet<String>()
        repeat(1000) { assertTrue(seen.add(PasswordMaker.make())) }
    }
}
