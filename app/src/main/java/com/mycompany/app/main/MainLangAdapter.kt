package com.mycompany.app.main

import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.os.Looper
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbRecentLang
import kotlin.math.min

/**
 * List model of the translation-language picker ([com.mycompany.app.dialog.DialogTransLang], drawn in Compose):
 * recent languages (from DbRecentLang) followed by all languages, or only the matches while searching;
 * [sel] is the selected language (recent rows are +1000). The old RecyclerView adapter part
 * (view holders built from `MyLineRelative`) was no longer attached to any list and was removed on 2026-11-17.
 */
class MainLangAdapter(
    var ctx: Context?,
    recentTypeParam: Int,
    var recent: MutableList<MainLangItem?>?,
    var items: ArrayList<MainLangItem?>?,
    var sel: Int,
    var listener: MainLangListener?
) {
    @JvmField
    var recentType: Byte
    @JvmField
    var selectedCode: String? = null
    @JvmField
    var filtered: Boolean = false
    @JvmField
    var showTranslated: Boolean = false
    @JvmField
    var removing: Boolean = false
    @JvmField
    var mainHandler: Handler? = null
    @JvmField
    var lastCount: Int = 0

    class MainLangItem {
        @JvmField
        var rowType: Byte = 0
        @JvmField
        var id: Long = 0
        @JvmField
        var code: Int = 0
        @JvmField
        var lang: String? = null
        @JvmField
        var tag: String? = null
        @JvmField
        var match: String? = null
        @JvmField
        var translated: String? = null
        @JvmField
        var paraText: String? = null
        @JvmField
        var lowerTrans: String? = null
        @JvmField
        var corner: Byte = 0
    }

    interface MainLangListener {
        fun onPick(position: Int, lang: String?)

        fun onRemoved()
    }

    init {
        this.recentType = recentTypeParam.toByte()
    }

    fun getItemCount(): Int {
        val lastCount = this.lastCount
        if (lastCount != 0) {
            return lastCount
        }
        if (this.filtered) {
            val arrayList = this.items
            if (arrayList != null) {
                return arrayList.size
            }
            return 0
        }
        val list = this.recent
        val size = if (list != null) list.size else 0
        val items = this.items
        return size + (if (items != null) items.size else 0)
    }

    /**
     * The "x" on a recent language: removes [item] from the recent block and from DbRecentLang (in the background),
     * moves the selection off it, then tells the listener ([MainLangListener.onRemoved]) on the main thread. Ignored while a
     * removal is running ([removing]).
     */
    fun removeRecent(item: MainLangItem) {
        if (this.removing) return
        this.removing = true
        MainApp.runInBackground(this.ctx) {
            val list = if (item.rowType.toInt() == 1) this.recent else null
            if (list != null && list.isNotEmpty()) {
                try {
                    list.remove(item)
                    if (list.size <= 1) this.recent = null
                    if (item.code == this.sel) this.sel -= 1000
                    val ctx = this.ctx
                    if (ctx != null && item.id > 0) {
                        DbUtil.deleteById(DbRecentLang.get(ctx).getWritableDatabase(), "DbRecentLang_table", item.id)
                    }
                    if (this.mainHandler == null) this.mainHandler = Handler(Looper.getMainLooper())
                    this.mainHandler!!.post {
                        this.listener?.onRemoved()
                        this.removing = false
                    }
                    return@runInBackground
                } catch (unused: Exception) {
                }
            }
            this.removing = false
        }
    }

    fun clearRecent() {
        val list = this.recent
        if (list == null || list.isEmpty()) {
            return
        }
        this.recent = null
        val sel = this.sel
        if (sel >= 1000) {
            this.sel = sel - 1000
        }
        DbRecentLang.trimType(this.ctx, this.recentType.toInt())
    }

    fun itemAt(pos: Int): MainLangItem? {
        if (this.filtered) {
            val arrayList = this.items
            if (arrayList == null || pos < 0 || pos >= arrayList.size) {
                return null
            }
            return this.items!!.get(pos)
        }
        val list = this.recent
        val size = if (list != null) list.size else 0
        if (pos < size) {
            val recent = this.recent
            if (recent == null || pos < 0 || pos >= recent.size) {
                return null
            }
            return this.recent!!.get(pos)
        }
        val index = pos - size
        val items = this.items
        if (items == null || index < 0 || index >= items.size) {
            return null
        }
        return this.items!!.get(index)
    }

    fun trimRecent(): Boolean {
        // Trims the recent-languages block to the current limit; [sel] is the selected index (recent ones are +1000).
        val recent = this.recent
        if (recent == null || recent.isEmpty()) {
            return false
        }
        val type = this.recentType.toInt()
        var limit: Int = DbRecentLang.limitOf(type)
        if (limit != 0) {
            if (limit == -1) {
                limit = Int.MAX_VALUE
            }
            var count = 0
            if (this.ctx != null) {
                var cursor: Cursor? = null
                try {
                    cursor = DbUtil.query(
                        DbRecentLang.get(this.ctx).getWritableDatabase(),
                        "DbRecentLang_table",
                        null,
                        "_type=?",
                        arrayOf<String?>(type.toString()),
                        null
                    )
                    if (cursor != null) {
                        count = cursor.getCount()
                    }
                } catch (unused: Exception) {
                    count = 0
                }
                if (cursor != null) {
                    cursor.close()
                }
            }
            limit = min(limit, count)
        }
        if (limit == 0) {
            dropRecent()
            return true
        }
        try {
            val kept: ArrayList<com.mycompany.app.main.MainLangAdapter.MainLangItem?> = ArrayList()
            val size = this.recent!!.size
            for (k in 0..<size) {
                val item = this.recent!!.get(k)
                if (item == null || item.rowType.toInt() != 1) {
                    continue
                }
                if (k < limit) {
                    kept.add(item)
                } else if (item.code == this.sel) {
                    this.sel -= 1000
                }
            }
            if (kept.isEmpty()) {
                dropRecent()
                return true
            }
            val sep = MainLangItem()
            sep.rowType = 2
            sep.code = -1
            kept.add(sep)
            this.recent = kept
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    private fun dropRecent() {
        this.recent = null
        if (this.sel >= 1000) {
            this.sel -= 1000
        }
    }
}
