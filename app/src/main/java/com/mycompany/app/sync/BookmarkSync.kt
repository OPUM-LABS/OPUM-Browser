package com.mycompany.app.sync

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import com.mycompany.app.data.sync.FloccusCrypt
import com.mycompany.app.data.sync.SyncConfig
import com.mycompany.app.data.sync.SyncError
import com.mycompany.app.data.sync.SyncFile
import com.mycompany.app.data.sync.SyncMerge
import com.mycompany.app.data.sync.SyncTransport
import com.mycompany.app.data.sync.Xbel
import com.mycompany.app.net.LocalNet
import com.mycompany.app.pref.store.Workspaces
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetAddress
import java.util.concurrent.Executors

/**
 * Bookmark sync with floccus (2026-11-28): each workspace can keep its bookmarks in one XBEL file on a WebDAV server
 * or in an S3 bucket, the same file the floccus extension syncs in Chrome, Firefox, Edge or Brave.
 *
 * One sync: take floccus' lock file, read the file (decrypting it with the passphrase), merge it with the phone's
 * bookmarks ([SyncMerge]), write the file when something changed on the phone, write the phone's bookmarks, keep the
 * new state, release the lock. Nothing on the phone changes before the file was written, so a failed upload leaves
 * both sides as they were.
 *
 * It runs when the browser is left with changed bookmarks, when it comes back after a while, every half hour in the
 * background (a job that waits for a network) and from "Sync now".
 */
object BookmarkSync {
    private const val LOCK_SUFFIX = ".lock"
    private const val LOCK_BODY = "<html><body>I am a lock file</body></html>"
    /** A lock older than this is left over from a sync that broke off (floccus uses the same time). */
    private const val LOCK_TIMEOUT = 15 * 60 * 1000L
    private const val RETURN_AFTER = 5 * 60 * 1000L
    const val JOB_PERIODIC = 0x0B5C
    const val JOB_SOON = 0x0B5D

    private val executor = Executors.newSingleThreadExecutor { r -> Thread(r, "bookmark-sync").apply { isDaemon = true } }
    private val running = HashSet<Int>()
    private val _busy = MutableStateFlow<Set<Int>>(emptySet())
    /** The workspaces syncing right now (the settings page shows it). */
    val busy: StateFlow<Set<Int>> get() = _busy
    private val _done = MutableStateFlow(0)
    /** Counts finished syncs, so a page can read the new result. */
    val done: StateFlow<Int> get() = _done

    /** The passphrase key of the last file read, per workspace (deriving it takes about a second). */
    private val keys = HashMap<Int, Pair<String, FloccusCrypt.Key>>()

    /** Workspaces that still exist and have the sync switched on. */
    fun activeNumbers(context: Context): List<Int> {
        val known = Workspaces.all().map { it.number }.toHashSet().apply { add(Workspaces.MAIN) }
        return BookmarkSyncStore.numbers(context).filter { it in known && BookmarkSyncStore.config(context, it).let { c -> c.enabled && c.isSet } }
    }

    /** Syncs workspace [number] in the background; [then] runs on that thread with the result (0 = fine). */
    fun syncAsync(context: Context, number: Int, then: ((Int) -> Unit)? = null) {
        val app = context.applicationContext
        executor.execute { val r = sync(app, number); then?.invoke(r) }
    }

    /** One sync of workspace [number], on the calling thread. Returns 0 or the [SyncError] kind. */
    fun sync(context: Context, number: Int): Int {
        synchronized(running) {
            if (!running.add(number)) return 0
            _busy.value = running.toSet()
        }
        try {
            val config = BookmarkSyncStore.config(context, number)
            val status = try {
                if (!config.isSet) throw SyncError(SyncError.NOT_SET)
                run(context, number, config)
            } catch (e: SyncError) {
                val kind = if (e.kind == SyncError.NETWORK && blockedLocal(context, config)) SyncError.LOCAL_NET else e.kind
                BookmarkSyncStore.Status(System.currentTimeMillis(), kind, e.detail, e.fingerprint, 0, 0, 0, false)
            } catch (e: Exception) {
                BookmarkSyncStore.Status(System.currentTimeMillis(), SyncError.SERVER, e.message ?: e.javaClass.simpleName, null, 0, 0, 0, false)
            }
            if (status.error != 0) BookmarkSyncStore.saveState(context, number, null, 0, status)
            return status.error
        } finally {
            synchronized(running) {
                running.remove(number)
                _busy.value = running.toSet()
            }
            _done.value = _done.value + 1
        }
    }

