package com.mycompany.app.ui.lists

import com.mycompany.app.ui.components.AppListItem
import com.mycompany.app.main.RequestCode
import android.app.Activity
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.text.TextUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.dialog.DialogDeleteBook
import com.mycompany.app.dialog.DialogDeleteBook.DeleteBookListener
import com.mycompany.app.dialog.DialogEditSearch
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsTopBar
import com.mycompany.app.web.WebSearch
import com.mycompany.app.web.WebSearch.WebSchItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.mycompany.app.ui.components.Sym

/**
 * Search engine chooser: the ten built-in engines (radio = the one in use, switch = shown in the browser's engine
 * menu) and your own engines (name + address with `%s`, optional icon or colour). Tapping an engine selects it and
 * closes the screen; selection is stored as before (`ZtwoSettings.searchType.value`: 0-9 built-in, 100 + id custom, with the address and
 * colour for custom ones). The "shown" switches are stored in `ZoneSettings.searchUse.value` when the screen closes. Replaces
 * `MainListSearch`; adding and editing a custom engine still uses the old dialog (it also picks and crops the icon).
 */
class SearchEngineListActivity : OpumComposeActivity() {

    private lateinit var enabled: BooleanArray

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        enabled = MainUtil.zoneFlags()
        super.onCreate(savedInstanceState)
    }

    /** Makes sure the selected built-in engine is shown, then stores the "shown" flags (same as the old finish()). */
    override fun finish() {
        val selected = ZtwoSettings.searchType.value
        if (selected < 10 && selected >= 0 && selected < enabled.size && !enabled[selected]) {
            enabled[selected] = true
        }
        val enabledText = enabled.joinToString("/") { if (it) "1" else "0" }
        if (enabledText.isNotEmpty() && enabledText != ZoneSettings.searchUse.value) {
            ZoneSettings.edit(applicationContext) { set(ZoneSettings.searchUse, enabledText) }
        }
        super.finish()
    }

    private fun chooseBuiltIn(index: Int) {
        enabled[index] = true
        if (ZtwoSettings.searchType.value != index) {
            ZtwoSettings.editLocal { set(ZtwoSettings.searchType, index) }
            ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, null) }
            ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, 0) }
            PrefZtwo.saveSearch(applicationContext)
        }
        finish()
    }

    private fun chooseCustom(engine: WebSchItem) {
        val index = engine.id.toInt() + 100
        if (ZtwoSettings.searchType.value != index || ZtwoSettings.searchColor.value != engine.color || !MainUtil.sameObject(ZtwoSettings.searchUser.value, engine.engineUrl)) {
            ZtwoSettings.editLocal { set(ZtwoSettings.searchType, index) }
            ZtwoSettings.editLocal { set(ZtwoSettings.searchUser, engine.engineUrl) }
            ZtwoSettings.editLocal { set(ZtwoSettings.searchColor, engine.color) }
            PrefZtwo.saveSearch(applicationContext)
        }
        finish()
    }

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context: Context = this
        val scope = rememberCoroutineScope()
        val korean = MainUtil.isKoreanLocale()
        var tab by remember { mutableIntStateOf(0) }
        val shownFlags = remember { mutableStateListOf<Boolean>().also { it.addAll(enabled.toList()) } }
        var custom by remember { mutableStateOf<List<WebSchItem>>(emptyList()) }
        var icons by remember { mutableStateOf<Map<Long, Bitmap>>(emptyMap()) }
        var reload by remember { mutableIntStateOf(0) }
        var menuFor by remember { mutableStateOf<WebSchItem?>(null) }

        LaunchedEffect(reload) {
            val result = withContext(Dispatchers.IO) {
                WebSearch.instance().engines = null // re-read the table (also refreshes the list the browser uses)
                val list = (DbBookSearch.engines(context) as? List<*>)?.filterIsInstance<WebSchItem>() ?: emptyList()
                list to list.mapNotNull { item -> DbBookSearch.iconById(context, item.id)?.let { item.id to it } }.toMap()
            }
            custom = result.first
            icons = result.second
        }

        fun editEngine(engine: WebSchItem?) {
            var dialog: DialogEditSearch? = null
            val color = if (engine != null) engine.color else {
                val colors = MainConst.iconColors
                var index = DbBookSearch.colorIndex % colors.size
                if (index < 0) index = 0
                DbBookSearch.colorIndex = (index + 3) % colors.size
                colors[index]
            }
            com.mycompany.app.ui.prefs.DialogShow.direct(
                context as android.app.Activity,
                requestCodes = intArrayOf(9, 12),
                onResult = { code, result, data -> handleEditResult(dialog, code, result, data) },
                onDismiss = { reload++ },
            ) { host ->
                DialogEditSearch(
                    host, engine?.id ?: 0L, engine?.name, engine?.engineUrl, color,
                    object : EditUrlListener {
                        override fun onSaved(id: Long, path: String?, extra: String?) {
                            // the dialog stays open after saving until its owner closes it
                            dialog?.dismiss()
                            reload++
                            tab = 1
                        }
                    },
                ).also { dialog = it }
            }
        }

        fun deleteEngine(engine: WebSchItem) {
            val item = ChildItem()
            item.id = engine.id
            item.path = engine.engineUrl
            item.name = engine.name
            var dialog: DialogDeleteBook? = null
            dialog = DialogDeleteBook(
                this, 34, arrayListOf<Any?>(item), null, false, true,
                object : DeleteBookListener {
                    override fun onCancelled() {}
                    override fun onDeleted() {
                        WebSearch.instance().engines = null
                        reload++
                        dialog?.dismiss()
                    }
                },
            )
        }

        Scaffold(
            topBar = {
                Column {
                    SettingsTopBar(title = stringResource(R.string.search_engine), onBack = { finish() })
                    SecondaryTabRow(selectedTabIndex = tab, containerColor = TabRowDefaults.primaryContainerColor, contentColor = TabRowDefaults.primaryContentColor) {
                        Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text(stringResource(R.string.default_title)) })
                        Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text(stringResource(R.string.user_defined)) })
                    }
                }
            },
            floatingActionButton = {
                if (tab == 1) {
                    FloatingActionButton(onClick = { editEngine(null) }) {
                        Icon(Sym.Add, contentDescription = stringResource(R.string.add))
                    }
                }
            },
        ) { padding ->
            LazyColumn(
                Modifier.fillMaxSize().padding(top = padding.calculateTopPadding()),
                contentPadding = PaddingValues(bottom = 96.dp + padding.calculateBottomPadding()),
            ) {
                if (tab == 0) {
                    items(MainConst.engineNames.size) { position ->
                        val index = if (korean) MainConst.engineOrderKo[position] else position
                        val selected = ZtwoSettings.searchType.value == index
                        AppListItem(
                            leadingContent = {
                                // the same site icons as the setup wizard; the built-in glyph while offline
                                com.mycompany.app.ui.components.EngineLogo(index, 32.dp) {
                                    Icon(painterResource(MainConst.engineIcons[index]), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(32.dp))
                                }
                            },
                            headlineContent = { Text(stringResource(MainConst.engineNames[index])) },
                            trailingContent = {
                                Switch(
                                    checked = shownFlags[index] || selected,
                                    enabled = !selected,
                                    onCheckedChange = { shownFlags[index] = it; enabled[index] = it },
                                )
                            },
                            supportingContent = if (selected) { { Text(stringResource(R.string.search_engine)) } } else null,
                            modifier = Modifier.clickable { chooseBuiltIn(index) },
                        )
                    }
                } else {
                    items(custom, key = { it.id }) { engine ->
                        AppListItem(
                            leadingContent = {
                                val bitmap = icons[engine.id]
                                if (bitmap != null) {
                                    Image(bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(32.dp).clip(CircleShape))
                                } else {
                                    Box(Modifier.size(32.dp).clip(CircleShape).background(Color(engine.color)))
                                }
                            },
                            headlineContent = { Text(engine.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            supportingContent = { Text(engine.engineUrl ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            trailingContent = {
                                Row2(ZtwoSettings.searchType.value == engine.id.toInt() + 100) { menuFor = engine }
                            },
                            modifier = Modifier.combinedClickable(onClick = { chooseCustom(engine) }, onLongClick = { menuFor = engine }),
                        )
                    }
                }
            }
        }

        menuFor?.let { engine ->
            com.mycompany.app.ui.components.OpumSheetDialog(
                onDismissRequest = { menuFor = null },
                title = { Text(engine.name ?: "", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                text = {
                    Column {
                        Text(
                            stringResource(R.string.edit),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().clickable { menuFor = null; editEngine(engine) }.padding(vertical = 12.dp),
                        )
                        Text(
                            stringResource(R.string.delete),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().clickable { menuFor = null; deleteEngine(engine) }.padding(vertical = 12.dp),
                        )
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { menuFor = null }) { Text(stringResource(R.string.cancel)) } },
            )
        }
    }

    /** Result of the icon picker (9) and the icon cropper (12) that the edit dialog starts; same steps as the old screen. */
    private fun handleEditResult(dialog: DialogEditSearch?, code: Int, result: Int, data: Intent?) {
        if (dialog == null) return
        if (code == 12) {
            val path = dialog.picturePath
            dialog.picturePath = null
            if (result == Activity.RESULT_OK) {
                if (TextUtils.isEmpty(path) || !File(path!!).exists()) {
                    MainUtil.showToast(dialog.appContext, R.string.invalid_path)
                } else {
                    dialog.iconChanged = true
                    dialog.loadIcon(0, 0, path)
                }
            }
            return
        }
        if (code != 9) return
        var uri = dialog.cameraUri
        dialog.cameraUri = null
        if (result != Activity.RESULT_OK) return
        data?.data?.let { uri = it }
        MainUtil.keepUriPermission(dialog.appContext, uri)
        if (dialog.hostActivity == null) return
        val context = dialog.appContext
        if (uri == null) {
            MainUtil.showToast(context, R.string.invalid_path)
            return
        }
        val dst = MainUtil.newCacheFilePath(context)
        dialog.picturePath = dst
        if (TextUtils.isEmpty(dst)) {
            MainUtil.showToast(dialog.appContext, R.string.invalid_path)
            return
        }
        val intent = Intent(dialog.appContext, com.mycompany.app.ui.viewer.ImageCropActivity::class.java)
        intent.data = uri
        intent.putExtra("EXTRA_DST", dst)
        intent.putExtra("EXTRA_ICON", true)
        (dialog.hostActivity as? com.mycompany.app.main.ActivityHost)?.launchExternalForResult(intent, RequestCode.IMAGE_FILE)
    }
}

@Composable
private fun Row2(selected: Boolean, onMore: () -> Unit) {
    androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = null)
        IconButton(onClick = onMore) { Icon(Sym.MoreVert, contentDescription = null) }
    }
}
