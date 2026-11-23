package com.mycompany.app.dialog

import android.app.Activity
import com.mycompany.app.ui.state.AppState
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView.ScaleType
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.remember
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.view.MyRoundImage

/**
 * Delete confirmation for one item: a thumbnail + title row that the caller fills in ([DelItemListener.bindHead] gets the image and
 * text views once) and a Delete row ([DelItemListener.onDelete] runs after the sheet closes). Compose now (on
 * [ComposeBottomDialog]); the two views stay legacy views hosted in the sheet, so existing listeners work unchanged.
 */
class DialogDeleteItem(activity: Activity?, delItemListener: DelItemListener?) : ComposeBottomDialog(activity) {
    private var listener: DelItemListener? = delItemListener

    interface DelItemListener {
        fun onDelete()

        fun bindHead(myRoundImage: MyRoundImage?, appCompatTextView: AppCompatTextView?)
    }

    init {
        setSheet {
            val ctx = LocalContext.current
            val dark = AppState.darkUi
            val textColor = if (dark) -328966 else -16777216
            val views = remember {
                val image = MyRoundImage(ctx)
                image.setScaleType(ScaleType.CENTER_CROP)
                image.setCircleRadius(MainApp.dp40 / 2.0f)
                val text: AppCompatTextView = com.mycompany.app.outline.LegacyViews.textViewMaxLines(ctx, null, 2)
                text.setEllipsize(android.text.TextUtils.TruncateAt.END)
                text.setTextSize(1, 16f)
                text.setTextColor(textColor)
                listener?.bindHead(image, text)
                Pair(image, text)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                AndroidView(factory = {
                    FrameLayout(it).apply { addView(views.first, FrameLayout.LayoutParams(MainApp.dp40, MainApp.dp40, Gravity.CENTER)) }
                }, modifier = Modifier.size(48.dp))
                AndroidView(factory = {
                    (views.second.parent as? ViewGroup)?.removeView(views.second); views.second
                }, modifier = Modifier.weight(1f).padding(start = 16.dp))
            }
            Text(
                stringResource(R.string.delete), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().clickable {
                    val l = listener
                    dismiss()
                    l?.onDelete()
                }.padding(horizontal = 24.dp, vertical = 16.dp),
            )
        }
        show()
    }

    /** The old dialog showed a busy state while deleting; the sheet closes at once, so this is kept only for callers. */
    @Suppress("UNUSED_PARAMETER")
    fun applyBusy(busy: Boolean) {}

    override fun dismiss() {
        listener = null
        super.dismiss()
    }
}
