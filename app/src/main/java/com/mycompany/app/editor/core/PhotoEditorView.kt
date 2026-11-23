package com.mycompany.app.editor.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import com.mycompany.app.editor.core.PhotoEffectView.PhotoSaveListener
import com.mycompany.app.main.MainUtil

@Suppress("deprecation")
class PhotoEditorView(context: Context?) : RelativeLayout(context) {
    @JvmField
    var imageViewId: Int = 0
    @JvmField
    var imageView: ImageView? = null
    @JvmField
    var effectView: PhotoEffectView? = null
    @JvmField
    var drawView: PhotoDrawView? = null
    @JvmField
    var effectKind: Int = 0
    @JvmField
    var effectBitmap: Bitmap? = null

    internal class SavedTask(@JvmField val outer: PhotoEditorView,
                                         @JvmField val photoSaveListener: PhotoSaveListener
    ) : Runnable {

        internal class SaveListener(val outer: SavedTask,
                                    val listener: PhotoSaveListener
        ) : PhotoSaveListener {

            internal class ShowSavedStep(val outer: SaveListener,
                                                val bitmap: Bitmap?,
                                                val listener: PhotoSaveListener
            ) : Runnable {

                internal class HideEffectStep(val outer: ShowSavedStep,
                                                    val listener: PhotoSaveListener
                ) : Runnable {
                    override fun run() {
                        val photoEffectView = this.outer.outer.outer.outer.effectView
                        if (photoEffectView == null) {
                            return
                        }
                        photoEffectView.setVisibility(View.GONE)
                        (this.listener as PhotoEditor.ViewSnapshotScaledStep).onSaved(null)
                    }
                }

                override fun run() {
                    val parentStep = this.outer.outer
                    val photoEditorView = parentStep.outer
                    if (photoEditorView.imageView == null) {
                        return
                    }
                    photoEditorView.effectBitmap = photoEditorView.imageBitmap
                    parentStep.outer.imageView!!.setImageBitmap(this.bitmap)
                    parentStep.outer.imageView!!.post(HideEffectStep(this, this.listener))
                }
            }

            override fun onSaved(bitmap: Bitmap?) {
                val imageView = this.outer.outer.imageView
                if (imageView == null) {
                    return
                }
                imageView.post(ShowSavedStep(this, bitmap, this.listener))
            }

            override fun onFailed() {
                (this.listener as PhotoEditor.ViewSnapshotScaledStep).onFailed()
            }
        }

        override fun run() {
            val photoEditorView = this.outer
            val photoEffectView = photoEditorView.effectView
            if (photoEffectView != null) {
                val effectKind = photoEditorView.effectKind
                val photoSaveListener = this.photoSaveListener
                if (effectKind == 0) {
                    (photoSaveListener as PhotoEditor.ViewSnapshotScaledStep).onSaved(null)
                    return
                }
                val saveListener = SaveListener(this, photoSaveListener)
                if (photoEffectView.saveListener != null) {
                    return
                }
                photoEffectView.saveListener = saveListener
                photoEffectView.requestRender()
            }
        }
    }

    var imageBitmap: Bitmap?

        get() {
            val imageView = this.imageView
            val drawable: Drawable? = imageView?.getDrawable()
            if (drawable == null || (drawable !is BitmapDrawable)) {
                return null
            }
            return drawable.getBitmap()
        }
        set(bitmap) {
            if (this.imageView == null) {
                return
            }
            if (bitmap != null && MainUtil.isBitmapOk(bitmap)) {
                val view = getParent() as View?
                var z = false
                if (view != null) {
                    val measuredWidth = view.getMeasuredWidth()
                    val measuredHeight = view.getMeasuredHeight()
                    if (measuredWidth > 0 && measuredHeight > 0 && bitmap.getHeight() / bitmap.getWidth() > measuredHeight / measuredWidth) {
                        z = true
                    }
                }
                val layoutParams = getLayoutParams()
                if (layoutParams != null) {
                    if (z) {
                        layoutParams.width = -2
                        layoutParams.height = -1
                    } else {
                        layoutParams.width = -1
                        layoutParams.height = -2
                    }
                }
                val layoutParams2 = this.imageView!!.getLayoutParams()
                if (layoutParams2 != null) {
                    if (z) {
                        layoutParams2.width = -2
                        layoutParams2.height = -1
                    } else {
                        layoutParams2.width = -1
                        layoutParams2.height = -2
                    }
                }
            }
            this.imageView!!.setImageBitmap(bitmap)
            this.effectView!!.setImageBitmap(bitmap!!)
        }

    fun childParams(wrap: Boolean): LayoutParams {
        val layoutParamsC = com.mycompany.app.outline.LegacyViews.relativeParams(-1, -2, 13)
        if (!wrap) {
            layoutParamsC.addRule(18, this.imageViewId)
            layoutParamsC.addRule(19, this.imageViewId)
            layoutParamsC.addRule(6, this.imageViewId)
            layoutParamsC.addRule(8, this.imageViewId)
        }
        return layoutParamsC
    }

    fun setEffectType(effectKind: Int) {
        val photoEffectView = this.effectView
        if (photoEffectView == null || this.effectKind == effectKind) {
            return
        }
        this.effectKind = effectKind
        photoEffectView.setEffectType(effectKind)
        val effectKindNow = this.effectKind
        val effectView = this.effectView!!
        if (effectKindNow == 0) {
            effectView.setVisibility(View.GONE)
        } else {
            effectView.setVisibility(View.VISIBLE)
        }
    }
}
