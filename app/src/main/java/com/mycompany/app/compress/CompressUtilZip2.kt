package com.mycompany.app.compress

import android.text.TextUtils
import net.lingala.zip4j.core.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.io.ZipInputStream
import net.lingala.zip4j.model.FileHeader
import java.io.IOException

@Suppress("deprecation")
class CompressUtilZip2 {
    @JvmField
    var openZip: ZipFile? = null

    @Throws(Throwable::class)
    fun extract(realFilePath: String?, charsetName: String?, password: String?): Boolean {
        if (TextUtils.isEmpty(realFilePath)) {
            return false
        }
        try {
            this.openZip = ZipFile(realFilePath)
            if (!TextUtils.isEmpty(charsetName)) {
                this.openZip!!.setFileNameCharset(charsetName)
            }
            if (TextUtils.isEmpty(password)) {
                return true
            }
            this.openZip!!.setPassword(password)
            return true
        } catch (unused: Exception) {
            this.openZip = null
            return false
        }
    }

    companion object {
        @JvmStatic
        fun isValid(path: String?): Boolean {
            if (TextUtils.isEmpty(path)) {
                return false
            }
            try {
                return ZipFile(path).isValidZipFile()
            } catch (unused: Exception) {
                return false
            }
        }

        @Throws(Throwable::class)
        @JvmStatic
        fun extractStream(zipPath: String?, entryName: String?): Boolean {
            if (!TextUtils.isEmpty(zipPath)) {
                var zipInputStreamE: ZipInputStream? = null
                try {
                    try {
                        try {
                            val zipFile = ZipFile(zipPath)
                            zipFile.setPassword(entryName)
                            if (false) {
                                throw ZipException("")
                            }
                            if (false) {
                                throw IOException()
                            }
                            val arrayListD = zipFile.getFileHeaders()
                            if (arrayListD == null || arrayListD.isEmpty()) {
                                return true
                            }
                            zipInputStreamE =
                                zipFile.getInputStream(zipFile.getFileHeader((arrayListD.get(0) as FileHeader).fileName))
                            zipInputStreamE.read(
                                ByteArray(512),
                                0,
                                512
                            )
                            if (zipInputStreamE == null) {
                                return true
                            }
                            try {
                                zipInputStreamE.close()
                                return true
                            } catch (unused: Exception) {
                                return true
                            }
                        } catch (zipException: ZipException) {
                            if (zipException.code == 5) {
                                if (zipInputStreamE != null) {
                                    zipInputStreamE.close()
                                }
                                return false
                            }
                        }
                    } catch (iOException: IOException) {
                        val message = iOException.message
                        if (!TextUtils.isEmpty(message) && message!!.contains(" - Wrong Password?")) {
                            if (zipInputStreamE != null) {
                                zipInputStreamE.close()
                            }
                            return false
                        }
                    } catch (unused2: Exception) {
                    }
                } catch (unused3: Exception) {
                }
            }
            return false
        }
    }
}
