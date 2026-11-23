package com.mycompany.app.dialog

import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.AlbumSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.ArrayList
import ch.opumlabs.opumbrowser.R as AppR
import android.animation.Animator
import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import android.text.TextUtils
import android.view.ActionMode
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MenuItem
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.Window
import android.webkit.JavascriptInterface
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.data.DataNews
import com.mycompany.app.data.DataNews.Companion.instance
import com.mycompany.app.data.DataTrans
import com.mycompany.app.data.DataTrans.Companion.instance
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.db.DbExport.exportCsv
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.db.book.DbBookAds.Companion.cached; import com.mycompany.app.db.book.DbBookAds.Companion.get
import com.mycompany.app.db.book.DbBookAgent.Companion.cached; import com.mycompany.app.db.book.DbBookAgent.Companion.get
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookBlock.Companion.cached; import com.mycompany.app.db.book.DbBookBlock.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookDown.Companion.cached; import com.mycompany.app.db.book.DbBookDown.Companion.clearDone
import com.mycompany.app.db.book.DbBookHistory.Companion.cached; import com.mycompany.app.db.book.DbBookHistory.Companion.exportFile
import com.mycompany.app.db.book.DbBookIcon.Companion.cached; import com.mycompany.app.db.book.DbBookIcon.Companion.exportFile
import com.mycompany.app.db.book.DbBookJava.Companion.cached; import com.mycompany.app.db.book.DbBookJava.Companion.get
import com.mycompany.app.db.book.DbBookLink.Companion.cached; import com.mycompany.app.db.book.DbBookLink.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookMemo.Companion.cached; import com.mycompany.app.db.book.DbBookMemo.Companion.get
import com.mycompany.app.db.book.DbBookOver.Companion.cached; import com.mycompany.app.db.book.DbBookOver.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPass.Companion.cached; import com.mycompany.app.db.book.DbBookPass.Companion.exportFile
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.db.book.DbBookPop.Companion.cached; import com.mycompany.app.db.book.DbBookPop.Companion.get
import com.mycompany.app.db.book.DbBookQuick.Companion.cached; import com.mycompany.app.db.book.DbBookQuick.Companion.exportFile
import com.mycompany.app.db.book.DbBookScript.Companion.cached; import com.mycompany.app.db.book.DbBookScript.Companion.deleteScript
import com.mycompany.app.db.book.DbBookSearch.Companion.cached; import com.mycompany.app.db.book.DbBookSearch.Companion.exportFile
import com.mycompany.app.db.book.DbBookTab.Companion.cached; import com.mycompany.app.db.book.DbBookTab.Companion.exportFile
import com.mycompany.app.db.book.DbBookTabOld.Companion.cached; import com.mycompany.app.db.book.DbBookTabOld.Companion.get
import com.mycompany.app.db.book.DbBookTmem.Companion.cached; import com.mycompany.app.db.book.DbBookTmem.Companion.exportFile
import com.mycompany.app.db.book.DbBookTrans.Companion.cached; import com.mycompany.app.db.book.DbBookTrans.Companion.exportFile
import com.mycompany.app.db.book.DbBookUser.Companion.cached; import com.mycompany.app.db.book.DbBookUser.Companion.exportFile
import com.mycompany.app.db.book.DbBookWeb.Companion.cached; import com.mycompany.app.db.book.DbBookWeb.Companion.exportFile
import com.mycompany.app.db.book.DbTabState.Companion.cached; import com.mycompany.app.db.book.DbTabState.Companion.exportFile
import com.mycompany.app.db.book.DbTabThumb.Companion.cached; import com.mycompany.app.db.book.DbTabThumb.Companion.exportFile
import com.mycompany.app.dialog.DialogConfirm.DialogConfListener
import com.mycompany.app.dialog.DialogDownPage.DownPageListener
import com.mycompany.app.dialog.DialogPrintPage.PathChangeListener
import com.mycompany.app.dialog.DialogSeekAudio.DialogSeekListener
import com.mycompany.app.dialog.DialogSetDesk.SetDeskListener
import com.mycompany.app.dialog.DialogTransLang.TransLangListener
import com.mycompany.app.down.DownParseDzen.parsePage
import com.mycompany.app.main.BitmapUtil.decodeBytes
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainNative
import com.mycompany.app.main.MainNative.backupKeyCache; import com.mycompany.app.main.MainNative.partsReady
import com.mycompany.app.main.MainTransText
import com.mycompany.app.main.MainUri.treeDocUri
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.isHttpUrl
import com.mycompany.app.main.MainUtil.parseColorSafe
import com.mycompany.app.main.MainUtil.onRenderGone
import com.mycompany.app.main.MainUtil.destroyWebView
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.isInsideView
import com.mycompany.app.main.MainUtil.evalConfirmJs
import com.mycompany.app.main.MainUtil.pointDistance
import com.mycompany.app.main.MainUtil.langPrefix
import com.mycompany.app.main.MainUtil.evalLongPressJs
import com.mycompany.app.main.MainUtil.evalJs
import com.mycompany.app.main.MainUtil.transLangFromCookie
import com.mycompany.app.main.MainUtil.applyRotationLock
import com.mycompany.app.main.MainUtil.checkTransClassJs
import com.mycompany.app.main.MainUtil.evalJsNow
import com.mycompany.app.main.MainUtil.classSelectorJs
import com.mycompany.app.main.MainUtil.injectTransJs
import com.mycompany.app.main.MainUtil.reloadWithDesktopMode
import com.mycompany.app.main.MainUtil.nativeJs14
import com.mycompany.app.main.MainUtil.transScriptJs
import com.mycompany.app.main.MainUtil.parseTransResult
import com.mycompany.app.main.MainUtil.parseLocale
import com.mycompany.app.main.MainUtil.isRtl
import com.mycompany.app.main.MainUtil.clearHandler
import com.mycompany.app.main.MainUtil.stripQuotes
import com.mycompany.app.main.MainUtil.hideDivsByClass
import com.mycompany.app.main.MainUtil.postRequestLayout
import com.mycompany.app.main.MainUtil.openFileInput
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.applyWindowBrightness
import com.mycompany.app.main.MainUtil.cancelToast
import com.mycompany.app.main.MainUtil.openWithApp
import com.mycompany.app.main.MainUtil.unescapeJavaText
import com.mycompany.app.main.MainUtil.readerFontPath
import com.mycompany.app.main.MainUtil.getTtsPartJs1
import com.mycompany.app.main.MainUtil.getTtsPartJs2
import com.mycompany.app.main.MainUtil.editorTextColor
import com.mycompany.app.main.MainUtil.clearWebCache
import com.mycompany.app.main.MainUtil.loadHtml
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.setNavBarColor
import com.mycompany.app.main.MainUtil.blendColors
import com.mycompany.app.main.MainUtil.clearCacheDirs
import com.mycompany.app.main.MainUtil.isFullScreenOff
import com.mycompany.app.main.MainUtil.setWindowStatusColor
import com.mycompany.app.main.MainUtil.applyBarAppearance
import com.mycompany.app.pref.PrefAlbum
import com.mycompany.app.pref.PrefAlbum.Companion.saveTrans
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefRead
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.quick.QuickAdapter.QuickItem
import com.mycompany.app.view.GlideApp.with
import com.mycompany.app.view.MyDialogBottom
import com.mycompany.app.view.MyFadeFrame
import com.mycompany.app.view.MyFadeListener
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.view.MyPopupMenu.MyPopupListener
import com.mycompany.app.view.MyProgressBar
import com.mycompany.app.view.MyScrollBar
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.view.MySnackbar.SnackbarListener
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebNestView.WebViewListener
import com.mycompany.app.web.WebReadTask
import com.mycompany.app.web.WebReadTask.ReadItem
import com.mycompany.app.web.WebReadTask.ReadWebListener
import com.mycompany.app.web.WebReadTask.WebReadListener
import com.mycompany.app.web.WebTransControl
import com.mycompany.app.web.WebTransControl.TransCtrlListener
import com.mycompany.app.web.WebVideoFrame.VideoFrameListener
import com.mycompany.app.web.WebVideoImage
import java.util.Locale
import java.util.concurrent.ExecutorService
import kotlin.Any
import kotlin.Array
import kotlin.Boolean
import kotlin.Byte
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.also
import kotlin.math.max
import kotlin.math.min

@Suppress("deprecation")
class DialogViewRead(context: Context?) : FrameLayout(context!!) {
    @JvmField
    var toolsRow: ReaderBar? = null
    @JvmField
    var morePopup: MyPopupMenu? = null
    @JvmField
    var miniLayoutParams: RelativeLayout.LayoutParams? = null
    @JvmField
    var scrollRunnable: Runnable?
    @JvmField
    var speakRow: ReaderBar? = null
    @JvmField
    var printDialog: DialogPrintPage? = null
    @JvmField
    var miniRect: RectF? = null
    @JvmField
    var videoImageOut: WebVideoImage? = null
    @JvmField
    var brightBtn: ReadBtn? = null
    @JvmField
    var saveSourceDialog: DialogSaveSource? = null
    @JvmField
    var miniColor: Int = 0
    @JvmField
    var videoTypeOut: Int = 0
    @JvmField
    var textSizeBtn: ReadBtn? = null
    @JvmField
    var transFromSpeak: Boolean = false
    @JvmField
    var miniPaint: Paint? = null
    @JvmField
    var videoRestore: Boolean = false
    @JvmField
    var audioBtn: ReadBtn? = null
    @JvmField
    var brightDialog: DialogSeekBright? = null
    @JvmField
    var hintTouch: Boolean = false
    @JvmField
    var jsResultE: String? = null
    @JvmField
    var speakBtn: ReadBtn? = null
    @JvmField
    var textDialog: DialogSeekText? = null
    @JvmField
    var touchX: Int = 0
    @JvmField
    var jsResultF: String? = null
    @JvmField
    var translateBtn: ReadBtn? = null
    @JvmField
    var audioDialog: DialogSeekAudio? = null
    @JvmField
    var touchY: Int = 0
    @JvmField
    var transControl: WebTransControl? = null
    @JvmField
    var prevBtn: ReadBtn? = null
    @JvmField
    var ttsDialog: DialogSetTts? = null
    @JvmField
    var dragOffsetX: Int = 0
    @JvmField
    var jsResultH: String? = null
    @JvmField
    var pauseBtn: ReadBtn? = null
    @JvmField
    var confirmDialog: DialogConfirm? = null
    @JvmField
    var dragOffsetY: Int = 0
    @JvmField
    var jsResultI: String? = null
    @JvmField
    var stopBtn: ReadBtn? = null
    @JvmField
    var transDialog: DialogViewTrans? = null
    @JvmField
    var minX: Int = 0
    @JvmField
    var snackbar: MySnackbar? = null
    @JvmField
    var speakFocusBtn: ReadBtn? = null
    @JvmField
    var configChanged: Boolean = false
    @JvmField
    var minY: Int = 0
    @JvmField
    var fullScreenSet: Boolean = false
    @JvmField
    var currentBtn: ReadBtn? = null
    @JvmField
    var textSizeState: Int = 0
    @JvmField
    var miniX: Int = 0
    @JvmField
    var miniAnimValue: Float = 0f
    @JvmField
    var nextBtn: ReadBtn? = null
    @JvmField
    var fontPathState: String? = null
    @JvmField
    var miniY: Int = 0
    @JvmField
    var miniAnimPosted: Boolean = false
    @JvmField
    var fadeText: com.mycompany.app.ui.components.FadeMessage? = null
    @JvmField
    var fontBoldState: Boolean = false
    @JvmField
    var maxX: Int = 0
    @JvmField
    var miniAnimEnd: Runnable?
    @JvmField
    var loadCover: com.mycompany.app.ui.components.LoadCover? = null
    @JvmField
    var readFromHtml: Boolean = false
    @JvmField
    var maxY: Int = 0
    @JvmField
    var videoImage: WebVideoImage? = null
    @JvmField
    var quickUrl: String? = null
    @JvmField
    var miniBarShown: Boolean = false
    @JvmField
    var pinchGuide: com.mycompany.app.ui.components.PinchGuide? = null
    @JvmField
    var newsIndex: Int = 0
    @JvmField
    var fullView: View? = null
    @JvmField
    var gestureDetect: GestureDetector? = null
    @JvmField
    var newsImage: String? = null
    @JvmField
    var bottomSheet: com.mycompany.app.ui.browser.ComposeBottomDialog? = null
    @JvmField
    var scaleDetector: ScaleGestureDetector? = null
    @JvmField
    var actionModeRef: ActionMode? = null
    @JvmField
    var pageUrl: String? = null
    @JvmField
    var showFromMini: Boolean = false
    @JvmField
    var sheetTransControl: WebTransControl? = null
    @JvmField
    var pageTitle: String? = null
    @JvmField
    var htmlLoading: Boolean = false
    @JvmField
    var transApplied: Boolean = false
    @JvmField
    var pageLang: String? = null
    @JvmField
    var paragraphList: ArrayList<ReadItem?>? = null
    @JvmField
    var transJsPending: Boolean = false
    @JvmField
    var readOpened: Boolean = false
    @JvmField
    var partPlay: Boolean = false
    @JvmField
    var loadPending: Boolean = false
    @JvmField
    var partStart: Int = 0
    @JvmField
    var transJsCache: String? = null
    @JvmField
    var partStartPos: Int = 0
    @JvmField
    var transState: Byte = 0
    @JvmField
    var partEnd: Int = 0
    @JvmField
    var autoTrans: Boolean = false
    @JvmField
    var alive: Boolean = true
    @JvmField
    var pageVisible: Boolean = false
    @JvmField
    var partEndPos: Int = 0
    @JvmField
    var transLang: String? = null
    @JvmField
    var activity: MainActivity? = null
    @JvmField
    var urlOverridden: Boolean = false
    @JvmField
    var transPickLast: String? = null
    @JvmField
    var ctx: Context?
    @JvmField
    var readTask: WebReadTask? = null
    @JvmField
    var detectedLang: String? = null
    @JvmField
    var uiHandler: Handler?
    @JvmField
    var speakBarShown: Boolean = false
    @JvmField
    var coverLoading: Boolean = false
    @JvmField
    var webHtml: String? = null
    @JvmField
    var layoutRtl: Boolean
    @JvmField
    var clickLocked: Boolean = false
    @JvmField
    var showAnim: ValueAnimator? = null
    @JvmField
    var scrollBase: Int = 0
    @JvmField
    var readListener: DialogReadListener? = null
    @JvmField
    var transRunning: Boolean = false
    @JvmField
    var hideAnim: ValueAnimator? = null
    @JvmField
    var scrollShift: Int = 0
    @JvmField
    var webBody: MyWebBody? = null
    @JvmField
    var ttsEngine: TextToSpeech? = null
    @JvmField
    var miniAnim: ValueAnimator? = null
    @JvmField
    var transLangDialog: DialogTransLang? = null
    @JvmField
    var quickOpened: Boolean = false
    @JvmField
    var ttsRateSet: Float = 0f
    @JvmField
    var barsHidden: Boolean = false
    @JvmField
    var transDone: Boolean = false
    @JvmField
    var ttsPitchSet: Float = 0f
    @JvmField
    var statusColor: Int = 0
    @JvmField
    var transText: MainTransText? = null
    @JvmField
    var ttsLang: String? = null
    @JvmField
    var naviColor: Int = 0
    @JvmField
    var selRect: Rect? = null
    @JvmField
    var ttsLangSet: String? = null
    @JvmField
    var windowColor: Int = 0
    @JvmField
    var selRects: ArrayList<Rect?>? = null
    @JvmField
    var ttsParagraphs: ArrayList<Any?>? = null
    @JvmField
    var savedStatusColor: Int = 0
    @JvmField
    var showAnimValue: Float = 0f
    @JvmField
    var ttsIndex: Int = 0
    @JvmField
    var savedNaviColor: Int = 0
    @JvmField
    var showAnimPosted: Boolean = false
    @JvmField
    var backBtn: ReadBtn? = null
    @JvmField
    var ttsState: Byte = 0
    @JvmField
    var savedWindowColor: Int = 0
    @JvmField
    var showAnimEnd: Runnable?
    @JvmField
    var miniBtn: ReadBtn? = null
    @JvmField
    var speakMode: Boolean = false
    @JvmField
    var executorCache: ExecutorService? = null
    @JvmField
    var hideAnimValue: Float = 0f
    @JvmField
    var openBtn: ReadBtn? = null
    @JvmField
    var ttsStartPos: Int = 0
    @JvmField
    var opening: Boolean = false
    @JvmField
    var hideAnimPosted: Boolean = false
    @JvmField
    var saveBtn: ReadBtn? = null
    @JvmField
    var ttsEndPos: Int = 0
    @JvmField
    var closing: Boolean = false
    @JvmField
    var hideAnimEnd: Runnable?
    @JvmField
    var reloadBtn: ReadBtn? = null
    @JvmField
    var ttsRestart: Boolean = false
    @JvmField
    var barInset: Int = 0
    @JvmField
    var pendingJs: String? = null
    @JvmField
    var moreBtn: ReadBtn? = null
    @JvmField
    var ttsMoveIndex: Int = 0
    @JvmField
    var miniBar: ReaderBar? = null
    @JvmField
    var paragraphQueue: MutableList<ReadItem?>? = null
    @JvmField
    var content: RelativeLayout? = null
    @JvmField
    var rangeStart: Int = 0
    @JvmField
    var fullBtn: ReadBtn? = null
    @JvmField
    var pendingList: MutableList<String?>? = null
    @JvmField
    var page: WebNestView? = null
    @JvmField
    var rangeEnd: Int = 0
    @JvmField
    var closeBtn: ReadBtn? = null
    @JvmField
    var ttsMoveRunnable: Runnable?
    @JvmField
    var progressBar: MyProgressBar? = null
    @JvmField
    var ttsJump: Boolean = false
    @JvmField
    var rewindBtn: ReadBtn? = null
    @JvmField
    var finishHandled: Boolean = false
    @JvmField
    var customView: View? = null
    @JvmField
    var pageJs: String? = null
    @JvmField
    var playBtn: ReadBtn? = null
    @JvmField
    var lastFinishedUrl: String? = null
    @JvmField
    var scrollBar: MyScrollBar? = null
    @JvmField
    var nativeScript5: String? = null
    @JvmField
    var forwardBtn: ReadBtn? = null
    @JvmField
    var progressValue: Int = 0
    @JvmField
    var speakBar: MyFadeFrame? = null
    @JvmField
    var nativeScript13: String? = null
    @JvmField
    var miniMode: Boolean = false
    @JvmField
    var progressRunnable: Runnable?
    @JvmField
    var topBar: ReaderBar? = null
    @JvmField
    var savePopup: MyPopupMenu? = null
    @JvmField
    var miniAnimating: Boolean = false
    @JvmField
    var scrollLocked: Boolean = false

