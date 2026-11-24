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
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.pref.store.ZtwoSettings
import com.mycompany.app.web.SuggestEngines
import androidx.appcompat.widget.AppCompatAutoCompleteTextView
import java.net.URLEncoder

/**
 * News search (news page > search): a "News title" field with search suggestions from the chosen search engine, as
 * in the address bar ([NewsSuggestAdapter]; none while search suggestions are off), kept as an
 * `AppCompatAutoCompleteTextView` because the suggestions are its dropdown, and Search; keyboard search, the button
 * or a suggestion pass the URL-encoded words to [EditTextListener.onText] (empty = "Empty"). Compose (on
 * [ComposeBottomDialog]); same constructor, shows itself.
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
    @JvmField var searchAdapter: NewsSuggestAdapter? = null

    init {
        setSheet {
            val textColor = MaterialTheme.colorScheme.onSurface.toArgb()
            val listColor = MaterialTheme.colorScheme.surfaceContainerHigh.toArgb()
            Column(Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 16.dp)) {
                Text(stringResource(AppR.string.news_title), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                AndroidView(
                    factory = { ctx -> build(ctx, textColor, listColor) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                )
                Box(Modifier.fillMaxWidth().height(2.dp).background(MaterialTheme.colorScheme.primary))
            }
            SheetButtons({ go() }, mainLabel = AppR.string.search_url)
        }
        show()
    }

    private fun build(ctx: Context, textColor: Int, listColor: Int): View {
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
        edit.setDropDownBackgroundDrawable(ColorDrawable(listColor))
        edit.inputType = 161
        MainUtil.disablePersonalizedIme(edit)
        edit.setSelectAllOnFocus(true)
        edit.setOnEditorActionListener { _, _, _ -> go(); true }
        edit.setOnItemClickListener { _, _, position, _ -> pick(searchAdapter?.getItem(position)) }
        frame.addView(edit, ViewGroup.LayoutParams(-1, -1))
        frameView = frame
        searchField = edit
        // focus + keyboard, then the suggestions
        edit.post {
            val field = searchField ?: return@post
            field.requestFocus()
            field.postDelayed({ val context = appContext; val fieldValue = searchField; if (context != null && fieldValue != null) MainUtil.openKeyboard(context, fieldValue) }, 200L)
            val adapter = NewsSuggestAdapter(appContext!!, textColor)
            searchAdapter = adapter
            field.setAdapter(adapter)
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

    /** A suggestion was tapped: search the news for it. */
    private fun pick(title: String?) {
        val field = searchField
        if (field == null || submitting || title.isNullOrEmpty()) return
        submitting = true
        field.post {
            submit(true, title)
            submitting = false
        }
    }

    /** Searches the field's text, or [picked] (a tapped suggestion) when [direct]. */
    fun submit(direct: Boolean, picked: String?) {
        var query: String?
        val field = searchField
        if (field == null || textListener == null) return
        query = if (direct) picked else MainUtil.editTextValue(field, true)
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
        searchAdapter = null
        appContext = null
        textListener = null
        anchorView = null
        frameView = null
        searchField = null
        super.dismiss()
    }
}

/**
 * The news search's suggestions: what the chosen search engine suggests for the typed words (the same services as the
 * address bar, [SuggestEngines]), search terms only; nothing while search suggestions are off or the engine has none.
 * Asked on the filter's own thread, 3 s at most.
 */
class NewsSuggestAdapter(context: Context, private val textColor: Int) : android.widget.ArrayAdapter<String>(context, 0) {
    private val shown = ArrayList<String>()

    override fun getCount(): Int = shown.size

    override fun getItem(position: Int): String? = shown.getOrNull(position)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val row = convertView as? android.widget.TextView ?: android.widget.TextView(context).apply {
            setTextColor(textColor)
            setTextSize(1, 16.0f)
            setSingleLine(true)
            ellipsize = TextUtils.TruncateAt.END
            val side = Math.round(16 * resources.displayMetrics.density)
            val vertical = Math.round(12 * resources.displayMetrics.density)
            setPadding(side, vertical, side, vertical)
        }
        row.text = getItem(position)
        return row
    }

    private val suggestFilter = object : android.widget.Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val list = fetch(constraint?.toString()?.trim())
            return FilterResults().apply { values = list; count = list.size }
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            shown.clear()
            @Suppress("UNCHECKED_CAST")
            (results?.values as? List<String>)?.let { shown.addAll(it) }
            if (shown.isEmpty()) notifyDataSetInvalidated() else notifyDataSetChanged()
        }

        override fun convertResultToString(resultValue: Any?): CharSequence = resultValue as? String ?: ""
    }

    override fun getFilter(): android.widget.Filter = suggestFilter

    private fun fetch(query: String?): List<String> {
        if (query.isNullOrEmpty() || BrowserSettings.sugEngine.value == 0) return emptyList()
        val url = SuggestEngines.url(ZtwoSettings.searchType.value, query, MainUtil.appLocaleTag()) ?: return emptyList()
        val connection = MainUtil.openHttpConnection(3000, 3000, context, url, null, false) ?: return emptyList()
        return try {
            val response = MainUtil.readTextFile(null, connection.inputStream, false)
            // web site suggestions (Google) have no place in a news search
            SuggestEngines.parse(response, query).filter { it.url == null }.map { it.title }
        } catch (_: Exception) {
            emptyList()
        } finally {
            try { connection.disconnect() } catch (_: Exception) {}
        }
    }
}
