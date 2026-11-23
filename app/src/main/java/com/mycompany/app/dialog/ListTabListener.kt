package com.mycompany.app.dialog

import java.util.ArrayList

/**
 * What the tab overview tells the browser activity: [onClose] the overview closes (with the edited list and the index
 * to show), [barHeight] width of the page area, [refreshBar] refresh the tab bar, [applyList] apply the edited list,
 * [selectTab] switch to a tab.
 */
interface ListTabListener {
    fun onClose(isSecret: Boolean, arrayList: ArrayList<com.mycompany.app.web.WebTabItem?>?, position: Int, isLast: Boolean)

    fun barHeight(): Int

    fun refreshBar()

    fun applyList(position: Int, arrayList: ArrayList<com.mycompany.app.web.WebTabItem?>?)

    fun selectTab(isSecret: Boolean, arrayList: ArrayList<com.mycompany.app.web.WebTabItem?>?, position: Int, isNew: Boolean)

    /**
     * "New tab" inside a group of the tab overview (or in a new, still empty group): the overview's list ([arrayList],
     * current tab at [position], the group's last tab) is taken over, then a new tab joining group [groupId] is added
     * after the group.
     */
    fun newTabInGroup(arrayList: ArrayList<com.mycompany.app.web.WebTabItem?>?, position: Int, groupId: Long, groupName: String?, groupColor: Int) {}
}
