package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.pref.store.AlbumSettings
import android.content.ContentValues
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.dialog.DialogQuickColor.QuickColorListener
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebTabItem

/**
 * Edit a tab group: its name and its colour. [arrayList] is the activity's full tab list, [arrayList2] the tabs of the
 * group being edited, [initName] / [initColor] the current name / colour. Apply writes the new name and colour into the group's tabs
 * (the items in both lists and the `DbBookTab3` rows) on a background thread, then calls the listener (which closes the
 * dialog, as before); while it runs the button turns into Cancel. The colour opens the colour picker (`DialogQuickColor`,
 * still a legacy dialog). Compose now (on [ComposeBottomDialog]); constructor as before, it shows itself.
 */
class DialogTabEdit(
    private val activity: Activity?,
    private val arrayList: ArrayList<com.mycompany.app.web.WebTabItem?>?,
    private val arrayList2: ArrayList<com.mycompany.app.web.WebTabItem?>?,
    private val initName: String?,
    private val initColor: Int,
    dialogApplyListener: DialogApplyListener?,
) : ComposeBottomDialog(activity) {
    private var listener: DialogApplyListener? = dialogApplyListener
    private var colour by mutableIntStateOf(initColor)
    private var working by mutableStateOf(false)
    private var picker: DialogQuickColor? = null
    private var cancelled = false

    private fun save(name: String) {
        val trimmed = name.trim()
        val tabs = arrayList2
        if (colour == initColor && MainUtil.sameObject(trimmed, initName)) { dismiss(); return }
        if (tabs == null || tabs.isEmpty()) { dismiss(); return }
        working = true
        cancelled = false
        val chosenColour = colour
        val app = context.applicationContext
        Thread {
            var done = false
            try {
                val db = DbBookTab.get(app).writableDatabase
                for (tab2 in tabs.toList()) {
                    if (cancelled) break
                    val tab = tab2 ?: continue
                    val index = tab.position
                    val original = if (arrayList == null || index < 0 || index >= arrayList.size) null else arrayList[index]
                    if (original != null) {
                        tab.groupName = trimmed; tab.groupColor = chosenColour
                        original.groupName = trimmed; original.groupColor = chosenColour
                        val values = ContentValues()
                        values.put("_gname", trimmed)
                        values.put("_color", chosenColour)
                        DbUtil.updateWhere(db, "DbBookTab3_table", values, "_uid=?", arrayOf(tab.uid.toString()))
                    }
                }
                done = !cancelled
            } catch (_: Exception) {
            }
            window?.decorView?.post {
                if (cancelled) { dismiss(); return@post }
                if (done) {
                    working = false
                    listener?.onApply()
                } else {
                    working = false
                    MainUtil.showToast(context, R.string.update_fail)
                }
            }
        }.start()
    }

    override fun canCancelFromScrim(): Boolean = !working

    init {
        setSheet {
            var name by remember { mutableStateOf(initName ?: "") }
            var dot by remember { mutableStateOf(AlbumSettings.notiQuick.value) }
            val focus = remember { FocusRequester() }
            LaunchedEffect(Unit) { try { focus.requestFocus() } catch (_: Exception) {} }
            if (working) LinearProgressIndicator(Modifier.fillMaxWidth())
            Row(
                Modifier.fillMaxWidth().clickable(enabled = !working) {
                    if (picker != null || activity == null) return@clickable
                    if (AlbumSettings.notiQuick.value) { AlbumSettings.edit(context) { set(AlbumSettings.notiQuick, false) }; dot = false }
                    val start = if (colour == 0) -65536 else colour
                    var chooser: DialogQuickColor? = null
                    chooser = DialogQuickColor(activity, start, object : QuickColorListener {
                        override fun onColor(color: Int) { colour = color; chooser?.dismiss(); picker = null }
                    })
                    chooser.setOnDismissListener { picker = null }
                    picker = chooser
                }.padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box {
                    Box(Modifier.size(32.dp).background(androidx.compose.ui.graphics.Color(if (colour == 0) -65536 else colour), CircleShape))
                    if (dot) Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.error, CircleShape).align(Alignment.TopEnd))
                }
            }
            OutlinedTextField(
                value = name, onValueChange = { name = it }, singleLine = true, enabled = !working,
                label = { Text(stringResource(R.string.name)) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).focusRequester(focus),
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { if (working) { cancelled = true } else save(name) }) {
                    Text(stringResource(if (working) R.string.cancel else R.string.apply))
                }
            }
        }
        show()
    }

    override fun dismiss() {
        cancelled = true
        picker?.dismiss()
        picker = null
        listener = null
        super.dismiss()
    }
}
