package com.mycompany.app.ui.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.webkit.WebView
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingsListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookAgent
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.PrefState

/*
 * Rows that moved between settings pages when the settings were regrouped (2026-11-19): the old "Advanced" and "Site"
 * pages were split up, text size and font went to Appearance. Each row keeps its own state and dialog, so a page only
 * adds the section. [version] is bumped by the page's reset so the rows read their values again.
 */

@Composable
private fun <T> rememberPrefAt(version: Int, read: () -> T, write: (T) -> Unit): PrefState<T> =
    remember(version) { PrefState(read(), write) }

/** A choice row with its dialog. */
@Composable
private fun ChoiceRowWithDialog(title: String, value: String, options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    SettingChoiceRow(title = title, value = value, onClick = { open = true })
    if (open) ChoiceDialog(title = title, options = options, selectedIndex = selected, onSelect = onSelect, onDismiss = { open = false })
}

// ---- Privacy: cookies, JavaScript, HTTPS (from Advanced) and site permissions (from Site) -----------------------

fun SettingsListScope.privacyWebSection(version: Int, texts: (Int) -> String) {
    settingsHeader(texts(R.string.cookie))
    item {
        val context = LocalContext.current
        val cookie = rememberPrefAt(version, { BrowserSettings.cookieType.value }) {
            BrowserSettings.edit(context) { set(BrowserSettings.cookieType, it) }; MainApp.applyCookieSetting(context)
        }
        ChoiceRowWithDialog(
            title = stringResource(R.string.accept_cookie),
            value = stringResource(MainConst.cookieNames[cookie.value]),
            options = MainConst.cookieValues.map { stringResource(MainConst.cookieNames[it]) },
            selected = MainConst.cookieValues.indexOf(cookie.value),
            onSelect = { cookie.set(MainConst.cookieValues[it]) },
        )
    }
    item {
        val context = LocalContext.current
        val third = rememberPrefAt(version, { BrowserSettings.thirdType.value }) { BrowserSettings.edit(context) { set(BrowserSettings.thirdType, it) } }
        ChoiceRowWithDialog(
            title = stringResource(R.string.third_cookie),
            value = stringResource(MainConst.cookieNames[third.value]),
            options = MainConst.cookieValues.map { stringResource(MainConst.cookieNames[it]) },
            selected = MainConst.cookieValues.indexOf(third.value),
            onSelect = { third.set(MainConst.cookieValues[it]) },
        )
    }
    settingsHeader("JavaScript")
    item {
        val context = LocalContext.current
        val js = rememberPrefAt(version, { BrowserSettings.enableJs.value }) { BrowserSettings.edit(context) { set(BrowserSettings.enableJs, it) } }
        SettingSwitchRow(
            title = stringResource(R.string.java_script),
            checked = js.value,
            onCheckedChange = js::set,
        )
    }
    item {
        val context = LocalContext.current
        SettingRow(
            title = stringResource(R.string.js_black),
            summary = stringResource(R.string.js_black_info),
            onClick = { com.mycompany.app.ui.lists.SiteListActivity.open(context, 27) },
        )
    }
    settingsFooter(texts(R.string.java_script_info) + "\n" + texts(R.string.dark_mode_info_2))
    settingsHeader(texts(R.string.hd_connection))
    item {
        val context = LocalContext.current
        val https = rememberPrefAt(version, { PdfSettings.onlyHttps.value }) { PdfSettings.edit(context) { set(PdfSettings.onlyHttps, it) } }
        SettingSwitchRow(
            title = stringResource(R.string.only_https),
            checked = https.value,
            onCheckedChange = https::set,
        )
    }
    item {
        val context = LocalContext.current
        val ssl = rememberPrefAt(version, { PdfSettings.blockSsl.value }) { PdfSettings.edit(context) { set(PdfSettings.blockSsl, it) } }
        SettingSwitchRow(
            title = stringResource(R.string.block_ssl),
            checked = ssl.value,
            onCheckedChange = ssl::set,
        )
    }
    settingsFooter(texts(R.string.only_https_info) + "\n" + texts(R.string.block_ssl_info))
    settingsHeader(texts(R.string.hd_sites))
    item {
        val context = LocalContext.current
        SettingRow(
            title = stringResource(R.string.pms_site_settings),
            summary = stringResource(R.string.ss_entry_info),
            onClick = { com.mycompany.app.ui.settings.SiteSettingsActivity.open(context, null) },
        )
    }
}

