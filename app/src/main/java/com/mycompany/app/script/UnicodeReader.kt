package com.mycompany.app.script

import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.PushbackInputStream
import java.io.Reader

@Suppress("deprecation")
class UnicodeReader(inputStream: InputStream?, defaultEncoding: String?) : Reader() {
    @JvmField
    var reader: InputStreamReader? = null

    init {
        val str2: String?
        val i: Int
        val bArr = ByteArray(4)
        val pushbackInputStream = PushbackInputStream(inputStream, 4)
        val i2 = pushbackInputStream.read(bArr, 0, 4)
        val b = bArr[0]
        if (b.toInt() == -17 && bArr[1].toInt() == -69 && bArr[2].toInt() == -65) {
            i = i2 - 3
            str2 = "UTF-8"
        } else if (b.toInt() == -2 && bArr[1].toInt() == -1) {
            i = i2 - 2
            str2 = "UTF-16BE"
        } else if (b.toInt() == -1 && bArr[1].toInt() == -2) {
            i = i2 - 2
            str2 = "UTF-16LE"
        } else if (b.toInt() == 0 && bArr[1].toInt() == 0 && bArr[2].toInt() == -2 && bArr[3].toInt() == -1) {
            i = i2 - 4
            str2 = "UTF-32BE"
        } else if (b.toInt() == -1 && bArr[1].toInt() == -2 && bArr[2].toInt() == 0 && bArr[3].toInt() == 0) {
            i = i2 - 4
            str2 = "UTF-32LE"
        } else {
            str2 = defaultEncoding
            i = i2
        }
        if (i > 0) {
            pushbackInputStream.unread(bArr, i2 - i, i)
        } else if (i < -1) {
            pushbackInputStream.unread(bArr, 0, 0)
        }
        if (str2 == null) {
            this.reader = InputStreamReader(pushbackInputStream)
        } else {
            this.reader = InputStreamReader(pushbackInputStream, str2)
        }
    }

    @Throws(IOException::class)  // java.io.Reader, java.io.Closeable, java.lang.AutoCloseable
    override fun close() {
        this.reader!!.close()
    }

    @Throws(IOException::class)
    override fun read(buffer: CharArray?, offset: Int, length: Int): Int {
        return this.reader!!.read(buffer, offset, length)
    }
}
