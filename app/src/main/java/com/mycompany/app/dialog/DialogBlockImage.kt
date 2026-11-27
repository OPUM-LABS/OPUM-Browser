package com.mycompany.app.dialog

import android.view.View
import com.mycompany.app.main.RequestCode

import com.mycompany.app.view.anyListener
import android.content.Context
import com.mycompany.app.pref.store.BrowserSettings
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.text.TextUtils
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R as AppR
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookBlock
import com.mycompany.app.dialog.DialogSetAdblock.DialogAdsListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.trimTrailingSlash
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.web.WebClean
import com.mycompany.app.web.WebViewActivity
import java.lang.ref.WeakReference

/**
 * Block one image (image long-press > Block image): the image [imageUrlParam] (loaded with the page [url] as referer, retried
 * without it; SVG through `PictureDrawable`), switches "Block on this site" / "Block on this page" (saved at once by
 * [DialogTask] in `DbBookBlock` and the ad blocker's image map), "Blocked images" (opens the list, [DialogListBook]),
 * the ad-blocker settings button and Refresh. On close [DialogAdsListener] hears what changed (as before). Compose now
 * (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogBlockImage(
    webViewActivity: WebViewActivity?,
    url: String?,
    imageUrlParam: String?,
    dialogAdsListener: DialogAdsListener?,
) : ComposeBottomDialog(webViewActivity) {
    @JvmField
    var hostActivity: WebViewActivity? = webViewActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var adsListener: DialogAdsListener? = dialogAdsListener
    @JvmField
    var pageUrl: String? = trimTrailingSlash(url)
    @JvmField
    var pageHost: String? = MainUtil.hostOfUrl(pageUrl!!, true)
    @JvmField
    var imageUrl: String? = imageUrlParam
    @JvmField
    var imageView: ImageView? = null
    @JvmField
    var glideUrl: GlideUrl? = null
    @JvmField
    var glideRequests: GlideRequests? = null
    /** Already retried without the referer. */
    @JvmField
    var noReferer: Boolean = false
    @JvmField
    var blockTask: DialogTask? = null
    private var listOpen = false
    /** Blocked on the site / on the page. */
    @JvmField
    var siteBlocked: Boolean = false
    @JvmField
    var pageBlocked: Boolean = false
    @JvmField
    var adsBlockStart: Boolean = BrowserSettings.adsBlock.value
    /** Settings changed / filter result (set by `WebViewActivity` from the settings screen). */
    @JvmField
    var listChanged: Boolean = false
    @JvmField
    var settingsKept: Boolean = false
    @JvmField
    var startBlocked: Boolean = false
    /** Refresh pressed. */
    @JvmField
    var refreshPage: Boolean = false
    @JvmField
    var webClean: WebClean? = MainApp.webClean(appContext, false)
    @JvmField
    var imageListener: RequestListener<Any?>? = null
    @JvmField
    var svgListener: RequestListener<Any?>? = null

    private var site by mutableStateOf(false)
    private var page by mutableStateOf(false)
    private var loaded by mutableStateOf(false)
    internal var working by mutableStateOf(false)

    private val ui = android.os.Handler(android.os.Looper.getMainLooper())

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    init {
        imageListener = object : RequestListener<Any?> {
            override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
                if (glideUrl != null && !noReferer && imageView != null) {
                    noReferer = true
                    ui.post { loadImage() }
                }
                return true
            }

            override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                if (glideRequests != null) loaded = true
                return false
            }
        }
        svgListener = object : RequestListener<Any?> {
            override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
                if (glideUrl != null && !noReferer && imageView != null) {
                    noReferer = true
                    ui.post { loadSvg() }
                }
                return true
            }

            override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
                if (glideRequests == null) return false
                imageView?.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                loaded = true
                return false
            }
        }
        val clean = webClean
        if (clean != null) {
            siteBlocked = clean.isUserBlockedSrc(pageHost, imageUrl)
            pageBlocked = clean.isUserBlockedUrl(pageUrl, imageUrl)
        }
        startBlocked = siteBlocked || pageBlocked
        site = siteBlocked
        page = pageBlocked
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(Modifier.weight(1f).height(140.dp), contentAlignment = Alignment.CenterStart) {
                    AndroidView(
                        factory = { ctx ->
                            ImageView(ctx).also {
                                it.setScaleType(ScaleType.FIT_START)
                                imageView = it
                                ui.post { loadImage() }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(140.dp).alpha(if (loaded) 1f else 0f),
                    )
                    if (!loaded) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                            Icon(painterResource(AppR.drawable.outline_image_black_24), contentDescription = null, modifier = Modifier.size(24.dp))
                        }
                    }
                }
                IconButton(onClick = { openSettings() }) {
                    Icon(painterResource(AppR.drawable.outline_settings_black_20), contentDescription = null)
                }
            }
            SwitchRow(stringResource(AppR.string.item_block_site), site) { setBlock(1, it) }
            SwitchRow(stringResource(AppR.string.item_block_page), page) { setBlock(2, it) }
            ListItem(
                headlineContent = { Text(stringResource(AppR.string.blocked_image)) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.clickable(enabled = !working) { openList() },
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { refreshPage = true; dismiss() }) { Text(stringResource(AppR.string.refresh)) }
            }
        }
        show()
    }

    @Composable
    private fun SwitchRow(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
        ListItem(
            headlineContent = { Text(title) },
            trailingContent = { Switch(checked = checked, onCheckedChange = onChange, enabled = !working) },
            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.clickable(enabled = !working) { onChange(!checked) },
        )
    }

    /** Switch 1 (site) or 2 (page) changed: saves at once. */
    private fun setBlock(mode: Int, newSiteBlocked: Boolean) {
        val path = if (mode == 1) {
            siteBlocked = newSiteBlocked
            site = newSiteBlocked
            pageHost
        } else {
            pageBlocked = newSiteBlocked
            page = newSiteBlocked
            pageUrl
        }
        blockTask?.cancelled = true
        blockTask = null
        val task = DialogTask(this, path, imageUrl, newSiteBlocked)
        blockTask = task
        task.start(appContext)
    }

    /** The list now opens as a Compose screen, [listOpen] guards double opens. */
    private fun openList() {
        val host = hostActivity ?: return
        if (listOpen) return
        listOpen = true
        com.mycompany.app.ui.lists.SavedSitesActivity.open(host, 23) {
            listOpen = false
            if (hostActivity != null) applyBlocked(false)
        }
    }

    private fun openSettings() {
        val act = hostActivity ?: return
        val intent = Intent(appContext, com.mycompany.app.ui.settings.BlockedSettingsActivity::class.java)
        intent.putExtra("EXTRA_POPUP", true)
        intent.putExtra("EXTRA_NOTI", true)
        intent.putExtra("EXTRA_INDEX", 19)
        intent.putExtra("EXTRA_PATH", pageUrl)
        act.launchExternalForResult(intent, RequestCode.DIALOG_SETTINGS)
    }

    /** Re-reads the two block states (after the list or settings), [blocked] passes on to the open list. */
    fun applyBlocked(blocked: Boolean) {
        val clean = webClean
        val onSite = clean?.isUserBlockedSrc(pageHost, imageUrl) ?: false
        val onPage = clean?.isUserBlockedUrl(pageUrl, imageUrl) ?: false
        siteBlocked = onSite
        pageBlocked = onPage
        site = onSite
        page = onPage
    }

    /** Loads the image (SVG through [loadSvg]); the page is the referer until a failure. */
    fun loadImage() {
        if (imageView == null) return
        if (isSvgName(guessDownloadName(imageUrl, null, null, true))) {
            loadSvg()
            return
        }
        glideUrl = if (URLUtil.isNetworkUrl(imageUrl)) MainUtil.glideUrlFor(appContext, imageUrl!!, if (noReferer) null else pageUrl) else null
        runAsync(Runnable {
            val act = hostActivity ?: return@Runnable
            if (glideRequests == null) glideRequests = GlideApp.with(act)
            ui.post {
                val requests = glideRequests ?: return@post
                val view = imageView ?: return@post
                val url = glideUrl
                val cache = DiskCacheStrategy.NONE
                if (url != null) {
                    (requests.load(url).diskCacheStrategy(cache)).anyListener(imageListener).into(view)
                } else {
                    (requests.load(imageUrl).diskCacheStrategy(cache)).anyListener(imageListener).into(view)
                }
            }
        })
    }

    fun loadSvg() {
        glideUrl = if (URLUtil.isNetworkUrl(imageUrl)) MainUtil.glideUrlFor(appContext, imageUrl!!, if (noReferer) null else pageUrl) else null
        runAsync(Runnable {
            val act = hostActivity ?: return@Runnable
            if (glideRequests == null) glideRequests = GlideApp.with(act)
            ui.post {
                val requests = glideRequests ?: return@post
                val view = imageView ?: return@post
                val url = glideUrl
                if (url != null) {
                    ((requests.`as`(PictureDrawable::class.java)).load(url)).anyListener(svgListener).into(view)
                } else {
                    ((requests.`as`(PictureDrawable::class.java)).load(imageUrl)).anyListener(svgListener).into(view)
                }
            }
        })
    }

    override fun canCancelFromScrim(): Boolean = !working

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        blockTask?.cancelled = true
        blockTask = null
        val listener = adsListener
        if (listener != null) {
            val blocked = siteBlocked || pageBlocked
            val changed = startBlocked != blocked
            listener.onApply(changed, adsBlockStart != BrowserSettings.adsBlock.value, listChanged, !settingsKept, refreshPage, if (blocked && changed && !refreshPage) imageUrl else null)
            adsListener = null
        }
        val view = imageView
        if (view != null) glideRequests?.clear(view)
        glideRequests = null
        hostActivity = null
        appContext = null
        imageView = null
        super.dismiss()
    }

    class DialogTask(dialogBlockImage: DialogBlockImage?, path: String?, image: String?, isBlock: Boolean) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = WeakReference<Any?>(dialogBlockImage)
        @JvmField
        var path: String? = path
        @JvmField
        var image: String? = image
        @JvmField
        var isBlock: Boolean = isBlock

        init {
            val d = dialogRef!!.get() as DialogBlockImage?
            if (d != null) {
                d.setCanceledOnTouchOutside(false)
                d.working = true
            }
        }

        public override fun doInBackground() {
            val d = dialogRef!!.get() as DialogBlockImage?
            if (d == null || cancelled) return
            val clean = d.webClean
            val image = image
            val path = path
            if (isBlock) {
                clean?.addUserBlock(path, image)
                DbBookBlock.addPath(d.appContext, path, image)
                return
            }
            clean?.removeUserBlock(path, image)
            val ctx = d.appContext
            if (ctx == null || TextUtils.isEmpty(path) || TextUtils.isEmpty(image)) return
            DbUtil.deleteWhere(DbBookBlock.get(ctx).getWritableDatabase(), "DbBookBlock_table", "_path=? AND _image=?", arrayOf<String?>(path!!, image!!))
        }

        public override fun onCancelled() {
            done()
        }

        public override fun onPostExecute() {
            done()
        }

        private fun done() {
            val d = dialogRef!!.get() as DialogBlockImage? ?: return
            d.blockTask = null
            if (d.appContext == null) return
            d.setCanceledOnTouchOutside(true)
            d.working = false
        }
    }

    companion object {
    }
}
