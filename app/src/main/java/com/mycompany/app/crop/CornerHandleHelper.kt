package com.mycompany.app.crop

import android.graphics.RectF

internal class CornerHandleHelper(horizontalEdge: Edge?, verticalEdge: Edge?) : HandleHelper(horizontalEdge, verticalEdge) {
    override fun move(x: Float, y: Float, aspectRatio: Float, snapRadius: Float, rectF: RectF) {
        val edge: Edge
        var primary = this.verticalEdge!!
        val leftEdge = Edge.b
        val left = if (primary == leftEdge) x else leftEdge.coordinate
        val secondary = this.horizontalEdge!!
        val topEdge = Edge.c
        val top = if (secondary == topEdge) y else topEdge.coordinate
        val rightEdge = Edge.d
        val right = if (primary == rightEdge) x else rightEdge.coordinate
        val bottomEdge = Edge.e
        val ratio = (right - left) / ((if (secondary == bottomEdge) y else bottomEdge.coordinate) - top)
        val edgePair = this.edges
        if (ratio > aspectRatio) {
            edgePair.primary = primary
            edgePair.secondary = secondary
            edge = primary
            primary = secondary
        } else {
            edgePair.primary = secondary
            edgePair.secondary = primary
            edge = secondary
        }
        edge.adjustTo(x, y, snapRadius, aspectRatio, rectF)
        val current = edge
        primary.adjustForRatio(aspectRatio)
        if (primary.isOutsideMargin(rectF, snapRadius)) {
            primary.snapToRect(rectF)
            current.adjustForRatio(aspectRatio)
        }
    }
}
