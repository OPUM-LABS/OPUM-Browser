package com.mycompany.app.crop

import android.graphics.RectF

internal class CenterHandleHelper(horizontalEdge: Edge?, verticalEdge: Edge?) : HandleHelper(horizontalEdge, verticalEdge) {
    override fun move(x: Float, y: Float, aspectRatio: Float, snapRadius: Float, rectF: RectF) {
        updateCropWindow(x, y, rectF, snapRadius)
    }
    override fun updateCropWindow(x: Float, y: Float, rectF: RectF, snapRadius: Float) {
        val edge = Edge.b
        val coordinate = edge.coordinate
        val topEdge = Edge.c
        val coordinateNow = topEdge.coordinate
        val rightEdge = Edge.d
        val rightX = rightEdge.coordinate
        val bottomEdge = Edge.e
        val dx = x - ((coordinate + rightX) / 2.0f)
        val dy = y - ((coordinateNow + bottomEdge.coordinate) / 2.0f)
        edge.offset(dx)
        topEdge.offset(dy)
        rightEdge.offset(dx)
        bottomEdge.offset(dy)
        if (edge.isOutsideMargin(rectF, snapRadius)) {
            rightEdge.offset(edge.snapToRect(rectF))
        } else if (rightEdge.isOutsideMargin(rectF, snapRadius)) {
            edge.offset(rightEdge.snapToRect(rectF))
        }
        if (topEdge.isOutsideMargin(rectF, snapRadius)) {
            bottomEdge.offset(topEdge.snapToRect(rectF))
        } else if (bottomEdge.isOutsideMargin(rectF, snapRadius)) {
            topEdge.offset(bottomEdge.snapToRect(rectF))
        }
    }
}
