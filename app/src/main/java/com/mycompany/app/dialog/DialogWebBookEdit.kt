package com.mycompany.app.dialog

import android.content.ContentValues
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.AlbumSettings
import android.content.DialogInterface
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.web.WebBookmarksDialog
import com.mycompany.app.web.WebBookmarksDialog.BookListListener
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.quick.QuickIcons
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Add or edit a bookmark ([childItem] = the bookmark to edit, null = new; [startUrlParam] = URL, [startTitleParam] = title): header with
 * the favicon (listener icon, cached favicon, the stored icon path via the image loader, or a letter) and the name
 * field, the URL field, the folder row (opens `WebBookmarksDialog` in folder-picker mode) and Save. A new bookmark is
 * checked for an existing one with the same URL in that folder ("Already added"); saving (`DbBookWeb`) runs in the
 * background, then [BookEditListener.onSaved] and close. `WebViewActivity` pushes a late page icon with [setIcon]. Compose
 * now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogWebBookEdit(
    mainActivity: MainActivity?,
    childItem: ChildItem?,
    startUrlParam: String?,
    startTitleParam: String?,
    bookEditListener: BookEditListener?,
) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: MainActivity? = mainActivity
    @JvmField
    var appContext: android.content.Context? = context
    @JvmField
    var editListener: BookEditListener? = bookEditListener
    @JvmField
    var noListener: Boolean = TextUtils.isEmpty(startUrlParam)
    @JvmField
    var editItem: ChildItem? = childItem
    @JvmField
    var startUrl: String? = startUrlParam
    @JvmField
    var startTitle: String? = startTitleParam
    @JvmField
    var lastDir: String? = null
    @JvmField
    var folderPath: String? = null
    @JvmField
    var iconPath: String? = childItem?.filePath
    @JvmField
    var itemId: Long = childItem?.id ?: 0L
    @JvmField
    var folderDialog: WebBookmarksDialog? = null
    @JvmField
    var checking: Boolean = false
    @JvmField
    var newItem: Boolean = childItem == null
    @JvmField
    var saveUrl: String? = null
    @JvmField
    var saveName: String? = null
    @JvmField
    var saveIcon: Bitmap? = null
    @JvmField
    var saveParentId: Long = 0
    @JvmField
    var iconUrl: String? = null

    @JvmField
    val mainHandler: Handler = Handler(Looper.getMainLooper())

    private var nameText by mutableStateOf(startTitleParam ?: "")
    private var urlText by mutableStateOf(startUrlParam ?: "")
    private var iconBmp by mutableStateOf<Bitmap?>(null)
    private var folderText by mutableStateOf("")
    private var saving by mutableStateOf(false)
    /** The refresh button is asking the site for its icon. */
    private var fetching by mutableStateOf(false)
    /** The icon the refresh button loaded from the site (2026-11-20): shown and saved with the bookmark. */
    private var refreshedIcon: Bitmap? = null

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

    interface BookEditListener {
        fun onSaved(id: Long, folder: String?, url: String?)
        val icon: Bitmap?
    }

    init {
        var dir = if (childItem != null) childItem.dir else (if (SyncSettings.secretMode.value) AlbumSettings.webBookSec.value else AlbumSettings.webBookDir.value)
        if (TextUtils.isEmpty(dir)) dir = "/"
        lastDir = dir
        setFolderPath(dir)
        val listener = if (noListener) null else editListener
        if (listener == null) {
            loadIcon(iconPath, startTitle, null)
        } else {
            var bmp = listener.icon
            if (!MainUtil.isBitmapOk(bmp)) bmp = MainUtil.cachedIcon(MainUtil.shortHostOf(startUrl!!))
            loadIcon(iconPath, startTitle, bmp)
        }
        // no page icon at hand: the site's saved favicon (the one saving would store anyway) instead of the letter
        val address = startUrl
        if (iconBmp == null && !TextUtils.isEmpty(address)) {
            val ctx = appContext
            runAsync(Runnable {
                val found = MainUtil.siteIcon(ctx, address)
                if (MainUtil.isBitmapOk(found)) mainHandler.post { if (iconBmp == null) iconBmp = found }
            })
        }
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val bmp = iconBmp
                if (bmp != null) {
                    Image(bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape))
                } else {
                    Box(
                        Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            nameText.trim().take(1).uppercase().ifEmpty { "?" },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                    }
                }
                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    enabled = !saving,
                    singleLine = true,
                    label = { Text(stringResource(AppR.string.name)) },
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                )
                // reloads the icon from the website
                Box(Modifier.padding(start = 4.dp).size(48.dp), contentAlignment = Alignment.Center) {
                    if (fetching) androidx.compose.material3.CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else androidx.compose.material3.IconButton(onClick = { refreshIcon() }, enabled = !saving) {
                        androidx.compose.material3.Icon(
                            androidx.compose.ui.res.painterResource(AppR.drawable.outline_refresh_black_24),
                            contentDescription = stringResource(AppR.string.refresh),
                        )
                    }
                }
            }
            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                enabled = !saving,
                singleLine = true,
                label = { Text(stringResource(AppR.string.url)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { save() }),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            )
            ListItem(
                headlineContent = { Text(stringResource(AppR.string.save_location)) },
                supportingContent = { Text(folderText, maxLines = 2) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                modifier = Modifier.clickable(enabled = !saving) { pickFolder() },
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { save() }, enabled = !saving) { Text(stringResource(AppR.string.save)) }
            }
        }
        show()
    }

    /** Refresh button: asks the site of the URL field for its icon; it is saved with the bookmark. */
    private fun refreshIcon() {
        if (fetching || saving) return
        val link = urlText.trim { it <= ' ' }
        if (TextUtils.isEmpty(link)) {
            showToast(appContext, AppR.string.input_url)
            return
        }
        val ctx = appContext ?: return
        fetching = true
        runAsync(Runnable {
            val bitmap = QuickIcons.fromSite(ctx, link)
            mainHandler.post {
                fetching = false
                if (MainUtil.isBitmapOk(bitmap)) {
                    refreshedIcon = bitmap
                    iconBmp = bitmap
                } else {
                    showToast(ctx, AppR.string.no_icon)
                }
            }
        })
    }

    /** A page icon that arrived after the sheet opened. */
    fun setIcon(bitmap: Bitmap?) {
        if (!MainUtil.isBitmapOk(bitmap) || refreshedIcon != null) return
        iconBmp = bitmap
    }

    private fun save() {
        if (checking || saving) return
        checking = true
        startSave(itemId)
    }

    private fun pickFolder() {
        if (hostActivity == null || folderDialog != null) return
        val list: ArrayList<Any?>? = editItem?.let { arrayListOf<Any?>(it) }
        val dialog = WebBookmarksDialog(hostActivity, folderPath, list, 6, object : BookListListener {
            override fun onPick(path: String?) {
                folderDialog?.dismiss()
                folderDialog = null
                setFolderPath(path)
            }

            override fun onAction(action: Int, path: String?) {}
        })
        folderDialog = dialog
        dialog.setOnDismissListener(DialogInterface.OnDismissListener {
            folderDialog?.dismiss()
            folderDialog = null
        })
    }

    /** Checks name and URL, then (new bookmark) the duplicate check, then saving. */
    fun startSave(parentId: Long) {
        val name = nameText.trim { it <= ' ' }
        if (TextUtils.isEmpty(name)) {
            showToast(appContext, AppR.string.input_name)
            checking = false
            return
        }
        val url = urlText.trim { it <= ' ' }
        if (TextUtils.isEmpty(url)) {
            showToast(appContext, AppR.string.input_url)
            checking = false
            return
        }
        saveUrl = url
        saveName = name
        saveIcon = null
        saveParentId = parentId
        iconUrl = null
        if (newItem) runAsync(Runnable { checkDuplicate() }) else saveBookmark()
    }

    private fun checkDuplicate() {
        val context = appContext
        val dir = folderPath
        val url = saveUrl
        var exists = false
        if (context != null && !TextUtils.isEmpty(url)) {
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookWeb.get(context).getWritableDatabase(), "DbBookWeb_table", null,
                    "_secret=? AND _dir=? AND _path=?", arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), dir, url), null,
                )
                if (cursor != null && cursor.moveToFirst()) exists = true
            } catch (unused: Exception) {
            }
            cursor?.close()
        }
        mainHandler.post {
            if (!exists) {
                saveBookmark()
            } else {
                showToast(appContext, AppR.string.already_added)
                checking = false
            }
        }
    }

    /** Saves in the background (new: `DbBookWeb.editItem`; edit: update the row), then listener + close. */
    fun saveBookmark() {
        saving = true
        val listener = if (!noListener) editListener else null
        if (listener != null) saveIcon = listener.icon
        runAsync(Runnable {
            val url = saveUrl
            val title = saveName
            val refreshed = refreshedIcon
            var bitmap = refreshed ?: saveIcon
            val refreshedBytes = if (!MainUtil.isBitmapOk(refreshed)) null else try {
                java.io.ByteArrayOutputStream().use { out -> refreshed!!.compress(Bitmap.CompressFormat.PNG, 100, out); out.toByteArray() }
            } catch (_: Exception) { null }
            saveUrl = null
            saveName = null
            saveIcon = null
            if (saveParentId == 0L) {
                if (!MainUtil.isBitmapOk(bitmap)) bitmap = MainUtil.siteIcon(appContext, url)
                if (noListener && !MainUtil.isBitmapOk(bitmap)) iconUrl = url
                val context = appContext
                var dir = folderPath
                if (newItem) {
                    if (context != null && !TextUtils.isEmpty(url)) {
                        if (TextUtils.isEmpty(dir)) dir = "/"
                        val values = ContentValues()
                        values.put("_isdir", 0 as Int?)
                        values.put("_path", url)
                        values.put("_time", System.currentTimeMillis())
                        // a refreshed icon replaces the stored one (editItem keeps an existing icon otherwise)
                        if (refreshedBytes != null) values.put("_icon", refreshedBytes)
                        val rowId = DbBookWeb.editItem(
                            context, arrayOf<String?>("_icon", "_id"), arrayOf<String?>(com.mycompany.app.pref.store.Workspaces.dataKey(), url!!),
                            values, dir, url, title, bitmap,
                        )
                        // the list marks and scrolls to the saved bookmark by this id
                        if (rowId > 0) saveParentId = rowId
                    }
                } else {
                    val item = DbBookWeb.addItem(context, dir!!, url, title, bitmap)
                    if (item != null) saveParentId = item.id
                }
            } else {
                val values = ContentValues()
                values.put("_dir", folderPath)
                values.put("_path", url)
                values.put("_title", title)
                if (refreshedBytes != null) values.put("_icon", refreshedBytes)
                if (!MainUtil.sameObject(lastDir, folderPath)) {
                    val now = System.currentTimeMillis()
                    values.put("_time", now)
                    values.put("_rsv4", now)
                }
                DbUtil.updateById(DbBookWeb.get(appContext).getWritableDatabase(), "DbBookWeb_table", values, saveParentId)
            }
            mainHandler.post {
                editListener?.onSaved(saveParentId, folderPath, iconUrl)
                dismiss()
            }
        })
    }

    /** Header icon: a bitmap, else the stored icon path through the image loader, else a letter. */
    fun loadIcon(url: String?, startTitle: String?, bitmap: Bitmap?) {
        if (MainUtil.isBitmapOk(bitmap)) {
            iconBmp = bitmap
            return
        }
        if (TextUtils.isEmpty(url)) {
            return
        }
        val viewItem = ViewItem()
        viewItem.itemType = 1.toByte()
        viewItem.url = url
        viewItem.filePath = url
        viewItem.cacheType = 2
        com.mycompany.app.glide.ThumbLoader.loadFor(viewItem, ImageView(context), object : ViewItemListener {
                override fun onFailed(viewItem: MainItem.ViewItem?, view: View?, failReason: Exception?) {
                }

                override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
                    if (MainUtil.isBitmapOk(bitmap)) iconBmp = bitmap
                }
            })
    }

    /** Folder row text ("Bookmark" + path). */
    fun setFolderPath(newFolderPath: String?) {
        folderPath = newFolderPath
        val base = context.getString(AppR.string.bookmark)
        folderText = if (TextUtils.isEmpty(newFolderPath) || newFolderPath == "/") base else base + newFolderPath
    }

    override fun canCancelFromScrim(): Boolean = !saving

    override fun dismiss() {
        val list = folderDialog
        folderDialog = null
        list?.dismiss()
        hostActivity = null
        editListener = null
        super.dismiss()
    }

    companion object {
    }
}
