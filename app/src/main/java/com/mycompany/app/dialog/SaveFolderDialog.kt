package com.mycompany.app.dialog

import android.content.Intent
import com.mycompany.app.main.RequestCode
import android.text.TextUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Shared "save to a folder" sheet (old DialogDownPage / DialogDownEdit / DialogSaveSource layout; also the removed DialogPassSave):
 * a header ([Header]), the Name field, the "Save location" row (recent folders and "Direct select",
 * which opens the system folder picker with request code 19 — the result comes back through [onFolderResult] or
 * [setFolder]), an optional progress bar and the Save / Cancel button. The name keeps the old rules: the suggested
 * name ([setBaseName]) follows the chosen folder (file-name cleanup `MainUtil.safeFileName`, extension [ext] via `MainUtil.replaceExt`) until the
 * user edits it; [checkedName] runs the old checks (folder chosen, name, at most 200 bytes). Subclasses call [start]
 * at the end of their `init`.
 */
@Suppress("deprecation")
abstract class SaveFolderDialog(mainActivity: android.app.Activity?, private val ext: String?) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: android.app.Activity? = mainActivity
    @JvmField
    var appContext: android.content.Context? = context

    /** Recent folders (tree URIs). */
    private var folders: ArrayList<Any?>? = null
    private var folderLabel by mutableStateOf<String?>(null)
    private var folderMenu by mutableStateOf(false)
    private var name by mutableStateOf("")
    private var baseName: String? = null
    private var autoName: String? = null
    private var edited = false
    private var saving = false
    protected var headerShown by mutableStateOf(true)
    protected var busy by mutableStateOf(false)
    protected var mainRes by mutableIntStateOf(AppR.string.save)
    protected var mainEnabled by mutableStateOf(true)

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

    /** Header above the name (icon + title, or an image preview). */
    @Composable
    protected abstract fun ColumnScope.Header()

    /** Suggested file name, worked out off the main thread before the sheet shows. */
    protected open fun prepare(): String? = null

    /** Save pressed with a valid [checkedName] result. */
    protected abstract fun onSave(fileName: String)

    /** Cancel pressed while [busy]. */
    protected open fun onCancelWork() {}

    protected fun start() {
        runAsync(Runnable {
            val ctx = appContext ?: return@Runnable
            val suggested = prepare()
            val list = MainUri.externalVolumes(ctx)
            PrefPath.picked = MainUri.findChild(ctx, PrefPath.picked!!, list)
            val label = MainUri.displayPath(ctx, MainUri.storageVolume())
            mainHandler.post {
                if (appContext == null) return@post
                folders = list
                folderLabel = label
                if (!TextUtils.isEmpty(suggested)) baseName = suggested
                refreshName(null)
                build()
            }
        })
    }

    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private fun build() {
        setSheet {
            if (headerShown) Header()
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (!edited && !MainUtil.sameObject(autoName, it)) edited = true
                    },
                    label = { Text(stringResource(AppR.string.name)) },
                    singleLine = true,
                    enabled = !busy,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { save() }),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                )
                Box {
                    ListItem(
                        headlineContent = { Text(stringResource(AppR.string.save_location)) },
                        supportingContent = { Text(folderLabel ?: stringResource(AppR.string.folder_not_selected), maxLines = 2) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                        modifier = Modifier.clickable(enabled = !busy) { openFolders() }.padding(horizontal = 8.dp),
                    )
                    com.mycompany.app.ui.components.BubbleMenu(expanded = folderMenu, onDismiss = { folderMenu = false }) {
                        val ctx = appContext
                        folders?.forEach { any ->
                            val path = any as String?
                            DropdownMenuItem(
                                text = { Text(if (ctx == null) "" else MainUri.volumeLabel(ctx, path)) },
                                onClick = { folderMenu = false; setFolder(path) },
                            )
                        }
                        DropdownMenuItem(
                            text = { Text(stringResource(AppR.string.direct_select)) },
                            onClick = { folderMenu = false; MainUtil.pickTreeFolder(hostActivity, MainUri.storageVolume()) },
                        )
                    }
                }
            }
            if (busy) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { if (busy) onCancelWork() else save() }, enabled = mainEnabled) { Text(stringResource(mainRes)) }
            }
        }
        show()
    }

    private fun openFolders() {
        val list = folders
        if (list == null || list.isEmpty()) {
            MainUtil.pickTreeFolder(hostActivity, MainUri.storageVolume())
            return
        }
        folderMenu = true
    }

    private fun save() {
        if (saving || busy) return
        saving = true
        mainHandler.post {
            val fileName = checkedName()
            if (fileName != null) onSave(fileName)
            saving = false
        }
    }

    /** Suggested name [str] (or keep the last one), adjusted to the current folder. */
    fun setBaseName(newBaseName: String?) {
        if (!TextUtils.isEmpty(newBaseName)) baseName = newBaseName
        refreshName(null)
    }

    private fun refreshName(unused: String?) {
        val cleaned = MainUtil.safeFileName(if (edited) name.trim() else baseName)
        if (TextUtils.isEmpty(MainUri.storageVolume())) {
            folderLabel = null
            autoName = cleaned
            name = cleaned ?: ""
            return
        }
        val withExt = suggestedWithExt(cleaned)
        autoName = withExt
        name = withExt ?: ""
    }

    /** Extension rule for the suggested name (default: [ext] when there is one). */
    protected open fun suggestedWithExt(cleaned: String?): String? =
        if (TextUtils.isEmpty(cleaned) || ext == null) cleaned else MainUtil.replaceExt(cleaned, ext)

    /** Old checks before saving; the cleaned file name, or null after a message. */
    protected fun checkedName(): String? {
        if (appContext == null) return null
        if (TextUtils.isEmpty(MainUri.storageVolume())) {
            showToast(appContext, AppR.string.select_dir)
            return null
        }
        val cleaned = name.trim { it <= ' ' }
        if (TextUtils.isEmpty(cleaned)) {
            showToast(appContext, AppR.string.input_name)
            return null
        }
        if (cleaned.toByteArray().size > 200) {
            showToast(appContext, AppR.string.long_name)
            return null
        }
        val full = if (ext == null) cleaned else MainUtil.replaceExt(cleaned, ext)
        if (TextUtils.isEmpty(full)) {
            showToast(appContext, AppR.string.input_name)
            return null
        }
        return MainUtil.safeFileName(full)
    }

    /** Chosen folder [str] (menu pick or the folder picker result). */
    fun setFolder(picked: String?) {
        if (TextUtils.isEmpty(picked) || picked == PrefPath.picked) return
        PrefPath.picked = picked
        runAsync(Runnable {
            val ctx = appContext ?: return@Runnable
            val dir = MainUri.storageVolume()
            PrefSet.putDownUri(ctx, dir)
            val label = MainUri.displayPath(ctx, dir)
            mainHandler.post {
                if (appContext == null) return@post
                folderLabel = label
                refreshName(null)
            }
        })
    }

    /** Folder picker result (request code 19); false for other codes. */
    fun onFolderResult(requestCode: Int, resultCode: Int, intent: Intent?): Boolean {
        if (requestCode != RequestCode.SAVE_LOCATION) return false
        if (resultCode == -1 && intent != null) {
            val data = intent.getData()
            if (data == null) {
                showToast(appContext, AppR.string.invalid_path)
                return true
            }
            val path = MainUri.treeDocUri(data)
            if (TextUtils.isEmpty(path)) {
                showToast(appContext, AppR.string.invalid_path)
                return true
            }
            setFolder(path)
            MainUtil.keepUriPermission(appContext, data)
        }
        return true
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        hostActivity = null
        appContext = null
        super.dismiss()
    }
}
