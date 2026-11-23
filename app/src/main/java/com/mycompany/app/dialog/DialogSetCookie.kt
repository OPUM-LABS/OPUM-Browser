package com.mycompany.app.dialog

import androidx.compose.foundation.layout.Arrangement
import com.mycompany.app.pref.store.BrowserSettings
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.components.SettingChoiceRow

/**
 * Cookie settings of the browser: which cookies to accept and what to do with third-party cookies. Compose now (on
 * [ComposeBottomDialog]); same constructor and [DialogApplyListener] (called only when something changed and was
 * stored by Apply). Shows itself.
 */
class DialogSetCookie(mainActivity: MainActivity?, dialogApplyListener: DialogApplyListener?) : ComposeBottomDialog(mainActivity) {
    private var listener: DialogApplyListener? = dialogApplyListener
    private var accept by mutableIntStateOf(BrowserSettings.cookieType.value)
    private var third by mutableIntStateOf(BrowserSettings.thirdType.value)

    private fun applyAndClose() {
        if (BrowserSettings.cookieType.value != accept || BrowserSettings.thirdType.value != third) {
            BrowserSettings.editLocal { set(BrowserSettings.cookieType, accept) }
            BrowserSettings.editLocal { set(BrowserSettings.thirdType, third) }
            val prefs = PrefWeb.get(context, false)
            prefs!!.putInt(BrowserSettings.cookieType.value, "mCookieType")
            prefs.putInt(BrowserSettings.thirdType.value, "mThirdType")
            prefs.saveAsync()
            MainApp.applyCookieSetting(context)
            listener?.onApply()
        }
        dismiss()
    }

    init {
        setSheet {
            var menu by remember { mutableIntStateOf(-1) }
            for (row in 0..1) {
                val current = if (row == 0) accept else third
                Box {
                    SettingChoiceRow(
                        title = stringResource(if (row == 0) R.string.accept_cookie else R.string.third_cookie),
                        value = stringResource(MainConst.cookieNames[current]),
                        onClick = { menu = row },
                    )
                    com.mycompany.app.ui.components.BubbleMenu(expanded = menu == row, onDismiss = { menu = -1 }) {
                        for (value in MainConst.cookieValues) {
                            DropdownMenuItem(
                                text = { Text(stringResource(MainConst.cookieNames[value])) },
                                onClick = {
                                    menu = -1
                                    if (row == 0) accept = value else third = value
                                },
                            )
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { applyAndClose() }) { Text(stringResource(R.string.apply)) }
            }
        }
        show()
    }

    override fun dismiss() {
        listener = null
        super.dismiss()
    }
}