/** Restores the defaults of [privacyWebSection]. */
fun resetPrivacyWeb(context: Context) {
    if (BrowserSettings.cookieType.value != 0) {
        BrowserSettings.editLocal { set(BrowserSettings.cookieType, 0) }; PrefSet.removeNow(context, 14, "mCookieType"); MainApp.applyCookieSetting(context)
    }
    if (!BrowserSettings.thirdType.isDefault) BrowserSettings.reset(context, listOf(BrowserSettings.thirdType))
    if (!BrowserSettings.enableJs.value) BrowserSettings.reset(context, listOf(BrowserSettings.enableJs))
    if (!PdfSettings.onlyHttps.isDefault) PdfSettings.reset(context, listOf(PdfSettings.onlyHttps))
    if (PdfSettings.blockSsl.value) PdfSettings.reset(context, listOf(PdfSettings.blockSsl))
}

// ---- Web pages: data saving and AMP (from Advanced); user agent and link handling (from Site) --------------------

fun SettingsListScope.pageDataSection(version: Int, texts: (Int) -> String) {
    settingsHeader(texts(R.string.hd_data))
    item {
        val context = LocalContext.current
        val saveData = rememberPrefAt(version, { PdfSettings.saveData.value }) { PdfSettings.edit(context) { set(PdfSettings.saveData, it) } }
        SettingSwitchRow(
            title = stringResource(R.string.save_data),
            checked = saveData.value,
            onCheckedChange = saveData::set,
        )
    }
    item {
        val context = LocalContext.current
        val blockAmp = rememberPrefAt(version, { PdfSettings.blockAmp.value }) { PdfSettings.edit(context) { set(PdfSettings.blockAmp, it) } }
        SettingSwitchRow(
            title = stringResource(R.string.block_amp),
            checked = blockAmp.value,
            onCheckedChange = blockAmp::set,
        )
    }
    settingsFooter(texts(R.string.save_data) + ": " + texts(R.string.not_support_site) + "\n" + texts(R.string.block_amp_info))
}

fun resetPageData(context: Context) {
    if (PdfSettings.saveData.value) PdfSettings.reset(context, listOf(PdfSettings.saveData))
    if (!PdfSettings.blockAmp.value) PdfSettings.reset(context, listOf(PdfSettings.blockAmp))
}

fun SettingsListScope.pageLinksSection(version: Int, texts: (Int) -> String) {
    settingsHeader(texts(R.string.hd_links))
    item {
        val context = LocalContext.current
        var tick by remember { mutableIntStateOf(0) }
        OnScreenResume { tick++ } // the list screen changes the agent
        fun agentName(): String? =
            if (ZtwoSettings.agentType.value < 4) MainConst.agentNames[ZtwoSettings.agentType.value]
            else DbBookAgent.titleById(context, (ZtwoSettings.agentType.value - 100).toLong())
        SettingChoiceRow(
            title = stringResource(R.string.user_agent),
            value = "$tick$version".let { agentName() ?: "" },
            onClick = { context.startActivity(Intent(context, com.mycompany.app.ui.lists.UserAgentListActivity::class.java)) },
        )
    }
    item {
        val context = LocalContext.current
        val linkApp = rememberPrefAt(version, { AlbumSettings.linkApp.value }) { AlbumSettings.edit(context) { set(AlbumSettings.linkApp, it) } }
        SettingSwitchRow(title = stringResource(R.string.link_app), checked = linkApp.value, onCheckedChange = linkApp::set)
    }
    item {
        val context = LocalContext.current
        val openLink = rememberPrefAt(version, { ZoneSettings.openLink.value }) { ZoneSettings.edit(context) { set(ZoneSettings.openLink, it) } }
        OnScreenResume { openLink.reload(ZoneSettings.openLink.value) }
        ChoiceRowWithDialog(
            title = stringResource(R.string.link) + " (" + stringResource(R.string.long_press) + ")",
            value = stringResource(MainConst.linkMenuNames[openLink.value]),
            options = MainConst.linkMenuValues.map { stringResource(MainConst.linkMenuNames[it]) },
            selected = MainConst.linkMenuValues.indexOf(openLink.value),
            onSelect = { openLink.set(MainConst.linkMenuValues[it]) },
        )
    }
    item {
        val context = LocalContext.current
        val openImage = rememberPrefAt(version, { ZoneSettings.openImage.value }) { ZoneSettings.edit(context) { set(ZoneSettings.openImage, it) } }
        OnScreenResume { openImage.reload(ZoneSettings.openImage.value) }
        ChoiceRowWithDialog(
            title = stringResource(R.string.image) + " (" + stringResource(R.string.long_press) + ")",
            value = stringResource(MainConst.imageMenuNames[openImage.value]),
            options = MainConst.imageMenuValues.map { stringResource(MainConst.imageMenuNames[it]) },
            selected = MainConst.imageMenuValues.indexOf(openImage.value),
            onSelect = { openImage.set(MainConst.imageMenuValues[it]) },
        )
    }
}

