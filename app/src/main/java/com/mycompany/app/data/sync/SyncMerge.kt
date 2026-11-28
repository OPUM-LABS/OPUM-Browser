package com.mycompany.app.data.sync

import org.json.JSONArray
import org.json.JSONObject

/** One bookmark or folder of the phone, as the merge sees it. [parentLocalId] 0 is the top level. */
class LocalItem(
    val localId: Long,
    val isFolder: Boolean,
    val title: String,
    val url: String?,
    val parentLocalId: Long,
    /** When the row last changed on the phone (ms; 0 when unknown). */
    val mtime: Long,
)

/** An item as it was after the last sync: its file id, its phone row, where it was and what it was called. */
class SnapItem(
    val serverId: String,
    val localId: Long,
    val parent: String,
    val title: String,
    val localTitle: String,
    val url: String?,
)

/** The state after the last sync, kept on the phone: every item, and the order of each folder's children. */
class Snapshot(val items: Map<String, SnapItem>, val order: Map<String, List<String>>) {
    fun toJson(): JSONObject {
        val items = JSONArray()
        for (s in this.items.values) {
            val o = JSONObject().put("i", s.serverId).put("l", s.localId).put("p", s.parent).put("t", s.title)
            if (s.localTitle != s.title) o.put("lt", s.localTitle)
            if (s.url != null) o.put("u", s.url)
            items.put(o)
        }
        val order = JSONObject()
        for ((k, v) in this.order) order.put(k, JSONArray(v))
        return JSONObject().put("items", items).put("order", order)
    }

    companion object {
        fun fromJson(o: JSONObject?): Snapshot? {
            if (o == null) return null
            return try {
                val items = HashMap<String, SnapItem>()
                val a = o.getJSONArray("items")
                for (i in 0 until a.length()) {
                    val x = a.getJSONObject(i)
                    val title = x.optString("t", "")
                    val s = SnapItem(x.getString("i"), x.optLong("l", 0), x.optString("p", ""), title,
                        if (x.has("lt")) x.getString("lt") else title, if (x.has("u")) x.getString("u") else null)
                    items[s.serverId] = s
                }
                val order = HashMap<String, List<String>>()
                val ord = o.optJSONObject("order")
                if (ord != null) for (k in ord.keys()) {
                    val arr = ord.getJSONArray(k)
                    order[k] = (0 until arr.length()).map { arr.getString(it) }
                }
                Snapshot(items, order)
            } catch (_: Exception) {
                null
            }
        }
    }
}

/** One row the phone should have after the sync. [localId] 0 is a new row; [order] is its place among its siblings. */
class PlannedRow(
    val localId: Long,
    val serverId: String,
    val isFolder: Boolean,
    val localTitle: String,
    val serverTitle: String,
    val url: String?,
    val parentServerId: String,
    val order: Int,
)

class MergeResult(
    /** The merged tree, as the file should be now. */
    val tree: SyncNode,
    val highestId: Long,
    /** The file has to be written (something changed on the phone, or the file is new). */
    val upload: Boolean,
    /** Every row the phone should have, parents before children. */
    val rows: List<PlannedRow>,
    /** Phone rows to delete. */
    val deletes: List<Long>,
    /** Counts for the result line: added / changed / removed on the phone, and whether the file changed. */
    val phoneAdded: Int,
    val phoneChanged: Int,
    val phoneRemoved: Int,
) {
    /** The state to keep after the phone has been changed; [newLocalIds] are the ids the new rows got. */
    fun snapshot(newLocalIds: Map<String, Long>): Snapshot {
        val items = HashMap<String, SnapItem>()
        for (r in rows) {
            val id = if (r.localId > 0) r.localId else newLocalIds[r.serverId] ?: 0L
            items[r.serverId] = SnapItem(r.serverId, id, r.parentServerId, r.serverTitle, r.localTitle, r.url)
        }
        val order = HashMap<String, List<String>>()
        fun walk(n: SyncNode) {
            if (n.isFolder) {
                order[n.id] = n.children.map { it.id }
                for (c in n.children) walk(c)
            }
        }
        walk(tree)
        return Snapshot(items, order)
    }
}

/**
 * The three-way merge of the bookmark sync (2026-11-28): the file as it is now, the phone's bookmarks as they are now,
 * and the state both had after the last sync ([Snapshot]). What changed on one side is taken over by the other; when
 * the same item changed on both, the newer change wins (the phone knows when each row changed; for the file, the time
 * it was last written stands in, as floccus keeps no times per item). Nothing is lost on a tie: an item deleted on one
 * side and changed on the other stays, a folder deleted on one side keeps what was newly added to it on the other.
 *
 * Without a snapshot (the first sync, or after a restore) both sides are joined: equal items are paired (a folder by its
 * name, a bookmark by its address, within the same folder), the rest is added to the other side.
 *
 * The phone shows folders before bookmarks and needs folder names that are unique in a folder and free of "/" (folders
 * are found by their path): the file keeps its own order and names, the phone gets "Name (2)" and "Name ∕ x" where
 * needed. A change of order on the phone is taken over by keeping the file's mix of folders and bookmarks and
 * reordering within each kind.
 */
