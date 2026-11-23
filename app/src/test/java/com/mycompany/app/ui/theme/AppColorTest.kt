package com.mycompany.app.ui.theme

import com.google.android.material.color.utilities.Hct
import com.mycompany.app.ui.theme.AppColor.Role
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

class AppColorTest {

    private fun luminance(argb: Int): Double {
        fun ch(v: Int): Double {
            val c = v / 255.0
            return if (c <= 0.03928) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * ch(argb shr 16 and 255) + 0.7152 * ch(argb shr 8 and 255) + 0.0722 * ch(argb and 255)
    }

    private fun contrast(a: Int, b: Int): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    private fun hueDistance(a: Double, b: Double): Double = abs(((a - b + 540) % 360) - 180)

    private val seeds = intArrayOf(
        0xFF1E88E5.toInt(), // blue
        0xFFE53935.toInt(), // red
        0xFF43A047.toInt(), // green
        0xFFFFEB3B.toInt(), // very light (yellow)
        0xFF000000.toInt(), // black: grey scheme
        0xFFFFFFFF.toInt(), // white: grey scheme
    )

    @Test
    fun lightSchemeHasLightTintedSurfacesAndReadableText() {
        for (seed in seeds) {
            val r = AppColor.roles(seed, dark = false)
            assertTrue("surface light for $seed", luminance(r[Role.SURFACE_CONTAINER]) > 0.6)
            assertTrue("surface light for $seed", luminance(r[Role.SURFACE_CONTAINER_LOW]) > 0.6)
            assertTrue(contrast(r[Role.ON_SURFACE], r[Role.SURFACE_CONTAINER_LOW]) >= 4.5)
            assertTrue(contrast(r[Role.ON_PRIMARY], r[Role.PRIMARY]) >= 4.5)
            assertTrue(contrast(r[Role.ON_PRIMARY_CONTAINER], r[Role.PRIMARY_CONTAINER]) >= 4.5)
        }
    }

    @Test
    fun darkSchemeHasDarkSurfacesAndReadableText() {
        for (seed in seeds) {
            val r = AppColor.roles(seed, dark = true)
            assertTrue("surface dark for $seed", luminance(r[Role.SURFACE_CONTAINER]) < 0.1)
            assertTrue(contrast(r[Role.ON_SURFACE], r[Role.SURFACE_CONTAINER_LOW]) >= 4.5)
            assertTrue(contrast(r[Role.ON_PRIMARY], r[Role.PRIMARY]) >= 4.5)
        }
    }

    @Test
    fun colouredSeedTintsSurfacesWithItsHue() {
        val seed = 0xFFE53935.toInt()
        val seedHue = Hct.fromInt(seed).hue
        val r = AppColor.roles(seed, dark = false)
        val surface = Hct.fromInt(r[Role.SURFACE_CONTAINER])
        assertTrue("surface is tinted, not grey", surface.chroma > 1.0)
        assertTrue(hueDistance(surface.hue, seedHue) < 30)
    }

    @Test
    fun trueColourKeepsTheExactColourOnControls() {
        // fully saturated colours stay exact (light and dark)
        for (seed in intArrayOf(0xFFFF0000.toInt(), 0xFF0000FF.toInt(), 0xFF43A047.toInt())) {
            assertEquals(seed, AppColor.roles(seed, dark = false)[Role.PRIMARY])
        }
        assertEquals(0xFFFF0000.toInt(), AppColor.roles(0xFFFF0000.toInt(), dark = true)[Role.PRIMARY])
        // too pale on a light background (no 3:1 contrast): the scheme's darker primary stays
        val pale = 0xFFFFF59D.toInt()
        val r = AppColor.roles(pale, dark = false)
        assertTrue(r[Role.PRIMARY] != pale)
        assertTrue(AppColor.contrast(r[Role.PRIMARY], r[Role.SURFACE]) >= 3.0)
    }

    @Test
    fun neutralThemeIsAlmostGrey() {
        for (dark in booleanArrayOf(false, true)) {
            val r = AppColor.roles(AppColor.NEUTRAL_SEED, dark, AppColor.Style.NEUTRAL)
            assertTrue(Hct.fromInt(r[Role.SURFACE_CONTAINER]).chroma < 4.0)
            assertTrue(Hct.fromInt(r[Role.PRIMARY]).chroma < 16.0)
            assertTrue(contrast(r[Role.ON_SURFACE], r[Role.SURFACE]) >= 4.5)
        }
    }

    @Test
    fun rolesAreOpaqueAndCached() {
        val r = AppColor.roles(0xFF3366CC.toInt(), dark = false)
        for (role in Role.entries) {
            if (role == Role.SCRIM) continue
            assertEquals("$role opaque", 0xFF, r[role] ushr 24)
        }
        assertSame(r, AppColor.roles(0x003366CC, dark = false)) // alpha of the seed is ignored
    }

    @Test
    fun choiceFollowsTheThemeSetting() {
        assertEquals(AppColor.Choice(0xFF123456.toInt(), AppColor.Style.TRUE_COLOR), AppColor.choiceFor(true, 0x00123456, true))
        assertEquals(AppColor.Choice(AppColor.NEUTRAL_SEED, AppColor.Style.NEUTRAL), AppColor.choiceFor(false, 0, false))
        assertEquals(null, AppColor.choiceFor(false, 0, true))
    }

    @Test
    fun barColourIsTheColourByDayAndADeepToneOfItAtNight() {
        for (seed in intArrayOf(0xFFFFEB3B.toInt(), 0xFF2196F3.toInt(), 0xFFE91E63.toInt(), 0xFF4CAF50.toInt())) {
            assertEquals(seed, AppColor.barColor(seed and 0xFFFFFF, dark = false))
            val night = AppColor.barColor(seed, dark = true)
            assertTrue("night bar darker for $seed", luminance(night) < luminance(seed))
            assertTrue("night bar dark for $seed", luminance(night) < 0.2)
            assertTrue("night bar keeps the hue of $seed", hueDistance(Hct.fromInt(night).hue, Hct.fromInt(seed).hue) < 30)
        }
    }

    @Test
    fun barInkGivesDarkIconsOnLightColours() {
        assertEquals(0, AppColor.barInk(0))
        assertEquals(1, AppColor.barInk(1))
        assertEquals(0, AppColor.barInk(0xFFFFEB3B.toInt())) // yellow: dark icons
        assertEquals(0, AppColor.barInk(0xFFFFFFFF.toInt()))
        assertEquals(0xFF3F51B5.toInt(), AppColor.barInk(0xFF3F51B5.toInt())) // indigo: white icons
        assertEquals(0xFFB71C1C.toInt(), AppColor.barInk(0xFFB71C1C.toInt())) // dark red: white icons
        // whichever ink is used, it is readable on the bar colour
        assertEquals(0, AppColor.barInk(0xFFFB8C00.toInt())) // orange: black reads better than white
        for (c in intArrayOf(0xFFFFEB3B.toInt(), 0xFF00E5FF.toInt(), 0xFF3F51B5.toInt(), 0xFFE53935.toInt(), 0xFF43A047.toInt(), 0xFF000000.toInt(), 0xFFFB8C00.toInt(), 0xFF808080.toInt())) {
            val ink = if (AppColor.barInk(c) == 0) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            assertTrue("ink on ${Integer.toHexString(c)}", AppColor.contrast(ink, c) >= 4.5)
        }
    }
}