/** Restores the defaults of [pageLinksSection]. */
fun resetPageLinks(context: Context) {
    if (ZtwoSettings.agentType.value != 0 || !TextUtils.isEmpty(ZtwoSettings.agentUser.value)) {
        ZtwoSettings.editLocal { set(ZtwoSettings.agentType, 0) }; ZtwoSettings.editLocal { set(ZtwoSettings.agentUser, "") }
        val p = PrefZtwo.get(context, false)
        p!!.removeKey("mAgentType"); p.removeKey("mAgentUser"); p.save()
    }
    if (AlbumSettings.linkApp.value) AlbumSettings.reset(context, listOf(AlbumSettings.linkApp))
    if (ZoneSettings.openLink.value != 0) ZoneSettings.reset(context, listOf(ZoneSettings.openLink))
    if (ZoneSettings.openImage.value != 0) ZoneSettings.reset(context, listOf(ZoneSettings.openImage))
}

// ---- Performance: pre-raster and web debugging (from Advanced) ---------------------------------------------------

fun SettingsListScope.performanceSection(version: Int) {
    item {
        val context = LocalContext.current
        val preRaster = rememberPrefAt(version, { ZoneSettings.preRaster.value }) { ZoneSettings.edit(context) { set(ZoneSettings.preRaster, it) } }
        SettingSwitchRow(
            title = stringResource(R.string.pre_raster),
            summary = stringResource(R.string.smooth_scroll_sum),
            checked = preRaster.value,
            onCheckedChange = preRaster::set,
        )
    }
}

fun resetPerformance(context: Context) {
    if (ZoneSettings.preRaster.value) ZoneSettings.reset(context, listOf(ZoneSettings.preRaster))
}

/** Web debugging (moved from Performance & memory to Web pages, 2026-11-21: it is a developer option). */
fun SettingsListScope.developerSection(version: Int, header: String) {
    settingsHeader(header)
    item {
        val context = LocalContext.current
        val debug = rememberPrefAt(version, { PdfSettings.debugMode.value }) {
            PdfSettings.edit(context) { set(PdfSettings.debugMode, it) }
            WebView.setWebContentsDebuggingEnabled(it)
        }
        SettingSwitchRow(
            title = stringResource(R.string.debug_mode),
            summary = stringResource(R.string.debug_mode_info),
            checked = debug.value,
            onCheckedChange = debug::set,
        )
    }
}

fun resetDeveloper(context: Context) {
    if (PdfSettings.debugMode.value) {
        PdfSettings.reset(context, listOf(PdfSettings.debugMode))
        WebView.setWebContentsDebuggingEnabled(PdfSettings.debugMode.value)
    }
}

/**
 * A limit as "Unlimited / Custom" segments plus a slider for the custom value. Values below [minOn] mean unlimited.
 * Shared by Performance & memory and Tabs (the open limit).
 */
internal fun SettingsListScope.limitRows(
    titleRes: Int,
    state: com.mycompany.app.ui.prefs.PrefState<Int>,
    range: IntRange,
    firstValue: Int,
    minOn: Int = 3,
    label: (Int) -> String = { it.toString() },
) {
    item {
        SettingSegmentedRow(
            title = stringResource(titleRes),
            options = listOf(stringResource(R.string.history_none), stringResource(R.string.user_defined)),
            selectedIndex = if (state.value < minOn) 0 else 1,
            onSelect = { state.set(if (it == 0) 0 else firstValue) },
        )
    }
    if (state.value >= minOn) {
        item {
            SettingSliderRow(title = stringResource(R.string.hd_count), value = state.value, range = range, label = label, onValueChange = state::set)
        }
    }
}

