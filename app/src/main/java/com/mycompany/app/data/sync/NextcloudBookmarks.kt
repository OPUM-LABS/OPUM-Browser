package com.mycompany.app.data.sync

import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * The Nextcloud Bookmarks app as a sync target (2026-11-28), through its REST API v2
 * (`index.php/apps/bookmarks/public/rest/v2/`), as floccus' "Nextcloud Bookmarks" profile uses it.
 *
 * The server's bookmarks are read as one tree. Folders get the id "f<id>"; a bookmark "b<id>;<folder id>", because one
 * Nextcloud bookmark can sit in several folders and is one entry per folder here (floccus does the same). After the
 * merge, [apply] makes the server match the merged tree with single requests: new folders and bookmarks, renames,
 * moves, removals and the order of each folder's children. A bookmark whose address is already on the server is added
 * to the folder instead of being created twice.
 *
 * The lock is the app's own (`POST /lock`, 423 when another client holds it); servers without it answer 405 and sync
 * without one.
 */
class NextcloudBookmarks(private val config: SyncConfig) {
    private val seen = arrayOfNulls<String>(1)
    private val client = SyncTransport.client(config.pin, seen)
    private val base: HttpUrl = run {
        val u = SyncConfig.withScheme(config.url).let { if (it.contains("index.php")) it.substringBefore("index.php") else it }
        (u.trimEnd('/') + "/index.php/apps/bookmarks/public/rest/v2/").toHttpUrlOrNull() ?: throw SyncError(SyncError.NOT_SET, "address")
    }
    private val auth = Credentials.basic(config.user, config.password, Charsets.UTF_8)
    private val json = "application/json".toMediaType()

    /** When the server says a bookmark last changed (ms), by its id in the tree. */
    val itemTimes = HashMap<String, Long>()

    private class Answer(val code: Int, val body: JSONObject?)

    private fun call(method: String, path: String, body: JSONObject? = null, okCodes: Set<Int> = emptySet()): Answer {
        val url = base.newBuilder().addEncodedPathSegments(path.substringBefore('?')).also { b ->
            val q = path.substringAfter('?', "")
            if (q.isNotEmpty()) b.encodedQuery(q)
        }.build()
        val req = Request.Builder().url(url).header("Authorization", auth).header("Accept", "application/json")
            .method(method, body?.toString()?.toRequestBody(json) ?: if (method == "POST" || method == "PUT" || method == "PATCH") "".toRequestBody(json) else null)
            .build()
        return try {
            client.newCall(req).execute().use { r ->
                val text = try { r.body.string() } catch (_: Exception) { "" }
                val o = try { if (text.isBlank()) null else JSONObject(text) } catch (_: Exception) { null }
                if (!r.isSuccessful && r.code !in okCodes) {
                    throw when (r.code) {
                        401, 403 -> SyncError(SyncError.AUTH, "HTTP ${r.code}")
                        404 -> SyncError(SyncError.FOLDER, "HTTP 404 $path")
                        423 -> SyncError(SyncError.LOCKED, "HTTP 423")
                        else -> SyncError(SyncError.SERVER, "HTTP ${r.code}" + (o?.optString("data")?.takeIf { it.isNotEmpty() }?.let { ": $it" } ?: ""))
                    }
                }
                if (r.isSuccessful && o != null && o.optString("status") == "error") throw SyncError(SyncError.SERVER, o.optString("data"))
                Answer(r.code, o)
            }
        } catch (e: IOException) {
            throw SyncTransport.failure(e, seen)
        }
    }

    fun lock() {
        call("POST", "lock", okCodes = setOf(405))
    }

    fun unlock() {
        try { call("DELETE", "lock", okCodes = setOf(404, 405)) } catch (_: Exception) {}
    }

    /** The id of the folder that holds the synced bookmarks ([SyncConfig.ncFolder]; made when missing), -1 for all. */
    fun rootFolder(): Long {
        val parts = config.ncFolder.split('/').map { it.trim() }.filter { it.isNotEmpty() }
        var id = -1L
        for (name in parts) {
            val list = call("GET", "folder?root=$id&layers=1").body?.optJSONArray("data") ?: JSONArray()
            var found: Long? = null
            for (i in 0 until list.length()) {
                val f = list.getJSONObject(i)
                if (f.optString("title") == name) { found = f.optString("id").toLongOrNull(); break }
            }
            id = found ?: call("POST", "folder", JSONObject().put("title", name).put("parent_folder", id))
                .body?.optJSONObject("item")?.optString("id")?.toLongOrNull() ?: throw SyncError(SyncError.SERVER, "folder $name")
        }
        return id
    }

    /** The server's tree below folder [root]. */
    fun read(root: Long): SyncNode {
        val data = call("GET", "folder/$root/children?layers=-1").body?.optJSONArray("data") ?: JSONArray()
        val top = SyncNode.root()
        fun fill(into: SyncNode, folderId: Long, items: JSONArray) {
            for (i in 0 until items.length()) {
                val o = items.optJSONObject(i) ?: continue
                val id = o.optString("id")
                if (o.optString("type") == "folder") {
                    val f = SyncNode("f$id", o.optString("title"), null)
                    into.children.add(f)
                    fill(f, id.toLongOrNull() ?: continue, o.optJSONArray("children") ?: JSONArray())
                } else {
                    val url = o.optString("url")
                    if (url.isEmpty()) continue
                    val tags = o.optJSONArray("tags")?.let { t -> (0 until t.length()).map { t.optString(it) } } ?: emptyList()
                    val node = SyncNode("b$id;$folderId", o.optString("title"), url, tags)
                    into.children.add(node)
                    o.optLong("lastmodified", 0L).takeIf { it > 0 }?.let { itemTimes[node.id] = it * 1000L }
                }
            }
        }
        fill(top, root, data)
        return top
    }

