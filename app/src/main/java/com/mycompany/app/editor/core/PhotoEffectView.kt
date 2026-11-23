package com.mycompany.app.editor.core

import android.content.Context
import android.graphics.Bitmap
import android.media.effect.Effect
import android.media.effect.EffectContext
import android.media.effect.EffectFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.Array
import kotlin.Exception
import kotlin.Int
import kotlin.IntArray
import kotlin.String
import kotlin.Suppress
import kotlin.also
import kotlin.arrayOf
import kotlin.intArrayOf

@Suppress("deprecation")
class PhotoEffectView(context: Context?) : GLSurfaceView(context), GLSurfaceView.Renderer {
    @JvmField
    var renderer: TextureRenderer? = null
    @JvmField
    var textures: IntArray? = null
    @JvmField
    var initPending: Boolean = false
    @JvmField
    var sourceBitmap: Bitmap? = null
    @JvmField
    var imageWidth: Int = 0
    @JvmField
    var imageHeight: Int = 0
    @JvmField
    var effectContext: EffectContext? = null
    @JvmField
    var currentEffect: Effect? = null
    @JvmField
    var effectKind: Int = 0
    @JvmField
    var saveListener: PhotoSaveListener? = null

    interface PhotoSaveListener {
        fun onSaved(bitmap: Bitmap?)

        fun onFailed()
    }

    fun applyEffect() {
        val half = 0.5f
        val effect = this.currentEffect
        var factory: EffectFactory? = null
        if (effect != null) {
            effect.release()
            this.currentEffect = null
        }
        val effectKind = this.effectKind
        if (effectKind == 0 || this.imageWidth == 0 || this.imageHeight == 0) {
            return
        }
        if (effectKind != 0) {
            var effectContextCreateWithCurrentGlContext = this.effectContext
            if (effectContextCreateWithCurrentGlContext == null) {
                effectContextCreateWithCurrentGlContext = EffectContext.createWithCurrentGlContext()
                this.effectContext = effectContextCreateWithCurrentGlContext
            }
            factory = effectContextCreateWithCurrentGlContext.getFactory()
        }
        when (this.effectKind) {
            1 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.AutoFixEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("scale", half)
            }

            2 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.BrightnessEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("brightness", 2.0f)
            }

