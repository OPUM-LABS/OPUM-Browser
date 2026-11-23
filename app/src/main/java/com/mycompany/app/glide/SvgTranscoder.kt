package com.mycompany.app.glide

import android.graphics.drawable.PictureDrawable
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder
import com.caverock.androidsvg.SVG

@Suppress("deprecation")
class SvgTranscoder : ResourceTranscoder<SVG, PictureDrawable> {
    override fun transcode(resource: Resource<SVG>, options: Options): Resource<PictureDrawable> {
        return SimpleResource(PictureDrawable(resource.get().renderToPicture()))
    }
}
