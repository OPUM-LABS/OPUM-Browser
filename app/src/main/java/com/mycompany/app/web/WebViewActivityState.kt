@file:Suppress("unused", "PropertyName", "LocalVariableName")

package com.mycompany.app.web

import com.mycompany.app.main.MainUtil.copyTextToast
import com.mycompany.app.main.MainUri.moveUri
import com.mycompany.app.web.WebClean
import android.webkit.WebView
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebHmgLoad
import android.webkit.ValueCallback
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.ui.browser.IconButton
import com.mycompany.app.web.WebViewActivity
import android.content.Intent
import android.animation.ValueAnimator
import com.mycompany.app.web.WebNestFrame
import com.mycompany.app.db.book.DbTabState.StateItem
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.web.WebReadTask
import com.mycompany.app.web.WebVideoPip
import com.mycompany.app.view.MyProgressBar
import com.mycompany.app.dialog.DialogConfirm
import com.mycompany.app.web.WebGridDialog
import com.mycompany.app.web.WebNestFrame.PageItem
import com.mycompany.app.web.WebVideoFrame
import com.mycompany.app.view.MyFadeFrame
import android.view.GestureDetector
import com.mycompany.app.view.MyFadeRelative
import com.mycompany.app.view.MyPopupMenu
import com.mycompany.app.ui.browser.ComposeTabOverview
import com.mycompany.app.web.WebTransControl
import com.mycompany.app.dialog.DialogSetMsg
import com.mycompany.app.dialog.DialogQuickEdit
import com.mycompany.app.quick.QuickSubView
import com.mycompany.app.dialog.DialogMenuList
import com.mycompany.app.wview.WebFltView
import com.mycompany.app.dialog.DialogTabEdit
import com.mycompany.app.web.WebViewActivity.TabLimitListener
import com.mycompany.app.main.MainFilterSvc
import com.mycompany.app.web.WebViewActivity.TabAddListener
import com.mycompany.app.web.WebViewActivity.TabViewListener
import com.mycompany.app.main.MainScriptSvc
import com.mycompany.app.quick.QuickNews
import android.speech.tts.TextToSpeech
import com.mycompany.app.script.ScriptCache
import com.mycompany.app.web.WebViewActivity.PrevPageListener
import com.mycompany.app.ui.browser.BarFace
import com.mycompany.app.main.MainTransNews
import com.mycompany.app.dialog.DialogUpdateScript
import com.mycompany.app.view.MyTouchFrame
import com.mycompany.app.dialog.DialogSetDesk
import com.mycompany.app.dialog.DialogDownFont
import com.mycompany.app.wview.WebTtsView
import com.mycompany.app.dialog.DialogSetUrl
import com.mycompany.app.web.WebViewActivity.PopItem
import com.mycompany.app.dialog.DialogSetRate
import com.mycompany.app.ui.browser.ScrollNavi
import com.mycompany.app.dialog.DialogSetDark
import com.mycompany.app.view.MyGesNoti
import com.mycompany.app.dialog.DialogBlockImage
import com.mycompany.app.dialog.DialogSetUseTts
import com.mycompany.app.dialog.DialogWebBookEdit
import android.webkit.WebChromeClient.FileChooserParams
import com.mycompany.app.wview.WebAreaMenu
import com.mycompany.app.wview.WebAreaView
import com.mycompany.app.view.MySnackbar
import com.mycompany.app.behavior.MyBehaviorTop
import com.mycompany.app.dialog.DialogViewRead
import com.mycompany.app.main.MainUri.UriItem
import com.mycompany.app.dialog.DialogGuideNoti
import android.print.PrintDocumentAdapter
import com.mycompany.app.view.MyBarView
import com.mycompany.app.dialog.DialogDownUrl
import com.mycompany.app.dialog.DialogViewSrc
import com.mycompany.app.dialog.DialogGuideArea
import com.mycompany.app.dialog.DialogSetPrivacy
import com.mycompany.app.dialog.DialogDownBlob
import com.mycompany.app.dialog.DialogSetCookie
import com.mycompany.app.dialog.DialogSetScrFil
import android.webkit.JsResult
import com.mycompany.app.dialog.DialogWebCerti
import android.webkit.PermissionRequest
import android.webkit.GeolocationPermissions
import com.mycompany.app.view.GlideRequests
import com.mycompany.app.quick.QuickSchEdit
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Message
import android.print.PrintJob
import android.view.ActionMode
import android.view.View
import java.io.File
import java.util.ArrayList

/** The browser activity's find state (moved out of `WebViewActivity`). */
class WebFindState {
    @JvmField
    var touchDownY: Float = 0f
    @JvmField
    var openInNextPageBusy: Boolean = false
    @JvmField
    var selectTabBusy: Boolean = false
}

