package com.mycompany.app.crop

import android.graphics.RectF
import kotlin.math.max
import kotlin.math.min

enum class Edge {
    b,
    c,
    d,
    e;

    var coordinate: Float = 0f

    fun adjustForRatio(aspectRatio: Float) {
        val coordinate = b.coordinate
        val coordinateNow = c.coordinate
        val newCoordinate = d.coordinate
        val bottomNow = e.coordinate
        val edgeIndex = ordinal
        if (edgeIndex == 0) {
            this.coordinate = newCoordinate - ((bottomNow - coordinateNow) * aspectRatio)
            return
        }
        if (edgeIndex == 1) {
            this.coordinate = bottomNow - ((newCoordinate - coordinate) / aspectRatio)
        } else if (edgeIndex == 2) {
            this.coordinate = (((bottomNow - coordinateNow) * aspectRatio) + coordinate)
        } else {
            if (edgeIndex != 3) {
                return
            }
            this.coordinate = ((newCoordinate - coordinate) / aspectRatio) + coordinateNow
        }
    }

    fun adjustTo(x: Float, y: Float, snapRadius: Float, aspectRatio: Float, rectF: RectF) {
        val edgeIndex = ordinal
        if (edgeIndex == 0) {
            var left = rectF.left
            if (x - left >= snapRadius) {
                val coordinate = d.coordinate
                var minX = coordinate - 40.0f
                if (x < minX) {
                    minX = Float.POSITIVE_INFINITY
                }
                left = min(
                    x,
                    min(
                        minX,
                        if ((coordinate - x) / aspectRatio <= 40.0f) coordinate - (aspectRatio * 40.0f) else Float.POSITIVE_INFINITY
                    )
                )
            }
            this.coordinate = left
            return
        }
        if (edgeIndex == 1) {
            var top = rectF.top
            if (y - top >= snapRadius) {
                val coordinate = e.coordinate
                var minY = coordinate - 40.0f
                if (y < minY) {
                    minY = Float.POSITIVE_INFINITY
                }
                top = min(
                    y,
                    min(
                        minY,
                        if ((coordinate - y) * aspectRatio <= 40.0f) coordinate - (40.0f / aspectRatio) else Float.POSITIVE_INFINITY
                    )
                )
            }
            this.coordinate = top
            return
        }
        if (edgeIndex == 2) {
            var right = rectF.right
            if (right - x >= snapRadius) {
                val coordinate = b.coordinate
                var maxX = coordinate + 40.0f
                if (x > maxX) {
                    maxX = Float.NEGATIVE_INFINITY
                }
                right = max(
                    x,
                    max(
                        maxX,
                        if ((x - coordinate) / aspectRatio <= 40.0f) (aspectRatio * 40.0f) + coordinate else Float.NEGATIVE_INFINITY
                    )
                )
            }
            this.coordinate = right
            return
        }
        if (edgeIndex != 3) {
            return
        }
        var bottom = rectF.bottom
        if (bottom - y >= snapRadius) {
            val coordinate = c.coordinate
            var maxY = coordinate + 40.0f
            if (y > maxY) {
                maxY = Float.NEGATIVE_INFINITY
            }
            bottom = max(
                y,
                max(
                    if ((y - coordinate) * aspectRatio <= 40.0f) (40.0f / aspectRatio) + coordinate else Float.NEGATIVE_INFINITY,
                    maxY
                )
            )
        }
        this.coordinate = bottom
    }

