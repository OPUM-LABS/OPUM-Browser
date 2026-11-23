package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.pref.store.SecretSettings
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.ui.browser.ComposeSwitchDialog

/**
 * What is cleared when the app locks (`SecretSettings.lockReset.value`, `mLockReset3`): quick access, bookmarks, download list, recent
 * searches, history, tabs (bits 2 .. 64). Compose now (on [ComposeSwitchDialog]); same constructor, shows itself.
 */
class DialogSetLock(activity: Activity?) : ComposeSwitchDialog(
    activity,
    intArrayOf(R.string.quick_access, R.string.bookmark, R.string.down_list, R.string.recent_search, R.string.history, R.string.tab_item),
    BooleanArray(6) { (SecretSettings.lockReset.value and (2 shl it)) != 0 },
    { states ->
        var mask = SecretSettings.lockReset.value
        for (index in states.indices) mask = if (states[index]) mask or (2 shl index) else mask and (2 shl index).inv()
        if (mask != SecretSettings.lockReset.value) {
            SecretSettings.edit(activity) { set(SecretSettings.lockReset, mask) }
        }
    },
)
