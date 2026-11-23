package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.pref.store.ReadSettings
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetButtons

/**
 * Block-area help ([guideTypeParam]: 0 = how to pick an area (with a help button for the next step, its one-time dot `mNotiAre2`,
 * and the path to the user filters), 1 = how to grow / shrink the pick (with two little pictures), 2 = the selector
 * syntax `^` `$` `*` with examples, else = what block area does, in three cards). OK closes. The help button calls
 * [DialogAreaListener.onHelp], the filter path (when [pickMode]) [DialogAreaListener.onPick]. Compose now (on [ComposeBottomDialog]);
 * same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogGuideArea(mainActivity: android.app.Activity?, guideTypeParam: Int, dialogAreaListener: DialogAreaListener?) : ComposeBottomDialog(mainActivity) {
    @JvmField var appContext: Context? = context
    @JvmField var areaListener: DialogAreaListener? = dialogAreaListener
    @JvmField var guideType: Byte = guideTypeParam.toByte()
    /** The filter path is a link (old field; nobody sets it at the moment). */
    @JvmField var pickMode: Boolean = false

    private var helpDot by mutableStateOf(ReadSettings.notiAre.value)

    init {
        setSheet {
            Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(AppR.drawable.outline_wb_incandescent_2_black_24), null)
                Text(stringResource(AppR.string.tip), style = MaterialTheme.typography.titleMedium, maxLines = 2, modifier = Modifier.padding(start = 16.dp))
            }
            HorizontalDivider()
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).fillMaxWidth().heightIn(min = 72.dp)) {
                when (guideType.toInt()) {
                    0 -> Pick()
                    1 -> Resize()
                    2 -> Syntax()
                    else -> Info()
                }
            }
            SheetButtons({ dismiss() }, mainLabel = AppR.string.ok)
        }
        show()
    }

    @Composable
    private fun Body(path: String, modifier: Modifier = Modifier) {
        Text(path, style = MaterialTheme.typography.bodyMedium, modifier = modifier)
    }

    @Composable
    private fun IconLine(icon: Int, areaGuide: Int) {
        Row(Modifier.fillMaxWidth().padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, Modifier.padding(start = 12.dp).size(20.dp))
            Body(stringResource(areaGuide), Modifier.padding(start = 12.dp))
        }
    }

    @Composable
    private fun ColumnScope.Pick() {
        Column(Modifier.padding(16.dp)) {
            Body(stringResource(AppR.string.area_guide_1) + "\n" + stringResource(AppR.string.area_guide_2))
            Row(Modifier.padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Body(stringResource(AppR.string.area_guide_3), Modifier.weight(1f))
                Box {
                    IconButton(onClick = { help() }) {
                        Icon(painterResource(AppR.drawable.outline_help_black_20), null, tint = MaterialTheme.colorScheme.primary)
                    }
                    if (helpDot) {
                        Box(Modifier.align(Alignment.TopEnd).offset(x = (-10).dp, y = 10.dp).size(8.dp).background(MaterialTheme.colorScheme.error, CircleShape))
                    }
                }
            }
            IconLine(AppR.drawable.outline_shadow_add_black_24, AppR.string.area_guide_4)
            IconLine(AppR.drawable.outline_shadow_minus_black_24, AppR.string.area_guide_5)
            Body(stringResource(AppR.string.area_guide_6), Modifier.padding(top = 24.dp))
            val path = stringResource(AppR.string.clean_mode) + " > " + stringResource(AppR.string.ads_filter) + " > " + stringResource(AppR.string.user_filter)
            if (pickMode) {
                Text(
                    path, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { areaListener?.onPick() }.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            } else {
                Body(path, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            }
        }
    }

    /** The old two pictures: a box with an outer and an inner frame; [outer] marks which one is the pick (red). */
    @Composable
    private fun Frames(outer: Boolean) {
        val red = Color(0xFFF44336.toInt())
        val line = MaterialTheme.colorScheme.outline
        Box(Modifier.padding(start = 44.dp, top = 10.dp).size(160.dp, 80.dp).border(2.dp, if (outer) red else line)) {
            Box(Modifier.fillMaxSize().padding(8.dp).border(2.dp, line))
            Box(Modifier.fillMaxSize().padding(16.dp).border(2.dp, if (outer) line else red))
        }
    }

    @Composable
    private fun ColumnScope.Resize() {
        Column(Modifier.padding(16.dp)) {
            Body(stringResource(AppR.string.area_guide_3))
            Row(Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(AppR.drawable.outline_shadow_add_black_24), null, Modifier.padding(start = 12.dp).size(20.dp))
                Body(stringResource(AppR.string.area_guide_4), Modifier.padding(start = 12.dp))
            }
            Frames(outer = true)
            Row(Modifier.fillMaxWidth().padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(painterResource(AppR.drawable.outline_shadow_minus_black_24), null, Modifier.padding(start = 12.dp).size(20.dp))
                Body(stringResource(AppR.string.area_guide_5), Modifier.padding(start = 12.dp))
            }
            Frames(outer = false)
            Body(stringResource(AppR.string.area_guide_7), Modifier.padding(top = 20.dp))
        }
    }

    @Composable
    private fun ColumnScope.Syntax() {
        Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)) {
            Body(
                stringResource(AppR.string.area_filter_guide_1) + "\n" + stringResource(AppR.string.area_filter_guide_2) + "\n\n" +
                    stringResource(AppR.string.area_filter_guide_3) + "\n" + stringResource(AppR.string.area_filter_guide_4) + "\n\n<Examples>\n"
            )
            val mark = MaterialTheme.colorScheme.primary
            Text(boldPart("^ : start with\n😓  div[id=\"ads_unit_123_xyz\"]\n😄  div[id^=\"ads_unit\"]\n", "^", mark), style = MaterialTheme.typography.bodyMedium)
            Text(boldPart("$ : end with\n😓  div[id=\"123_xyz_ads_unit\"]\n😄  div[id$=\"ads_unit\"]\n", "$", mark), style = MaterialTheme.typography.bodyMedium)
            Text(
                boldPart(
                    "* : contain\n😓  div[id=\"123_xyz_ads_unit_123_xyz\"]\n😄  div[id*=\"ads_unit\"]\n\nads_unit : characters that do not change\n123_xyz : characters that change frequently",
                    "*", mark,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }

    @Composable
    private fun Card(content: @Composable ColumnScope.() -> Unit) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Column(Modifier.padding(16.dp), content = content)
        }
    }

    @Composable
    private fun ColumnScope.Info() {
        val name = stringResource(AppR.string.block_area)
        Column(Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Card {
                Body(
                    stringResource(AppR.string.area_info_1) + "\n" + stringResource(AppR.string.area_info_2) + "\n\n" +
                        stringResource(AppR.string.area_info_3) + "\n" + stringResource(AppR.string.area_info_4) + "\n" + stringResource(AppR.string.area_info_5)
                )
            }
            Body("😓", Modifier.padding(start = 16.dp, top = 24.dp))
            Card {
                Body(stringResource(AppR.string.area_info_6))
                Text(stringResource(AppR.string.area_info_7), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                Body(
                    stringResource(AppR.string.area_info_8) + "\n" + stringResource(AppR.string.area_info_9) + "\n" + stringResource(AppR.string.area_info_10),
                    Modifier.padding(top = 24.dp),
                )
            }
            Body("😄", Modifier.padding(start = 16.dp, top = 24.dp))
            Card {
                Body(String.format(java.util.Locale.US, stringResource(AppR.string.area_info_11), name))
                Text(stringResource(AppR.string.area_info_12), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
                Body(
                    stringResource(AppR.string.area_info_13) + "\n" + String.format(java.util.Locale.US, stringResource(AppR.string.area_info_14), name) +
                        "\n\n" + stringResource(AppR.string.area_info_15) + "\n" + stringResource(AppR.string.area_info_16),
                    Modifier.padding(top = 24.dp, bottom = 8.dp),
                )
            }
        }
    }

    /** Help button: clears the one-time dot, then the listener. */
    private fun help() {
        if (ReadSettings.notiAre.value) {
            ReadSettings.edit(appContext) { set(ReadSettings.notiAre, false) }
            helpDot = false
        }
        areaListener?.onHelp()
    }

    interface DialogAreaListener {
        fun onPick()

        fun onHelp()
    }

    override fun dismiss() {
        appContext = null
        areaListener = null
        super.dismiss()
    }

    companion object {
        /** [str] with the second-or-later occurrence of [str2] marked (as `MainUtil.highlightBackground` does). */
        @JvmStatic
        fun boldPart(full: String, bold: String, color: Color): AnnotatedString {
            val at = full.indexOf(bold, 1)
            if (at < 0) return AnnotatedString(full)
            return buildAnnotatedString {
                append(full.substring(0, at))
                withStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold)) { append(bold) }
                append(full.substring(at + bold.length))
            }
        }
    }
}
