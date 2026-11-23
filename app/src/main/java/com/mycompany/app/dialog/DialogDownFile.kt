package com.mycompany.app.dialog

import android.view.View

import com.mycompany.app.view.anyListener
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.text.TextUtils
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R as AppR
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.mycompany.app.compress.Compress
import com.mycompany.app.dialog.DialogDownUrl.DownUrlListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests

/**
 * Save an image again from the Downloads list: image [fileUrl] (referer [referer], retried without it; SVG through
 * `PictureDrawable`) as a preview with a button that opens it ([DownUrlListener.preview]), or the image icon and the file
 * name [startName] while it loads; the name (file name, kept extension) and save location; Save creates the file
 * (`MainUri.createUri`) and hands it to [DownUrlListener.onDownload], then closes. The folder picker result comes through [setFolderPath]; the
 * header hides in landscape ([applyLandscape]). Compose now (on [SaveFolderDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogDownFile(
    mainActivity: android.app.Activity?,
    fileUrl: String?,
    referer: String?,
    startName: String?,
    downUrlListener: DownUrlListener?,
) : SaveFolderDialog(mainActivity, null) {
    @JvmField
    var glideUrl: GlideUrl? = null
    @JvmField
    var glideRequests: GlideRequests? = null
    /** Already retried without the referer. */
    @JvmField
    var noReferer: Boolean = false
    @JvmField
    var saveUri: UriItem? = null
    @JvmField
    var imageListener: RequestListener<Any?>? = null
    @JvmField
    var svgListener: RequestListener<Any?>? = null
    @JvmField
    var imageView: ImageView? = null
    @JvmField
    var fileUrl: String? = fileUrl
    @JvmField
    var referer: String? = referer
    @JvmField
    var startName: String? = startName
    @JvmField
    var downListener: DownUrlListener? = downUrlListener

    private var loaded by mutableStateOf(false)
    private val ui = android.os.Handler(android.os.Looper.getMainLooper())

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
        start()
        applyLandscape((hostActivity as? MainActivity)?.isLandscapeScreen() ?: MainUtil.isLandscape(context))
    }

    /** Suggested name: the file name shortened to fit, keeping its extension. */
    override fun prepare(): String? {
        var name = startName
        if (TextUtils.isEmpty(name)) return name
        val dot = name!!.lastIndexOf('.')
        var room = 190
        val ext: String?
        if (dot != -1) {
            ext = name.substring(dot)
            room = 190 - ext.length
            if (dot < name.length) name = name.substring(0, dot)
        } else {
            ext = null
        }
        return if (ext != null) MainUtil.safeTitleFileName(room, name, "Download") + ext else MainUtil.safeTitleFileName(room, name, "Download")
    }

    @Composable
    override fun ColumnScope.Header() {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.weight(1f).height(if (loaded) 140.dp else 48.dp), contentAlignment = Alignment.CenterStart) {
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                            Icon(painterResource(AppR.drawable.outline_image_black_24), contentDescription = null, modifier = Modifier.size(24.dp))
                        }
                        Text(
                            startName ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp),
                        )
                    }
                }
            }
            FilledTonalIconButton(onClick = { downListener?.preview(fileUrl, null, "image/*", false) }, modifier = Modifier.padding(start = 12.dp)) {
                Icon(painterResource(AppR.drawable.baseline_play_arrow_black_24), contentDescription = null)
            }
        }
    }

    /** Loads the image (SVG through [loadSvg]); the referer is used until a failure. */
    fun loadImage() {
        if (imageView == null) return
        if (Compress.isSvgName(guessDownloadName(fileUrl, null, null, true))) {
            loadSvg()
            return
        }
        glideUrl = if (URLUtil.isNetworkUrl(fileUrl)) MainUtil.glideUrlFor(appContext, fileUrl!!, if (noReferer) null else referer) else null
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
                    (requests.load(fileUrl).diskCacheStrategy(cache)).anyListener(imageListener).into(view)
                }
            }
        })
    }

    fun loadSvg() {
        glideUrl = if (URLUtil.isNetworkUrl(fileUrl)) MainUtil.glideUrlFor(appContext, fileUrl!!, if (noReferer) null else referer) else null
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
                    ((requests.`as`(PictureDrawable::class.java)).load(fileUrl)).anyListener(svgListener).into(view)
                }
            }
        })
    }

    override fun onSave(fileName: String) {
        runAsync(Runnable {
            if (downListener != null) saveUri = MainUri.createUri(appContext, MainUri.storageVolume(), null, fileName)
            ui.post {
                val item = saveUri
                saveUri = null
                downListener?.onDownload(fileUrl, item, 1, false, null, referer)
                dismiss()
            }
        })
    }

    /** Landscape ([newLandscape]) hides the header. */
    fun applyLandscape(newLandscape: Boolean) {
        var landscape = newLandscape
        if (landscape) landscape = (hostActivity as? MainActivity)?.smallScreen ?: MainUtil.isSmallScreen(context)
        headerShown = !landscape
    }

    /** Folder picker result path. */
    fun setFolderPath(path: String?) {
        setFolder(path)
    }

    override fun dismiss() {
        val view = imageView
        if (view != null) glideRequests?.clear(view)
        glideRequests = null
        imageView = null
        downListener = null
        super.dismiss()
    }

    companion object {
    }
}
