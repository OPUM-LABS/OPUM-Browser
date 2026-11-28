package com.mycompany.app.web

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookPage
import com.mycompany.app.db.book.DbBookPass
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.db.book.DbTabState
import com.mycompany.app.db.book.DbTabThumb
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.WorkspaceSettings
import com.mycompany.app.pref.store.Workspaces

/** The stored rows of a workspace (2026-11-24): every table that tells spaces apart by its `_secret` column. */
object WorkspaceData {
    /** Table name and how to open its database. */
    private val tables: List<Pair<String, (Context) -> SQLiteDatabase>> = listOf(
        "DbBookTab3_table" to { c -> DbBookTab.get(c).writableDatabase },
        "DbTabState_table" to { c -> DbTabState.get(c).writableDatabase },
        "DbTabThumb_table" to { c -> DbTabThumb.get(c).writableDatabase },
        "DbBookHistory_table" to { c -> DbBookHistory.get(c).writableDatabase },
        "DbBookWeb_table" to { c -> DbBookWeb.get(c).writableDatabase },
        "DbBookQuick_table" to { c -> DbBookQuick.get(c).writableDatabase },
        "DbBookRecent_table" to { c -> DbBookRecent.get(c).writableDatabase },
        "DbBookPass_table" to { c -> DbBookPass.get(c).writableDatabase },
        "DbBookAddr_table" to { c -> com.mycompany.app.db.book.DbBookAddr.get(c).writableDatabase },
        "DbBookPage_table" to { c -> DbBookPage.get(c).writableDatabase },
        "DbBookDown_table" to { c -> DbBookDown.get(c).writableDatabase },
    )

    /**
     * Deletes every row of workspace [number] (tabs, history, bookmarks, quick access, recent searches, passwords,
     * addresses, saved pages, download entries; downloaded files stay). Never Main's or incognito's. Background thread.
     */
    fun deleteRows(context: Context, number: Int): Int {
        if (number < Workspaces.FIRST_NUMBER) return 0
        val args = arrayOf<String?>(number.toString())
        var deleted = 0
        for ((table, open) in tables) {
            deleted += try {
                DbUtil.deleteWhere(open(context), table, "_secret=?", args)
            } catch (_: Exception) {
                0
            }
        }
        return deleted
    }

    /** The tables incognito keeps only while it is open (the others it shares with the space it came from). */
    private val temporary = setOf(
        "DbBookTab3_table", "DbTabState_table", "DbTabThumb_table", "DbBookHistory_table", "DbBookRecent_table", "DbBookDown_table",
    )

    /**
     * Forgotten app lock while it covers only locked workspaces: every locked workspace is emptied (its rows, its
     * picture and its WebView profile) and unlocked; Main and the other workspaces stay as they are. When the
     * workspace in use was one of them, Main is chosen instead and true returned (the browser must start again).
     * Background thread.
     */
    fun clearLocked(context: Context): Boolean {
        val numbers = Workspaces.all().filter { it.locked }.map { it.number }
        if (numbers.isEmpty()) return false
        val inUse = !SyncSettings.secretMode.value && Workspaces.currentNumber() in numbers
        if (inUse) {
            val tab = Workspaces.switchTo(context, Workspaces.MAIN, SyncSettings.normalIndex.value)
            SyncSettings.editLocal { set(SyncSettings.normalIndex, tab) }
            PrefSync.saveNormalIndex(context)
        }
        for (number in numbers) {
            deleteRows(context, number)
            SpaceCover.forget(context, number)
        }
        Workspaces.unlockAndForget(context, numbers)
        // the profiles now, or (the one in use) at the next start
        com.mycompany.app.main.MainApp.postToMain(context, Runnable { WebProfiles.dropDeleted(context) })
        Log.i("OpumSpace", "lock reset: cleared locked workspace(s) $numbers")
        return inUse
    }

    /** Moves every row of space [from] to space [to] (all tables). Background thread, or the one-time move at start. */
    fun moveRows(context: Context, from: Int, to: Int): Int {
        val values = ContentValues().also { it.put("_secret", to) }
        val args = arrayOf<String?>(from.toString())
        var moved = 0
        for ((table, open) in tables) {
            moved += try {
                DbUtil.updateWhere(open(context), table, values, "_secret=?", args)
            } catch (_: Exception) {
                0
            }
        }
        return moved
    }

    /** Whether any table holds rows of space [number]. */
    fun hasRows(context: Context, number: Int): Boolean {
        val args = arrayOf<String?>(number.toString())
        for ((table, open) in tables) {
            try {
                open(context).rawQuery("SELECT 1 FROM $table WHERE _secret=? LIMIT 1", args).use { if (it.moveToFirst()) return true }
            } catch (_: Exception) {
            }
        }
        return false
    }

