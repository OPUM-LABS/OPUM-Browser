package com.mycompany.app.view

import android.content.Context
import android.graphics.Canvas
import android.widget.ImageView
import com.mycompany.app.image.ImageSizeListener

@Suppress("deprecation")
class MySizeImage(context: Context?) : ImageView(context) {
    @JvmField
    var sizeCallback: ImageSizeListener? = null

    public override fun onDraw(canvas: Canvas) {
        try {
            super.onDraw(canvas)
        } catch (unused: Exception) {
        }
    }

    public override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        val imageSizeListener = this.sizeCallback
        if (imageSizeListener != null) {
            imageSizeListener.onSize(this, width, height)
        }
    }

    fun setListener(imageSizeListener: ImageSizeListener?) {
        this.sizeCallback = imageSizeListener
    }
}
