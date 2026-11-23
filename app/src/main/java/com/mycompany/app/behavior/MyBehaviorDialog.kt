package com.mycompany.app.behavior

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.ClassLoaderCreator
import android.util.AttributeSet
import android.view.AbsSavedState
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.math.MathUtils
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.accessibility.AccessibilityViewCommand
import androidx.customview.widget.ViewDragHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.web.WebNestView
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@Suppress("deprecation")
class MyBehaviorDialog<V : View> constructor(context: Context) :
    CoordinatorLayout.Behavior<V?>() {
    @JvmField
    var nestedLocked: Boolean = false
    @JvmField
    var callbacks: ArrayList<BottomSheetCallback?> = ArrayList()
    @JvmField
    var tracker: VelocityTracker? = null
    @JvmField
    var activePointerId: Int = 0
    @JvmField
    var initialY: Int = 0
    @JvmField
    var touchingScrollingChild: Boolean = false
    @JvmField
    var importantForA11yMap: HashMap<View?, Int>? = null
    @JvmField
    var dragCallback: ViewDragHelper.Callback?
    @JvmField
    var fitToContents: Boolean = true
    @JvmField
    var maximumVelocity: Float
    @JvmField
    var peekHeightAuto: Boolean
    @JvmField
    var peekFullWidth: Boolean
    @JvmField
    var peekHeightMin: Int = 0
    @JvmField
    var settleTask: SettleRunnable? = null
    @JvmField
    var fitToContentsOffset: Int = 0
    @JvmField
    var halfExpandedOffset: Int = 0
    @JvmField
    var collapsedOffset: Int = 0
    @JvmField
    var hideable: Boolean = false
    @JvmField
    var draggable: Boolean = true
    @JvmField
    var hideOnDrag: Boolean = false
    @JvmField
    var sheetState: Int = 4
    @JvmField
    var nestedScrollOff: Boolean = false
    @JvmField
    var fixedScrollChild: View? = null
    @JvmField
    var dragHelper: ViewDragHelper? = null
    @JvmField
    var ignoreEvents: Boolean = false
    @JvmField
    var lastNestedScrollDy: Int = 0
    @JvmField
    var nestedScrolled: Boolean = false
    @JvmField
    var parentWidth: Int = 0
    @JvmField
    var parentHeight: Int = 0
    @JvmField
    var childHeight: Int = 0
    @JvmField
    var hideThreshold: Int = 0
    @JvmField
    var viewRef: WeakReference<*>? = null
    @JvmField
    var nestedScrollingChildRef: WeakReference<*>? = null

    internal class SettleTask(@JvmField val outer: MyBehaviorDialog<*>,
                                   @JvmField val view: View, stateParam: Int
    ) : Runnable {
        val state: Byte

        init {
            this.state = (stateParam).toByte()
        }

        override fun run() {
            this.outer.settleToState(this.view, this.state.toInt())
        }
    }

    internal inner class ExpandedOffsetCallback : ViewDragHelper.Callback() {
        override fun clampViewPositionHorizontal(view: View, left: Int, dx: Int): Int {
            return view.getLeft()
        }

        override fun clampViewPositionVertical(view: View, top: Int, dy: Int): Int {
            val myBehaviorDialog: MyBehaviorDialog<*> = this@MyBehaviorDialog
            return MathUtils.clamp(
                top,
                myBehaviorDialog.expandedOffset(),
                if (myBehaviorDialog.hideable) myBehaviorDialog.parentHeight else myBehaviorDialog.collapsedOffset
            )
        }

        override fun getViewVerticalDragRange(child: View): Int {
            val myBehaviorDialog: MyBehaviorDialog<*> = this@MyBehaviorDialog
            return if (myBehaviorDialog.hideable) myBehaviorDialog.parentHeight else myBehaviorDialog.collapsedOffset
        }

        override fun onViewDragStateChanged(state: Int) {
            if (state == 1) {
                val myBehaviorDialog: MyBehaviorDialog<*> = this@MyBehaviorDialog
                if (myBehaviorDialog.draggable) {
                    myBehaviorDialog.setStateInternal(1)
                }
            }
        }

        override fun onViewPositionChanged(view: View, left: Int, top: Int, dx: Int, dy: Int) {
            this@MyBehaviorDialog.dispatchOnSlide(top)
        }

        override fun onViewReleased(view: View, xVelocity: Float, yVelocity: Float) {
            var topValue = 0
            var state = 6
            val myBehaviorDialog: MyBehaviorDialog<*> = this@MyBehaviorDialog
            if (yVelocity < 0.0f) {
                if (myBehaviorDialog.fitToContents) {
                    topValue = myBehaviorDialog.fitToContentsOffset
                } else {
                    val top = view.getTop()
                    val halfExpandedOffset = myBehaviorDialog.halfExpandedOffset
                    if (top > halfExpandedOffset) {
                        topValue = halfExpandedOffset
                    }
                }
                state = 3
            } else if (!myBehaviorDialog.hideable || !myBehaviorDialog.shouldHide(view, yVelocity)) {
                if (myBehaviorDialog.hideOnDrag) {
                    if (myBehaviorDialog.fitToContents) {
                        topValue = myBehaviorDialog.fitToContentsOffset
                    }
                } else if (yVelocity == 0.0f || abs(xVelocity) > abs(yVelocity)) {
                    val top = view.getTop()
                    if (!myBehaviorDialog.fitToContents) {
                        val halfExpandedOffset = myBehaviorDialog.halfExpandedOffset
                        val collapsedOffset = myBehaviorDialog.collapsedOffset
                        if (top < halfExpandedOffset) {
                            if (top >= abs(top - collapsedOffset)) {
                                topValue = myBehaviorDialog.halfExpandedOffset
                            }
                        } else if (abs(top - halfExpandedOffset) < abs(top - collapsedOffset)) {
                            topValue = myBehaviorDialog.halfExpandedOffset
                        } else {
                            topValue = myBehaviorDialog.collapsedOffset
                            state = 4
                        }
                    } else if (abs(top - myBehaviorDialog.fitToContentsOffset) < abs(top - myBehaviorDialog.collapsedOffset)) {
                        topValue = myBehaviorDialog.fitToContentsOffset
                    } else {
                        topValue = myBehaviorDialog.collapsedOffset
                        state = 4
                    }
                } else {
                    if (myBehaviorDialog.fitToContents) {
                        topValue = myBehaviorDialog.collapsedOffset
                    } else {
                        val top = view.getTop()
                        if (abs(top - myBehaviorDialog.halfExpandedOffset) < abs(top - myBehaviorDialog.collapsedOffset)) {
                            topValue = myBehaviorDialog.halfExpandedOffset
                        } else {
                            topValue = myBehaviorDialog.collapsedOffset
                        }
                    }
                    state = 4
                }
                state = 3
            } else if (abs(xVelocity) >= abs(yVelocity) || yVelocity <= 500.0f) {
                val top = view.getTop()
                val parentHeight = myBehaviorDialog.parentHeight
                if (!(parentHeight == myBehaviorDialog.childHeight && myBehaviorDialog.hideOnDrag && top > myBehaviorDialog.hideThreshold) && top <= (myBehaviorDialog.expandedOffset() + parentHeight) / 2) {
                    if (myBehaviorDialog.fitToContents) {
                        topValue = myBehaviorDialog.fitToContentsOffset
                    } else if (abs(view.getTop()) >= abs(view.getTop() - myBehaviorDialog.halfExpandedOffset)) {
                        topValue = myBehaviorDialog.halfExpandedOffset
                    }
                    state = 3
                } else {
                    topValue = myBehaviorDialog.parentHeight
                    state = 5
                }
            } else {
                topValue = myBehaviorDialog.parentHeight
                state = 5
            }
            myBehaviorDialog.startSettling(view, state, topValue, true)
        }

        override fun tryCaptureView(view: View, pointerId: Int): Boolean {
            val myBehaviorDialog: MyBehaviorDialog<*> = this@MyBehaviorDialog
            val sheetState = myBehaviorDialog.sheetState
            if (sheetState == 1 || myBehaviorDialog.touchingScrollingChild) {
                return false
            }
            if (sheetState == 3 && myBehaviorDialog.activePointerId == pointerId) {
                val weakReference = myBehaviorDialog.nestedScrollingChildRef
                val scrollView = if (weakReference != null) weakReference.get() as View? else null
                if (scrollView != null && scrollView.canScrollVertically(-1)) {
                    return false
                }
            }
            val weakReference = myBehaviorDialog.viewRef
            return weakReference != null && weakReference.get() === view
        }
    }

    internal inner class ApplyStateStep(@JvmField val outer: MyBehaviorDialog<*>, stateParam: Int) :
        AccessibilityViewCommand {
        val state: Byte

        init {
            this.state = (stateParam).toByte()
        }

        override fun perform(view: View, arguments: AccessibilityViewCommand.CommandArguments?): Boolean {
            this.outer.applyState(this.state.toInt())
            return true
        }
    }

    abstract class BottomSheetCallback {
        abstract fun onStateChanged(state: Int)
    }

    inner class SettleRunnable(@JvmField var view: View, targetStateParam: Int) : Runnable {
        @JvmField
        var isPosted: Boolean = false
        @JvmField
        var targetState: Byte

        init {
            this.targetState = (targetStateParam).toByte()
        }

        override fun run() {
            val myBehaviorDialog: MyBehaviorDialog<*> = this@MyBehaviorDialog
            val viewDragHelper = myBehaviorDialog.dragHelper
            if (viewDragHelper == null || !viewDragHelper.continueSettling(true)) {
                myBehaviorDialog.setStateInternal(this.targetState.toInt())
            } else {
                this.view.postOnAnimation(this)
            }
            this.isPosted = false
        }
    }

    init {
        this.callbacks = ArrayList()
        this.dragCallback = ExpandedOffsetCallback()
        this.peekHeightAuto = true
        this.peekFullWidth = true
        this.maximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity().toFloat()
    }

    fun findScrollingChild(view: View?): View? {
        if (view == null) {
            return null
        }
        val tag: Any? = view.getTag()
        val layoutManager: RecyclerView.LayoutManager? = (view as? RecyclerView)?.layoutManager
        if (if (view.getVisibility() != View.VISIBLE || (tag != null && (tag is String) && "skip_scroll" == tag) || ((view is RecyclerView) && !(layoutManager != null && (layoutManager is LinearLayoutManager) && (layoutManager).orientation == 1))
            ) false else ViewCompat.isNestedScrollingEnabled(view)) {
            return view
        }
        if (view !is ViewGroup) {
            return null
        }
        val viewGroup = view
        val childCount = viewGroup.getChildCount()
        for (index in 0..<childCount) {
            val viewA = findScrollingChild(viewGroup.getChildAt(index))
            if (viewA != null) {
                return viewA
            }
        }
        return null
    }

    fun expandedOffset(): Int {
        if (this.fitToContents) {
            return this.fitToContentsOffset
        }
        return 0
    }

    fun setStateInternal(sheetState: Int) {
        if (this.sheetState == sheetState) {
            return
        }
        this.sheetState = sheetState
        val weakReference = this.viewRef
        if (weakReference == null || (weakReference.get() as View?) == null) {
            return
        }
        var index = 0
        if (sheetState == 3) {
            updateImportantForA11y(true)
        } else if (sheetState == 6 || sheetState == 5 || sheetState == 4) {
            updateImportantForA11y(false)
        }
        while (true) {
            val arrayList = this.callbacks
            if (index >= arrayList.size) {
                updateA11yActions()
                return
            } else {
                (arrayList.get(index) as BottomSheetCallback).onStateChanged(sheetState)
                index++
            }
        }
    }

    fun settleToState(view: View, sheetState: Int) {
        var sheetState = sheetState
        var targetOffset: Int
        if (sheetState == 4) {
            targetOffset = this.collapsedOffset
        } else if (sheetState == 6) {
            targetOffset = this.halfExpandedOffset
            val fitToContentsOffset = this.fitToContentsOffset
            if (this.fitToContents && targetOffset <= fitToContentsOffset) {
                sheetState = 3
                targetOffset = fitToContentsOffset
            }
        } else if (sheetState == 3) {
            targetOffset = expandedOffset()
        } else {
            if (!this.hideable || sheetState != 5) {
                throw IllegalArgumentException("Illegal state argument: $sheetState")
                return
            }
            targetOffset = this.parentHeight
        }
        startSettling(view, sheetState, targetOffset, false)
    }

    fun shouldHide(view: View, yVelocity: Float): Boolean {
        val top = view.getTop()
        if (this.hideOnDrag && top > this.hideThreshold) {
            return true
        }
        if (top >= this.collapsedOffset) {
            return abs(
                ((yVelocity * 0.1f) + (view.getTop().toFloat())) - (this.collapsedOffset.toFloat())
            ) / (calculatePeekHeight().toFloat()) > 0.5f
        }
        return false
    }

    fun startSettling(view: View, sheetState: Int, top: Int, settle: Boolean) {
        if (sheetState == 5) {
            setStateInternal(sheetState)
            return
        }
        val viewDragHelper = this.dragHelper
        if (viewDragHelper == null || (if (!settle) viewDragHelper.smoothSlideViewTo(
                view,
                view.getLeft(),
                top
            ) else viewDragHelper.settleCapturedViewAt(view.getLeft(), top))
        ) {
            setStateInternal(sheetState)
            return
        }
        setStateInternal(2)
        var settleRunnable: SettleRunnable? = this.settleTask
        if (settleRunnable == null) {
            settleRunnable = SettleRunnable(view, sheetState)
            this.settleTask = settleRunnable
        }
        if (settleRunnable.isPosted) {
            settleRunnable.targetState = (sheetState).toByte()
            return
        }
        settleRunnable.targetState = (sheetState).toByte()
        view.postOnAnimation(settleRunnable)
        settleRunnable.isPosted = true
    }

    fun updateA11yActions() {
        val weakReference = this.viewRef
        val view: View? = if (weakReference == null) null else weakReference.get() as View?
        if (view == null) {
            return
        }
        ViewCompat.removeAccessibilityAction(view, 524288)
        ViewCompat.removeAccessibilityAction(view, 262144)
        ViewCompat.removeAccessibilityAction(view, 1048576)
        if (this.hideable && this.sheetState != 5) {
            ViewCompat.replaceAccessibilityAction(
                view,
                AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_DISMISS,
                null,
                ApplyStateStep(this, 5)
            )
        }
        val sheetState = this.sheetState
        val fitToContents = this.fitToContents
        if (sheetState == 3) {
            ViewCompat.replaceAccessibilityAction(
                view,
                AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE,
                null,
                ApplyStateStep(this, if (fitToContents) 4 else 6)
            )
            return
        }
        if (sheetState == 4) {
            ViewCompat.replaceAccessibilityAction(
                view,
                AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND,
                null,
                ApplyStateStep(this, if (fitToContents) 3 else 6)
            )
        } else {
            if (sheetState != 6) {
                return
            }
            ViewCompat.replaceAccessibilityAction(
                view,
                AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_COLLAPSE,
                null,
                ApplyStateStep(this, 4)
            )
            ViewCompat.replaceAccessibilityAction(
                view,
                AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_EXPAND,
                null,
                ApplyStateStep(this, 3)
            )
        }
    }

    fun updateImportantForA11y(expanded: Boolean) {
        val weakReference = this.viewRef
        if (weakReference != null) {
            val parent = (weakReference.get() as View).getParent()
            if (parent is CoordinatorLayout) {
                val coordinatorLayout = parent
                val childCount = coordinatorLayout.getChildCount()
                if (expanded) {
                    if (this.importantForA11yMap != null) {
                        return
                    } else {
                        this.importantForA11yMap = HashMap(childCount)
                    }
                }
                for (index in 0..<childCount) {
                    val childAt = coordinatorLayout.getChildAt(index)
                    if (childAt !== this.viewRef!!.get() && expanded) {
                        this.importantForA11yMap!!.put(childAt, childAt.getImportantForAccessibility())
                    }
                }
                if (expanded) {
                    return
                }
                this.importantForA11yMap = null
            }
        }
    }

    fun applyState(sheetState: Int) {
        if (sheetState != this.sheetState) {
            val weakReference = this.viewRef
            if (weakReference == null) {
                if (sheetState == 4 || sheetState == 3 || sheetState == 6 || (this.hideable && sheetState == 5)) {
                    this.sheetState = sheetState
                    return
                }
                return
            }
            val view = weakReference.get() as View?
            if (view == null) {
                return
            }
            val parent = view.getParent()
            if (parent != null && parent.isLayoutRequested() && view.isAttachedToWindow()) {
                view.post(SettleTask(this, view, sheetState))
            } else {
                settleToState(view, sheetState)
            }
        }
    }

    override fun onAttachedToLayoutParams(layoutParams: CoordinatorLayout.LayoutParams) {
        this.viewRef = null
        this.dragHelper = null
    }

    override fun onDetachedFromLayoutParams() {
        this.viewRef = null
        this.dragHelper = null
    }

    override fun onInterceptTouchEvent(
        coordinatorLayout: CoordinatorLayout,
        view: V,
        motionEvent: MotionEvent
    ): Boolean {
        val viewDragHelper: ViewDragHelper?
        if (!view.isShown() || !this.draggable) {
            this.ignoreEvents = true
            return false
        }
        val actionMasked = motionEvent.getActionMasked()
        if (actionMasked == 5) {
            this.ignoreEvents = true
            return false
        }
        if (actionMasked == 0) {
            this.activePointerId = -1
            val velocityTracker = this.tracker
            if (velocityTracker != null) {
                velocityTracker.recycle()
                this.tracker = null
            }
        }
        var velocityTrackerObtain = this.tracker
        if (velocityTrackerObtain == null) {
            velocityTrackerObtain = VelocityTracker.obtain()
            this.tracker = velocityTrackerObtain
        }
        velocityTrackerObtain.addMovement(motionEvent)
        if (actionMasked == 0) {
            val x = motionEvent.getX().toInt()
            this.initialY = motionEvent.getY().toInt()
            if (this.sheetState != 2) {
                val weakReference = this.nestedScrollingChildRef
                val view2 = if (weakReference != null) weakReference.get() as View? else null
                if (view2 != null && coordinatorLayout.isPointInChildBounds(view2, x, this.initialY)) {
                    this.activePointerId = motionEvent.getPointerId(motionEvent.getActionIndex())
                    this.touchingScrollingChild = true
                }
            }
            this.ignoreEvents = this.activePointerId == -1 && !coordinatorLayout.isPointInChildBounds(view, x, this.initialY)
        } else if (actionMasked == 1 || actionMasked == 3) {
            this.touchingScrollingChild = false
            this.activePointerId = -1
            if (this.ignoreEvents) {
                this.ignoreEvents = false
                return false
            }
        }
        viewDragHelper = this.dragHelper
        if (this.ignoreEvents || viewDragHelper == null || !viewDragHelper.shouldInterceptTouchEvent(
                motionEvent
            )
        ) {
            val weakReference = this.nestedScrollingChildRef
            val view3 = if (weakReference != null) weakReference.get() as View? else null
            if (actionMasked != 2 || view3 == null || this.ignoreEvents || this.sheetState == 1 || coordinatorLayout.isPointInChildBounds(
                    view3,
                    motionEvent.getX().toInt(),
                    motionEvent.getY().toInt()
                ) || this.dragHelper == null || abs(this.initialY - motionEvent.getY()) <= this.dragHelper!!.touchSlop
            ) {
                return false
            }
        }
        return true
    }

    override fun onLayoutChild(coordinatorLayout: CoordinatorLayout, view: V, layoutDirection: Int): Boolean {
        val collapsedTop: Int
        val context = view.getContext()
        if (this.viewRef == null) {
            this.peekHeightMin = MainUtil.dpToPx(context, 64.0f).toInt()
            this.viewRef = WeakReference<Any?>(view)
            updateA11yActions()
            if (view.getImportantForAccessibility() == 0) {
                view.setImportantForAccessibility(1)
            }
        }
        if (this.dragCallback == null) {
            this.dragCallback = ExpandedOffsetCallback()
        }
        if (this.dragHelper == null) {
            this.dragHelper = ViewDragHelper.create(coordinatorLayout, this.dragCallback!!)
        }
        val top = view.getTop()
        coordinatorLayout.onLayoutChild(view, layoutDirection)
        this.parentWidth = coordinatorLayout.getWidth()
        this.parentHeight = coordinatorLayout.getHeight()
        this.childHeight = view.getHeight()
        var nestedLocked = false
        val shortScreen = MainUtil.pxToDp(context, this.parentHeight.toFloat()) < 600.0f
        if (shortScreen) {
            this.hideOnDrag = true
            this.sheetState = 3
        }
        val parentHeight = this.parentHeight
        val childHeight = this.childHeight
        if (parentHeight == childHeight) {
            this.hideThreshold = min(parentHeight / 4, MainApp.dp48 shl 2)
        } else {
            this.hideThreshold = (childHeight / 2) + (parentHeight - childHeight)
        }
        this.fitToContentsOffset = max(0, this.parentHeight - this.childHeight)
        this.halfExpandedOffset = (0.5f * this.parentHeight).toInt()
        val peekHeight = calculatePeekHeight()
        val parentHeightNow = this.parentHeight
        if (this.fitToContents) {
            collapsedTop = max(parentHeightNow - peekHeight, this.fitToContentsOffset)
            this.collapsedOffset = collapsedTop
        } else {
            collapsedTop = parentHeightNow - peekHeight
            this.collapsedOffset = collapsedTop
        }
        val sheetState = this.sheetState
        if (sheetState == 3) {
            val topOffset = expandedOffset()
            view.offsetTopAndBottom(topOffset)
        } else if (sheetState == 6) {
            val halfExpandedOffset = this.halfExpandedOffset
            view.offsetTopAndBottom(halfExpandedOffset)
        } else if (this.hideable && sheetState == 5) {
            val fullHeight = this.parentHeight
            view.offsetTopAndBottom(fullHeight)
        } else if (sheetState == 4) {
            view.offsetTopAndBottom(collapsedTop)
        } else if (sheetState == 1 || sheetState == 2) {
            val offset = top - view.getTop()
            view.offsetTopAndBottom(offset)
        }
        var viewA = this.fixedScrollChild
        if (viewA == null) {
            viewA = if (this.nestedScrollOff) null else findScrollingChild(view)
            this.nestedScrollingChildRef = WeakReference<Any?>(viewA)
        }
        if (viewA != null && (viewA is WebNestView)) {
            nestedLocked = true
        }
        this.nestedLocked = nestedLocked
        return true
    }

    override fun onNestedPreFling(coordinatorLayout: CoordinatorLayout, child: V, view: View, velocityX: Float, velocityY: Float): Boolean {
        val weakReference = this.nestedScrollingChildRef
        return if (weakReference == null || view !== weakReference.get() || this.sheetState == 3) false else true
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        view: V,
        target: View,
        dx: Int,
        scrollDy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (type != 1) {
            val weakReference = this.nestedScrollingChildRef
            if (target === (if (weakReference != null) weakReference.get() as View? else null)) {
                val top = view.getTop()
                val index = top - scrollDy
                if (scrollDy > 0) {
                    if (index < expandedOffset()) {
                        val overshoot = top - expandedOffset()
                        consumed[1] = overshoot
                        val dy = -overshoot
                        view.offsetTopAndBottom(dy)
                        setStateInternal(3)
                    } else {
                        if (!this.draggable) {
                            return
                        }
                        consumed[1] = scrollDy
                        view.offsetTopAndBottom(-scrollDy)
                        setStateInternal(1)
                    }
                } else if (scrollDy < 0) {
                    if (!target.canScrollVertically(-1)) {
                        val collapsedOffset = this.collapsedOffset
                        if (index > collapsedOffset && !this.hideable) {
                            val newTop = top - collapsedOffset
                            consumed[1] = newTop
                            val dy = -newTop
                            view.offsetTopAndBottom(dy)
                            setStateInternal(4)
                        } else {
                            if (!this.draggable) {
                                return
                            }
                            consumed[1] = scrollDy
                            view.offsetTopAndBottom(-scrollDy)
                            setStateInternal(1)
                        }
                    } else if (this.nestedLocked) {
                        this.ignoreEvents = true
                    }
                }
                dispatchOnSlide(view.getTop())
                this.lastNestedScrollDy = scrollDy
                this.nestedScrolled = true
            }
        }
    }

    override fun onRestoreInstanceState(coordinatorLayout: CoordinatorLayout, view: V, parcelable: Parcelable) {
        try {
            val sheetState = (parcelable as SavedState).state
            if (sheetState != 1 && sheetState != 2) {
                this.sheetState = sheetState
            } else if (this.hideOnDrag) {
                this.sheetState = 3
            } else {
                this.sheetState = 4
            }
        } catch (unused: Exception) {
        }
    }

    override fun onSaveInstanceState(coordinatorLayout: CoordinatorLayout, view: V): Parcelable {
        return SavedState(this)
    }

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        view: V,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        this.lastNestedScrollDy = 0
        this.nestedScrolled = false
        return (axes and 2) != 0
    }

    override fun onStopNestedScroll(coordinatorLayout: CoordinatorLayout, view: V, target: View, type: Int) {
        val top: Int
        val topValue: Int
        val viewTop: Int
        val halfTop: Int
        val collapsedTop: Int
        val yVelocity: Float
        var state = 3
        if (view.getTop() == expandedOffset()) {
            setStateInternal(3)
            return
        }
        val weakReference = this.nestedScrollingChildRef
        if (weakReference != null && target === weakReference.get() && this.nestedScrolled) {
            val lastNestedScrollDy = this.lastNestedScrollDy
            val fitToContents = this.fitToContents
            if (lastNestedScrollDy > 0) {
                if (fitToContents) {
                    topValue = this.fitToContentsOffset
                } else {
                    val top = view.getTop()
                    val halfExpandedOffset = this.halfExpandedOffset
                    if (top > halfExpandedOffset) {
                        topValue = halfExpandedOffset
                        state = 6
                    } else {
                        topValue = 0
                    }
                }
            } else if (this.hideable) {
                val velocityTracker = this.tracker
                if (velocityTracker == null) {
                    yVelocity = 0.0f
                } else {
                    velocityTracker.computeCurrentVelocity(1000, this.maximumVelocity)
                    yVelocity = this.tracker!!.getYVelocity(this.activePointerId)
                }
                if (shouldHide(view, yVelocity)) {
                    topValue = this.parentHeight
                    state = 5
                } else if (this.hideOnDrag) {
                    if (fitToContents) {
                        topValue = this.fitToContentsOffset
                    } else {
                        topValue = 0
                    }
                } else if (this.lastNestedScrollDy == 0) {
                    viewTop = view.getTop()
                    if (fitToContents) {
                        halfTop = this.halfExpandedOffset
                        collapsedTop = this.collapsedOffset
                        if (viewTop < halfTop) {
                            if (viewTop < abs(viewTop - collapsedTop)) {
                                topValue = 0
                            } else {
                                topValue = this.halfExpandedOffset
                            }
                        } else if (abs(viewTop - halfTop) < abs(viewTop - collapsedTop)) {
                            topValue = this.halfExpandedOffset
                        } else {
                            topValue = this.collapsedOffset
                        }
                        state = 6
                    } else if (abs(viewTop - this.fitToContentsOffset) < abs(viewTop - this.collapsedOffset)) {
                        topValue = this.fitToContentsOffset
                    } else {
                        topValue = this.collapsedOffset
                    }
                    state = 4
                } else {
                    if (fitToContents) {
                        topValue = this.collapsedOffset
                    } else {
                        top = view.getTop()
                        if (abs(top - this.halfExpandedOffset) < abs(top - this.collapsedOffset)) {
                            topValue = this.halfExpandedOffset
                            state = 6
                        } else {
                            topValue = this.collapsedOffset
                        }
                    }
                    state = 4
                }
            } else if (this.hideOnDrag) {
                if (fitToContents) {
                    topValue = this.fitToContentsOffset
                } else {
                    topValue = 0
                }
            } else if (this.lastNestedScrollDy == 0) {
                viewTop = view.getTop()
                if (fitToContents) {
                    halfTop = this.halfExpandedOffset
                    collapsedTop = this.collapsedOffset
                    if (viewTop < halfTop) {
                        if (viewTop < abs(viewTop - collapsedTop)) {
                            topValue = 0
                        } else {
                            topValue = this.halfExpandedOffset
                        }
                    } else if (abs(viewTop - halfTop) < abs(viewTop - collapsedTop)) {
                        topValue = this.halfExpandedOffset
                    } else {
                        topValue = this.collapsedOffset
                    }
                    state = 6
                } else if (abs(viewTop - this.fitToContentsOffset) < abs(viewTop - this.collapsedOffset)) {
                    topValue = this.fitToContentsOffset
                } else {
                    topValue = this.collapsedOffset
                }
                state = 4
            } else {
                if (fitToContents) {
                    topValue = this.collapsedOffset
                } else {
                    top = view.getTop()
                    if (abs(top - this.halfExpandedOffset) < abs(top - this.collapsedOffset)) {
                        topValue = this.halfExpandedOffset
                        state = 6
                    } else {
                        topValue = this.collapsedOffset
                    }
                }
                state = 4
            }
            startSettling(view, state, topValue, false)
            this.nestedScrolled = false
        }
    }

    override fun onTouchEvent(
        coordinatorLayout: CoordinatorLayout,
        view: V,
        motionEvent: MotionEvent
    ): Boolean {
        if (!view.isShown()) {
            return false
        }
        val actionMasked = motionEvent.getActionMasked()
        if (this.sheetState == 1 && actionMasked == 0) {
            return true
        }
        val viewDragHelper = this.dragHelper
        if (viewDragHelper != null) {
            viewDragHelper.processTouchEvent(motionEvent)
        }
        if (actionMasked == 0) {
            this.activePointerId = -1
            val velocityTracker = this.tracker
            if (velocityTracker != null) {
                velocityTracker.recycle()
                this.tracker = null
            }
        }
        var velocityTrackerObtain = this.tracker
        if (velocityTrackerObtain == null) {
            velocityTrackerObtain = VelocityTracker.obtain()
            this.tracker = velocityTrackerObtain
        }
        velocityTrackerObtain.addMovement(motionEvent)
        if (this.dragHelper != null && actionMasked == 2 && !this.ignoreEvents) {
            val dragDistance = abs(this.initialY - motionEvent.getY())
            val dragHelper = this.dragHelper
            if (dragDistance > dragHelper!!.touchSlop) {
                dragHelper.captureChildView(view, motionEvent.getPointerId(motionEvent.getActionIndex()))
            }
        }
        return !this.ignoreEvents
    }

    fun calculatePeekHeight(): Int {
        if (!this.peekHeightAuto) {
            return 0
        }
        var parentHeight = this.parentHeight
        var parentWidth = this.parentWidth
        if (parentHeight <= parentWidth) {
            parentWidth = parentHeight
            parentHeight = parentWidth
        }
        val peekHeightMin = this.peekHeightMin
        return if (this.peekFullWidth) max(peekHeightMin, parentHeight - parentWidth) else max(peekHeightMin, parentHeight - ((parentWidth * 9) / 16))
    }

    fun dispatchOnSlide(index: Int) {
        if ((this.viewRef!!.get() as View?) != null) {
            val arrayList = this.callbacks
            if (arrayList.isEmpty()) {
                return
            }
            if (index <= this.collapsedOffset) {
                expandedOffset()
            }
            for (index2 in arrayList.indices) {
                (arrayList.get(index2) as BottomSheetCallback).javaClass
            }
        }
    }

    protected class SavedState : androidx.customview.view.AbsSavedState {
        @JvmField
        var state: Int
        @JvmField
        var peekHeight: Int
        @JvmField
        var fitToContents: Boolean
        @JvmField
        var isHideable: Boolean
        @JvmField
        var skipCollapsed: Boolean

        constructor(parcel: Parcel, classLoader: ClassLoader?) : super(parcel, classLoader) {
            this.state = parcel.readInt()
            this.peekHeight = parcel.readInt()
            this.fitToContents = parcel.readInt() == 1
            this.isHideable = parcel.readInt() == 1
            this.skipCollapsed = parcel.readInt() == 1
        }

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            super.writeToParcel(parcel, flags)
            parcel.writeInt(this.state)
            parcel.writeInt(this.peekHeight)
            parcel.writeInt(if (this.fitToContents) 1 else 0)
            parcel.writeInt(if (this.isHideable) 1 else 0)
            parcel.writeInt(if (this.skipCollapsed) 1 else 0)
        }

        internal class StateCreator : ClassLoaderCreator<SavedState?> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel, null)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls<SavedState>(size)
            }

            override fun createFromParcel(parcel: Parcel, classLoader: ClassLoader?): SavedState {
                return SavedState(parcel, classLoader)
            }
        }

        constructor(myBehaviorDialog: MyBehaviorDialog<*>) : super(AbsSavedState.EMPTY_STATE) {
            this.state = myBehaviorDialog.sheetState
            this.peekHeight = 0
            this.fitToContents = myBehaviorDialog.fitToContents
            this.isHideable = myBehaviorDialog.hideable
            this.skipCollapsed = false
        }

        companion object {
            @JvmField
            val CREATOR: Parcelable.Creator<SavedState?> = StateCreator()
        }
    }

    constructor(context: Context, attributeSet: AttributeSet?) : this(context)
}