/** The browser activity's video state (moved out of `WebViewActivity`). */
class WebVideoState {
    @JvmField
    var linkPillBusy: Boolean = false
    @JvmField
    var videoEnterBusy: Boolean = false
    @JvmField
    var linkPillX: Int = 0
    @JvmField
    var videoExitBusy: Boolean = false
    @JvmField
    var linkPillY: Int = 0
    @JvmField
    var longPressLinkUrl: String? = null
    @JvmField
    var areaMenuBusy: Boolean = false
    @JvmField
    var longPressSrcUrl: String? = null
    @JvmField
    var pendingTabUrl: String? = null
    @JvmField
    var longPressTitle: String? = null
    @JvmField
    var webViewActivityBusy: Boolean = false
    @JvmField
    var videoCheckUrl: String? = null
    @JvmField
    var areaToolbarBusy: Boolean = false
    @JvmField
    var longPressInspectBusy: Boolean = false
    @JvmField
    var longPressPending: Boolean = false
    @JvmField
    var videoPip: WebVideoPip? = null
    @JvmField
    var rateRetryPending: Boolean = false
    @JvmField
    var popItem: PopItem? = null
    @JvmField
    var blockedTouchDownTime: Long = 0
    @JvmField
    var videoDialogOpen: Boolean = false
    @JvmField
    var blobLength: Long = 0
    @JvmField
    var eventReceiverItem: Intent? = null
    @JvmField
    var areaMenu: WebAreaMenu? = null
    @JvmField
    var eventReceiverText: String? = null
    @JvmField
    var addTabImplBusy: Boolean = false
    @JvmField
    var downUrlDialog: DialogDownUrl? = null
    @JvmField
    var longPressFollowUpBusy: Boolean = false
    @JvmField
    var freshLoad: Boolean = false
    @JvmField
    var showNavBar: Boolean = false
    @JvmField
    var webQuickListenerBusy: Boolean = false
    @JvmField
    var pageFrameBusy: Boolean = false
    @JvmField
    var moveHintBusy: Boolean = false
    @JvmField
    var quickSchEdit: QuickSchEdit? = null
    @JvmField
    var backPlayOn: Boolean = false
}

/** The browser activity's translate state (moved out of `WebViewActivity`). */
class WebTranslateState {
    @JvmField
    var keyboardHeight: Int = 0
    @JvmField
    var transLangMenuListenerBusy: Boolean = false
    @JvmField
    var pendingTransControl: WebTransControl? = null
}

/** The browser activity's speech state (moved out of `WebViewActivity`). */
class WebSpeechState {
    @JvmField
    var tts: TextToSpeech? = null
    @JvmField
    var jsBridgeBusy: Boolean = false
    @JvmField
    var ttsPanelShown: Boolean = false
    @JvmField
    var ttsRate: Float = 0f
    @JvmField
    var ttsPitch: Float = 0f
    @JvmField
    var ttsLang: String? = null
    @JvmField
    var ttsLocale: String? = null
    @JvmField
    var ttsButton: WebTtsView? = null
}

/** The browser activity's tabs state (moved out of `WebViewActivity`). */
class WebTabsState {
    @JvmField
    var tabUndoAllowed: Boolean = false
    @JvmField
    var currentFrame: WebNestFrame? = null
    @JvmField
    var tabViewListener: TabViewListener? = null
    @JvmField
    var composeOverview: ComposeTabOverview? = null
    /** Group (id, name, colour) the next added tab joins: set by "New tab" in a group of the tab overview, used once. */
    @JvmField
    var nextTabGroup: Triple<Long, String?, Int>? = null
    @JvmField
    var tabItems: ArrayList<WebTabItem?>? = null
    @JvmField
    var tabUndoSnackbarFrame: WebNestFrame? = null
    @JvmField
    var tabSwipeState: Byte = 0
    @JvmField
    var swipePrevFrame: WebNestFrame? = null
    @JvmField
    var touchDownX: Float = 0f
    @JvmField
    var swipeNextFrame: WebNestFrame? = null
    @JvmField
    var tabSwipeForward: Boolean = false
    @JvmField
    var pendingDeskMode: Boolean = false
    @JvmField
    var setUrlDialog: DialogSetUrl? = null
    @JvmField
    var tabBarBusy: Boolean = false
    @JvmField
    var restartBusy: Boolean = false
    @JvmField
    var tabDragMoved: Boolean = false
    @JvmField
    var tabMenuDragFrom: Int = 0
    @JvmField
    var gestureDirection: Byte = 0
    @JvmField
    var goBackAnimBusy: Boolean = false
    @JvmField
    var layoutRtl: Boolean = false
    @JvmField
    var tabDragging: Boolean = false
    @JvmField
    var goBackPageBusy: Boolean = false
    @JvmField
    var dragTabIndex: Int = 0
    @JvmField
    var startGoForwardBusy: Boolean = false
    @JvmField
    var isTop: Boolean = false
    @JvmField
    var animateFrame: Boolean = false
    @JvmField
    var nestView: WebNestView? = null
    @JvmField
    var touchBusy: Boolean = false
    @JvmField
    var swipeFrame: WebNestFrame? = null
    @JvmField
    var touchTabIndex: Int = 0
    @JvmField
    var createBackTabBusy: Boolean = false
    @JvmField
    var reusedPending: Boolean = false
    @JvmField
    var backTabUrl: String? = null
    @JvmField
    var pageSwipeState: Byte = 0
    @JvmField
    var touchBarView: View? = null
    @JvmField
    var reloadTabViewsBusy: Boolean = false
    @JvmField
    var touchBarItem: Int = 0
    @JvmField
    var switchToTabBusy: Boolean = false
    @JvmField
    var redrawTabIndex: Int = 0
    @JvmField
    var exitAppBusy: Boolean = false
    @JvmField
    var finishTabAnimBusy: Boolean = false
    @JvmField
    var closingBackListener: PrevPageListener? = null
    @JvmField
    var jsResult: JsResult? = null
    @JvmField
    var closingFrame: WebNestFrame? = null
    @JvmField
    var animFrame: WebNestFrame? = null
    @JvmField
    var tabSwipeActive: Boolean = false
    @JvmField
    var switchIndex: Int = 0
    @JvmField
    var switchFrame: WebNestFrame? = null
    @JvmField
    var gestureDetector: GestureDetector? = null
    @JvmField
    var barsDisabledMode: Byte = 0
}

