package com.mycompany.app.data.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The merge against a simulated phone (rows with ids, as the bookmark table holds them) and a server file, sync after
 * sync, as the app runs it: merge, write the file, write the phone, keep the snapshot.
 */
class SyncMergeTest {

    private class Phone {
        val items = ArrayList<LocalItem>()
        var next = 1L
        var snap: Snapshot? = null

        fun folder(title: String, parent: Long = 0, mtime: Long = 1): Long {
            val id = next++
            items.add(LocalItem(id, true, title, null, parent, mtime)); return id
        }

        fun bookmark(title: String, url: String, parent: Long = 0, mtime: Long = 1): Long {
            val id = next++
            items.add(LocalItem(id, false, title, url, parent, mtime)); return id
        }

        fun byTitle(t: String) = items.first { it.title == t }

        fun edit(id: Long, title: String? = null, url: String? = null, parent: Long? = null, mtime: Long) {
            val i = items.indexOfFirst { it.localId == id }
            val o = items[i]
            items[i] = LocalItem(o.localId, o.isFolder, title ?: o.title, url ?: o.url, parent ?: o.parentLocalId, mtime)
        }

        fun remove(id: Long) {
            val gone = HashSet<Long>()
            fun mark(x: Long) { gone.add(x); items.filter { it.parentLocalId == x }.forEach { mark(it.localId) } }
            mark(id)
            items.removeAll { it.localId in gone }
        }

        /** Items in display order (parents first, each folder's children in their order, folders first). */
        fun ordered(): List<LocalItem> {
            val out = ArrayList<LocalItem>()
            fun walk(p: Long) { for (c in items.filter { it.parentLocalId == p }.sortedBy { !it.isFolder }) { out.add(c); if (c.isFolder) walk(c.localId) } }
            walk(0)
            return out
        }

        fun apply(r: MergeResult) {
            val byServer = HashMap<String, Long>()
            val newIds = HashMap<String, Long>()
            val old = items.associateBy { it.localId }
            items.removeAll { it.localId in r.deletes }
            val rebuilt = ArrayList<LocalItem>()
            for (row in r.rows) {
                val parent = if (row.parentServerId == SyncNode.ROOT_ID) 0L else byServer[row.parentServerId]!!
                val id = if (row.localId > 0) row.localId else next++.also { newIds[row.serverId] = it }
                byServer[row.serverId] = id
                val mtime = old[id]?.mtime ?: 0
                rebuilt.add(LocalItem(id, row.isFolder, row.localTitle, row.url, parent, mtime))
            }
            items.clear(); items.addAll(rebuilt)
            snap = r.snapshot(newIds)
        }

        /** Titles as an outline: "Work[a,c] b d". */
        fun outline(): String {
            fun of(p: Long): String = ordered().filter { it.parentLocalId == p }.joinToString(" ") {
                if (it.isFolder) it.title + "[" + of(it.localId).replace(' ', ',') + "]" else it.title
            }
            return of(0)
        }
    }

    private class Server {
        var file: SyncFile? = null
        var time = 0L
        fun outline(): String {
            fun of(n: SyncNode): String = n.children.joinToString(" ") { if (it.isFolder) it.title + "[" + of(it).replace(' ', ',') + "]" else it.title }
            return file?.let { of(it.root) } ?: ""
        }
        fun node(title: String) = file!!.root.descendants().first { it.title == title }
        /** Another browser changes the file. */
        fun change(at: Long, block: (SyncNode) -> Unit) {
            val f = Xbel.parse(Xbel.write(file!!.root, file!!.highestId))
            block(f.root)
            var h = f.highestId
            for (n in f.root.descendants()) if (n.id.isEmpty()) n.id = (++h).toString()
            file = SyncFile(f.root, h); time = at
        }
    }

    private fun sync(p: Phone, s: Server, now: Long, deletedAt: Long = 0): MergeResult {
        val r = SyncMerge.merge(p.ordered(), s.file, p.snap, s.time, deletedAt)
        if (r.upload) { s.file = Xbel.parse(Xbel.write(r.tree, r.highestId)); s.time = now }
        p.apply(r)
        return r
    }

    private fun setUp(): Pair<Phone, Server> {
        val p = Phone()
        val work = p.folder("Work")
        p.bookmark("a", "https://a/", work)
        p.bookmark("b", "https://b/")
        val s = Server()
        s.file = Xbel.parse("""<xbel><!--- highestId :4: x --><folder id="1"><title>Work</title><bookmark id="2" href="https://c/"><title>c</title></bookmark><bookmark id="3" href="https://a/"><title>a</title></bookmark></folder><bookmark id="4" href="https://d/"><title>d</title></bookmark></xbel>""")
        s.time = 5
        sync(p, s, 10)
        return p to s
    }

    @Test
    fun firstSyncJoinsBothSides() {
        val (p, s) = setUp()
        assertEquals("Work[c,a] d b", s.outline())
        assertEquals("Work[c,a] d b", p.outline())
        // nothing changed: nothing is written
        val again = sync(p, s, 20)
        assertFalse(again.upload)
        assertEquals(0, again.phoneAdded + again.phoneChanged + again.phoneRemoved)
    }

