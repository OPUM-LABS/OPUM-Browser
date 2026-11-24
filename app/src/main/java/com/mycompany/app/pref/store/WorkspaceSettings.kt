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
}
