package com.mycompany.app.compress

import android.content.Context
import com.mycompany.app.data.DataAlbum
import com.mycompany.app.main.MainItem.ChildItem

@Suppress("deprecation")
class CompressUtilAlbum(context: Context?, path: String?, referer: String?) : CompressUtilZip(context, path, referer) {
    override fun albumCount(): Int {
        val context = this.context
        if (context == null) {
            return 0
        }
        return DataAlbum.instance(context).pathCount()
    }

    override fun albumIndexOf(name: String?): Int {
        val context = this.context
        if (context == null) {
            return 0
        }
        return DataAlbum.instance(context).indexOfPath(name)
    }

    override fun albumItemAt(index: Int): ChildItem? {
        val context = this.context
        if (context == null) {
            return null
        }
        return DataAlbum.instance(context).itemAt(index)
    }

    override fun albumItems(): MutableList<ChildItem?>? {
        val context = this.context
        if (context == null) {
            return null
        }
        return DataAlbum.instance(context).items
    }

    override fun archiveType(): Int {
        return 3
    }
}
