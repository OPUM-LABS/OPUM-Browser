package com.mycompany.app.crop

import android.graphics.RectF

internal class HorizontalHandleHelper(var edge: Edge) : HandleHelper(edge, null) {
    override fun move(x: Float, y: Float, aspectRatio: Float, snapRadius: Float, rectF: RectF) {
        val edge = this.edge
        edge.adjustTo(x, y, snapRadius, aspectRatio, rectF)
        val leftEdge = Edge.b
        val coordinate = leftEdge.coordinate
        val rightEdge = Edge.d
        val coordinateNow = rightEdge.coordinate
        val halfDelta = (((Edge.e.coordinate - Edge.c.coordinate) * aspectRatio) - (coordinateNow - coordinate)) / 2.0f
        leftEdge.coordinate = coordinate - halfDelta
        rightEdge.coordinate = coordinateNow + halfDelta
        if (leftEdge.isOutsideMargin(rectF, snapRadius) && !edge.isNewRectOutOfBounds(leftEdge, rectF, aspectRatio)) {
            rightEdge.offset(-leftEdge.snapToRect(rectF))
            edge.adjustForRatio(aspectRatio)
        }
        if (!rightEdge.isOutsideMargin(rectF, snapRadius) || edge.isNewRectOutOfBounds(rightEdge, rectF, aspectRatio)) {
            return
        }
        leftEdge.offset(-rightEdge.snapToRect(rectF))
        edge.adjustForRatio(aspectRatio)
    }
}
