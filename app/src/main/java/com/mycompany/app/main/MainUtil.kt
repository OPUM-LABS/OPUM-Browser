package com.mycompany.app.main

import android.view.inputmethod.InputMethodManager

import android.webkit.WebViewClient
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ListSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import com.mycompany.app.pref.store.MainSettings
import com.mycompany.app.pref.store.ImageSettings
import com.mycompany.app.pref.store.VideoSettings
import java.util.ArrayList
import java.util.HashMap
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Movie
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.Path
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PictureDrawable
import android.media.AudioManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import android.os.Process
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.media.session.PlaybackStateCompat
import android.text.Editable
import android.text.Layout
import android.text.SpannableString
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.format.DateFormat
import android.text.style.ForegroundColorSpan
import android.text.style.ReplacementSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.util.Patterns
import android.util.Rational
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewParent
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.webkit.CookieManager
import android.webkit.MimeTypeMap
import android.webkit.RenderProcessGoneDetail
import android.webkit.URLUtil
import android.webkit.ValueCallback
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewDatabase
import android.widget.EditText
import android.widget.ImageView
import android.widget.RemoteViews
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.biometric.BiometricManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.WindowInsetsControllerCompat
import androidx.webkit.URLUtilCompat
import androidx.webkit.WebSettingsCompat
import androidx.webkit.WebStorageCompat
import androidx.webkit.WebViewFeature
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.caverock.androidsvg.SVG
import com.mycompany.app.compress.Compress
import com.mycompany.app.compress.Compress.Companion.isImageExt
import com.mycompany.app.compress.Compress.Companion.isAudioExt
import com.mycompany.app.compress.Compress.Companion.isPdfExt
import com.mycompany.app.compress.Compress.Companion.isSubtitleExt
import com.mycompany.app.compress.Compress.Companion.isVideoExt
import com.mycompany.app.compress.Compress.Companion.isMhtExt
import com.mycompany.app.compress.Compress.Companion.create
import com.mycompany.app.compress.Compress.Companion.isAlbumExt
import com.mycompany.app.compress.Compress.Companion.isApkExt
import com.mycompany.app.data.DataTrans
import com.mycompany.app.data.DataUrl.ImgCntItem
import com.mycompany.app.data.book.DataBookAds.Companion.instance
import com.mycompany.app.data.book.DataBookAlbum.Companion.instance
import com.mycompany.app.data.book.DataBookBlock.Companion.instance
import com.mycompany.app.data.book.DataBookCmp.Companion.instance
import com.mycompany.app.data.book.DataBookDown.Companion.instance
import com.mycompany.app.data.book.DataBookFilter.Companion.instance
import com.mycompany.app.data.book.DataBookHistory.Companion.instance
import com.mycompany.app.data.book.DataBookJava.Companion.instance
import com.mycompany.app.data.book.DataBookLink
import com.mycompany.app.data.book.DataBookOver.Companion.instance
import com.mycompany.app.data.book.DataBookPdf.Companion.instance
import com.mycompany.app.data.book.DataBookPop.Companion.instance
import com.mycompany.app.data.book.DataBookScript.Companion.instance
import com.mycompany.app.data.book.DataBookTmem.Companion.instance
import com.mycompany.app.data.book.DataBookTrans.Companion.instance
import com.mycompany.app.data.book.DataBookUser.Companion.instance
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookAlbum
import com.mycompany.app.db.book.DbBookCmp
import com.mycompany.app.db.book.DbBookIcon
import com.mycompany.app.db.book.DbBookPage
import com.mycompany.app.db.book.DbBookPass
import com.mycompany.app.db.book.DbBookPdf
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookRecent.Companion.addAsync
import com.mycompany.app.db.book.DbBookRecent.Companion.isEnabled
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.db.book.DbTabThumb.Companion.thumbById
import com.mycompany.app.dialog.DialogSetPopup
import com.mycompany.app.lock.FingerActivity
import com.mycompany.app.lock.PassActivity
import com.mycompany.app.lock.PatternActivity
import com.mycompany.app.lock.PinActivity
import com.mycompany.app.main.MainDownSvc.DownItem
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefAlbum.Companion.saveTrans
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefImage
import com.mycompany.app.pref.PrefList
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefMain.Companion.get
import com.mycompany.app.pref.PrefPath
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefSet.putBoolNow
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefVideo
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtwo
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.subtitle.Subtitle
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebViewActivity
import com.mycompany.app.widget.WidgetBookProviderDark
import com.mycompany.app.widget.WidgetBookProviderLight

import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.OkHttpClient
import okhttp3.Request

import org.apache.commons.text.StringEscapeUtils

import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.mozilla.universalchardet.UniversalDetector
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URISyntaxException
import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.concurrent.Executors
import java.util.regex.Pattern
import java.util.zip.GZIPInputStream
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.BooleanArray
import kotlin.Byte
import kotlin.ByteArray
import kotlin.Char
import kotlin.CharSequence
import kotlin.Comparator
import kotlin.Exception
import kotlin.Int
import kotlin.IntArray
import kotlin.KotlinVersion
import kotlin.Long
import kotlin.NumberFormatException
import kotlin.String
import kotlin.Suppress
import kotlin.Throws
import kotlin.also
import kotlin.arrayOf
import kotlin.arrayOfNulls
import kotlin.code
import kotlin.floatArrayOf
import kotlin.intArrayOf
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.plus
import kotlin.synchronized
import kotlin.times
import kotlin.toString
import kotlin.uuid.Uuid

@Suppress("deprecation")
object MainUtil {
    /** Host that [matchesNativeHost] compares against, set by the browser for one site. */
    @JvmField
    var nativeHost: String? = null
    /** [nativeHost] without its prefix (`stripHostPrefix`), for the exact-match case. */
    @JvmField
    var nativeHostBare: String? = null

    @JvmStatic
    fun deleteUri(context: Context?, input: String?): Boolean {
        if (TextUtils.isEmpty(input)) {
            return false
        }
        if (!MainUri.isContentUri(input)) {
            return File(input!!).delete()
        }
        try {
            if (MainUri.isMediaUri(input)) {
                if (!TextUtils.isEmpty(input) && context!!.getContentResolver()
                        .delete(Uri.parse(input), null, null) > 0
                ) {
                    return true
                }
            } else if (!TextUtils.isEmpty(input)) {
                return DocumentsContract.deleteDocument(
                    context!!.getContentResolver(),
                    Uri.parse(input)
                )
            }
        } catch (unused: Exception) {
        }
        return false
    }

    @JvmStatic
    fun parseJson(json: String?): JSONObject? {
        if (TextUtils.isEmpty(json)) {
            return null
        }
        try {
            return JSONObject(json!!)
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun googleRedirectTarget(siteUrl: String?): String? {
        val length = if (siteUrl == null) 0 else siteUrl.length
        val urlStart = if (siteUrl == null || length == 0 || !siteUrl.startsWith("https://www.google.com/")) 0 else siteUrl.indexOf("&url=http", 23)
        if (siteUrl == null || length == 0 || !siteUrl.startsWith("https://www.google.com/") || urlStart < 23 || urlStart + 9 >= length) {
            return siteUrl
        }
        val start = urlStart + 5
        val valueStart = urlStart + 9
        val urlEnd = siteUrl.indexOf(38.toChar(), valueStart)
        val target =
            if (urlEnd <= valueStart || urlEnd >= length) siteUrl.substring(start) else siteUrl.substring(
                start,
                urlEnd
            )
        try {
            return URLDecoder.decode(target, "UTF-8")
        } catch (unused: Exception) {
            return target
        }
    }

    @JvmStatic
    fun areaMultiJs(brightness: Int): String? {
        return getAreaMultiJs(brightness)
        return null
    }

    @JvmStatic
    fun screenBrightnessPercent(context: Context?): Int {
        var brightness: Int
        if (context == null) {
            return 90
        }
        try {
            brightness = Settings.System.getInt(context.getContentResolver(), "screen_brightness", -1)
        } catch (unused: Exception) {
            brightness = -1
        }
        if (brightness == -1) {
            return 90
        }
        return Math.round((brightness / 255.0f) * 100.0f)
    }

    @JvmStatic
    fun requestCamera(activity: Activity?, requestCode: Int): Boolean {
        if (activity == null || ContextCompat.checkSelfPermission(activity, "android.permission.CAMERA") == 0) {
            return false
        }
        ActivityCompat.requestPermissions(activity, arrayOf<String?>("android.permission.CAMERA"), requestCode)
        return true
    }

    @JvmStatic
    fun isHttpUrl(pageUrl: String?): Boolean {
        if (TextUtils.isEmpty(pageUrl)) {
            return false
        }
        return pageUrl!!.startsWith("http://")
    }

    @JvmStatic
    fun parseFloatOr(eventValue: String?, fallback: Float): Float {
        if (!TextUtils.isEmpty(eventValue)) {
            try {
                return eventValue!!.toFloat()
            } catch (unused: Exception) {
            }
        }
        return fallback
    }

    @JvmStatic
    fun applyDisplayCutout(window: Window?, showStatusBar: Boolean, cutoutOn: Boolean) {
        var cutoutOn = cutoutOn
        val index = Build.VERSION.SDK_INT
        val attributes: WindowManager.LayoutParams? = if (index >= 35 || index < 28 || window == null) null else window.getAttributes()
        if (index >= 35 || index < 28 || window == null || attributes == null) {
            return
        }
        var layoutInDisplayCutoutMode = 0
        if (index >= 28) {
            if (!MainConst.isApi30) {
                cutoutOn = !showStatusBar && cutoutOn
            }
            if (cutoutOn) {
                layoutInDisplayCutoutMode = 1
            }
        }
        if (attributes.layoutInDisplayCutoutMode == layoutInDisplayCutoutMode) {
            return
        }
        attributes.layoutInDisplayCutoutMode = layoutInDisplayCutoutMode
        window.setAttributes(attributes)
    }

    @JvmStatic
    fun clearBrowsingData(handler: Handler?, runnable: Runnable?) {
        var canDelete: Boolean
        try {
            canDelete = WebViewFeature.isFeatureSupported("DELETE_BROWSING_DATA")
        } catch (unused: Exception) {
            canDelete = false
        }
        if (!canDelete) {
            if (handler != null) {
                handler.post(runnable!!)
                return
            }
            return
        }
        try {
            val executorServiceNewCachedThreadPool = Executors.newCachedThreadPool()
            val webStorage = WebStorage.getInstance()
            if (WebViewFeature.isFeatureSupported(WebViewFeature.DELETE_BROWSING_DATA)) {
                WebStorageCompat.deleteBrowsingData(
                    webStorage,
                    executorServiceNewCachedThreadPool,
                    runnable!!
                )
            } else {
                handler?.post(runnable!!)
            }

        } catch (unused2: Exception) {
            if (handler != null) {
                handler.post(runnable!!)
            }
        } catch (unused3: NoSuchMethodError) {
            if (handler != null) {
                handler.post(runnable!!)
            }
        }
    }

    @JvmStatic
    fun datePattern(skeleton: String?): String? {
        try {
            return DateFormat.getBestDateTimePattern(MainApp.Companion.currentLocale(), skeleton)
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun hmgPageNumber(url: String?): Int {
        var pageText: String? = null
        val length = if (!MainUtil.isHmgUrl(url!!)) 0 else url.length
        val hashIndex = if (!MainUtil.isHmgUrl(url) || length == 0) 0 else url.lastIndexOf(35.toChar())
        val pageNumber = if (!MainUtil.isHmgUrl(url) || length == 0 || hashIndex < 0) 0 else (hashIndex + 1)
        val pageLength = if (!MainUtil.isHmgUrl(url) || length == 0 || hashIndex < 0 || pageNumber >= length) 0 else (url.substring(pageNumber).also { pageText = it }).length
        if (!MainUtil.isHmgUrl(url) || length == 0 || hashIndex < 0 || pageNumber >= length || pageLength == 0) {
            return -1
        }
        val dashIndex = pageText!!.indexOf(45.toChar())
        if (dashIndex > 0 && dashIndex + 1 <= pageLength) {
            pageText = pageText.substring(0, dashIndex)
        }
        val pageIndex = parseIntSafe(pageText) - 1
        if (pageIndex < 0) {
            return -1
        }
        return pageIndex
    }

    @JvmStatic
    fun areaUpScript(): String? {
        val sb = StringBuilder()
        for (index in 0..1) {
            val areaUpJs = getAreaUpJs(index)
            if (TextUtils.isEmpty(areaUpJs)) {
                return null
            }
            sb.append(areaUpJs)
        }
        return sb.toString()
    }

    @JvmStatic
    fun screenOffTimeout(context: Context?): Int {
        if (context == null) {
            return 0
        }
        try {
            return Settings.System.getInt(context.getContentResolver(), "screen_off_timeout", 0)
        } catch (unused: Exception) {
            return 0
        }
    }

    @JvmStatic
    fun openCustomTab(activity: Activity?, packageName: String?, input: String?): Boolean {
        var packageName = packageName
        try {
            if (TextUtils.isEmpty(packageName)) {
                packageName = "com.android.chrome"
            }
            val builder = CustomTabsIntent.Builder()
            builder.setStartAnimations(activity!!, R.anim.trans_in, R.anim.no_anim)
            // exit animation bundle, colour scheme, share state off, no share menu item, URL bar hiding
            builder.setExitAnimations(
                activity,
                R.anim.no_anim, R.anim.trans_out
            )
            builder.setColorScheme(
                if (AppState.darkWeb) CustomTabsIntent.COLOR_SCHEME_DARK else CustomTabsIntent.COLOR_SCHEME_LIGHT
            )

            builder.setShareState(CustomTabsIntent.SHARE_STATE_OFF)
            builder.setDefaultShareMenuItemEnabled(false)
            builder.setUrlBarHidingEnabled(true)
            val customTabsIntentA = builder.build()
            val intent = customTabsIntentA.intent
            intent.setPackage(packageName)
            intent.setData(Uri.parse(input))
            activity.startActivity(intent, customTabsIntentA.startAnimationBundle)
            return true
        } catch (unused: ActivityNotFoundException) {
            showToast(activity, R.string.apps_none)
            return false
        } catch (unused2: Exception) {
            showToast(activity, R.string.not_supported)
            return false
        }
    }

    @JvmStatic
    fun parseIntSafe(valueText: String?): Int {
        if (TextUtils.isEmpty(valueText)) {
            return 0
        }
        try {
            return valueText!!.toInt()
        } catch (unused: Exception) {
            return 0
        }
    }

    @JvmStatic
    fun applySystemUiFlags(window: Window?, view: View?, fullScreen: Boolean, lightStatus: Boolean, lightNavi: Boolean, keepBars: Boolean) {
        var view = view
        var keepBars = keepBars
        var attributes: WindowManager.LayoutParams? = null
        val uiFlags: Int
        val windowFlags: Int
        val barFlags: Int
        if (window != null) {
            if (view == null) {
                view = window.getDecorView()
            }
            attributes = window.getAttributes()
            val sdkInt = Build.VERSION.SDK_INT
            if (sdkInt >= 28) {
                var layoutInDisplayCutoutMode = 0
                if (sdkInt >= 28) {
                    if (!MainConst.isApi30) {
                        keepBars = !fullScreen && keepBars
                    }
                    if (keepBars) {
                        layoutInDisplayCutoutMode = 1
                    }
                }
                attributes!!.layoutInDisplayCutoutMode = layoutInDisplayCutoutMode
            }
            val systemUiVisibility = view.getSystemUiVisibility()
            val flags = attributes!!.flags
            if (fullScreen) {
                val newFlags = flags and (-1025)
                attributes.flags = newFlags
                uiFlags = systemUiVisibility and (-5)
                if (lightNavi) {
                    windowFlags = newFlags or 67108864
                    attributes.flags = windowFlags
                } else {
                    windowFlags = (-67109889) and flags
                    attributes.flags = windowFlags
                }
            } else {
                uiFlags = systemUiVisibility or 4
                windowFlags = 67109888 or flags
                attributes.flags = windowFlags
            }
            if (lightStatus) {
                attributes.flags = windowFlags
                barFlags = uiFlags and (-4099)
                if (lightNavi) {
                    attributes.flags = windowFlags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
                } else {
                    attributes.flags = windowFlags and (-(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION))
                }
            } else {
                barFlags = uiFlags or 4098
                attributes.flags = windowFlags or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
            }
            val naviFlags = if (lightNavi) barFlags or 1 else barFlags and (-2)
            window.setAttributes(attributes)
            view.setSystemUiVisibility(naviFlags)
        }
    }

    /**
     * The client for pages that are hidden (the previous tab), detached or being closed (2026-11-21): a plain
     * `WebViewClient` there does not handle a dead renderer, and since the pages share one renderer process, its death
     * (memory pressure with many tabs) then took the whole app down. This one handles it like the main client.
     */
    @JvmField
    val quietWebClient: android.webkit.WebViewClient = object : android.webkit.WebViewClient() {
        override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
            onPageRenderGone(view, detail)
            return true
        }
    }

    @JvmStatic
    fun onPageRenderGone(webView: WebView?, renderProcessGoneDetail: RenderProcessGoneDetail?) {
        if (webView == null) {
            return
        }
        try {
            val parent: ViewParent? = if ((webView is WebNestView)) webView.getParent() else null
            if ((webView is WebNestView) && parent != null && (parent is WebNestFrame)) {
                val webNestFrame = parent
                val webNestView = webView
                var didCrash = false
                if (renderProcessGoneDetail != null) {
                    try {
                        didCrash = renderProcessGoneDetail.didCrash()
                    } catch (unused: Exception) {
                    }
                }
                // ended to free memory (not a crash): every tab recovers in place instead of an app restart
                if (!didCrash && com.mycompany.app.web.RendererRecovery.onRendererGone()) return
                webNestFrame.onPageDestroyed(webNestView, !didCrash)
                return
            }
        } catch (unused2: Exception) {
        }
        onRenderGone(webView, renderProcessGoneDetail)
    }

    @JvmStatic
    fun urlDecode(source: String?): String? {
        if (TextUtils.isEmpty(source)) {
            return null
        }
        try {
            return URLDecoder.decode(source, "UTF-8")
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun pathOfUrl(url: String?, keepSlash: Boolean): String? {
        var index: Int
        if (keepSlash && TextUtils.isEmpty(url)) {
            return null
        }
        val schemeIndex = url!!.indexOf("://")
        if (schemeIndex == -1) {
            val firstSlash = url.indexOf(47.toChar())
            if (firstSlash != -1) {
                if (firstSlash > 0) {
                    return url.substring(0, firstSlash)
                }
                index = firstSlash + 1
            }
            return url
        }
        index = schemeIndex + 3
        if (index < url.length) {
            var pathSlash = url.indexOf(47.toChar(), index)
            if (pathSlash == -1) {
                return url.substring(index)
            }
            if (pathSlash == index) {
                index++
                if (index < url.length) {
                    pathSlash = url.indexOf(47.toChar(), index)
                    if (pathSlash == -1) {
                        return url.substring(index)
                    }
                }
            }
            return if (pathSlash <= index) url.substring(index) else url.substring(index, pathSlash)
        }
        return url
    }

    @JvmStatic
    fun legacyBackupKey(index: Int): String? {
        val keyBase: String?
        if (true) {
            if (TextUtils.isEmpty(MainNative.backupKeyCache)) {
                MainNative.backupKeyCache = backupTag
            }
            keyBase = MainNative.backupKeyCache
        } else {
            keyBase = null
        }
        if (index == 0) {
            return keyBase
        }
        if (!TextUtils.isEmpty(keyBase)) {
            return keyBase + index
        }
        return null
    }

    @JvmStatic
    fun timestampName(dateText: String?, datePattern: String?): String {
        val now = System.currentTimeMillis()
        val datePart = formatDate(now, dateText, "yyyyMMdd")
        val timePart = formatDate(now, datePattern, "kkmmss")
        val patternCompile = Pattern.compile("\\p{Punct}")
        return patternCompile.matcher(datePart!!).replaceAll("")
            .replace(" ", "") + "_" + patternCompile.matcher(timePart!!).replaceAll("")
            .replace(" ", "") + "_" + String.format(
            Locale.US, "%03d", now % 1000
        )
    }

    @JvmStatic
    fun pickTreeFolder(mainActivity: android.app.Activity?, input: String?): Boolean {
        if (mainActivity == null) {
            return false
        }
        try {
            val intent = Intent("android.intent.action.OPEN_DOCUMENT_TREE")
            if (!TextUtils.isEmpty(input)) {
                intent.putExtra("android.provider.extra.INITIAL_URI", Uri.parse(input))
            }
            (mainActivity as? ActivityHost)?.launchExternalForResult(intent, RequestCode.SAVE_LOCATION)
            return true
        } catch (unused: ActivityNotFoundException) {
            showToast(mainActivity, R.string.apps_none)
            return false
        } catch (unused2: Exception) {
            showToast(mainActivity, R.string.not_supported)
            return false
        }
    }

    @JvmStatic
    fun parseColorSafe(part: String?): Int {
        if (TextUtils.isEmpty(part)) {
            return -1
        }
        try {
            return part!!.toInt()
        } catch (unused: Exception) {
            return -1
        }
    }

    @JvmStatic
    fun clearStatusBarColor(window: Window?) {
        val index = Build.VERSION.SDK_INT
        if (index >= 35 || index < 30 || window == null) {
            return
        }
        if (windowStatusColor(window) != 1862270976) {
            setWindowStatusColor(window, 1862270976)
        }
        if (navBarColor(window) != 1862270976) {
            setNavBarColor(window, 1862270976)
        }
    }

    @JvmStatic
    fun onRenderGone(webView: WebView?, renderProcessGoneDetail: RenderProcessGoneDetail?) {
        if (webView != null) {
            val context = webView.getContext()
            var didCrash = false
            destroyWebView(webView, false)
            if (renderProcessGoneDetail != null) {
                try {
                    didCrash = renderProcessGoneDetail.didCrash()
                } catch (unused: Exception) {
                }
            }
            if (didCrash || context == null) {
                return
            }
            // a helper web view (page loaders, dialogs) lost the shared renderer: the tabs recover in place
            if (com.mycompany.app.web.RendererRecovery.onRendererGone()) return
            MainApp.Companion.runInBackground(context, RestartActivityTask(context))
        }
    }

    @JvmStatic
    fun screenSize(activity: Activity?): SizeItem? {
        if (activity == null) {
            return null
        }
        val systemBarTypes = WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
        val currentWindowMetrics = activity.getWindowManager().getCurrentWindowMetrics()
        val insetsIgnoringVisibility =
            currentWindowMetrics.getWindowInsets().getInsetsIgnoringVisibility(systemBarTypes)
        val bounds = currentWindowMetrics.getBounds()
        return SizeItem(
            (bounds.width() - insetsIgnoringVisibility.left) - insetsIgnoringVisibility.right,
            (bounds.height() - insetsIgnoringVisibility.top) - insetsIgnoringVisibility.bottom
        )
    }

    @JvmStatic
    fun imageUrlFallback(path: String?, host: String?): String? {
        if (TextUtils.isEmpty(host)) {
            return null
        }
        val trimmedPath = trimSlashes(path)
        if (TextUtils.isEmpty(trimmedPath)) {
            return null
        }
        val parentUrl = MainUtil.parentUrlOf(trimmedPath!!)
        if (TextUtils.isEmpty(parentUrl) || trimmedPath == parentUrl || !isImageExt(fileExtFromName(trimmedPath, false))) {
            return null
        }
        if (path!!.startsWith("//")) {
            return if (host!!.startsWith("https://")) ("https:" + path) else ("http:" + path)
        }
        return "$host/$trimmedPath"
    }

    @JvmStatic
    fun googleLensJs(lensMode: Int): String? {
        return getGoogleLens(lensMode)
        return null
    }

    @JvmStatic
    fun topBarSize(): Int {
        if (TextUtils.isEmpty(MainSettings.topItems.value)) {
            return 0
        }
        return PdfSettings.topHeight.value
    }

    @JvmStatic
    fun openWithChooser(
        mainActivity: android.app.Activity?,
        dir: String?,
        dname: String?,
        url: String?,
        pageUrl: String?,
        name: String?,
        mime: String?
    ): Boolean {
        var mime = mime
        if (!TextUtils.isEmpty(dir) && dir!!.startsWith("isCustomTab:")) {
            return openCustomTab(mainActivity, dir.substring(12), url)
        }
        if (TextUtils.isEmpty(mime)) {
            mime = "*/*"
        }
        if (openWithComponent(mainActivity, dir, dname, url, pageUrl, name, mime)) {
            return true
        }
        return openWithComponent(mainActivity, dir, dname, url, pageUrl, name, null)
    }

    /** True when the image file/URI at `str` is animated (GIF, animated WebP, ...).  */
    @JvmStatic
    fun isAnimatedImage(context: Context?, input: String?): Boolean {
        if (TextUtils.isEmpty(input)) {
            return false
        }
        try {
            val source = if (MainUri.isContentUri(input))
                ImageDecoder.createSource(context!!.getContentResolver(), Uri.parse(input))
            else
                ImageDecoder.createSource(File(input!!))
            val drawable = ImageDecoder.decodeDrawable(source)
            return drawable is AnimatedImageDrawable
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun parseLongSafe(valueText: String?): Long {
        if (TextUtils.isEmpty(valueText)) {
            return 0L
        }
        try {
            return valueText!!.toLong()
        } catch (unused: Exception) {
            return 0L
        }
    }

    @JvmStatic
    fun toggleSystemBars(
        window: Window?,
        view: View?,
        windowInsetsControllerCompat: WindowInsetsControllerCompat?,
        showStatusBar: Boolean,
        showNavBar: Boolean
    ) {
        var windowInsetsControllerCompat = windowInsetsControllerCompat
        if (window == null) {
            return
        }
        if (windowInsetsControllerCompat == null && (insetsController(
                window,
                view
            ).also { windowInsetsControllerCompat = it }) == null
        ) {
            return
        }
        if (showStatusBar) {
            windowInsetsControllerCompat!!.show(androidx.core.view.WindowInsetsCompat.Type.statusBars())
        } else {
            windowInsetsControllerCompat!!.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars())
        }
        if (showNavBar) {
            windowInsetsControllerCompat.show(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
        } else {
            windowInsetsControllerCompat.hide(androidx.core.view.WindowInsetsCompat.Type.navigationBars())
        }
    }

    @JvmStatic
    fun detachWebView(webView: WebView?) {
        if (webView != null) {
            try {
                if (webView is WebNestView) {
                    webView.setDetached(true)
                }
            } catch (unused: Exception) {
            }
            detachFromParent(webView)
            try {
                webView.setWebViewClient(quietWebClient)
                webView.setWebChromeClient(null)
                webView.setDownloadListener(null)
            } catch (unused2: Exception) {
            }
        }
    }

    @JvmStatic
    fun chromeVersion(context: Context?): String {
        var version: String? = MainApp.Companion.chromeVersionCache
        if (TextUtils.isEmpty(version)) {
            initUserAgent(context)
            val defaultUserAgent: String? = MainApp.Companion.defaultUserAgent
            version = null
            val length = if (defaultUserAgent != null) defaultUserAgent.length else 0
            val chromeStart = if (defaultUserAgent != null && length != 0) defaultUserAgent.indexOf("Chrome/") else 0
            val index = if (defaultUserAgent != null && length != 0 && chromeStart >= 0) (chromeStart + 7) else 0
            val versionEnd = if (defaultUserAgent != null && length != 0 && chromeStart >= 0 && index < length) defaultUserAgent.indexOf(32.toChar(), index) else 0
            if (defaultUserAgent != null && length != 0 && chromeStart >= 0 && index < length && versionEnd > index && versionEnd <= length) {
                version = defaultUserAgent.substring(index, versionEnd)
            }
            if (TextUtils.isEmpty(version)) {
                return "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            }
            MainApp.Companion.chromeVersionCache = version
        }
        return "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/$version Safari/537.36"
    }

    @JvmStatic
    fun absoluteUrl(src: String?, base: String?): String? {
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(base)) {
            return null
        }
        if (src!!.startsWith("//")) {
            return if (base!!.startsWith("https://")) ("https:" + src) else ("http:" + src)
        }
        val trimmedSrc = trimSlashes(src)
        if (TextUtils.isEmpty(trimmedSrc)) {
            return null
        }
        return "$base/$trimmedSrc"
    }

    @JvmStatic
    fun searchImageUrl(searchType: Int, query: String?): String? {
        var query = query
        if (TextUtils.isEmpty(query)) {
            return null
        }
        val searchImage = getSearchImage(searchType)
        if (TextUtils.isEmpty(searchImage)) {
            return null
        }
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            if (!TextUtils.isEmpty(encodedQuery)) {
                query = encodedQuery
            }
        } catch (unused: Exception) {
        }
        return if (searchType == 1) String.format(Locale.US, searchImage!!, query, query, query) else "$searchImage$query"
    }

    @JvmStatic
    fun topBarHeight(): Int {
        if (!PdfSettings.topToBottom.value) {
            val barSize = topBarSize()
            val tabBar = BrowserSettings.tabBar.value
            return (if (tabBar == 1 || tabBar == 2) PdfSettings.tabHeight.value else 0) + barSize
        }
        val tabBar = BrowserSettings.tabBar.value
        if (tabBar == 1 || tabBar == 2) {
            return PdfSettings.tabHeight.value
        }
        return 0
    }

    @JvmStatic
    fun openWithComponent(
        mainActivity: android.app.Activity?,
        dir: String?,
        dname: String?,
        input: String?,
        pageUrl: String?,
        name: String?,
        mime: String?
    ): Boolean {
        var input = input
        var pageUrl = pageUrl
        val cookies: String?
        val referer: String?
        if (mainActivity == null || TextUtils.isEmpty(input)) {
            return false
        }
        try {
            val intent = Intent()
            intent.setComponent(ComponentName(dir!!, dname!!))
            if (TextUtils.isEmpty(mime)) {
                intent.setData(Uri.parse(input))
            } else {
                intent.setDataAndType(Uri.parse(input), mime)
            }
            if (dir.startsWith("idm")) {
                if (isGoogleSearch(pageUrl)) {
                    referer = null
                    cookies = null
                } else {
                    if (MainUtil.isGoogleUserContent(input!!)) {
                        pageUrl = input
                    }
                    val cookie = cookieFor(mainActivity, pageUrl)
                    if (!TextUtils.isEmpty(pageUrl)) {
                        input = pageUrl
                    }
                    cookies = cookie
                    referer = input
                }
                if (!TextUtils.isEmpty(name)) {
                    intent.putExtra("extra_filename", name)
                }
                intent.putExtra("extra_useragent", userAgentPref())
                if (!TextUtils.isEmpty(referer)) {
                    intent.putExtra("extra_referer", referer)
                }
                if (!TextUtils.isEmpty(cookies)) {
                    intent.putExtra("extra_cookies", cookies)
                }
            }
            mainActivity.startActivity(intent)
            return true
        } catch (unused: ActivityNotFoundException) {
            if (!TextUtils.isEmpty(mime)) {
                return false
            }
            showToast(mainActivity, R.string.apps_none)
            return false
        } catch (unused2: Exception) {
            if (!TextUtils.isEmpty(mime)) {
                return false
            }
            showToast(mainActivity, R.string.not_supported)
            return false
        }
    }

    @JvmStatic
    fun imageViewerDualPage(mainActivity: MainActivity?): Boolean {
        if (mainActivity != null) {
            mainActivity.isLandscapeScreen()
        }
        return ImageSettings.splitPort.value
    }

    @JvmStatic
    fun parseColorText(valueText: String?): Int {
        var valueText = valueText
        var length = if (valueText != null) valueText.length else 0
        if (valueText != null && length != 0) {
            if (length != 3 || "btc" != valueText) {
                try {
                    if (valueText.get(0) == '#') {
                        val commaIndex = valueText.indexOf(44.toChar())
                        if (commaIndex == 4 || commaIndex == 7) {
                            val hexPart = valueText.substring(0, commaIndex)
                            if (!TextUtils.isEmpty(hexPart)) {
                                length = hexPart.length
                                valueText = hexPart
                            }
                        }
                        if (length == 4) {
                            val red = valueText.substring(1, 2)
                            val green = valueText.substring(2, 3)
                            val blue = valueText.substring(3, 4)
                            valueText =
                                "#" + red + red + green + green + blue + blue
                        }
                    } else if (valueText.startsWith("rgb")) {
                        if (valueText.startsWith("a(", 3)) {
                            val rgbaParts: Array<String?> =
                                valueText.substring(5, length - 1).split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                            if (rgbaParts.size == 4) {
                                val rgbaColor = Color.rgb(
                                    parseIntSafe(rgbaParts[0]!!.trim { it <= ' ' }),
                                    parseIntSafe(rgbaParts[1]!!.trim { it <= ' ' }),
                                    parseIntSafe(rgbaParts[2]!!.trim { it <= ' ' })
                                )
                                if ((!AppState.darkUi || rgbaColor != -16777216) && !isLightColor(rgbaColor)) {
                                    return rgbaColor
                                }
                            }
                        } else if (valueText.startsWith("(", 3)) {
                            val rgbParts: Array<String?> =
                                valueText.substring(4, length - 1).split(",".toRegex())
                                    .dropLastWhile { it.isEmpty() }.toTypedArray()
                            if (rgbParts.size == 3) {
                                val rgbColor = Color.rgb(
                                    parseIntSafe(rgbParts[0]!!.trim { it <= ' ' }),
                                    parseIntSafe(rgbParts[1]!!.trim { it <= ' ' }),
                                    parseIntSafe(rgbParts[2]!!.trim { it <= ' ' })
                                )
                                if ((!AppState.darkUi || rgbColor != -16777216) && !isLightColor(rgbColor)) {
                                    return rgbColor
                                }
                            }
                        }
                    } else if (valueText.startsWith("hsl(")) {
                        val hslParts: Array<String?> =
                            valueText.substring(4, length - 1).split(",".toRegex())
                                .dropLastWhile { it.isEmpty() }.toTypedArray()
                        if (hslParts.size == 3) {
                            var saturation = hslParts[1]!!.trim { it <= ' ' }
                            if (saturation.endsWith("%")) {
                                saturation = saturation.substring(0, saturation.length - 1)
                            }
                            var lightness = hslParts[2]!!.trim { it <= ' ' }
                            if (lightness.endsWith("%")) {
                                lightness = lightness.substring(0, lightness.length - 1)
                            }
                            val hslColor =
                                ColorUtils.HSLToColor(
                                    floatArrayOf(
                                        parseIntSafe(hslParts[0]!!.trim { it <= ' ' }).toFloat(),
                                        parseFloatSafe(saturation),
                                        parseFloatSafe(lightness)
                                    )
                                )
                            if ((!AppState.darkUi || hslColor != -16777216) && !isLightColor(hslColor)) {
                                return hslColor
                            }
                        }
                    }
                    val color = Color.parseColor(valueText)
                    if ((!AppState.darkUi || color != -16777216) && !isLightColor(color)) {
                        return color
                    }
                } catch (unused: Exception) {
                }
            } else if (!AppState.darkUi) {
                return -16777216
            }
        }
        return 0
    }

    @JvmStatic
    fun applySystemBarsMode(
        window: Window?,
        view: View?,
        windowInsetsControllerCompat: WindowInsetsControllerCompat?,
        showStatusBar: Boolean,
        showNavBar: Boolean
    ) {
        var windowInsetsControllerCompat = windowInsetsControllerCompat
        if (window == null) {
            return
        }
        if (windowInsetsControllerCompat == null && (insetsController(
                window,
                view
            ).also { windowInsetsControllerCompat = it }) == null
        ) {
            return
        }
        if (showStatusBar && showNavBar) {
            return
        }
        windowInsetsControllerCompat!!.setSystemBarsBehavior(androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE)
    }

    @JvmStatic
    fun destroyWebView(webView: WebView?, clearCache: Boolean) {
        if (webView == null) {
            return
        }
        detachWebView(webView)
        val mainWebDestroyV: MainWebDestroy? = MainApp.Companion.webDestroy(webView.getContext())
        if (mainWebDestroyV != null) {
            mainWebDestroyV.add(webView)
        } else if (clearCache) {
            Handler(Looper.getMainLooper()).post(DestroyTask(webView))
        } else {
            try {
                webView.destroy()
            } catch (unused: Exception) {
            }
        }
    }

    @JvmStatic
    fun filesSubdir(context: Context?, subdir: String?): String? {
        if (context == null) {
            return null
        }
        val file = File(context.getFilesDir(), subdir!!)
        if (file.isDirectory() || file.mkdir()) {
            return file.getPath()
        }
        return null
    }

    @JvmStatic
    fun stripWww(pageHost: String?): String? {
        val length = if (pageHost == null) 0 else pageHost.length
        if (pageHost == null || length == 0) {
            return null
        }
        if (length > 4 && pageHost.startsWith("www.")) {
            return pageHost.substring(4)
        }
        if (length <= 2 || !pageHost.startsWith(".", 1)) {
            return if (length <= 7 || !pageHost.startsWith("mobile.")) pageHost else pageHost.substring(7)
        }
        return pageHost.substring(2)
    }

    @JvmStatic
    fun snsJs1(index: Int): String? {
        return getSnsJs1(index)
        return null
    }

    @JvmStatic
    fun uniqueFile(file: File?): File? {
        if (!file!!.isDirectory()) {
            return file
        }
        val fileArrListFiles = file.listFiles()
        var found: File? = null
        if (fileArrListFiles != null && fileArrListFiles.size != 0) {
            var lastSize: Long = 0
            for (child in fileArrListFiles) {
                var child: File? = child
                if (child!!.isDirectory()) {
                    child = MainUtil.uniqueFile(child)
                }
                if (child != null) {
                    val length = child.length()
                    if (length > lastSize) {
                        found = child
                        lastSize = length
                    }
                }
            }
        }
        return found
    }

    @JvmStatic
    fun openInExternalApp(activity: Activity?, url: String?): Boolean {
        var resolveInfoResolveActivity: ResolveInfo?
        if (activity == null) {
            return false
        }
        val redirectTarget = googleRedirectTarget(url)
        if (TextUtils.isEmpty(redirectTarget)) {
            return false
        }
        val sdkInt = Build.VERSION.SDK_INT
        if (sdkInt >= 30) {
            try {
                val intent = Intent("android.intent.action.VIEW", Uri.parse(redirectTarget))
                intent.addCategory("android.intent.category.BROWSABLE")
                intent.setFlags(Intent.FLAG_ACTIVITY_REQUIRE_DEFAULT or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER or Intent.FLAG_ACTIVITY_NEW_TASK)
                startSoulActivity(activity, intent)
                return true
            } catch (unused: Exception) {
                return false
            }
        }
        if (!TextUtils.isEmpty(redirectTarget)) {
            try {
                val uri = Intent.parseUri(redirectTarget, Intent.URI_INTENT_SCHEME)
                val packageManager = activity.getPackageManager()
                if (packageManager != null) {
                    var listQueryIntentActivities: MutableList<ResolveInfo?>? = null
                    try {
                        resolveInfoResolveActivity = if (Build.VERSION.SDK_INT >= 33) packageManager.resolveActivity(
                            uri,
                            PackageManager.ResolveInfoFlags.of(0L)
                        ) else packageManager.resolveActivity(uri, 0)
                    } catch (unused2: Exception) {
                        resolveInfoResolveActivity = null
                    }
                    if (resolveInfoResolveActivity == null) {
                        return false
                    }
                    val packageName = resolveInfoResolveActivity.activityInfo.packageName
                    if (!TextUtils.isEmpty(packageName) && (packageName == "android" || packageName == "ch.opumlabs.opumbrowser" || packageName!!.contains(
                            "chrome"
                        ))
                    ) {
                        return false
                    }
                    uri.addCategory("android.intent.category.BROWSABLE")
                    uri.setComponent(null)
                    uri.setSelector(null)
                    try {
                        listQueryIntentActivities =
                            if (Build.VERSION.SDK_INT >= 33) packageManager.queryIntentActivities(
                                uri,
                                PackageManager.ResolveInfoFlags.of(PackageManager.GET_RESOLVED_FILTER.toLong())
                            ) else packageManager.queryIntentActivities(uri, PackageManager.GET_RESOLVED_FILTER)
                    } catch (unused3: Exception) {
                    }
                    if (listQueryIntentActivities == null || listQueryIntentActivities.isEmpty()) {
                        return false
                    }
                    val it = listQueryIntentActivities.iterator()
                    while (it.hasNext()) {
                        val intentFilter = it.next()!!.filter
                        if (intentFilter != null && (intentFilter.countDataAuthorities() != 0 || intentFilter.countDataPaths() != 0)) {
                            return activity.startActivityIfNeeded(uri, -1)
                        }
                    }
                    return false
                }
            } catch (unused4: Exception) {
                return false
            }
        }
        return false
    }

    @JvmStatic
    fun pxToDp(context: Context?, px: Float): Float {
        if (context == null) {
            return 0.0f
        }
        val resources = context.getResources().getDisplayMetrics().density
        return if ((resources).compareTo(0.0f) == 0) px else px / resources
    }

    @JvmStatic
    fun applyScreenRotation(activity: Activity?, lock: Boolean) {
        var resources: Resources? = null
        var darkUi = true
        if (lock) {
            AppState.darkUi = true
            return
        }
        if (if (!BrowserSettings.themeSys.value) BrowserSettings.themeUi.value != 1 else !(activity != null && (activity.getResources()
                .also {
                    resources = it
                }) != null && (resources!!.getConfiguration().uiMode and 48) == 32)
        ) {
            darkUi = false
        }
        AppState.darkUi = darkUi
    }

    @JvmStatic
    fun dpToPx(context: Context?, penSize: kotlin.Float): kotlin.Float {
        if (context == null) {
            return 0.0f
        }
        return penSize * context.getResources().getDisplayMetrics().density
    }

    @JvmStatic
    fun feedbackText(context: Context?, path: String?): String {
        val sb = StringBuilder()
        if (path != null && "file:///android_asset/shortcut.html" != path) {
            sb.append(path)
            sb.append("\n\n")
        }
        sb.append("-- App ")
        sb.append(appVersion(context))
        sb.append(", Android ")
        sb.append(Build.VERSION.RELEASE)
        val webViewHint: Byte = AppState.webViewHint
        if (webViewHint.toInt() == 1) {
            sb.append(". --\n\n")
        } else if (webViewHint.toInt() == 2) {
            sb.append(" --\n\n")
        } else {
            sb.append(".. --\n\n")
        }
        return sb.toString()
    }

    @JvmStatic
    fun hostNameOf(url: String?, stripWwwFlag: Boolean): String? {
        val strippedHost = stripWww(hostOfUrl(url, stripWwwFlag))
        return if (TextUtils.isEmpty(strippedHost)) url else strippedHost
    }

    @JvmStatic
    fun snsJs2(index: Int): String? {
        return getSnsJs2(index)
        return null
    }

    @JvmStatic
    fun nativeDialogJs(message: String?): StringBuilder? {
        if (TextUtils.isEmpty(message)) {
            return null
        }
        val container = MainNative.transContainer()
        if (TextUtils.isEmpty(container)) {
            return null
        }
        val elementSel = MainNative.transElementSel()
        if (TextUtils.isEmpty(elementSel)) {
            return null
        }
        val sb = StringBuilder()
        sb.append("var gtb=document.querySelector('")
        sb.append(container)
        sb.append("');if(gtb){var gte=")
        sb.append(elementSel)
        sb.append(".querySelector(\"[id*='")
        sb.append(message)
        sb.append("']\");if(gte){gte.click();}}")
        return sb
    }

    @JvmStatic
    fun pickContent(mainActivity: android.app.Activity?, type: Int, multiple: Boolean, requestCode: Int): Boolean {
        var mimeType: String?
        if (mainActivity == null) {
            return false
        }
        if (type == 4) {
            mimeType = "image/*"
        } else if (type == 5) {
            mimeType = "video/*"
        } else if (type == 6) {
            mimeType = "audio/*"
        } else {
            mimeType = if (type == 7) "text/*" else null
        }
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "*/*"
        }
        try {
            val intent = Intent("android.intent.action.OPEN_DOCUMENT")
            intent.addCategory("android.intent.category.OPENABLE")
            intent.setType(mimeType)
            intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", multiple)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            (mainActivity as? ActivityHost)?.launchExternalForResult(intent, requestCode)
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun isInsideView(rawX: Int, rawY: Int, slop: Int, view: View?): Boolean {
        if (view != null) {
            var location: IntArray? = MainApp.Companion.locationBuffer
            if (location == null) {
                location = IntArray(2)
                MainApp.Companion.locationBuffer = location
            }
            view.getLocationOnScreen(location)
            var index = location[0]
            var top = location[1]
            var width = view.getWidth() + index
            var height = view.getHeight() + top
            if (slop != 0) {
                index -= slop
                width += slop
                top -= slop
                height += slop
            }
            if (index < width && top < height && rawX >= index && rawX < width && rawY >= top && rawY < height) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun randomBetween(low: Int, size: Int): Int {
        return (Random().nextInt(size - low) + low) % (size + 1)
    }

    @JvmStatic
    fun evalConfirmJs(webView: WebView?, message: String?) {
        val sbG = nativeDialogJs(message)
        if (sbG == null) {
            return
        }
        sbG.insert(0, "(function(){")
        sbG.append("})();")
        evalJs(webView, sbG.toString(), false)
    }

    @JvmStatic
    fun drawableToBitmap(size: Int, drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        var heightValue: Int = 0
        try {
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.getBitmap()
                if (isBitmapOk(bitmap)) {
                    if (size > 0) {
                        val width = bitmap!!.getWidth()
                        val height = bitmap.getHeight()
                        if (width > size || height > size) {
                            val scale = (min(width, height) / size).toFloat()
                            var scaledWidth = Math.round(width / scale)
                            val scaledHeight = Math.round(height / scale)
                            if (scaledWidth == 0) {
                                scaledWidth = 1
                            }
                            val bitmapH = scaleBitmap(scaledWidth, if (scaledHeight != 0) scaledHeight else 1, bitmap)
                            if (isBitmapOk(bitmapH)) {
                                return bitmapH
                            }
                        }
                    }
                    return bitmap
                }
            }
            var intrinsicWidth = drawable.getIntrinsicWidth()
            val intrinsicHeight = drawable.getIntrinsicHeight()
            if (intrinsicWidth > 0 && intrinsicHeight > 0) {
                if (size <= 0 || (intrinsicWidth <= size && intrinsicHeight <= size)) {
                    heightValue = intrinsicHeight
                } else {
                    val intrinsicScale = (min(intrinsicWidth, intrinsicHeight) / size).toFloat()
                    var scaledIntrinsicWidth = Math.round(intrinsicWidth / intrinsicScale)
                    val scaledIntrinsicHeight = Math.round(intrinsicHeight / intrinsicScale)
                    if (scaledIntrinsicWidth == 0) {
                        scaledIntrinsicWidth = 1
                    }
                    heightValue = if (scaledIntrinsicHeight != 0) scaledIntrinsicHeight else 1
                    intrinsicWidth = scaledIntrinsicWidth
                }
                val bitmapCreateBitmap =
                    Bitmap.createBitmap(intrinsicWidth, heightValue, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmapCreateBitmap)
                drawable.setBounds(0, 0, intrinsicWidth, heightValue)
                drawable.draw(canvas)
                return bitmapCreateBitmap
            }
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
        return null
    }

    @JvmStatic
    fun pointDistance(touchX: kotlin.Float, rawX: kotlin.Float, touchY: kotlin.Float, rawY: kotlin.Float): kotlin.Float {
        val dx = touchX - rawX
        val dy = touchY - rawY
        return sqrt(((dy * dy) + (dx * dx)).toDouble()).toFloat()
    }

    @JvmStatic
    fun hostOfUrl(input: String?, stripWww: Boolean): String? {
        if (stripWww && TextUtils.isEmpty(input)) {
            return null
        }
        var schemeEnd = 8
        if (!input!!.startsWith("https://")) {
            if (input.startsWith("http://")) {
                schemeEnd = 7
            } else if (!input.startsWith("file:///")) {
                if (!input.startsWith("file://")) {
                    if (input.startsWith("data:")) {
                        return null
                    }
                    return Uri.parse(input).getHost()
                }
                schemeEnd = 7
            }
        }
        if (schemeEnd >= input.length) {
            return null
        }
        val pathSlash = input.indexOf(47.toChar(), schemeEnd)
        return if (pathSlash <= schemeEnd) input.substring(schemeEnd) else input.substring(schemeEnd, pathSlash)
    }

    @JvmStatic
    fun snsUrl(index: Int): String? {
        return getSnsUrl(index)
        return null
    }

    @JvmStatic
    fun langPrefix(text: String?): String {
        val length = if (text == null) 0 else text.length
        val dashIndex = if (text == null || length < 5) 0 else text.indexOf(45.toChar())
        val langEnd = if (text == null || length < 5 || dashIndex <= 0) 0 else (dashIndex + 1)
        if (text == null || length < 5 || dashIndex <= 0 || langEnd >= length) {
            return "-"
        }
        val prefix = text.substring(0, langEnd)
        return if (TextUtils.isEmpty(prefix)) "-" else prefix
    }

    @JvmStatic
    fun pickImages(requestCode: Int, mainActivity: android.app.Activity?, multiple: Boolean): Boolean {
        if (mainActivity == null) {
            return false
        }
        try {
            val intent =
                Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", multiple)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            (mainActivity as? ActivityHost)?.launchExternalForResult(intent, requestCode)
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun isRootPath(pageUrl: String?): Boolean {
        var slashIndex: Int = 0
        val host = hostOf(pageUrl, true)
        var length = if (host == null) 0 else host.length
        if (host == null || length == 0) {
            return false
        }
        val schemeEnd = length + 1
        val pageUrlSize = pageUrl!!.length
        return schemeEnd >= pageUrlSize || (pageUrl.indexOf(47.toChar(), schemeEnd)
            .also { slashIndex = it }) == -1 || slashIndex + 1 == pageUrlSize
    }

    @JvmStatic
    fun releaseRetriever(mediaMetadataRetriever: MediaMetadataRetriever?) {
        try {
            mediaMetadataRetriever!!.release()
        } catch (unused: Exception) {
        }
        try {
            com.mycompany.app.outline.LegacyViews.release(mediaMetadataRetriever)
        } catch (unused2: Exception) {
        }
    }

    @JvmStatic
    fun evalLongPressJs(webView: WebView?, js: String?) {
        val sbG: StringBuilder? = if (webView == null || TextUtils.isEmpty(js)) null else nativeDialogJs("confirm")
        if (webView == null || TextUtils.isEmpty(js) || sbG == null) {
            return
        }
        val script = sbG.toString()
        if (TextUtils.isEmpty(script)) {
            return
        }
        val sb = StringBuilder()
        sb.append("changeLang('")
        sb.append(js)
        sb.append("');")
        sb.append(script)
        sb.insert(0, "(function(){")
        sb.append("})();")
        evalJs(webView, sb.toString(), true)
    }

    @JvmStatic
    fun evalJs(webView: WebView?, pendingJs: String?, now: Boolean) {
        if (BrowserSettings.enableJs.value) {
            evalJsNow(webView, pendingJs, now)
        }
    }

    @JvmStatic
    fun userAgentPref(): String {
        val agent = ZtwoSettings.soulAgent.value
        return (if (!TextUtils.isEmpty(agent)) agent else "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) OPUM/4.0 Chrome/131.0.0.0 Mobile Safari/537.36")!!
    }

    @JvmStatic
    fun hostOf(url: String?, stripWww: Boolean): String? {
        if (stripWww && TextUtils.isEmpty(url)) {
            return null
        }
        var schemeEnd = 8
        if (!url!!.startsWith("https://")) {
            if (url.startsWith("http://")) {
                schemeEnd = 7
            } else if (!url.startsWith("file:///")) {
                if (url.startsWith("file://")) {
                    schemeEnd = 7
                } else {
                    schemeEnd = 0
                }
            }
        }
        if (schemeEnd == 0 || schemeEnd >= url.length) {
            return null
        }
        val pathSlash = url.indexOf(47.toChar(), schemeEnd)
        if (pathSlash == -1) {
            return url
        }
        if (pathSlash > schemeEnd) {
            return url.substring(0, pathSlash)
        }
        return null
    }

    @JvmStatic
    fun nativeJsN(): String? {
        if (TextUtils.isEmpty(MainNative.transCsp)) {
            MainNative.transCsp = transCsp
        }
        return MainNative.transCsp
    }

    @JvmStatic
    fun transLangFromCookie(cookie: String?): String? {
        var length = if (cookie == null) 0 else cookie.length
        val transStart = if (cookie == null || length == 0) 0 else cookie.indexOf("googtrans")
        val langStart = if (cookie == null || length == 0 || transStart == -1) 0 else (transStart + 10)
        if (cookie == null || length == 0 || transStart == -1 || langStart >= length) {
            return null
        }
        val valueEnd = cookie.indexOf(59.toChar(), langStart)
        if (valueEnd != -1) {
            length = valueEnd
        } else if (cookie.endsWith("\"")) {
            length--
        }
        val lastSlash = cookie.lastIndexOf(47.toChar(), length)
        val codeStart = if (lastSlash == -1) 0 else (lastSlash + 1)
        if (lastSlash == -1 || codeStart >= length) {
            return null
        }
        if (!TextUtils.isEmpty(AlbumSettings.transCode.value) && AlbumSettings.transCode.value!!.length == length - codeStart && cookie.startsWith(
                AlbumSettings.transCode.value!!,
                codeStart
            )
        ) {
            return null
        }
        return cookie.substring(codeStart, length)
    }

    @JvmStatic
    fun getContent(mainActivity: android.app.Activity?, type: Int, multiple: Boolean, requestCode: Int): Boolean {
        val mimeType: String?
        if (mainActivity == null) {
            return false
        }
        if (type == 4) {
            mimeType = "image/*"
        } else if (type == 5) {
            mimeType = "video/*"
        } else if (type == 6) {
            mimeType = "audio/*"
        } else {
            mimeType = if (type == 7) "text/*" else null
        }
        try {
            val intent = Intent("android.provider.action.PICK_IMAGES")
            if (!TextUtils.isEmpty(mimeType)) {
                intent.setType(mimeType)
            }
            if (multiple) {
                intent.putExtra("android.provider.extra.PICK_IMAGES_MAX", 100)
            }
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            (mainActivity as? ActivityHost)?.launchExternalForResult(intent, requestCode)
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun isFilmizle(pageHost: String?): Boolean {
        if (!TextUtils.isEmpty(pageHost) && pageHost!!.contains("filmizle")) {
            return pageHost.contains("4k") || pageHost.contains("sene")
        }
        return false
    }

    @JvmStatic
    fun applyRotationLock(exitedVideoType: Int, mainActivity: MainActivity?, videoWasImage: Boolean) {
        if (mainActivity != null) {
            var orientation = 6
            if (!videoWasImage || exitedVideoType == 6) {
                val rotation = MainSettings.appRotation.value
                if (rotation == 1) {
                    orientation = 1
                } else if (rotation != 2) {
                    orientation = -1
                }
                try {
                    if (orientation == mainActivity.getRequestedOrientation()) {
                        return
                    }
                } catch (unused: Exception) {
                }
                setActivityOrientation(mainActivity, orientation)
            }
        }
    }

    @JvmStatic
    fun checkTransClassJs(webView: WebView?) {
        if (webView == null) {
            return
        }
        val container = MainNative.transContainer()
        if (TextUtils.isEmpty(container)) {
            return
        }
        evalJs(
            webView,
            "(function(){var tag='onTransClass';var val=0;var gtb=document.querySelector('" + container + "');if(gtb){val=gtb.className;}android.onJsResult(tag,val);})();",
            true
        )
    }

    @JvmStatic
    fun evalJsNow(webView: WebView?, pendingJs: String?, now: Boolean) {
        if (webView == null || pendingJs == null) {
            return
        }
        if (!now) {
            webView.evaluateJavascript(pendingJs, null)
            return
        }
        val mainWebDestroy: MainWebDestroy? = if ((webView is WebNestView)) webView.webDestroy else null
        if ((webView is WebNestView) && mainWebDestroy != null) {
            mainWebDestroy.busyChecked = true
        }
        webView.post(EvaluateJavascriptTask(webView, pendingJs))
    }

    @JvmStatic
    fun downFilePath(context: Context?, path: String?): String? {
        if (context == null || TextUtils.isEmpty(path)) {
            return null
        }
        return filesSubdir(context, ".down") + "/" + Md5FileNameGenerator().generate(path)
    }

    @JvmStatic
    fun siteNameOf(tabTitle: String?, stripWww: Boolean): String? {
        var hostName = hostNameOf(tabTitle, stripWww)
        val length = if (hostName == null) 0 else hostName.length
        if (hostName == null || length == 0) {
            return tabTitle
        }
        var lastDot = hostName.lastIndexOf(46.toChar())
        if (lastDot > 0 && lastDot < length && length - lastDot < 5) {
            val prevDot = hostName.lastIndexOf(46.toChar(), lastDot - 1)
            if (prevDot > 0 && prevDot < lastDot && lastDot - prevDot < 4) {
                lastDot = prevDot
            }
            val withoutTld = hostName.substring(0, lastDot)
            if (!TextUtils.isEmpty(withoutTld)) {
                hostName = withoutTld
            }
        }
        val upperLabel = hostLabelUpper(hostName)
        return if (!TextUtils.isEmpty(upperLabel)) upperLabel else hostName
    }

    /**
     * Ad-hiding CSS selector list assembled from the 30 native parts, with
     * "div[class*=\"cls\"]," inserted before part 20 when a site class name (longer than 2) is given.
     */
    @JvmStatic
    fun classSelectorJs(cls: String?): String? {
        val parts = nativeParts(30, 0)
        if (parts == null) {
            return null
        }
        val sb = StringBuilder()
        for (index in 0..29) {
            val part = parts.get(index) as String?
            if (TextUtils.isEmpty(part)) {
                return null
            }
            if (index == 20 && cls != null && cls.length > 2) {
                sb.append("div[class*=\"").append(cls).append("\"],")
            }
            sb.append(part)
        }
        return sb.toString()
    }

    /**
     * Native string parts (set 0 getTransTag, 1 getTtsMarkJs, 2 getYouJs1, 3 getYouJs2); cached in
     * MainNative. Null without the native library or when a part is missing.
     */
    private fun nativeParts(count: Int, partSet: Int): ArrayList<Any?>? {
        val cached =
            if (partSet == 0) MainNative.nativeParts0 else if (partSet == 1) MainNative.nativeParts1 else if (partSet == 2) MainNative.nativeParts2 else MainNative.nativeParts3
        if (MainNative.partsReady(count, cached)) {
            return cached
        }
        val parts: ArrayList<Any?> = ArrayList<Any?>()
        for (index in 0..<count) {
            val part =
                if (partSet == 0) getTransTag(index) else if (partSet == 1) getTtsMarkJs(index) else if (partSet == 2) getYouJs1(
                    index
                ) else getYouJs2(index)
            if (TextUtils.isEmpty(part)) {
                return null
            }
            parts.add(part)
        }
        if (partSet == 0) {
            MainNative.nativeParts0 = parts
        } else if (partSet == 1) {
            MainNative.nativeParts1 = parts
        } else if (partSet == 2) {
            MainNative.nativeParts2 = parts
        } else {
            MainNative.nativeParts3 = parts
        }
        return parts
    }

    private fun joinNative(count: Int, partSet: Int): String? {
        val parts = nativeParts(count, partSet)
        if (parts == null || parts.size != count) {
            return null
        }
        val sb = StringBuilder()
        for (part in parts) {
            if (TextUtils.isEmpty(part as String?)) {
                return null
            }
            sb.append(part as String)
        }
        return sb.toString()
    }

    @JvmStatic
    fun injectTransJs(webView: WebView?) {
        val script: String?
        if (webView == null) {
            return
        }
        val elementSel = MainNative.transElementSel()
        if (TextUtils.isEmpty(elementSel)) {
            return
        }
        if (true) {
            if (TextUtils.isEmpty(MainNative.transLang)) {
                MainNative.transLang = transLang
            }
            script = MainNative.transLang
        } else {
            script = null
        }
        if (TextUtils.isEmpty(script)) {
            return
        }
        evalJs(
            webView,
            "(function(){var tag='onTransList';var val=null;if(setLangId(document,window)){var gtb=document.querySelector(\"$script\");if(gtb){val=$elementSel.innerHTML;}}android.onJsResult(tag,val);})();",
            true
        )
    }

    @JvmStatic
    fun quickViewPdf(activity: Activity?, input: String?): Boolean {
        try {
            val intent = Intent("android.intent.action.QUICK_VIEW")
            intent.setDataAndType(Uri.parse(input), "application/pdf")
            activity!!.startActivity(intent)
            return true
        } catch (unused: Exception) {
            showToast(activity, R.string.not_support_device)
            return false
        }
    }

    @JvmStatic
    fun isKoreanLocale(): Boolean {
        val localeI: Locale = MainApp.Companion.currentLocale()
        return "ko".equals(localeI.getLanguage(), ignoreCase = true)
    }

    @JvmStatic
    fun reloadWithDesktopMode(webNestView: WebNestView?, exitedVideoType: Int, videoWasImage: Boolean) {
        if (webNestView != null) {
            if (!videoWasImage) {
                MainUtil.injectWindowOpenJs(webNestView, exitedVideoType, webNestView.getUrl(), false)
            } else if (lacksAlgorithmicDark() && AppState.darkWeb) {
                val url = webNestView.getUrl()
                MainUtil.applyDesktopMode(webNestView, url!!, MainUtil.hostOf(url, true), true)
            }
        }
    }

    @JvmStatic
    fun checkTransJs(webView: WebView?, tag: String?) {
        if (webView == null || TextUtils.isEmpty(tag)) {
            return
        }
        val container = MainNative.transContainer()
        if (TextUtils.isEmpty(container)) {
            return
        }
        evalJs(
            webView,
            "(function(){var tag='$tag';var val=0;var gtb=document.querySelector('$container');if(gtb){val=2;}else if(document.getElementById('sb_trs_style')){val=1;}android.onJsResult(tag,val);})();",
            true
        )
    }

    @JvmStatic
    fun cancelNotifications(context: Context?, cancelDown: Boolean, cancelOther: Boolean) {
        try {
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(context)
            if (notificationManagerP != null) {
                if (cancelDown) {
                    notificationManagerP.cancel(2147483642)
                }
                notificationManagerP.cancel(2147483641)
                notificationManagerP.cancel(2147483639)
            }
            if (TtsSettings.checkBack.value) {
                TtsSettings.editLocal { set(TtsSettings.checkBack, false) }
                putBoolNow(12, context, "mCheckBack", false)
            }
        } catch (unused: Exception) {
        }
    }

    @JvmStatic
    fun gifUriFor(context: Context?, downItem: DownItem?): UriItem? {
        val uriItem: UriItem? = if (downItem != null) downItem.uriItem else null
        if (downItem != null && uriItem != null) {
            try {
                val name = uriItem.name
                if ("jpg" == lowerExtOf(name) && ("jpg" != lowerExtOf(guessDownloadName(downItem.url, null, null, true))) && isAnimatedImage(
                        context,
                        downItem.path
                    )
                ) {
                    val uriItemC = MainUri.createUri(context, PrefPath.downFolder(context), null, stripExtension(name) + ".gif")
                    if (uriItemC != null) {
                        if (moveFileOrUri(context, downItem.path, uriItemC.uriString)) {
                            return uriItemC
                        }
                        deleteUri(context, uriItemC.uriString)
                    }
                }
            } catch (unused: Exception) {
            }
        }
        return null
    }

    @JvmStatic
    fun translateUrlFor(pageUrl: String?): String {
        return "https://" + hostOfUrl(pageUrl, true) + "/" + appLocaleTag()
    }

    /** Read-aloud word-marking script from 14 native parts.  */
    @JvmStatic
    fun nativeJs14(): String? {
        return joinNative(14, 1)
    }

    @JvmStatic
    fun transScriptJs(): StringBuilder? {
        val tagPart: String?
        val promptLang: String?
        val finishSection: String?
        val finishLang: String?
        val trId: String?
        val spId: String?
        val container = MainNative.transContainer()
        if (!TextUtils.isEmpty(container)) {
            val elementSel = MainNative.transElementSel()
            if (!TextUtils.isEmpty(elementSel)) {
                if (true) {
                    if (TextUtils.isEmpty(MainNative.transPromptSection)) {
                        MainNative.transPromptSection = transPromptSection
                    }
                    tagPart = MainNative.transPromptSection
                } else {
                    tagPart = null
                }
                if (!TextUtils.isEmpty(tagPart)) {
                    if (true) {
                        if (TextUtils.isEmpty(MainNative.transPromptLang)) {
                            MainNative.transPromptLang = transPromptLang
                        }
                        promptLang = MainNative.transPromptLang
                    } else {
                        promptLang = null
                    }
                    if (!TextUtils.isEmpty(promptLang)) {
                        if (true) {
                            if (TextUtils.isEmpty(MainNative.transFinishSection)) {
                                MainNative.transFinishSection = transFinishSection
                            }
                            finishSection = MainNative.transFinishSection
                        } else {
                            finishSection = null
                        }
                        if (!TextUtils.isEmpty(tagPart)) {
                            if (true) {
                                if (TextUtils.isEmpty(MainNative.transFinishLang)) {
                                    MainNative.transFinishLang = transFinishLang
                                }
                                finishLang = MainNative.transFinishLang
                            } else {
                                finishLang = null
                            }
                            if (!TextUtils.isEmpty(promptLang)) {
                                if (true) {
                                    if (TextUtils.isEmpty(MainNative.transTrId)) {
                                        MainNative.transTrId = transTrId
                                    }
                                    trId = MainNative.transTrId
                                } else {
                                    trId = null
                                }
                                if (!TextUtils.isEmpty(trId)) {
                                    if (true) {
                                        if (TextUtils.isEmpty(MainNative.transSpId)) {
                                            MainNative.transSpId = transSpId
                                        }
                                        spId = MainNative.transSpId
                                    } else {
                                        spId = null
                                    }
                                    if (!TextUtils.isEmpty(spId)) {
                                        val sb = StringBuilder()
                                        sb.append("function myF1(ele){var gtb=ele.querySelector('")
                                        sb.append(container)
                                        sb.append("');if(gtb){var gte=")
                                        sb.append(elementSel)
                                        sb.append(";if(gte&&gte.innerHTML){android.onObserDet(null,0);return gte;}}return null;}function myPs1(ele,win){var gtv=ele.querySelector(\"")
                                        sb.append(trId).append("'").append(tagPart).append("']\");if(gtv){myPs4(ele,win);if(myPs3(gtv))return true;}return false;}function myPs2(ele,win){if(ele.id.includes('")
                                        sb.append(tagPart).append("')){myPs4(ele,win);if(myPs3(ele))return true;}return false;}function myPs3(ele){var gtt=ele.querySelector(\"").append(spId).append("'")
                                        sb.append(promptLang).append("']\");if(gtt&&(gtt.innerText.length>0)){android.onObserDet(gtt.innerText,1);return true;}return false;}function myPs4(ele,win){if(!win.sb_ps){win.sb_ps=new MutationObserver(e=>{for(var i=0;i<e.length;i++){if(myPs3(e[i].target))break;if(myPs3(e[i].target.parentNode))break;}}),win.sb_ps.observe(ele,{childList:!0,subtree:!0});}}function myFs1(ele,win){var gtv=ele.querySelector(\"").append(trId).append("'")
                                        sb.append(finishSection).append("']\");if(gtv){myFs4(ele,win);if(myFs3(gtv))return true;}return false;}function myFs2(ele,win){if(ele.id.includes('").append(finishSection).append("')){myFs4(ele,win);if(myFs3(ele))return true;}return false;}function myFs3(ele){var gtt=ele.querySelector(\"")
                                        sb.append(spId).append("'").append(finishLang).append("']\");if(gtt&&(gtt.innerText.length>0)){android.onObserDet(gtt.innerText,2);return true;}return false;}function myFs4(ele,win){if(!win.sb_fs){win.sb_fs=new MutationObserver(e=>{for(var i=0;i<e.length;i++){if(myFs3(e[i].target))break;if(myFs3(e[i].target.parentNode))break;}}),win.sb_fs.observe(ele,{childList:!0,subtree:!0,attributeOldValue:0});}}function myF5(ele,win){if(!win.sb_tr2){win.sb_tr2=new MutationObserver(e=>{for(var i=0;i<e.length;i++){if(myPs1(e[i].target,win))break;if(myPs2(e[i].target.parentNode,win))break;if(myFs1(e[i].target,win))break;if(myFs2(e[i].target.parentNode,win))break;}}),win.sb_tr2.observe(ele,{childList:!0,subtree:!0});}}function myF6(ele,win){if(!ele||!win)return false;var gte=myF1(ele);if(!gte)return false;myPs1(gte,win);myFs1(gte,win);myF5(gte,win);return true;}(function(){if(myF6(document,window))return;if(!document.body||window.sb_tr1)return;window.sb_tr1=new MutationObserver(e=>{for(var i=0;i<e.length;i++){var tgt=e[i].target;if(tgt.tagName=='BODY'){if(myF6(tgt,window)){if(window.sb_tr1){window.sb_tr1.disconnect();window.sb_tr1=null;}}break;}}}),window.sb_tr1.observe(document.body,{childList:!0,subtree:!0});})();")
                                        return sb
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    @JvmStatic
    fun openPermissionScreen(activity: Activity?, requestCode: Int): Boolean {
        if (activity == null) {
            return false
        }
        val intent = Intent(activity, MainPermission::class.java as Class<*>)
        intent.putExtra("EXTRA_TYPE", requestCode)
        activity.startActivity(intent)
        return true
    }

    @JvmStatic
    fun isLandscape(context: Context?): Boolean {
        val pointP = screenPoint(context)
        return pointP != null && pointP.x > pointP.y
    }

    @JvmStatic
    fun destroyWebViewNow(webView: WebView?) {
        if (webView == null) {
            return
        }
        destroyWebView(webView, true)
    }

    @JvmStatic
    fun parseTransResult(context: Context?, html: String?) {
        var document: Document?
        var arrayList: ArrayList<String?>? = null
        try {
            document = Jsoup.parse(html!!)
        } catch (unused: Exception) {
            document = null
        } catch (unused: LinkageError) {
            document = null
        } catch (unused: OutOfMemoryError) {
            document = null
        }
        if (document != null) {
            try {
                val elementsSelect = document.select("span.text")
                if (elementsSelect.size != 0) {
                    val size = elementsSelect.size
                    var index = 0
                    while (index < size) {
                        val rawElement: Any? = elementsSelect.get(index)
                        index++
                        val element = rawElement as Element?
                        if (element != null) {
                            val elementText = element.text()
                            if (!TextUtils.isEmpty(elementText)) {
                                if (arrayList == null) {
                                    arrayList = ArrayList()
                                }
                                arrayList.add(elementText)
                            }
                        }
                    }
                }
            } catch (unused2: Exception) {
            }
            val dataTransA = DataTrans.instance(context)
            dataTransA.javaClass
            if (arrayList != null) {
                try {
                    if (arrayList.isEmpty()) {
                        return
                    }
                    val languages = dataTransA.languages
                    if (languages == null || languages.isEmpty()) {
                        dataTransA.languages = arrayList
                        if (!TextUtils.isEmpty(AlbumSettings.transPick.value) && !arrayList.contains(AlbumSettings.transPick.value)) {
                            AlbumSettings.editLocal { set(AlbumSettings.transPick, "") }
                            AlbumSettings.editLocal { set(AlbumSettings.transCode, "") }
                            saveTrans(context)
                        }
                        if (TextUtils.isEmpty(ZtwoSettings.newsPick.value) || arrayList.contains(ZtwoSettings.newsPick.value)) {
                            return
                        }
                        ZtwoSettings.edit(context) { set(ZtwoSettings.newsPick, "") }
                    }
                } catch (unused3: Exception) {
                }
            }
        }
    }

    @JvmStatic
    fun urlFromText(text: String?): String? {
        val httpIndex = if (!TextUtils.isEmpty(text)) text!!.indexOf("http") else 0
        if (!TextUtils.isEmpty(text) && httpIndex > 0 && httpIndex < text!!.length) {
            val urlText = text.substring(httpIndex)
            if (!TextUtils.isEmpty(urlText)) {
                return urlText
            }
        }
        return text
    }

    @JvmStatic
    fun httpsUrl(host: String?, localeTag: String?): String {
        var localeTag = localeTag
        if (TextUtils.isEmpty(localeTag)) {
            localeTag = appLocaleTag()
        }
        return "https://$host$localeTag"
    }

    @JvmStatic
    fun navBarColor(window: Window?): Int {
        if (Build.VERSION.SDK_INT < 35 && window != null) {
            return window.getNavigationBarColor()
        }
        return 0
    }

    @JvmStatic
    fun parseLocale(lang: String?): Locale? {
        val tag: String?
        var localeOf: Locale?
        if (TextUtils.isEmpty(lang)) {
            return null
        }
        if (hasChar(95, lang)) {
            tag = "_"
        } else {
            tag = if (hasChar(45, lang)) "-" else null
        }
        val parts: Array<String?>? = if (TextUtils.isEmpty(lang) || TextUtils.isEmpty(tag)) null else lang!!.split(tag!!.toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray()
        val length = if (TextUtils.isEmpty(lang) || TextUtils.isEmpty(tag) || parts == null) 0 else parts.size
        if (TextUtils.isEmpty(lang) || TextUtils.isEmpty(tag) || parts == null || length == 0) {
            localeOf = null
        } else {
            val language = parts[0]
            if (TextUtils.isEmpty(language)) {
                localeOf = null
            } else {
                var country = if (length > 1) parts[1] else null
                var variant: String? = ""
                if (country == null) {
                    country = ""
                }
                val variantPart = if (length > 2) parts[2] else null
                if (variantPart != null) {
                    variant = variantPart
                }
                try {
                    localeOf =
                        if (Build.VERSION.SDK_INT >= 36) Locale.of(language!!, country, variant!!) else Locale(
                            language!!,
                            country,
                            variant!!
                        )
                } catch (unused: Exception) {
                    localeOf = null
                }
            }
        }
        if (localeOf != null) {
            return localeOf
        }
        try {
            return if (Build.VERSION.SDK_INT >= 36) Locale.of(lang!!) else Locale(lang!!)
        } catch (unused2: Exception) {
            return null
        }
    }

    @JvmStatic
    fun requestStoragePermission(mainActivity: android.app.Activity?, requestCode: Int) {
        if (getContent(mainActivity, 4, false, requestCode) || pickImages(requestCode, mainActivity, false) || pickContent(
                mainActivity,
                4,
                false,
                requestCode
            )
        ) {
            return
        }
        showToast(mainActivity, R.string.apps_none)
    }

    @JvmStatic
    fun isRtl(context: Context?): Boolean {
        return context != null && context.getResources().getConfiguration()
            .getLayoutDirection() == 1
    }

    @JvmStatic
    fun clearCookies(context: Context?) {
        try {
            val cookieManagerL: CookieManager? = MainApp.Companion.cookieManager(context)
            if (cookieManagerL != null) {
                cookieManagerL.removeAllCookies(FlushCookiesCallback(context))
            }
        } catch (unused: Exception) {
        } catch (unused: NoSuchMethodError) {
        }
    }

    @JvmStatic
    fun setUserAgent(webSettings: WebSettings?, agent: String?) {
        if (webSettings == null) {
            return
        }
        try {
            webSettings.setUserAgentString(agent)
        } catch (unused: Exception) {
        }
    }

    @JvmStatic
    fun themeColorAt(id: Int, extraTint: Int): Int {
        if (extraTint == 0) {
            if (id >= 0) {
                val blackIcons = MainConst.moreIconsBlack
                if (id < blackIcons.size) {
                    return blackIcons[id]
                }
            }
            return MainConst.mainMenuIconsBlack[0]
        }
        if (id >= 0) {
            val darkIcons = MainConst.moreIconsDark
            if (id < darkIcons.size) {
                return darkIcons[id]
            }
        }
        return MainConst.mainMenuIconsDark[0]
    }

    @JvmStatic
    fun mixUrlOf(url: String?): String? {
        val length = if (url != null) url.length else 0
        if (url != null && length >= 5 && url.startsWith("mix:")) {
            val separator = url.indexOf("<,>", 4)
            val firstUrl =
                if (separator <= 4 || separator >= length) url.substring(4) else url.substring(
                    4,
                    separator
                )
            if (!TextUtils.isEmpty(firstUrl)) {
                return "https:" + firstUrl
            }
        }
        return url
    }

    @JvmStatic
    fun imageUrlFor(url: String?, found: Boolean): String? {
        if (!TextUtils.isEmpty(parentUrlOf(url)) && !url!!.contains("{{")) {
            val lowerCase = url.lowercase()
            if (lowerCase.indexOf("?page_url=http", 7) == -1) {
                val fileExt = fileExtFromName(url, false)
                if (TextUtils.isEmpty(fileExt) || isImageExt(fileExt)) {
                    return url
                }
                if ((isTumblrMedia(url) && ("gifv" == fileExt || "pnj" == fileExt)) || !found || isDcinsideImage(lowerCase)) {
                    return url
                }
            }
        }
        return null
    }

    @JvmStatic
    fun navBarInset(context: Context?, window: Window?, view: View?): Int {
        var view = view
        val naviHeight = if (MainSettings.naviConfirmed.value) MainSettings.naviHeight.value else 0
        if (MainSettings.naviConfirmed.value && naviHeight > 0) {
            return naviHeight
        }
        if (window != null) {
            if (view == null) {
                view = window.getDecorView()
            }
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            if (rect.bottom > 0) {
                val pointP = screenPoint(context)
                val index = (if (pointP == null) 0 else pointP.y) - rect.bottom
                if (index > 0 && index < (dpToPx(context, 140.0f).toInt())) {
                    val savedHeight = MainSettings.naviHeight.value
                    val sameHeight = savedHeight == index
                    if (savedHeight == index && MainSettings.naviConfirmed.value == sameHeight) {
                        return savedHeight
                    }
                    MainSettings.editLocal { set(MainSettings.naviConfirmed, sameHeight) }
                    MainSettings.editLocal { set(MainSettings.naviHeight, index) }
                    val prefMainR = get(context, false)
                    prefMainR!!.putInt(MainSettings.naviHeight.value, "mNaviHeight2")
                    prefMainR.putBool("mNaviConfirm", MainSettings.naviConfirmed.value)
                    prefMainR.saveAsync()
                    return MainSettings.naviHeight.value
                }
            }
        }
        return 0
    }

    @JvmStatic
    fun lastSeparatorBefore(end: Int, text: String?): Int {
        val length = if (text != null) text.length else 0
        if (text != null && length != 0 && end >= 0 && end <= length) {
            return max(
                max(text.lastIndexOf(47.toChar(), end), text.lastIndexOf(63.toChar(), end)),
                max(text.lastIndexOf(61.toChar(), end), text.lastIndexOf(38.toChar(), end))
            )
        }
        return -1
    }

    @JvmStatic
    fun openVideoWithApp(mainActivity: android.app.Activity?, packageName: String?, className: String?, input: String?, mode: Int): Boolean {
        val intent: Intent?
        if (TextUtils.isEmpty(input)) {
            return false
        }
        try {
            if (mode == 1) {
                intent = Intent("android.intent.action.VIEW")
                intent.setPackage(packageName)
                intent.setDataAndTypeAndNormalize(Uri.parse(input), "video/*")
            } else if (mode == 2) {
                intent = Intent("android.intent.action.VIEW")
                intent.setPackage(packageName)
                intent.setDataAndNormalize(Uri.parse(input))
            } else if (mode == 3) {
                val typedIntent = Intent()
                typedIntent.setComponent(ComponentName(packageName!!, className!!))
                typedIntent.setDataAndType(Uri.parse(input), "video/*")
                intent = typedIntent
            } else {
                val plainIntent = Intent()
                plainIntent.setComponent(ComponentName(packageName!!, className!!))
                plainIntent.setData(Uri.parse(input))
                intent = plainIntent
            }
            mainActivity!!.startActivity(intent)
            return true
        } catch (unused: ActivityNotFoundException) {
            if (mode != 0) {
                return false
            }
            showToast(mainActivity, R.string.apps_none)
            return false
        } catch (unused2: Exception) {
            if (mode != 0) {
                return false
            }
            showToast(mainActivity, R.string.not_supported)
            return false
        }
    }

    @JvmStatic
    fun isLightColor(color: Int): Boolean {
        return color == 0 || ColorUtils.calculateLuminance(color) > 0.5
    }

    @JvmStatic
    fun clearHandler(handler: Handler?) {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null)
        }
    }

    @JvmStatic
    fun injectVideoObserver(webView: WebView?, sb: StringBuilder?, withAudio: Boolean) {
        if (webView != null) {
            sb!!.append("(function(){")
            if (withAudio) {
                sb.append("if(window.sb_vi1){window.sb_vi1.disconnect();window.sb_vi1=null;}")
                sb.append("if(window.sb_vi2){window.sb_vi2.disconnect();window.sb_vi2=null;}")
            }
            sb.append("if(myVd(document,window))return;").append("if(!document.body||window.sb_vi1||window.sb_vi2)return;").append("window.sb_vi1=new MutationObserver(e=>{for(var i=0;i<e.length;i++){").append("var tgt=e[i].target;if(myVd(tgt,window)){")
            evalJs(
                webView,
                sb.append("if(window.sb_vi1){window.sb_vi1.disconnect();window.sb_vi1=null;}").append("break;}if(tgt.tagName=='BODY')break;}}),").append("window.sb_vi1.observe(document.body,{childList:!0,subtree:!0});})();").toString(),
                true
            )
        }
    }

    @JvmStatic
    fun appendAdsGoogleJs(sb: StringBuilder?): StringBuilder? {
        var sb = sb
        var arrayList: ArrayList<Any?>? = null
        if (!MainNative.partsReady(13, MainNative.adsGoogleParts)) {
            val adsGoogleParts: ArrayList<Any?> = ArrayList<Any?>()
            var index = 0
            while (true) {
                if (index >= 13) {
                    MainNative.adsGoogleParts = adsGoogleParts
                    arrayList = adsGoogleParts
                    break
                }
                val adsGoogleJs = getAdsGoogleJs(index)
                if (TextUtils.isEmpty(adsGoogleJs)) {
                    break
                }
                adsGoogleParts.add(adsGoogleJs)
                index++
            }
        } else {
            arrayList = MainNative.adsGoogleParts
        }
        if (arrayList != null && arrayList.size == 13) {
            if (sb == null) {
                sb = StringBuilder()
            }
            for (index in 0..12) {
                val url = arrayList.get(index) as String?
                if (TextUtils.isEmpty(url)) {
                    break
                }
                sb.append(url)
            }
        }
        return sb
    }

    @JvmStatic
    fun firstUrlIn(downUrl: String?): String? {
        val length = if (downUrl != null) downUrl.length else 0
        val httpIndex = if (downUrl != null && length != 0) downUrl.indexOf("http") else 0
        if (downUrl != null && length != 0 && httpIndex > 0 && httpIndex < length) {
            val index = httpIndex + 4
            val separatorIndex = if (index < length) downUrl.indexOf("<,>", index) else -1
            val firstUrl =
                if (separatorIndex <= httpIndex || separatorIndex >= length) downUrl.substring(httpIndex) else downUrl.substring(
                    httpIndex,
                    separatorIndex
                )
            if (!TextUtils.isEmpty(firstUrl)) {
                return firstUrl
            }
        }
        return downUrl
    }

    @JvmStatic
    fun shortHostOf(path: String?): String? {
        return stripHostPrefix(hostOfUrl(path, true))
    }

    @JvmStatic
    fun fileNumberSuffix(lowerCase: String?): String? {
        var index = -1
        val length = if (lowerCase != null) lowerCase.length else 0
        if (lowerCase != null && length != 0) {
            var dotIndex = lowerCase.lastIndexOf(46.toChar())
            while (dotIndex > 0 && dotIndex < length) {
                val lastDigit = dotIndex - 1
                if (lowerCase.get(lastDigit) >= '0' && lowerCase.get(lastDigit) <= '9' && length != 0 && lastDigit > 0) {
                    var digitPos = lastDigit
                    while (digitPos > 0) {
                        val prevPos = digitPos - 1
                        if (lowerCase.get(prevPos) < '0' || lowerCase.get(prevPos) > '9') {
                            break
                        }
                        digitPos--
                    }
                    if (digitPos > 0 && lowerCase.get(digitPos - 1) != '.') {
                        index = lastDigit
                        break
                    }
                }
                dotIndex = lowerCase.lastIndexOf(46.toChar(), lastDigit)
            }
        }
        if (index <= 0) {
            return lowerCase
        }
        val afterDigits = index + 1
        while (index > 0) {
            val beforePos = index - 1
            if (lowerCase!!.get(beforePos) < '0' || lowerCase.get(beforePos) > '9') {
                break
            }
            index--
        }
        val sb = StringBuilder()
        if (index > 0) {
            sb.append(lowerCase!!.substring(0, index))
        }
        if (afterDigits < lowerCase!!.length) {
            sb.append(lowerCase.substring(afterDigits))
        }
        return sb.toString()
    }

    @JvmStatic
    fun nextPageUrl(input: String?): String? {
        var idSegment: String?
        val url: String?
        var pageNo: Long
        var prevSeparator: Int = 0
        var parsedPageNo: Long
        val length = if (input != null) input.length else 0
        var separator = if (input != null && length != 0) lastSeparatorBefore(length, input) else 0
        if (input != null && length != 0 && separator != -1) {
            idSegment = input.substring(separator + 1)
            if (TextUtils.isEmpty(idSegment)) {
                pageNo = -1
            } else {
                try {
                    pageNo = idSegment.toLong()
                } catch (unused: Exception) {
                    pageNo = -1
                }
            }
            if (pageNo == -1L || idSegment.length < 6) {
                while (separator > 1 && (lastSeparatorBefore(separator - 1, input).also { prevSeparator = it }) != -1) {
                    val segment = input.substring(prevSeparator + 1, separator)
                    if (TextUtils.isEmpty(segment)) {
                        parsedPageNo = -1
                    } else {
                        try {
                            parsedPageNo = segment.toLong()
                        } catch (unused2: Exception) {
                            parsedPageNo = -1
                        }
                    }
                    if (parsedPageNo != -1L && segment.length > 5) {
                        idSegment = segment
                        break
                    }
                    separator = prevSeparator
                }
            }
        } else {
            idSegment = null
        }
        if (!TextUtils.isEmpty(idSegment)) {
            if (true) {
                if (TextUtils.isEmpty(MainNative.twitUrl)) {
                    MainNative.twitUrl = twitUrl
                }
                url = MainNative.twitUrl
            } else {
                url = null
            }
            if (!TextUtils.isEmpty(url)) {
                return "$url$idSegment"
            }
        }
        return null
    }

    @JvmStatic
    fun openPlayStore(activity: Activity?, input: String?): Boolean {
        if (activity == null || TextUtils.isEmpty(input)) {
            return false
        }
        try {
            val intent = Intent("android.intent.action.VIEW")
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + input))
            intent.setPackage("com.android.vending")
            activity.startActivity(intent)
            return true
        } catch (unused: ActivityNotFoundException) {
            showToast(activity, R.string.apps_none)
            return false
        } catch (unused2: Exception) {
            showToast(activity, R.string.not_supported)
            return false
        }
    }

    @JvmStatic
    fun sameSite(pageHost: String?, otherUrl: String?): Boolean {
        var pageHost = pageHost
        var otherUrl = otherUrl
        if (!TextUtils.isEmpty(pageHost) && !TextUtils.isEmpty(otherUrl)) {
            try {
                if (pageHost!!.endsWith("prospekt.com") && otherUrl!!.endsWith("prospekte.com")) {
                    return true
                }
                val pageSchemeEnd = pageHost.indexOf("://")
                if (pageSchemeEnd != -1) {
                    pageHost = pageHost.substring(pageSchemeEnd + 3)
                }
                val pageParts: Array<String?> =
                    pageHost.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val length = pageParts.size
                if (length == 0) {
                    return false
                }
                val otherSchemeEnd = otherUrl!!.indexOf("://")
                if (otherSchemeEnd != -1) {
                    otherUrl = otherUrl.substring(otherSchemeEnd + 3)
                }
                val otherParts: Array<String?> =
                    otherUrl.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val otherPartsSize = otherParts.size
                if (otherPartsSize == 0) {
                    return false
                }
                var commonLength = min(length, otherPartsSize)
                if (commonLength > 2) {
                    commonLength--
                }
                if (commonLength <= 0) {
                    return false
                }
                val cut = commonLength + 1
                for (index in 1..<cut) {
                    if (pageParts[length - index] != otherParts[otherPartsSize - index]) {
                        return false
                    }
                }
                return true
            } catch (unused: Exception) {
            }
        }
        return false
    }

    @JvmStatic
    fun trimTrailingSlash(text: String?): String? {
        if (TextUtils.isEmpty(text)) {
            return text
        }
        var length = text!!.length
        while (length > 0 && text.get(length - 1) == '/') {
            length--
        }
        return if (length < text.length) text.substring(0, length) else text
    }

    @JvmStatic
    fun applyVideoRotation(activity: Activity?) {
        if (activity == null) {
            return
        }
        val rotation = VideoSettings.rotation.value
        if (rotation == 1) {
            showPermissionNote(activity, 7)
        } else if (rotation == 2) {
            showPermissionNote(activity, 6)
        } else {
            showPermissionNote(activity, 4)
        }
    }

    @JvmStatic
    fun childThumb(context: Context?, childItem: ChildItem?): Bitmap? {
        if (!TextUtils.isEmpty(childItem!!.path)) {
            if (TextUtils.isEmpty(childItem.filePath)) {
                childItem.filePath = cacheFilePathFor(context, childItem.path)
            }
            if (File(childItem.filePath!!).length() > 0) {
                return BitmapUtil.decodeFile(childItem.filePath)
            }
            val path = childItem.path
            val filePath = childItem.filePath
            if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(filePath)) {
                val compressB = create(context, 1, path, null)
                val bitmapD = compressB.coverBitmap()
                compressB.release()
                if (bitmapD != null) {
                    MainApp.Companion.runInBackground(context, SaveBitmapPngTask(context, bitmapD, filePath))
                    return bitmapD
                }
            }
        }
        return null
    }

    @JvmStatic
    fun archiveIconFor(name: String?): Int {
        val lowerExt = lowerExtOf(name)
        if (TextUtils.isEmpty(lowerExt)) {
            return 43
        }
        if (isMhtExt(lowerExt)) {
            return 31
        }
        if (isImageExt(lowerExt)) {
            return 4
        }
        if (isVideoExt(lowerExt)) {
            return 5
        }
        if (isAudioExt(lowerExt)) {
            return 6
        }
        return if (Compress.isDocExt(lowerExt)) 7 else 43
    }

    @JvmStatic
    fun themeIconColor(barColor: Int, tintColor: Int): Int {
        if (barColor == 0) {
            return -2039584
        }
        if (barColor == 1) {
            return -12632257
        }
        return tintColor
    }

    @JvmStatic
    fun nextFileInFolder(filePath: String?): String? {
        var baseName: String?
        var index: Int
        if (TextUtils.isEmpty(filePath)) {
            return filePath
        }
        val file = File(filePath!!)
        val fileArrListFiles = file.getParentFile()?.listFiles()
        if (fileArrListFiles == null || fileArrListFiles.size == 0) {
            return filePath
        }
        val path = file.getParentFile()!!.getPath()
        val name = file.getName()
        val dotIndex = name.lastIndexOf(46.toChar())
        var ext = ""
        if (dotIndex != -1) {
            baseName = name.substring(0, dotIndex)
            if (dotIndex < name.length) {
                ext = name.substring(dotIndex)
            }
        } else {
            baseName = name
        }
        val numberStart = if (!baseName.endsWith(")")) 0 else baseName.lastIndexOf(" (")
        if (!baseName.endsWith(")") || numberStart == -1 || numberStart >= baseName.length - 3) {
            index = 1
        } else {
            try {
                index = baseName.substring(numberStart + 2, baseName.length - 1).toInt()
                try {
                    baseName = name.substring(0, numberStart)
                } catch (unused: Exception) {
                }
            } catch (unused2: Exception) {
                index = 1
            }
        }
        // the next "name (n).ext" no file in the folder already has
        val taken = HashSet<String>()
        for (index2 in fileArrListFiles) taken.add(index2.getName().lowercase())
        while (true) {
            index++
            val candidate = baseName + " (" + index + ")" + ext
            if (candidate.lowercase() !in taken) return path + "/" + candidate
        }
    }

    @JvmStatic
    fun fileProviderUri(context: Context?, path: String?): Uri? {
        if (TextUtils.isEmpty(path) || context == null) {
            return null
        }
        try {
            return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", File(path!!))
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun hasTopTitle(): Boolean {
        if (!PdfSettings.topToBottom.value || TextUtils.isEmpty(MainSettings.topItems.value)) {
            return !TextUtils.isEmpty(MainSettings.botItems.value)
        }
        return true
    }

    @JvmStatic
    fun isMediaResponse(httpURLConnection: HttpURLConnection?): Boolean {
        try {
            val contentType = httpURLConnection!!.getContentType()
            if (TextUtils.isEmpty(contentType)) {
                return false
            }
            return contentType!!.startsWith("image") || contentType.startsWith("video") || contentType.startsWith(
                "audio"
            )
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun trimTrailingSpace(text: String?): String? {
        if (TextUtils.isEmpty(text)) {
            return text
        }
        var length = text!!.length
        while (length > 0 && text.get(length - 1) <= ' ') {
            length--
        }
        return if (length < text.length) text.substring(0, length) else text
    }

    @JvmStatic
    fun injectWindowOpenJs(webView: WebView?, exitedVideoType: Int, videoUrl: String?, inject: Boolean) {
        var sb: StringBuilder?
        val openScript: String?
        if (webView != null) {
            var mediaName: String? = null
            if (exitedVideoType != 2) {
                sb = null
            } else {
                if (inject) {
                    openScript =
                        "if(window.sb_win_open){window.sb_win_open.disconnect();window.sb_win_open=null;}"
                } else {
                    openScript = if (ZtwoSettings.addPage.value || (ZtwoSettings.tabOpen.value and 2) == 2) nativeJs5c() else null
                }
                if (TextUtils.isEmpty(openScript)) {
                    sb = null
                } else {
                    sb = StringBuilder().append(openScript)
                }
            }
            if (lacksAlgorithmicDark() && AppState.darkWeb) {
                val host = hostOf(videoUrl, true)
                val darkWeb = !inject
                if (if (!lacksAlgorithmicDark()) !(TextUtils.isEmpty(host) || !needsDesktopMode(
                        videoUrl,
                        host
                    )) else ("file:///android_asset/shortcut.html" != videoUrl)
                ) {
                    mediaName = mediaNameFor(videoUrl, host, darkWeb, true)
                }
                if (!TextUtils.isEmpty(mediaName)) {
                    if (sb == null) {
                        sb = StringBuilder()
                    }
                    sb.append(mediaName)
                }
            }
            if (sb == null) {
                return
            }
            sb.insert(0, "(function(){")
            sb.append("})();")
            evalJs(webView, sb.toString(), true)
        }
    }

    @JvmStatic
    fun appendAmpJs(sb: StringBuilder?): StringBuilder? {
        var sb = sb
        var arrayList: ArrayList<Any?>? = null
        if (!MainNative.partsReady(6, MainNative.ampParts)) {
            val ampParts: ArrayList<Any?> = ArrayList<Any?>()
            var index = 0
            while (true) {
                if (index >= 6) {
                    MainNative.ampParts = ampParts
                    arrayList = ampParts
                    break
                }
                val ampJs = getAmpJs(index)
                if (TextUtils.isEmpty(ampJs)) {
                    break
                }
                ampParts.add(ampJs)
                index++
            }
        } else {
            arrayList = MainNative.ampParts
        }
        if (arrayList != null && arrayList.size == 6) {
            val urlScript = StringBuilder()
            for (index in 0..5) {
                val url = arrayList.get(index) as String?
                if (!TextUtils.isEmpty(url)) {
                    urlScript.append(url)
                }
            }
            val script = urlScript.toString()
            if (sb == null) {
                sb = StringBuilder()
            }
            sb.append(script)
            sb.append("{mySm1(document);if(!document.body||window.sb_sch)return;window.sb_sch=new MutationObserver(e=>{for(var i=0;i<e.length;i++){mySm2(e[i].target);}}),window.sb_sch.observe(document.body,{childList:!0,subtree:!0,attributeOldValue:0});}")
        }
        return sb
    }

    @JvmStatic
    fun editTextValue(editText: EditText?, trim: Boolean): String? {
        val editable: Editable? = if (editText == null) null else editText.getText()
        if (editText == null || editable == null) {
            return null
        }
        val valueText = editable.toString()
        return if (!trim || TextUtils.isEmpty(valueText)) valueText else valueText.trim { it <= ' ' }
    }

    @JvmStatic
    fun archiveKindOf(name: String?): Int {
        val lowerExt = lowerExtOf(name)
        if (TextUtils.isEmpty(lowerExt)) {
            return R.drawable.outline_draft_black_24
        }
        if (isAlbumExt(lowerExt)) {
            return R.drawable.outline_local_library_black_24
        }
        if (isPdfExt(lowerExt)) {
            return R.drawable.outline_picture_as_pdf_black_24
        }
        if (Compress.isArchiveExt(lowerExt)) {
            return R.drawable.outline_folder_zip_black_24
        }
        if (isImageExt(lowerExt)) {
            return R.drawable.outline_image_black_24
        }
        if (isVideoExt(lowerExt)) {
            return R.drawable.baseline_play_arrow_black_24
        }
        if (isAudioExt(lowerExt)) {
            return R.drawable.baseline_music_note_black_24
        }
        if (Compress.isDocExt(lowerExt)) {
            return R.drawable.outline_description_black_24
        }
        if (isMhtExt(lowerExt)) {
            return R.drawable.outline_public_black_24
        }
        return if (isApkExt(lowerExt)) R.drawable.outline_android_black_24 else R.drawable.outline_draft_black_24
    }

    @JvmStatic
    fun uniqueName(arrayList: Collection<String?>?, name: String?): String? {
        if (arrayList == null || arrayList.isEmpty() || !arrayList.contains(name)) {
            return name
        }
        var index = 1
        do {
            index++
        } while (arrayList.contains(name + " (" + index + ")"))
        return name + " (" + index + ")"
    }

    /**
     * Blocking: whether the server answers byte ranges (asks for the first byte and wants `206` with a matching
     * `Content-Range`). Split ("fast") downloads use it before they split (2026-11-20).
     */
    @JvmStatic
    fun supportsRanges(context: Context?, url: String?, referer: String?): Boolean {
        val conn = openHttpConnection(15_000, 15_000, context, url, referer, false) ?: return false
        return try {
            conn.setRequestProperty("Range", "bytes=0-0")
            conn.connect()
            conn.responseCode == 206 && (conn.getHeaderField("Content-Range")?.trim()?.startsWith("bytes 0-0/") == true)
        } catch (_: Exception) {
            false
        } finally {
            try { conn.disconnect() } catch (_: Exception) { }
        }
    }

    @JvmStatic
    fun openHttpConnection(
        connectTimeout: Int,
        readTimeout: Int,
        context: Context?,
        url: String?,
        referer: String?,
        gzip: Boolean
    ): HttpURLConnection? {
        return openHeadConnection(context, url, referer, connectTimeout, readTimeout, gzip, userAgentPref(), false)
    }

    @JvmStatic
    fun hasTopTabBar(): Boolean {
        val tabBar = BrowserSettings.tabBar.value
        return tabBar == 3 || tabBar == 4
    }

    @JvmStatic
    fun isAudioPlaying(context: Context?): Boolean {
        try {
            val audioManager = context!!.getSystemService("audio") as AudioManager?
            if (audioManager == null) {
                return false
            }
            return audioManager.isMusicActive()
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun stripMarker(text: String?): String? {
        var text = text
        if (TextUtils.isEmpty(text)) {
            return text
        }
        val markerIndex = text!!.lastIndexOf("wlrtitdb")
        if (markerIndex != -1) {
            text = text.substring(0, markerIndex)
        }
        return if (TextUtils.isEmpty(text)) text else text.trim { it <= ' ' }
    }

    @JvmStatic
    fun evalPageJs(webNestView: WebNestView?, url: String?, pageHost: String?) {
        if (webNestView == null) {
            return
        }
        val sbH = viewportJs(null, webNestView, url, pageHost)
        sbH.insert(0, "(function(){")
        sbH.append("})();")
        evalJs(webNestView, sbH.toString(), true)
    }

    @JvmStatic
    fun appContextOf(context: Context?): Context? {
        var applicationContext: Context? = null
        return if (context == null || (context.getApplicationContext()
                .also { applicationContext = it }) == null
        ) context else applicationContext
    }

    @JvmStatic
    fun lofiUrl(url: String?, agent: String?): String? {
        var length: Int = 0
        return if (agent == null || (agent.length.also { length = it }) == 0) url else "$agent/s/${url!!.substring(length + 8)}"
    }

    @JvmStatic
    fun imageViewerIntent(context: Context?): Intent {
        return Intent(context, com.mycompany.app.ui.viewer.ImageViewerActivity::class.java as Class<*>)
    }

    @JvmStatic
    fun baseNameAndExt(name: String?): String? {
        var baseName: String?
        if (TextUtils.isEmpty(name)) {
            return null
        }
        val dotIndex = name!!.lastIndexOf(46.toChar())
        var extPos = 0
        var ext = ""
        if (dotIndex != -1) {
            baseName = name.substring(0, dotIndex)
            if (dotIndex < name.length) {
                ext = name.substring(dotIndex)
            }
        } else {
            baseName = name
        }
        val numberStart = if (baseName.endsWith(")")) baseName.lastIndexOf(" (") else 0
        if (baseName.endsWith(")") && numberStart != -1 && numberStart < baseName.length - 3) {
            try {
                val readTimeout =
                    baseName.substring(numberStart + 2, baseName.length - 1).toInt()
                try {
                    baseName = name.substring(0, numberStart)
                } catch (unused: Exception) {
                }
                extPos = readTimeout
            } catch (unused2: Exception) {
            }
        }
        return baseName + " (" + (extPos + 1) + ")" + ext
    }

    @JvmStatic
    fun openHeadConnection(
        context: Context?,
        url: String?,
        referer: String?,
        connectTimeout: Int,
        readTimeout: Int,
        gzip: Boolean,
        agent: String?,
        noCache: Boolean
    ): HttpURLConnection? {
        var referer = referer
        var agent = agent
        val cookie: String?
        if (!TextUtils.isEmpty(url)) {
            try {
                val httpURLConnection = URL(url).openConnection() as HttpURLConnection
                if (noCache) {
                    httpURLConnection.setRequestMethod("HEAD")
                }
                if (connectTimeout == 0) {
                    httpURLConnection.setConnectTimeout(5000)
                } else if (connectTimeout > 0) {
                    httpURLConnection.setConnectTimeout(connectTimeout)
                }
                if (readTimeout == 0) {
                    httpURLConnection.setReadTimeout(20000)
                } else if (readTimeout > 0) {
                    httpURLConnection.setReadTimeout(readTimeout)
                }
                var found = false
                if (isGoogleSearch(referer)) {
                    cookie = null
                    referer = null
                } else {
                    if (MainUtil.isGoogleUserContent(url!!)) {
                        referer = url
                    } else if (TextUtils.isEmpty(referer)) {
                        val mainAppO: MainApp? = MainApp.Companion.from(context)
                        if (mainAppO != null && "https://missav.com" == mainAppO.hostCacheValue(url)) {
                            found = true
                        }
                    } else {
                        val mainAppO: MainApp? = MainApp.Companion.from(context)
                        if (mainAppO != null) {
                            val cachedHost = mainAppO.hostCacheValue(url)
                            if (!TextUtils.isEmpty(cachedHost)) {
                                httpURLConnection.setRequestProperty("Origin", cachedHost)
                            }
                        }
                    }
                    cookie = cookieFor(context, referer)
                    if (TextUtils.isEmpty(referer)) {
                        referer = url
                    }
                }
                httpURLConnection.setRequestProperty("Accept", MainUtil.acceptHeaderFor(url!!))
                httpURLConnection.setRequestProperty(
                    "Accept-Encoding",
                    if (gzip) "gzip, identity" else "identity"
                )
                if (!found) {
                    if (TextUtils.isEmpty(agent)) {
                        agent = userAgentPref()
                    }
                    httpURLConnection.setRequestProperty("User-Agent", agent)
                }
                if (!TextUtils.isEmpty(referer)) {
                    httpURLConnection.setRequestProperty("Referer", referer)
                }
                if (!TextUtils.isEmpty(cookie)) {
                    httpURLConnection.setRequestProperty("Cookie", cookie)
                }
                return httpURLConnection
            } catch (unused: Exception) {
            }
        }
        return null
    }

    @JvmStatic
    fun hasClipboard(context: Context?): Boolean {
        if (context == null) {
            return false
        }
        try {
            val clipboardManagerJ: ClipboardManager? = MainApp.Companion.clipboard(context)
            if (clipboardManagerJ == null) {
                return false
            }
            return clipboardManagerJ.hasPrimaryClip()
        } catch (unused: Exception) {
            return false
        } catch (unused: OutOfMemoryError) {
            return false
        }
    }

    @JvmStatic
    fun hasPermission(context: Context?, permission: Int): Boolean {
        if (context == null) {
            return false
        }
        if (permission == 0) {
            if (ContextCompat.checkSelfPermission(context, "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                return false
            }
        } else if (permission == 2) {
            if (ContextCompat.checkSelfPermission(context, "android.permission.CAMERA") == 0) {
                return false
            }
        } else if (permission == 3) {
            if (ContextCompat.checkSelfPermission(context, "android.permission.RECORD_AUDIO") == 0) {
                return false
            }
        } else if (permission == 4) {
            if (ContextCompat.checkSelfPermission(context, "android.permission.ACCESS_FINE_LOCATION") == 0) {
                return false
            }
        } else if (permission == 5) {
            if (Build.VERSION.SDK_INT < 33 || ContextCompat.checkSelfPermission(
                    context,
                    "android.permission.POST_NOTIFICATIONS"
                ) == 0
            ) {
                return false
            }
        } else {
            if (permission != 6) {
                if (permission != 7) {
                    return false
                }
                return !context.getPackageManager().canRequestPackageInstalls()
            }
            val index = Build.VERSION.SDK_INT
            val appOpsManager: AppOpsManager? = if (index < 26) null else (context.getSystemService("appops") as AppOpsManager?)
            if (index < 26 || appOpsManager == null) {
                return false
            }
            if (index >= 29) {
                if (appOpsManager.unsafeCheckOpNoThrow(
                        "android:picture_in_picture",
                        Process.myUid(),
                        context.getPackageName()
                    ) == 0
                ) {
                    return false
                }
            } else if (appOpsManager.checkOpNoThrow(
                    "android:picture_in_picture",
                    Process.myUid(),
                    context.getPackageName()
                ) == 0
            ) {
                return false
            }
        }
        return true
    }

    @JvmStatic
    fun detachFromParent(view: View?) {
        if (view != null) {
            try {
                val parent = view.getParent()
                if (parent == null || parent !is ViewGroup) {
                    return
                }
                parent.removeViewInLayout(view)
            } catch (unused: Exception) {
            }
        }
    }

    @JvmStatic
    fun cacheIcon(url: String?, bitmap: Bitmap?) {
        if (TextUtils.isEmpty(url) || !isBitmapOk(bitmap)) {
            return
        }
        com.mycompany.app.glide.ImageCache.put(com.mycompany.app.glide.ImageCache.key(2, url), bitmap)
    }

    @JvmStatic
    fun loadDrawable(context: Context?, res: Int): Drawable? {
        if (context == null) {
            return null
        }
        try {
            val drawable = ContextCompat.getDrawable(context, res)
            if (drawable != null) {
                return drawable
            }
        } catch (unused: Exception) {
        }
        try {
            val resources = context.getResources()
            val drawable = ResourcesCompat.getDrawable(resources, res, null)
            if (drawable != null) {
                return drawable
            }
        } catch (unused2: Exception) {
        }
        try {
            val applicationContext = context.getApplicationContext()
            if (applicationContext == null) {
                return null
            }
            val resources = applicationContext.getResources()
            val drawable = ResourcesCompat.getDrawable(resources, res, null)
            if (drawable != null) {
                return drawable
            }
        } catch (unused3: Exception) {
        }
        return null
    }

    @JvmStatic
    fun imageTypeOf(path: String?): String {
        if (TextUtils.isEmpty(path)) {
            return "jpg"
        }
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            BitmapFactory.decodeFile(path, options)
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
        val outMimeType = options.outMimeType
        if (TextUtils.isEmpty(outMimeType)) {
            return "jpg"
        }
        val lowerCase = outMimeType!!.lowercase()
        if (lowerCase.contains("gif")) {
            return "gif"
        }
        if (lowerCase.contains("png")) {
            return "png"
        }
        return if (lowerCase.contains("ico")) "ico" else "jpg"
    }

    @JvmStatic
    fun cachedBitmapAny(path: String?): Bitmap? {
        val found: Boolean
        if (!TextUtils.isEmpty(path)) {
            var index = 1
            var bitmapA: Bitmap? = null
            while (true) {
                if (index >= 26) {
                    found = false
                    break
                }
                try {
                    val cacheKey = com.mycompany.app.glide.ImageCache.key(com.mycompany.app.glide.ImageCache.TYPES[index], path)
                    if (!TextUtils.isEmpty(cacheKey)) {
                        bitmapA = com.mycompany.app.glide.ImageCache.get(cacheKey)
                        if (isBitmapOk(bitmapA)) {
                            found = true
                            break
                        }
                    }
                    index++
                } catch (unused: Exception) {
                    return bitmapA
                } catch (unused: OutOfMemoryError) {
                    return bitmapA
                }
            }
            if (found) {
                val width = bitmapA!!.getWidth()
                val height = bitmapA.getHeight()
                val dp48Px: Int = MainApp.Companion.dp48
                val sampleSize = if (width > dp48Px || height > dp48Px) sampleSizeFor(width, height, dp48Px, dp48Px) else 1
                if (sampleSize <= 1) {
                    return bitmapA
                }
                val sample = sampleSize.toFloat()
                var scaledWidth = Math.round(width / sample)
                val scaledHeight = Math.round(height / sample)
                if (scaledWidth == 0) {
                    scaledWidth = 1
                }
                return scaleBitmap(scaledWidth, if (scaledHeight != 0) scaledHeight else 1, bitmapA)
            }
        }
        return null
    }

    /**
     * File extension of the last path segment of `url` (ignoring query and fragment), or null.
     * Upper-cased with `upper`; mixed-case extensions are otherwise lower-cased. "gif" urls that
     * are really mp4 (`.gif?type=mp4`) report mp4.
     */
    @JvmStatic
    fun fileExtOf(url: String?, upper: Boolean): String? {
        var ext: String? = null
        var end = if (url != null) url.length else 0
        if (url != null && end != 0) {
            try {
                val query = url.indexOf('?')
                if (query >= 0) {
                    end = query
                }
                var slash = url.lastIndexOf('/', end)
                if (slash < 0) {
                    slash = 0
                }
                val hash = lastIndexOfCharIn(url, '#', slash, end)
                if (hash > slash && hash < end) {
                    end = hash
                }
                val dot = lastIndexOfCharIn(url, '.', slash, end)
                if (dot >= slash) {
                    val start = dot + 1
                    var hasUpper = false
                    var pos = start
                    while (pos < end) {
                        val c = url.get(pos)
                        if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                            pos++
                            continue
                        }
                        if (c >= 'A' && c <= 'Z') {
                            hasUpper = true
                            pos++
                            continue
                        }
                        end = pos
                        break
                        pos++
                    }
                    if (end > start) {
                        ext = url.substring(start, end)
                        if (upper) {
                            ext = ext.uppercase()
                        } else if (hasUpper) {
                            ext = ext.lowercase()
                        }
                    }
                }
            } catch (unused: Exception) {
            }
        }
        if (TextUtils.isEmpty(ext)) {
            return null
        }
        if ((if (upper) "GIF" else "gif") == ext && url!!.lowercase().contains(".gif?type=mp4")) {
            return if (upper) "MP4" else "mp4"
        }
        return ext
    }

    @JvmStatic
    fun isDarkMode(): Boolean {
        return if (SyncSettings.secretMode.value) BrowserSettings.quickBackSecret.value else BrowserSettings.quickBack.value
    }

    @JvmStatic
    fun isOfflineMht(fileSourceUrl: String?): Boolean {
        if (!TextUtils.isEmpty(fileSourceUrl) && fileSourceUrl!!.startsWith("file:///")) {
            return fileSourceUrl.endsWith(".mht")
        }
        return false
    }

    @JvmStatic
    fun removeFromParent(view: View?) {
        if (view != null) {
            try {
                val parent = view.getParent()
                if (parent == null || parent !is ViewGroup) {
                    return
                }
                parent.removeView(view)
            } catch (unused: Exception) {
            }
        }
    }

    @JvmStatic
    fun applyZoomSettings(webView: WebView?, landscape: Boolean) {
        if (webView == null) {
            return
        }
        val settings = webView.getSettings()
        settings.setTextZoom(100)
        settings.setSupportZoom(true)
        settings.setBuiltInZoomControls(true)
        settings.setDisplayZoomControls(false)
        settings.setUseWideViewPort(true)
        settings.setLoadWithOverviewMode(true)
        settings.setDomStorageEnabled(true)
        settings.setJavaScriptCanOpenWindowsAutomatically(false)
        settings.setSupportMultipleWindows(false)
        settings.setMediaPlaybackRequiresUserGesture(!landscape)
        settings.setDatabaseEnabled(true)
        settings.setMixedContentMode(0)
        settings.setAllowFileAccess(true)
        settings.setLoadsImagesAutomatically(landscape)
        settings.setJavaScriptEnabled(true)
        val context = webView.getContext()
        MainApp.Companion.applyCookieSetting(context)
        MainApp.Companion.setThirdPartyCookies(context, webView, MainApp.Companion.cookiesAllowed(true))
        webView.setOverScrollMode(2)
    }

    @JvmStatic
    fun appIcon(context: Context?, sourceDir: String?): Bitmap? {
        var packageArchiveInfo: PackageInfo?
        val packageManager: PackageManager? = if (context == null || TextUtils.isEmpty(sourceDir)) null else context.getPackageManager()
        if (context == null || TextUtils.isEmpty(sourceDir) || packageManager == null) {
            return null
        }
        try {
            packageArchiveInfo =
                if (Build.VERSION.SDK_INT >= 33) packageManager.getPackageArchiveInfo(
                    sourceDir!!,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
                ) else packageManager.getPackageArchiveInfo(sourceDir!!, PackageManager.GET_ACTIVITIES)
        } catch (unused: Exception) {
            packageArchiveInfo = null
        }
        val applicationInfo: ApplicationInfo? = if (packageArchiveInfo == null) null else packageArchiveInfo.applicationInfo
        if (packageArchiveInfo == null || applicationInfo == null) {
            return null
        }
        applicationInfo.sourceDir = sourceDir
        applicationInfo.publicSourceDir = sourceDir
        return drawableToBitmap(MainApp.Companion.dp48, applicationInfo.loadIcon(packageManager))
    }

    @JvmStatic
    fun dataUriExt(url: String?): String {
        var semicolon: Int = 0
        return if (TextUtils.isEmpty(url) || !url!!.startsWith("data:image/") || (url.indexOf(
                59.toChar(),
                11
            ).also { semicolon = it }) <= 11 || semicolon > url.length
        ) "jpg" else url.substring(11, semicolon)
    }

    /** Opens `str` for writing: a content:// URI through the resolver, otherwise a local file.  */
    @Throws(IOException::class)
    @JvmStatic
    fun openOutput(context: Context?, input: String?, append: Boolean): OutputStream? {
        if (MainUri.isContentUri(input)) {
            return context!!.getContentResolver()
                .openOutputStream(Uri.parse(input), if (append) "wa" else "w")
        }
        return openFileOutput(input, append)
    }

    /**
     * Suggested file name for downloading `url` with the given Content-Disposition and mime
     * type. Fixes up the extension to match the media type (and subtitle files), and with
     * `clean` strips characters that are not allowed in file names.
     */
    @JvmStatic
    fun guessDownloadName(url: String?, disposition: String?, mime: String?, clean: Boolean): String? {
        var mime = mime
        if (TextUtils.isEmpty(url)) {
            return null
        }
        var urlExt: String? = null
        if ("video/*" == mime) {
            // A "video" that is really an audio file.
            if (url!!.endsWith(".aac")) {
                mime = "audio/aac"
            } else if (url.endsWith(".mp3")) {
                mime = "audio/mp3"
            } else {
                urlExt = fileExtOf(url, false)
                if ("aac" == urlExt) {
                    mime = "audio/aac"
                } else if ("mp3" == urlExt) {
                    mime = "audio/mp3"
                }
            }
        }
        var subExt: String? = null
        if (TextUtils.isEmpty(mime) || mime!!.startsWith("video") || mime.startsWith("text")) {
            subExt = subtitleExt(url, urlExt)
            if (!TextUtils.isEmpty(subExt)) {
                mime = "text/*"
            }
        }
        var name = dispositionFileName(disposition)
        if (TextUtils.isEmpty(name)) {
            if (TextUtils.isEmpty(mime)) {
                mime = mimeForPath(url)
            }
            name = URLUtilCompat.guessFileName(
                url!!,
                disposition,
                if (!TextUtils.isEmpty(mime) && mime!!.endsWith("octet-stream")) null else mime
            )
        } else if (TextUtils.isEmpty(mime)) {
            mime = mimeForUrl(name)
        }
        if ("downloadfile.bin" == name && !TextUtils.isEmpty(urlExt)) {
            // Use the path segment before the extension, e.g. ".../name/aac" style urls.
            val path = urlDecode(url)
            val at = if (!TextUtils.isEmpty(path)) path!!.lastIndexOf(urlExt!!) else 0
            val slash = if (!TextUtils.isEmpty(path) && at > 0 && at < url!!.length) path!!.lastIndexOf('/', at - 1) else 0
            if (!TextUtils.isEmpty(path) && at > 0 && at < url!!.length && slash > 0 && slash + 1 < at - 1) {
                val segment = path!!.substring(slash + 1, at - 1)
                if (!TextUtils.isEmpty(segment)) {
                    name = segment
                }
            }
        }
        if (clean) {
            name = safeFileName(name)
        }
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(mime)) {
            return timestampName(null, null)
        }
        if (!TextUtils.isEmpty(subExt)) {
            return if (isSubtitleExt(lowerExtOf(name))) name else MainUtil.withExt(name!!, subExt)
        }
        if (mime!!.startsWith("image")) {
            val ext = lowerExtOf(name)
            if ("gifv" == ext) {
                return MainUtil.withExt(name!!, "gif")
            }
            if (!isImageExt(ext)) {
                val sub = if (mime.length > 6) mime.substring(6) else null
                return MainUtil.withExt(name!!, if (sub != null && isImageExt(sub)) sub else "jpg")
            }
        } else if (mime.startsWith("video")) {
            if (!isVideoExt(lowerExtOf(name))) {
                val sub = if (mime.length > 6) mime.substring(6) else null
                return MainUtil.withExt(name!!, if (sub != null && isVideoExt(sub)) sub else "mp4")
            }
        } else if (mime.startsWith("audio") && !isAudioExt(lowerExtOf(name))) {
            val sub = if (mime.length > 6) mime.substring(6) else null
            return MainUtil.withExt(name!!, if (sub != null && isAudioExt(sub)) sub else "mp3")
        }
        return name
    }

    /** Subtitle extension (without dot) of `url`, or null.  */
    private fun subtitleExt(url: String?, urlExt: String?): String? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        for (dotted in Subtitle.DOT_EXTENSIONS) {
            if (url!!.endsWith(dotted)) {
                return dotted.substring(1)
            }
        }
        val ext = if (TextUtils.isEmpty(urlExt)) fileExtOf(url, false) else urlExt
        if (TextUtils.isEmpty(ext)) {
            return null
        }
        for (known in Subtitle.EXTENSIONS) {
            if (ext == known) {
                return known
            }
        }
        return null
    }

    /** Replaces (or adds) the extension of `name`.  */
    private fun withExt(name: String?, ext: String?): String {
        var name = name
        val dot = name!!.lastIndexOf('.')
        if (dot != -1) {
            name = name.substring(0, dot)
        }
        return name + "." + ext
    }

    @JvmStatic
    fun hasBottomTitle(): Boolean {
        if (PdfSettings.topToBottom.value) {
            return false
        }
        return !TextUtils.isEmpty(MainSettings.topItems.value)
    }

    @JvmStatic
    fun isSecretLocked(): Boolean {
        val secureKey = SecretSettings.secureKey.value
        if (secureKey == 1) {
            return true
        }
        if (secureKey == 2) {
            return SyncSettings.secretMode.value
        }
        return false
    }

    @JvmStatic
    fun stripQuotes(text: String?): String? {
        if (TextUtils.isEmpty(text)) {
            return text
        }
        var length = text!!.length
        var index = 0
        while (index < length && text.get(index) == '\"') {
            index++
        }
        while (index < length && text.get(length - 1) == '\"') {
            length--
        }
        return if (index > 0 || length < text.length) text.substring(index, length) else text
    }

    @JvmStatic
    fun setAlgorithmicDark(webSettings: WebSettings?, darkWeb: Boolean) {
        if (webSettings == null || lacksAlgorithmicDark()) {
            return
        }
        try {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING)) {
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(webSettings, darkWeb)
            }
        } catch (unused: Exception) {
        }
    }

    @JvmStatic
    fun freeMemory(context: Context?): Long {
        try {
            val activityManager = context!!.getSystemService("activity") as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            return memoryInfo.availMem
        } catch (unused: Exception) {
            return 0L
        }
    }

    @JvmStatic
    fun urlInParens(text: String?): String? {
        val httpIndex = if (TextUtils.isEmpty(text)) 0 else text!!.indexOf("http")
        val parenIndex = if (TextUtils.isEmpty(text) || httpIndex == -1) 0 else text!!.indexOf(41.toChar(), httpIndex)
        if (TextUtils.isEmpty(text) || httpIndex == -1 || parenIndex == -1) {
            return null
        }
        var url = text!!.substring(httpIndex, parenIndex)
        if (TextUtils.isEmpty(url)) {
            return null
        }
        if (url.endsWith("'") || url.endsWith("\"")) {
            url = url.let { s -> s.substring(0, s.length - 1) }
            if (TextUtils.isEmpty(url)) {
                return null
            }
        }
        val cleanUrl = url.replace(" ", "").replace('\\', '%')
        if (TextUtils.isEmpty(cleanUrl)) {
            return null
        }
        return urlDecode(cleanUrl)
    }

    @Throws(IOException::class)
    @JvmStatic
    fun urlInputStream(filePath: String?): ContentLengthInputStream? {
        if (TextUtils.isEmpty(filePath)) {
            return null
        }
        return ContentLengthInputStream(
            BufferedInputStream(openFileInput(filePath), 32768),
            File(filePath!!).length().toInt()
        )
    }

    @JvmStatic
    fun positionText(count: Int, index: Int): String {
        if (count == 0) {
            return "0 / 0"
        }
        return (index + 1).toString() + " / " + count
    }

    @JvmStatic
    fun fileExtFromName(name: String?, lower: Boolean): String? {
        var extEnd: Int = 0
        val dotIndex = if (TextUtils.isEmpty(name)) 0 else name!!.lastIndexOf(46.toChar())
        if (TextUtils.isEmpty(name) || dotIndex == -1) {
            return null
        }
        val fileExt = fileExtOf(name!!.substring(dotIndex), false)
        if (TextUtils.isEmpty(fileExt) || fileExt!!.length > 4) {
            val queryIndex = name.lastIndexOf(63.toChar(), dotIndex)
            if (queryIndex == -1) {
                return null
            }
            return fileExtFromName(name.substring(0, queryIndex), lower)
        }
        if (lower) {
            if (fileExt == "jpeg" || fileExt == "jpe") {
                return "jpg"
            }
            if (!isImageExt(fileExt)) {
                if (MainUtil.isTumblrMedia(name)) {
                    if ("gifv" == fileExt) {
                        return "gif"
                    }
                    if ("pnj" == fileExt) {
                        return "png"
                    }
                }
                return null
            }
        } else if (!isImageExt(fileExt) && (name.lastIndexOf(63.toChar(), dotIndex)
                .also { extEnd = it }) != -1
        ) {
            return fileExtFromName(name.substring(0, extEnd), lower)
        }
        return fileExt
    }

    @JvmStatic
    fun clearHistoryData(context: Context?) {
        if (context == null) {
            return
        }
        MainApp.Companion.runInBackground(context, ClearHistoryNotiTask(context))
    }

    @JvmStatic
    fun isSmallScreen(context: Context?): Boolean {
        val pointP = screenPoint(context)
        return pointP != null && min(pointP.x, pointP.y) < MainApp.Companion.dp600
    }

    @JvmStatic
    fun trimSlashes(text: String?): String? {
        if (TextUtils.isEmpty(text)) {
            return text
        }
        var length = text!!.length
        var index = 0
        while (index < length && text.get(index) == '/') {
            index++
        }
        while (index < length && text.get(length - 1) == '/') {
            length--
        }
        return if (index > 0 || length < text.length) text.substring(index, length) else text
    }

    @JvmStatic
    fun youTubeAutoplayJs(webView: WebView?) {
        if (webView != null) {
            var sbR =
                if (ZtwoSettings.youUnmute.value) StringBuilder().append("var el1=document.querySelector(\"button[class*='ytp-unmute']\");if(el1){el1.click();}") else null
            if (!ZtwoSettings.autoPlay.value) {
                if (sbR == null) {
                    sbR = StringBuilder()
                }
                sbR.append("var el2=document.querySelector(\"video\");if(el2){el2.pause();}")
            }
            if (sbR == null) {
                return
            }
            sbR.insert(0, "(function(){")
            sbR.append("})();")
            evalJs(webView, sbR.toString(), true)
        }
    }

    @JvmStatic
    fun backupDirPath(context: Context?, secret: Boolean, backdropLandscape: Boolean): String? {
        if (backdropLandscape) {
            if (!TextUtils.isEmpty(MainApp.Companion.backLandPath)) {
                return MainApp.Companion.backLandPath
            }
            if (!TextUtils.isEmpty(MainApp.Companion.backLandPath2)) {
                val backLandPath: String? = MainApp.Companion.backLandPath2
                MainApp.Companion.backLandPath = backLandPath
                return backLandPath
            }
        } else {
            if (!TextUtils.isEmpty(MainApp.Companion.backPortPath)) {
                return MainApp.Companion.backPortPath
            }
            if (!TextUtils.isEmpty(MainApp.Companion.backPortPath2)) {
                val backPortPath: String? = MainApp.Companion.backPortPath2
                MainApp.Companion.backPortPath = backPortPath
                return backPortPath
            }
        }
        val filesDir: File? = if (context == null) null else context.getFilesDir()
        if (context == null || filesDir == null) {
            return null
        }
        val path = filesDir.getPath()
        if (TextUtils.isEmpty(path)) {
            return null
        }
        val backDir = "$path/.back/"
        MainApp.Companion.backPortPath = backDir + if (secret) "sec_port.jpg" else "back.jpg"
        val landPath = backDir + if (secret) "sec_land.jpg" else "nor_land.jpg"
        MainApp.Companion.backLandPath = landPath
        return if (backdropLandscape) landPath else MainApp.Companion.backPortPath
    }

    @JvmStatic
    fun extOf(name: String?): String? {
        val dotIndex = if (TextUtils.isEmpty(name)) 0 else name!!.lastIndexOf(46.toChar())
        val extStart = if (TextUtils.isEmpty(name) || dotIndex == -1) 0 else (dotIndex + 1)
        if (TextUtils.isEmpty(name) || dotIndex == -1 || extStart >= name!!.length) {
            return null
        }
        val ext = name.substring(extStart)
        if (TextUtils.isEmpty(ext)) {
            return null
        }
        val trimmedExt = ext.trim { it <= ' ' }
        if (TextUtils.isEmpty(trimmedExt)) {
            return null
        }
        return trimmedExt
    }

    @JvmStatic
    fun imageSize(context: Context?, input: String?): SizeItem? {
        var inputStreamOpenInputStream: InputStream?
        var sizeItem: SizeItem? = null
        if (TextUtils.isEmpty(input)) {
            return null
        }
        try {
            inputStreamOpenInputStream =
                context!!.getContentResolver().openInputStream(Uri.parse(input))
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapUtil.decodeStream(inputStreamOpenInputStream, options)
                sizeItem = SizeItem(options.outWidth, options.outHeight)
            } catch (unused: Exception) {
            }
        } catch (unused2: Exception) {
            inputStreamOpenInputStream = null
        }
        if (inputStreamOpenInputStream != null) {
            try {
                inputStreamOpenInputStream.close()
            } catch (unused3: Exception) {
            }
        }
        return sizeItem
    }

    @JvmStatic
    fun offlineUrlFor(path: String?): String {
        val lastSlash = if (!TextUtils.isEmpty(path)) path!!.lastIndexOf(47.toChar()) else 0
        if (!TextUtils.isEmpty(path) && lastSlash > 0 && lastSlash < path!!.length) {
            val fileName = path.substring(lastSlash)
            if (!TextUtils.isEmpty(fileName)) {
                return "file:///offline" + fileName
            }
        }
        return "file:///offline"
    }

    @JvmStatic
    fun parentUrlOf(url: String?): String? {
        var trimmed = trimSlashes(url)
        if (TextUtils.isEmpty(trimmed)) {
            return null
        }
        if (url!!.startsWith("https://") || url.startsWith("http://")) {
            val host = hostOf(url, false)
            if (TextUtils.isEmpty(host) || host == trimmed) {
                return null
            }
        }
        val lastSlash = trimmed!!.lastIndexOf(47.toChar())
        if (lastSlash != -1) {
            trimmed = trimmed.substring(lastSlash + 1)
            if (TextUtils.isEmpty(trimmed)) {
                return null
            }
        }
        val lowerCase = trimmed.lowercase()
        for (placeholder in MainConst.placeholderWords) {
            if (lowerCase.contains(placeholder!!)) {
                return null
            }
        }
        return trimmed
    }

    @JvmStatic
    fun closeKeyboard(context: Context?, view: View?) {
        if (context == null || view == null) {
            return
        }
        val mainAppO: MainApp? = MainApp.Companion.from(context)
        if (mainAppO == null || mainAppO.inputMethodCache == null) {
            MainApp.Companion.runInBackground(context, HideKeyboardLaterTask(context, view))
        } else {
            MainApp.Companion.postToMain(context, HideKeyboardTask(context, view))
        }
    }

    @JvmStatic
    fun isTumblrMedia(lowerCase: String?): Boolean {
        val host = hostOf(lowerCase, true)
        return !TextUtils.isEmpty(host) && host!!.endsWith("tumblr.com") && host.contains("media")
    }

    @JvmStatic
    fun trimSpaceDash(text: String?): String? {
        if (TextUtils.isEmpty(text)) {
            return text
        }
        var length = text!!.length
        var index = 0
        while (index < length && (text.get(index) <= ' ' || text.get(index) == '-')) {
            index++
        }
        while (index < length) {
            val last = length - 1
            if (text.get(last) > ' ' && (length <= 0 || text.get(last) != '-')) {
                break
            }
            length--
        }
        return if (index > 0 || length < text.length) text.substring(index, length) else text
    }

    @JvmStatic
    fun shareFile(type: Int, activity: Activity?, input: String?, name: String?, mimeType: String?): Boolean {
        var mimeType = mimeType
        val mime: String?
        if (activity == null) {
            return false
        }
        if (TextUtils.isEmpty(input)) {
            showToast(activity, R.string.invalid_path)
            return false
        }
        val isContent = MainUri.isContentUri(input)
        if (TextUtils.isEmpty(mimeType)) {
            if (type == 4) {
                mimeType = "image/*"
            } else if (type == 5) {
                mimeType = "video/*"
            } else if (type == 6) {
                mimeType = "audio/*"
            } else if (type == 7) {
                mimeType = "text/*"
            } else {
                if (isContent) {
                    mime = mimeForUrl(if (TextUtils.isEmpty(name)) MainUri.treeDisplayPath(activity, input) else name)
                } else {
                    mime = mimeForUrl(input)
                }
                mimeType = mime
                if (TextUtils.isEmpty(mimeType)) {
                    mimeType = "*/*"
                }
            }
        }
        try {
            val uriO = if (isContent) Uri.parse(input) else fileProviderUri(activity, input)
            val intent = Intent("android.intent.action.SEND")
            intent.setDataAndType(uriO, mimeType)
            intent.putExtra("android.intent.extra.STREAM", uriO)
            if (!TextUtils.isEmpty(name)) {
                intent.putExtra("android.intent.extra.TITLE", name)
                intent.putExtra("android.intent.extra.SUBJECT", name)
            }
            intent.putExtra("EXTRA_SOUL", true)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            activity.startActivity(Intent.createChooser(intent, activity.getString(R.string.share)))
            return true
        } catch (unused: ActivityNotFoundException) {
            showToast(activity, R.string.apps_none)
            return false
        } catch (unused2: Exception) {
            showToast(activity, R.string.invalid_path)
            return false
        }
    }

    @JvmStatic
    fun backupDirPath2(context: Context?, secret: Boolean, land: Boolean): String? {
        if (land) {
            if (!TextUtils.isEmpty(MainApp.Companion.backLandPath2)) {
                return MainApp.Companion.backLandPath2
            }
        } else if (!TextUtils.isEmpty(MainApp.Companion.backPortPath2)) {
            return MainApp.Companion.backPortPath2
        }
        val filesDir = filesSubdir(context, ".back")
        if (TextUtils.isEmpty(filesDir)) {
            return null
        }
        val backDir = "$filesDir/"
        MainApp.Companion.backPortPath2 = backDir + if (secret) "sec_port.jpg" else "back.jpg"
        val landPath = backDir + if (secret) "sec_land.jpg" else "nor_land.jpg"
        MainApp.Companion.backLandPath2 = landPath
        return if (land) landPath else MainApp.Companion.backPortPath2
    }

    @JvmStatic
    fun lowerExtOf(name: String?): String? {
        val ext = extOf(name)
        if (ext == null) {
            return null
        }
        return ext.lowercase()
    }

    @JvmStatic
    fun childBitmap(childItem: ChildItem?, withThumb: Boolean): Bitmap? {
        if (TextUtils.isEmpty(childItem!!.path)) {
            return null
        }
        if (withThumb) {
            val bitmapR = cachedBitmapAny(childItem.path)
            if (isBitmapOk(bitmapR)) {
                return bitmapR
            }
        }
        val path = childItem.path
        if (TextUtils.isEmpty(path)) {
            return null
        }
        try {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            try {
                BitmapFactory.decodeFile(path, options)
            } catch (unused: Exception) {
            } catch (unused: OutOfMemoryError) {
            }
            val outWidth = options.outWidth
            val dp48Px: Int = MainApp.Companion.dp48
            if (outWidth > dp48Px || options.outHeight > dp48Px) {
                options.inSampleSize = sampleSizeFor(outWidth, options.outHeight, dp48Px, dp48Px)
            }
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeFile(path, options)
        } catch (unused2: Exception) {
            return null
        } catch (unused2: OutOfMemoryError) {
            return null
        }
    }

    @JvmStatic
    fun offlinePathFor(context: Context?, page: String?): String? {
        if (isOfflineMht(page)) {
            var path: String? = null
            if (!TextUtils.isEmpty(page) && page!!.startsWith("file:///offline/") && context != null) {
                val fileName = page.substring(15)
                if (!TextUtils.isEmpty(fileName)) {
                    path = context.getFilesDir().toString() + "/.page" + fileName
                }
            }
            if (!TextUtils.isEmpty(path)) {
                return if (File(path!!).exists()) path else "file:///expired"
            }
        }
        return page
    }

    @JvmStatic
    fun openWithCookies(context: Context?, url: String?, pageUrl: String?, map: Map<String, String>?): InputStream? {
        if (TextUtils.isEmpty(url) || map == null) {
            return null
        }
        try {
            // a copy: the caller's map (a WebView request's headers) is not changed
            val headers = HashMap(map)
            val cookie = cookieFor(context, pageUrl)
            if (!TextUtils.isEmpty(cookie)) {
                headers.put("Cookie", cookie!!)
            }
            val builder = Request.Builder()
            builder.url(url!!)
            builder.headers(headers.toHeaders())
            val responseBody =
                OkHttpClient().newCall(builder.build()).execute().body
            return responseBody.byteStream()
            return null
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun clearAppData(context: Context?) {
        if (context == null) {
            return
        }
        MainApp.Companion.runInBackground(context, CancelNotificationsTask(context))
    }

    @JvmStatic
    fun isXLoginPage(pageUrl: String?, pageHost: String?): Boolean {
        if (TextUtils.isEmpty(pageUrl) || TextUtils.isEmpty(pageHost) || (pageHost != "x.com")) {
            return false
        }
        return pageUrl!!.endsWith("login") || pageUrl.endsWith("signup")
    }

    @JvmStatic
    fun trimSpaceSlash(text: String?): String? {
        if (TextUtils.isEmpty(text)) {
            return text
        }
        var length = text!!.length
        var index = 0
        while (index < length && (text.get(index) <= ' ' || text.get(index) == '/')) {
            index++
        }
        while (index < length) {
            val last = length - 1
            if (text.get(last) > ' ' && (length <= 0 || text.get(last) != '/')) {
                break
            }
            length--
        }
        return if (index > 0 || length < text.length) text.substring(index, length) else text
    }

    @JvmStatic
    fun shareText(activity: Activity?, path: String?, name: String?): Boolean {
        if (activity != null && !TextUtils.isEmpty(path)) {
            try {
                val intent = Intent("android.intent.action.SEND")
                intent.setType("text/plain")
                intent.putExtra("android.intent.extra.TEXT", path)
                if (!TextUtils.isEmpty(name)) {
                    intent.putExtra("android.intent.extra.TITLE", name)
                    intent.putExtra("android.intent.extra.SUBJECT", name)
                }
                intent.putExtra("EXTRA_SOUL", true)
                activity.startActivity(
                    Intent.createChooser(
                        intent,
                        activity.getString(R.string.share)
                    )
                )
                return true
            } catch (unused: ActivityNotFoundException) {
                showToast(activity, R.string.apps_none)
            } catch (unused2: Exception) {
                showToast(activity, R.string.not_supported)
                return false
            }
        }
        return false
    }

    @JvmStatic
    fun findMediaJs(backPlaySrc: String?): StringBuilder {
        val sb = StringBuilder()
        if (TextUtils.isEmpty(backPlaySrc)) {
            sb.append("var ele=null;")
        } else {
            sb.append("var ele=document.querySelector(\"video[src*='").append(backPlaySrc).append("']\");if(!ele){ele=document.querySelector(\"source[src*='").append(backPlaySrc)
            sb.append("']\");if(ele){ele=ele.parentNode;}else{ele=document.querySelector(\"audio[src*='").append(backPlaySrc).append("']\");}if(!ele){")
        }
        sb.append("var eles=document.querySelectorAll(\"video,audio\");if(eles&&(eles.length>0)){if(eles.length==1){ele=eles[0];}else{for(var i=0;i<eles.length;i++){if(!eles[i].paused&&eles[i].duration){ele=eles[i];break;}}if(!ele){ele=eles[0];}}}if(!ele){eles=document.querySelectorAll(\"source[type*='video'],source[type*='audio']\");if(eles&&(eles.length>0)){if(eles.length==1){ele=eles[0].parentNode;}else{for(var i=0;i<eles.length;i++){if(!eles[i].parentNode.paused&&eles[i].parentNode.duration){ele=eles[i].parentNode;break;}}if(!ele){ele=eles[0].parentNode;}}}}")
        if (!TextUtils.isEmpty(backPlaySrc)) {
            sb.append("if(ele){function myVideoPlaying(evt){var src=null;var ele=evt.target||evt.srcElement;if(ele){src=ele.src;}android.onVideoPaused(src,false);}function myVideoPaused(evt){var src=null;var ele=evt.target||evt.srcElement;if(ele){src=ele.src;}android.onVideoPaused(src,true);}ele.addEventListener(\"playing\",myVideoPlaying);ele.addEventListener(\"pause\",myVideoPaused);}}}")
        }
        return sb
    }

    @JvmStatic
    fun upperExtOf(name: String?): String? {
        val ext = extOf(name)
        if (ext == null) {
            return null
        }
        return ext.uppercase()
    }

    @JvmStatic
    fun backupItemName(count: Int, index: Int, part: Int, path: String?): String? {
        if (TextUtils.isEmpty(path)) {
            return null
        }
        return path + "." + count + "." + index + "." + part + ".b"
    }

    @JvmStatic
    fun pageTitleForFile(context: Context?, fileSourceUrl: String?): String? {
        var length: Int = 0
        var cursorF: Cursor?
        if (!isOfflineMht(fileSourceUrl)) {
            return fileSourceUrl
        }
        val dbBookPage = DbBookPage.cached
        var pageUrl: String? = null
        val underscore = if (context != null && !TextUtils.isEmpty(fileSourceUrl)) fileSourceUrl!!.lastIndexOf(95.toChar()) else 0
        val index = if (context != null && !TextUtils.isEmpty(fileSourceUrl) && underscore >= 0) (underscore + 1) else 0
        if (context != null && !TextUtils.isEmpty(fileSourceUrl) && underscore >= 0 && index < ((fileSourceUrl!!.length - 4).also { length = it })) {
            val savedTime = parseLongSafe(fileSourceUrl.substring(index, length))
            if (savedTime != 0L) {
                try {
                    cursorF = DbUtil.queryById(
                        DbBookPage.get(context).getWritableDatabase(),
                        "DbBookPage_table",
                        arrayOf<String?>("_url"),
                        savedTime
                    )
                    if (cursorF != null) {
                        try {
                            if (cursorF.moveToFirst()) {
                                pageUrl = cursorF.getString(cursorF.getColumnIndex("_url"))
                            }
                        } catch (unused: Exception) {
                        }
                    }
                } catch (unused2: Exception) {
                    cursorF = null
                }
                if (cursorF != null) {
                    cursorF.close()
                }
            }
        }
        return if (TextUtils.isEmpty(pageUrl)) offlineUrlFor(fileSourceUrl) else pageUrl
    }

    @JvmStatic
    fun scriptNameFor(context: Context?, url: String?): String? {
        val length = if (url != null) url.length else 0
        if (url != null && length != 0) {
            if (length <= 13 || !url.startsWith("userscript://")) {
                val fileName =
                    if (URLUtil.isNetworkUrl(url)) guessDownloadName(url, null, null, true) else parentPathOf(context, url)
                val nameLength = if (fileName == null) 0 else fileName.length
                if (fileName == null || nameLength == 0) {
                    return fileName
                }
                val userJsIndex = fileName.lastIndexOf(".user.js")
                if (userJsIndex > 0 && userJsIndex < nameLength) {
                    return fileName.substring(0, userJsIndex)
                }
                val dotIndex = fileName.lastIndexOf(46.toChar())
                return if (dotIndex <= 0 || dotIndex >= nameLength) fileName else fileName.substring(
                    0,
                    dotIndex
                )
            }
            val tail = url.substring(13)
            if (!TextUtils.isEmpty(tail)) {
                return if (!tail.startsWith("user.")) tail else ("U" + tail.substring(
                    1
                ))
            }
        }
        return url
    }

    @JvmStatic
    fun hideDivsByClass(webView: WebView?, className: String?) {
        if (webView == null || TextUtils.isEmpty(className)) {
            return
        }
        val sb = StringBuilder()
        sb.append("var clns=document.querySelectorAll('div[class^=\"")
        sb.append(className)
        sb.append("\"]');if(clns&&(clns.length>0)){for(var i=0;i<clns.length;i++){clns[i].style.display='none';}}")
        sb.insert(0, "(function(){")
        sb.append("})();")
        evalJs(webView, sb.toString(), true)
    }

    @JvmStatic
    fun hasKnownScheme(url: String?): Boolean {
        if (TextUtils.isEmpty(url)) {
            return false
        }
        return url!!.startsWith("https:") || url.startsWith("http:") || url.startsWith("file:") || url.startsWith(
            "content:"
        ) || url.startsWith("inline:") || url.startsWith("data:") || url.startsWith("about:") || url.startsWith(
            "blob:"
        ) || url.startsWith("chrome:") || url.startsWith("javascript:") || url.startsWith("view-source:")
    }

    @JvmStatic
    fun postRequestLayout(view: View?) {
        if (view == null) {
            return
        }
        view.post(RequestFocusTask(view))
    }

    @JvmStatic
    fun addShortcut(context: Context?, webNestView: WebNestView?, paused: Boolean, playing: Boolean) {
        if (context == null || webNestView == null) {
            return
        }
        MainApp.Companion.runInBackground(
            context,
            NotificationsTask(context, webNestView.getTitle(), playing, paused, webNestView.getFavicon())
        )
    }

    @JvmStatic
    fun writeBytesEntry(bufferedWriter: BufferedWriter?, save: String?, bArr: ByteArray?): Boolean {
        var encoded = base64NoWrap(bArr)
        if (TextUtils.isEmpty(encoded)) {
            encoded = "null"
        }
        try {
            bufferedWriter!!.write(save)
            bufferedWriter.write("\n")
            bufferedWriter.write("b")
            bufferedWriter.write("\n")
            bufferedWriter.write(encoded)
            bufferedWriter.write("\n")
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun classifyChild(childItem: ChildItem?, ext: String?): ChildItem? {
        if (childItem != null && childItem.iconColor == 0) {
            if (childItem.isDir) {
                childItem.listType = 0
                childItem.itemType = 0.toByte()
                childItem.iconColor = 0
                childItem.iconRes = R.drawable.outline_folder_black_24
                return childItem
            }
            if (childItem.listType == 31) {
                childItem.itemType = 0.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_public_black_24
                return childItem
            }
            if (TextUtils.isEmpty(ext)) {
                childItem.listType = 43
                childItem.itemType = 0.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_draft_black_24
                return childItem
            }
            if (isAlbumExt(ext)) {
                childItem.listType = 1
                childItem.itemType = 4.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_local_library_black_24
                return childItem
            }
            if (isPdfExt(ext)) {
                childItem.listType = 2
                childItem.itemType = 5.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_picture_as_pdf_black_24
                return childItem
            }
            if (Compress.isArchiveExt(ext)) {
                childItem.listType = 3
                childItem.itemType = 0.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_folder_zip_black_24
                return childItem
            }
            if (isImageExt(ext)) {
                childItem.listType = 4
                childItem.itemType = 1.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_image_black_24
                return childItem
            }
            if (isVideoExt(ext)) {
                childItem.listType = 5
                childItem.itemType = 2.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.baseline_play_arrow_black_24
                return childItem
            }
            if (isAudioExt(ext)) {
                childItem.listType = 6
                childItem.itemType = 3.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.baseline_music_note_black_24
                return childItem
            }
            if (Compress.isDocExt(ext)) {
                childItem.listType = 7
                childItem.itemType = 0.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_description_black_24
                return childItem
            }
            if (isMhtExt(ext)) {
                childItem.listType = 8
                childItem.itemType = 11.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_public_black_24
                return childItem
            }
            if (isApkExt(ext)) {
                childItem.listType = 9
                childItem.itemType = 6.toByte()
                childItem.iconColor = -460552
                childItem.iconRes = R.drawable.outline_android_black_24
                return childItem
            }
            childItem.listType = 43
            childItem.itemType = 0.toByte()
            childItem.iconColor = -460552
            childItem.iconRes = R.drawable.outline_draft_black_24
        }
        return childItem
    }

    /** Opens `str` for reading: a content:// URI through the resolver, otherwise as a file path.  */
    @Throws(IOException::class)
    @JvmStatic
    fun openUriInput(context: Context?, input: String?): InputStream? {
        if (MainUri.isContentUri(input)) {
            return context!!.getContentResolver().openInputStream(Uri.parse(input))
        }
        return openFileInput(input)
    }

    @JvmStatic
    fun extractRedirect(url: String?): String? {
        val schemeEnd = schemeKind(url)
        if (schemeEnd == 0) {
            return url
        }
        val lowerCase = url!!.lowercase()
        val httpsIndex = lowerCase.indexOf("https://", schemeEnd)
        var end = -1
        if (httpsIndex != -1) {
            val start = httpsIndex + 8
            val httpsAmp = url.indexOf(38.toChar(), start)
            val httpsQuote = url.indexOf(34.toChar(), start)
            if (httpsAmp != -1) {
                end = if (httpsQuote != -1) min(httpsAmp, httpsQuote) else httpsAmp
            } else if (httpsQuote != -1) {
                end = httpsQuote
            }
            return if (end <= httpsIndex || end > url.length) url else url.substring(httpsIndex, end)
        }
        val httpIndex = lowerCase.indexOf("http://", schemeEnd)
        if (httpIndex != -1) {
            val start = httpIndex + 7
            val httpAmp = url.indexOf(38.toChar(), start)
            val httpQuote = url.indexOf(34.toChar(), start)
            if (httpAmp != -1) {
                end = if (httpQuote != -1) min(httpAmp, httpQuote) else httpAmp
            } else if (httpQuote != -1) {
                end = httpQuote
            }
            return if (end <= httpIndex || end > url.length) url else url.substring(
                httpIndex,
                end
            )
        }
        val encodedHttpsIndex = lowerCase.indexOf("https%3a%2f%2f", schemeEnd)
        if (encodedHttpsIndex != -1) {
            var encodedHttps = url.substring(encodedHttpsIndex)
            try {
                encodedHttps = URLDecoder.decode(encodedHttps, "UTF-8")
            } catch (unused: Exception) {
            }
            val encodedHttpsAmp = encodedHttps.indexOf(38.toChar())
            val encodedHttpsQuote = encodedHttps.indexOf(34.toChar())
            if (encodedHttpsAmp != -1) {
                end = if (encodedHttpsQuote != -1) min(encodedHttpsAmp, encodedHttpsQuote) else encodedHttpsAmp
            } else if (encodedHttpsQuote != -1) {
                end = encodedHttpsQuote
            }
            return if (end <= 0 || end > encodedHttps.length) encodedHttps else encodedHttps.substring(
                0,
                end
            )
        }
        val encodedHttpIndex = lowerCase.indexOf("http%3a%2f%2f", schemeEnd)
        if (encodedHttpIndex == -1) {
            val viewImageIndex = lowerCase.indexOf("/bbs/view_image.php?fn=", schemeEnd)
            val start = if (viewImageIndex == -1) 0 else (viewImageIndex + 23)
            if (viewImageIndex == -1 || start >= url.length) {
                return url
            }
            val host = hostOf(url, false)
            var viewImageTail = url.substring(start)
            try {
                viewImageTail = URLDecoder.decode(viewImageTail, "UTF-8")
            } catch (unused2: Exception) {
            }
            return "$host$viewImageTail"
        }
        var encodedHttp = url.substring(encodedHttpIndex)
        try {
            encodedHttp = URLDecoder.decode(encodedHttp, "UTF-8")
        } catch (unused3: Exception) {
        }
        val encodedHttpAmp = encodedHttp.indexOf(38.toChar())
        val encodedHttpQuote = encodedHttp.indexOf(34.toChar())
        if (encodedHttpAmp != -1) {
            end = if (encodedHttpQuote != -1) min(encodedHttpAmp, encodedHttpQuote) else encodedHttpAmp
        } else if (encodedHttpQuote != -1) {
            end = encodedHttpQuote
        }
        return if (end <= 0 || end > encodedHttp.length) encodedHttp else encodedHttp.substring(
            0,
            end
        )
    }

    @JvmStatic
    fun filterFilePath(context: Context?, path: String?): String? {
        if (context == null || TextUtils.isEmpty(path)) {
            return null
        }
        return filesSubdir(context, ".filter") + "/" + Md5FileNameGenerator().generate(path)
    }

    @JvmStatic
    fun indexOfCharIn(text: String?, target: Char, from: Int, end: Int): Int {
        var from = from
        var end = end
        try {
            val length = text!!.length
            if (length == 0 || from >= length) {
                return -1
            }
            if (from < 0) {
                from = 0
            }
            if (end >= length) {
                end = length - 1
            }
            val endExclusive = end + 1
            while (from < endExclusive) {
                if (text.get(from) == target) {
                    return from
                }
                from++
            }
            return -1
        } catch (unused: Exception) {
            return -1
        }
    }

    @JvmStatic
    fun hasImageExtAt(length: Int, pageUrl: String?): Boolean {
        val index = length - 4
        if (pageUrl!!.get(index) == '.') {
            val extStart = length - 3
            return pageUrl.startsWith("jpg", extStart) || pageUrl.startsWith("png", extStart) || pageUrl.startsWith(
                "gif",
                extStart
            )
        }
        if (pageUrl.get(length - 5) == '.') {
            return pageUrl.startsWith("webp", index) || pageUrl.startsWith("jpeg", index)
        }
        return false
    }

    @JvmStatic
    fun restartActivity(context: Context?, path: String?, onStartPage: Boolean) {
        val intentMakeRestartActivityTask: Intent
        if (MainApp.Companion.restarting) {
            return
        }
        MainApp.Companion.restarting = true
        cancelNotifications(context, true, true)
        try {
            if (isDarkPage(onStartPage)) {
                intentMakeRestartActivityTask =
                    Intent(context, MainSwitchBlack::class.java as Class<*>)
                intentMakeRestartActivityTask.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            } else {
                intentMakeRestartActivityTask = Intent.makeRestartActivityTask(
                    context!!.getPackageManager()
                        .getLaunchIntentForPackage(context.getPackageName())!!.getComponent()
                )
            }
            if (!TextUtils.isEmpty(path)) {
                intentMakeRestartActivityTask.putExtra("EXTRA_PATH", path)
                if (TtsSettings.appKeypad.value && path == "short_new_tab") {
                    intentMakeRestartActivityTask.putExtra("EXTRA_KEYPAD", true)
                }
            }
            context!!.startActivity(intentMakeRestartActivityTask)
            if (context is Activity) {
                if (Build.VERSION.SDK_INT >= 34) {
                    context.overrideActivityTransition(
                        Activity.OVERRIDE_TRANSITION_OPEN,
                        R.anim.slide_in_left,
                        R.anim.slide_out_left
                    )
                } else {
                    context.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
            }
        } catch (unused: Exception) {
        }
        if (AppState.exiting) {
            return
        }
        AppState.exiting = true
        try {
            System.exit(0)
        } catch (unused2: Exception) {
        }
    }

    @JvmStatic
    fun openKeyboard(context: Context?, view: View?) {
        if (context == null || view == null) {
            return
        }
        val mainAppO: MainApp? = MainApp.Companion.from(context)
        if (mainAppO == null || mainAppO.inputMethodCache == null) {
            MainApp.Companion.runInBackground(context, InputMethodsTask(context, view))
        } else {
            MainApp.Companion.postToMain(context, ShowKeyboardTask(context, view))
        }
    }

    @JvmStatic
    fun writeIntEntry(bufferedWriter: BufferedWriter?, save: String?, intValue: Int): Boolean {
        try {
            bufferedWriter!!.write(save)
            bufferedWriter.write("\n")
            bufferedWriter.write("i")
            bufferedWriter.write("\n")
            bufferedWriter.write(intValue.toString())
            bufferedWriter.write("\n")
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun classifyChildIfNeeded(childItem: ChildItem?) {
        if (childItem == null || childItem.iconColor != 0) {
            return
        }
        classifyChild(childItem, if (childItem.isDir) null else lowerExtOf(childItem.name))
    }

    @JvmStatic
    fun intentToUrl(url: String?, strict: Boolean): String? {
        var url = url
        if (TextUtils.isEmpty(url)) {
            return null
        }
        if (!url!!.startsWith("intent")) {
            if (TextUtils.isEmpty(url)) {
                return null
            }
            try {
                val httpIndex = url.indexOf("http", 4)
                if (httpIndex < 4) {
                    return null
                }
                val decodedUrl = URLDecoder.decode(url.substring(httpIndex), "UTF-8")
                if (URLUtil.isNetworkUrl(decodedUrl)) {
                    return decodedUrl
                }
                return null
            } catch (unused: Exception) {
                return null
            }
        }
        try {
            val uri = Intent.parseUri(url, Intent.URI_INTENT_SCHEME)
            if (uri == null) {
                return null
            }
            url = uri.getStringExtra("browser_fallback_url")
            if (TextUtils.isEmpty(url) || url!!.startsWith("https://play.google.com")) {
                return null
            }
        } catch (unused2: URISyntaxException) {
        }
        if (strict) {
            val host = MainUtil.hostOf(url!!, true)
            if (TextUtils.isEmpty(host) || !host!!.endsWith("yahoo.co.jp")) {
                return null
            }
        }
        if (hasKnownScheme(url)) {
            return url
        }
        return null
    }

    @JvmStatic
    fun alphaFromPercent(penAlpha: Int): Int {
        var penAlpha = penAlpha
        if (penAlpha < 0) {
            penAlpha = 0
        } else if (penAlpha > 90) {
            penAlpha = 90
        }
        return Math.round(((100 - penAlpha) * KotlinVersion.MAX_COMPONENT_VALUE) / 100.0f)
    }

    @JvmStatic
    fun replaceExt(cleaned: String?, ext: String?): String? {
        if (TextUtils.isEmpty(cleaned) || TextUtils.isEmpty(ext)) {
            return null
        }
        if (cleaned!!.length <= ext!!.length) {
            return cleaned + ext
        }
        val dotIndex = cleaned.lastIndexOf(46.toChar())
        if (dotIndex <= 0 || dotIndex >= cleaned.length) {
            return cleaned + ext
        }
        val oldExt = cleaned.substring(dotIndex)
        if (TextUtils.isEmpty(oldExt)) {
            return cleaned + ext
        }
        if (oldExt == ext) {
            return cleaned
        }
        return if (oldExt.length > 4) (cleaned + ext) else (cleaned.substring(
            0,
            dotIndex
        ) + ext)
    }

    @JvmStatic
    fun indexOfWord(text: String?, fromIndex: Int, endIndex: Int, word: String?): Int {
        var fromIndex = fromIndex
        var endIndex = endIndex
        try {
            val length = text!!.length
            if (length == 0 || fromIndex >= length) {
                return -1
            }
            if (fromIndex < 0) {
                fromIndex = 0
            }
            if (endIndex >= length) {
                endIndex = length - 1
            }
            val wordSize = word!!.length
            if (wordSize == 0) {
                return -1
            }
            val firstChar = word.get(0)
            val start = (endIndex - wordSize) + 2
            while (fromIndex < start) {
                if (text.get(fromIndex) != firstChar) {
                    do {
                        fromIndex++
                        if (fromIndex >= start) {
                            break
                        }
                    } while (text.get(fromIndex) != firstChar)
                }
                if (fromIndex < start) {
                    val end = fromIndex + wordSize
                    var textPos = fromIndex + 1
                    var index = 1
                    while (textPos < end && text.get(textPos) == word.get(index)) {
                        textPos++
                        index++
                    }
                    if (textPos == end) {
                        return fromIndex
                    }
                }
                fromIndex++
            }
        } catch (unused: Exception) {
        }
        return -1
    }

    @JvmStatic
    fun isImagePath(pageUrl: String?): Boolean {
        val length = if (pageUrl == null) 0 else pageUrl.length
        if (pageUrl == null || length < 10) {
            return false
        }
        if (pageUrl.startsWith("data:image/") || hasImageExtAt(length, pageUrl)) {
            return true
        }
        val queryIndex = pageUrl.indexOf(63.toChar())
        if (queryIndex < 10) {
            return false
        }
        return hasImageExtAt(queryIndex, pageUrl)
    }

    @JvmStatic
    fun restartTask(activity: Activity?, path: String?, onStartPage: Boolean) {
        if (MainConst.isApi28) {
            MainUtil.restartActivity(activity!!, path, onStartPage)
            return
        }
        if (activity != null) {
            cancelNotifications(activity, true, false)
            try {
                activity.finishAffinity()
                val intentL = browserIntent(activity)
                if (!TextUtils.isEmpty(path)) {
                    intentL.putExtra("EXTRA_PATH", path)
                }
                activity.startActivity(intentL)
            } catch (unused: Exception) {
            }
        }
    }

    @JvmStatic
    fun lockAppData(context: Context?) {
        if (!SecretSettings.secretNoti.value || context == null) {
            return
        }
        MainApp.Companion.runInBackground(context, ShowSecretNotiTask(context))
    }

    @JvmStatic
    fun writeLineMark(bufferedWriter: BufferedWriter?): Boolean {
        try {
            bufferedWriter!!.write("//|#_line_#|//")
            bufferedWriter.write("\n")
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    /** Opens a local file for reading.  */
    @Throws(IOException::class)
    @JvmStatic
    fun openFileInput(src: String?): InputStream? {
        return Files.newInputStream(Paths.get(src))
    }

    @JvmStatic
    fun parseIntList(valueText: String?): IntArray? {
        var values: IntArray? = null
        if (TextUtils.isEmpty(valueText)) {
            return null
        }
        val parts: Array<String?> =
            valueText!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size != 0) {
            val length = parts.size
            values = IntArray(length)
            for (index in 0..<length) {
                val value = parseIntSafe(parts[index])
                // action ids, and button groups on the bars (ui/browser/BarGroups)
                if ((value >= 0 && value < 74) || com.mycompany.app.ui.browser.BarGroups.isGroup(value)) {
                    values[index] = value
                }
            }
        }
        return values
    }

    @JvmStatic
    fun phTitle(name: String?): String? {
        if (!TextUtils.isEmpty(name) && name!!.length > 14 && name.endsWith(" - Pornhub.com")) {
            val trimmedName = name.let { s -> s.substring(0, s.length - 14) }
            if (!TextUtils.isEmpty(trimmedName)) {
                return trimmedName
            }
        }
        return name
    }

    @JvmStatic
    fun appVersion(context: Context?): String? {
        var packageInfo: PackageInfo?
        if (context == null) {
            return null
        }
        try {
            packageInfo = if (Build.VERSION.SDK_INT >= 33) context.getPackageManager()
                .getPackageInfo(
                    context.getPackageName(),
                    PackageManager.PackageInfoFlags.of(0L)
                ) else context.getPackageManager().getPackageInfo(context.getPackageName(), 0)
        } catch (unused: Exception) {
            packageInfo = null
        }
        if (packageInfo == null) {
            return null
        }
        return packageInfo.versionName
    }

    @JvmStatic
    fun disablePersonalizedIme(editText: EditText?) {
        if (!isSecretLocked() || editText == null) {
            return
        }
        try {
            editText.setImeOptions(editText.getImeOptions() or 16777216)
        } catch (unused: Exception) {
        }
    }

    @JvmStatic
    fun isPhnGif(url: String?): Boolean {
        val host = hostOf(url, true)
        if (!TextUtils.isEmpty(host) && host!!.endsWith("phncdn.com") && url!!.startsWith(
                "/pics/gifs/",
                host.length
            )
        ) {
            return "gif".equals(fileExtOf(url, false), ignoreCase = true)
        }
        return false
    }

    @JvmStatic
    fun faviconUrlFor(iconUrl: String?): String? {
        val basePath = pathOfUrl(iconUrl, true)
        if (TextUtils.isEmpty(basePath)) {
            return null
        }
        val lowerCase = basePath!!.lowercase()
        if (TextUtils.isEmpty(lowerCase)) {
            return null
        }
        return "https://www.google.com/s2/favicons?sz=64&domain_url=$lowerCase"
    }

    @JvmStatic
    fun showToast(context: Context?, invalidPath: Int) {
        if (context == null) {
            return
        }
        cancelToast()
        // OPUM's in-app message pill (2026-11-20); system toast only without a visible screen
        val text = try { context.getString(invalidPath) } catch (_: Exception) { return }
        com.mycompany.app.ui.components.OpumMessage.show(context, text)
    }

    @JvmStatic
    fun writeLongEntry(bufferedWriter: BufferedWriter?, save: String?, longNumber: Long): Boolean {
        try {
            bufferedWriter!!.write(save)
            bufferedWriter.write("\n")
            bufferedWriter.write("l")
            bufferedWriter.write("\n")
            bufferedWriter.write(longNumber.toString())
            bufferedWriter.write("\n")
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    @JvmStatic
    fun parentPathOf(context: Context?, dir: String?): String? {
        var index: Int
        val length = if (dir == null) 0 else dir.length
        if (dir == null || length == 0) {
            return null
        }
        if (context != null && MainUri.isContentUri(dir)) {
            return MainUri.treeDisplayPath(context, dir)
        }
        var trimmedLength = length
        while (true) {
            index = trimmedLength - 1
            if (index <= 0 || dir.get(index) != '/') {
                break
            }
            trimmedLength--
        }
        if (trimmedLength > 1) {
            val lastSlash = dir.lastIndexOf(47.toChar(), index)
            val nameStart = if (lastSlash >= 0) (lastSlash + 1) else 0
            if (lastSlash >= 0 && nameStart < trimmedLength) {
                return if (trimmedLength == length) dir.substring(nameStart) else dir.substring(nameStart, trimmedLength)
            }
            if (trimmedLength != length) {
                return dir.substring(0, trimmedLength)
            }
        } else if (length != 1) {
            return dir.substring(0, 1)
        }
        return dir
    }

    @JvmStatic
    fun joinIntList(values: IntArray?): String? {
        if (values == null) {
            return null
        }
        var sb: StringBuilder? = null
        for (index in values) {
            if ((index >= 0 && index < 74) || com.mycompany.app.ui.browser.BarGroups.isGroup(index)) {
                if (sb == null) {
                    sb = StringBuilder()
                } else {
                    sb.append(",")
                }
                sb.append(index)
            }
        }
        if (sb == null) {
            return null
        }
        return sb.toString()
    }

    @JvmStatic
    fun pinterestOriginal(url: String?): String? {
        val sizeStart = if (TextUtils.isEmpty(url) || !url!!.startsWith("https://i.pinimg.com/") || url.startsWith(
                "736x/",
                21
            )) 0 else url.indexOf("x/", 21)
        val sizeEnd = if (TextUtils.isEmpty(url) || !url!!.startsWith("https://i.pinimg.com/") || url.startsWith(
                "736x/",
                21
            ) || sizeStart <= 21) 0 else parseColorSafe(
                url.substring(
                    21,
                    sizeStart
                )
            )
        val sizeAt = if (TextUtils.isEmpty(url) || !url!!.startsWith("https://i.pinimg.com/") || url.startsWith(
                "736x/",
                21
            ) || sizeStart <= 21 || sizeEnd == -1 || sizeEnd >= 736) 0 else (sizeStart + 2)
        if (TextUtils.isEmpty(url) || !url!!.startsWith("https://i.pinimg.com/") || url.startsWith(
                "736x/",
                21
            ) || sizeStart <= 21 || sizeEnd == -1 || sizeEnd >= 736 || sizeAt >= url.length) {
            return null
        }
        val tail = url.substring(sizeAt)
        if (TextUtils.isEmpty(tail)) {
            return null
        }
        return "https://i.pinimg.com/736x/" + tail
    }

    @JvmStatic
    fun downMenuCount(tint: Int, withVideo: Boolean): Int {
        val showPip = ZoneSettings.showPip.value
        val showFull = ZoneSettings.showFull.value
        var index = if (showFull) (if (showPip) 1 else 0) + 1 else if (showPip) 1 else 0
        if (ZoneSettings.showDown.value) {
            index++
        }
        if (index > 1) {
            if (withVideo) {
                return R.drawable.outline_video_library_white_24
            }
            return if (tint == 0) R.drawable.outline_video_library_black_24 else R.drawable.outline_video_library_dark_24
        }
        if (showPip) {
            if (withVideo) {
                return R.drawable.outline_picture_in_picture_alt_white_24
            }
            return if (tint == 0) R.drawable.outline_picture_in_picture_alt_black_24 else R.drawable.outline_picture_in_picture_alt_dark_24
        }
        if (showFull) {
            if (withVideo) {
                return R.drawable.outline_fullscreen_white_24
            }
            return if (tint == 0) R.drawable.outline_fullscreen_black_24 else R.drawable.outline_fullscreen_dark_24
        }
        if (withVideo) {
            return R.drawable.outline_download_white_24
        }
        return if (tint == 0) R.drawable.outline_download_black_24 else R.drawable.outline_download_dark_24
    }

    @JvmStatic
    fun initUserAgent(context: Context?) {
        if (TextUtils.isEmpty(MainApp.Companion.defaultUserAgent) || TextUtils.isEmpty(ZtwoSettings.soulAgent.value)) {
            var userAgent: String? = MainApp.Companion.defaultUserAgent
            if (TextUtils.isEmpty(userAgent)) {
                userAgent = null
                if (context != null) {
                    try {
                        userAgent = WebSettings.getDefaultUserAgent(context)
                    } catch (unused: Exception) {
                    } catch (unused: UnsatisfiedLinkError) {
                    }
                }
                if (TextUtils.isEmpty(userAgent)) {
                    return
                } else {
                    MainApp.Companion.defaultUserAgent = userAgent
                }
            }
            if (userAgent != null && userAgent.length != 0) {
                if (TextUtils.isEmpty(ZtwoSettings.soulAgentName.value)) {
                    val sb = StringBuilder()
                    sb.append(randomBetween(65, 90).toChar())
                    val minorVersion = randomBetween(3, 9)
                    for (index in 0..<minorVersion) {
                        sb.append(randomBetween(97, 122).toChar())
                    }
                    sb.append("/")
                    val agentName = sb.toString()
                    ZtwoSettings.edit(context) { set(ZtwoSettings.soulAgentName, agentName) }
                }
                userAgent = userAgent.replace("; wv", "").replace("Version/", ZtwoSettings.soulAgentName.value!!)
            }
            if (TextUtils.isEmpty(userAgent) || userAgent == ZtwoSettings.soulAgent.value) {
                return
            }
            ZtwoSettings.edit(context) { set(ZtwoSettings.soulAgent, userAgent) }
        }
    }

    @JvmStatic
    fun isUserScriptUrl(scriptInstallUrl: String?): Boolean {
        val pathEnd = MainUrl.pathEnd(scriptInstallUrl)
        if (pathEnd >= 6 && scriptInstallUrl!!.startsWith(".user.js", pathEnd - 5) && URLUtil.isNetworkUrl(scriptInstallUrl)) {
            return !scriptInstallUrl.startsWith("https://github.com/")
        }
        return false
    }

    @JvmStatic
    fun applyNightMode() {
        val nightMode: Int
        if (BrowserSettings.themeSys.value) {
            nightMode = -1
        } else {
            nightMode = if (BrowserSettings.themeWeb.value == 1) 2 else 1
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    @JvmStatic
    fun showToastText(context: Context?, charSequence: CharSequence?) {
        if (context == null) {
            return
        }
        cancelToast()
        com.mycompany.app.ui.components.OpumMessage.show(context, charSequence)
    }

    @JvmStatic
    fun fillAllFlags(flags: BooleanArray?, boolValue: Boolean) {
        if (flags == null) {
            return
        }
        Arrays.fill(flags, boolValue)
    }

    @JvmStatic
    fun writeStringEntry(bufferedWriter: BufferedWriter?, key: String?, value: String?): Boolean {
        var value = value
        if (TextUtils.isEmpty(value)) {
            value = "null"
        }
        try {
            bufferedWriter!!.write(key)
            bufferedWriter.write("\n")
            bufferedWriter.write("s")
            bufferedWriter.write("\n")
            bufferedWriter.write(value)
            bufferedWriter.write("\n")
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    /** Opens a local file for writing (`z`: append).  */
    @Throws(IOException::class)
    @JvmStatic
    fun openFileOutput(pathText: String?, append: Boolean): OutputStream? {
        val path = Paths.get(pathText)
        return if (append) Files.newOutputStream(
            path,
            StandardOpenOption.CREATE,
            StandardOpenOption.APPEND
        ) else Files.newOutputStream(path)
    }

    @JvmStatic
    fun jsDelivrToGithub(path: String?): String? {
        if (path == null || path.length <= 28 || !path.startsWith("https://cdn.jsdelivr.net/gh/")) {
            return null
        }
        var masterIndex = path.indexOf("@master/", 28)
        masterIndex = if (masterIndex == -1) path.indexOf("@main/", 28) else 0
        if (masterIndex == -1 && masterIndex == -1) {
            return null
        }
        val repo = path.substring(28, masterIndex)
        if (TextUtils.isEmpty(repo)) {
            return null
        }
        val filePath = path.substring(masterIndex + 1)
        if (TextUtils.isEmpty(filePath)) {
            return null
        }
        return "https://raw.githubusercontent.com/$repo/$filePath"
    }

    @JvmStatic
    fun pipAspectRatio(pipWidth: Int, pipHeight: Int): Rational {
        if (pipWidth == 1280 && pipHeight == 720) {
            return Rational(pipWidth, pipHeight)
        }
        val rational = Rational(pipWidth, pipHeight)
        val ratio = rational.toFloat()
        if (ratio < 0.41841f) {
            return Rational(Math.round(pipHeight * 0.42f), pipHeight)
        }
        return if (ratio > 2.39f) Rational(pipWidth, Math.round(pipWidth / 2.38f)) else rational
    }

    @JvmStatic
    fun childThumbCached(context: Context?, childItem: ChildItem?): Bitmap? {
        if (TextUtils.isEmpty(childItem!!.path)) {
            return null
        }
        if (TextUtils.isEmpty(childItem.filePath)) {
            childItem.filePath = cacheFilePathFor(context, childItem.path)
        }
        if (File(childItem.filePath!!).length() > 0) {
            return BitmapUtil.decodeFile(childItem.filePath)
        }
        val bitmapC = videoFrameBitmap(context, childItem.path)
        saveBitmapPng(context, bitmapC, childItem.filePath)
        return bitmapC
    }

    @JvmStatic
    fun isIpAddress(pageUrl: String?): Boolean {
        if (pageUrl == null) {
            return false
        }
        val length = pageUrl.length
        if (length == 0) {
            return false
        }
        val lastIndex = min(length - 1, 80)
        for (pattern in MainConst.loginWords) {
            try {
                if (b5Find(pageUrl, pattern, lastIndex)) {
                    return true
                }
            } catch (unused: Exception) {
            }
        }
        return false
    }

    /**
     * Inlined search used by [.b5]: looks for `pattern` in `str` starting at index 7 and
     * ending before `iMin`. If the first character matched as upper case, the rest may match either case.
     */
    private fun b5Find(text: String?, pattern: String?, endLimit: Int): Boolean {
        val len = text!!.length
        if (len == 0 || 7 >= len) {
            return false
        }
        val end = if (endLimit >= len) len - 1 else endLimit
        val plen = pattern!!.length
        if (plen == 0) {
            return false
        }
        val p0 = pattern.get(0)
        val p0Upper = (p0.code - 32).toChar()
        val limit = end - plen + 2
        var pos = 7
        while (pos < limit) {
            var upper = false
            if (text.get(pos) != p0) {
                while (true) {
                    pos++
                    if (pos >= limit) {
                        break
                    }
                    val c = text.get(pos)
                    if (c == p0) {
                        break
                    }
                    if (c == p0Upper) {
                        upper = true
                        break
                    }
                }
            }
            if (pos >= limit) {
                return false
            }
            val stop = pos + plen
            var idx = pos + 1
            var k = 1
            if (upper) {
                while (idx < stop) {
                    val s = text.get(idx)
                    val pc = pattern.get(k)
                    if (s != pc && s != (pc.code - 32).toChar()) {
                        break
                    }
                    idx++
                    k++
                }
            } else {
                while (idx < stop && text.get(idx) == pattern.get(k)) {
                    idx++
                    k++
                }
            }
            if (idx == stop) {
                return true
            }
            pos++
        }
        return false
    }

    @JvmStatic
    fun isBitmapOk(bitmap: Bitmap?): Boolean {
        return if (bitmap == null || bitmap.isRecycled() || bitmap.getWidth() == 0 || bitmap.getHeight() == 0) false else true
    }

    @JvmStatic
    fun applyWindowBrightness(window: Window?, index: Int, user: Boolean) {
        var index = index
        val screenBrightness: kotlin.Float
        if (window != null) {
            if (user) {
                if (index < 0) {
                    index = 0
                } else if (index > 100) {
                    index = 100
                }
                screenBrightness = index / 100.0f
            } else {
                screenBrightness = -1.0f
            }
            val attributes = window.getAttributes()
            if ((attributes.screenBrightness).compareTo(screenBrightness) != 0) {
                attributes.screenBrightness = screenBrightness
                window.setAttributes(attributes)
                window.addFlags(4)
            }
        }
    }

    @JvmStatic
    fun childComparator(group: Int, key: Int, reverse: Boolean): Comparator<ChildItem?> {
        if (key == 0) {
            return SortChildName(group, reverse)
        }
        if (key == 1) {
            return SortChildData(group, reverse)
        }
        if (key == 2) {
            return SortChildExt(group, reverse)
        }
        if (key != 3) {
            return if (key != 4) SortChildName(group, reverse) else SortChildSize(group, reverse)
        }
        return SortChildTime(group, reverse)
    }

    @JvmStatic
    fun cancelToast() {
        com.mycompany.app.ui.components.OpumMessage.hide()
        val toast: Toast? = MainApp.Companion.lastToast
        MainApp.Companion.lastToast = null
        if (toast != null) {
            try {
                toast.cancel()
            } catch (unused: Exception) {
            }
        }
    }

    @JvmStatic
    fun decodeDataUri(context: Context?, dataUri: String?): Bitmap? {
        var bitmap: Bitmap?
        var byteArrayInputStream: ByteArrayInputStream? = null
        var bitmapDecodeStream: Bitmap? = null
        if (context == null || TextUtils.isEmpty(dataUri)) {
            return null
        }
        try {
            val commaIndex = dataUri!!.indexOf(44.toChar())
            val dataStart = if (commaIndex < 0) 0 else (commaIndex + 1)
            if (commaIndex < 0 || dataStart >= dataUri.length) {
                return null
            }
            val dataStream =
                ByteArrayInputStream(Base64.decode(dataUri.substring(dataStart).toByteArray(), 0))
            try {
                bitmapDecodeStream = BitmapFactory.decodeStream(dataStream)
            } catch (unused: Exception) {
            } catch (unused: OutOfMemoryError) {
            }
            bitmap = bitmapDecodeStream
            byteArrayInputStream = dataStream
        } catch (unused2: Exception) {
            bitmap = null
        } catch (unused2: OutOfMemoryError) {
            bitmap = null
        }
        if (byteArrayInputStream != null) {
            try {
                byteArrayInputStream.close()
            } catch (unused3: Exception) {
            }
        }
        return bitmap
    }

    @JvmStatic
    fun uriDisplayName(context: Context?, dir: String?): String? {
        var dir = dir
        if (!TextUtils.isEmpty(dir)) {
            if (context != null && MainUri.isContentUri(dir)) {
                if (TextUtils.isEmpty(dir)) {
                    return null
                }
                MainUri.documentFile(context, dir)
                val start = max(
                    max(dir!!.lastIndexOf("%2F"), dir.lastIndexOf("%3A")),
                    dir.lastIndexOf(47.toChar())
                )
                return if (start <= 0 || start >= dir.length) dir else dir.substring(0, start)
            }
            while (dir!!.endsWith("/")) {
                if (dir.length <= 1) {
                    return "/"
                }
                dir = dir.let { s -> s.substring(0, s.length - 1) }
            }
            val lastSlash = dir.lastIndexOf(47.toChar())
            if (lastSlash != -1) {
                val parentDir = dir.substring(0, lastSlash)
                if (!TextUtils.isEmpty(parentDir)) {
                    return parentDir
                }
            }
        }
        return "/"
    }

    @JvmStatic
    fun requestHeadersFor2(context: Context?, videoUrl: String?, pageUrl: String?): HashMap<String, String> {
        var pageUrl = pageUrl
        val cookies: String?
        val referer: String?
        val map: HashMap<String, String> = HashMap()
        var found = false
        if (isGoogleSearch(pageUrl)) {
            referer = null
            cookies = null
        } else {
            if (isGoogleUserContent(videoUrl)) {
                pageUrl = videoUrl
            } else if (TextUtils.isEmpty(pageUrl)) {
                val mainAppO: MainApp? = MainApp.Companion.from(context)
                if (mainAppO != null && "https://missav.com" == mainAppO.hostCacheValue(videoUrl)) {
                    found = true
                }
            } else {
                val mainAppO: MainApp? = MainApp.Companion.from(context)
                if (mainAppO != null) {
                    val cachedHost = mainAppO.hostCacheValue(videoUrl)
                    if (!TextUtils.isEmpty(cachedHost)) {
                        map.put("Origin", cachedHost!!)
                    }
                }
            }
            val cookie = cookieFor(context, pageUrl)
            if (TextUtils.isEmpty(pageUrl)) {
                pageUrl = videoUrl
            }
            val pageReferer = pageUrl
            cookies = cookie
            referer = pageReferer
        }
        map.put("Accept", acceptHeaderFor(videoUrl))
        map.put("Accept-Encoding", "gzip, identity")
        if (!found) {
            map.put("User-Agent", userAgentPref())
        }
        if (!TextUtils.isEmpty(referer)) {
            map.put("Referer", referer!!)
        }
        if (!TextUtils.isEmpty(cookies)) {
            map.put("Cookie", cookies!!)
        }
        return map
    }

    @JvmStatic
    fun htmlDataUri(webUrlIn: String?, hidden: Boolean): String? {
        if (TextUtils.isEmpty(webUrlIn)) {
            return webUrlIn
        }
        return if (hidden) "data:text/html;charset=utf-8,<?xml version=\"1.0\"encoding=\"UTF-8\"?><html><head><style>html{background-color:black;}</style><meta name=\"viewport\"content=\"width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=5.0,user-scalable=yes\"></head><body><video src=\"$webUrlIn\"width=\"100%\"height=\"100%\"muted autoplay loop controls controlsList=\"nodownload\"></video></body></html>" else "data:text/html;charset=utf-8,<?xml version=\"1.0\"encoding=\"UTF-8\"?><html><head><style>html{background-color:black;}</style><meta name=\"viewport\"content=\"width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=1.0,user-scalable=no\"></head><body><audio src=\"$webUrlIn\"style=\"width:100%\"preload=\"auto\"autoplay loop controls controlsList=\"nodownload noplaybackrate\"></audio></body></html>"
    }

    @JvmStatic
    fun videoFrameBitmap(context: Context?, input: String?): Bitmap? {
        var bitmapH: Bitmap?
        var frameAtTime: Bitmap?
        if (context != null && !TextUtils.isEmpty(input)) {
            try {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                try {
                    mediaMetadataRetriever.setDataSource(context, Uri.parse(input))
                    mediaMetadataRetriever.extractMetadata(1)
                    frameAtTime = mediaMetadataRetriever.getFrameAtTime(1000000L, MediaMetadataRetriever.OPTION_CLOSEST)
                } catch (unused: Exception) {
                    frameAtTime = null
                } catch (unused: OutOfMemoryError) {
                    frameAtTime = null
                }
                releaseRetriever(mediaMetadataRetriever)
                if (isBitmapOk(frameAtTime)) {
                    if (frameAtTime!!.getWidth() > MainApp.Companion.dp48 || frameAtTime.getHeight() > MainApp.Companion.dp48) {
                        val width = frameAtTime.getWidth()
                        val height = frameAtTime.getHeight()
                        val dp48Px: Int = MainApp.Companion.dp48
                        val sampleSize = sampleSizeFor(width, height, dp48Px, dp48Px)
                        if (sampleSize > 1) {
                            val sample = sampleSize.toFloat()
                            var scaledWidth = Math.round(frameAtTime.getWidth() / sample)
                            var scaledHeight = Math.round(frameAtTime.getHeight() / sample)
                            if (scaledWidth == 0) {
                                scaledWidth = 8
                            }
                            if (scaledHeight == 0) {
                                scaledHeight = 8
                            }
                            bitmapH = scaleBitmap(scaledWidth, scaledHeight, frameAtTime)
                            try {
                                frameAtTime.recycle()
                            } catch (unused2: Exception) {
                            } catch (unused2: OutOfMemoryError) {
                            }
                            if (isBitmapOk(bitmapH)) {
                                return bitmapH
                            }
                        }
                    }
                    return frameAtTime
                }
            } catch (unused3: Exception) {
                bitmapH = null
            } catch (unused3: OutOfMemoryError) {
                bitmapH = null
            }
        }
        return null
    }

    @JvmStatic
    fun isChinaLocale(): Boolean {
        val localeI: Locale = MainApp.Companion.currentLocale()
        return "CN".equals(localeI.getCountry(), ignoreCase = true)
    }

    @JvmStatic
    fun isImageUrl(context: Context?, audioUrl: String?, referer: String?): Boolean {
        var contentType: String?
        val httpURLConnectionQ = openHeadConnection(context, audioUrl, referer, 0, 0, false, userAgentPref(), true)
        var found = false
        if (httpURLConnectionQ == null) {
            return false
        }
        try {
            httpURLConnectionQ.setDoInput(true)
            httpURLConnectionQ.connect()
            if (httpURLConnectionQ.getResponseCode() == 200) {
                try {
                    contentType = httpURLConnectionQ.getContentType()
                } catch (unused: Exception) {
                    contentType = null
                }
                found = TextUtils.isEmpty(contentType) || !contentType!!.startsWith("text/html")
                if (!found) {
                    val mime = mimeForPath(audioUrl)
                    if (!TextUtils.isEmpty(mime) && mime!!.startsWith("text/html")) {
                        found = true
                    }
                }
            }
        } catch (unused2: Exception) {
        }
        httpURLConnectionQ.disconnect()
        return found
    }

    @JvmStatic
    fun animateIcon(context: Context?, view: View?, animRes: Int, tagValue: Boolean) {
        if (view != null) {
            if (animRes == R.anim.ic_rotate_in) {
                if (view.getVisibility() == View.VISIBLE) {
                    view.setTag(tagValue)
                    view.clearAnimation()
                    return
                }
            } else if (animRes == R.anim.ic_rotate_out && view.getVisibility() != View.VISIBLE) {
                view.setTag(tagValue)
                view.clearAnimation()
                return
            }
            val animationLoadAnimation = AnimationUtils.loadAnimation(context, animRes)
            if (animationLoadAnimation == null) {
                return
            }
            animationLoadAnimation.setAnimationListener(HideAfterAnimation(view))
            view.setTag(tagValue)
            view.clearAnimation()
            view.setVisibility(View.VISIBLE)
            view.startAnimation(animationLoadAnimation)
        }
    }

    /**
     * Natural order for names: runs of digits compare by their value ("a2" < "a10"; with equal value the one with more
     * leading zeros is greater), whitespace is skipped, everything else compares by its (modified UTF-8) byte value, so
     * upper case comes before lower case. At equal content the longer string is greater; an empty one is greater than
     * any other.
     */
    private fun compareName(first: String?, second: String?): Int {
        val a = modifiedUtf8(first)
        val b = modifiedUtf8(second)
        if (a.isEmpty() && b.isEmpty()) return 0
        if (a.isEmpty()) return 1
        if (b.isEmpty()) return -1
        fun isSpace(c: Int) = c == 32 || c in 9..13
        fun isDigit(c: Int) = c in 48..57
        var i = 0
        var j = 0
        while (true) {
            while (i < a.size && isSpace(a[i].toInt() and 0xff)) i++
            while (j < b.size && isSpace(b[j].toInt() and 0xff)) j++
            if (i >= a.size || j >= b.size) break
            val x = a[i].toInt() and 0xff
            val y = b[j].toInt() and 0xff
            if (isDigit(x) && isDigit(y)) {
                var si = i
                while (i < a.size && isDigit(a[i].toInt())) i++
                var sj = j
                while (j < b.size && isDigit(b[j].toInt())) j++
                while (si < i && a[si] == '0'.code.toByte()) si++
                while (sj < j && b[sj] == '0'.code.toByte()) sj++
                val lenA = i - si
                val lenB = j - sj
                if (lenA != lenB) return if (lenA < lenB) -1 else 1
                for (k in 0 until lenA) {
                    val da = a[si + k].toInt()
                    val db = b[sj + k].toInt()
                    if (da != db) return if (da < db) -1 else 1
                }
                continue
            }
            if (x != y) return if (x < y) -1 else 1
            i++
            j++
        }
        val restA = a.size - i
        val restB = b.size - j
        if (restA != restB) return if (restA < restB) -1 else 1
        if (a.size != b.size) return if (a.size < b.size) -1 else 1
        return 0
    }

    /** The string as Java's modified UTF-8 (each UTF-16 unit on its own, U+0000 as two bytes). */
    private fun modifiedUtf8(text: String?): ByteArray {
        if (text.isNullOrEmpty()) return ByteArray(0)
        val out = java.io.ByteArrayOutputStream(text.length + 8)
        for (ch in text) {
            val c = ch.code
            when {
                c in 1..0x7f -> out.write(c)
                c <= 0x7ff -> { out.write(0xc0 or (c shr 6)); out.write(0x80 or (c and 0x3f)) }
                else -> { out.write(0xe0 or (c shr 12)); out.write(0x80 or ((c shr 6) and 0x3f)); out.write(0x80 or (c and 0x3f)) }
            }
        }
        return out.toByteArray()
    }

    @JvmStatic
    fun openWithApp(activity: Activity?, path: String?, mime: String?, multiple: Boolean, chooser: Boolean): Boolean {
        var mime = mime
        if (activity != null && !TextUtils.isEmpty(path)) {
            if (TextUtils.isEmpty(mime)) {
                mime = mimeForUrl(MainUri.treeDisplayPath(activity, path))
                if (TextUtils.isEmpty(mime)) {
                    if (multiple) {
                        mime = "*/*"
                    }
                }
            }
            val index = Build.VERSION.SDK_INT
            if (index >= 26) {
                if ((if (TextUtils.isEmpty(mime)) false else (mime == "application/vnd.android.package-archive")) && hasPermission(
                        activity,
                        7
                    )
                ) {
                    if (index < 26) {
                        return true
                    }
                    val intent = Intent(activity, MainPermission::class.java as Class<*>)
                    intent.putExtra("EXTRA_TYPE", 7)
                    if (!TextUtils.isEmpty(path)) {
                        intent.putExtra("EXTRA_PATH", path)
                        intent.putExtra("EXTRA_EXT", chooser)
                    }
                    activity.startActivity(intent)
                    return true
                }
            }
            return openFileWith(activity, path, mime, multiple, chooser)
        }
        return false
    }

    @JvmStatic
    fun sampleSizeFor(width: Int, outHeight: Int, reqWidth: Int, reqHeight: Int): Int {
        val halfWidth = width / 2
        val halfHeight = outHeight / 2
        var index = 1
        while (halfWidth / index > reqWidth && halfHeight / index > reqHeight) {
            index = index shl 1
        }
        if (index < 1) {
            return 1
        }
        return index
    }

    @JvmStatic
    fun sizePairText(readSize: Long, size: Long): String {
        return sizeTextShort(readSize) + " / " + sizeTextShort(size)
    }

    @JvmStatic
    fun truncateUtf8(room: Int, text: String?): String? {
        var text = text
        if (TextUtils.isEmpty(text)) {
            return text
        }
        var bytes: ByteArray? = text!!.toByteArray()
        if (bytes == null || bytes.size <= room) {
            return text.trim { it <= ' ' }
        }
        while (bytes!!.size > room) {
            if (text!!.length < 2) {
                return text.trim { it <= ' ' }
            }
            text = text.let { s -> s.substring(0, s.length - 1) }
            bytes = text.toByteArray()
            if (bytes.size <= room) {
                return text.trim { it <= ' ' }
            }
        }
        return text.trim { it <= ' ' }
    }

    @JvmStatic
    fun zoneIconList(iconSet: Int, articleA: Boolean): IntArray {
        var order: String? = null
        if (iconSet == 0) {
            val linkPopupValues = DialogSetPopup.popupValues
            order = ZoneSettings.linkOrder.value
        } else {
            val imagePopupValues = DialogSetPopup.popupValues
            order = ZoneSettings.imgOrder.value
        }
        val icons = IntArray(12)
        var index = 0
        if (articleA) {
            while (index < 12) {
                val next = index + 1
                icons[index] = next
                index = next
            }
        } else {
            val parts: Array<String?>? =
                if (!TextUtils.isEmpty(order)) order!!.split("/".toRegex())
                    .dropLastWhile { it.isEmpty() }.toTypedArray() else null
            if (parts == null || parts.size != 12) {
                if (iconSet == 0) {
                    ZoneSettings.editLocal { set(ZoneSettings.linkOrder, "") }
                } else {
                    ZoneSettings.editLocal { set(ZoneSettings.imgOrder, "") }
                }
                while (index < 12) {
                    val nextIcon = index + 1
                    icons[index] = nextIcon
                    index = nextIcon
                }
            } else {
                while (index < 12) {
                    icons[index] = parseIntSafe(parts[index])
                    index++
                }
            }
        }
        return icons
    }

    @JvmStatic
    fun viewSnapshotScaled(view: View?, backColor: Int, scale: kotlin.Float, freeMem: Long, config: Bitmap.Config?): Bitmap? {
        var config = config
        var maxHeight: Int = 0
        if (view == null) {
            return null
        }
        try {
            val width = view.getWidth()
            var height = view.getHeight()
            if (width != 0 && height != 0) {
                val maxHeight = if (freeMem > 0) (freeMem / ((width.toLong()) shl 5)).toInt() else 0
                if (freeMem > 0 && height > maxHeight) {
                    height = maxHeight
                }
                val scaledWidth = Math.round(width * scale)
                val scaledHeight = Math.round(height * scale)
                if (scaledWidth != 0 && scaledHeight != 0) {
                    view.invalidate()
                    if (config == null) {
                        config = Bitmap.Config.ARGB_8888
                    }
                    val bitmapCreateBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight, config)
                    val canvas = Canvas(bitmapCreateBitmap)
                    if (backColor != 0) {
                        canvas.drawColor(backColor)
                    }
                    canvas.scale(scale, scale)
                    view.draw(canvas)
                    return bitmapCreateBitmap
                }
            }
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
        return null
    }

    @JvmStatic
    fun needsDesktopMode(url: String?, host: String?): Boolean {
        if (TextUtils.isEmpty(host)) {
            return false
        }
        return needsTextZoom(url) || url!!.startsWith("https://www.amazon.") || host!!.endsWith("fandom.com") || host.endsWith(
            "www.elektra.com.mx"
        )
    }

    @JvmStatic
    fun isReadableFile(context: Context?, path: String?): Boolean {
        if (TextUtils.isEmpty(path)) {
            return false
        }
        if (MainUri.isContentUri(path)) {
            return MainUri.isFile(context, path)
        }
        val file = File(path!!)
        return file.isFile() && file.canRead()
    }

    @JvmStatic
    fun applyDesktopMode(webView: WebView?, url: String?, resumeHost: String?, darkWeb: Boolean) {
        if (webView != null) {
            if (lacksAlgorithmicDark()) {
                if ("file:///android_asset/shortcut.html" == url) {
                    return
                }
            } else if (TextUtils.isEmpty(resumeHost) || !needsDesktopMode(url, resumeHost)) {
                return
            }
            val mediaName = mediaNameFor(url, resumeHost, darkWeb, true)
            if (TextUtils.isEmpty(mediaName)) {
                return
            }
            evalJs(webView, mediaName, false)
        }
    }

    @JvmStatic
    fun refreshZoneIcons(context: Context?) {
        if (context == null || !ZoneSettings.backPlay.value) {
            return
        }
        MainApp.Companion.runInBackground(context, RefreshZoneNotiTask(context))
    }

    @JvmStatic
    fun canUseBiometric(context: Context?, notify: Boolean): Boolean {
        try {
            val result = BiometricManager.from(context!!).canAuthenticate()
            if (result != -2) {
                if (result != -1) {
                    if (result == 0) {
                        return true
                    }
                    if (result != 1) {
                        if (result != 11) {
                            if (result != 12) {
                                if (result == 15) {
                                    if (notify) {
                                        showToast(context, R.string.finger_security)
                                        return false
                                    }
                                } else if (notify) {
                                    showToast(context, R.string.pay_fail)
                                }
                            } else if (notify) {
                                showToast(context, R.string.finger_no_hardware)
                                return false
                            }
                        } else if (notify) {
                            showToast(context, R.string.finger_register)
                            return false
                        }
                    } else if (notify) {
                        showToast(context, R.string.finger_unavailable)
                        return false
                    }
                } else if (notify) {
                    showToast(context, R.string.finger_unknown)
                    return false
                }
            } else if (notify) {
                showToast(context, R.string.finger_unsupported)
                return false
            }
        } catch (unused: Exception) {
        }
        return false
    }

    @JvmStatic
    fun bitmapToStream(bitmap: Bitmap?): ByteArrayInputStream? {
        if (!isBitmapOk(bitmap)) {
            return null
        }
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap!!.compress(CompressFormat.PNG, 0, byteArrayOutputStream)
        return ByteArrayInputStream(byteArrayOutputStream.toByteArray())
    }

    @JvmStatic
    fun languageTag(locale: Locale?): String? {
        if (locale == null) {
            return null
        }
        val language = locale.getLanguage()
        if (TextUtils.isEmpty(language)) {
            return null
        }
        val country = locale.getCountry()
        if (TextUtils.isEmpty(country)) {
            return null
        }
        return "$language-$country"
    }

    @JvmStatic
    fun countLabel(done: Int, size: Int): String {
        return done.toString() + " / " + size
    }

    @JvmStatic
    fun viewScreenX(view: View?): Int {
        if (view == null) {
            return 0
        }
        var location: IntArray? = MainApp.Companion.locationBuffer
        if (location == null) {
            location = IntArray(2)
            MainApp.Companion.locationBuffer = location
        }
        view.getLocationOnScreen(location)
        return location[0]
    }

    @JvmStatic
    fun isDarkResources(resources: Resources?, night: Boolean): Boolean {
        if (BrowserSettings.themeSys.value) {
            if (resources == null || (resources.getConfiguration().uiMode and 48) != 32) {
                return false
            }
        } else if (night) {
            if (BrowserSettings.themeUi.value != 1) {
                return false
            }
        } else if (BrowserSettings.themeWeb.value != 1) {
            return false
        }
        return true
    }

    @JvmStatic
    fun setDecorFits(window: Window?, showStatusBar: Boolean) {
        val index = Build.VERSION.SDK_INT
        if (index >= 35 || index < 30 || window == null) {
            return
        }
        window.setDecorFitsSystemWindows(showStatusBar)
    }

    @JvmStatic
    fun flushCookies(context: Context?, sync: Boolean) {
        val mainAppO: MainApp? = MainApp.Companion.from(context)
        if (mainAppO == null || mainAppO.cookieFlushPending) {
            return
        }
        mainAppO.cookieFlushPending = true
        if (sync) {
            mainAppO.execute(mainAppO.FlushTask())
            return
        }
        try {
            val cookieManagerL: CookieManager? =
                MainApp.Companion.cookieManager(mainAppO.getApplicationContext())
            if (cookieManagerL != null) {
                cookieManagerL.flush()
            }
        } catch (unused: Exception) {
        } catch (unused: NoSuchMethodError) {
        }
        mainAppO.cookieFlushPending = false
    }

    @JvmStatic
    fun imageCountChanged(imgCntItem: ImgCntItem?, lastType: Int, newType: Int): Boolean {
        if (imgCntItem == null) {
            return true
        }
        if (imgCntItem.jpgCount > 0) {
            if (((lastType and 2) == 2) != ((newType and 2) == 2)) {
                return true
            }
        }
        if (imgCntItem.pngCount > 0) {
            if (((lastType and 4) == 4) != ((newType and 4) == 4)) {
                return true
            }
        }
        if (imgCntItem.gifCount > 0) {
            if (((lastType and 8) == 8) != ((newType and 8) == 8)) {
                return true
            }
        }
        if (imgCntItem.webpCount > 0) {
            if (((lastType and 16) == 16) != ((newType and 16) == 16)) {
                return true
            }
        }
        if (imgCntItem.otherCount > 0) {
            if (((lastType and 32) == 32) != ((newType and 32) == 32)) {
                return true
            }
        }
        if (imgCntItem.noExtCount > 0) {
            if (((lastType and 64) == 64) != ((newType and 64) == 64)) {
                return true
            }
        }
        return false
    }

    /**
     * Stored icon/thumbnail for a list item: `i` is the list type, `id` the row id, `str` the path.
     */
    @JvmStatic
    fun loadAlbumThumb(listType: Int, id: Long, context: Context?, black: String?): Bitmap? {
        if (context == null) {
            return null
        }
        val secret = com.mycompany.app.pref.store.Workspaces.dataKey()
        when (listType) {
            14 -> return if (id > 0) iconById(
                DbBookAlbum.get(context).getWritableDatabase(),
                "DbBookAlbum_table",
                id
            ) else null

            15 -> return if (id > 0) iconById(
                DbBookPdf.get(context).getWritableDatabase(),
                "DbBookPdf_table",
                id
            ) else null

            16 -> return if (id > 0) iconById(
                DbBookCmp.get(context).getWritableDatabase(),
                "DbBookCmp_table",
                id
            ) else null

            17 -> {
                if (id > 0) {
                    return iconById(
                        DbBookWeb.get(context).getWritableDatabase(),
                        "DbBookWeb_table",
                        id
                    )
                }
                if (TextUtils.isEmpty(black)) {
                    return null
                }
                return MainUtil.iconWhere(
                    DbBookWeb.get(context).getWritableDatabase(),
                    "DbBookWeb_table",
                    "_secret=? AND _path=?",
                    arrayOf<String?>(secret, black!!),
                    0
                )
            }

            18, 39 -> return DbBookIcon.iconOf(context, shortHostOf(black))
            31 -> return if (id > 0) iconById(
                DbBookPage.get(context).getWritableDatabase(),
                "DbBookPage_table",
                id
            ) else null

            32 -> {
                if (TextUtils.isEmpty(black)) {
                    return null
                }
                return MainUtil.iconWhere(
                    get(context).getWritableDatabase(),
                    "DbBookQuick_table",
                    "_secret=? AND _path=?",
                    arrayOf<String?>(secret, black!!),
                    1
                )
            }

            33 -> return DbBookPass.iconOf(context, black)
            34 -> {
                if (TextUtils.isEmpty(black)) {
                    return null
                }
                return MainUtil.iconWhere(
                    DbBookSearch.get(context).getWritableDatabase(),
                    "DbBookSearch_table",
                    "_text=?",
                    arrayOf<String?>(black!!),
                    1
                )
            }

            40 -> return thumbById(context, id)
            0 -> {
                if (TextUtils.isEmpty(black)) {
                    return null
                }
                return MainUtil.iconWhere(
                    DbBookPage.get(context).getWritableDatabase(),
                    "DbBookPage_table",
                    "_secret=? AND _path=?",
                    arrayOf<String?>(secret, black!!),
                    0
                )
            }

            else -> return null
        }
    }

    private fun iconById(db: SQLiteDatabase?, table: String?, id: Long): Bitmap? {
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.queryById(db, table, arrayOf<String?>("_icon"), id)
            return iconFromCursor(cursor, 0)
        } catch (unused: Exception) {
            return null
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }

    private fun iconWhere(
        db: SQLiteDatabase?,
        table: String?,
        selection: String?,
        args: Array<String?>?,
        minLength: Int
    ): Bitmap? {
        var cursor: Cursor? = null
        try {
            cursor = DbUtil.query(db, table, arrayOf<String?>("_icon"), selection, args, null)
            return iconFromCursor(cursor, minLength)
        } catch (unused: Exception) {
            return null
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }

    /** Decodes the `_icon` blob of the first row if it is longer than `minLength` bytes.  */
    private fun iconFromCursor(cursor: Cursor?, minLength: Int): Bitmap? {
        if (cursor == null || !cursor.moveToFirst()) {
            return null
        }
        val blob = cursor.getBlob(cursor.getColumnIndex("_icon"))
        if (blob == null || blob.size <= minLength) {
            return null
        }
        return BitmapUtil.decodeBytes(blob, blob.size)
    }

    @JvmStatic
    fun fileSizeOf(context: Context?, path: String?): Long {
        if (TextUtils.isEmpty(path)) {
            return 0L
        }
        return if (MainUri.isContentUri(path)) MainUri.fileSize(context, path) else File(path!!).length()
    }

    @JvmStatic
    fun appLocaleTag(): String {
        val tag = languageTag(MainApp.Companion.currentLocale())
        return (if (TextUtils.isEmpty(tag)) "en-US" else tag)!!
    }

    @JvmStatic
    fun pairText(doneText: String?, totalText: String?): String {
        return "$doneText / $totalText"
    }

    @JvmStatic
    fun viewScreenY(view: View?): Int {
        if (view == null) {
            return 0
        }
        var location: IntArray? = MainApp.Companion.locationBuffer
        if (location == null) {
            location = IntArray(2)
            MainApp.Companion.locationBuffer = location
        }
        view.getLocationOnScreen(location)
        return location[1]
    }

    @JvmStatic
    fun isDarkTheme(night: Boolean, configuration: Configuration?): Boolean {
        if (BrowserSettings.themeSys.value) {
            if ((configuration!!.uiMode and 48) != 32) {
                return false
            }
        } else if (night) {
            if (BrowserSettings.themeUi.value != 1) {
                return false
            }
        } else if (BrowserSettings.themeWeb.value != 1) {
            return false
        }
        return true
    }

    @JvmStatic
    fun isYouTubeHost(pageHost: String?): Boolean {
        val length = if (pageHost == null) 0 else pageHost.length
        if (pageHost == null || length < 11 || !pageHost.endsWith("youtube.com")) {
            return false
        }
        if (length == 11) {
            return true
        }
        if (length >= 13) {
            if (pageHost.startsWith("m.", length - 13)) {
                return true
            }
            if (length >= 15 && pageHost.startsWith("www.", length - 15)) {
                return true
            }
        }
        return pageHost.startsWith("/", length - 12)
    }

    @JvmStatic
    fun selectAllOnFocus(editText: EditText?, focusAll: Boolean) {
        if (editText == null) {
            return
        }
        editText.setSelectAllOnFocus(focusAll)
        if (focusAll) {
            editText.setOnFocusChangeListener(null)
        } else {
            editText.setOnFocusChangeListener(EditTextFocusChange(editText))
        }
    }

    @JvmStatic
    fun unescapeJavaText(escaped: String?): String? {
        if (TextUtils.isEmpty(escaped)) {
            return null
        }
        try {
            try {
                return StringEscapeUtils.unescapeJava(escaped)
            } catch (unused: OutOfMemoryError) {
                return StringEscapeUtils.unescapeJava(escaped)
            }
        } catch (unused2: Exception) {
            return null
        } catch (unused2: LinkageError) {
            return null
        } catch (unused2: OutOfMemoryError) {
            return null
        }
    }

    @JvmStatic
    fun isTapInside(point: Point?, motionEvent: MotionEvent?, withStatus: Boolean): Boolean {
        val top: Int
        var bottom = 0
        if (motionEvent == null) {
            return false
        }
        if (motionEvent.getActionMasked() != 0) {
            return MainApp.Companion.edgeTouchActive
        }
        MainApp.Companion.edgeTouchActive = false
        val y = motionEvent.getY()
        val dp20Px: Int = MainApp.Companion.dp20
        if (y < dp20Px) {
            val edgeTouchActive = !withStatus
            MainApp.Companion.edgeTouchActive = edgeTouchActive
            return edgeTouchActive
        }
        if (point != null) {
            bottom = point.x
            top = point.y
        } else {
            top = 0
        }
        if (bottom > top) {
            val x = motionEvent.getX()
            val edge: Int = MainApp.Companion.dp20
            if (x < edge || x > bottom - edge) {
                MainApp.Companion.edgeTouchActive = true
            }
        } else if (y > top - dp20Px) {
            MainApp.Companion.edgeTouchActive = !withStatus
        }
        return MainApp.Companion.edgeTouchActive
    }

    @JvmStatic
    fun base64NoWrap(bArr: ByteArray?): String? {
        if (bArr != null && bArr.size != 0) {
            try {
                val encoded = Base64.encodeToString(bArr, 0)
                if (TextUtils.isEmpty(encoded)) {
                    return null
                }
                val oneLine = encoded!!.replace("\n", "")
                if (TextUtils.isEmpty(oneLine)) {
                    return null
                }
                return "data:image/png;base64," + oneLine
            } catch (unused: Exception) {
            } catch (unused: OutOfMemoryError) {
            }
        }
        return null
    }

    @JvmStatic
    fun sizeLabel(size: Long): String {
        if (size / 1024L != 0L) {
            if (size / 1048576L == 0L) {
                return String.format(Locale.US, "%.2f", size / 1024.0f) + " KB"
            }
            return if (size / 1073741824 == 0L) (String.format(
                Locale.US,
                "%.2f",
                size / 1048576.0f
            ) + " MB") else (String.format(
                Locale.US, "%.2f", size / 1.0737418E9f
            ) + " GB")
        }
        return size.toString() + ".00 B"
    }

    @JvmStatic
    fun capitalize(word: String?, locale: Locale?): String? {
        val length = if (word == null) 0 else word.length
        if (word == null || length == 0) {
            return null
        }
        if (length == 1) {
            return word.uppercase(locale!!)
        }
        return word.substring(0, 1).uppercase(locale!!) + word.substring(1)
    }

    @JvmStatic
    fun readerFontPath(context: Context?): String? {
        if (!TextUtils.isEmpty(MainApp.Companion.readerFontPathCache)) {
            return MainApp.Companion.readerFontPathCache
        }
        val fontDir = filesSubdir(context, ".font")
        if (TextUtils.isEmpty(fontDir)) {
            return null
        }
        val fontPath = "$fontDir/read.ttf"
        MainApp.Companion.readerFontPathCache = fontPath
        return fontPath
    }

    @JvmStatic
    fun viewSnapshot(view: View?, scale: kotlin.Float): Bitmap? {
        if (view == null) {
            return null
        }
        try {
            val width = view.getWidth()
            val height = view.getHeight()
            if (width != 0 && height != 0) {
                val scaledWidth = Math.round(width * scale)
                val scaledHeight = Math.round(height * scale)
                if (scaledWidth != 0 && scaledHeight != 0) {
                    val bitmapCreateBitmap =
                        Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmapCreateBitmap)
                    val rectF = RectF()
                    rectF.set(0.0f, 0.0f, scaledWidth.toFloat(), scaledHeight.toFloat())
                    val radius: kotlin.Float = MainApp.Companion.dp8 * scale
                    val path = Path()
                    path.addRoundRect(rectF, radius, radius, android.graphics.Path.Direction.CW)
                    path.close()
                    canvas.clipPath(path)
                    canvas.scale(scale, scale)
                    view.draw(canvas)
                    return bitmapCreateBitmap
                }
            }
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
        return null
    }

    /**
     * True when the system WebView cannot darken pages itself (no ALGORITHMIC_DARKENING), so the app's own CSS
     * darkening is used. The answer is cached in [AppState.webViewHint]: 0 = not checked yet, 1 = lacking, 2 = supported.
     */
    @JvmStatic
    fun lacksAlgorithmicDark(): Boolean {
        var supported: Boolean
        var webViewHint: Byte = AppState.webViewHint
        if (webViewHint.toInt() == 0) {
            try {
                supported = WebViewFeature.isFeatureSupported("ALGORITHMIC_DARKENING")
            } catch (unused: Exception) {
                supported = false
            }
            if (supported) {
                webViewHint = 2
                AppState.webViewHint = 2.toByte()
            } else {
                AppState.webViewHint = 1.toByte()
                webViewHint = 1
            }
        }
        return webViewHint.toInt() == 1
    }

    @JvmStatic
    fun isYouTubeShorts(pageUrl: String?): Boolean {
        var length: Int = 0
        var youTubeAt: Int = 0
        var shortsAt: Int = 0
        return if (pageUrl == null || (pageUrl.length.also {
                length = it
            }) == 0 || (pageUrl.indexOf("youtube.com/")
                .also { youTubeAt = it }) == -1 || (pageUrl.indexOf("shorts/", youTubeAt + 12)
                .also { shortsAt = it }) == -1 || shortsAt + 7 >= length
        ) false else true
    }

    @JvmStatic
    fun hapticTap(view: View?) {
        if (view == null) {
            return
        }
        view.performHapticFeedback(1, if (Build.VERSION.SDK_INT >= 33) 1 else 3)
    }

    @JvmStatic
    fun notifyBookUpdate(context: Context?, always: Boolean) {
        if ((always || !SyncSettings.secretMode.value) && context != null) {
            try {
                val intent = Intent(context, WidgetBookProviderLight::class.java as Class<*>)
                intent.setAction("ch.opumlabs.opumbrowser.ACTION_BOOKGET_UPDATE")
                context.sendBroadcast(intent)
                val darkIntent = Intent(context, WidgetBookProviderDark::class.java as Class<*>)
                darkIntent.setAction("ch.opumlabs.opumbrowser.ACTION_BOOKGET_UPDATE")
                context.sendBroadcast(darkIntent)
            } catch (unused: Exception) {
            }
        }
    }

    fun getAdsGoogleJs(i: Int): String? = ScriptParts.adsGoogleJs.getOrNull(i)

    fun getAdsHamJs(i: Int): String? = ScriptParts.adsHamJs.getOrNull(i)

    fun getAmpJs(i: Int): String? = ScriptParts.ampJs.getOrNull(i)

    private fun getAreaJs(i: Int): String? = ScriptParts.areaJs.getOrNull(i)

    private fun getAreaMultiJs(i: Int): String? = ScriptParts.areaMultiJs.getOrNull(i)

    private fun getAreaUpJs(i: Int): String? = ScriptParts.areaUpJs.getOrNull(i)

    val backupTag: String = "layer_log"

    fun getBlobJs(i: Int): String? = ScriptParts.blobJs.getOrNull(i)

    fun getFaceJs(i: Int): String? = ScriptParts.faceJs.getOrNull(i)

    val faceUrl: String = "https://www.facebook.com/watch/?v="

    fun getFansJs(i: Int): String? = ScriptParts.fansJs.getOrNull(i)

    private fun getGoogleLens(i: Int): String = when (i) {
        1 -> "com.google.android.apps.search.lens.LensShareEntryPointActivity"
        else -> "com.google.android.googlequicksearchbox"
    }

    val instaAid: String = "X-IG-App-ID"

    fun getInstaData(i: Int): String? = ScriptParts.instaData.getOrNull(i)

    fun getInstaJs(i: Int): String? = ScriptParts.instaJs.getOrNull(i)

    val instaMid1: String = "media?id="

    val instaMid2: String = "\"media_id\":\""

    fun getOverlayJs(i: Int): String? = ScriptParts.overlayJs.getOrNull(i)

    fun getPassJs1(i: Int): String? = ScriptParts.passJs1.getOrNull(i)

    fun getPassJs2(i: Int): String? = ScriptParts.passJs2.getOrNull(i)

    fun getPassJs3(i: Int): String? = ScriptParts.passJs3.getOrNull(i)

    fun getRedditJs(i: Int): String? = ScriptParts.redditJs.getOrNull(i)

    private fun getSearchImage(i: Int): String = when (i) {
        1 -> "https://www.bing.com/images/search?view=detailv2&iss=sbi&form=SBIHMP&sbisrc=UrlPaste&idpbck=1&selectedindex=0&vt=2&sim=11&q=imgurl:%s&id=%s&mediaurl=%s"
        2 -> "https://yandex.ru/images/search?rpt=imageview&url="
        else -> "https://lens.google.com/uploadbyurl?url="
    }

    private fun getSnsJs1(i: Int): String = when (i) {
        2 -> "(function(){var ele=document.querySelector(\"input[name='tweet']\");if(ele){ele.value='"
        else -> "(function(){var ele=document.querySelector(\"input[type='text']\");if(ele){ele.value='"
    }

    private fun getSnsJs2(i: Int): String = when (i) {
        2 -> "var __cfRLUnblockHandlers=1;var ele=document.querySelector(\"input[type='submit']\");if(ele){ele.onclick='';ele.click();}"
        else -> "var __cfRLUnblockHandlers=1;var ele=document.querySelector(\"button[type='submit']\");if(ele){ele.onclick='';ele.click();}"
    }

    private fun getSnsUrl(i: Int): String = when (i) {
        1 -> "https://indown.io"
        2 -> "https://twittervideodownloader.com"
        else -> "https://snapsave.app"
    }

    fun getTiktokJs(i: Int): String? = ScriptParts.tiktokJs.getOrNull(i)

    val transContain: String = "iframe[id=\":0.container\"]"

    val transCsp: String = "meta[http-equiv='x-pjax-csp-version']"

    val transElement: String = "gtb.contentDocument.documentElement"

    val transFinishLang: String = "finishTargetLang"

    val transFinishSection: String = "finishSection"

    val transLang: String = "iframe[id='sbtr_lang']"

    val transPromptLang: String = "promptTargetLang"

    val transPromptSection: String = "promptSection"

    val transSpId: String = "span[id*="

    fun getTransTag(i: Int): String? = ScriptParts.transTag.getOrNull(i)

    val transTrId: String = "tr[id*="

    fun getTtsMarkJs(i: Int): String? = ScriptParts.ttsMarkJs.getOrNull(i)

    fun getTtsPartJs1(i: Int): String? = ScriptParts.ttsPartJs1.getOrNull(i)

    fun getTtsPartJs2(i: Int): String? = ScriptParts.ttsPartJs2.getOrNull(i)

    fun getTwitJs(i: Int): String? = ScriptParts.twitJs.getOrNull(i)

    fun getTwitReq(i: Int): String? = ScriptParts.twitReq.getOrNull(i)

    val twitUrl: String = "https://x.com/i/status/"

    fun getYouJs1(i: Int): String? = ScriptParts.youJs1.getOrNull(i)

    fun getYouJs2(i: Int): String? = ScriptParts.youJs2.getOrNull(i)

    @JvmStatic
    fun updateCutoutInsets(context: Context?, window: Window?, view: View?) {
        var view = view
        if (window != null) {
            if (view == null) {
                try {
                    view = window.getDecorView()
                } catch (unused: Exception) {
                    return
                }
            }
            val rect = Rect()
            view.getWindowVisibleDisplayFrame(rect)
            var statusHeight = MainSettings.statusHeight.value
            var statusOk = MainSettings.statusConfirmed.value
            var naviHeight = MainSettings.naviHeight.value
            var naviOk = MainSettings.naviConfirmed.value
            if (BrowserSettings.showStatus.value && rect.top > 0) {
                val adHeight = dpToPx(context, 70.0f).toInt()
                val top = rect.top
                if (top < adHeight) {
                    statusOk = statusHeight == top
                    statusHeight = top
                }
            }
            if (BrowserSettings.showNavi.value && rect.bottom > 0) {
                val pointP = screenPoint(context)
                val index = (if (pointP == null) 0 else pointP.y) - rect.bottom
                if (index > 0 && index < (dpToPx(context, 140.0f).toInt())) {
                    naviOk = naviHeight == index
                    naviHeight = index
                }
            }
            if (MainSettings.statusHeight.value == statusHeight && MainSettings.statusConfirmed.value == statusOk && MainSettings.naviHeight.value == naviHeight && MainSettings.naviConfirmed.value == naviOk) {
                return
            }
            MainSettings.editLocal { set(MainSettings.statusConfirmed, statusOk) }
            MainSettings.editLocal { set(MainSettings.statusHeight, statusHeight) }
            MainSettings.editLocal { set(MainSettings.naviConfirmed, naviOk) }
            MainSettings.editLocal { set(MainSettings.naviHeight, naviHeight) }
            val prefMainR = get(context, false)
            prefMainR!!.putInt(MainSettings.statusHeight.value, "mStatusHeight")
            prefMainR.putBool("mStatusConfirm", MainSettings.statusConfirmed.value)
            prefMainR.putInt(MainSettings.naviHeight.value, "mNaviHeight2")
            prefMainR.putBool("mNaviConfirm", MainSettings.naviConfirmed.value)
            prefMainR.saveAsync()
        }
    }

    @JvmStatic
    fun bottomBarHeightPref(): Int {
        if (TextUtils.isEmpty(MainSettings.botItems.value)) {
            return 0
        }
        return PdfSettings.botHeight.value
    }

    @JvmStatic
    fun sizeTextShort(readSize: Long): String {
        if (readSize / 1024L != 0L) {
            if (readSize / 1048576L == 0L) {
                return String.format(Locale.US, "%.1f", readSize / 1024.0f) + " KB"
            }
            return if (readSize / 1073741824 == 0L) (String.format(
                Locale.US,
                "%.1f",
                readSize / 1048576.0f
            ) + " MB") else (String.format(
                Locale.US, "%.1f", readSize / 1.0737418E9f
            ) + " GB")
        }
        return readSize.toString() + ".0 B"
    }

    @JvmStatic
    fun lockIntent(context: Context?, type: Int): Intent {
        if (type == 2) {
            return Intent(context, PinActivity::class.java as Class<*>)
        }
        if (type == 3) {
            return Intent(context, PassActivity::class.java as Class<*>)
        }
        return if (type == 4) Intent(context, FingerActivity::class.java as Class<*>) else Intent(
            context,
            PatternActivity::class.java as Class<*>
        )
    }

    @JvmStatic
    fun scaleBitmap(width: Int, height: Int, bitmap: Bitmap?): Bitmap? {
        if (!isBitmapOk(bitmap)) {
            return null
        }
        try {
            return Bitmap.createScaledBitmap(bitmap!!, width, height, true)
        } catch (unused: Exception) {
            return null
        } catch (unused: OutOfMemoryError) {
            return null
        }
    }

    @JvmStatic
    fun viewportJs(
        sb: StringBuilder?,
        webNestView: WebNestView?,
        pageUrl: String?,
        pageHost: String?
    ): StringBuilder {
        var sb = sb
        if (sb == null) {
            sb = StringBuilder()
        }
        if ("file:///android_asset/shortcut.html" != pageUrl) {
            if (isImagePath(pageUrl)) {
                sb.append("var evp=document.querySelector(\"meta[name=viewport]\");if(evp){evp.setAttribute('content','width=device-width,minimum-scale=0.1');}")
                return sb
            }
            if (webNestView!!.isDesktopAgent()) {
                sb.append("var evp=document.querySelector(\"meta[name=viewport]\");if(evp){evp.setAttribute('content','user-scalable=yes');}")
                return sb
            }
            if (ZtwoSettings.webScale.value != 0) {
                sb.append("var evp=document.querySelector(\"meta[name=viewport]\");if(evp){var vwcnt=evp.content;if(!vwcnt||(vwcnt.includes('user-scalable=no')||vwcnt.includes('user-scalable=0')||vwcnt.includes('maximum-scale=1'))){")
                if (!TextUtils.isEmpty(pageHost) && pageHost!!.endsWith("namu.wiki")) {
                    sb.append("evp.setAttribute('content','width=device-width,initial-scale=1.1,minimum-scale=1.0,maximum-scale=5.0,user-scalable=yes');")
                }
                sb.append("evp.setAttribute('content','width=device-width,initial-scale=1.0,minimum-scale=1.0,maximum-scale=5.0,user-scalable=yes');}}")
                return sb
            }
        }
        return sb
    }

    @JvmStatic
    fun isDarkPage(onStartPage: Boolean): Boolean {
        return AppState.darkUi || isDarkWebPage(onStartPage)
    }

    @JvmStatic
    fun isYouTubeUrl(pageUrl: String?): Boolean {
        return isYouTubeHost(hostOf(pageUrl, true))
    }

    @JvmStatic
    fun resetTextSizePref() {
        // the Colour theme with coloured bars: the bars take the theme colour; PrefWeb's
        // getters turn it into the night tone while the app is dark
        if (BrowserSettings.colorTheme.value && BrowserSettings.colorBars.value) {
            val color = BrowserSettings.themeColor.value or -0x1000000
            PrefWeb.headColor = color
            PrefWeb.iconColor = lighterColor(color)
            return
        }
        val index = BrowserSettings.headIndex.value
        if (index < 0 || index >= MainConst.headColors.size) {
            BrowserSettings.editLocal { set(BrowserSettings.headIndex, 16) }
        }
        val headColor = MainConst.headColors[BrowserSettings.headIndex.value]
        PrefWeb.headColor = headColor
        PrefWeb.iconColor = lighterColor(headColor)
    }

    @JvmStatic
    fun hasVideoButton(): Boolean {
        if (ZoneSettings.youPos.value == 0) {
            return false
        }
        return ZoneSettings.youRate.value
    }

    @JvmStatic
    fun trimMemoryCaches() {
        try {
            com.mycompany.app.glide.ImageCache.trim()
        } catch (unused: Exception) {
        }
    }

    @JvmStatic
    fun bottomInsetTotal(): Int {
        val barHeight: Int
        var bottom = 0
        if (!PdfSettings.topToBottom.value) {
            barHeight = bottomBarHeightPref()
            if (hasTopTabBar()) {
                bottom = PdfSettings.tabHeight.value
            }
        } else {
            if (!MainActivity.Companion.splitLand) {
                return (if (hasTopTabBar()) PdfSettings.tabHeight.value else 0) + bottomBarHeightPref() + topBarSize()
            }
            barHeight = max(topBarSize(), bottomBarHeightPref())
            if (hasTopTabBar()) {
                bottom = PdfSettings.tabHeight.value
            }
        }
        return bottom + barHeight
    }

    @JvmStatic
    fun uriFileName(context: Context?, path: String?): String? {
        if (TextUtils.isEmpty(path)) {
            return null
        }
        if (context == null || !MainUri.isContentUri(path)) {
            return stripExtension(parentPathOf(context, path))
        }
        val displayPath = MainUri.treeDisplayPath(context, path)
        if (TextUtils.isEmpty(displayPath)) {
            return null
        }
        val dotIndex = displayPath!!.lastIndexOf(46.toChar())
        return if (dotIndex <= 0 || dotIndex > displayPath.length) displayPath else displayPath.substring(
            0,
            dotIndex
        )
    }

    @JvmStatic
    fun moreIconFor(color: Int, dark: Boolean): Int {
        if (barInk(color) == 0) {
            return if (dark) R.drawable.outline_more_vert_black_24 else MainConst.mainMenuIconsBlack[2]
        }
        return if (dark) R.drawable.outline_more_vert_dark_24 else MainConst.mainMenuIconsDark[2]
    }

    @JvmStatic
    fun safeTitleFileName(room: Int, pageTitle: String?, fallback: String?): String? {
        if (!TextUtils.isEmpty(pageTitle)) {
            val safeName = safeFileName(pageTitle)
            val truncated = MainUtil.truncateUtf8(
                room,
                (if (TextUtils.isEmpty(safeName)) null else safeName!!.replace(
                    "[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]&&[^()-]+".toRegex(),
                    ""
                ).replace("\\p{InEmoticons}&&[^()-]+".toRegex(), "")
                    .replace("\\p{So}&&[^()-]+".toRegex(), "")
                    .replace("\\p{InMiscellaneousSymbolsAndPictographs}&&[^()-]+".toRegex(), ""))!!
            )
            if (!TextUtils.isEmpty(truncated)) {
                return truncated
            }
        }
        return fallback
    }

    @JvmStatic
    fun backupBitmap(context: Context?, backdropLandscape: Boolean): Bitmap? {
        val backDir = backupDirPath(context, SyncSettings.secretMode.value, backdropLandscape)
        if (TextUtils.isEmpty(backDir)) {
            return null
        }
        return com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(0, backDir))
    }

    @JvmStatic
    fun isDcinsideImage(url: String?): Boolean {
        val host = hostOf(url, true)
        if (TextUtils.isEmpty(host)) {
            return false
        }
        if (!host!!.endsWith("dcinside.com") && !host.endsWith("dcinside.co.kr")) {
            return false
        }
        val length = host.length
        val start = length + 1
        if (url!!.startsWith("viewimage", start)) {
            return url.indexOf(".php", length + 10) != -1
        }
        return url.startsWith("dccon", start) && url.indexOf(".php", length + 6) != -1
    }

    @JvmStatic
    fun isYouTubeWatch(url: String?): Boolean {
        val hostIndex = if (TextUtils.isEmpty(url)) 0 else url!!.indexOf("youtube.com/")
        if (TextUtils.isEmpty(url) || hostIndex == -1) {
            return false
        }
        val watchIndex = url!!.indexOf("watch?", hostIndex + 12)
        if (watchIndex != -1) {
            val videoParam = url.indexOf("v=", watchIndex + 6)
            return videoParam != -1 && videoParam + 2 < url.length
        }
        val shortsIndex = url.indexOf("shorts/", watchIndex + 12)
        return shortsIndex != -1 && shortsIndex + 7 < url.length
    }

    @JvmStatic
    fun isDarkBarIcons(barColor: Int, onStartPage: Boolean): Boolean {
        if (barColor == 0 || hasTopTabBar() || isDarkWebPage(onStartPage)) {
            return true
        }
        return onStartPage && BrowserSettings.headMode() != 2
    }

    @JvmStatic
    fun cacheFilePath(context: Context?, name: String?): String? {
        val cacheDir: File? = if (context == null) null else context.getCacheDir()
        if (context == null || cacheDir == null) {
            return null
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir.getPath() + "/" + name
    }

    @JvmStatic
    fun stripExtension(name: String?): String? {
        if (TextUtils.isEmpty(name)) {
            return null
        }
        val dotIndex = name!!.lastIndexOf(46.toChar())
        return if (dotIndex != -1) name.substring(0, dotIndex) else name
    }

    @JvmStatic
    fun iconResFor(id: Int, barColor: Int): Int {
        // a button group (ui/browser/BarGroups) shows its lead button's icon; an empty one (or the editor's "Group" tile) a stack
        if (com.mycompany.app.ui.browser.BarGroups.isGroup(id) || id == com.mycompany.app.ui.browser.BarGroups.NEW) {
            val lead = if (id == com.mycompany.app.ui.browser.BarGroups.NEW) 0 else com.mycompany.app.ui.browser.BarGroups.lead(id)
            if (lead != 0) return iconResFor(lead, barColor)
            return if (barInk(barColor) == 0) R.drawable.outline_layers_black_24 else R.drawable.outline_layers_dark_24
        }
        // a light custom bar colour gets the dark icons of the light bar
        val colour = barInk(barColor)
        var found = false
        if (colour == 0) {
            if (id < 0 || id >= MainConst.mainMenuIconsBlack.size) {
                return MainConst.mainMenuIconsBlack[0]
            }
        } else if (id < 0 || id >= MainConst.mainMenuIconsDark.size) {
            return MainConst.mainMenuIconsDark[0]
        }
        if (id == 6) {
            val hideImage = ZoneSettings.hideImage.value
            if (colour == 0) {
                return if (hideImage) R.drawable.outline_image_black_24 else MainConst.mainMenuIconsBlack[id]
            }
            return if (hideImage) R.drawable.outline_image_dark_24 else MainConst.mainMenuIconsDark[id]
        }
        if (id == 21) {
            val secretMode = SyncSettings.secretMode.value
            if (colour == 0) {
                return if (secretMode) R.drawable.outline_mood_black_24 else MainConst.mainMenuIconsBlack[id]
            }
            return if (secretMode) R.drawable.outline_mood_dark_24 else MainConst.mainMenuIconsDark[id]
        }
        if (id != 22) {
            return if (colour == 0) MainConst.mainMenuIconsBlack[id] else MainConst.mainMenuIconsDark[id]
        }
        if (AppState.darkUi && AppState.darkWeb) {
            found = true
        }
        if (colour == 0) {
            return if (found) R.drawable.outline_sunny_black_24 else MainConst.mainMenuIconsBlack[id]
        }
        return if (found) R.drawable.outline_sunny_dark_24 else MainConst.mainMenuIconsDark[id]
    }

    @JvmStatic
    fun fileTimeStamp(time: Long): String {
        return SimpleDateFormat("yyyyMMdd_hhmmss", MainApp.Companion.currentLocale()).format(Date(time))
    }

    @JvmStatic
    fun backupBitmapCopy(context: Context?, backdropLandscape: Boolean): Bitmap? {
        if (backdropLandscape) {
            val copyPath = backupDirPath2(context, SyncSettings.secretMode.value, true)
            if (!TextUtils.isEmpty(copyPath) && !File(copyPath!!).exists()) {
                copyFile(backupDirPath2(context, SyncSettings.secretMode.value, false), copyPath)
            }
        }
        val landPath = backupDirPath(context, SyncSettings.secretMode.value, backdropLandscape)
        if (TextUtils.isEmpty(landPath)) {
            return null
        }
        val cacheKey = com.mycompany.app.glide.ImageCache.key(0, landPath)
        val bitmapC = BitmapUtil.decodeFile(landPath)
        if (isBitmapOk(bitmapC)) {
            com.mycompany.app.glide.ImageCache.put(cacheKey, bitmapC)
        }
        return bitmapC
    }

    @JvmStatic
    fun isPdfUrl(url: String?): Boolean {
        if (!URLUtil.isNetworkUrl(url)) {
            return false
        }
        if (url!!.endsWith(".pdf")) {
            return true
        }
        if (TextUtils.isEmpty(fileExtOf(url, false))) {
            return false
        }
        return "pdf" == url
    }

    @JvmStatic
    fun lastIndexOfCharIn(text: String?, target: Char, slash: Int, end: Int): Int {
        var slash = slash
        var end = end
        try {
            val length = text!!.length
            if (length != 0 && slash < length) {
                if (slash < 0) {
                    slash = 0
                }
                if (end >= length) {
                    end = length - 1
                }
                val start = slash - 1
                while (end > start) {
                    if (text.get(end) == target) {
                        return end
                    }
                    end--
                }
            }
        } catch (unused: Exception) {
        }
        return -1
    }

    @JvmStatic
    fun applyViewerOrientation(activity: Activity?) {
        if (activity == null) {
            return
        }
        val rotation = ImageSettings.rotation.value
        if (rotation == 1) {
            showPermissionNote(activity, 7)
        } else if (rotation == 2) {
            showPermissionNote(activity, 6)
        } else {
            showPermissionNote(activity, 4)
        }
    }

    @JvmStatic
    fun clearWebData(context: Context?) {
    }

    @JvmStatic
    fun editorTextColor(): Int {
        val filterUse = EditorSettings.scrFilUse.value
        if (filterUse == 1) {
            return PrefEditor.scrFilArgb
        }
        if (filterUse == 2 && AppState.darkUi && AppState.darkWeb) {
            return PrefEditor.scrFilArgb
        }
        return 0
    }

    @JvmStatic
    fun menuLabelAt(id: Int): Int {
        if (id >= 0) {
            val menuNames = MainConst.mainMenuNames
            if (id < menuNames.size) {
                if (id == 6) {
                    return if (ZoneSettings.hideImage.value) R.string.show_image else menuNames[id]
                }
                if (id == 21) {
                    return if (SyncSettings.secretMode.value) R.string.normal_mode else menuNames[id]
                }
                if (id == 22) {
                    return if (AppState.darkUi && AppState.darkWeb) R.string.day_mode else menuNames[id]
                }
                return menuNames[id]
            }
        }
        return MainConst.mainMenuNames[0]
    }

    @JvmStatic
    fun siteIcon(context: Context?, address: String?): Bitmap? {
        if (context == null || TextUtils.isEmpty(address)) {
            return null
        }
        val shortHost = MainUtil.shortHostOf(address!!)
        if (TextUtils.isEmpty(shortHost)) {
            return null
        }
        val bitmapK = cachedIcon(shortHost)
        if (isBitmapOk(bitmapK)) {
            return bitmapK
        }
        val bitmapB = DbBookIcon.iconOf(context, shortHost)
        if (isBitmapOk(bitmapB)) {
            return bitmapB
        }
        val hosts =
            arrayOf<String?>("google.com", "youtube.com", "facebook.com", "instagram.com", "x.com")
        val icons = intArrayOf(
            R.drawable.baseline_search_google,
            R.drawable.baseline_search_youtube,
            R.drawable.ic_facebook,
            R.drawable.ic_instagram,
            R.drawable.ic_twitter
        )
        for (index in 0..4) {
            if (shortHost!!.endsWith(hosts[index]!!)) {
                return BitmapUtil.decodeResource(context.getResources(), icons[index])
            }
        }
        return null
    }

    @JvmStatic
    fun cachedIcon(url: String?): Bitmap? {
        if (TextUtils.isEmpty(url)) {
            return null
        }
        return com.mycompany.app.glide.ImageCache.get(com.mycompany.app.glide.ImageCache.key(2, url))
    }

    @JvmStatic
    fun isHmgPath(title: String?): Boolean {
        val host = hostOf(title, true)
        val length = if (host == null) 0 else host.length
        if (host == null || length == 0) {
            return false
        }
        return title!!.startsWith("/h/", length)
    }

    @JvmStatic
    fun lighterColor(headColor: Int): Int {
        if (headColor == 0) {
            return 0
        }
        if (headColor == -16777216) {
            return -13421773
        }
        return ColorUtils.blendARGB(headColor, -1, 0.2f)
    }

    @JvmStatic
    fun applyIncognitoIme(editText: EditText?) {
        if (editText == null) {
            return
        }
        try {
            val imeOptions = editText.getImeOptions()
            editText.setImeOptions(if (isSecretLocked()) imeOptions or 16777216 else imeOptions and (-16777217))
        } catch (unused: Exception) {
        }
    }

    @JvmStatic
    fun clearWebCache(webView: WebView?) {
        if (webView == null) {
            return
        }
        try {
            webView.clearCache(false)
        } catch (unused: Exception) {
        }
    }

    @JvmStatic
    fun newCacheFilePath(context: Context?): String? {
        val cacheDir: File? = if (context == null) null else context.getCacheDir()
        if (context == null || cacheDir == null) {
            return null
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
        return cacheDir.getPath() + "/" + System.currentTimeMillis()
    }

    @JvmStatic
    fun openBrowserConnection(context: Context?, source: String?): HttpURLConnection? {
        initUserAgent(context)
        val defaultUserAgent: String? = MainApp.Companion.defaultUserAgent
        return openHeadConnection(
            context,
            source,
            null,
            0,
            0,
            false,
            if (!TextUtils.isEmpty(defaultUserAgent)) defaultUserAgent!!.replace(
                "; wv",
                ""
            ) else "Mozilla/5.0 (Linux; Android 14; SM-S24 Build/PQ3A.190801.002) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/131.0.0.0 Mobile Safari/537.36",
            false
        )
    }

    @JvmStatic
    fun menuIconDarkAt(id: Int, color: Int): Int {
        if (color == 0) {
            if (id >= 0) {
                val blackIcons = MainConst.mainMenuIconsBlack
                if (id < blackIcons.size) {
                    return blackIcons[id]
                }
            }
            return MainConst.mainMenuIconsBlack[0]
        }
        if (id >= 0) {
            val darkIcons = MainConst.mainMenuIconsDark
            if (id < darkIcons.size) {
                return darkIcons[id]
            }
        }
        return MainConst.mainMenuIconsDark[0]
    }

    @JvmStatic
    fun schemeKind(path: String?): Int {
        if (TextUtils.isEmpty(path)) {
            return 0
        }
        if (path!!.startsWith("https://")) {
            return 8
        }
        if (path.startsWith("http://")) {
            return 7
        }
        if (path.startsWith("file:///")) {
            return 8
        }
        return if (path.startsWith("file://")) 7 else 0
    }

    @JvmStatic
    fun browserIntent(context: Context?): Intent {
        return Intent(context, WebViewActivity::class.java as Class<*>)
    }

    @JvmStatic
    fun isEmgUrl(page: String?): Boolean {
        return isEmgPath(page, hostOf(page, true))
    }

    @JvmStatic
    fun compareStrings(first: String?, second: String?, descending: Boolean): Int {
        val lowerNameEmpty = TextUtils.isEmpty(first)
        val lowerName2Empty = TextUtils.isEmpty(second)
        if (lowerNameEmpty && lowerName2Empty) {
            return 0
        }
        val sign = if (descending) -1 else 1
        if (lowerNameEmpty) {
            return sign
        }
        if (lowerName2Empty) {
            return -sign
        }
        return compareName(first, second) * sign
    }

    @JvmStatic
    fun wildcardHost(rule: String?): String? {
        var rule = rule
        if (rule == null) {
            return rule
        }
        if (rule.endsWith(".*")) {
            rule = rule.substring(0, rule.length - 1) + "com"
        }
        if (hasChar(42, rule)) {
            rule = rule.replace("*", "")
        }
        return stripHostPrefix(rule)
    }

    @JvmStatic
    fun menuIconAt(id: Int): Int {
        if (id >= 0) {
            val menuNames = MainConst.mainMenuNames
            if (id < menuNames.size) {
                return menuNames[id]
            }
        }
        return MainConst.mainMenuNames[0]
    }

    @JvmStatic
    fun safeFileName(full: String?): String? {
        if (TextUtils.isEmpty(full)) {
            return full
        }
        try {
            return trimSpaceDash(URLDecoder.decode(full!!.replace("[\\\\/:*?\"<>|]".toRegex(), "-"), "UTF-8"))
        } catch (unused: Exception) {
            return full
        }
    }

    @JvmStatic
    fun pageUrlOrNull(path: String?): String? {
        if (TextUtils.isEmpty(path) || "about:blank" == path) {
            return null
        }
        return path
    }

    @JvmStatic
    fun isEmgPath(page: String?, path: String?): Boolean {
        val length = if (path == null) 0 else path.length
        if (path == null || length == 0 || !path.endsWith("e-hentai.org")) {
            return false
        }
        return page!!.startsWith("/s/", length) || page.startsWith("/lofi/s/", length)
    }

    @JvmStatic
    fun loadHtml(webView: WebView?, baseUrl: String?, html: String?) {
        var baseUrl = baseUrl
        if (webView == null || TextUtils.isEmpty(html)) {
            return
        }
        if (!TextUtils.isEmpty(baseUrl) && baseUrl!!.startsWith("https://m.youtube.com/")) {
            baseUrl = "https://www" + baseUrl.substring(9)
        }
        val base = baseUrl
        webView.loadDataWithBaseURL(base, html!!, "text/html; charset=utf-8", "UTF-8", base)
    }

    @JvmStatic
    fun clipRound3(view: View?) {
        if (view == null) {
            return
        }
        view.setOutlineProvider(TopRound16Outline())
        view.setClipToOutline(true)
    }

    @JvmStatic
    fun compareStringsIgnoreCase(first: String?, second: String?, descending: Boolean): Int {
        val pathEmpty = TextUtils.isEmpty(first)
        val path2Empty = TextUtils.isEmpty(second)
        if (pathEmpty && path2Empty) {
            return 0
        }
        val sign = if (descending) -1 else 1
        if (pathEmpty) {
            return sign
        }
        return if (path2Empty) -sign else first!!.compareTo(second!!) * sign
    }

    @JvmStatic
    fun detectCharset(inputStream: InputStream?): String {
        var found: Boolean
        if (inputStream == null) {
            return "EUC-KR"
        }
        val bArr = ByteArray(8192)
        val universalDetector = UniversalDetector(null)
        while (true) {
            try {
                val index = inputStream.read(bArr)
                if (index <= 0 || universalDetector.isDone) {
                    break
                }
                universalDetector.handleData(bArr, 0, index)
            } catch (unused: Exception) {
                found = true
            }
        }
        found = false
        universalDetector.dataEnd()
        try {
            inputStream.close()
        } catch (unused2: Exception) {
        }
        val charset = if (found) null else universalDetector.detectedCharset
        universalDetector.reset()
        return if (charset == null || "WINDOWS-1252" == charset) "EUC-KR" else charset
    }

    @JvmStatic
    fun alphaColor(color: Int, sizeValue: Int): Int {
        var sizeValue = sizeValue
        if (sizeValue < 20) {
            sizeValue = 20
        } else if (sizeValue > 100) {
            sizeValue = 100
        }
        return (Math.round(((100 - sizeValue) * KotlinVersion.MAX_COMPONENT_VALUE) / 100.0f) shl 24) or (color and 16777215)
    }

    @JvmStatic
    fun maxTextureSize(): Int {
        val maxSize = PdfSettings.maxTexSize.value
        if (maxSize > 0) {
            return maxSize
        }
        return 2048
    }

    @JvmStatic
    fun searchUrlFor(spoken: String?): String? {
        var spoken = spoken
        val engineUrl: String?
        if (TextUtils.isEmpty(spoken)) {
            return spoken
        }
        try {
            spoken = URLEncoder.encode(spoken, "UTF-8")
        } catch (unused: Exception) {
        }
        if (ZtwoSettings.searchType.value < 10) {
            engineUrl = MainConst.engineUrls[ZtwoSettings.searchType.value]
        } else if (TextUtils.isEmpty(ZtwoSettings.searchUser.value)) {
            engineUrl = MainConst.engineUrls[4]
        } else {
            engineUrl = if (!ZtwoSettings.searchForm.value) ZtwoSettings.searchUser.value else null
        }
        return if (!TextUtils.isEmpty(engineUrl)) "$engineUrl$spoken" else String.format(
            Locale.US,
            ZtwoSettings.searchUser.value!!,
            spoken
        )
    }

    @JvmStatic
    fun normalizeUrl(context: Context?, path: String?): String? {
        var result = path
        if (!TextUtils.isEmpty(path)) {
            result = path
            if (!URLUtil.isValidUrl(path)) {
                result = path
                if (!path!!.startsWith("chrome://")) {
                    result = path
                    if (!path.startsWith("data:image/")) {
                        result = path
                        if (!path.startsWith("blob:")) {
                            if (context != null && DbBookRecent.isEnabled()) {
                                addAsync(context, path)
                            }

                            if (Patterns.WEB_URL.matcher(path).matches()) {
                                // an address typed without a scheme: https unless "Connect with HTTPS" is off
                                val scheme = if (PdfSettings.onlyHttps.value) "https://" else "http://"

                                result = scheme + path
                            } else {
                                result = MainUtil.searchUrlFor(path)
                            }
                        }
                    }
                }
            }
        }

        if (result == null) {
            return result
        } else {
            val length = result.length
            var hostEnd: Byte = 16
            if (length >= 16 && result.startsWith("http")) {
                if (result.startsWith("s://naver.com", 4)) {
                    hostEnd = 17
                } else if (!result.startsWith("://naver.com", 4)) {
                    return result
                }

                return if (hostEnd >= length) "https://m.naver.com" else ("https://m.naver.com" + result.substring(
                    hostEnd.toInt()
                ))
            } else {
                return result
            }
        }
    }

    @JvmStatic
    fun sameObject(first: Any?, second: Any?): Boolean {
        if (first == null) {
            return second == null
        }
        if (second == null) {
            return false
        }
        return first == second
    }

    @JvmStatic
    fun initMaxTextureSize(context: Context?) {
        val maximumBitmapWidth = if (PdfSettings.maxTexSize.value <= 0) (Canvas().getMaximumBitmapWidth() / 2) else 0
        if (PdfSettings.maxTexSize.value <= 0 && maximumBitmapWidth > 0) {
            PdfSettings.edit(context) { set(PdfSettings.maxTexSize, maximumBitmapWidth) }
        }
    }

    @JvmStatic
    fun compareLongs(first: Long, second: Long, descending: Boolean): Int {
        val sign = if (descending) -1 else 1
        if (first < second) {
            return -sign
        }
        if (first > second) {
            return sign
        }
        return 0
    }

    @JvmStatic
    fun extractUrl(input: String?): String? {
        var start: Int
        if (TextUtils.isEmpty(input)) {
            return null
        }
        if (input!!.startsWith("https://")) {
            start = 8
        } else {
            start = if (input.startsWith("http://")) 7 else 0
        }
        if (start == 0 || start >= input.length) {
            return input
        }
        val httpIndex = input.indexOf("http", start)
        if (httpIndex > 0) {
            val schemeStart = httpIndex + 4
            if (input.startsWith("s://", schemeStart)) {
                start = httpIndex + 8
                if (start >= input.length) {
                    return input
                }
            } else if (input.startsWith("://", schemeStart) && ((httpIndex + 7).also {
                    start = it
                }) >= input.length) {
                return input
            }
        }
        val tail = input.substring(start)
        val slashIndex = tail.indexOf(47.toChar())
        if (slashIndex <= 0 || slashIndex >= tail.length) {
            return tail
        }
        val hostPart = tail.substring(0, slashIndex)
        if (TextUtils.isEmpty(hostPart)) {
            return tail
        }
        val hostPartSize = hostPart.length
        val numberSuffix = fileNumberSuffix(hostPart)
        if (TextUtils.isEmpty(numberSuffix)) {
            return tail
        }
        val sbR = StringBuilder().append(numberSuffix)
        sbR.append(tail.substring(hostPartSize))
        val found = sbR.toString()
        val ext = fileExtFromName(found, false)
        if (!numberSuffix!!.endsWith("tumblr.com") || !numberSuffix.contains("media")) {
            if (!isImageExt(ext)) {
                return found
            }
        } else if (!isImageExt(ext) && ("gifv" != ext) && ("pnj" != ext)) {
            return null
        }
        val extIndex = found.lowercase().lastIndexOf(ext!!)
        val length = if (extIndex != -1) (ext.length + extIndex) else 0
        if (extIndex != -1 && length <= found.length) {
            return found.substring(0, length)
        }
        return found
    }

    @JvmStatic
    fun uriFileSize(context: Context?, input: String?): Long {
        var statSize: Long = 0
        if (TextUtils.isEmpty(input)) {
            return 0L
        }
        var parcelFileDescriptorOpenFileDescriptor: ParcelFileDescriptor? = null
        try {
            parcelFileDescriptorOpenFileDescriptor =
                context!!.getContentResolver().openFileDescriptor(
                    Uri.parse(input), "r"
                )
            statSize = parcelFileDescriptorOpenFileDescriptor!!.getStatSize()
        } catch (unused: SecurityException) {
            statSize = -1234
        } catch (unused2: Exception) {
        }
        if (parcelFileDescriptorOpenFileDescriptor != null) {
            try {
                parcelFileDescriptorOpenFileDescriptor.close()
            } catch (unused3: Exception) {
            }
        }
        return statSize
    }

    @JvmStatic
    fun cacheFilePathFor(context: Context?, path: String?): String? {
        if (context == null || TextUtils.isEmpty(path)) {
            return null
        }
        return filesSubdir(context, ".cache") + "/" + Md5FileNameGenerator().generate(path)
    }

    @JvmStatic
    fun zoneFlags(): BooleanArray {
        val length = MainConst.engineNames.size
        val booleanArray = BooleanArray(length)
        val searchUse = ZoneSettings.searchUse.value
        val parts: Array<String?>? =
            if (!TextUtils.isEmpty(searchUse)) searchUse!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray() else null
        if (parts == null || parts.size != length) {
            ZoneSettings.editLocal { set(ZoneSettings.searchUse, "") }
            for (index in 0..<length) {
                booleanArray[index] = true
            }
        } else {
            for (index in 0..<length) {
                if (index == ZtwoSettings.searchType.value) {
                    booleanArray[index] = true
                } else {
                    booleanArray[index] = parseIntSafe(parts[index]) == 1
                }
            }
        }
        return booleanArray
    }

    @JvmStatic
    fun insetsController(window: Window?, view: View?): WindowInsetsControllerCompat? {
        var view = view
        if (window == null) {
            return null
        }
        if (view == null) {
            try {
                view = window.getDecorView()
            } catch (unused: Exception) {
                return null
            }
        }
        return WindowInsetsControllerCompat(window, view)
    }

    @JvmStatic
    fun isFakeStream(url: String?): Boolean {
        if (TextUtils.isEmpty(url)) {
            return false
        }
        return url!!.startsWith("m3fake:") || url.startsWith("tsfake:")
    }

    @JvmStatic
    fun highlightText(content: String?, color: Int, index: Int, query: String?): SpannableString? {
        if (content == null) {
            return null
        }
        val spannableString = SpannableString(content)
        if (query != null) {
            try {
                val lowerCase = content.lowercase()
                var length = lowerCase.length
                val matchIndex = if (index < length) lowerCase.indexOf(query, index) else 0
                if (index < length && matchIndex >= 0 && matchIndex < length) {
                    val matchEnd = query.length + matchIndex
                    if (matchEnd <= length) {
                        length = matchEnd
                    }
                    if (color == -14784824) {
                        spannableString.setSpan(StyleSpan(1), matchIndex, length, 33)
                        return spannableString
                    }
                    spannableString.setSpan(ForegroundColorSpan(color), matchIndex, length, 33)
                }
            } catch (unused: Exception) {
            }
        }
        return spannableString
    }

    @JvmStatic
    fun setNavBarColor(window: Window?, bottomBarColor: Int) {
        if (Build.VERSION.SDK_INT >= 35 || window == null) {
            return
        }
        window.setNavigationBarColor(bottomBarColor)
    }

    @JvmStatic
    fun saveBitmapPng(context: Context?, bitmap: Bitmap?, filePath: String?): Boolean {
        if (isBitmapOk(bitmap)) {
            return saveBitmap(
                context,
                bitmap,
                filePath,
                if (bitmap!!.hasAlpha()) CompressFormat.PNG else CompressFormat.JPEG
            )
        }
        return false
    }

    @JvmStatic
    fun stripHostPrefix(pageHost: String?): String? {
        val numberSuffix = fileNumberSuffix(pageHost)
        val length = if (numberSuffix == null) 0 else numberSuffix.length
        if (numberSuffix == null || length == 0) {
            return numberSuffix
        }
        if (length > 4 && numberSuffix.startsWith("www.")) {
            return numberSuffix.substring(4)
        }
        if (length <= 2 || !numberSuffix.startsWith(".", 1)) {
            return if (length <= 7 || !numberSuffix.startsWith("mobile.")) numberSuffix else numberSuffix.substring(7)
        }
        return numberSuffix.substring(2)
    }

    @JvmStatic
    fun blendColors(statusColor: Int, filterTint: Int): Int {
        val bitmapCreateBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmapCreateBitmap)
        canvas.drawColor(statusColor)
        canvas.drawColor(filterTint)
        val pixel = bitmapCreateBitmap.getPixel(0, 0)
        bitmapCreateBitmap.recycle()
        return pixel
    }

    @JvmStatic
    fun durationText(durationMs: Long, millis: Long): String {
        if (durationMs == 0L) {
            return "00:00"
        }
        val seconds = millis / 1000
        var hours = seconds / 3600
        val minutes = (seconds / 60) % 60
        val secs = seconds % 60
        if (durationMs < 3600000) {
            return String.format(Locale.US, "%02d:%02d", minutes, secs)
        }
        if (hours > 999) {
            hours = 999
        }
        return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
    }

    @JvmStatic
    fun retryKind(code: Int): Int {
        if (code == 429 || code == 503 || code == 504) {
            return 1
        }
        return if (code / 100 == 3) 2 else 0
    }

    @JvmStatic
    fun screenPoint(context: Context?): Point? {
        if (context == null) {
            return null
        }
        val point = Point()
        val windowManager = context.getSystemService("window") as WindowManager
        val currentWindowMetrics = windowManager.getCurrentWindowMetrics()
        val insetsIgnoringVisibility =
            currentWindowMetrics.getWindowInsets().getInsetsIgnoringVisibility(0)
        val bounds = currentWindowMetrics.getBounds()
        point.x = (bounds.width() - insetsIgnoringVisibility.left) - insetsIgnoringVisibility.right
        point.y = (bounds.height() - insetsIgnoringVisibility.top) - insetsIgnoringVisibility.bottom
        return point
    }

    @JvmStatic
    fun isDarkWebPage(onStartPage: Boolean): Boolean {
        return onStartPage && isDarkMode() && BrowserSettings.darkHome.value
    }

    @JvmStatic
    fun underlined(content: String?): SpannableString {
        val spannableString = SpannableString(content)
        spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
        return spannableString
    }

    @JvmStatic
    fun applyScreenOrientation(activity: Activity?) {
        if (activity != null) {
            val rotation = MainSettings.appRotation.value
            if (rotation == 1) {
                showPermissionNote(activity, 1)
            } else if (rotation == 2) {
                showPermissionNote(activity, 6)
            }
        }
    }

    @JvmStatic
    fun saveBitmap(
        context: Context?,
        bitmap: Bitmap?,
        path: String?,
        compressFormat: CompressFormat?
    ): Boolean {
        var outputStreamS: OutputStream?
        var found = false
        if (context != null && isBitmapOk(bitmap)) {
            if (TextUtils.isEmpty(path)) {
                return false
            }
            try {
                outputStreamS = openOutput(context, path, false)
                try {
                    bitmap!!.compress(compressFormat!!, 100, outputStreamS!!)
                    found = true
                } catch (unused: Exception) {
                }
            } catch (unused2: Exception) {
                outputStreamS = null
            }
            if (outputStreamS != null) {
                try {
                    outputStreamS.close()
                } catch (unused3: Exception) {
                }
            }
        }
        return found
    }

    @JvmStatic
    fun clipboardText(context: Context?): String? {
        if (context == null) {
            return null
        }
        try {
            val clipboardManagerJ: ClipboardManager? = MainApp.Companion.clipboard(context)
            val primaryClip: ClipData? = if (!clipboardManagerJ!!.hasPrimaryClip() || clipboardManagerJ.getPrimaryClipDescription() == null) null else clipboardManagerJ.getPrimaryClip()
            val itemAt: ClipData.Item? = if (!clipboardManagerJ.hasPrimaryClip() || clipboardManagerJ.getPrimaryClipDescription() == null || primaryClip == null) null else primaryClip.getItemAt(0)
            if (!clipboardManagerJ.hasPrimaryClip() || clipboardManagerJ.getPrimaryClipDescription() == null || primaryClip == null || itemAt == null) {
                return null
            }
            val clip = itemAt.getText()
            if (clip != null) {
                return clip.toString()
            }
            val uri = itemAt.getUri()
            if (uri != null) {
                return uri.getPath()
            }
            return null
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
        return null
    }

    @JvmStatic
    fun hostLabelUpper(host: String?): String? {
        var host = host
        var length = if (host == null) 0 else host.length
        if (host == null || length == 0) {
            return null
        }
        if (length == 1) {
            return host.uppercase()
        }
        if (length > 2 && host.startsWith(".", 1)) {
            host = host.substring(2)
            length -= 2
            if (length == 1) {
                return host.uppercase()
            }
        }
        val sb = StringBuilder()
        sb.append(host.substring(0, 1).uppercase())
        if (length <= 5 || !(host.endsWith(".com") || host.endsWith(".net"))) {
            sb.append(host.substring(1))
        } else {
            sb.append(host.substring(1, length - 4))
        }
        return sb.toString()
    }

    @JvmStatic
    fun durationTextFull(millis: Long): String {
        if (millis == 0L) {
            return "00:00"
        }
        val seconds = millis / 1000
        var hours = seconds / 3600
        val minutes = (seconds / 60) % 60
        val secs = seconds % 60
        if (hours > 999) {
            hours = 999
        }
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, secs)
    }

    @JvmStatic
    fun defaultUserAgent(context: Context?): String {
        initUserAgent(context)
        val agent = ZtwoSettings.soulAgent.value
        return (if (!TextUtils.isEmpty(agent)) agent else "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) OPUM/4.0 Chrome/131.0.0.0 Mobile Safari/537.36")!!
    }

    @JvmStatic
    fun videoMenuCount(tint: Int, live: Boolean): Int {
        val youPip = ZoneSettings.youPip.value
        if ((if (ZoneSettings.youRate.value) (if (youPip) 1 else 0) + 1 else if (youPip) 1 else 0) > 1) {
            if (live) {
                return R.drawable.outline_youtube_library_2_white_24
            }
            return if (tint == 0) R.drawable.outline_youtube_library_2_black_24 else R.drawable.outline_youtube_library_2_dark_24
        }
        if (youPip) {
            if (live) {
                return R.drawable.outline_picture_in_picture_alt_white_24
            }
            return if (tint == 0) R.drawable.outline_picture_in_picture_alt_black_24 else R.drawable.outline_picture_in_picture_alt_dark_24
        }
        if (live) {
            return R.drawable.outline_slow_motion_video_white_24
        }
        return if (tint == 0) R.drawable.outline_slow_motion_video_black_24 else R.drawable.outline_slow_motion_video_dark_24
    }

    @JvmStatic
    fun isGoogleUserContent(url: String?): Boolean {
        val host = hostOf(url, true)
        if (TextUtils.isEmpty(host)) {
            return false
        }
        return host!!.endsWith("googleusercontent.com")
    }

    @JvmStatic
    fun copyUriToFile(context: Context?, own: String?, parts: String?): Boolean {
        var outputStreamS: OutputStream?
        var found = false
        if (!TextUtils.isEmpty(own) && !TextUtils.isEmpty(parts)) {
            var inputStreamX: InputStream? = null
            try {
                outputStreamS = openOutput(context, own, true)
            } catch (unused: Exception) {
                outputStreamS = null
            }
            if (outputStreamS != null) {
                if (!TextUtils.isEmpty(parts)) {
                    try {
                        inputStreamX = openUriInput(context, parts)
                        val bArr = ByteArray(8192)
                        while (true) {
                            val read = inputStreamX!!.read(bArr, 0, 8192)
                            if (read == -1) {
                                break
                            }
                            outputStreamS.write(bArr, 0, read)
                        }
                        found = true
                    } catch (unused2: Exception) {
                    }
                    if (inputStreamX != null) {
                        try {
                            inputStreamX.close()
                        } catch (unused3: Exception) {
                        }
                    }
                }
                try {
                    outputStreamS.close()
                } catch (unused4: Exception) {
                }
            }
        }
        return found
    }

    @JvmStatic
    fun showPermissionNote(activity: Activity?, requestCode: Int) {
        if (activity == null) {
            return
        }
        MainApp.Companion.runInBackground(activity, SetActivityOrientationTask(activity, requestCode))
    }

    @JvmStatic
    fun hasChar(code: Int, line: String?): Boolean {
        return line != null && line.indexOf(code.toChar()) >= 0
    }

    /** The bottom bar's colour: the theme colour while the bars take it ([BrowserSettings.headMode] 2), else 0. */
    @JvmStatic
    fun bottomBarColor(): Int {
        if (BrowserSettings.headMode() != 2) return 0
        if (PrefWeb.headColor == 0) resetTextSizePref()
        return PrefWeb.headColor
    }

    /** The bottom bar's lighter tint (address pill, presses) to go with [bottomBarColor]. */
    @JvmStatic
    fun bottomBarLight(): Int {
        if (BrowserSettings.headMode() != 2) return 0
        if (PrefWeb.iconColor == 0) resetTextSizePref()
        return PrefWeb.iconColor
    }

    /**
     * The icon and text kind for a bar colour: 0 (dark icons) for the light bar and for light custom colours, which
     * would otherwise get white icons; else the colour unchanged (1 = dark bar, other = white icons).
     */
    @JvmStatic
    fun barInk(colour: Int): Int = com.mycompany.app.ui.theme.AppColor.barInk(colour)

    @JvmStatic
    fun barTint(tintColor: Int, onStartPage: Boolean): Int {
        if (isDarkWebPage(onStartPage)) {
            return 1
        }
        if (BrowserSettings.headMode() == 0 || tintColor == 0 || tintColor == 1) {
            return if (AppState.darkUi) 1 else 0
        }
        return tintColor
    }

    @JvmStatic
    fun durationTextLong(left: Long): String {
        if (left <= 0) {
            return "0:00:00"
        }
        val seconds = left / 1000
        var hours = seconds / 3600
        val minutes = (seconds / 60) % 60
        val secs = seconds % 60
        if (hours > 999) {
            hours = 999
        }
        return String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
    }

    @JvmStatic
    fun stripHitomiTitle(rawTitle: String?, title: String?): String? {
        return if (TextUtils.isEmpty(rawTitle) || TextUtils.isEmpty(title) || !rawTitle!!.endsWith("hitomi.la") || 12 >= title!!.length || !title.endsWith(
                " | Hitomi.la"
            )
        ) title else title.let { s -> s.substring(0, s.length - 12) }
    }

    /** YouTube helper script, first half, from 5 native parts.  */
    @JvmStatic
    fun nativeJs5b(): String? {
        return joinNative(5, 2)
    }

    @JvmStatic
    fun isGoogleHome(pageHost: String?): Boolean {
        val length = if (pageHost == null) 0 else pageHost.length
        if (pageHost == null || length == 0 || !pageHost.endsWith("google.com")) {
            return false
        }
        if (length > 10 && pageHost.lastIndexOf(46.toChar(), length - 10) != length - 11) {
            return pageHost == "https://google.com"
        }
        return true
    }

    @JvmStatic
    fun moveFileOrUri(context: Context?, path: String?, uriString: String?): Boolean {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(uriString)) {
            return false
        }
        val srcIsContent = MainUri.isContentUri(path)
        val destIsContent = MainUri.isContentUri(uriString)
        if (srcIsContent || destIsContent) {
            return MainUri.moveUri(context, path, uriString, srcIsContent, destIsContent)
        }
        val file = File(path!!)
        if (file.exists()) {
            return file.renameTo(File(uriString!!))
        }
        return false
    }

    @JvmStatic
    fun applyImageOrientation(activity: Activity?) {
        val rotation = MainSettings.appRotation.value
        if (rotation == 1) {
            showPermissionNote(activity, 1)
        } else if (rotation == 2) {
            showPermissionNote(activity, 6)
        } else {
            showPermissionNote(activity, -1)
        }
    }

    @JvmStatic
    fun copyTextToast(context: Context?, copiedClipboard: Int, label: String?, url: String?) {
        var label = label
        if (context == null || TextUtils.isEmpty(url)) {
            return
        }
        if (TextUtils.isEmpty(label)) {
            label = "text"
        }
        MainApp.Companion.runInBackground(context, ClipboardTask(context, copiedClipboard, label, url))
    }

    @JvmStatic
    fun acceptHeaderFor(url: String?): String {
        if (isImagePath(url)) {
            return "application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
        }
        var isMedia = false
        if (MainUrl.isMediaUrl(url, false)) {
            return "application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
        }
        val host = hostOf(url, true)
        if (!TextUtils.isEmpty(host) && (host!!.endsWith("giphy.com") || host.endsWith("tumblr.com"))) {
            isMedia = host.contains("media")
        }
        return if (!isMedia) "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9" else "application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
    }

    @JvmStatic
    fun mimeForExt(ext: String?): String? {
        if (TextUtils.isEmpty(ext)) {
            return null
        }
        val lowerCase = ext!!.lowercase()
        if (lowerCase == "gifv") {
            return "image/gif"
        }
        if (lowerCase == "m3u8") {
            return "video/*"
        }
        if (lowerCase == "flv") {
            return "video/x-flv"
        }
        if (lowerCase == "aac") {
            return "audio/aac"
        }
        return if (isSubtitleExt(lowerCase)) "text/*" else MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(lowerCase)
    }

    @JvmStatic
    fun comicSiteKind(host: String?): Int {
        if (TextUtils.isEmpty(host)) {
            return 0
        }
        if (host!!.endsWith("hitomi.la")) {
            return 1
        }
        return if (host.endsWith("manatoki.net") || host.endsWith("newtoki.com")) 2 else 0
    }

    /** YouTube helper script, second half, from 5 native parts.  */
    @JvmStatic
    fun nativeJs5c(): String? {
        return joinNative(5, 3)
    }

    @JvmStatic
    fun isGoogleSearch(pageUrl: String?): Boolean {
        if (TextUtils.isEmpty(pageUrl)) {
            return false
        }
        return pageUrl!!.startsWith("https://www.google.com/search")
    }

    @JvmStatic
    fun shouldOpenInNewTab(
        webNestView: WebNestView?,
        pageUrl: String?,
        pageHost: String?,
        linkUrl: String?,
        linkHost: String?,
        strict: Boolean
    ): Boolean {
        var pageHost = pageHost
        var linkHost = linkHost
        if (!ZtwoSettings.addPage.value || sameObject(pageUrl, linkUrl)) {
            return false
        }
        if ("file:///android_asset/shortcut.html" == pageUrl) {
            return true
        }
        if (strict && webNestView != null && webNestView.pageState != 4) {
            return false
        }
        if (isIpAddress(pageUrl) || isIpAddress(linkUrl)) {
            return false
        }
        if (TextUtils.isEmpty(pageHost)) {
            pageHost = hostOf(pageUrl, true)
            if (TextUtils.isEmpty(pageHost)) {
                return true
            }
        }
        if (TextUtils.isEmpty(linkUrl)) {
            return true
        }
        if (TextUtils.isEmpty(linkHost)) {
            linkHost = MainUtil.hostOf(linkUrl!!, false)
            if (TextUtils.isEmpty(linkHost)) {
                return true
            }
        }
        if (!isGoogleHome(pageHost) || !isGoogleHome(linkHost)) {
            return true
        }
        return linkUrl!!.lastIndexOf("&sclient=") == -1
    }

    @JvmStatic
    fun injectOverlayJs(webView: WebView?) {
        var scriptIndex = 0
        var arrayList: ArrayList<Any?>? = null
        if (!MainNative.partsReady(9, MainNative.overlayParts)) {
            val overlayParts: ArrayList<Any?> = ArrayList<Any?>()
            var index = 0
            while (true) {
                if (index >= 9) {
                    MainNative.overlayParts = overlayParts
                    arrayList = overlayParts
                    break
                } else {
                    val overlayJs = getOverlayJs(index)
                    if (TextUtils.isEmpty(overlayJs)) {
                        break
                    }
                    overlayParts.add(overlayJs)
                    index++
                }
            }
        } else {
            arrayList = MainNative.overlayParts
        }
        if (arrayList == null || arrayList.size != 9) {
            return
        }
        val sb = StringBuilder()
        while (true) {
            if (scriptIndex >= 5) {
                if (TtsSettings.checkOver.value == 1) {
                    sb.append("(function(){myOvr1(document);})();")
                    evalJs(webView, sb.toString(), true)
                    return
                }
                for (index in 5..8) {
                    val script = arrayList.get(index) as String?
                    if (TextUtils.isEmpty(script)) {
                        return
                    }
                    sb.append(script)
                }
                evalJs(webView, sb.toString(), true)
                return
            }
            val script = arrayList.get(scriptIndex) as String?
            if (TextUtils.isEmpty(script)) {
                return
            }
            sb.append(script)
            scriptIndex++
        }
    }

    @JvmStatic
    fun copyFileOrUri(context: Context?, src: String?, dest: String?): Boolean {
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(dest)) {
            return false
        }
        val srcIsContent = MainUri.isContentUri(src)
        val destIsContent = MainUri.isContentUri(dest)
        return if (srcIsContent || destIsContent) MainUri.copyUri(context, src, dest, srcIsContent, destIsContent) else copyFile(src, dest)
    }

    @JvmStatic
    fun cookieFor(context: Context?, referer: String?): String? {
        val cookieManagerL: CookieManager? = if (TextUtils.isEmpty(referer)) null else MainApp.Companion.cookieManager(context)
        if (TextUtils.isEmpty(referer) || cookieManagerL == null) {
            return null
        }
        return cookieManagerL.getCookie(referer)
    }

    @JvmStatic
    fun userFontPath(context: Context?): String? {
        if (!TextUtils.isEmpty(MainApp.Companion.userFontPathCache)) {
            return MainApp.Companion.userFontPathCache
        }
        val fontDir = filesSubdir(context, ".font")
        if (TextUtils.isEmpty(fontDir)) {
            return null
        }
        val fontPath = "$fontDir/font.ttf"
        MainApp.Companion.userFontPathCache = fontPath
        return fontPath
    }

    @JvmStatic
    fun mimeForUrl(name: String?): String? {
        return mimeForExt(extOf(name))
    }

    @JvmStatic
    fun parseFontList(fontSpec: String?): String? {
        val chosenFont: String?
        if (TextUtils.isEmpty(fontSpec)) {
            return fontSpec
        }
        val fontList = fontSpec!!.trim { it <= ' ' }
        val fontEntries: Array<String?>? = if (!TextUtils.isEmpty(fontList) && hasChar(44, fontList)) fontList.split(",".toRegex())
                .dropLastWhile { it.isEmpty() }.toTypedArray() else null
        if (!TextUtils.isEmpty(fontList) && hasChar(44, fontList) && fontEntries != null && fontEntries.size != 0) {
            var fonts: String? = null
            var start = 0
            for (entry in fontEntries) {
                if (!TextUtils.isEmpty(entry)) {
                    val fontEntry = entry!!.trim { it <= ' ' }
                    val spaceIndex = if (!TextUtils.isEmpty(fontEntry)) fontEntry.lastIndexOf(32.toChar()) else 0
                    val weightStart = if (!TextUtils.isEmpty(fontEntry) && spaceIndex != -1) (spaceIndex + 1) else 0
                    if (!TextUtils.isEmpty(fontEntry) && spaceIndex != -1 && weightStart < fontEntry.length - 1) {
                        var weightText = fontEntry.let { s -> s.substring(weightStart, s.length - 1) }
                        if (!TextUtils.isEmpty(weightText)) {
                            var length = weightText.length
                            var index = 0
                            while (index < length && (weightText.get(index) < '0' || weightText.get(index) > '9')) {
                                index++
                            }
                            while (index < length) {
                                val last = length - 1
                                if (weightText.get(last) >= '0' && (length <= 0 || weightText.get(last) <= '9')) {
                                    break
                                }
                                length--
                            }
                            if (index > 0 || length < weightText.length) {
                                weightText = weightText.substring(index, length)
                            }
                        }
                        val weight = parseIntSafe(weightText)
                        if (weight > start) {
                            val fontName = fontEntry.substring(0, spaceIndex)
                            if (!TextUtils.isEmpty(fontName)) {
                                start = weight
                                fonts = fontName
                            }
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(fonts)) {
                fonts = fontEntries[fontEntries.size - 1]
                if (TextUtils.isEmpty(fonts)) {
                    return fontList
                }
            }
            chosenFont = fonts!!.trim { it <= ' ' }
            if (!TextUtils.isEmpty(chosenFont) && (!URLUtil.isNetworkUrl(fontList) || URLUtil.isNetworkUrl(
                    chosenFont
                ))
            ) {
                return chosenFont
            }
        }
        return fontList
    }

    @JvmStatic
    fun youTubeIdOf(pageUrl: String?): String? {
        var length: Int = 0
        val hostIndex = if (TextUtils.isEmpty(pageUrl)) 0 else pageUrl!!.indexOf("youtube.com/")
        val watchIndex = if (TextUtils.isEmpty(pageUrl) || hostIndex == -1) 0 else pageUrl!!.indexOf("watch?", hostIndex + 12)
        val videoParam = if (TextUtils.isEmpty(pageUrl) || hostIndex == -1 || watchIndex == -1) 0 else pageUrl!!.indexOf("v=", watchIndex + 6)
        val start = if (TextUtils.isEmpty(pageUrl) || hostIndex == -1 || watchIndex == -1 || videoParam == -1) 0 else (videoParam + 2)
        if (TextUtils.isEmpty(pageUrl) || hostIndex == -1 || watchIndex == -1 || videoParam == -1 || start >= (pageUrl!!.length.also { length = it })) {
            return null
        }
        val end = videoParam + 13
        return if (end >= length) pageUrl.substring(start) else pageUrl.substring(start, end)
    }

    @JvmStatic
    fun matchesNativeHost(pageHost: String?, strict: Boolean): Boolean {
        if (TextUtils.isEmpty(pageHost) || TextUtils.isEmpty(nativeHost)) {
            return false
        }
        if (pageHost!!.endsWith(nativeHost!!)) {
            return true
        }
        if (!strict || TextUtils.isEmpty(nativeHostBare)) {
            return false
        }
        return pageHost == nativeHostBare
    }

    @JvmStatic
    fun needsTextZoom(url: String?): Boolean {
        if (BrowserSettings.themeImg.value && BrowserSettings.imgBright.value != 100) {
            return "file:///android_asset/shortcut.html" != url
        }
        return false
    }

    @JvmStatic
    fun handleDownloadLink(
        activity: Activity?,
        context: Context?,
        webNestView: WebNestView?,
        pageUrl: String?,
        pageHost: String?,
        url: String?,
        urlHost: String?,
        start: Int
    ): Int {
        val txtIndex: Int
        val titleIndex: Int
        if (activity == null || webNestView == null || TextUtils.isEmpty(url)) {
            return 0
        }
        if (MainUtil.isPdfUrl(url!!)) {
            return 6
        }
        if (DataBookLink.instance(context).hasPage(url)) {
            return 9
        }
        if (TextUtils.isEmpty(urlHost)) {
            val httpIndex = if (!TextUtils.isEmpty(url) && url.startsWith("abp:subscribe")) url.indexOf("http") else 0
            val listEnd = if (!TextUtils.isEmpty(url) && url.startsWith("abp:subscribe") && httpIndex > 0 && httpIndex < url.length) url.indexOf(".txt", httpIndex) else 0
            if (!TextUtils.isEmpty(url) && url.startsWith("abp:subscribe") && httpIndex > 0 && httpIndex < url.length && listEnd > httpIndex && listEnd + 4 <= url.length) {
                return 7
            }
        } else {
            if (!TextUtils.isEmpty(url) && url.startsWith("https://subscribe.adblockplus.org/?location=http") && (((url.indexOf(
                    ".txt",
                    44
                ).also {
                    txtIndex = it
                }) > 44 && txtIndex + 4 <= url.length) || ((url.indexOf("&title=", 44)
                    .also { titleIndex = it }) > 44 && titleIndex <= url.length))
            ) {
                return 7
            }
            if (BrowserSettings.popBlock.value == 0 || !isYouTubeHost(pageHost)) {
                if (isHitomi(pageHost)) {
                    if (MainUtil.isHitomiReader(url, urlHost)) {
                        return 4
                    }
                    if (!isHitomi(urlHost)) {
                        return 8
                    }
                } else if (MainUtil.isEmgPath(url, urlHost) && !isEmgUrl(pageUrl)) {
                    return 4
                }
            } else if ((TextUtils.isEmpty(urlHost) || (!isYouTubeHost(urlHost) && !urlHost!!.endsWith("accounts.google.com"))) && !urlHost!!.endsWith(
                    "youtu.be"
                )
            ) {
                return 4
            }
        }
        if (url.startsWith("https://play.google.com/store/apps/details")) {
            if (TextUtils.isEmpty(pageUrl) || !pageUrl!!.startsWith("https://play.google.com")) {
                val appBlock = PdfSettings.appBlock.value
                if (appBlock == 2) {
                    return 13
                }
                if (appBlock == 0) {
                    if (openInExternalApp(activity, url)) {
                        return 14
                    }
                }
            }
        } else if (AlbumSettings.linkApp.value) {
            if (openInExternalApp(activity, url)) {
                return 14
            }
        }
        return start
    }

    /** Copies file `str` to `str2`; a partial target is deleted on failure.  */
    @JvmStatic
    fun copyFile(portraitPath: String?, landscapePath: String?): Boolean {
        if (TextUtils.isEmpty(portraitPath) || TextUtils.isEmpty(landscapePath)) {
            return false
        }
        var ok = false
        try {
            FileInputStream(portraitPath).use { `in` ->
                FileOutputStream(landscapePath).use { out ->
                    `in`.getChannel().use { source ->
                        out.getChannel().use { target ->
                            source.transferTo(0, source.size(), target)
                            ok = true
                        }
                    }
                }
            }
        } catch (unused: Exception) {
        }
        if (!ok) {
            val file = File(landscapePath!!)
            if (file.exists()) {
                file.delete()
            }
        }
        return ok
    }

    @JvmStatic
    fun requestHeadersFor(context: Context?, referer: String?, userAgentText: String?): HashMap<String, String>? {
        var userAgentText = userAgentText
        val cookie: String?
        if (!TextUtils.isEmpty(referer)) {
            try {
                val map: HashMap<String, String> = HashMap()
                var found = false
                if (isGoogleSearch(userAgentText)) {
                    cookie = null
                    userAgentText = null
                } else {
                    if (MainUtil.isGoogleUserContent(referer!!)) {
                        userAgentText = referer
                    } else if (TextUtils.isEmpty(userAgentText)) {
                        val mainAppO: MainApp? = MainApp.Companion.from(context)
                        if (mainAppO != null && "https://missav.com" == mainAppO.hostCacheValue(referer)) {
                            found = true
                        }
                    } else {
                        val mainAppO: MainApp? = MainApp.Companion.from(context)
                        if (mainAppO != null) {
                            val cachedHost = mainAppO.hostCacheValue(referer)
                            if (!TextUtils.isEmpty(cachedHost)) {
                                map.put("Origin", cachedHost!!)
                            }
                        }
                    }
                    cookie = cookieFor(context, userAgentText)
                    if (TextUtils.isEmpty(userAgentText)) {
                        userAgentText = referer
                    }
                }
                map.put("Accept", MainUtil.acceptHeaderFor(referer!!))
                map.put("Accept-Encoding", "identity")
                if (!found) {
                    map.put("User-Agent", userAgentPref())
                }
                if (!TextUtils.isEmpty(userAgentText)) {
                    map.put("Referer", userAgentText!!)
                }
                if (!TextUtils.isEmpty(cookie)) {
                    map.put("Cookie", cookie!!)
                }
                return map
            } catch (unused: Exception) {
            }
        }
        return null
    }

    @JvmStatic
    fun userFontResponse(context: Context?, url: String?): WebResourceResponse? {
        var inputStreamZ: InputStream?
        if (TextUtils.isEmpty(url) || !url!!.contains("soul_user_font.ttf")) {
            return null
        }
        try {
            inputStreamZ = openFileInput(userFontPath(context))
            try {
                return WebResourceResponse("application/x-font-ttf", "UTF-8", inputStreamZ)
            } catch (unused: Exception) {
                if (inputStreamZ != null) {
                    try {
                        inputStreamZ.close()
                    } catch (unused2: Exception) {
                    }
                }
                return null
            }
        } catch (unused3: Exception) {
            inputStreamZ = null
        }
        return null
    }

    @JvmStatic
    fun mimeForPath(url: String?): String? {
        return mimeForExt(fileExtOf(url, false))
    }

    @JvmStatic
    fun windowStatusColor(window: Window?): Int {
        if (Build.VERSION.SDK_INT < 35 && window != null) {
            return window.getStatusBarColor()
        }
        return 0
    }

    @JvmStatic
    fun youTubeVideoId(pageUrl: String?): String? {
        val length = if (pageUrl == null) 0 else pageUrl.length
        val hostIndex = if (pageUrl == null || length == 0) 0 else pageUrl.indexOf("youtube.com/")
        if (pageUrl == null || length == 0 || hostIndex == -1) {
            return null
        }
        val watchIndex = pageUrl.indexOf("watch?", hostIndex + 12)
        if (watchIndex != -1) {
            val videoParam = pageUrl.indexOf("v=", watchIndex + 6)
            val start = if (videoParam == -1) 0 else (videoParam + 2)
            if (videoParam == -1 || start >= length) {
                return null
            }
            val end = videoParam + 13
            return if (end >= length) pageUrl.substring(start) else pageUrl.substring(start, end)
        }
        val shortsIndex = pageUrl.indexOf("shorts/", watchIndex + 12)
        val start = if (shortsIndex == -1) 0 else (shortsIndex + 7)
        if (shortsIndex == -1 || start >= length) {
            return null
        }
        val end = shortsIndex + 18
        return if (end >= length) pageUrl.substring(start) else pageUrl.substring(start, end)
    }

    @JvmStatic
    fun hasFragmentPage(url: String?): Boolean {
        var length: Int = 0
        var hashIndex: Int = 0
        var from: Int = 0
        var dashIndex: Int = 0
        return url != null && (url.length.also {
            length = it
        }) != 0 && (url.lastIndexOf(35.toChar())
            .also { hashIndex = it }) >= 0 && ((hashIndex + 1).also {
            from = it
        }) < length && (indexOfCharIn(url, '-', from, length).also { dashIndex = it }) > from && dashIndex + 1 <= length
    }

    @JvmStatic
    fun isLockEnabled(): Boolean {
        if (SecretSettings.lockType.value == 0) {
            return false
        }
        return !SecretSettings.lockSecret.value || SyncSettings.secretMode.value
    }

    @JvmStatic
    fun keepUriPermission(context: Context?, uri: Uri?) {
        if (context != null) {
            try {
                if (uri != null) {
                    try {
                        context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    } catch (unused: SecurityException) {
                        context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
            } catch (unused2: Exception) {
            }
        }
    }

    @JvmStatic
    fun copyText(context: Context?, copiedClipboard: Int, label: String?, valueText: String?) {
        var label = label
        if (context == null || TextUtils.isEmpty(valueText)) {
            return
        }
        if (TextUtils.isEmpty(label)) {
            label = "text"
        }
        try {
            MainApp.Companion.clipboard(context)!!.setPrimaryClip(ClipData.newPlainText(label, valueText))
            if (copiedClipboard != 0) {
                Handler(Looper.getMainLooper()).post(MessageToastTask(context, copiedClipboard))
            }
        } catch (unused: Exception) {
        } catch (unused: OutOfMemoryError) {
        }
    }

    /**
     * Content length of `url` (0 if unknown). An HTML response counts only when the url
     * itself points at an HTML file, so error / login pages are not mistaken for the download.
     */
    @JvmStatic
    fun contentLengthOf(context: Context?, url: String?, referer: String?): Long {
        if (TextUtils.isEmpty(url)) {
            return 0
        }
        val conn = openHeadConnection(context, url, referer, 0, 0, false, userAgentPref(), true)
        if (conn == null) {
            return 0
        }
        var length: Long = 0
        try {
            conn.setDoInput(true)
            conn.connect()
            var type: String? = null
            try {
                type = conn.getContentType()
            } catch (unused: Exception) {
            }
            var ok = TextUtils.isEmpty(type) || !type!!.startsWith("text/html")
            if (!ok) {
                val expected = mimeForPath(url)
                ok = !TextUtils.isEmpty(expected) && expected!!.startsWith("text/html")
            }
            if (ok) {
                length = conn.getContentLengthLong()
            }
        } catch (unused: Exception) {
        }
        conn.disconnect()
        return length
    }

    @JvmStatic
    fun loadTypeface(context: Context?, path: String?): Typeface? {
        if (TextUtils.isEmpty(path)) {
            return null
        }
        try {
            return if (MainUri.isContentUri(path)) Typeface.createFromFile(
                MainUri.fileNameOf(
                    context,
                    path
                )
            ) else Typeface.createFromFile(path)
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun embeddedPictureOf(context: Context?, input: String?): ByteArray? {
        var embeddedPicture: ByteArray?
        if (context != null && !TextUtils.isEmpty(input)) {
            val mediaMetadataRetriever = MediaMetadataRetriever()
            try {
                mediaMetadataRetriever.setDataSource(context, Uri.parse(input))
                embeddedPicture = mediaMetadataRetriever.getEmbeddedPicture()
            } catch (unused: Exception) {
                embeddedPicture = null
            }
            releaseRetriever(mediaMetadataRetriever)
            if (embeddedPicture != null) {
                return embeddedPicture
            }
        }
        return null
    }

    @JvmStatic
    fun openFileWith(activity: Activity?, input: String?, mime: String?, externalFlag: Boolean, externalOpen: Boolean): Boolean {
        if (activity == null || TextUtils.isEmpty(input)) {
            return false
        }
        try {
            val intent = Intent("android.intent.action.VIEW")
            intent.setDataAndType(Uri.parse(input), mime)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            if (externalOpen) {
                activity.startActivity(
                    Intent.createChooser(
                        intent,
                        activity.getString(R.string.open_with)
                    )
                )
                return true
            }
            activity.startActivity(intent)
            return true
        } catch (unused: ActivityNotFoundException) {
            if (!externalFlag) {
                return false
            }
            showToast(activity, R.string.apps_none)
            return false
        } catch (unused2: Exception) {
            if (!externalFlag) {
                return false
            }
            showToast(activity, R.string.invalid_path)
            return false
        }
    }

    @JvmStatic
    fun isHitomi(pageHost: String?): Boolean {
        if (TextUtils.isEmpty(pageHost)) {
            return false
        }
        return pageHost!!.endsWith("hitomi.la")
    }

    @JvmStatic
    fun hasZtwoFlag(key: Int): Boolean {
        return (ZtwoSettings.tabOpen.value and key) == key
    }

    @JvmStatic
    fun injectPhVideoJs(webNestView: WebNestView?, downVideo: String?, host: String?) {
        if (TextUtils.isEmpty(downVideo) || TextUtils.isEmpty(host) || !downVideo!!.startsWith("http")) {
            return
        }
        if (!host!!.endsWith("pornhub.com")) {
            val sb = StringBuilder()
            sb.append("var ele=document.querySelector(\"video[src*='")
            sb.append(downVideo)
            sb.append("']\");if(!ele){ele=document.querySelector(\"source[src*='")
            sb.append(downVideo)
            sb.append("']\");if(ele){ele=ele.parentNode;}}if(ele){android.onPstDet('")
            sb.append(downVideo)
            sb.append("',ele.poster,null);}")
            sb.insert(0, "(function(){")
            sb.append("})();")
            evalJs(webNestView, sb.toString(), true)
            return
        }
        val length = downVideo.length
        var videoName: String? = null
        if (length != 0) {
            var mp4Index = downVideo.lastIndexOf(".mp4")
            if (mp4Index == -1) {
                mp4Index = downVideo.lastIndexOf(".webm")
            }
            val nameEnd = if (mp4Index > 0 && mp4Index <= length) downVideo.lastIndexOf(
                    47.toChar(),
                    mp4Index - 1
                ) else 0
            val nameStart = if (mp4Index > 0 && mp4Index <= length && nameEnd > 0 && nameEnd < mp4Index) downVideo.lastIndexOf(
                    47.toChar(),
                    nameEnd - 1
                ) else 0
            val index = if (mp4Index > 0 && mp4Index <= length && nameEnd > 0 && nameEnd < mp4Index && nameStart >= 0) (nameStart + 1) else 0
            if (mp4Index > 0 && mp4Index <= length && nameEnd > 0 && nameEnd < mp4Index && nameStart >= 0 && index < nameEnd) {
                videoName = downVideo.substring(index, nameEnd)
            }
        }
        if (TextUtils.isEmpty(videoName)) {
            return
        }
        val sb = StringBuilder()
        sb.append("var ele=document.querySelector(\"img[src*='")
        sb.append(videoName)
        sb.append("'],img[data-src*='")
        sb.append(videoName)
        sb.append("']\");if(ele){var par=ele.parentNode;if(par){android.onPhPstDet('")
        sb.append(videoName).append("',ele.src,par.innerHTML);}else{android.onPstDet('").append(videoName).append("',ele.src,null);}}")
        sb.insert(0, "(function(){")
        sb.append("})();")
        evalJs(webNestView, sb.toString(), true)
    }

    @JvmStatic
    fun clearCacheDirs(context: Context?) {
        if (context != null) {
            try {
                val cacheDir = context.getCacheDir()
                deleteRecursive(cacheDir)
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs()
                }
            } catch (unused: Exception) {
            }
            try {
                val externalCacheDir = context.getExternalCacheDir()
                MainUtil.deleteRecursive(externalCacheDir!!)
                if (externalCacheDir.exists()) {
                    return
                }
                externalCacheDir.mkdirs()
            } catch (unused2: Exception) {
            }
        }
    }

    @JvmStatic
    fun zeroPadFormat(index: Int): String? {
        if (index < 2) {
            return null
        }
        return "%0${index}d"
    }

    @JvmStatic
    fun zeroPadded(index: Int, digits: Int): String {
        if (digits < 2) {
            return index.toString()
        }
        return String.format(Locale.US, StringBuilder("%0").append(digits).append("d").toString(), index)
    }

    @JvmStatic
    fun childThumb2(context: Context?, childItem: ChildItem?): Bitmap? {
        var bitmapB: Bitmap? = null
        if (TextUtils.isEmpty(childItem!!.path)) {
            return null
        }
        if (TextUtils.isEmpty(childItem.filePath)) {
            childItem.filePath = cacheFilePathFor(context, childItem.path)
        }
        if (File(childItem.filePath!!).length() > 0) {
            return BitmapUtil.decodeFile(childItem.filePath)
        }
        val path = childItem.path
        val bArrV: ByteArray? = if (!TextUtils.isEmpty(path)) embeddedPictureOf(context, path) else null
        if (!TextUtils.isEmpty(path) && bArrV != null && bArrV.size != 0) {
            try {
                val options = BitmapFactory.Options()
                options.inJustDecodeBounds = true
                BitmapUtil.decodeBytesWith(bArrV, bArrV.size, options)
                val outWidth = options.outWidth
                val dp48Px: Int = MainApp.Companion.dp48
                if (outWidth > dp48Px || options.outHeight > dp48Px) {
                    options.inSampleSize = sampleSizeFor(outWidth, options.outHeight, dp48Px, dp48Px)
                }
                options.inJustDecodeBounds = false
                bitmapB = BitmapUtil.decodeBytesWith(bArrV, bArrV.size, options)
            } catch (unused: Exception) {
            }
        }
        saveBitmapPng(context, bitmapB, childItem.filePath)
        return bitmapB
    }

    @JvmStatic
    fun statusBarInsetPx(context: Context?, window: Window?, view: View?): Int {
        var view = view
        val statusHeight = if (MainSettings.statusConfirmed.value) MainSettings.statusHeight.value else 0
        if (MainSettings.statusConfirmed.value && statusHeight > 0) {
            return statusHeight
        }
        if (window == null) {
            return 0
        }
        if (view == null) {
            view = window.getDecorView()
        }
        val rect = Rect()
        view.getWindowVisibleDisplayFrame(rect)
        if (rect.top <= 0) {
            return 0
        }
        val fallbackHeight = dpToPx(context, 70.0f).toInt()
        val top = rect.top
        if (top >= fallbackHeight) {
            return 0
        }
        val savedHeight = MainSettings.statusHeight.value
        val sameTop = savedHeight == top
        if (savedHeight == top && MainSettings.statusConfirmed.value == sameTop) {
            return savedHeight
        }
        MainSettings.editLocal { set(MainSettings.statusConfirmed, sameTop) }
        MainSettings.editLocal { set(MainSettings.statusHeight, top) }
        val prefMainR = get(context, false)
        prefMainR!!.putInt(MainSettings.statusHeight.value, "mStatusHeight")
        prefMainR.putBool("mStatusConfirm", MainSettings.statusConfirmed.value)
        prefMainR.saveAsync()
        return MainSettings.statusHeight.value
    }

    @JvmStatic
    fun isHmgUrl(url: String?): Boolean {
        return isHitomiReader(url, hostOf(url, true))
    }

    @JvmStatic
    fun needsDesktopUa(pageHost: String?): Boolean {
        val length = if (pageHost == null) 0 else pageHost.length
        if (pageHost == null || length < 8) {
            return false
        }
        if (pageHost.endsWith(".com")) {
            if (pageHost.startsWith("m.vk", length - 8)) {
                return true
            }
            if (length > 9 && pageHost.startsWith("openai", length - 10)) {
                return true
            }
        } else if (length >= 23 && pageHost.endsWith("biharbhumi.bihar.gov.in")) {
            return true
        }
        return false
    }

    @JvmStatic
    fun setActivityOrientation(activity: Activity?, orientation: Int) {
        if (activity != null) {
            try {
                activity.setRequestedOrientation(orientation)
            } catch (unused: Exception) {
            }
        }
    }

    @JvmStatic
    fun ensureWebViewDir(context: Context?) {
        if (context != null) {
            try {
                val file = File(context.getDataDir().getPath(), "app_webview")
                deleteRecursive(file)
                if (file.exists()) {
                    return
                }
                file.mkdirs()
            } catch (unused: Exception) {
            }
        }
    }

    @JvmStatic
    fun signOf(size: Int): Int {
        var size = size
        var index = 0
        if (size < 0) {
            size = -size
        }
        while (size != 0) {
            size /= 10
            index++
        }
        return index
    }

    @JvmStatic
    fun formatDate(time: Long, formatter: String?, pattern: String?): String? {
        var formatter = formatter
        formatter = if (formatter == null) datePattern(pattern) else null
        if (formatter == null) {
            return null
        }
        try {
            return DateFormat.format(formatter, time).toString()
        } catch (unused: Exception) {
            return SimpleDateFormat(formatter, MainApp.Companion.currentLocale()).format(Date(time))
        }
    }

    @JvmStatic
    fun stripPunctuation(text: String?, pattern: Pattern?): String? {
        var text = text
        var pattern = pattern
        val edgeChar: String?
        if (TextUtils.isEmpty(text)) {
            return null
        }
        if (!TextUtils.isEmpty(text) && (pattern != null || (Pattern.compile("\\p{Punct}")
                .also { pattern = it }) != null)
        ) {
            text = pattern!!.matcher(text!!).replaceAll("")
        }
        if (TextUtils.isEmpty(text)) {
            return null
        }
        if (!TextUtils.isEmpty(text)) {
            val length = text!!.length
            var index = 0
            while (index < length && text.get(index) <= ' ') {
                index++
            }
            if (index > 0 || length < text.length) {
                text = text.substring(index, length)
            }
        }
        if (TextUtils.isEmpty(text)) {
            return null
        }
        if (!text!!.startsWith("˝")) {
            edgeChar = text.substring(0, 1)
        } else {
            if (text.length == 1) {
                return null
            }
            edgeChar = text.substring(1, 2)
        }
        if (TextUtils.isEmpty(edgeChar)) {
            return null
        }
        return edgeChar.uppercase()
    }

    @JvmStatic
    fun readTextFile(context: Context?, inputStream: InputStream?, trim: Boolean): String? {
        var bufferedReader: BufferedReader?
        var content: String? = null
        if (inputStream != null) {
            try {
                bufferedReader = if (trim) BufferedReader(
                    InputStreamReader(
                        GZIPInputStream(inputStream),
                        "UTF-8"
                    )
                ) else BufferedReader(
                    InputStreamReader(inputStream, "UTF-8")
                )
                var sb: StringBuilder? = null
                while (true) {
                    try {
                        val line = bufferedReader.readLine()
                        if (line == null) {
                            break
                        }
                        if (!TextUtils.isEmpty(line)) {
                            if (sb == null) {
                                sb = StringBuilder()
                            } else {
                                sb.append("\n")
                            }
                            sb.append(line)
                        }
                    } catch (unused: Exception) {
                    } catch (unused2: OutOfMemoryError) {
                        restartLater(context)
                    }
                }
                if (sb != null) {
                    content = sb.toString()
                }
            } catch (unused3: Exception) {
                bufferedReader = null
            } catch (unused4: OutOfMemoryError) {
                bufferedReader = null
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close()
                } catch (unused5: Exception) {
                }
            }
            try {
                inputStream.close()
            } catch (unused6: Exception) {
            }
        }
        return content
    }

    @JvmStatic
    fun openIntentUri(activity: Activity?, intentUri: String?): Boolean {
        if (activity != null && !TextUtils.isEmpty(intentUri)) {
            try {
                val uri = Intent.parseUri(intentUri, Intent.URI_INTENT_SCHEME)
                if (uri == null) {
                    return false
                }
                try {
                    uri.addCategory("android.intent.category.BROWSABLE")
                    uri.setComponent(null)
                    uri.setSelector(null)
                    uri.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startSoulActivity(activity, uri)
                    return true
                } catch (unused: Exception) {
                    val packageName = uri.getPackage()
                    if (!TextUtils.isEmpty(packageName)) {
                        return openPlayStore(activity, packageName)
                    }
                    return false
                }
            } catch (unused2: Exception) {
            }
        }
        return false
    }

    @JvmStatic
    fun isHitomiReader(url: String?, host: String?): Boolean {
        val length = if (host == null) 0 else host.length
        if (host == null || length == 0 || !host.endsWith("hitomi.la")) {
            return false
        }
        return url!!.startsWith("/reader/", length)
    }

    @JvmStatic
    fun isFullScreenOff(): Boolean {
        if (MainConst.isApi30Insets) {
            return !BrowserSettings.showStatus.value
        }
        return false
    }

    @JvmStatic
    fun deleteRecursive(file: File?) {
        try {
            if (!file!!.isDirectory()) {
                if (file.exists()) {
                    file.delete()
                    return
                }
                return
            }
            val fileArrListFiles = file.listFiles()
            if (fileArrListFiles != null && fileArrListFiles.size > 0) {
                for (file2 in fileArrListFiles) {
                    if (file2.isDirectory()) {
                        deleteRecursive(file2)
                    } else if (file2.exists()) {
                        file2.delete()
                    }
                }
            }
            if (file.exists()) {
                file.delete()
            }
        } catch (unused: Exception) {
        }
    }

    @JvmStatic
    fun darkStyleJs(pageUrl: String?, darkWeb: Boolean, lightNavi: Boolean): String? {
        if (!needsTextZoom(pageUrl)) {
            return null
        }
        if (darkWeb) {
            return (StringBuilder("if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='img{-webkit-filter:brightness(")).append(BrowserSettings.imgBright.value).append("%);}';document.head.appendChild(ele);}}").toString()
        }
        if (lightNavi) {
            return "var edk=document.getElementById('sb_dark_style');if(edk){document.head.removeChild(edk);}"
        }
        return null
    }

    @JvmStatic
    fun githubRawToRepo(path: String?): String? {
        var parts: Array<String>? = null
        val pathText: String?
        val length = if (path == null || path.length <= 34 || !path.startsWith("https://raw.githubusercontent.com/")) 0 else (path.substring(
                34
            ).split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                .also { parts = it }).size
        if (path == null || path.length <= 34 || !path.startsWith("https://raw.githubusercontent.com/") || length < 4) {
            return null
        }
        val user = parts!![0]
        val repo = parts[1]
        var index = 3
        if (length >= 5 && parts[2] == "refs" && parts[3] == "heads") {
            pathText = parts[4]
            index = 5
        } else {
            pathText = parts[2]
        }
        val sb = StringBuilder()
        while (index < length) {
            sb.append(parts[index])
            if (index < length - 1) {
                sb.append("/")
            }
            index++
        }
        val url = sb.toString()
        if (url.length == 0) {
            return null
        }
        return (StringBuilder("https://cdn.jsdelivr.net/gh/").append(user).append("/").append(repo).append("@")).append(pathText).append("/").append(url).toString()
    }

    @JvmStatic
    fun dispositionFileName(disposition: String?): String? {
        var decodedName: String? = null
        val length = if (disposition == null) 0 else disposition.length
        val filenameIndex = if (disposition == null || length < 10) 0 else disposition.lowercase()
                .indexOf("filename=")
        val nameStart = if (disposition == null || length < 10 || filenameIndex == -1) 0 else (filenameIndex + 9)
        if (disposition == null || length < 10 || filenameIndex == -1 || nameStart >= length) {
            return null
        }
        val quoteStart = disposition.indexOf(34.toChar(), nameStart)
        val index = if (quoteStart >= nameStart) (quoteStart + 1) else 0
        val quoteEnd = if (quoteStart >= nameStart && index < length) disposition.indexOf(34.toChar(), index) else 0
        if (quoteStart >= nameStart && index < length && quoteEnd > index && quoteEnd < length) {
            decodedName = disposition.substring(index, quoteEnd)
        }
        if (TextUtils.isEmpty(decodedName)) {
            decodedName = disposition.substring(nameStart)
        }
        try {
            decodedName = URLDecoder.decode(decodedName, "UTF-8")
            return decodedName.trim { it <= ' ' }
        } catch (unused: Exception) {
            return decodedName
        }
    }

    @JvmStatic
    fun resourceBitmap(context: Context?, baselineErrorGray: Int): Bitmap? {
        try {
            val url = baselineErrorGray.toString()
            val bitmapK = cachedIcon(url)
            if (isBitmapOk(bitmapK)) {
                return bitmapK
            }
            val bitmapD = BitmapUtil.decodeResource(context!!.getResources(), baselineErrorGray)
            cacheIcon(url, bitmapD)
            return bitmapD
        } catch (unused: Exception) {
            return null
        }
    }

    @JvmStatic
    fun startSoulActivity(activity: Activity?, intent: Intent?) {
        if (activity == null) {
            return
        }
        intent!!.putExtra("EXTRA_SOUL", true)
        if (activity !is WebViewActivity) {
            activity.startActivity(intent)
            return
        }
        val webViewActivity = activity
        webViewActivity.dialogsState.miscBusy = true
        webViewActivity.startActivity(intent)
        webViewActivity.dialogsState.miscBusy = false
    }

    @JvmStatic
    fun isInRect(rectF: RectF?, x: Int, y: Int): Boolean {
        if (rectF == null) {
            return false
        }
        val xF = x.toFloat()
        if (xF < rectF.left || xF >= rectF.right) {
            return false
        }
        val yF = y.toFloat()
        return yF >= rectF.top && yF < rectF.bottom
    }

    @JvmStatic
    fun restartLater(context: Context?) {
        if (context != null) {
            try {
                Handler(Looper.getMainLooper()).post(OutOfMemoryToastTask(context))
            } catch (unused: Exception) {
            }
        }
    }

    @JvmStatic
    fun setWindowStatusColor(window: Window?, topBarColor: Int) {
        if (Build.VERSION.SDK_INT >= 35 || window == null) {
            return
        }
        window.setStatusBarColor(topBarColor)
    }

    @JvmStatic
    fun noOp(segmentDir: String?) {
        if (TextUtils.isEmpty(segmentDir)) {
            return
        }
        deleteRecursive(File(segmentDir!!))
    }

    @JvmStatic
    fun mediaNameFor(pageUrl: String?, pageHost: String?, darkWeb: Boolean, secret: Boolean): String? {
        if (lacksAlgorithmicDark()) {
            if ((!darkWeb && !secret) || TextUtils.isEmpty(pageUrl)) {
                return null
            }
            if (isImagePath(pageUrl)) {
                return darkStyleJs(pageUrl, darkWeb, secret)
            }
            if (pageUrl!!.endsWith(".mp4") || pageUrl.endsWith(".webm") || pageUrl.endsWith(".m3u8") || pageUrl.endsWith(
                    ".mov"
                ) || pageUrl.endsWith(".flv") || pageUrl.endsWith(".aac") || pageUrl.endsWith(".mp3")
            ) {
                return darkStyleJs(pageUrl, darkWeb, secret)
            }
            if (TextUtils.isEmpty(pageHost) || pageHost!!.endsWith("giphy.com") || pageHost.endsWith("pornhub.com") || pageHost.endsWith(
                    "imdb.com"
                ) || pageHost.endsWith("wowhead.com")
            ) {
                return darkStyleJs(pageUrl, darkWeb, secret)
            }
            if (!darkWeb) {
                return "var edk=document.getElementById('sb_dark_style');if(edk){document.head.removeChild(edk);}"
            }
            if (isYouTubeHost(pageHost)) {
                if (!needsTextZoom(pageUrl)) {
                    return "if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='html,body{background:black !important;color:white !important;}div[class*=\"metadata\"],div[class*=\"comment\"],*[class=\"section\"]{color:white;}div[class*=\"text\"],div[class*=\"cbox\"],span[class*=\"title\"],button[class*=\"comment\"],button[class*=\"button\"]{color:#aaa;}ytm-badge,div[class*=\"comment-simplebox-input\"]{-webkit-filter:invert(1)hue-rotate(180deg);}';document.head.appendChild(ele);}}"
                }
                return (StringBuilder("if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='html,body{background:black !important;color:white !important;}div[class*=\"metadata\"],div[class*=\"comment\"],*[class=\"section\"]{color:white;}div[class*=\"text\"],div[class*=\"cbox\"],span[class*=\"title\"],button[class*=\"comment\"],button[class*=\"button\"]{color:#aaa;}ytm-badge,div[class*=\"comment-simplebox-input\"]{-webkit-filter:invert(1)hue-rotate(180deg);}img{-webkit-filter:brightness(")).append(BrowserSettings.imgBright.value).append("%);}';document.head.appendChild(ele);}}").toString()
            }
            if (!needsTextZoom(pageUrl)) {
                return "if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='html,img,video,iframe,canvas,*[style*=\"url(\"]{-webkit-filter:invert(1)hue-rotate(180deg);}';document.head.appendChild(ele);}}"
            }
            return (StringBuilder("if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='html,img,video,iframe,canvas,*[style*=\"url(\"]{-webkit-filter:invert(1)hue-rotate(180deg);}img{-webkit-filter:brightness(")).append(BrowserSettings.imgBright.value).append("%);}';document.head.appendChild(ele);}}").toString()
        }
        if (TextUtils.isEmpty(pageHost)) {
            return null
        }
        if (!TextUtils.isEmpty(pageUrl) && pageUrl!!.startsWith("https://www.amazon.")) {
            if (!darkWeb) {
                if (secret) {
                    return "var edk=document.getElementById('sb_dark_style');if(edk){document.head.removeChild(edk);}"
                }
                return null
            }
            if (!needsTextZoom(pageUrl)) {
                return "if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='span[class=\"a-button-inner\"]{background:black !important;color:white !important;}';document.head.appendChild(ele);}}"
            }
            return (StringBuilder("if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='span[class=\"a-button-inner\"]{background:black !important;color:white !important;}img{-webkit-filter:brightness(")).append(BrowserSettings.imgBright.value).append("%);}';document.head.appendChild(ele);}}").toString()
        }
        if (pageHost!!.endsWith("fandom.com")) {
            if (!darkWeb) {
                if (secret) {
                    return "var edk=document.getElementById('sb_dark_style');if(edk){document.head.removeChild(edk);}"
                }
                return null
            }
            if (!needsTextZoom(pageUrl)) {
                return "if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='body{background:black !important;color:white !important;}p{color:white !important;}';document.head.appendChild(ele);}}"
            }
            return (StringBuilder("if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='body{background:black !important;color:white !important;}p{color:white !important;}img{-webkit-filter:brightness(")).append(BrowserSettings.imgBright.value).append("%);}';document.head.appendChild(ele);}}").toString()
        }
        if (!pageHost.endsWith("www.elektra.com.mx")) {
            return darkStyleJs(pageUrl, darkWeb, secret)
        }
        if (!darkWeb) {
            if (secret) {
                return "var edk=document.getElementById('sb_dark_style');if(edk){document.head.removeChild(edk);}"
            }
            return null
        }
        if (!needsTextZoom(pageUrl)) {
            return "if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='div[class*=\"imageContainer\"]{-webkit-filter:invert(1)hue-rotate(180deg);}';document.head.appendChild(ele);}}"
        }
        return (StringBuilder("if(document.head){if(!document.getElementById('sb_dark_style')){var ele=document.createElement('style');ele.id='sb_dark_style';ele.innerText='div[class*=\"imageContainer\"]{-webkit-filter:invert(1)hue-rotate(180deg);}img{-webkit-filter:brightness(")).append(BrowserSettings.imgBright.value).append("%);}';document.head.appendChild(ele);}}").toString()
    }

    @JvmStatic
    fun glideUrlFor(context: Context?, imageUrl: String?, referer: String?): GlideUrl? {
        var referer = referer
        val cookie: String?
        var refererValue: String? = null
        if (TextUtils.isEmpty(imageUrl)) {
            return null
        }
        if (isGoogleSearch(referer)) {
            cookie = null
        } else {
            if (isGoogleUserContent(imageUrl)) {
                referer = imageUrl
            }
            val refererCookie = cookieFor(context, referer)
            if (TextUtils.isEmpty(referer)) {
                referer = imageUrl
            }
            cookie = refererCookie
            refererValue = referer
        }
        val builder = LazyHeaders.Builder()
        builder.addHeader("Accept", acceptHeaderFor(imageUrl))
        builder.addHeader("Accept-Encoding", "identity")
        builder.addHeader("User-Agent", userAgentPref())
        if (!TextUtils.isEmpty(refererValue)) {
            builder.addHeader("Referer", refererValue!!)
        }
        if (!TextUtils.isEmpty(cookie)) {
            builder.addHeader("Cookie", cookie!!)
        }
        return GlideUrl(imageUrl, builder.build())
    }

    @JvmStatic
    fun areaScriptJs(areaToolbarOn: Boolean): String? {
        var areaJs: String?
        val sb = StringBuilder()
        for (index in 0..91) {
            if (index != 90) {
                areaJs = getAreaJs(index)
                if (TextUtils.isEmpty(areaJs)) {
                    return null
                }
                sb.append(areaJs)
            } else {
                sb.append(if (ReadSettings.areaMulti.value) "true;" else "false;")
                if (areaToolbarOn) {
                    areaJs = getAreaJs(index)
                    if (TextUtils.isEmpty(areaJs)) {
                        return null
                    }
                    sb.append(areaJs)
                } else {
                    continue
                }
            }
        }
        return sb.toString()
    }

    @JvmStatic
    fun svgToBitmap(inputStream: InputStream?): Bitmap? {
        try {
            return drawableToBitmap(0, PictureDrawable(SVG.getFromInputStream(inputStream).renderToPicture()))
        } catch (unused: Exception) {
            return null
        } catch (unused: OutOfMemoryError) {
            return null
        }
    }

    @JvmStatic
    fun newMediaUri(requestCode: Int, mainActivity: android.app.Activity?, secret: Boolean): Uri? {
        if (mainActivity != null) {
            try {
                val cachePath = newCacheFilePath(mainActivity)
                if (!TextUtils.isEmpty(cachePath)) {
                    val sb = StringBuilder()
                    sb.append(cachePath)
                    sb.append(if (secret) ".mp4" else ".jpg")
                    val uriO = fileProviderUri(mainActivity, sb.toString())
                    val intent =
                        Intent(if (secret) "android.media.action.VIDEO_CAPTURE" else "android.media.action.IMAGE_CAPTURE")
                    intent.putExtra("output", uriO)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    (mainActivity as? ActivityHost)?.launchExternalForResult(intent, requestCode)
                    return uriO
                }
            } catch (unused: Exception) {
                showToast(mainActivity, R.string.not_supported)
                return null
            }
        }
        return null
    }

    @JvmStatic
    fun isDarkBars(onStartPage: Boolean): Boolean {
        return onStartPage && isDarkMode()
    }

    @JvmStatic
    fun parseFloatSafe(text: String?): kotlin.Float {
        if (TextUtils.isEmpty(text)) {
            return 0.0f
        }
        try {
            return text!!.toFloat()
        } catch (unused: Exception) {
            return 0.0f
        }
    }

    @JvmStatic
    fun applyBarAppearance(
        window: Window?,
        view: View?,
        windowInsetsControllerCompat: WindowInsetsControllerCompat?,
        isDark: Boolean,
        isWebDark: Boolean
    ) {
        var view = view
        var windowInsetsControllerCompat = windowInsetsControllerCompat
        if (window != null) {
            val index = Build.VERSION.SDK_INT
            if (index < 30) {
                if (view == null) {
                    view = window.getDecorView()
                }
                val systemUiVisibility = view.getSystemUiVisibility()
                var uiFlags = if (isDark) systemUiVisibility and (-8193) else systemUiVisibility or 8192
                if (index >= 26) {
                    uiFlags = if (isWebDark) uiFlags and (-17) else uiFlags or 16
                }
                view.setSystemUiVisibility(uiFlags)
                return
            }
            val lightStatus = !isDark
            val lightNavi = !isWebDark
            if (windowInsetsControllerCompat == null && (insetsController(
                    window,
                    view
                ).also { windowInsetsControllerCompat = it }) == null
            ) {
                return
            }
            if (windowInsetsControllerCompat!!.isAppearanceLightStatusBars() != lightStatus) {
                windowInsetsControllerCompat.setAppearanceLightStatusBars(lightStatus)
            }
            if (windowInsetsControllerCompat.isAppearanceLightNavigationBars() != lightNavi) {
                windowInsetsControllerCompat.setAppearanceLightNavigationBars(lightNavi)
            }
        }
    }

    internal class ShowSecretNotiTask(val context: Context) : Runnable {
        // Tapping the notification, like its button, sends ACTION_SECRET_EXIT to the browser's own receiver, which only
        // switches secret mode off (no activity is started from it), so the broadcast content intent is intended.
        @android.annotation.SuppressLint("LaunchActivityFromNotification")
        override fun run() {
            val context = this.context
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(context)
            if (notificationManagerP == null) {
                return
            }
            val intent = Intent("ch.opumlabs.opumbrowser.ACTION_SECRET_EXIT")
            intent.setPackage(context.getPackageName())
            val broadcast = PendingIntent.getBroadcast(context, 5, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            val action = NotificationCompat.Action(
                R.drawable.outline_close_black_24, context.getString(
                    R.string.secret_exit
                ), broadcast
            )
            val builder = NotificationCompat.Builder(context, "SecretMode")
            builder.setSmallIcon(R.drawable.outline_secret_mode_white_24)
            builder.setContentTitle(context.getString(R.string.secret_exit))
            builder.setContentIntent(broadcast)
            builder.setPriority(1)
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET)
            builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_SECRET")
            builder.addAction(action)
            val notificationB = builder.build()
            notificationB.flags = notificationB.flags or 48
            notificationManagerP.createNotificationChannel(
                NotificationChannel(
                    "SecretMode", context.getString(
                        R.string.secret_mode
                    ), NotificationManager.IMPORTANCE_LOW
                )
            )
            notificationManagerP.notify(2147483642, notificationB)
        }
    }

    internal class RequestFocusTask(val view: View) : Runnable {
        override fun run() {
            this.view.requestFocus()
        }
    }

    internal class SetActivityOrientationTask(val activity: Activity?, val orientation: Int) :
        Runnable {
        override fun run() {
            setActivityOrientation(this.activity, this.orientation)
        }
    }

    internal class TopRound16Outline : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (view == null || outline == null) {
                return
            }
            val width = view.getWidth()
            val height = view.getHeight()
            val dp16Px: Int = MainApp.Companion.dp16
            outline.setRoundRect(0, 0, width, height + dp16Px, dp16Px.toFloat())
        }
    }

    internal class ClipboardTask(val context: Context?,
                                    val messageRes: Int,
                                    val label: String?,
                                    val clipText: String?
    ) : Runnable {

        internal class CopiedToastStep(val context: Context?,
                                       val messageRes: Int
        ) : Runnable {
            override fun run() {
                showToast(this.context, this.messageRes)
            }
        }

        override fun run() {
            val context = this.context
            try {
                MainApp.Companion.clipboard(context)!!.setPrimaryClip(ClipData.newPlainText(this.label, this.clipText))
                val messageRes = this.messageRes
                // Android 13+ shows its own clipboard confirmation: no second message on top of it (2026-11-20)
                if (messageRes != 0 && android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.TIRAMISU) {
                    Handler(Looper.getMainLooper()).post(CopiedToastStep(context, messageRes))
                }
            } catch (unused: Exception) {
            } catch (unused: OutOfMemoryError) {
            }
        }
    }

    internal class MessageToastTask(val context: Context?,
                                    val messageRes: Int
    ) : Runnable {
        override fun run() {
            showToast(this.context, this.messageRes)
        }
    }

    internal class FlushCookiesCallback(val context: Context?) : ValueCallback<Boolean?> {
        override fun onReceiveValue(bool: Boolean?) {
            flushCookies(this.context, true)
        }
    }

    internal class EditTextFocusChange(val editText: EditText) : OnFocusChangeListener {

        internal class CheckValueStep(val editText: EditText?) : Runnable {
            override fun run() {
                val editText = this.editText
                val fileName = editTextValue(editText, false)
                if (TextUtils.isEmpty(fileName)) {
                    return
                }
                val dotIndex = fileName!!.lastIndexOf(46.toChar())
                if (dotIndex == -1) {
                    editText!!.selectAll()
                } else if ((fileName.length - dotIndex) - 1 > 4) {
                    editText!!.selectAll()
                } else {
                    if (dotIndex == 0) {
                        return
                    }
                    editText!!.setSelection(0, dotIndex)
                }
            }
        }

        override fun onFocusChange(view: View?, hasFocus: Boolean) {
            if (hasFocus) {
                val editText = this.editText
                editText.postDelayed(CheckValueStep(editText), 100L)
            }
        }
    }

    internal class RestartActivityTask(val context: Context) : Runnable {
        override fun run() {
            restartActivity(this.context, null, false)
        }
    }

    internal class CancelNotificationsTask(val context: Context?) : Runnable {
        override fun run() {
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(this.context)
            if (notificationManagerP != null) {
                notificationManagerP.cancel(2147483642)
            }
        }
    }

    internal class DestroyTask(val webView: WebView?) : Runnable {
        override fun run() {
            val webView = this.webView
            if (webView == null) {
                return
            }
            try {
                webView.destroy()
            } catch (unused: Exception) {
            }
        }
    }

    internal class EvaluateJavascriptTask(val webView: WebView, val script: String) :
        Runnable {
        override fun run() {
            this.webView.evaluateJavascript(this.script, null)
        }
    }

    internal class HideAfterAnimation(val view: View) : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            val view = this.view
            val tag = view.getTag()
            view.clearAnimation()
            if (tag != null && (tag is Boolean) && tag) {
                view.setVisibility(View.GONE)
            }
        }

        override fun onAnimationRepeat(animation: Animation?) {
        }

        override fun onAnimationStart(animation: Animation?) {
        }
    }

    internal class SaveBitmapPngTask(val context: Context?,
                                    val bitmap: Bitmap?,
                                    val path: String?
    ) : Runnable {
        override fun run() {
            saveBitmapPng(this.context, this.bitmap, this.path)
        }
    }

    internal class OutOfMemoryToastTask(val context: Context?) : Runnable {
        override fun run() {
            showToast(this.context, R.string.out_of_memory)
            MainApp.Companion.lastToast = null
        }
    }

    internal class NotificationsTask(val context: Context,
                                   val title: String?,
                                   val playing: Boolean,
                                   val paused: Boolean,
                                   val bitmap: Bitmap?
    ) : Runnable {
        override fun run() {
            val notificationManager: NotificationManager?
            val builder: NotificationCompat.Builder
            val manager: NotificationManager?
            val rewindIcon: Int
            val forwardIcon: Int
            val context = this.context
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(context)
            if (notificationManagerP != null) {
                var title = this.title
                val titleEmpty = TextUtils.isEmpty(title)
                val playing = this.playing
                var subTitle: String? = null
                if (titleEmpty) {
                    title = context.getString(R.string.back_play)
                } else if (!playing) {
                    subTitle = context.getString(R.string.back_play)
                }
                val intentL = browserIntent(context)
                intentL.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                val activity = PendingIntent.getActivity(context, 6, intentL, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                val packageName = context.getPackageName()
                if (playing) {
                    val playIcon =
                        if (this.paused) R.drawable.baseline_play_arrow_white_24 else R.drawable.baseline_pause_white_24
                    if (isRtl(context)) {
                        rewindIcon = R.drawable.baseline_fast_forward_white_24
                        forwardIcon = R.drawable.baseline_fast_rewind_white_24
                    } else {
                        rewindIcon = R.drawable.baseline_fast_rewind_white_24
                        forwardIcon = R.drawable.baseline_fast_forward_white_24
                    }
                    val outlineCloseWhite = R.drawable.outline_close_white_24
                    val intent = Intent("ch.opumlabs.opumbrowser.ACTION_PIP_FRWD")
                    intent.setPackage(packageName)
                    val broadcast = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val playIntent = Intent("ch.opumlabs.opumbrowser.ACTION_PIP_PLAY")
                    playIntent.setPackage(packageName)
                    notificationManager = notificationManagerP
                    val playBroadcast = PendingIntent.getBroadcast(context, 1, playIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val ffwdIntent = Intent("ch.opumlabs.opumbrowser.ACTION_PIP_FFWD")
                    ffwdIntent.setPackage(packageName)
                    val ffwdBroadcast = PendingIntent.getBroadcast(context, 2, ffwdIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val stopIntent = Intent("ch.opumlabs.opumbrowser.ACTION_BACK_STOP")
                    stopIntent.setPackage(packageName)
                    val stopBroadcast = PendingIntent.getBroadcast(context, 3, stopIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    val remoteViews = RemoteViews(packageName, R.layout.noti_player)
                    remoteViews.setTextViewText(R.id.noti_title, title)
                    remoteViews.setImageViewResource(R.id.icon_frwd, rewindIcon)
                    remoteViews.setImageViewResource(R.id.icon_play, playIcon)
                    remoteViews.setContentDescription(R.id.icon_play, context.getString(if (this.paused) R.string.play else R.string.pause))
                    remoteViews.setImageViewResource(R.id.icon_ffwd, forwardIcon)
                    remoteViews.setImageViewResource(R.id.icon_stop, outlineCloseWhite)
                    remoteViews.setOnClickPendingIntent(R.id.icon_frwd, broadcast)
                    remoteViews.setOnClickPendingIntent(R.id.icon_play, playBroadcast)
                    remoteViews.setOnClickPendingIntent(R.id.icon_ffwd, ffwdBroadcast)
                    remoteViews.setOnClickPendingIntent(R.id.icon_stop, stopBroadcast)
                    builder = NotificationCompat.Builder(context, "BackPlayMode")
                    builder.setSmallIcon(R.drawable.baseline_play_arrow_white_24)
                    builder.setContentTitle(title)
                    builder.setContentIntent(activity)
                    builder.setPriority(1)
                    builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_BACKPLAY")
                    builder.setContent(remoteViews)
                    builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
                } else {
                    notificationManager = notificationManagerP
                    val intent = Intent("ch.opumlabs.opumbrowser.ACTION_BACK_STOP")
                    intent.setPackage(packageName)
                    val action = NotificationCompat.Action(
                        R.drawable.outline_close_white_24, context.getString(
                            R.string.stop
                        ), PendingIntent.getBroadcast(context, 3, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
                    )
                    builder = NotificationCompat.Builder(context, "BackPlayMode")
                    builder.setSmallIcon(R.drawable.baseline_play_arrow_white_24)
                    builder.setContentTitle(title)
                    builder.setContentIntent(activity)
                    builder.setPriority(1)
                    builder.setGroup("ch.opumlabs.opumbrowser.NOTI_GROUP_BACKPLAY")
                    builder.addAction(action)
                    if (!TextUtils.isEmpty(subTitle)) {
                        builder.setContentText(subTitle)
                    }
                }
                val bitmap = this.bitmap
                if (isBitmapOk(bitmap)) {
                    builder.setLargeIcon(bitmap)
                }
                val notificationB = builder.build()
                notificationB.flags = notificationB.flags or 48
                manager = notificationManager
                manager.createNotificationChannel(
                    NotificationChannel(
                        "BackPlayMode", context.getString(
                            R.string.back_play
                        ), NotificationManager.IMPORTANCE_LOW
                    )
                )
                manager.notify(2147483641, notificationB)
                if (TtsSettings.checkBack.value) {
                    return
                }
                TtsSettings.editLocal { set(TtsSettings.checkBack, true) }
                putBoolNow(12, context, "mCheckBack", true)
            }
        }
    }

    internal class ClearHistoryNotiTask(val context: Context?) : Runnable {
        override fun run() {
            val context = this.context
            val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(context)
            if (notificationManagerP != null) {
                notificationManagerP.cancel(2147483641)
            }
            if (TtsSettings.checkBack.value) {
                TtsSettings.editLocal { set(TtsSettings.checkBack, false) }
                putBoolNow(12, context, "mCheckBack", false)
            }
        }
    }

    internal class RefreshZoneNotiTask(val context: Context?) : Runnable {
        override fun run() {
            val context = this.context
            if (MainUtil.isAudioPlaying(context!!)) {
                val notificationManagerP: NotificationManager? = MainApp.Companion.notifications(context)
                if (notificationManagerP != null) {
                    notificationManagerP.cancel(2147483641)
                }
                if (TtsSettings.checkBack.value) {
                    TtsSettings.editLocal { set(TtsSettings.checkBack, false) }
                    putBoolNow(12, context, "mCheckBack", false)
                }
                try {
                    val intent = Intent("ch.opumlabs.opumbrowser.ACTION_BACK_STOP")
                    intent.setPackage(context.getPackageName())
                    context.sendBroadcast(intent)
                } catch (unused: Exception) {
                }
            }
        }
    }

    internal class InputMethodsTask(val context: Context?,
                                   val view: View?
    ) : Runnable {
        override fun run() {
            val context = this.context
            MainApp.Companion.inputMethods(context)
            val view: View? = if (context == null) null else this.view
            if (context == null || view == null) {
                return
            }
            MainApp.Companion.postToMain(context, ShowKeyboardTask(context, view))
        }
    }

    internal class ShowKeyboardTask(val context: Context?,
                                   val view: View?
    ) : Runnable {
        override fun run() {
            try {
                MainApp.Companion.inputMethods(this.context)!!.showSoftInput(
                    this.view,
                    if (Build.VERSION.SDK_INT < 37) InputMethodManager.SHOW_IMPLICIT else 0
                )
            } catch (unused: Exception) {
            }
        }
    }

    internal class HideKeyboardLaterTask(val context: Context?,
                                   val view: View?
    ) : Runnable {
        override fun run() {
            val context = this.context
            MainApp.Companion.inputMethods(context)
            val view: View? = if (context == null) null else this.view
            if (context == null || view == null) {
                return
            }
            MainApp.Companion.postToMain(context, MainUtil.HideKeyboardTask(context, view))
        }
    }

    internal class HideKeyboardTask(val context: Context?,
                                   val view: View
    ) : Runnable {
        override fun run() {
            try {
                MainApp.Companion.inputMethods(this.context)!!.hideSoftInputFromWindow(
                    this.view.getWindowToken(),
                    if (Build.VERSION.SDK_INT < 37) InputMethodManager.HIDE_NOT_ALWAYS else 0
                )
            } catch (unused: Exception) {
            }
        }
    }

    interface LoopCancelListener {
        val isCancelled: Boolean
    }

    class SizeItem {
        constructor() : super()

        @JvmField
        var width: Int = 0
        @JvmField
        var height: Int = 0

        constructor(i: Int, i2: Int) {
            this.width = i
            this.height = i2
        }
    }

    class SortChildData(var sortType: Int, var reversed: Boolean) : Comparator<ChildItem?> {
        override fun compare(first: ChildItem?, second: ChildItem?): Int {
            val childItem = first
            val other = second
            if (childItem == null) {
                if (other == null) {
                    return 0
                }
            } else {
                if (other == null) {
                    return -1
                }
                val isDir = childItem.isDir
                val reversed = this.reversed
                if ((!isDir && !other.isDir) || !ListSettings.bookWebFtop.value) {
                    val statusNow = childItem.status
                    val status = if (statusNow == 1) 0 else other.status
                    if (statusNow == 1 || status == 1) {
                        if (other.status != 1) {
                            return -1
                        }
                        if (statusNow == 1) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    } else if (statusNow == 2 || status == 2) {
                        if (status != 2) {
                            return -1
                        }
                        if (statusNow == 2) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    } else {
                        if (statusNow != 4 && status != 4) {
                            val sortType = this.sortType
                            if (sortType != 0) {
                                val bySortName = compareStrings(childItem.sortName, other.sortName, reversed)
                                if (bySortName != 0) {
                                    return bySortName
                                }
                                val byGroup = if ((sortType == 1 || sortType == 2)) compareStringsIgnoreCase(
                                        childItem.groupKey,
                                        other.groupKey,
                                        reversed
                                    ) else 0
                                if ((sortType == 1 || sortType == 2) && byGroup != 0) {
                                    return byGroup
                                }
                            }
                            val byType = compareLongs(childItem.listType.toLong(), other.listType.toLong(), reversed)
                            if (byType != 0) {
                                return byType
                            }
                            val byExt = compareStringsIgnoreCase(childItem.sortExt, other.sortExt, reversed)
                            if (byExt != 0) {
                                return byExt
                            }
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            if (byName != 0) {
                                return byName
                            }
                            val byPath = compareStringsIgnoreCase(childItem.path, other.path, reversed)
                            return if (byPath != 0) byPath else compareLongs(childItem.time, other.time, reversed)
                        }
                        if (status != 4) {
                            return -1
                        }
                        if (statusNow == 4) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    }
                } else {
                    if (!other.isDir) {
                        return -1
                    }
                    if (isDir) {
                        val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                        if (byName != 0) {
                            return byName
                        }
                        val byPath = compareStringsIgnoreCase(childItem.path, other.path, reversed)
                        return if (byPath != 0) byPath else compareLongs(childItem.time, other.time, reversed)
                    }
                }
            }
            return 1
        }
    }

    class SortChildExt(var sortType: Int, var reversed: Boolean) : Comparator<ChildItem?> {
        override fun compare(first: ChildItem?, second: ChildItem?): Int {
            val childItem = first
            val other = second
            if (childItem == null) {
                if (other == null) {
                    return 0
                }
            } else {
                if (other == null) {
                    return -1
                }
                val isDir = childItem.isDir
                val reversed = this.reversed
                if ((!isDir && !other.isDir) || !ListSettings.bookWebFtop.value) {
                    val statusNow = childItem.status
                    val status = if (statusNow == 1) 0 else other.status
                    if (statusNow == 1 || status == 1) {
                        if (other.status != 1) {
                            return -1
                        }
                        if (statusNow == 1) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    } else if (statusNow == 2 || status == 2) {
                        if (status != 2) {
                            return -1
                        }
                        if (statusNow == 2) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    } else {
                        if (statusNow != 4 && status != 4) {
                            val sortType = this.sortType
                            if (sortType != 0) {
                                val bySortName = compareStrings(childItem.sortName, other.sortName, reversed)
                                if (bySortName != 0) {
                                    return bySortName
                                }
                                val byGroup = if ((sortType == 1 || sortType == 2)) compareStringsIgnoreCase(
                                        childItem.groupKey,
                                        other.groupKey,
                                        reversed
                                    ) else 0
                                if ((sortType == 1 || sortType == 2) && byGroup != 0) {
                                    return byGroup
                                }
                            }
                            val byExt = compareStringsIgnoreCase(childItem.sortExt, other.sortExt, reversed)
                            if (byExt != 0) {
                                return byExt
                            }
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            if (byName != 0) {
                                return byName
                            }
                            val byPath = compareStringsIgnoreCase(childItem.path, other.path, reversed)
                            return if (byPath != 0) byPath else compareLongs(childItem.time, other.time, reversed)
                        }
                        if (status != 4) {
                            return -1
                        }
                        if (statusNow == 4) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    }
                } else {
                    if (!other.isDir) {
                        return -1
                    }
                    if (isDir) {
                        val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                        if (byName != 0) {
                            return byName
                        }
                        val byPath = compareStringsIgnoreCase(childItem.path, other.path, reversed)
                        return if (byPath != 0) byPath else compareLongs(childItem.time, other.time, reversed)
                    }
                }
            }
            return 1
        }
    }

    class SortChildName(var sortType: Int, var reversed: Boolean) : Comparator<ChildItem?> {
        override fun compare(first: ChildItem?, second: ChildItem?): Int {
            val childItem = first
            val other = second
            if (childItem == null) {
                if (other == null) {
                    return 0
                }
            } else {
                if (other == null) {
                    return -1
                }
                val isDir = childItem.isDir
                val reversed = this.reversed
                if ((!isDir && !other.isDir) || !ListSettings.bookWebFtop.value) {
                    val statusNow = childItem.status
                    val status = if (statusNow == 1) 0 else other.status
                    if (statusNow == 1 || status == 1) {
                        if (other.status != 1) {
                            return -1
                        }
                        if (statusNow == 1) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    } else if (statusNow == 2 || status == 2) {
                        if (status != 2) {
                            return -1
                        }
                        if (statusNow == 2) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    } else {
                        if (statusNow != 4 && status != 4) {
                            val sortType = this.sortType
                            if (sortType != 0) {
                                val bySortName = compareStrings(childItem.sortName, other.sortName, reversed)
                                if (bySortName != 0) {
                                    return bySortName
                                }
                                val byGroup = if ((sortType == 1 || sortType == 2)) compareStringsIgnoreCase(
                                        childItem.groupKey,
                                        other.groupKey,
                                        reversed
                                    ) else 0
                                if ((sortType == 1 || sortType == 2) && byGroup != 0) {
                                    return byGroup
                                }
                            }
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            if (byName != 0) {
                                return byName
                            }
                            val byExt = compareStringsIgnoreCase(childItem.sortExt, other.sortExt, reversed)
                            if (byExt != 0) {
                                return byExt
                            }
                            val byPath = compareStringsIgnoreCase(childItem.path, other.path, reversed)
                            if (byPath != 0) {
                                return byPath
                            }
                            val byOrder = compareLongs(childItem.order, other.order, reversed)
                            return if (byOrder != 0) byOrder else compareLongs(childItem.time, other.time, reversed)
                        }
                        if (status != 4) {
                            return -1
                        }
                        if (statusNow == 4) {
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    }
                } else {
                    if (!other.isDir) {
                        return -1
                    }
                    if (isDir) {
                        val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                        if (byName != 0) {
                            return byName
                        }
                        val byPath = compareStringsIgnoreCase(childItem.path, other.path, reversed)
                        return if (byPath != 0) byPath else compareLongs(childItem.time, other.time, reversed)
                    }
                }
            }
            return 1
        }
    }

    class SortChildSize(var sortType: Int, var reversed: Boolean) : Comparator<ChildItem?> {
        override fun compare(first: ChildItem?, second: ChildItem?): Int {
            val childItem = first
            val other = second
            if (childItem == null) {
                if (other == null) {
                    return 0
                }
            } else {
                if (other == null) {
                    return -1
                }
                val isDir = childItem.isDir
                val reversed = this.reversed
                if ((!isDir && !other.isDir) || !ListSettings.bookWebFtop.value) {
                    val statusNow = childItem.status
                    val status = if (statusNow == 1) 0 else other.status
                    if (statusNow == 1 || status == 1) {
                        if (other.status != 1) {
                            return -1
                        }
                        if (statusNow == 1) {
                            val bySize = compareLongs(childItem.size, other.size, reversed)
                            if (bySize != 0) {
                                return bySize
                            }
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    } else if (statusNow == 2 || status == 2) {
                        if (status != 2) {
                            return -1
                        }
                        if (statusNow == 2) {
                            val bySize = compareLongs(childItem.size, other.size, reversed)
                            if (bySize != 0) {
                                return bySize
                            }
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    } else {
                        if (statusNow != 4 && status != 4) {
                            val sortType = this.sortType
                            if (sortType != 0) {
                                val bySortName = compareStrings(childItem.sortName, other.sortName, reversed)
                                if (bySortName != 0) {
                                    return bySortName
                                }
                                val byGroup = if ((sortType == 1 || sortType == 2)) compareStringsIgnoreCase(
                                        childItem.groupKey,
                                        other.groupKey,
                                        reversed
                                    ) else 0
                                if ((sortType == 1 || sortType == 2) && byGroup != 0) {
                                    return byGroup
                                }
                            }
                            val bySize = compareLongs(childItem.size, other.size, reversed)
                            if (bySize != 0) {
                                return bySize
                            }
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            if (byName != 0) {
                                return byName
                            }
                            val byExt = compareStringsIgnoreCase(childItem.sortExt, other.sortExt, reversed)
                            if (byExt != 0) {
                                return byExt
                            }
                            val byPath = compareStringsIgnoreCase(childItem.path, other.path, reversed)
                            return if (byPath != 0) byPath else compareLongs(childItem.time, other.time, reversed)
                        }
                        if (status != 4) {
                            return -1
                        }
                        if (statusNow == 4) {
                            val bySize = compareLongs(childItem.size, other.size, reversed)
                            if (bySize != 0) {
                                return bySize
                            }
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            return if (byName != 0) byName else compareLongs(childItem.time, other.time, reversed)
                        }
                    }
                } else {
                    if (!other.isDir) {
                        return -1
                    }
                    if (isDir) {
                        val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                        if (byName != 0) {
                            return byName
                        }
                        val byPath = compareStringsIgnoreCase(childItem.path, other.path, reversed)
                        return if (byPath != 0) byPath else compareLongs(childItem.time, other.time, reversed)
                    }
                }
            }
            return 1
        }
    }

    class SortChildTime(var sortType: Int, var reversed: Boolean) : Comparator<ChildItem?> {
        override fun compare(first: ChildItem?, second: ChildItem?): Int {
            val childItem = first
            val other = second
            if (childItem == null) {
                if (other == null) {
                    return 0
                }
            } else {
                if (other == null) {
                    return -1
                }
                val isDir = childItem.isDir
                val reversed = this.reversed
                if ((!isDir && !other.isDir) || !ListSettings.bookWebFtop.value) {
                    val statusNow = childItem.status
                    val status = if (statusNow == 1) 0 else other.status
                    if (statusNow == 1 || status == 1) {
                        if (other.status != 1) {
                            return -1
                        }
                        if (statusNow == 1) {
                            val byTime = compareLongs(childItem.time, other.time, reversed)
                            return if (byTime != 0) byTime else compareStrings(childItem.lowerName, other.lowerName, reversed)
                        }
                    } else if (statusNow == 2 || status == 2) {
                        if (status != 2) {
                            return -1
                        }
                        if (statusNow == 2) {
                            val byTime = compareLongs(childItem.time, other.time, reversed)
                            return if (byTime != 0) byTime else compareStrings(childItem.lowerName, other.lowerName, reversed)
                        }
                    } else {
                        if (statusNow != 4 && status != 4) {
                            val sortType = this.sortType
                            if (sortType != 0) {
                                val bySortName = compareStrings(childItem.sortName, other.sortName, reversed)
                                if (bySortName != 0) {
                                    return bySortName
                                }
                                val byGroup = if ((sortType == 1 || sortType == 2)) compareStringsIgnoreCase(
                                        childItem.groupKey,
                                        other.groupKey,
                                        reversed
                                    ) else 0
                                if ((sortType == 1 || sortType == 2) && byGroup != 0) {
                                    return byGroup
                                }
                            }
                            val byTime = compareLongs(childItem.time, other.time, reversed)
                            if (byTime != 0) {
                                return byTime
                            }
                            val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                            if (byName != 0) {
                                return byName
                            }
                            val byExt = compareStringsIgnoreCase(childItem.sortExt, other.sortExt, reversed)
                            return if (byExt != 0) byExt else compareStringsIgnoreCase(childItem.path, other.path, reversed)
                        }
                        if (status != 4) {
                            return -1
                        }
                        if (statusNow == 4) {
                            val byTime = compareLongs(childItem.time, other.time, reversed)
                            return if (byTime != 0) byTime else compareStrings(childItem.lowerName, other.lowerName, reversed)
                        }
                    }
                } else {
                    if (!other.isDir) {
                        return -1
                    }
                    if (isDir) {
                        val byTime = compareLongs(childItem.time, other.time, reversed)
                        if (byTime != 0) {
                            return byTime
                        }
                        val byName = compareStrings(childItem.lowerName, other.lowerName, reversed)
                        return if (byName != 0) byName else compareStringsIgnoreCase(childItem.path, other.path, reversed)
                    }
                }
            }
            return 1
        }
    }

    class SortChildUser : Comparator<ChildItem?> {
        override fun compare(first: ChildItem?, second: ChildItem?): Int {
            val childItem = first
            val other = second
            if (childItem == null) {
                return if (other == null) 0 else 1
            }
            if (other == null) {
                return -1
            }
            val byOrder = compareLongs(childItem.order, other.order, false)
            if (byOrder != 0) {
                return byOrder
            }
            val byTime = compareLongs(childItem.time, other.time, false)
            if (byTime != 0) {
                return byTime
            }
            val byName = compareStrings(childItem.name, other.name, false)
            return if (byName != 0) byName else compareStringsIgnoreCase(childItem.path, other.path, false)
        }
    }

}
