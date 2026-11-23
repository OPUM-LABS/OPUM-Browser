package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.ui.browser.ComposeSwitchDialog

/**
 * Tab restore options: restore the web page of each tab (`BrowserSettings.tabRestore.value`, `mTabRestore`) and offer to undo closing tabs
 * (`ZoneSettings.tabUndelete.value`, `mTabUndelete`). The listener is called when either changed. Compose now (on [ComposeSwitchDialog]);
 * same constructor, shows itself.
 */
class DialogSetTabRestore(activity: Activity?, dialogApplyListener: DialogApplyListener?) : ComposeSwitchDialog(
    activity,
    intArrayOf(R.string.web_page, R.string.tab_list),
    booleanArrayOf(BrowserSettings.tabRestore.value, ZoneSettings.tabUndelete.value),
    { states ->
        var changed = false
        if (BrowserSettings.tabRestore.value != states[0]) { BrowserSettings.edit(activity) { set(BrowserSettings.tabRestore, states[0]) }; changed = true }
        if (ZoneSettings.tabUndelete.value != states[1]) { ZoneSettings.edit(activity) { set(ZoneSettings.tabUndelete, states[1]) } ; changed = true }
        if (changed) dialogApplyListener?.onApply()
    },
)
