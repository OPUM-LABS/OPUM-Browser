package com.mycompany.app.ui.viewer

/** The PDF entry point of the picture-book reader (opens a PDF from another app); see [ImageViewerActivity]. */
class PdfViewerActivity : ImageViewerActivity() {
    override val pdfEntry: Boolean = true
}
