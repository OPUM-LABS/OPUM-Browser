package com.mycompany.app.dialog

import com.mycompany.app.view.anyListener
import com.mycompany.app.ui.components.GuideState
import com.mycompany.app.ui.components.PinchGuideCard
import android.view.ViewGroup
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import java.util.HashMap
import com.mycompany.app.pref.store.ReadSettings
import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.net.Uri
import android.os.Handler
import android.text.TextUtils
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.webkit.DownloadListener
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.mycompany.app.compress.Compress.Companion.isSvgName
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainUtil.destroyWebViewNow
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.firstUrlIn
import com.mycompany.app.main.MainUtil.fileExtOf
import com.mycompany.app.main.MainUtil.applyZoomSettings
import com.mycompany.app.main.MainUtil.guessDownloadName
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.htmlDataUri
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.isFakeStream
import com.mycompany.app.main.MainUtil.mimeForExt
import com.mycompany.app.main.MainUtil.requestHeadersFor
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.video.VideoSeekControl
import com.mycompany.app.video.VideoSeekControl.SeekCtrlListener
import com.mycompany.app.view.GlideApp
import com.mycompany.app.view.GlideRequest
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.view.MyFadeListener
import com.mycompany.app.view.MyPlayerView
import com.mycompany.app.view.MyPlayerView.PlayerViewListener
import com.mycompany.app.view.MyWebSafe
import com.mycompany.app.zoom.ZoomImageAttacher
import com.mycompany.app.zoom.ZoomImageAttacher.AttacherListener
import com.mycompany.app.zoom.ZoomVideoAttacher
import com.mycompany.app.zoom.ZoomVideoAttacher.VideoAttacherListener
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mycompany.app.ui.browser.ComposeFullDialog