// ---- Appearance: text size and font (from Web content) -----------------------------------------------------------

private fun fontName(context: Context): String {
    if (!ZoneSettings.userFont.value) return context.getString(R.string.font_default)
    val path = ZoneSettings.fontPath.value
    val name = if (!TextUtils.isEmpty(path) && path!!.startsWith("app://")) {
        val comma = path.lastIndexOf(',')
        if (comma > 0 && comma + 1 < path.length) path.substring(comma + 1) else null
    } else {
        MainUtil.uriFileName(context, path)
    }
    return if (TextUtils.isEmpty(name)) context.getString(R.string.no_title) else name!!
}

fun SettingsListScope.textFontSection(version: Int, texts: (Int) -> String) {
    item {
        var tick by remember { mutableIntStateOf(0) }
        var textZoom by remember { mutableStateOf(false) }
        OnScreenResume { tick++ }
        fun textSize(): String {
            val sb = StringBuilder().append(ZoneSettings.textSize.value).append("%")
            if (ZtriSettings.zoomIcon.value) sb.append(" (").append(ZtriSettings.zoomSize.value).append("%)")
            return sb.toString()
        }
        SettingChoiceRow(title = stringResource(R.string.text_size), value = "$tick$version".let { textSize() }, onClick = { textZoom = true })
        if (textZoom) TextZoomDialog(onDismiss = { textZoom = false; tick++ })
    }
    item {
        val context = LocalContext.current
        var tick by remember { mutableIntStateOf(0) }
        OnScreenResume { tick++ }
        SettingRow(
            title = stringResource(R.string.font),
            summary = "$tick$version".let { fontName(context) },
            onClick = { context.startActivity(Intent(context, FontSettingsActivity::class.java)) },
        )
    }
    settingsFooter(texts(R.string.font_info_1))
}

/** Restores the defaults of [textFontSection] (text size, the zoom button, the font). */
fun resetTextFont(context: Context) {
    val color = MainConst.gradientColors[5]
    val pos = MainConst.presetPositions[5]
    if (ZtriSettings.zoomIcon.value || ZtriSettings.zoomSize.value != 200) {
        ZtriSettings.editLocal { set(ZtriSettings.zoomIcon, false) }; ZtriSettings.editLocal { set(ZtriSettings.zoomSize, 200) }
        val p = PrefZtri.get(context)
        p!!.removeKey("mZoomIcon"); p.removeKey("mZoomSize"); p.save()
    }
    if (ZoneSettings.textSize.value != 100) ZoneSettings.reset(context, listOf(ZoneSettings.textSize))
    if (EditorSettings.zoomAlpha.value != 0 || EditorSettings.zoomColor.value != color || EditorSettings.zoomPos.value.compareTo(pos) != 0) {
        EditorSettings.editLocal { set(EditorSettings.zoomAlpha, 0) }; EditorSettings.editLocal { set(EditorSettings.zoomColor, color) }; EditorSettings.editLocal { set(EditorSettings.zoomPos, pos) }; PrefEditor.zoomArgb = PrefEditor.withAlpha(color, 0)
        val p = PrefEditor.get(context)
        p!!.removeKey("mZoomAlpha"); p.removeKey("mZoomColor"); p.removeKey("mZoomPos"); p.save()
    }
    if (ZoneSettings.userFont.value || ZoneSettings.fontBold.value || !TextUtils.isEmpty(ZoneSettings.fontPath.value)) {
        ZoneSettings.editLocal { set(ZoneSettings.userFont, false) }; ZoneSettings.editLocal { set(ZoneSettings.fontPath, "") }; ZoneSettings.editLocal { set(ZoneSettings.fontBold, false) }
        val p = PrefZone.get(context, false)
        p!!.removeKey("mUserFont"); p.removeKey("mFontPath"); p.removeKey("mFontBold"); p.save()
    }
}

// ---- Links to other screens -------------------------------------------------------------------------------------

/** A row that opens another settings screen. */
fun SettingsListScope.screenLinkRow(titleRes: Int, summary: String? = null, open: (Activity) -> Unit) {
    item {
        val activity = activityOf(LocalContext.current)
        SettingRow(title = stringResource(titleRes), summary = summary, onClick = { activity?.let(open) })
    }
}

/** The activity behind [context] (Compose may hand out a wrapped context). */
fun activityOf(context: Context): Activity? {
    var c: Context? = context
    while (c is android.content.ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}
