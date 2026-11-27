package com.mycompany.app.web

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.view.Gravity
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.ui.state.AppState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The screenshot tool (2026-11-27; was: a mode question, a crop frame, then a name / folder sheet): one tap in the menu
 * copies the visible page, flashes, saves it at once in the download folder under the page title and the time, and
 * shows a small preview in the corner with Share, Edit (the image editor), Long screenshot (the whole page, the old
 * scroll mode) and Delete. A long press on the picture opens the name / folder sheet to save it elsewhere. The preview
 * goes away by itself after a few seconds; the screenshot stays saved.
 */
fun WebViewActivity.takeScreenshot() {
    val frame = this.tabsState.currentFrame
    if (this.currentPage == null || frame == null || this.isClosing || isAnyDialogOpen()) return
    closeScreenshotPreview()
    TabSnapshot.copyVisible(this, frame, pageVisibleBottom()) { picture ->
        if (picture == null || this.isClosing || isFinishing || isDestroyed) {
            MainUtil.showToast(this, R.string.image_fail)
            return@copyVisible
        }
        flashScreen()
        com.mycompany.app.ui.components.Haptics.tick(getWindow()?.decorView)
        val title = pageTitleOf(this.currentPage, this.dialogsState.pageUrl)
        val context = applicationContext
        runInBackground(Runnable {
            val shot = saveScreenshot(context, picture, title)
            this.webCoord?.post { if (!this.isClosing) showScreenshotPreview(picture, shot) }
        })
    }
}

/** A saved screenshot: its path (uri string), download-list id and file name; null fields when saving failed. */
class SavedShot(val path: String?, val id: Long, val name: String?)

private fun saveScreenshot(context: android.content.Context, picture: Bitmap, title: String?): SavedShot {
    return try {
        val stamp = SimpleDateFormat("yyyy-MM-dd HH.mm.ss", Locale.ROOT).format(Date())
        val name = MainUtil.safeTitleFileName(160, title, "Screenshot") + " " + stamp + ".jpg"
        val item = MainUri.createUri(context, MainUri.storageVolume(), null, name)
        val path = item?.uriString
        if (path == null || !MainUtil.saveBitmap(context, picture, path, Bitmap.CompressFormat.JPEG)) return SavedShot(null, 0, name)
        val uriItem = MainUri.createIn(context, path, MainUri.storageVolume())
        val id = if (uriItem != null) DbBookDown.addFile(context, path, null, uriItem) else 0L
        SavedShot(path, id, name)
    } catch (_: Exception) {
        SavedShot(null, 0, null)
    }
}

/** A short white flash over the page, like a camera. */
private fun WebViewActivity.flashScreen() {
    val overlays = this.floatingState.overlays ?: return
    val flash = View(this)
    flash.setBackgroundColor(android.graphics.Color.WHITE)
    flash.alpha = 0.7f
    try { overlays.addView(flash, -1, -1) } catch (_: Exception) { return }
    flash.animate().alpha(0f).setDuration(220L).withEndAction { overlays.removeView(flash) }.start()
}

fun WebViewActivity.closeScreenshotPreview() {
    val preview = this.screenshotPreview ?: return
    this.screenshotPreview = null
    this.floatingState.overlays?.removeView(preview)
}

private fun WebViewActivity.showScreenshotPreview(picture: Bitmap, shot: SavedShot) {
    val overlays = this.floatingState.overlays ?: return
    closeScreenshotPreview()
    if (shot.path == null) MainUtil.showToast(this, R.string.image_fail)
    val preview = ScreenshotPreview(this, picture, shot)
    this.screenshotPreview = preview
    // above the bottom toolbar, in the corner
    val decorHeight = getWindow()?.decorView?.height ?: 0
    val bottom = (decorHeight - pageVisibleBottom().coerceAtMost(decorHeight)).coerceAtLeast(0)
    val params = com.mycompany.app.ui.browser.OverlayParams(-2, -2)
    params.gravity = Gravity.BOTTOM or Gravity.START
    params.bottomMargin = bottom + MainApp.dp16
    params.leftMargin = MainApp.dp16
    try { overlays.addCoordView(preview, params) } catch (_: Exception) {}
}

