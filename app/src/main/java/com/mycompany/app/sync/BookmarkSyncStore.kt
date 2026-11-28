package com.mycompany.app.sync

import android.content.Context
import com.mycompany.app.data.sync.SyncConfig
import com.mycompany.app.data.sync.Snapshot
import com.mycompany.app.db.book.PassCrypt
import com.mycompany.app.pref.PrefBookSync
import org.json.JSONObject
import java.io.File

/**
 * What the bookmark sync keeps (2026-11-28):
 *  - per workspace, where it syncs ([SyncConfig]), in the preference file `PrefBookSync` (part of backups with the
 *    settings); the password, the S3 secret key and the passphrase are sealed with the phone's key ([PassCrypt]),
 *  - per workspace, the state after the last sync and its result, in `files/bsync/<number>.json` (not in backups: after
 *    a restore the next sync starts afresh and joins both sides).
 */
object BookmarkSyncStore {
    private const val KEY = "mProfiles"

    /** The result of the last sync, for the settings page. */
    class Status(
        val time: Long,
        val error: Int,
        val detail: String?,
        val fingerprint: String?,
        val added: Int,
        val changed: Int,
        val removed: Int,
        val uploaded: Boolean,
    )

    /** [place] is where the snapshot was synced ([placeOf]): a snapshot of another place is never used. */
    class State(val snapshot: Snapshot?, val applied: Long, val status: Status?, val lastOk: Long, val place: String = "")

    /** The storage, server, bucket and file a sync state belongs to. */
    fun placeOf(c: SyncConfig): String = listOf(c.kind, if (c.kind == SyncConfig.S3) c.endpoint.trim() + "|" + c.bucket.trim() else c.url.trim(),
        when (c.kind) {
            SyncConfig.NEXTCLOUD -> c.user.trim() + "|" + c.ncFolder.trim()
            SyncConfig.GIT -> c.branch.trim() + "|" + c.file.trim().trim('/')
            else -> c.file.trim().trim('/')
        }).joinToString("|")

    // --- where it syncs

    private fun all(context: Context): JSONObject {
        val text = PrefBookSync.get(context, false)?.readString(KEY, "").orEmpty()
        return try { if (text.isBlank()) JSONObject() else JSONObject(text) } catch (_: Exception) { JSONObject() }
    }

    fun numbers(context: Context): List<Int> = all(context).keys().asSequence().mapNotNull { it.toIntOrNull() }.toList()

    fun config(context: Context, number: Int): SyncConfig {
        val o = all(context).optJSONObject(number.toString()) ?: return SyncConfig()
        return fromJson(o) { PassCrypt.open(it) ?: "" }
    }

    @Synchronized
    fun save(context: Context, number: Int, config: SyncConfig) {
        val all = all(context)
        all.put(number.toString(), toJson(config) { PassCrypt.seal(it) ?: "" })
        write(context, all)
    }

    @Synchronized
    fun remove(context: Context, number: Int) {
        val all = all(context)
        if (all.has(number.toString())) {
            all.remove(number.toString())
            write(context, all)
        }
        stateFile(context, number).delete()
        // the Git repository kept for it (only our own folder under files/bsync)
        File(File(context.filesDir, "bsync"), "git-$number").deleteRecursively()
    }

    private fun write(context: Context, all: JSONObject) {
        val pref = PrefBookSync.get(context, false) ?: return
        pref.putString(KEY, all.toString())
        pref.save()
    }

    /** Values written in plain text (a restored backup) are sealed again; at app start and after a restore. */
    @Synchronized
    fun sealPlain(context: Context) {
        val all = all(context)
        var changed = false
        for (k in all.keys()) {
            val o = all.optJSONObject(k) ?: continue
            for (f in SECRET_FIELDS) {
                val v = o.optString(f, "")
                if (v.isNotEmpty() && !PassCrypt.isSealed(v)) { o.put(f, PassCrypt.seal(v)); changed = true }
            }
        }
        if (changed) write(context, all)
    }

    /** The content of `PrefBookSync` with the sealed values opened, for a backup copy at [file]. */
    fun openAllIn(file: File) {
        val text = file.readText()
        val root = JSONObject(text)
        val inner = root.optString(KEY, "")
        if (inner.isBlank()) return
        val all = JSONObject(inner)
        for (k in all.keys()) {
            val o = all.optJSONObject(k) ?: continue
            for (f in SECRET_FIELDS) o.put(f, PassCrypt.open(o.optString(f, "")) ?: "")
        }
        root.put(KEY, all.toString())
        file.writeText(root.toString())
    }

