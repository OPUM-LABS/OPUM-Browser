package com.mycompany.app.dialog

import org.junit.Assert.assertEquals
import org.junit.Test

/** Gallery "download selected" names: the title is built from the name without its extension (was "logo.png.png"). */
class DialogDownListNameTest {
    @Test
    fun stripsTheExtension() {
        assertEquals("googlelogo_color_272x92dp", DialogDownList.baseName("googlelogo_color_272x92dp.png"))
        assertEquals("favicon", DialogDownList.baseName("favicon.ico"))
        assertEquals("archive.tar", DialogDownList.baseName("archive.tar.gz"))
    }

    @Test
    fun keepsNamesWithoutAnExtension() {
        assertEquals("image", DialogDownList.baseName("image"))
        assertEquals(".hidden", DialogDownList.baseName(".hidden"))
        assertEquals("", DialogDownList.baseName(""))
    }

    @Test
    fun trailingDotLeavesTheBase() {
        assertEquals("name", DialogDownList.baseName("name."))
    }
}
