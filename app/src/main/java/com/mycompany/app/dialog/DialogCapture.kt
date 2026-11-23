package com.mycompany.app.dialog

import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ReadSettings
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.RectF
import android.os.Handler
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.crop.CropImageView
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.applyDisplayCutout
import com.mycompany.app.main.MainUtil.applySystemUiFlags
import com.mycompany.app.main.MainUtil.clearStatusBarColor
import com.mycompany.app.main.MainUtil.toggleSystemBars
import com.mycompany.app.main.MainUtil.applySystemBarsMode
import com.mycompany.app.main.MainUtil.applyScreenRotation
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isSmallScreen
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.openWithApp
import com.mycompany.app.main.MainUtil.viewSnapshotScaled
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.main.MainUtil.insetsController
import com.mycompany.app.main.MainUtil.saveBitmap
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet.putBool
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.components.AreaOverview
import com.mycompany.app.view.MySizeImage
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.view.MySnackbar.SnackbarListener
import com.mycompany.app.zoom.ZoomImageAttacher
import com.mycompany.app.zoom.ZoomImageAttacher.AttacherListener
import java.lang.ref.WeakReference
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalDensity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.mycompany.app.ui.browser.ComposeFullDialog
import com.mycompany.app.pref.PrefSet

/**
 * Screenshot crop / save (browser > screenshot; [bitmap] the capture, [fullModeParam] = full page capture, [pageUrl] the page title):
 * a full-screen black page with the picture (visible part: a crop frame in [CropImageView]; full page: a zoomable
 * [MySizeImage] with the [AreaOverview] and fit / fit-width buttons), Cancel and "Crop & save". Saving goes
 * through [DialogDownEdit] (name, folder) and [SaveTask] (unchanged), then a snackbar. Full-page mode shows the
 * one-time pinch / scroll guide (`mGuideCap2`). System bars are hidden while it is open. Compose now (on
 * [ComposeFullDialog]); same constructor, shows itself; [onEditResult], [applyEditSize], [hideSystemBars], [imageHeight], [editShown] stay for the browser.
 */
@Suppress("deprecation")
class DialogCapture(mainActivity: MainActivity?, bitmap: Bitmap?, fullModeParam: Boolean, pageUrl: String?) : ComposeFullDialog(mainActivity) {
    /** Compose stand-in for old views that only changed visibility. */
    class UiPart(visible: Boolean) {
        var shown by mutableStateOf(visible)
        fun setVisibility(visibility: Int) { shown = visibility == 0 }
        fun show(dialogWebBookDir: Boolean) { shown = true }
        fun hide(fade: Boolean) { shown = false }
    }

    @JvmField val mainHandler: Handler? = Handler(android.os.Looper.getMainLooper())
    private var executor: java.util.concurrent.ExecutorService? = null

    @JvmField var pageUrl: String? = pageUrl
    /** Parent for the snackbar (the window's content frame). */
    @JvmField var contentRoot: ViewGroup? = null
    @JvmField var cropView: CropImageView? = null
    @JvmField var sizeImage: MySizeImage? = null
    @JvmField var zoomAttacher: ZoomImageAttacher? = null
    @JvmField var topBar: UiPart? = UiPart(true)
    @JvmField var loadCover: UiPart? = UiPart(false)
    @JvmField var loadFailed: Boolean = false
    @JvmField var saving: Boolean = false
    @JvmField var editDialog: DialogDownEdit? = null
    @JvmField var editShown: Boolean = false
    @JvmField var editBitmap: Bitmap? = null
    @JvmField var freeMem: Long = 0
    @JvmField var snackbar: MySnackbar? = null
    @JvmField var hostActivity: MainActivity? = mainActivity
    @JvmField var appContext: Context? = context
    @JvmField var fullMode: Boolean = fullModeParam
    @JvmField var fullBitmap: Bitmap? = if (fullModeParam) bitmap else null
    @JvmField var orientation: Byte = 0
    @JvmField var bitmapWidth: Int = 0
    @JvmField var bitmapHeight: Int = 0

    private var guide by mutableStateOf(false)

    /** Overview of a zoomed full-page capture (was a `MyAreaView`): the image view's size and its displayed rect. */
    private var areaSize by mutableStateOf<Pair<Int, Int>?>(null)
    private var areaRect by mutableStateOf<RectF?>(null)

