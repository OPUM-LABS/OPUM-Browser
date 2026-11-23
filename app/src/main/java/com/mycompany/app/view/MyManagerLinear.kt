package com.mycompany.app.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler

@Suppress("deprecation")
open class MyManagerLinear : LinearLayoutManager {
    constructor(p0: Int) : super(null as android.content.Context?, p0, false)

    constructor(context: Context?, attributeSet: AttributeSet?, i: Int, i2: Int) : super(
        context,
        attributeSet,
        i,
        i2
    )

    override fun onLayoutChildren(recycler: Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (unused: Exception) {
        }
    }
}
