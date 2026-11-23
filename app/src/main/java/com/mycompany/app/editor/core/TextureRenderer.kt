package com.mycompany.app.editor.core

import java.nio.FloatBuffer

@Suppress("deprecation")
class TextureRenderer {
    @JvmField
    var program: Int = 0
    @JvmField
    var samplerLoc: Int = 0
    @JvmField
    var positionLoc: Int = 0
    @JvmField
    var texCoordLoc: Int = 0
    @JvmField
    var texVertices: FloatBuffer? = null
    @JvmField
    var posVertices: FloatBuffer? = null
    @JvmField
    var viewWidth: Int = 0
    @JvmField
    var viewHeight: Int = 0
    @JvmField
    var texWidth: Int = 0
    @JvmField
    var texHeight: Int = 0

    fun computeOutputVertices() {
        val bottom: Float
        val floatBuffer = this.posVertices
        if (floatBuffer == null) {
            return
        }
        var ratio = (this.viewWidth.toFloat() / this.viewHeight) / (this.texWidth.toFloat() / this.texHeight)
        var right = 1.0f
        var left = -1.0f
        if (ratio > 1.0f) {
            val scaledLeft = (-1.0f) / ratio
            val scaledRight = 1.0f / ratio
            ratio = 1.0f
            right = scaledRight
            left = scaledLeft
            bottom = -1.0f
        } else {
            bottom = -ratio
        }
        floatBuffer.put(floatArrayOf(left, bottom, right, bottom, left, ratio, right, ratio)).position(0)
    }

    companion object {
        @JvmField
        val texCoords: FloatArray = floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f)
        @JvmField
        val vertexCoords: FloatArray = floatArrayOf(-1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f)
    }
}
