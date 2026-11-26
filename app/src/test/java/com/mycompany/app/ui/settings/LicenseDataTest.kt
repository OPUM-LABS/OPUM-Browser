package com.mycompany.app.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** The license list and the license files in `assets/license` match: none missing, none left over. */
class LicenseDataTest {
    private val dir = listOf(File("src/main/assets/license"), File("app/src/main/assets/license")).first { it.isDirectory }
    private val files = dir.listFiles()!!.filter { it.name.endsWith(".txt") }.map { it.name.removeSuffix(".txt") }.toSet()
    private val used = LicenseData.entries.map { it.licenseId }.toSet()

    @Test
    fun everyLicenseHasItsFile() {
        assertEquals(emptySet<String>(), used - files)
    }

    @Test
    fun everyFileIsUsed() {
        assertEquals(emptySet<String>(), files - used)
    }

    @Test
    fun everyLicenseHasAName() {
        for (id in used) assertTrue(id, LicenseData.licenseNames.containsKey(id))
    }

    @Test
    fun namesAreUnique() {
        assertEquals(LicenseData.entries.size, LicenseData.entries.map { it.name }.toSet().size)
    }
}
