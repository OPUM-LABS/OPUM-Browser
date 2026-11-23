@file:Suppress("unused")

package com.mycompany.app.web

import android.view.GestureDetector
import com.mycompany.app.pref.store.TtsSettings
import com.mycompany.app.pref.store.ReadSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.PdfSettings
import android.view.MotionEvent
import com.mycompany.app.main.MainApp
import com.mycompany.app.pref.PrefWeb
import com.mycompany.app.pref.PrefZone
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pull.MyPullView
import com.mycompany.app.view.MyBarView
import com.mycompany.app.ui.browser.IconButton
import com.mycompany.app.view.MyGesNoti
import com.mycompany.app.view.MyScrollBar
import com.mycompany.app.ui.browser.ScrollNavi
import com.mycompany.app.view.MyWebBody
import com.mycompany.app.view.MyWebCoord
import kotlin.math.abs
import android.text.TextUtils
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainConst
import com.mycompany.app.pref.PrefZtwo
import com.mycompany.app.pref.PrefTts
import com.mycompany.app.pref.PrefRead

/**
 * The browser screen's touch handling, moved out of `WebViewActivity.dispatchTouchEvent`: the bar and page-zone swipes,
 * tab switching by swipe, the edge gestures and two-finger gestures. Each handler below covers one gesture; inside a
 * handler the steps are `when` branches.
 */
class WebTouchController(@JvmField val wa: WebViewActivity) {
    /** The event being handled and what the handlers share about it. */
    private var ev: MotionEvent? = null
    private var action = 0
    private var touchX = 0f
    private var touchY = 0f
    /** Result of the default dispatch (`super.dispatchTouchEvent`), returned by most paths. */
    private var handled = false
    private var superDispatch: (MotionEvent?) -> Boolean = { false }
    // temporaries the handlers hand to each other
    private var middleTabSwipeOnStart: Boolean = false
    private var middleSwipeOnStart: Boolean = false
    private var swipeSlop: Int = 0
    private var dyFromLast: Float = 0f
    private var halfSlop: Int = 0
    private var dxFromLast: Float = 0f
    private var directionSlop: Float = 0f
    private var fullSlop: Int = 0
    private var landscape: Boolean = false

    /** True while a touch is being handled; a nested call (none known) gets its own instance, like the original's locals. */
    private var busy = false

    fun dispatch(event: MotionEvent?, superDispatch: (MotionEvent?) -> Boolean): Boolean {
        if (busy) return WebTouchController(wa).dispatch(event, superDispatch)
        busy = true
        try {
            this.ev = event
            this.superDispatch = superDispatch
            return dispatch()
        } finally {
            busy = false
        }
    }

    /** Entry: early exits, the gesture-blocked mode, per-touch resets, overlays that take all touches, the gesture detector, then the default dispatch and the handler for the action. */
    private fun dispatch(): Boolean {
        fun endTouch(): Boolean {
            wa.postBarUpdate()
            wa.pageState.pinchActive = false
            wa.miscState.pinchStartX = null
            wa.miscState.pinchStartY = null
            wa.miscState.touchBusy = false
            wa.miscState.hintsDismissed = false
            wa.resetGestureState(false, false)
            return handled
        }
        fun dispatchByAction(): Boolean {
            handled = superDispatch(ev)
            if ((action).toLong() == 0L) { return onDown()!! }
            if ((action).toLong() == 1L) { return endTouch() }
            if ((action).toLong() == 2L) { return onMove()!! }
            if ((action).toLong() == 3L) { return endTouch() }
            if ((action).toLong() == 5L) { return onSecondFingerDown() }
            return handled
        }
        fun routeToOverlays(): Boolean {
            if (wa.videoFrame == null) {
                if (wa.dialogsState.menuListDialog == null) {
                    if (wa.quickState.quickSubView == null) {
                        if (wa.tabBarSubView == null) {
                            if (wa.transControl == null) {
                                if (wa.miscState.gestureGuideView == null) {
                                    if (wa.miscState.hintsDismissed == true) {
                                        return superDispatch(ev)
                                    }
                                    wa.tabsState.gestureDetector?.onTouchEvent(ev!!)
                                    return dispatchByAction()
                                }
                                return superDispatch(ev)
                            }
                            return superDispatch(ev)
                        }
                        return superDispatch(ev)
                    }
                    return superDispatch(ev)
                }
                return superDispatch(ev)
            }
            return superDispatch(ev)
        }
        fun noteEdgeSwipe(): Boolean {
            wa.miscState.edgeTouchSide = wa.edgeSwipeDirection(touchX, touchY)
            return routeToOverlays()
        }
        fun startEdgeTracking(): Boolean {
            wa.resetBarTouch()
            val edgeAtDown: Int = wa.edgeAt(touchX)
            wa.miscState.touchDownEdge = edgeAtDown
            wa.miscState.edgeTouchSide = 0
            wa.floatingState.edgeTouchActive = false
            if ((edgeAtDown).toLong() == 0L) { return routeToOverlays() }
            wa.barsState.touchStartX = ev!!.getX()
            wa.barsState.touchStartY = ev!!.getY()
            return routeToOverlays()
        }
        middleTabSwipeOnStart = false
        middleSwipeOnStart = false
        if (wa.tabsState.restartBusy == false) {
            if (wa.isClosing == true) {
                wa.resetEdgeTouch()
                return false
            }
            if (ev != null) {
                action = ev!!.getActionMasked()
                if (wa.isBarUpdatePending() == false) {
                    wa.videoState.blockedTouchDownTime = 0
                    touchX = ev!!.getX()
                    touchY = ev!!.getY()
                    if ((action).toLong() != 0L) {
                        if ((action).toLong() == 1L) { return noteEdgeSwipe() }
                        if ((action).toLong() != 3L) { return routeToOverlays() }
                        return noteEdgeSwipe()
                    }
                    wa.videoState.longPressLinkUrl = null
                    wa.videoState.longPressSrcUrl = null
                    wa.videoState.longPressTitle = null
                    wa.videoState.webViewActivityBusy = false
                    wa.quickState.longPressImage = null
                    wa.videoState.longPressInspectBusy = false
                    wa.quickState.longPressInspectBusy = false
                    wa.dialogsState.longParseBusy = false
                    wa.videoState.longPressPending = false
                    wa.dialogsState.longPressBusy = false
                    if (wa.miscState.hintsDismissed == false) { return startEdgeTracking() }
                    wa.miscState.hintsDismissed = false
                    wa.resetHintTouch()
                    return startEdgeTracking()
                }
                wa.resetEdgeTouch()
                wa.pageState.pinchActive = false
                wa.miscState.pinchStartX = null
                wa.miscState.pinchStartY = null
                wa.miscState.touchBusy = false
                wa.miscState.hintsDismissed = false
                wa.resetGestureState(true, false)
                if ((action).toLong() != 0L) { return finishBlockedTouch()!! }
                wa.miscState.backSwipeState = 0
                wa.miscState.forwardSwipeState = 0
                wa.pageState.canGoBackCache = 0
                wa.pageState.canGoForwardCache = 0
                if ((wa.videoState.blockedTouchDownTime) != 0L) {
                    return true
                }
                wa.videoState.blockedTouchDownTime = System.currentTimeMillis()
                return true
            }
            wa.resetEdgeTouch()
            wa.resetBusyFlags()
            return false
        }
        wa.resetEdgeTouch()
        return false
    }

