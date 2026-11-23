package com.mycompany.app.main

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.InputStream

@Suppress("deprecation")
object BitmapUtil {
    @JvmStatic
    fun decodeBytes(bArr: ByteArray?, size: Int): Bitmap? {
        try {
            return BitmapFactory.decodeByteArray(bArr, 0, size)
        } catch (unused: Exception) {
            return null
        } catch (unused: OutOfMemoryError) {
            return null
        }
    }

    @JvmStatic
    fun decodeBytesWith(bArr: ByteArray?, size: Int, options: BitmapFactory.Options?): Bitmap? {
        try {
            return BitmapFactory.decodeByteArray(bArr, 0, size, options)
        } catch (unused: Exception) {
            return null
        } catch (unused: OutOfMemoryError) {
            return null
        }
    }

    @JvmStatic
    fun decodeFile(path: String?): Bitmap? {
        try {
            return BitmapFactory.decodeFile(path)
        } catch (unused: Exception) {
            return null
        } catch (unused: OutOfMemoryError) {
            return null
        }
    }

    @JvmStatic
    fun decodeResource(resources: Resources?, baselineSearchYoutube: Int): Bitmap? {
        try {
            return BitmapFactory.decodeResource(resources, baselineSearchYoutube)
        } catch (unused: Exception) {
            return null
        } catch (unused: OutOfMemoryError) {
            return null
        }
    }

    @JvmStatic
    fun decodeStream(inputStream: InputStream?, options: BitmapFactory.Options?): Bitmap? {
        try {
            return BitmapFactory.decodeStream(inputStream, null, options)
        } catch (unused: Exception) {
            return null
        } catch (unused: OutOfMemoryError) {
            return null
        }
    }
}
