package com.mycompany.app.view

import android.graphics.Bitmap
import com.mycompany.app.ui.state.AppState
import android.view.View
import android.widget.PopupWindow
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.isKoreanLocale
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.web.WebSearch

/**
 * The search-engine menu under the address editor's engine icon: the built-in engines that are switched on (in the order
 * the settings use), then the custom ones with their saved icon or colour dot. Compose now, in an anchored
 * [PopupWindow]; constructor, [MyEngineListener] and the [MyPopupWrap] contract are as before: [dismissNow] releases the menu and
 * tells the listener (the activity forgets the menu and goes back to normal), [dismiss] closes it; picking an engine reports
 * `b(index, colour, url template)` (built-in engines have index < 100, custom ones 100 + id) and then closes.
 * [view] was the window the old popup covered and [dark] the bar's colour flag; neither is needed for the Material popup.
 */
class MyEngineMenu(
    mainActivity: MainActivity?,
    view: View?,
    view2: View?,
    dark: Boolean,
    myEngineListener: MyEngineListener?,
) : MyPopupWrap() {
    interface MyEngineListener {
        fun onClose()
        fun onPick(engine: Int, color: Int, url: String?)
    }

    /** The popup's content view: a [ComposeOverlay] so the popup window gets its own Compose owners. */
    private class Content(context: android.content.Context) : ComposeOverlay(context) {
        fun set(content: @androidx.compose.runtime.Composable () -> Unit) = setPage({ AppState.darkUi }, content)
    }

    private class Entry(val index: Int, val name: String?, val nameRes: Int, val iconRes: Int, val icon: ImageBitmap?)

    private var activity: MainActivity? = mainActivity
    private var listener: MyEngineListener? = myEngineListener
    private var popup: PopupWindow? = null
    private var closing = false

    init {
        val host = mainActivity
        val anchor = view2
        if (host == null || anchor == null) {
            dismissNow()
        } else {
            Thread {
                val entries = loadEntries(host)
                anchor.post { show(host, anchor, entries) }
            }.start()
        }
    }

    /** Built-in (enabled) engines in the settings order, then custom engines with their icon. Off the main thread. */
    private fun loadEntries(host: MainActivity): List<Entry> {
        val list = ArrayList<Entry>()
        try {
            val reordered = isKoreanLocale()
            val enabled = MainUtil.zoneFlags()
            for (k in 0 until MainConst.engineNames.size) {
                val engine = if (reordered) MainConst.engineOrderKo[k] else k
                if (engine >= 0 && engine < enabled.size && enabled[engine]) {
                    list.add(Entry(engine, null, MainConst.engineNames[engine], MainConst.engineIcons[engine], null))
                }
            }
            val custom = DbBookSearch.engines(host)
            if (custom != null) {
                for (sch2 in custom) {
                    val sch = sch2 ?: continue
                    var res = 0
                    var bitmap: Bitmap? = null
                    if (sch.color != 0) {
                        res = DbBookSearch.iconRes(sch.color)
                    } else {
                        var icon = DbBookSearch.cachedIcon(sch.engineUrl)
                        if (!MainUtil.isBitmapOk(icon)) {
                            icon = DbBookSearch.iconById(host, sch.id)
                            if (MainUtil.isBitmapOk(icon)) DbBookSearch.cacheEngineIcon(sch.engineUrl, icon)
                        }
                        if (MainUtil.isBitmapOk(icon)) bitmap = icon else res = DbBookSearch.iconRes(-65536)
                    }
                    list.add(Entry(sch.id.toInt() + 100, sch.name, 0, res, bitmap?.asImageBitmap()))
                }
            }
        } catch (_: Exception) {
        }
        return list
    }

    private fun show(host: MainActivity, anchor: View, entries: List<Entry>) {
        if (activity == null) return
        try {
            val content = Content(host)
            content.set {
                    Surface(
                        shape = RoundedCornerShape(12.dp), tonalElevation = 3.dp, shadowElevation = 8.dp,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        androidx.compose.foundation.layout.Column(
                            Modifier.widthIn(min = 200.dp, max = 300.dp).heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
                        ) {
                            for (entry in entries) {
                                Row(
                                    Modifier.fillMaxWidth().clickable { pick(entry.index) }.padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    if (entry.icon != null) {
                                        Image(entry.icon, contentDescription = null, modifier = Modifier.size(24.dp))
                                    } else if (entry.index < 10) {
                                        // the glyph (dark ink only) tinted for the menu until the site icon is there
                                        com.mycompany.app.ui.components.EngineLogo(entry.index, 24.dp) {
                                            androidx.compose.material3.Icon(painterResource(entry.iconRes), contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    } else if (entry.iconRes != 0) {
                                        Image(painterResource(entry.iconRes), contentDescription = null, modifier = Modifier.size(24.dp))
                                    }
                                    Text(
                                        entry.name ?: stringResource(entry.nameRes), style = MaterialTheme.typography.bodyLarge,
                                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(start = 16.dp),
                                    )
                                }
                            }
                        }
                    }
            }
            val window = PopupWindow(content, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, true)
            window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(0))
            window.setOnDismissListener { dismissNow() }
            popup = window
            window.showAsDropDown(anchor, 0, 0)
        } catch (_: Exception) {
            dismissNow()
        }
    }

    private fun pick(index: Int) {
        val listenerNow = listener
        if (listenerNow != null) {
            if (index < 100) {
                listenerNow.onPick(index, 0, null)
            } else {
                val item = WebSearch.instance().engineById((index - 100).toLong())
                if (item != null) listenerNow.onPick(index, item.color, item.engineUrl) else listenerNow.onPick(4, 0, null)
            }
        }
        dismiss()
    }

    /** Releases the menu and tells the listener, once. */
    override fun dismissNow() {
        val host = activity
        activity = null
        if (host == null) return
        val window = popup
        popup = null
        try { window?.dismiss() } catch (_: Exception) {}
        val listenerNow = listener
        listener = null
        listenerNow?.onClose()
    }

    /** Closes the menu (the old one animated out first). */
    override fun dismiss() {
        if (closing) return
        closing = true
        dismissNow()
    }
}
