package com.mycompany.app.data.sync

import org.json.JSONObject
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * The encryption floccus uses when a passphrase is set (2026-11-28), so an encrypted file opens on both sides:
 * PBKDF2-SHA256 with 250,000 rounds turns the passphrase into an AES-256 key; the salt is a random 64-byte value
 * written as hex, and that hex text (its UTF-8 bytes) is the salt. The file is AES-GCM with a 16-byte IV in front of
 * the ciphertext, Base64, stored as JSON `{"ciphertext": …, "salt": …}`. Older floccus files are the bare Base64 text
 * with the file name as the salt.
 */
object FloccusCrypt {
    private const val ITERATIONS = 250_000
    private const val IV_LENGTH = 16
    private val random = SecureRandom()

    class WrongPassphrase : Exception("wrong passphrase")

    /** A derived key and the salt it belongs to; kept between reading and writing the file (the derivation is slow). */
    class Key(val salt: String, val key: SecretKey)

    fun isEncrypted(text: String): Boolean {
        val t = text.trimStart()
        return t.startsWith("{") && t.contains("\"ciphertext\"")
    }

    fun derive(passphrase: String, salt: String): Key {
        val spec = PBEKeySpec(passphrase.toCharArray(), salt.toByteArray(Charsets.UTF_8), ITERATIONS, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Key(salt, SecretKeySpec(bytes, "AES"))
    }

    fun newSalt(): String {
        val bytes = ByteArray(64)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /** The salt of an encrypted file, or [legacySalt] for the old bare format. */
    fun saltOf(text: String, legacySalt: String): String =
        if (isEncrypted(text)) JSONObject(text).getString("salt") else legacySalt

    fun decrypt(text: String, key: Key): String {
        val b64 = if (isEncrypted(text)) JSONObject(text).getString("ciphertext") else text.trim()
        val all = try { Base64.getDecoder().decode(b64) } catch (_: Exception) { throw WrongPassphrase() }
        if (all.size <= IV_LENGTH) throw WrongPassphrase()
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key.key, GCMParameterSpec(128, all, 0, IV_LENGTH))
            String(cipher.doFinal(all, IV_LENGTH, all.size - IV_LENGTH), Charsets.UTF_8)
        } catch (_: Exception) {
            throw WrongPassphrase()
        }
    }

    fun encrypt(plain: String, key: Key): String {
        val iv = ByteArray(IV_LENGTH)
        random.nextBytes(iv)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key.key, GCMParameterSpec(128, iv))
        val ct = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        val b64 = Base64.getEncoder().encodeToString(iv + ct)
        return JSONObject().put("ciphertext", b64).put("salt", key.salt).toString()
    }
}
