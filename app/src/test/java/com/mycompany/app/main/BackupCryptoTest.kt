package com.mycompany.app.main

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.RandomAccessFile
import kotlin.random.Random

/** The password backup format (2026-11-21, release roadmap step 3): AES-GCM in 1 MiB chunks behind the OPUMBK2 header. */
class BackupCryptoTest {
    @get:Rule
    val dir = TemporaryFolder()

    private val password = "correct horse".toCharArray()

    private fun plain(size: Int): File = dir.newFile().apply { writeBytes(Random(size).nextBytes(size)) }

    private fun encrypted(src: File): File {
        val out = File(dir.root, src.name + ".bk")
        assertTrue(BackupCrypto.encrypt(src.path, out.path, password))
        return out
    }

    @Test
    fun roundTripOverSeveralChunks() {
        val src = plain((2.5 * (1 shl 20)).toInt())
        val enc = encrypted(src)
        assertTrue(BackupCrypto.isPasswordBackup(enc.path))
        assertNotEquals(src.length(), 0L)
        val back = File(dir.root, "back")
        assertEquals(BackupCrypto.OK, BackupCrypto.decrypt(enc.path, back.path, password))
        assertArrayEquals(src.readBytes(), back.readBytes())
    }

    @Test
    fun emptyAndExactChunkSizes() {
        for (size in intArrayOf(0, 1, 1 shl 20, (1 shl 20) + 1)) {
            val src = plain(size)
            val back = File(dir.root, "back$size")
            assertEquals("size $size", BackupCrypto.OK, BackupCrypto.decrypt(encrypted(src).path, back.path, password))
            assertArrayEquals("size $size", src.readBytes(), back.readBytes())
        }
    }

    @Test
    fun wrongPasswordIsReportedAsSuch() {
        val enc = encrypted(plain(5000))
        assertEquals(BackupCrypto.WRONG_PASSWORD, BackupCrypto.decrypt(enc.path, File(dir.root, "x").path, "wrong horse".toCharArray()))
    }

    @Test
    fun damagedLaterChunkFails() {
        val enc = encrypted(plain(3 shl 20))
        // flip one byte well inside the second chunk: the first chunk still opens, so this is damage, not a wrong password
        RandomAccessFile(enc, "rw").use { file ->
            val at = (1 shl 20) + 4096L
            file.seek(at); val b = file.read(); file.seek(at); file.write(b xor 0x40)
        }
        assertEquals(BackupCrypto.FAILED, BackupCrypto.decrypt(enc.path, File(dir.root, "x").path, password))
    }

    @Test
    fun truncatedBackupDoesNotRestore() {
        val enc = encrypted(plain(3 shl 20))
        // cut off the last chunk: the chunk before it is not marked last, so the end is missing
        RandomAccessFile(enc, "rw").use { it.setLength(it.length() - (1 shl 19)) }
        assertNotEquals(BackupCrypto.OK, BackupCrypto.decrypt(enc.path, File(dir.root, "x").path, password))
    }

    @Test
    fun plainFileIsNotAPasswordBackup() {
        assertFalse(BackupCrypto.isPasswordBackup(plain(100).path))
        assertFalse(BackupCrypto.isPasswordBackup(null))
        assertFalse(BackupCrypto.isPasswordBackup(File(dir.root, "missing").path))
    }
}
