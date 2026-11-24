package com.mycompany.app.web

import android.content.Context
import android.database.sqlite.SQLiteDatabase
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
        "DbBookPage_table" to { c -> DbBookPage.get(c).writableDatabase },
        "DbBookDown_table" to { c -> DbBookDown.get(c).writableDatabase },
    )

    /**
     * Deletes every row of workspace [number] (tabs, history, bookmarks, quick access, recent searches, passwords,
     * saved pages, download entries; downloaded files stay). Never Main's or incognito's. Background thread.
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
}
