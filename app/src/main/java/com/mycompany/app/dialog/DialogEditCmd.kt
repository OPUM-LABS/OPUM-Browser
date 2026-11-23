package com.mycompany.app.dialog

import android.content.DialogInterface
import com.mycompany.app.pref.store.ReadSettings
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.data.book.DataBookUser
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.dialog.DialogEditUrl.EditUrlListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Edit one user filter rule (element hiding / block-area command, [ruleText]; [ruleId] = its id): a help icon (opens
 * `DialogGuideArea` kind 2, badge while `ReadSettings.notiArea.value`), "^" "$" "*" buttons that insert at the cursor, a multi-line
 * field and Apply. With [addMode] (called from `DialogEditArea`) Apply just hands the text back; otherwise an empty rule is
 * refused and a non-empty one is stored in the background (`DbBookUser`, `DataBookUser`, the user filter in
 * `DataBookFilter`) before [EditUrlListener.onSaved]. Compose now (on [ComposeBottomDialog]); same
 * constructor, shows itself.
 */
class DialogEditCmd(
    mainActivity: android.app.Activity?,
    ruleId: Long,
    ruleText: String?,
    addMode: Boolean,
    editUrlListener: EditUrlListener?,
) : ComposeBottomDialog(mainActivity) {
    @JvmField
    var hostActivity: android.app.Activity? = mainActivity
    @JvmField
    var editListener: EditUrlListener? = editUrlListener
    @JvmField
    var ruleId: Long = ruleId
    @JvmField
    var ruleText: String? = ruleText
    @JvmField
    var addMode: Boolean = addMode
    @JvmField
    var guideDialog: DialogGuideArea? = null

    private val ui = Handler(Looper.getMainLooper())
    private var field by mutableStateOf(TextFieldValue(ruleText ?: "", TextRange((ruleText ?: "").length)))
    private var saving by mutableStateOf(false)
    private var helpBadge by mutableStateOf(ReadSettings.notiArea.value)

    init {
        setSheet {
            val focus = remember { FocusRequester() }
            Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 16.dp, top = 8.dp)) {
                IconButton(onClick = { openHelp() }) {
                    if (helpBadge) {
                        BadgedBox(badge = { Badge() }) { Icon(painterResource(AppR.drawable.outline_help_black_24), contentDescription = null) }
                    } else {
                        Icon(painterResource(AppR.drawable.outline_help_black_24), contentDescription = null)
                    }
                }
                Spacer(Modifier.weight(1f))
                listOf("^", "$", "*").forEach { s ->
                    FilledTonalButton(
                        onClick = { insertText(s) },
                        enabled = !saving,
                        modifier = Modifier.padding(start = 8.dp),
                    ) { Text(s, fontFamily = FontFamily.Monospace) }
                }
            }
            OutlinedTextField(
                value = field,
                onValueChange = { field = it },
                enabled = !saving,
                minLines = 5,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp).focusRequester(focus),
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { apply() }, enabled = !saving) { Text(stringResource(AppR.string.apply)) }
            }
            LaunchedEffect(Unit) { focus.requestFocus() }
        }
        show()
    }

    private fun openHelp() {
        if (ReadSettings.notiArea.value) {
            ReadSettings.edit(context) { set(ReadSettings.notiArea, false) }
            helpBadge = false
        }
        if (hostActivity == null || guideDialog != null) return
        val guide = DialogGuideArea(hostActivity, 2, null)
        guideDialog = guide
        guide.setOnDismissListener(DialogInterface.OnDismissListener {
            guideDialog?.dismiss()
            guideDialog = null
        })
    }

    /** Inserts [str] at the cursor (replacing a selection). */
    fun insertText(insert: String?) {
        if (TextUtils.isEmpty(insert)) return
        val fieldNow = field
        val start = minOf(fieldNow.selection.start, fieldNow.selection.end).coerceIn(0, fieldNow.text.length)
        val end = maxOf(fieldNow.selection.start, fieldNow.selection.end).coerceIn(0, fieldNow.text.length)
        val newText = fieldNow.text.substring(0, start) + insert + fieldNow.text.substring(end)
        field = TextFieldValue(newText, TextRange(start + insert!!.length))
    }

    private fun apply() {
        val listener = editListener ?: return
        if (saving) return
        val path = field.text.trim { it <= ' ' }
        if (addMode) {
            listener.onSaved(ruleId, path, null)
            dismiss()
            return
        }
        if (TextUtils.isEmpty(path)) {
            showToast(context, AppR.string.empty)
            return
        }
        ruleText = path
        saving = true
        val context = context
        val runnable = Runnable {
            val rule = DbBookUser.hostOfRule(ruleText)
            val item = DbBookUser.saveRule(context, ruleId, MainUtil.stripHostPrefix(rule), ruleText)
            if (item != null) {
                ruleId = item.id
                val cached = DataBookUser.instance(context).itemFor(ruleId)
                if (cached != null) {
                    cached.path = ruleText
                    cached.name = ruleText
                    cached.subText = rule
                }
                DataBookUser.instance(context).putItem(item)
                DataBookFilter.instance(context).putItem(DbBookFilter.addPathItem(context, "sb_user_filter_path", "sb_user_filter_path"))
                com.mycompany.app.web.filter.UboFilter.onListsChanged(context)
            } else {
                ruleId = 0L
            }
            ui.post {
                editListener?.onSaved(ruleId, null, null)
                dismiss()
            }
        }
        val ex = MainApp.executor(context)
        try {
            if (ex != null) ex.execute(runnable) else Thread(runnable).start()
        } catch (unused: Exception) {
            saving = false
        }
    }

    override fun canCancelFromScrim(): Boolean = !saving

    override fun dismiss() {
        val guide = guideDialog
        guideDialog = null
        guide?.dismiss()
        hostActivity = null
        editListener = null
        super.dismiss()
    }

    companion object {
    }
}
