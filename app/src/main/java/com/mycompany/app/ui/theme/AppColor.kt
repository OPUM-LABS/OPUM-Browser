// The Material Color Utilities classes are marked "library group only" (@RestrictTo) because Google ships them inside
// Material Components. Here they come from our own copy (app/libs/material-color-utilities-1.12.0.jar), which only
// changes when we replace the jar, so the warning about breaking library updates does not apply.
@file:android.annotation.SuppressLint("RestrictedApi")

package com.mycompany.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.google.android.material.color.utilities.DynamicColor
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.DynamicScheme
import com.google.android.material.color.utilities.SchemeFidelity
import com.google.android.material.color.utilities.SchemeNeutral

/**
 * The app's colour schemes, built with Google's Material Color Utilities:
 * - **Colour theme** ([Style.TRUE_COLOR]): the chosen colour seeds the whole Material 3 scheme ("fidelity" variant,
 *   which keeps the colour's own saturation); buttons, switches and highlights use the exact colour when it stands out
 *   enough from the background, and backgrounds get light tints of it (dark ones in dark mode), so text stays readable.
 * - **Light / Dark** ([Style.NEUTRAL]): calm, almost grey schemes from [NEUTRAL_SEED].
 * - **System**: no scheme from here; [OpumTheme] uses the wallpaper colours (Android 12+) or the stock scheme.
 */
object AppColor {

    /** No seed: wallpaper colours on Android 12+, else the stock Material 3 scheme. */
    const val SYSTEM = 0

    /** Seed of the neutral Light and Dark themes (a blue grey). */
    const val NEUTRAL_SEED = 0xFF5F6B7A.toInt()

    enum class Style { TRUE_COLOR, NEUTRAL }

    /** What [OpumTheme] shows: a seed and its style, or [SYSTEM]. */
    data class Choice(val seed: Int, val style: Style)

    /**
     * The colours for the settings: the own colour ([colorTheme], colour [themeColor]), the neutral grey, or null for
     * the device's wallpaper colours ([systemColors]). Light or dark is decided separately.
     */
    fun choiceFor(colorTheme: Boolean, themeColor: Int, systemColors: Boolean): Choice? = when {
        colorTheme -> Choice(themeColor or 0xFF000000.toInt(), Style.TRUE_COLOR)
        !systemColors -> Choice(NEUTRAL_SEED, Style.NEUTRAL)
        else -> null
    }

    /**
     * The colour of browser bars filled with the own colour: the colour itself by day; at night a deep tone of the same
     * hue (HCT tone 25, chroma kept up to 48), so the bars do not glare next to dark pages. A fixed tone, because the
     * true-colour scheme keeps a light seed (yellow) light even in its dark containers.
     */
    fun barColor(seed: Int, dark: Boolean): Int {
        val opaque = seed or 0xFF000000.toInt()
        if (!dark) return opaque
        val hct = Hct.fromInt(opaque)
        return Hct.from(hct.hue, minOf(hct.chroma, 48.0), 25.0).toInt()
    }

    /** Every Material 3 colour role of a seeded scheme, as ARGB ints (also for code that draws with ints). */
    class Roles internal constructor(private val values: IntArray) {
        operator fun get(role: Role): Int = values[role.ordinal]
    }

