package com.mycompany.app.data.sync

import android.annotation.SuppressLint
import okhttp3.Credentials
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.security.KeyStore
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLException
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/** Where a workspace's bookmarks are synced (2026-11-28): a WebDAV folder or an S3 bucket, plus the file and its passphrase. */
data class SyncConfig(
    val enabled: Boolean = false,
    val kind: Int = WEBDAV,
    // WebDAV
    val url: String = "",
    val user: String = "",
    val password: String = "",
    // S3
    val endpoint: String = "",
    val region: String = "",
    val bucket: String = "",
    val accessKey: String = "",
    val secretKey: String = "",
    // both
    val file: String = DEFAULT_FILE,
    val passphrase: String = "",
    val background: Boolean = true,
    /** SHA-256 of a server certificate the user chose to trust (a server with its own certificate), hex; "" = none. */
    val pin: String = "",
) {
    val isSet: Boolean
        get() = file.isNotBlank() && when (kind) {
            S3 -> endpoint.isNotBlank() && bucket.isNotBlank() && accessKey.isNotBlank() && secretKey.isNotBlank()
            else -> url.isNotBlank()
        }

    /** The server's host name (for the local network check and the page's summary). */
    val host: String?
        get() = (if (kind == S3) withScheme(endpoint) else withScheme(url)).toHttpUrlOrNull()?.host

    companion object {
        const val WEBDAV = 0
        const val S3 = 1
        const val DEFAULT_FILE = "bookmarks.xbel"

        fun withScheme(s: String): String {
            val t = s.trim()
            return if (t.isEmpty() || t.contains("://")) t else "https://$t"
        }
    }
}

/** Why a sync did not work; [kind] decides the message the page shows. */
class SyncError(val kind: Int, val detail: String? = null, val fingerprint: String? = null) : Exception(detail) {
    companion object {
        const val NETWORK = 1
        const val AUTH = 2
        const val FOLDER = 3
        const val LOCKED = 4
        const val CERT = 5
        const val SERVER = 6
        const val PASSPHRASE = 7
        const val NEED_PASSPHRASE = 8
        const val BAD_FILE = 9
        const val LOCAL_NET = 10
        const val NOT_SET = 11
    }
}

/** A file on the server: its bytes (null = there is none), when it was last written and the server's own clock. */
class Remote(val body: ByteArray?, val lastModified: Long?, val serverDate: Long?)

/** The few file operations the sync needs; WebDAV and S3 do them their own way. */
interface SyncTransport {
    fun get(name: String): Remote
    fun put(name: String, bytes: ByteArray, type: String)
    fun delete(name: String)
    /** Writes [name] so that a reader never sees half a file where the server allows it. */
    fun replace(name: String, bytes: ByteArray, type: String) = put(name, bytes, type)

