package com.mycompany.app.data.sync

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.dircache.DirCache
import org.eclipse.jgit.dircache.DirCacheEditor
import org.eclipse.jgit.dircache.DirCacheEntry
import org.eclipse.jgit.lib.CommitBuilder
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.lib.FileMode
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.PersonIdent
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.RefSpec
import org.eclipse.jgit.transport.RemoteRefUpdate
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.eclipse.jgit.treewalk.TreeWalk
import org.eclipse.jgit.transport.HttpTransport
import org.eclipse.jgit.transport.http.HttpConnection
import org.eclipse.jgit.transport.http.JDKHttpConnectionFactory
import java.io.File
import java.net.Proxy
import java.net.URL

/**
 * The bookmark file in a Git repository over HTTPS, the way floccus' "Git" profile keeps it (2026-11-28): one file on
 * one branch, each sync that changed something is one commit pushed without force. floccus' lock is a tag
 * "floccus-lock-<time in ms>" on the server; a newer one than 15 minutes means another browser is syncing. A push the
 * server refuses (someone pushed in between) is reported like a lock, and the next sync tries again.
 *
 * The repository is kept in [dir] between syncs (a bare clone of the one branch), so each fetch is small.
 *
 * HTTPS uses the same certificate check as WebDAV and S3 ([SyncTransport.trustManager]): the phone's trusted
 * certificates plus the one the user chose to trust for this server, so "Trust this certificate" works here too.
 */
class GitStore(private val config: SyncConfig, private val dir: File) : FileStore {
    private val url = SyncConfig.withScheme(config.url)
    private val branch = config.branch.trim().ifEmpty { SyncConfig.DEFAULT_BRANCH }
    private val path = config.file.trim().trim('/')
    private val creds = UsernamePasswordCredentialsProvider(config.user, config.password)
    private var repo: Repository? = null
    private var head: ObjectId? = null
    private var lockTag: String? = null
    private val seen = arrayOfNulls<String>(1)
    private val tls = Tls(config.pin, seen)

    private fun repo(): Repository {
        repo?.let { return it }
        // JGit looks for a user configuration in the home folder, which an app has not got
        if (System.getProperty("user.home").isNullOrEmpty()) System.setProperty("user.home", dir.parentFile?.path ?: dir.path)
        val r = FileRepositoryBuilder().setGitDir(dir).setBare().build()
        if (!File(dir, "config").exists()) r.create(true)
        repo = r
        return r
    }

    private fun <T> git(block: () -> T): T {
        installFactory()
        val before = current.get()
        current.set(tls)
        return try {
            block()
        } catch (e: SyncError) {
            throw e
        } catch (e: GitAPIException) {
            throw failure(e)
        } catch (e: Exception) {
            throw failure(e)
        } finally {
            current.set(before)
        }
    }

    private fun failure(e: Exception): SyncError {
        val text = generateSequence<Throwable>(e) { it.cause }.mapNotNull { it.message }.joinToString(" / ")
        val lower = text.lowercase()
        // a refused certificate first: its message ("trust anchor ... not found") would read as a missing repository
        return when {
            "certificate" in lower || "trust anchor" in lower || "ssl" in lower || "peer not" in lower ->
                SyncError(SyncError.CERT, text, seen[0])
            "not authorized" in lower || "authentication" in lower || " 401" in lower || " 403" in lower -> SyncError(SyncError.AUTH, text)
            "not found" in lower || " 404" in lower -> SyncError(SyncError.FOLDER, text)
            "unknownhost" in lower || "timed out" in lower || "connect" in lower -> SyncError(SyncError.NETWORK, text)
            else -> SyncError(SyncError.SERVER, text)
        }
    }

    private fun fetch() = git {
        Git(repo()).fetch()
            .setRemote(url)
            .setCredentialsProvider(creds)
            .setRefSpecs(RefSpec("+${Constants.R_HEADS}$branch:${Constants.R_REMOTES}origin/$branch"))
            .call()
        head = repo().resolve("${Constants.R_REMOTES}origin/$branch")
    }

    private fun push(spec: RefSpec): Iterable<PushResult> = git {
        Git(repo()).push().setRemote(url).setCredentialsProvider(creds).setRefSpecs(spec).call()
    }

