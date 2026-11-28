package com.mycompany.app.data.sync

/**
 * A place that holds the one bookmark file of a sync (2026-11-28): a WebDAV folder, an S3 bucket ([TransportStore]) or
 * a Git repository ([GitStore]). [lock] keeps other browsers from writing while this sync runs, as floccus does.
 */
interface FileStore {
    fun lock()
    fun unlock()
    fun read(): Remote
    fun write(bytes: ByteArray)
}

/** The file on a WebDAV server or in an S3 bucket, locked with floccus' lock file "<file>.lock". */
class TransportStore(private val transport: SyncTransport, file: String) : FileStore {
    private val name = file.trim().trim('/')
    private val lockName = name + LOCK_SUFFIX

    override fun lock() {
        // a fresh lock file means another browser is syncing right now
        val lock = transport.get(lockName)
        if (lock.body != null) {
            val written = lock.lastModified ?: throw SyncError(SyncError.LOCKED)
            val now = lock.serverDate ?: System.currentTimeMillis()
            if (now - written < LOCK_TIMEOUT) throw SyncError(SyncError.LOCKED)
        }
        transport.put(lockName, LOCK_BODY.toByteArray(), "text/html")
    }

    override fun unlock() {
        try { transport.delete(lockName) } catch (_: Exception) {}
    }

    override fun read(): Remote = transport.get(name)

    override fun write(bytes: ByteArray) = transport.replace(name, bytes, "application/xml")

    companion object {
        private const val LOCK_SUFFIX = ".lock"
        private const val LOCK_BODY = "<html><body>I am a lock file</body></html>"
        /** A lock older than this is left over from a sync that broke off (floccus uses the same time). */
        const val LOCK_TIMEOUT = 15 * 60 * 1000L
    }
}
