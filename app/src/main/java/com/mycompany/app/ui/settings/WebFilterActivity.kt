package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.text.TextUtils
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.ui.OpumComposeActivity
import java.io.File

/**
 * What the Web filter pages share: the browser opens them for a result and reloads its filters when they changed
 * (`EXTRA_FILTER`; `EXTRA_TYPE` 62 when opened from the browser's filter dialog, `EXTRA_POPUP`). A page that changes
 * the filters calls [markFilterChanged]; one that opens the ad filter list calls [openFilterList], and when the list
 * is closed the filter files no list uses any more are removed. A page that opens another Web filter page passes the
 * other page's result on with [takeResult].
 */
abstract class WebFilterActivity : OpumComposeActivity() {
    private var fromPopup = false
    private var filterCountAtStart = 0L
    private var filterCountSeen = 0L
    private var filterChanged = false
    private var filterOpened = false
    private var filterPending = false
    private var filterProcessed = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        fromPopup = intent.getBooleanExtra("EXTRA_POPUP", false)
        super.onCreate(savedInstanceState)
        Thread {
            val count = DbBookFilter.totalCount(this)
            filterCountAtStart = count
            filterCountSeen = count
        }.start()
    }

    override fun onResume() {
        super.onResume()
        if (filterPending) {
            filterPending = false
            onFilterListClosed(this)
        }
    }

    override fun finish() {
        if (filterChanged) {
            setResult(RESULT_OK, filterResult())
        } else if (filterOpened && !filterProcessed && filterCountAtStart != DbBookFilter.totalCount(this)) {
            setResult(RESULT_OK, filterResult())
        }
        super.finish()
    }

    /** The filters changed: the browser reloads them when this page closes. */
    fun markFilterChanged() { filterChanged = true }

    /** Passes on the result of another Web filter page this one opened. */
    fun takeResult(data: Intent?) {
        if (data?.getBooleanExtra("EXTRA_FILTER", false) == true) filterChanged = true
    }

    /** Opens the ad filter list ([com.mycompany.app.ui.lists.FilterScriptListActivity]). */
    fun openFilterList() {
        filterOpened = true
        filterPending = true
        startActivity(
            Intent(this, com.mycompany.app.ui.lists.FilterScriptListActivity::class.java)
                .putExtra(com.mycompany.app.ui.lists.FilterScriptListActivity.EXTRA_TYPE, 24)
        )
    }

    /** Opens a site list of the Web filter ([com.mycompany.app.ui.lists.SiteListActivity] or the saved sites). */
    fun openSiteList(code: Int) {
        if (com.mycompany.app.ui.lists.SiteListActivity.supports(code)) {
            com.mycompany.app.ui.lists.SiteListActivity.open(this, code)
            return
        }
        if (code == 23) {
            startActivity(
                Intent(this, com.mycompany.app.ui.lists.SavedSitesActivity::class.java)
                    .putExtra(com.mycompany.app.ui.lists.SavedSitesActivity.EXTRA_TYPE, 23)
            )
        }
    }

    /** The intent that opens another Web filter page, passing on where the browser opened this one from. */
    fun pageIntent(page: Class<*>): Intent {
        val intent = Intent(this, page).putExtra("EXTRA_POPUP", fromPopup)
        this.intent.getStringExtra("EXTRA_PATH")?.let { intent.putExtra("EXTRA_PATH", it) }
        return intent
    }

    private fun filterResult(): Intent {
        val data = Intent().putExtra("EXTRA_FILTER", true)
        if (fromPopup) data.putExtra("EXTRA_TYPE", 62)
        return data
    }

    /** Runs after the ad filter list was closed: remembers that the filters changed and removes unused filter files. */
    private fun onFilterListClosed(context: Context) {
        Thread {
            val count = DbBookFilter.totalCount(context)
            if (filterCountAtStart != count) filterChanged = true
            filterProcessed = true
            if (filterCountSeen == count) return@Thread
            filterCountSeen = count
            if (PdfSettings.filterInit.value) return@Thread
            val keep = ArrayList<Any?>()
            var cursor: Cursor? = null
            try {
                cursor = DbUtil.query(
                    DbBookFilter.get(context).getWritableDatabase(), "DbBookFilter_table",
                    arrayOf<String?>("_path"), null, null, null,
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex("_path")
                    do {
                        val path = MainUtil.filterFilePath(context, cursor.getString(index))
                        if (!TextUtils.isEmpty(path)) keep.add(path)
                    } while (cursor.moveToNext())
                }
            } catch (_: Exception) {
            }
            cursor?.close()
            val dir = MainUtil.filesSubdir(context, ".filter")
            if (TextUtils.isEmpty(dir)) return@Thread
            deleteUnused(File(dir!!), true, keep)
        }.start()
    }

    private fun deleteUnused(file: File, keepSelf: Boolean, keep: List<Any?>) {
        try {
            val children = if (file.isDirectory) file.listFiles() else null
            if (file.isDirectory && children != null) {
                for (child in children) {
                    if (child.isDirectory) deleteUnused(child, false, keep)
                    else if (!keep.contains(child.path)) child.delete()
                }
            }
            if (!keepSelf) file.delete()
        } catch (_: Exception) {
        }
    }
}
