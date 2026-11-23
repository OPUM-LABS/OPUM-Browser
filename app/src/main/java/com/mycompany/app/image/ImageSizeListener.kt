package com.mycompany.app.image

import android.view.View

@Suppress("deprecation")
interface ImageSizeListener {
    fun onSize(view: View?, width: Int, height: Int)
}
