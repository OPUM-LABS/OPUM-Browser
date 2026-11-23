package com.mycompany.app.editor.core

import android.content.Context
import android.graphics.Bitmap
import android.view.GestureDetector
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.editor.core.PhotoDrawView.PhotoDrawListener
import com.mycompany.app.editor.core.PhotoEffectView.PhotoSaveListener
import com.mycompany.app.editor.core.PhotoTouchListener.ObjHolder
import com.mycompany.app.editor.core.PhotoTouchListener.PhotoObjectListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import ch.opumlabs.opumbrowser.R

@Suppress("deprecation")
class PhotoEditor(@JvmField var context: Context?, @JvmField var photoEditorView: PhotoEditorView?, @JvmField var listener: PhotoListener) {
    @JvmField
    var sourceImage: ImageView? = null
    @JvmField
    var brushView: PhotoDrawView? = null
    @JvmField
    var addedViews: ArrayList<View?> = ArrayList()
    @JvmField
    var redoViews: ArrayList<View?> = ArrayList()
    @JvmField
    var editMode: Byte = 0

    internal inner class DrawListener : PhotoDrawListener {
        override fun onDrawing(isDrawing: Boolean) {
            val photoEditor = this@PhotoEditor
            val editMode = photoEditor.editMode
            if (isDrawing) {
                if (editMode.toInt() != 1) {
                    photoEditor.editMode = 1.toByte()
                    photoEditor.clearSelection(null)
                    return
                }
                return
            }
            if (editMode.toInt() != 1) {
                return
            }
            photoEditor.editMode = 0.toByte()
            photoEditor.redoViews.clear()
            photoEditor.addedViews.add(photoEditor.brushView)
            photoEditor.updateUndoRedo()
        }

        override fun isDrawMode(): Boolean {
            val photoEditor = this@PhotoEditor
            val photoEditorView = photoEditor.photoEditorView
            if (photoEditorView != null) {
                val childCount = photoEditorView.getChildCount()
                for (index in 0..<childCount) {
                    val childAt = photoEditor.photoEditorView!!.getChildAt(index)
                    if (childAt != null && (childAt is FrameLayout)) {
                        val tag = childAt.getTag()
                        val holder = if (tag == null || tag !is ObjHolder) null else tag
                        val imageView: ImageView? = holder?.imageView
                        if (holder != null && imageView != null && imageView.getVisibility() == View.VISIBLE) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        override fun canDraw(): Boolean {
            return this@PhotoEditor.editMode.toInt() != 0
        }
    }

    internal class AddTextStep(@JvmField val outer: PhotoEditor,
                                         @JvmField val label: String?,
                                         @JvmField val color: Int, state: Int
    ) : Runnable {
        val holding: Boolean

        init {
            this.holding = ((state) != 0)
        }

        override fun run() {
            val photoEditor = this.outer
            val context: Context? = photoEditor.context
            if (photoEditor.photoEditorView == null || context == null) {
                return
            }
            val frameLayout = FrameLayout(context)
            val borderFrame = FrameLayout(context)
            borderFrame.setBackgroundResource(R.drawable.text_border)
            val layoutParams = FrameLayout.LayoutParams(-2, -2)
            val padding = MainUtil.dpToPx(context, 20.0f).toInt()
            layoutParams.setMarginStart(padding)
            layoutParams.topMargin = padding
            layoutParams.setMarginEnd(MainApp.dp8)
            layoutParams.bottomMargin = MainApp.dp8
            frameLayout.addView(borderFrame, layoutParams)
            val appCompatTextView = AppCompatTextView(context, null)
            appCompatTextView.setTextSize(1, 24.0f)
            appCompatTextView.setTextColor(-1)
            val closeParams = FrameLayout.LayoutParams(-2, -2)
            closeParams.setMarginStart(MainApp.dp4)
            val topMargin = MainApp.dp4
            closeParams.topMargin = topMargin
            closeParams.setMarginEnd(topMargin)
            closeParams.bottomMargin = MainApp.dp4
            borderFrame.addView(appCompatTextView, closeParams)
            val imageView = ImageView(context)
            imageView.setScaleType(ScaleType.CENTER_INSIDE)
            imageView.setImageResource(R.drawable.outline_cancel_white_24)
            imageView.setBackgroundResource(R.drawable.selector_circle_close)
            val width = MainApp.dp40
            frameLayout.addView(imageView, width, width)
            val holding = this.holding
            if (holding) {
                appCompatTextView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                appCompatTextView.setGravity(17)
                appCompatTextView.setTextSize(1, 36.0f)
            } else {
                appCompatTextView.setTextColor(this.color)
            }
            appCompatTextView.setText(this.label)
            val holder = ObjHolder()
            holder.frame = frameLayout
            holder.borderFrame = borderFrame
            holder.appCompatTextView = appCompatTextView
            holder.imageView = imageView
            frameLayout.setTag(holder)
            val contextNow = photoEditor.context
            val sourceImage = photoEditor.sourceImage
            val clearSelectionStep: ClearSelectionStep =
                ClearSelectionStep(photoEditor, if (holding) 1 else 0)
            val photoTouchListener = PhotoTouchListener()
            if (contextNow != null) {
                photoTouchListener.touchSlop = MainUtil.dpToPx(contextNow, 1.0f)
                photoTouchListener.activePointerId = -1
                photoTouchListener.deleteView = sourceImage
                photoTouchListener.heldObject = holder
                photoTouchListener.objectListener = clearSelectionStep
                photoTouchListener.scaleDetector = ScaleGestureDetector(photoTouchListener.ScaleListener())
                photoTouchListener.tapDetector =
                    GestureDetector(contextNow, photoTouchListener.AddTextViewGestures())
            }
            frameLayout.setOnTouchListener(photoTouchListener)
            imageView.setTag(frameLayout)
            imageView.setOnClickListener(photoEditor.ImageViewClick())
            photoEditor.clearSelection(null)
            photoEditor.photoEditorView!!.addView(frameLayout, com.mycompany.app.outline.LegacyViews.relativeParams(-2, -2, 13))
            photoEditor.redoViews.clear()
            photoEditor.addedViews.add(frameLayout)
            photoEditor.updateUndoRedo()
        }
    }

    internal class ClearSelectionStep(@JvmField val outer: PhotoEditor, state: Int) :
        PhotoObjectListener {
        val holding: Boolean

        init {
            this.holding = ((state) != 0)
        }

        fun onObjectHeld(objHolder: ObjHolder?, held: Boolean) {
            val photoEditor = this.outer
            if (!held) {
                photoEditor.editMode = 0.toByte()
            } else {
                if (objHolder == null || photoEditor.editMode.toInt() == 2) {
                    return
                }
                photoEditor.editMode = 2.toByte()
                photoEditor.clearSelection(objHolder.frame)
            }
        }
    }

    internal inner class ImageViewClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val tag: Any? = view?.getTag()
            if (view == null || tag == null || (tag !is FrameLayout)) {
                return
            }
            val frameLayout = tag
            val photoEditor = this@PhotoEditor
            val photoEditorView = photoEditor.photoEditorView
            if (photoEditorView == null || photoEditorView.indexOfChild(frameLayout) == -1) {
                return
            }
            photoEditor.photoEditorView!!.removeView(frameLayout)
            photoEditor.redoViews.remove(frameLayout)
            photoEditor.addedViews.add(frameLayout)
            photoEditor.updateUndoRedo()
        }
    }

    internal class ViewSnapshotScaledStep(@JvmField val outer: PhotoEditor,
                                         @JvmField val listener: PhotoSaveListener
    ) : PhotoSaveListener {

        internal class SnapshotTask(val outer: ViewSnapshotScaledStep,
                                             val listener: PhotoSaveListener
        ) : Runnable {

            internal class ViewSnapshotScaledTask(val outer: SnapshotTask,
                                                val freeMem: Long,
                                                val photoSaveListener: PhotoSaveListener
            ) : Runnable {
                override fun run() {
                    val photoEditorView = this.outer.outer.outer.photoEditorView
                    var bitmap: Bitmap? = null
                    if (photoEditorView!!.effectView != null) {
                        val bitmapD = MainUtil.viewSnapshotScaled(photoEditorView, 0, 1.0f, this.freeMem, null)
                        if (MainUtil.isBitmapOk(photoEditorView.effectBitmap)) {
                            photoEditorView.imageView!!.setImageBitmap(photoEditorView.effectBitmap)
                            photoEditorView.effectBitmap = null
                        }
                        val effectKind = photoEditorView.effectKind
                        val photoEffectView = photoEditorView.effectView
                        if (effectKind == 0) {
                            photoEffectView!!.setVisibility(View.GONE)
                        } else {
                            photoEffectView!!.setVisibility(View.VISIBLE)
                        }
                        bitmap = bitmapD
                    }
                    val bitmapOk = MainUtil.isBitmapOk(bitmap)
                    val photoSaveListener = this.photoSaveListener
                    if (bitmapOk) {
                        photoSaveListener.onSaved(bitmap)
                    } else {
                        photoSaveListener.onFailed()
                    }
                }
            }

            override fun run() {
                val photoEditor = this.outer.outer
                val freeMemory = MainUtil.freeMemory(photoEditor.context)
                val photoEditorView = photoEditor.photoEditorView
                if (photoEditorView == null) {
                    return
                }
                photoEditorView.post(ViewSnapshotScaledTask(this, freeMemory, this.listener))
            }
        }

        override fun onSaved(bitmap: Bitmap?) {
            val photoEditor = this.outer
            photoEditor.clearSelection(null)
            if (photoEditor.photoEditorView == null) {
                return
            }
            MainApp.runInBackground(photoEditor.context, ViewSnapshotScaledStep.SnapshotTask(this, this.listener))
        }

        override fun onFailed() {
            this.listener.onFailed()
        }
    }

    interface PhotoListener {
        fun onUndoRedo(canUndo: Boolean, canRedo: Boolean)

        fun addTextView(frameLayout: FrameLayout?, label: String?, color: Int)
    }

    init {
        val photoEditorView = this.photoEditorView
        if (photoEditorView != null) {
            this.sourceImage = photoEditorView.imageView
            val drawView = this.photoEditorView!!.drawView
            this.brushView = drawView
            drawView!!.setListener(DrawListener())
        }
    }

    fun addText(color: Int, type: Int, message: String?) {
        val photoEditorView = this.photoEditorView
        if (photoEditorView == null) {
            return
        }
        photoEditorView.post(AddTextStep(this, message, color, type))
    }

    fun clearSelection(frameLayout: FrameLayout?) {
        val photoEditorView = this.photoEditorView
        if (photoEditorView != null) {
            val childCount = photoEditorView.getChildCount()
            for (index in 0..<childCount) {
                val childAt = this.photoEditorView!!.getChildAt(index)
                if (childAt != null && (childAt is FrameLayout)) {
                    val child = childAt
                    if (frameLayout == null || frameLayout != child) {
                        val tag = child.getTag()
                        val holder = if (tag == null || tag !is ObjHolder) null else tag
                        if (holder != null) {
                            val borderFrame = holder.borderFrame
                            if (borderFrame != null) {
                                borderFrame.setBackgroundResource(0)
                            }
                            val imageView = holder.imageView
                            if (imageView != null) {
                                imageView.setVisibility(View.GONE)
                            }
                        }
                    }
                }
            }
        }
    }

    fun redo() {
        val size = this.redoViews.size
        if (size == 0) {
            return
        }
        val index = size - 1
        val view = this.redoViews.get(index)
        if (view is PhotoDrawView) {
            val photoDrawView = this.brushView!!
            val stack = photoDrawView.redoLines
            if (stack != null && !stack.empty()) {
                photoDrawView.drawnLines!!.push(photoDrawView.redoLines!!.pop())
                photoDrawView.invalidate()
            }
        } else {
            val childIndex = this.photoEditorView!!.indexOfChild(view)
            val photoEditorView = this.photoEditorView
            if (childIndex == -1) {
                photoEditorView!!.addView(view)
            } else {
                photoEditorView!!.removeView(view)
            }
        }
        this.redoViews.removeAt(index)
        this.addedViews.add(view)
        updateUndoRedo()
    }

    fun clearAll() {
        val arrayList = this.addedViews
        val size = arrayList.size
        var index = 0
        while (index < size) {
            val item: Any? = arrayList.get(index)
            index++
            val view = item as View?
            if (view !is PhotoDrawView) {
                this.photoEditorView!!.removeView(view)
            }
        }
        val photoDrawView = this.brushView!!
        val stack = photoDrawView.drawnLines
        if (stack != null) {
            stack.clear()
            photoDrawView.redoLines!!.clear()
            photoDrawView.invalidate()
        }
        this.addedViews.clear()
        this.redoViews.clear()
        updateUndoRedo()
        this.photoEditorView!!.setEffectType(0)
    }

    /** Saves the edited photo: re-adds the drawing layer on top, then captures the view (see PhotoEditorView$1).  */
    fun save(photoSaveListener: PhotoSaveListener) {
        val editorView = this.photoEditorView
        if (editorView == null) {
            return
        }
        val listener: ViewSnapshotScaledStep = ViewSnapshotScaledStep(this, photoSaveListener)
        if (editorView.drawView == null) {
            return
        }
        editorView.removeView(editorView.drawView!!)
        editorView.addView(editorView.drawView!!, 2, editorView.childParams(false))
        editorView.post(PhotoEditorView.SavedTask(editorView, listener))
    }

    fun updateUndoRedo() {
        val arrayList = this.addedViews
        this.listener.onUndoRedo(arrayList.size > 0, this.redoViews.size > 0)
    }

    fun undo() {
        val size = this.addedViews.size
        if (size == 0) {
            return
        }
        val index = size - 1
        val view = this.addedViews.get(index)
        if (view is PhotoDrawView) {
            val photoDrawView = this.brushView!!
            val stack = photoDrawView.drawnLines
            if (stack != null && !stack.empty()) {
                photoDrawView.redoLines!!.push(photoDrawView.drawnLines!!.pop())
                photoDrawView.invalidate()
            }
        } else {
            val childIndex = this.photoEditorView!!.indexOfChild(view)
            val photoEditorView = this.photoEditorView
            if (childIndex == -1) {
                photoEditorView!!.addView(view)
            } else {
                photoEditorView!!.removeView(view)
            }
        }
        this.addedViews.removeAt(index)
        this.redoViews.add(view)
        updateUndoRedo()
    }
}
