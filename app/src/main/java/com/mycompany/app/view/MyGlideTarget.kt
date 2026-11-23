package com.mycompany.app.view

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.Request
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target

/** A Glide target that loads at the original size and ignores the placeholder callbacks; subclasses handle the result. */
abstract class MyGlideTarget<Z : Any> : Target<Z> {
    @JvmField
    var currentRequest: Request? = null

    override fun getRequest(): Request? = this.currentRequest

    override fun setRequest(request: Request?) {
        this.currentRequest = request
    }

    override fun getSize(cb: SizeReadyCallback) {
        cb.onSizeReady(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
    }

    override fun removeCallback(cb: SizeReadyCallback) {
    }

    override fun onLoadStarted(placeholder: Drawable?) {
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
    }

    override fun onLoadCleared(placeholder: Drawable?) {
    }

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun onDestroy() {
    }
}