    @Test
    fun changesOnTheServerReachThePhone() {
        val (p, s) = setUp()
        s.change(30) { root ->
            root.descendants().first { it.title == "c" }.title = "c2"
            root.children.add(SyncNode("", "e", "https://e/"))
            root.children.removeAll { it.title == "d" }
        }
        val r = sync(p, s, 40)
        assertFalse(r.upload)
        assertEquals("Work[c2,a] b e", p.outline())
    }

    @Test
    fun changesOnThePhoneReachTheServer() {
        val (p, s) = setUp()
        p.edit(p.byTitle("b").localId, title = "b2", mtime = 50)
        p.remove(p.byTitle("d").localId)
        p.bookmark("f", "https://f/", p.byTitle("Work").localId, mtime = 50)
        val r = sync(p, s, 60, deletedAt = 50)
        assertTrue(r.upload)
        assertEquals("Work[c,a,f] b2", s.outline())
        assertEquals("Work[c,a,f] b2", p.outline())
    }

    @Test
    fun bothChangedTheNewerWins() {
        val (p, s) = setUp()
        s.change(30) { root -> root.descendants().first { it.title == "b" }.title = "server" }
        p.edit(p.byTitle("b").localId, title = "phone", mtime = 40)
        sync(p, s, 50)
        assertEquals("phone", p.items.first { it.url == "https://b/" }.title)
        assertEquals("phone", s.file!!.root.descendants().first { it.url == "https://b/" }.title)

        s.change(80) { root -> root.descendants().first { it.url == "https://b/" }.title = "server2" }
        p.edit(p.items.first { it.url == "https://b/" }.localId, title = "phone2", mtime = 70)
        sync(p, s, 90)
        assertEquals("server2", p.items.first { it.url == "https://b/" }.title)
        assertEquals("server2", s.file!!.root.descendants().first { it.url == "https://b/" }.title)
    }

    @Test
    fun deletedOnThePhoneButChangedLaterOnTheServerStays() {
        val (p, s) = setUp()
        p.remove(p.byTitle("d").localId)
        s.change(30) { root -> root.descendants().first { it.title == "d" }.title = "d2" }
        sync(p, s, 40, deletedAt = 20)
        assertEquals("Work[c,a] d2 b", s.outline())
        assertEquals("Work[c,a] d2 b", p.outline())
    }

    @Test
    fun movedOnThePhone() {
        val (p, s) = setUp()
        p.edit(p.byTitle("b").localId, parent = p.byTitle("Work").localId, mtime = 30)
        sync(p, s, 40)
        assertEquals("Work[c,a,b] d", s.outline())
    }

    @Test
    fun folderDeletedOnThePhoneKeepsWhatTheServerAddedToIt() {
        val (p, s) = setUp()
        p.remove(p.byTitle("Work").localId)
        s.change(30) { root -> root.children.first { it.title == "Work" }.children.add(SyncNode("", "new", "https://new/")) }
        sync(p, s, 40, deletedAt = 20)
        assertEquals("Work[new] d b", s.outline())
        assertEquals("Work[new] d b", p.outline())
    }

    @Test
    fun folderNamesThePhoneCannotHold() {
        val p = Phone()
        val s = Server()
        s.file = Xbel.parse("""<xbel><folder id="1"><title>A</title></folder><folder id="2"><title>A</title></folder><folder id="3"><title>x/y</title></folder></xbel>""")
        s.time = 1
        sync(p, s, 10)
        assertEquals("A[] A (2)[] x∕y[]", p.outline())
        // the file keeps its own names
        assertEquals("A[] A[] x/y[]", s.outline())
        assertFalse(sync(p, s, 20).upload)
    }

    @Test
    fun joiningPairsAFolderWithItsPhoneName() {
        val p = Phone()
        p.folder("x\u2215y")
        val s = Server()
        s.file = Xbel.parse("""<xbel><folder id="1"><title>x/y</title></folder></xbel>""")
        s.time = 1
        val r = sync(p, s, 10)
        assertFalse(r.upload)
        assertEquals("x/y[]", s.outline())
        assertEquals("x\u2215y[]", p.outline())
    }

    @Test
    fun reorderOnThePhoneKeepsTheFilesMixOfFoldersAndBookmarks() {
        val (p, s) = setUp()
        // server: Work[c,a] d b; the phone shows Work, then d, b: it moves b before d
        val b = p.byTitle("b")
        val d = p.byTitle("d")
        p.items.remove(b); p.items.add(p.items.indexOf(d), b)
        sync(p, s, 30)
        assertEquals("Work[c,a] b d", s.outline())
    }

    @Test
    fun emptyServerGetsThePhonesBookmarks() {
        val p = Phone()
        p.bookmark("a", "https://a/")
        val s = Server()
        val r = sync(p, s, 10)
        assertTrue(r.upload)
        assertEquals("a", s.outline())
        assertEquals(1L, s.file!!.highestId)
    }
}
