package com.mycompany.app.view

import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.store.ZtwoSettings
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.Filter
import android.widget.ListPopupWindow
import android.widget.ListView
import androidx.appcompat.widget.AppCompatEditText
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil.editTextValue
import com.mycompany.app.main.MainUtil.trimSpaceSlash
import com.mycompany.app.main.MainUtil.disablePersonalizedIme
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.pref.PrefZtwo
import java.util.concurrent.ExecutorService

@Suppress("deprecation")
class MyEditAuto(mainActivity: MainActivity?) : AppCompatEditText(mainActivity!!, null, 0) {
    @JvmField
    var lastFilterText: String? = null
    @JvmField
    var requestText: String? = null
    @JvmField
    var requestPos: Int = 0
    @JvmField
    var pendingText: String? = null
    @JvmField
    var pendingPos: Int = 0
    @JvmField
    var stepText: String? = null
    @JvmField
    var stepPos: Int = 0
    @JvmField
    var selectText: String? = null
    @JvmField
    var selectPos: Int = 0
    @JvmField
    var completeRunnable: Runnable?
    @JvmField
    var restoreText: String? = null
    @JvmField
    var alive: Boolean = true
    @JvmField
    var autoListener: EditAutoListener? = null
    @JvmField
    var backListener: KeyBackListener? = null
    @JvmField
    var suggestions: MutableList<String?>? = null
    @JvmField
    var typedText: String? = null
    @JvmField
    var prefixStripped: Boolean = false
    @JvmField
    var appendText: String? = null
    @JvmField
    var appendPaint: Paint? = null
    @JvmField
    var appendRect: Rect? = null
    @JvmField
    var appendRight: Int = 0
    @JvmField
    var appendHalfHeight: Int = 0
    @JvmField
    var appendTextHalf: Int = 0
    @JvmField
    var appendBaseline: Int = 0
    @JvmField
    var lastTextColor: Int = 0
    @JvmField
    var halfHeight: Int = 0
    @JvmField
    var autoExecutor: ExecutorService? = null
    @JvmField
    var hostActivity: MainActivity? = null
    @JvmField
    var dropDown: ListPopupWindow? = null
    @JvmField
    var dropFilter: Filter? = null

    internal inner class NoOpClick : OnClickListener {
        override fun onClick(view: View?) {
        }
    }