    /** Finger up / cancel while page gestures are blocked (`isBarUpdatePending()`): end the scroll-navigation arrows, gesture hint, scrollbar and pull disc. */
    private fun finishBlockedTouch(): Boolean? {
        fun finishPull(): Boolean? {
            if (ZoneSettings.pullRefresh.value == false) {
                return superDispatch(ev)
            }
            wa.pullView?.cancelTouch()
            return superDispatch(ev)
        }
        fun restartScrollBarTimer(): Boolean? {
            if ((ZoneSettings.scrollPos.value).toLong() == 0L) { return finishPull() }
            wa.scrollBar?.restartTimer()
            return finishPull()
        }
        fun releaseSideButton(): Boolean? {
            val sideButton: IconButton? = wa.sideButton
            if (sideButton == null) { return restartScrollBarTimer() }
            if (sideButton.isPressed() == false) { return restartScrollBarTimer() }
            sideButton.cancelPress()
            return restartScrollBarTimer()
        }
        fun fadeGestureHint(): Boolean? {
            wa.floatingState.gestureHint?.fadeOutNow()
            return releaseSideButton()
        }
        fun hideRightScrollNavi(): Boolean? {
            wa.floatingState.rightScrollNavi?.hide()
            return fadeGestureHint()
        }
        fun hideScrollNavis(): Boolean? {
            if (wa.isZoneGestureOff() == false) { return fadeGestureHint() }
            wa.leftScrollNavi?.hide()
            return hideRightScrollNavi()
        }
        fun resetIfBlockedLong(): Boolean? {
            if ((System.currentTimeMillis() - wa.videoState.blockedTouchDownTime) <= 500) { return hideScrollNavis() }
            wa.resetBusyFlags()
            return hideScrollNavis()
        }
        if ((action).toLong() == 1L) { return resetIfBlockedLong() }
        if ((action).toLong() != 3L) { return true }
        return resetIfBlockedLong()
    }

    /** A second finger touches down: start the two-finger tab gesture (pinch) when the settings and the tab list allow it. */
    private fun onSecondFingerDown(): Boolean {
        if (wa.isZoneGestureOff() && !wa.isSwipeMoving()) wa.resetScrollNavis(false, false)
        if (ZoneSettings.pullRefresh.value) wa.pullView?.cancelTouch()
        if (!BrowserSettings.tabMulti.value) return handled
        if ((wa.tabsState.gestureDirection).toLong() != 0L) return handled
        if (wa.quickState.editingAddress) return handled
        val page: WebNestView = wa.currentPage ?: return handled
        if (page.quickPanelOpen()) return handled
        if (wa.videoState.quickSchEdit != null) return handled
        val tabs: ArrayList<WebTabItem?> = wa.tabsState.tabItems ?: return handled
        if (tabs.isEmpty()) return handled
        val event: MotionEvent = ev!!
        if (event.getPointerCount() <= 1) return handled
        wa.pageState.pinchActive = true
        val startX = FloatArray(2)
        val startY = FloatArray(2)
        wa.miscState.pinchStartX = startX
        wa.miscState.pinchStartY = startY
        for (pointer in 0 until 2) {
            startX[pointer] = event.getX(pointer)
            startY[pointer] = event.getY(pointer)
        }
        return handled
    }

    /** Finger move: two-finger tab gesture, or the single-finger gestures. */
    private fun onMove(): Boolean? {
        var pinchDirection: Byte = 0
        var pinchStartX: FloatArray? = null
        fun trackPinch(): Boolean? {
            wa.tabsState.gestureDirection = pinchDirection
            pinchStartX = wa.miscState.pinchStartX
            if (pinchStartX == null) {
                if ((wa.tabsState.gestureDirection).toLong() == 0L) { return handled }
                return movePinch()
            }
            pinchStartX!![0] = ev!!.getX(0)
            wa.miscState.pinchStartX!![1] = ev!!.getX(1)
            if ((wa.tabsState.gestureDirection).toLong() == 0L) { return handled }
            return movePinch()
        }
        val pinchActive: Boolean = wa.pageState.pinchActive
        swipeSlop = WebViewActivity.Xn
        if (pinchActive == false) { return onSingleFingerMove() }
        if ((wa.tabsState.gestureDirection).toLong() != 0L) { return movePinch() }
        val pinchDx: Float = wa.twoFingerDx(ev!!)
        val pinchDy: Float = wa.twoFingerDy(ev!!)
        val pinchDxAbs: Float = abs(pinchDx)
        val pinchDyAbs: Float = abs(pinchDy)
        if (pinchDxAbs <= pinchDyAbs) {
            if (pinchDy <= 0.0f) {
                if ((wa.tabsState.gestureDirection).toLong() == 0L) { return handled }
                return movePinch()
            }
            if (pinchDyAbs <= (swipeSlop shl 1)) {
                if ((wa.tabsState.gestureDirection).toLong() == 0L) { return handled }
                return movePinch()
            }
            wa.floatingState.gestureMode = 1
            wa.floatingState.gestureZone = 5
            wa.tabsState.gestureDirection = 3
            val pinchStartY: FloatArray? = wa.miscState.pinchStartY
            if (pinchStartY == null) {
                if ((wa.tabsState.gestureDirection).toLong() == 0L) { return handled }
                return movePinch()
            }
            pinchStartY[0] = ev!!.getY(0)
            wa.miscState.pinchStartY!![1] = ev!!.getY(1)
            if ((wa.tabsState.gestureDirection).toLong() == 0L) { return handled }
            return movePinch()
        }
        val tabs: ArrayList<WebTabItem?>? = wa.tabsState.tabItems
        if (tabs != null) {
            if (tabs.size < 2) {
                wa.pageState.pinchActive = false
                return handled
            }
            if (pinchDxAbs <= (swipeSlop shl 1)) {
                if ((wa.tabsState.gestureDirection).toLong() == 0L) { return handled }
                return movePinch()
            }
            wa.pageState.pinchActive = false
            wa.floatingState.gestureMode = 1
            wa.floatingState.gestureZone = 5
            if (pinchDx <= 0.0f) {
                pinchDirection = 2
                return trackPinch()
            }
            pinchDirection = 1
            return trackPinch()
        }
        wa.pageState.pinchActive = false
        return handled
    }

    /** Two-finger gesture under way (`da` 1 / 2 horizontal, 3 vertical): move the tabs with the fingers. */
    private fun movePinch(): Boolean? {
        val webCoord: MyWebCoord? = wa.webCoord
        if (webCoord == null) { return handled }
        val page: WebNestView? = wa.currentPage
        if (page == null) { return handled }
        page.swipeTouch = true
        webCoord.cancelPull()
        val direction: Byte = wa.tabsState.gestureDirection
        if ((direction).toLong() != 1L) {
            if ((direction).toLong() != 2L) {
                if ((direction).toLong() != 3L) {
                    wa.dialogsState.touchBusy = true
                    return handled
                }
                var pinchY: Float = wa.twoFingerDy(ev!!)
                if (wa.swipeTabFrame != null) {
                    if ((pinchY.compareTo(0.0f)).toLong() != 0L) {
                        wa.revealPrevTab(pinchY, false)
                        wa.dialogsState.touchBusy = true
                        return handled
                    }
                    pinchY = wa.swipeTabFrame!!.tabY
                    wa.revealPrevTab(pinchY, false)
                    wa.dialogsState.touchBusy = true
                    return handled
                }
                wa.revealPrevTab(pinchY, false)
                wa.dialogsState.touchBusy = true
                return handled
            }
            wa.dragNextTab(wa.twoFingerDx(ev!!) + wa.pageAreaWidth(), false)
            wa.dialogsState.touchBusy = true
            return handled
        }
        wa.dragPrevTab(wa.twoFingerDx(ev!!), 4)
        wa.dialogsState.touchBusy = true
        return handled
    }

