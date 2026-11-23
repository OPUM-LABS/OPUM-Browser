package com.mycompany.app.data

import android.content.ContentValues
import com.mycompany.app.pref.store.PathSettings
import android.content.Context
import android.text.TextUtils
import com.mycompany.app.compress.Compress
import com.mycompany.app.db.DbAlbum
import com.mycompany.app.db.DbCmp
import com.mycompany.app.db.DbPdf
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookAlbum
import com.mycompany.app.db.book.DbBookCmp
import com.mycompany.app.db.book.DbBookPdf
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefSet

@Suppress("deprecation")
object DataUtil {
    @JvmStatic
    fun addUri(context: Context?, newKind: Int, uriItem: UriItem?) {
        if (newKind != 0) {
            if (newKind == 1) {
                DataAlbum.Companion.instance(context).addItem(context, uriItem)
                DbAlbum.addItem(context, uriItem)
            } else if (newKind == 2) {
                DataPdf.Companion.instance(context).addItem(context, uriItem)
                DbPdf.addItem(context, uriItem)
            } else if (newKind == 3) {
                DataCmp.Companion.instance(context).addItem(context, uriItem)
                DbCmp.addItem(context, uriItem)
            }
        }
    }

    @JvmStatic
    fun insertUri(context: Context?, uriItem: UriItem) {
        val type = uriItem.type
        if (type == 1) {
            DbAlbum.addItem(context, uriItem)
        } else if (type == 2) {
            DbPdf.addItem(context, uriItem)
        } else if (type == 3) {
            DbCmp.addItem(context, uriItem)
        }
    }

    @JvmStatic
    fun removePath(context: Context?, listType: Int, path: String?) {
        if (listType == 1) {
            DataAlbum.Companion.instance(context).removePath(path)
            DbAlbum.removePath(context, path)
            val dbBookAlbum = DbBookAlbum.cached
            if (context == null || TextUtils.isEmpty(path)) {
                return
            }
            DbUtil.deleteWhere(
                DbBookAlbum.get(context).getWritableDatabase(),
                "DbBookAlbum_table",
                "_path=?",
                arrayOf<String?>(path!!)
            )
            return
        }
        if (listType == 2) {
            DataPdf.Companion.instance(context).removePath(path)
            DbPdf.removePath(context, path)
            val dbBookPdf = DbBookPdf.cached
            if (context == null || TextUtils.isEmpty(path)) {
                return
            }
            DbUtil.deleteWhere(
                DbBookPdf.get(context).getWritableDatabase(),
                "DbBookPdf_table",
                "_path=?",
                arrayOf<String?>(path!!)
            )
            return
        }
        if (listType == 3) {
            DataCmp.Companion.instance(context).removePath(path)
            DbCmp.removePath(context, path)
            val dbBookCmp = DbBookCmp.cached
            if (context == null || TextUtils.isEmpty(path)) {
                return
            }
            DbUtil.deleteWhere(
                DbBookCmp.get(context).getWritableDatabase(),
                "DbBookCmp_table",
                "_path=?",
                arrayOf<String?>(path!!)
            )
        }
    }

    @JvmStatic
    fun fileKind(name: String?): Int {
        val lowerExt = MainUtil.lowerExtOf(name)
        if (lowerExt == null) {
            return 0
        }
        if (Compress.isAlbumExt(lowerExt)) {
            return 1
        }
        if (Compress.isPdfExt(lowerExt)) {
            return 2
        }
        return if (Compress.isArchiveExt(lowerExt)) 3 else 0
    }

