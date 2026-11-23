package com.mycompany.app.help

import android.content.Context
import com.mycompany.app.pref.store.MainSettings
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefMain

@Suppress("deprecation")
class KeyHelper(
    context: Context?,
    view: View?,
    statusBarShown: Boolean,
    navBarShown: Boolean,
    keyHelperListener: KeyHelperListener?
) {
    @JvmField
    var minKeyboard: Int = 0
    @JvmField
    var keyListener: KeyHelperListener? = null
    @JvmField
    var decorRoot: View? = null
    @JvmField
    var padView: View? = null
    @JvmField
    var statusBarShown: Boolean = false
    @JvmField
    var navBarShown: Boolean = false
    @JvmField
    var lastHeight: Int = 0
    @JvmField
    var keyHeight: Int = 0
    @JvmField
    var shown: Boolean = false

    internal inner class KeyboardShownStep : OnGlobalLayoutListener {

        internal inner class KeyboardShownTask : Runnable {
            override fun run() {
                val keyHelper = this@KeyHelper
                val padView = keyHelper.padView
                if (padView != null) {
                    if (keyHelper.decorRoot == null) {
                        keyHelper.decorRoot = padView.getRootView()
                        if (keyHelper.decorRoot == null) {
                            return
                        }
                    }
                    val rect = Rect()
                    keyHelper.decorRoot!!.getWindowVisibleDisplayFrame(rect)
                    val height = keyHelper.decorRoot!!.getHeight() - rect.bottom
                    if (keyHelper.keyHeight != height) {
                        keyHelper.keyHeight = height
                        val minKeyboard = keyHelper.minKeyboard
                        val shown = keyHelper.shown
                        if (height <= minKeyboard) {
                            if (shown) {
                                keyHelper.shown = false
                                val keyHelperListener = keyHelper.keyListener
                                if (keyHelperListener != null) {
                                    keyHelperListener.onKeyboardShown(false)
                                }
                            } else {
                                val keyHelperListener = keyHelper.keyListener
                                if (keyHelperListener != null) {
                                    keyHelperListener.onKeyboard()
                                }
                            }
                            if (!keyHelper.usesInsets() || keyHelper.padView!!.getPaddingBottom() == 0) {
                                return
                            }
                            keyHelper.padView!!.setPadding(0, 0, 0, 0)
                            return
                        }
                        if (shown) {
                            val keyHelperListener = keyHelper.keyListener
                            if (keyHelperListener != null) {
                                keyHelperListener.onKeyboard()
                            }
                        } else {
                            keyHelper.shown = true
                            val keyHelperListener = keyHelper.keyListener
                            if (keyHelperListener != null) {
                                keyHelperListener.onKeyboardShown(true)
                            }
                        }
                        if (keyHelper.usesInsets()) {
                            val view: View? = keyHelper.padView
                            val padding = keyHelper.keyHeight - (if (!keyHelper.navBarShown || view == null || view.getWidth() >= view.getHeight()) 0 else MainSettings.naviHeight.value)
                            if (keyHelper.padView!!.getPaddingBottom() != padding) {
                                keyHelper.padView!!.setPadding(0, 0, 0, padding)
                            }
                        }
                    }
                }
            }
        }

        override fun onGlobalLayout() {
            val keyHelper = this@KeyHelper
            val view = keyHelper.padView
            if (view != null) {
                if (keyHelper.statusBarShown && keyHelper.navBarShown) {
                    val height = view.getHeight()
                    if (keyHelper.lastHeight == height) {
                        return
                    } else {
                        keyHelper.lastHeight = height
                    }
                }
                keyHelper.padView!!.postDelayed(KeyboardShownTask(), 400L)
            }
        }
    }

    interface KeyHelperListener {
        fun onKeyboard()

        fun onKeyboardShown(shown: Boolean)
    }

    init {
        if (view != null) {
            this.padView = view
            this.statusBarShown = statusBarShown
            this.navBarShown = navBarShown
            this.keyListener = keyHelperListener
            this.lastHeight = view.getHeight()
            this.minKeyboard = MainUtil.dpToPx(context, 100.0f).toInt()
            this.padView!!.getViewTreeObserver().addOnGlobalLayoutListener(KeyboardShownStep())
        }
    }

    fun usesInsets(): Boolean {
        val index = Build.VERSION.SDK_INT
        val statusBarShown = this.statusBarShown
        if (index < 30) {
            return if (statusBarShown && this.navBarShown) false else true
        }
        return !statusBarShown
    }

    fun applyMode(statusBarShown: Boolean, navBarShown: Boolean) {
        if (this.statusBarShown == statusBarShown && this.navBarShown == navBarShown) {
            return
        }
        this.statusBarShown = statusBarShown
        this.navBarShown = navBarShown
        if (this.padView == null || usesInsets() || this.padView!!.getPaddingBottom() == 0) {
            return
        }
        this.padView!!.setPadding(0, 0, 0, 0)
    }
}
