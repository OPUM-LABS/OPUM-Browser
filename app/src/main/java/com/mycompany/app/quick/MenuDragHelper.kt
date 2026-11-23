package com.mycompany.app.quick

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

@Suppress("deprecation")
class MenuDragHelper(var menuDragListener: MenuDragListener?) : ItemTouchHelper.Callback() {
    interface MenuDragListener {
        fun onDragState(state: Int)

        fun onMove(from: Int, to: Int)
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return makeMovementFlags(15, 0)
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        val menuDragListener = this.menuDragListener
        if (menuDragListener != null) {
            menuDragListener.onMove(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        }
        return true
    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        val menuDragListener = this.menuDragListener
        if (menuDragListener != null) {
            menuDragListener.onDragState(actionState)
        }
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }
}
