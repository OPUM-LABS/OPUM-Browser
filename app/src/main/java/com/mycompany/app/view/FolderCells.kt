package com.mycompany.app.view

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainUtil
import com.mycompany.app.quick.QuickAdapter.QuickSubItem
import com.mycompany.app.quick.QuickIcons

/**
 * The four small cells of a shortcut-folder icon: each the favicon of one of the folder's first four shortcuts, or
 * its letter on its colour. Shared by [MyRoundImage] and the Compose tile (`quick/QuickTileView`). Favicons missing
 * from the cache load in the background; [onChange] runs on the main thread when a cell changed later.
 */
class FolderCells(private val context: Context?, private val onChange: () -> Unit) {
    private val main = Handler(Looper.getMainLooper())
    val bitmap = arrayOfNulls<Bitmap>(4)
    val letters = arrayOfNulls<String>(4)
    val color = IntArray(4)

    /** Folder whose items are shown (0 = none). */
    var folderId = 0
        private set
    var items: List<Any?>? = null
        private set
    private var secret = false
    /** False after [release]: late background results are dropped. */
    private var alive = true

    val isActive get() = items != null

    /**
     * Shows the first four [list] items of folder [id]; [secret] reads the secret-mode favicon cache. Returns true if a
     * cell shows something right away. An empty list or folder 0 clears it.
     */
    fun set(id: Int, list: List<Any?>?, secret: Boolean): Boolean {
        folderId = id
        items = list
        this.secret = secret
        if (id == 0 || list == null || list.isEmpty()) {
            clear()
            return false
        }
        var shown = false
        for (cell in 3 downTo 0) {
            if (load(cell)) shown = true
        }
        return shown
    }

    /** Fills [cell] from the folder list; true if it shows something now. */
    fun load(cell: Int): Boolean {
        val item = items?.getOrNull(cell) as QuickSubItem?
        if (item == null) {
            setBitmap(cell, null)
            clearText(cell)
            return false
        }
        val itemLetter = item.letterText
        val color = item.color
        val cached = if (TextUtils.isEmpty(item.iconUrl) || item.letterOnly) null else QuickIcons.cached(item.iconUrl, secret)
        if (cached != null) {
            clearText(cell)
            setBitmap(cell, cached)
            return true
        }
        // the letter first (on its colour); a favicon found later replaces it
        val shown = if (TextUtils.isEmpty(itemLetter)) {
            setBitmap(cell, null)
            clearText(cell)
            false
        } else {
            setText(cell, color, itemLetter)
            true
        }
        if (TextUtils.isEmpty(item.iconUrl) || item.letterOnly) return shown
        val id = folderId
        val iconUrl = item.iconUrl!!
        val ctx = context
        val secret = secret
        MainApp.runInBackground(ctx, Runnable {
            if (!alive || id != folderId) return@Runnable
            val loaded = QuickIcons.load(ctx, iconUrl, secret) ?: return@Runnable
            main.post {
                if (!alive || id != folderId || (items?.getOrNull(cell) as QuickSubItem?)?.iconUrl != iconUrl) return@post
                clearText(cell)
                setBitmap(cell, loaded)
                onChange()
            }
        })
        return shown
    }

    fun setBitmap(cell: Int, value: Bitmap?) {
        if (cell in 0..3) bitmap[cell] = value
    }

    /** [cell] shows [letterText] on colour [newColor] (0 = red). */
    fun setText(cell: Int, newColor: Int, letterText: String?) {
        if (cell !in 0..3) return
        bitmap[cell] = null
        letters[cell] = letterText
        color[cell] = if (newColor == 0) -65536 else newColor
    }

    fun clearText(cell: Int) {
        if (cell in 0..3) letters[cell] = null
    }

    fun clear() {
        items = null
        bitmap.fill(null)
        letters.fill(null)
    }

    fun release() {
        alive = false
        clear()
    }
}
