package com.mycompany.app.ui.settings

import android.app.Dialog
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.ZtwoSettings
import android.content.Context
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import android.text.TextUtils
import ch.opumlabs.opumbrowser.R
import androidx.compose.runtime.LaunchedEffect
import com.mycompany.app.data.DataNews
import com.mycompany.app.db.book.DbRecentLang
import com.mycompany.app.dialog.DialogEditText
import com.mycompany.app.dialog.DialogNewsLocale
import com.mycompany.app.dialog.DialogSeekSimple
import com.mycompany.app.dialog.DialogTransLang
import com.mycompany.app.dialog.DialogTransLang.TransLangListener
import com.mycompany.app.dialog.DialogTransLang.TransNotiListener
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainSelectAdapter.MainSelectListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.quick.QuickView
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingSliderRow
import com.mycompany.app.ui.components.SettingSegmentedRow
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.ChoiceDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingChoiceRow
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.prefs.OnScreenResume
import com.mycompany.app.ui.prefs.rememberPref

/**
 * News settings (one page for news and RSS since 2026-11-20): the News button on the start page, how articles open,
 * Top stories from Google News (on / off, language & region, headline translation), the feed list, and the news
 * search button. The custom news address is gone (it was a feed; it moves into the feed list), and so are the two
 * "recent languages" limits (the language lists keep their current limit). The locale list, translation language dialogs need the legacy activity type and go
 * through `DialogShow.direct`; the URL dialog and the number picker take a plain Activity.
 */
