package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppListItem
import android.content.Context
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.PathSettings
import com.mycompany.app.pref.store.ZoneSettings
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.text.TextUtils
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSaveConfirm
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.InfoDialog
import com.mycompany.app.ui.components.SettingsTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** A selectable font file. [path] is a file path, a SAF document URI, or `app://package,dir,file` (an asset of another app). */
private class FontEntry(val path: String, val name: String)

/**
 * Custom font for the reader mode (`EXTRA_PAGE` true) or for web pages: pick a font from other font apps, the system
 * or a storage folder (TTF only), see it in a preview, optionally bold. The chosen font file is copied to the app's
 * font file only when saved, so leaving without saving changes nothing. Replaces the old view-pager screen with its
 * WebView preview.
 */
class FontSettingsActivity : OpumComposeActivity() {

    companion object {
        const val EXTRA_PAGE = "EXTRA_PAGE"

        /** Copies the font at [path] (see [FontEntry]) to [dest]. */
        private fun copyFont(context: Context, path: String?, dest: String?): Boolean {
            if (TextUtils.isEmpty(path) || TextUtils.isEmpty(dest)) return false
            if (!path!!.startsWith("app://")) return MainUtil.copyFileOrUri(context, path, dest)
            val parts = path.substring(6).split(",")
            if (parts.size != 3 || parts.any { it.isEmpty() }) return false
            val assets: AssetManager = try {
                context.createPackageContext(parts[0], 0).assets
            } catch (_: Exception) {
                return false
            }
            val file = if (parts[1] == "/") parts[2] else parts[1] + "/" + parts[2]
            var input: InputStream? = null
            var output: OutputStream? = null
            return try {
                input = assets.open(file)
                output = MainUtil.openFileOutput(dest, false)
                input.copyTo(output!!)
                true
            } catch (_: Exception) {
                false
            } finally {
                try { output?.close() } catch (_: Exception) {}
                try { input?.close() } catch (_: Exception) {}
            }
        }

        private fun isTtf(name: String?): Boolean = name != null && name.lowercase().endsWith(".ttf")

        /** TTF files in `/system/fonts`. */
        private fun systemFonts(): List<FontEntry> {
            val files = File("/system/fonts").listFiles() ?: return emptyList()
            return files.filter { isTtf(it.name) && it.length() != 0L }
                .map { FontEntry(it.path, it.name) }
                .sortedBy { it.name.lowercase() }
        }

        /** TTF assets (also one folder level down) of installed non-system apps whose package name contains "font". */
        private fun appFonts(context: Context): List<FontEntry> {
            val result = ArrayList<FontEntry>()
            try {
                val pm = context.packageManager
                val packages = if (Build.VERSION.SDK_INT >= 33) {
                    pm.getInstalledPackages(PackageManager.PackageInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION") pm.getInstalledPackages(0)
                }
                for (info in packages) {
                    val pkg = info.packageName
                    val flags = info.applicationInfo?.flags ?: continue
                    if ((flags and 1) != 0 || !pkg.lowercase().contains("font")) continue
                    try {
                        val assets = context.createPackageContext(pkg, 0).assets
                        for (entry in assets.list("") ?: emptyArray()) {
                            if (entry.lastIndexOf('.') == -1) {
                                for (child in assets.list(entry) ?: emptyArray()) {
                                    if (isTtf(child)) result.add(FontEntry("app://$pkg,$entry,$child", child))
                                }
                            } else if (isTtf(entry)) {
                                result.add(FontEntry("app://$pkg,/,$entry", entry))
                            }
                        }
                    } catch (_: Exception) {
                    }
                }
            } catch (_: Exception) {
            }
            return result.sortedBy { it.name.lowercase() }
        }

        /** TTF files in the user's font folder (a SAF tree). */
        private fun folderFonts(context: Context, tree: String?): List<FontEntry> {
            if (TextUtils.isEmpty(tree)) return emptyList()
            val result = ArrayList<FontEntry>()
            try {
                val treeUri = Uri.parse(tree)
                val children = DocumentsContract.buildChildDocumentsUriUsingTree(
                    treeUri, DocumentsContract.getTreeDocumentId(treeUri),
                )
                context.contentResolver.query(
                    children, arrayOf("document_id", "_display_name", "_size"), null, null, null,
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val name = cursor.getString(1)
                        if (!isTtf(name) || cursor.getLong(2) == 0L) continue
                        val doc = DocumentsContract.buildDocumentUriUsingTree(treeUri, cursor.getString(0)) ?: continue
                        result.add(FontEntry(doc.toString(), name))
                    }
                }
            } catch (_: Exception) {
            }
            return result.sortedBy { it.name.lowercase() }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val reader = intent.getBooleanExtra(EXTRA_PAGE, false)
        val savedPath = remember { (if (reader) (if (ReadSettings.userFont.value) ReadSettings.fontPath.value else "") else (if (ZoneSettings.userFont.value) ZoneSettings.fontPath.value else "")) ?: "" }
        val savedBold = remember { if (reader) ReadSettings.fontBold.value else ZoneSettings.fontBold.value }
        var selected by remember { mutableStateOf(savedPath) }
        var bold by remember { mutableStateOf(savedBold) }
        var tab by remember { mutableIntStateOf(0) }
        var guide by remember { mutableStateOf(false) }
        var folder by remember { mutableStateOf(PathSettings.scanFont.value ?: "") }
        var appList by remember { mutableStateOf<List<FontEntry>?>(null) }
        var systemList by remember { mutableStateOf<List<FontEntry>?>(null) }
        var folderList by remember { mutableStateOf<List<FontEntry>?>(null) }
        var folderToast by remember { mutableStateOf(false) }
        var typeface by remember { mutableStateOf<Typeface?>(null) }
        val changed = bold != savedBold || selected != savedPath

        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                appList = appFonts(context)
                systemList = systemFonts()
            }
        }
        LaunchedEffect(folder) {
            folderList = withContext(Dispatchers.IO) { folderFonts(context, folder) }
            if (folderToast) {
                folderToast = false
                if (folderList.isNullOrEmpty()) MainUtil.showToast(context, R.string.no_found)
                else MainUtil.showToastText(context, resources.getQuantityString(R.plurals.file_found, folderList!!.size, folderList!!.size))
            }
        }
        // preview: copy the choice to a cache file and load it as a typeface
        LaunchedEffect(selected) {
            typeface = if (selected.isEmpty()) null else withContext(Dispatchers.IO) {
                try {
                    val file = File(cacheDir, "font_preview_${System.nanoTime()}.ttf")
                    if (copyFont(context, selected, file.path)) Typeface.createFromFile(file).also { file.delete() } else null
                } catch (_: Exception) {
                    null
                }
            }
            if (selected.isNotEmpty() && typeface == null) MainUtil.showToast(context, R.string.invalid_file)
        }

