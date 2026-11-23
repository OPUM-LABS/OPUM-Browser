package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.pref.store.ZtwoSettings
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.ui.browser.ComposeSwitchDialog

/**
 * Which places a link opened in a new tab can come from (`ZtwoSettings.tabOpen.value`, `mTabOpen2`): link, search URL, quick access,
 * bookmark, history (bits 2, 4, 8, 16, 32). Compose now (on [ComposeSwitchDialog]); same constructor, shows itself.
 */
class DialogSetOpen(activity: Activity?) : ComposeSwitchDialog(
    activity,
    intArrayOf(R.string.link, R.string.search_url, R.string.quick_access, R.string.bookmark, R.string.history),
    BooleanArray(5) { (ZtwoSettings.tabOpen.value and (2 shl it)) != 0 },
    { states ->
        var mask = ZtwoSettings.tabOpen.value
        for (index in states.indices) mask = if (states[index]) mask or (2 shl index) else mask and (2 shl index).inv()
        if (mask != ZtwoSettings.tabOpen.value) {
            ZtwoSettings.edit(activity) { set(ZtwoSettings.tabOpen, mask) }
        }
    },
)