    /** The server is on the local network and Android 17's local network permission is missing. */
    private fun blockedLocal(context: Context, config: SyncConfig): Boolean {
        if (!LocalNet.applies() || LocalNet.granted(context)) return false
        val host = config.host ?: return false
        if (LocalNet.isLocalHost(host)) return true
        return try { InetAddress.getAllByName(host).any { LocalNet.isLocalAddress(it) } } catch (_: Exception) { false }
    }

    private fun run(context: Context, number: Int, config: SyncConfig): BookmarkSyncStore.Status {
        if (LocalNet.applies() && !LocalNet.granted(context) && LocalNet.isLocalHost(config.host)) throw SyncError(SyncError.LOCAL_NET)
        val transport = SyncTransport.of(config)
        val file = config.file.trim().trim('/')
        val lockName = file + LOCK_SUFFIX
        // floccus' lock: a fresh lock file means another browser is syncing right now
        val lock = transport.get(lockName)
        if (lock.body != null) {
            val written = lock.lastModified ?: throw SyncError(SyncError.LOCKED)
            val now = lock.serverDate ?: System.currentTimeMillis()
            if (now - written < LOCK_TIMEOUT) throw SyncError(SyncError.LOCKED)
        }
        transport.put(lockName, LOCK_BODY.toByteArray(), "text/html")
        try {
            val remote = transport.get(file)
            var key: FloccusCrypt.Key? = null
            var encrypted = false
            val parsed: SyncFile? = remote.body?.let { bytes ->
                val text = String(bytes, Charsets.UTF_8)
                val plain = when {
                    FloccusCrypt.isEncrypted(text) || (!Xbel.looksLike(text) && config.passphrase.isNotEmpty()) -> {
                        if (config.passphrase.isEmpty()) throw SyncError(SyncError.NEED_PASSPHRASE)
                        encrypted = true
                        val salt = try { FloccusCrypt.saltOf(text, file.substringAfterLast('/')) } catch (_: Exception) { throw SyncError(SyncError.BAD_FILE) }
                        key = keyFor(number, config.passphrase, salt)
                        try { FloccusCrypt.decrypt(text, key!!) } catch (_: FloccusCrypt.WrongPassphrase) { throw SyncError(SyncError.PASSPHRASE) }
                    }
                    Xbel.looksLike(text) -> text
                    text.isBlank() -> null
                    else -> throw SyncError(SyncError.BAD_FILE)
                }
                plain?.let { try { Xbel.parse(it) } catch (e: Xbel.FormatException) { throw SyncError(SyncError.BAD_FILE, e.message) } }
            }
            val state = BookmarkSyncStore.state(context, number)
            val local = BookmarkSyncLocal.read(context, number)
            val (_, deletedAt) = BookmarkSyncLocal.changeTimes(context, number)
            // the state of the last sync counts only for the same file: a new place, or a file that is gone, is
            // joined with the phone's bookmarks like a first sync (nothing on the phone is deleted because of it)
            val place = BookmarkSyncStore.placeOf(config)
            val snapshot = if (parsed == null || state.place != place) null else state.snapshot
            val result = SyncMerge.merge(local, parsed, snapshot, remote.lastModified ?: Long.MAX_VALUE, deletedAt)
            // a passphrase set or removed on the phone: the file is written again, encrypted or not
            val reEncrypt = parsed != null && encrypted != config.passphrase.isNotEmpty()
            if (result.upload || reEncrypt) {
                var text = Xbel.write(result.tree, result.highestId)
                if (config.passphrase.isNotEmpty()) {
                    val k = key ?: keyFor(number, config.passphrase, null)
                    text = FloccusCrypt.encrypt(text, k)
                }
                transport.replace(file, text.toByteArray(Charsets.UTF_8), "application/xml")
            }
            val (newIds, changedAt) = BookmarkSyncLocal.apply(context, number, local, result)
            val status = BookmarkSyncStore.Status(System.currentTimeMillis(), 0, null, null,
                result.phoneAdded, result.phoneChanged, result.phoneRemoved, result.upload || reEncrypt)
            BookmarkSyncStore.saveState(context, number, result.snapshot(newIds), changedAt, status, place)
            return status
        } finally {
            try { transport.delete(lockName) } catch (_: Exception) {}
        }
    }