    private fun folderNum(treeId: String, root: Long): Long = if (treeId == SyncNode.ROOT_ID) root else treeId.removePrefix("f").toLong()

    private fun bookmarkNum(treeId: String): Long = treeId.removePrefix("b").substringBefore(';').toLong()

    private fun bookmarkFolder(treeId: String): Long = treeId.substringAfter(';').toLong()

    /**
     * Makes the server's tree [before] (below [root]) look like [after]. Returns the new ids of the items that got
     * one (new items, and bookmarks that moved to another folder).
     */
    fun apply(before: SyncNode, after: SyncNode, root: Long): Map<String, String> {
        val renames = HashMap<String, String>()
        fun real(id: String) = renames[id] ?: id
        val oldNodes = HashMap<String, SyncNode>()
        val oldParent = HashMap<String, String>()
        fun indexOld(n: SyncNode) { for (c in n.children) { oldNodes[c.id] = c; oldParent[c.id] = n.id; indexOld(c) } }
        indexOld(before)
        val newIds = HashSet<String>()
        fun indexNew(n: SyncNode) { for (c in n.children) { newIds.add(c.id); indexNew(c) } }
        indexNew(after)

        // 1. folders and bookmarks top down: new ones made, changed ones updated, moved ones moved
        fun walk(parent: SyncNode) {
            val parentNum = folderNum(real(parent.id), root)
            for (c in parent.children) {
                val old = oldNodes[c.id]
                if (c.isFolder) {
                    if (old == null) {
                        val item = call("POST", "folder", JSONObject().put("title", c.title).put("parent_folder", parentNum)).body?.optJSONObject("item")
                        val id = item?.optString("id")?.takeIf { it.isNotEmpty() } ?: throw SyncError(SyncError.SERVER, "folder ${c.title}")
                        renames[c.id] = "f$id"
                    } else if (old.title != c.title || oldParent[c.id] != parent.id) {
                        call("PUT", "folder/${folderNum(c.id, root)}", JSONObject().put("title", c.title).put("parent_folder", parentNum))
                    }
                    walk(c)
                } else if (old == null) {
                    // an address the server knows already is added to this folder, not created again
                    val existing = call("GET", "bookmark?url=" + java.net.URLEncoder.encode(c.url!!, "UTF-8"))
                        .body?.optJSONArray("data")?.optJSONObject(0)
                    val bid = if (existing != null) {
                        val id = existing.optString("id")
                        call("POST", "folder/$parentNum/bookmarks/$id")
                        if (existing.optString("title") != c.title) call("PUT", "bookmark/$id", JSONObject().put("title", c.title))
                        id
                    } else {
                        call("POST", "bookmark", JSONObject().put("url", c.url).put("title", c.title).put("folders", JSONArray().put(parentNum)))
                            .body?.optJSONObject("item")?.optString("id")?.takeIf { it.isNotEmpty() } ?: throw SyncError(SyncError.SERVER, "bookmark ${c.url}")
                    }
                    renames[c.id] = "b$bid;$parentNum"
                } else {
                    val bid = bookmarkNum(c.id)
                    if (old.title != c.title || old.url != c.url) {
                        call("PUT", "bookmark/$bid", JSONObject().put("title", c.title).put("url", c.url))
                    }
                    val from = bookmarkFolder(c.id)
                    if (from != parentNum) {
                        call("POST", "folder/$parentNum/bookmarks/$bid")
                        call("DELETE", "folder/$from/bookmarks/$bid", okCodes = setOf(403, 404))
                        renames[c.id] = "b$bid;$parentNum"
                    }
                }
            }
        }
        walk(after)

        // 2. what the merged tree no longer has
        for ((id, old) in oldNodes) {
            if (id in newIds || old.isFolder) continue
            call("DELETE", "folder/${bookmarkFolder(id)}/bookmarks/${bookmarkNum(id)}", okCodes = setOf(403, 404))
        }
        for ((id, old) in oldNodes) {
            if (id in newIds || !old.isFolder) continue
            call("DELETE", "folder/${folderNum(id, root)}", okCodes = setOf(404))
        }

        // 3. the order of each folder's children, where it changed
        val oldOrder = HashMap<String, List<String>>()
        oldOrder[SyncNode.ROOT_ID] = before.children.map { it.id }
        for ((id, n) in oldNodes) if (n.isFolder) oldOrder[id] = n.children.map { it.id }
        fun order(n: SyncNode) {
            val now = n.children.map { real(it.id) }
            if (n.children.size > 1 && oldOrder[n.id] != n.children.map { it.id }) {
                val data = JSONArray()
                for (id in now) {
                    data.put(JSONObject().put("type", if (id.startsWith("f")) "folder" else "bookmark")
                        .put("id", if (id.startsWith("f")) id.removePrefix("f").toLong() else bookmarkNum(id)))
                }
                call("PATCH", "folder/${folderNum(real(n.id), root)}/childorder", JSONObject().put("data", data), okCodes = setOf(404, 405))
            }
            for (c in n.children) if (c.isFolder) order(c)
        }
        order(after)
        return renames
    }
}
