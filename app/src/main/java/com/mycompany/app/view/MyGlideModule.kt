package com.mycompany.app.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.Registry
import com.bumptech.glide.module.AppGlideModule
import com.caverock.androidsvg.SVG
import com.mycompany.app.glide.SvgBitmapDecoder
import com.mycompany.app.glide.SvgDecoder
import com.mycompany.app.glide.SvgTranscoder
import com.mycompany.app.glide.ViewItemLoader
import com.mycompany.app.main.MainItem
import java.io.InputStream

/**
 * The app's Glide module: SVG decoding (as `PictureDrawable`, and as Bitmap for thumbnails) and the app's image items
 * ([ViewItemLoader]). Registered through `com.bumptech.glide.GeneratedAppGlideModuleImpl`.
 */
class MyGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.register(SVG::class.java, PictureDrawable::class.java, SvgTranscoder())
        registry.append(InputStream::class.java, SVG::class.java, SvgDecoder())
        registry.prepend(Registry.BUCKET_BITMAP, InputStream::class.java, Bitmap::class.java, SvgBitmapDecoder(glide.bitmapPool))
        registry.prepend(MainItem.ViewItem::class.java, InputStream::class.java, ViewItemLoader.Factory(context))
    }

    // the old bundle had this inlined (`!(this instanceof MyGlideModule)`): no manifest modules
    override fun isManifestParsingEnabled(): Boolean = false
}