    enum class Role(internal val color: (MaterialDynamicColors) -> DynamicColor) {
        PRIMARY({ it.primary() }), ON_PRIMARY({ it.onPrimary() }),
        PRIMARY_CONTAINER({ it.primaryContainer() }), ON_PRIMARY_CONTAINER({ it.onPrimaryContainer() }),
        INVERSE_PRIMARY({ it.inversePrimary() }),
        SECONDARY({ it.secondary() }), ON_SECONDARY({ it.onSecondary() }),
        SECONDARY_CONTAINER({ it.secondaryContainer() }), ON_SECONDARY_CONTAINER({ it.onSecondaryContainer() }),
        TERTIARY({ it.tertiary() }), ON_TERTIARY({ it.onTertiary() }),
        TERTIARY_CONTAINER({ it.tertiaryContainer() }), ON_TERTIARY_CONTAINER({ it.onTertiaryContainer() }),
        BACKGROUND({ it.background() }), ON_BACKGROUND({ it.onBackground() }),
        SURFACE({ it.surface() }), ON_SURFACE({ it.onSurface() }),
        SURFACE_VARIANT({ it.surfaceVariant() }), ON_SURFACE_VARIANT({ it.onSurfaceVariant() }),
        SURFACE_TINT({ it.surfaceTint() }),
        INVERSE_SURFACE({ it.inverseSurface() }), INVERSE_ON_SURFACE({ it.inverseOnSurface() }),
        ERROR({ it.error() }), ON_ERROR({ it.onError() }),
        ERROR_CONTAINER({ it.errorContainer() }), ON_ERROR_CONTAINER({ it.onErrorContainer() }),
        OUTLINE({ it.outline() }), OUTLINE_VARIANT({ it.outlineVariant() }),
        SCRIM({ it.scrim() }),
        SURFACE_BRIGHT({ it.surfaceBright() }), SURFACE_DIM({ it.surfaceDim() }),
        SURFACE_CONTAINER({ it.surfaceContainer() }),
        SURFACE_CONTAINER_HIGH({ it.surfaceContainerHigh() }),
        SURFACE_CONTAINER_HIGHEST({ it.surfaceContainerHighest() }),
        SURFACE_CONTAINER_LOW({ it.surfaceContainerLow() }),
        SURFACE_CONTAINER_LOWEST({ it.surfaceContainerLowest() }),
        // the "fixed" roles (same tone in light and dark), which the current ColorScheme constructor takes too
        PRIMARY_FIXED({ it.primaryFixed() }), PRIMARY_FIXED_DIM({ it.primaryFixedDim() }),
        ON_PRIMARY_FIXED({ it.onPrimaryFixed() }), ON_PRIMARY_FIXED_VARIANT({ it.onPrimaryFixedVariant() }),
        SECONDARY_FIXED({ it.secondaryFixed() }), SECONDARY_FIXED_DIM({ it.secondaryFixedDim() }),
        ON_SECONDARY_FIXED({ it.onSecondaryFixed() }), ON_SECONDARY_FIXED_VARIANT({ it.onSecondaryFixedVariant() }),
        TERTIARY_FIXED({ it.tertiaryFixed() }), TERTIARY_FIXED_DIM({ it.tertiaryFixedDim() }),
        ON_TERTIARY_FIXED({ it.onTertiaryFixed() }), ON_TERTIARY_FIXED_VARIANT({ it.onTertiaryFixedVariant() }),
    }

