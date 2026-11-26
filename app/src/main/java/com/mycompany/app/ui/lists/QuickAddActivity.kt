package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import android.Manifest
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.BrowserSettings
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.quick.QuickIcons
import com.mycompany.app.dialog.DialogQuickColor
import com.mycompany.app.dialog.DialogQuickColor.QuickColorListener
import com.mycompany.app.dialog.DialogQuickIcon
import com.mycompany.app.dialog.DialogQuickIcon.QuickLoadListener
import com.mycompany.app.list.book.ListBookWeb
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsTopBar
import com.mycompany.app.ui.viewer.ImageCropActivity
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Collections
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import com.mycompany.app.ui.components.Sym

/**
 * Adds a shortcut (quick access tile): name, address and picture. The picture is a letter on a colour (pick the colour),
 * the site's own icon, a gallery picture cropped to a square or a camera picture. Name and address can be filled from the
 * current page (`EXTRA_PATH` + `EXTRA_NAME`), the bookmarks (folders open) or the recent history. Save checks that the
 * address is not already a shortcut, stores it and returns `EXTRA_PATH`, `EXTRA_NAME`, `EXTRA_ICON` (colour) and
 * `EXTRA_INDEX` (order). Replaces `QuickAdd`.
 */
class QuickAddActivity : OpumComposeActivity() {

    private var customIcon by mutableStateOf<Bitmap?>(null)
    private var iconColor by mutableIntStateOf(0)
    private var saving by mutableStateOf(false)
    /** The refresh button is asking the site for its icon. */
    private var fetching by mutableStateOf(false)
    private var croppedPath: String? = null
    private var cameraUri: Uri? = null
    private val punct: Pattern = Pattern.compile("\\p{Punct}")
    private var loader: MainListLoader? = null
    private val pending = HashMap<String, (Bitmap?) -> Unit>()