    /** The space numbers that have rows in any table. */
    fun numbersInUse(context: Context): Set<Int> {
        val out = HashSet<Int>()
        for ((table, open) in tables) {
            try {
                open(context).rawQuery("SELECT DISTINCT _secret FROM $table", null).use { while (it.moveToNext()) out.add(it.getInt(0)) }
            } catch (_: Exception) {
            }
        }
        return out
    }

    /**
     * After a backup restore: rows of workspaces the restored list does not know (made after the backup, left in
     * tables the backup does not replace) are deleted; their WebView profiles go at the next start
     * ([WebProfiles.dropDeleted]). The number counter moves past them, so none is given twice.
     */
    fun dropUnlisted(context: Context) {
        val list = Workspaces.all()
        val numbers = Workspaces.unlisted(numbersInUse(context), list)
        if (numbers.isEmpty()) return
        for (number in numbers) deleteRows(context, number)
        val next = maxOf(WorkspaceSettings.next.value, numbers.max() + 1)
        WorkspaceSettings.edit(context) { set(WorkspaceSettings.next, next) }
        Log.i("OpumSpace", "deleted leftovers of workspace(s) $numbers not in the restored list")
    }

    /** Deletes what incognito left in its temporary tables (tabs, history, recent searches, download entries). */
    fun clearIncognito(context: Context) {
        val args = arrayOf<String?>(Workspaces.INCOGNITO.toString())
        for ((table, open) in tables) {
            if (table !in temporary) continue
            try {
                DbUtil.deleteWhere(open(context), table, "_secret=?", args)
            } catch (_: Exception) {
            }
        }
    }

    /**
     * At every start (2026-11-24): incognito is purely temporary now.
     *
     * Once: what incognito kept until now (its tabs, history, bookmarks, quick
     * access, passwords ...) moves into a workspace "Private" ([privateName]), locked if incognito was. Started in
     * incognito then, the browser opens "Private" instead, where those tabs now are.
     *
     * After a backup restore: leftovers of workspaces the restored list does not know are deleted ([dropUnlisted]).
     *
     * Every start in normal mode: anything incognito left behind is deleted (in the background).
     */
    fun atStart(context: Context, privateName: String) {
        if (WorkspaceSettings.migrated.value) {
            if (WorkspaceSettings.restored.value) {
                try {
                    dropUnlisted(context)
                } catch (e: Exception) {
                    Log.w("OpumSpace", "restore check failed: ${e.message}")
                }
                WorkspaceSettings.editNow(context) { set(WorkspaceSettings.restored, false) }
            }
            if (!SyncSettings.secretMode.value) {
                com.mycompany.app.main.MainApp.runInBackground(context, Runnable { clearIncognito(context) })
            }
            return
        }
        try {
            if (hasRows(context, Workspaces.INCOGNITO)) {
                com.mycompany.app.pref.PrefSecret.load(context, false)
                val number = Workspaces.create(context, Workspaces.freeName(privateName, Workspaces.all()))
                val moved = moveRows(context, Workspaces.INCOGNITO, number)
                if (SecretSettings.lockSecret.value && SecretSettings.lockType.value != 0) Workspaces.setLocked(context, number, true)
                // its open tab is the one incognito had open
                val tabs = HashMap(Workspaces.parseTabs(WorkspaceSettings.tabs.value))
                tabs[number] = PrefSync.currentSecretIndex
                WorkspaceSettings.edit(context) { set(WorkspaceSettings.tabs, Workspaces.formatTabs(tabs)) }
                if (SyncSettings.secretMode.value) {
                    Workspaces.switchTo(context, number, SyncSettings.normalIndex.value)
                    SyncSettings.editLocal { set(SyncSettings.normalIndex, PrefSync.currentSecretIndex) }
                    SyncSettings.editLocal { set(SyncSettings.secretMode, false) }
                }
                // incognito starts empty from now on
                PrefSync.currentSecretIndex = 0
                PrefSync.saveNormalIndex(context)
                Log.i("OpumSpace", "kept incognito data moved to workspace $number ($moved rows)")
            }
        } catch (e: Exception) {
            Log.w("OpumSpace", "moving incognito data failed: ${e.message}")
        }
        WorkspaceSettings.editNow(context) { set(WorkspaceSettings.migrated, true) }
    }

    /**
     * Incognito keeps nothing (owner's decision 2026-11-24): no history, tabs and logins gone on leaving, download
     * entries shown while it is open. The old settings stay in the file (older backups carry them) but no longer count.
     */
    fun enforceIncognitoRules() {
        // after SecretSettings were read (MainApp.loadEarlySettings), else the file's values come back
        SecretSettings.editLocal {
            set(SecretSettings.keepTab, false)
            set(SecretSettings.keepLogin, false)
            set(SecretSettings.secretHist, 0L)
            if (SecretSettings.secretDown.value == 0L) set(SecretSettings.secretDown, 1L)
        }
    }
}
