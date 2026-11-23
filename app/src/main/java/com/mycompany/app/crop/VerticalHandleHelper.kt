package com.mycompany.app.crop

import android.graphics.RectF

internal class VerticalHandleHelper(var edge: Edge) : HandleHelper(null, edge) {
    override fun move(x: Float, y: Float, aspectRatio: Float, snapRadius: Float, rectF: RectF) {
        val edge = this.edge
        edge.adjustTo(x, y, snapRadius, aspectRatio, rectF)
        val topEdge = Edge.c
        val coordinate = topEdge.coordinate
        val bottomEdge = Edge.e
        val coordinateNow = bottomEdge.coordinate
        val halfDelta = (((Edge.d.coordinate - Edge.b.coordinate) / aspectRatio) - (coordinateNow - coordinate)) / 2.0f
        topEdge.coordinate = coordinate - halfDelta
        bottomEdge.coordinate = coordinateNow + halfDelta
        if (topEdge.isOutsideMargin(rectF, snapRadius) && !edge.isNewRectOutOfBounds(topEdge, rectF, aspectRatio)) {
            bottomEdge.offset(-topEdge.snapToRect(rectF))
            edge.adjustForRatio(aspectRatio)
        }
        if (!bottomEdge.isOutsideMargin(rectF, snapRadius) || edge.isNewRectOutOfBounds(bottomEdge, rectF, aspectRatio)) {
            return
        }
        topEdge.offset(-bottomEdge.snapToRect(rectF))
        edge.adjustForRatio(aspectRatio)
    }
}