    /** Single-finger move: hide floating buttons once the finger travels, track the direction for edge gestures, auto-hide on vertical scroll, then the zone gesture. */
    private fun onSingleFingerMove(): Boolean? {
        fun checkScrollMove(): Boolean? {
            val lastScrollY: Float = wa.miscState.lastScrollY
            val dyFromLastScroll: Float = touchY - lastScrollY
            val scrollSlop: Float = (MainApp.dp10).toFloat()
            if (dyFromLastScroll <= scrollSlop) {
                if ((lastScrollY - touchY) <= scrollSlop) { return continueZoneGesture() }
                wa.miscState.lastScrollY = touchY
                if (wa.hasFloatingButtons() == false) { return continueZoneGesture() }
                val webCoord2: MyWebCoord? = wa.webCoord
                if (webCoord2 == null) { return handled }
                wa.floatingState.floatingShown = true
                val floatingOnDragUp: Runnable? = wa.touchTask2
                webCoord2.removeCallbacks(floatingOnDragUp)
                wa.webCoord!!.post(floatingOnDragUp)
                return continueZoneGesture()
            }
            wa.miscState.lastScrollY = touchY
            if (wa.hasFloatingButtons() == false) { return continueZoneGesture() }
            val webCoord: MyWebCoord? = wa.webCoord
            if (webCoord == null) { return handled }
            wa.floatingState.floatingShown = true
            val floatingOnDragDown: Runnable? = wa.touchTask
            webCoord.removeCallbacks(floatingOnDragDown)
            wa.webCoord!!.post(floatingOnDragDown)
            return continueZoneGesture()
        }
        fun trackMoveDirection(): Boolean? {
            if ((wa.tabsState.gestureDirection).toLong() != 0L) { return checkScrollMove() }
            dxFromLast = abs(touchX - wa.miscState.lastMoveX)
            dyFromLast = abs(touchY - wa.miscState.lastMoveY)
            directionSlop = (MainApp.dp80).toFloat()
            if (dxFromLast > directionSlop) { trackEdgeSwipeDirection()?.let { return it }; return checkScrollMove() }
            if (dyFromLast <= directionSlop) { return checkScrollMove() }
            trackEdgeSwipeDirection()?.let { return it }; return checkScrollMove()
        }
        fun closeMenusAndTrack(): Boolean? {
            wa.closeFloatingPopupMenu()
            wa.closeTabBarItemMenu()
            return trackMoveDirection()
        }
        fun checkMovedFromDown(): Boolean? {
            if (abs(touchX - wa.tabsState.touchDownX) <= MainApp.dp10) {
                if (abs(touchY - wa.findState.touchDownY) > MainApp.dp10) { return closeMenusAndTrack() }
                return trackMoveDirection()
            }
            return closeMenusAndTrack()
        }
        if (wa.tabsState.tabDragMoved == false) { return trackMoveDirection() }
        if (wa.floatingState.popupMenu != null) { return checkMovedFromDown() }
        if (wa.tabBarItemMenu == null) { return trackMoveDirection() }
        return checkMovedFromDown()
    }

    /** Direction of the current drag (`ga`: 1 up, 2 down, 3 left, 4 right) and the edge-gesture hint (`ha`) when the direction turns. Continues with the rest of the move. */
    private fun trackEdgeSwipeDirection(): Boolean? {
        fun resetEdgeDirection(): Boolean? {
            wa.barsState.edgeDirection = 0
            wa.floatingState.edgeHint = 0
            wa.floatingState.gestureHint?.fadeOutNow()
            wa.miscState.lastMoveX = touchX
            wa.miscState.lastMoveY = touchY
            return null
        }
        fun edgeMovedRight(): Boolean? {
            wa.barsState.edgeDirection = 4
            wa.miscState.lastMoveX = touchX
            wa.miscState.lastMoveY = touchY
            return null
        }
        fun edgeMovedLeft(): Boolean? {
            wa.barsState.edgeDirection = 3
            wa.miscState.lastMoveX = touchX
            wa.miscState.lastMoveY = touchY
            return null
        }
        fun trackHorizontalEdge(): Boolean? {
            if (dxFromLast > directionSlop) {
                if (dyFromLast >= directionSlop) { return resetEdgeDirection() }
                val lastX: Float = wa.miscState.lastMoveX
                val lastDirection: Byte = wa.barsState.edgeDirection
                if (touchX >= lastX) {
                    if ((lastDirection).toLong() == 1L) {
                        if ((ZoneSettings.gesUpRight.value).toLong() == 0L) { return edgeMovedRight() }
                        wa.floatingState.edgeHint = 2
                        wa.floatingState.gestureMode = 2
                        wa.floatingState.gestureZone = 0
                        wa.floatingState.gestureHint?.showDirection(2)
                        return edgeMovedRight()
                    }
                    if ((lastDirection).toLong() == 2L) {
                        if ((ZoneSettings.gesDownRight.value).toLong() == 0L) { return edgeMovedRight() }
                        wa.floatingState.edgeHint = 4
                        wa.floatingState.gestureMode = 2
                        wa.floatingState.gestureZone = 0
                        wa.floatingState.gestureHint?.showDirection(4)
                        return edgeMovedRight()
                    }
                    if ((lastDirection).toLong() != 3L) { return edgeMovedRight() }
                    wa.floatingState.edgeHint = 0
                    wa.floatingState.gestureHint?.fadeOutNow()
                    return edgeMovedRight()
                }
                if ((lastDirection).toLong() == 1L) {
                    if ((ZoneSettings.gesUpLeft.value).toLong() == 0L) { return edgeMovedLeft() }
                    wa.floatingState.edgeHint = 1
                    wa.floatingState.gestureMode = 2
                    wa.floatingState.gestureZone = 0
                    wa.floatingState.gestureHint?.showDirection(1)
                    return edgeMovedLeft()
                }
                if ((lastDirection).toLong() == 2L) {
                    if ((ZoneSettings.gesDownLeft.value).toLong() == 0L) { return edgeMovedLeft() }
                    wa.floatingState.edgeHint = 3
                    wa.floatingState.gestureMode = 2
                    wa.floatingState.gestureZone = 0
                    wa.floatingState.gestureHint?.showDirection(3)
                    return edgeMovedLeft()
                }
                if ((lastDirection).toLong() != 4L) { return edgeMovedLeft() }
                wa.floatingState.edgeHint = 0
                wa.floatingState.gestureHint?.fadeOutNow()
                return edgeMovedLeft()
            }
            return resetEdgeDirection()
        }
        if (dxFromLast < directionSlop) {
            if (dyFromLast <= directionSlop) { return trackHorizontalEdge() }
            if (touchY >= wa.miscState.lastMoveY) {
                wa.barsState.edgeDirection = 2
                wa.floatingState.edgeHint = 0
                wa.floatingState.gestureMode = 2
                wa.floatingState.gestureZone = 0
                wa.floatingState.gestureHint?.fadeOutNow()
                wa.miscState.lastMoveX = touchX
                wa.miscState.lastMoveY = touchY
                return null
            }
            wa.barsState.edgeDirection = 1
            wa.floatingState.edgeHint = 0
            wa.floatingState.gestureMode = 2
            wa.floatingState.gestureZone = 0
            wa.floatingState.gestureHint?.fadeOutNow()
            wa.miscState.lastMoveX = touchX
            wa.miscState.lastMoveY = touchY
            return null
        }
        return trackHorizontalEdge()
    }

