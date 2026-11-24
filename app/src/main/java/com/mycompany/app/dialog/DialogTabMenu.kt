package com.mycompany.app.dialog

import android.app.Activity
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.ui.browser.ActionItem
import com.mycompany.app.ui.browser.ComposeActionDialog

/**
 * The tab menu: close all / other / this tab (the first two only when [othersParam] says there are other tabs), new tab, new tab
 * with the current page, group, back and memory saving. Picking a row calls the listener with its id (the caller closes
 * the sheet, as before). Compose now (on [ComposeActionDialog]); [theme] only chose the old dialog theme.
 */
class DialogTabMenu(
    activity: Activity?,
    theme: Int,
    othersParam: Boolean,
    dialogSeekListener: DialogSeekListener?,
) : ComposeActionDialog(activity, tabMenuItems(activity, othersParam), { dialogSeekListener?.onValue(it) }) {
    companion object {
        private fun tabMenuItems(activity: Activity?, others: Boolean): List<ActionItem> {
            val current = (activity?.getString(R.string.new_url) ?: "") + " (" + (activity?.getString(R.string.current_page) ?: "") + ")"
            val list = ArrayList<ActionItem>()
            if (others) {
                list.add(ActionItem(0, R.drawable.outline_tab_close_black_24, R.drawable.outline_tab_close_dark_24, R.string.delete_all_tab))
                list.add(ActionItem(1, R.drawable.outline_tab_close_right_black_24, R.drawable.outline_tab_close_right_dark_24, R.string.delete_other_tab))
            }
            list.add(ActionItem(2, R.drawable.outline_disabled_by_default_black_24, R.drawable.outline_disabled_by_default_dark_24, R.string.delete_tab))
            list.add(ActionItem(3, R.drawable.outline_add_box_black_24, R.drawable.outline_add_box_dark_24, R.string.new_url))
            list.add(ActionItem(4, R.drawable.outline_tab_new_right_black_24, R.drawable.outline_tab_new_right_dark_24, label = current))
            list.add(ActionItem(5, R.drawable.outline_library_add_black_24, R.drawable.outline_library_add_dark_24, R.string.group_url))
            list.add(ActionItem(6, R.drawable.outline_flip_to_back_black_24, R.drawable.outline_flip_to_back_dark_24, R.string.back_url))
            list.add(ActionItem(7, R.drawable.outline_energy_savings_leaf_black_24, R.drawable.outline_energy_savings_leaf_dark_24, R.string.mem_limit))
            // Main, the workspaces, incognito's way out (2026-11-24): a long press on the tab button reaches them
            list.add(ActionItem(8, R.drawable.outline_workspaces_black_24, R.drawable.outline_workspaces_dark_24, R.string.workspaces))
            return list
        }
    }
}
