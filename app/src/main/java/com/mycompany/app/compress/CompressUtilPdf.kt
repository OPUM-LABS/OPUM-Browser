package com.mycompany.app.compress

import android.content.Context
import com.mycompany.app.pref.store.PdfSettings
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.text.TextUtils
import com.mycompany.app.data.DataPdf
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefPdf
import java.io.File
import java.io.InputStream
import kotlin.math.min

@Suppress("deprecation")
class CompressUtilPdf(context: Context?, path: String?, referer: String?) : Compress(context, path, referer) {
    override fun load() {
        this.entryTotal = 0
        val pdfRendererY = openRenderer()
        if (pdfRendererY == null) {
            return
        }
        try {
            this.entryTotal = pdfRendererY.getPageCount()
        } catch (unused: Exception) {
        }
        closeRenderer(pdfRendererY, null)
    }

    fun renderPage(index: Int, entryTotal: Int, full: Boolean): Bitmap? {
        var pageOpenPage: PdfRenderer.Page?
        var bitmapCreateBitmap: Bitmap?
        val pdfRendererY: PdfRenderer? = if (index >= 0 && (entryTotal <= 0 || index < entryTotal)) openRenderer() else null
        if (pdfRendererY != null) {
            if (entryTotal == 0) {
                try {
                    if (index >= pdfRendererY.getPageCount()) {
                        Companion.closeRenderer(pdfRendererY, null)
                        return null
                    }
                } catch (unused: Exception) {
                    Companion.closeRenderer(pdfRendererY, null)
                    return null
                }
            }
            try {
                pageOpenPage = pdfRendererY.openPage(index)
            } catch (unused2: Exception) {
                pageOpenPage = null
            }
            if (pageOpenPage == null) {
                Companion.closeRenderer(pdfRendererY, null)
                return null
            }
            val width = pageOpenPage.getWidth()
            val height = pageOpenPage.getHeight()
            if (width == 0 || height == 0) {
                Companion.closeRenderer(pdfRendererY, null)
                return null
            }
            var scale = 1.0f
            try {
                if (full) {
                    val dp48Px = MainApp.dp48
                    if (width > dp48Px || height > dp48Px) {
                        val thumbSize = dp48Px.toFloat()
                        scale = min(thumbSize / width, thumbSize / height)
                    }
                } else {
                    val pointP = MainUtil.screenPoint(this.context)
                    if (pointP != null) {
                        scale = (min(pointP.x, pointP.y) / min(width, height)).toFloat()
                    } else {
                        val maxTexture = MainUtil.maxTextureSize()
                        if (width > maxTexture || height > maxTexture) {
                            val maxSize = maxTexture.toFloat()
                            scale = min(maxSize / width, maxSize / height)
                        }
                    }
                }
                bitmapCreateBitmap = Bitmap.createBitmap(
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    Bitmap.Config.ARGB_8888
                )
            } catch (unused3: Exception) {
                bitmapCreateBitmap = null
            } catch (unused3: OutOfMemoryError) {
                bitmapCreateBitmap = null
            }
            if (!MainUtil.isBitmapOk(bitmapCreateBitmap)) {
                Companion.closeRenderer(pdfRendererY, pageOpenPage)
                return null
            }
            try {
                bitmapCreateBitmap!!.eraseColor(-1)
                pageOpenPage.render(bitmapCreateBitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            } catch (unused4: Exception) {
            }
            Companion.closeRenderer(pdfRendererY, pageOpenPage)
            var rectW: Rect? = if (full || !PdfSettings.crop.value) null else contentRect(bitmapCreateBitmap, 0, 0, 0, 0)
            if (full || !PdfSettings.crop.value || rectW == null) {
                return bitmapCreateBitmap
            }
            val contentBounds: Rect? =
                contentRect(bitmapCreateBitmap, rectW.left, rectW.top, rectW.right, rectW.bottom)
            if (contentBounds != null) {
                rectW = contentBounds
            }
            try {
                val bitmapWidth = bitmapCreateBitmap!!.getWidth()
                val bitmapHeight = bitmapCreateBitmap.getHeight()
                var cropWidth = bitmapWidth - (rectW.left + rectW.right)
                var cropHeight = bitmapHeight - (rectW.top + rectW.bottom)
                if (width > height) {
                    while (cropWidth <= cropHeight) {
                        var left = rectW.left
                        if (left > 0) {
                            left--
                            rectW.left = left
                        } else if (rectW.right <= 0) {
                            break
                        }
                        var right = rectW.right
                        if (right > 0) {
                            right--
                            rectW.right = right
                        }
                        cropWidth = bitmapWidth - (left + right)
                    }
                } else {
                    while (cropWidth > cropHeight) {
                        var top = rectW.top
                        if (top > 0) {
                            top--
                            rectW.top = top
                        } else if (rectW.bottom <= 0) {
                            break
                        }
                        var bottom = rectW.bottom
                        if (bottom > 0) {
                            bottom--
                            rectW.bottom = bottom
                        }
                        cropHeight = bitmapHeight - (top + bottom)
                    }
                }
                return Bitmap.createBitmap(bitmapCreateBitmap, rectW.left, rectW.top, cropWidth, cropHeight)
            } catch (unused5: Exception) {
            } catch (unused5: OutOfMemoryError) {
            }
        }
        return null
    }

    fun openRenderer(): PdfRenderer? {
        if (TextUtils.isEmpty(this.path)) {
            return null
        }
        if (TextUtils.isEmpty(this.realFilePath)) {
            this.realFilePath = CompressUtil.realPath(this.context, this.path)
        }
        try {
            return PdfRenderer(ParcelFileDescriptor.open(File(this.realFilePath!!), 268435456))
        } catch (unused: Exception) {
            return null
        }
    }

    override fun albumCount(): Int {
        val context = this.context
        if (context == null) {
            return 0
        }
        return DataPdf.instance(context).pathCount()
    }

    override fun albumIndexOf(name: String?): Int {
        val context = this.context
        if (context == null) {
            return 0
        }
        return DataPdf.instance(context).indexOfPath(name)
    }

    override fun albumItemAt(index: Int): ChildItem? {
        val context = this.context
        if (context == null) {
            return null
        }
        return DataPdf.instance(context).itemAt(index)
    }

    override fun albumItems(): MutableList<ChildItem?>? {
        val context = this.context
        if (context == null) {
            return null
        }
        return DataPdf.instance(context).items
    }

    override fun indexOfName(name: String?): Int {
        val length = if (TextUtils.isEmpty(name) || TextUtils.isEmpty(this.path)) 0 else (this.path!!.length + 1)
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(this.path) || length >= name!!.length) {
            return -1
        }
        return MainUtil.parseIntSafe(name.substring(length))
    }

