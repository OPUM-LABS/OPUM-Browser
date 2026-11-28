package com.mycompany.app.data.sync

/**
 * One folder or bookmark of the shared bookmark file (floccus XBEL, see [Xbel]). [id] is the file's own id (a number
 * as text), [url] is null for a folder, [tags] are kept as floccus writes them so OPUM never drops them. The root of
 * the file is a [SyncNode] with id [ROOT_ID] that is not written itself.
 */
class SyncNode(
    var id: String,
    var title: String,
    var url: String?,
    val tags: List<String> = emptyList(),
    val children: MutableList<SyncNode> = ArrayList(),
) {
    val isFolder: Boolean get() = url == null

    fun copyTree(): SyncNode = SyncNode(id, title, url, tags, children.mapTo(ArrayList()) { it.copyTree() })

    /** Every node below this one (not this one), parents before their children. */
    fun descendants(): List<SyncNode> {
        val out = ArrayList<SyncNode>()
        fun walk(n: SyncNode) { for (c in n.children) { out.add(c); walk(c) } }
        walk(this)
        return out
    }

    companion object {
        const val ROOT_ID = ""
        fun root(): SyncNode = SyncNode(ROOT_ID, "", null)
    }
}

/** A parsed bookmark file: its tree and the highest id the file says was given out. */
class SyncFile(val root: SyncNode, val highestId: Long, val hadMissingIds: Boolean = false)
