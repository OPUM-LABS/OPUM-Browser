package com.mycompany.app.widget

import android.content.Intent
import android.os.IBinder

@Suppress("deprecation")
class WidgetBookServiceDark : WidgetBookServiceBody() {
    override fun onBind(intent: Intent?): IBinder? {
        this.darkTheme = true
        return super.onBind(intent)
    }

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory? {
        this.darkTheme = true
        return super.onGetViewFactory(intent)
    }
}