    private val loadListener = object : ListLoadListener {
        override fun onFailed(childItem: ChildItem?, view: View?) {
            childItem?.path?.let { pending.remove(it)?.invoke(null) }
        }

        override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
            childItem?.path?.let { pending.remove(it)?.invoke(if (MainUtil.isBitmapOk(bitmap)) bitmap else null) }
        }
    }

    private fun faviconItem(url: String?, title: String?, type: Int): ChildItem {
        val item = ChildItem()
        item.listType = type
        item.itemType = 11.toByte()
        item.path = url
        item.name = title
        item.filePath = url
        return item
    }

    /** The cached picture of a web address, or loaded through the old icon loader; null when there is none. */
    private suspend fun favicon(url: String?, title: String?, type: Int): Bitmap? {
        if (url.isNullOrEmpty()) return null
        val item = faviconItem(url, title, type)
        val cached = withContext(Dispatchers.IO) { MainListLoader.cachedIconFor(item) }
        if (MainUtil.isBitmapOk(cached)) return cached
        return suspendCancellableCoroutine { cont ->
            val current = loader ?: MainListLoader(applicationContext, false, loadListener).also { loader = it }
            pending[url] = { bitmap -> if (cont.isActive) cont.resume(bitmap) }
            cont.invokeOnCancellation { pending.remove(url) }
            current.enqueue(item, ImageView(this))
        }
    }

    private fun nextColor(): Int {
        if (iconColor == 0) iconColor = DbBookQuick.nextColor()
        return iconColor
    }

    private fun save(name: String, url: String, onDone: () -> Unit) {
        if (saving) return
        saving = true
        val context = applicationContext
        Thread {
            val exists = DbBookQuick.hasPath(context, url)
            var order = 0
            var color = iconColor
            if (!exists) {
                order = DbBookQuick.folderCount(context)
                val values = ContentValues()
                values.put("_path", url)
                values.put("_title", name)
                values.put("_order", order)
                values.put("_secret", com.mycompany.app.pref.store.Workspaces.sharedNumber())
                val bitmap = customIcon
                if (MainUtil.isBitmapOk(bitmap)) {
                    color = 0
                    try {
                        val out = ByteArrayOutputStream()
                        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                        values.put("_icon", out.toByteArray())
                        out.close()
                        MainListLoader.cacheListIcon(url, bitmap, SyncSettings.secretMode.value)
                    } catch (_: Exception) {
                    }
                } else {
                    if (color == 0) color = DbBookQuick.nextColor()
                    values.put("_rsv4", color)
                }
                DbUtil.insert(DbBookQuick.get(context).writableDatabase, "DbBookQuick_table", values)
            }
            runOnUiThread {
                saving = false
                if (exists) {
                    MainUtil.showToast(this, R.string.already_added)
                } else {
                    setResult(RESULT_OK, Intent().putExtra("EXTRA_PATH", url).putExtra("EXTRA_NAME", name).putExtra("EXTRA_ICON", color).putExtra("EXTRA_INDEX", order))
                    onDone()
                    finish()
                }
            }
        }.start()
    }

    // ---- import lists ----

    private fun loadBookmarks(folder: String?, secret: Boolean): ArrayList<ChildItem> {
        var dir = MainUtil.trimSlashes(folder)
        dir = if (TextUtils.isEmpty(dir)) "/" else "/$dir"
        val list = ArrayList<ChildItem>()
        try {
            DbUtil.query(
                DbBookWeb.get(applicationContext).writableDatabase, "DbBookWeb_table",
                arrayOf("_id", "_isdir", "_path", "_title", "_time", "_rsv4"),
                "_secret=? AND _dir=?", arrayOf(com.mycompany.app.pref.store.Workspaces.sharedKey(), dir), null,
            )?.use { cursor ->
                val id = cursor.getColumnIndex("_id")
                val isDir = cursor.getColumnIndex("_isdir")
                val path = cursor.getColumnIndex("_path")
                val title = cursor.getColumnIndex("_title")
                val time = cursor.getColumnIndex("_time")
                val rsv = cursor.getColumnIndex("_rsv4")
                while (cursor.moveToNext()) {
                    val item = ChildItem()
                    item.listType = 17
                    item.id = cursor.getLong(id)
                    item.isDir = cursor.getInt(isDir) == 1
                    item.dir = dir
                    item.name = cursor.getString(title)
                    item.time = cursor.getLong(time)
                    if (!item.isDir) {
                        item.path = cursor.getString(path)
                        if (TextUtils.isEmpty(item.path)) continue
                    } else {
                        item.path = if (dir == "/") "/" + item.name else dir + "/" + item.name
                    }
                    if (ListSettings.bookWebUser.value) item.order = cursor.getLong(rsv) else ListBookWeb.lowerTitle(item)
                    list.add(item)
                }
            }
        } catch (_: Exception) {
        }
        try {
            if (ListSettings.bookWebUser.value) Collections.sort(list, MainUtil.SortChildUser())
            else Collections.sort(list, MainUtil.childComparator(0, ListSettings.bookWebItem.value, ListSettings.bookWebRvse.value))
        } catch (_: Exception) {
        }
        if (dir != "/") {
            val up = ChildItem()
            up.rowType = 1.toByte()
            up.isDir = true
            up.name = ".."
            up.path = MainUtil.uriDisplayName(null, dir)
            list.add(0, up)
        }
        return list
    }

    private fun loadHistory(secret: Boolean): ArrayList<ChildItem> {
        val list = ArrayList<ChildItem>()
        if (BrowserSettings.historyTime.value == 0) return list
        if (SyncSettings.secretMode.value && SecretSettings.secretHist.value == 0L && secret) return list
        var since = 0L
        if (BrowserSettings.historyTime.value != -1) {
            since = System.currentTimeMillis() - BrowserSettings.historyTime.value * 86400000L
            if (SyncSettings.secretMode.value && SecretSettings.secretHist.value > since) since = SecretSettings.secretHist.value
        }
        try {
            DbUtil.query(
                DbBookHistory.get(applicationContext).writableDatabase, "DbBookHistory_table",
                arrayOf("_id", "_path", "_title"), "_secret=? AND _time>?",
                arrayOf(com.mycompany.app.pref.store.Workspaces.keyFor(secret), since.toString()), "_time DESC",
            )?.use { cursor ->
                val id = cursor.getColumnIndex("_id")
                val path = cursor.getColumnIndex("_path")
                val title = cursor.getColumnIndex("_title")
                while (cursor.moveToNext() && list.size < 100) {
                    val item = ChildItem()
                    item.listType = 18
                    item.id = cursor.getLong(id)
                    item.path = cursor.getString(path)
                    item.name = cursor.getString(title)
                    list.add(item)
                }
            }
        } catch (_: Exception) {
        }
        return list
    }

    // ---- screen ----

    @OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val startUrl = remember { intent.getStringExtra("EXTRA_PATH") }
        val startName = remember { intent.getStringExtra("EXTRA_NAME") }
        val hasStart = !startUrl.isNullOrEmpty() && !startName.isNullOrEmpty()
        var name by remember { mutableStateOf(if (hasStart) startName else "") }
        var url by remember { mutableStateOf(if (hasStart) startUrl else "") }
        var iconMenu by remember { mutableStateOf(false) }
        var urlMenu by remember { mutableStateOf(false) }
        var tab by remember { mutableIntStateOf(0) }
        var folder by remember { mutableStateOf("/") }
        var bookSecret by remember { mutableStateOf(SyncSettings.secretMode.value) }
        var histSecret by remember { mutableStateOf(SyncSettings.secretMode.value) }
        var books by remember { mutableStateOf<List<ChildItem>>(emptyList()) }
        var history by remember { mutableStateOf<List<ChildItem>>(emptyList()) }
        var loaded by remember { mutableStateOf(false) }
        val scope = androidx.compose.runtime.rememberCoroutineScope()

        LaunchedEffect(folder, bookSecret) { books = withContext(Dispatchers.IO) { loadBookmarks(folder, bookSecret) }; loaded = true }
        LaunchedEffect(histSecret) { history = withContext(Dispatchers.IO) { loadHistory(histSecret) } }
        // the current page's site icon to start with (the tile would show it anyway), unless a picture was chosen meanwhile
        LaunchedEffect(Unit) {
            if (!hasStart) return@LaunchedEffect
            val wanted = startUrl
            val bitmap = withContext(Dispatchers.IO) { QuickIcons.load(applicationContext, wanted, SyncSettings.secretMode.value) }
            if (bitmap != null && customIcon == null && iconColor == 0 && url == wanted) customIcon = bitmap
        }
        /** Refresh button (2026-11-20): asks the site of the address for its icon. */
        fun refreshIcon() {
            val address = url.trim()
            if (address.isEmpty()) { MainUtil.showToast(this, R.string.input_url); return }
            if (fetching) return
            fetching = true
            scope.launch {
                val bitmap = withContext(Dispatchers.IO) { QuickIcons.fromSite(applicationContext, address) }
                fetching = false
                if (bitmap != null) { customIcon = bitmap; iconColor = 0 } else MainUtil.showToast(this@QuickAddActivity, R.string.no_icon)
            }
        }
        fun takeOver(title: String?, address: String?, type: Int) {
            name = title ?: ""
            url = address ?: ""
            customIcon = null
            iconColor = 0
            val wanted = address
            scope.launch {
                val bitmap = favicon(wanted, title, type)
                if (bitmap != null && url == wanted) { customIcon = bitmap; iconColor = 0 }
            }
        }

        val cropLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val path = croppedPath
            croppedPath = null
            if (result.resultCode == RESULT_OK && path != null && File(path).exists()) {
                customIcon = BitmapFactory.decodeFile(path)
                iconColor = 0
            } else if (result.resultCode == RESULT_OK) {
                MainUtil.showToast(this, R.string.invalid_path)
            }
        }
        fun cropIcon(uri: Uri) {
            val target = MainUtil.newCacheFilePath(applicationContext)
            if (target.isNullOrEmpty()) { MainUtil.showToast(this, R.string.invalid_path); return }
            croppedPath = target
            cropLauncher.launch(Intent(this, ImageCropActivity::class.java).setData(uri).putExtra("EXTRA_DST", target).putExtra("EXTRA_ICON", true))
        }
        val pick = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) { MainUtil.keepUriPermission(applicationContext, uri); cropIcon(uri) }
        }
        val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
            val uri = cameraUri
            cameraUri = null
            if (ok && uri != null) cropIcon(uri) else MainUtil.showToast(this, R.string.invalid_path)
        }
        fun takePhoto() {
            val base = MainUtil.newCacheFilePath(applicationContext)
            val uri = if (base.isNullOrEmpty()) null else MainUtil.fileProviderUri(applicationContext, "$base.jpg")
            if (uri == null) { MainUtil.showToast(this, R.string.not_supported); return }
            cameraUri = uri
            try { camera.launch(uri) } catch (_: Exception) { MainUtil.showToast(this, R.string.not_supported) }
        }
        val cameraPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { if (it) takePhoto() }

        fun submit() {
            val forget = name.trim()
            val title = url.trim()
            if (forget.isEmpty()) { MainUtil.showToast(this, R.string.input_name); return }
            if (title.isEmpty()) { MainUtil.showToast(this, R.string.input_url); return }
            save(forget, title) {}
        }

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = stringResource(R.string.quick_access),
                    onBack = { finish() },
                    actions = { IconButton(onClick = { submit() }) { Icon(Sym.Check, contentDescription = stringResource(R.string.save)) } },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box {
                        IconPreview(customIcon, iconColor, name.ifEmpty { url }, Modifier.size(64.dp).clickable { iconMenu = true })
                        com.mycompany.app.ui.components.BubbleMenu(expanded = iconMenu, onDismiss = { iconMenu = false }) {
                            DropdownMenuItem(text = { Text(stringResource(R.string.web_title)) }, onClick = {
                                iconMenu = false
                                val address = url.trim()
                                if (address.isEmpty()) { MainUtil.showToast(this@QuickAddActivity, R.string.input_url); return@DropdownMenuItem }
                                DialogQuickIcon(this@QuickAddActivity, address, object : QuickLoadListener {
                                    override fun onIcon(bitmap: Bitmap?) {
                                        if (MainUtil.isBitmapOk(bitmap)) { customIcon = bitmap; iconColor = 0 }
                                    }
                                })
                            })
                            DropdownMenuItem(text = { Text(stringResource(R.string.image)) }, onClick = {
                                iconMenu = false
                                try { pick.launch(arrayOf("image/*")) } catch (_: Exception) { MainUtil.showToast(this@QuickAddActivity, R.string.apps_none) }
                            })
                            DropdownMenuItem(text = { Text(stringResource(R.string.camera)) }, onClick = {
                                iconMenu = false
                                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) takePhoto()
                                else cameraPermission.launch(Manifest.permission.CAMERA)
                            })
                            DropdownMenuItem(text = { Text(stringResource(R.string.color_title)) }, onClick = {
                                iconMenu = false
                                DialogQuickColor(this@QuickAddActivity, iconColor, object : QuickColorListener {
                                    override fun onColor(color: Int) { customIcon = null; iconColor = color }
                                })
                            })
                        }
                    }
                    // reloads the icon from the website
                    Box(Modifier.padding(top = 4.dp).size(48.dp), contentAlignment = Alignment.Center) {
                        if (fetching) androidx.compose.material3.CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        else IconButton(onClick = { refreshIcon() }) { Icon(painterResource(R.drawable.outline_refresh_black_24), contentDescription = stringResource(R.string.refresh)) }
                    }
                    }
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it }, singleLine = true,
                            label = { Text(stringResource(R.string.name)) },
                            trailingIcon = { if (name.isNotEmpty()) IconButton(onClick = { name = "" }) { Icon(Sym.Close, null) } },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = url, onValueChange = { url = it }, singleLine = true,
                            label = { Text(stringResource(R.string.url)) },
                            trailingIcon = {
                                Box {
                                    if (url.isNotEmpty()) IconButton(onClick = { url = "" }) { Icon(Sym.Close, null) }
                                    else TextButton(onClick = { urlMenu = true }) { Text("https://") }
                                    com.mycompany.app.ui.components.BubbleMenu(expanded = urlMenu, onDismiss = { urlMenu = false }) {
                                        DropdownMenuItem(text = { Text("https://") }, onClick = { urlMenu = false; url = "https://" })
                                        DropdownMenuItem(text = { Text("www.") }, onClick = { urlMenu = false; url = "www." })
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    }
                }
                if (hasStart) {
                    AppListItem(
                        headlineContent = { Text(stringResource(R.string.current_page)) },
                        supportingContent = { Text(startUrl, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.clickable { takeOver(startName, startUrl, 18) },
                    )
                }
                SecondaryTabRow(selectedTabIndex = tab, containerColor = TabRowDefaults.primaryContainerColor, contentColor = TabRowDefaults.primaryContentColor) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.bookmark)) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.history)) })
                }
                val shown = if (tab == 0) books else history
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(shown, key = { "${it.id}-${it.path}-${it.name}" }) { item ->
                            val folderItem = item.isDir
                            AppListItem(
                                leadingContent = {
                                    if (folderItem) {
                                        Icon(painterResource(if (item.rowType.toInt() == 1) R.drawable.outline_shift_2_black_24 else R.drawable.outline_folder_black_24), null, Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurface)
                                    } else {
                                        var bitmap by remember(item.path) { mutableStateOf<Bitmap?>(null) }
                                        LaunchedEffect(item.path) { bitmap = favicon(item.path, item.name, item.listType) }
                                        IconPreview(bitmap, -460552, item.name ?: item.path, Modifier.size(40.dp))
                                    }
                                },
                                headlineContent = { Text(item.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                supportingContent = if (folderItem) null else ({ Text(item.path ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) }),
                                modifier = Modifier.clickable {
                                    if (folderItem) { folder = item.path ?: "/" } else takeOver(item.name, item.path, item.listType)
                                },
                            )
                        }
                    }
                    if (shown.isEmpty()) {
                        Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                            if (SyncSettings.secretMode.value) {
                                // bookmarks are the space's own also in incognito: only history has a "normal" set to show
                                val secret = if (tab == 0) false else histSecret
                                if (secret) {
                                    TextButton(onClick = {
                                        if (tab == 0) { bookSecret = false; folder = "/" } else histSecret = false
                                    }) { Text(stringResource(R.string.import_normal)) }
                                } else {
                                    Text(stringResource(if (tab == 0) R.string.import_no_book else R.string.import_no_history))
                                }
                            } else if (loaded) {
                                Text(stringResource(if (tab == 0) R.string.import_no_book else R.string.import_no_history))
                            }
                        }
                    }
                }
            }
        }
    }

    /** The picture of a shortcut: a bitmap when there is one, else the first letters on a colour. */
    @Composable
    private fun IconPreview(bitmap: Bitmap?, color: Int, label: String?, modifier: Modifier) {
        if (bitmap != null) {
            Image(bitmap.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = modifier.clip(CircleShape))
        } else {
            val letters = MainUtil.stripPunctuation(label, punct) ?: ""
            val base = if (color == 0) (if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) Color(-14606047) else Color(-460552)) else Color(color)
            Box(modifier.clip(CircleShape).background(base), contentAlignment = Alignment.Center) {
                if (letters.isEmpty()) {
                    Icon(painterResource(R.drawable.outline_public_black_24), null, tint = MaterialTheme.colorScheme.onSurface)
                } else {
                    Text(letters, color = if (base.luminance() > 0.5f) Color.Black else Color.White, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    private fun Color.luminance(): Float = (0.299f * red + 0.587f * green + 0.114f * blue)
}
