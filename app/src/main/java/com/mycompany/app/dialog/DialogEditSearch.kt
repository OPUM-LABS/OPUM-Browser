package com.mycompany.app.dialog

import android.content.Context
import com.mycompany.app.main.RequestCode
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.webkit.URLUtil
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.dialog.DialogQuickColor.QuickColorListener
import com.mycompany.app.dialog.DialogQuickIcon.QuickLoadListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.requestCamera
import com.mycompany.app.main.MainUtil.requestStoragePermission
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference

/**
 * Add / edit a user search engine (row [itemId], name [startLabel], search URL [startLinkParam], color [startColorParam]): round icon (stored icon, the
 * site's icon via the list loader, or the color with a search glyph; tap for Web title icon / Image / Camera / Color),
 * Name field (focused), URL field (several lines) and Save. Save checks name / network URL / duplicates, then
 * [DialogTask] stores the row in `DbBookSearch` and calls [EditUrlListener]. Picked images come back through the
 * caller (`cameraUri`, `picturePath`), as before. Compose now (on [ComposeBottomDialog]); same constructor, shows
 * itself.
 */
@Suppress("deprecation")
class DialogEditSearch(
    mainActivity: android.app.Activity?,
    itemId: Long,
    startLabel: String?,
    startLinkParam: String?,
    startColorParam: Int,
    editUrlListener: EditUrlListener?,
) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: android.app.Activity? = mainActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var editListener: EditUrlListener? = editUrlListener
    @JvmField
    var itemId: Long = itemId
    @JvmField
    var startLabel: String? = startLabel
    @JvmField
    var startLink: String? = startLinkParam
    @JvmField
    var startColor: Int = startColorParam
    /** Icon color (0 = use [iconBitmap]). */
    @JvmField
    var colorValue: Int = if (startColorParam == 0) -65536 else startColorParam
    /** Icon bitmap (stored, loaded, picked or web icon). */
    @JvmField
    var iconBitmap: Bitmap? = null
    /** The icon was changed by the user. */
    @JvmField
    var iconChanged: Boolean = false
    @JvmField
    var saveTask: DialogTask? = null
    @JvmField
    var saving: Boolean = false
    @JvmField
    var listLoader: MainListLoader? = null
    @JvmField
    var cameraUri: Uri? = null
    @JvmField
    var picturePath: String? = null
    @JvmField
    var iconDialog: DialogQuickIcon? = null
    @JvmField
    var colorDialog: DialogQuickColor? = null

    private var iconBmp by mutableStateOf<Bitmap?>(null)
    private var iconColor by mutableIntStateOf(colorValue)
    private var name by mutableStateOf(TextFieldValue(startLabel ?: "", TextRange(0, (startLabel ?: "").length)))
    private var url by mutableStateOf(startLinkParam ?: "")
    private var menu by mutableStateOf(false)
    internal var busy by mutableStateOf(false)

    init {
        loadIcon(startColor, 34, startLink)
        setSheet {
            val focus = remember { FocusRequester() }
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Box(
                        Modifier.size(48.dp).clip(CircleShape).clickable(enabled = !busy) { openMenu() },
                        contentAlignment = Alignment.Center,
                    ) {
                        val bmp = iconBmp
                        if (bmp != null) {
                            Image(bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
                        } else {
                            Box(Modifier.size(48.dp).background(Color(iconColor)), contentAlignment = Alignment.Center) {
                                Icon(
                                    painterResource(AppR.drawable.outline_search_white_24), contentDescription = null,
                                    tint = Color.White, modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.web_title)) }, onClick = { menu = false; pick(0) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.image)) }, onClick = { menu = false; pick(1) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.camera)) }, onClick = { menu = false; pick(2) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.color_title)) }, onClick = { menu = false; pick(3) })
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text(stringResource(AppR.string.name)) },
                    singleLine = true,
                    enabled = !busy,
                    modifier = Modifier.weight(1f).padding(start = 16.dp).focusRequester(focus),
                )
            }
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                placeholder = { Text("https://...") },
                minLines = 5,
                enabled = !busy,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
            )
            if (busy) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { save() }, enabled = !busy) { Text(stringResource(AppR.string.save)) }
            }
            LaunchedEffect(Unit) { focus.requestFocus() }
        }
        show()
    }

    private fun openMenu() {
        if (hostActivity == null) return
        cameraUri = null
        picturePath = null
        menu = true
    }

    /** Menu pick: 0 = web title icon, 1 = image, 2 = camera, 3 = color. */
    private fun pick(color: Int) {
        val act = hostActivity ?: return
        when (color) {
            1 -> requestStoragePermission(act, RequestCode.CAMERA_OR_FILE)
            2 -> if (!requestCamera(act, 30)) cameraUri = MainUtil.newMediaUri(RequestCode.CAMERA_OR_FILE, act, false)
            3 -> {
                if (iconDialog != null || colorDialog != null) return
                val dialogQuickColor = DialogQuickColor(act, colorValue, object : QuickColorListener {
                    override fun onColor(color: Int) {
                        colorDialog?.dismiss()
                        colorDialog = null
                        iconBitmap = null
                        colorValue = color
                        resetIcon()
                    }
                })
                colorDialog = dialogQuickColor
                dialogQuickColor.setOnDismissListener(DialogInterface.OnDismissListener {
                    colorDialog?.dismiss()
                    colorDialog = null
                })
            }
            else -> {
                if (iconDialog != null || colorDialog != null) return
                val link = url.trim { it <= ' ' }
                if (TextUtils.isEmpty(link)) {
                    showToast(appContext, AppR.string.input_url)
                    return
                }
                val dialogQuickColor = DialogQuickIcon(act, link, object : QuickLoadListener {
                    override fun onIcon(bitmap: Bitmap?) {
                        if (!MainUtil.isBitmapOk(bitmap)) return
                        iconChanged = true
                        setIcon(bitmap)
                    }
                })
                iconDialog = dialogQuickColor
                dialogQuickColor.setOnDismissListener(DialogInterface.OnDismissListener {
                    iconDialog?.dismiss()
                    iconDialog = null
                })
            }
        }
    }

    private fun setIcon(bitmap: Bitmap?) {
        iconBitmap = bitmap
        colorValue = 0
        iconBmp = bitmap
    }

    private fun save() {
        if (saving || busy) return
        saving = true
        val label = name.text.trim { it <= ' ' }
        val link = url.trim { it <= ' ' }
        when {
            TextUtils.isEmpty(label) -> showToast(appContext, AppR.string.input_name)
            TextUtils.isEmpty(link) -> showToast(appContext, AppR.string.input_url)
            !URLUtil.isNetworkUrl(link) -> showToast(appContext, AppR.string.invalid_url)
            itemId > 0 && link == startLink && !iconChanged && colorValue == startColor && label == startLabel -> dismiss()
            !(itemId > 0 && link == startLink) && exists(appContext, link) -> showToast(appContext, AppR.string.already_added)
            else -> {
                saveTask?.cancelled = true
                saveTask = null
                val task = DialogTask(this, label, link)
                saveTask = task
                task.start(appContext)
            }
        }
        saving = false
    }

    private fun exists(context: Context?, link: String?): Boolean {
        if (context == null || TextUtils.isEmpty(link)) return false
        var cursor: Cursor? = null
        var found = false
        try {
            cursor = DbUtil.query(
                DbBookSearch.get(context).getWritableDatabase(),
                "DbBookSearch_table", null, "_text=?", arrayOf<String?>(link!!), null,
            )
            found = cursor != null && cursor.moveToFirst()
        } catch (ignored: Exception) {
        }
        cursor?.close()
        return found
    }

    class DialogTask(dialogEditSearch: DialogEditSearch?, title: String?, label: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*> = WeakReference<Any?>(dialogEditSearch)
        @JvmField
        var itemId: Long = 0
        @JvmField
        var title: String? = null
        @JvmField
        var url: String? = null
        @JvmField
        var colorValue: Int = 0
        @JvmField
        var icon: Bitmap? = null
        @JvmField
        var changed: Boolean = false

        init {
            val d = dialogRef.get() as DialogEditSearch?
            if (d != null) {
                itemId = d.itemId
                colorValue = d.colorValue
                icon = d.iconBitmap
                this.title = title
                url = label
                d.busy = true
                d.setCanceledOnTouchOutside(false)
            }
        }

        public override fun doInBackground() {
            val d = dialogRef.get() as DialogEditSearch?
            if (d == null || cancelled) return
            val title = title
            if (TextUtils.isEmpty(title)) return
            val url = url
            if (TextUtils.isEmpty(url)) return
            val values = android.content.ContentValues().also { cv -> cv.put("_title", title); cv.put("_text", url) }
            val bitmap = icon
            if (MainUtil.isBitmapOk(bitmap)) {
                try {
                    val out = ByteArrayOutputStream()
                    bitmap!!.compress(CompressFormat.PNG, 100, out)
                    values.put("_icon", out.toByteArray())
                    out.close()
                    values.put("_color", 0 as Int?)
                    DbBookSearch.cacheEngineIcon(url, bitmap)
                } catch (unused: Exception) {
                }
            } else {
                if (itemId > 0) values.put("_icon", ByteArray(1))
                var color = colorValue
                if (color == 0) {
                    val colors = MainConst.iconColors
                    val index = (DbBookSearch.colorIndex % colors.size).let { if (it >= 0) it else 0 }
                    color = colors[index]
                    DbBookSearch.colorIndex = (index + 3) % colors.size
                    colorValue = color
                }
                values.put("_color", color)
            }
            val db = DbBookSearch.get(d.appContext).getWritableDatabase()
            if (itemId > 0) {
                DbUtil.updateById(db, "DbBookSearch_table", values, itemId)
            } else {
                itemId = DbUtil.insert(db, "DbBookSearch_table", values)
            }
            changed = true
        }

        public override fun onPostExecute() {
            val d = dialogRef.get() as DialogEditSearch? ?: return
            if (changed) {
                d.saveTask = null
                d.editListener?.onSaved(itemId, null, null)
                return
            }
            if (d.appContext != null) {
                d.busy = false
                d.setCanceledOnTouchOutside(true)
                showToast(d.appContext, AppR.string.update_fail)
            }
        }
    }

    /** Color icon: [colorValue] (red when 0) with the search glyph. */
    fun resetIcon() {
        if (colorValue == 0) colorValue = -65536
        iconColor = colorValue
        iconBmp = null
    }

    /** Icon for color [newColor] or the icon of [str] ([listType] list type; 0 = an image file path). */
    fun loadIcon(newColor: Int, listType: Int, path: String?) {
        if (TextUtils.isEmpty(path)) {
            iconBitmap = null
            resetIcon()
            return
        }
        if (newColor != 0 && newColor != -460552) {
            iconBitmap = null
            colorValue = newColor
            resetIcon()
            return
        }
        val item = ChildItem()
        item.listType = listType
        item.path = path
        item.filePath = path
        item.itemType = if (listType == 0) 1.toByte() else 11.toByte()
        val cached = DbBookSearch.cachedIcon(path)
        if (MainUtil.isBitmapOk(cached)) {
            setIcon(cached)
            return
        }
        resetIcon()
        val ctx = appContext ?: return
        listLoader?.release()
        val target = View(ctx)
        target.setTag(0)
        val loader = MainListLoader(ctx, false, object : ListLoadListener {
            override fun onFailed(childItem: ChildItem?, view: View?) {
                iconBitmap = null
                resetIcon()
            }

            override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                if (!MainUtil.isBitmapOk(bitmap)) {
                    iconBitmap = null
                    resetIcon()
                } else {
                    setIcon(bitmap)
                }
            }
        })
        listLoader = loader
        loader.enqueue(item, target)
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        saveTask?.cancelled = true
        saveTask = null
        iconDialog?.dismiss()
        iconDialog = null
        colorDialog?.dismiss()
        colorDialog = null
        listLoader?.release()
        listLoader = null
        hostActivity = null
        appContext = null
        editListener = null
        cameraUri = null
        picturePath = null
        super.dismiss()
    }

    companion object {
    }
}
