package com.mycompany.app.web

import java.util.ArrayList

/**
 * One tab of the browser's tab list (a row of the tab table): [uid] its id, [parentId] the id of the previous tab,
 * [groupId] / [groupName] / [groupColor] its group, [position] its index, [groupPos] its bar position, [tabUrl] and
 * [tabTitle] address and title, [webFrame] the live page, [groupTabs] the members when this item stands for a group in a
 * tab bar / overview.
 */
class WebTabItem {
    @JvmField
    var lastUsed: Long = 0
    @JvmField
    var uid: Long = 0
    @JvmField
    var parentId: Long = 0
    @JvmField
    var groupId: Long = 0
    @JvmField
    var groupName: String? = null
    @JvmField
    var groupColor: Int = 0
    @JvmField
    var position: Int = 0
    @JvmField
    var groupPos: Int = 0
    @JvmField
    var tabUrl: String? = null
    @JvmField
    var tabTitle: String? = null
    @JvmField
    var deskMode: Boolean = false
    @JvmField
    var inGroup: Boolean = false
    @JvmField
    var reloadPending: Boolean = false
    @JvmField
    var webFrame: WebNestFrame? = null
    @JvmField
    var groupTabs: ArrayList<WebTabItem?>? = null
}