    override fun nameAt(index: Int): String? {
        if (TextUtils.isEmpty(this.path)) {
            return null
        }
        return this.path + "/" + index
    }

    override fun streamFor(name: String?): InputStream? {
        return MainUtil.bitmapToStream(renderPage(indexOfName(name), this.entryTotal, false))
    }

    override fun bitmapAt(index: Int): Bitmap? {
        return renderPage(index, this.entryTotal, true)
    }

    companion object {
        @JvmStatic
        fun closeRenderer(pdfRenderer: PdfRenderer, page: PdfRenderer.Page?) {
            if (page != null) {
                try {
                    page.close()
                } catch (unused: Exception) {
                }
            }
            try {
                pdfRenderer.close()
            } catch (unused2: Exception) {
            }
        }

        @JvmStatic
        fun contentRect(bitmap: Bitmap?, left: Int, top: Int, right: Int, bottom: Int): Rect? {
            if (!MainUtil.isBitmapOk(bitmap)) {
                return null
            }
            try {
                val width = bitmap!!.getWidth()
                val height = bitmap.getHeight()
                val pixel = bitmap.getPixel(left, top)
                var c = 16.toChar()
                var index = left
                var leftMargin = index
                var stepY = 16
                loop0@ while (index < width / 2) {
                    var rowY = stepY + top
                    while (rowY < height - bottom) {
                        if (pixel != bitmap.getPixel(index, rowY)) {
                            break@loop0
                        }
                        rowY += 16
                    }
                    stepY = (stepY + 2) / 16
                    leftMargin++
                    index++
                }
                val topPixel = bitmap.getPixel(left, top)
                var topY = top
                var topMargin = topY
                var stepX = 16
                loop2@ while (topY < height / 2) {
                    var colX = stepX + left
                    while (colX < width - right) {
                        if (topPixel != bitmap.getPixel(colX, topY)) {
                            break@loop2
                        }
                        colX += 16
                    }
                    stepX = (stepX + 2) / 16
                    topMargin++
                    topY++
                }
                var rightValue = (width - 1) - right
                val rightPixel = bitmap.getPixel(rightValue, top)
                var rightMargin = right
                var rightStepY = 16
                loop4@ while (rightValue > width / 2) {
                    var rightRowY = rightStepY + top
                    while (rightRowY < height - bottom) {
                        if (rightPixel != bitmap.getPixel(rightValue, rightRowY)) {
                            break@loop4
                        }
                        rightRowY += 16
                    }
                    rightStepY = (rightStepY + 2) / 16
                    rightMargin++
                    rightValue--
                }
                var bottomValue = (height - 1) - bottom
                val bottomPixel = bitmap.getPixel(left, bottomValue)
                var bottomMargin = bottom
                var bottomStepX = 16
                loop6@ while (bottomValue > height / 2) {
                    val c2 = c
                    var bottomColX = bottomStepX + left
                    while (bottomColX < width - right) {
                        if (bottomPixel != bitmap.getPixel(bottomColX, bottomValue)) {
                            break@loop6
                        }
                        bottomColX += 16
                    }
                    bottomStepX = (bottomStepX + 2) / 16
                    bottomMargin++
                    bottomValue--
                    c = c2
                }
                if (leftMargin == left && topMargin == top && rightMargin == right && bottomMargin == bottom) {
                    return null
                }
                val cropHeight = height - (topMargin + bottomMargin)
                if ((width - (leftMargin + rightMargin)) + leftMargin > width || cropHeight + topMargin > height) {
                    return null
                }
                return Rect(leftMargin, topMargin, rightMargin, bottomMargin)
            } catch (unused: Exception) {
                return null
            }
        }
    }
}
