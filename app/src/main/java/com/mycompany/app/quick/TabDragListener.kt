package com.mycompany.app.quick

/**
 * What a dragged tab reports (it was nested in the old `TabDragHelper`): [onDragState] drag state (0 end, 1 swipe, 2 drag), [onMove] swap the
 * tabs at the two positions (true when done), [onMerge] drop the first onto the second (group), [onSwiped] the tab at the position was
 * swiped away, [onSwipePos] the position of the tab being swiped.
 */
interface TabDragListener {
    fun onDragState(state: Int)

    fun onMove(from: Int, to: Int): Boolean

    fun onMerge(from: Int, to: Int)

    fun onSwiped(position: Int)

    fun onSwipePos(state: Int)
}