    init {
        val ctx = context
        if (fullMode) {
            val image = MySizeImage(ctx)
            image.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            sizeImage = image
            if (MainUtil.isBitmapOk(bitmap)) {
                orientation = 0.toByte()
                bitmapWidth = bitmap!!.width
                this.bitmapHeight = bitmap.height
                image.setListener(CancelFlingStep())
                image.setImageBitmap(bitmap)
                zoomAttacher = ZoomImageAttacher(image, AreaZoomListener(), 0)
            } else {
                loadFailed = true
                image.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                image.setImageResource(AppR.drawable.outline_error_dark_web_48)
            }
        } else {
            val crop = CropImageView(ctx)
            val pad = MainApp.dp8
            crop.setPadding(0, pad, 0, pad)
            crop.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
            crop.adjustViewBounds = true
            cropView = crop
            if (MainUtil.isBitmapOk(bitmap)) {
                crop.setImageBitmap(bitmap)
            } else {
                loadFailed = true
                crop.scaleType = android.widget.ImageView.ScaleType.CENTER_INSIDE
                crop.setImageResource(AppR.drawable.outline_error_dark_web_48)
            }
        }
        val bar = topBar!!
        val cover = loadCover!!
        setPage {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                Column(Modifier.fillMaxSize().navigationBarsPadding()) {
                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        val main: View? = cropView ?: sizeImage
                        if (main != null) {
                            AndroidView(factory = { main.apply { layoutParams = ViewGroup.LayoutParams(-1, -1) } }, modifier = Modifier.fillMaxSize())
                        }
                        val area = areaSize
                        if (fullMode && area != null) {
                            val pad = with(LocalDensity.current) { 8.dp.roundToPx() }
                            AreaOverview(area.first, area.second, bitmapWidth, this@DialogCapture.bitmapHeight, areaRect, pad, Modifier.padding(8.dp))
                        }
                        Row(Modifier.align(Alignment.BottomEnd).padding(8.dp)) {
                            val colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color(0xA0000000.toInt()), contentColor = Color.White)
                            if (fullMode) {
                                FilledTonalIconButton(onClick = { zoomAttacher?.resetIfZoomed() }, colors = colors) {
                                    Icon(painterResource(AppR.drawable.outline_fullscreen_exit_white_24), null)
                                }
                            }
                            FilledTonalIconButton(onClick = { fit() }, colors = colors, modifier = Modifier.padding(start = 8.dp)) {
                                Icon(painterResource(AppR.drawable.outline_fullscreen_white_24), null)
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth().height(56.dp).background(Color.Black)) {
                        if (bar.shown) {
                            TextButton(onClick = { dismiss() }, modifier = Modifier.weight(1f).fillMaxSize()) {
                                Text(stringResource(AppR.string.cancel), color = Color.White, fontSize = 16.sp)
                            }
                            TextButton(onClick = { save() }, modifier = Modifier.weight(1f).fillMaxSize()) {
                                Text(stringResource(AppR.string.crop_save), color = Color.White, fontSize = 16.sp)
                            }
                        }
                    }
                }
                if (cover.shown) {
                    Box(Modifier.fillMaxSize().background(Color(0xA0000000.toInt())).clickable(enabled = true) { }, contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color.White)
                    }
                }
                if (guide) Guide()
            }
        }
        contentRoot = window?.decorView?.findViewById(android.R.id.content)
        setCanceledOnTouchOutside(false)
        show()
        hostActivity?.swapScreenOnView(contentRoot, true)
        hideSystemBars()
        if (fullMode && ReadSettings.guideCap.value) guide = true
    }

    /** The one-time pinch / scroll guide of the full-page mode; any tap clears it. */
    @androidx.compose.runtime.Composable
    private fun Guide() {
        Box(Modifier.fillMaxSize().clickable { closeGuide() }.padding(8.dp)) {
            Column(
                Modifier.align(Alignment.Center).width(132.dp).background(Color(0xCC000000.toInt()), RoundedCornerShape(16.dp)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(painterResource(AppR.drawable.outline_pinch), null, tint = Color.White, modifier = Modifier.size(84.dp))
                Text(stringResource(AppR.string.guide_pinch), color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
            }
            Column(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(Color(0xCC000000.toInt()), RoundedCornerShape(8.dp)).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(AppR.string.scroll_guide_1), color = Color.White, fontSize = 16.sp)
                Text(stringResource(AppR.string.scroll_guide_2), color = Color.White, fontSize = 16.sp, modifier = Modifier.padding(top = 16.dp))
            }
        }
    }

    private fun closeGuide() {
        if (ReadSettings.guideCap.value) {
            ReadSettings.edit(appContext) { set(ReadSettings.guideCap, false) }
        }
        guide = false
    }

    /** Fit button. */
    private fun fit() {
        val attacher = zoomAttacher
        if (attacher != null) {
            attacher.fitBounds()
            return
        }
        val crop = cropView ?: return
        val rect = crop.cropRect ?: return
        crop.resetCropRect(rect, true)
        crop.invalidate()
    }

    /** Crop & save. */
    private fun save() {
        if (loadFailed) {
            showToast(appContext, AppR.string.image_fail)
        } else {
            if (saving) return
            runAsync(CloseEditTask())
        }
    }

    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    internal inner class CloseEditTask : Runnable {
        internal inner class CloseEditStep : Runnable {
            override fun run() {
                val bitmapD: Bitmap?
                val dialogCapture = this@DialogCapture
                if (dialogCapture.hostActivity == null || dialogCapture.editDialog != null) {
                    return
                }
                dialogCapture.closeEdit()
                if (dialogCapture.cropView != null) {
                    bitmapD = dialogCapture.editBitmap
                } else {
                    val mySizeImage = dialogCapture.sizeImage
                    bitmapD = if (mySizeImage != null) MainUtil.viewSnapshotScaled(
                        mySizeImage,
                        0,
                        0.5f,
                        dialogCapture.freeMem,
                        null
                    ) else null
                }
                dialogCapture.editBitmap = null
                dialogCapture.topBar!!.setVisibility(4)
                dialogCapture.editShown = true
                val mainActivity = dialogCapture.hostActivity
                AppState.darkUi = true
                val dialogDownEdit = DialogDownEdit(
                    mainActivity,
                    dialogCapture.pageUrl,
                    bitmapD,
                    dialogCapture.SaveImageListener()
                )
                dialogCapture.editDialog = dialogDownEdit
                dialogDownEdit.setOnDismissListener(dialogCapture.DownEditDialogDismiss())
            }
        }
        override fun run() {
            val dialogCapture = this@DialogCapture
            val cropImageView = dialogCapture.cropView
            if (cropImageView != null) {
                dialogCapture.editBitmap = cropImageView.croppedImage
            } else if (dialogCapture.sizeImage != null) {
                dialogCapture.freeMem = MainUtil.freeMemory(dialogCapture.appContext!!)
            }
            val handler = dialogCapture.mainHandler
            if (handler == null) {
                return
            }
            handler.post(CloseEditStep())
        }
    }
    internal inner class SaveImageListener : DownPageListener {
        override fun onSave(url: String?, path: String?, bitmap: Bitmap?) {
            val dialogCapture = this@DialogCapture
            SaveTask(dialogCapture, path, bitmap).start(dialogCapture.appContext)
        }
    }
    internal inner class DownEditDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogCapture = this@DialogCapture
            val linearLayout = dialogCapture.topBar
            if (linearLayout == null) {
                return
            }
            linearLayout.setVisibility(0)
            dialogCapture.closeEdit()
        }
    }
    internal class SavedImageStep(val outer: DialogCapture,
                                    val path: String?
    ) : Runnable {
        internal inner class SavedSnackbarListener : SnackbarListener {
            override fun onSecond() {
            }
            override fun onDismissed() {
            }
            override fun onFirst() {
            }
            override fun onDismiss() {
                this@SavedImageStep.outer.snackbar = null
            }
        }
        internal inner class OpenWithAppStep(val outer: SavedImageStep,
                                             val url: String?
        ) : SnackbarListener {
            override fun onSecond() {
            }
            override fun onDismissed() {
            }
            override fun onFirst() {
                openWithApp(this.outer.outer.hostActivity, this.url, "image/*", true, false)
            }
            override fun onDismiss() {
                this.outer.outer.snackbar = null
            }
        }
        override fun run() {
            val dialogCapture = this.outer
            if (dialogCapture.contentRoot == null) {
                return
            }
            cancelToast()
            val mySnackbar = dialogCapture.snackbar
            if (mySnackbar != null) {
                mySnackbar.hide(false)
                dialogCapture.snackbar = null
            }
            dialogCapture.snackbar = MySnackbar(dialogCapture.hostActivity!!)
            val path = this.path
            if (TextUtils.isEmpty(path)) {
                AppState.darkUi = true
                dialogCapture.snackbar!!.showSingle(
                    dialogCapture.contentRoot,
                    AppR.string.save_fail,
                    SavedSnackbarListener()
                )
                applyScreenRotation(dialogCapture.hostActivity, false)
            } else {
                AppState.darkUi = true
                dialogCapture.snackbar!!.showWithButton(
                    dialogCapture.contentRoot,
                    AppR.string.save_success,
                    1,
                    OpenWithAppStep(this, path)
                )
                applyScreenRotation(dialogCapture.hostActivity, false)
            }
        }
    }


    internal inner class CancelFlingStep : ImageSizeListener {
        override fun onSize(view: View?, width: Int, height: Int) {
            val dialogCapture = this@DialogCapture
            val zoomImageAttacher = dialogCapture.zoomAttacher
            if (zoomImageAttacher != null) {
                zoomImageAttacher.cancelFling()
                val orientation = if (width > height) 2.toByte() else 1.toByte()
                if (dialogCapture.orientation != orientation) {
                    dialogCapture.orientation = orientation
                    dialogCapture.zoomAttacher!!.resetMatrix()
                }
                dialogCapture.areaSize = if (dialogCapture.bitmapWidth == 0 || dialogCapture.bitmapHeight == 0) null else Pair(width, height)
            }
        }
    }

    private class SaveTask(dialogCapture: DialogCapture?, path: String?, bitmap: Bitmap?) :
        MyAsyncTask() {
        var dialogRef: WeakReference<*>? = null
        var path: String? = null
        var bitmap: Bitmap? = null
        var imageHeight: Int = 0
        var displayRect: RectF? = null
        var hasPath: Boolean = false

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogCapture)
            this.dialogRef = weakReference
            val dialogCapture = weakReference.get() as DialogCapture?
            if (dialogCapture != null) {
                this.path = path
                dialogCapture.saving = true
                val myCoverView = dialogCapture.loadCover
                if (myCoverView != null) {
                    myCoverView.show(true)
                }
                if (!dialogCapture.fullMode) {
                    if (MainUtil.isBitmapOk(bitmap)) {
                        this.bitmap = bitmap
                        return@init
                    }
                    val cropImageView = dialogCapture.cropView
                    if (cropImageView == null) {
                        return@init
                    }
                    this.bitmap = cropImageView.croppedImage
                    return@init
                }
                this.bitmap = bitmap
                val mySizeImage = dialogCapture.sizeImage
                if (mySizeImage != null) {
                    this.imageHeight = mySizeImage.getHeight()
                    val zoomImageAttacher = dialogCapture.zoomAttacher
                    if (zoomImageAttacher != null) {
                        zoomImageAttacher.cancelFling()
                        zoomImageAttacher.checkMatrixBounds()
                        this.displayRect = zoomImageAttacher.displayRectOf(zoomImageAttacher.currentMatrix())
                    }
                }
            }
        
        }
    }
        public override fun doInBackground() {
            var bitmapCreateBitmap: Bitmap?
            val dialogCapture = this.dialogRef!!.get() as DialogCapture?
            if (dialogCapture != null) {
                val rectF = this.displayRect
                if (rectF != null && rectF.left > 0.0f && MainUtil.isBitmapOk(dialogCapture.fullBitmap)) {
                    val bitmap = dialogCapture.fullBitmap
                    val width = bitmap!!.getWidth()
                    val height = bitmap.getHeight()
                    val bottom = rectF.bottom
                    val top = rectF.top
                    val visibleHeight = bottom - top
                    val offsetTop = -top
                    val offsetBottom = (visibleHeight - offsetTop) - this.imageHeight
                    val scale = visibleHeight / height
                    val cropTop = Math.round(offsetTop / scale)
                    val cropBottom = Math.round(offsetBottom / scale)
                    val cropHeight = if ((cropTop > 0 || cropBottom > 0) && cropTop >= 0) ((height - cropTop) - cropBottom) else 0
                    if ((cropTop > 0 || cropBottom > 0) && cropTop >= 0 && cropHeight > 0) {
                        try {
                            bitmapCreateBitmap = Bitmap.createBitmap(bitmap, 0, cropTop, width, cropHeight)
                        } catch (unused: Exception) {
                            bitmapCreateBitmap = null
                        } catch (unused: OutOfMemoryError) {
                            bitmapCreateBitmap = null
                        }
                    } else {
                        bitmapCreateBitmap = null
                    }
                    if (MainUtil.isBitmapOk(bitmapCreateBitmap)) {
                        this.bitmap = bitmapCreateBitmap
                    } else if (MainUtil.isBitmapOk(bitmap)) {
                        this.bitmap = bitmap
                    }
                }
                if (MainUtil.isBitmapOk(this.bitmap)) {
                    val context = dialogCapture.appContext
                    val bitmap = this.bitmap
                    val compressFormat =
                        if (bitmap!!.hasAlpha()) CompressFormat.PNG else CompressFormat.JPEG
                    val uri = this.path
                    val saved = saveBitmap(context, bitmap, uri, compressFormat)
                    val uriItemJ: UriItem? = if (saved) MainUri.createIn(dialogCapture.appContext, uri, MainUri.storageVolume()) else null
                    if (saved && uriItemJ != null) {
                        DbBookDown.addFile(dialogCapture.appContext, uri, null, uriItemJ)
                    }
                    this.hasPath = saved
                }
            }
        }
        public override fun onCancelled() {
            val dialogCapture = this.dialogRef!!.get() as DialogCapture?
            if (dialogCapture != null) {
                dialogCapture.saving = false
                val myCoverView = dialogCapture.loadCover
                if (myCoverView != null) {
                    myCoverView.hide(true)
                }
            }
        }
        public override fun onPostExecute() {
            val dialogCapture = this.dialogRef!!.get() as DialogCapture?
            if (dialogCapture != null) {
                dialogCapture.saving = false
                val myCoverView = dialogCapture.loadCover
                if (myCoverView != null) {
                    myCoverView.hide(true)
                }
                val path = if (this.hasPath) this.path else null
                val handler = dialogCapture.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(SavedImageStep(dialogCapture, path))
            }
        }
    }

    internal inner class AreaZoomListener : AttacherListener {
        override fun onMatrixChanged(rectF: RectF?, isZoomed: Boolean) {
            val dialogCapture = this@DialogCapture
            // A copy: the attacher keeps changing the same RectF.
            dialogCapture.areaRect = if (rectF == null) null else RectF(rectF)
        }
        override fun onTap(): Boolean {
            return false
        }
        override fun canScale(): Boolean {
            return false
        }
        override fun onDoubleTap() {
        }
        override fun onTouch(motionEvent: MotionEvent?, isDown: Boolean) {
        }
    }

    /** The dialog is up. */
    fun isOpen(): Boolean = isShowing

    /** Old popup-menu close; there is none. */
    fun refreshButtons() {}

    override fun dismiss() {
        if (appContext == null || saving) {
            if (appContext == null) super.dismiss()
            return
        }
        closeEdit()
        val crop = cropView
        if (crop != null) {
            crop.alive = false
            crop.borderPaint = null
            crop.cornerPaint = null
            crop.guidePaint = null
            crop.dimPaint = null
            crop.cropRect = null
            crop.touchOffset = null
            crop.activeHandle = null
            cropView = null
        }
        sizeImage?.sizeCallback = null
        sizeImage = null
        areaSize = null
        areaRect = null
        zoomAttacher?.detach()
        zoomAttacher = null
        snackbar?.hide(false)
        snackbar = null
        hostActivity?.swapScreenOnView(null, false)
        hostActivity = null
        appContext = null
        fullBitmap = null
        pageUrl = null
        contentRoot = null
        super.dismiss()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (saving) return
        dismiss()
    }

    fun onEditResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        val edit = editDialog
        return edit != null && edit.onPickResult(requestCode, resultCode, intent)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemBars()
    }

    fun closeEdit() {
        val edit = editDialog
        if (edit != null) {
            edit.dismiss()
            editDialog = null
            editShown = false
            applyScreenRotation(hostActivity, false)
        }
    }

    fun applyEditSize(newZz: Boolean) {
        var zz = newZz
        if (zz) zz = hostActivity?.smallScreen ?: isSmallScreen(context)
        hideSystemBars()
        editDialog?.applyLandscape(zz)
    }

    /** Hides the system bars. */
    fun hideSystemBars() {
        if (hostActivity == null) return
        mainHandler?.post {
            val w = window ?: return@post
            val controller = WindowCompat.getInsetsController(w, w.decorView)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    companion object {
    }
}