    internal inner class SameObjectTextWatcher : TextWatcher {
        @JvmField
        var wasShowing: Boolean = false

        override fun afterTextChanged(editable: Editable?) {
            val myEditAuto = this@MyEditAuto
            if (myEditAuto.autoListener != null) {
                val filterText = if (editable != null) editable.toString() else null
                if (myEditAuto.autoListener!!.onText(filterText)) {
                    return
                }
                if (myEditAuto.dropDown != null && this.wasShowing && !myEditAuto.isDropDownShowing()) {
                    myEditAuto.lastFilterText = null
                    return
                }
                if (ZtwoSettings.autoComp.value) {
                    if (MainUtil.sameObject(filterText, myEditAuto.lastFilterText)) {
                        return
                    } else {
                        myEditAuto.lastFilterText = filterText
                    }
                }
                val filter = myEditAuto.dropFilter
                if (filter != null) {
                    filter.filter(editable)
                }
            }
        }

        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            val myEditAuto = this@MyEditAuto
            if (myEditAuto.dropDown != null) {
                this.wasShowing = myEditAuto.isDropDownShowing()
            }
        }

        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
        }
    }

    internal inner class ApplyPendingTextTask : Runnable {
        override fun run() {
            this@MyEditAuto.applyPendingText()
        }
    }

    internal inner class SameObjectTask : Runnable {
        override fun run() {
            val myEditAuto = this@MyEditAuto
            val stepText = myEditAuto.stepText
            val stepPos = myEditAuto.stepPos
            myEditAuto.stepText = null
            val requestText = myEditAuto.requestText
            if (myEditAuto.alive && myEditAuto.requestPos == stepPos && MainUtil.sameObject(requestText, stepText)) {
                myEditAuto.setText(stepText)
                if (stepPos < 0 || TextUtils.isEmpty(stepText)) {
                    return
                }
                myEditAuto.selectText = stepText
                myEditAuto.selectPos = stepPos
                myEditAuto.post(myEditAuto.SetSelectionTask())
            }
        }
    }

    internal inner class SetSelectionTask : Runnable {
        override fun run() {
            val myEditAuto = this@MyEditAuto
            val selectText = myEditAuto.selectText
            val selectPos = myEditAuto.selectPos
            myEditAuto.selectText = null
            val requestText = myEditAuto.requestText
            myEditAuto.requestText = null
            if (myEditAuto.alive && myEditAuto.requestPos == selectPos && MainUtil.sameObject(
                    requestText,
                    selectText
                ) && selectPos >= 0 && !TextUtils.isEmpty(selectText)
            ) {
                try {
                    myEditAuto.setSelection(selectPos)
                } catch (unused: Exception) {
                }
            }
        }
    }

    internal inner class SetTextAppendTask : Runnable {

        override fun run() {
            var shortText: String?
            val myEditAuto = this@MyEditAuto
            val typedText = myEditAuto.typedText
            if (TextUtils.isEmpty(typedText)) {
                myEditAuto.setTextAppend(null)
                return
            }
            val list: MutableList<String?>? = (myEditAuto.suggestions)
            if (list == null || list.isEmpty()) {
                myEditAuto.setTextAppend(null)
                return
            }
            for (strSubstring in list) {
                if (myEditAuto.suggestions == null || (typedText != myEditAuto.typedText) || TextUtils.isEmpty(strSubstring)) {
                    break
                }
                myEditAuto.prefixStripped = false
                val length = if (strSubstring == null) 0 else strSubstring.length
                if (strSubstring == null || length == 0) {
                    shortText = strSubstring
                } else if (length > 4 && strSubstring.startsWith("www.")) {
                    myEditAuto.prefixStripped = true
                    shortText = strSubstring.substring(4)
                } else if (length > 2 && strSubstring.startsWith(".", 1)) {
                    myEditAuto.prefixStripped = true
                    shortText = strSubstring.substring(2)
                } else if (length <= 7 || !strSubstring.startsWith("mobile.")) {
                    shortText = strSubstring
                } else {
                    myEditAuto.prefixStripped = true
                    shortText = strSubstring.substring(7)
                }
                if (!TextUtils.isEmpty(shortText) && shortText!!.startsWith(typedText!!)) {
                    myEditAuto.setTextAppend(shortText)
                    return
                } else if (myEditAuto.prefixStripped && strSubstring!!.startsWith(typedText!!)) {
                    myEditAuto.setTextAppend(strSubstring)
                    return
                }
            }
            myEditAuto.setTextAppend(null)
        }
    }

    internal inner class SetTextTask : Runnable {

        override fun run() {
            val myEditAuto = this@MyEditAuto
            try {
                val restoreText = myEditAuto.restoreText
                myEditAuto.restoreText = null
                val length = if (restoreText == null) 0 else restoreText.length
                if (restoreText == null || length == 0 || (restoreText != myEditAuto.typedText)) {
                    return
                }
                myEditAuto.setText(restoreText)
                myEditAuto.setSelection(length)
            } catch (unused: Exception) {
            }
        }
    }

    interface EditAutoListener {
        fun onText(input: String?): Boolean
    }

    interface KeyBackListener {
        fun onBackKey(isShown: Boolean, isEditing: Boolean): Boolean
    }

    init {
        this.completeRunnable = SetTextAppendTask()
        disablePersonalizedIme(this)
        val inputType = getInputType()
        if ((inputType and 15) == 1) {
            setRawInputType(inputType or 65536)
        }
        setFocusable(true)
        setOnClickListener(NoOpClick())
        addTextChangedListener(SameObjectTextWatcher())
    }

    // landscape: keep the keyboard below the app instead of Android's full-screen typing box, which hides the
    // suggestions and the page (Compose fields do the same by default)
    override fun onCreateInputConnection(outAttrs: android.view.inputmethod.EditorInfo): android.view.inputmethod.InputConnection? {
        val connection = super.onCreateInputConnection(outAttrs)
        outAttrs.imeOptions = outAttrs.imeOptions or android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT_UI or
            android.view.inputmethod.EditorInfo.IME_FLAG_NO_FULLSCREEN
        return connection
    }

    fun setTextAppend(appendText: String?) {
        if (TextUtils.isEmpty(appendText) || TextUtils.isEmpty(this.typedText)) {
            if (this.appendText != null) {
                this.appendText = null
                postInvalidate()
                return
            }
            return
        }
        val paint = getPaint()
        if (paint != null) {
            if (this.appendPaint == null) {
                val appendPaint = Paint()
                this.appendPaint = appendPaint
                appendPaint.setAntiAlias(true)
                this.appendPaint!!.setColor(-2137994503)
            }
            if (this.appendRect == null) {
                this.appendRect = Rect()
            }
            val typedText = this.typedText
            paint.getTextBounds(typedText, 0, typedText!!.length, this.appendRect)
            val right = this.appendRect!!.right
            if (appendText != this.appendText) {
                this.appendText = appendText
                if (paint.getColor() != getCurrentTextColor()) {
                    paint.setColor(getCurrentTextColor())
                }
                paint.getTextBounds(appendText, 0, appendText!!.length, this.appendRect)
                this.appendRight = this.appendRect!!.right
            }
            val rect = this.appendRect
            rect!!.left = right
            rect.right = this.appendRight
            if (this.appendHalfHeight == 0) {
                this.appendHalfHeight = MainApp.dp16 - MainApp.dp4
            }
            val textCenter = Math.round((paint.ascent() + paint.descent()) / 2.0f)
            this.appendTextHalf = textCenter
            val appendRect = this.appendRect
            val top = if (appendRect != null) this.halfHeight else 0
            if (appendRect != null && top != 0) {
                val appendHalfHeight = this.appendHalfHeight
                appendRect.top = top - appendHalfHeight
                appendRect.bottom = appendHalfHeight + top
                this.appendBaseline = top - textCenter
            }
            postInvalidate()
        }
    }

    fun dismissDropDown() {
        if (this.dropDown != null) {
            val mainActivity = this.hostActivity
            if (mainActivity != null) {
                mainActivity.activeEditAuto = null
            }
            if (isDropDownShowing()) {
                this.dropDown!!.dismiss()
            }
        }
    }

    fun clearSuggestions() {
        this.suggestions = null
        this.typedText = null
        setTextAppend(null)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val typedText: String? = if (event!!.getActionMasked() == 0) this.appendText else null
        if (event.getActionMasked() == 0 && typedText != null) {
            setTextAt(-1, typedText)
        }
        try {
            return super.dispatchTouchEvent(event)
        } catch (unused: Exception) {
            return true
        }
    }

    fun isDropDownShowing(): Boolean {
        val listPopupWindow = this.dropDown
        if (listPopupWindow == null) {
            return false
        }
        return listPopupWindow.isShowing()
    }

    fun release() {
        this.alive = false
        this.autoListener = null
        this.backListener = null
        this.suggestions = null
        this.typedText = null
        this.appendText = null
        this.appendPaint = null
        this.appendRect = null
        this.autoExecutor = null
        this.hostActivity = null
        this.dropDown = null
        this.dropFilter = null
        this.lastFilterText = null
    }

    fun updateComplete(typedText: String?): Boolean {
        val list = this.suggestions
        if (list != null && !list.isEmpty()) {
            if (this.appendText != null && this.typedText != null) {
                val length = if (typedText == null) 0 else typedText.length
                if (length < this.typedText!!.length && (length <= 0 || this.typedText!!.startsWith(typedText!!))) {
                    this.appendText = null
                    this.restoreText = this.typedText
                    post(SetTextTask())
                    return true
                }
            }
            this.restoreText = null
            if (TextUtils.isEmpty(typedText)) {
                this.typedText = ""
                setTextAppend(null)
                return false
            }
            if (this.typedText == null) {
                this.typedText = ""
            }
            if (typedText!!.length < this.typedText!!.length) {
                this.typedText = typedText
                setTextAppend(null)
                return false
            }
            if (typedText != this.typedText) {
                this.typedText = typedText
                val runnable = this.completeRunnable
                var executorServiceH = this.autoExecutor
                if (executorServiceH == null || executorServiceH.isShutdown() || executorServiceH.isTerminated()) {
                    executorServiceH = MainApp.executor(getContext())
                    if (executorServiceH != null) {
                        this.autoExecutor = executorServiceH
                        try {
                            executorServiceH.execute(runnable)
                        } catch (unused: Exception) {
                        }
                    }
                } else {
                    executorServiceH.execute(runnable)
                }
            }
        }
        return false
    }

    override fun getAccessibilityClassName(): CharSequence {
        return MyEditAuto::class.java.getName()
    }

    val autoText: String?
        get() = if (!TextUtils.isEmpty(this.appendText)) trimSpaceSlash(this.appendText) else trimSpaceSlash(
            editTextValue(
                this,
                false
            )
        )

    var dropDownHeight: Int
        get() {
            val listPopupWindow = this.dropDown
            if (listPopupWindow == null) {
                return 0
            }
            return listPopupWindow.getHeight()
        }
        set(i) {
            val listPopupWindow = this.dropDown
            if (listPopupWindow == null) {
                return
            }
            listPopupWindow.setHeight(i)
        }

    var dropDownHorizontalOffset: Int
        get() {
            val listPopupWindow = this.dropDown
            if (listPopupWindow == null) {
                return 0
            }
            return listPopupWindow.getHorizontalOffset()
        }
        set(i) {
            val listPopupWindow = this.dropDown
            if (listPopupWindow == null) {
                return
            }
            listPopupWindow.setHorizontalOffset(i)
        }

    var dropDownWidth: Int
        get() {
            val listPopupWindow = this.dropDown
            if (listPopupWindow == null) {
                return 0
            }
            return listPopupWindow.getWidth()
        }
        set(i) {
            val listPopupWindow = this.dropDown
            if (listPopupWindow == null) {
                return
            }
            listPopupWindow.setWidth(i)
        }

    val selected: String?
        get() {
            val text = editTextValue(this, false)
            if (TextUtils.isEmpty(text)) {
                return null
            }
            val selectionStart = getSelectionStart()
            val selectionEnd = getSelectionEnd()
            return if (selectionStart < 0 || selectionStart >= selectionEnd || selectionEnd > text!!.length) text else text.substring(
                selectionStart,
                selectionEnd
            )
        }

    fun applyPendingText() {
        val pendingText = this.pendingText
        val pendingPos = this.pendingPos
        this.pendingText = null
        val requestText = this.requestText
        if (this.alive && this.requestPos == pendingPos && MainUtil.sameObject(requestText, pendingText)) {
            setTextAppend(null)
            this.stepText = pendingText
            this.stepPos = pendingPos
            post(SameObjectTask())
        }
    }

    fun setTextAt(requestPos: Int, typedText: String?) {
        this.typedText = typedText
        this.requestText = typedText
        this.requestPos = requestPos
        this.pendingText = typedText
        this.pendingPos = requestPos
        post(ApplyPendingTextTask())
    }

    fun showDropDown() {
        val listPopupWindow = this.dropDown
        if (listPopupWindow != null) {
            val mainActivity = this.hostActivity
            if (mainActivity != null) {
                mainActivity.activeEditAuto = this
            }
            listPopupWindow.setInputMethodMode(1)
            this.dropDown!!.show()
        }
    }

    override fun onDetachedFromWindow() {
        dismissDropDown()
        super.onDetachedFromWindow()
    }

    public override fun onDisplayHint(hint: Int) {
        super.onDisplayHint(hint)
        if (hint == 4) {
            dismissDropDown()
        }
    }

    public override fun onDraw(canvas: Canvas) {
        val paint: Paint? = if (this.appendText != null) this.appendPaint else null
        val rect: Rect? = if (this.appendText != null && paint != null) this.appendRect else null
        if (this.appendText != null && paint != null && rect != null) {
            try {
                canvas.drawRect(rect, paint)
                canvas.drawText(this.appendText!!, 0.0f, this.appendBaseline.toFloat(), getPaint())
                return
            } catch (unused: Exception) {
            }
        }
        super.onDraw(canvas)
    }

    public override fun onFocusChanged(gainFocus: Boolean, direction: Int, rect: Rect?) {
        super.onFocusChanged(gainFocus, direction, rect)
        if (isTemporarilyDetached() || gainFocus) {
            return
        }
        dismissDropDown()
    }

    override fun onKeyDown(keyCode: Int, keyEvent: KeyEvent?): Boolean {
        val dropDownShowing = isDropDownShowing()
        if (dropDownShowing && this.dropDown!!.onKeyDown(keyCode, keyEvent!!)) {
            return true
        }
        if (keyCode == 61 && dropDownShowing && keyEvent!!.hasNoModifiers()) {
            return true
        }
        val handled = super.onKeyDown(keyCode, keyEvent)
        if (handled && dropDownShowing) {
            this.dropDown!!.clearListSelection()
        }
        return handled
    }

    override fun onKeyPreIme(keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (this.backListener != null && keyCode == 4) {
            if (this.backListener!!.onBackKey(keyEvent!!.getAction() == 1, false)) {
                return true
            }
        }
        if ((keyCode == 4 || keyCode == 111) && isDropDownShowing()) {
            val action = keyEvent!!.getAction()
            if (action == 0) {
                if (keyEvent.getRepeatCount() == 0) {
                    val keyDispatcherState = getKeyDispatcherState()
                    if (keyDispatcherState != null) {
                        keyDispatcherState.startTracking(keyEvent, this)
                    }
                    return true
                }
            } else if (action == 1) {
                val keyDispatcherState = getKeyDispatcherState()
                if (keyDispatcherState != null) {
                    keyDispatcherState.handleUpEvent(keyEvent)
                }
                if (keyEvent.isTracking() && !keyEvent.isCanceled()) {
                    dismissDropDown()
                    return true
                }
            }
        }
        return super.onKeyPreIme(keyCode, keyEvent)
    }

    override fun onKeyUp(keyCode: Int, keyEvent: KeyEvent?): Boolean {
        val dropDownShowing = isDropDownShowing()
        if (dropDownShowing && this.dropDown!!.onKeyUp(keyCode, keyEvent!!) && (keyCode == 23 || keyCode == 61 || keyCode == 66 || keyCode == 160)) {
            if (keyEvent.hasNoModifiers()) {
                dismissDropDown()
            }
            return true
        }
        if (keyCode != 61 || !dropDownShowing || !keyEvent!!.hasNoModifiers()) {
            return super.onKeyUp(keyCode, keyEvent)
        }
        dismissDropDown()
        return true
    }

    public override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        val halfHeight = Math.round(height / 2.0f)
        this.halfHeight = halfHeight
        val rect = this.appendRect
        if (rect == null || halfHeight == 0) {
            return
        }
        val appendHalfHeight = this.appendHalfHeight
        rect.top = halfHeight - appendHalfHeight
        rect.bottom = appendHalfHeight + halfHeight
        this.appendBaseline = halfHeight - this.appendTextHalf
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            return
        }
        dismissDropDown()
    }

    fun setAutoList(list: MutableList<String?>?) {
        this.suggestions = list
    }

    fun setDropDownAnchor(view: View?) {
        val listPopupWindow = this.dropDown
        if (listPopupWindow == null) {
            return
        }
        if (view != null) {
            listPopupWindow.setAnchorView(view)
        } else {
            listPopupWindow.setAnchorView(this)
        }
    }

    fun setDropDownBackgroundDrawable(drawable: Drawable?) {
        val listPopupWindow = this.dropDown
        if (listPopupWindow == null) {
            return
        }
        listPopupWindow.setBackgroundDrawable(drawable)
    }

    fun setDropDownFilter(filter: Filter?) {
        this.dropFilter = filter
        this.lastFilterText = null
    }

    fun setEditAutoListener(editAutoListener: EditAutoListener?) {
        this.autoListener = editAutoListener
    }

    public override fun setFrame(left: Int, top: Int, right: Int, bottom: Int): Boolean {
        val frame = super.setFrame(left, top, right, bottom)
        if (isDropDownShowing()) {
            showDropDown()
        }
        return frame
    }

    fun setOldKeyListener(keyBackListener: KeyBackListener?) {
        if (MainConst.isApi33) {
            return
        }
        this.backListener = keyBackListener
    }

    override fun setTextColor(color: Int) {
        if (this.lastTextColor == color) {
            return
        }
        this.lastTextColor = color
        super.setTextColor(color)
    }

    companion object {
    }
}