/** The browser activity's quick state (moved out of `WebViewActivity`). */
class WebQuickState {
    @JvmField
    var quickSubView: QuickSubView? = null
    @JvmField
    var longPressImage: String? = null
    @JvmField
    var quickNews: QuickNews? = null
    @JvmField
    var newsListType: Int = 0
    @JvmField
    var transNews: MainTransNews? = null
    @JvmField
    var longPressInspectBusy: Boolean = false
    @JvmField
    var fakeTsMisses: Int = 0
    @JvmField
    var webQuickListenerBusy: Boolean = false
    @JvmField
    var longPressVideo: String? = null
    @JvmField
    var quickAddBusy: Boolean = false
    @JvmField
    var newsRefreshTask: Runnable? = null
    @JvmField
    var quickSearchImeAnimationBusy: Boolean = false
    @JvmField
    var quickActionBusy: Boolean = false
    @JvmField
    var editingAddress: Boolean = false
    @JvmField
    var tabBarRefreshed: Boolean = false
    @JvmField
    var imeAnimationSet: Boolean = false
    @JvmField
    var sdkVersionChecked: Boolean = false
}

/** The browser activity's downloads state (moved out of `WebViewActivity`). */
class WebDownloadsState {
    @JvmField
    var videoMenuBusy: Boolean = false
}

/** The browser activity's bars state (moved out of `WebViewActivity`). */
class WebBarsState {
    @JvmField
    var findBarMargin: Int = 0
    @JvmField
    var addressEditAfterAdd: Boolean = false
    @JvmField
    var bottomDownIcon: com.mycompany.app.ui.browser.BarIndicator? = null
    @JvmField
    var siteFixMode: Byte = 0
    @JvmField
    var pageItem: PageItem? = null
    @JvmField
    var animPageIndex: Int = 0
    @JvmField
    var restoreDeskMode: Boolean = false
    @JvmField
    var touchFrame: MyTouchFrame? = null
    @JvmField
    var touchStartX: Float = 0f
    @JvmField
    var touchStartY: Float = 0f
    @JvmField
    var barAddressSource: String? = null
    @JvmField
    var barAddressCached: String? = null
    @JvmField
    var youIconColor: Int = 0
    @JvmField
    var edgeDirection: Byte = 0
    @JvmField
    var topBarView: MyBarView? = null
    @JvmField
    var bottomBarView: MyBarView? = null
    @JvmField
    var resetIcons: Boolean = false
    @JvmField
    var barTouchDown: Boolean = false
    @JvmField
    var editorBar: com.mycompany.app.view.EditorBar? = null
    @JvmField
    var overScrollMoves: Int = 0
    @JvmField
    var scrolledMoves: Int = 0
    @JvmField
    var addrLeftIcon: com.mycompany.app.view.AddrIcon? = null
    @JvmField
    var barTouchDecided: Boolean = false
    @JvmField
    var addrRightIcon: com.mycompany.app.view.AddrIcon? = null
    @JvmField
    var barLayoutMode: Byte = 0
    @JvmField
    var editorBarColor: Int = 0
    @JvmField
    var editorBarDark: Boolean = false
    @JvmField
    var playVideoInPlayerBusy: Boolean = false
    @JvmField
    var scriptAlreadyInstalled: Boolean = false
}