    companion object {
        fun of(config: SyncConfig): SyncTransport = if (config.kind == SyncConfig.S3) S3Transport(config) else WebDavTransport(config)

        /**
         * The HTTP client: the phone's trusted certificates, plus the one certificate the user chose to trust for this
         * server ([pin]). The certificate a server showed is kept in [seen], so a refusal can offer it.
         */
        fun client(pin: String, seen: Array<String?>): OkHttpClient {
            val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
            tmf.init(null as KeyStore?)
            val system = tmf.trustManagers.filterIsInstance<X509TrustManager>().first()
            // the system's check always runs, except for the one certificate the user compared and chose to trust
            @SuppressLint("CustomX509TrustManager")
            val trust = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) = system.checkClientTrusted(chain, authType)
                override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
                    val fp = if (chain.isNotEmpty()) fingerprint(chain[0]) else null
                    seen[0] = fp
                    if (pin.isNotEmpty() && fp == pin) return
                    system.checkServerTrusted(chain, authType)
                }
                override fun getAcceptedIssuers(): Array<X509Certificate> = system.acceptedIssuers
            }
            val ssl = SSLContext.getInstance("TLS")
            ssl.init(null, arrayOf(trust), null)
            return OkHttpClient.Builder()
                .sslSocketFactory(ssl.socketFactory, trust)
                .hostnameVerifier { host, session ->
                    val leaf = try { session.peerCertificates.firstOrNull() as? X509Certificate } catch (_: Exception) { null }
                    (pin.isNotEmpty() && leaf != null && fingerprint(leaf) == pin) || HttpsURLConnection.getDefaultHostnameVerifier().verify(host, session)
                }
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .followRedirects(true)
                .build()
        }

        fun fingerprint(cert: X509Certificate): String =
            MessageDigest.getInstance("SHA-256").digest(cert.encoded).joinToString("") { "%02x".format(it) }

        /** A network failure as a [SyncError]; a refused certificate carries the one the server showed. */
        fun failure(e: IOException, seen: Array<String?>): SyncError {
            var c: Throwable? = e
            var refused = false
            while (c != null && !refused) {
                refused = c is CertificateException || c is javax.net.ssl.SSLPeerUnverifiedException
                c = c.cause
            }
            return if (refused || (e is SSLException && seen[0] != null && e.message?.contains("ertificat") == true)) {
                SyncError(SyncError.CERT, e.message, seen[0])
            } else {
                SyncError(SyncError.NETWORK, e.message ?: e.javaClass.simpleName)
            }
        }

        fun check(r: Response, notFoundIsFolder: Boolean) {
            if (r.isSuccessful) return
            throw when (r.code) {
                401, 403 -> SyncError(SyncError.AUTH, "HTTP ${r.code}")
                404, 409 -> if (notFoundIsFolder) SyncError(SyncError.FOLDER, "HTTP ${r.code}") else SyncError(SyncError.SERVER, "HTTP ${r.code}")
                423 -> SyncError(SyncError.LOCKED, "HTTP 423")
                else -> SyncError(SyncError.SERVER, "HTTP ${r.code}")
            }
        }

        fun remoteOf(r: Response, body: ByteArray?): Remote =
            Remote(body, r.headers.getDate("Last-Modified")?.time, r.headers.getDate("Date")?.time)
    }
}

/** A file next to others in a WebDAV folder (Nextcloud, ownCloud, a NAS …), with HTTP Basic sign-in. */
class WebDavTransport(private val config: SyncConfig) : SyncTransport {
    private val seen = arrayOfNulls<String>(1)
    private val client = SyncTransport.client(config.pin, seen)
    private val base: HttpUrl = (SyncConfig.withScheme(config.url).trimEnd('/') + "/").toHttpUrlOrNull()
        ?: throw SyncError(SyncError.NOT_SET, "address")

    private fun urlOf(name: String): HttpUrl {
        val b = base.newBuilder()
        for (seg in name.trim('/').split('/')) if (seg.isNotEmpty()) b.addPathSegment(seg)
        return b.build()
    }

    private fun request(url: HttpUrl): Request.Builder {
        val r = Request.Builder().url(url)
        if (config.user.isNotEmpty() || config.password.isNotEmpty()) r.header("Authorization", Credentials.basic(config.user, config.password, Charsets.UTF_8))
        return r
    }

    private fun <T> call(req: Request, block: (Response) -> T): T = try {
        client.newCall(req).execute().use(block)
    } catch (e: IOException) {
        throw SyncTransport.failure(e, seen)
    }

    override fun get(name: String): Remote = call(request(urlOf(name)).get().header("Cache-Control", "no-cache").build()) { r ->
        if (r.code == 404) return@call SyncTransport.remoteOf(r, null)
        SyncTransport.check(r, false)
        SyncTransport.remoteOf(r, r.body.bytes())
    }

    override fun put(name: String, bytes: ByteArray, type: String) =
        call(request(urlOf(name)).put(bytes.toRequestBody(type.toMediaType())).build()) { SyncTransport.check(it, true) }

    override fun delete(name: String) = call(request(urlOf(name)).delete().build()) { r ->
        if (r.code != 404) SyncTransport.check(r, false)
    }