    override fun lock() {
        val refs = git { Git.lsRemoteRepository().setRemote(url).setCredentialsProvider(creds).setTags(true).setHeads(true).call() }
        val newest = refs.mapNotNull { it.name.removePrefix("${Constants.R_TAGS}$LOCK_PREFIX").takeIf { n -> n != it.name }?.toLongOrNull() }.maxOrNull()
        if (newest != null && System.currentTimeMillis() - newest < FileStoreLockTimeout) throw SyncError(SyncError.LOCKED)
        // a new, empty repository (or a branch not made yet) has nothing to fetch: the first write makes it
        if (refs.any { it.name == Constants.R_HEADS + branch }) fetch() else head = null
        // a tag needs something to point at: a branch that does not exist yet is not locked (nobody else can write it)
        val at = head ?: return
        val tag = LOCK_PREFIX + System.currentTimeMillis()
        push(RefSpec("${at.name}:${Constants.R_TAGS}$tag"))
        lockTag = tag
    }

    override fun unlock() {
        val tag = lockTag ?: return
        lockTag = null
        try { push(RefSpec(":${Constants.R_TAGS}$tag")) } catch (_: Exception) {}
        try { repo?.close() } catch (_: Exception) {}
    }

    override fun read(): Remote = git {
        val commitId = head ?: return@git Remote(null, null, null)
        RevWalk(repo()).use { walk ->
            val commit = walk.parseCommit(commitId)
            val tw = TreeWalk.forPath(repo(), path, commit.tree) ?: return@git Remote(null, commit.commitTime * 1000L, null)
            Remote(repo().open(tw.getObjectId(0)).bytes, commit.commitTime * 1000L, null)
        }
    }

    override fun write(bytes: ByteArray) {
        val r = repo()
        val commitId = git {
            r.newObjectInserter().use { ins ->
                val blob = ins.insert(Constants.OBJ_BLOB, bytes)
                val index = DirCache.newInCore()
                val parent = head
                if (parent != null) {
                    RevWalk(r).use { walk ->
                        val builder = index.builder()
                        r.newObjectReader().use { reader -> builder.addTree(ByteArray(0), DirCacheEntry.STAGE_0, reader, walk.parseCommit(parent).tree) }
                        builder.finish()
                    }
                }
                val editor = index.editor()
                editor.add(object : DirCacheEditor.PathEdit(path) {
                    override fun apply(ent: DirCacheEntry) {
                        ent.fileMode = FileMode.REGULAR_FILE
                        ent.setObjectId(blob)
                        ent.setLength(bytes.size)
                    }
                })
                editor.finish()
                val tree = index.writeTree(ins)
                val who = PersonIdent(AUTHOR, AUTHOR_MAIL)
                val commit = CommitBuilder()
                commit.setTreeId(tree)
                if (parent != null) commit.setParentId(parent)
                commit.author = who
                commit.committer = who
                commit.message = "OPUM Browser: bookmarks ($path)\n"
                val id = ins.insert(commit)
                ins.flush()
                id
            }
        }
        val results = push(RefSpec("${commitId.name}:${Constants.R_HEADS}$branch"))
        for (res in results) for (u in res.remoteUpdates) {
            when (u.status) {
                RemoteRefUpdate.Status.OK, RemoteRefUpdate.Status.UP_TO_DATE -> {}
                RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD, RemoteRefUpdate.Status.REJECTED_REMOTE_CHANGED ->
                    throw SyncError(SyncError.LOCKED, "pushed in between")
                else -> throw SyncError(SyncError.SERVER, u.status.name + (u.message?.let { ": $it" } ?: ""))
            }
        }
        head = commitId
    }

    /** The certificate check of one sync, for the connections JGit opens on this thread. */
    private class Tls(val pin: String, val seen: Array<String?>)

    companion object {
        private val current = ThreadLocal<Tls?>()
        @Volatile private var installed = false

        /**
         * JGit opens its HTTPS connections through one factory for the whole app: this one gives each connection the
         * check of the sync running on its thread. JGit replaces it only for repositories that turn checks off
         * (http.sslVerify), which OPUM never does.
         */
        @Synchronized
        private fun installFactory() {
            if (installed) return
            HttpTransport.setConnectionFactory(object : JDKHttpConnectionFactory() {
                override fun create(url: URL): HttpConnection = withCheck(super.create(url))
                override fun create(url: URL, proxy: Proxy?): HttpConnection = withCheck(super.create(url, proxy))
            })
            installed = true
        }

        private fun withCheck(conn: HttpConnection): HttpConnection {
            val tls = current.get() ?: return conn
            if (conn.url.protocol.equals("https", ignoreCase = true)) {
                conn.configure(null, arrayOf(SyncTransport.trustManager(tls.pin, tls.seen)), null)
                conn.setHostnameVerifier(SyncTransport.hostnameVerifier(tls.pin))
            }
            return conn
        }

        private const val LOCK_PREFIX = "floccus-lock-"
        private const val FileStoreLockTimeout = TransportStore.LOCK_TIMEOUT
        private const val AUTHOR = "OPUM Browser"
        private const val AUTHOR_MAIL = "bookmarks@opum.invalid"
    }
}
