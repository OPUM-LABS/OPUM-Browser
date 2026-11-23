package com.mycompany.app.editor.core

import android.opengl.GLES20

@Suppress("deprecation")
object GLToolbox {
    @JvmStatic
    fun checkGlError(operation: String?) {
        val error = GLES20.glGetError()
        if (error == 0) {
            return
        }
        throw RuntimeException(operation + ": glError " + error)
    }

    @JvmStatic
    fun loadShader(shaderType: Int, source: String?): Int {
        val shaderId = GLES20.glCreateShader(shaderType)
        if (shaderId != 0) {
            GLES20.glShaderSource(shaderId, source)
            GLES20.glCompileShader(shaderId)
            val compiled = IntArray(1)
            GLES20.glGetShaderiv(shaderId, 35713, compiled, 0)
            if (compiled[0] == 0) {
                val shaderLog = GLES20.glGetShaderInfoLog(shaderId)
                GLES20.glDeleteShader(shaderId)
                throw RuntimeException("Could not compile shader " + shaderType + ":" + shaderLog)
            }
        }
        return shaderId
    }
}
