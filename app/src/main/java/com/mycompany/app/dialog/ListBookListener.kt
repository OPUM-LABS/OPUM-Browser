package com.mycompany.app.dialog

import com.mycompany.app.main.MainItem.ChildItem

/**
 * A row of the browser's History was chosen (was `DialogListBook.ListBookListener`, moved out when the old list
 * engine was removed): [onItem] gets (position, item, action) with action 2 = open, 3 = new tab, 4 = group tab,
 * 5 = background tab; [onClosed] is the old "list closed" callback.
 */
interface ListBookListener {
    fun onItem(position: Int, childItem: ChildItem?, action: Int)

    fun onClosed()
}
