package com.mycompany.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.mycompany.app.main.MainApp
import ch.opumlabs.opumbrowser.R

@Suppress("deprecation")
open class WidgetSearchProviderBody : AppWidgetProvider() {
    @JvmField
    var darkTheme: Boolean = false
    @JvmField
    var updateContext: Context? = null
    @JvmField
    var widgetManager: AppWidgetManager? = null
    @JvmField
    var widgetIds: IntArray? = null
    @JvmField
    var widgetIndex: Int = 0
    @JvmField
    var receiveContext: Context? = null

    internal inner class UpdateAllTask : Runnable {
        override fun run() {
            val widgetSearchProviderBody = this@WidgetSearchProviderBody
            val context = widgetSearchProviderBody.receiveContext
            widgetSearchProviderBody.receiveContext = null
            val appWidgetManager: AppWidgetManager? = if (context == null) null else AppWidgetManager.getInstance(context)
            if (context == null || appWidgetManager == null) {
                return
            }
            widgetSearchProviderBody.updateAll(
                context,
                appWidgetManager,
                appWidgetManager.getAppWidgetIds(
                    ComponentName(
                        context,
                        (if (widgetSearchProviderBody.darkTheme) WidgetSearchProviderDark::class.java else WidgetSearchProviderLight::class.java) as Class<*>
                    )
                )
            )
        }
    }

    internal inner class UpdateNextTask : Runnable {
        override fun run() {
            this@WidgetSearchProviderBody.updateNext()
        }
    }

    internal inner class UpdateNextStep : Runnable {
        override fun run() {
            this@WidgetSearchProviderBody.updateNext()
        }
    }

    fun updateAll(context: Context?, appWidgetManager: AppWidgetManager?, widgetIds: IntArray?) {
        if (context == null || widgetIds == null || widgetIds.size == 0) {
            this.updateContext = null
            this.widgetManager = null
            this.widgetIds = null
        } else {
            this.updateContext = context
            this.widgetManager = appWidgetManager
            this.widgetIds = widgetIds
            this.widgetIndex = 0
            MainApp.runInBackground(context, UpdateNextTask())
        }
    }

    fun updateNext() {
        try {
            val widgetIndex = this.widgetIndex
            val widgetId = if (widgetIndex < 0 || this.widgetIds == null || widgetIndex >= this.widgetIds!!.size) -1234 else this.widgetIds!![widgetIndex]
            if (widgetId == -1234) {
                this.updateContext = null
                this.widgetManager = null
                this.widgetIds = null
                return
            }
            updateWidget(this.updateContext!!, this.widgetManager, widgetId)
            val newWidgetIndex = this.widgetIndex + 1
            this.widgetIndex = newWidgetIndex
            if ((if (newWidgetIndex < 0 || this.widgetIds == null || newWidgetIndex >= this.widgetIds!!.size) -1234 else this.widgetIds!![newWidgetIndex]) != -1234) {
                MainApp.runInBackground(this.updateContext, UpdateNextStep())
                return
            }
            this.updateContext = null
            this.widgetManager = null
            this.widgetIds = null
        } catch (unused: Exception) {
            this.updateContext = null
            this.widgetManager = null
            this.widgetIds = null
        }
    }

    fun updateWidget(context: Context, appWidgetManager: AppWidgetManager?, widgetId: Int) {
        if (appWidgetManager == null) {
            return
        }
        val intent = Intent(context, WidgetSearchActivity::class.java as Class<*>)
        intent.putExtra("EXTRA_VOICE", false)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)))
        val activity = PendingIntent.getActivity(context, 8, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val voiceIntent = Intent(context, WidgetSearchActivity::class.java as Class<*>)
        voiceIntent.putExtra("EXTRA_VOICE", true)
        voiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        voiceIntent.setData(Uri.parse(voiceIntent.toUri(Intent.URI_INTENT_SCHEME)))
        val voicePending = PendingIntent.getActivity(context, 9, voiceIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val remoteViews = RemoteViews(
            context.getPackageName(),
            if (this.darkTheme) R.layout.widget_search_layout_dark else R.layout.widget_search_layout_light
        )
        remoteViews.setOnClickPendingIntent(R.id.back_view, activity)
        remoteViews.setOnClickPendingIntent(R.id.icon_view, voicePending)
        appWidgetManager.updateAppWidget(widgetId, remoteViews)
    }

    override fun onReceive(context: Context?, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.getAction()
        action!!.javaClass
        if (action == "ch.opumlabs.opumbrowser.ACTION_SRCHGET_UPDATE") {
            this.receiveContext = context
            MainApp.runInBackground(context, UpdateAllTask())
        }
    }

    override fun onUpdate(context: Context?, appWidgetManager: AppWidgetManager?, appWidgetIds: IntArray?) {
        updateAll(context, appWidgetManager, appWidgetIds)
    }

    companion object {
    }
}
