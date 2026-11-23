package com.mycompany.app.dialog

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.dialog.DialogEditText.EditTextListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetButtons
import com.mycompany.app.web.WebSearchAdapter.SearchItem
import com.mycompany.app.web.WebSearchAdapter.WebSearchListener
import com.mycompany.app.web.WebSearchAdapter2
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import java.net.URLEncoder

/**
 * News search (news page > search): a "News title" field with the user's search history as suggestions (the old
 * `AppCompatAutoCompleteTextView` + [WebSearchAdapter2], kept as a view because the suggestion list is that adapter's
 * dropdown) and Search; keyboard search, the button or a suggestion pass the URL-encoded words to
 * [EditTextListener.onText] (empty = "Empty"). Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogNewsSearch(activity: Activity?, view: View?, editTextListener: EditTextListener?) : ComposeBottomDialog(activity) {
    @JvmField var appContext: Context? = context
    @JvmField var textListener: EditTextListener? = editTextListener
    @JvmField var anchorView: View? = view
    /** The field's frame (dropdown anchor; also the top used for the suggestion height). */
    @JvmField var frameView: FrameLayout? = null
    @JvmField var searchField: AppCompatAutoCompleteTextView? = null
    @JvmField var submitting: Boolean = false
    @JvmField var searchAdapter: WebSearchAdapter2? = null

    init {
        setSheet {
            val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
            Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
                Text(stringResource(AppR.string.news_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                AndroidView(
                    factory = { ctx -> build(ctx, textColor) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                )
                Box(Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.primary))
            }
            SheetButtons({ go() }, mainLabel = AppR.string.search_url)
        }
        show()
    }

    private fun build(ctx: Context, textColor: Int): View {
        val frame = FrameLayout(ctx)
        frame.id = AppR.id.item_frame_view
        val edit = AppCompatAutoCompleteTextView(ctx, null)
        edit.gravity = 16
        edit.setSingleLine(true)
        edit.textDirection = 3
        edit.setTextSize(1, 16.0f)
        edit.setTextCursorDrawable(AppR.drawable.edit_cursor)
        edit.imeOptions = 268435456
        edit.background = null
        edit.setPadding(0, 0, 0, 0)
        edit.setTextColor(textColor)
        edit.threshold = 1
        edit.setDropDownAnchor(AppR.id.item_frame_view)
        // the field's real width (MATCH_PARENT inside Compose resolved to a huge popup that could not be drawn)
        frame.addOnLayoutChangeListener { v, _, _, _, _, _, _, _, _ -> if (v.width > 0) edit.dropDownWidth = v.width }
        edit.dropDownHeight = -2
        edit.setDropDownBackgroundDrawable(ColorDrawable(16777216))
        edit.inputType = 161
        MainUtil.disablePersonalizedIme(edit)
        edit.setSelectAllOnFocus(true)
        edit.setOnEditorActionListener { _, _, _ -> go(); true }
        frame.addView(edit, ViewGroup.LayoutParams(-1, -1))
        frameView = frame
        searchField = edit
        // focus + keyboard, then the suggestions
        edit.post {
            val field = searchField ?: return@post
            field.requestFocus()
            field.postDelayed({ val context = appContext; val fieldValue = searchField; if (context != null && fieldValue != null) MainUtil.openKeyboard(context, fieldValue) }, 200L)
            val adapter = WebSearchAdapter2(appContext!!, null, null, null, true, 2, ViewScreenYStep())
            searchAdapter = adapter
            field.setAdapter<WebSearchAdapter2?>(adapter)
        }
        return frame
    }

    /** Search button / keyboard. */
    private fun go() {
        val field = searchField
        if (field == null || submitting) return
        submitting = true
        field.post {
            submit(false, null)
            submitting = false
        }
    }

    internal inner class ViewScreenYStep : WebSearchListener {
        override fun onItemsShown(count: Int, isEmpty: Boolean) {
            val field = if (count < 0) null else searchField
            field?.post { searchField?.setListSelection(count) }
        }

        override fun headerCount(): Int {
            val frame = frameView ?: return 0
            return MainUtil.viewScreenY(frame) - MainUtil.viewScreenY(anchorView)
        }

        override fun loadRecent() {}

        override fun showClip(): Boolean = false

        override fun setClipText(clipText: String?) {}

        override fun setQuery(cursor: Int, query: String?) {
            val field = searchField ?: return
            field.setText(query)
            try { field.setSelection(cursor) } catch (unused: Exception) { }
        }

        override fun onItemClick(searchItem: SearchItem?) {
            val field = searchField
            if (field == null || submitting) return
            submitting = true
            field.post {
                submit(true, searchItem)
                submitting = false
            }
        }
    }

    fun submit(direct: Boolean, searchItem: SearchItem?) {
        var query: String?
        val field = searchField
        if (field == null || textListener == null) return
        if (!direct) {
            query = MainUtil.editTextValue(field, true)
        } else {
            if (searchItem == null) return
            val itemKind = searchItem.itemKind
            if (itemKind.toInt() == 3) {
                dismiss()
                return
            } else if (itemKind.toInt() != 0) {
                return
            } else {
                query = searchItem.title
            }
        }
        if (TextUtils.isEmpty(query)) {
            MainUtil.showToast(appContext, AppR.string.empty)
        } else {
            try {
                query = URLEncoder.encode(query, "UTF-8")
            } catch (unused: Exception) {
            }
            textListener!!.onText(query)
        }
    }

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        searchAdapter?.release()
        searchAdapter = null
        appContext = null
        textListener = null
        anchorView = null
        frameView = null
        searchField = null
        super.dismiss()
    }
}
