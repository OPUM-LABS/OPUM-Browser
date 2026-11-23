package com.mycompany.app.main

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.security.SecureRandom
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Backup files protected by a password the user chooses (replaces the built-in archive key).
 *
 * Layout: "OPUMBK2\n" | version (1 byte) | PBKDF2 iterations (4 bytes, big-endian) | salt (16) | base nonce (12) |
 * chunks. The key comes from PBKDF2-HMAC-SHA256 over the password and salt.
 *
 * The zip is encrypted in chunks of [CHUNK] bytes, each one AES-256-GCM on its own (ciphertext + 16-byte tag), so
 * neither side ever holds more than one chunk in memory (Android's GCM buffers everything up to the tag check).
 * Chunk i uses the base nonce with i XOR-ed into bytes 7..10 and 1 XOR-ed into byte 11 for the last chunk, and the
 * 41-byte header as associated data. So a changed header, reordered, dropped or appended chunks all fail a tag check.
 */
object BackupCrypto {
    private val MAGIC = "OPUMBK2\n".toByteArray(Charsets.US_ASCII)
    private const val VERSION: Byte = 2
    private const val ITERATIONS = 210_000
    private const val SALT_LEN = 16
    private const val IV_LEN = 12
    private const val TAG_LEN = 16
    private const val HEADER_LEN = 8 + 1 + 4 + SALT_LEN + IV_LEN
    private const val CHUNK = 1 shl 20

    /** Smallest password the save screen accepts. */
    const val MIN_PASSWORD = 6

    const val OK = 0
    const val WRONG_PASSWORD = 1
    const val FAILED = 2

    /** True when [path] starts with the password-backup header. */
    @JvmStatic
    fun isPasswordBackup(path: String?): Boolean {
        if (path.isNullOrEmpty()) return false
        return try {
            FileInputStream(path).use { input ->
                val head = ByteArray(MAGIC.size)
                readUpTo(input, head) == MAGIC.size && head.contentEquals(MAGIC)
            }
        } catch (exception: Exception) {
            false
        }
    }

    private fun key(password: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, iterations, 256)
        try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            return SecretKeySpec(bytes, "AES")
        } finally {
            spec.clearPassword()
        }
    }

    private fun nonce(base: ByteArray, index: Int, last: Boolean): GCMParameterSpec {
        val n = base.copyOf()
        n[7] = (n[7].toInt() xor (index ushr 24)).toByte()
        n[8] = (n[8].toInt() xor (index ushr 16)).toByte()
        n[9] = (n[9].toInt() xor (index ushr 8)).toByte()
        n[10] = (n[10].toInt() xor index).toByte()
        if (last) n[11] = (n[11].toInt() xor 1).toByte()
        return GCMParameterSpec(TAG_LEN * 8, n)
    }

    /** Reads until [buf] is full or the stream ends; returns the byte count. */
    private fun readUpTo(input: InputStream, buf: ByteArray): Int {
        var n = 0
        while (n < buf.size) {
            val r = input.read(buf, n, buf.size - n)
            if (r < 0) break
            n += r
        }
        return n
    }

    /** Encrypts the file [src] into [dst]. */
    @JvmStatic
    fun encrypt(src: String, dst: String, password: CharArray): Boolean {
        return try {
            val random = SecureRandom()
            val salt = ByteArray(SALT_LEN).also { random.nextBytes(it) }
            val base = ByteArray(IV_LEN).also { random.nextBytes(it) }
            val header = java.io.ByteArrayOutputStream(HEADER_LEN).apply {
                write(MAGIC)
                write(VERSION.toInt())
                write(ITERATIONS ushr 24); write(ITERATIONS ushr 16); write(ITERATIONS ushr 8); write(ITERATIONS)
                write(salt)
                write(base)
            }.toByteArray()
            val key = key(password, salt, ITERATIONS)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            BufferedInputStream(FileInputStream(src)).use { input ->
                BufferedOutputStream(FileOutputStream(dst)).use { out ->
                    out.write(header)
                    var cur = ByteArray(CHUNK)
                    var next = ByteArray(CHUNK)
                    var curLen = readUpTo(input, cur)
                    var index = 0
                    while (true) {
                        // A chunk is the last one when nothing follows it (only an empty file gives an empty chunk).
                        val nextLen = if (curLen == CHUNK) readUpTo(input, next) else 0
                        val last = nextLen == 0
                        cipher.init(Cipher.ENCRYPT_MODE, key, nonce(base, index, last))
                        cipher.updateAAD(header)
                        out.write(cipher.doFinal(cur, 0, curLen))
                        if (last) break
                        val following = cur; cur = next; next = following
                        curLen = nextLen
                        index++
                    }
                }
            }
            true
        } catch (exception: Exception) {
            File(dst).delete()
            false
        }
    }

    /** Decrypts [src] into [dst]; returns [OK], [WRONG_PASSWORD] or [FAILED]. */
    @JvmStatic
    fun decrypt(src: String, dst: String, password: CharArray): Int {
        var index = 0
        return try {
            BufferedInputStream(FileInputStream(src)).use { input ->
                val header = ByteArray(HEADER_LEN)
                if (readUpTo(input, header) != HEADER_LEN) return FAILED
                if (!header.copyOfRange(0, MAGIC.size).contentEquals(MAGIC) || header[MAGIC.size] != VERSION) {
                    return FAILED
                }
                var p = MAGIC.size + 1
                val iterations = ((header[p].toInt() and 255) shl 24) or ((header[p + 1].toInt() and 255) shl 16) or
                    ((header[p + 2].toInt() and 255) shl 8) or (header[p + 3].toInt() and 255)
                p += 4
                if (iterations < 10_000 || iterations > 10_000_000) return FAILED
                val salt = header.copyOfRange(p, p + SALT_LEN); p += SALT_LEN
                val base = header.copyOfRange(p, p + IV_LEN)
                val key = key(password, salt, iterations)
                val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                val complete = BufferedOutputStream(FileOutputStream(dst)).use { out ->
                    var cur = ByteArray(CHUNK + TAG_LEN)
                    var next = ByteArray(CHUNK + TAG_LEN)
                    var curLen = readUpTo(input, cur)
                    var done = false
                    while (curLen >= TAG_LEN) {
                        val nextLen = if (curLen == cur.size) readUpTo(input, next) else 0
                        val last = nextLen == 0
                        out.write(openChunk(cipher, key, header, base, index, last, cur, curLen))
                        if (last) {
                            done = true
                            break
                        }
                        val following = cur; cur = next; next = following
                        curLen = nextLen
                        index++
                    }
                    done
                }
                if (complete) OK else {
                    File(dst).delete()
                    FAILED
                }
            }
        } catch (exception: Exception) {
            File(dst).delete()
            // Only the first chunk tells a wrong password apart; a later tag failure means a damaged file.
            if (index == 0 && badTag(exception)) WRONG_PASSWORD else FAILED
        }
    }

    private class CutFile : Exception()

    private fun openChunk(
        cipher: Cipher, key: SecretKeySpec, header: ByteArray, base: ByteArray,
        index: Int, last: Boolean, buf: ByteArray, len: Int
    ): ByteArray {
        try {
            cipher.init(Cipher.DECRYPT_MODE, key, nonce(base, index, last))
            cipher.updateAAD(header)
            return cipher.doFinal(buf, 0, len)
        } catch (exception: Exception) {
            // The first chunk failing as the last one could be a file cut after it rather than a wrong password:
            // when it opens as a middle chunk, the password is right and the file is short.
            if (index == 0 && last && badTag(exception)) {
                val cut = try {
                    cipher.init(Cipher.DECRYPT_MODE, key, nonce(base, 0, false))
                    cipher.updateAAD(header)
                    cipher.doFinal(buf, 0, len)
                    true
                } catch (exception2: Exception) {
                    false
                }
                if (cut) throw CutFile()
            }
            throw exception
        }
    }

    private fun badTag(throwable: Throwable?): Boolean {
        var t = throwable
        while (t != null) {
            if (t is AEADBadTagException) return true
            if (t.javaClass.simpleName.contains("BadTag")) return true
            t = t.cause
        }
        return false
    }
}
