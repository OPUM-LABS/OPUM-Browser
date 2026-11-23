package com.mycompany.app.main

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import com.mycompany.app.main.MainUri.NumItem
import com.mycompany.app.main.MainUri.UriItem

@Suppress("deprecation")
object MainUriDoc {
    @JvmStatic
    fun createNumbered(context: Context?, uri: Uri?, numItem: NumItem?): String? {
        val uriCreateDocument: Uri?
        if (context == null || uri == null || numItem == null) {
            return null
        }
        try {
            if (numItem.digits > 20) {
                uriCreateDocument = DocumentsContract.createDocument(
                    context.getContentResolver(),
                    uri,
                    "vnd.android.document/directory",
                    numItem.base + "_" + System.currentTimeMillis() + numItem.suffix
                )
            } else {
                uriCreateDocument = DocumentsContract.createDocument(
                    context.getContentResolver(),
                    uri,
                    "vnd.android.document/directory",
                    numItem.prefix + numItem.counter + numItem.ext
                )
            }
            if (uriCreateDocument != null) {
                return uriCreateDocument.toString()
            }
        } catch (unused: Exception) {
        }
        val digits = numItem.digits
        if (digits > 20) {
            return null
        }
        numItem.counter++
        numItem.digits = digits + 1
        return createNumbered(context, uri, numItem)
    }

    @JvmStatic
    fun findChild(context: Context?, input: String?, nameList: String?): String? {
        var uri: Uri? = null
        if (context != null && !TextUtils.isEmpty(input)) {
            try {
                var parse = Uri.parse(input)
                if (parse != null) {
                    try {
                        val uriCreateDocument = DocumentsContract.createDocument(
                            context.getContentResolver(),
                            parse,
                            "vnd.android.document/directory",
                            (nameList as String)
                        )
                        if (uriCreateDocument != null) {
                            return uriCreateDocument.toString()
                        }
                    } catch (unused: Exception) {
                        uri = parse
                        parse = uri
                    }
                    return createNumbered(context, parse, MainUri.splitNumber(nameList))
                }
            } catch (unused2: Exception) {
            }
        }
        return null
    }

    @JvmStatic
    fun createIn(context: Context?, uri: Uri?, dirPath: String?, name: String?, numItem: NumItem?): UriItem? {
        var uri = uri
        if (context == null || uri == null || numItem == null) {
            return null
        }
        try {
            if (numItem.digits > 20) {
                uri = DocumentsContract.createDocument(
                    context.getContentResolver(),
                    uri,
                    name!!,
                    numItem.base + "_" + System.currentTimeMillis() + numItem.suffix
                )
            } else {
                uri = DocumentsContract.createDocument(
                    context.getContentResolver(),
                    uri,
                    name!!,
                    numItem.prefix + numItem.counter + numItem.ext
                )
            }
            if (uri != null) {
                return uriItemFor(context, uri, dirPath, true)
            }
        } catch (unused: Exception) {
        }
        val digits = numItem.digits
        if (digits > 20) {
            return null
        }
        numItem.counter++
        numItem.digits = digits + 1
        return createIn(context, uri, dirPath, name, numItem)
    }

    @JvmStatic
    fun uriItemFor(context: Context?, uri: Uri?, dirPath: String?, isDir: Boolean): UriItem? {
        if (context == null || uri == null) {
            return null
        }
        try {
            val uriItem = UriItem()
            uriItem.uri = uri
            uriItem.uriString = uri.toString()
            if (!TextUtils.isEmpty(dirPath)) {
                uriItem.dirPath = dirPath
                uriItem.dirName = MainUri.treeDisplayPath(context, dirPath)
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
    fun renameNumbered(context: Context?, uri: Uri?, numItem: NumItem?): UriItem? {
        var uri = uri
        if (context == null || uri == null || numItem == null) {
            return null
        }
        try {
            if (numItem.digits > 20) {
                uri = DocumentsContract.renameDocument(
                    context.getContentResolver(),
                    uri,
                    numItem.base + "_" + System.currentTimeMillis() + numItem.suffix
                )
            } else {
                uri = DocumentsContract.renameDocument(
                    context.getContentResolver(),
                    uri,
                    numItem.prefix + numItem.counter + numItem.ext
                )
            }
            if (uri != null) {
                return uriItemFor(context, uri, null, false)
            }
        } catch (unused: Exception) {
        }
        val digits = numItem.digits
        if (digits > 20) {
            return null
        }
        numItem.counter++
        numItem.digits = digits + 1
        return renameNumbered(context, uri, numItem)
    }
}
