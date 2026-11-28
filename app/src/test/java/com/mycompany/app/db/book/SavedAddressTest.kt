package com.mycompany.app.db.book

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedAddressTest {
    private val home = SavedAddress(id = 3, label = "Home", name = "Anna Muster", street = "Bahnhofstrasse 5b", postcode = "8001", city = "Zürich", country = "Switzerland")

    @Test
    fun streetSplitsNumberAtEndOrStart() {
        assertEquals("Bahnhofstrasse" to "5b", SavedAddress.splitStreet("Bahnhofstrasse 5b"))
        assertEquals("Via Roma" to "12/3", SavedAddress.splitStreet("Via Roma 12/3"))
        assertEquals("Baker Street" to "221B", SavedAddress.splitStreet("221B Baker Street"))
        assertEquals("rue de la Paix" to "12", SavedAddress.splitStreet("12 rue de la Paix"))
        assertEquals("Hauptplatz" to "", SavedAddress.splitStreet("Hauptplatz"))
    }

    @Test
    fun nameSplitsLastWord() {
        assertEquals("Anna Maria" to "Muster", SavedAddress.splitName("Anna Maria Muster"))
        assertEquals("Anna" to "", SavedAddress.splitName("Anna"))
    }

    @Test
    fun sameFieldsAreCovered() {
        // a form with fewer fields, typed with other spacing and case: nothing new
        assertTrue(SavedAddress(street = "bahnhofstrasse  5b", city = "ZÜRICH", postcode = "8001").coveredBy(home))
        assertFalse(SavedAddress(street = "Bahnhofstrasse 5b", postcode = "8001", tel = "+41 44 000 00 00").coveredBy(home))
    }

    @Test
    fun changeOfSamePlaceMergesAndKeepsId() {
        val changed = SavedAddress(name = "Anna Muster", street = "Bahnhofstrasse 5b", postcode = "8001", tel = "+41 44 000 00 00")
        assertTrue(changed.samePlace(home))
        val merged = changed.mergedInto(home)
        assertEquals(3L, merged.id)
        assertEquals("Home", merged.label)
        assertEquals("Zürich", merged.city)
        assertEquals("+41 44 000 00 00", merged.tel)
        assertFalse(SavedAddress(street = "Seestrasse 1", postcode = "8001").samePlace(home))
    }

    @Test
    fun summaryAndParts() {
        assertEquals("Anna Muster, Bahnhofstrasse 5b, 8001 Zürich", home.summary())
        assertEquals(4, home.addressParts)
        assertEquals("Home", home.title())
    }
}