/** The preview card in the corner. */
@SuppressLint("ViewConstructor")
class ScreenshotPreview(private val host: WebViewActivity, private val picture: Bitmap, private val shot: SavedShot) : ComposeOverlay(host) {
    private val visible = MutableTransitionState(false).apply { targetState = true }
    /** A finger was on the card: it stays until an action or a tap elsewhere. */
    private var touched = false

    init {
        setPage({ AppState.darkUi }) { Card() }
    }

    private fun close() {
        visible.targetState = false
        postDelayed({ if (host.screenshotPreview === this) host.closeScreenshotPreview() else host.floatingState.overlays?.removeView(this) }, 250L)
    }

    private fun delete() {
        val context = host.applicationContext
        if (shot.path != null) host.runInBackground(Runnable { DbBookDown.deleteFile(shot.id, context, shot.path, null, true) })
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    private fun Card() {
        LaunchedEffect(Unit) {
            delay(AUTO_HIDE_MS)
            if (!touched) close()
        }
        AnimatedVisibility(
            visibleState = visible,
            enter = slideInHorizontally { -it },
            exit = slideOutHorizontally { -it } + fadeOut(),
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 8.dp,
                modifier = Modifier.pointerInput(Unit) {
                    awaitPointerEventScope { while (true) { awaitPointerEvent(androidx.compose.ui.input.pointer.PointerEventPass.Initial); touched = true } }
                },
            ) {
                Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val image = remember(picture) { picture.asImageBitmap() }
                    Image(
                        image, contentDescription = stringResource(R.string.capture), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter,
                        modifier = Modifier.width(84.dp).heightIn(max = 150.dp).height((84f * picture.height / picture.width.coerceAtLeast(1)).dp)
                            .clip(RoundedCornerShape(12.dp))
                            .combinedClickable(onClick = { edit() }, onLongClick = { saveAs() }),
                    )
                    Column(Modifier.padding(start = 4.dp)) {
                        Action(R.drawable.outline_share_black_24, R.string.share, enabled = shot.path != null) {
                            close(); MainUtil.shareFile(31, host, shot.path, shot.name, "image/jpeg")
                        }
                        Action(R.drawable.outline_edit_black_24, R.string.edit, enabled = shot.path != null) { edit() }
                        Action(R.drawable.outline_vertical_align_bottom_black_24, R.string.shot_long) {
                            close(); delete(); WvaSettingListener(host).onSetting(null, 1, false, 0)
                        }
                        Action(R.drawable.outline_delete_black_20, R.string.delete, enabled = shot.path != null) { close(); delete() }
                    }
                }
            }
        }
    }

    private fun edit() {
        val path = shot.path ?: return
        close()
        try {
            host.launchExternalForResult(
                Intent(host, com.mycompany.app.ui.viewer.ImageEditActivity::class.java).putExtra("EXTRA_PATH", path),
                com.mycompany.app.main.RequestCode.EDIT_IMAGE,
            )
        } catch (_: Exception) {}
    }

    /** The old name / folder sheet: saved there, the automatic copy goes. */
    private fun saveAs() {
        close()
        // the sheet shows itself
        com.mycompany.app.dialog.DialogDownEdit(host, host.dialogsState.pageUrl, picture, object : com.mycompany.app.dialog.DialogDownPage.DownPageListener {
            override fun onSave(url: String?, path: String?, bitmap: Bitmap?) {
                if (path == null) return
                val context = host.applicationContext
                host.runInBackground(Runnable {
                    if (MainUtil.saveBitmap(context, picture, path, Bitmap.CompressFormat.JPEG)) {
                        MainUri.createIn(context, path, MainUri.storageVolume())?.let { DbBookDown.addFile(context, path, null, it) }
                        if (shot.path != null) DbBookDown.deleteFile(shot.id, context, shot.path, null, true)
                    }
                })
            }
        })
    }

    @Composable
    private fun Action(icon: Int, label: Int, enabled: Boolean = true, onClick: () -> Unit) {
        TextButton(onClick = onClick, enabled = enabled, contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)) {
            Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(label), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(start = 10.dp))
        }
    }

    private companion object {
        const val AUTO_HIDE_MS = 6000L
    }
}