/** The browser activity's floating state (moved out of `WebViewActivity`). */
class WebFloatingState {
    @JvmField
    var lockButton: WebFltView? = null
    @JvmField
    var upButtonHidden: Boolean = false
    @JvmField
    var translateButton: WebFltView? = null
    @JvmField
    var popupMenu: MyPopupMenu? = null
    @JvmField
    var zoomButton: WebFltView? = null
    @JvmField
    var userButton1: WebFltView? = null
    @JvmField
    var edgeTouchActive: Boolean = false
    @JvmField
    var userButton2: WebFltView? = null
    @JvmField
    var userButton3: WebFltView? = null
    @JvmField
    var fullscreenButton: WebFltView? = null
    @JvmField
    var floatingShown: Boolean = false
    @JvmField
    var rssButton: WebFltView? = null
    @JvmField
    var rightScrollNavi: ScrollNavi? = null
    @JvmField
    var gestureMode: Byte = 0
    @JvmField
    var overlays: com.mycompany.app.ui.browser.OverlayStack? = null
    @JvmField
    var gestureHint: MyGesNoti? = null
    @JvmField
    var gestureZone: Byte = 0
    @JvmField
    var edgeHint: Byte = 0
    /** The "Fill login" bar shown on a login page ([LoginFillBar]), or null. */
    @JvmField
    var loginFillBar: android.view.View? = null
    /** The page the address fill bar was offered on (once per page, AddressFill). */
    var addrOfferedUrl: String? = null
    /** The floating button size the buttons were drawn with (FloatSettings.fltSize). */
    @JvmField
    var savedSize: Int = com.mycompany.app.pref.store.FloatSettings.fltSize.value
}

