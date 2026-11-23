package com.mycompany.app.dialog

import android.app.ActivityManager
import com.mycompany.app.main.RequestCode
import com.mycompany.app.pref.store.ZoneSettings
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.requestCamera
import com.mycompany.app.main.MainUtil.requestStoragePermission
import com.mycompany.app.main.MainUtil.loadDrawable
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.web.WebShortcut

import java.io.File

/**
 * Home-screen shortcut for the current page ([shortType] = 0, URL [pageUrl], name [shortName]) or the album ([shortType] = 1): the icon (page
 * favicon, cached site icon or a letter; tap for Default image / Image / Camera, picked images go through the crop
 * screen, [onPickResult]) with the old "new" dot, the Name field and Apply. The icon is still drawn by an off-screen
 * [MyRoundImage] ([iconImage]) so the launcher gets the same picture; the pin request (`ShortcutManager`, or the old install
 * broadcast) is the old code. [EditShortListener] gives the page icon and hears a pinned shortcut. Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogEditShort(
    mainActivity: android.app.Activity?,
    pageUrl: String?,
    shortName: String?,
    shortType: Int,
    editShortListener: EditShortListener?,
) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: android.app.Activity? = mainActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var shortListener: EditShortListener? = editShortListener
    @JvmField
    var pageUrl: String? = pageUrl
    @JvmField
    var shortType: Byte = shortType.toByte()
    /** Off-screen icon view: draws the favicon / picked image / letter; [preview] and the shortcut icon come from it. */
    @JvmField
    var iconImage: MyRoundImage? = null
    @JvmField
    var applying: Boolean = false
    /** True while showing the default icon (a late page favicon may replace it, [setIcon]). */
    @JvmField
    var iconLoading: Boolean = false
    @JvmField
    var cameraUri: Uri? = null
    @JvmField
    var picturePath: String? = null
    /** True while the icon is the letter of the name (follows the name field). */
    @JvmField
    var iconReady: Boolean = false

    @JvmField
    val mainHandler = Handler(Looper.getMainLooper())

    private var preview by mutableStateOf<Bitmap?>(null)
    private var name by mutableStateOf(shortName ?: "")
    private var menu by mutableStateOf(false)
    private var dot by mutableStateOf(ZoneSettings.notiShort.value)

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

    interface EditShortListener {
        fun onCreated()
        val icon: Bitmap?
    }

    init {
        val size = MainApp.dp40
        val image = MyRoundImage(context)
        image.setScaleType(ScaleType.CENTER_CROP)
        image.setCircleRadius(size / 2.0f)
        image.measure(View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY))
        image.layout(0, 0, size, size)
        iconImage = image
        loadIcon(null, true)
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Box(Modifier.size(48.dp).clip(CircleShape).clickable { openMenu() }) {
                        val bmp = preview
                        if (bmp != null) Image(bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
                    }
                    if (dot) {
                        Box(
                            Modifier.align(Alignment.TopStart).size(10.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                        )
                    }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.default_image)) }, onClick = { menu = false; pick(0) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.image)) }, onClick = { menu = false; pick(1) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.camera)) }, onClick = { menu = false; pick(2) })
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (iconReady && it.isNotEmpty()) {
                            iconImage?.showBoxLetter(AppR.mipmap.ic_launcher, it)
                            refresh()
                        }
                    },
                    placeholder = { Text(stringResource(AppR.string.name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { apply() }),
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                )
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { apply() }) { Text(stringResource(AppR.string.apply)) }
            }
        }
        show()
    }

    private fun refresh() {
        preview = MainUtil.viewSnapshot(iconImage, 1.0f)
    }

    private fun openMenu() {
        if (hostActivity == null) return
        if (ZoneSettings.notiShort.value) {
            ZoneSettings.edit(appContext) { set(ZoneSettings.notiShort, false) }
            dot = false
        }
        cameraUri = null
        picturePath = null
        menu = true
    }

    private fun pick(index: Int) {
        when (index) {
            1 -> requestStoragePermission(hostActivity, RequestCode.CAMERA_OR_FILE)
            2 -> if (!requestCamera(hostActivity, 30)) cameraUri = MainUtil.newMediaUri(RequestCode.CAMERA_OR_FILE, hostActivity, false)
            else -> loadIcon(null, true)
        }
    }

    private fun apply() {
        if (applying) return
        applying = true
        mainHandler.post {
            save()
            applying = false
        }
    }

    /** A late page favicon replaces the default icon. */
    fun setIcon(bitmap: Bitmap?) {
        val image = iconImage ?: return
        if (!iconLoading || !MainUtil.isBitmapOk(bitmap)) return
        iconReady = false
        image.setImageBitmap(bitmap)
        refresh()
    }

    /** Pins the shortcut (URL [str] for a page, the album otherwise) named [str2] with the current icon. */
    fun createShortcut(input: String?, label: String?, bitmap: Bitmap?) {
        val ctx = appContext ?: return
        val intent: Intent
        if (shortType.toInt() == 1) {
            intent = Intent(ctx, com.mycompany.app.ui.lists.AlbumListActivity::class.java)
            intent.putExtra("EXTRA_SHORT", true)
        } else {
            intent = Intent(ctx, WebShortcut::class.java)
            intent.setData(Uri.parse(input))
        }
        intent.setAction("android.intent.action.VIEW")
        val shortcutManager = ctx.getSystemService(ShortcutManager::class.java)
        if (shortcutManager == null || !shortcutManager.isRequestPinShortcutSupported()) {
            mainHandler.post { showToast(appContext, AppR.string.not_supported) }
            return
        }
        val icon = (if (MainUtil.isBitmapOk(bitmap)) Icon.createWithBitmap(bitmap) else null)
            ?: Icon.createWithResource(ctx, AppR.mipmap.ic_launcher)
        shortcutManager.requestPinShortcut(
            ShortcutInfo.Builder(ctx, System.currentTimeMillis().toString())
                .setIntent(intent).setShortLabel(label!!).setIcon(icon).build(),
            null,
        )
        mainHandler.post {
            shortListener?.onCreated()
            dismiss()
        }
    }

    /** Activity results: 9 = picked / camera image (goes to the crop screen), 12 = cropped icon file. */
    fun onPickResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode == RequestCode.CAMERA_OR_FILE) {
            var uri = cameraUri
            cameraUri = null
            if (resultCode == -1) {
                val data = intent?.getData()
                if (data != null) uri = data
                MainUtil.keepUriPermission(appContext, uri)
                if (hostActivity != null) {
                    if (uri == null) {
                        showToast(appContext, AppR.string.invalid_path)
                        return true
                    }
                    val dst = MainUtil.newCacheFilePath(appContext)
                    picturePath = dst
                    if (TextUtils.isEmpty(dst)) {
                        showToast(appContext, AppR.string.invalid_path)
                        return true
                    }
                    val crop = Intent(appContext, com.mycompany.app.ui.viewer.ImageCropActivity::class.java)
                    crop.setData(uri)
                    crop.putExtra("EXTRA_DST", picturePath)
                    crop.putExtra("EXTRA_ICON", true)
                    (hostActivity as? com.mycompany.app.main.ActivityHost)?.launchExternalForResult(crop, RequestCode.IMAGE_FILE)
                    return true
                }
            }
        } else {
            if (requestCode != RequestCode.IMAGE_FILE) return false
            val picturePathNow = picturePath
            picturePath = null
            if (resultCode == -1) {
                if (TextUtils.isEmpty(picturePathNow) || !File(picturePathNow!!).exists()) {
                    showToast(appContext, AppR.string.invalid_path)
                    return true
                }
                loadIcon(picturePathNow, false)
                return true
            }
        }
        return true
    }

    /** Apply: checks the name (and the page URL), then pins in the background. */
    fun save() {
        val label = name.trim { it <= ' ' }
        if (TextUtils.isEmpty(label)) {
            showToast(appContext, AppR.string.input_name)
            return
        }
        var url: String? = null
        if (shortType.toInt() == 0) {
            url = pageUrl?.trim { it <= ' ' }
            if (TextUtils.isEmpty(url)) {
                showToast(appContext, AppR.string.input_url)
                return
            }
        }
        // the icon view is drawn here on the UI thread; pinning the shortcut then runs in the background
        val snapshot = MainUtil.viewSnapshot(iconImage, 1.0f)
        runAsync(Runnable { createShortcut(url, label, snapshot) })
    }

    /** Icon: image file [str] ([loading] false), or the default (page favicon, cached site icon, letter; album: app icon). */
    fun loadIcon(url: String?, loading: Boolean) {
        val image = iconImage ?: return
        iconLoading = loading
        if (!loading && !TextUtils.isEmpty(url)) {
            val viewItem = ViewItem()
            viewItem.itemType = 1.toByte()
            viewItem.url = url
            viewItem.cacheType = 2
            com.mycompany.app.glide.ThumbLoader.loadFor(viewItem, image, object : ViewItemListener {
                override fun onFailed(viewItem: MainItem.ViewItem?, view: View?, failReason: Exception?) {
                    val img = iconImage ?: return
                    if (shortType.toInt() != 0) {
                        iconReady = false
                        img.setImageResource(AppR.mipmap.ic_launcher)
                    } else {
                        iconReady = true
                        img.showBoxLetter(AppR.mipmap.ic_launcher, name.trim())
                    }
                    refresh()
                }

                override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
                    val img = iconImage ?: return
                    iconReady = false
                    img.setImageBitmap(bitmap)
                    refresh()
                }
            })
            return
        }
        if (shortType.toInt() != 0) {
            iconReady = false
            image.setImageResource(AppR.mipmap.ic_launcher)
            refresh()
            return
        }
        var icon = shortListener?.icon
        if (!MainUtil.isBitmapOk(icon)) icon = MainUtil.cachedIcon(MainUtil.shortHostOf(pageUrl!!))
        if (MainUtil.isBitmapOk(icon)) {
            iconReady = false
            image.setImageBitmap(icon)
        } else {
            iconReady = true
            image.showBoxLetter(AppR.mipmap.ic_launcher, name.trim())
        }
        refresh()
    }

    override fun dismiss() {
        iconImage?.release()
        iconImage = null
        hostActivity = null
        shortListener = null
        cameraUri = null
        picturePath = null
        super.dismiss()
    }

    companion object {
    }
}
