package com.mycompany.app.main

import ch.opumlabs.opumbrowser.R as AppR
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import com.mycompany.app.dialog.DialogOpenType

@Suppress("deprecation")
class MainLauncher : MainActivity() {
    @JvmField
    var pendingIntent: Intent? = null
    @JvmField
    var openTypeDialog: DialogOpenType? = null

    internal inner class HandleIntentTask : Runnable {
        override fun run() {
            this@MainLauncher.handleIntent()
        }
    }

    internal inner class HandleNewIntentTask : Runnable {
        override fun run() {
            this@MainLauncher.handleIntent()
        }
    }

    internal inner class OpenTypeDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val mainLauncher = this@MainLauncher
            val dialogOpenType = mainLauncher.openTypeDialog
            if (dialogOpenType != null) {
                dialogOpenType.dismiss()
                mainLauncher.openTypeDialog = null
            }
            mainLauncher.finish()
        }
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        val dialogOpenType = this.openTypeDialog
        if (dialogOpenType != null) {
            dialogOpenType.dismiss()
            this.openTypeDialog = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.ownBackHandling = true
        super.onCreate(savedInstanceState)
        this.pendingIntent = getIntent()
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(HandleIntentTask())
    }

    override fun onDestroy() {
        super.onDestroy()
        this.pendingIntent = null
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.pendingIntent = intent
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(HandleNewIntentTask())
    }

    override fun onPause() {
        super.onPause()
        val dialogOpenType = this.openTypeDialog
        if (dialogOpenType != null) {
            dialogOpenType.dismiss()
            this.openTypeDialog = null
        }
    }

    fun handleIntent() {
        val intent = this.pendingIntent
        this.pendingIntent = null
        if (intent != null) {
            val stringExtra = intent.getStringExtra("EXTRA_NOTI")
            if (TextUtils.isEmpty(stringExtra)) {
                MainUtil.showToast(this, AppR.string.not_supported)
                finish()
                return
            }
            val type = intent.getStringExtra("EXTRA_TYPE")
            val applicationContext = getApplicationContext()
            val mainAppO: MainApp? = MainApp.Companion.from(applicationContext)
            if (!TextUtils.isEmpty(if (mainAppO == null) null else mainAppO.pendingVideoUrl)) {
                try {
                    val playerIntent =
                        Intent(applicationContext, com.mycompany.app.ui.viewer.VideoPlayerActivity::class.java as Class<*>)
                    playerIntent.putExtra("EXTRA_NOTI", stringExtra)
                    if (!TextUtils.isEmpty(type)) {
                        playerIntent.putExtra("EXTRA_TYPE", type)
                    }
                    playerIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    startActivity(playerIntent)
                    finish()
                    return
                } catch (unused: Exception) {
                }
            }
            if (MainUtil.openWithApp(this, stringExtra, type, false, false)) {
                finish()
                return
            }
            val dialogOpenType = this.openTypeDialog
            if (dialogOpenType == null) {
                var theme = AppR.style.DialogExpandTheme
                if (isLandscapeScreen()) {
                    theme = 0
                }
                val openTypeDialog = DialogOpenType(this, theme, stringExtra, false)
                this.openTypeDialog = openTypeDialog
                openTypeDialog.setOnDismissListener(OpenTypeDialogDismiss())
                if (MainConst.isApi30Insets || theme != 0) {
                    this.openTypeDialog!!.applyColors(false, false, 0, 0, true)
                }
            }
        }
    }

    companion object {
    }
}
