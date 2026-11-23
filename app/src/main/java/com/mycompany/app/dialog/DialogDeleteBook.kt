package com.mycompany.app.dialog

import com.mycompany.app.ui.browser.SheetTitle
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.main.ViewItemListener
import com.mycompany.app.main.JarBridge
import com.mycompany.app.main.MainItem
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import android.widget.ImageView.ScaleType
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.book.DataBookAds
import com.mycompany.app.data.book.DataBookAlbum
import com.mycompany.app.data.book.DataBookBlock
import com.mycompany.app.data.book.DataBookCmp
import com.mycompany.app.data.book.DataBookDown
import com.mycompany.app.data.book.DataBookFilter
import com.mycompany.app.data.book.DataBookHistory
import com.mycompany.app.data.book.DataBookJava
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.data.book.DataBookOver
import com.mycompany.app.data.book.DataBookPdf
import com.mycompany.app.data.book.DataBookPop
import com.mycompany.app.data.book.DataBookScript
import com.mycompany.app.data.book.DataBookTmem
import com.mycompany.app.data.book.DataBookTrans
import com.mycompany.app.data.book.DataBookUser
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.db.DbUtil.deleteById
import com.mycompany.app.db.DbUtil.query
import com.mycompany.app.db.DbUtil.updateById
import com.mycompany.app.db.book.DbBookAds
import com.mycompany.app.db.book.DbBookAgent
import com.mycompany.app.db.book.DbBookAlbum
import com.mycompany.app.db.book.DbBookBlock
import com.mycompany.app.db.book.DbBookCmp
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.db.book.DbBookDown.Companion.deleteById
import com.mycompany.app.db.book.DbBookDown.Companion.deleteFile
import com.mycompany.app.db.book.DbBookFilter
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookHistory.Companion.clearSecret
import com.mycompany.app.db.book.DbBookJava
import com.mycompany.app.db.book.DbBookLink
import com.mycompany.app.db.book.DbBookMemo
import com.mycompany.app.db.book.DbBookOver
import com.mycompany.app.db.book.DbBookPage
import com.mycompany.app.db.book.DbBookPdf
import com.mycompany.app.db.book.DbBookPms
import com.mycompany.app.db.book.DbBookPop
import com.mycompany.app.db.book.DbBookScript
import com.mycompany.app.db.book.DbBookScript.Companion.cached; import com.mycompany.app.db.book.DbBookScript.Companion.deleteScript
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.db.book.DbBookTab.Companion.deleteById
import com.mycompany.app.db.book.DbBookTmem
import com.mycompany.app.db.book.DbBookTrans
import com.mycompany.app.db.book.DbBookUser
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.db.book.DbBookWeb.Companion.deleteById
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainItem.ViewItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUri.createIn
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUtil.deleteUri
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isBitmapOk
import com.mycompany.app.main.MainUtil.countLabel
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.pref.PrefSet.remove
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.view.MyRoundImage
import com.mycompany.app.web.WebClean

import java.io.File
import java.lang.ref.WeakReference
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * Delete items of a list ([listType] = list type: album, PDF, archive, bookmarks, history, site lists, blocked images,
 * filters, scripts, downloads, search engines, user agents, memos, tabs…; [list] items, [headText] a folder name, [withFiles] all of
 * them, [withData] also delete the files): header with the item icon (favicon, thumbnail, type icon or letter) and its name
 * or "n items", Delete (downloads: Delete file / Delete record / Stop); while deleting Total n / m with a progress bar
 * and Cancel / Canceling. The icon is still drawn by an off-screen `MyRoundImage` ([iconImage]) with the old per-list rules;
 * the delete work ([DialogTask]) and [DeleteBookListener] are the old code. Compose now (on [ComposeBottomDialog]); same
 * constructor, shows itself.
 */