    /** As floccus does: written to "<file>.temp" first, then moved over the file; a server without MOVE gets a plain write. */
    override fun replace(name: String, bytes: ByteArray, type: String) {
        val temp = "$name.temp"
        put(temp, bytes, type)
        val moved = call(
            request(urlOf(temp)).method("MOVE", null).header("Destination", urlOf(name).toString()).header("Overwrite", "T").build(),
        ) { it.isSuccessful }
        if (!moved) {
            put(name, bytes, type)
            try { delete(temp) } catch (_: SyncError) {}
        }
    }
}

/** A file in an S3 bucket (Amazon S3 or an S3-compatible service), signed with Signature Version 4. */
class S3Transport(private val config: SyncConfig) : SyncTransport {
    private val seen = arrayOfNulls<String>(1)
    private val client = SyncTransport.client(config.pin, seen)
    private val endpoint: HttpUrl = SyncConfig.withScheme(config.endpoint).toHttpUrlOrNull()
        ?: throw SyncError(SyncError.NOT_SET, "endpoint")
    private val region = config.region.trim().ifEmpty { regionOf(endpoint.host) }
    // Amazon's own endpoints address the bucket by host name, the others by path (all of them accept that)
    private val virtualHost = endpoint.host.endsWith("amazonaws.com")

    private fun urlOf(name: String): HttpUrl {
        val b = HttpUrl.Builder().scheme(endpoint.scheme).port(endpoint.port)
        if (virtualHost) b.host(config.bucket.trim() + "." + endpoint.host) else b.host(endpoint.host)
        for (seg in endpoint.pathSegments) if (seg.isNotEmpty()) b.addPathSegment(seg)
        if (!virtualHost) b.addPathSegment(config.bucket.trim())
        for (seg in name.trim('/').split('/')) if (seg.isNotEmpty()) b.addPathSegment(seg)
        return b.build()
    }

    private fun signed(method: String, name: String, body: ByteArray?, type: String?): Request {
        val url = urlOf(name)
        val hostHeader = if (url.port == HttpUrl.defaultPort(url.scheme)) url.host else "${url.host}:${url.port}"
        val path = url.pathSegments.joinToString("/", prefix = "/") { S3Signer.encodeSegment(it) }
        val hash = if (body == null) S3Signer.EMPTY_SHA256 else S3Signer.sha256Hex(body)
        val sig = S3Signer.sign(method, hostHeader, path, "", hash, region, config.accessKey.trim(), config.secretKey.trim())
        val exact = url.newBuilder().encodedPath(path).build()
        val r = Request.Builder().url(exact)
        for ((k, v) in sig.headers) r.header(k, v)
        r.method(method, body?.toRequestBody((type ?: "application/octet-stream").toMediaType()))
        return r.build()
    }

    private fun <T> call(req: Request, block: (Response) -> T): T = try {
        client.newCall(req).execute().use(block)
    } catch (e: IOException) {
        throw SyncTransport.failure(e, seen)
    }

    override fun get(name: String): Remote = call(signed("GET", name, null, null)) { r ->
        if (r.code == 404) {
            val text = try { r.body.string() } catch (_: Exception) { "" }
            if (text.contains("NoSuchBucket")) throw SyncError(SyncError.FOLDER, "NoSuchBucket")
            return@call SyncTransport.remoteOf(r, null)
        }
        SyncTransport.check(r, false)
        SyncTransport.remoteOf(r, r.body.bytes())
    }

    override fun put(name: String, bytes: ByteArray, type: String) =
        call(signed("PUT", name, bytes, type)) { SyncTransport.check(it, true) }

    override fun delete(name: String) = call(signed("DELETE", name, null, null)) { r ->
        if (r.code != 404) SyncTransport.check(r, false)
    }

    companion object {
        /** The region written in an endpoint's host name (s3.eu-central-1.amazonaws.com, s3.eu-central-003.backblazeb2.com …). */
        fun regionOf(host: String): String {
            val m = Regex("""s3[.-]([a-z]{2,}-[a-z]+-\d+)\.""").find(host)
            if (m != null) return m.groupValues[1]
            if (host.endsWith(".r2.cloudflarestorage.com")) return "auto"
            return "us-east-1"
        }
    }
}
