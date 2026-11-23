package com.mycompany.app.glide

import com.bumptech.glide.load.Options
import com.bumptech.glide.load.ResourceDecoder
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.resource.SimpleResource
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import java.io.IOException
import java.io.InputStream

/** Decodes an SVG stream for Glide's `PictureDrawable` requests (see [SvgTranscoder]), at the requested size. */
class SvgDecoder : ResourceDecoder<InputStream, SVG> {
    override fun handles(source: InputStream, options: Options): Boolean = true

    override fun decode(source: InputStream, width: Int, height: Int, options: Options): Resource<SVG> {
        try {
            val svg = SVG.getFromInputStream(source)
            if (width != Int.MIN_VALUE) svg.setDocumentWidth(width.toFloat())
            if (height != Int.MIN_VALUE) svg.setDocumentHeight(height.toFloat())
            return SimpleResource(svg)
        } catch (sVGParseException: SVGParseException) {
            throw IOException("Cannot load SVG from stream", sVGParseException)
        }
    }
}
