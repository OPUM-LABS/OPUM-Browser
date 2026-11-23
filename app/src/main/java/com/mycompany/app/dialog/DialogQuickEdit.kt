package com.mycompany.app.dialog

import android.content.ContentValues
import com.mycompany.app.main.RequestCode
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.AlbumSettings
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView.ScaleType
import androidx.compose.foundation.Image
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import com.mycompany.app.quick.QuickIcons
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.dialog.DialogQuickColor.QuickColorListener
import com.mycompany.app.dialog.DialogQuickIcon.QuickLoadListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.requestCamera
import com.mycompany.app.main.MainUtil.requestStoragePermission
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.web.WebViewActivity
import java.io.ByteArrayOutputStream
import java.lang.ref.WeakReference

/**
 * Add / edit a quick-access item ([editModeParam] = a folder of items [arrayList], [largeIconParam] = edit an existing one; URL [itemUrlParam], name
 * [itemLabelParam], page icon [bitmap], color [color]): round icon (stored / site icon, folder collage, color + letter; tap for Web
 * title (default title for folders) / Image / Camera / Color), Name field, URL field (not for folders), Apply with a
 * progress bar and Cancel. Checks and the save ([DialogTask], `DbBookQuick`) are the old code; [QuickEditListener]
 * hears the result. The icon is still drawn by an off-screen [MyRoundImage] ([iconImage]) and shown from its bitmap, so the
 * saved icon looks as before. Picked images come back through `WebViewActivity` ([cameraUri], [picturePath], [loadIcon]). Compose now (on
 * [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogQuickEdit(
    webViewActivity: WebViewActivity?,
    editModeParam: Boolean,
    itemUrlParam: String?,
    itemLabelParam: String?,
    bitmap: Bitmap?,
    color: Int,
    largeIconParam: Boolean,
    arrayList: ArrayList<com.mycompany.app.quick.QuickAdapter.QuickSubItem?>?,
    quickEditListener: QuickEditListener?,
    /** The shortcut shows the letter the user chose (1-byte `_icon`), not the site's icon. */
    letterOnlyParam: Boolean = false,
) : ComposeBottomDialog(webViewActivity) {
    /** The letter is the user's choice (from the start, or picked as a colour here): saved as the 1-byte `_icon` marker. */
    @JvmField
    var letterChosen: Boolean = letterOnlyParam
    /** The user picked or reloaded the icon here: a late site icon no longer replaces it. */
    private var iconTouched = false
    /** The refresh button is asking the site for its icon. */
    private var fetching by mutableStateOf(false)
    @JvmField
    var iconDialog: DialogQuickIcon? = null
    @JvmField
    var colorDialog: DialogQuickColor? = null
    @JvmField
    var hostActivity: WebViewActivity? = webViewActivity
    @JvmField
    var appContext: Context? = context
    @JvmField
    var editListener: QuickEditListener? = quickEditListener
    /** Editing an existing item. */
    @JvmField
    var largeIcon: Boolean = largeIconParam
    /** A folder. */
    @JvmField
    var editMode: Boolean = editModeParam
    @JvmField
    var itemUrl: String? = itemUrlParam
    @JvmField
    var itemLabel: String? = itemLabelParam
    @JvmField
    var startIcon: Bitmap? = bitmap
    /** Icon is an image ([iconBitmap]). */
    @JvmField
    var iconLoading: Boolean = false
    @JvmField
    var iconBitmap: Bitmap? = null
    /** The icon was changed by the user. */
    @JvmField
    var iconChanged: Boolean = false
    @JvmField
    var startColor: Int = color
    /** Icon color (0 = image). */
    @JvmField
    var iconColor: Int = color
    @JvmField
    var quickItems: ArrayList<com.mycompany.app.quick.QuickAdapter.QuickSubItem?>? = arrayList
    /** Folder showed its collage at the start / shows it now. */
    @JvmField
    var startCustomIcon: Boolean = false
    @JvmField
    var customIcon: Boolean = false
    /** Off-screen icon view: draws the icon; [preview] and the saved icon come from it. */
    @JvmField
    var iconImage: MyRoundImage? = null
    @JvmField
    var saveTask: DialogTask? = null
    @JvmField
    var applied: Boolean = false
    @JvmField
    var listLoader: MainListLoader? = null
    @JvmField
    var cameraUri: Uri? = null
    @JvmField
    var picturePath: String? = null

    private var preview by mutableStateOf<Bitmap?>(null)
    private var name by mutableStateOf(TextFieldValue(itemLabelParam ?: "", TextRange(0, (itemLabelParam ?: "").length)))
    private var url by mutableStateOf(TextFieldValue(itemUrlParam ?: ""))
    private var menu by mutableStateOf(false)
    internal var busy by mutableStateOf(false)
    internal var mainRes by mutableIntStateOf(AppR.string.apply)
    private var mainEnabled by mutableStateOf(true)

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

    interface QuickEditListener {
        fun onSaved(path: String?, color: Int, order: Int, title: String?)
        fun onSavedCustom(oldPath: String?, path: String?, title: String?, color: Int, arrayList: ArrayList<com.mycompany.app.quick.QuickAdapter.QuickSubItem?>?)
    }

    init {
        val size = MainApp.dp40
        val image = MyRoundImage(context)
        image.setScaleType(ScaleType.CENTER_CROP)
        image.setCircleRadius(size / 2.0f)
        image.measure(View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY))
        image.layout(0, 0, size, size)
        iconImage = image
        val icon = startIcon
        startIcon = null
        val list = quickItems
        if (list == null || list.isEmpty()) {
            loadIcon(itemUrl, itemLabel, icon, startColor, if (largeIcon) 32 else 18)
            runAsync(Runnable { quickItems = DbBookQuick.subItems(appContext, itemUrl) })
        } else {
            startCustomIcon = true
            customIcon = true
            drawCollage()
        }
        setSheet {
            Row(
                Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Box(Modifier.size(48.dp).clip(CircleShape).clickable(enabled = !busy) { openMenu() }) {
                        val bmp = preview
                        if (bmp != null) Image(bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(48.dp))
                    }
                    com.mycompany.app.ui.components.BubbleMenu(expanded = menu, onDismiss = { menu = false }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(if (editMode) AppR.string.default_title else AppR.string.web_title)) },
                            onClick = { menu = false; pick(0) },
                        )
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.image)) }, onClick = { menu = false; pick(1) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.camera)) }, onClick = { menu = false; pick(2) })
                        DropdownMenuItem(text = { Text(stringResource(AppR.string.color_title)) }, onClick = { menu = false; pick(3) })
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (!editMode && !iconLoading) applyColorIcon(it.text.ifEmpty { null })
                    },
                    placeholder = { Text(stringResource(AppR.string.name)) },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(imeAction = if (editMode) ImeAction.Done else ImeAction.Next),
                    keyboardActions = KeyboardActions(onDone = { apply() }),
                    modifier = Modifier.weight(1f).padding(start = 16.dp),
                )
                if (!editMode) {
                    // reloads the icon from the website (2026-11-20)
                    Box(Modifier.padding(start = 4.dp).size(48.dp), contentAlignment = Alignment.Center) {
                        if (fetching) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        else IconButton(onClick = { refreshIcon() }, enabled = !busy) {
                            Icon(painterResource(AppR.drawable.outline_refresh_black_24), contentDescription = stringResource(AppR.string.refresh))
                        }
                    }
                }
            }
            if (!editMode) {
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(stringResource(AppR.string.url)) },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { apply() }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                )
            }
            if (busy) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { if (saveTask != null) cancelSave() else apply() }, enabled = mainEnabled) { Text(stringResource(mainRes)) }
            }
        }
        show()
    }

    /** The name as typed. */
    fun nameText(): String = name.text.trim { it <= ' ' }

    private fun refresh() {
        preview = MainUtil.viewSnapshot(iconImage, 1.0f)
    }

    /** Refreshes now and a little later (the collage and letters may draw late). */
    private fun refreshSoon() {
        refresh()
        ui.postDelayed({ if (iconImage != null) refresh() }, 300)
        ui.postDelayed({ if (iconImage != null) refresh() }, 1000)
    }

    private fun drawCollage() {
        iconImage?.setFolder(1, quickItems, SyncSettings.secretMode.value, if (AppState.darkUi) -11513776 else -460552)
        refreshSoon()
    }

    /** Shows image [bitmap] as the icon. */
    fun setIcon(bitmap: Bitmap?) {
        val image = iconImage ?: return
        iconLoading = true
        iconBitmap = bitmap
        iconColor = 0
        image.setBackColor(0)
        image.setImageBitmap(bitmap)
        refresh()
    }

    /** Refresh button: asks the site of the address field for its icon and shows it (saved with Apply). */
    private fun refreshIcon() {
        if (fetching || busy) return
        val link = url.text.trim { it <= ' ' }
        if (TextUtils.isEmpty(link)) {
            showToast(appContext, AppR.string.input_url)
            return
        }
        val ctx = appContext ?: return
        fetching = true
        iconTouched = true
        runAsync(Runnable {
            val bitmap = QuickIcons.fromSite(ctx, link)
            ui.post {
                fetching = false
                if (iconImage == null) return@post
                if (MainUtil.isBitmapOk(bitmap)) {
                    customIcon = false
                    letterChosen = false
                    iconChanged = true
                    setIcon(bitmap)
                } else {
                    showToast(ctx, AppR.string.no_icon)
                }
            }
        })
    }

    private fun openMenu() {
        if (hostActivity == null) return
        if (AlbumSettings.notiQuick.value) {
            AlbumSettings.edit(appContext) { set(AlbumSettings.notiQuick, false) }
        }
        cameraUri = null
        picturePath = null
        menu = true
    }

    /** Menu pick: 0 = web title icon (folders: default collage), 1 = image, 2 = camera, 3 = color. */
    private fun pick(color: Int) {
        val act = hostActivity ?: return
        iconTouched = true
        when (color) {
            1 -> requestStoragePermission(act, RequestCode.CAMERA_OR_FILE)
            2 -> if (!requestCamera(act, 30)) cameraUri = MainUtil.newMediaUri(RequestCode.CAMERA_OR_FILE, act, false)
            3 -> {
                if (iconDialog != null || colorDialog != null) return
                val dialogQuickColor = DialogQuickColor(act, iconColor, object : QuickColorListener {
                    override fun onColor(color: Int) {
                        colorDialog?.dismiss()
                        colorDialog = null
                        if (iconImage == null) return
                        customIcon = false
                        letterChosen = true
                        iconLoading = false
                        iconBitmap = null
                        iconColor = color
                        applyColorIcon(nameText())
                    }
                })
                colorDialog = dialogQuickColor
                dialogQuickColor.setOnDismissListener(DialogInterface.OnDismissListener {
                    colorDialog?.dismiss()
                    colorDialog = null
                })
            }
            else -> {
                if (editMode) {
                    customIcon = true
                    iconLoading = false
                    iconBitmap = null
                    iconColor = 0
                    val list = quickItems
                    if (list == null || list.isEmpty()) {
                        runAsync(Runnable {
                            quickItems = DbBookQuick.subItems(appContext, itemUrl)
                            ui.post { drawCollage() }
                        })
                    } else {
                        drawCollage()
                    }
                    return
                }
                if (iconDialog != null || colorDialog != null) return
                val link = url.text.trim { it <= ' ' }
                if (TextUtils.isEmpty(link)) {
                    showToast(appContext, AppR.string.input_url)
                    return
                }
                val dialogQuickColor = DialogQuickIcon(act, link, object : QuickLoadListener {
                    override fun onIcon(bitmap: Bitmap?) {
                        if (iconImage == null || !MainUtil.isBitmapOk(bitmap)) return
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

    private fun apply() {
        if (applied || busy) return
        applied = true
        save()
        applied = false
    }

    /** Cancel: stops the save ("Canceling"), or closes. */
    fun cancelSave() {
        if (saveTask == null) {
            dismiss()
            return
        }
        mainEnabled = false
        mainRes = AppR.string.canceling
        saveTask?.cancelled = true
        saveTask = null
    }

    /** Apply: checks name / URL / duplicates (unchanged edits just close), then saves. */
    fun save() {
        val label = nameText()
        val start = startColor
        if (editMode) {
            if (!iconChanged && iconColor == start && customIcon == startCustomIcon && label == itemLabel) {
                dismiss()
                return
            }
            saveTask?.cancelled = true
            saveTask = null
            val task = DialogTask(this, itemUrl, itemUrl, label)
            saveTask = task
            task.start(appContext)
            return
        }
        if (TextUtils.isEmpty(label)) {
            showToast(appContext, AppR.string.input_name)
            return
        }
        val link = url.text.trim { it <= ' ' }
        if (TextUtils.isEmpty(link)) {
            showToast(appContext, AppR.string.input_url)
            return
        }
        if (largeIcon && link == itemUrl) {
            if (!iconChanged && iconColor == start && customIcon == startCustomIcon && label == itemLabel) {
                dismiss()
                return
            }
        } else if (DbBookQuick.hasPath(appContext, link)) {
            url = TextFieldValue(link, TextRange(0, link.length))
            showToast(appContext, AppR.string.already_added)
            return
        }
        saveTask?.cancelled = true
        saveTask = null
        val task = DialogTask(this, itemUrl, link, label)
        saveTask = task
        task.start(appContext)
    }

    /** Color icon: folder color icon, or the color with the first letter of [str]. */
    fun applyColorIcon(title: String?) {
        val image = iconImage ?: return
        var color = iconColor
        if (editMode) {
            if (color == 0) {
                color = -65536
                iconColor = -65536
            }
            image.showIcon(0, DbBookQuick.iconRes(color))
            refresh()
            return
        }
        if (color == 0 && TextUtils.isEmpty(title)) {
            image.showIcon(0, AppR.drawable.outline_public_black_24)
            refresh()
            return
        }
        var iconColorNow = iconColor
        if (iconColorNow == 0) {
            iconColorNow = DbBookQuick.nextColor()
            iconColor = iconColorNow
        }
        image.showLetterNeutral(iconColorNow, title, null)
        refresh()
    }

    /** Icon for URL / image file [str] ([listType] list type, 0 = file), name [str2], bitmap, color [newIconColor]. */
    fun loadIcon(path: String?, itemLabel: String?, bitmap: Bitmap?, newIconColor: Int, listType: Int) {
        val image = iconImage ?: return
        customIcon = false
        if (TextUtils.isEmpty(path)) {
            iconLoading = false
            iconBitmap = null
            applyColorIcon(itemLabel)
            return
        }
        if (MainUtil.isBitmapOk(bitmap)) {
            setIcon(bitmap)
            return
        }
        if (largeIcon && !editMode && !letterChosen && listType == 32) {
            // an existing shortcut: the same picture as its tile (stored icon, saved favicon, the site's large icon);
            // its colour is only the colour of the letter shown until then or when there is none (2026-11-20)
            val secret = SyncSettings.secretMode.value
            val cached = QuickIcons.cached(path, secret)
            if (cached != null) {
                setIcon(cached)
                startColor = 0
                return
            }
            iconLoading = false
            iconBitmap = null
            iconColor = if (newIconColor != -460552) newIconColor else 0
            applyColorIcon(itemLabel)
            startColor = iconColor
            val ctx = appContext ?: return
            runAsync(Runnable {
                val loaded = QuickIcons.load(ctx, path, secret)
                ui.post {
                    if (iconImage == null || iconTouched || customIcon || !MainUtil.isBitmapOk(loaded)) return@post
                    setIcon(loaded)
                    startColor = 0
                }
            })
            return
        }
        if (newIconColor != 0 && newIconColor != -460552) {
            iconLoading = false
            iconBitmap = null
            iconColor = newIconColor
            applyColorIcon(itemLabel)
            return
        }
        val item = ChildItem()
        item.listType = listType
        item.path = path
        item.filePath = path
        val secret = SyncSettings.secretMode.value
        item.isSecret = secret
        item.itemType = if (listType == 0) 1.toByte() else 11.toByte()
        val cached = if (listType == 32) MainListLoader.cachedListIcon(path, secret) else MainListLoader.cachedIconFor(item)
        if (MainUtil.isBitmapOk(cached)) {
            setIcon(cached)
            return
        }
        val ctx = appContext ?: return
        listLoader?.release()
        listLoader = MainListLoader(ctx, false, object : ListLoadListener {
            override fun onFailed(childItem: ChildItem?, view: View?) {
                if (customIcon || iconImage == null) return
                iconLoading = false
                iconBitmap = null
                applyColorIcon(nameText())
            }

            override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                if (customIcon || iconImage == null) return
                if (!MainUtil.isBitmapOk(bitmap)) {
                    iconLoading = false
                    iconBitmap = null
                    applyColorIcon(nameText())
                } else {
                    setIcon(bitmap)
                }
            }
        })
        if (editMode) image.showIcon(0, DbBookQuick.iconRes(-65536)) else image.showLetterNeutral(if (AppState.darkUi) -11513776 else -460552, itemLabel, null)
        refresh()
        val target = View(ctx)
        target.setTag(0)
        listLoader!!.enqueue(item, target)
    }

    override fun cancel() {
        cancelSave()
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
        iconImage?.release()
        iconImage = null
        hostActivity = null
        appContext = null
        editListener = null
        cameraUri = null
        picturePath = null
        super.dismiss()
    }

    class DialogTask(dialogQuickEdit: DialogQuickEdit?, oldPath: String?, path: String?, title: String?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = WeakReference<Any?>(dialogQuickEdit)
        @JvmField
        var old: String? = oldPath
        @JvmField
        var path: String? = path
        @JvmField
        var title: String? = title
        @JvmField
        var taskColor: Int = 0
        @JvmField
        var total: Int = 0
        @JvmField
        var changed: Boolean = false

        init {
            val d = dialogRef!!.get() as DialogQuickEdit?
            if (d != null) {
                taskColor = d.iconColor
                d.setCanceledOnTouchOutside(false)
                d.busy = true
                d.mainRes = AppR.string.cancel
            }
        }

        public override fun doInBackground() {
            val d = dialogRef!!.get() as DialogQuickEdit? ?: return
            val folder = d.editMode
            if (cancelled) return
            val editing = d.largeIcon
            val title = title
            val path = path
            try {
                if (editing || folder) {
                    val old = old
                    if (TextUtils.isEmpty(old)) return
                    if (!folder && (TextUtils.isEmpty(path) || TextUtils.isEmpty(title))) return
                    val args = arrayOf<String?>(if (SyncSettings.secretMode.value) "1" else "0", old)
                    val values = ContentValues()
                    if (folder) values.put("_rsv6", if (!d.customIcon) 1 else 0) else values.put("_path", path)
                    values.put("_title", title)
                    if (MainUtil.isBitmapOk(d.iconBitmap)) {
                        taskColor = 0
                        try {
                            val out = ByteArrayOutputStream()
                            d.iconBitmap!!.compress(CompressFormat.PNG, 100, out)
                            values.put("_icon", out.toByteArray())
                            out.close()
                            values.put("_rsv4", 0 as Int?)
                            MainListLoader.cacheListIcon(path, d.iconBitmap, SyncSettings.secretMode.value)
                        } catch (unused: Exception) {
                        }
                    } else {
                        var color = taskColor
                        if (color == 0) {
                            color = DbBookQuick.nextColor()
                            taskColor = color
                        }
                        values.put("_rsv4", color)
                        // the 1-byte marker ("letter chosen") only when the user chose it; otherwise the tile keeps
                        // looking for the site's icon (before 2026-11-20 every edit of a name turned it into a letter)
                        if (d.letterChosen || folder) values.put("_icon", ByteArray(1))
                    }
                    DbUtil.updateWhere(DbBookQuick.get(d.appContext).getWritableDatabase(), "DbBookQuick_table", values, "_secret=? AND _path=?", args)
                    changed = true
                } else {
                    if (TextUtils.isEmpty(path) || TextUtils.isEmpty(title)) return
                    total = DbBookQuick.folderCount(d.appContext)
                    val values = android.content.ContentValues().also { cv -> cv.put("_path", path); cv.put("_title", title) }
                    values.put("_order", total)
                    values.put("_secret", if (SyncSettings.secretMode.value) 1 else 0)
                    if (MainUtil.isBitmapOk(d.iconBitmap)) {
                        taskColor = 0
                        try {
                            val out = ByteArrayOutputStream()
                            d.iconBitmap!!.compress(CompressFormat.PNG, 100, out)
                            values.put("_icon", out.toByteArray())
                            out.close()
                            values.put("_rsv4", 0 as Int?)
                            MainListLoader.cacheListIcon(path, d.iconBitmap, SyncSettings.secretMode.value)
                        } catch (unused: Exception) {
                        }
                    } else {
                        var color = taskColor
                        if (color == 0) {
                            color = DbBookQuick.nextColor()
                            taskColor = color
                        }
                        values.put("_rsv4", color)
                    }
                    DbUtil.insert(DbBookQuick.get(d.appContext).getWritableDatabase(), "DbBookQuick_table", values)
                    changed = true
                }
            } catch (unused: Exception) {
            }
        }

        public override fun onCancelled() {
            val d = dialogRef!!.get() as DialogQuickEdit? ?: return
            d.saveTask = null
            d.dismiss()
        }

        public override fun onPostExecute() {
            val d = dialogRef!!.get() as DialogQuickEdit? ?: return
            d.saveTask = null
            if (!changed) {
                if (d.appContext != null) {
                    d.setCanceledOnTouchOutside(true)
                    d.busy = false
                    d.mainRes = AppR.string.apply
                    showToast(d.appContext, AppR.string.update_fail)
                }
                return
            }
            if (!d.largeIcon) {
                d.editListener?.onSaved(path, taskColor, total, title)
                return
            }
            if (d.customIcon) d.editListener?.onSavedCustom(old, path, title, taskColor, d.quickItems) else d.editListener?.onSavedCustom(old, path, title, taskColor, null)
        }
    }

    companion object {
    }
}