object SyncMerge {

    fun merge(
        local: List<LocalItem>,
        server: SyncFile?,
        snap: Snapshot?,
        /** When the file was last written (ms); Long.MAX_VALUE when the server does not say. */
        serverTime: Long,
        /** When the phone last deleted a bookmark (ms). */
        localDeleteTime: Long,
    ): MergeResult {
        val snapItems = snap?.items ?: emptyMap()
        val serverRoot = server?.root ?: SyncNode.root()
        var highest = server?.highestId ?: 0L
        val m = serverRoot.copyTree()

        // the file as downloaded: parent of each id
        val rParent = HashMap<String, String>()
        val rNode = HashMap<String, SyncNode>()
        fun indexR(n: SyncNode) { for (c in n.children) { rParent[c.id] = n.id; rNode[c.id] = c; indexR(c) } }
        indexR(serverRoot)
        val rChildren = HashMap<String, List<String>>()
        rChildren[SyncNode.ROOT_ID] = serverRoot.children.map { it.id }
        for ((id, n) in rNode) if (n.isFolder) rChildren[id] = n.children.map { it.id }
        val serverChanged = HashSet<String>()
        for ((id, node) in rNode) {
            val s = snapItems[id] ?: continue
            if (node.title != s.title || node.url != s.url || rParent[id] != s.parent) serverChanged.add(id)
        }

        val localById = local.associateBy { it.localId }
        val localChildren = local.groupBy { it.parentLocalId }
        val serverIdOfLocal = HashMap<Long, String>()
        for (s in snapItems.values) if (s.localId > 0) serverIdOfLocal[s.localId] = s.serverId

        // 1. deleted on the phone: removed from the file, unless the file changed it later (folders that keep a
        //    newly added item stay, with only what they keep)
        val deleted = HashSet<String>()
        for (s in snapItems.values) {
            if (s.localId <= 0 || localById.containsKey(s.localId)) continue
            if (s.serverId in serverChanged && serverTime > localDeleteTime) continue
            deleted.add(s.serverId)
        }

        val mById = HashMap<String, SyncNode>()
        val mParent = HashMap<String, String>()
        mById[SyncNode.ROOT_ID] = m
        fun indexM(n: SyncNode) { for (c in n.children) { mById[c.id] = c; mParent[c.id] = n.id; indexM(c) } }
        indexM(m)

        val localOf = HashMap<String, Long>()
        val claimed = HashSet<String>()
        fun isDescendant(node: SyncNode, of: SyncNode): Boolean {
            if (node === of) return true
            return of.children.any { isDescendant(node, it) }
        }
        fun move(node: SyncNode, to: SyncNode) {
            if (isDescendant(to, node)) return
            mById[mParent[node.id]]?.children?.remove(node)
            to.children.add(node)
            mParent[node.id] = to.id
        }
        fun newNode(item: LocalItem, parent: SyncNode): SyncNode {
            val n = SyncNode((++highest).toString(), item.title, item.url)
            parent.children.add(n)
            mById[n.id] = n
            mParent[n.id] = parent.id
            return n
        }
        // an item the file deleted stays when the phone changed it later, or (a folder) holds such an item
        fun changedSince(item: LocalItem, s: SnapItem): Boolean =
            item.title != s.localTitle || item.url != s.url
        fun keepsAlive(item: LocalItem): Boolean {
            val sid = serverIdOfLocal[item.localId]
            val s = sid?.let { snapItems[it] }
            if (s == null) return true // added on the phone
            if (rNode.containsKey(s.serverId)) {
                // still in the file: only its children matter
            } else if (changedSince(item, s) && item.mtime > serverTime) {
                return true
            }
            return item.isFolder && localChildren[item.localId].orEmpty().any { keepsAlive(it) }
        }

        // 2. the phone's tree, top down
        fun place(item: LocalItem, parent: SyncNode) {
            val sid = serverIdOfLocal[item.localId]
            val s = sid?.let { snapItems[it] }
            val existing = sid?.let { mById[it] }?.takeIf { it.id !in claimed }
            val node: SyncNode = when {
                existing != null && s != null -> {
                    val serverTitle = existing.title != s.title
                    val serverUrl = existing.url != s.url
                    val localNewer = item.mtime > serverTime
                    if (item.title != s.localTitle && (!serverTitle || localNewer)) existing.title = item.title
                    if (!item.isFolder && item.url != s.url && (!serverUrl || localNewer)) existing.url = item.url
                    val localMoved = parent.id != s.parent
                    val serverMoved = rParent[s.serverId] != s.parent
                    if (localMoved && mParent[existing.id] != parent.id && (!serverMoved || localNewer)) move(existing, parent)
                    existing
                }
                // the file deleted it: it stays only when the phone changed it later (or it holds such an item)
                s != null && !rNode.containsKey(s.serverId) -> if (keepsAlive(item)) newNode(item, parent) else return
                // new on the phone: an equal new item of the file is the same one
                else -> parent.children.firstOrNull { c ->
                    c.id !in claimed && c.id !in snapItems && c.isFolder == item.isFolder &&
                        (if (item.isFolder) c.title == item.title || phoneName(c.title) == item.title else c.url == item.url)
                } ?: newNode(item, parent)
            }
            claimed.add(node.id)
            localOf[node.id] = item.localId
            if (item.isFolder) {
                val kids = localChildren[item.localId].orEmpty()
                for (k in kids) place(k, node)
                reorder(node, kids, snap, rChildren, localOf)
            }
        }
        val top = localChildren[0L].orEmpty()
        for (item in top) place(item, m)
        reorder(m, top, snap, rChildren, localOf)
        // the phone's deletions are applied last, after moves out of a deleted folder were taken over
        fun prune(n: SyncNode) {
            val it = n.children.iterator()
            while (it.hasNext()) {
                val c = it.next()
                prune(c)
                if (c.id in deleted && (!c.isFolder || c.children.isEmpty())) it.remove()
            }
        }
        prune(m)

        // 3. what the phone should have
        val rows = ArrayList<PlannedRow>()
        fun plan(n: SyncNode) {
            val used = HashSet<String>()
            var folderPos = 0
            var bookPos = 0
            for (c in n.children) {
                val localTitle = if (c.isFolder) {
                    val base = phoneName(c.title)
                    var name = base
                    var k = 2
                    while (!used.add(name.lowercase())) name = "$base (${k++})"
                    name
                } else c.title
                rows.add(PlannedRow(localOf[c.id] ?: 0L, c.id, c.isFolder, localTitle, c.title, c.url, n.id,
                    if (c.isFolder) folderPos++ else bookPos++))
                if (c.isFolder) plan(c)
            }
        }
        plan(m)
        val kept = rows.mapNotNull { r -> r.localId.takeIf { it > 0 } }.toHashSet()
        val deletes = local.map { it.localId }.filter { it !in kept }

        var added = 0
        var changed = 0
        val planById = rows.associateBy { it.serverId }
        for (r in rows) {
            if (r.localId <= 0) { added++; continue }
            val l = localById[r.localId] ?: continue
            val parentLocal = if (r.parentServerId == SyncNode.ROOT_ID) 0L else planById[r.parentServerId]?.localId ?: -1L
            if (l.title != r.localTitle || l.url != r.url || l.parentLocalId != parentLocal) changed++
        }

        val upload = server == null || server.hadMissingIds ||
            Xbel.write(m, highest) != Xbel.write(serverRoot, server.highestId)
        return MergeResult(m, highest, upload, rows, deletes, added, changed, deletes.size)
    }

