package com.mycompany.app.data.sync

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * AWS Signature Version 4 for S3 requests (2026-11-28), as Amazon S3 and the S3-compatible services (Backblaze B2,
 * Cloudflare R2, Wasabi, Hetzner, MinIO, Garage …) expect it: the payload's SHA-256 goes into `x-amz-content-sha256`,
 * the request is signed with a key derived from the secret, the date, the region and "s3".
 */
object S3Signer {
    const val EMPTY_SHA256 = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"

    class Signed(val headers: Map<String, String>, val authorization: String)

    /**
     * Signs a request to [host] + [path] (path already in its final, encoded form, starting with "/").
     * [extraHeaders] are signed too (lower-case names). Returns the headers to send, including Authorization.
     */
    fun sign(
        method: String,
        host: String,
        path: String,
        query: String,
        payloadSha256: String,
        region: String,
        accessKey: String,
        secretKey: String,
        now: Date = Date(),
        extraHeaders: Map<String, String> = emptyMap(),
    ): Signed {
        val amzDate = format("yyyyMMdd'T'HHmmss'Z'", now)
        val day = amzDate.substring(0, 8)
        val headers = sortedMapOf<String, String>()
        headers["host"] = host
        headers["x-amz-content-sha256"] = payloadSha256
        headers["x-amz-date"] = amzDate
        for ((k, v) in extraHeaders) headers[k.lowercase()] = v.trim()
        val signedHeaders = headers.keys.joinToString(";")
        val canonical = buildString {
            append(method).append('\n')
            append(path).append('\n')
            append(query).append('\n')
            for ((k, v) in headers) append(k).append(':').append(v).append('\n')
            append('\n')
            append(signedHeaders).append('\n')
            append(payloadSha256)
        }
        val scope = "$day/$region/s3/aws4_request"
        val toSign = "AWS4-HMAC-SHA256\n$amzDate\n$scope\n" + hex(sha256(canonical.toByteArray(Charsets.UTF_8)))
        var key = hmac("AWS4$secretKey".toByteArray(Charsets.UTF_8), day)
        key = hmac(key, region)
        key = hmac(key, "s3")
        key = hmac(key, "aws4_request")
        val signature = hex(hmac(key, toSign))
        val auth = "AWS4-HMAC-SHA256 Credential=$accessKey/$scope,SignedHeaders=$signedHeaders,Signature=$signature"
        val out = LinkedHashMap<String, String>()
        for ((k, v) in headers) if (k != "host") out[k] = v
        out["Authorization"] = auth
        return Signed(out, auth)
    }

    /** One path segment encoded as S3 wants it (RFC 3986 unreserved characters stay). */
    fun encodeSegment(s: String): String {
        val b = StringBuilder()
        for (byte in s.toByteArray(Charsets.UTF_8)) {
            val c = byte.toInt() and 0xff
            val ch = c.toChar()
            if (ch in 'A'..'Z' || ch in 'a'..'z' || ch in '0'..'9' || ch == '-' || ch == '_' || ch == '.' || ch == '~') b.append(ch)
            else b.append('%').append("%02X".format(c))
        }
        return b.toString()
    }

    fun sha256Hex(bytes: ByteArray): String = hex(sha256(bytes))

    private fun sha256(b: ByteArray): ByteArray = MessageDigest.getInstance("SHA-256").digest(b)

    private fun hmac(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(Charsets.UTF_8))
    }

    private fun hex(b: ByteArray): String = b.joinToString("") { "%02x".format(it) }

    private fun format(pattern: String, date: Date): String =
        SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }.format(date)
}