    private fun keyFor(number: Int, passphrase: String, salt: String?): FloccusCrypt.Key {
        synchronized(keys) {
            val cached = keys[number]
            if (cached != null && cached.first == passphrase && (salt == null || cached.second.salt == salt)) return cached.second
        }
        val key = FloccusCrypt.derive(passphrase, salt ?: FloccusCrypt.newSalt())
        synchronized(keys) { keys[number] = passphrase to key }
        return key
    }

    /** The bookmarks changed since the last sync. */
    fun isDirty(context: Context, number: Int): Boolean {
        val (changed, _) = BookmarkSyncLocal.changeTimes(context, number)
        return changed > BookmarkSyncStore.state(context, number).applied
    }

    // --- when it runs

    /** The browser is left: changed bookmarks are synced as soon as there is a network. */
    fun onLeave(context: Context) {
        val app = context.applicationContext
        executor.execute {
            try {
                if (activeNumbers(app).any { isDirty(app, it) }) scheduleSoon(app)
            } catch (_: Exception) {
            }
        }
    }

    /** The browser comes back: the workspace in use is synced when it changed or was last synced a while ago. */
    fun onReturn(context: Context) {
        val app = context.applicationContext
        executor.execute {
            try {
                val number = Workspaces.currentNumber()
                if (number !in activeNumbers(app)) return@execute
                val state = BookmarkSyncStore.state(app, number)
                val last = state.status?.time ?: 0L
                if (isDirty(app, number) || System.currentTimeMillis() - last > RETURN_AFTER) sync(app, number)
            } catch (_: Exception) {
            }
        }
    }

    /** The background job: on when any workspace syncs in the background, off otherwise. */
    fun schedule(context: Context) {
        val app = context.applicationContext
        executor.execute {
            try {
                val scheduler = app.getSystemService(JobScheduler::class.java) ?: return@execute
                val wanted = activeNumbers(app).any { BookmarkSyncStore.config(app, it).background }
                if (!wanted) { scheduler.cancel(JOB_PERIODIC); return@execute }
                if (scheduler.getPendingJob(JOB_PERIODIC) != null) return@execute
                scheduler.schedule(
                    JobInfo.Builder(JOB_PERIODIC, ComponentName(app, BookmarkSyncJob::class.java))
                        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        .setPeriodic(30 * 60 * 1000L)
                        .build(),
                )
            } catch (_: Exception) {
            }
        }
    }

    private fun scheduleSoon(context: Context) {
        try {
            val scheduler = context.getSystemService(JobScheduler::class.java) ?: return
            scheduler.schedule(
                JobInfo.Builder(JOB_SOON, ComponentName(context, BookmarkSyncJob::class.java))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .build(),
            )
        } catch (_: Exception) {
        }
    }

    /** A workspace was deleted: its sync goes too. */
    fun forget(context: Context, number: Int) {
        BookmarkSyncStore.remove(context, number)
        synchronized(keys) { keys.remove(number) }
        schedule(context)
    }
}
