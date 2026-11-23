package com.mycompany.app.outline

import android.content.Context
import com.mycompany.app.ui.settings.SettingItem
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.view.MyManagerLinear
import com.mycompany.app.view.MyRecyclerView

/**
 * Small builders shared by the remaining View-based screens.
 */
object LegacyViews {
    /** Sets [textRes] on [view] and returns new `LinearLayout` params of [width] × [height]. */
    @JvmStatic fun setTextLinearParams(view: AppCompatTextView?, textRes: Int, width: Int, height: Int): LinearLayout.LayoutParams {
        view!!.setText(textRes)
        return LinearLayout.LayoutParams(width, height)
    }

    /** `RelativeLayout` params of [width] × [height] with the rule [verb] anchored to view id [subject]. */
    @JvmStatic fun relativeParams(width: Int, height: Int, verb: Int, subject: Int): RelativeLayout.LayoutParams {
        val lp = RelativeLayout.LayoutParams(width, height)
        lp.addRule(verb, subject)
        return lp
    }

    /** `RelativeLayout` params of [width] × [height] with the parent rule [verb]. */
    @JvmStatic fun relativeParams(width: Int, height: Int, verb: Int): RelativeLayout.LayoutParams {
        val lp = RelativeLayout.LayoutParams(width, height)
        lp.addRule(verb)
        return lp
    }

    /** Closes / releases [obj] (R8's desugared `AutoCloseable.close()`; the app passes a `MediaMetadataRetriever`). */
    @JvmStatic fun release(top: Any?) {
        when (top) {
            is AutoCloseable -> top.close()
            is android.content.res.TypedArray -> top.recycle()
            is android.media.MediaMetadataRetriever -> top.release()
            is java.util.concurrent.ExecutorService -> top.shutdown()
            else -> throw IllegalArgumentException()
        }
    }

    @JvmStatic fun textViewMaxLines(context: Context?, attrs: AttributeSet?, maxLines: Int): AppCompatTextView {
        val appCompatTextView = AppCompatTextView(context!!, attrs)
        appCompatTextView.setMaxLines(maxLines)
        return appCompatTextView
    }


    @JvmStatic fun recyclerView(context: Context?, vertical: Boolean, horizontal: Boolean): MyRecyclerView {
        val myRecyclerView = MyRecyclerView(context!!)
        myRecyclerView.setVerticalScrollBarEnabled(vertical)
        myRecyclerView.setHorizontalScrollBarEnabled(horizontal)
        return myRecyclerView
    }
}