@Suppress("deprecation")
class DialogDeleteBook(
    activity: Activity?,
    listType: Int,
    list: MutableList<Any?>?,
    headText: String?,
    withFiles: Boolean,
    withData: Boolean,
    deleteBookListener: DeleteBookListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var appContext: Context? = null
    @JvmField
    var deleteListener: DeleteBookListener? = null
    /** Downloads list: show the "Keep the file" choice (set by [askKeepFile] right after construction). */
    private var askKeep by androidx.compose.runtime.mutableStateOf(false)
    private var keepFile by androidx.compose.runtime.mutableStateOf(false)

    /** One Delete for downloads, with a "Keep the file" checkbox (unchecked = file and record go). */
    fun askKeepFile() { askKeep = true }
    @JvmField
    var listType: Int = 0
    @JvmField
    var deleteItems: MutableList<Any?>? = null
    @JvmField
    var headText: String? = null
    @JvmField
    var withFiles: Boolean = false
    @JvmField
    var withData: Boolean = false
    /** Off-screen icon view (old header icon): the per-list icon rules draw into it, [icon] shows its bitmap. */
    @JvmField
    var iconImage: MyRoundImage? = null
    @JvmField
    var deleteTask: DialogTask? = null
    @JvmField
    var listLoader: MainListLoader? = null
    @JvmField
    var deleting: Boolean = false
    /** One running download: the button stops it. */
    @JvmField
    var hasDownloads: Boolean = false
    @JvmField
    var webClean: WebClean? = null

    private var head by mutableStateOf("")
    private var icon by mutableStateOf<Bitmap?>(null)
    private var running by mutableStateOf(false)
    internal var countText by mutableStateOf("")
    internal var progress by mutableFloatStateOf(0f)
    internal var progressMax by mutableIntStateOf(1)
    private var mainRes by mutableIntStateOf(AppR.string.delete)
    private var mainEnabled by mutableStateOf(true)

    private val ui = android.os.Handler(android.os.Looper.getMainLooper())

    internal fun setHead(name: CharSequence?) {
        head = name?.toString() ?: ""
    }

    internal fun setMain(res: Int) {
        mainRes = res
    }

    internal fun refreshIcon() {
        icon = MainUtil.viewSnapshot(iconImage, 1.0f)
        ui.postDelayed({ if (iconImage != null) icon = MainUtil.viewSnapshot(iconImage, 1.0f) }, 300)
    }

    internal inner class LoadForTask : Runnable {

        override fun run() {
            val matchType: Int
            var items: MutableList<Any?>? = null
            var iconRes: Int = 0
            val itemType: Byte
            var childItem: ChildItem?
            val bitmapB: Bitmap?
            val status: Int
            val dialogDeleteBook = this@DialogDeleteBook
            val listType = dialogDeleteBook.listType
            if (dialogDeleteBook.appContext == null) {
                return
            }
            if (TextUtils.isEmpty(dialogDeleteBook.headText)) {
                if (dialogDeleteBook.deleteItems!!.size == 1) {
                    val firstItem = dialogDeleteBook.deleteItems!!.get(0) as ChildItem?
                    if (firstItem == null) {
                        dialogDeleteBook.setHead("1" + dialogDeleteBook.appContext!!.getString(AppR.string.items))
                    } else if (listType == 26) {
                        val noBadge = TextUtils.isEmpty(firstItem.badge)
                        if (noBadge) {
                            dialogDeleteBook.setHead(firstItem.name)
                        } else {
                            dialogDeleteBook.setHead(firstItem.badge)
                        }
                    } else {
                        dialogDeleteBook.setHead(firstItem.name)
                    }
                } else {
                    dialogDeleteBook.setHead(
                        dialogDeleteBook.deleteItems!!.size.toString() + dialogDeleteBook.appContext!!.getString(
                            AppR.string.items
                        )
                    )
                }
                if (listType == 23 || listType == 26) {
                    val list: MutableList<Any?>? = if (dialogDeleteBook.iconImage != null) dialogDeleteBook.deleteItems else null
                    if (dialogDeleteBook.iconImage != null && list != null && !list.isEmpty()) {
                        val listIcon =
                            if (listType == 26) AppR.drawable.outline_extension_black_24 else AppR.drawable.outline_image_black_24
                        if (dialogDeleteBook.deleteItems!!.size != 1) {
                            dialogDeleteBook.iconImage!!.showIcon(-460552, listIcon)
                        } else {
                            val firstItem = dialogDeleteBook.deleteItems!!.get(0) as ChildItem?
                            if (firstItem != null) {
                                val url = if (listType == 26) firstItem.dir else firstItem.name
                                if (TextUtils.isEmpty(url)) {
                                    dialogDeleteBook.iconImage!!.showIcon(-460552, listIcon)
                                } else {
                                    val bitmapA = com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, url))
                                    if (MainUtil.isBitmapOk(bitmapA)) {
                                        if (listType == 26) {
                                            dialogDeleteBook.iconImage!!.setIconSmall(true)
                                        }
                                        dialogDeleteBook.iconImage!!.setImageBitmap(bitmapA)
                                    } else {
                                        dialogDeleteBook.iconImage!!.showIcon(-460552, listIcon)
                                        val viewItem = ViewItem()
                                        viewItem.itemType = 7.toByte()
                                        viewItem.url = url
                                        viewItem.referer = url
                                        viewItem.position = firstItem.position
                                        viewItem.cacheType = 2
                                        com.mycompany.app.glide.ThumbLoader.loadFor(viewItem, dialogDeleteBook.iconImage, dialogDeleteBook.ThumbListener())
                                    }
                                }
                            }
                        }
                    }
                } else if (dialogDeleteBook.iconImage != null && (dialogDeleteBook.deleteItems.also {
                        items = it
                    }) != null && !items!!.isEmpty()) {
                    if (listType == 19 || listType == 20 || listType == 21 || listType == 22 || listType == 27 || listType == 28 || listType == 29) {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_public_black_24)
                    } else if (dialogDeleteBook.deleteItems!!.size == 1) {
                        val firstItem = dialogDeleteBook.deleteItems!!.get(0) as ChildItem?
                        if (firstItem != null) {
                            if (listType == 31) {
                                if (firstItem.status != 3) {
                                    dialogDeleteBook.iconImage!!.showIcon(firstItem.iconColor, firstItem.iconRes)
                                } else {
                                    itemType = firstItem.itemType
                                    if (itemType.toInt() != 1 || itemType.toInt() == 2 || itemType.toInt() == 3 || itemType.toInt() == 4 || itemType.toInt() == 5 || itemType.toInt() == 6 || itemType.toInt() == 11) {
                                        childItem = ChildItem()
                                        if (itemType.toInt() == 11) {
                                            if (listType == 41) {
                                                childItem.listType = 39
                                                childItem.path = firstItem.path
                                                childItem.id = firstItem.uid
                                            } else {
                                                childItem.listType = listType
                                                childItem.path = firstItem.filePath
                                                childItem.id = firstItem.id
                                            }
                                            childItem.itemType = itemType
                                            childItem.filePath = firstItem.filePath
                                            childItem.position = firstItem.position
                                            childItem.iconColor = firstItem.iconColor
                                            childItem.iconRes = firstItem.iconRes
                                        } else {
                                            childItem = firstItem
                                        }
                                        if (!TextUtils.isEmpty(childItem.path)) {
                                            bitmapB = MainListLoader.cachedIconFor(childItem)
                                            if (MainUtil.isBitmapOk(bitmapB)) {
                                                if (childItem.itemType.toInt() == 4) {
                                                    dialogDeleteBook.iconImage!!.setBackColor(-460552)
                                                }
                                                dialogDeleteBook.applyIconSize()
                                                dialogDeleteBook.iconImage!!.setImageBitmap(bitmapB)
                                            } else {
                                                dialogDeleteBook.listLoader = MainListLoader(
                                                    dialogDeleteBook.appContext!!,
                                                    false,
                                                    dialogDeleteBook.IconLoadListener()
                                                )
                                                if (listType != 17 || listType == 18 || listType == 35) {
                                                    dialogDeleteBook.iconImage!!.showLetterIcon(
                                                        firstItem.iconColor,
                                                        firstItem.iconRes,
                                                        firstItem.name,
                                                        null
                                                    )
                                                } else {
                                                    dialogDeleteBook.iconImage!!.showIcon(
                                                        childItem.iconColor,
                                                        childItem.iconRes
                                                    )
                                                }
                                                dialogDeleteBook.iconImage!!.setTag(childItem.position)
                                                dialogDeleteBook.listLoader!!.enqueue(
                                                    childItem,
                                                    dialogDeleteBook.iconImage
                                                )
                                            }
                                        } else if (listType != 17 || listType == 18 || listType == 35) {
                                            dialogDeleteBook.iconImage!!.showLetterIcon(
                                                firstItem.iconColor,
                                                firstItem.iconRes,
                                                firstItem.name,
                                                null
                                            )
                                        } else {
                                            dialogDeleteBook.iconImage!!.showIcon(childItem.iconColor, childItem.iconRes)
                                        }
                                    } else if (listType == 17 || listType == 18 || listType == 35) {
                                        dialogDeleteBook.iconImage!!.showLetterIcon(
                                            firstItem.iconColor,
                                            firstItem.iconRes,
                                            firstItem.name,
                                            null
                                        )
                                    } else {
                                        dialogDeleteBook.iconImage!!.showIcon(firstItem.iconColor, firstItem.iconRes)
                                    }
                                }
                            } else if (listType == 18 && (firstItem.iconRes.also {
                                    iconRes = it
                                }) == AppR.drawable.outline_offline_pin_black_24) {
                                dialogDeleteBook.iconImage!!.showIcon(firstItem.iconColor, iconRes)
                            } else {
                                itemType = firstItem.itemType
                                if (itemType.toInt() != 1) {
                                    childItem = ChildItem()
                                    if (itemType.toInt() == 11) {
                                        if (listType == 41) {
                                            childItem.listType = 39
                                            childItem.path = firstItem.path
                                            childItem.id = firstItem.uid
                                        } else {
                                            childItem.listType = listType
                                            childItem.path = firstItem.filePath
                                            childItem.id = firstItem.id
                                        }
                                        childItem.itemType = itemType
                                        childItem.filePath = firstItem.filePath
                                        childItem.position = firstItem.position
                                        childItem.iconColor = firstItem.iconColor
                                        childItem.iconRes = firstItem.iconRes
                                    } else {
                                        childItem = firstItem
                                    }
                                    if (!TextUtils.isEmpty(childItem.path)) {
                                        bitmapB = MainListLoader.cachedIconFor(childItem)
                                        if (MainUtil.isBitmapOk(bitmapB)) {
                                            if (childItem.itemType.toInt() == 4) {
                                                dialogDeleteBook.iconImage!!.setBackColor(-460552)
                                            }
                                            dialogDeleteBook.applyIconSize()
                                            dialogDeleteBook.iconImage!!.setImageBitmap(bitmapB)
                                        } else {
                                            dialogDeleteBook.listLoader = MainListLoader(
                                                dialogDeleteBook.appContext!!,
                                                false,
                                                dialogDeleteBook.IconLoadListener()
                                            )
                                            if (listType != 17) {
                                                dialogDeleteBook.iconImage!!.showLetterIcon(
                                                    firstItem.iconColor,
                                                    firstItem.iconRes,
                                                    firstItem.name,
                                                    null
                                                )
                                            } else {
                                                dialogDeleteBook.iconImage!!.showLetterIcon(
                                                    firstItem.iconColor,
                                                    firstItem.iconRes,
                                                    firstItem.name,
                                                    null
                                                )
                                            }
                                            dialogDeleteBook.iconImage!!.setTag(childItem.position)
                                            dialogDeleteBook.listLoader!!.enqueue(childItem, dialogDeleteBook.iconImage)
                                        }
                                    } else if (listType != 17) {
                                        dialogDeleteBook.iconImage!!.showLetterIcon(
                                            firstItem.iconColor,
                                            firstItem.iconRes,
                                            firstItem.name,
                                            null
                                        )
                                    } else {
                                        dialogDeleteBook.iconImage!!.showLetterIcon(
                                            firstItem.iconColor,
                                            firstItem.iconRes,
                                            firstItem.name,
                                            null
                                        )
                                    }
                                } else {
                                    childItem = ChildItem()
                                    if (itemType.toInt() == 11) {
                                        if (listType == 41) {
                                            childItem.listType = 39
                                            childItem.path = firstItem.path
                                            childItem.id = firstItem.uid
                                        } else {
                                            childItem.listType = listType
                                            childItem.path = firstItem.filePath
                                            childItem.id = firstItem.id
                                        }
                                        childItem.itemType = itemType
                                        childItem.filePath = firstItem.filePath
                                        childItem.position = firstItem.position
                                        childItem.iconColor = firstItem.iconColor
                                        childItem.iconRes = firstItem.iconRes
                                    } else {
                                        childItem = firstItem
                                    }
                                    if (!TextUtils.isEmpty(childItem.path)) {
                                        bitmapB = MainListLoader.cachedIconFor(childItem)
                                        if (MainUtil.isBitmapOk(bitmapB)) {
                                            if (childItem.itemType.toInt() == 4) {
                                                dialogDeleteBook.iconImage!!.setBackColor(-460552)
                                            }
                                            dialogDeleteBook.applyIconSize()
                                            dialogDeleteBook.iconImage!!.setImageBitmap(bitmapB)
                                        } else {
                                            dialogDeleteBook.listLoader = MainListLoader(
                                                dialogDeleteBook.appContext!!,
                                                false,
                                                dialogDeleteBook.IconLoadListener()
                                            )
                                            if (listType != 17) {
                                                dialogDeleteBook.iconImage!!.showLetterIcon(
                                                    firstItem.iconColor,
                                                    firstItem.iconRes,
                                                    firstItem.name,
                                                    null
                                                )
                                            } else {
                                                dialogDeleteBook.iconImage!!.showLetterIcon(
                                                    firstItem.iconColor,
                                                    firstItem.iconRes,
                                                    firstItem.name,
                                                    null
                                                )
                                            }
                                            dialogDeleteBook.iconImage!!.setTag(childItem.position)
                                            dialogDeleteBook.listLoader!!.enqueue(childItem, dialogDeleteBook.iconImage)
                                        }
                                    } else if (listType != 17) {
                                        dialogDeleteBook.iconImage!!.showLetterIcon(
                                            firstItem.iconColor,
                                            firstItem.iconRes,
                                            firstItem.name,
                                            null
                                        )
                                    } else {
                                        dialogDeleteBook.iconImage!!.showLetterIcon(
                                            firstItem.iconColor,
                                            firstItem.iconRes,
                                            firstItem.name,
                                            null
                                        )
                                    }
                                }
                            }
                        }
                    } else if (listType == 14) {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_local_library_black_24)
                    } else if (listType == 15) {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_picture_as_pdf_black_24)
                    } else if (listType == 16) {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_folder_zip_black_24)
                    } else if (listType == 24 || listType == 25) {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_verified_user_black_24)
                    } else if (listType == 34) {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_search_black_24)
                    } else if (listType == 36) {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_text_snippet_black_24)
                    } else if (listType == 17 || listType == 18 || listType == 35 || listType == 41) {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_public_black_24)
                    } else {
                        dialogDeleteBook.iconImage!!.showIcon(-460552, AppR.drawable.outline_draft_black_24)
                    }
                }
                matchType = 31
            } else {
                dialogDeleteBook.setHead(dialogDeleteBook.headText)
                dialogDeleteBook.iconImage!!.showIcon(0, AppR.drawable.outline_folder_black_24)
                matchType = 31
            }
            if (listType == matchType) {
                val onlyItem: ChildItem? = if (dialogDeleteBook.deleteItems!!.size == 1) (dialogDeleteBook.deleteItems!!.get(0) as ChildItem?) else null
                if (dialogDeleteBook.deleteItems!!.size == 1 && onlyItem != null && onlyItem.listType != 8 && ((onlyItem.status.also {
                        status = it
                    }) == 1 || status == 2)) {
                    val hasDownloads = onlyItem.size == 0L && onlyItem.readSize > 0
                    dialogDeleteBook.hasDownloads = hasDownloads
                    if (hasDownloads) {
                        dialogDeleteBook.setMain(AppR.string.stop)
                    }
                }
                if (!dialogDeleteBook.hasDownloads) {
                    val withData = dialogDeleteBook.withData
                    if (withData) {
                        dialogDeleteBook.setMain(AppR.string.delete_file)
                    } else {
                        dialogDeleteBook.setMain(AppR.string.delete_record)
                    }
                }
            } else {
                dialogDeleteBook.setMain(AppR.string.delete)
            }
            dialogDeleteBook.refreshIcon()
        }
    }

    internal inner class IconLoadListener : ListLoadListener {
        override fun onFailed(childItem: ChildItem?, view: View?) {
        }

        override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
            val itemType = childItem!!.itemType
            val dialogDeleteBook = this@DialogDeleteBook
            if (itemType.toInt() == 4) {
                dialogDeleteBook.iconImage!!.setBackColor(-460552)
            }
            dialogDeleteBook.iconImage!!.usePath(null, true)
            dialogDeleteBook.applyIconSize()
            dialogDeleteBook.iconImage!!.setImageBitmap(bitmap)
            dialogDeleteBook.refreshIcon()
        }
    }

    internal inner class ThumbListener : ViewItemListener {
        override fun onComplete(viewItem: MainItem.ViewItem?, view: View?, bitmap: Bitmap?) {
            var dialogDeleteBook: DialogDeleteBook? = null
            val myRoundImage: MyRoundImage? = if (!MainUtil.isBitmapOk(bitmap)) null else (this@DialogDeleteBook.also {
                    dialogDeleteBook = it
                }).iconImage
            if (!MainUtil.isBitmapOk(bitmap) || myRoundImage == null) {
                return
            }
            if (dialogDeleteBook!!.listType == 26) {
                myRoundImage.setIconSmall(true)
            }
            dialogDeleteBook.iconImage!!.usePath(null, true)
            dialogDeleteBook.iconImage!!.setImageBitmap(bitmap)
            dialogDeleteBook.refreshIcon()
        }
    }

    interface DeleteBookListener {
        fun onCancelled()

        fun onDeleted()
    }

    class DialogTask(dialogDeleteBook: DialogDeleteBook?) : MyAsyncTask() {
        @JvmField
        var dialogRef: WeakReference<*>? = null
        @JvmField
        var listType: Int = 0
        @JvmField
        var items: MutableList<Any?>? = null
        @JvmField
        var withFiles: Boolean = false
        @JvmField
        var withData: Boolean = false
        @JvmField
        var total: Int = 0
        @JvmField
        var done: Int = 0

        init {
        run init@{
            val weakReference: WeakReference<*> = WeakReference<Any?>(dialogDeleteBook)
            this.dialogRef = weakReference
            val dialogDeleteBook = weakReference.get() as DialogDeleteBook?
            if (dialogDeleteBook == null) {
                return@init
            }
            this.listType = dialogDeleteBook.listType
            this.items = dialogDeleteBook.deleteItems
            this.withFiles = dialogDeleteBook.withFiles
            this.withData = dialogDeleteBook.withData

        }
    }

        /** Deletes the selected items (or the whole list) for [listType], publishing progress per item.  */
        public override fun doInBackground() {
            val dialog = this.dialogRef!!.get() as DialogDeleteBook?
            if (dialog == null || this.cancelled) {
                return
            }
            val context = dialog.appContext
            if (context == null || this.items == null || this.items!!.isEmpty()) {
                return
            }
            this.items = ArrayList<Any?>(this.items!!)
            this.total = this.items!!.size
            if (this.withFiles) {
                deleteAll(dialog, context)
                if (this.listType == 17 || this.listType == 18) {
                    return
                }
                for (item2 in this.items!!) {
                    if (this.cancelled) {
                        return
                    }
                    val item = item2 as ChildItem?
                    if (item == null) {
                        continue
                    }
                    if (this.listType == 31 && this.withData && !TextUtils.isEmpty(item.path)) {
                        deleteUri(context, item.path)
                    }
                    if (!TextUtils.isEmpty(item.filePath)) {
                        com.mycompany.app.glide.ImageCache.removeUrl(item.filePath)
                    }
                    this.done++
                    publishProgress()
                }
                return
            }
            for (item2 in this.items!!) {
                if (this.cancelled) {
                    return
                }
                val item = item2 as ChildItem?
                if (item == null) {
                    continue
                }
                deleteOne(dialog, context, item)
                this.done++
                publishProgress()
            }
            if (this.listType == 25) {
                refreshUserFilter(context)
            }
        }

        fun deleteAll(dialog: DialogDeleteBook?, context: Context?) {
            when (this.listType) {
                14 -> {
                    DataBookAlbum.instance(context).clear()
                    deleteWhere(DbBookAlbum.get(context).getWritableDatabase(), "DbBookAlbum_table", null, null)
                }

                15 -> {
                    DataBookPdf.instance(context).clear()
                    deleteWhere(DbBookPdf.get(context).getWritableDatabase(), "DbBookPdf_table", null, null)
                }

                16 -> {
                    DataBookCmp.instance(context).clear()
                    deleteWhere(DbBookCmp.get(context).getWritableDatabase(), "DbBookCmp_table", null, null)
                }

                17 -> DbUtil.deleteWhere(
                    DbBookWeb.get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    "_secret=?",
                    arrayOf<String?>(
                        secretArg()
                    )
                )

                18 -> {
                    DataBookHistory.instance(context).clear()
                    clearSecret(context, SyncSettings.secretMode.value)
                }

                19 -> {
                    DataBookAds.instance(context).sites = null
                    DataBookAds.instance(context).clear()
                    deleteWhere(DbBookAds.get(context).getWritableDatabase(), "DbBookAds_table", null, null)
                }

                20 -> {
                    DataBookOver.instance(context).paths = null
                    DataBookOver.instance(context).clear()
                    deleteWhere(DbBookOver.get(context).getWritableDatabase(), "DbBookOver_table", null, null)
                }

                21 -> {
                    DataBookPop.instance(context).sites = null
                    DataBookPop.instance(context).clear()
                    deleteWhere(DbBookPop.get(context).getWritableDatabase(), "DbBookPop_table", null, null)
                }

                22 -> {
                    DataBookLink.instance(context).links = null
                    DataBookLink.instance(context).clear()
                    deleteWhere(DbBookLink.get(context).getWritableDatabase(), "DbBookLink_table", null, null)
                }

                23 -> {
                    if (dialog!!.webClean != null) {
                        dialog.webClean!!.userBlocks = null
                        dialog.webClean!!.userBlocksCopy = null
                    }
                    DataBookBlock.instance(context).clear()
                    deleteWhere(DbBookBlock.get(context).getWritableDatabase(), "DbBookBlock_table", null, null)
                }

                24 -> {
                    DataBookFilter.instance(context).clear()
                    deleteWhere(
                        DbBookFilter.get(context).getWritableDatabase(),
                        "DbBookFilter_table",
                        null,
                        null
                    )
                    for (item2 in this.items!!) {
                        val item = item2 as ChildItem?
                        try {
                            if (item != null && !TextUtils.isEmpty(item.filePath)) {
                                File(item.filePath!!).delete()
                            }
                        } catch (unused: Exception) {
                        }
                    }
                    deleteWhere(DbBookUser.get(context).getWritableDatabase(), "DbBookUser_table", null, null)
                }

                25 -> {
                    DataBookUser.instance(context).clear()
                    deleteWhere(DbBookUser.get(context).getWritableDatabase(), "DbBookUser_table", null, null)
                    DataBookFilter.instance(context).removeUserFilter()
                    DbUtil.deleteWhere(
                        DbBookFilter.get(context).getWritableDatabase(),
                        "DbBookFilter_table",
                        "_path=?",
                        arrayOf<String?>("sb_user_filter_path")
                    )
                }

                26 -> {
                    DataBookScript.instance(context).clear()
                    deleteWhere(
                        DbBookScript.get(context).getWritableDatabase(),
                        "DbBookScript_table",
                        null,
                        null
                    )
                }

                27 -> {
                    DataBookJava.instance(context).sites = null
                    DataBookJava.instance(context).clear()
                    deleteWhere(DbBookJava.get(context).getWritableDatabase(), "DbBookJava_table", null, null)
                }

                28 -> {
                    DataBookTmem.instance(context).sites = null
                    DataBookTmem.instance(context).clear()
                    deleteWhere(DbBookTmem.get(context).getWritableDatabase(), "DbBookTmem_table", null, null)
                }

                29 -> {
                    DataBookTrans.instance(context).sites = null
                    DataBookTrans.instance(context).clear()
                    deleteWhere(DbBookTrans.get(context).getWritableDatabase(), "DbBookTrans_table", null, null)
                }

                31 -> deleteAllDownloads(dialog, context)
                34 -> deleteWhere(
                    DbBookSearch.get(context).getWritableDatabase(),
                    "DbBookSearch_table",
                    null,
                    null
                )

                35 -> deleteWhere(
                    DbBookAgent.get(context).getWritableDatabase(),
                    "DbBookAgent_table",
                    null,
                    null
                )

                36 -> deleteWhere(DbBookMemo.get(context).getWritableDatabase(), "DbBookMemo_table", null, null)
                else -> {}
            }
        }

        fun deleteAllDownloads(dialog: DialogDeleteBook?, context: Context?) {
            if (dialog!!.hasDownloads && this.items!!.size == 1) {
                val item = this.items!!.get(0) as ChildItem?
                if (item != null) {
                    cancelDownload(context, item)
                    return
                }
            }
            DataBookDown.instance(context).clear()
            DbUtil.deleteWhere(
                DbBookPage.get(context).getWritableDatabase(),
                "DbBookPage_table",
                "_secret=?",
                arrayOf<String?>(
                    secretArg()
                )
            )
            DbUtil.deleteWhere(
                DbBookDown.get(context).getWritableDatabase(),
                "DbBookDown_table",
                "_secret=?",
                arrayOf<String?>(
                    secretArg()
                )
            )
            val app = MainApp.from(context)
            if (app == null || !app.downBinding) {
                return
            }
            val svc = app.downService
            if (svc == null || svc.appCtx == null) {
                return
            }
            synchronized(svc.itemLock) {
                val queue = svc.downItems
                svc.downItems = null
                if (queue == null || queue.isEmpty()) {
                    return
                }
                try {
                    for (downItem2 in queue) {
                        val downItem = downItem2 as DownItem?
                        if (downItem == null) {
                            continue
                        }
                        downItem.status = 6
                        svc.removeRunning(downItem)
                    }
                    svc.stopIfIdle(false)
                } catch (unused: Exception) {
                }
            }
        }

        fun deleteOne(dialog: DialogDeleteBook?, context: Context?, item: ChildItem?) {
            val id = item!!.id
            when (this.listType) {
                14 -> {
                    DataBookAlbum.instance(context).removeId(id)
                    deleteRow(
                        DbBookAlbum.get(context).getWritableDatabase(),
                        "DbBookAlbum_table",
                        id,
                        item.filePath
                    )
                }

                15 -> {
                    DataBookPdf.instance(context).removeId(id)
                    deleteRow(
                        DbBookPdf.get(context).getWritableDatabase(),
                        "DbBookPdf_table",
                        id,
                        item.filePath
                    )
                }

                16 -> {
                    DataBookCmp.instance(context).removeId(id)
                    deleteRow(
                        DbBookCmp.get(context).getWritableDatabase(),
                        "DbBookCmp_table",
                        id,
                        item.filePath
                    )
                }

                17 -> DbBookWeb.deleteById(context, id)
                18 -> {
                    DataBookHistory.instance(context).removeId(id)
                    deleteRow(
                        DbBookHistory.get(context).getWritableDatabase(),
                        "DbBookHistory_table",
                        id,
                        null
                    )
                }

                19 -> {
                    removePath(DataBookAds.instance(context).sites, item.path)
                    DataBookAds.instance(context).removeId(id)
                    deleteRow(
                        DbBookAds.get(context).getWritableDatabase(),
                        "DbBookAds_table",
                        id,
                        null
                    )
                }

                20 -> {
                    removePath(DataBookOver.instance(context).paths, item.path)
                    DataBookOver.instance(context).removeId(id)
                    deleteRow(
                        DbBookOver.get(context).getWritableDatabase(),
                        "DbBookOver_table",
                        id,
                        null
                    )
                }

                21 -> {
                    removePath(DataBookPop.instance(context).sites, item.path)
                    DataBookPop.instance(context).removeId(id)
                    val db = DbBookPop.get(context).getWritableDatabase()
                    if (id > 0) {
                        deleteById(db, "DbBookPop_table", id)
                    } else if (!TextUtils.isEmpty(item.path)) {
                        deleteWhere(db, "DbBookPop_table", "_path=?", arrayOf<String?>(item.path))
                    }
                }

                22 -> {
                    removePath(DataBookLink.instance(context).links, item.path)
                    DataBookLink.instance(context).removeId(id)
                    deleteRow(
                        DbBookLink.get(context).getWritableDatabase(),
                        "DbBookLink_table",
                        id,
                        null
                    )
                }

                23 -> {
                    if (dialog!!.webClean != null) {
                        dialog.webClean!!.removeUserBlock(item.path, item.name)
                    }
                    DataBookBlock.instance(context).removeId(id)
                    val db = DbBookBlock.get(context).getWritableDatabase()
                    if (id > 0) {
                        deleteById(db, "DbBookBlock_table", id)
                    } else if (!TextUtils.isEmpty(item.path) && !TextUtils.isEmpty(item.name)) {
                        deleteWhere(
                            db,
                            "DbBookBlock_table",
                            "_path=? AND _image=?",
                            arrayOf<String?>(item.path, item.name)
                        )
                    }
                }

                24 -> {
                    DataBookFilter.instance(context).removeId(id)
                    if (id > 0) {
                        if (!TextUtils.isEmpty(item.filePath)) {
                            try {
                                File(item.filePath!!).delete()
                            } catch (unused: Exception) {
                            }
                        }
                        deleteById(DbBookFilter.get(context).getWritableDatabase(), "DbBookFilter_table", id)
                    }
                    if ("sb_user_filter_path" == item.path) {
                        deleteWhere(
                            DbBookUser.get(context).getWritableDatabase(),
                            "DbBookUser_table",
                            null,
                            null
                        )
                    }
                }

                25 -> {
                    DataBookUser.instance(context).removeId(id)
                    deleteRow(
                        DbBookUser.get(context).getWritableDatabase(),
                        "DbBookUser_table",
                        id,
                        null
                    )
                }

                26 -> {
                    DataBookScript.instance(context).removeId(id)
                    deleteScript(context, item.name, item.namespace)
                }

                27 -> {
                    removePath(DataBookJava.instance(context).sites, item.path)
                    DataBookJava.instance(context).removeId(id)
                    deleteRow(
                        DbBookJava.get(context).getWritableDatabase(),
                        "DbBookJava_table",
                        id,
                        null
                    )
                }

                28 -> {
                    removePath(DataBookTmem.instance(context).sites, item.path)
                    DataBookTmem.instance(context).removeId(id)
                    deleteRow(
                        DbBookTmem.get(context).getWritableDatabase(),
                        "DbBookTmem_table",
                        id,
                        null
                    )
                }

                29 -> {
                    removePath(DataBookTrans.instance(context).sites, item.path)
                    DataBookTrans.instance(context).removeId(id)
                    deleteRow(
                        DbBookTrans.get(context).getWritableDatabase(),
                        "DbBookTrans_table",
                        id,
                        null
                    )
                }

                31 -> if (item.listType == 8) {
                    // saved page
                    DataBookDown.instance(context).removeId(-id)
                    if (id > 0) {
                        if (this.withData && !TextUtils.isEmpty(item.path)) {
                            deleteUri(context, item.path)
                        }
                        deleteById(DbBookPage.get(context).getWritableDatabase(), "DbBookPage_table", id)
                    }
                } else if (dialog!!.hasDownloads) {
                    cancelDownload(context, item)
                } else {
                    DataBookDown.instance(context).removeId(id)
                    DbBookDown.deleteFile(id, context, item.path, item.filePath, this.withData)
                    val app = MainApp.from(context)
                    if (app != null && app.downBinding && app.downService != null) {
                        app.downService!!.removeItem(id, false)
                    }
                }

                34 -> deleteRow(
                    DbBookSearch.get(context).getWritableDatabase(),
                    "DbBookSearch_table",
                    id,
                    null
                )

                35 -> deleteRow(
                    DbBookAgent.get(context).getWritableDatabase(),
                    "DbBookAgent_table",
                    id,
                    null
                )

                36 -> deleteRow(
                    DbBookMemo.get(context).getWritableDatabase(),
                    "DbBookMemo_table",
                    id,
                    null
                )

                41 -> DbBookTab.deleteById(context, item.uid)
                else -> {}
            }
        }

        public override fun onCancelled() {
            val dialogDeleteBook = this.dialogRef!!.get() as DialogDeleteBook?
            if (dialogDeleteBook != null) {
                dialogDeleteBook.deleteTask = null
                val deleteBookListener = dialogDeleteBook.deleteListener
                if (deleteBookListener != null) {
                    deleteBookListener.onDeleted()
                }
            }
        }

        public override fun onPostExecute() {
            val dialogDeleteBook = this.dialogRef!!.get() as DialogDeleteBook?
            if (dialogDeleteBook != null) {
                dialogDeleteBook.deleteTask = null
                val deleteBookListener = dialogDeleteBook.deleteListener
                if (deleteBookListener != null) {
                    deleteBookListener.onDeleted()
                }
            }
        }

        public override fun onProgressUpdate() {
            val d = this.dialogRef!!.get() as DialogDeleteBook? ?: return
            if (this.done > this.total) this.done = this.total
            d.countText = MainUtil.countLabel(this.done, this.total)
            d.progressMax = this.total
            d.progress = this.done.toFloat()
        }

        companion object {
            private fun removePath(paths: ArrayList<String?>?, path: String?) {
                try {
                    if (paths != null && !paths.isEmpty() && !TextUtils.isEmpty(path)) {
                        paths.remove(path)
                    }
                } catch (unused: Exception) {
                }
            }

            private fun deleteRow(
                db: SQLiteDatabase?,
                table: String?,
                id: Long,
                imagePath: String?
            ) {
                if (id <= 0) {
                    return
                }
                if (!TextUtils.isEmpty(imagePath)) {
                    com.mycompany.app.glide.ImageCache.removeUrl(imagePath)
                }
                deleteById(db, table, id)
            }

            private fun secretArg(): String {
                return if (SyncSettings.secretMode.value) "1" else "0"
            }

            /** Stops a running download and marks its record as cancelled (state 3) instead of deleting it.  */
            private fun cancelDownload(context: Context?, item: ChildItem?) {
                val cached = DataBookDown.instance(context).itemFor(item!!.id)
                if (cached != null) {
                    cached.status = 3
                    cached.isSecret = false
                }
                DbBookDown.deleteById(context, item.id)
                val app = MainApp.from(context)
                if (app != null && app.downBinding && app.downService != null) {
                    app.downService!!.removeItem(item.id, true)
                }
            }

            /** After deleting user filters, keep the "user filter" entry only while some user rule remains.  */
            private fun refreshUserFilter(context: Context?) {
                var hasRule = false
                var cursor: Cursor? = null
                try {
                    cursor = query(
                        DbBookUser.get(context).getWritableDatabase(),
                        "DbBookUser_table",
                        null,
                        null,
                        null,
                        null
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        val col = cursor.getColumnIndex("_path")
                        do {
                            if (!TextUtils.isEmpty(cursor.getString(col))) {
                                hasRule = true
                                break
                            }
                        } while (cursor.moveToNext())
                    }
                } catch (unused: Exception) {
                }
                if (cursor != null) {
                    cursor.close()
                }
                if (hasRule) {
                    DataBookFilter.instance(context)
                        .putItem(DbBookFilter.addPathItem(context, "sb_user_filter_path", "sb_user_filter_path"))
                } else {
                    DataBookFilter.instance(context).removeUserFilter()
                    DbUtil.deleteWhere(
                        DbBookFilter.get(context).getWritableDatabase(),
                        "DbBookFilter_table",
                        "_path=?",
                        arrayOf<String?>("sb_user_filter_path")
                    )
                }
            }
        }
    }
    init {
        if (list != null && !list.isEmpty()) {
            appContext = context
            deleteListener = deleteBookListener
            this.listType = listType
            deleteItems = list
            this.headText = headText
            this.withFiles = withFiles
            this.withData = withData
            if (listType == 23) webClean = MainApp.webClean(context, false)
            val size = MainApp.dp40
            val image = MyRoundImage(context)
            image.setScaleType(ScaleType.CENTER_CROP)
            image.setCircleRadius(size / 2.0f)
            image.measure(View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.EXACTLY))
            image.layout(0, 0, size, size)
            iconImage = image
            LoadForTask().run()
            // 2026-11-20: a short destructive question, so a centred card with a title ("Delete all" /
            // "Delete"), the items below it, Cancel and a red Delete; was a bottom sheet with only "48 items" and Delete
            setCard {
                SheetTitle(stringResource(if (withFiles) AppR.string.delete_all else AppR.string.delete))
                Row(
                    Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val bmp = icon
                    if (bmp != null) Image(bmp.asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp))
                    Text(
                        head, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(start = 16.dp),
                    )
                }
                if (running) {
                    Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 8.dp)) {
                        Text(stringResource(AppR.string.total), modifier = Modifier.weight(1f))
                        Text(countText)
                    }
                    LinearProgressIndicator(
                        progress = { (progress / progressMax.coerceAtLeast(1)).coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                    )
                }
                if (askKeep && !running) {
                    // downloads: one Delete; the file goes too unless this is on (was two entries, "Delete file" /
                    // "Delete record", 2026-11-20)
                    Row(
                        Modifier.fillMaxWidth().clickable { keepFile = !keepFile }.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        androidx.compose.material3.Checkbox(checked = keepFile, onCheckedChange = { keepFile = it })
                        Text(stringResource(AppR.string.down_keep_file), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    if (!running) TextButton(onClick = { cancel() }) { Text(stringResource(AppR.string.cancel)) }
                    TextButton(
                        onClick = {
                            if (running) cancelDelete() else {
                                if (askKeep) this@DialogDeleteBook.withData = !keepFile
                                startDelete()
                            }
                        },
                        enabled = mainEnabled,
                        colors = if (running) androidx.compose.material3.ButtonDefaults.textButtonColors()
                        else androidx.compose.material3.ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    ) { Text(stringResource(if (askKeep && !running) AppR.string.delete else mainRes)) }
                }
            }
            show()
        }
    }

    private fun startDelete() {
        if (deleting) return
        deleting = true
        val list = deleteItems
        val listener = if (list != null && !list.isEmpty()) deleteListener else null
        if (list != null && !list.isEmpty() && listener != null) {
            listener.onCancelled()
            val size = list.size
            countText = MainUtil.countLabel(0, size)
            progressMax = size
            progress = 0f
            setCanceledOnTouchOutside(false)
            running = true
            mainRes = AppR.string.cancel
            deleteTask?.cancelled = true
            deleteTask = null
            val task = DialogTask(this)
            deleteTask = task
            task.start(appContext)
        }
        deleting = false
    }

    /** Cancel: stops the delete ("Canceling"), or closes. */
    fun cancelDelete() {
        if (deleteTask == null) {
            dismiss()
            return
        }
        mainEnabled = false
        mainRes = AppR.string.canceling
        deleteTask?.cancelled = true
        deleteTask = null
    }

    fun applyIconSize() {
        val myRoundImage = this.iconImage
        if (myRoundImage != null) {
            val listType = this.listType
            if (listType == 17 || listType == 18 || listType == 19 || listType == 20 || listType == 21 || listType == 22 || listType == 23 || listType == 27 || listType == 28 || listType == 29 || listType == 34 || listType == 35) {
                myRoundImage.setIconSmall(true)
                return
            }
            val list: MutableList<Any?>? = if (listType != 31) null else this.deleteItems
            val childItem: ChildItem? = if (listType != 31 || list == null || list.isEmpty()) null else (this.deleteItems!!.get(0) as ChildItem?)
            if (listType != 31 || list == null || list.isEmpty() || childItem == null || childItem.listType != 8) {
                return
            }
            this.iconImage!!.setIconSmall(true)
        }
    }

    override fun cancel() {
        cancelDelete()
    }

    override fun canCancelFromScrim(): Boolean = deleteTask == null

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        deleteTask?.cancelled = true
        deleteTask = null
        listLoader?.release()
        listLoader = null
        iconImage?.release()
        iconImage = null
        appContext = null
        deleteListener = null
        deleteItems = null
        headText = null
        webClean = null
        super.dismiss()
    }

    companion object {
    }
}