/** The browser activity's dialogs state (moved out of `WebViewActivity`). */
class WebDialogsState {
    /** The login about to be offered has a saved name with another password ("Update password?"). */
    @JvmField
    var infoPassUpdate: Boolean = false
    @JvmField
    var lockCheckedUrl: String? = null
    @JvmField
    var hasRealPage: Boolean = false
    @JvmField
    var applySystemBarsBusy: Boolean = false
    @JvmField
    var openTypeId: Long = 0
    @JvmField
    var quickEditDialog: DialogQuickEdit? = null
    @JvmField
    var savedZtwor: Boolean = false
    @JvmField
    var instaAppId: String? = null
    @JvmField
    var backPlaySrc: String? = null
    @JvmField
    var siteLockList: ArrayList<Any?>? = null
    @JvmField
    var openTypeUrl: String? = null
    @JvmField
    var progressBar: MyProgressBar? = null
    @JvmField
    var savedThemeImg: Boolean = false
    @JvmField
    var savedSecretLocked: Boolean = false
    @JvmField
    var menuListDialog: DialogMenuList? = null
    @JvmField
    var onYouTube: Boolean = false
    @JvmField
    var backPlayIsPaused: Boolean = false
    @JvmField
    var savePassHost: String? = null
    @JvmField
    var lastLoadUrl: String? = null
    @JvmField
    var showOpenTypeDialogText: String? = null
    @JvmField
    var savedZtwoZ: Int = 0
    @JvmField
    var transState: Byte = 0
    @JvmField
    var isVideoPage: Boolean = false
    @JvmField
    var savePassUser: String? = null
    @JvmField
    var handModeBar: com.mycompany.app.ui.browser.HandModeBar? = null
    @JvmField
    var unusedBusy: Boolean = false
    @JvmField
    var savedDarkHome: Boolean = false
    @JvmField
    var isVideoHost: Boolean = false
    @JvmField
    var addPullViewBusy: Boolean = false
    @JvmField
    var savePassWord: String? = null
    @JvmField
    var unusedBusy2: Boolean = false
    @JvmField
    var infoPassHost: String? = null
    @JvmField
    var tabEditDialog: DialogTabEdit? = null
    @JvmField
    var savedSecretx: Boolean = false
    @JvmField
    var videoRateOff: Boolean = false
    @JvmField
    var savePassUrl: String? = null
    @JvmField
    var menuItemBusy: Boolean = false
    @JvmField
    var infoPassUser: String? = null
    @JvmField
    var videoPageUrl: String? = null
    @JvmField
    var pageClientBusy: Boolean = false
    @JvmField
    var passInfoListenerText: String? = null
    @JvmField
    var infoPassWord: String? = null
    @JvmField
    var transReady: Boolean = false
    @JvmField
    var pageScale: Float = 0f
    @JvmField
    var jsBridgeBusy: Boolean = false
    @JvmField
    var openImagePath: String? = null
    @JvmField
    var infoPassUrl: String? = null
    @JvmField
    var lastScale: Float = 0f
    @JvmField
    var openImageFile: File? = null
    @JvmField
    var areaSelectBusy: Boolean = false
    @JvmField
    var addressMenuText: String? = null
    @JvmField
    var lockMenu: MyPopupMenu? = null
    @JvmField
    var snsUrl: String? = null
    @JvmField
    var openImageBitmap: Bitmap? = null
    @JvmField
    var areaResultCallbackText: String? = null
    @JvmField
    var menuItemCode: Int = 0
    @JvmField
    var segmentsFound: Boolean = false
    @JvmField
    var lockCheckedHost: String? = null
    @JvmField
    var youTubeUrl: String? = null
    @JvmField
    var openImageSaved: Boolean = false
    @JvmField
    var menuItemView: View? = null
    @JvmField
    var longParseBusy: Boolean = false
    @JvmField
    var exitOnBackArmed: Boolean = false
    @JvmField
    var savedEditorJ: Int = 0
    @JvmField
    var segmentLengths: ArrayList<Any?>? = null
    @JvmField
    var checkSiteLockBusy: Boolean = false
    @JvmField
    var imageUrl: String? = null
    @JvmField
    var openLoadedImageText: String? = null
    @JvmField
    var videoMode: Byte = 0
    @JvmField
    var segmentSplits: MutableList<Any?>? = null
    @JvmField
    var faceJson: String? = null
    @JvmField
    var openImageDownId: Long = 0
    @JvmField
    var downBlobBusy: Boolean = false
    @JvmField
    var longPressBusy: Boolean = false
    @JvmField
    var segmentUrls: MutableList<Any?>? = null
    @JvmField
    var scriptInstallDialog: DialogSetMsg? = null
    @JvmField
    var faceKind: Int = 0
    @JvmField
    var ttsSpeaking: Boolean = false
    @JvmField
    var updateScriptDialog: DialogUpdateScript? = null
    @JvmField
    var fansHtml: String? = null
    @JvmField
    var extraStart: Boolean = false
    @JvmField
    var tabBarScrollIndex: Int = 0
    @JvmField
    var fansPoster: String? = null
    @JvmField
    var gridDialog: WebGridDialog? = null
    @JvmField
    var fansMode: Int = 0
    @JvmField
    var setupStarted: Boolean = false
    @JvmField
    var siteDialog: WebSiteDialog? = null
    @JvmField
    var imageResults: ArrayList<com.mycompany.app.main.MainDownAdapter.DownListItem?>? = null
    @JvmField
    var videoJsonIndex: Int = 0
    @JvmField
    var openQuickFolderBusy: Boolean = false
    @JvmField
    var previewDialogUrl: String? = null
    @JvmField
    var actionModeMenuClickText: String? = null
    @JvmField
    var restoredPageUrl: String? = null
    @JvmField
    var fansVideoUrl: String? = null
    @JvmField
    var downloadItems: ArrayList<com.mycompany.app.main.MainDownAdapter.DownListItem?>? = null
    @JvmField
    var viewsCreated: Boolean = false
    @JvmField
    var addressPrefixMenu: MyPopupMenu? = null
    @JvmField
    var transCtrlMenu: MyPopupMenu? = null
    @JvmField
    var adsDialogUrl: String? = null
    @JvmField
    var colorsForStart: Boolean = false
    @JvmField
    var speechText: String? = null
    @JvmField
    var downloadFromBridge: Boolean = false
    @JvmField
    var setupDone: Boolean = false
    @JvmField
    var setDeskDialog: DialogSetDesk? = null
    @JvmField
    var downFontDialog: DialogDownFont? = null
    @JvmField
    var adsAllowed: Boolean = false
    @JvmField
    var pendingPageUrl: String? = null
    @JvmField
    var addedPlaySrc: String? = null
    @JvmField
    var sendAppToBackgroundBusy: Boolean = false
    @JvmField
    var siteFixModeNow: Int = 0
    @JvmField
    var backPlayPaused: Boolean = false
    @JvmField
    var phpDownload: Boolean = false
    @JvmField
    var linkActionUrl: String? = null
    @JvmField
    var pullViewAdded: Boolean = false
    @JvmField
    var siteFixMainFrame: Boolean = false
    @JvmField
    var backPlayAudio: Boolean = false
    @JvmField
    var downloadPageUrl: String? = null
    @JvmField
    var darkSettingsChanged: Boolean = false
    @JvmField
    var quickSettingsMenu: MyPopupMenu? = null
    @JvmField
    var lastSnsUrl: String? = null
    @JvmField
    var pausedSrc: String? = null
    @JvmField
    var topBarColor: Int = 0
    @JvmField
    var videoPaused: Boolean = false
    @JvmField
    var pageBarColor: Int = 0
    @JvmField
    var setRateDialog: DialogSetRate? = null
    @JvmField
    var savedEditorq: Int = 0
    @JvmField
    var bottomBarColor: Int = 0
    @JvmField
    var blobUrl: String? = null
    @JvmField
    var webBody: MyWebBody? = null
    @JvmField
    var windowBgColor: Int = 0
    @JvmField
    var blobDisposition: String? = null
    @JvmField
    var savedWebLand: Int = 0
    @JvmField
    var setDarkDialog: DialogSetDark? = null
    @JvmField
    var windowBgMode: Byte = 0
    @JvmField
    var blobMime: String? = null
    @JvmField
    var quickSubMenu: MyPopupMenu? = null
    @JvmField
    var blockImageDialog: DialogBlockImage? = null
    @JvmField
    var windowBgKey: Int = 0
    @JvmField
    var savedZtwoF: Int = 0
    @JvmField
    var pathsResetDialog: DialogConfirm? = null
    @JvmField
    var setUseTtsDialog: DialogSetUseTts? = null
    @JvmField
    var postWindowBackgroundUpdateBusy: Boolean = false
    @JvmField
    var instaIdLoaded: Boolean = false
    @JvmField
    var vidDetKind: Int = 0
    @JvmField
    var captureFreeMemory: Long = 0
    @JvmField
    var appContext: Context? = null
    @JvmField
    var bookEditDialog: DialogWebBookEdit? = null
    @JvmField
    var applySettingsChangeBusy: Boolean = false
    @JvmField
    var vidDe2Url: String? = null
    @JvmField
    var requestsCamera: Boolean = false
    @JvmField
    var savedMainC: String? = null
    @JvmField
    var vidDe2Poster: String? = null
    @JvmField
    var requestsMic: Boolean = false
    @JvmField
    var savedMainD: String? = null
    /** The bars' button groups when Settings opened (a change rebuilds both bars). */
    @JvmField
    var savedGroups: String? = null
    @JvmField
    var areaToolbar: WebAreaView? = null
    @JvmField
    var vidDe3Text: String? = null
    @JvmField
    var mainMenuItemDismissUrl: String? = null
    @JvmField
    var requestsProtectedMedia: Boolean = false
    @JvmField
    var tabUndoSnackbar: MySnackbar? = null
    @JvmField
    var actionMode: ActionMode? = null
    @JvmField
    var vidDe3Kind: Int = 0
    @JvmField
    var bookmarkEditBusy: Boolean = false
    @JvmField
    var downPageResultText: String? = null
    @JvmField
    var requestsLocation: Boolean = false
    @JvmField
    var topBehavior: MyBehaviorTop? = null
    @JvmField
    var swipePage: WebNestView? = null
    @JvmField
    var viewReadDialog: DialogViewRead? = null
    @JvmField
    var hasNewIntent: Boolean = false
    @JvmField
    var hamTagClass: String? = null
    @JvmField
    var uriItem: UriItem? = null
    @JvmField
    var grantCamera: Boolean = false
    @JvmField
    var guideNotiDialog: DialogGuideNoti? = null
    @JvmField
    var areaReinjectScriptText: String? = null
    @JvmField
    var printAdapter: PrintDocumentAdapter? = null
    @JvmField
    var grantMic: Boolean = false
    @JvmField
    var pageProgress: Int = 0
    @JvmField
    var grantLocation: Boolean = false
    @JvmField
    var areaToolbarListenerText: String? = null
    @JvmField
    var viewSrcDialog: DialogViewSrc? = null
    @JvmField
    var applyTabLimitBusy: Boolean = false
    @JvmField
    var pstMatch: String? = null
    @JvmField
    var offerCamera: Boolean = false
    @JvmField
    var themeColor: Int = 0
    @JvmField
    var savedFloatj: Int = 0
    @JvmField
    var guideAreaDialog: DialogGuideArea? = null
    @JvmField
    var setPrivacyDialog: DialogSetPrivacy? = null
    @JvmField
    var pstPoster: String? = null
    @JvmField
    var offerMic: Boolean = false
    @JvmField
    var langCheckTries: Int = 0
    @JvmField
    var downBlobDialog: DialogDownBlob? = null
    @JvmField
    var pstPoster2: String? = null
    @JvmField
    var offerMidi: Boolean = false
    @JvmField
    var langCheckBusy: Boolean = false
    @JvmField
    var popupBlockSnackbar: MySnackbar? = null
    @JvmField
    var setCookieDialog: DialogSetCookie? = null
    @JvmField
    var phPstPoster: String? = null
    @JvmField
    var videoCheckStarted: Boolean = false
    @JvmField
    var offerLocation: Boolean = false
    @JvmField
    var appBlockSnackbar: MySnackbar? = null
    @JvmField
    var savedTabClose: Boolean = false
    @JvmField
    var savedFloats: Int = 0
    @JvmField
    var setScrFilDialog: DialogSetScrFil? = null
    @JvmField
    var phPstPoster2: String? = null
    @JvmField
    var lastVideoCheckPage: String? = null
    @JvmField
    var permissionAllow: Int = 0
    @JvmField
    var pageInTransLang: Boolean = false
    @JvmField
    var certDialog: DialogWebCerti? = null
    @JvmField
    var phPstMatch: String? = null
    @JvmField
    var restartGuideDialog: DialogConfirm? = null
    @JvmField
    var printJob: PrintJob? = null
    @JvmField
    var comicMode: Int = 0
    @JvmField
    var quickItemIsLink: Boolean = false
    @JvmField
    var restartNoticeDialog: DialogSetMsg? = null
    @JvmField
    var showStatusBar: Boolean = false
    @JvmField
    var showDeleteTabsDialogBusy: Boolean = false
    @JvmField
    var loadHtml: String? = null
    @JvmField
    var deskModeBefore: Boolean = false
    @JvmField
    var fullscreenButtonMode: Byte = 0
    @JvmField
    var createBusy: Boolean = false
    @JvmField
    var switchSecretModeBusy: Boolean = false
    @JvmField
    var vidHasVideo: Boolean = false
    @JvmField
    var snsFixUrl: String? = null
    @JvmField
    var useCurrentTab: Boolean = false
    @JvmField
    var loadDialogKind: Byte = 0
    @JvmField
    var vidPaused: Boolean = false
    @JvmField
    var videoQualitiesFound: Boolean = false
    @JvmField
    var previewImageUrl: String? = null
    @JvmField
    var openInOtherAppPostText: String? = null
    @JvmField
    var barsTinted: Boolean = false
    @JvmField
    var vidDuration: String? = null
    @JvmField
    var chromeClientBusy: Boolean = false
    @JvmField
    var previewReferer: String? = null
    @JvmField
    var pendingLinkUrl: String? = null
    @JvmField
    var permissionPageUrl: String? = null
    @JvmField
    var savedMainA: String? = null
    @JvmField
    var pageUrl: String? = null
    @JvmField
    var miscBusy: Boolean = false
    @JvmField
    var touchBusy: Boolean = false
    @JvmField
    var barUpdateBusy: Boolean = false
    @JvmField
    var lastHamTag: String? = null
    @JvmField
    var previewFromLink: Boolean = false
    @JvmField
    var showPermissionDialogText: String? = null
    @JvmField
    var pageHost: String? = null
    @JvmField
    var leavingByIntent: Boolean = false
    @JvmField
    var scriptInstallUrl: String? = null
    @JvmField
    var pageTitle: String? = null
    @JvmField
    var savedZoneGestureOff: Boolean = false
    @JvmField
    var savedNewsPick: String? = null
    @JvmField
    var passwordCheckDone: Boolean = false
    @JvmField
    var lastYouSpeedSave: Boolean = false
    @JvmField
    var snsFixBusy: Boolean = false
    @JvmField
    var blobPageUrl: String? = null
    @JvmField
    var backPlayOn: Boolean = false
    @JvmField
    var keyboardRelayout: Boolean = false
    @JvmField
    var setSystemBarsHiddenBusy: Boolean = false
    @JvmField
    var vkvListListenerList: ArrayList<com.mycompany.app.main.MainDownAdapter.DownListItem?>? = null
}

