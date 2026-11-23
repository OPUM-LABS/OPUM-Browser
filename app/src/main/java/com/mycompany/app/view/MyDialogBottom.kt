package com.mycompany.app.view

import android.annotation.TargetApi
import com.mycompany.app.main.MainUtil
import java.util.ArrayList
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Insets
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.FrameLayout
import android.window.OnBackInvokedCallback
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.behavior.MyBehaviorDialog
import com.mycompany.app.image.ImageSizeListener
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isLandscape
import com.mycompany.app.main.MainUtil.navBarColor
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.isSmallScreen
import com.mycompany.app.main.MainUtil.applyWindowBrightness
import com.mycompany.app.main.MainUtil.setDecorFits
import com.mycompany.app.main.MainUtil.isDarkPage
import com.mycompany.app.main.MainUtil.insetsController
import com.mycompany.app.main.MainUtil.setNavBarColor
import com.mycompany.app.main.MainUtil.blendColors
import com.mycompany.app.main.MainUtil.applyBarAppearance
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefWeb
import java.lang.ref.WeakReference
import java.util.concurrent.ExecutorService

/**
 * Holder for the types other code still uses. The rest of the class (a legacy View-based adapter / dialog) was unreachable
 * after the Compose port and was removed in T5 (2026-11-16).
 */
class MyDialogBottom {
    interface UserShowListener {
        fun onShow()
    }
}