    /** The zone gesture started at touch-down (`ca`): bar swipes (1 top, 2 bottom) or page zone swipes (3 / 4 / 5). */
    private fun continueZoneGesture(): Boolean? {
        var dyFromDown: Float = 0f
        var zone: Byte = 0
        var dxFromDown: Float = 0f
        var swipeDirection: Byte = 0
        fun decideSwipeDirection(): Boolean? {
            if (dxFromDown <= dyFromDown) { return moveBarSwipe() }
            if (dxFromDown <= swipeSlop) { return moveBarSwipe() }
            if (touchX <= wa.tabsState.touchDownX) {
                swipeDirection = 2
                wa.tabsState.gestureDirection = swipeDirection
                wa.tabsState.touchDownX = touchX
                return moveBarSwipe()
            }
            swipeDirection = 1
            wa.tabsState.gestureDirection = swipeDirection
            wa.tabsState.touchDownX = touchX
            return moveBarSwipe()
        }
        zone = wa.floatingState.gestureZone
        if ((zone).toLong() == 0L) { return handled }
        halfSlop = WebViewActivity.Yn
        fullSlop = WebViewActivity.Wn
        if ((zone).toLong() == 3L) { return movePageZoneSwipe() }
        if ((zone).toLong() == 4L) { return movePageZoneSwipe() }
        if ((zone).toLong() == 5L) { return movePageZoneSwipe() }
        if ((wa.tabsState.gestureDirection).toLong() != 0L) { return moveBarSwipe() }
        dxFromDown = abs(touchX - wa.tabsState.touchDownX)
        dyFromDown = abs(touchY - wa.findState.touchDownY)
        if (wa.floatingState.edgeTouchActive == false) {
            if ((wa.floatingState.gestureMode).toLong() != 0L) { return decideSwipeDirection() }
            swipeSlop = fullSlop
            return decideSwipeDirection()
        }
        swipeSlop = halfSlop
        return decideSwipeDirection()
    }