/** The browser activity's page state (moved out of `WebViewActivity`). */
class WebPageState {
    @JvmField
    var frameShownHandled: Boolean = false
    @JvmField
    var pageBottomMargin: Int = 0
    @JvmField
    var pageFollowsSet: Boolean = false
    @JvmField
    var pageViewListenerText: String? = null
    @JvmField
    var pullMode: Byte = 0
    @JvmField
    var barOffset: Int = 0
    @JvmField
    var imageOpenUrl: String? = null
    @JvmField
    var imageOpenReferer: String? = null
    @JvmField
    var jsBridgePage: WebNestView? = null
    @JvmField
    var restartBrowserBusy: Boolean = false
    @JvmField
    var textZoomPending: Boolean = false
    @JvmField
    var textZoomNudged: Boolean = false
    @JvmField
    var themeOnStart: Boolean = false
    @JvmField
    var pinchActive: Boolean = false
    @JvmField
    var popupTitle: String? = null
    @JvmField
    var jsBridgeBusy: Boolean = false
    @JvmField
    var urlChanged: Boolean = false
    @JvmField
    var videoDownListenerText: String? = null
    @JvmField
    var hostChanged: Boolean = false
    @JvmField
    var externalDownload: Boolean = false
    @JvmField
    var startupBusy: Boolean = false
    @JvmField
    var videoHostEntered: Boolean = false
    @JvmField
    var videoJsonResultList: ArrayList<Any?>? = null
    @JvmField
    var useAppKeypad: Boolean = false
    @JvmField
    var createBusy: Boolean = false
    @JvmField
    var phpDownload: Boolean = false
    @JvmField
    var fromMenu: Boolean = false
    @JvmField
    var addressPathSource: String? = null
    @JvmField
    var addressPathCached: String? = null
    @JvmField
    var pushBars: Runnable? = null
    @JvmField
    var finishImplBusy: Boolean = false
    @JvmField
    var valueCallback: ValueCallback<*>? = null
    @JvmField
    var fileChooserParams: FileChooserParams? = null
    @JvmField
    var addressEditBusy: Boolean = false
    @JvmField
    var readDialogOpen: Boolean = false
    @JvmField
    var backPageClientPage: WebNestView? = null
    @JvmField
    var backFinishedPage: WebNestView? = null
    @JvmField
    var rebuildTabsBusy: Boolean = false
    @JvmField
    var backHiddenPage: WebNestView? = null
    @JvmField
    var pageViewListenerBusy: Boolean = false
    @JvmField
    var permissionBlock: Int = 0
    @JvmField
    var chromeClientBusy: Boolean = false
    @JvmField
    var permissionRequest: PermissionRequest? = null
    @JvmField
    var permissionOrigin: String? = null
    @JvmField
    var canGoBackCache: Byte = 0
    @JvmField
    var geoCallback: GeolocationPermissions.Callback? = null
    @JvmField
    var canGoForwardCache: Byte = 0
    @JvmField
    var popupPage: WebNestView? = null
    @JvmField
    var pendingPage: WebNestView? = null
    @JvmField
    var popupLoadState: Byte = 0
    @JvmField
    var runLinkActionListenerText: String? = null
    @JvmField
    var pendingPermissionAllow: Int = 0
    @JvmField
    var popupUrl: String? = null
    @JvmField
    var permissionHost: String? = null
    /** The open permission request was answered with "Allow this time": granted but not saved (WvaSavePermissionTask). */
    var permissionOnce: Boolean = false
    @JvmField
    var stateItem: StateItem? = null
    @JvmField
    var popupHost: String? = null
    @JvmField
    var pageFrameListenerBusy: Boolean = false
    @JvmField
    var sitePermissionAllow: Int = 0
    @JvmField
    var popupPending: Boolean = false
    @JvmField
    var pageTopMargin: Int = 0
    @JvmField
    var permissionKind: Int = 0
}