            3 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.ContrastEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("contrast", 1.4f)
            }

            4 -> this.currentEffect = factory!!.createEffect("android.media.effect.effects.CrossProcessEffect")
            5 -> this.currentEffect = factory!!.createEffect("android.media.effect.effects.DocumentaryEffect")
            6 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.DuotoneEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("first_color", -256)
                this.currentEffect!!.setParameter("second_color", -12303292)
            }

            7 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.FillLightEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("strength", 0.8f)
            }

            8 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.FisheyeEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("scale", half)
            }

            9 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.GrainEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("strength", 1.0f)
            }

            10 -> this.currentEffect = factory!!.createEffect("android.media.effect.effects.GrayscaleEffect")
            11 -> this.currentEffect = factory!!.createEffect("android.media.effect.effects.LomoishEffect")
            12 -> this.currentEffect = factory!!.createEffect("android.media.effect.effects.NegativeEffect")
            13 -> this.currentEffect = factory!!.createEffect("android.media.effect.effects.PosterizeEffect")
            14 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.SaturateEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("scale", half)
            }

            15 -> this.currentEffect = factory!!.createEffect("android.media.effect.effects.SepiaEffect")
            16 -> this.currentEffect = factory!!.createEffect("android.media.effect.effects.SharpenEffect")
            17 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.ColorTemperatureEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("scale", 0.9f)
            }

            18 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.TintEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("tint", -65281)
            }

            19 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.VignetteEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("scale", half)
            }

            20 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.FlipEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("horizontal", true)
            }

            21 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.FlipEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("vertical", true)
            }

            22 -> {
                val effectCreateEffect =
                    factory!!.createEffect("android.media.effect.effects.RotateEffect")
                this.currentEffect = effectCreateEffect
                effectCreateEffect.setParameter("angle", 180)
            }
        }
        val currentEffect = this.currentEffect
        if (currentEffect != null) {
            val textureIds = this.textures
            currentEffect.apply(textureIds!![0], this.imageWidth, this.imageHeight, textureIds[1])
        }
        val textureRenderer = this.renderer
        val texture = this.textures!![1]
        textureRenderer!!.javaClass
        GLES20.glBindFramebuffer(36160, 0)
        GLES20.glUseProgram(textureRenderer.program)
        GLToolbox.checkGlError("glUseProgram")
        GLES20.glViewport(0, 0, textureRenderer.viewWidth, textureRenderer.viewHeight)
        GLToolbox.checkGlError("glViewport")
        GLES20.glDisable(3042)
        GLES20.glVertexAttribPointer(
            textureRenderer.positionLoc,
            2,
            5126,
            false,
            0,
            textureRenderer.texVertices as Buffer?
        )
        GLES20.glEnableVertexAttribArray(textureRenderer.positionLoc)
        GLES20.glVertexAttribPointer(
            textureRenderer.texCoordLoc,
            2,
            5126,
            false,
            0,
            textureRenderer.posVertices as Buffer?
        )
        GLES20.glEnableVertexAttribArray(textureRenderer.texCoordLoc)
        GLToolbox.checkGlError("vertex attribute setup")
        GLES20.glActiveTexture(33984)
        GLToolbox.checkGlError("glActiveTexture")
        GLES20.glBindTexture(3553, texture)
        GLToolbox.checkGlError("glBindTexture")
        GLES20.glUniform1i(textureRenderer.samplerLoc, 0)
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glClear(16384)
        GLES20.glDrawArrays(5, 0, 4)
    }

    fun initTextures() {
        val programId: Int
        if (this.renderer == null || this.textures == null || !MainUtil.isBitmapOk(this.sourceBitmap) || this.effectKind == 0 || this.imageWidth == 0 || this.imageHeight == 0) {
            return
        }
        val textureRenderer = this.renderer
        textureRenderer!!.javaClass
        val vertexShader = GLToolbox.loadShader(
            35633,
            "attribute vec4 a_position;\nattribute vec2 a_texcoord;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_Position = a_position;\n  v_texcoord = a_texcoord;\n}\n"
        )
        val fragmentShader: Int = if (vertexShader == 0) 0 else GLToolbox.loadShader(
            35632,
            "precision mediump float;\nuniform sampler2D tex_sampler;\nvarying vec2 v_texcoord;\nvoid main() {\n  gl_FragColor = texture2D(tex_sampler, v_texcoord);\n}\n"
        )
        if (vertexShader == 0 || fragmentShader == 0) {
            programId = 0
        } else {
            programId = GLES20.glCreateProgram()
            if (programId != 0) {
                GLES20.glAttachShader(programId, vertexShader)
                GLToolbox.checkGlError("glAttachShader")
                GLES20.glAttachShader(programId, fragmentShader)
                GLToolbox.checkGlError("glAttachShader")
                GLES20.glLinkProgram(programId)
                val linkStatus = IntArray(1)
                GLES20.glGetProgramiv(programId, 35714, linkStatus, 0)
                if (linkStatus[0] != 1) {
                    val programLog = GLES20.glGetProgramInfoLog(programId)
                    GLES20.glDeleteProgram(programId)
                    throw RuntimeException("Could not link program: $programLog")
                    return
                }
            }
        }
        textureRenderer.program = programId
        textureRenderer.samplerLoc = GLES20.glGetUniformLocation(programId, "tex_sampler")
        textureRenderer.positionLoc = GLES20.glGetAttribLocation(textureRenderer.program, "a_texcoord")
        textureRenderer.texCoordLoc = GLES20.glGetAttribLocation(textureRenderer.program, "a_position")
        val floatBufferAsFloatBuffer =
            ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureRenderer.texVertices = floatBufferAsFloatBuffer
        floatBufferAsFloatBuffer.put(TextureRenderer.Companion.texCoords).position(0)
        val allocateDirect =
            ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer()
        textureRenderer.posVertices = allocateDirect
        allocateDirect.put(TextureRenderer.Companion.vertexCoords).position(0)
        val renderer = this.renderer
        val imageWidth = this.imageWidth
        val imageHeight = this.imageHeight
        renderer!!.texWidth = imageWidth
        renderer.texHeight = imageHeight
        renderer.computeOutputVertices()
        GLES20.glGenTextures(2, this.textures, 0)
        GLES20.glBindTexture(3553, this.textures!![0])
        GLUtils.texImage2D(3553, 0, this.sourceBitmap, 0)
        GLES20.glTexParameteri(3553, 10240, 9729)
        GLES20.glTexParameteri(3553, 10241, 9729)
        GLES20.glTexParameteri(3553, 10242, 33071)
        GLES20.glTexParameteri(3553, 10243, 33071)
    }

    override fun onDrawFrame(gl10: GL10) {
        if (this.renderer == null || this.textures == null) {
            return
        }
        try {
            if (this.initPending) {
                this.initPending = false
                initTextures()
            }
            applyEffect()
        } catch (unused: Exception) {
        }
        val listener = this.saveListener
        if (listener == null) {
            return
        }
        // a save is pending: read the frame back (GL rows are bottom-up and RGBA) into an ARGB bitmap
        var bitmap: Bitmap? = null
        try {
            val width = getWidth()
            val height = getHeight()
            val gl = IntArray(width * height)
            val argb = IntArray(width * height)
            val buffer = IntBuffer.wrap(gl)
            buffer.position(0)
            gl10.glReadPixels(0, 0, width, height, 6408, 5121, buffer)
            for (row in 0..<height) {
                val src = row * width
                val dst = (height - row - 1) * width
                for (col in 0..<width) {
                    val px = gl[src + col]
                    argb[dst + col] =
                        ((px shl 16) and 0xff0000) or (px and -0xff0100) or ((px shr 16) and 0xff)
                }
            }
            bitmap = Bitmap.createBitmap(argb, width, height, Bitmap.Config.ARGB_8888)
        } catch (unused2: OutOfMemoryError) {
            bitmap = null
        } catch (unused2: Exception) {
            bitmap = null
        }
        listener.onSaved(bitmap)
        this.saveListener = null
    }

    override fun onSurfaceChanged(gl10: GL10?, width: Int, height: Int) {
        val textureRenderer = this.renderer
        if (textureRenderer != null) {
            textureRenderer.viewWidth = width
            textureRenderer.viewHeight = height
            textureRenderer.computeOutputVertices()
        }
    }

    override fun onSurfaceCreated(gl10: GL10?, eGLConfig: EGLConfig?) {
    }

    fun setEffectType(effectKind: Int) {
        if (this.effectKind == effectKind) {
            return
        }
        this.effectKind = effectKind
        requestRender()
    }

    fun setImageBitmap(bitmap: Bitmap) {
        if (MainUtil.isBitmapOk(bitmap)) {
            this.sourceBitmap = bitmap
            this.imageWidth = bitmap.getWidth()
            this.imageHeight = this.sourceBitmap!!.getHeight()
            this.initPending = true
            requestRender()
        }
    }

    companion object {
        @JvmField
        val effectNames: Array<String?> = arrayOf<String?>(
            "ORIGINAL",
            "AUTO FIX",
            "BRIGHTNESS",
            "CONTRAST",
            "CROSS PROCESS",
            "DOCUMENTARY",
            "DUO TONE",
            "FILL LIGHT",
            "FISH EYE",
            "GRAIN",
            "GRAY SCALE",
            "LOMOISH",
            "NEGATIVE",
            "POSTERIZE",
            "SATURATE",
            "SEPIA",
            "SHARPEN",
            "TEMPERATURE",
            "TINT",
            "VIGNETTE",
            "FLIP HORIZONTAL",
            "FLIP VERTICAL",
            "ROTATE"
        )
        @JvmField
        val effectIcons: IntArray = intArrayOf(
            R.drawable.effect_original,
            R.drawable.effect_auto_fix,
            R.drawable.effect_brightness,
            R.drawable.effect_contrast,
            R.drawable.effect_cross_process,
            R.drawable.effect_documentary,
            R.drawable.effect_duo_tone,
            R.drawable.effect_fill_light,
            R.drawable.effect_fish_eye,
            R.drawable.effect_grain,
            R.drawable.effect_gray_scale,
            R.drawable.effect_lomoish,
            R.drawable.effect_negative,
            R.drawable.effect_posterize,
            R.drawable.effect_saturate,
            R.drawable.effect_sepia,
            R.drawable.effect_sharpen,
            R.drawable.effect_temperature,
            R.drawable.effect_tint,
            R.drawable.effect_vignette,
            R.drawable.effect_flip_horizontal,
            R.drawable.effect_flip_vertical,
            R.drawable.effect_rotate
        )
    }
}
