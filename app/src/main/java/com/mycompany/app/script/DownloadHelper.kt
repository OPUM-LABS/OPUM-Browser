package com.mycompany.app.script

import android.text.TextUtils
import android.webkit.URLUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

@Suppress("deprecation")
object DownloadHelper {
    /**
     * GETs url (5 s read timeout); returns the body and the Content-Encoding header (used as the charset), or null.
     * On an I/O error the error stream is drained and null returned.
     */
    @JvmStatic
    fun download(url: String?): DownloadData? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        var conn: HttpURLConnection? = null
        try {
            conn = URL(url).openConnection() as HttpURLConnection?
            conn!!.setRequestMethod("GET")
            conn.setDoOutput(false)
            conn.setDoInput(true)
            conn.setUseCaches(false)
            conn.setReadTimeout(5000)
            conn.connect()
            if (conn.getResponseCode() != 200) {
                throw Exception("HTTP Response: " + conn.getResponseCode())
            }
            val `in` = conn.getInputStream()
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(4096)
            val encoding = conn.getContentEncoding()
            var read: Int
            while ((`in`.read(buffer).also { read = it }) != -1) {
                out.write(buffer, 0, read)
            }
            val bytes = out.toByteArray()
            `in`.close()
            val data = DownloadData()
            data.bytes = bytes
            data.encoding = encoding
            try {
                conn.disconnect()
            } catch (ignored: Exception) {
            }
            return data
        } catch (exception: Exception) {
            if (exception is IOException && conn != null) {
                var reader: UnicodeReader? = null
                try {
                    val err = conn.getErrorStream()
                    if (err != null) {
                        reader = UnicodeReader(err, null)
                        val chars = CharArray(4096)
                        val ignored = StringBuilder()
                        var count: Int
                        while ((reader.reader!!.read(chars, 0, 4096).also { count = it }) != -1) {
                            if (count > 0) {
                                ignored.append(chars, 0, count)
                            }
                        }
                    }
                } catch (ignored: Exception) {
                }
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (ignored: Exception) {
                    }
                }
            }
            if (conn != null) {
                try {
                    conn.disconnect()
                } catch (ignored: Exception) {
                }
            }
            return null
        }
    }

    @JvmStatic
    fun resolveUrl(relative: String?, url: String?): String? {
        if (TextUtils.isEmpty(relative)) {
            return null
        }
        try {
            return URL(if (URLUtil.isNetworkUrl(url)) URL(url) else null, relative).toURI().toString()
        } catch (unused: Exception) {
            return null
        }
    }

    class DownloadData {
        @JvmField
        var bytes: ByteArray? = null
        @JvmField
        var encoding: String? = null
    }
}