    @JvmStatic
    fun saveFile(context: Context?, oldKind: Int, newKind: Int, url: String?, newPath: String?, uriItem: UriItem) {
        var oldDir: String?
        if (TextUtils.isEmpty(url)) {
            return
        }
        if (oldKind == 1) {
            oldDir = PathSettings.albumPath.value
        } else if (oldKind == 2) {
            oldDir = PathSettings.pdfPath.value
        } else {
            oldDir = if (oldKind == 3) PathSettings.cmpPath.value else null
        }
        if (!TextUtils.isEmpty(oldDir)) {
            var trimmedUrl = url
            while (trimmedUrl!!.endsWith("/")) {
                trimmedUrl = trimmedUrl.let { s -> s.substring(0, s.length - 1) }
            }
            if (!TextUtils.isEmpty(trimmedUrl)) {
                while (oldDir!!.endsWith("/")) {
                    oldDir = oldDir.let { s -> s.substring(0, s.length - 1) }
                }
                if (!TextUtils.isEmpty(oldDir) && trimmedUrl == oldDir) {
                    val path = if (oldKind != newKind) "" else newPath
                    if (oldKind == 1) {
                        PathSettings.edit(context) { set(PathSettings.albumPath, path) }
                    } else if (oldKind == 2) {
                        PathSettings.edit(context) { set(PathSettings.pdfPath, path) }
                    } else if (oldKind == 3) {
                        PathSettings.edit(context) { set(PathSettings.cmpPath, path) }
                    }
                }
            }
        }
        if (oldKind != newKind) {
            removePath(context, oldKind, url)
            addUri(context, newKind, uriItem)
            return
        }
        if (newKind == 1) {
            DataAlbum.Companion.instance(context).updateItem(newKind, url, newPath, uriItem.name)
            val name = uriItem.name
            val dbAlbum = DbAlbum.cached
            if (context != null && !TextUtils.isEmpty(url)) {
                com.mycompany.app.glide.ImageCache.removeUrl(url)
                val contentValues = ContentValues()
                contentValues.put("_path", newPath)
                contentValues.put("_name", name)
                contentValues.put("_icon", MainUtil.cacheFilePathFor(context, newPath))
                DbUtil.updateWhere(
                    DbAlbum.get(context).getWritableDatabase(),
                    "DbAlbum_table",
                    contentValues,
                    "_path=?",
                    arrayOf<String?>(url)
                )
            }
            val nameNow = uriItem.name
            val dbBookAlbum = DbBookAlbum.cached
            if (context == null || TextUtils.isEmpty(url) || TextUtils.isEmpty(newPath)) {
                return
            }
            DbUtil.updateWhere(
                DbBookAlbum.get(context).getWritableDatabase(),
                "DbBookAlbum_table",
                android.content.ContentValues().also { cv -> cv.put("_path", newPath); cv.put("_name", nameNow) },
                "_path=?",
                arrayOf<String?>(url)
            )
            return
        }
        if (newKind == 2) {
            DataPdf.Companion.instance(context).updateItem(newKind, url, newPath, uriItem.name)
            val nameNow = uriItem.name
            val dbPdf = DbPdf.cached
            if (context != null && !TextUtils.isEmpty(url)) {
                com.mycompany.app.glide.ImageCache.removeUrl(url)
                val contentValues = ContentValues()
                contentValues.put("_path", newPath)
                contentValues.put("_name", nameNow)
                contentValues.put("_icon", MainUtil.cacheFilePathFor(context, newPath))
                DbUtil.updateWhere(
                    DbPdf.get(context).getWritableDatabase(),
                    "DbPdf_table",
                    contentValues,
                    "_path=?",
                    arrayOf<String?>(url)
                )
            }
            val name = uriItem.name
            val dbBookPdf = DbBookPdf.cached
            if (context == null || TextUtils.isEmpty(url) || TextUtils.isEmpty(newPath)) {
                return
            }
            DbUtil.updateWhere(
                DbBookPdf.get(context).getWritableDatabase(),
                "DbBookPdf_table",
                android.content.ContentValues().also { cv -> cv.put("_path", newPath); cv.put("_name", name) },
                "_path=?",
                arrayOf<String?>(url)
            )
            return
        }
        if (newKind == 3) {
            DataCmp.Companion.instance(context).updateItem(newKind, url, newPath, uriItem.name)
            val nameNow = uriItem.name
            val dbCmp = DbCmp.cached
            if (context != null && !TextUtils.isEmpty(url)) {
                com.mycompany.app.glide.ImageCache.removeUrl(url)
                val contentValues = ContentValues()
                contentValues.put("_path", newPath)
                contentValues.put("_name", nameNow)
                DbUtil.updateWhere(
                    DbCmp.get(context).getWritableDatabase(),
                    "DbCmp_table",
                    contentValues,
                    "_path=?",
                    arrayOf<String?>(url)
                )
            }
            val name = uriItem.name
            val dbBookCmp = DbBookCmp.cached
            if (context == null || TextUtils.isEmpty(url) || TextUtils.isEmpty(newPath)) {
                return
            }
            DbUtil.updateWhere(
                DbBookCmp.get(context).getWritableDatabase(),
                "DbBookCmp_table",
                android.content.ContentValues().also { cv -> cv.put("_path", newPath); cv.put("_name", name) },
                "_path=?",
                arrayOf<String?>(url)
            )
        }
    }
}
