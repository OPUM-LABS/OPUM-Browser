package com.mycompany.app.quick

/**
 * What a drag in [QuickGridView] reports to its owner ([QuickView], [QuickSubView]), unchanged from the old
 * `QuickDragHelper` (an `ItemTouchHelper` callback, removed 2026-11-16 when the lists became Compose grids).
 */
interface QuickDragListener {
    /** The drag state: 2 a drag started, 0 it ended. */
    fun onDragState(state: Int)

    /** The dragged item at [from] may move to [to]; true if it moved. */
    fun onMove(from: Int, to: Int): Boolean

    /** The item at [from] was dropped on the shortcut at [to] (merge into a folder). */
    fun onMerge(from: Int, to: Int)
}
