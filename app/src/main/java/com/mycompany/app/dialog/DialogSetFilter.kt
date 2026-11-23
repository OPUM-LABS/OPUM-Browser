package com.mycompany.app.dialog

import android.content.Intent

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import java.util.ArrayList
import java.util.HashMap
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.text.TextUtils
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.dialog.DialogConfirm.DialogConfListener
import com.mycompany.app.dialog.DialogWebView.DialogWebListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.filterFilePath
import com.mycompany.app.main.MainUtil.jsDelivrToGithub
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.browserIntent
import com.mycompany.app.main.MainUtil.clipRound3
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.moveFileOrUri
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.web.WebNestView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.pref.PrefSet

/**
 * Add ad-filter lists from a catalog ([adguardModeParam]: AdGuard, 18 lists; else Adblock Plus, 44 lists): header with the logo,
 * "n / total" and a select-all box; the lists under Basic / Functional / Locale with a checkbox each (locale lists
 * show their languages) and a docs button (opens the list's home page in [DialogWebView]); Add saves the selection
 * (`AlbumSettings.adguardAdd`) and hands the lists not yet added to [SetFilterListener]. First use shows the old license note
 * ([DialogConfirm], `ReadSettings.guideLicense.value`). The catalogs and helpers in the companion are unchanged. Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogSetFilter(
    mainActivity: android.app.Activity?,
    /** 0 = Adblock Plus, 1 = AdGuard, 2 = uBlock Origin ([CATALOG_UBO]), 3 = Brave ([CATALOG_BRAVE]). */
    catalog: Int,
    setFilterListener: SetFilterListener?,
) : ComposeBottomDialog(mainActivity) {
    /** Old signature: Adblock Plus or AdGuard catalog. */
    constructor(mainActivity: android.app.Activity?, adguardModeParam: Boolean, setFilterListener: SetFilterListener?) :
        this(mainActivity, if (adguardModeParam) 1 else 0, setFilterListener)

    @JvmField
    var catalogMode: Int = catalog
    @JvmField
    var hostActivity: android.app.Activity? = mainActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var filterListener: SetFilterListener? = setFilterListener
    @JvmField
    var adguardMode: Boolean = catalog == 1
    @JvmField
    var selectedFlags: BooleanArray? = null
    @JvmField
    var confirmDialog: DialogConfirm? = null
    @JvmField
    var docsDialog: DialogWebView? = null

    private val checked = mutableStateListOf<Boolean>()
    private var version by mutableIntStateOf(0)

    interface SetFilterListener {
        fun onAdded(arrayList: ArrayList<Any?>?)
    }

    init {
        val adguard = adguardMode
        val newList = newCatalog(catalogMode)
        val ubo = newList != null
        val count = if (newList != null) newList.size else if (adguard) 18 else 44
        var saved: ArrayList<Int>? = null
        val pref = savedSelection()
        if (!TextUtils.isEmpty(pref)) {
            val parts = pref!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
            if (parts.isEmpty()) {
                saveSelection(null, local = true)
            } else {
                for (part in parts) {
                    val index = MainUtil.parseColorSafe(part)
                    if (index != -1) {
                        if (saved == null) saved = ArrayList()
                        saved.add(index)
                    }
                }
            }
        }
        val selected = BooleanArray(count) { saved != null && saved.contains(it) }
        selectedFlags = selected
        selected.forEach { checked.add(it) }
        // section starts in the list (old SettingItem layout): Basic, Functional, Locale
        val functionalAt = if (ubo) (if (catalogMode == CATALOG_BRAVE) BRAVE_FUNCTIONAL_AT else UBO_FUNCTIONAL_AT) else if (adguard) 2 else 1
        val localeAt = if (ubo) count else if (adguard) 9 else 10
        setSheet {
            val n = checked.count { it }
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painterResource(if (adguard) AppR.drawable.ic_adguard else AppR.drawable.ic_adblock),
                    contentDescription = null, modifier = Modifier.size(32.dp),
                )
                Text(
                    if (catalogMode == CATALOG_BRAVE) "Brave" else if (ubo) "uBlock Origin" else if (adguard) "AdGuard" else "Adblock Plus", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                )
                Text(MainUtil.countLabel(n, count), style = MaterialTheme.typography.bodyLarge)
                Checkbox(checked = n == count, onCheckedChange = { setAll(it) })
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                for (k in 0 until count) {
                    when (k) {
                        0 -> Section(stringResource(AppR.string.basic))
                        functionalAt -> Section(stringResource(AppR.string.functional))
                        localeAt -> Section(stringResource(AppR.string.locale))
                    }
                    val name = if (newList != null) newList[k][0] else if (adguard) adguardFilters[k]!![0] else filters[k]!![0]
                    val info = if (newList != null) newList[k][3] else if (adguard) null else filters[k]!![3]
                    val on = checked.getOrElse(k) { false }
                    ListItem(
                        headlineContent = { Text(name ?: "") },
                        supportingContent = if (info != null) ({ Text(info) }) else null,
                        leadingContent = { Checkbox(checked = on, onCheckedChange = { toggle(k, it) }) },
                        trailingContent = {
                            IconButton(onClick = { openDocs(k) }) {
                                Icon(painterResource(AppR.drawable.outline_info_black_24), contentDescription = null)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable { toggle(k, !on) },
                    )
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { add() }, enabled = n > 0) { Text(stringResource(AppR.string.add)) }
            }
        }
        show()
        if (ReadSettings.guideLicense.value) android.os.Handler(android.os.Looper.getMainLooper()).post { showLicense() }
    }

    @androidx.compose.runtime.Composable
    private fun Section(title: String) {
        Text(
            title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 4.dp),
        )
    }

    private fun toggle(index: Int, on: Boolean) {
        val arr = selectedFlags ?: return
        if (index < arr.size) arr[index] = on
        if (index < checked.size) checked[index] = on
    }

    private fun setAll(on: Boolean) {
        val arr = selectedFlags ?: return
        for (k in arr.indices) {
            arr[k] = on
            checked[k] = on
        }
    }

    /** Docs for list [i]: the AdGuard filters page, or the list's home page. */
    private fun openDocs(action: Int) {
        val act = hostActivity ?: return
        if (confirmDialog != null || docsDialog != null) return
        var url: String?
        val newList = newCatalog(catalogMode)
        if (newList != null) {
            if (action < 0 || action >= newList.size) return
            url = newList[action][2]
        } else if (adguardMode) {
            if (action < 0 || action >= 18) return
            url = "https://kb.adguard.com/en/general/adguard-ad-filters"
        } else {
            if (action < 0 || action >= 44) return
            url = filters[action]!![2]
            if ("abp" == url) url = "https://adblockplus.org/en/subscriptions"
        }
        val web = DialogWebView(act, url, url, false, 2, object : DialogWebView.DialogWebListener {
            override fun onAction(action: Int, url: String?, label: String?) {
                val activity = hostActivity ?: return
                val intent = MainUtil.browserIntent(appContext)
                intent.putExtra("EXTRA_PATH", url)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                activity.startActivity(intent)
            }
            override fun onClosed() {}
            override fun onDownload(url: String?, userAgent: String?, mimeType: String?, size: Long) {}
            override fun onNewWindow(webNestView: com.mycompany.app.web.WebNestView?, url: String?) {}
            override fun onShown() {}
            override fun onHidden() {}
        })
        docsDialog = web
        web.setOnDismissListener(DialogInterface.OnDismissListener {
            docsDialog?.dismiss()
            docsDialog = null
        })
    }

    private fun showLicense() {
        val act = hostActivity ?: return
        if (confirmDialog != null || docsDialog != null) return
        val ctx = appContext ?: return
        val message = ctx.getString(AppR.string.filter_link_1) + "\n" + ctx.getString(AppR.string.filter_link_2) + "\n" +
            ctx.getString(AppR.string.filter_link_3) + "\n\n" + ctx.getString(AppR.string.filter_link_4) + "\n" +
            ctx.getString(AppR.string.filter_link_5)
        val confirm = DialogConfirm(act as Activity?, message, true, object : DialogConfirm.DialogConfListener {
            override fun onResult(confirmed: Boolean) {
                if (confirmed) {
                    ReadSettings.edit(appContext) { set(ReadSettings.guideLicense, false) }
                }
                confirmDialog?.dismiss()
                confirmDialog = null
            }
        })
        confirmDialog = confirm
        confirm.setOnDismissListener(DialogInterface.OnDismissListener {
            confirmDialog?.dismiss()
            confirmDialog = null
        })
    }

    /** Add: saves the selection and hands the lists that are not added yet to the listener. */
    private fun add() {
        val adguard = adguardMode
        var selection: String? = null
        val arr = selectedFlags
        if (arr != null && arr.isNotEmpty()) {
            val sb = StringBuilder()
            for (k in arr.indices) {
                if (arr[k]) {
                    if (sb.isNotEmpty()) sb.append("/")
                    sb.append(k)
                }
            }
            if (sb.isNotEmpty()) selection = sb.toString()
        }
        if (!MainUtil.sameObject(savedSelection(), selection)) saveSelection(selection, local = false)
        val listener = filterListener ?: return
        var added: ArrayList<Any?>? = null
        val ctx = appContext
        if (ctx != null) {
            val existing = DataBookFilter.instance(ctx).items
            val pref = savedSelection()
            if (!TextUtils.isEmpty(pref)) {
                for (part in pref!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }) {
                    val index = MainUtil.parseColorSafe(part)
                    val url: String?
                    val name: String?
                    val newList = newCatalog(catalogMode)
                    if (newList != null) {
                        if (index < 0 || index >= newList.size) continue
                        url = newList[index][1]
                        name = newList[index][0]
                    } else if (adguard) {
                        url = adguardUrl(index)
                        if (TextUtils.isEmpty(url)) continue
                        name = adguardFilters[index]!![0]
                    } else {
                        if (index < 0 || index >= 44) continue
                        url = filters[index]!![1]
                        name = filters[index]!![0]
                    }
                    if (added == null) added = ArrayList()
                    val known = existing?.any { (it)?.path == url } == true
                    if (!known) {
                        val item = ChildItem()
                        item.path = url
                        item.name = name
                        added.add(item)
                    }
                }
            }
        }
        listener.onAdded(added)
    }

    /** The catalog's saved selection ("/"-separated indexes). */
    private fun savedSelection(): String? = when (catalogMode) {
        CATALOG_UBO -> AlbumSettings.uboAdd.value
        CATALOG_BRAVE -> AlbumSettings.braveAdd.value
        1 -> AlbumSettings.adguardAdd.value
        else -> AlbumSettings.filterAdd.value
    }

    private fun saveSelection(selection: String?, local: Boolean) {
        val key = when (catalogMode) {
            CATALOG_UBO -> AlbumSettings.uboAdd
            CATALOG_BRAVE -> AlbumSettings.braveAdd
            1 -> AlbumSettings.adguardAdd
            else -> AlbumSettings.filterAdd
        }
        if (local) AlbumSettings.editLocal { set(key, selection ?: "") } else AlbumSettings.edit(appContext) { set(key, selection ?: "") }
    }

    override fun dismiss() {
        confirmDialog?.dismiss()
        confirmDialog = null
        docsDialog?.dismiss()
        docsDialog = null
        hostActivity = null
        filterListener = null
        super.dismiss()
    }

    companion object {
        const val CATALOG_UBO = 2
        const val CATALOG_BRAVE = 3

        /** The list of a catalog in the new format (name, URL, home page, note), or null for the old catalogs. */
        @JvmStatic
        fun newCatalog(mode: Int): Array<Array<String?>>? = when (mode) {
            CATALOG_UBO -> uboFilters
            CATALOG_BRAVE -> braveFilters
            else -> null
        }

        /**
         * Brave's own lists (brave/adblock-lists, from Brave's list_catalog.json) — they complement uBlock Origin's and
         * EasyList's lists, which Brave also uses — plus AdGuard's "Tracking URL blocker", the list Brave offers for
         * removing tracking parameters from links (`$removeparam`). Basic = Brave's defaults; from
         * [BRAVE_FUNCTIONAL_AT] the optional ones. Brave's lists may use trusted scriptlets and Brave's own scriptlets
         * (`assets/filter/brave-resources.json`).
         */
        @JvmField
        val braveFilters: Array<Array<String?>> = arrayOf(
            arrayOf("Brave Specific", "$BRAVE_LISTS/brave-lists/brave-specific.txt", BRAVE_HOME, null),
            arrayOf("Brave Android Specific", "$BRAVE_LISTS/brave-lists/brave-android-specific.txt", BRAVE_HOME, null),
            arrayOf("Brave Unbreak", "$BRAVE_LISTS/brave-unbreak.txt", BRAVE_HOME, null),
            arrayOf("Brave Social", "$BRAVE_LISTS/brave-lists/brave-social.txt", BRAVE_HOME, null),
            arrayOf("Brave First Party", "$BRAVE_LISTS/brave-lists/brave-firstparty.txt", BRAVE_HOME, null),
            arrayOf("Brave First Party (regional)", "$BRAVE_LISTS/brave-lists/brave-firstparty-regional.txt", BRAVE_HOME, null),
            arrayOf("Brave SugarCoat", "$BRAVE_LISTS/brave-lists/brave-sugarcoat.txt", BRAVE_HOME, null),
            arrayOf(
                "Tracking URL blocker (AdGuard URL Tracking)",
                "https://raw.githubusercontent.com/AdguardTeam/FiltersRegistry/master/filters/filter_17_TrackParam/filter.txt",
                "https://adguard.com/kb/general/ad-filtering/adguard-filters/", null,
            ),
            arrayOf("Brave Cookie Specific", "$BRAVE_LISTS/brave-lists/brave-cookie-specific.txt", BRAVE_HOME, null),
            arrayOf("Mobile app promo blocker", "https://secure.fanboy.co.nz/fanboy-mobile-notifications.txt", "https://easylist.to", null),
            arrayOf("Newsletter popup blocker", "https://secure.fanboy.co.nz/fanboy-newsletter.txt", "https://easylist.to", null),
            arrayOf("YouTube Shorts blocker", "$BRAVE_LISTS/brave-lists/yt-shorts.txt", BRAVE_HOME, null),
            arrayOf("YouTube recommendations blocker", "$BRAVE_LISTS/brave-lists/yt-recommended.txt", BRAVE_HOME, null),
            arrayOf("YouTube distractions blocker", "$BRAVE_LISTS/brave-lists/yt-distracting.txt", BRAVE_HOME, null),
            arrayOf("Allow Facebook embeds", "$BRAVE_LISTS/brave-lists/embed-exceptions-facebook.txt", BRAVE_HOME, null),
            arrayOf("Allow X/Twitter embeds", "$BRAVE_LISTS/brave-lists/embed-exceptions-twitter.txt", BRAVE_HOME, null),
            arrayOf("Allow LinkedIn embeds", "$BRAVE_LISTS/brave-lists/embed-exceptions-linkedin.txt", BRAVE_HOME, null),
        )

        /** First optional entry of [braveFilters]. */
        const val BRAVE_FUNCTIONAL_AT = 8

        private const val BRAVE_LISTS = "https://raw.githubusercontent.com/brave/adblock-lists/master"
        private const val BRAVE_HOME = "https://github.com/brave/adblock-lists"

        /**
         * uBlock Origin's catalog (name, URL, home page, note): its default lists first (Basic), then the optional
         * ones from index [UBO_FUNCTIONAL_AT]. Regional lists come from the Adblock Plus / AdGuard catalogs.
         * EasyList / EasyPrivacy use the same addresses as the Adblock Plus catalog, so they are not added twice.
         * uBO's own lists (uAssets) may use trusted scriptlets (`UboFilter.isTrustedListUrl`).
         */
        @JvmField
        val uboFilters: Array<Array<String?>> = arrayOf(
            arrayOf("uBlock filters – Ads", "$UASSETS/filters.txt", UASSETS_HOME, null),
            arrayOf("uBlock filters – Badware risks", "$UASSETS/badware.txt", UASSETS_HOME, null),
            arrayOf("uBlock filters – Privacy", "$UASSETS/privacy.txt", UASSETS_HOME, null),
            arrayOf("uBlock filters – Quick fixes", "$UASSETS/quick-fixes.txt", UASSETS_HOME, null),
            arrayOf("uBlock filters – Unbreak", "$UASSETS/unbreak.txt", UASSETS_HOME, null),
            arrayOf("EasyList", "https://easylist-downloads.adblockplus.org/easylist.txt", "https://easylist.to", null),
            arrayOf("EasyPrivacy", "https://easylist-downloads.adblockplus.org/easyprivacy.txt", "https://easylist.to", null),
            arrayOf(
                "Peter Lowe's Ad and tracking server list",
                "https://pgl.yoyo.org/adservers/serverlist.php?hostformat=adblockplus&showintro=0&mimetype=plaintext",
                "https://pgl.yoyo.org/adservers", null,
            ),
            arrayOf(
                "Online Malicious URL Blocklist",
                "https://malware-filter.gitlab.io/malware-filter/urlhaus-filter-ag-online.txt",
                "https://gitlab.com/malware-filter/urlhaus-filter", null,
            ),
            arrayOf("uBlock filters – Cookie notices", "$UASSETS/annoyances-cookies.txt", UASSETS_HOME, null),
            arrayOf("uBlock filters – Other annoyances", "$UASSETS/annoyances-others.txt", UASSETS_HOME, null),
            arrayOf("uBlock filters – Resource abuse", "$UASSETS/resource-abuse.txt", UASSETS_HOME, null),
            arrayOf("uBlock filters – Block outsider intrusion into LAN", "$UASSETS/lan-block.txt", UASSETS_HOME, null),
            arrayOf("EasyList/uBO – Cookie Notices", "https://secure.fanboy.co.nz/fanboy-cookiemonster_ubo.txt", "https://easylist.to", null),
            arrayOf("EasyList/uBO – Annoyances", "https://secure.fanboy.co.nz/fanboy-annoyance_ubo.txt", "https://easylist.to", null),
        )

        /** First optional entry of [uboFilters]; the ones before it are uBlock Origin's defaults. */
        const val UBO_FUNCTIONAL_AT = 9

        /** The catalog indexes of uBlock Origin's default lists, as stored in `AlbumSettings.uboAdd`. */
        @JvmStatic
        fun uboDefaultSelection(): String = (0 until UBO_FUNCTIONAL_AT).joinToString("/")

        /** Adds uBlock Origin's default lists that are not in the filter list yet (enabled; downloaded later). */
        @JvmStatic
        fun addUboDefaults(context: Context?) {
            for (i in 0 until UBO_FUNCTIONAL_AT) {
                com.mycompany.app.db.book.DbBookFilter.addFilter(context, uboFilters[i][1], uboFilters[i][0])
            }
        }

        /** uBlock Origin's default lists that are missing from [paths] (name, URL). */
        @JvmStatic
        fun missingUboDefaults(paths: kotlin.collections.Collection<String?>): kotlin.collections.List<Array<String?>> =
            (0 until UBO_FUNCTIONAL_AT).map { uboFilters[it] }.filter { it[1] !in paths }

        /**
         * The lists behind the "Block cookie banners" switch (Web Filter settings), as Brave's "Block cookie consent
         * notices" does it: uBO's and EasyList's cookie-notice lists plus Brave's (name, URL).
         */
        @JvmStatic
        fun cookieLists(): kotlin.collections.List<Array<String?>> = listOf(
            uboFilters[UBO_FUNCTIONAL_AT], // uBlock filters – Cookie notices
            uboFilters[13], // EasyList/uBO – Cookie Notices
            braveFilters[BRAVE_FUNCTIONAL_AT], // Brave Cookie Specific
        )

        /** Other addresses of cookie-notice lists (the Adblock Plus catalog's EasyList Cookie List): turned off too. */
        private val COOKIE_OTHER = listOf(
            "https://easylist-downloads.adblockplus.org/fanboy-cookiemonster.txt",
            "https://secure.fanboy.co.nz/fanboy-cookiemonster.txt",
        )

        /** True when every list of [cookieLists] is in the filter list and enabled. */
        @JvmStatic
        fun cookieBlockOn(context: Context?): Boolean {
            if (context == null) return false
            val used = HashSet<String?>()
            var cursor: android.database.Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookFilter.get(context).writableDatabase, "DbBookFilter_table",
                    arrayOf<String?>("_path"), "_use=?", arrayOf<String?>("1"), null,
                )
                if (cursor != null && cursor.moveToFirst()) {
                    do used.add(cursor.getString(0)) while (cursor.moveToNext())
                }
            } catch (_: Exception) {
            }
            cursor?.close()
            return cookieLists().all { it[1] in used }
        }

        /**
         * Switches the cookie-notice lists: off disables every cookie list in the filter list (rows and files kept, so
         * switching on again is instant); on enables the ones already there and returns the ones still to download
         * (as list items for `DialogUpdateFilter`). Runs database work; call off the main thread.
         */
        @JvmStatic
        fun setCookieBlock(context: Context?, on: Boolean): ArrayList<Any?> {
            val missing = ArrayList<Any?>()
            if (context == null) return missing
            val db = DbBookFilter.get(context).writableDatabase
            val paths = cookieLists().map { it[1] } + if (on) emptyList() else COOKIE_OTHER
            val values = ContentValues()
            values.put("_use", if (on) 1 else 0)
            for (path in paths) {
                DbUtil.updateWhere(db, "DbBookFilter_table", values, "_path=?", arrayOf(path))
            }
            if (on) {
                for (list in cookieLists()) {
                    if (DbUtil.existsWhere(db, "DbBookFilter_table", null, "_path=?", arrayOf(list[1])) != 1) {
                        missing.add(ChildItem().also { it.path = list[1]; it.name = list[0] })
                    }
                }
            }
            DataBookFilter.instance(context).clear()
            return missing
        }

        private const val UASSETS = "https://raw.githubusercontent.com/uBlockOrigin/uAssets/master/filters"
        private const val UASSETS_HOME = "https://github.com/uBlockOrigin/uAssets"

        @JvmField
        val filters: Array<Array<String?>?> = arrayOf<Array<String?>?>(
            arrayOf<String?>(
                "EasyList",
                "https://easylist-downloads.adblockplus.org/easylist.txt",
                "abp",
                null
            ),
            arrayOf<String?>(
                "EasyPrivacy",
                "https://easylist-downloads.adblockplus.org/easyprivacy.txt",
                "abp",
                null
            ),
            arrayOf<String?>(
                "EasyList Cookie List",
                "https://easylist-downloads.adblockplus.org/fanboy-cookiemonster.txt",
                "abp",
                null
            ),
            arrayOf<String?>(
                "Fanboy's Annoyance List",
                "https://easylist-downloads.adblockplus.org/fanboy-annoyance.txt",
                "abp",
                null
            ),
            arrayOf<String?>(
                "Fanboy's Notifications Blocking List",
                "https://easylist-downloads.adblockplus.org/fanboy-notifications.txt",
                "abp",
                null
            ),
            arrayOf<String?>(
                "Fanboy's Social Blocking List",
                "https://easylist-downloads.adblockplus.org/fanboy-social.txt",
                "abp",
                null
            ),
            arrayOf<String?>(
                "ABP filters",
                "https://easylist-downloads.adblockplus.org/abp-filters-anti-cv.txt",
                "https://github.com/abp-filters/abp-filters-anti-cv",
                null
            ),
            arrayOf<String?>(
                "CPBL Filters for Adblock Plus",
                "https://raw.githubusercontent.com/bongochong/CombinedPrivacyBlockLists/master/cpbl-abp-list.txt",
                "https://git.io/JTTLB",
                null
            ),
            arrayOf<String?>(
                "NoCoin",
                "https://raw.githubusercontent.com/hoshsadiq/adblock-nocoin-list/master/nocoin.txt",
                "https://github.com/hoshsadiq/adblock-nocoin-list",
                null
            ),
            arrayOf<String?>(
                "Spam404",
                "https://raw.githubusercontent.com/Spam404/lists/master/adblock-list.txt",
                "https://www.spam404.com",
                null
            ),
            arrayOf<String?>(
                "ABPindo",
                "https://easylist-downloads.adblockplus.org/abpindo.txt",
                "https://github.com/ABPindo/indonesianadblockrules",
                "Bahasa Indonesia, Melayu"
            ),
            arrayOf<String?>(
                "EasyList Czech and Slovak",
                "https://raw.github.com/tomasko126/easylistczechandslovak/master/filters.txt",
                "https://adblock.sk",
                "Čeština, Slovenčina"
            ),
            arrayOf<String?>(
                "EasyList Germany",
                "https://easylist-downloads.adblockplus.org/easylistgermany.txt",
                "abp",
                "Deutsch"
            ),
            arrayOf<String?>(
                "Raajje Adlist",
                "https://raw.githubusercontent.com/evenxzero/Raajje-AdList/master/filter.txt",
                "https://github.com/evenxzero/Raajje-AdList",
                "Dhivehi"
            ),
            arrayOf<String?>(
                "Eesti saitidele kohandatud filter",
                "https://adblock.ee/list.php",
                "https://adblock.ee",
                "Eesti keel"
            ),
            arrayOf<String?>(
                "Estonian filters by Gurud.ee",
                "https://gurud.ee/ab.txt",
                "https://gurud.ee",
                "Eesti keel"
            ),
            arrayOf<String?>(
                "Peter Lowe's list",
                "https://pgl.yoyo.org/adservers/serverlist.php?hostformat=adblockplus&mimetype=plaintext",
                "https://pgl.yoyo.org/adservers",
                "English"
            ),
            arrayOf<String?>(
                "Colombian filters by yecarrillo",
                "https://raw.githubusercontent.com/yecarrillo/adblock-colombia/master/adblock_co.txt",
                "https://github.com/yecarrillo/adblock-colombia",
                "Español"
            ),
            arrayOf<String?>(
                "EasyList Spanish",
                "https://easylist-downloads.adblockplus.org/easylistspanish.txt",
                "abp",
                "Español"
            ),
            arrayOf<String?>(
                "Tiswagos Liri AdBlockList",
                "https://raw.githubusercontent.com/Xaival/AdBlockList/main/Adblock_list.txt",
                "https://github.com/Xaival/AdBlockList",
                "Español"
            ),
            arrayOf<String?>(
                "Liste FR",
                "https://easylist-downloads.adblockplus.org/liste_fr.txt",
                "https://forums.lanik.us/viewforum.php?f=91",
                "Français"
            ),
            arrayOf<String?>(
                "EasyList Italy",
                "https://easylist-downloads.adblockplus.org/easylistitaly.txt",
                "abp",
                "Italiano"
            ),
            arrayOf<String?>(
                "Xfiles",
                "https://raw.githubusercontent.com/gioxx/xfiles/master/filtri.txt",
                "https://xfiles.noads.it",
                "Italiano"
            ),
            arrayOf<String?>(
                "Latvian List",
                "https://raw.githubusercontent.com/Latvian-List/adblock-latvian/master/lists/latvian-list.txt",
                "https://github.com/Latvian-List/adblock-latvian",
                "Latviešu valoda"
            ),
            arrayOf<String?>(
                "EasyList Lithuania",
                "https://easylist-downloads.adblockplus.org/easylistlithuania.txt",
                "https://github.com/EasyList-Lithuania/easylist_lithuania",
                "Lietuvių kalba"
            ),
            arrayOf<String?>(
                "hufilter",
                "https://raw.githubusercontent.com/hufilter/hufilter/refs/heads/gh-pages/hufilter-abp.txt",
                "https://github.com/hufilter/hufilter/wiki",
                "Magyar"
            ),
            arrayOf<String?>(
                "EasyList Dutch",
                "https://easylist-downloads.adblockplus.org/easylistdutch.txt",
                "abp",
                "Nederlands"
            ),
            arrayOf<String?>(
                "Dandelion Sprout's Nordic Filters",
                "https://raw.githubusercontent.com/DandelionSprout/adfilt/master/NorwegianExperimentalList%20alternate%20versions/NordicFiltersABP-Inclusion.txt",
                "https://github.com/DandelionSprout/adfilt",
                "Norsk, Norsk, Norsk, Dansk, Íslenska, Føroyskt, Kalaallisut"
            ),
            arrayOf<String?>(
                "EasyList Polish",
                "https://easylist-downloads.adblockplus.org/easylistpolish.txt",
                "abp",
                "Polski"
            ),
            arrayOf<String?>(
                "EasyList Portuguese",
                "https://easylist-downloads.adblockplus.org/easylistportuguese.txt",
                "abp",
                "Português"
            ),
            arrayOf<String?>(
                "RU AdList",
                "https://easylist-downloads.adblockplus.org/advblock.txt",
                "https://forums.lanik.us/viewforum.php?f=102",
                "Русский, Українська"
            ),
            arrayOf<String?>(
                "ROList",
                "https://easylist-downloads.adblockplus.org/rolist.txt",
                "https://zoso.ro/rolist",
                "Românesc"
            ),
            arrayOf<String?>(
                "ABPVN List",
                "https://easylist-downloads.adblockplus.org/abpvn.txt",
                "https://abpvn.com",
                "Tiếng Việt"
            ),
            arrayOf<String?>(
                "void.gr",
                "https://www.void.gr/kargig/void-gr-filters.txt",
                "https://github.com/kargig/greek-adblockplus-filter",
                "ελληνικά"
            ),
            arrayOf<String?>(
                "Bulgarian list",
                "https://stanev.org/abp/adblock_bg.txt",
                "https://stanev.org/abp",
                "български"
            ),
            arrayOf<String?>(
                "EasyList Hebrew",
                "https://raw.githubusercontent.com/easylist/EasyListHebrew/master/EasyListHebrew.txt",
                "https://github.com/easylist/EasyListHebrew",
                "עברית"
            ),
            arrayOf<String?>(
                "Liste AR",
                "https://easylist-downloads.adblockplus.org/Liste_AR.txt",
                "https://code.google.com/p/liste-ar-adblock",
                "العربية"
            ),
            arrayOf<String?>(
                "AdBlockFarsi",
                "https://raw.githubusercontent.com/SlashArash/adblockfa/master/adblockfa.txt",
                "https://github.com/SlashArash/adblockfa",
                "فارسی"
            ),
            arrayOf<String?>(
                "IndianList",
                "https://easylist-downloads.adblockplus.org/indianlist.txt",
                "abp",
                "বাংলা (ভারত), ગુજરાતી (ભારત), भारतीय, ਪੰਜਾਬੀ (ਭਾਰਤ), অসমীয়া, मराठी, മലയാളം, తెలుగు, ಕನ್ನಡ, ଓଡ଼ିଆ, नेपाली, සිංහල"
            ),
            arrayOf<String?>(
                "EasyList China",
                "https://easylist-downloads.adblockplus.org/easylistchina.txt",
                "https://github.com/easylist/easylistchina",
                "中文"
            ),
            arrayOf<String?>(
                "CJX's Annoyance List",
                "https://easylist-downloads.adblockplus.org/cjx-annoyance.txt",
                "https://github.com/cjx82630/cjxlist",
                "Removes self-promotion and privacy protection, 中文"
            ),
            arrayOf<String?>(
                "ABP Japanese Filters",
                "https://raw.githubusercontent.com/k2jp/abp-japanese-filters/master/abpjf.txt",
                "https://github.com/k2jp/abp-japanese-filters",
                "日本語"
            ),
            arrayOf<String?>(
                "KoreanList",
                "https://easylist-downloads.adblockplus.org/koreanlist.txt",
                "https://forums.lanik.us/viewforum.php?f=111",
                "한국어"
            ),
            arrayOf<String?>(
                "YousList",
                "https://raw.githubusercontent.com/yous/YousList/master/youslist.txt",
                "https://github.com/yous/YousList",
                "한국어"
            )
        )
        @JvmField
        val adguardFilters: Array<Array<String?>?> = arrayOf<Array<String?>?>(
            arrayOf<String?>("Base filter", "filter_2_Base"),
            arrayOf<String?>("Mobile ads filter", "filter_11_Mobile"),
            arrayOf<String?>("Annoyances filter", "filter_14_Annoyances"),
            arrayOf<String?>("DNS filter", "filter_15_DnsFilter"),
            arrayOf<String?>("Filter unblocking search ads and self-promotions", "filter_10_Useful"),
            arrayOf<String?>("Social media filter", "filter_4_Social"),
            arrayOf<String?>("Tracking Protection filter", "filter_3_Spyware"),
            arrayOf<String?>("URL Tracking filter", "filter_17_TrackParam"),
            arrayOf<String?>("Experimental filter", "filter_5_Experimental"),
            arrayOf<String?>("Chinese filter", "filter_224_Chinese"),
            arrayOf<String?>("Dutch filter", "filter_8_Dutch"),
            arrayOf<String?>("French filter", "filter_16_French"),
            arrayOf<String?>("German filter", "filter_6_German"),
            arrayOf<String?>("Japanese filter", "filter_7_Japanese"),
            arrayOf<String?>("Russian filter", "filter_1_Russian"),
            arrayOf<String?>("Spanish/Portuguese filter", "filter_9_Spanish"),
            arrayOf<String?>("Turkish filter", "filter_13_Turkish"),
            arrayOf<String?>("Ukrainian filter", "filter_23_Ukrainian")
        )
        @JvmField
        val extraFilters: Array<Array<String?>?> = arrayOf<Array<String?>?>(
            arrayOf<String?>(
                "https://easylist.to/easylist/easylist.txt",
                "https://easylist-downloads.adblockplus.org/easylist.txt"
            ),
            arrayOf<String?>(
                "https://easylist.to/easylist/easyprivacy.txt",
                "https://easylist-downloads.adblockplus.org/easyprivacy.txt"
            ),
            arrayOf<String?>(
                "https://secure.fanboy.co.nz/fanboy-cookiemonster.txt",
                "https://easylist-downloads.adblockplus.org/fanboy-cookiemonster.txt"
            ),
            arrayOf<String?>(
                "https://secure.fanboy.co.nz/fanboy-annoyance.txt",
                "https://easylist-downloads.adblockplus.org/fanboy-annoyance.txt"
            ),
            arrayOf<String?>(
                "https://easylist.to/easylist/fanboy-social.txt",
                "https://easylist-downloads.adblockplus.org/fanboy-social.txt"
            ),
            arrayOf<String?>(
                "https://raw.githubusercontent.com/ABPindo/indonesianadblockrules/master/subscriptions/abpindo.txt",
                "https://easylist-downloads.adblockplus.org/abpindo.txt"
            ),
            arrayOf<String?>(
                "https://notabug.org/latvian-list/adblock-latvian/raw/master/lists/latvian-list.txt",
                "https://raw.githubusercontent.com/Latvian-List/adblock-latvian/master/lists/latvian-list.txt"
            ),
            arrayOf<String?>(
                "https://raw.githubusercontent.com/EasyList-Lithuania/easylist_lithuania/master/easylistlithuania.txt",
                "https://easylist-downloads.adblockplus.org/easylistlithuania.txt"
            ),
            arrayOf<String?>(
                "https://www.zoso.ro/pages/rolist.txt",
                "https://easylist-downloads.adblockplus.org/rolist.txt"
            ),
            arrayOf<String?>(
                "https://raw.githubusercontent.com/abpvn/abpvn/master/filter/abpvn.txt",
                "https://easylist-downloads.adblockplus.org/abpvn.txt"
            ),
            arrayOf<String?>(
                "https://raw.githubusercontent.com/cjx82630/cjxlist/master/cjx-annoyance.txt",
                "https://easylist-downloads.adblockplus.org/cjx-annoyance.txt"
            ),
            arrayOf<String?>(
                "https://raw.githubusercontent.com/AdguardTeam/FiltersRegistry/master/filters/filter_2_English/filter.txt",
                "https://raw.githubusercontent.com/AdguardTeam/FiltersRegistry/master/filters/filter_2_Base/filter.txt"
            )
        )

        @JvmStatic
        fun addFilters(context: Context?, map: HashMap<String?, String?>?) {
            if (map!!.isEmpty()) {
                return
            }
            try {
                for (path in map.keys) {
                    if (!TextUtils.isEmpty(path)) {
                        val mapped = map.get(path)
                        if (!TextUtils.isEmpty(mapped)) {
                            val dbBookFilter = DbBookFilter.cached
                            if (context != null && !TextUtils.isEmpty(path) && !TextUtils.isEmpty(
                                    mapped
                                )
                            ) {
                                val contentValues = ContentValues()
                                contentValues.put("_path", mapped)
                                DbUtil.updateWhere(
                                    DbBookFilter.get(context).getWritableDatabase(),
                                    "DbBookFilter_table",
                                    contentValues,
                                    "_path=?",
                                    arrayOf<String?>(path!!)
                                )
                            }
                            MainUtil.moveFileOrUri(context, filterFilePath(context, path), filterFilePath(context, mapped))
                        }
                    }
                }
            } catch (unused: Exception) {
            }
        }

        @JvmStatic
        fun adguardUrl(regional: Int): String? {
            if (regional < 0 || regional >= 18) {
                return null
            }
            return (StringBuilder("https://raw.githubusercontent.com/AdguardTeam/FiltersRegistry/master/filters/")).append(adguardFilters[regional]!![1]).append("/filter.txt").toString()
        }

        @JvmStatic
        fun adguardIdOf(key: String?): String? {
            val prefixLength: Int
            if (TextUtils.isEmpty(key)) {
                return null
            }
            if (!key!!.endsWith("filter-AdGuard.txt") && !key.endsWith("filter-uBlockOrigin.txt")) {
                return null
            }
            if (!key.startsWith("https://raw.githubusercontent.com/")) {
                if (key.startsWith("https://cdn.jsdelivr.net/gh/")) {
                    prefixLength = 28
                    if (!key.startsWith("List-KR/List-KR@", 28)) {
                        return null
                    }
                }
                return null
            }
            prefixLength = 34
            if (!key.startsWith("List-KR/List-KR/", 34)) {
                return null
            }
            val pathStart = prefixLength + 16
            if (key.startsWith("master/", pathStart)) {
                return "master/"
            }
            if (key.startsWith("latest/", pathStart)) {
                return "latest/"
            }
            return null
        }

        @JvmStatic
        fun normalizeUrl(path: String?): String? {
            var extraList: Array<Array<String?>?>
            if (TextUtils.isEmpty(path)) {
                return null
            }
            val githubUrl = MainUtil.jsDelivrToGithub(path)
            if (!TextUtils.isEmpty(githubUrl)) {
                if (if (TextUtils.isEmpty(githubUrl)) false else githubUrl!!.startsWith("https://raw.githubusercontent.com/AdguardTeam/")) {
                    for (index in 0..17) {
                        if (githubUrl == adguardUrl(index)) {
                            return githubUrl
                        }
                    }
                } else {
                    for (index in 0..43) {
                        if (githubUrl == filters[index]!![1]) {
                            return githubUrl
                        }
                    }
                }
            }
            var index = 0
            while (true) {
                extraList = extraFilters
                if (index >= 12) {
                    index = -1
                    break
                }
                if (extraList[index]!![0] == path) {
                    break
                }
                index++
            }
            if (index < 0 || index >= 12) {
                return null
            }
            return extraList[index]!![1]
        }
    }
}
