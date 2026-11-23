package com.mycompany.app.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle

@Suppress("deprecation")
class WidgetSearchProviderDark : WidgetSearchProviderBody() {
    override fun onAppWidgetOptionsChanged(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        this.darkTheme = true
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        this.darkTheme = true
        super.onDeleted(context, appWidgetIds)
    }

    override fun onDisabled(context: Context?) {
        this.darkTheme = true
        super.onDisabled(context)
    }

    override fun onEnabled(context: Context?) {
        this.darkTheme = true
        super.onEnabled(context)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        this.darkTheme = true
        super.onReceive(context, intent)
    }

    override fun onRestored(context: Context?, oldWidgetIds: IntArray?, newWidgetIds: IntArray?) {
        this.darkTheme = true
        super.onRestored(context, oldWidgetIds, newWidgetIds)
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        this.darkTheme = true
        updateAll(context, appWidgetManager, appWidgetIds)
    }
}
