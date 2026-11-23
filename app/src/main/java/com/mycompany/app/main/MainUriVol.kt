package com.mycompany.app.main

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import com.mycompany.app.compress.Compress.Companion.isSubtitleExt
import com.mycompany.app.main.MainUri.NumItem
import com.mycompany.app.main.MainUri.UriItem

@Suppress("deprecation")
object MainUriVol {
    /**
     * Creates a directory entry named after `num` (adding a counter on clashes, or a timestamp
     * after 20 tries) and returns its name, or null.
     */
    @JvmStatic
    fun insertNumbered(context: Context?, uri: Uri?, values: ContentValues?, num: NumItem?): String? {
        if (context == null || uri == null || num == null) {
            return null
        }
        try {
            val name =
                if (num.digits > 20) num.base + "_" + System.currentTimeMillis() + num.suffix else num.prefix + num.counter + num.ext
            var exists = false
            var cursor: Cursor? = null
            try {
                cursor = context.getContentResolver().query(
                    uri, null, "_display_name=? AND mime_type=?",
                    arrayOf<String?>(name, "vnd.android.document/directory"), null
                )
                exists = cursor != null && cursor.moveToFirst()
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                try {
                    cursor.close()
                } catch (unused: Exception) {
                }
            }
            if (exists) {
                return name
            }
            values!!.put("_display_name", name)
            val created = context.getContentResolver().insert(uri, values)
            if (created != null) {
                return MainUri.treeDisplayPath(context, created.toString())
            }
            if (num.digits > 20) {
                return null
            }
            num.counter++
            num.digits++
            return insertNumbered(context, uri, values, num)
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun findMediaUri(context: Context?, dir: String?, sub: String?): String? {
        var dir = dir
        var contentValues: ContentValues?
        var cursorQuery: Cursor?
        var uri: Uri? = null
        if (context != null && !TextUtils.isEmpty(sub)) {
            try {
                if (TextUtils.isEmpty(dir)) {
                    dir = "external_primary"
                }
                var contentUri: Uri? = MediaStore.Downloads.getContentUri(dir!!)
                if (contentUri != null) {
                    var found = false
                    try {
                        cursorQuery = context.getContentResolver().query(
                            contentUri,
                            null,
                            "_display_name=? AND mime_type=?",
                            arrayOf<String?>(sub!!, "vnd.android.document/directory"),
                            null
                        )
                        if (cursorQuery != null) {
                            try {
                                if (cursorQuery.moveToFirst()) {
                                    found = true
                                }
                            } catch (unused: Exception) {
                            }
                        }
                    } catch (unused2: Exception) {
                        cursorQuery = null
                    }
                    if (cursorQuery != null) {
                        try {
                            cursorQuery.close()
                        } catch (unused3: Exception) {
                            contentValues = null
                            uri = contentUri
                            contentUri = uri
                            return MainUriVol.insertNumbered(context, contentUri, contentValues, MainUri.splitNumber(sub))
                        }
                    }
                    if (found) {
                        return sub
                    }
                    contentValues = ContentValues()
                    try {
                        contentValues.put("_display_name", sub)
                        contentValues.put("mime_type", "vnd.android.document/directory")
                        val uriInsert =
                            context.getContentResolver().insert(contentUri, contentValues)
                        if (uriInsert != null) {
                            return MainUri.treeDisplayPath(context, uriInsert.toString())
                        }
                    } catch (unused4: Exception) {
                        uri = contentUri
                        contentUri = uri
                    }
                    return insertNumbered(context, contentUri, contentValues, MainUri.splitNumber(sub))
                }
            } catch (unused5: Exception) {
                contentValues = null
            }
        }
        return null
    }

    @JvmStatic
    fun insertWith(
        context: Context?,
        uri: Uri?,
        contentValues: ContentValues?,
        volume: String?,
        dir: String?,
        numItem: NumItem?
    ): UriItem? {
        val uriInsert: Uri?
        if (context == null || uri == null || numItem == null) {
            return null
        }
        try {
            if (numItem.digits > 20) {
                contentValues!!.put(
                    "_display_name",
                    numItem.base + "_" + System.currentTimeMillis() + numItem.suffix
                )
                uriInsert = context.getContentResolver().insert(uri, contentValues)
            } else {
                contentValues!!.put("_display_name", numItem.prefix + numItem.counter + numItem.ext)
                uriInsert = context.getContentResolver().insert(uri, contentValues)
            }
            if (uriInsert != null) {
                return createUriItem(context, uriInsert, volume, dir, true)
            }
        } catch (unused: Exception) {
        }
        val digits = numItem.digits
        if (digits > 20) {
            return null
        }
        numItem.counter++
        numItem.digits = digits + 1
        return insertWith(context, uri, contentValues, volume, dir, numItem)
    }

    /**
     * Creates file `name` in the Downloads collection of volume `volume` (Android 10+),
     * optionally inside sub folder `dir`. If the name is taken, falls back to a numbered name.
     */
    @JvmStatic
    fun createInVolume(context: Context?, volume: String?, dir: String?, name: String?): UriItem? {
        var volume = volume
        if (context == null || TextUtils.isEmpty(name)) {
            return null
        }
        if (TextUtils.isEmpty(volume)) {
            volume = "external_primary"
        }
        val collection: Uri?
        try {
            collection = MediaStore.Downloads.getContentUri(volume!!)
        } catch (unused: Exception) {
            return null
        }
        val values = ContentValues()
        try {
            val ext = MainUtil.extOf(name)
            var mime = MainUtil.mimeForExt(ext)
            if (TextUtils.isEmpty(mime) || isSubtitleExt(ext)) {
                mime = "application/octet-stream"
            }
            if (!TextUtils.isEmpty(dir)) {
                values.put("relative_path", Environment.DIRECTORY_DOWNLOADS + "/" + dir)
            }
            values.put("_display_name", name)
            values.put("mime_type", mime)
            val created = context.getContentResolver().insert(collection, values)
            if (created != null) {
                return createUriItem(context, created, volume, dir, true)
            }
        } catch (unused: Exception) {
        }
        try {
            return insertWith(context, collection, values, volume, dir, MainUri.splitNumber(name))
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun createUriItem(context: Context?, uri: Uri?, dirPath: String?, dirName: String?, isDir: Boolean): UriItem? {
        if (context == null || uri == null) {
            return null
        }
        try {
            val uriItem = UriItem()
            uriItem.uri = uri
            uriItem.uriString = uri.toString()
            if (!TextUtils.isEmpty(dirPath)) {
                if (TextUtils.isEmpty(dirName)) {
                    uriItem.dirPath = dirPath + "/" + Environment.DIRECTORY_DOWNLOADS
                    uriItem.dirName = Environment.DIRECTORY_DOWNLOADS
                } else {
                    uriItem.dirPath = dirPath + "/" + Environment.DIRECTORY_DOWNLOADS + "/" + dirName
                    uriItem.dirName = dirName
                }
            }
            val documentFileB = DocumentFile.fromSingleUri(context, uri)
            uriItem.name = documentFileB!!.getName()
            if (isDir) {
                uriItem.time = System.currentTimeMillis()
                return uriItem
            }
            uriItem.time = documentFileB.lastModified()
            val fileSize = documentFileB.length()
            uriItem.size = fileSize
            if (fileSize == 0L) {
                uriItem.size = MainUri.fileSize(context, uriItem.uriString)
            }
            return uriItem
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun createNumbered(context: Context?, uri: Uri?, numItem: NumItem?, mime: String?): UriItem? {
        var mime = mime
        if (context == null || uri == null || numItem == null) {
            return null
        }
        try {
            val contentValues = ContentValues()
            if (numItem.digits > 20) {
                contentValues.put(
                    "_display_name",
                    numItem.base + "_" + System.currentTimeMillis() + numItem.suffix
                )
            } else {
                contentValues.put("_display_name", numItem.prefix + numItem.counter + numItem.ext)
            }
            if (TextUtils.isEmpty(mime)) {
                mime = "application/octet-stream"
            }
            contentValues.put("mime_type", mime)
            if (context.getContentResolver().update(uri, contentValues, null, null) > 0) {
                return createUriItem(context, uri, null, null, false)
            }
        } catch (unused: Exception) {
        }
        val digits = numItem.digits
        if (digits > 20) {
            return null
        }
        numItem.counter++
        numItem.digits = digits + 1
        return createNumbered(context, uri, numItem, mime)
    }
}
