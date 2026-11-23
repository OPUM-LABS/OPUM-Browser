package com.mycompany.app.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class ColorWheelPickerTest {

    @Test
    fun hsvRoundTripsForEveryColourOnAGrid() {
        for (r in 0..255 step 17) for (g in 0..255 step 17) for (b in 0..255 step 17) {
            val argb = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            assertEquals(Integer.toHexString(argb), argb, toArgb(toHsv(argb)))
        }
    }

    @Test
    fun knownColours() {
        assertEquals(0xFFFF0000.toInt(), toArgb(floatArrayOf(0f, 1f, 1f)))
        assertEquals(0xFF00FF00.toInt(), toArgb(floatArrayOf(120f, 1f, 1f)))
        assertEquals(0xFF0000FF.toInt(), toArgb(floatArrayOf(240f, 1f, 1f)))
        assertEquals(0xFFFFFFFF.toInt(), toArgb(floatArrayOf(77f, 0f, 1f)))
        assertEquals(0xFF000000.toInt(), toArgb(floatArrayOf(200f, 1f, 0f)))
        assertEquals(0xFFFF0000.toInt(), toArgb(floatArrayOf(360f, 1f, 1f)))
        val hsv = toHsv(0xFF808080.toInt())
        assertEquals(0f, hsv[1], 0f)
    }
}
