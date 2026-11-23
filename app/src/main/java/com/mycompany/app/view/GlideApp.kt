package com.mycompany.app.view

import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions

/*
 * Glide's "generated API" (GlideApp / GlideRequests / GlideRequest / GlideOptions) only re-typed the standard classes:
 * the app has no Glide extensions. With the real Glide library these names are aliases of the standard API.
 */
typealias GlideRequests = RequestManager
typealias GlideRequest<T> = RequestBuilder<T>
typealias GlideOptions = RequestOptions

object GlideApp {
    @JvmStatic
    fun with(fragmentActivity: FragmentActivity?): GlideRequests? = Glide.with(fragmentActivity!!)

    /**
     * Any activity. A `FragmentActivity` ties the requests to its lifecycle; any other (a Compose screen is a
     * `ComponentActivity`) gets the app-wide request manager, which is what Glide's deprecated `with(Activity)` did.
     */
    fun with(activity: android.app.Activity?): GlideRequests? =
        if (activity is FragmentActivity) Glide.with(activity) else Glide.with(activity!!.applicationContext)
}

/**
 * Attaches a listener written for any resource type. The app's listeners only check the model, the target and the
 * error, and one listener serves the Drawable and the PictureDrawable (SVG) request of a view, so they are typed
 * `RequestListener<Any?>`; Glide's `listener()` wants the request's own type. The cast is safe: a listener gets the
 * resource as an object either way.
 */
@Suppress("UNCHECKED_CAST")
fun <T> RequestBuilder<T>.anyListener(listener: com.bumptech.glide.request.RequestListener<Any?>?): RequestBuilder<T> =
    listener(listener as com.bumptech.glide.request.RequestListener<T>?)