    fun isNewRectOutOfBounds(edge: Edge, rectF: RectF, aspectRatio: Float): Boolean {
        val edgePos: Float
        val coordinate = edge.coordinate
        val otherIndex = edge.ordinal
        if (otherIndex == 0) {
            edgePos = rectF.left
        } else if (otherIndex != 1) {
            edgePos = if (otherIndex != 2) rectF.bottom else rectF.right
        } else {
            edgePos = rectF.top
        }
        val offset = edgePos - coordinate
        val edgeIndex = ordinal
        val topEdge = c
        val bottomEdge = e
        val rightEdge = d
        if (edgeIndex != 0) {
            val leftEdge = b
            if (edgeIndex != 1) {
                if (edgeIndex != 2) {
                    if (edgeIndex == 3) {
                        if (edge == leftEdge) {
                            val left = rectF.left
                            val newLeft = rightEdge.coordinate - offset
                            val coordinateNow = topEdge.coordinate
                            return isOutOfBounds(coordinateNow, left, ((newLeft - left) / aspectRatio) + coordinateNow, newLeft, rectF)
                        }
                        if (edge == rightEdge) {
                            val right = rectF.right
                            val newRight = leftEdge.coordinate - offset
                            val coordinateNow = topEdge.coordinate
                            return isOutOfBounds(coordinateNow, newRight, ((right - newRight) / aspectRatio) + coordinateNow, right, rectF)
                        }
                    }
                } else {
                    if (edge == topEdge) {
                        val top = rectF.top
                        val newTop = bottomEdge.coordinate - offset
                        val coordinateNow = leftEdge.coordinate
                        return isOutOfBounds(top, coordinateNow, newTop, (((newTop - top) * aspectRatio) + coordinateNow), rectF)
                    }
                    if (edge == bottomEdge) {
                        val bottom = rectF.bottom
                        val newBottom = topEdge.coordinate - offset
                        val coordinateNow = leftEdge.coordinate
                        return isOutOfBounds(newBottom, coordinateNow, bottom, (((bottom - newBottom) * aspectRatio) + coordinateNow), rectF)
                    }
                }
            } else {
                if (edge == leftEdge) {
                    val left = rectF.left
                    val newLeft = rightEdge.coordinate - offset
                    val coordinateNow = bottomEdge.coordinate
                    return isOutOfBounds(coordinateNow - ((newLeft - left) / aspectRatio), left, coordinateNow, newLeft, rectF)
                }
                if (edge == rightEdge) {
                    val right = rectF.right
                    val newRight = leftEdge.coordinate - offset
                    val coordinateNow = bottomEdge.coordinate
                    return isOutOfBounds(coordinateNow - ((right - newRight) / aspectRatio), newRight, coordinateNow, right, rectF)
                }
            }
        } else {
            if (edge == topEdge) {
                val top = rectF.top
                val newTop = bottomEdge.coordinate - offset
                val coordinateNow = rightEdge.coordinate
                return isOutOfBounds(top, coordinateNow - ((newTop - top) * aspectRatio), newTop, coordinateNow, rectF)
            }
            if (edge == bottomEdge) {
                val bottom = rectF.bottom
                val newBottom = topEdge.coordinate - offset
                val coordinateNow = rightEdge.coordinate
                return isOutOfBounds(newBottom, coordinateNow - ((bottom - newBottom) * aspectRatio), bottom, coordinateNow, rectF)
            }
        }
        return true
    }

    fun isOutsideMargin(rectF: RectF, snapRadius: Float): Boolean {
        val edgeIndex = ordinal
        if (edgeIndex == 0) {
            return this.coordinate - rectF.left < snapRadius
        }
        if (edgeIndex == 1) {
            return this.coordinate - rectF.top < snapRadius
        }
        if (edgeIndex != 2) {
            return rectF.bottom - this.coordinate < snapRadius
        }
        return rectF.right - this.coordinate < snapRadius
    }

    fun offset(coordinate: Float) {
        this.coordinate += coordinate
    }

    fun snapToRect(rectF: RectF): Float {
        val coordinate = this.coordinate
        val edgeIndex = ordinal
        if (edgeIndex == 0) {
            this.coordinate = rectF.left
        } else if (edgeIndex == 1) {
            this.coordinate = rectF.top
        } else if (edgeIndex == 2) {
            this.coordinate = rectF.right
        } else if (edgeIndex == 3) {
            this.coordinate = rectF.bottom
        }
        return this.coordinate - coordinate
    }

    companion object {
        @JvmStatic
        fun isOutOfBounds(top: Float, left: Float, bottom: Float, right: Float, rectF: RectF): Boolean {
            return top < rectF.top || left < rectF.left || bottom > rectF.bottom || right > rectF.right
        }
    }
}