class NewsSettingsActivity : OpumComposeActivity() {
    private var useAtStart = false
    private var urlAtStart: String? = null
    private var langAtStart = 0

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        useAtStart = ZtwoSettings.newsUse.value
        urlAtStart = ZtwoSettings.newsUrl.value
        langAtStart = ZtwoSettings.newsLang.value
        super.onCreate(savedInstanceState)
    }

    override fun finish() {
        if (useAtStart != ZtwoSettings.newsUse.value || langAtStart != ZtwoSettings.newsLang.value || !MainUtil.sameObject(urlAtStart, ZtwoSettings.newsUrl.value)) {
            // changed: drop the cached news pick and the news data when the feature is now off
            if (ZtwoSettings.newsTopic.value == 9) {
                ZtwoSettings.editLocal { set(ZtwoSettings.newsTopic, 0) }
                ZtwoSettings.editLocal { set(ZtwoSettings.newsSearch, "") }
                PrefZtwo.saveNews(this)
            }
            if (!ZtwoSettings.newsUse.value) DataNews.instance(this).setItems(null)
            useAtStart = ZtwoSettings.newsUse.value; langAtStart = ZtwoSettings.newsLang.value; urlAtStart = ZtwoSettings.newsUrl.value
        }
        super.finish()
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val langNames = remember { resources.getStringArray(R.array.news_lang) }
        remember { com.mycompany.app.quick.RssFeeds.migrateCustomNews(context); true }
        val use = rememberPref({ ZtwoSettings.newsUse.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.newsUse, it) } }
        val homeButton = rememberPref({ ZtwoSettings.newsButton.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.newsButton, it) } }
        val openWith = rememberPref({ ZtwoSettings.newsOpen.value }) { ZtwoSettings.edit(context) { set(ZtwoSettings.newsOpen, it) } }
        val icon = rememberPref({ ZtriSettings.newsIcon.value }) { ZtriSettings.edit(context) { set(ZtriSettings.newsIcon, it) } }
        var tick by remember { mutableIntStateOf(0) } // values changed by dialogs / read from the legacy statics
        val color = rememberPref({ EditorSettings.newsColor.value }) { c ->
            val at = MainConst.gradientColors.indexOf(c)
            EditorSettings.edit(context) {
                set(EditorSettings.newsColor, c)
                if (at >= 0) set(EditorSettings.newsPos, MainConst.presetPositions[at])
            }
            PrefEditor.newsArgb = PrefEditor.withAlpha(EditorSettings.newsColor.value, EditorSettings.newsAlpha.value)
        }
        val alpha = rememberPref({ EditorSettings.newsAlpha.value.coerceIn(0, 90) }) {
            EditorSettings.edit(context) { set(EditorSettings.newsAlpha, it) }
            PrefEditor.newsArgb = PrefEditor.withAlpha(EditorSettings.newsColor.value, EditorSettings.newsAlpha.value)
        }
        var liveAlpha by remember { mutableIntStateOf(alpha.value) }
        var choosing by remember { mutableIntStateOf(0) } // 2 url, 3 open with, 6 local recent, 8 title language, 9 title recent
        var resetting by remember { mutableStateOf(false) }

        OnScreenResume { tick++ }

        SettingsScaffold(
            title = stringResource(R.string.news_title),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            // News & RSS are one page since 2026-11-20 (Top stories from Google News, My feeds, each feed)
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.news_home_button),
                    checked = homeButton.value,
                    onCheckedChange = homeButton::set,
                )
            }
            item {
                SettingChoiceRow(
                    title = stringResource(R.string.news_open_in),
                    value = stringResource(openLabels[openWith.value]),
                    onClick = { choosing = 3 },
                )
            }
            settingsHeader(getString(R.string.news_top))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.news_google_top),
                    summary = stringResource(R.string.news_google_info),
                    checked = use.value,
                    onCheckedChange = use::set,
                )
            }
            if (use.value) {
                item {
                    SettingRow(
                        title = stringResource(R.string.news_locale),
                        summary = "$tick".let { (langNames.getOrNull(ZtwoSettings.newsLang.value) ?: "") + "\n" + getString(R.string.not_support_locale) },
                        onClick = {
                            com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host ->
                                var dialog: DialogNewsLocale? = null
                                dialog = DialogNewsLocale(host, langNames.map { it }.toTypedArray(), object : MainSelectListener {
                                    override fun onPick(id: Int) {
                                        dialog?.dismiss()
                                        if (ZtwoSettings.newsLang.value != id) { ZtwoSettings.edit(context) { set(ZtwoSettings.newsLang, id) } }
                                        tick++
                                    }
                                })
                                dialog.transListener = object : TransNotiListener { override fun onDone() { dialog.dismiss() } }
                                dialog
                            }
                        },
                    )
                }
                item {
                    SettingChoiceRow(
                        title = stringResource(R.string.news_trans),
                        value = "$tick".let { if (ZtwoSettings.newsTitle.value) ZtwoSettings.newsPick.value ?: "" else getString(R.string.not_used) },
                        onClick = { choosing = 8 },
                    )
                }
            }
            settingsHeader(getString(R.string.news_my_feeds))
            item {
                SettingRow(
                    title = stringResource(R.string.rss_feeds),
                    summary = "$tick".let { com.mycompany.app.quick.RssFeeds.feeds().size.let { resources.getQuantityString(R.plurals.news_feeds_count, it, it) } },
                    onClick = { com.mycompany.app.quick.RssFeedsDialog(context) { tick++ } },
                )
            }
            settingsHeader(getString(R.string.news_search_button))
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.search_icon),
                    checked = icon.value,
                    onCheckedChange = icon::set,
                )
            }
            item {
                ButtonColorCircles(
                    title = stringResource(R.string.icon_color), enabled = icon.value, reason = stringResource(R.string.btn_off_reason),
                    selected = color.value, transparency = liveAlpha, onPick = color::set,
                )
            }
            item {
                SettingSliderRow(
                    title = stringResource(R.string.color_alpha), value = alpha.value, range = 0..90, label = { "$it%" },
                    onValueChange = { alpha.set(it); liveAlpha = it }, enabled = icon.value,
                    below = { v -> LaunchedEffect(v) { liveAlpha = v } },
                )
            }
            settingsFooter(getString(R.string.drag_move_guide))
        }

        when (choosing) {
            3 -> ChoiceDialog(
                title = stringResource(R.string.open_with),
                options = openLabels.map { stringResource(it) },
                selectedIndex = openWith.value,
                onSelect = { openWith.set(it) },
                onDismiss = { choosing = 0 },
            )
            8 -> ChoiceDialog(
                title = stringResource(R.string.news_trans),
                options = listOf(stringResource(R.string.locale), stringResource(R.string.not_used)),
                selectedIndex = if (ZtwoSettings.newsTitle.value) 0 else 1,
                onSelect = { index ->
                    if (index == 0) {
                        com.mycompany.app.ui.prefs.DialogShow.direct(context as android.app.Activity) { host ->
                            var dialog: DialogTransLang? = null
                            dialog = DialogTransLang(host, true, object : TransLangListener {
                                override fun onLanguage(lang: String?) { dialog?.dismiss() }
                            })
                            dialog.transListener = object : TransNotiListener { override fun onDone() { dialog.dismiss() } }
                            dialog
                        }
                    } else if (ZtwoSettings.newsTitle.value || !TextUtils.isEmpty(ZtwoSettings.newsPick.value)) {
                        ZtwoSettings.editLocal { set(ZtwoSettings.newsTitle, false) }
                        ZtwoSettings.editLocal { set(ZtwoSettings.newsPick, "") }
                        val prefs = PrefZtwo.get(context, false)
                        if (prefs != null) {
                            prefs.putBool("mNewsTitle", ZtwoSettings.newsTitle.value)
                            prefs.putString("mNewsPick", ZtwoSettings.newsPick.value)
                            prefs.saveAsync()
                        }
                        tick++
                    }
                },
                onDismiss = { choosing = 0 },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    use.reload(ZtwoSettings.newsUse.value); homeButton.reload(ZtwoSettings.newsButton.value); openWith.reload(ZtwoSettings.newsOpen.value); icon.reload(ZtriSettings.newsIcon.value)
                    color.reload(EditorSettings.newsColor.value); alpha.reload(EditorSettings.newsAlpha.value); liveAlpha = alpha.value
                    tick++
                },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Labels of "open with" (index = value). Used by the settings search index. */
        @JvmField
        val openLabels: IntArray = intArrayOf(
            R.string.reader_mode, R.string.preview, R.string.open_url, R.string.new_url,
            R.string.group_url, R.string.back_url, R.string.secret_tab,
        )

        /** Restores the defaults of this page. */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            val color = MainConst.gradientColors[5]
            val pos = MainConst.presetPositions[5]
            if (ZtwoSettings.newsUse.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.newsUse))
            if (!ZtwoSettings.newsButton.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.newsButton))
            if (!TextUtils.isEmpty(ZtwoSettings.newsUrl.value)) ZtwoSettings.reset(context, listOf(ZtwoSettings.newsUrl))
            if (ZtwoSettings.recentLocal.value != 5) ZtwoSettings.reset(context, listOf(ZtwoSettings.recentLocal))
            if (ZtwoSettings.recentTitle.value != 5) ZtwoSettings.reset(context, listOf(ZtwoSettings.recentTitle))
            if (!ZtwoSettings.newsTitle.value) ZtwoSettings.reset(context, listOf(ZtwoSettings.newsTitle))
            if (ZtwoSettings.newsOpen.value != 0) ZtwoSettings.reset(context, listOf(ZtwoSettings.newsOpen))
            if (!ZtriSettings.newsIcon.value) ZtriSettings.reset(context, listOf(ZtriSettings.newsIcon))
            if (EditorSettings.newsAlpha.value != MainConst.BUTTON_ALPHA || EditorSettings.newsColor.value != color || EditorSettings.newsPos.value.compareTo(pos) != 0) {
                EditorSettings.editLocal { set(EditorSettings.newsAlpha, MainConst.BUTTON_ALPHA) }; EditorSettings.editLocal { set(EditorSettings.newsColor, color) }; EditorSettings.editLocal { set(EditorSettings.newsPos, pos) }; PrefEditor.newsArgb = PrefEditor.withAlpha(color, MainConst.BUTTON_ALPHA)
                val p = PrefEditor.get(context)
                p!!.removeKey("mNewsAlpha"); p.removeKey("mNewsColor"); p.removeKey("mNewsPos"); p.save()
            }
        }
    }
}