    /** Swipe that started on a bar: pick a direction or cancel when the finger leaves the bar; then switch tabs with the swipe. */
    private fun moveBarSwipe(): Boolean? {
        var topInset: Int = 0
        var topBarZoneHeight: Int = 0
        var topBar: MyBarView? = null
        var xInt: Int = 0
        var yInt: Int = 0
        var bottomZoneHeight: Int = 0
        var bottomBarHeight: Int = 0
        var bottomZone: Int = 0
        var bottomEdge: Int = 0
        var bottomBar: MyBarView? = null
        var landscape: Boolean = false
        var topBarInset: Int = 0
        var onTopBarButton: Boolean = false
        var landscape3: Boolean = false
        var bottomBarInset: Int = 0
        var onBottomBarButton: Boolean = false
        fun cancelBottomBarSwipe(): Boolean? {
            onBottomBarButton = false
            if (onBottomBarButton == true) { return handled }
            wa.floatingState.gestureMode = 2
            wa.floatingState.gestureZone = 0
            return handled
        }
        fun checkBottomBarHit(): Boolean? {
            bottomBar = wa.barsState.bottomBarView
            val xInt2: Int = touchX.toInt()
            val yInt2: Int = touchY.toInt()
            if (bottomBar != null) {
                if (bottomBar!!.barTouched == false) { return cancelBottomBarSwipe() }
                onBottomBarButton = MainUtil.isInsideView(xInt2, yInt2, bottomBarInset, bottomBar)
                if (onBottomBarButton == true) { return handled }
                wa.floatingState.gestureMode = 2
                wa.floatingState.gestureZone = 0
                return handled
            }
            return cancelBottomBarSwipe()
        }
        fun checkBottomZone(): Boolean? {
            if (touchY >= (bottomEdge - bottomZone)) { return handled }
            if (landscape3 == false) {
                bottomBarInset = bottomZone - PdfSettings.botHeight.value
                return checkBottomBarHit()
            }
            bottomBarInset = MainApp.dp4
            return checkBottomBarHit()
        }
        fun findBottomEdge(): Boolean? {
            bottomZone = bottomZoneHeight + bottomBarHeight
            val body: MyWebBody? = wa.dialogsState.webBody
            if (body != null) {
                if (MainConst.isApi30 == false) {
                    bottomEdge = body.rectBottom + wa.miscState.contentTop
                    return checkBottomZone()
                }
                bottomEdge = body.rectBottom
                return checkBottomZone()
            }
            bottomEdge = 0
            return checkBottomZone()
        }
        fun cancelTopBarSwipe(): Boolean? {
            onTopBarButton = false
            if (onTopBarButton == true) { return handled }
            wa.floatingState.gestureMode = 2
            wa.floatingState.gestureZone = 0
            return handled
        }
        fun checkTopBarHit(): Boolean? {
            if (topBar != null) {
                if (topBar!!.barTouched == false) { return cancelTopBarSwipe() }
                onTopBarButton = MainUtil.isInsideView(xInt, yInt, topBarInset, topBar)
                if (onTopBarButton == true) { return handled }
                wa.floatingState.gestureMode = 2
                wa.floatingState.gestureZone = 0
                return handled
            }
            return cancelTopBarSwipe()
        }
        fun checkTopZone(): Boolean? {
            if (touchY <= (topInset + topBarZoneHeight)) { return handled }
            topBar = wa.barsState.topBarView
            xInt = touchX.toInt()
            yInt = touchY.toInt()
            if (landscape == false) {
                topBarInset = ZoneSettings.portAreaTop.value - PdfSettings.topHeight.value
                return checkTopBarHit()
            }
            topBarInset = 0
            return checkTopBarHit()
        }
        if ((wa.tabsState.gestureDirection).toLong() != 0L) {
            val webCoord: MyWebCoord? = wa.webCoord
            if (webCoord == null) { return handled }
            val page: WebNestView? = wa.currentPage
            if (page == null) { return handled }
            page.swipeTouch = true
            webCoord.cancelPull()
            val direction: Byte = wa.tabsState.gestureDirection
            val mode: Byte = wa.floatingState.gestureMode
            if ((direction).toLong() != 1L) {
                if ((mode).toLong() == 0L) {
                    if (wa.swipeForwardOpensTab() == true) {
                        if (wa.tabsState.layoutRtl == false) {
                            wa.dragNextPage((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX)
                            wa.dialogsState.touchBusy = true
                            return handled
                        }
                        wa.dragPrevPage((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX)
                        wa.dialogsState.touchBusy = true
                        return handled
                    }
                    val rightScrollNavi: ScrollNavi? = wa.floatingState.rightScrollNavi
                    if (rightScrollNavi == null) {
                        wa.dialogsState.touchBusy = true
                        return handled
                    }
                    if (rightScrollNavi.isIdle() == false) {
                        if (touchX >= wa.tabsState.touchDownX) {
                            wa.dialogsState.touchBusy = true
                            return handled
                        }
                        if (abs(touchY - wa.findState.touchDownY) >= fullSlop) {
                            wa.dialogsState.touchBusy = true
                            return handled
                        }
                        wa.floatingState.rightScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                        wa.dialogsState.touchBusy = true
                        return handled
                    }
                    wa.floatingState.rightScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                    wa.dialogsState.touchBusy = true
                    return handled
                }
                if ((mode).toLong() != 1L) {
                    wa.dialogsState.touchBusy = true
                    return handled
                }
                if (wa.tabsState.layoutRtl == false) {
                    wa.dragNextTab((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX, false)
                    wa.dialogsState.touchBusy = true
                    return handled
                }
                wa.dragPrevTab((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX, 4)
                wa.dialogsState.touchBusy = true
                return handled
            }
            if ((mode).toLong() == 0L) {
                if (wa.swipeBackOpensTab() == false) {
                    val leftScrollNavi: ScrollNavi? = wa.leftScrollNavi
                    if (leftScrollNavi == null) {
                        wa.dialogsState.touchBusy = true
                        return handled
                    }
                    if (leftScrollNavi.isIdle() == false) {
                        if (touchX <= wa.tabsState.touchDownX) {
                            wa.dialogsState.touchBusy = true
                            return handled
                        }
                        if (abs(touchY - wa.findState.touchDownY) >= fullSlop) {
                            wa.dialogsState.touchBusy = true
                            return handled
                        }
                        wa.leftScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                        wa.dialogsState.touchBusy = true
                        return handled
                    }
                    wa.leftScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                    wa.dialogsState.touchBusy = true
                    return handled
                }
                val rtl2: Boolean = wa.tabsState.layoutRtl
                val dxFromDown2: Float = touchX - wa.tabsState.touchDownX
                if (rtl2 == false) {
                    wa.dragPrevPage(dxFromDown2)
                    wa.dialogsState.touchBusy = true
                    return handled
                }
                wa.dragNextPage(dxFromDown2)
                wa.dialogsState.touchBusy = true
                return handled
            }
            if ((mode).toLong() != 1L) {
                wa.dialogsState.touchBusy = true
                return handled
            }
            val rtl: Boolean = wa.tabsState.layoutRtl
            val dxFromDown: Float = touchX - wa.tabsState.touchDownX
            if (rtl == false) {
                wa.dragPrevTab(dxFromDown, 4)
                wa.dialogsState.touchBusy = true
                return handled
            }
            wa.dragNextTab(dxFromDown, false)
            wa.dialogsState.touchBusy = true
            return handled
        }
        val zone: Byte = wa.floatingState.gestureZone
        if ((zone).toLong() != 1L) {
            if ((zone).toLong() != 2L) { return handled }
            if (wa.webCoord == null) { return handled }
            landscape3 = wa.isLandscapeScreen()
            if (landscape3 == false) {
                bottomZoneHeight = ZoneSettings.portAreaBot.value
                bottomBarHeight = MainApp.dp4
                return findBottomEdge()
            }
            bottomZoneHeight = ZoneSettings.landAreaBot.value
            bottomBarHeight = MainApp.dp4
            return findBottomEdge()
        }
        landscape = wa.isLandscapeScreen()
        topInset = wa.miscState.contentTop
        if (landscape == false) {
            topBarZoneHeight = ZoneSettings.portAreaTop.value
            return checkTopZone()
        }
        topBarZoneHeight = ZoneSettings.landAreaTop.value
        return checkTopZone()
    }

    /** Swipe that started in a page zone (left / right edge, middle): pick the direction, then back / forward or the scroll-navigation arrows. */
    private fun movePageZoneSwipe(): Boolean? {
        if ((wa.tabsState.gestureDirection).toLong() != 0L) { return applyPageZoneSwipe() }
        if (wa.webCoord == null) { return handled }
        val page: WebNestView? = wa.currentPage
        if (page == null) { return handled }
        if (page.quickPanelOpen() == false) { startPageZoneSwipe()?.let { return it }; return applyPageZoneSwipe() }
        wa.floatingState.gestureMode = 2
        wa.floatingState.gestureZone = 0
        return handled
    }

    /**
     * Page zone swipe without a direction yet: decide from the travel whether it has started (or cancel it), then pick
     * the direction. Continues with applying it (`null`). Zones: 3 left edge, 4 right edge, 5 middle.
     */
    private fun startPageZoneSwipe(): Boolean? {
        val barsFixed = wa.barsState.barTouchDown
        if (!barsFixed) {
            // the page scrolled during this touch: a few more moves are allowed before the swipe is given up
            if (wa.currentPage!!.hasScrolled) {
                if ((wa.floatingState.gestureZone).toLong() == 5L) return cancelZoneGesture()
                val scrolledMoves: Int = wa.barsState.scrolledMoves + 1
                wa.barsState.scrolledMoves = scrolledMoves
                if (scrolledMoves > 2) return cancelZoneGesture()
            } else if (wa.barsState.resetIcons && (wa.floatingState.gestureZone).toLong() == 5L) {
                return cancelZoneGesture()
            }
        } else if (wa.scrollBar?.dragging == true) {
            return cancelZoneGesture()
        }
        if (!wa.barsState.barTouchDecided) {
            if (barsFixed || wa.webCoord!!.isPullActive()) {
                measureSwipeTravel()
            } else {
                // 0 / 1: the page can still scroll this way; otherwise it is at its end
                val pageScrollState: Int = wa.currentPage!!.pageState
                if (pageScrollState == 0 || pageScrollState == 1) measureSwipeTravel() else countOverScroll()
            }
            if (!wa.barsState.barTouchDecided || !wa.floatingState.edgeTouchActive) return null
        }
        // direction 2 = towards the left, 1 = towards the right
        val zone: Byte = wa.floatingState.gestureZone
        if (touchX <= wa.tabsState.touchDownX) {
            if ((zone).toLong() == 4L || (zone).toLong() == 5L) return decideZoneSwipe(2)
            return beginSwipe(2)
        }
        if ((zone).toLong() == 3L || (zone).toLong() == 5L) return decideZoneSwipe(1)
        return beginSwipe(1)
    }

    /** The finger has moved far enough from the touch-down point in the zone's direction: the swipe has started. */
    private fun measureSwipeTravel() {
        if (wa.floatingState.edgeTouchActive) {
            swipeSlop = halfSlop
        } else if ((wa.floatingState.gestureMode).toLong() == 0L) {
            swipeSlop = fullSlop
        }
        val zone: Byte = wa.floatingState.gestureZone
        val downX: Float = wa.tabsState.touchDownX
        val travel: Float = when {
            (zone).toLong() == 3L -> touchX - downX
            (zone).toLong() == 4L -> downX - touchX
            else -> abs(touchX - downX)
        }
        if (travel > swipeSlop) {
            wa.barsState.barTouchDecided = true
            wa.barsState.scrolledMoves = 0
        }
    }

    /** The page cannot scroll further: count the moves against its end; enough of them start the swipe. */
    private fun countOverScroll() {
        val moveCount: Int
        if (wa.currentPage!!.isOverScrolled() ||
            (wa.barsState.resetIcons && !wa.currentPage!!.hasScrolled && (wa.floatingState.gestureZone).toLong() != 5L)
        ) {
            moveCount = wa.barsState.overScrollMoves + 1
            wa.barsState.overScrollMoves = moveCount
            wa.barsState.scrolledMoves = 0
        } else {
            wa.barsState.overScrollMoves = 0
            moveCount = 0
        }
        wa.barsState.barTouchDecided =
            if ((wa.floatingState.gestureZone).toLong() != 5L) moveCount > 0 else moveCount > 2
    }

    /**
     * A started swipe in a zone that may mean page back / forward or a tab switch ([direction] 1 right, 2 left). With no
     * mode yet, an edge swipe (or the direction of "back" for the layout) goes back, falling back to the previous tab;
     * the other direction goes forward. In tab mode it needs a tab in that direction (or tab looping).
     */
    private fun decideZoneSwipe(direction: Byte): Boolean? {
        val mode: Byte = wa.floatingState.gestureMode
        if ((mode).toLong() == 0L) {
            val towardsBack = wa.tabsState.layoutRtl == ((direction).toLong() == 2L) || wa.floatingState.edgeTouchActive
            if (towardsBack) {
                if (canSwipeBack()) return beginSwipe(direction)
                if (!wa.hasPrevTab()) return cancelZoneGesture()
                wa.tabsState.tabSwipeActive = true
                wa.floatingState.gestureMode = 1
                return beginSwipe(direction)
            }
            return if (canSwipeForward()) beginSwipe(direction) else cancelZoneGesture()
        }
        if ((mode).toLong() != 1L) return beginSwipe(direction)
        val tabs: ArrayList<WebTabItem?> = wa.tabsState.tabItems ?: return cancelZoneGesture()
        if (tabs.size < 2) return cancelZoneGesture()
        if (ZtwoSettings.tabLoop.value) return beginSwipe(direction)
        val tabThere = if ((direction).toLong() == 1L) wa.currentTabIndex > 0
            else wa.currentTabIndex < (wa.tabsState.tabItems!!.size - 1)
        return if (tabThere) beginSwipe(direction) else cancelZoneGesture()
    }

    /**
     * Whether the page can go back. With "add page" on it checks the tab's own page list; the answer is kept for the
     * rest of the touch in `backSwipeState` (1 yes, 2 no), except when there is no frame.
     */
    private fun canSwipeBack(): Boolean {
        if (!ZtwoSettings.addPage.value) return wa.pageCanGoBack()
        val known: Byte = wa.miscState.backSwipeState
        if ((known).toLong() != 0L) return (known).toLong() == 1L
        val frame: WebNestFrame = wa.tabsState.currentFrame ?: return false
        val page: WebNestView? = frame.pageValid
        val canGo = if (page != null && page.canGoBack()) true else {
            val pages: List<Any?>? = frame.pageItems
            pages != null && pages.size >= 2 && frame.currentPageIndex > 0
        }
        wa.miscState.backSwipeState = if (canGo) 1 else 2
        return canGo
    }

    /** Whether the page can go forward; like [canSwipeBack], kept in `forwardSwipeState`. */
    private fun canSwipeForward(): Boolean {
        if (!ZtwoSettings.addPage.value) return wa.pageCanGoForward()
        val known: Byte = wa.miscState.forwardSwipeState
        if ((known).toLong() != 0L) return (known).toLong() == 1L
        val frame: WebNestFrame = wa.tabsState.currentFrame ?: return false
        val page: WebNestView? = frame.pageValid
        val canGo = if (page != null && page.canGoForward()) true else {
            val pages: List<Any?>? = frame.pageItems
            pages != null && pages.size >= 2 && frame.currentPageIndex < pages.size - 1
        }
        wa.miscState.forwardSwipeState = if (canGo) 1 else 2
        return canGo
    }

    /** The swipe has a direction: remember it and restart the travel from here. */
    private fun beginSwipe(direction: Byte): Boolean? {
        wa.tabsState.gestureDirection = direction
        wa.tabsState.touchDownX = touchX
        wa.findState.touchDownY = touchY
        return null
    }

    /** Give up the zone gesture for the rest of this touch. */
    private fun cancelZoneGesture(): Boolean {
        wa.floatingState.gestureMode = 2
        wa.floatingState.gestureZone = 0
        return handled
    }

    /** Page zone swipe with a direction: move the page / tab and the scroll-navigation arrows with the finger. */
    private fun applyPageZoneSwipe(): Boolean? {
        if ((wa.tabsState.gestureDirection).toLong() == 0L) { return handled }
        val webCoord: MyWebCoord? = wa.webCoord
        if (webCoord == null) { return handled }
        val page: WebNestView? = wa.currentPage
        if (page == null) { return handled }
        page.swipeTouch = true
        webCoord.cancelPull()
        val direction: Byte = wa.tabsState.gestureDirection
        val mode: Byte = wa.floatingState.gestureMode
        if ((direction).toLong() != 1L) {
            if ((mode).toLong() == 0L) {
                if (wa.swipeForwardOpensTab() == true) {
                    if (wa.tabsState.layoutRtl == false) {
                        wa.dragNextPage((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX)
                        return handled
                    }
                    wa.dragPrevPage((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX)
                    return handled
                }
                val rightScrollNavi2: ScrollNavi? = wa.floatingState.rightScrollNavi
                if (rightScrollNavi2 == null) { return handled }
                if (rightScrollNavi2.isIdle() == false) {
                    if (touchX >= wa.tabsState.touchDownX) { return handled }
                    if (abs(touchY - wa.findState.touchDownY) >= fullSlop) { return handled }
                    wa.floatingState.rightScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                    return handled
                }
                wa.floatingState.rightScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                return handled
            }
            if ((mode).toLong() != 1L) { return handled }
            if (wa.tabsState.layoutRtl == false) {
                if (wa.floatingState.edgeTouchActive == true) {
                    if (wa.tabsState.tabSwipeActive == false) {
                        wa.dragNextTab((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX, false)
                        return handled
                    }
                    val rightScrollNavi: ScrollNavi? = wa.floatingState.rightScrollNavi
                    if (rightScrollNavi == null) { return handled }
                    if (rightScrollNavi.isIdle() == false) {
                        if (touchX >= wa.tabsState.touchDownX) { return handled }
                        if (abs(touchY - wa.findState.touchDownY) >= fullSlop) { return handled }
                        wa.floatingState.rightScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                        return handled
                    }
                    wa.floatingState.rightScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                    return handled
                }
                wa.dragNextTab((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX, false)
                return handled
            }
            wa.dragPrevTab((wa.pageAreaWidth() + touchX) - wa.tabsState.touchDownX, 4)
            return handled
        }
        if ((mode).toLong() == 0L) {
            if (wa.swipeBackOpensTab() == false) {
                val leftScrollNavi2: ScrollNavi? = wa.leftScrollNavi
                if (leftScrollNavi2 == null) { return handled }
                if (leftScrollNavi2.isIdle() == false) {
                    if (touchX <= wa.tabsState.touchDownX) { return handled }
                    if (abs(touchY - wa.findState.touchDownY) >= fullSlop) { return handled }
                    wa.leftScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                    return handled
                }
                wa.leftScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                return handled
            }
            val rtl: Boolean = wa.tabsState.layoutRtl
            val dxFromDown: Float = touchX - wa.tabsState.touchDownX
            if (rtl == false) {
                wa.dragPrevPage(dxFromDown)
                return handled
            }
            wa.dragNextPage(dxFromDown)
            return handled
        }
        if ((mode).toLong() != 1L) { return handled }
        if (wa.tabsState.layoutRtl == true) {
            if (wa.floatingState.edgeTouchActive == true) {
                if (wa.tabsState.tabSwipeActive == false) {
                    wa.dragNextTab(touchX - wa.tabsState.touchDownX, false)
                    return handled
                }
                val leftScrollNavi: ScrollNavi? = wa.leftScrollNavi
                if (leftScrollNavi == null) { return handled }
                if (leftScrollNavi.isIdle() == false) {
                    if (touchX <= wa.tabsState.touchDownX) { return handled }
                    if (abs(touchY - wa.findState.touchDownY) >= fullSlop) { return handled }
                    wa.leftScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                    return handled
                }
                wa.leftScrollNavi!!.onScroll(wa.tabsState.touchDownX, touchX)
                return handled
            }
            wa.dragNextTab(touchX - wa.tabsState.touchDownX, false)
            return handled
        }
        wa.dragPrevTab(touchX - wa.tabsState.touchDownX, 4)
        return handled
    }

    /** First finger down: reset the gesture state, remember the start point, then find the zone the touch started in. */
    private fun onDown(): Boolean? {
        fun applyTabSwipeDown(): Boolean? {
            val longTapAction: Int = BrowserSettings.tabSwipeDown.value
            if (longTapAction < 0) {
                wa.tabsState.touchBusy = false
                return handled
            }
            if (longTapAction >= 74) {
                wa.tabsState.touchBusy = false
                return handled
            }
            if ((longTapAction).toLong() != 0L) {
                wa.tabsState.touchBusy = true
                wa.tabsState.touchTabIndex = wa.tabBarItemAtTouch()
                return handled
            }
            wa.tabsState.touchBusy = false
            return handled
        }
        wa.pageState.pinchActive = false
        wa.miscState.pinchStartX = null
        wa.miscState.pinchStartY = null
        wa.miscState.touchBusy = true
        wa.resetGestureState(true, wa.isPortalHome())
        wa.miscState.backSwipeState = 0
        wa.miscState.forwardSwipeState = 0
        wa.pageState.canGoBackCache = 0
        wa.pageState.canGoForwardCache = 0
        wa.dialogsState.touchBusy = false
        wa.tabsState.tabDragging = false
        wa.tabsState.dragTabIndex = -1
        wa.tabsState.touchBusy = false
        wa.tabsState.touchTabIndex = -1
        wa.tabsState.touchBarView = null
        wa.tabsState.touchBarItem = -1
        if (wa.dismissHints() == false) {
            wa.tabsState.touchDownX = touchX
            wa.findState.touchDownY = touchY
            wa.miscState.lastScrollY = touchY
            wa.miscState.lastMoveX = touchX
            wa.miscState.lastMoveY = touchY
            wa.miscState.contentTop = wa.statusBarInset()
            if ((BrowserSettings.tabBar.value).toLong() == 0L) { return classifyDownZone() }
            if (WebViewActivity.isTouchFrameActive(wa.barsState.touchFrame) == false) { return classifyDownZone() }
            val tapAction: Int = BrowserSettings.tabSwipeUp.value
            if (tapAction < 0) { return applyTabSwipeDown() }
            if (tapAction >= 74) { return applyTabSwipeDown() }
            if ((tapAction).toLong() == 0L) { return applyTabSwipeDown() }
            wa.tabsState.touchBusy = true
            wa.tabsState.touchTabIndex = wa.tabBarItemAtTouch()
            return handled
        }
        wa.miscState.hintsDismissed = true
        return handled
    }

    /** Which gesture zone the touch-down is in (sets `ca` / `ba` / `na`). */
    private fun classifyDownZone(): Boolean? {
        if (wa.webCoord == null) { return handled }
        landscape = wa.isLandscapeScreen()
        if (wa.keyboardOpen == false) { checkBottomBarZone()?.let { return it }; return checkTopBarAndPageZones() }
        return checkTopBarAndPageZones()
    }

    /** Touch-down on or near the bottom bar: bottom-bar swipe (`ca` 2) when enabled. Continues with the top bar / page zones. */
    private fun checkBottomBarZone(): Boolean? {
        var bottomBarEnabled: Boolean = false
        var bottomBar: MyBarView? = null
        var onBottomBarButton: Boolean = false
        var onBottomBar: Boolean = false
        var downY: Float = 0f
        var body: MyWebBody? = null
        var bottomEdge: Int = 0
        var bottomZone: Int = 0
        var bottomBarHit: Boolean = false
        var bottomBarAction: Int = 0
        var onBottomBarButton2: Boolean = false
        fun startBottomBarSwipe(): Boolean? {
            bottomBarAction = ZoneSettings.gesBot.value
            if ((bottomBarAction).toLong() != 0L) {
                if ((bottomBarAction).toLong() != 1L) { return handled }
                val tabs: ArrayList<WebTabItem?>? = wa.tabsState.tabItems
                if (tabs == null) { return handled }
                if (tabs.size < 2) { return handled }
                wa.floatingState.gestureMode = 1
                wa.floatingState.gestureZone = 2
                wa.barsState.barTouchDown = true
                return handled
            }
            wa.floatingState.gestureMode = 0
            wa.floatingState.gestureZone = 2
            wa.barsState.barTouchDown = true
            return handled
        }
        fun checkBelowBottomZone(): Boolean? {
            if (downY <= (bottomEdge - bottomZone)) { return null }
            return startBottomBarSwipe()
        }
        fun checkBottomZoneHit(): Boolean? {
            if (bottomBarEnabled == false) { return null }
            if (bottomBarHit == true) { return startBottomBarSwipe() }
            downY = wa.findState.touchDownY
            body = wa.dialogsState.webBody
            if (body != null) {
                if (MainConst.isApi30 == false) {
                    bottomEdge = body!!.rectBottom + wa.miscState.contentTop
                    return checkBelowBottomZone()
                }
                bottomEdge = body!!.rectBottom
                return checkBelowBottomZone()
            }
            bottomEdge = 0
            return checkBelowBottomZone()
        }
        fun pickBottomBarItem(): Boolean? {
            if (onBottomBar == false) { return checkBottomZoneHit() }
            wa.pickBarItem(touchX.toInt(), touchY.toInt())
            return checkBottomZoneHit()
        }
        fun noBottomBarHit(): Boolean? {
            bottomBarHit = false
            return pickBottomBarItem()
        }
        fun checkBottomBarButton(): Boolean? {
            if (onBottomBarButton2 == false) { return noBottomBarHit() }
            bottomBarHit = true
            return pickBottomBarItem()
        }
        fun measureBottomZone(): Boolean? {
            if (landscape == false) {
                bottomZone = ZoneSettings.portAreaBot.value + MainApp.dp4
                if (bottomBarEnabled == false) { return noBottomBarHit() }
                val bottomBar2: MyBarView? = wa.barsState.bottomBarView
                if (bottomBar2 != null) {
                    onBottomBarButton2 = bottomBar2.barTouched
                    return checkBottomBarButton()
                }
                onBottomBarButton2 = false
                return checkBottomBarButton()
            }
            bottomZone = ZoneSettings.landAreaBot.value + MainApp.dp4
            bottomBarHit = onBottomBar
            return pickBottomBarItem()
        }
        fun notOnBottomBar(): Boolean? {
            onBottomBar = false
            return measureBottomZone()
        }
        fun checkOnBottomBar(): Boolean? {
            if (onBottomBarButton == false) { return notOnBottomBar() }
            onBottomBar = true
            return measureBottomZone()
        }
        fun checkBottomBarTouched(): Boolean? {
            if (bottomBarEnabled == false) { return notOnBottomBar() }
            bottomBar = wa.barsState.bottomBarView
            if (bottomBar != null) {
                onBottomBarButton = bottomBar!!.barTouched
                return checkOnBottomBar()
            }
            onBottomBarButton = false
            return checkOnBottomBar()
        }
        fun enableBottomBar(): Boolean? {
            bottomBarEnabled = true
            return checkBottomBarTouched()
        }
        if (wa.dialogsState.isVideoHost == true) {
            if ((wa.pageState.barOffset).toLong() == 0L) { return enableBottomBar() }
            bottomBarEnabled = false
            return checkBottomBarTouched()
        }
        return enableBottomBar()
    }

    /** Touch-down on the top bar (`ca` 1) or in a page zone: left / right edge (3 / 4) or the middle (5). */
    private fun checkTopBarAndPageZones(): Boolean? {
        var downY: Float = 0f
        var topInset: Int = 0
        var topZoneHeight: Int = 0
        var topBar: MyBarView? = null
        var onTopBarButton: Boolean = false
        var cutoutInset: Int = 0
        var leftZoneWidth: Int = 0
        var leftZoneEnd: Int = 0
        var rightZoneWidth: Int = 0
        var downX2: Float = 0f
        fun startTopBarSwipe(): Boolean? {
            val topBarAction: Int = ZoneSettings.gesTop.value
            if ((topBarAction).toLong() != 0L) {
                if ((topBarAction).toLong() != 1L) { return handled }
                val tabs4: ArrayList<WebTabItem?>? = wa.tabsState.tabItems
                if (tabs4 == null) { return handled }
                if (tabs4.size < 2) { return handled }
                wa.floatingState.gestureMode = 1
                wa.floatingState.gestureZone = 1
                wa.barsState.barTouchDown = true
                return handled
            }
            wa.floatingState.gestureMode = 0
            wa.floatingState.gestureZone = 1
            wa.barsState.barTouchDown = true
            return handled
        }
        fun startLeftZoneSwipe(): Boolean? {
            if ((ZoneSettings.gesLeft.value).toLong() != 0L) {
                if (wa.topBarModeHidden() == false) {
                    if ((ZoneSettings.gesLeft.value).toLong() != 1L) { return handled }
                    val tabs3: ArrayList<WebTabItem?>? = wa.tabsState.tabItems
                    if (tabs3 == null) { return handled }
                    if (tabs3.size < 2) { return handled }
                    if (ZtwoSettings.tabLoop.value == false) {
                        if (wa.currentTabIndex <= 0) { return handled }
                        wa.floatingState.gestureMode = 1
                        wa.floatingState.gestureZone = 3
                        wa.barsState.barTouchDown = true
                        return handled
                    }
                    wa.floatingState.gestureMode = 1
                    wa.floatingState.gestureZone = 3
                    wa.barsState.barTouchDown = true
                    return handled
                }
                wa.floatingState.gestureMode = 0
                wa.floatingState.gestureZone = 3
                wa.barsState.barTouchDown = true
                wa.floatingState.edgeTouchActive = true
                return handled
            }
            wa.floatingState.gestureMode = 0
            wa.floatingState.gestureZone = 3
            wa.barsState.barTouchDown = true
            return handled
        }
        fun startRightZoneSwipe(): Boolean? {
            if ((ZoneSettings.gesRight.value).toLong() != 0L) {
                if (wa.bottomBarModeHidden() == false) {
                    if ((ZoneSettings.gesRight.value).toLong() != 1L) { return handled }
                    val tabs2: ArrayList<WebTabItem?>? = wa.tabsState.tabItems
                    if (tabs2 == null) { return handled }
                    if (tabs2.size < 2) { return handled }
                    if (ZtwoSettings.tabLoop.value == false) {
                        if (wa.currentTabIndex >= (wa.tabsState.tabItems!!.size - 1)) { return handled }
                        wa.floatingState.gestureMode = 1
                        wa.floatingState.gestureZone = 4
                        wa.barsState.barTouchDown = true
                        return handled
                    }
                    wa.floatingState.gestureMode = 1
                    wa.floatingState.gestureZone = 4
                    wa.barsState.barTouchDown = true
                    return handled
                }
                wa.floatingState.gestureMode = 0
                wa.floatingState.gestureZone = 4
                wa.barsState.barTouchDown = true
                wa.floatingState.edgeTouchActive = true
                return handled
            }
            wa.floatingState.gestureMode = 0
            wa.floatingState.gestureZone = 4
            wa.barsState.barTouchDown = true
            return handled
        }
        fun checkRightAndMiddleZones(): Boolean? {
            val rightZoneStart: Int = wa.dialogsState.webBody!!.rectEnd - rightZoneWidth
            downX2 = wa.tabsState.touchDownX
            if (downX2 > (rightZoneStart - MainApp.dp10)) {
                if (downX2 <= rightZoneStart) { return handled }
                if (ReadSettings.guideGesture.value == false) { return startRightZoneSwipe() }
                wa.showGestureGuide()
                return startRightZoneSwipe()
            }
            val middleZoneAction: Int = ZoneSettings.gesCenter.value
            if ((middleZoneAction).toLong() != 0L) {
                if ((middleZoneAction).toLong() != 1L) { return handled }
                val tabs: ArrayList<WebTabItem?>? = wa.tabsState.tabItems
                if (tabs == null) { return handled }
                if (tabs.size < 2) { return handled }
                wa.floatingState.gestureMode = 1
                wa.floatingState.gestureZone = 5
                if (wa.onStartPage == false) {
                    if (TextUtils.isEmpty(wa.dialogsState.pageUrl) == false) {
                        wa.barsState.barTouchDown = middleTabSwipeOnStart
                        return handled
                    }
                    middleTabSwipeOnStart = true
                    wa.barsState.barTouchDown = middleTabSwipeOnStart
                    return handled
                }
                middleTabSwipeOnStart = true
                wa.barsState.barTouchDown = middleTabSwipeOnStart
                return handled
            }
            wa.floatingState.gestureMode = 0
            wa.floatingState.gestureZone = 5
            if (wa.onStartPage == false) {
                if (TextUtils.isEmpty(wa.dialogsState.pageUrl) == false) {
                    wa.barsState.barTouchDown = middleSwipeOnStart
                    return handled
                }
                middleSwipeOnStart = true
                wa.barsState.barTouchDown = middleSwipeOnStart
                return handled
            }
            middleSwipeOnStart = true
            wa.barsState.barTouchDown = middleSwipeOnStart
            return handled
        }
        fun checkLeftZone(): Boolean? {
            leftZoneEnd = wa.dialogsState.webBody!!.rectStart + leftZoneWidth
            val downX: Float = wa.tabsState.touchDownX
            if (downX < (MainApp.dp10 + leftZoneEnd)) {
                if (downX >= leftZoneEnd) { return handled }
                if (ReadSettings.guideGesture.value == false) { return startLeftZoneSwipe() }
                wa.showGestureGuide()
                return startLeftZoneSwipe()
            }
            if (landscape == false) {
                rightZoneWidth = ZoneSettings.portAreaRight.value
                return checkRightAndMiddleZones()
            }
            rightZoneWidth = Math.max(ZoneSettings.landAreaRight.value, TtsSettings.webLand2.value) - cutoutInset
            return checkRightAndMiddleZones()
        }
        fun measureSideZones(): Boolean? {
            if (onTopBarButton == true) { return startTopBarSwipe() }
            if (wa.currentPage == null) { return handled }
            if (landscape == false) {
                leftZoneWidth = ZoneSettings.portAreaLeft.value
                cutoutInset = 0
                return checkLeftZone()
            }
            cutoutInset = MainUtil.viewScreenX(wa.dialogsState.webBody)
            leftZoneWidth = Math.max(ZoneSettings.landAreaLeft.value, TtsSettings.webLand.value) + cutoutInset
            return checkLeftZone()
        }
        fun checkTopBarTouch(): Boolean? {
            if (downY < (topInset + topZoneHeight)) { return startTopBarSwipe() }
            topBar = wa.barsState.topBarView
            if (topBar != null) {
                onTopBarButton = topBar!!.barTouched
                return measureSideZones()
            }
            onTopBarButton = false
            return measureSideZones()
        }
        downY = wa.findState.touchDownY
        topInset = wa.miscState.contentTop
        if (landscape == false) {
            topZoneHeight = ZoneSettings.portAreaTop.value
            return checkTopBarTouch()
        }
        topZoneHeight = ZoneSettings.landAreaTop.value
        return checkTopBarTouch()
    }
}
