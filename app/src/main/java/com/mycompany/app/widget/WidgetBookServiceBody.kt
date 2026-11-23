package com.mycompany.app.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.mycompany.app.db.book.DbBookHistory.Companion.get
import com.mycompany.app.main.MainItem.ChildItem

@Suppress("deprecation")
open class WidgetBookServiceBody : RemoteViewsService() {
    @JvmField
    var darkTheme: Boolean = false

    inner class RemoteFactory(@JvmField var context: Context?, intent: Intent) : RemoteViewsFactory {
        @JvmField
        var widgetId: Int = 0
        @JvmField
        var items: MutableList<ChildItem?>? = null

        init {
            this.widgetId = intent.getIntExtra("appWidgetId", 0)
        }

        override fun getCount(): Int {
            val list = this.items
            if (list == null) {
                return 0
            }
            return list.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getLoadingView(): RemoteViews? {
            return null
        }

        override fun getViewAt(position: Int): RemoteViews? {
            var childItem: ChildItem? = null
            if (this.context == null) {
                return null
            }
            val list = this.items
            if (list != null && position >= 0 && position < list.size) {
                childItem = this.items!!.get(position)
            }
            return WidgetBookProviderBody.Companion.itemViews(
                this.context,
                childItem,
                this@WidgetBookServiceBody.darkTheme
            )
        }

        override fun getViewTypeCount(): Int {
            return 1
        }

        override fun hasStableIds(): Boolean {
            return true
        }

        override fun onCreate() {
            val widgetBookDataB: WidgetBookData = WidgetBookData.Companion.instance()
            val sparseArray = widgetBookDataB.widgetItems
            var list: MutableList<ChildItem?>? = null
            val index = if (sparseArray != null && sparseArray.size() != 0) this.widgetId else 0
            if (sparseArray != null && sparseArray.size() != 0 && index != 0) {
                try {
                    list = widgetBookDataB.widgetItems!!.get(index)
                } catch (unused: Exception) {
                }
            }
            this.items = list
        }

        override fun onDataSetChanged() {
            val widgetBookDataB: WidgetBookData = WidgetBookData.Companion.instance()
            val sparseArray = widgetBookDataB.widgetItems
            var list: MutableList<ChildItem?>? = null
            val index = if (sparseArray != null && sparseArray.size() != 0) this.widgetId else 0
            if (sparseArray != null && sparseArray.size() != 0 && index != 0) {
                try {
                    list = widgetBookDataB.widgetItems!!.get(index)
                } catch (unused: Exception) {
                }
            }
            this.items = list
        }

        override fun onDestroy() {
            this.context = null
            this.items = null
        }
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory? {
        return RemoteFactory(getApplicationContext(), intent)
    }
}
