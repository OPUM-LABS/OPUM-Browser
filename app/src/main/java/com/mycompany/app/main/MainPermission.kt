package com.mycompany.app.main

import ch.opumlabs.opumbrowser.R as AppR
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import androidx.core.app.ActivityCompat

@Suppress("deprecation")
class MainPermission : MainActivity() {
    @JvmField
    var appContext: Context? = null
    @JvmField
    var waitResume: Boolean = false
    @JvmField
    var permType: Int = 0
    @JvmField
    var apkPath: String? = null
    @JvmField
    var externalOpen: Boolean = false
    @JvmField
    var granted: Boolean = false
    @JvmField
    var paused: Boolean = false

    internal inner class ParseTask : Runnable {

        override fun run() {
            val mainPermission = this@MainPermission
            if (mainPermission.mainHandler != null) {
                val permType = mainPermission.permType
                var granted = true
                if (permType == 0) {
                    ActivityCompat.requestPermissions(
                        mainPermission,
                        arrayOf<String?>("android.permission.WRITE_EXTERNAL_STORAGE"),
                        20
                    )
                } else if (permType == 1) {
                    ActivityCompat.requestPermissions(
                        mainPermission,
                        arrayOf<String?>(
                            "android.permission.CAMERA",
                            "android.permission.RECORD_AUDIO"
                        ),
                        21
                    )
                } else if (permType == 2) {
                    ActivityCompat.requestPermissions(
                        mainPermission,
                        arrayOf<String?>("android.permission.CAMERA"),
                        22
                    )
                } else if (permType == 3) {
                    ActivityCompat.requestPermissions(
                        mainPermission,
                        arrayOf<String?>("android.permission.RECORD_AUDIO"),
                        23
                    )
                } else if (permType == 4) {
                    ActivityCompat.requestPermissions(
                        mainPermission,
                        arrayOf<String?>("android.permission.ACCESS_FINE_LOCATION"),
                        24
                    )
                } else if (permType != 5) {
                    try {
                        if (permType == 6) {
                            val intent = Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS")
                            intent.setData(Uri.parse("package:" + mainPermission.getPackageName()))
                            mainPermission.launchForResult(intent, RequestCode.PIP_SETTINGS)
                        } else if (permType != 7) {
                            granted = false
                        } else {
                            val intent = Intent("android.settings.MANAGE_UNKNOWN_APP_SOURCES")
                            intent.setData(Uri.parse("package:" + mainPermission.getPackageName()))
                            mainPermission.launchForResult(intent, RequestCode.INSTALL_SETTINGS)
                        }
                    } catch (unused: Exception) {
                    }
                } else if (Build.VERSION.SDK_INT >= 33) {
                    ActivityCompat.requestPermissions(
                        mainPermission,
                        arrayOf<String?>("android.permission.POST_NOTIFICATIONS"),
                        25
                    )
                } else {
                    granted = false
                }
                mainPermission.granted = granted
                if (granted) {
                    MainUtil.applyScreenOrientation(mainPermission)
                } else {
                    MainUtil.showToast(mainPermission, AppR.string.no_permission)
                    mainPermission.finish()
                }
            }
        }
    }

    override fun onLauncherResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        this.granted = false
        if (requestCode == RequestCode.PIP_SETTINGS) {
            if (MainUtil.hasPermission(this.appContext, 6)) {
                return
            }
            MainUtil.showToast(this, AppR.string.permission_granted)
        } else {
            if (requestCode != RequestCode.INSTALL_SETTINGS || MainUtil.hasPermission(this.appContext, 7)) {
                return
            }
            if (TextUtils.isEmpty(this.apkPath)) {
                MainUtil.showToast(this, AppR.string.permission_granted)
            } else {
                this.paused = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        this.ownBackHandling = true
        super.onCreate(savedInstanceState)
        this.appContext = getApplicationContext()
        this.waitResume = true
        val intExtra = getIntent().getIntExtra("EXTRA_TYPE", -1)
        this.permType = intExtra
        if (intExtra == 7) {
            this.apkPath = getIntent().getStringExtra("EXTRA_PATH")
            this.externalOpen = getIntent().getBooleanExtra("EXTRA_EXT", false)
        }
        launchForResult(null, RequestCode.PIP_SETTINGS)
        launchForResult(null, RequestCode.INSTALL_SETTINGS)
        val handler = this.mainHandler
        if (handler == null) {
            return
        }
        handler.post(ParseTask())
    }

    override fun onDestroy() {
        super.onDestroy()
        this.appContext = null
        this.apkPath = null
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing()) {
            MainApp.Companion.lastToast = null
        }
        if (this.paused) {
            this.paused = false
            MainUtil.openFileWith(this, this.apkPath, "application/vnd.android.package-archive", false, this.externalOpen)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        this.granted = false
        when (requestCode) {
            20, 22, 23, 24, 25 -> if (grantResults.size > 0 && grantResults[0] == 0) {
                MainUtil.showToast(this, AppR.string.permission_granted)
            } else {
                MainUtil.showToast(this, AppR.string.permission_denied)
            }

            21 -> if (grantResults.size > 1 && grantResults[0] == 0 && grantResults[1] == 0) {
                MainUtil.showToast(this, AppR.string.permission_granted)
            } else {
                MainUtil.showToast(this, AppR.string.permission_denied)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (this.waitResume) {
            this.waitResume = false
            return
        }
        if (!this.granted) {
            finish()
        }
        this.granted = false
    }
}
