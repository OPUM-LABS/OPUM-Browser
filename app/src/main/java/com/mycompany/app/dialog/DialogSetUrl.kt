package com.mycompany.app.dialog

import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.ui.browser.ActionItem
import com.mycompany.app.ui.browser.ComposeActionDialog
import com.mycompany.app.web.WebViewActivity

/**
 * The address menu. [addParam] picks the list: true = "add" (bookmark, quick access, home screen, homepage, start page); false =
 * the URL actions (copy, share, paste, open copied, new tab, open with, an ownership line [ownerParam] when there is one, and the
 * certificate when [certificateParam]). Picking a row calls the listener with its id (the caller closes the sheet, as before). Compose
 * now (on [ComposeActionDialog]); [theme] only chose the old dialog theme.
 */
class DialogSetUrl(
    webViewActivity: WebViewActivity?,
    theme: Int,
    addParam: Boolean,
    ownerParam: String?,
    certificateParam: Boolean,
    dialogSeekListener: DialogSeekListener?,
) : ComposeActionDialog(webViewActivity, urlItems(addParam, ownerParam, certificateParam), { dialogSeekListener?.onValue(it) }) {
    companion object {
        private fun urlItems(add: Boolean, owner: String?, certificate: Boolean): List<ActionItem> {
            val list = ArrayList<ActionItem>()
            if (add) {
                list.add(ActionItem(0, R.drawable.baseline_star_add_black_24, R.drawable.baseline_star_add_dark_24, R.string.add_book))
                list.add(ActionItem(1, R.drawable.outline_add_home_black_24, R.drawable.outline_add_home_dark_24, R.string.add_quick))
                list.add(ActionItem(2, R.drawable.outline_add_to_home_screen_black_24, R.drawable.outline_add_to_home_screen_dark_24, R.string.add_home_2))
                list.add(ActionItem(3, R.drawable.outline_home_black_24, R.drawable.outline_home_dark_24, R.string.set_homepage))
                list.add(ActionItem(4, R.drawable.outline_electric_bolt_black_24, R.drawable.outline_electric_bolt_dark_24, R.string.set_startpage))
            } else {
                list.add(ActionItem(0, R.drawable.outline_link_black_24, R.drawable.outline_link_dark_24, R.string.copy_url))
                list.add(ActionItem(1, R.drawable.outline_share_black_24, R.drawable.outline_share_dark_24, R.string.share_url))
                list.add(ActionItem(2, R.drawable.outline_content_paste_black_24, R.drawable.outline_content_paste_dark_24, R.string.paste_url))
                list.add(ActionItem(3, R.drawable.outline_open_in_new_black_24, R.drawable.outline_open_in_new_dark_24, R.string.open_copied_url))
                list.add(ActionItem(4, R.drawable.outline_tab_new_right_black_24, R.drawable.outline_tab_new_right_dark_24, R.string.open_new_tab))
                list.add(ActionItem(5, R.drawable.outline_apps_black_24, R.drawable.outline_apps_dark_24, R.string.open_with))
                if (!owner.isNullOrEmpty()) {
                    list.add(ActionItem(6, R.drawable.outline_copyright_black_24, R.drawable.outline_copyright_dark_24, label = owner))
                }
                if (certificate) {
                    list.add(ActionItem(7, R.drawable.outline_verified_user_black_24, R.drawable.outline_verified_user_dark_24, R.string.view_certi))
                }
            }
            return list
        }
    }
}
