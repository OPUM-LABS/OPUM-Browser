package com.mycompany.app.crop

import android.graphics.RectF

internal abstract class HandleHelper(var horizontalEdge: Edge?, var verticalEdge: Edge?) {
    @JvmField
    var edges: EdgePair

    init {
        val edgePair = EdgePair()
        edgePair.primary = horizontalEdge
        edgePair.secondary = verticalEdge
        this.edges = edgePair
    }

    abstract fun move(x: Float, y: Float, aspectRatio: Float, snapRadius: Float, rectF: RectF)

    open fun updateCropWindow(x: Float, y: Float, rectF: RectF, snapRadius: Float) {
        val xValue: Float
        val targetY: Float
        val bounds: RectF
        val radius: Float
        val edgePair = this.edges
        val edge = edgePair.primary
        val secondary = edgePair.secondary
        if (edge != null) {
            xValue = x
            targetY = y
            bounds = rectF
            radius = snapRadius
            edge.adjustTo(xValue, targetY, radius, 1.0f, bounds)
        } else {
            xValue = x
            targetY = y
            bounds = rectF
            radius = snapRadius
        }
        if (secondary != null) {
            val radiusNow = radius
            val targetYNow = targetY
            secondary.adjustTo(xValue, targetYNow, radiusNow, 1.0f, bounds)
        }
    }
}
