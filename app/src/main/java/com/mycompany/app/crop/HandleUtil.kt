package com.mycompany.app.crop

import kotlin.math.sqrt

object HandleUtil {
    @JvmStatic
    fun distance(x: Float, y: Float, cx: Float, cy: Float): Float {
        val dx = cx - x
        val dy = cy - y
        return sqrt(((dy * dy) + (dx * dx)).toDouble()).toFloat()
    }
}
