package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import com.mycompany.app.pref.PrefSpace

/**
 * The stored values of the workspaces (file `PrefSpace`, 2026-11-24); [Workspaces] reads and changes them.
 */
object WorkspaceSettings : SettingStore() {
    override fun open(context: Context): PrefCore? = PrefSpace.get(context, false)

    /** The workspaces other than Main, as JSON ([Workspaces.format]). */
    val list = string("mSpaces") { "" }

    /** The number of the workspace in use ([Workspaces.MAIN] when none was chosen). */
    val current = int("mSpaceNow") { Workspaces.MAIN }

    /** The number the next new workspace gets: numbers are never used twice ([Workspaces.nextNumber]). */
    val next = int("mSpaceNext") { Workspaces.FIRST_NUMBER }

    /** The open tab of each space (Main and workspaces) when it was left, as JSON ([Workspaces.formatTabs]). */
    val tabs = string("mSpaceTabs") { "" }

    /**
     * What incognito kept before it became temporary has been moved into a workspace (2026-11-24). Cleared by a backup
     * restore, so the data of an older backup is moved as well ([com.mycompany.app.web.WorkspaceData.atStart]).
     */
    val migrated = bool("mSpaceMoved") { false }

    /** WebView profiles of deleted workspaces still to delete (comma-separated numbers; [Workspaces.dropList]). */
    val drop = string("mSpaceDrop") { "" }
}