    private val SECRET_FIELDS = listOf("password", "secretKey", "passphrase")

    private fun toJson(c: SyncConfig, secret: (String) -> String) = JSONObject()
        .put("enabled", c.enabled).put("kind", c.kind)
        .put("url", c.url).put("user", c.user).put("password", secret(c.password))
        .put("endpoint", c.endpoint).put("region", c.region).put("bucket", c.bucket)
        .put("accessKey", c.accessKey).put("secretKey", secret(c.secretKey))
        .put("file", c.file).put("passphrase", secret(c.passphrase))
        .put("background", c.background).put("pin", c.pin)
        .put("branch", c.branch).put("ncFolder", c.ncFolder)

    private fun fromJson(o: JSONObject, open: (String) -> String) = SyncConfig(
        enabled = o.optBoolean("enabled", false),
        kind = o.optInt("kind", SyncConfig.WEBDAV),
        url = o.optString("url", ""),
        user = o.optString("user", ""),
        password = open(o.optString("password", "")),
        endpoint = o.optString("endpoint", ""),
        region = o.optString("region", ""),
        bucket = o.optString("bucket", ""),
        accessKey = o.optString("accessKey", ""),
        secretKey = open(o.optString("secretKey", "")),
        file = o.optString("file", SyncConfig.DEFAULT_FILE).ifBlank { SyncConfig.DEFAULT_FILE },
        passphrase = open(o.optString("passphrase", "")),
        background = o.optBoolean("background", true),
        pin = o.optString("pin", ""),
        branch = o.optString("branch", SyncConfig.DEFAULT_BRANCH).ifBlank { SyncConfig.DEFAULT_BRANCH },
        ncFolder = o.optString("ncFolder", ""),
    )

    // --- state after the last sync

    private fun stateFile(context: Context, number: Int) = File(File(context.filesDir, "bsync"), "$number.json")

    @Synchronized
    fun state(context: Context, number: Int): State {
        val f = stateFile(context, number)
        val o = try { if (f.exists()) JSONObject(f.readText()) else null } catch (_: Exception) { null }
            ?: return State(null, 0, null, 0)
        val s = o.optJSONObject("status")?.let {
            Status(it.optLong("time"), it.optInt("error"), it.optString("detail").takeIf { d -> d.isNotEmpty() },
                it.optString("fp").takeIf { d -> d.isNotEmpty() }, it.optInt("added"), it.optInt("changed"),
                it.optInt("removed"), it.optBoolean("uploaded"))
        }
        return State(Snapshot.fromJson(o.optJSONObject("snapshot")), o.optLong("applied"), s, o.optLong("lastOk"), o.optString("place", ""))
    }

    @Synchronized
    fun saveState(context: Context, number: Int, snapshot: Snapshot?, applied: Long, status: Status, place: String? = null) {
        val old = state(context, number)
        val o = JSONObject()
        val snap = snapshot ?: old.snapshot
        if (snap != null) o.put("snapshot", snap.toJson())
        o.put("applied", if (snapshot != null) applied else old.applied)
        o.put("place", place ?: old.place)
        o.put("lastOk", if (status.error == 0) status.time else old.lastOk)
        o.put("status", JSONObject().put("time", status.time).put("error", status.error).put("detail", status.detail ?: "")
            .put("fp", status.fingerprint ?: "").put("added", status.added).put("changed", status.changed)
            .put("removed", status.removed).put("uploaded", status.uploaded))
        val f = stateFile(context, number)
        f.parentFile?.mkdirs()
        val tmp = File(f.path + ".tmp")
        tmp.writeText(o.toString())
        if (!tmp.renameTo(f)) { f.delete(); tmp.renameTo(f) }
    }

    /** Forgets the state of every workspace (the bookmarks were restored from a backup: the rows are new ones). */
    @Synchronized
    fun forgetStates(context: Context) {
        File(context.filesDir, "bsync").listFiles()?.filter { it.isFile }?.forEach { f ->
            try {
                val o = JSONObject(f.readText())
                o.remove("snapshot")
                o.put("applied", 0)
                f.writeText(o.toString())
            } catch (_: Exception) {
                f.delete()
            }
        }
    }
}
