package com.mycompany.app.list

import android.content.Context
import com.mycompany.app.pref.store.PathSettings
import android.database.Cursor
import android.net.Uri
import android.provider.DocumentsContract
import android.text.TextUtils
import androidx.documentfile.provider.DocumentFile
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.data.DataAlbum
import com.mycompany.app.data.DataCmp
import com.mycompany.app.data.DataPdf
import com.mycompany.app.data.DataUtil
import com.mycompany.app.main.MainUri
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.pref.PrefPath
import java.lang.ref.WeakReference

@Suppress("deprecation")
class ListScan(@JvmField var context: Context?, mode: Int, @JvmField var allTypes: Boolean, @JvmField var listener: ListScanListener) {
    var mode: Byte = 0
    @JvmField
    var scanTask: ScanTask? = null

    interface ListScanListener {
        fun onScanned()

        fun onScanFailed()
    }

    class ScanTask(listScan: ListScan?) : MyAsyncTask() {
        @JvmField
        var listRef: WeakReference<*>
        @JvmField
        var isDone: Boolean = false

        init {
            this.listRef = WeakReference<Any?>(listScan)
        }

        public override fun doInBackground() {
            this.isDone = true
            val scan = this.listRef.get() as ListScan?
            if (scan == null || this.cancelled) {
                return
            }
            val mode = scan.mode
            val context = scan.context
            val tree =
                if (mode.toInt() == 1) PathSettings.scanAlbum.value else if (mode.toInt() == 2) PathSettings.scanPdf.value else if (mode.toInt() == 3) PathSettings.scanZip.value else null
            if (TextUtils.isEmpty(tree)) {
                return
            }
            var cursor: Cursor? = null
            try {
                val treeUri = Uri.parse(tree)
                cursor = context!!.getContentResolver().query(
                    DocumentsContract.buildChildDocumentsUriUsingTree(
                        treeUri,
                        DocumentsContract.getTreeDocumentId(treeUri)
                    ),
                    arrayOf<String?>("document_id", "_display_name", "last_modified", "_size"),
                    null,
                    null,
                    null
                )
                if (cursor != null && cursor.moveToFirst()) {
                    val idCol = cursor.getColumnIndex("document_id")
                    val nameCol = cursor.getColumnIndex("_display_name")
                    val timeCol = cursor.getColumnIndex("last_modified")
                    val sizeCol = cursor.getColumnIndex("_size")
                    // The Downloads provider lists entries whose file is gone; check those.
                    val downloads =
                        tree == "content://com.android.providers.downloads.documents/tree/downloads/document/downloads"
                    val albumSize = DataAlbum.instance(context).pathCount()
                    val pdfSize = DataPdf.instance(context).pathCount()
                    val cmpSize = DataCmp.instance(context).pathCount()
                    var albumSeen = 0
                    var pdfSeen = 0
                    var cmpSeen = 0
                    var dirLoaded = false
                    var dirName: String? = null
                    do {
                        if (this.cancelled) {
                            break
                        }
                        val name = cursor.getString(nameCol)
                        if (TextUtils.isEmpty(name)) {
                            continue
                        }
                        val type = DataUtil.fileKind(name)
                        val wanted =
                            if (scan.allTypes) (type == 1 || type == 2 || type == 3) else type == mode.toInt()
                        if (!wanted) {
                            continue
                        }
                        val size = cursor.getLong(sizeCol)
                        if (size == 0L) {
                            continue
                        }
                        val docUri = DocumentsContract.buildDocumentUriUsingTree(
                            treeUri,
                            cursor.getString(idCol)
                        )
                        if (docUri == null) {
                            continue
                        }
                        val uri = docUri.toString()
                        if (TextUtils.isEmpty(uri)) {
                            continue
                        }
                        // Skip entries already in the list (only checked while fewer matches than its size were found).
                        if (mode.toInt() == 1 && albumSeen < albumSize) {
                            if (DataAlbum.instance(context).hasPath(uri)) {
                                albumSeen++
                                continue
                            }
                        } else if (mode.toInt() == 2 && pdfSeen < pdfSize) {
                            if (DataPdf.instance(context).hasPath(uri)) {
                                pdfSeen++
                                continue
                            }
                        } else if (mode.toInt() == 3 && cmpSeen < cmpSize) {
                            if (DataCmp.instance(context).hasPath(uri)) {
                                cmpSeen++
                                continue
                            }
                        }
                        if (downloads) {
                            var exists = false
                            try {
                                exists = DocumentFile.fromSingleUri(context, docUri)!!.isFile()
                            } catch (unused: Exception) {
                            }
                            if (!exists) {
                                continue
                            }
                        }
                        if (!dirLoaded) {
                            dirName = MainUri.treeDisplayPath(context, tree)
                            dirLoaded = true
                        }
                        val item = UriItem()
                        item.type = type
                        item.dirPath = tree
                        item.dirName = dirName
                        item.uriString = uri
                        item.name = name
                        item.time = cursor.getLong(timeCol)
                        item.size = size
                        if (mode.toInt() == 1) {
                            DataAlbum.instance(context).addUri(item)
                        } else if (mode.toInt() == 2) {
                            DataPdf.instance(context).addUri(item)
                        } else if (mode.toInt() == 3) {
                            DataCmp.instance(context).addUri(item)
                        }
                    } while (cursor.moveToNext())
                }
            } catch (unused: Exception) {
            }
            if (cursor != null) {
                cursor.close()
            }
        }

        public override fun onCancelled() {
            val listScan = this.listRef.get() as ListScan?
            if (listScan != null) {
                listScan.scanTask = null
                if (this.isDone) {
                    listScan.listener.onScanFailed()
                }
            }
        }

        public override fun onPostExecute() {
            val listScan = this.listRef.get() as ListScan?
            if (listScan == null) {
                return
            }
            listScan.scanTask = null
            listScan.listener.onScanned()
        }
    }

    init {
        this.mode = (mode).toByte()
    }
}