        val folderPick = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data?.data
            if (result.resultCode != RESULT_OK || data == null) return@rememberLauncherForActivityResult
            val path = MainUri.treeDocUri(data)
            if (TextUtils.isEmpty(path)) {
                MainUtil.showToast(context, R.string.invalid_path)
                return@rememberLauncherForActivityResult
            }
            if (path != PathSettings.scanFont.value) {
                PathSettings.edit(context) { set(PathSettings.scanFont, path) }
            }
            MainUtil.keepUriPermission(context, data)
            MainUtil.showToast(context, R.string.dir_scanning)
            folderToast = true
            folder = path ?: ""
        }

        fun pickFolder() {
            try {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                val initial = MainUri.storageVolume()
                if (!TextUtils.isEmpty(initial)) {
                    intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse(initial))
                }
                folderPick.launch(intent)
            } catch (_: Exception) {
                MainUtil.showToast(context, R.string.not_supported)
            }
        }

        fun save() {
            val dest = if (reader) MainUtil.readerFontPath(context) else MainUtil.userFontPath(context)
            if (selected.isEmpty()) {
                if (reader) { ReadSettings.editLocal { set(ReadSettings.userFont, false) }; ReadSettings.editLocal { set(ReadSettings.fontPath, "") } } else { ZoneSettings.editLocal { set(ZoneSettings.userFont, false) }; ZoneSettings.editLocal { set(ZoneSettings.fontPath, "") } }
            } else {
                if (selected != savedPath) copyFont(context, selected, dest)
                if (reader) { ReadSettings.editLocal { set(ReadSettings.userFont, true) }; ReadSettings.editLocal { set(ReadSettings.fontPath, selected) } } else { ZoneSettings.editLocal { set(ZoneSettings.userFont, true) }; ZoneSettings.editLocal { set(ZoneSettings.fontPath, selected) } }
            }
            if (reader) {
                ReadSettings.editLocal { set(ReadSettings.fontBold, bold) }
                PrefRead.get(context, false)?.let {
                    it.putBool("mUserFont", ReadSettings.userFont.value); it.putString("mFontPath", ReadSettings.fontPath.value); it.putBool("mFontBold", ReadSettings.fontBold.value); it.saveAsync()
                }
            } else {
                ZoneSettings.editLocal { set(ZoneSettings.fontBold, bold) }
                PrefZone.get(context, false)?.let {
                    it.putBool("mUserFont", ZoneSettings.userFont.value); it.putString("mFontPath", ZoneSettings.fontPath.value); it.putBool("mFontBold", ZoneSettings.fontBold.value); it.saveAsync()
                }
            }
            setResult(RESULT_OK)
        }

        fun leave() {
            if (!changed) { finish(); return }
            DialogSaveConfirm(this, object : DialogSeekListener {
                override fun onValue(newValue: Int) {
                    if (newValue == 0) save()
                    finish()
                }
            })
        }

        BackHandler { leave() }

        val title = stringResource(R.string.font) + " (" +
            stringResource(if (reader) R.string.reader_mode else R.string.web_page) + ")"

        Scaffold(
            topBar = {
                SettingsTopBar(
                    title = title,
                    onBack = { leave() },
                    actions = {
                        IconButton(onClick = { guide = true }) { Icon(Icons.Filled.Info, contentDescription = null) }
                        if (changed) {
                            IconButton(onClick = { save(); finish() }) {
                                Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.ok))
                            }
                        }
                    },
                )
            },
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(top = padding.calculateTopPadding())) {
                Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    val style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = typeface?.let { FontFamily(it) },
                        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                    )
                    Text("ABCDEabcde", style = style)
                    Text("1234567890", style = style)
                    Text(stringResource(R.string.font_preview), style = style)
                }
                AppListItem(
                    headlineContent = { Text(stringResource(R.string.font_bold)) },
                    trailingContent = { Switch(checked = bold, onCheckedChange = { bold = it }) },
                    modifier = Modifier.clickable { bold = !bold },
                )
                AppListItem(
                    headlineContent = { Text(stringResource(R.string.font_default)) },
                    leadingContent = { RadioButton(selected = selected.isEmpty(), onClick = { selected = "" }) },
                    modifier = Modifier.clickable { selected = "" },
                )
                SecondaryTabRow(selectedTabIndex = tab, containerColor = TabRowDefaults.primaryContainerColor, contentColor = TabRowDefaults.primaryContentColor) {
                    Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.system_name)) })
                    Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.storage)) })
                }
                val apps = appList.orEmpty()
                val system = systemList.orEmpty()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp + padding.calculateBottomPadding()),
                ) {
                    if (tab == 0) {
                        items(apps, key = { it.path }) { FontRow(it, selected) { path -> selected = path } }
                        if (apps.isNotEmpty() && system.isNotEmpty()) item { HorizontalDivider() }
                        items(system, key = { it.path }) { FontRow(it, selected) { path -> selected = path } }
                    } else {
                        item {
                            AppListItem(
                                headlineContent = { Text(stringResource(R.string.select_dir)) },
                                supportingContent = { if (folder.isNotEmpty()) Text(MainUri.volumeLabel(context, folder)) },
                                trailingContent = { TextButton(onClick = { pickFolder() }) { Text(stringResource(R.string.select_dir)) } },
                                modifier = Modifier.clickable { pickFolder() },
                            )
                        }
                        val files = folderList.orEmpty()
                        if (files.isEmpty() && folderList != null) {
                            item { Row(Modifier.padding(16.dp)) { Text(stringResource(R.string.no_found)) } }
                        }
                        items(files, key = { it.path }) { FontRow(it, selected) { path -> selected = path } }
                    }
                }
            }
        }

        if (guide) {
            InfoDialog(
                title = stringResource(R.string.font),
                message = stringResource(R.string.font_guide_1) + "\n" + stringResource(R.string.font_guide_2) +
                    "\n\n" + stringResource(R.string.font_guide_3),
                onDismiss = { guide = false },
            )
        }
    }
}

@Composable
private fun FontRow(entry: FontEntry, selected: String, onSelect: (String) -> Unit) {
    AppListItem(
        headlineContent = { Text(entry.name, maxLines = 1) },
        leadingContent = { RadioButton(selected = entry.path == selected, onClick = { onSelect(entry.path) }) },
        modifier = Modifier.clickable { onSelect(entry.path) },
    )
}