/** The browser activity's misc state (moved out of `WebViewActivity`). */
class WebMiscState {
    @JvmField
    var openTextViewerBusy: Boolean = false
    @JvmField
    var eventReceiver: WvaEventReceiver? = null
    @JvmField
    var areaMenuX: Int = 0
    @JvmField
    var transMsgPending: Boolean = false
    @JvmField
    var handEnterAnim: ValueAnimator? = null
    @JvmField
    var pendingDownloadUrl: String? = null
    @JvmField
    var areaMenuY: Int = 0
    @JvmField
    var handExitAnim: ValueAnimator? = null
    @JvmField
    var tabLimitListener: TabLimitListener? = null
    @JvmField
    var pendingDownloadDisposition: String? = null
    @JvmField
    var rateScriptUrl: String? = null
    @JvmField
    var filterService: MainFilterSvc? = null
    @JvmField
    var pendingDownloadMime: String? = null
    @JvmField
    var scriptService: MainScriptSvc? = null
    @JvmField
    var pendingDownloadLength: Long = 0
    @JvmField
    var scriptCacheTime: Long = 0
    @JvmField
    var pendingDownloadUseCurrent: Boolean = false
    @JvmField
    var scriptCacheUrl: String? = null
    @JvmField
    var overrideUrl: String? = null
    @JvmField
    var pinchStartX: FloatArray? = null
    @JvmField
    var scriptCache: ScriptCache? = null
    @JvmField
    var prevPageListener: PrevPageListener? = null
    @JvmField
    var pinchStartY: FloatArray? = null
    /** A two-finger swipe with an action of its own: run when the fingers lift (0: none). */
    @JvmField
    var pinchAction: Int = 0
    @JvmField
    var requestSetDownDialogBusy: Boolean = false
    @JvmField
    var touchBusy: Boolean = false
    @JvmField
    var hintsDismissed: Boolean = false
    @JvmField
    var popupResultUrl: String? = null
    @JvmField
    var contentTop: Int = 0
    @JvmField
    var previewDialogListenerBusy: Boolean = false
    @JvmField
    var windowBgLandscape: Boolean = false
    @JvmField
    var startPageSetupDone: Boolean = false
    @JvmField
    var touchDownEdge: Int = 0
    @JvmField
    var windowBgBitmap: Bitmap? = null
    @JvmField
    var edgeGestureStarted: Boolean = false
    @JvmField
    var restartAppBusy: Boolean = false
    @JvmField
    var edgeTouchSide: Int = 0
    @JvmField
    var fromEdgeGesture: Boolean = false
    @JvmField
    /** No longer set: the green gesture card became a tip ([tipView], 2026-11-20). */
    var gestureGuideView: android.view.View? = null
    /** The tip on screen (ui/tips), if any. */
    var tipView: com.mycompany.app.ui.browser.TipOverlay? = null
    @JvmField
    var openIntentUrlBusy: Boolean = false
    @JvmField
    var lastScrollY: Float = 0f
    @JvmField
    var popupTabUrl: String? = null
    @JvmField
    var startPageSetupBusy: Boolean = false
    @JvmField
    var lastMoveX: Float = 0f
    @JvmField
    var lastMoveY: Float = 0f
    @JvmField
    var lastLandscape: Boolean = false
    @JvmField
    var createBusy: Boolean = false
    @JvmField
    var handEnterOffset: Int = 0
    @JvmField
    /** No longer set: the first-start intro overlay became the setup wizard (OnboardingActivity, 2026-11-20). */
    var introView: android.view.View? = null
    @JvmField
    var handEnterAnimBusy: Boolean = false
    @JvmField
    var tabAddListener: TabAddListener? = null
    @JvmField
    var handModeOffset: Int = 0
    @JvmField
    var handExitAnimBusy: Boolean = false
    @JvmField
    var webClean: WebClean? = null
    @JvmField
    var themeLight: Int = 0
    @JvmField
    var webStyleListenerBusy: Boolean = false
    @JvmField
    var backSwipeState: Byte = 0
    @JvmField
    var forwardSwipeState: Byte = 0
    @JvmField
    var barsSettingApplyBusy: Boolean = false
    @JvmField
    var windowHeightWatched: Boolean = false
    @JvmField
    var requestImagePreviewBusy: Boolean = false
    @JvmField
    var hmgLoad: WebHmgLoad? = null
    @JvmField
    var dataCachesDirty: Boolean = false
    @JvmField
    var videoRateInjectBusy: Boolean = false
    @JvmField
    var glide: GlideRequests? = null
    @JvmField
    var runDownActionBusy: Boolean = false
    @JvmField
    var moveHintX: Int = 0
    @JvmField
    var moveHintY: Int = 0
}
