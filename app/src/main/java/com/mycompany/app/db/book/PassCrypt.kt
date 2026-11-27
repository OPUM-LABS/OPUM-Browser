package com.mycompany.app.db.book

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Saved passwords at rest (2026-11-27; plain text in `DbBookPass.db` before): AES-256-GCM with a key kept in the
 * Android keystore (in the phone's secure hardware where there is one), so the database file alone does not reveal them.
 *
 * The key never leaves the phone. That is why the app's own backups ([DbBookPass.exportFile], the Chrome / Firefox
 * CSV export) write the passwords opened, inside the backup that the user's backup password protects, and a restore
 * seals them again with the new phone's key ([DbBookPass.sealPlain]). For the same reason the password database is
 * not part of Android's own cloud backup and device transfer (backup_rules): there it would arrive unreadable.
 *
 * A stored value is `enc1:` + Base64(12-byte IV + ciphertext with tag). Anything else is an old plain value and is
 * read as it is (until [DbBookPass.sealPlain] seals it).
 */
object PassCrypt {
    private const val ALIAS = "opum_saved_passwords_v1"
    private const val PREFIX = "enc1:"
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128

    @Volatile private var cachedKey: SecretKey? = null

    fun isSealed(value: String?): Boolean = value != null && value.startsWith(PREFIX)

    /** [plain] sealed for storing; null or empty stays as it is. */
    fun seal(plain: String?): String? {
        if (plain.isNullOrEmpty() || isSealed(plain)) return plain
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val sealed = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        return PREFIX + Base64.encodeToString(sealed, Base64.NO_WRAP)
    }

    /**
     * The stored [value] opened: an old plain value as it is, a sealed one decrypted. Null when it cannot be opened
     * (the database came from another phone through Android's transfer, or the key is gone).
     */
    fun open(value: String?): String? {
        if (value == null || !isSealed(value)) return value
        return try {
            val bytes = Base64.decode(value.substring(PREFIX.length), Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, bytes, 0, IV_BYTES))
            String(cipher.doFinal(bytes, IV_BYTES, bytes.size - IV_BYTES), Charsets.UTF_8)
        } catch (_: Exception) {
            null
        }
    }

    @Synchronized
    private fun key(): SecretKey {
        cachedKey?.let { return it }
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val key = (store.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry)?.secretKey ?: run {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            generator.init(
                KeyGenParameterSpec.Builder(ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            generator.generateKey()
        }
        cachedKey = key
        return key
    }
}