    /** The last 32 schemes (dragging the colour wheel makes many). */
    private val cache = object : LinkedHashMap<Long, Roles>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Roles>?) = size > 32
    }

    /** All roles of the scheme seeded by [seed] (ARGB, alpha ignored), light or [dark], in [style]. Cached. */
    fun roles(seed: Int, dark: Boolean, style: Style = Style.TRUE_COLOR): Roles {
        val opaque = seed or 0xFF000000.toInt()
        val key = (opaque.toLong() and 0xFFFFFFFFL) or (if (dark) 1L shl 32 else 0L) or (style.ordinal.toLong() shl 33)
        synchronized(cache) { cache[key]?.let { return it } }
        val hct = Hct.fromInt(opaque)
        val scheme: DynamicScheme = when (style) {
            Style.TRUE_COLOR -> SchemeFidelity(hct, dark, 0.0)
            Style.NEUTRAL -> SchemeNeutral(hct, dark, 0.0)
        }
        val colors = MaterialDynamicColors()
        val values = IntArray(Role.entries.size) { Role.entries[it].color(colors).getArgb(scheme) }
        if (style == Style.TRUE_COLOR) exactPrimary(values, opaque)
        return Roles(values).also { synchronized(cache) { cache[key] = it } }
    }

    /**
     * The colour theme shows the chosen colour itself on buttons, switches and highlights: [seed] becomes primary
     * (and surface tint) when it has at least 3:1 contrast to the background (the WCAG minimum for controls), with
     * white or black text on it, whichever reads better. Otherwise (e.g. pale yellow on a light background) the
     * scheme's own, darker or lighter, primary stays.
     */
    private fun exactPrimary(values: IntArray, seed: Int) {
        if (contrast(seed, values[Role.SURFACE.ordinal]) < 3.0) return
        values[Role.PRIMARY.ordinal] = seed
        values[Role.SURFACE_TINT.ordinal] = seed
        val white = 0xFFFFFFFF.toInt()
        val black = 0xFF000000.toInt()
        values[Role.ON_PRIMARY.ordinal] = if (contrast(white, seed) >= contrast(black, seed)) white else black
    }

    /**
     * Icon / text kind for a browser bar colour (the bar code's values: 0 light bar, 1 dark bar, else a colour with
     * white icons): a colour on which black has more contrast than white (relative luminance above ~0.18, e.g.
     * yellow, orange, light blue) gets 0, the light bar's dark icons; anything else is returned unchanged.
     */
    fun barInk(colour: Int): Int =
        if (colour != 0 && colour != 1 && contrast(0xFF000000.toInt(), colour or 0xFF000000.toInt()) > contrast(-1, colour or 0xFF000000.toInt())) 0 else colour

    /** WCAG contrast ratio of two opaque colours (1 … 21). */
    fun contrast(a: Int, b: Int): Double {
        val la = luminance(a)
        val lb = luminance(b)
        return (maxOf(la, lb) + 0.05) / (minOf(la, lb) + 0.05)
    }

    private fun luminance(argb: Int): Double {
        fun ch(v: Int): Double {
            val c = v / 255.0
            return if (c <= 0.03928) c / 12.92 else Math.pow((c + 0.055) / 1.055, 2.4)
        }
        return 0.2126 * ch(argb shr 16 and 255) + 0.7152 * ch(argb shr 8 and 255) + 0.0722 * ch(argb and 255)
    }

    /** The Compose colour scheme seeded by [seed] in [style]. */
    fun colorScheme(seed: Int, dark: Boolean, style: Style = Style.TRUE_COLOR): ColorScheme {
        val r = roles(seed, dark, style)
        fun c(role: Role) = Color(r[role])
        return ColorScheme(
            primary = c(Role.PRIMARY), onPrimary = c(Role.ON_PRIMARY),
            primaryContainer = c(Role.PRIMARY_CONTAINER), onPrimaryContainer = c(Role.ON_PRIMARY_CONTAINER),
            inversePrimary = c(Role.INVERSE_PRIMARY),
            secondary = c(Role.SECONDARY), onSecondary = c(Role.ON_SECONDARY),
            secondaryContainer = c(Role.SECONDARY_CONTAINER), onSecondaryContainer = c(Role.ON_SECONDARY_CONTAINER),
            tertiary = c(Role.TERTIARY), onTertiary = c(Role.ON_TERTIARY),
            tertiaryContainer = c(Role.TERTIARY_CONTAINER), onTertiaryContainer = c(Role.ON_TERTIARY_CONTAINER),
            background = c(Role.BACKGROUND), onBackground = c(Role.ON_BACKGROUND),
            surface = c(Role.SURFACE), onSurface = c(Role.ON_SURFACE),
            surfaceVariant = c(Role.SURFACE_VARIANT), onSurfaceVariant = c(Role.ON_SURFACE_VARIANT),
            surfaceTint = c(Role.SURFACE_TINT),
            inverseSurface = c(Role.INVERSE_SURFACE), inverseOnSurface = c(Role.INVERSE_ON_SURFACE),
            error = c(Role.ERROR), onError = c(Role.ON_ERROR),
            errorContainer = c(Role.ERROR_CONTAINER), onErrorContainer = c(Role.ON_ERROR_CONTAINER),
            outline = c(Role.OUTLINE), outlineVariant = c(Role.OUTLINE_VARIANT),
            scrim = c(Role.SCRIM),
            surfaceBright = c(Role.SURFACE_BRIGHT), surfaceDim = c(Role.SURFACE_DIM),
            surfaceContainer = c(Role.SURFACE_CONTAINER),
            surfaceContainerHigh = c(Role.SURFACE_CONTAINER_HIGH),
            surfaceContainerHighest = c(Role.SURFACE_CONTAINER_HIGHEST),
            surfaceContainerLow = c(Role.SURFACE_CONTAINER_LOW),
            surfaceContainerLowest = c(Role.SURFACE_CONTAINER_LOWEST),
            primaryFixed = c(Role.PRIMARY_FIXED), primaryFixedDim = c(Role.PRIMARY_FIXED_DIM),
            onPrimaryFixed = c(Role.ON_PRIMARY_FIXED), onPrimaryFixedVariant = c(Role.ON_PRIMARY_FIXED_VARIANT),
            secondaryFixed = c(Role.SECONDARY_FIXED), secondaryFixedDim = c(Role.SECONDARY_FIXED_DIM),
            onSecondaryFixed = c(Role.ON_SECONDARY_FIXED), onSecondaryFixedVariant = c(Role.ON_SECONDARY_FIXED_VARIANT),
            tertiaryFixed = c(Role.TERTIARY_FIXED), tertiaryFixedDim = c(Role.TERTIARY_FIXED_DIM),
            onTertiaryFixed = c(Role.ON_TERTIARY_FIXED), onTertiaryFixedVariant = c(Role.ON_TERTIARY_FIXED_VARIANT),
        )
    }

}