    /** A folder name as the phone can hold it: no "/" (folders are found by their path), never empty. */
    fun phoneName(title: String): String = title.replace('/', '\u2215').trim().ifEmpty { "\u2026" }

    /**
     * A new order of [folder]'s children on the phone is taken over when the file kept the old one: the positions of
     * folders and bookmarks in the file stay, the items of each kind take the phone's order.
     */
    private fun reorder(folder: SyncNode, localKids: List<LocalItem>, snap: Snapshot?, rChildren: Map<String, List<String>>, localOf: Map<String, Long>) {
        val before = snap?.order?.get(folder.id) ?: return
        val idOfLocal = HashMap<Long, String>()
        for ((sid, lid) in localOf) idOfLocal[lid] = sid
        val serverNow = rChildren[folder.id]
        for (kindFolder in listOf(true, false)) {
            val inBefore = before.toHashSet()
            val localSeq = localKids.filter { it.isFolder == kindFolder }.mapNotNull { idOfLocal[it.localId] }.filter { it in inBefore }
            if (localSeq.size < 2) continue
            val set = localSeq.toHashSet()
            val snapSeq = before.filter { it in set }
            if (snapSeq == localSeq) continue
            // the file reordered the same folder: its order stays
            if (serverNow != null && serverNow.filter { it in set } != snapSeq) continue
            val slots = folder.children.withIndex().filter { it.value.id in set }.map { it.index }
            val byId = folder.children.associateBy { it.id }
            val seq = localSeq.filter { byId.containsKey(it) }
            if (seq.size != slots.size) continue
            for ((i, slot) in slots.withIndex()) folder.children[slot] = byId[seq[i]]!!
        }
    }
}