/**
 * Media preview (download list / browser / save sheets; [pageUrl] the URL, [referer] referer, [bitmap] a ready picture,
 * [mimeType] the MIME type): a black card in the middle of a dimmed screen with the picture (zoomable, SVG too), video
 * (the app's player with the seek bar, or the page's own video in a WebView for blobs / failed players) or audio
 * (WebView player); a tap shows the actions for a few seconds: download, move, share, copy link, open (each through
 * [PreviewListener]). The media logic (players, Glide, WebView, zoom, one-time pinch guide `mGuidePrev`) is the old
 * code on the old views; the card, the action buttons and their fading are Compose. Same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogPreview(
    mainActivity: android.app.Activity?,
    pageUrl: String?,
    referer: String?,
    bitmap: Bitmap?,
    mimeType: String?,
    previewListener: PreviewListener?
) : ComposeFullDialog(mainActivity) {
    /** Compose stand-in for the old fading action layer (`MyFadeFrame`). */
    class Fade(private val ui: Handler) {
        var shown by mutableStateOf(false)
        private var autoHide = true
        private val hide = Runnable { shown = false }
        fun setAutoHide(newAutoHide: Boolean) { autoHide = newAutoHide; if (!newAutoHide) ui.removeCallbacks(hide) }
        fun setVisibility(visibility: Int) { shown = visibility == 0 }
        fun hideNow(animate: Boolean) { ui.removeCallbacks(hide); shown = false }
        fun showNow(animate: Boolean) { ui.removeCallbacks(hide); shown = true; if (autoHide) ui.postDelayed(hide, 3000) }
        fun toggle(show: Boolean) { if (show) showNow(true) else hideNow(true) }
        fun isShown(): Boolean = shown
        fun restartHide() { ui.removeCallbacks(hide); if (shown && autoHide) ui.postDelayed(hide, 3000) }
        fun cancelHide() { ui.removeCallbacks(hide) }
    }

    /** Compose stand-in for a button that only changed visibility. */
    class UiPart(visible: Boolean) {
        var shown by mutableStateOf(visible)
        fun setVisibility(visibility: Int) { shown = visibility == 0 }
    }

    /**
     * The media area (old: the two `FrameLayout`s `mediaArea` / `contentArea`), drawn by Compose (2026-11-17). It holds the render
     * surface (the picture's `ImageView`, the player view or the web player, each shown through `AndroidView`) and
     * the media height; posts go to the dialog's handler. [alive] replaces the old "frame is not null" checks.
     */
    inner class MediaArea {
        val surfaces = mutableStateListOf<View>()
        var heightDp by mutableIntStateOf(320)
        /** Audio: the web player is a bar at the bottom with a music note in the middle. */
        var audio by mutableStateOf(false)
        fun addView(view: View?, index: Int, lp: ViewGroup.LayoutParams? = null) { if (view != null && view !in surfaces) surfaces.add(view) }
        fun removeView(view: View?) { surfaces.remove(view) }
        fun post(r: Runnable?): Boolean = r != null && mainHandler?.post(r) == true
        fun postDelayed(r: Runnable?, ms: Long): Boolean = r != null && mainHandler?.postDelayed(r, ms) == true
        fun removeCallbacks(runnable: Runnable?) { if (runnable != null) mainHandler?.removeCallbacks(runnable) }
    }

    /** The "server is slow" note under the media (old `AppCompatTextView`). */
    class NoteState {
        var shown by mutableStateOf(false)
        fun setVisibility(visibility: Int) { shown = visibility == 0 }
    }

    /** The loading cover over the media (old `LoadCover`): [canScale] fade in, [duration] hide, black back while loading. */
    class CoverState(private val ui: Handler) {
        var shown by mutableStateOf(false)
        var black by mutableStateOf(false)
        fun show(dialogWebBookDir: Boolean) { shown = true }
        fun hide(fade: Boolean) { shown = false }
        fun reset() { shown = false }
        fun setBackgroundColor(color: Int) { black = color != 0 }
        fun setBackground(d: Any?) { black = false }
        fun setForeSize(size: Int) {}
        fun postDelayed(runnable: Runnable, ms: Long) { ui.postDelayed(runnable, ms) }
    }


    @JvmField val mainHandler: Handler? = Handler(android.os.Looper.getMainLooper())
    private var executor: java.util.concurrent.ExecutorService? = null

    @JvmField var imageZoom: ZoomImageAttacher? = null
    @JvmField var videoZoom: ZoomVideoAttacher? = null
    @JvmField var gestureDetect: GestureDetector? = null
    @JvmField var noteShown: Boolean = false
    @JvmField var noteTime: Long = 0
    @JvmField var glide: GlideRequests? = null
    @JvmField var imageFailed: Boolean = false
    @JvmField var videoName: String? = null
    @JvmField var currentUrl: String? = null
    @JvmField var playerStarted: Boolean = false
    @JvmField var touchMoved: Boolean = false
    @JvmField var imageUrlIn: String? = null
    @JvmField var imageListener: RequestListener<Any?>? = null
    @JvmField var mediaUrlCopy: String? = null
    @JvmField var thumbListener: RequestListener<Any?>? = null
    @JvmField var webPageUrl: String? = null
    @JvmField var webUrlIn: String? = null
    @JvmField var downUrl: String? = null
    @JvmField var downExt: String? = null
    @JvmField var downMime: String? = null
    @JvmField var htmlUrlIn: String? = null
    @JvmField var htmlRefererIn: String? = null
    @JvmField var hostActivity: android.app.Activity? = mainActivity
    @JvmField var htmlMimeIn: String? = null
    @JvmField var appCtx: Context? = context
    @JvmField var audioUrlIn: String? = null
    @JvmField var listener: PreviewListener? = previewListener
    @JvmField var audioUrl: String? = null
    @JvmField var pageUrl: String? = pageUrl
    @JvmField var webUrlOut: String? = null
    @JvmField var mediaUrl: String? = null
    @JvmField var finishHandled: Boolean = false
    @JvmField var referer: String? = referer
    @JvmField var lastFinishedUrl: String? = null
    @JvmField var hideRunnable: Runnable? = null
    @JvmField var mimeType: String? = mimeType
    @JvmField var seekValue: Int = 0
    @JvmField var mediaType: Byte = 0
    @JvmField var seekTarget: Int = 0
    @JvmField var loadedOnce: Boolean = false
    /** Outer frame of the media (alive check, posts). */
    @JvmField var mediaArea: MediaArea? = null
    /** The media frame (picture / player / web player, cover, server-delay note, pinch guide). */
    @JvmField var contentArea: MediaArea? = null
    @JvmField var playerView: MyPlayerView? = null
    @JvmField var seekControl: VideoSeekControl? = null
    @JvmField var seekVisible: Boolean = false
    @JvmField var seekPos: Int = 0
    @JvmField var previewImage: ImageView? = null
    @JvmField var webSafe: MyWebSafe? = null
    @JvmField var webLoading: Boolean = false
    /** Old: the music-note view over the audio player (now [MediaArea.audio]); kept as a "shown" marker. */
    @JvmField var webPaused: Any? = null
    @JvmField var noteState: NoteState? = null
    @JvmField var coverState: CoverState? = null
    @JvmField var fadeFrame: Fade? = null
    @JvmField var openPart: UiPart? = null
    @JvmField var guideState: GuideState? = null
    /** The video seek bar is set up (drawn with the fading actions). */
    private var seekShown by mutableStateOf(false)

    init {
        var mime = mimeType
        imageListener = PlayVideoLoadListener()
        thumbListener = HideNoteLoadListener()
        hideRunnable = RestartHideTask()
        if (MainUtil.isFakeStream(pageUrl)) {
            loadedOnce = true
            mime = "video/*"
        }
        this.mimeType = mime
        mediaUrl = if ("blob:" == mime) pageUrl else firstUrlIn(pageUrl)
        val ctx = context
        val media = MediaArea()
        val small = ((mainActivity as? MainActivity)?.isLandscapeScreen() ?: MainUtil.isLandscape(ctx)) && ((mainActivity as? MainActivity)?.smallScreen ?: MainUtil.isSmallScreen(ctx))
        media.heightDp = if (small) 280 else 320
        mediaArea = media
        contentArea = media
        if (MainUtil.isBitmapOk(bitmap)) {
            val imageView = ImageView(ctx)
            imageView.scaleType = ScaleType.FIT_CENTER
            media.addView(imageView, -1)
            imageView.setImageBitmap(bitmap)
            previewImage = imageView
        } else {
            noteState = NoteState()
            coverState = CoverState(mainHandler!!)
            fadeFrame = Fade(mainHandler)
            openPart = UiPart(true)
        }
        val fade = fadeFrame
        val open = openPart
        val note = noteState
        val cover = coverState
        setPage {
            val none = remember { MutableInteractionSource() }
            Box(
                Modifier.fillMaxSize().background(Color(0x99000000.toInt())).clickable(interactionSource = none, indication = null) { dismiss() },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier.widthIn(max = 640.dp).fillMaxWidth().padding(horizontal = 2.dp)
                        .clip(RoundedCornerShape(16.dp)).background(Color.Black)
                        .clickable(interactionSource = none, indication = null) { }
                        .padding(vertical = 16.dp)
                ) {
                    MediaBox(media, note, cover)
                    if (fade != null && fade.shown) {
                        val seek = if (seekShown) seekControl else null
                        if (seek != null) {
                            AndroidView(factory = { seek }, modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter))
                        }
                        Row(Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                            val colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color(0xA0000000.toInt()), contentColor = Color.White)
                            FilledTonalIconButton(onClick = { HideNowClick().onClick(null) }, colors = colors) { Icon(painterResource(AppR.drawable.outline_download_white_24), null) }
                            FilledTonalIconButton(onClick = { DownloadClick().onClick(null) }, colors = colors) { Icon(painterResource(AppR.drawable.outline_open_with_white_24), null) }
                            FilledTonalIconButton(onClick = { ShareClick().onClick(null) }, colors = colors) { Icon(painterResource(AppR.drawable.outline_share_white_24), null) }
                            FilledTonalIconButton(onClick = { ShowNowClick().onClick(null) }, colors = colors) { Icon(painterResource(AppR.drawable.outline_link_white_24), null) }
                            if (open?.shown != false) {
                                FilledTonalIconButton(onClick = { ActionClick().onClick(null) }, colors = colors) { Icon(painterResource(AppR.drawable.outline_open_in_new_white_24), null) }
                            }
                        }
                    }
                }
            }
        }
        show()
        if (previewImage != null && MainUtil.isBitmapOk(bitmap)) releaseVideoZoom()
        if (previewImage == null) media.post(MimeForExtTask())
    }

    /** The dialog is up. */
    fun isShowingNow(): Boolean = isShowing

    /** Old sheet drag switch (allow swipe-down while the zoomed media is at its top); the card has no drag. */
    fun setSheetDrag(enabled: Boolean) {}

    fun runInBackground(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    internal inner class MimeForExtTask : Runnable {
        internal inner class ShowGuideStep : Runnable {
            override fun run() {
                val dialogPreview = this@DialogPreview
                val handler: Handler? = if (!ReadSettings.guidePrev.value || dialogPreview.guideState != null || dialogPreview.contentArea == null) null else dialogPreview.mainHandler
                if (!ReadSettings.guidePrev.value || dialogPreview.guideState != null || dialogPreview.contentArea == null || handler == null) {
                    return
                }
                handler.post(dialogPreview.ShowGuideTask())
            }
        }
        override fun run() {
            var fileExt: String?
            val dialogPreview = this@DialogPreview
            if (dialogPreview.mediaArea != null) {
                if (TextUtils.isEmpty(dialogPreview.mimeType)) {
                    fileExt = fileExtOf(dialogPreview.mediaUrl, false)
                    dialogPreview.mimeType = MainUtil.mimeForExt(fileExt)
                } else {
                    fileExt = null
                }
                if (!TextUtils.isEmpty(dialogPreview.mimeType)) {
                    if (dialogPreview.mimeType!!.startsWith("audio")) {
                        val mediaUrl = dialogPreview.mediaUrl
                        if (dialogPreview.mediaArea != null) {
                            dialogPreview.mediaType = 6.toByte()
                            dialogPreview.releasePlayer()
                            val imageView = dialogPreview.previewImage
                            if (imageView != null) {
                                val frameLayout = dialogPreview.contentArea
                                if (frameLayout != null) {
                                    frameLayout.removeView(imageView)
                                }
                                dialogPreview.previewImage = null
                            }
                            if (dialogPreview.webSafe == null) {
                                val myWebSafe = MyWebSafe(dialogPreview.hostActivity!!)
                                dialogPreview.webSafe = myWebSafe
                                MainApp.resumeWebTimers(dialogPreview.appCtx, myWebSafe)
                                dialogPreview.webSafe!!.setBackgroundColor(-16777216)
                                dialogPreview.contentArea!!.addView(dialogPreview.webSafe, 0)
                            }
                            dialogPreview.audioUrlIn = mediaUrl
                            val handler = dialogPreview.mainHandler
                            if (handler != null) {
                                handler.post(dialogPreview.LoadAudioUrlTask())
                            }
                        }
                    } else if (dialogPreview.mimeType!!.startsWith("image")) {
                        if (TextUtils.isEmpty(fileExt)) {
                            fileExt = fileExtOf(dialogPreview.mediaUrl, false)
                        }
                        dialogPreview.playVideo(dialogPreview.mediaUrl, fileExt)
                    }
                }
                if (dialogPreview.mediaType.toInt() == 0) {
                    dialogPreview.showVideo(dialogPreview.mediaUrl)
                }
                if (!ReadSettings.guidePrev.value || dialogPreview.mediaType.toInt() == 6) {
                    return
                }
                dialogPreview.mediaArea!!.post(ShowGuideStep())
            }
        }
    }
    internal inner class ShowGuideTask : Runnable {
        override fun run() {
            if (ReadSettings.guidePrev.value) {
                val dialogPreview = this@DialogPreview
                if (dialogPreview.guideState != null || dialogPreview.contentArea == null) {
                    return
                }
                dialogPreview.guideState = GuideState()
            }
        }
    }
    /** First touch on the media while the pinch guide shows: never show it again, fade it out (old listener 14). */
    fun guideTouched() {
        if (ReadSettings.guidePrev.value) ReadSettings.edit(appCtx) { set(ReadSettings.guidePrev, false) }
        guideState?.hideNow(true)
    }

    /** The guide has faded out: drop it (old listener 13). */
    fun guideGone() {
        guideState?.reset()
        guideState = null
    }
    internal inner class ShowNoteLaterTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.noteState == null || dialogPreview.noteTime == 0L || System.currentTimeMillis() - dialogPreview.noteTime < 5000) {
                return
            }
            dialogPreview.noteTime = 0L
            dialogPreview.noteState!!.setVisibility(0)
        }
    }
    internal inner class GlideUrlForTask : Runnable {
        internal inner class LoadImageStep : Runnable {
            override fun run() {
                val dialogPreview = this@DialogPreview
                val imageUrlIn = dialogPreview.imageUrlIn
                dialogPreview.imageUrlIn = null
                if (dialogPreview.glide == null) {
                    return
                }
                val isNetworkImage = URLUtil.isNetworkUrl(imageUrlIn)
                val diskCacheStrategy = DiskCacheStrategy.NONE
                if (isNetworkImage) {
                    dialogPreview.imageFailed = true
                    (dialogPreview.glide!!.load(MainUtil.glideUrlFor(dialogPreview.appCtx, imageUrlIn!!, dialogPreview.referer))
                        .diskCacheStrategy(diskCacheStrategy)).anyListener(dialogPreview.imageListener)
                        .into(dialogPreview.previewImage!!)
                } else {
                    dialogPreview.imageFailed = false
                    (dialogPreview.glide!!.load(imageUrlIn).diskCacheStrategy(diskCacheStrategy)).anyListener(
                        dialogPreview.imageListener
                    ).into(dialogPreview.previewImage!!)
                }
            }
        }
        override fun run() {
            val dialogPreview = this@DialogPreview
            val mainActivity = dialogPreview.hostActivity
            if (mainActivity != null) {
                if (dialogPreview.glide == null) {
                    dialogPreview.glide = GlideApp.with(mainActivity)
                }
                val handler = dialogPreview.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadImageStep())
            }
        }
    }
    internal inner class PlayVideoLoadListener : RequestListener<Any?> {
        internal inner class PlayVideoStep : Runnable {
            override fun run() {
                val dialogPreview = this@DialogPreview
                dialogPreview.playVideo(dialogPreview.mediaUrl, dialogPreview.videoName)
            }
        }
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.previewImage == null) {
                return true
            }
            if (!dialogPreview.imageFailed || TextUtils.isEmpty(dialogPreview.referer)) {
                dialogPreview.hideNote()
                dialogPreview.previewImage!!.setScaleType(ScaleType.CENTER_INSIDE)
                dialogPreview.previewImage!!.setImageResource(AppR.drawable.outline_error_dark_web_48)
                return true
            }
            val isApi = MainConst.isApi28
            dialogPreview.referer = null
            dialogPreview.previewImage!!.post(PlayVideoStep())
            return true
        }
        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.previewImage != null) {
                dialogPreview.hideNote()
                dialogPreview.previewImage!!.setScaleType(ScaleType.FIT_CENTER)
                dialogPreview.releaseVideoZoom()
            }
            return false
        }
    }
    internal inner class LoadMediaTask : Runnable {
        internal inner class LoadMediaStep : Runnable {
            override fun run() {
                val dialogPreview = this@DialogPreview
                val mediaUrlCopy = dialogPreview.mediaUrlCopy
                dialogPreview.mediaUrlCopy = null
                if (dialogPreview.glide == null) {
                    return
                }
                if (!URLUtil.isNetworkUrl(mediaUrlCopy)) {
                    dialogPreview.imageFailed = false
                    ((dialogPreview.glide!!.`as`(PictureDrawable::class.java)).load(mediaUrlCopy)).anyListener(
                        dialogPreview.thumbListener
                    ).into(dialogPreview.previewImage!!)
                } else {
                    dialogPreview.imageFailed = true
                    ((dialogPreview.glide!!.`as`(PictureDrawable::class.java)).load(
                        MainUtil.glideUrlFor(dialogPreview.appCtx, mediaUrlCopy!!, dialogPreview.referer)
                    )).anyListener(dialogPreview.thumbListener).into(dialogPreview.previewImage!!)
                }
            }
        }
        override fun run() {
            val dialogPreview = this@DialogPreview
            val mainActivity = dialogPreview.hostActivity
            if (mainActivity != null) {
                if (dialogPreview.glide == null) {
                    dialogPreview.glide = GlideApp.with(mainActivity)
                }
                val handler = dialogPreview.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(LoadMediaStep())
            }
        }
    }
    internal inner class HideNoteLoadListener : RequestListener<Any?> {
        internal inner class ReloadMediaStep : Runnable {
            override fun run() {
                val dialogPreview = this@DialogPreview
                dialogPreview.mediaUrlCopy = dialogPreview.mediaUrl
                dialogPreview.runInBackground(dialogPreview.LoadMediaTask())
            }
        }
        override fun onLoadFailed(glideException: GlideException?, model: Any?, target: com.bumptech.glide.request.target.Target<Any?>, isFirstResource: Boolean): Boolean {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.previewImage == null) {
                return true
            }
            if (dialogPreview.imageFailed && !TextUtils.isEmpty(dialogPreview.referer)) {
                val isApi = MainConst.isApi28
                dialogPreview.referer = null
                dialogPreview.previewImage!!.post(ReloadMediaStep())
                return true
            }
            dialogPreview.hideNote()
            dialogPreview.previewImage!!.setLayerType(View.LAYER_TYPE_NONE, null)
            dialogPreview.previewImage!!.setScaleType(ScaleType.CENTER_INSIDE)
            dialogPreview.previewImage!!.setImageResource(AppR.drawable.outline_error_dark_web_48)
            return true
        }
        override fun onResourceReady(resource: Any?, model: Any, target: com.bumptech.glide.request.target.Target<Any?>?, dataSource: com.bumptech.glide.load.DataSource, isFirstResource: Boolean): Boolean {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.previewImage != null) {
                dialogPreview.hideNote()
                dialogPreview.previewImage!!.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                dialogPreview.previewImage!!.setScaleType(ScaleType.FIT_CENTER)
                dialogPreview.releaseVideoZoom()
            }
            return false
        }
    }


    internal inner class ApplyZoomSettingsTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            val webPageUrl = dialogPreview.webPageUrl
            dialogPreview.webPageUrl = null
            val myWebSafe = dialogPreview.webSafe
            if (myWebSafe != null) {
                applyZoomSettings(myWebSafe, true)
                dialogPreview.webSafe!!.setWebViewClient(dialogPreview.LocalWebViewClient())
                dialogPreview.webSafe!!.setDownloadListener(dialogPreview.PageDownloadListener())
                dialogPreview.webUrlIn = webPageUrl
                val handler = dialogPreview.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogPreview.LoadWebUrlTask())
            }
        }
    }
    internal inner class PageDownloadListener : DownloadListener {
        override fun onDownloadStart(
            url: String?,
            userAgent: String?,
            contentDisposition: String?,
            mimeType: String?,
            contentLength: Long
        ) {
            var mimeType = mimeType
            val dialogPreview = this@DialogPreview
            val myWebSafe = dialogPreview.webSafe
            if (myWebSafe != null) {
                var ext: String? = null
                myWebSafe.setDownloadListener(null)
                if (TextUtils.isEmpty(url)) {
                    return
                }
                if (url != dialogPreview.pageUrl) {
                    dialogPreview.pageUrl = url
                    if (TextUtils.isEmpty(mimeType)) {
                        ext = fileExtOf(url, false)
                        mimeType = MainUtil.mimeForExt(ext)
                    }
                    if (!TextUtils.isEmpty(mimeType) && mimeType!!.startsWith("video")) {
                        dialogPreview.showNote()
                        dialogPreview.webSafe!!.loadUrl(MainUtil.htmlDataUri(url, true)!!)
                        return
                    }
                }
                dialogPreview.downUrl = url
                dialogPreview.downExt = ext
                dialogPreview.downMime = mimeType
                val handler = dialogPreview.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogPreview.StartDownloadTask())
            }
        }
    }
    internal inner class LoadWebUrlTask : Runnable {
        override fun run() {
            val referer: String?
            val mapU: HashMap<String, String>?
            val myWebSafe: MyWebSafe?
            val dialogPreview = this@DialogPreview
            val webUrlIn = dialogPreview.webUrlIn
            dialogPreview.webUrlIn = null
            if (dialogPreview.webSafe != null) {
                val myFadeFrame = dialogPreview.fadeFrame
                if (myFadeFrame != null) {
                    myFadeFrame.showNow(false)
                }
                dialogPreview.showNote()
                val host = MainUtil.hostOf(webUrlIn!!, true)
                if (TextUtils.isEmpty(host) || !(host!!.endsWith("dcinside.com") || host.endsWith(
                        "dcinside.co.kr"
                    ))
                ) {
                    referer = dialogPreview.referer
                    if (dialogPreview.webSafe != null) {
                        mapU = MainUtil.requestHeadersFor(dialogPreview.appCtx, webUrlIn, referer)
                        myWebSafe = dialogPreview.webSafe
                        if (mapU != null) {
                            myWebSafe!!.loadUrl(webUrlIn, mapU)
                        } else {
                            myWebSafe!!.loadUrl(webUrlIn)
                        }
                    }
                } else {
                    val length = host.length
                    if (!webUrlIn.startsWith("viewmovie", length + 1) || webUrlIn.indexOf(
                            ".php",
                            length + 10
                        ) == -1 || webUrlIn.lastIndexOf("&type=mp4", webUrlIn.length - 4) == -1
                    ) {
                        referer = dialogPreview.referer
                        if (dialogPreview.webSafe != null) {
                            mapU = MainUtil.requestHeadersFor(dialogPreview.appCtx, webUrlIn, referer)
                            myWebSafe = dialogPreview.webSafe
                            if (mapU != null) {
                                myWebSafe!!.loadUrl(webUrlIn, mapU)
                            } else {
                                myWebSafe!!.loadUrl(webUrlIn)
                            }
                        }
                    } else {
                        dialogPreview.webSafe!!.loadUrl(MainUtil.htmlDataUri(webUrlIn, true)!!)
                    }
                }
                if (dialogPreview.gestureDetect != null) {
                    return
                }
                val zoomImageAttacher = dialogPreview.imageZoom
                if (zoomImageAttacher != null) {
                    zoomImageAttacher.detach()
                    dialogPreview.imageZoom = null
                }
                val zoomVideoAttacher = dialogPreview.videoZoom
                if (zoomVideoAttacher != null) {
                    zoomVideoAttacher.detach()
                    dialogPreview.videoZoom = null
                }
                dialogPreview.gestureDetect =
                    GestureDetector(dialogPreview.appCtx, dialogPreview.TapGestures())
            }
        }
    }
    internal inner class StartDownloadTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            val downUrl = dialogPreview.downUrl
            val downExt = dialogPreview.downExt
            val downMime = dialogPreview.downMime
            dialogPreview.downUrl = null
            dialogPreview.downExt = null
            dialogPreview.downMime = null
            if (dialogPreview.webSafe != null) {
                dialogPreview.releaseWeb()
                dialogPreview.htmlUrlIn = downUrl
                dialogPreview.htmlRefererIn = downExt
                dialogPreview.htmlMimeIn = downMime
                val handler = dialogPreview.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogPreview.ShowPreviewMediaTask())
            }
        }
    }
    internal inner class ShowPreviewMediaTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            val htmlUrlIn = dialogPreview.htmlUrlIn
            var referer = dialogPreview.htmlRefererIn
            var mime = dialogPreview.htmlMimeIn
            dialogPreview.htmlUrlIn = null
            dialogPreview.htmlRefererIn = null
            dialogPreview.htmlMimeIn = null
            if (dialogPreview.mediaArea == null) {
                return
            }
            if (TextUtils.isEmpty(mime)) {
                if (TextUtils.isEmpty(referer)) {
                    referer = fileExtOf(htmlUrlIn, false)
                }
                mime = MainUtil.mimeForExt(referer)
            }
            if (TextUtils.isEmpty(mime) || !mime!!.startsWith("image")) {
                dialogPreview.showVideo(htmlUrlIn)
            } else {
                dialogPreview.playVideo(htmlUrlIn, referer)
            }
        }
    }
    internal inner class LoadAudioUrlTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            val audioUrlIn = dialogPreview.audioUrlIn
            dialogPreview.audioUrlIn = null
            if (dialogPreview.webSafe != null) {
                val myFadeFrame = dialogPreview.fadeFrame
                if (myFadeFrame != null) {
                    myFadeFrame.setAutoHide(false)
                    dialogPreview.fadeFrame!!.setVisibility(0)
                }
                val myButtonImage = dialogPreview.openPart
                if (myButtonImage != null) {
                    myButtonImage.setVisibility(8)
                }
                // audio: the web player becomes a bar at the bottom, a music note in the middle
                dialogPreview.contentArea?.audio = true
                if (dialogPreview.webPaused == null) dialogPreview.webPaused = true
                dialogPreview.audioUrl = audioUrlIn
                val handler = dialogPreview.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogPreview.PlayAudioTask())
            }
        }
    }
    internal inner class PlayAudioTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            val audioUrl = dialogPreview.audioUrl
            dialogPreview.audioUrl = null
            val myWebSafe = dialogPreview.webSafe
            if (myWebSafe != null) {
                applyZoomSettings(myWebSafe, true)
                dialogPreview.webSafe!!.setWebViewClient(dialogPreview.LocalWebViewClient())
                dialogPreview.webUrlOut = audioUrl
                val handler = dialogPreview.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogPreview.LoadWebOutTask())
            }
        }
    }
    internal inner class LoadWebOutTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            val webUrlOut = dialogPreview.webUrlOut
            dialogPreview.webUrlOut = null
            val myWebSafe = dialogPreview.webSafe
            if (myWebSafe == null) {
                return
            }
            myWebSafe.loadUrl(MainUtil.htmlDataUri(webUrlOut, false)!!)
        }
    }
    internal inner class UpdateBaseMatrixStep : PlayerViewListener {
        override fun onVideoSize(width: Int, height: Int) {
            val zoomVideoAttacher = this@DialogPreview.videoZoom
            if (zoomVideoAttacher != null) {
                zoomVideoAttacher.updateBaseMatrix(width, height, 0, true)
            }
        }
        override fun onPlaying(playing: Boolean) {
            val dialogPreview = this@DialogPreview
            if (playing) {
                dialogPreview.showNote()
                return
            }
            dialogPreview.hideNote()
            if (dialogPreview.seekVisible) {
                return
            }
            dialogPreview.seekVisible = true
            val videoSeekControl = dialogPreview.seekControl
            if (videoSeekControl != null) {
                videoSeekControl.refreshLater()
            }
        }
        override fun onProgress() {
            val videoSeekControl = this@DialogPreview.seekControl
            if (videoSeekControl != null) {
                videoSeekControl.refreshLater()
            }
        }
        override fun onCompleted() {
            val dialogPreview = this@DialogPreview
            dialogPreview.hideNote()
            dialogPreview.releasePlayer()
            val handler = dialogPreview.mainHandler
            if (handler == null) {
                return
            }
            handler.postDelayed(dialogPreview.ShowVideoTask(), 400L)
        }
    }
    internal inner class AddSeekControlTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.playerView == null || dialogPreview.fadeFrame == null || dialogPreview.seekControl != null) {
                return
            }
            dialogPreview.seekControl = VideoSeekControl(dialogPreview.appCtx ?: return)
            dialogPreview.seekShown = true
            val handler = dialogPreview.mainHandler
            if (handler != null) {
                handler.post(dialogPreview.StartSeekControlTask())
            }
        }
    }
    internal inner class CloseClick : View.OnClickListener {
        override fun onClick(view: View?) {
            this@DialogPreview.dismiss()
        }
    }
    internal inner class StartSeekControlTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            val videoSeekControl = dialogPreview.seekControl
            if (videoSeekControl == null) {
                return
            }
            videoSeekControl.start(dialogPreview.appCtx, dialogPreview.SeekControlListener())
        }
    }
    internal inner class SeekControlListener : SeekCtrlListener {
        override fun isZoomed(): Boolean {
            val zoomVideoAttacher = this@DialogPreview.videoZoom
            if (zoomVideoAttacher == null) {
                return false
            }
            return zoomVideoAttacher.handledTouch
        }
        override fun seekTo(position: Int, isFinal: Boolean) {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.playerView != null) {
                if (!isFinal) {
                    dialogPreview.seekTo(position)
                    return
                }
                dialogPreview.seekPos = position
                if (dialogPreview.touchMoved) {
                    return
                }
                val runnable = dialogPreview.hideRunnable
                dialogPreview.seekValue = position
                val frameLayout = dialogPreview.mediaArea
                if (frameLayout != null) {
                    frameLayout.removeCallbacks(runnable)
                    dialogPreview.mediaArea!!.post(runnable)
                }
                dialogPreview.seekPos = -1
            }
        }
        override fun pause() {
            val dialogPreview = this@DialogPreview
            val myPlayerView = dialogPreview.playerView
            if (myPlayerView == null) {
                return
            }
            myPlayerView.playPause(false, true)
            dialogPreview.hideNote()
        }
        override fun position(): Int {
            val myPlayerView = this@DialogPreview.playerView
            if (myPlayerView == null) {
                return 0
            }
            return myPlayerView.position
        }
        override fun duration(): Int {
            val myPlayerView = this@DialogPreview.playerView
            if (myPlayerView == null) {
                return 0
            }
            return myPlayerView.duration
        }
        override fun isPlaying(): Boolean {
            val myPlayerView = this@DialogPreview.playerView
            if (myPlayerView != null) {
                val mediaPlayer = myPlayerView.player
                if (if (mediaPlayer == null) false else myPlayerView.prepared) {
                    return mediaPlayer!!.isPlaying()
                }
            }
            return false
        }
        override fun play() {
            val myPlayerView = this@DialogPreview.playerView
            if (myPlayerView == null) {
                return
            }
            myPlayerView.playPause(true, true)
        }
    }
    internal inner class ShowVideoTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            if (!dialogPreview.playerStarted) {
                dialogPreview.playerStarted = true
                dialogPreview.currentUrl = null
                dialogPreview.showVideo(dialogPreview.mediaUrl)
                return
            }
            val loadedOnce = dialogPreview.loadedOnce
            val mediaUrl = dialogPreview.mediaUrl
            if (loadedOnce) {
                dialogPreview.showWeb(mediaUrl)
                return
            }
            val fileExt = fileExtOf(mediaUrl, false)
            val mime = MainUtil.mimeForExt(fileExt)
            if (TextUtils.isEmpty(mime) || !mime!!.startsWith("image")) {
                dialogPreview.showWeb(dialogPreview.mediaUrl)
            } else {
                dialogPreview.mimeType = mime
                dialogPreview.playVideo(dialogPreview.mediaUrl, fileExt)
            }
        }
    }
    internal inner class ToggleStep : AttacherListener {
        override fun onMatrixChanged(rectF: RectF?, isZoomed: Boolean) {
        }
        override fun onTouch(motionEvent: MotionEvent?, isDown: Boolean) {
            val found: Boolean
            val dialogPreview = this@DialogPreview
            val zoomImageAttacher = dialogPreview.imageZoom
            if (zoomImageAttacher == null) {
                found = true
            } else {
                val rectF = zoomImageAttacher.displayRect
                if (rectF.top <= -1.0f) {
                    found = false
                } else {
                    found = true
                }
            }
            dialogPreview.setSheetDrag(found)
        }
        override fun onTap(): Boolean {
            val dialogPreview = this@DialogPreview
            val myFadeFrame: Fade? = if (dialogPreview.mediaType.toInt() == 4) dialogPreview.fadeFrame else null
            if (dialogPreview.mediaType.toInt() == 4 && myFadeFrame != null) {
                myFadeFrame.toggle(!myFadeFrame.isShown())
            }
            return true
        }
        override fun canScale(): Boolean {
            return false
        }
        override fun onDoubleTap() {
        }
    }
    internal inner class VideoToggleStep : VideoAttacherListener {
        override fun onMatrixChanged(rectF: RectF?) {
        }
        override fun onTap(): Boolean {
            val dialogPreview = this@DialogPreview
            val myFadeFrame: Fade? = if (dialogPreview.playerView != null) dialogPreview.fadeFrame else null
            if (dialogPreview.playerView != null && myFadeFrame != null) {
                myFadeFrame.toggle(!myFadeFrame.isShown())
            }
            return true
        }
        override fun canScale(): Boolean {
            return false
        }
        override fun onDoubleTap() {
        }
        override fun onTouch(motionEvent: MotionEvent?) {
            val found: Boolean
            val dialogPreview = this@DialogPreview
            val zoomVideoAttacher = dialogPreview.videoZoom
            if (zoomVideoAttacher == null) {
                found = true
            } else {
                val rectF = zoomVideoAttacher.displayRect
                if (rectF.top <= -1.0f) {
                    found = false
                } else {
                    found = true
                }
            }
            dialogPreview.setSheetDrag(found)
        }
    }
    internal inner class TapGestures : SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val dialogPreview = this@DialogPreview
            val myFadeFrame: Fade? = if (dialogPreview.mediaType.toInt() != 6) dialogPreview.fadeFrame else null
            if (dialogPreview.mediaType.toInt() != 6 && myFadeFrame != null) {
                myFadeFrame.toggle(!myFadeFrame.isShown())
            }
            return true
        }
    }
    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            MainUtil.clearWebCache(dialogPreview.webSafe)
            dialogPreview.finishHandled = false
        }
    }
    internal inner class DismissTask : Runnable {
        override fun run() {
            this@DialogPreview.dismiss()
        }
    }
    internal inner class RestartHideTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            val seekValue = dialogPreview.seekValue
            val myFadeFrame = dialogPreview.fadeFrame
            if (myFadeFrame != null) {
                myFadeFrame.restartHide()
                if (seekValue >= 0) {
                    dialogPreview.seekTarget = seekValue
                    val frameLayout = dialogPreview.mediaArea
                    if (frameLayout == null) {
                        return
                    }
                    frameLayout.postDelayed(dialogPreview.SeekToTask(), 100L)
                }
            }
        }
    }
    internal inner class SeekToTask : Runnable {
        override fun run() {
            val dialogPreview = this@DialogPreview
            dialogPreview.seekTo(dialogPreview.seekTarget)
        }
    }


    internal inner class HideNowClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogPreview = this@DialogPreview
            val myFadeFrame: Fade? = if (dialogPreview.mediaType.toInt() != 6) dialogPreview.fadeFrame else null
            if (dialogPreview.mediaType.toInt() != 6 && myFadeFrame != null) {
                myFadeFrame.hideNow(true)
            }
            val previewListener = dialogPreview.listener
            if (previewListener != null) {
                previewListener.openWith(dialogPreview.pageUrl, dialogPreview.mimeType)
            }
        }
    }
    internal inner class DownloadClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogPreview = this@DialogPreview
            val myFadeFrame: Fade? = if (dialogPreview.mediaType.toInt() != 6) dialogPreview.fadeFrame else null
            if (dialogPreview.mediaType.toInt() != 6 && myFadeFrame != null) {
                myFadeFrame.hideNow(true)
            }
            val previewListener = dialogPreview.listener
            if (previewListener != null) {
                previewListener.download(dialogPreview.mediaUrl, dialogPreview.mimeType)
            }
        }
    }
    internal inner class ShareClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogPreview = this@DialogPreview
            val myFadeFrame: Fade? = if (dialogPreview.mediaType.toInt() != 6) dialogPreview.fadeFrame else null
            if (dialogPreview.mediaType.toInt() != 6 && myFadeFrame != null) {
                myFadeFrame.hideNow(true)
            }
            val previewListener = dialogPreview.listener
            if (previewListener != null) {
                previewListener.share(dialogPreview.mediaUrl)
            }
        }
    }
    internal inner class ShowNowClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogPreview = this@DialogPreview
            val myFadeFrame: Fade? = if (dialogPreview.mediaType.toInt() != 6) dialogPreview.fadeFrame else null
            if (dialogPreview.mediaType.toInt() != 6 && myFadeFrame != null) {
                myFadeFrame.showNow(true)
            }
            val previewListener = dialogPreview.listener
            if (previewListener != null) {
                previewListener.copyUrl(dialogPreview.mediaUrl)
            }
        }
    }
    internal inner class ActionClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.mediaType.toInt() != 6) {
                val myFadeFrame = dialogPreview.fadeFrame
                if (myFadeFrame != null) {
                    myFadeFrame.hideNow(true)
                }
                val previewListener = dialogPreview.listener
                if (previewListener == null) {
                    return
                }
                previewListener.openFile(dialogPreview.mediaUrl, 0L, dialogPreview.mediaType.toInt() == 4)
            }
        }
    }

    interface PreviewListener {
        fun copyUrl(url: String?)

        fun openFile(url: String?, size: Long, isVideo: Boolean)

        fun download(url: String?, mimeType: String?)

        fun share(url: String?)

        fun openWith(url: String?, mimeType: String?)
    }

    fun hideNote() {
        if (this.noteShown) {
            this.noteShown = false
            this.noteTime = 0L
            val appCompatTextView = this.noteState
            if (appCompatTextView != null) {
                appCompatTextView.setVisibility(8)
            }
            val myCoverView = this.coverState
            if (myCoverView != null) {
                myCoverView.setBackground(null)
                this.coverState!!.hide(true)
            }
        }
    }

    fun updateLayout(landscape: Boolean) {
        var landscape = landscape
        if (this.contentArea == null || this.mediaType.toInt() == 6) {
            return
        }
        if (landscape) {
            landscape = (hostActivity as? MainActivity)?.smallScreen ?: MainUtil.isSmallScreen(context)
        }
        val area = this.contentArea!!
        if (landscape) {
            area.heightDp = 280
            val zoomImageAttacher = this.imageZoom
            if (zoomImageAttacher != null) {
                zoomImageAttacher.resetMatrix()
                return
            }
            return
        }
        area.heightDp = 320
        val zoomImageAttacher = this.imageZoom
        if (zoomImageAttacher != null) {
            zoomImageAttacher.resetMatrix()
        }
    }

    fun pauseMedia() {
        val myWebSafe = this.webSafe
        if (myWebSafe != null) {
            myWebSafe.onPause()
        }
        val myPlayerView = this.playerView
        if (myPlayerView != null) {
            myPlayerView.stop()
        }
    }

    fun releasePlayer() {
        val myPlayerView = this.playerView
        if (myPlayerView != null) {
            myPlayerView.stop()
            clearHandler(myPlayerView.mainHandler)
            myPlayerView.mainHandler = null
            myPlayerView.ctx = null
            myPlayerView.playerListener = null
            myPlayerView.mediaUri = null
            myPlayerView.referer = null
            myPlayerView.userAgentText = null
            val frameLayout = this.contentArea
            if (frameLayout != null) {
                frameLayout.removeView(this.playerView)
            }
            this.playerView = null
        }
        val videoSeekControl = this.seekControl
        if (videoSeekControl != null) {
            videoSeekControl.release()
            this.seekControl = null
            seekShown = false
        }
    }

    fun releaseWeb() {
        val myWebSafe = this.webSafe
        if (myWebSafe != null) {
            if (this.webLoading) {
                this.webLoading = false
                myWebSafe.stopLoading()
            }
            destroyWebViewNow(this.webSafe)
            this.webSafe = null
        }
        if (this.webPaused != null) {
            this.contentArea?.audio = false
            this.webPaused = null
        }
    }

    fun resumeMedia() {
        val myWebSafe = this.webSafe
        if (myWebSafe != null) {
            myWebSafe.onResume()
        }
        val myPlayerView = this.playerView
        if (myPlayerView != null) {
            myPlayerView.open(myPlayerView.mediaUri, myPlayerView.referer, myPlayerView.userAgentText)
        }
    }

    fun releaseVideoZoom() {
        if (this.imageZoom != null || this.previewImage == null) {
            return
        }
        val zoomVideoAttacher = this.videoZoom
        if (zoomVideoAttacher != null) {
            zoomVideoAttacher.detach()
            this.videoZoom = null
        }
        this.gestureDetect = null
        this.imageZoom = ZoomImageAttacher(this.previewImage!!, ToggleStep())
    }

    fun playVideo(mediaUrlCopy: String?, videoName: String?) {
        if (this.mediaArea == null) {
            return
        }
        this.videoName = videoName
        this.mediaType = 4.toByte()
        releasePlayer()
        releaseWeb()
        if (this.previewImage == null) {
            val imageView = ImageView(this.hostActivity)
            this.previewImage = imageView
            this.contentArea!!.addView(imageView, 0)
        }
        val myFadeFrame = this.fadeFrame
        if (myFadeFrame != null) {
            myFadeFrame.showNow(false)
        }
        showNote()
        if (isSvgName(guessDownloadName(mediaUrlCopy, null, null, true))) {
            this.mediaUrlCopy = mediaUrlCopy
            runInBackground(LoadMediaTask())
        } else {
            this.imageUrlIn = mediaUrlCopy
            runInBackground(GlideUrlForTask())
        }
    }

    fun seekTo(seekPos: Int) {
        val myPlayerView: MyPlayerView? = if (seekPos < 0) null else this.playerView
        if (seekPos < 0 || myPlayerView == null) {
            return
        }
        this.seekPos = -1
        if (myPlayerView.duration != 0) {
            myPlayerView.seekPos = seekPos
            myPlayerView.player!!.seekTo(seekPos)
        }
        val myCoverView = this.coverState
        if (myCoverView != null) {
            this.noteShown = true
            myCoverView.setBackground(null)
            this.coverState!!.show(true)
        }
    }

    fun showVideo(input: String?) {
        if (this.mediaArea != null) {
            if (!TextUtils.isEmpty(this.currentUrl) && this.currentUrl == input) {
                hideNote()
                return
            }
            this.currentUrl = input
            this.mediaType = 5.toByte()
            val imageView = this.previewImage
            if (imageView != null) {
                val frameLayout = this.contentArea
                if (frameLayout != null) {
                    frameLayout.removeView(imageView)
                }
                this.previewImage = null
            }
            releaseWeb()
            if (this.playerView == null) {
                val myPlayerView = MyPlayerView(this.hostActivity!!)
                this.playerView = myPlayerView
                this.contentArea!!.addView(myPlayerView, 0)
            }
            this.seekVisible = false
            this.seekPos = -1
            this.playerView!!.setListener(UpdateBaseMatrixStep())
            if (this.videoZoom == null && this.playerView != null) {
                val zoomImageAttacher = this.imageZoom
                if (zoomImageAttacher != null) {
                    zoomImageAttacher.detach()
                    this.imageZoom = null
                }
                this.gestureDetect = null
                this.videoZoom = ZoomVideoAttacher(this.playerView!!, VideoToggleStep())
            }
            val referer = if (this.playerStarted) null else this.referer
            val myPlayerView = this.playerView
            myPlayerView!!.javaClass
            if (!TextUtils.isEmpty(input)) {
                myPlayerView.open(Uri.parse(input), input, referer)
            }
            val handler: Handler? = if (this.seekControl != null) null else this.mainHandler
            if (this.seekControl != null || handler == null) {
                return
            }
            handler.post(AddSeekControlTask())
        }
    }

    fun showWeb(webPageUrl: String?) {
        if (this.mediaArea != null) {
            if ("blob:" == this.mimeType) {
                this.mimeType = "image/*"
                this.mediaType = 4.toByte()
            } else {
                this.mediaType = 5.toByte()
            }
            releasePlayer()
            val imageView = this.previewImage
            if (imageView != null) {
                val frameLayout = this.contentArea
                if (frameLayout != null) {
                    frameLayout.removeView(imageView)
                }
                this.previewImage = null
            }
            if (this.webSafe == null) {
                val myWebSafe = MyWebSafe(this.hostActivity!!)
                this.webSafe = myWebSafe
                MainApp.resumeWebTimers(this.appCtx, myWebSafe)
                this.webSafe!!.setBackgroundColor(-16777216)
                this.contentArea!!.addView(this.webSafe, 0)
            }
            this.webPageUrl = webPageUrl
            val handler = this.mainHandler
            if (handler == null) {
                return
            }
            handler.post(ApplyZoomSettingsTask())
        }
    }

    fun showNote() {
        if (this.coverState == null) {
            return
        }
        this.noteShown = true
        this.noteTime = System.currentTimeMillis()
        this.coverState!!.setBackgroundColor(-16777216)
        this.coverState!!.setForeSize(MainApp.dp24)
        this.coverState!!.show(true)
        this.coverState!!.postDelayed(ShowNoteLaterTask(), 5000L)
    }
    public override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (this.fadeFrame != null) {
            val actionMasked = event.getActionMasked()
            if (actionMasked == 0) {
                this.seekPos = -1
                this.touchMoved = true
            } else if (actionMasked == 1 || actionMasked == 3) {
                this.touchMoved = false
                this.seekValue = this.seekPos
                val frameLayout = this.mediaArea
                if (frameLayout != null) {
                    val runnable = this.hideRunnable
                    frameLayout.removeCallbacks(runnable)
                    this.mediaArea!!.post(runnable)
                }
                this.seekPos = -1
            }
        }
        val gestureDetector = this.gestureDetect
        if (gestureDetector != null) {
            gestureDetector.onTouchEvent(event)
        }
        return super.dispatchTouchEvent(event)
    }

    private inner class LocalWebViewClient : WebViewClient() {
        override fun onPageFinished(webView: WebView?, url: String?) {
            val dialogPreview = this@DialogPreview
            if (dialogPreview.webSafe == null) {
                return
            }
            dialogPreview.webLoading = false
            if (!dialogPreview.finishHandled && !TextUtils.isEmpty(url) && (url != dialogPreview.lastFinishedUrl)) {
                dialogPreview.finishHandled = true
                dialogPreview.lastFinishedUrl = url
                val handler = dialogPreview.mainHandler
                if (handler != null) {
                    handler.post(dialogPreview.ClearCacheTask())
                }
            }
            val myWebSafe: MyWebSafe? = if (dialogPreview.mediaType.toInt() == 5) dialogPreview.webSafe else null
            if (dialogPreview.mediaType.toInt() == 5 && myWebSafe != null) {
                evalJsNow(
                    myWebSafe,
                    "(function(){var ele=document.querySelector(\"video\");if(ele){ele.muted=true;ele.loop=true;ele.style.width='100%';ele.setAttribute('controlsList','nodownload');if(ele.paused){ele.play();}}})();",
                    false
                )
            }
            dialogPreview.hideNote()
        }
        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val dialogPreview = this@DialogPreview
            val myWebSafe = dialogPreview.webSafe
            if (myWebSafe == null) {
                return
            }
            dialogPreview.webLoading = true
            if (dialogPreview.mediaType.toInt() == 5) {
                evalJsNow(
                    myWebSafe,
                    "(function(){var ele=document.querySelector(\"video\");if(ele){ele.muted=true;ele.loop=true;ele.style.width='100%';ele.setAttribute('controlsList','nodownload');if(ele.paused){ele.play();}}})();",
                    false
                )
            }
            dialogPreview.hideNote()
        }
        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val dialogPreview = this@DialogPreview
            dialogPreview.webSafe = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = dialogPreview.mainHandler
            if (handler == null) {
                return true
            }
            handler.post(dialogPreview.DismissTask())
            return true
        }
        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            return false
        }
    }

    override fun dismiss() {
        if (appCtx == null) {
            super.dismiss()
            return
        }
        val requests = glide
        if (requests != null) {
            previewImage?.let { requests.clear(it) }
            glide = null
        }
        val player = playerView
        if (player != null) {
            player.stop()
            clearHandler(player.mainHandler)
            player.mainHandler = null
            player.ctx = null
            player.playerListener = null
            player.mediaUri = null
            player.referer = null
            player.userAgentText = null
            playerView = null
        }
        seekControl?.release()
        seekControl = null
        val web = webSafe
        if (web != null) {
            if (webLoading) {
                webLoading = false
                web.stopLoading()
            }
            destroyWebViewNow(webSafe)
            webSafe = null
        }
        coverState?.reset()
        coverState = null
        fadeFrame?.cancelHide()
        fadeFrame = null
        guideState?.reset()
        guideState = null
        imageZoom?.detach()
        imageZoom = null
        videoZoom?.detach()
        videoZoom = null
        hostActivity = null
        appCtx = null
        listener = null
        pageUrl = null
        mediaUrl = null
        referer = null
        mimeType = null
        mediaArea = null
        contentArea = null
        previewImage = null
        webPaused = null
        noteState = null
        gestureDetect = null
        videoName = null
        currentUrl = null
        super.dismiss()
    }

    /**
     * The media: the current surface through `AndroidView`, the music note for audio, the slow-server note, the
     * loading cover and the pinch guide (whose first touch is seen in the early pass, so it still reaches the media).
     */
    @Composable
    private fun MediaBox(media: MediaArea, note: NoteState?, cover: CoverState?) {
        val guide = guideState
        Box(
            Modifier.fillMaxWidth().height(media.heightDp.dp).pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.type == PointerEventType.Press && guideState?.shown == true) guideTouched()
                    }
                }
            }
        ) {
            for (view in media.surfaces) {
                key(view) {
                    val bar = media.audio && view === webSafe
                    AndroidView(
                        factory = { view },
                        modifier = if (bar) Modifier.fillMaxWidth().wrapContentHeight().align(Alignment.BottomCenter) else Modifier.fillMaxSize(),
                    )
                }
            }
            if (media.audio) {
                Icon(
                    painterResource(AppR.drawable.baseline_music_note_dark_24), null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(with(LocalDensity.current) { MainApp.dp64.toDp() }).align(Alignment.Center).alpha(0.4f),
                )
            }
            if (note?.shown == true) {
                Text(
                    stringResource(AppR.string.server_delay), color = Color(-328966), fontSize = 14.sp, textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                )
            }
            if (cover?.shown == true) {
                Box(
                    Modifier.fillMaxSize().then(if (cover.black) Modifier.background(Color.Black) else Modifier),
                    contentAlignment = Alignment.Center,
                ) {
                    val px = with(LocalDensity.current) { MainApp.dp24.toDp() }
                    CircularProgressIndicator(Modifier.size(px), color = Color(-328966), strokeWidth = 2.dp)
                }
            }
            if (guide != null) PinchGuideCard(guide, onGone = { if (guideState === guide) guideGone() }, modifier = Modifier.matchParentSize())
        }
    }

    companion object {
    }
}