    internal inner class BuildReaderTask : Runnable {

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val mainActivity: MainActivity? = if (dialogViewRead.content != null) null else dialogViewRead.activity
            if (dialogViewRead.content != null || mainActivity == null) {
                return
            }
            val myRoundItem = readerFrame(mainActivity)
            dialogViewRead.addView(myRoundItem, -1, -1)
            val myProgressBar = MyProgressBar(mainActivity, 0)
            myProgressBar.setVisibility(View.GONE)
            val layoutParams = RelativeLayout.LayoutParams(-1, MainApp.dp4)
            if (!BrowserSettings.showStatus.value) {
                layoutParams.topMargin = MainApp.dp4 / 2
            }
            myRoundItem.addView(myProgressBar, layoutParams)
            val view = View(mainActivity)
            view.setBackgroundResource(AppR.drawable.shadow_list_up)
            view.setVisibility(View.GONE)
            myRoundItem.addView(view, -1, MainApp.dp16)
            val fullView = View(mainActivity)
            fullView.setVisibility(View.GONE)
            val ctrlParams = RelativeLayout.LayoutParams(
                dpToPx(mainActivity, 130.0f).toInt(),
                dpToPx(mainActivity, 24.0f).toInt()
            )
            ctrlParams.addRule(12)
            ctrlParams.addRule(21)
            myRoundItem.addView(fullView, ctrlParams)
            val myScrollBar = MyScrollBar(mainActivity)
            myScrollBar.useCompact()
            myScrollBar.setVisibility(View.INVISIBLE)
            val sideParams = RelativeLayout.LayoutParams(dpToPx(mainActivity, 32.0f).toInt(), -1)
            if (ZoneSettings.scrollPos.value == 1) {
                sideParams.addRule(9)
                myScrollBar.setPosLeft(true)
            } else {
                sideParams.addRule(11)
            }
            myRoundItem.addView(myScrollBar, sideParams)
            val myFadeFrame = MyFadeFrame(mainActivity)
            myFadeFrame.setTouchable(true)
            myFadeFrame.setAutoHide(true)
            myFadeFrame.setVisibility(View.GONE)
            myRoundItem.addView(myFadeFrame, -1, -1)
            val myFadeText = com.mycompany.app.ui.components.FadeMessage(mainActivity, AppR.string.reader_empty)
            myRoundItem.addView(myFadeText, -1, -1)
            val myCoverView = com.mycompany.app.ui.components.LoadCover(mainActivity)
            myRoundItem.addView(myCoverView, -1, -1)
            dialogViewRead.content = myRoundItem
            dialogViewRead.progressBar = myProgressBar
            dialogViewRead.customView = view
            dialogViewRead.fullView = fullView
            dialogViewRead.scrollBar = myScrollBar
            dialogViewRead.speakBar = myFadeFrame
            dialogViewRead.fadeText = myFadeText
            dialogViewRead.loadCover = myCoverView
            dialogViewRead.setVisibility(View.INVISIBLE)
            dialogViewRead.setOnClickListener(RootClick())
            dialogViewRead.scrollBar!!.setListener(dialogViewRead.PageScrollBarListener())
            dialogViewRead.applyColors()
            dialogViewRead.gestureDetect = GestureDetector(dialogViewRead.ctx, dialogViewRead.PageGestures())
            val webReadTask = WebReadTask(
                dialogViewRead.activity,
                dialogViewRead.ctx,
                dialogViewRead.readFromHtml,
                true,
                dialogViewRead.quickOpened,
                dialogViewRead.ArticleLoadListener()
            )
            dialogViewRead.readTask = webReadTask
            val myWebBody = dialogViewRead.webBody
            val progressListener = dialogViewRead.ReadProgressListener()
            webReadTask.container = myWebBody
            webReadTask.readListener = progressListener
            val pageUrl = dialogViewRead.pageUrl
            val pageTitle = dialogViewRead.pageTitle
            val newsImage = dialogViewRead.newsImage
            webReadTask.pageUrl = pageUrl
            webReadTask.pageHost = MainUtil.hostOfUrl(pageUrl!!, true)
            webReadTask.articleTitle = pageTitle
            webReadTask.subTitle = newsImage
            if (ReadSettings.autoTrans.value) {
                dialogViewRead.transApplied = true
                dialogViewRead.transState = 1.toByte()
            }
            dialogViewRead.transPickLast = AlbumSettings.transPick.value
            val pageParams = RelativeLayout.LayoutParams(-1, -1)
            if (!dialogViewRead.showFromMini) {
                pageParams.addRule(2, AppR.id.web_cast_ctrl)
            }
            dialogViewRead.webBody!!.addView(dialogViewRead, pageParams)
            if (dialogViewRead.showFromMini) {
                val handler = dialogViewRead.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogViewRead.EnterMiniModeTask())
                return
            }
            if (dialogViewRead.alive && dialogViewRead.showAnim == null && dialogViewRead.hideAnim == null) {
                dialogViewRead.showAnimValue = 0.0f
                dialogViewRead.showAnimPosted = false
                val valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f)
                dialogViewRead.showAnim = valueAnimatorOfFloat
                valueAnimatorOfFloat.setDuration(300L)
                dialogViewRead.showAnim!!.interpolator = android.view.animation.DecelerateInterpolator()
                dialogViewRead.showAnim!!.addUpdateListener(dialogViewRead.ShowAnimUpdate())
                dialogViewRead.showAnim!!.addListener(dialogViewRead.ShowAnimListener())
                dialogViewRead.showAnim!!.start()
            }
        }
    }

    internal inner class TransSheetDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.bottomSheet != null) {
                dialogViewRead.updateSpeakBar()
            }
            dialogViewRead.dismissTransSheet()
        }
    }

    internal inner class TransLangPicked : TransLangListener {
        override fun onLanguage(lang: String?) {
            val dialogViewRead = this@DialogViewRead
            val dialogTransLang = dialogViewRead.transLangDialog
            if (dialogTransLang != null) {
                dialogTransLang.dismiss()
                dialogViewRead.transLangDialog = null
            }
            dialogViewRead.transPickLast = AlbumSettings.transPick.value
            if (!TextUtils.isEmpty(dialogViewRead.transJsCache)) {
                dialogViewRead.dismissTransSheet()
                dialogViewRead.setTransJs(true)
            } else if (dialogViewRead.transState.toInt() == 3) {
                dialogViewRead.dismissTransSheet()
                if (TextUtils.isEmpty(lang)) {
                    return
                }
                dialogViewRead.runInBackground(EvalLongPressJsTask(dialogViewRead, lang))
            }
        }
    }

    internal inner class TransLangDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogTransLang = dialogViewRead.transLangDialog
            if (dialogTransLang != null) {
                dialogTransLang.dismiss()
                dialogViewRead.transLangDialog = null
            }
        }
    }

    internal inner class EvalJsStep : MenuItem.OnMenuItemClickListener {
        override fun onMenuItemClick(menuItem: MenuItem): Boolean {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.page == null) {
                return true
            }
            if (dialogViewRead.transState.toInt() == 1) {
                showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                return true
            }
            val partJs = dialogViewRead.partJs
            if (TextUtils.isEmpty(partJs)) {
                showToast(dialogViewRead.ctx, AppR.string.play_error)
                return true
            }
            evalJs(dialogViewRead.page, partJs, true)
            return true
        }
    }

    internal inner class JsResultStep : MenuItem.OnMenuItemClickListener {
        override fun onMenuItemClick(menuItem: MenuItem): Boolean {
            val webNestView = this@DialogViewRead.page
            if (webNestView == null) {
                return true
            }
            evalJs(
                webNestView,
                "(function(){var tag='onActionTrans';var val=window.getSelection().toString();android.onJsResult(tag,val);})();",
                true
            )
            return true
        }
    }

    internal inner class InvalidateActionModeTask : Runnable {
        override fun run() {
            val actionMode = this@DialogViewRead.actionModeRef
            if (actionMode == null) {
                return
            }
            try {
                actionMode.invalidateContentRect()
            } catch (unused: Exception) {
            }
        }
    }

    internal inner class SetPartPlayTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val jsResultH = dialogViewRead.jsResultH
            dialogViewRead.jsResultH = null
            dialogViewRead.setPartPlay(jsResultH)
            dialogViewRead.finishActionMode()
        }
    }

    internal inner class ShowTransTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val jsResultI = dialogViewRead.jsResultI
            dialogViewRead.jsResultI = null
            dialogViewRead.showTrans(jsResultI)
            dialogViewRead.finishActionMode()
        }
    }

    internal inner class SetMiniModeClick : OnClickListener {

        internal inner class ExitMiniModeStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                dialogViewRead.setMiniMode(false)
                dialogViewRead.opening = false
            }
        }

        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            val myButtonImage = dialogViewRead.fullBtn
            if (myButtonImage == null || dialogViewRead.opening) {
                return
            }
            dialogViewRead.opening = true
            myButtonImage.post(ExitMiniModeStep())
        }
    }

    internal inner class SavedSourceStep(@JvmField val outer: DialogViewRead,
                                           @JvmField val path: String?
    ) : Runnable {

        internal inner class SaveSnackbarListener : SnackbarListener {
            override fun onSecond() {
            }

            override fun onDismissed() {
            }

            override fun onFirst() {
            }

            override fun onDismiss() {
                this@SavedSourceStep.outer.snackbar = null
            }
        }

        internal inner class OpenSavedFileSnackbar(@JvmField val outer: SavedSourceStep,
                                             @JvmField val path: String?
        ) : SnackbarListener {
            override fun onSecond() {
            }

            override fun onDismissed() {
            }

            override fun onFirst() {
                openWithApp(this.outer.outer.activity, this.path, "text/plain", true, false)
            }

            override fun onDismiss() {
                this.outer.outer.snackbar = null
            }
        }

        override fun run() {
            val dialogViewRead = this.outer
            if (dialogViewRead.miniMode || !dialogViewRead.alive) {
                return
            }
            cancelToast()
            val mySnackbar = dialogViewRead.snackbar
            if (mySnackbar != null) {
                mySnackbar.hide(false)
                dialogViewRead.snackbar = null
            }
            dialogViewRead.snackbar = MySnackbar(dialogViewRead.activity!!)
            val path = this.path
            val pathEmpty = TextUtils.isEmpty(path)
            val snackbar = dialogViewRead.snackbar
            if (pathEmpty) {
                snackbar!!.showSingle(
                    dialogViewRead,
                    AppR.string.save_fail,
                    SaveSnackbarListener()
                )
            } else {
                snackbar!!.showWithButton(
                    dialogViewRead,
                    AppR.string.save_success,
                    1,
                    OpenSavedFileSnackbar(this, path)
                )
            }
        }
    }

    internal inner class ApplyBarAppearanceTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val fullScreenSet = dialogViewRead.fullScreenSet
            val mainActivity = dialogViewRead.activity
            if (mainActivity == null) {
                return
            }
            MainUtil.applyBarAppearance(mainActivity.getWindow(), dialogViewRead.activity!!.decorViewCached(), dialogViewRead.activity!!.insetsControllerCompat(), fullScreenSet, fullScreenSet)
        }
    }

    internal inner class SetMiniModeTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.setMiniMode(false)
            dialogViewRead.opening = false
        }
    }

    internal inner class CreatePageTask : Runnable {

        internal inner class CreatePageStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                dialogViewRead.createPage()
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.buildTopBar()
            val handler: Handler? = if (dialogViewRead.page != null) null else dialogViewRead.uiHandler
            if (dialogViewRead.page != null || handler == null) {
                return
            }
            handler.post(CreatePageStep())
        }
    }

    internal inner class MiniAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.miniAnim == null || !dialogViewRead.alive) {
                return
            }
            val animatedValue = (valueAnimator.getAnimatedValue() as Float)
            if (dialogViewRead.miniAnim == null || !dialogViewRead.alive) {
                return
            }
            dialogViewRead.miniAnimValue = animatedValue
            if (dialogViewRead.miniAnimPosted) {
                return
            }
            dialogViewRead.miniAnimPosted = true
            MainApp.postToMain(dialogViewRead.ctx, dialogViewRead.miniAnimEnd!!)
        }
    }

    internal inner class MiniAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.miniAnim == null) {
                return
            }
            dialogViewRead.miniAnim = null
            dialogViewRead.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.miniAnim == null) {
                return
            }
            MainApp.postToMain(dialogViewRead.ctx, dialogViewRead.MiniAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimMiniTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.miniAnimPosted = false
            if (dialogViewRead.miniAnim == null) {
                return
            }
            dialogViewRead.setValAnimMini(dialogViewRead.miniAnimValue)
        }
    }

    internal inner class SetPosTask : Runnable {
        override fun run() {
            this@DialogViewRead.setPos(true)
        }
    }

    internal inner class MoveToMiniPosTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.setX(dialogViewRead.miniX.toFloat())
            dialogViewRead.setY(dialogViewRead.miniY.toFloat())
        }
    }

    internal inner class MiniAnimEndTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.miniAnim != null) {
                dialogViewRead.miniAnim = null
                if (dialogViewRead.alive) {
                    if (dialogViewRead.miniMode) {
                        dialogViewRead.setValAnimMini(0.0f)
                    } else {
                        dialogViewRead.setValAnimMini(1.0f)
                    }
                    dialogViewRead.invalidate()
                }
            }
        }
    }

    internal inner class CloseReaderClick : OnClickListener {

        internal inner class CloseReaderStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                dialogViewRead.closeReader(dialogViewRead.readListener)
                dialogViewRead.opening = false
            }
        }

        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            val myButtonImage = dialogViewRead.closeBtn
            if (myButtonImage == null || dialogViewRead.opening) {
                return
            }
            dialogViewRead.opening = true
            myButtonImage.post(CloseReaderStep())
        }
    }

    internal inner class TtsStartTask : Runnable {
        override fun run() {
            this@DialogViewRead.ttsStart()
        }
    }

    internal inner class RoundOutline : ViewOutlineProvider() {
        override fun getOutline(view: View?, outline: Outline?) {
            if (view == null || outline == null) {
                return
            }
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), MainApp.dp16.toFloat())
        }
    }

    internal inner class AutoHideMiniBarTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val myButtonImage: ReadBtn? = if (!dialogViewRead.miniBarShown) null else dialogViewRead.fullBtn
            if (!dialogViewRead.miniBarShown || myButtonImage == null || myButtonImage.isPressed() || dialogViewRead.closeBtn!!.isPressed() || dialogViewRead.rewindBtn!!.isPressed() || dialogViewRead.playBtn!!.isPressed()) {
                return
            }
            dialogViewRead.forwardBtn!!.isPressed()
        }
    }

    internal inner class SetScaleGestureStep : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.miniMode) {
                return false
            }
            dialogViewRead.setScaleGesture(scaleGestureDetector)
            return true
        }
    }

    internal inner class TtsPrevClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.speakMode) {
                dialogViewRead.ttsPrev()
            }
        }
    }

    internal inner class TtsPauseClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            val speakMode = dialogViewRead.speakMode
            if (speakMode) {
                dialogViewRead.ttsPause()
                return
            }
            val dialogReadListener: DialogReadListener? = if (speakMode) null else dialogViewRead.readListener
            if (speakMode || dialogReadListener == null) {
                return
            }
            if (dialogReadListener.isSpeaking()) {
                dialogViewRead.post(dialogViewRead.TtsStartTask())
            } else {
                dialogViewRead.ttsStart()
            }
        }
    }

    internal inner class TtsNextClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.speakMode) {
                dialogViewRead.ttsNext()
            }
        }
    }

    internal inner class HandleBackClick : OnClickListener {
        override fun onClick(view: View?) {
            this@DialogViewRead.handleBack()
        }
    }

    internal inner class RootClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    internal inner class DismissDialogsClick : OnClickListener {

        internal inner class ToMiniModeStep : Runnable {

            internal inner class SetMiniModeLaterStep : Runnable {
                override fun run() {
                    val dismissDialogsClick = this@DismissDialogsClick
                    this@DialogViewRead.setMiniMode(true)
                    this@DialogViewRead.opening = false
                }
            }

            override fun run() {
                val dialogViewRead = this@DialogViewRead
                if (dialogViewRead.miniBtn == null) {
                    return
                }
                dialogViewRead.dismissDialogs()
                dialogViewRead.dismissSaveMenu()
                dialogViewRead.dismissMoreMenu()
                dialogViewRead.finishActionMode()
                dialogViewRead.miniBtn!!.post(SetMiniModeLaterStep())
            }
        }

        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            val myButtonImage = dialogViewRead.miniBtn
            if (myButtonImage == null || dialogViewRead.opening) {
                return
            }
            dialogViewRead.opening = true
            myButtonImage.post(ToMiniModeStep())
        }
    }

    internal inner class OpenUrlClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (!dialogViewRead.readOpened) {
                showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                return
            }
            val dialogReadListener = dialogViewRead.readListener
            if (dialogReadListener != null) {
                dialogReadListener.openUrl(dialogViewRead.pageUrl)
            }
        }
    }

    internal inner class DismissSaveMenuClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (!dialogViewRead.readOpened) {
                showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                return
            }
            if (dialogViewRead.saveMenu == null) {
                dialogViewRead.dismissSaveMenu()
                val myFadeFrame = dialogViewRead.speakBar
                if (myFadeFrame != null) {
                    myFadeFrame.setAutoHide(false)
                }
                dialogViewRead.saveMenu = listOf(ReaderMenuItem(0, "PDF"), ReaderMenuItem(1, "TXT"))
            }
        }
    }

    internal inner class StopTtsClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (!dialogViewRead.readOpened) {
                showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                return
            }
            dialogViewRead.stopTts()
            val myRoundItem = dialogViewRead.content
            if (myRoundItem != null) {
                postRequestLayout(myRoundItem)
            }
            val myCoverView = dialogViewRead.loadCover
            if (myCoverView == null || myCoverView.isCoverShown()) {
                return
            }
            val readFromHtml = dialogViewRead.readFromHtml
            if (readFromHtml) {
                if (!readFromHtml || dialogViewRead.readTask == null) {
                    return
                }
                dialogViewRead.showCoverProgress()
                dialogViewRead.readTask!!.createWebView()
                return
            }
            val dialogReadListener: DialogReadListener? = if (readFromHtml) null else dialogViewRead.readListener
            val webNestViewA: WebNestView? = if (readFromHtml || dialogReadListener == null) null else dialogReadListener.currentWeb()
            if (readFromHtml || dialogReadListener == null || webNestViewA == null) {
                return
            }
            dialogViewRead.showCoverProgress()
            webNestViewA.evaluateJavascript(
                "document.documentElement.lang",
                dialogViewRead.ArticleHtmlCallback()
            )
        }
    }

    internal inner class DismissMoreMenuClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (!dialogViewRead.readOpened) {
                showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                return
            }
            if (ReadSettings.notiRead.value) {
                ReadSettings.edit(dialogViewRead.ctx) { set(ReadSettings.notiRead, false) }
                val myButtonImage = dialogViewRead.moreBtn
                if (myButtonImage != null) {
                    myButtonImage.setNoti(false)
                }
            }
            if (dialogViewRead.moreMenu == null) {
                dialogViewRead.dismissMoreMenu()
                val myFadeFrame = dialogViewRead.speakBar
                if (myFadeFrame != null) {
                    myFadeFrame.setAutoHide(false)
                }
                dialogViewRead.moreMenu = listOf(
                    ReaderMenuItem(0, null, AppR.string.font),
                    ReaderMenuItem(1, "TTS"),
                    ReaderMenuItem(2, null, AppR.string.tts_highlight, ZtriSettings.readAccent.value),
                    ReaderMenuItem(3, null, AppR.string.auto_speak, ReadSettings.autoSpeak.value),
                    ReaderMenuItem(4, null, AppR.string.trans_auto, ReadSettings.autoTrans.value),
                )
            }
        }
    }

    internal inner class HasDialogClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.activity != null && !dialogViewRead.hasDialog()) {
                val dialogSeekBright = dialogViewRead.brightDialog
                if (dialogSeekBright != null) {
                    dialogSeekBright.dismiss()
                    dialogViewRead.brightDialog = null
                }
                val mainActivity = dialogViewRead.activity
                val brightDialog =
                    DialogSeekBright(mainActivity, mainActivity!!.getWindow(), 0, null)
                dialogViewRead.brightDialog = brightDialog
                brightDialog.setOnDismissListener(dialogViewRead.BrightDialogDismiss())
            }
            val myFadeFrame = dialogViewRead.speakBar
            if (myFadeFrame != null) {
                myFadeFrame.hide(true)
            }
        }
    }

    internal inner class TextSizeClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.activity != null && !dialogViewRead.hasDialog()) {
                val dialogSeekText = dialogViewRead.textDialog
                if (dialogSeekText != null) {
                    dialogSeekText.dismiss()
                    dialogViewRead.textDialog = null
                }
                val dialog =
                    DialogSeekText(dialogViewRead.activity, dialogViewRead.TextSizeSeek())
                dialogViewRead.textDialog = dialog
                dialog.setOnDismissListener(dialogViewRead.TextSizeDialogDismiss())
            }
            val myFadeFrame = dialogViewRead.speakBar
            if (myFadeFrame != null) {
                myFadeFrame.hide(true)
            }
        }
    }

    internal inner class AudioClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.activity != null && !dialogViewRead.hasDialog()) {
                val dialogSeekAudio = dialogViewRead.audioDialog
                if (dialogSeekAudio != null) {
                    dialogSeekAudio.dismiss()
                    dialogViewRead.audioDialog = null
                }
                val audioDialog = DialogSeekAudio(dialogViewRead.activity, null)
                dialogViewRead.audioDialog = audioDialog
                audioDialog.setOnDismissListener(dialogViewRead.AudioDialogDismiss())
            }
            val myFadeFrame = dialogViewRead.speakBar
            if (myFadeFrame != null) {
                myFadeFrame.hide(true)
            }
        }
    }

    internal inner class TtsStartClick : OnClickListener {
        override fun onClick(view: View?) {
            this@DialogViewRead.ttsStart()
        }
    }

    internal inner class TranslateBtnClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.activity != null && !dialogViewRead.hasDialog()) {
                dialogViewRead.dismissTransSheet()
                if (dialogViewRead.transControl == null) {
                    dialogViewRead.transControl = WebTransControl(dialogViewRead.activity!!)
                    val handler = dialogViewRead.uiHandler
                    if (handler != null) {
                        handler.post(dialogViewRead.ShowTransControlTask())
                    }
                }
            }
            val myFadeFrame = dialogViewRead.speakBar
            if (myFadeFrame != null) {
                myFadeFrame.hide(true)
            }
        }
    }

    internal inner class BarTtsPrevClick : OnClickListener {
        override fun onClick(view: View?) {
            this@DialogViewRead.ttsPrev()
        }
    }

    internal inner class CurrentBtnClick : OnClickListener {
        override fun onClick(view: View?) {
            this@DialogViewRead.setIconCurr(true)
        }
    }

    internal inner class BarTtsNextClick : OnClickListener {
        override fun onClick(view: View?) {
            this@DialogViewRead.ttsNext()
        }
    }

    internal inner class BarTtsPauseClick : OnClickListener {
        override fun onClick(view: View?) {
            this@DialogViewRead.ttsPause()
        }
    }

    internal inner class LockClick : OnClickListener {
        override fun onClick(view: View?) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.lockClick()) {
                return
            }
            if (dialogViewRead.isBusy(true)) {
                showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                return
            }
            dialogViewRead.speakMode = false
            dialogViewRead.ttsMoveIndex = -1234
            dialogViewRead.ttsJump = false
            dialogViewRead.stopTts()
            dialogViewRead.hideSpeakBar()
        }
    }

    internal inner class ShowAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.showAnim == null || !dialogViewRead.alive) {
                return
            }
            val animatedValue = (valueAnimator.getAnimatedValue() as Float)
            if (dialogViewRead.showAnim == null || !dialogViewRead.alive) {
                return
            }
            dialogViewRead.showAnimValue = animatedValue
            if (dialogViewRead.showAnimPosted) {
                return
            }
            dialogViewRead.showAnimPosted = true
            MainApp.postToMain(dialogViewRead.ctx, dialogViewRead.showAnimEnd!!)
        }
    }

    internal inner class ShowAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.showAnim == null) {
                return
            }
            dialogViewRead.showAnim = null
            dialogViewRead.invalidate()
        }

        override fun onAnimationEnd(animator: Animator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.showAnim == null) {
                return
            }
            MainApp.postToMain(dialogViewRead.ctx, dialogViewRead.ShowAnimEndTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimShowTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.showAnimPosted = false
            if (dialogViewRead.showAnim == null) {
                return
            }
            dialogViewRead.setValAnimShow(dialogViewRead.showAnimValue)
        }
    }

    internal inner class ApplyWindowTask : Runnable {
        override fun run() {
            val darkUi = AppState.darkUi
            val statusColor = if (darkUi) -16777216 else -1
            this@DialogViewRead.applyWindow(statusColor, darkUi)
        }
    }

    internal inner class ShowAnimEndTask : Runnable {

        internal inner class ShowAnimEndStep : Runnable {

            internal inner class BuildMiniBarStep : Runnable {

                internal inner class CreatePageLaterStep : Runnable {
                    override fun run() {
                        val dialogViewRead = this@DialogViewRead
                        dialogViewRead.createPage()
                    }
                }

                override fun run() {
                    val showAnimEndTask = this@ShowAnimEndTask
                    val dialogViewRead = this@DialogViewRead
                    dialogViewRead.buildMiniBar()
                    val dialog = this@DialogViewRead
                    val handler: Handler? = if (dialog.page != null) null else dialog.uiHandler
                    if (dialog.page != null || handler == null) {
                        return
                    }
                    handler.post(CreatePageLaterStep())
                }
            }

            override fun run() {
                val dialogViewRead = this@DialogViewRead
                dialogViewRead.buildTopBar()
                val handler: Handler? = if (dialogViewRead.miniBar != null) null else dialogViewRead.uiHandler
                if (dialogViewRead.miniBar != null || handler == null) {
                    return
                }
                handler.post(BuildMiniBarStep())
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.showAnim != null) {
                dialogViewRead.showAnim = null
                if (dialogViewRead.alive) {
                    dialogViewRead.setValAnimShow(1.0f)
                    dialogViewRead.invalidate()
                    val handler: Handler? = if ((dialogViewRead.topBar == null || dialogViewRead.miniBar == null || dialogViewRead.page == null)) dialogViewRead.uiHandler else null
                    if ((dialogViewRead.topBar == null || dialogViewRead.miniBar == null || dialogViewRead.page == null) && handler != null) {
                        handler.post(ShowAnimEndStep())
                    }
                }
            }
        }
    }

    internal inner class HideAnimUpdate : AnimatorUpdateListener {
        override fun onAnimationUpdate(valueAnimator: ValueAnimator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.hideAnim == null || !dialogViewRead.alive) {
                return
            }
            val animatedValue = (valueAnimator.getAnimatedValue() as Float)
            if (dialogViewRead.hideAnim == null || !dialogViewRead.alive) {
                return
            }
            dialogViewRead.hideAnimValue = animatedValue
            if (dialogViewRead.hideAnimPosted) {
                return
            }
            dialogViewRead.hideAnimPosted = true
            MainApp.postToMain(dialogViewRead.ctx, dialogViewRead.hideAnimEnd!!)
        }
    }

    internal inner class CloseReaderAnimListener : Animator.AnimatorListener {
        override fun onAnimationCancel(animator: Animator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.hideAnim == null) {
                return
            }
            dialogViewRead.hideAnim = null
            dialogViewRead.closeReader(dialogViewRead.readListener)
        }

        override fun onAnimationEnd(animator: Animator) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.hideAnim == null) {
                return
            }
            MainApp.postToMain(dialogViewRead.ctx, dialogViewRead.CloseReaderTask())
        }

        override fun onAnimationRepeat(animator: Animator) {
        }

        override fun onAnimationStart(animator: Animator) {
        }
    }

    internal inner class SetValAnimHideTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.hideAnimPosted = false
            if (dialogViewRead.hideAnim == null) {
                return
            }
            dialogViewRead.setValAnimHide(dialogViewRead.hideAnimValue)
        }
    }

    internal inner class CloseReaderTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.hideAnim == null) {
                return
            }
            dialogViewRead.hideAnim = null
            dialogViewRead.closeReader(dialogViewRead.readListener)
        }
    }

    internal inner class ReloadTask : Runnable {

        internal inner class ReloadPageStep : Runnable {
            override fun run() {
                val webNestView = this@DialogViewRead.page
                if (webNestView == null) {
                    return
                }
                webNestView.reload()
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val webNestView = dialogViewRead.page
            if (webNestView == null) {
                return
            }
            webNestView.clearCache(false)
            dialogViewRead.page!!.post(ReloadPageStep())
        }
    }

    internal inner class PageScrollBarListener : MyScrollBar.ScrollBarListener {
        override fun scrollTo(position: Int) {
            val webNestView = this@DialogViewRead.page
            if (webNestView == null) {
                return
            }
            webNestView.scrollTo(0, position)
        }

        override fun scrollPos(): Int {
            val webNestView = this@DialogViewRead.page
            if (webNestView == null) {
                return 0
            }
            return webNestView.scrollTop
        }

        override fun onDragEnd() {
        }

        override fun scrollRange(): Int {
            val webNestView = this@DialogViewRead.page
            if (webNestView == null) {
                return 0
            }
            return webNestView.scrollRange
        }

        override fun scrollExtent(): Int {
            val webNestView = this@DialogViewRead.page
            if (webNestView == null) {
                return 0
            }
            return webNestView.scrollExtent
        }
    }

    internal inner class ShowPinchGuideTask : Runnable {
        override fun run() {
            if (ReadSettings.guideRead.value) {
                val dialogViewRead = this@DialogViewRead
                val mainActivity: MainActivity? = if (dialogViewRead.pinchGuide == null && dialogViewRead.alive) dialogViewRead.activity else null
                if (dialogViewRead.pinchGuide == null && dialogViewRead.alive && mainActivity != null) {
                    // one-time pinch guide (Compose; was a MyFadeFrame with a card): first touch turns it off, then it
                    // fades out and is removed
                    dialogViewRead.pinchGuide = com.mycompany.app.ui.components.PinchGuide(
                        mainActivity,
                        onTouch = {
                            if (ReadSettings.guideRead.value) {
                                ReadSettings.edit(dialogViewRead.ctx) { set(ReadSettings.guideRead, false) }
                            }
                        },
                        onGone = {
                            val guide = dialogViewRead.pinchGuide
                            if (guide != null) {
                                dialogViewRead.removeView(guide)
                                dialogViewRead.pinchGuide = null
                            }
                            dialogViewRead.updateSpeakBar()
                        },
                    )
                    dialogViewRead.addView(dialogViewRead.pinchGuide, -1, -1)
                }
            }
        }
    }

    internal inner class ArticleHtmlCallback : ValueCallback<String> {
        override fun onReceiveValue(value: String?) {
            val valueText = value
            val dialogViewRead = this@DialogViewRead
            val dialogReadListener: DialogReadListener? = if (dialogViewRead.readFromHtml) null else dialogViewRead.readListener
            val webNestViewA: WebNestView? = if (dialogViewRead.readFromHtml || dialogReadListener == null) null else dialogReadListener.currentWeb()
            if (dialogViewRead.readFromHtml || dialogReadListener == null || webNestViewA == null) {
                return
            }
            dialogViewRead.pageLang = stripQuotes(valueText)
            // the article is found inside the page (fast); the whole page goes to the app only when that finds none
            com.mycompany.app.web.ReaderExtract.run(webNestViewA, dialogViewRead.pageUrl) { found ->
                val task = dialogViewRead.readTask
                if (found != null && task != null) {
                    task.startLoadArticle(found)
                } else if (isHttpUrl(dialogViewRead.pageUrl)) {
                    webNestViewA.evaluateJavascript(
                        "document.documentElement.innerHTML",
                        dialogViewRead.SetReadHtmlCallback()
                    )
                } else {
                    evalJs(
                        webNestViewA,
                        "(function(){android.onReadHtml(document.documentElement.innerHTML);})();",
                        true
                    )
                }
            }
        }
    }

    internal inner class SetReadHtmlCallback : ValueCallback<String> {

        internal inner class ApplyReadHtmlStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                val pendingJs = dialogViewRead.pendingJs
                dialogViewRead.pendingJs = null
                if (dialogViewRead.readListener == null) {
                    return
                }
                dialogViewRead.setReadHtml(MainUtil.unescapeJavaText(stripQuotes(pendingJs)))
            }
        }

        override fun onReceiveValue(value: String?) {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.pendingJs = value
            dialogViewRead.runInBackground(ApplyReadHtmlStep())
        }
    }

    internal inner class SetTransJsTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.page != null) {
                dialogViewRead.transJsCache = dialogViewRead.transJs
                if (ReadSettings.autoTrans.value) {
                    dialogViewRead.setTransJs(false)
                }
            }
        }
    }

    internal inner class ShowSpeakBarTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.htmlLoading) {
                dialogViewRead.showSpeakBar(true)
            }
        }
    }

    internal inner class ResetTtsStep : OnInitListener {
        override fun onInit(status: Int) {
            val dialogViewRead = this@DialogViewRead
            if (status == -1) {
                dialogViewRead.resetTts()
            } else if (status == 0) {
                dialogViewRead.transRunning = false
                dialogViewRead.startAutoTrans()
            }
        }
    }

    internal inner class StopTtsStep : UtteranceProgressListener() {

        internal inner class StopTtsLaterStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                if (dialogViewRead.speakBarShown) {
                    return
                }
                dialogViewRead.stopTts()
            }
        }

        override fun onDone(utteranceId: String?) {
            val dialogViewRead = this@DialogViewRead
            val arrayList = dialogViewRead.ttsParagraphs
            if (arrayList == null || dialogViewRead.ttsIndex >= arrayList.size - 1) {
                dialogViewRead.setTtsState(0, false)
            } else {
                dialogViewRead.startReading(false)
            }
        }

        @Deprecated("Deprecated in Java")
        override fun onError(utteranceId: String?) {
            this@DialogViewRead.setTtsState(0, false)
        }

        override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
            val dialogViewRead = this@DialogViewRead
            val ttsStartPos = dialogViewRead.ttsStartPos
            val rangeStart = ttsStartPos + start
            dialogViewRead.rangeStart = rangeStart
            val rangeEnd = ttsStartPos + end
            dialogViewRead.rangeEnd = rangeEnd
            dialogViewRead.ttsEndPos = start
            if (dialogViewRead.ttsJump) {
                return
            }
            dialogViewRead.highlightRange(rangeStart, rangeEnd, dialogViewRead.ttsIndex, false)
        }

        override fun onStart(utteranceId: String?) {
            val dialogViewRead = this@DialogViewRead
            val ttsState = dialogViewRead.ttsState
            if (ttsState.toInt() == 1) {
                dialogViewRead.setTtsState(1, false)
                return
            }
            if (ttsState.toInt() == 2) {
                dialogViewRead.pauseTts(true)
            } else {
                val handler: Handler? = if (dialogViewRead.speakBarShown) null else dialogViewRead.uiHandler
                if (dialogViewRead.speakBarShown || handler == null) {
                    return
                }
                handler.postDelayed(StopTtsLaterStep(), 100L)
            }
        }
    }

    internal inner class InitTtsTask : Runnable {

        /**
         * Read aloud: with new paragraphs ([.s2]) splits them into text-to-speech chunks
         * (at line, sentence, comma or word breaks within the engine's input limit); otherwise
         * moves to the next chunk. Then speaks the current chunk.
         */
        override fun run() {
            val view = this@DialogViewRead
            val paragraphs = view.paragraphQueue
            view.paragraphQueue = null
            view.initTts()
            if (view.ttsEngine == null) {
                return
            }
            view.ttsState = 1
            view.speakMode = true
            try {
                if (paragraphs != null) {
                    view.ttsParagraphs = ttsChunks(paragraphs, view.autoTrans)
                    view.ttsIndex = 0
                } else {
                    view.ttsIndex++
                }
                val current = if (view.ttsParagraphs != null && view.ttsIndex >= 0 && view.ttsIndex < view.ttsParagraphs!!.size)
                    view.ttsParagraphs!!.get(view.ttsIndex) as TtsItem?
                else
                    null
                val speech = if (current != null) current.speech else null
                if (TextUtils.isEmpty(speech)) {
                    view.ttsState = 0
                    view.speakMode = false
                    view.ttsParagraphs = null
                    view.ttsIndex = 0
                    view.setTtsState(0, false)
                    return
                }
                if (view.ttsEngine!!.isSpeaking()) {
                    view.ttsEngine!!.stop()
                }
                view.applyTtsLang()
                if (view.ttsState.toInt() == 1) {
                    if (view.ttsEngine!!.speak(
                            speech,
                            TextToSpeech.QUEUE_FLUSH,
                            null,
                            "0"
                        ) == TextToSpeech.SUCCESS
                    ) {
                        view.setTtsState(1, true)
                    } else {
                        view.ttsState = 0
                        view.speakMode = false
                    }
                    if (view.ttsState.toInt() == 0) {
                        view.ttsParagraphs = null
                        view.ttsIndex = 0
                        view.setTtsState(0, false)
                    }
                }
            } catch (unused: Exception) {
                view.ttsState = 0
                view.speakMode = false
            }
        }
    }

    internal inner class PageGestures : SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val insideView: Boolean
            val dialogViewRead = this@DialogViewRead
            if (!dialogViewRead.miniMode && dialogViewRead.page != null) {
                val linearLayout = dialogViewRead.speakRow
                if (linearLayout == null || linearLayout.getVisibility() != View.VISIBLE) {
                    insideView = false
                } else {
                    insideView = isInsideView(
                        e.getX().toInt(),
                        e.getY().toInt(),
                        0,
                        dialogViewRead.speakRow
                    )
                }
                if (!insideView) {
                    val index = ReadSettings.textSize.value
                    dialogViewRead.setTextSize(if (index <= 90 || index >= 110) 100 else 200)
                    return true
                }
            }
            return false
        }

        override fun onFling(down: MotionEvent?, move: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.miniMode || dialogViewRead.page == null) {
                return false
            }
            return super.onFling(down, move, velocityX, velocityY)
        }

        override fun onScroll(down: MotionEvent?, move: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.miniMode || dialogViewRead.page == null) {
                return false
            }
            return super.onScroll(down, move, distanceX, distanceY)
        }

        override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
            val insideView: Boolean
            val dialogViewRead = this@DialogViewRead
            if (!dialogViewRead.miniMode && dialogViewRead.content != null && !dialogViewRead.hasDialog() && dialogViewRead.savePopup == null && dialogViewRead.morePopup == null && dialogViewRead.saveMenu == null && dialogViewRead.moreMenu == null) {
                if (isInsideView(
                        e.getX().toInt(),
                        e.getY().toInt(),
                        0,
                        dialogViewRead.content
                    ) && dialogViewRead.speakBar != null
                ) {
                    val linearLayout = dialogViewRead.speakRow
                    if (linearLayout == null || linearLayout.getVisibility() != View.VISIBLE) {
                        insideView = false
                    } else {
                        insideView = isInsideView(
                            e.getX().toInt(),
                            e.getY().toInt(),
                            0,
                            dialogViewRead.speakRow
                        )
                    }
                    if (!insideView) {
                        val speakBarShown = dialogViewRead.speakBar!!.isShowingNow()
                        val accent = !speakBarShown
                        if (!speakBarShown) {
                            dialogViewRead.updateBottomRows()
                        }
                        dialogViewRead.speakBar!!.toggle(accent)
                        return true
                    }
                }
            }
            return false
        }
    }

    internal inner class HideSpeakBarTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            try {
                if (!dialogViewRead.speakMode) {
                    dialogViewRead.hideSpeakBar()
                }
                val myButtonImage = dialogViewRead.speakFocusBtn
                if (myButtonImage != null) {
                    myButtonImage.setLoad(false)
                    dialogViewRead.speakFocusBtn = null
                }
                dialogViewRead.updatePlayIcon()
            } catch (unused: Exception) {
            }
        }
    }

    internal inner class ReleaseStep : SetDeskListener {
        override fun onChanged(deskMode: Boolean) {
            val dialogViewRead = this@DialogViewRead
            val mainTransText = dialogViewRead.transText
            if (mainTransText != null) {
                mainTransText.release()
                dialogViewRead.transText = null
            }
            if (dialogViewRead.page == null) {
                return
            }
            dialogViewRead.transDone = true
            if (dialogViewRead.transFromSpeak) {
                dialogViewRead.transFromSpeak = false
                dialogViewRead.showCoverProgress()
                dialogViewRead.runInBackground(dialogViewRead.HideCoverProgressTask())
            } else {
                dialogViewRead.startReading(true)
            }
            val myButtonImage = dialogViewRead.speakFocusBtn
            if (myButtonImage != null) {
                myButtonImage.setLoad(false)
                dialogViewRead.speakFocusBtn = null
            }
            dialogViewRead.updatePlayIcon()
        }
    }

    internal inner class InjectPageJsTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.page != null) {
                val pageScript = nativeJs14()
                dialogViewRead.pageJs = pageScript
                if (TextUtils.isEmpty(pageScript)) {
                    return
                }
                evalJsNow(dialogViewRead.page, dialogViewRead.pageJs, true)
            }
        }
    }

    internal inner class ParseFloatOrTask(@JvmField val outer: DialogViewRead,
                                          @JvmField val range: String?
    ) : Runnable {
        override fun run() {
            val range = this.range
            val rangeParts: Array<String?>? = if (TextUtils.isEmpty(range)) null else range!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
            if (TextUtils.isEmpty(range) || rangeParts == null || rangeParts.size != 2) {
                return
            }
            val dialogViewRead = this.outer
            val partTop =
                Math.round(MainUtil.dpToPx(dialogViewRead.ctx, MainUtil.parseFloatOr(rangeParts[0], -1.0f)))
            val partBottom =
                Math.round(MainUtil.dpToPx(dialogViewRead.ctx, MainUtil.parseFloatOr(rangeParts[1], -1.0f)))
            val webNestView: WebNestView? = if (partTop == -1 || partBottom == -1) null else dialogViewRead.page
            if (partTop == -1 || partBottom == -1 || webNestView == null) {
                return
            }
            val scrollY = webNestView.getScrollY() - MainApp.dp16
            val dp4Px = MainApp.dp4
            val top = (scrollY - dp4Px) + partTop
            val bottom = dp4Px + scrollY + partBottom
            val scrollUpTo = max(0, top - dialogViewRead.barInset)
            if (scrollUpTo < scrollY) {
                dialogViewRead.page!!.scrollTo(0, scrollUpTo)
                return
            }
            val scrollDownTo = min(top, bottom - (dialogViewRead.content!!.getHeight() - (MainApp.dp8 * 15)))
            if (scrollDownTo > scrollY) {
                dialogViewRead.page!!.scrollTo(0, scrollDownTo)
            }
        }
    }

    internal inner class StartReadingTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val arrayList: ArrayList<WebReadTask.ReadItem?>? = if (!dialogViewRead.htmlLoading || !dialogViewRead.pageVisible || dialogViewRead.transRunning || !dialogViewRead.alive) null else dialogViewRead.paragraphList
            if (!dialogViewRead.htmlLoading || !dialogViewRead.pageVisible || dialogViewRead.transRunning || !dialogViewRead.alive || arrayList == null || arrayList.isEmpty()) {
                return
            }
            if (!ReadSettings.autoTrans.value || dialogViewRead.autoTrans) {
                dialogViewRead.htmlLoading = false
                if (dialogViewRead.ttsState.toInt() == 0) {
                    dialogViewRead.partPlay = false
                    if (!dialogViewRead.autoTrans || dialogViewRead.transDone) {
                        dialogViewRead.startReading(true)
                    } else {
                        dialogViewRead.translatePage(false)
                    }
                    val myFadeFrame = dialogViewRead.speakBar
                    if (myFadeFrame != null) {
                        myFadeFrame.hide(true)
                    }
                }
            }
        }
    }

    internal inner class DismissSaveMenuStep : MyPopupListener {
        override fun onClose() {
            this@DialogViewRead.dismissSaveMenu()
        }

        override fun onItem(view: View?, id: Int): Boolean {
            val dialogViewRead = this@DialogViewRead
            if (id != 0) {
                if (dialogViewRead.autoTrans && !dialogViewRead.transDone) {
                    dialogViewRead.translatePage(true)
                    return true
                }
                dialogViewRead.showCoverProgress()
                dialogViewRead.runInBackground(dialogViewRead.HideCoverProgressTask())
                return true
            }
            if (dialogViewRead.activity == null || dialogViewRead.hasDialog()) {
                return true
            }
            val dialogPrintPage = dialogViewRead.printDialog
            if (dialogPrintPage != null) {
                dialogPrintPage.dismiss()
                dialogViewRead.printDialog = null
            }
            val printDialog = DialogPrintPage(
                dialogViewRead.activity,
                dialogViewRead.pageTitle,
                null,
                dialogViewRead.PrintPathChange()
            )
            dialogViewRead.printDialog = printDialog
            printDialog.setOnDismissListener(dialogViewRead.PrintDialogDismiss())
            return true
        }
    }

    internal inner class DismissMoreMenuStep : MyPopupListener {
        override fun onClose() {
            this@DialogViewRead.dismissMoreMenu()
        }

        override fun onItem(view: View?, id: Int): Boolean {
            val dialogViewRead = this@DialogViewRead
            if (id != 0) {
                if (id == 1) {
                    if (dialogViewRead.activity != null && !dialogViewRead.hasDialog()) {
                        val dialogSetTts = dialogViewRead.ttsDialog
                        if (dialogSetTts != null) {
                            dialogSetTts.dismiss()
                            dialogViewRead.ttsDialog = null
                        }
                        if (dialogViewRead.ttsState.toInt() == 1) {
                            dialogViewRead.pauseTts(true)
                        }
                        val ttsDialog = DialogSetTts(dialogViewRead.activity)
                        dialogViewRead.ttsDialog = ttsDialog
                        ttsDialog.setOnDismissListener(dialogViewRead.TtsDialogDismiss())
                        return true
                    }
                } else {
                    if (id == 2) {
                        if (ZtriSettings.readAccent.value) {
                            dialogViewRead.clearSelection()
                        }
                        val accent = !ZtriSettings.readAccent.value
                        ZtriSettings.edit(dialogViewRead.ctx) { set(ZtriSettings.readAccent, accent) }
                        return true
                    }
                    if (id == 3) {
                        val autoSpeak = !ReadSettings.autoSpeak.value
                        ReadSettings.edit(dialogViewRead.ctx) { set(ReadSettings.autoSpeak, autoSpeak) }
                        if (ReadSettings.autoSpeak.value && ReadSettings.guideTts.value && dialogViewRead.activity != null && !dialogViewRead.hasDialog()) {
                            val dialogConfirm = dialogViewRead.confirmDialog
                            if (dialogConfirm != null) {
                                dialogConfirm.dismiss()
                                dialogViewRead.confirmDialog = null
                            }
                            val confirmDialog = DialogConfirm(
                                dialogViewRead.activity,
                                AppR.string.auto_speak,
                                AppR.string.auto_speak_guide,
                                dialogViewRead.ConfirmResult()
                            )
                            dialogViewRead.confirmDialog = confirmDialog
                            confirmDialog.setOnDismissListener(dialogViewRead.ConfirmDialogDismiss())
                            return true
                        }
                    } else if (id == 4) {
                        val autoTrans = !ReadSettings.autoTrans.value
                        ReadSettings.edit(dialogViewRead.ctx) { set(ReadSettings.autoTrans, autoTrans) }
                        if (ReadSettings.autoTrans.value) {
                            dialogViewRead.setTransJs(true)
                            return true
                        }
                    }
                }
            } else if (dialogViewRead.activity != null) {
                val intent = Intent(dialogViewRead.ctx, com.mycompany.app.ui.settings.FontSettingsActivity::class.java as Class<*>)
                intent.putExtra("EXTRA_PAGE", true)
                dialogViewRead.activity!!.startActivity(intent)
                return true
            }
            return true
        }
    }

    internal inner class PrintPathChange : PathChangeListener {
        override fun onPath(path: String?) {
            val pathEmpty = TextUtils.isEmpty(path)
            val dialogViewRead = this@DialogViewRead
            if (pathEmpty) {
                showToast(dialogViewRead.ctx, AppR.string.input_name)
                return
            }
            val dialogPrintPage = dialogViewRead.printDialog
            if (dialogPrintPage != null) {
                dialogPrintPage.dismiss()
                dialogViewRead.printDialog = null
            }
            val dialogReadListener = dialogViewRead.readListener
            if (dialogReadListener != null) {
                dialogReadListener.printPage(dialogViewRead.page, path)
            }
        }
    }

    internal inner class PrintDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogPrintPage = dialogViewRead.printDialog
            if (dialogPrintPage != null) {
                dialogPrintPage.dismiss()
                dialogViewRead.printDialog = null
            }
        }
    }

    internal inner class HideCoverProgressTask : Runnable {

        internal inner class ShowPendingListStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                val list = dialogViewRead.pendingList
                dialogViewRead.pendingList = null
                dialogViewRead.hideCoverProgress()
                if (dialogViewRead.activity == null || dialogViewRead.hasDialog()) {
                    return
                }
                val dialogSaveSource = dialogViewRead.saveSourceDialog
                if (dialogSaveSource != null) {
                    dialogSaveSource.dismiss()
                    dialogViewRead.saveSourceDialog = null
                }
                if (list == null || list.isEmpty()) {
                    showToast(dialogViewRead.ctx, AppR.string.empty)
                    return
                }
                val saveSourceDialog = DialogSaveSource(
                    dialogViewRead.activity,
                    dialogViewRead.pageTitle,
                    null,
                    list,
                    dialogViewRead.SavePageListener()
                )
                dialogViewRead.saveSourceDialog = saveSourceDialog
                saveSourceDialog.setOnDismissListener(dialogViewRead.SaveSourceDialogDismiss())
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.pendingList = dialogViewRead.textList
            val handler = dialogViewRead.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ShowPendingListStep())
        }
    }

    internal inner class ArticleLoadListener : WebReadListener {

        internal inner class OpenInNewTabStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                val dialogReadListener = dialogViewRead.readListener
                if (dialogReadListener != null) {
                    dialogReadListener.openInNewTab(dialogViewRead.pageUrl)
                }
            }
        }

        override fun onFailed() {
            this@DialogViewRead.hideCoverProgress()
        }

        override fun onArticle(pageUrl: String?, pageTitle: String?, html: String?, arrayList: ArrayList<com.mycompany.app.web.WebReadTask.ReadItem?>?) {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.pageTitle = pageTitle
            dialogViewRead.paragraphList = arrayList
            if (!dialogViewRead.readFromHtml) {
                dialogViewRead.hideCoverProgress()
            }
            dialogViewRead.setWebHtml(html)
            if (dialogViewRead.htmlLoading) {
                dialogViewRead.startAutoTrans()
            }
        }

        override fun onEmpty() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.urlOverridden = true
        }

        /** Lead image found by the reader: remember it for the news item being read.  */
        override fun onImageUrl(imageUrl: String?) {
            val view = this@DialogViewRead
            view.newsImage = imageUrl
            if (!view.readFromHtml) {
                return
            }
            if (!TextUtils.isEmpty(imageUrl)) {
                val news = DataNews.instance(view.ctx)
                if (view.newsIndex >= 0 && news.items != null && view.newsIndex < news.items!!.size) {
                    val item = news.items!!.get(view.newsIndex)
                    if (item != null) {
                        item.imageUrl = imageUrl
                        news.loadedAt = System.currentTimeMillis()
                    }
                }
            }
            if (view.uiHandler != null) {
                view.uiHandler!!.post(OpenInNewTabStep())
            }
        }
    }

    internal inner class SavePageListener : DownPageListener {
        override fun onSave(url: String?, path: String?, bitmap: Bitmap?) {
            val dialogViewRead = this@DialogViewRead
            val handler: Handler? = if (dialogViewRead.miniMode) null else dialogViewRead.uiHandler
            if (dialogViewRead.miniMode || handler == null) {
                return
            }
            handler.post(SavedSourceStep(dialogViewRead, path))
        }
    }

    internal inner class SaveSourceDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogSaveSource = dialogViewRead.saveSourceDialog
            if (dialogSaveSource != null) {
                dialogSaveSource.dismiss()
                dialogViewRead.saveSourceDialog = null
            }
        }
    }

    internal inner class BrightDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogSeekBright = dialogViewRead.brightDialog
            if (dialogSeekBright != null) {
                dialogSeekBright.dismiss()
                dialogViewRead.brightDialog = null
            }
            dialogViewRead.updateSpeakBar()
        }
    }

    internal inner class TextSizeSeek : DialogSeekListener {
        override fun onValue(newValue: Int) {
            this@DialogViewRead.setTextSize(newValue)
        }
    }

    internal inner class TextSizeDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogSeekText = dialogViewRead.textDialog
            if (dialogSeekText != null) {
                dialogSeekText.dismiss()
                dialogViewRead.textDialog = null
            }
            dialogViewRead.updateSpeakBar()
        }
    }

    internal inner class AudioDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogSeekAudio = dialogViewRead.audioDialog
            if (dialogSeekAudio != null) {
                dialogSeekAudio.dismiss()
                dialogViewRead.audioDialog = null
            }
            dialogViewRead.updateSpeakBar()
        }
    }

    internal inner class TtsDialogDismiss : DialogInterface.OnDismissListener {

        internal inner class SpeakAfterTtsDialogStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                if (dialogViewRead.alive) {
                    dialogViewRead.speakNow()
                }
            }
        }

        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogSetTts = dialogViewRead.ttsDialog
            if (dialogSetTts != null) {
                dialogSetTts.dismiss()
                dialogViewRead.ttsDialog = null
            }
            if (!MainUtil.sameObject(TtsSettings.ttsLang.value, dialogViewRead.ttsLang) || (TtsSettings.ttsRate.value).compareTo(dialogViewRead.ttsRateSet) != 0 || (TtsSettings.ttsPitch.value).compareTo(dialogViewRead.ttsPitchSet) != 0
            ) {
                dialogViewRead.applyTtsSettings()
                dialogViewRead.ttsLang = TtsSettings.ttsLang.value
            }
            val handler: Handler? = if (dialogViewRead.ttsState.toInt() != 2) null else dialogViewRead.uiHandler
            if (dialogViewRead.ttsState.toInt() != 2 || handler == null) {
                return
            }
            handler.post(SpeakAfterTtsDialogStep())
        }
    }

    internal inner class ConfirmResult : DialogConfListener {
        override fun onResult(confirmed: Boolean) {
            val dialogViewRead = this@DialogViewRead
            if (confirmed) {
                ReadSettings.edit(dialogViewRead.ctx) { set(ReadSettings.guideTts, false) }
            }
            val dialogConfirm = dialogViewRead.confirmDialog
            if (dialogConfirm != null) {
                dialogConfirm.dismiss()
                dialogViewRead.confirmDialog = null
            }
        }
    }

    internal inner class ConfirmDialogDismiss : DialogInterface.OnDismissListener {
        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogConfirm = dialogViewRead.confirmDialog
            if (dialogConfirm != null) {
                dialogConfirm.dismiss()
                dialogViewRead.confirmDialog = null
            }
        }
    }

    internal inner class UnlockClickTask : Runnable {
        override fun run() {
            this@DialogViewRead.clickLocked = false
        }
    }

    internal inner class ReadProgressListener : ReadWebListener {
        override fun onProgress(progress: Int) {
            this@DialogViewRead.setProgressBar(progress)
        }

        override fun onDone() {
            val dialogViewRead = this@DialogViewRead
            val myProgressBar = dialogViewRead.progressBar
            if (myProgressBar == null) {
                return
            }
            if (myProgressBar.drawSkipped) {
                dialogViewRead.showCoverProgress()
            } else {
                dialogViewRead.coverLoading = true
            }
        }
    }

    internal inner class HighlightRangeTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val ttsIndex = if (!dialogViewRead.speakMode) 0 else dialogViewRead.ttsMoveIndex
            if (!dialogViewRead.speakMode || ttsIndex == -1234) {
                return
            }
            dialogViewRead.ttsMoveIndex = -1234
            dialogViewRead.ttsJump = false
            if (dialogViewRead.ttsState.toInt() != 2) {
                dialogViewRead.highlightRange(dialogViewRead.rangeStart, dialogViewRead.rangeEnd, dialogViewRead.ttsIndex, false)
            } else {
                dialogViewRead.ttsIndex = ttsIndex - 1
                dialogViewRead.startReading(false)
            }
        }
    }

    internal inner class SpeakNowDismiss : DialogInterface.OnDismissListener {

        internal inner class SpeakAfterDismissStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                if (dialogViewRead.alive) {
                    dialogViewRead.speakNow()
                }
            }
        }

        override fun onDismiss(dialogInterface: DialogInterface?) {
            val dialogViewRead = this@DialogViewRead
            val dialogViewTrans = dialogViewRead.transDialog
            if (dialogViewTrans != null) {
                dialogViewTrans.dismiss()
                dialogViewRead.transDialog = null
            }
            dialogViewRead.transPickLast = AlbumSettings.transPick.value
            val handler: Handler? = if (dialogViewRead.ttsState.toInt() != 2) null else dialogViewRead.uiHandler
            if (dialogViewRead.ttsState.toInt() != 2 || handler == null) {
                return
            }
            handler.post(SpeakAfterDismissStep())
        }
    }

    internal inner class PageLongClick : OnLongClickListener {
        override fun onLongClick(view: View?): Boolean {
            return this@DialogViewRead.speakMode
        }
    }

    internal inner class PageListener : WebViewListener {
        override fun onCrashAction(action: Int) {
        }

        override fun openAdvanced() {
        }

        override fun onProgress(progress: Int) {
            val dialogViewRead = this@DialogViewRead
            val view = dialogViewRead.customView
            var offset = 0
            if (view != null) {
                view.setVisibility(if (progress > 0) View.VISIBLE else View.GONE)
            }
            val scrollBase = dialogViewRead.scrollBase
            val fullView = dialogViewRead.fullView
            val height = if (fullView != null && fullView.getVisibility() == View.VISIBLE) dialogViewRead.fullView!!.getHeight() else 0
            if (fullView != null && fullView.getVisibility() == View.VISIBLE && height != 0) {
                val scrollShift = (progress - scrollBase) + dialogViewRead.scrollShift
                dialogViewRead.scrollShift = scrollShift
                if (scrollShift > height) {
                    dialogViewRead.scrollShift = height
                    offset = height
                } else if (scrollShift < 0) {
                    dialogViewRead.scrollShift = 0
                } else {
                    offset = scrollShift
                }
                dialogViewRead.fullView!!.setAlpha(1.0f - (offset / height))
                dialogViewRead.fullView!!.setTranslationY(dialogViewRead.scrollShift.toFloat())
            }
            dialogViewRead.scrollBase = progress
            val runnable = dialogViewRead.scrollRunnable
            val myScrollBar = dialogViewRead.scrollBar
            if (myScrollBar == null || dialogViewRead.scrollLocked) {
                return
            }
            dialogViewRead.scrollLocked = true
            myScrollBar.removeCallbacks(runnable)
            dialogViewRead.scrollBar!!.post(runnable)
        }

        override fun canPull(): Boolean {
            return false
        }

        override fun onScrollStop() {
        }

        override fun onBlockCount(count: Int) {
        }

        override fun onPull(x: Float, y: Float, action: Int): Boolean {
            return false
        }

        override fun onPageCommit(pageUrl: String?) {
        }
    }

    internal inner class ShowCoverProgressTask : Runnable {

        internal inner class ShowCoverProgressStep : Runnable {

            internal inner class PageSettingsStep : Runnable {

                internal inner class LoadReadPageStep : Runnable {
                    override fun run() {
                        val dialogViewRead = this@DialogViewRead
                        if (dialogViewRead.page != null) {
                            val readFromHtml = dialogViewRead.readFromHtml
                            if (readFromHtml) {
                                if (!readFromHtml || dialogViewRead.readTask == null) {
                                    return
                                }
                                dialogViewRead.showCoverProgress()
                                dialogViewRead.readTask!!.createWebView()
                                return
                            }
                            val dialogReadListener: DialogReadListener? = if (readFromHtml) null else dialogViewRead.readListener
                            val webNestViewA: WebNestView? = if (readFromHtml || dialogReadListener == null) null else dialogReadListener.currentWeb()
                            if (readFromHtml || dialogReadListener == null || webNestViewA == null) {
                                return
                            }
                            dialogViewRead.showCoverProgress()
                            webNestViewA.evaluateJavascript(
                                "document.documentElement.lang",
                                dialogViewRead.ArticleHtmlCallback()
                            )
                        }
                    }
                }

                override fun run() {
                    val showCoverProgressTask = this@ShowCoverProgressTask
                    val dialogViewRead = this@DialogViewRead
                    val webNestView = dialogViewRead.page
                    if (webNestView != null) {
                        dialogViewRead.setWebSettin3(webNestView)
                        val handler = this@DialogViewRead.uiHandler
                        if (handler == null) {
                            return
                        }
                        handler.post(LoadReadPageStep())
                    }
                }
            }

            override fun run() {
                val dialogViewRead = this@DialogViewRead
                val webNestView = dialogViewRead.page
                if (webNestView != null) {
                    dialogViewRead.setWebSettin2(webNestView)
                    val handler = dialogViewRead.uiHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(PageSettingsStep())
                }
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val webNestView = dialogViewRead.page
            if (webNestView != null) {
                dialogViewRead.setWebSetting(webNestView)
                val handler = dialogViewRead.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(ShowCoverProgressStep())
            }
        }
    }

    internal inner class ClearCacheTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            MainUtil.clearWebCache(dialogViewRead.page)
            dialogViewRead.finishHandled = false
        }
    }

    internal inner class HandleBackTask : Runnable {
        override fun run() {
            this@DialogViewRead.handleBack()
        }
    }

    internal inner class RemoveJsBridgeTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val webNestView = dialogViewRead.page
            if (webNestView == null) {
                return
            }
            dialogViewRead.loadPending = false
            webNestView.removeJavascriptInterface("android")
        }
    }

    internal inner class AddJsBridgeTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val webNestView = dialogViewRead.page
            if (webNestView == null) {
                return
            }
            dialogViewRead.loadPending = true
            webNestView.addJavascriptInterface(dialogViewRead.WebAppInterface(), "android")
        }
    }

    internal inner class SetProgressBarTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.setProgressBar(dialogViewRead.progressValue)
        }
    }

    internal inner class EnterMiniModeTask : Runnable {

        internal inner class EnterMiniModeStep : Runnable {
            override fun run() {
                this@DialogViewRead.setMiniMode(true)
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.buildMiniBar()
            val handler = dialogViewRead.uiHandler
            if (handler == null) {
                return
            }
            handler.post(EnterMiniModeStep())
        }
    }

    internal inner class UpdatePosTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val myScrollBar = dialogViewRead.scrollBar
            if (myScrollBar != null) {
                myScrollBar.updatePos(0, 0)
            }
            dialogViewRead.scrollLocked = false
        }
    }

    internal inner class SetupVideoFrameTask : Runnable {

        internal inner class SetupFrameStep : Runnable {

            internal inner class SetupFrameTask : Runnable {

                internal inner class SetupImageFrameStep : Runnable {
                    override fun run() {
                        val dialogViewRead = this@DialogViewRead
                        val webVideoImage = dialogViewRead.videoImage
                        if (webVideoImage == null || !dialogViewRead.alive) {
                            return
                        }
                        webVideoImage.setupFrame(null, false, VideoDownloadListener())
                    }
                }

                override fun run() {
                    val setupVideoFrameTask = this@SetupVideoFrameTask
                    val dialogViewRead = this@DialogViewRead
                    if (dialogViewRead.videoImage == null || !dialogViewRead.alive) {
                        return
                    }
                    dialogViewRead.applyWindow(-16777216, false)
                    val handler = this@DialogViewRead.uiHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(SetupImageFrameStep())
                }
            }

            override fun run() {
                val dialogViewRead = this@DialogViewRead
                if (dialogViewRead.videoImage == null || !dialogViewRead.alive) {
                    return
                }
                if (!AppState.darkUi) {
                    dialogViewRead.setWindowColor(-16777216)
                }
                val handler = dialogViewRead.uiHandler
                if (handler == null) {
                    return
                }
                handler.post(SetupFrameTask())
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.videoImage == null || !dialogViewRead.alive) {
                return
            }
            dialogViewRead.stopTts()
            val handler = dialogViewRead.uiHandler
            if (handler == null) {
                return
            }
            handler.post(SetupFrameStep())
        }
    }

    internal inner class VideoDownloadListener : VideoFrameListener {
        override fun onDownload() {
        }
    }

    internal inner class ReleaseVideoImageTask : Runnable {

        internal inner class ApplyWindowStep : Runnable {

            internal inner class RestoreWindowStep : Runnable {

                internal inner class RotationLockStep : Runnable {

                    internal inner class WindowColorStep : Runnable {

                        internal inner class ClearFocusStep : Runnable {

                            internal inner class ApplyDarkUiStep : Runnable {
                                override fun run() {
                                    val dialogViewRead = this@DialogViewRead
                                    if (dialogViewRead.videoImage == null && dialogViewRead.alive) {
                                        val darkUi = AppState.darkUi
                                        dialogViewRead.applyWindow(if (darkUi) -16777216 else -1, darkUi)
                                    }
                                }
                            }

                            override fun run() {
                                val rotationLockStep = this@RotationLockStep
                                val dialogViewRead = this@DialogViewRead
                                if (dialogViewRead.videoImage == null && dialogViewRead.alive) {
                                    val webNestView = dialogViewRead.page
                                    if (webNestView != null) {
                                        webNestView.clearFocus()
                                    }
                                    val handler = this@DialogViewRead.uiHandler
                                    if (handler == null) {
                                        return
                                    }
                                    handler.post(ApplyDarkUiStep())
                                }
                            }
                        }

                        override fun run() {
                            val restoreWindowStep = this@RestoreWindowStep
                            val dialogViewRead = this@DialogViewRead
                            if (dialogViewRead.videoImage == null && dialogViewRead.alive) {
                                if (!AppState.darkUi) {
                                    dialogViewRead.setWindowColor(-1)
                                }
                                val handler = this@DialogViewRead.uiHandler
                                if (handler == null) {
                                    return
                                }
                                handler.post(ClearFocusStep())
                            }
                        }
                    }

                    override fun run() {
                        val applyWindowStep = this@ApplyWindowStep
                        val dialogViewRead = this@DialogViewRead
                        if (dialogViewRead.videoImage == null && dialogViewRead.alive) {
                            applyRotationLock(dialogViewRead.videoTypeOut, dialogViewRead.activity, dialogViewRead.videoRestore)
                            val handler = this@DialogViewRead.uiHandler
                            if (handler == null) {
                                return
                            }
                            handler.post(WindowColorStep())
                        }
                    }
                }

                override fun run() {
                    val releaseVideoImageTask = this@ReleaseVideoImageTask
                    val dialogViewRead = this@DialogViewRead
                    if (dialogViewRead.videoImage == null && dialogViewRead.alive) {
                        val mainActivity = dialogViewRead.activity
                        val videoRestore = dialogViewRead.videoRestore
                        if (mainActivity != null && !videoRestore) {
                            MainUtil.applyWindowBrightness(mainActivity.getWindow(), PdfSettings.brightness.value, PdfSettings.userBrightness.value)
                        }
                        this@DialogViewRead.runInBackground(RotationLockStep())
                    }
                }
            }

            override fun run() {
                val dialogViewRead = this@DialogViewRead
                if (dialogViewRead.videoImage == null && dialogViewRead.alive) {
                    reloadWithDesktopMode(dialogViewRead.page, dialogViewRead.videoTypeOut, dialogViewRead.videoRestore)
                    val handler = dialogViewRead.uiHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(RestoreWindowStep())
                }
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val webVideoImage = dialogViewRead.videoImageOut
            dialogViewRead.videoImageOut = null
            if (webVideoImage != null) {
                webVideoImage.releaseFrame()
            }
            val handler = dialogViewRead.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ApplyWindowStep())
        }
    }

    internal inner class CacheTransJsTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.page == null) {
                return
            }
            dialogViewRead.transJsCache = dialogViewRead.transJs
            dialogViewRead.setTransJs(false)
        }
    }

    internal inner class TransLangFromCookieCallback : ValueCallback<String> {
        override fun onReceiveValue(cookies: String?) {
            val cookieLang = transLangFromCookie(cookies)
            if (TextUtils.isEmpty(cookieLang) || cookieLang == AlbumSettings.transCode.value) {
                return
            }
            AlbumSettings.edit(this@DialogViewRead.ctx) { set(AlbumSettings.transCode, cookieLang) }
        }
    }

    internal inner class ParseTransResultTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val jsResultE = dialogViewRead.jsResultE
            dialogViewRead.jsResultE = null
            if (dialogViewRead.page == null) {
                return
            }
            parseTransResult(dialogViewRead.ctx, jsResultE)
        }
    }

    internal inner class LangPrefixTask : Runnable {
        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val jsResultF = dialogViewRead.jsResultF
            dialogViewRead.jsResultF = null
            if (dialogViewRead.page == null || !TextUtils.isEmpty(dialogViewRead.detectedLang)) {
                return
            }
            val prefix = langPrefix(jsResultF)
            dialogViewRead.detectedLang = prefix
            if (prefix.length > 2) {
                hideDivsByClass(dialogViewRead.page, prefix)
            }
        }
    }

    internal inner class EvalLongPressJsTask(@JvmField val outer: DialogViewRead,
                                          @JvmField val script: String?
    ) : Runnable {
        override fun run() {
            evalLongPressJs(this.outer.page, this.script)
        }
    }

    internal inner class ShowTransControlTask : Runnable {

        internal inner class ShowTransControlStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                val webTransControl = dialogViewRead.transControl
                dialogViewRead.transControl = null
                if (webTransControl == null || dialogViewRead.sheetTransControl != null) {
                    return
                }
                dialogViewRead.sheetTransControl = webTransControl
                try {
                    val sheetTransControl = dialogViewRead.sheetTransControl
                    if (sheetTransControl != null) {
                        val transState = dialogViewRead.transState
                        sheetTransControl.showState(dialogViewRead.transLang, dialogViewRead.autoTrans, transState.toInt())
                    }
                    dialogViewRead.sheetTransControl!!.setListener(dialogViewRead.TransControlActions())
                    // The translate bar in a Compose sheet, page behind not dimmed (was a bare MyDialogBottom).
                    // place 3: 40 dp logo inset + one button row + margin
                    val barHeight = MainUtil.dpToPx(dialogViewRead.ctx, 40.0f).toInt() + MainApp.dp48 + MainApp.dp16
                    val sheet = com.mycompany.app.ui.browser.ViewSheetDialog(dialogViewRead.activity, dialogViewRead.sheetTransControl!!, false, barHeight)
                    dialogViewRead.bottomSheet = sheet
                    sheet.setOnDismissListener(dialogViewRead.TransSheetDismiss())
                } catch (unused: Exception) {
                }
            }
        }

        override fun run() {
            val dialogViewRead = this@DialogViewRead
            val webTransControl = dialogViewRead.transControl
            if (webTransControl != null) {
                webTransControl.applyMode(3)
            }
            val handler = dialogViewRead.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ShowTransControlStep())
        }
    }

    interface DialogReadListener {
        fun currentWeb(): WebNestView?

        fun openUrl(url: String?)

        fun openInNewTab(url: String?)

        fun onClosed()

        fun applyColors(topColor: Int, bottomColor: Int, backColor: Int)

        fun printPage(webView: WebView?, title: String?)

        fun onSpeak()

        fun isSpeaking(): Boolean
    }

    private inner class LocalChromeClient : WebChromeClient() {
        override fun onHideCustomView() {
            this@DialogViewRead.releaseVideo()
        }

        override fun onProgressChanged(webView: WebView?, newProgress: Int) {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.readFromHtml) {
                return
            }
            dialogViewRead.setProgressBar(newProgress)
        }

        override fun onShowCustomView(view: View?, customViewCallback: CustomViewCallback?) {
            if (view != null) {
                val dialogViewRead = this@DialogViewRead
                if (dialogViewRead.videoImage != null) {
                    if (customViewCallback != null) {
                        customViewCallback.onCustomViewHidden()
                    }
                } else if (dialogViewRead.page != null) {
                    val webVideoImage = WebVideoImage(dialogViewRead.activity)
                    dialogViewRead.videoImage = webVideoImage
                    webVideoImage.showVideo(
                        dialogViewRead.activity,
                        dialogViewRead,
                        dialogViewRead.page,
                        dialogViewRead.pageUrl,
                        6,
                        view,
                        customViewCallback
                    )
                    val handler = dialogViewRead.uiHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(dialogViewRead.SetupVideoFrameTask())
                }
            }
        }
    }

    class TtsItem {
        @JvmField
        var start: Int = 0
        @JvmField
        var end: Int = 0
        @JvmField
        var speech: String? = null
    }

    private inner class WebAppInterface {

        inner class ArticleReadyStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                if (dialogViewRead.ctx != null) {
                    dialogViewRead.hideCoverProgress()
                    val view: View? = if (dialogViewRead.alive) dialogViewRead.fullView else null
                    if (dialogViewRead.alive && view != null) {
                        if (AppState.darkUi) {
                            view.setBackgroundResource(AppR.drawable.trans_logo_short_back_dark)
                        } else {
                            view.setBackgroundResource(AppR.drawable.trans_logo_short_back_color)
                        }
                        if (dialogViewRead.transState.toInt() != 3 || !dialogViewRead.autoTrans) {
                            dialogViewRead.fullView!!.setVisibility(View.GONE)
                        } else if (dialogViewRead.fullView!!.getVisibility() != View.VISIBLE) {
                            dialogViewRead.scrollShift = 0
                            dialogViewRead.fullView!!.setAlpha(1.0f)
                            dialogViewRead.fullView!!.setTranslationY(0.0f)
                            dialogViewRead.fullView!!.setVisibility(View.VISIBLE)
                        }
                    }
                    val webTransControl = dialogViewRead.sheetTransControl
                    if (webTransControl != null) {
                        webTransControl.showState(
                            dialogViewRead.transLang,
                            dialogViewRead.autoTrans,
                            dialogViewRead.transState.toInt()
                        )
                    }
                    if (dialogViewRead.htmlLoading) {
                        dialogViewRead.startAutoTrans()
                    } else {
                        dialogViewRead.stopTts()
                    }
                    if (dialogViewRead.transState.toInt() != 1) {
                        val transPickLast = dialogViewRead.transPickLast
                        dialogViewRead.transPickLast = null
                        if (TextUtils.isEmpty(transPickLast)) {
                            val webNestView: WebNestView? = if (!TextUtils.isEmpty(AlbumSettings.transPick.value)) dialogViewRead.page else null
                            if (!TextUtils.isEmpty(AlbumSettings.transPick.value) && webNestView != null) {
                                webNestView.evaluateJavascript(
                                    "document.cookie",
                                    dialogViewRead.TransLangFromCookieCallback()
                                )
                            }
                        } else if (!TextUtils.isEmpty(transPickLast)) {
                            dialogViewRead.runInBackground(EvalLongPressJsTask(dialogViewRead, transPickLast))
                        }
                        val context = dialogViewRead.ctx
                        if (context != null && !DataTrans.instance(context).hasLanguages()) {
                            injectTransJs(dialogViewRead.page)
                        }
                        if (TextUtils.isEmpty(dialogViewRead.detectedLang)) {
                            checkTransClassJs(dialogViewRead.page)
                        }
                    }
                }
            }
        }

        @JavascriptInterface
        fun onJsResult(callback: String?, jsResultE: String?) {
            if (TextUtils.isEmpty(callback)) {
                return
            }
            val isTransList = callback == "onTransList"
            val dialogViewRead = this@DialogViewRead
            if (isTransList) {
                if (dialogViewRead.page == null || TextUtils.isEmpty(jsResultE)) {
                    return
                }
                dialogViewRead.jsResultE = jsResultE
                dialogViewRead.runInBackground(dialogViewRead.ParseTransResultTask())
                return
            }
            if (callback == "onTransClass") {
                if (dialogViewRead.page != null) {
                    if ("0" == jsResultE) {
                        dialogViewRead.detectedLang = "-"
                        return
                    }
                    if (TextUtils.isEmpty(jsResultE)) {
                        dialogViewRead.detectedLang = "-"
                        return
                    } else {
                        if (TextUtils.isEmpty(dialogViewRead.detectedLang)) {
                            dialogViewRead.jsResultF = jsResultE
                            dialogViewRead.runInBackground(dialogViewRead.LangPrefixTask())
                            return
                        }
                        return
                    }
                }
                return
            }
            if (callback == "onTtsMark") {
                val handler = dialogViewRead.uiHandler
                if (handler != null) {
                    handler.post(ParseFloatOrTask(dialogViewRead, jsResultE))
                    return
                }
                return
            }
            if (callback == "onPartPlay") {
                dialogViewRead.jsResultH = jsResultE
                val handler = dialogViewRead.uiHandler
                if (handler != null) {
                    handler.post(dialogViewRead.SetPartPlayTask())
                    return
                }
                return
            }
            if (callback == "onActionTrans") {
                dialogViewRead.jsResultI = jsResultE
                val handler = dialogViewRead.uiHandler
                if (handler != null) {
                    handler.post(dialogViewRead.ShowTransTask())
                }
            }
        }

        @JavascriptInterface
        fun onObserDet(transLang: String?, autoTrans: Int) {
            val dialogViewRead = this@DialogViewRead
            if (autoTrans == 0) {
                dialogViewRead.transState = 1.toByte()
            } else {
                dialogViewRead.transState = 3.toByte()
                dialogViewRead.autoTrans = autoTrans == 2
                dialogViewRead.transLang = transLang
                if (TextUtils.isEmpty(AlbumSettings.transPick.value)) {
                    AlbumSettings.editLocal { set(AlbumSettings.transPick, transLang) }
                    AlbumSettings.editLocal { set(AlbumSettings.transCode, "") }
                    saveTrans(dialogViewRead.ctx)
                }
                if (MainUtil.sameObject(dialogViewRead.transPickLast, transLang)) {
                    dialogViewRead.transPickLast = null
                }
            }
            dialogViewRead.transDone = false
            val handler = dialogViewRead.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ArticleReadyStep())
        }
    }

    init {
        this.showAnimEnd = SetValAnimShowTask()
        this.hideAnimEnd = SetValAnimHideTask()
        this.ttsMoveRunnable = HighlightRangeTask()
        this.progressRunnable = SetProgressBarTask()
        this.scrollRunnable = UpdatePosTask()
        this.miniAnimEnd = SetValAnimMiniTask()
        this.ctx = context
        this.uiHandler = Handler(Looper.getMainLooper())
        this.layoutRtl = isRtl(this.ctx)
        this.selRect = Rect()
        val arrayList: ArrayList<Rect?> = ArrayList()
        this.selRects = arrayList
        arrayList.add(this.selRect)
    }

    private val newsLocale: Locale?
        get() {
            val context = this.ctx
            val stringArray: Array<String>? = if (context != null) context.getResources()
                    .getStringArray(AppR.array.news_code) else null
            val i = if (context != null && stringArray != null) ZtwoSettings.newsLang.value else 0
            if (context != null && stringArray != null && i >= 0 && i < stringArray.size) {
                val str = stringArray[i]
                val colonIndex = str.lastIndexOf(58.toChar())
                val equalsIndex = str.lastIndexOf(61.toChar(), colonIndex)
                try {
                    return if (Build.VERSION.SDK_INT >= 36) Locale.of(
                        str.substring(
                            colonIndex + 1
                        ), str.substring(equalsIndex + 1, colonIndex)
                    ) else Locale(
                        str.substring(colonIndex + 1),
                        str.substring(equalsIndex + 1, colonIndex)
                    )
                } catch (unused: Exception) {
                }
            }
            return null
        }

    val partJs: String?
        /**
         * Read-aloud highlighting script, assembled once from native string parts (13 parts for the
         * translated view, 5 otherwise). Null when the native library is unavailable.
         */
        get() {
            if (this.autoTrans) {
                if (TextUtils.isEmpty(this.nativeScript13)) {
                    this.nativeScript13 = nativeScript(13, true)
                }
                return this.nativeScript13
            }
            if (TextUtils.isEmpty(this.nativeScript5)) {
                this.nativeScript5 = nativeScript(5, false)
            }
            return this.nativeScript5
        }

    private val partList: MutableList<ReadItem?>?
        get() {
            val startPart = this.partStart
            val startPos = this.partStartPos
            val endPart = this.partEnd
            val endPos = this.partEndPos
            val partCount = (endPart - startPart) + 1
            var arrayList: ArrayList<ReadItem?>? = null
            for (offset in 0..<partCount) {
                val paragraph = offset + startPart
                val arrayList2 = this.paragraphList
                val readItem =
                    if (arrayList2 == null || paragraph < 0 || paragraph >= arrayList2.size) null else arrayList2.get(
                        paragraph
                    )
                if (readItem != null) {
                    var partText = if (this.autoTrans) readItem.transText else readItem.articleTitle
                    val length = if (partText != null) partText.length else 0
                    if (partText != null && length != 0) {
                        val cutStart = if (paragraph == startPart) max(0, startPos) else 0
                        val cutEnd = if (paragraph == endPart) min(length, endPos) else length
                        if (cutStart < cutEnd) {
                            if (cutStart != 0 || cutEnd != length) {
                                partText = partText.substring(cutStart, cutEnd)
                            }
                            val readItem2 = ReadItem()
                            readItem2.index = readItem.index
                            readItem2.end = cutStart
                            if (this.autoTrans) {
                                readItem2.transText = partText
                            } else {
                                readItem2.articleTitle = partText
                            }
                            if (arrayList == null) {
                                arrayList = ArrayList()
                            }
                            arrayList.add(readItem2)
                        }
                    }
                }
            }
            return arrayList
        }

    val textList: MutableList<String?>?

        get() {
            val arrayList = this.paragraphList
            if (arrayList == null || arrayList.isEmpty()) {
                return null
            }
            val arrayList2: ArrayList<String?> = ArrayList()
            val size = arrayList.size
            var i = 0
            while (i < size) {
                val obj: Any? = arrayList.get(i)
                i++
                val readItem = obj as ReadItem?
                if (readItem != null) {
                    val str = if (this.autoTrans) readItem.transText else readItem.articleTitle
                    if (!TextUtils.isEmpty(str)) {
                        arrayList2.add(str)
                        arrayList2.add("\n")
                    }
                }
            }
            return arrayList2
        }

    val transJs: String?

        get() {
            if (!TextUtils.isEmpty(this.transJsCache)) {
                return this.transJsCache
            }
            val sbK3 = transScriptJs()
            if (sbK3 == null) {
                return null
            }
            val classJs = classSelectorJs(this.detectedLang)
            if (TextUtils.isEmpty(classJs)) {
                return null
            }
            sbK3.insert(0, classJs)
            return sbK3.toString()
        }

    private fun setAnimPos(miniAnimValue: kotlin.Float) {
        var miniX: Int
        var miniY: Int
        if (!this.alive || this.miniLayoutParams == null) {
            return
        }
        var posX = Math.round((this.maxX - this.minX) * miniAnimValue) + this.minX
        val posY = Math.round((this.maxY - this.minY) * miniAnimValue)
        var minY = this.minY
        val bottomY = posY + minY
        var maxX = this.maxX
        if (posX > maxX || posX < (this.minX.also { maxX = it })) {
            posX = maxX
        }
        val maxY = this.maxY
        if (bottomY > maxY) {
            minY = maxY
        } else if (bottomY >= minY) {
            minY = bottomY
        }
        val layoutParams = this.miniLayoutParams
        layoutParams!!.width = posX
        layoutParams.height = minY
        setLayoutParams(layoutParams)
        val leftX = ZtriSettings.readLtX.value
        val rightX = ZtriSettings.readRtX.value
        val upY = ZtriSettings.readUpY.value
        val downY = ZtriSettings.readDnY.value
        if (leftX >= 0.0f || rightX >= 0.0f || upY >= 0.0f || downY >= 0.0f) {
            miniX = Math.round((((this.maxX - posX).toFloat()) / (leftX + rightX)) * leftX)
            miniY = Math.round((((this.maxY - minY).toFloat()) / (upY + downY)) * upY)
        } else {
            miniX = (this.maxX - posX) / 2
            miniY = (this.maxY - minY) / 2
        }
        val right = miniX + posX
        val maxXNow = this.maxX
        if (right > maxXNow) {
            miniX = maxXNow - posX
        }
        val bottom = miniY + minY
        val maxYNow = this.maxY
        if (bottom > maxYNow) {
            miniY = maxYNow - minY
        }
        if (miniX < 0) {
            miniX = 0
        }
        if (miniY < 0) {
            miniY = 0
        }
        setX((miniX + this.miniX).toFloat())
        setY((miniY + this.miniY).toFloat())
    }

    fun setIconCurr(mini: Boolean) {
        if (lockClick()) {
            return
        }
        if (!isBusy(true)) {
            setTtsMove(0)
        } else if (mini) {
            showToast(this.ctx, AppR.string.wait_retry)
        }
    }

    fun setMiniMode(mini: Boolean) {
        val valueAnimatorOfFloat: ValueAnimator
        if (this.alive && this.miniAnim == null && this.miniMode != mini) {
            this.miniMode = mini
            val myWebBody = this.webBody
            if (myWebBody != null) {
                val paddingLeft = myWebBody.getPaddingLeft()
                val paddingTop = myWebBody.getPaddingTop()
                val width = (myWebBody.getWidth() - paddingLeft) - myWebBody.getPaddingRight()
                val height = (myWebBody.getHeight() - paddingTop) - myWebBody.getPaddingBottom()
                this.miniX = paddingLeft
                this.miniY = paddingTop
                this.maxX = width
                this.maxY = height
                applyMiniBarColors()
                if (this.showFromMini) {
                    this.showFromMini = false
                    setVisibility(View.VISIBLE)
                    this.content!!.setVisibility(View.GONE)
                    setValAnimMini(0.0f)
                    val handler: Handler? = if ((this.topBar == null || this.page == null)) this.uiHandler else null
                    if ((this.topBar == null || this.page == null) && handler != null) {
                        handler.post(CreatePageTask())
                        return
                    }
                    return
                }
                dismissDialogs()
                dismissSaveMenu()
                dismissMoreMenu()
                finishActionMode()
                this.miniAnimating = true
                if (this.miniMode) {
                    this.miniAnimValue = 1.0f
                    this.miniAnimPosted = false
                    valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f)
                    this.miniAnim = valueAnimatorOfFloat
                } else {
                    this.miniAnimValue = 0.0f
                    this.miniAnimPosted = false
                    valueAnimatorOfFloat = ValueAnimator.ofFloat(0.0f, 1.0f)
                    this.miniAnim = valueAnimatorOfFloat
                }
                valueAnimatorOfFloat.setDuration(300L)
                val miniMode = this.miniMode
                val valueAnimator = this.miniAnim
                if (miniMode) {
                    valueAnimator!!.interpolator = android.view.animation.AccelerateInterpolator()
                } else {
                    valueAnimator!!.interpolator = android.view.animation.DecelerateInterpolator()
                }
                this.miniAnim!!.addUpdateListener(MiniAnimUpdate())
                this.miniAnim!!.addListener(MiniAnimListener())
                this.miniAnim!!.start()
            }
        }
    }

    fun setPartPlay(jsResultH: String?) {
        if (TextUtils.isEmpty(jsResultH)) {
            showToast(this.ctx, AppR.string.empty)
            return
        }
        val parts: Array<String?> =
            jsResultH!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (parts.size != 4) {
            showToast(this.ctx, AppR.string.play_error)
            return
        }
        var startPart = parseColorSafe(parts[0])
        var startPos = parseColorSafe(parts[1])
        var endPart = parseColorSafe(parts[2])
        var endPos = parseColorSafe(parts[3])
        if (startPart == -1 || startPos == -1 || endPart == -1 || endPos == -1) {
            showToast(this.ctx, AppR.string.play_error)
            return
        }
        if (startPart == endPart && startPos == endPos) {
            showToast(this.ctx, AppR.string.play_error)
            return
        }
        if (startPart > endPart || (startPart == endPart && startPos > endPos)) {
            // selected backwards: swap the ends so the range runs forward
            val firstPart = endPart
            val firstPos = endPos
            endPart = startPart
            endPos = startPos
            startPart = firstPart
            startPos = firstPos
        }
        this.partPlay = true
        this.partStart = startPart
        this.partStartPos = startPos
        this.partEnd = endPart
        this.partEndPos = endPos
        if (!this.autoTrans || this.transDone) {
            startReading(true)
        } else {
            translatePage(false)
        }
        showSpeakBar(true)
    }

    fun setPos(force: Boolean) {
        val miniX: Int
        val miniY: Int
        val myWebBody: MyWebBody? = if (this.miniMode && this.alive) this.webBody else null
        if (this.miniMode && this.alive && myWebBody != null) {
            if (force || this.miniAnim == null) {
                val paddingLeft = myWebBody.getPaddingLeft()
                val paddingTop = myWebBody.getPaddingTop()
                val width = (myWebBody.getWidth() - paddingLeft) - myWebBody.getPaddingRight()
                val height = (myWebBody.getHeight() - paddingTop) - myWebBody.getPaddingBottom()
                if (!force && paddingLeft == this.miniX && paddingTop == this.miniY && width == this.maxX && height == this.maxY) {
                    return
                }
                this.miniX = paddingLeft
                this.miniY = paddingTop
                this.maxX = width
                this.maxY = height
                val leftX = ZtriSettings.readLtX.value
                val rightX = ZtriSettings.readRtX.value
                val upY = ZtriSettings.readUpY.value
                val downY = ZtriSettings.readDnY.value
                if (leftX >= 0.0f || rightX >= 0.0f || upY >= 0.0f || downY >= 0.0f) {
                    miniX = Math.round((((width - this.minX).toFloat()) / (leftX + rightX)) * leftX)
                    miniY = Math.round((((this.maxY - this.minY).toFloat()) / (upY + downY)) * upY)
                } else {
                    miniX = (width - this.minX) / 2
                    miniY = (height - this.minY) / 2
                }
                moveMini(miniX, miniY)
                if ((leftX).compareTo(ZtriSettings.readLtX.value) == 0 && (rightX).compareTo(ZtriSettings.readRtX.value) == 0 && (upY).compareTo(ZtriSettings.readUpY.value) == 0 && (downY).compareTo(ZtriSettings.readDnY.value) == 0
                ) {
                    return
                }
                saveMiniPos()
            }
        }
    }

    fun setProgressBar(newProgressValue: Int) {
        this.progressValue = newProgressValue
        val myProgressBar = this.progressBar
        if (myProgressBar != null) {
            val progressValue = Math.round(myProgressBar.progress)
            if (newProgressValue == 100 && progressValue == 100) {
                this.progressBar!!.setSkipDraw(true)
                if (this.coverLoading) {
                    this.coverLoading = false
                    val myCoverView = this.loadCover
                    if (myCoverView != null) {
                        myCoverView.showNow(true)
                        return
                    }
                    return
                }
                return
            }
            val myCoverView = this.loadCover
            if (myCoverView != null) {
                myCoverView.hideCover(false)
            }
            val progressBar = this.progressBar
            if (progressBar!!.drawSkipped) {
                progressBar.progress = 0.0f
                this.progressBar!!.setSkipDraw(false)
                setProgressBar(max(newProgressValue, 50))
            } else if (progressValue < newProgressValue) {
                progressBar.progress = (progressValue + 3).toFloat()
                val runnable = this.progressRunnable
                if (runnable != null) {
                    this.progressBar!!.removeCallbacks(runnable)
                    this.progressBar!!.post(runnable)
                }
            }
        }
    }

    fun setScaleGesture(scaleGestureDetector: ScaleGestureDetector?) {
        if (scaleGestureDetector == null || this.page == null) {
            return
        }
        var textSize = ReadSettings.textSize.value
        if (textSize == 0) {
            textSize = 100
        }
        var zoomPercent =
            Math.round(100.0f / ((1.0f - scaleGestureDetector.getScaleFactor()) + (100.0f / textSize)))
        var clamped = 50
        if (zoomPercent >= 50) {
            clamped = 500
            if (zoomPercent > 500) {
                zoomPercent = clamped
            }
        } else {
            zoomPercent = clamped
        }
        setTextSize(zoomPercent)
    }

    fun setTextSize(size: Int) {
        val webNestView = this.page
        if (webNestView == null || ReadSettings.textSize.value == size) {
            return
        }
        ReadSettings.editLocal { set(ReadSettings.textSize, size) }
        webNestView.getSettings().setTextZoom(ReadSettings.textSize.value)
    }

    fun setTransJs(enabled: Boolean) {
        if (!this.transJsPending || TextUtils.isEmpty(this.transJsCache)) {
            return
        }
        val transJsCache = this.transJsCache
        this.transJsPending = false
        this.transJsCache = null
        if (enabled) {
            showCoverProgress()
        }
        evalJsNow(this.page, transJsCache, true)
    }

    private fun setTtsMove(state: Int) {
        val arrayList: ArrayList<Any?>? = if (this.page == null) null else this.ttsParagraphs
        if (this.page == null || arrayList == null) {
            return
        }
        var size = this.ttsMoveIndex
        if (size == -1234) {
            size = this.ttsIndex
            this.ttsMoveIndex = size
        }
        if (state == 1) {
            size--
        } else if (state == 2) {
            size++
        }
        if (size >= arrayList.size) {
            size = this.ttsParagraphs!!.size - 1
        }
        if (size < 0) {
            size = 0
        }
        val ttsParagraphs = this.ttsParagraphs
        val ttsItem =
            if (ttsParagraphs == null || size < 0 || size >= ttsParagraphs.size) null else this.ttsParagraphs!!.get(
                size
            ) as TtsItem?
        val speech = if (ttsItem != null) ttsItem.speech else null
        if (TextUtils.isEmpty(speech)) {
            return
        }
        this.speakMode = true
        val webNestView = this.page
        val runnable = this.ttsMoveRunnable
        webNestView!!.removeCallbacks(runnable)
        if (state != 0) {
            pauseTts(false)
        }
        highlightRange(0, speech!!.length, size, true)
        this.ttsMoveIndex = size
        this.ttsJump = true
        this.page!!.postDelayed(runnable, 200L)
    }

    fun setValAnimHide(hideAnimValue: kotlin.Float) {
        val myWebBody: MyWebBody? = if (!this.alive) null else this.webBody
        if (!this.alive || myWebBody == null) {
            return
        }
        setTranslationY((1.0f - hideAnimValue) * myWebBody.getHeight() * 0.1f)
        setAlpha(hideAnimValue)
    }

    fun setValAnimMini(miniAnimValue: kotlin.Float) {
        if (this.alive) {
            if (this.miniLayoutParams == null) {
                this.miniLayoutParams = getLayoutParams() as RelativeLayout.LayoutParams?
            }
            val miniMode = this.miniMode
            val miniAnimating = this.miniAnimating
            var translating = true
            if (miniMode) {
                if (miniAnimating) {
                    val dialogReadListener = this.readListener
                    if (dialogReadListener != null) {
                        dialogReadListener.applyColors(this.statusColor, this.naviColor, this.windowColor)
                    }
                    this.statusColor = this.savedStatusColor
                    this.naviColor = this.savedNaviColor
                    this.windowColor = this.savedWindowColor
                    this.miniLayoutParams!!.removeRule(2)
                    this.content!!.setVisibility(View.GONE)
                    val myFadeFrame = this.speakBar
                    if (myFadeFrame != null) {
                        myFadeFrame.hide(false)
                    }
                }
                if ((miniAnimValue).compareTo(0.0f) == 0) {
                    this.miniBar!!.setVisibility(View.VISIBLE)
                    updateMiniElevation()
                    updateMiniShape()
                } else {
                    translating = false
                }
            } else {
                if (miniAnimating) {
                    val dialogReadListener = this.readListener
                    if (dialogReadListener != null) {
                        dialogReadListener.onClosed()
                    }
                    this.miniBar!!.setVisibility(View.GONE)
                    updateMiniElevation()
                    updateMiniShape()
                }
                if ((miniAnimValue).compareTo(1.0f) == 0) {
                    val darkUi = AppState.darkUi
                    applyWindow(if (darkUi) -16777216 else -1, darkUi)
                    this.content!!.setVisibility(View.VISIBLE)
                    if (this.speakMode) {
                        setIconCurr(false)
                    }
                } else {
                    translating = false
                }
            }
            this.miniAnimating = false
            if (translating) {
                val miniModeNow = this.miniMode
                val layoutParams = this.miniLayoutParams
                if (miniModeNow) {
                    layoutParams!!.width = this.minX
                    layoutParams.height = this.minY
                    setLayoutParams(layoutParams)
                    val handler = this.uiHandler
                    if (handler == null) {
                        return
                    } else {
                        handler.post(SetPosTask())
                    }
                } else {
                    layoutParams!!.width = -1
                    layoutParams.height = -1
                    layoutParams.addRule(2, AppR.id.web_cast_ctrl)
                    setLayoutParams(this.miniLayoutParams)
                    val handler = this.uiHandler
                    if (handler == null) {
                        return
                    } else {
                        handler.post(MoveToMiniPosTask())
                    }
                }
            } else {
                setAnimPos(miniAnimValue)
            }
            if (translating) {
                this.miniLayoutParams = null
            }
        }
    }

    fun setValAnimShow(progress: kotlin.Float) {
        val myWebBody: MyWebBody? = if (!this.alive) null else this.webBody
        if (!this.alive || myWebBody == null) {
            return
        }
        setTranslationY((1.0f - progress) * myWebBody.getHeight() * 0.1f)
        setAlpha(progress)
        if (getVisibility() != View.VISIBLE) {
            setVisibility(View.VISIBLE)
        }
        val handler: Handler? = if ((progress).compareTo(1.0f) != 0) null else this.uiHandler
        if ((progress).compareTo(1.0f) != 0 || handler == null) {
            return
        }
        handler.post(ApplyWindowTask())
    }

    fun setWebHtml(html: String?) {
        if (this.fadeText != null) {
            if (TextUtils.isEmpty(html)) {
                hideCoverProgress()
                this.fadeText!!.show()
                return
            }
            this.fadeText!!.hide()
            if (this.page != null) {
                val translateUrl = MainUtil.translateUrlFor(this.pageUrl!!)
                this.webHtml = translateUrl
                MainUtil.loadHtml(this.page, translateUrl, html)
                this.page!!.setVisibility(View.VISIBLE)
                runInBackground(SetTransJsTask())
                applyTtsSettings()
                val handler: Handler? = if (!this.htmlLoading) null else this.uiHandler
                if (!this.htmlLoading || handler == null) {
                    return
                }
                handler.post(ShowSpeakBarTask())
            }
        }
    }

    fun setWebSettin2(webView: WebView?) {
        if (webView == null) {
            return
        }
        webView.setWebViewClient(LocalWebViewClient())
        webView.setWebChromeClient(LocalChromeClient())
    }

    fun setWebSettin3(webView: WebView?) {
        if (webView == null) {
            return
        }
        this.loadPending = true
        webView.addJavascriptInterface(WebAppInterface(), "android")
    }

    fun setWebSetting(webView: WebView?) {
        if (webView == null) {
            return
        }
        val settings = webView.getSettings()
        settings.setTextZoom(ReadSettings.textSize.value)
        settings.setSupportZoom(true)
        settings.setBuiltInZoomControls(true)
        settings.setDisplayZoomControls(false)
        settings.setUseWideViewPort(true)
        settings.setLoadWithOverviewMode(true)
        settings.setJavaScriptCanOpenWindowsAutomatically(false)
        settings.setSupportMultipleWindows(false)
        settings.setMediaPlaybackRequiresUserGesture(false)
        settings.setJavaScriptEnabled(true)
        webView.setOverScrollMode(2)
        webView.setVerticalScrollBarEnabled(false)
        webView.setHorizontalScrollBarEnabled(false)
    }

    fun setWindowColor(windowColor: Int) {
        val mainActivity: MainActivity? = if (AppState.darkUi) null else this.activity
        if (AppState.darkUi || mainActivity == null || this.windowColor == windowColor) {
            return
        }
        this.windowColor = windowColor
        mainActivity.getWindow().setBackgroundDrawable(ColorDrawable(windowColor))
    }

    fun finishActionMode() {
        val actionMode = this.actionModeRef
        if (actionMode != null) {
            actionMode.finish()
            this.actionModeRef = null
        }
    }

    fun setTtsState(state: Int, translating: Boolean) {
        if (this.speakBtn == null || !this.alive) {
            return
        }
        this.ttsState = (state).toByte()
        if (state == 1) {
            this.speakMode = true
        } else {
            this.speakMode = false
            clearSelection()
        }
        val handler: Handler? = if ((this.ttsState.toInt() == 1 && translating)) null else this.uiHandler
        if ((this.ttsState.toInt() == 1 && translating) || handler == null) {
            return
        }
        handler.post(HideSpeakBarTask())
    }

    fun dismissDialogs() {
        val dialogPrintPage = this.printDialog
        if (dialogPrintPage != null) {
            dialogPrintPage.dismiss()
            this.printDialog = null
        }
        val dialogSaveSource = this.saveSourceDialog
        if (dialogSaveSource != null) {
            dialogSaveSource.dismiss()
            this.saveSourceDialog = null
        }
        val dialogSeekBright = this.brightDialog
        if (dialogSeekBright != null) {
            dialogSeekBright.dismiss()
            this.brightDialog = null
        }
        val dialogSeekText = this.textDialog
        if (dialogSeekText != null) {
            dialogSeekText.dismiss()
            this.textDialog = null
        }
        val dialogSeekAudio = this.audioDialog
        if (dialogSeekAudio != null) {
            dialogSeekAudio.dismiss()
            this.audioDialog = null
        }
        val dialogSetTts = this.ttsDialog
        if (dialogSetTts != null) {
            dialogSetTts.dismiss()
            this.ttsDialog = null
        }
        val dialogConfirm = this.confirmDialog
        if (dialogConfirm != null) {
            dialogConfirm.dismiss()
            this.confirmDialog = null
        }
        val dialogViewTrans = this.transDialog
        if (dialogViewTrans != null) {
            dialogViewTrans.dismiss()
            this.transDialog = null
        }
        dismissTransSheet()
        val dialogTransLang = this.transLangDialog
        if (dialogTransLang != null) {
            dialogTransLang.dismiss()
            this.transLangDialog = null
        }
    }

    fun startReading(fromStart: Boolean) {
        if (this.page == null) {
            return
        }
        clearSelection()
        this.speakMode = true
        this.ttsStartPos = 0
        this.ttsEndPos = 0
        var list: MutableList<ReadItem?>? = null
        if (fromStart) {
            val partList: MutableList<ReadItem?>? = if (this.partPlay) this.partList else this.paragraphList
            if (partList == null || partList.isEmpty()) {
                this.ttsState = 0.toByte()
                this.speakMode = false
                this.ttsParagraphs = null
                this.ttsIndex = 0
                setTtsState(0, false)
                return
            }
            list = partList
        }
        this.paragraphQueue = list
        runInBackground(InitTtsTask())
    }

    fun hideCoverProgress() {
        val myCoverView = this.loadCover
        if (myCoverView == null) {
            return
        }
        this.coverLoading = false
        myCoverView.hideCover(false)
    }

    fun startAutoTrans() {
        val arrayList: ArrayList<WebReadTask.ReadItem?>? = if (!this.htmlLoading || !this.pageVisible || this.transRunning || !this.alive) null else this.paragraphList
        if (!this.htmlLoading || !this.pageVisible || this.transRunning || !this.alive || arrayList == null || arrayList.isEmpty()) {
            return
        }
        if (!ReadSettings.autoTrans.value || this.autoTrans) {
            this.uiHandler!!.post(StartReadingTask())
        }
    }

    fun dismissMoreMenu() {
        moreMenu = null
        val myPopupMenu = this.morePopup
        if (myPopupMenu != null) {
            val mainActivity = this.activity
            if (mainActivity != null) {
                mainActivity.popupMenu = null
            }
            myPopupMenu.dismissNow()
            this.morePopup = null
        }
        val myFadeFrame = this.speakBar
        if (myFadeFrame != null) {
            myFadeFrame.setAutoHide(true)
        }
    }

    fun stopTts() {
        clearSelection()
        this.ttsState = 0.toByte()
        this.speakMode = false
        this.ttsParagraphs = null
        this.ttsIndex = 0
        this.ttsStartPos = 0
        this.ttsEndPos = 0
        this.partPlay = false
        try {
            val textToSpeech = this.ttsEngine
            if (textToSpeech != null && textToSpeech.isSpeaking()) {
                this.ttsEngine!!.stop()
            }
            val myButtonImage = this.speakFocusBtn
            if (myButtonImage != null) {
                myButtonImage.setLoad(false)
                this.speakFocusBtn = null
            }
            updatePlayIcon()
        } catch (unused: Exception) {
        }
    }

    fun dismissSaveMenu() {
        saveMenu = null
        val myPopupMenu = this.savePopup
        if (myPopupMenu != null) {
            val mainActivity = this.activity
            if (mainActivity != null) {
                mainActivity.popupMenu = null
            }
            myPopupMenu.dismissNow()
            this.savePopup = null
        }
        val myFadeFrame = this.speakBar
        if (myFadeFrame != null) {
            myFadeFrame.setAutoHide(true)
        }
    }

    fun dismissTransSheet() {
        val myDialogBottom = this.bottomSheet
        if (myDialogBottom != null) {
            myDialogBottom.dismiss()
            this.bottomSheet = null
        }
        val webTransControl = this.sheetTransControl
        if (webTransControl != null) {
            webTransControl.release()
            this.sheetTransControl = null
        }
    }

    fun hideSpeakBar() {
        val myFadeFrame = this.speakBar
        if (myFadeFrame == null || !this.speakBarShown) {
            return
        }
        this.speakBarShown = false
        this.speakFocusBtn = null
        myFadeFrame.hide(true)
    }

    fun releaseVideo() {
        val webVideoImage = this.videoImage
        this.videoImage = null
        if (webVideoImage != null) {
            this.videoImageOut = webVideoImage
            this.videoTypeOut = webVideoImage.videoType
            this.videoRestore = true
            webVideoImage.exitCustomView()
            val handler = this.uiHandler
            if (handler == null) {
                return
            }
            handler.post(ReleaseVideoImageTask())
        }
    }

    fun openReader(
        mainActivity: MainActivity?,
        myWebBody: MyWebBody?,
        pageUrl: String?,
        pageTitle: String?,
        quickItem: QuickItem?,
        showFromMini: Boolean,
        statusColor: Int,
        naviColor: Int,
        windowColor: Int,
        quickOpened: Boolean,
        dialogReadListener: DialogReadListener?
    ) {
        this.activity = mainActivity
        this.readListener = dialogReadListener
        this.webBody = myWebBody
        this.pageUrl = pageUrl
        this.pageTitle = pageTitle
        val darkUi = AppState.darkUi
        this.configChanged = darkUi
        this.textSizeState = ReadSettings.textSize.value
        this.fontPathState = ReadSettings.fontPath.value
        this.fontBoldState = ReadSettings.fontBold.value
        this.showFromMini = showFromMini
        this.quickOpened = quickOpened
        this.barsHidden = darkUi
        this.statusColor = statusColor
        this.naviColor = naviColor
        this.windowColor = windowColor
        this.savedStatusColor = statusColor
        this.savedNaviColor = naviColor
        this.savedWindowColor = windowColor
        var htmlLoading = true
        if (quickItem != null) {
            this.readFromHtml = true
            this.quickUrl = quickItem.label
            this.newsIndex = quickItem.newsIndex
            var imageUrl = quickItem.imageUrl
            if (TextUtils.isEmpty(imageUrl) || (imageUrl!!.startsWith("https://encrypted-tbn") && imageUrl.contains(
                    "/faviconV2?"
                ))
            ) {
                imageUrl = null
            }
            this.newsImage = imageUrl
            if (!TextUtils.isEmpty(this.quickUrl)) {
                this.pageTitle = this.quickUrl
            }
        }
        val index = ReadSettings.textSize.value
        if (index < 50) {
            ReadSettings.editLocal { set(ReadSettings.textSize, 50) }
        } else if (index > 500) {
            ReadSettings.editLocal { set(ReadSettings.textSize, 500) }
        }
        this.transRunning = true
        if (!ReadSettings.autoSpeak.value && !this.showFromMini) {
            htmlLoading = false
        }
        this.htmlLoading = htmlLoading
        val handler = this.uiHandler
        if (handler == null) {
            return
        }
        handler.post(BuildReaderTask())
    }

    fun buildTopBar() {
        val myFadeFrame: MyFadeFrame? = if (this.topBar != null) null else this.speakBar
        val mainActivity: MainActivity? = if (this.topBar != null || myFadeFrame == null) null else this.activity
        if (this.topBar != null || myFadeFrame == null || mainActivity == null) {
            return
        }
        var barInset = MainApp.dp56
        var barTop = MainApp.dp4
        if (MainUtil.isFullScreenOff()) {
            val extraTop = dpToPx(mainActivity, 28.0f).toInt()
            barInset += extraTop
            barTop += extraTop
        }
        this.barInset = barInset
        val host: View = this
        val top = ReaderBar(mainActivity)
        myFadeFrame.addView(top, -1, this.barInset)
        val btnBack = ReadBtn(host)
        val btnMini = ReadBtn(host)
        val btnOpen = ReadBtn(host)
        if (!this.readFromHtml) {
            btnOpen.setVisibility(8)
        }
        val btnSave = ReadBtn(host)
        val btnReload = ReadBtn(host)
        val btnMore = ReadBtn(host)
        val rowHeight = dpToPx(mainActivity, 80.0f).toInt()
        val rowTools = ReaderBar(mainActivity)
        rowTools.setVisibility(View.GONE)
        val bottomRowParams = LayoutParams(-1, rowHeight)
        bottomRowParams.gravity = 80
        myFadeFrame.addView(rowTools, bottomRowParams)
        val rowSpeak = ReaderBar(mainActivity)
        rowSpeak.setVisibility(View.GONE)
        val layoutParams = LayoutParams(-1, rowHeight)
        layoutParams.gravity = 80
        myFadeFrame.addView(rowSpeak, layoutParams)
        this.backBtn = btnBack
        this.miniBtn = btnMini
        this.openBtn = btnOpen
        this.saveBtn = btnSave
        this.reloadBtn = btnReload
        this.moreBtn = btnMore
        this.topBar = top
        this.toolsRow = rowTools
        this.speakRow = rowSpeak
        this.brightBtn = ReadBtn(host)
        this.textSizeBtn = ReadBtn(host)
        this.audioBtn = ReadBtn(host)
        this.speakBtn = ReadBtn(host)
        this.translateBtn = ReadBtn(host)
        this.prevBtn = ReadBtn(host)
        this.pauseBtn = ReadBtn(host)
        this.stopBtn = ReadBtn(host)
        this.currentBtn = ReadBtn(host)
        this.nextBtn = ReadBtn(host)
        val topPx = barTop
        top.content({ readerDark }) {
            ReaderTopBar(
                top, topPx, MainApp.dp4, btnBack, btnMini, btnOpen, btnSave, btnReload, btnMore,
                saveMenu, moreMenu,
                onMenuDismiss = { more -> if (more) dismissMoreMenu() else dismissSaveMenu() },
                onMenuPick = { more, id -> pickMenu(more, id) },
            )
        }
        val tools = listOf(this.brightBtn!!, this.textSizeBtn!!, this.audioBtn!!, this.speakBtn!!, this.translateBtn!!)
        rowTools.content({ readerDark }) { ReaderBottomRow(rowTools, tools) }
        val speak = listOf(this.prevBtn!!, this.pauseBtn!!, this.stopBtn!!, this.currentBtn!!, this.nextBtn!!)
        rowSpeak.content({ readerDark }) { ReaderBottomRow(rowSpeak, speak) }
        applyTopBarColors()
        this.backBtn!!.setOnClickListener(HandleBackClick())
        this.miniBtn!!.setOnClickListener(DismissDialogsClick())
        if (this.readFromHtml) {
            this.openBtn!!.setOnClickListener(OpenUrlClick())
        }
        this.saveBtn!!.setOnClickListener(DismissSaveMenuClick())
        this.reloadBtn!!.setOnClickListener(StopTtsClick())
        if (ReadSettings.notiRead.value) {
            this.moreBtn!!.setNoti(true)
        }
        this.moreBtn!!.setOnClickListener(DismissMoreMenuClick())
        this.brightBtn!!.setOnClickListener(HasDialogClick())
        this.textSizeBtn!!.setOnClickListener(TextSizeClick())
        this.audioBtn!!.setOnClickListener(AudioClick())
        this.speakBtn!!.setOnClickListener(TtsStartClick())
        this.translateBtn!!.setOnClickListener(TranslateBtnClick())
        this.prevBtn!!.setOnClickListener(BarTtsPrevClick())
        this.currentBtn!!.setOnClickListener(CurrentBtnClick())
        this.nextBtn!!.setOnClickListener(BarTtsNextClick())
        this.pauseBtn!!.setOnClickListener(BarTtsPauseClick())
        this.stopBtn!!.setOnClickListener(LockClick())
    }

    /** Compose chrome state: theme, and the save (PDF / TXT) and more menus of the top bar (null = closed). */
    private var readerDark by androidx.compose.runtime.mutableStateOf(AppState.darkUi)
    private var saveMenu by androidx.compose.runtime.mutableStateOf<List<ReaderMenuItem>?>(null)
    private var moreMenu by androidx.compose.runtime.mutableStateOf<List<ReaderMenuItem>?>(null)

    private fun pickMenu(more: Boolean, id: Int) {
        if (more) {
            dismissMoreMenu()
            DismissMoreMenuStep().onItem(null, id)
        } else {
            dismissSaveMenu()
            DismissSaveMenuStep().onItem(null, id)
        }
    }

    fun buildMiniBar() {
        val mainActivity: MainActivity? = if (this.miniBar != null) null else this.activity
        if (this.miniBar != null || mainActivity == null) {
            return
        }
        val frameLayout = ReaderBar(mainActivity)
        frameLayout.setVisibility(View.GONE)
        addView(frameLayout, dpToPx(mainActivity, 160.0f).toInt(), dpToPx(mainActivity, 96.0f).toInt())
        val host: View = this
        val full = ReadBtn(host)
        val close = ReadBtn(host)
        val rew = ReadBtn(host)
        val play = ReadBtn(host)
        val fwd = ReadBtn(host)
        frameLayout.content({ readerDark }) { ReaderMiniControls(frameLayout, full, close, rew, play, fwd) }
        this.miniBar = frameLayout
        this.fullBtn = full
        this.closeBtn = close
        this.rewindBtn = rew
        this.playBtn = play
        this.forwardBtn = fwd
        this.minX = Math.round(dpToPx(this.ctx, 160.0f))
        this.minY = Math.round(dpToPx(this.ctx, 96.0f))
        applyMiniBarColors()
        updateMiniShape()
        this.fullBtn!!.setOnClickListener(SetMiniModeClick())
        this.closeBtn!!.setOnClickListener(CloseReaderClick())
        this.rewindBtn!!.setOnClickListener(TtsPrevClick())
        this.playBtn!!.setOnClickListener(TtsPauseClick())
        this.forwardBtn!!.setOnClickListener(TtsNextClick())
    }

    fun initTts(): Boolean {
        if (this.ctx == null || this.ttsEngine != null) {
            return false
        }
        this.ttsLang = TtsSettings.ttsLang.value
        try {
            val textToSpeech = TextToSpeech(this.ctx, ResetTtsStep())
            this.ttsEngine = textToSpeech
            textToSpeech.setOnUtteranceProgressListener(StopTtsStep())
            return true
        } catch (unused: Exception) {
            return false
        }
    }

    fun createPage() {
        if (this.page != null || this.content == null) {
            return
        }
        val webNestView = WebNestView(this.activity)
        this.page = webNestView
        webNestView.setNoAutofill(false)
        val darkUi = AppState.darkUi
        val page = this.page
        if (darkUi) {
            page!!.setBackgroundColor(-14606047)
        } else {
            page!!.setBackgroundColor(-1)
        }
        this.page!!.setFocusableInTouchMode(false)
        this.page!!.setVisibility(View.GONE)
        this.page!!.setOnLongClickListener(PageLongClick())
        this.page!!.setListener(PageListener())
        this.content!!.addView(this.page, 0, ViewGroup.LayoutParams(-1, -2))
        val handler = this.uiHandler
        if (handler != null) {
            handler.post(ShowCoverProgressTask())
        }
    }

    /**
     * The reader's content frame (was `view/MyRoundItem`): all four corners rounded by `MainApp.dp16`; its light / dark
     * background is set in [applyColors].
     */
    private fun readerFrame(context: Context): RelativeLayout {
        val radius = MainApp.dp16.toFloat()
        val frame = RelativeLayout(context)
        frame.outlineProvider = object : android.view.ViewOutlineProvider() {
            override fun getOutline(view: View, outline: android.graphics.Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, radius)
            }
        }
        frame.clipToOutline = true
        return frame
    }

    fun isSamePage(url: String?, fromHtml: Boolean, checkTrans: Boolean): Boolean {
        var dialogReadListener: DialogReadListener? = null
        if (this.readFromHtml != fromHtml || !MainUtil.sameObject(this.pageUrl, url)) {
            return false
        }
        if (!checkTrans) {
            val handler = this.uiHandler
            if (handler != null && !this.opening) {
                this.opening = true
                handler.post(SetMiniModeTask())
                return true
            }
        } else if (!this.speakMode && (this.readListener.also { dialogReadListener = it }) != null) {
            if (dialogReadListener!!.isSpeaking()) {
                post(TtsStartTask())
                return true
            }
            ttsStart()
            return true
        }
        return true
    }

    fun hasDialog(): Boolean {
        return if (this.printDialog == null && this.saveSourceDialog == null && this.brightDialog == null && this.textDialog == null && this.audioDialog == null && this.ttsDialog == null && this.confirmDialog == null && this.transDialog == null && this.bottomSheet == null && this.transLangDialog == null) false else true
    }

    fun isBusy(checkTrans: Boolean): Boolean {
        val myCoverView: com.mycompany.app.ui.components.LoadCover?
        return this.transRunning || this.htmlLoading || (checkTrans && this.autoTrans && !this.transDone) || this.transState.toInt() == 1 || ((this.loadCover.also {
            myCoverView = it
        }) != null && myCoverView!!.isCoverShown())
    }

    fun canSelect(): Boolean {
        return this.miniMode && this.miniAnim == null && this.selRect != null && this.selRects != null
    }

    fun pauseTts(pause: Boolean) {
        if (pause) {
            clearSelection()
        }
        this.ttsState = 2.toByte()
        try {
            val textToSpeech = this.ttsEngine
            if (textToSpeech != null && textToSpeech.isSpeaking()) {
                this.ttsEngine!!.stop()
            }
            updatePlayIcon()
        } catch (unused: Exception) {
        }
    }

    fun closeReader(dialogReadListener: DialogReadListener?) {
        if (this.alive) {
            this.alive = false
            val textSizeState = this.textSizeState
            val textSize = ReadSettings.textSize.value
            if (textSizeState != textSize) {
                this.textSizeState = textSize
                putInt(this.ctx, 8, textSize, "mTextSize")
            }
            val webReadTask = this.readTask
            if (webReadTask != null) {
                webReadTask.release()
                this.readTask = null
            }
            val mainTransText = this.transText
            if (mainTransText != null) {
                mainTransText.release()
                this.transText = null
            }
            resetTts()
            val myWebBody = this.webBody
            if (myWebBody != null) {
                myWebBody.removeView(this)
                this.webBody = null
            }
            this.readListener = null
            if (dialogReadListener != null) {
                dialogReadListener.onSpeak()
            }
            val myButtonImage = this.backBtn
            if (myButtonImage != null) {
                myButtonImage.clearClick()
                this.backBtn = null
            }
            val miniBtn = this.miniBtn
            if (miniBtn != null) {
                miniBtn.clearClick()
                this.miniBtn = null
            }
            val openBtn = this.openBtn
            if (openBtn != null) {
                openBtn.clearClick()
                this.openBtn = null
            }
            val saveBtn = this.saveBtn
            if (saveBtn != null) {
                saveBtn.clearClick()
                this.saveBtn = null
            }
            val reloadBtn = this.reloadBtn
            if (reloadBtn != null) {
                reloadBtn.clearClick()
                this.reloadBtn = null
            }
            val moreBtn = this.moreBtn
            if (moreBtn != null) {
                moreBtn.clearClick()
                this.moreBtn = null
            }
            this.content = null
            val webNestView = this.page
            if (webNestView != null) {
                destroyWebView(webNestView, true)
                this.page = null
            }
            val myProgressBar = this.progressBar
            if (myProgressBar != null) {
                myProgressBar.release()
                this.progressBar = null
            }
            val myScrollBar = this.scrollBar
            if (myScrollBar != null) {
                myScrollBar.releaseAll()
                this.scrollBar = null
            }
            val myFadeFrame = this.speakBar
            if (myFadeFrame != null) {
                myFadeFrame.release()
                this.speakBar = null
            }
            val brightBtn = this.brightBtn
            if (brightBtn != null) {
                brightBtn.clearClick()
                this.brightBtn = null
            }
            val textSizeBtn = this.textSizeBtn
            if (textSizeBtn != null) {
                textSizeBtn.clearClick()
                this.textSizeBtn = null
            }
            val audioBtn = this.audioBtn
            if (audioBtn != null) {
                audioBtn.clearClick()
                this.audioBtn = null
            }
            val speakBtn = this.speakBtn
            if (speakBtn != null) {
                speakBtn.clearClick()
                this.speakBtn = null
            }
            val translateBtn = this.translateBtn
            if (translateBtn != null) {
                translateBtn.clearClick()
                this.translateBtn = null
            }
            val prevBtn = this.prevBtn
            if (prevBtn != null) {
                prevBtn.clearClick()
                this.prevBtn = null
            }
            val pauseBtn = this.pauseBtn
            if (pauseBtn != null) {
                pauseBtn.clearClick()
                this.pauseBtn = null
            }
            val stopBtn = this.stopBtn
            if (stopBtn != null) {
                stopBtn.clearClick()
                this.stopBtn = null
            }
            val currentBtn = this.currentBtn
            if (currentBtn != null) {
                currentBtn.clearClick()
                this.currentBtn = null
            }
            val nextBtn = this.nextBtn
            if (nextBtn != null) {
                nextBtn.clearClick()
                this.nextBtn = null
            }
            this.fadeText = null
            this.loadCover = null
            val webVideoImage = this.videoImage
            if (webVideoImage != null) {
                webVideoImage.exitCustomView()
                this.videoImage = null
            }
            this.pinchGuide = null
            val fullBtn = this.fullBtn
            if (fullBtn != null) {
                fullBtn.clearClick()
                this.fullBtn = null
            }
            val closeBtn = this.closeBtn
            if (closeBtn != null) {
                closeBtn.clearClick()
                this.closeBtn = null
            }
            val rewindBtn = this.rewindBtn
            if (rewindBtn != null) {
                rewindBtn.clearClick()
                this.rewindBtn = null
            }
            val playBtn = this.playBtn
            if (playBtn != null) {
                playBtn.clearClick()
                this.playBtn = null
            }
            val forwardBtn = this.forwardBtn
            if (forwardBtn != null) {
                forwardBtn.clearClick()
                this.forwardBtn = null
            }
            val webTransControl = this.sheetTransControl
            if (webTransControl != null) {
                webTransControl.release()
                this.sheetTransControl = null
            }
            this.activity = null
            this.ctx = null
            clearHandler(this.uiHandler)
            this.uiHandler = null
            this.customView = null
            this.topBar = null
            this.toolsRow = null
            this.speakRow = null
            this.gestureDetect = null
            this.scaleDetector = null
            this.pageUrl = null
            this.pageTitle = null
            this.pageLang = null
            this.pageJs = null
            this.nativeScript5 = null
            this.nativeScript13 = null
            this.fontPathState = null
            this.quickUrl = null
            this.newsImage = null
            this.paragraphList = null
            this.executorCache = null
            this.miniBar = null
            this.miniLayoutParams = null
            this.miniRect = null
            this.miniPaint = null
            this.fullView = null
            this.transJsCache = null
            this.transLang = null
            this.transPickLast = null
            this.detectedLang = null
            this.webHtml = null
            this.selRect = null
            this.selRects = null
        }
    }

    fun resetTts() {
        clearSelection()
        this.ttsState = 0.toByte()
        this.speakMode = false
        this.ttsLang = null
        this.ttsLangSet = null
        this.ttsParagraphs = null
        this.ttsIndex = 0
        this.ttsStartPos = 0
        this.ttsEndPos = 0
        val textToSpeech = this.ttsEngine
        if (textToSpeech != null) {
            try {
                if (textToSpeech.isSpeaking()) {
                    this.ttsEngine!!.stop()
                }
                this.ttsEngine!!.shutdown()
            } catch (unused: Exception) {
            }
            this.ttsEngine = null
        }
    }

    fun clearSelection() {
        val webNestView: WebNestView? = if (!ZtriSettings.readAccent.value) null else this.page
        if (!ZtriSettings.readAccent.value || webNestView == null) {
            return
        }
        evalJsNow(webNestView, "window.getSelection().removeAllRanges();", true)
    }

    fun applyTextStyle() {
        val webNestView = this.page
        if (webNestView != null) {
            if (this.textSizeState != ReadSettings.textSize.value) {
                webNestView.getSettings().setTextZoom(ReadSettings.textSize.value)
            }
            if (this.fontBoldState == ReadSettings.fontBold.value && MainUtil.sameObject(this.fontPathState, ReadSettings.fontPath.value)) {
                return
            }
            this.fontPathState = ReadSettings.fontPath.value
            this.fontBoldState = ReadSettings.fontBold.value
            this.page!!.post(ReloadTask())
        }
    }

    fun speakNow() {
        initTts()
        if (this.ttsEngine == null) {
            return
        }
        this.ttsState = 1.toByte()
        this.speakMode = true
        try {
            val ttsIndex = this.ttsIndex
            val arrayList = this.ttsParagraphs
            val ttsItem =
                if (arrayList == null || ttsIndex < 0 || ttsIndex >= arrayList.size) null else this.ttsParagraphs!!.get(ttsIndex) as TtsItem?
            val speech = if (ttsItem == null) null else ttsItem.speech
            if (TextUtils.isEmpty(speech)) {
                this.ttsState = 0.toByte()
                this.speakMode = false
                this.ttsParagraphs = null
                this.ttsIndex = 0
                setTtsState(0, false)
                return
            }
            val ttsStartPos = this.ttsStartPos + this.ttsEndPos
            this.ttsStartPos = ttsStartPos
            val remaining = speech!!.substring(ttsStartPos)
            if (TextUtils.isEmpty(remaining)) {
                this.ttsState = 0.toByte()
                this.speakMode = false
                this.ttsParagraphs = null
                this.ttsIndex = 0
                setTtsState(0, false)
                return
            }
            if (this.ttsEngine!!.isSpeaking()) {
                this.ttsEngine!!.stop()
            }
            applyTtsLang()
            if (this.ttsEngine!!.speak(remaining, 0, null, "0") == 0) {
                setTtsState(1, true)
            } else {
                this.ttsState = 0.toByte()
                this.speakMode = false
            }
            if (this.ttsState.toInt() == 0) {
                this.ttsParagraphs = null
                this.ttsIndex = 0
                setTtsState(0, false)
            }
        } catch (unused: Exception) {
            this.ttsState = 0.toByte()
            this.speakMode = false
        }
    }

    fun runInBackground(runnable: Runnable?) {
        var executorServiceH = this.executorCache
        if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
            executorServiceH = MainApp.executor(this.ctx)
            if (executorServiceH == null) {
                return
            } else {
                this.executorCache = executorServiceH
            }
        }
        try {
            executorServiceH.execute(runnable)
        } catch (unused: Exception) {
        }
    }

    fun startTrans() {
        this.transApplied = false
        this.transPickLast = AlbumSettings.transPick.value
        this.transJsPending = true
        if (this.autoTrans) {
            this.autoTrans = false
            runInBackground(CacheTransJsTask())
        } else if (ReadSettings.autoTrans.value) {
            setTransJs(false)
        }
    }

    fun applyColors() {
        if (!this.alive || this.content == null) {
            return
        }
        if (AppState.darkUi) {
            setBackgroundColor(-16777216)
            this.content!!.setBackgroundColor(-14606047)
            this.progressBar!!.applyColors(-922746881, -16777216)
            this.scrollBar!!.setPreColor(-12632257)
            this.loadCover!!.setBackColor(-1593835520)
            val webNestView = this.page
            if (webNestView != null) {
                webNestView.setBackgroundColor(-14606047)
            }
        } else {
            setBackgroundColor(-1)
            this.content!!.setBackgroundColor(-1)
            this.progressBar!!.applyColors(-13022805, -460552)
            this.scrollBar!!.setPreColor(-2434342)
            this.loadCover!!.setBackColor(-1577058305)
            val webNestView = this.page
            if (webNestView != null) {
                webNestView.setBackgroundColor(-1)
            }
        }
        val view = this.fullView
        if (view != null) {
            if (AppState.darkUi) {
                view.setBackgroundResource(AppR.drawable.trans_logo_short_back_dark)
            } else {
                view.setBackgroundResource(AppR.drawable.trans_logo_short_back_color)
            }
        }
        applyMiniBarColors()
        updateMiniShape()
        applyTopBarColors()
    }

    fun applyTopBarColors() {
        readerDark = AppState.darkUi
        val relativeLayout: ReaderBar? = if (!this.alive) null else this.topBar
        if (!this.alive || relativeLayout == null) {
            return
        }
        if (AppState.darkUi) {
            relativeLayout.setBackgroundColor(-14606047)
            this.toolsRow!!.setBackgroundColor(-14606047)
            this.speakRow!!.setBackgroundColor(-14606047)
            this.backBtn!!.setImageResource(AppR.drawable.outline_chevron_left_dark_24)
            this.miniBtn!!.setImageResource(AppR.drawable.outline_picture_in_picture_alt_dark_24)
            this.saveBtn!!.setImageResource(AppR.drawable.outline_download_dark_20)
            this.reloadBtn!!.setImageResource(AppR.drawable.outline_refresh_dark_20)
            this.moreBtn!!.setImageResource(AppR.drawable.outline_more_vert_dark_20)
            this.brightBtn!!.setImageResource(AppR.drawable.outline_brightness_6_dark_24)
            this.textSizeBtn!!.setImageResource(AppR.drawable.outline_text_fields_dark_24)
            this.audioBtn!!.setImageResource(AppR.drawable.outline_volume_up_dark_24)
            this.speakBtn!!.setImageResource(AppR.drawable.baseline_play_arrow_dark_24)
            this.translateBtn!!.setImageResource(AppR.drawable.outline_g_translate_dark_24)
            this.pauseBtn!!.setImageResource(AppR.drawable.baseline_pause_dark_24)
            this.stopBtn!!.setImageResource(AppR.drawable.baseline_stop_dark_24)
            this.currentBtn!!.setImageResource(AppR.drawable.outline_search_dark_24)
            this.backBtn!!.setBgPreColor(-12632257)
            this.miniBtn!!.setBgPreColor(-12632257)
            this.saveBtn!!.setBgPreColor(-12632257)
            this.reloadBtn!!.setBgPreColor(-12632257)
            this.moreBtn!!.setBgPreColor(-12632257)
            this.brightBtn!!.setBgPreColor(-12632257)
            this.textSizeBtn!!.setBgPreColor(-12632257)
            this.audioBtn!!.setBgPreColor(-12632257)
            this.speakBtn!!.setBgPreColor(-12632257)
            this.translateBtn!!.setBgPreColor(-12632257)
            this.prevBtn!!.setBgPreColor(-12632257)
            this.pauseBtn!!.setBgPreColor(-12632257)
            this.stopBtn!!.setBgPreColor(-12632257)
            this.currentBtn!!.setBgPreColor(-12632257)
            this.nextBtn!!.setBgPreColor(-12632257)
            this.brightBtn!!.setBgNorColor(-11513776)
            this.textSizeBtn!!.setBgNorColor(-11513776)
            this.audioBtn!!.setBgNorColor(-11513776)
            this.speakBtn!!.setBgNorColor(-11513776)
            this.translateBtn!!.setBgNorColor(-11513776)
            this.prevBtn!!.setBgNorColor(-11513776)
            this.pauseBtn!!.setBgNorColor(-11513776)
            this.stopBtn!!.setBgNorColor(-11513776)
            this.currentBtn!!.setBgNorColor(-11513776)
            this.nextBtn!!.setBgNorColor(-11513776)
            if (this.readFromHtml) {
                this.openBtn!!.setImageResource(AppR.drawable.outline_open_in_new_dark_20)
                this.openBtn!!.setBgPreColor(-12632257)
            }
        } else {
            relativeLayout.setBackgroundColor(-1)
            this.toolsRow!!.setBackgroundColor(-1)
            this.speakRow!!.setBackgroundColor(-1)
            this.backBtn!!.setImageResource(AppR.drawable.outline_chevron_left_black_24)
            this.miniBtn!!.setImageResource(AppR.drawable.outline_picture_in_picture_alt_black_24)
            this.saveBtn!!.setImageResource(AppR.drawable.outline_download_black_20)
            this.reloadBtn!!.setImageResource(AppR.drawable.outline_refresh_black_20)
            this.moreBtn!!.setImageResource(AppR.drawable.outline_more_vert_black_20)
            this.brightBtn!!.setImageResource(AppR.drawable.outline_brightness_6_black_24)
            this.textSizeBtn!!.setImageResource(AppR.drawable.outline_text_fields_black_24)
            this.audioBtn!!.setImageResource(AppR.drawable.outline_volume_up_black_24)
            this.speakBtn!!.setImageResource(AppR.drawable.baseline_play_arrow_black_24)
            this.translateBtn!!.setImageResource(AppR.drawable.outline_g_translate_black_24)
            this.pauseBtn!!.setImageResource(AppR.drawable.baseline_pause_black_24)
            this.stopBtn!!.setImageResource(AppR.drawable.baseline_stop_black_24)
            this.currentBtn!!.setImageResource(AppR.drawable.outline_search_black_24)
            this.backBtn!!.setBgPreColor(-2039584)
            this.miniBtn!!.setBgPreColor(-2039584)
            this.saveBtn!!.setBgPreColor(-2039584)
            this.reloadBtn!!.setBgPreColor(-2039584)
            this.moreBtn!!.setBgPreColor(-2039584)
            this.brightBtn!!.setBgPreColor(-2039584)
            this.textSizeBtn!!.setBgPreColor(-2039584)
            this.audioBtn!!.setBgPreColor(-2039584)
            this.speakBtn!!.setBgPreColor(-2039584)
            this.translateBtn!!.setBgPreColor(-2039584)
            this.prevBtn!!.setBgPreColor(-2039584)
            this.pauseBtn!!.setBgPreColor(-2039584)
            this.stopBtn!!.setBgPreColor(-2039584)
            this.currentBtn!!.setBgPreColor(-2039584)
            this.nextBtn!!.setBgPreColor(-2039584)
            this.brightBtn!!.setBgNorColor(-460552)
            this.textSizeBtn!!.setBgNorColor(-460552)
            this.audioBtn!!.setBgNorColor(-460552)
            this.speakBtn!!.setBgNorColor(-460552)
            this.translateBtn!!.setBgNorColor(-460552)
            this.prevBtn!!.setBgNorColor(-460552)
            this.pauseBtn!!.setBgNorColor(-460552)
            this.stopBtn!!.setBgNorColor(-460552)
            this.currentBtn!!.setBgNorColor(-460552)
            this.nextBtn!!.setBgNorColor(-460552)
            if (this.readFromHtml) {
                this.openBtn!!.setImageResource(AppR.drawable.outline_open_in_new_black_20)
                this.openBtn!!.setBgPreColor(-2039584)
            }
        }
        applyTtsIcons()
    }

    public override fun dispatchDraw(canvas: Canvas) {
        if (this.alive) {
            super.dispatchDraw(canvas)
            val paint = this.miniPaint
            val rectF: RectF? = if (paint == null) null else this.miniRect
            if (paint == null || rectF == null) {
                return
            }
            val radius = MainApp.dp16.toFloat()
            canvas.drawRoundRect(rectF, radius, radius, paint)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val myFadeFrame: MyFadeFrame?
        val myScrollBar: MyScrollBar?
        if (!this.alive) {
            return super.dispatchTouchEvent(event)
        }
        if (this.showAnim != null || this.hideAnim != null || this.miniAnim != null) {
            return true
        }
        val actionMasked = event!!.getActionMasked()
        if (actionMasked == 0) {
            this.hintTouch = true
            this.miniBarShown = false
            if (this.miniMode) {
                val rawX = event.getRawX()
                val rawY = event.getRawY()
                this.touchX = Math.round(rawX)
                this.touchY = Math.round(rawY)
                val rangeX = (this.maxX - this.minX).toFloat()
                val leftX = ZtriSettings.readLtX.value
                val anchorX = Math.round((rangeX / (leftX + ZtriSettings.readRtX.value)) * leftX)
                val rangeY = (this.maxY - this.minY).toFloat()
                val upY = ZtriSettings.readUpY.value
                val anchorY = Math.round((rangeY / (upY + ZtriSettings.readDnY.value)) * upY)
                this.dragOffsetX = anchorX - this.touchX
                this.dragOffsetY = anchorY - this.touchY
            }
        } else if (actionMasked == 1) {
            if (this.miniMode) {
                myFadeFrame = this.speakBar
                if (myFadeFrame != null) {
                    myFadeFrame.restartTimer()
                }
                myScrollBar = this.scrollBar
                if (myScrollBar != null) {
                    myScrollBar.restartTimer()
                }
            } else if (this.miniBarShown) {
                saveMiniPos()
                if (canSelect()) {
                    invalidate()
                }
            }
            this.hintTouch = false
            this.miniBarShown = false
        } else if (actionMasked != 2) {
            if (actionMasked == 3) {
                if (this.miniMode) {
                    myFadeFrame = this.speakBar
                    if (myFadeFrame != null) {
                        myFadeFrame.restartTimer()
                    }
                    myScrollBar = this.scrollBar
                    if (myScrollBar != null) {
                        myScrollBar.restartTimer()
                    }
                } else if (this.miniBarShown) {
                    saveMiniPos()
                    if (canSelect()) {
                        invalidate()
                    }
                }
                this.hintTouch = false
                this.miniBarShown = false
            } else if (actionMasked == 5 && !this.miniMode && event.getPointerCount() > 1 && this.scaleDetector == null && this.ctx != null) {
                this.scaleDetector = ScaleGestureDetector(this.ctx!!, SetScaleGestureStep())
            }
        } else if (this.miniMode) {
            val rawX = event.getRawX()
            val rawY = event.getRawY()
            val distance = pointDistance(this.touchX.toFloat(), rawX, this.touchY.toFloat(), rawY)
            if (this.miniBarShown) {
                if (distance >= MainApp.dp4 / 2.0f) {
                    this.touchX = Math.round(rawX)
                    val newY = Math.round(rawY)
                    this.touchY = newY
                    moveMini(this.touchX + this.dragOffsetX, newY + this.dragOffsetY)
                }
            } else if (distance >= MainApp.dp4) {
                this.miniBarShown = true
                post(AutoHideMiniBarTask())
                this.touchX = Math.round(rawX)
                val newY = Math.round(rawY)
                this.touchY = newY
                moveMini(this.touchX + this.dragOffsetX, newY + this.dragOffsetY)
            }
        }
        if (!this.miniMode) {
            val gestureDetector = this.gestureDetect
            if (gestureDetector != null) {
                gestureDetector.onTouchEvent(event)
            }
            val scaleGestureDetector = this.scaleDetector
            if (scaleGestureDetector != null) {
                scaleGestureDetector.onTouchEvent(event)
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun updateBottomRows() {
        val linearLayout = this.toolsRow
        if (linearLayout == null) {
            return
        }
        if (this.speakMode) {
            linearLayout.setVisibility(View.GONE)
            this.speakRow!!.setVisibility(View.VISIBLE)
        } else {
            linearLayout.setVisibility(View.VISIBLE)
            this.speakRow!!.setVisibility(View.GONE)
        }
    }

    fun applyBold() {
        if (this.page == null) {
            return
        }
        if (this.fontBoldState) {
            val sb = StringBuilder()
            sb.append("if(document.head&&!document.getElementById('sb_bold_style')){var ele=document.createElement('style');ele.id='sb_bold_style';ele.innerText='body,body *:not([class*=\"icon\"]):not([class^=\"fa\"]):not(ion-icon){font-weight:bold !important;}';document.head.appendChild(ele);}")
            sb.insert(0, "(function(){")
            sb.append("})();")
            evalJs(this.page, sb.toString(), true)
            return
        }
        val sb = StringBuilder()
        sb.append("var ele=document.getElementById('sb_bold_style');if(ele){document.head.removeChild(ele);}")
        sb.insert(0, "(function(){")
        sb.append("})();")
        evalJs(this.page, sb.toString(), true)
    }

    fun selectArea(left: Int, top: Int, right: Int, bottom: Int) {
        var menuLeft: Int = 0
        var left = left
        var right = right
        val index: Int
        if (this.selRect == null || this.selRects == null) {
            return
        }
        val viewX = getX().toInt()
        val dp48Px = MainApp.dp48
        if (viewX < dp48Px) {
            index = dp48Px - viewX
            if (index <= left) {
                return
            } else {
                menuLeft = index - dp48Px
            }
        } else {
            val myWebBody = this.webBody
            if (myWebBody == null) {
                return
            }
            val rightEdge = viewX + right
            val width = myWebBody.getWidth()
            val handleSize = MainApp.dp48
            val maxLeft = width - handleSize
            val menuLeft = if (rightEdge <= maxLeft) 0 else (right - (rightEdge - maxLeft))
            if (rightEdge <= maxLeft || menuLeft >= right) {
                return
            } else {
                index = menuLeft + handleSize
            }
        }
        if (menuLeft >= left) {
            left = menuLeft
        }
        if (index <= right) {
            right = index
        }
        try {
            this.selRect!!.set(left, top, right, bottom)
            setSystemGestureExclusionRects(this.selRects!!)
        } catch (unused: Exception) {
        }
    }

    fun ttsNext() {
        if (lockClick()) {
            return
        }
        if (isBusy(true)) {
            showToast(this.ctx, AppR.string.wait_retry)
        } else {
            setTtsMove(2)
        }
    }

    fun ttsPause() {
        if (lockClick()) {
            return
        }
        if (isBusy(true)) {
            showToast(this.ctx, AppR.string.wait_retry)
            return
        }
        this.speakMode = false
        this.ttsMoveIndex = -1234
        this.ttsJump = false
        if (this.ttsState.toInt() == 0) {
            stopTts()
        } else {
            pauseTts(true)
        }
        hideSpeakBar()
    }

    override fun invalidate() {
        if (this.alive) {
            super.invalidate()
        }
    }

    fun ttsStart() {
        if (this.speakMode) {
            return
        }
        if (isBusy(false)) {
            showToast(this.ctx, AppR.string.wait_retry)
            return
        }
        val ttsState = this.ttsState
        if (ttsState.toInt() == 2) {
            speakNow()
        } else if (ttsState.toInt() == 0) {
            this.partPlay = false
            if (!this.autoTrans || this.transDone) {
                startReading(true)
            } else {
                translatePage(false)
            }
        }
        this.ttsRestart = true
        showSpeakBar(false)
    }

    fun ttsPrev() {
        if (lockClick()) {
            return
        }
        if (isBusy(true)) {
            showToast(this.ctx, AppR.string.wait_retry)
        } else {
            setTtsMove(1)
        }
    }

    fun applyMiniBarColors() {
        readerDark = AppState.darkUi
        val frameLayout: ReaderBar? = if (!this.miniMode) null else this.miniBar
        if (!this.miniMode || frameLayout == null) {
            return
        }
        if (AppState.darkUi) {
            frameLayout.setBackgroundColor(-14606047)
            this.fullBtn!!.setImageResource(AppR.drawable.outline_fullscreen_dark_24)
            this.closeBtn!!.setImageResource(AppR.drawable.outline_close_dark_24)
            this.fullBtn!!.setBgPreColor(-12632257)
            this.closeBtn!!.setBgPreColor(-12632257)
            this.rewindBtn!!.setBgPreColor(-12632257)
            this.playBtn!!.setBgPreColor(-12632257)
            this.forwardBtn!!.setBgPreColor(-12632257)
        } else {
            frameLayout.setBackgroundColor(-1)
            this.fullBtn!!.setImageResource(AppR.drawable.outline_fullscreen_black_24)
            this.closeBtn!!.setImageResource(AppR.drawable.outline_close_black_24)
            this.fullBtn!!.setBgPreColor(-2039584)
            this.closeBtn!!.setBgPreColor(-2039584)
            this.rewindBtn!!.setBgPreColor(-2039584)
            this.playBtn!!.setBgPreColor(-2039584)
            this.forwardBtn!!.setBgPreColor(-2039584)
        }
        applyTtsIcons()
        updatePlayIcon()
    }

    fun updatePlayIcon() {
        val myButtonImage: ReadBtn? = if (!this.miniMode) null else this.playBtn
        if (!this.miniMode || myButtonImage == null) {
            return
        }
        val darkUi = AppState.darkUi
        val speakMode = this.speakMode
        if (darkUi) {
            if (speakMode) {
                myButtonImage.setImageResource(AppR.drawable.baseline_pause_dark_24)
            } else {
                myButtonImage.setImageResource(AppR.drawable.baseline_play_arrow_dark_24)
            }
        } else if (speakMode) {
            myButtonImage.setImageResource(AppR.drawable.baseline_pause_black_24)
        } else {
            myButtonImage.setImageResource(AppR.drawable.baseline_play_arrow_black_24)
        }
        val busy = isBusy(false)
        val playBtn = this.playBtn
        if (busy) {
            playBtn!!.setLoading(if (AppState.darkUi) -328966 else -16777216, true)
        } else {
            playBtn!!.setLoad(false)
        }
    }

    fun updateMiniShape() {
        if (this.miniBar != null) {
            if (!this.miniMode) {
                this.miniColor = 0
                this.miniRect = null
                this.miniPaint = null
                return
            }
            if (this.miniRect == null) {
                this.miniRect = RectF()
            }
            val miniColor = if (AppState.darkUi) -5197648 else -16777216
            if (this.miniColor != miniColor) {
                this.miniColor = miniColor
                if (this.miniPaint == null) {
                    val paint = Paint()
                    this.miniPaint = paint
                    paint.setAntiAlias(true)
                    this.miniPaint!!.setStyle(Paint.Style.STROKE)
                    this.miniPaint!!.setStrokeWidth(MainApp.dp1.toFloat())
                }
                this.miniPaint!!.setColor(this.miniColor)
                invalidate()
            }
        }
    }

    fun updateMiniElevation() {
        if (this.miniBar == null) {
            return
        }
        if (!this.miniMode) {
            setElevation(0.0f)
            setClipToOutline(false)
        } else {
            setElevation(MainApp.dp16.toFloat())
            setOutlineProvider(RoundOutline())
            setClipToOutline(true)
        }
    }

    public override fun onConfigurationChanged(configuration: Configuration?) {
        super.onConfigurationChanged(configuration)
        setPos(false)
    }

    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (canSelect()) {
            selectArea(0, 0, getWidth(), getHeight())
        }
    }

    public override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        setPos(false)
        if (changed && canSelect()) {
            selectArea(left, top, right, bottom)
        }
    }

    public override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        setPos(false)
        val rectF = this.miniRect
        if (rectF != null) {
            val inset = MainApp.dp1 / 2.0f
            rectF.set(inset, inset, width - inset, height - inset)
        }
    }

    fun moveMini(x: Int, y: Int) {
        var x = x
        var y = y
        if (this.alive) {
            val dp48Px = MainApp.dp48
            val minX = this.minX
            var leftX = dp48Px - minX
            val minY = this.minY
            var upY = dp48Px - minY
            val right = x + dp48Px
            val maxX = this.maxX
            if (right > maxX) {
                x = maxX - dp48Px
            }
            val bottom = y + dp48Px
            val maxY = this.maxY
            if (bottom > maxY) {
                y = maxY - dp48Px
            }
            if (x >= leftX) {
                leftX = x
            }
            if (y >= upY) {
                upY = y
            }
            ZtriSettings.editLocal { set(ZtriSettings.readLtX, leftX.toFloat()) }
            ZtriSettings.editLocal { set(ZtriSettings.readRtX, (maxX - (minX + leftX)).toFloat()) }
            ZtriSettings.editLocal { set(ZtriSettings.readUpY, upY.toFloat()) }
            ZtriSettings.editLocal { set(ZtriSettings.readDnY, (maxY - (minY + upY)).toFloat()) }
            setX((leftX + this.miniX).toFloat())
            setY((upY + this.miniY).toFloat())
        }
    }

    fun saveMiniPos() {
        val context = this.ctx
        val prefZtriR: PrefZtri? = if (context == null) null else PrefZtri.get(context)
        if (context == null || prefZtriR == null) {
            return
        }
        prefZtriR.putFloat("mReadLtX", ZtriSettings.readLtX.value)
        prefZtriR.putFloat("mReadRtX", ZtriSettings.readRtX.value)
        prefZtriR.putFloat("mReadUpY", ZtriSettings.readUpY.value)
        prefZtriR.putFloat("mReadDnY", ZtriSettings.readDnY.value)
        prefZtriR.saveAsync()
    }

    fun applyTtsIcons() {
        val res: Int
        val rewindIcon: Int
        if (this.layoutRtl) {
            if (AppState.darkUi) {
                res = AppR.drawable.baseline_fast_forward_dark_24
                rewindIcon = AppR.drawable.baseline_fast_rewind_dark_24
            } else {
                res = AppR.drawable.baseline_fast_forward_black_24
                rewindIcon = AppR.drawable.baseline_fast_rewind_black_24
            }
        } else if (AppState.darkUi) {
            res = AppR.drawable.baseline_fast_rewind_dark_24
            rewindIcon = AppR.drawable.baseline_fast_forward_dark_24
        } else {
            res = AppR.drawable.baseline_fast_rewind_black_24
            rewindIcon = AppR.drawable.baseline_fast_forward_black_24
        }
        val myButtonImage = this.prevBtn
        if (myButtonImage != null) {
            myButtonImage.setImageResource(res)
            this.nextBtn!!.setImageResource(rewindIcon)
        }
        val rewindBtn = this.rewindBtn
        if (rewindBtn != null) {
            rewindBtn.setImageResource(res)
            this.forwardBtn!!.setImageResource(rewindIcon)
        }
    }

    fun applyWindow(statusColor: Int, fullScreenSet: Boolean) {
        var statusColor = statusColor
        var found: Boolean
        val mainActivity = this.activity
        val window: Window? = if (mainActivity == null) null else mainActivity.getWindow()
        if (mainActivity == null || window == null) {
            return
        }
        val barsHidden = this.barsHidden
        val darkUi = AppState.darkUi
        var changed = true
        if (barsHidden != darkUi) {
            this.barsHidden = darkUi
            found = true
        } else {
            found = false
        }
        val textColor = MainUtil.editorTextColor()
        if (textColor != 0) {
            statusColor = MainUtil.blendColors(statusColor, textColor)
        }
        if (this.statusColor != statusColor) {
            this.statusColor = statusColor
            if (MainConst.isApi30) {
                this.webBody!!.topColor = statusColor
            } else {
                MainUtil.setWindowStatusColor(window, statusColor)
            }
            found = true
        }
        val sdkInt = Build.VERSION.SDK_INT
        val naviColor = this.naviColor
        if (sdkInt >= 26) {
            if (naviColor != statusColor) {
                this.naviColor = statusColor
                if (MainConst.isApi30) {
                    this.webBody!!.botColor = statusColor
                } else {
                    MainUtil.setNavBarColor(window, statusColor)
                }
            } else {
                changed = found
            }
        } else if (naviColor != -16777216) {
            this.naviColor = -16777216
            if (MainConst.isApi30) {
                this.webBody!!.botColor = -16777216
            } else {
                MainUtil.setNavBarColor(window, -16777216)
            }
        } else {
            changed = found
        }
        if (changed) {
            this.fullScreenSet = fullScreenSet
            post(ApplyBarAppearanceTask())
        }
    }

    fun setActionMode(actionMode: ActionMode?) {
        this.actionModeRef = null
        if (actionMode != null) {
            try {
                val menu = actionMode.getMenu()
                if (menu != null) {
                    val size = menu.size()
                    var maxId = -1
                    var maxOrder = -1
                    for (index in 0..<size) {
                        val item = menu.getItem(index)
                        if (item != null) {
                            maxId = max(maxId, item.getItemId())
                            maxOrder = max(maxOrder, item.getOrder())
                        }
                    }
                    menu.add(0, maxId + 1, maxOrder + 1, AppR.string.play).setOnMenuItemClickListener(
                        EvalJsStep()
                    )
                    menu.add(0, maxId + 2, maxOrder + 2, AppR.string.google_trans)
                        .setOnMenuItemClickListener(
                            JsResultStep()
                        )
                    this.actionModeRef = actionMode
                    val webNestView = this.page
                    if (webNestView != null) {
                        webNestView.post(InvalidateActionModeTask())
                    }
                }
            } catch (unused: Exception) {
            }
        }
    }

    /**
     * The browser's top bar lies over the top of the reading view (its web view starts under the status bar), so the
     * page's own 20 px top padding left the title hidden behind the bar (2026-11-21: on Wikipedia only a "-" of
     * "Switzerland - Wikipedia" showed). Moves the content down by the part of the reader the top bar covers; a bar at
     * the bottom of the screen covers nothing here.
     */
    fun fitUnderTopBar(attempt: Int = 0) {
        val web = this.page ?: return
        val bar = (this.activity as? com.mycompany.app.web.WebViewActivity)?.topBarFrame ?: return
        web.postDelayed({
            // the reader fades in after loading: wait until it is on screen (up to ~2 s)
            if (!web.isShown || web.height <= 0) {
                if (attempt < 12 && this.page === web) fitUnderTopBar(attempt + 1)
                return@postDelayed
            }
            if (!bar.isShown || bar.height <= 0) return@postDelayed
            val w = IntArray(2)
            val b = IntArray(2)
            web.getLocationOnScreen(w)
            bar.getLocationOnScreen(b)
            // only a bar along the top edge of the reader
            if (b[1] > w[1] + bar.height) return@postDelayed
            val covered = (b[1] + bar.height - w[1]).coerceIn(0, web.height / 3)
            if (covered <= 0) return@postDelayed
            val css = Math.round(covered / web.resources.displayMetrics.density) + 20
            evalJsNow(web, "(function(){if(document.body)document.body.style.paddingTop='" + css + "px';})();", true)
        }, if (attempt == 0) 0L else 160L)
    }

    fun setReadHtml(innerHTML: String?) {
        val webReadTask = this.readTask
        if (webReadTask == null) {
            return
        }
        webReadTask.startLoad(innerHTML)
    }

    fun applyTtsSettings() {
        val ttsReady = initTts()
        if (this.ttsEngine != null) {
            var ttsRateSet = TtsSettings.ttsRate.value
            var ttsPitchSet = 0.5f
            if (ttsRateSet < 0.5f) {
                TtsSettings.editLocal { set(TtsSettings.ttsRate, 0.5f) }
                ttsRateSet = 0.5f
            } else if (ttsRateSet > 3.0f) {
                TtsSettings.editLocal { set(TtsSettings.ttsRate, 3.0f) }
                ttsRateSet = 3.0f
            }
            val ttsPitch = TtsSettings.ttsPitch.value
            if (ttsPitch < 0.5f) {
                TtsSettings.editLocal { set(TtsSettings.ttsPitch, 0.5f) }
            } else {
                ttsPitchSet = 2.0f
                if (ttsPitch > 2.0f) {
                    TtsSettings.editLocal { set(TtsSettings.ttsPitch, 2.0f) }
                } else {
                    ttsPitchSet = ttsPitch
                }
            }
            try {
                if (ttsReady) {
                    this.ttsRateSet = ttsRateSet
                    this.ttsPitchSet = ttsPitchSet
                    if ((ttsRateSet).compareTo(1.0f) != 0) {
                        this.ttsEngine!!.setSpeechRate(TtsSettings.ttsRate.value)
                    }
                    if ((TtsSettings.ttsPitch.value).compareTo(1.0f) != 0) {
                        this.ttsEngine!!.setPitch(TtsSettings.ttsPitch.value)
                        return
                    }
                    return
                }
                if ((ttsRateSet).compareTo(this.ttsRateSet) != 0) {
                    val ttsRate = TtsSettings.ttsRate.value
                    this.ttsRateSet = ttsRate
                    this.ttsEngine!!.setSpeechRate(ttsRate)
                }
                if ((TtsSettings.ttsPitch.value).compareTo(this.ttsPitchSet) != 0) {
                    val newTtsPitchSet = TtsSettings.ttsPitch.value
                    this.ttsPitchSet = newTtsPitchSet
                    this.ttsEngine!!.setPitch(newTtsPitchSet)
                }
            } catch (unused: Exception) {
            }
        }
    }

    fun loadPage(pageUrl: String?) {
        if (this.page != null) {
            val isHttp = isHttpUrl(pageUrl)
            val loadPending = this.loadPending
            if (isHttp) {
                if (loadPending) {
                    this.loadPending = false
                    val webNestView = this.page
                    if (webNestView != null) {
                        webNestView.post(RemoveJsBridgeTask())
                        return
                    }
                    return
                }
                return
            }
            if (loadPending) {
                return
            }
            this.loadPending = true
            val webNestView = this.page
            if (webNestView == null) {
                return
            }
            webNestView.post(AddJsBridgeTask())
        }
    }

    /**
     * Sets the read-aloud language: the TTS setting, else the translation target, the news
     * locale, the page language or the device locale. Falls back to the device locale when the
     * engine lacks the language.
     */
    fun applyTtsLang() {
        var locale: Locale? = null
        val language: String?
        if (!TextUtils.isEmpty(TtsSettings.ttsLang.value)) {
            language = TtsSettings.ttsLang.value
        } else if (this.autoTrans && !TextUtils.isEmpty(AlbumSettings.transCode.value)) {
            language = AlbumSettings.transCode.value
        } else if (this.readFromHtml) {
            locale = this.newsLocale
            language = if (locale != null) locale.toString() else null
        } else if (!TextUtils.isEmpty(this.pageLang)) {
            language = this.pageLang
        } else if (TextUtils.isEmpty(this.ttsLang)) {
            locale = MainApp.currentLocale()
            language = locale.toString()
        } else {
            language = this.ttsLang
        }
        if (MainUtil.sameObject(this.ttsLangSet, language)) {
            return
        }
        this.ttsLangSet = language
        if (locale == null) {
            locale = parseLocale(language)
        }
        if (locale == null) {
            locale = MainApp.currentLocale()
        } else {
            var available: Int
            try {
                available = this.ttsEngine!!.isLanguageAvailable(locale)
            } catch (unused: Exception) {
                available = -2
            }
            if (available != TextToSpeech.LANG_AVAILABLE && available != TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                locale = MainApp.currentLocale()
            }
        }
        try {
            this.ttsEngine!!.setLanguage(locale)
        } catch (unused: Exception) {
        }
    }

    fun lockClick(): Boolean {
        if (this.clickLocked) {
            return true
        }
        this.clickLocked = true
        val handler = this.uiHandler
        if (handler == null) {
            return false
        }
        handler.post(UnlockClickTask())
        return false
    }

    fun highlightRange(rangeStart: Int, rangeEnd: Int, ttsIndex: Int, force: Boolean) {
        var force = force
        if (this.ttsRestart) {
            this.ttsRestart = false
            force = true
        }
        if ((ZtriSettings.readAccent.value || force) && this.page != null) {
            val arrayList = this.ttsParagraphs
            val ttsItem =
                if (arrayList == null || ttsIndex < 0 || ttsIndex >= arrayList.size) null else this.ttsParagraphs!!.get(ttsIndex) as TtsItem?
            if (ttsItem == null) {
                return
            }
            val end = ttsItem.end
            val selStart = rangeStart + end
            val selEnd = end + rangeEnd
            val sb = StringBuilder("(function(){var ele=document.getElementById('")
            sb.append(ttsItem.start).append("');setSelRange(ele,").append(selStart).append(",")
            sb.append(selEnd)
            sb.append(");")
            if (force) {
                sb.append("var tag='onTtsMark';var val=0;var bcr=null;if(range){bcr=range.getBoundingClientRect();}else{bcr=ele.getBoundingClientRect();}if(bcr){val=bcr.top+','+bcr.bottom;}android.onJsResult(tag,val);")
                if (!ZtriSettings.readAccent.value) {
                    sb.append("window.getSelection().removeAllRanges();")
                }
            }
            sb.append("})();")
            evalJsNow(this.page, sb.toString(), true)
        }
    }

    fun closeAll() {
        dismissDialogs()
        dismissSaveMenu()
        dismissMoreMenu()
        finishActionMode()
        val dialogReadListener = this.readListener
        if (dialogReadListener != null) {
            dialogReadListener.applyColors(this.statusColor, this.naviColor, this.windowColor)
        }
        this.hideAnimValue = 1.0f
        this.hideAnimPosted = false
        val valueAnimatorOfFloat = ValueAnimator.ofFloat(1.0f, 0.0f)
        this.hideAnim = valueAnimatorOfFloat
        valueAnimatorOfFloat.setDuration(200L)
        this.hideAnim!!.interpolator = android.view.animation.AccelerateInterpolator()
        this.hideAnim!!.addUpdateListener(HideAnimUpdate())
        this.hideAnim!!.addListener(CloseReaderAnimListener())
        val valueAnimator = this.showAnim
        if (valueAnimator != null) {
            valueAnimator.cancel()
            this.showAnim = null
        }
        this.hideAnim!!.start()
    }

    fun updateSpeakBar() {
        val myFadeFrame: MyFadeFrame? = if (this.speakMode) null else this.speakBar
        if (this.speakMode || myFadeFrame == null) {
            return
        }
        if (this.miniMode || this.miniAnim != null) {
            myFadeFrame.hide(false)
        } else {
            updateBottomRows()
            this.speakBar!!.show(true)
        }
    }

    fun handleBack(): Boolean {
        var handled = false
        if (this.alive && this.hideAnim == null) {
            if (this.miniMode) {
                closeReader(this.readListener)
                return false
            }
            handled = true
            if (!this.closing) {
                this.closing = true
                closeAll()
            }
            return true
        }
        return handled
    }

    fun showTrans(jsResultI: String?) {
        if (this.activity == null || hasDialog()) {
            return
        }
        val dialogViewTrans = this.transDialog
        var locale: String? = null
        if (dialogViewTrans != null) {
            dialogViewTrans.dismiss()
            this.transDialog = null
        }
        if (TextUtils.isEmpty(jsResultI) || jsResultI == "\"\"") {
            showToast(this.ctx, AppR.string.empty)
            return
        }
        if (this.ttsState.toInt() == 1) {
            pauseTts(true)
        }
        if (this.readFromHtml) {
            val newsLocale = this.newsLocale
            if (newsLocale != null) {
                locale = newsLocale.toString()
            }
        } else if (!TextUtils.isEmpty(this.pageLang)) {
            locale = this.pageLang
        }
        val transDialog = DialogViewTrans(this.activity, jsResultI, locale, this.transLang)
        this.transDialog = transDialog
        transDialog.setOnDismissListener(SpeakNowDismiss())
    }

    fun showCoverProgress() {
        val myCoverView = this.loadCover
        if (myCoverView == null) {
            return
        }
        this.coverLoading = false
        myCoverView.showNow(true)
    }

    fun translatePage(enabled: Boolean) {
        if (this.alive) {
            if (this.transText != null) {
                showToast(this.ctx, AppR.string.wait_retry)
                return
            }
            if (enabled) {
                this.transFromSpeak = true
                showCoverProgress()
            } else {
                this.speakMode = true
                val myButtonImage = this.speakFocusBtn
                if (myButtonImage != null) {
                    myButtonImage.setLoading(if (AppState.darkUi) -328966 else -16777216, true)
                }
                updatePlayIcon()
            }
            this.transText = MainTransText(
                this.activity,
                this.ctx,
                this.webBody,
                this.paragraphList,
                0,
                this.webHtml,
                ReleaseStep()
            )
        }
    }

    fun showSpeakBar(show: Boolean) {
        if (this.speakBar == null || this.speakBarShown) {
            return
        }
        this.speakBarShown = true
        this.ttsMoveIndex = -1234
        this.ttsJump = false
        this.speakFocusBtn = this.stopBtn
        if (isBusy(true)) {
            this.speakFocusBtn!!.setLoading(if (AppState.darkUi) -328966 else -16777216, true)
        }
        updatePlayIcon()
        if (this.miniMode || this.miniAnim != null) {
            this.speakBar!!.hide(false)
        } else {
            updateBottomRows()
            this.speakBar!!.show(show)
        }
    }

    internal inner class TransControlActions : TransCtrlListener {
        override fun onOriginal() {
            val dialogViewRead = this@DialogViewRead
            if (!TextUtils.isEmpty(dialogViewRead.transJsCache)) {
                dialogViewRead.dismissTransSheet()
                return
            }
            if (dialogViewRead.transState.toInt() == 1) {
                showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                return
            }
            dialogViewRead.dismissTransSheet()
            if (dialogViewRead.transState.toInt() == 3) {
                evalConfirmJs(dialogViewRead.page, "restore")
            }
        }

        override fun onMenu(view: View?) {
            val dialogViewRead = this@DialogViewRead
            val dialogTransLang: DialogTransLang? = if (dialogViewRead.activity == null) null else dialogViewRead.transLangDialog
            if (dialogViewRead.activity == null || dialogTransLang != null) {
                return
            }
            if (!DataTrans.instance(dialogViewRead.ctx).hasLanguages() && dialogViewRead.transState.toInt() == 1) {
                showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                return
            }
            val transLangDialog =
                DialogTransLang(dialogViewRead.activity, false, dialogViewRead.TransLangPicked())
            dialogViewRead.transLangDialog = transLangDialog
            transLangDialog.setOnDismissListener(dialogViewRead.TransLangDialogDismiss())
        }

        override fun onTranslate() {
            val dialogViewRead = this@DialogViewRead
            if (!TextUtils.isEmpty(dialogViewRead.transJsCache)) {
                dialogViewRead.dismissTransSheet()
                dialogViewRead.setTransJs(true)
            } else {
                if (dialogViewRead.transState.toInt() == 1) {
                    showToast(dialogViewRead.ctx, AppR.string.wait_retry)
                    return
                }
                dialogViewRead.dismissTransSheet()
                if (dialogViewRead.transState.toInt() == 3) {
                    evalConfirmJs(dialogViewRead.page, "confirm")
                }
            }
        }

        override fun onClose() {
        }
    }

    private inner class LocalWebViewClient : WebViewClient() {

        inner class ShowPinchGuideStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                val handler: Handler? = if (ReadSettings.guideRead.value && dialogViewRead.pinchGuide == null && dialogViewRead.alive) dialogViewRead.uiHandler else null
                if (ReadSettings.guideRead.value && dialogViewRead.pinchGuide == null && dialogViewRead.alive && handler != null) {
                    handler.post(dialogViewRead.ShowPinchGuideTask())
                }
            }
        }

        inner class StartAutoTransStep : Runnable {
            override fun run() {
                val dialogViewRead = this@DialogViewRead
                dialogViewRead.startAutoTrans()
            }
        }

        override fun onPageCommitVisible(webView: WebView?, url: String?) {
            super.onPageCommitVisible(webView, url)
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.readOpened = true
            dialogViewRead.applyBold()
            if (dialogViewRead.readFromHtml) {
                dialogViewRead.hideCoverProgress()
            }
            val handler: Handler? = if (ReadSettings.guideRead.value) dialogViewRead.uiHandler else null
            if (ReadSettings.guideRead.value && handler != null) {
                handler.post(ShowPinchGuideStep())
            }
            if (dialogViewRead.transApplied) {
                dialogViewRead.startTrans()
            }
            if (dialogViewRead.htmlLoading) {
                dialogViewRead.pageVisible = true
                val webNestView = dialogViewRead.page
                if (webNestView == null) {
                    return
                }
                webNestView.post(StartAutoTransStep())
            }
        }

        override fun onPageFinished(webView: WebView?, url: String?) {
            val dialogViewRead = this@DialogViewRead
            val webNestView = dialogViewRead.page
            if (webNestView != null) {
                webNestView.setWebLoading(false)
                dialogViewRead.loadPage(url)
                if (!dialogViewRead.finishHandled && !TextUtils.isEmpty(url) && (url != dialogViewRead.lastFinishedUrl)) {
                    dialogViewRead.finishHandled = true
                    dialogViewRead.lastFinishedUrl = url
                    val handler = dialogViewRead.uiHandler
                    if (handler != null) {
                        handler.post(dialogViewRead.ClearCacheTask())
                    }
                }
                dialogViewRead.applyBold()
                dialogViewRead.fitUnderTopBar()
                dialogViewRead.startTrans()
                if (TextUtils.isEmpty(dialogViewRead.pageJs)) {
                    dialogViewRead.runInBackground(dialogViewRead.InjectPageJsTask())
                } else {
                    evalJsNow(dialogViewRead.page, dialogViewRead.pageJs, true)
                }
            }
        }

        override fun onPageStarted(webView: WebView?, url: String?, bitmap: Bitmap?) {
            val dialogViewRead = this@DialogViewRead
            val webNestView = dialogViewRead.page
            if (webNestView == null) {
                return
            }
            webNestView.setWebLoading(true)
            dialogViewRead.loadPage(url)
        }

        override fun onRenderProcessGone(
            webView: WebView?,
            renderProcessGoneDetail: RenderProcessGoneDetail?
        ): Boolean {
            val dialogViewRead = this@DialogViewRead
            dialogViewRead.page = null
            onRenderGone(webView, renderProcessGoneDetail)
            val handler = dialogViewRead.uiHandler
            if (handler == null) {
                return true
            }
            handler.post(dialogViewRead.HandleBackTask())
            return true
        }

        override fun shouldInterceptRequest(
            webView: WebView?,
            request: WebResourceRequest?
        ): WebResourceResponse? {
            val view = this@DialogViewRead
            if (view.page == null || request == null || request.getUrl() == null) {
                return null
            }
            val url = request.getUrl().toString()
            view.loadPage(url)
            // The reader page asks for the user's own font file when one is chosen.
            if (!TextUtils.isEmpty(url) && ReadSettings.userFont.value && url.contains("soul_user_font.ttf")) {
                try {
                    return WebResourceResponse("application/x-font-ttf", "UTF-8", openFileInput(MainUtil.readerFontPath(view.ctx)))
                } catch (unused: Exception) {
                }
            }
            return null
        }

        override fun shouldOverrideUrlLoading(
            webView: WebView?,
            webResourceRequest: WebResourceRequest?
        ): Boolean {
            val dialogViewRead = this@DialogViewRead
            if (dialogViewRead.page == null || webResourceRequest == null || webResourceRequest.getUrl() == null) {
                return false
            }
            val url = webResourceRequest.getUrl().toString()
            if (TextUtils.isEmpty(url)) {
                return false
            }
            dialogViewRead.loadPage(url)
            if (!dialogViewRead.urlOverridden) {
                return false
            }
            val dialogReadListener = dialogViewRead.readListener
            if (dialogReadListener == null) {
                return true
            }
            dialogReadListener.openUrl(url)
            return true
        }
    }

    companion object {

        /**
         * Splits reader paragraphs into text-to-speech chunks within the engine's input limit,
         * breaking at a line, sentence, comma or word boundary in the second half of each chunk.
         *
         * @param translated speak the translated text (`isSpeaking`) instead of the original (`b`)
         */
        @JvmStatic
        fun ttsChunks(paragraphs: List<Any?>?, translated: Boolean): ArrayList<Any?> {
            val chunks: ArrayList<Any?> = ArrayList<Any?>()
            val limit = min(TextToSpeech.getMaxSpeechInputLength(), 160)
            for (item2 in paragraphs!!) {
                val item = item2 as ReadItem?
                if (item == null) {
                    continue
                }
                val text = if (translated) item.transText else item.articleTitle
                val length = if (text != null) text.length else 0
                var start = 0
                while (start < length) {
                    var end = min(start + limit, length)
                    if (end < length) {
                        end = Companion.splitPoint(text!!, start, end)
                    }
                    val chunk = text!!.substring(start, end).replace("\n", "")
                    if (!TextUtils.isEmpty(chunk) && !TextUtils.isEmpty(chunk.trim { it <= ' ' })) {
                        val tts = TtsItem()
                        tts.start = item.index
                        tts.end = item.end + start
                        tts.speech = chunk
                        chunks.add(tts)
                        item.end = 0
                    }
                    start = end
                }
            }
            return chunks
        }

        /** Best break before `end` in the second half of the chunk, skipping following spaces.  */
        @JvmStatic
        fun splitPoint(text: String?, start: Int, end: Int): Int {
            val half = start + (end - start) / 2
            val last = end - 1
            var cut = -1
            var at = text!!.lastIndexOf('\n', last)
            if (at > half && at + 1 < end) {
                cut = at + 1
            } else if ((text.lastIndexOf(". ", last).also { at = it }) > half && at + 2 < end) {
                cut = at + 2
            } else if ((text.lastIndexOf(", ", last).also { at = it }) > half && at + 2 < end) {
                cut = at + 2
            } else if ((text.lastIndexOf(' ', last).also { at = it }) > half && at + 1 < end) {
                cut = at + 1
            }
            if (cut <= start || cut >= end) {
                return end
            }
            var skip = cut
            while (text.startsWith(" ", skip) || text.startsWith("\n", skip)) {
                skip++
            }
            return if (skip > start && skip < end) skip else cut
        }

        private fun nativeScript(count: Int, translated: Boolean): String? {
            var parts = if (translated) MainNative.viewReadTransParts else MainNative.viewReadParts
            if (!partsReady(count, parts)) {
                parts = ArrayList<Any?>()
                for (index in 0..<count) {
                    val part = if (translated) MainUtil.getTtsPartJs2(index) else MainUtil.getTtsPartJs1(index)
                    if (TextUtils.isEmpty(part)) {
                        return null
                    }
                    parts.add(part)
                }
                if (translated) {
                    MainNative.viewReadTransParts = parts
                } else {
                    MainNative.viewReadParts = parts
                }
            }
            if (parts!!.size != count) {
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
    }
}
