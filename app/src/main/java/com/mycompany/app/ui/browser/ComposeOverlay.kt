package com.mycompany.app.ui.browser

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.mycompany.app.ui.theme.OpumTheme
import java.util.WeakHashMap

/**
 * A plain view that shows one Compose page, for the browser's old View-based overlays (SSL error page, crash page):
 * activities provide view-tree owners, but plain dialog windows have none, so on attach this makes sure the window root
 * has them (reusing what is there, creating and counting [ComposeOwners] otherwise).
 * Subclasses keep their old public API and call [setPage].
 */
open class ComposeOverlay(context: Context) : FrameLayout(context) {
    private val composeView = ComposeView(context)
    private var root: View? = null
    private var ownsRoot = false

    init {
        // overlays can sit inside legacy RecyclerView items (MyProgressBar in the quick view's grid): see
        // prepareForLegacyRecycler. Outside a pooling container the strategy behaves the same as the default.
        composeView.prepareForLegacyRecycler()
        addView(composeView,LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
    }

    /** Sets the page; [dark] is read inside, so callers pass a state-backed getter. */
    protected fun setPage(dark: () -> Boolean, content: @Composable () -> Unit) {
        composeView.setContent { OpumTheme(darkTheme = dark()) { content() } }
    }

    override fun onAttachedToWindow() {
        val top = rootView
        if (top.findViewTreeLifecycleOwner() == null) {
            acquire(top)
            root = top
            ownsRoot = true
        }
        super.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        val top = root
        if (ownsRoot && top != null) release(top)
        root = null
        ownsRoot = false
    }

    private class Entry(val owners: ComposeOwners, var count: Int)

    companion object {
        private val roots = WeakHashMap<View, Entry>()

        private fun acquire(top: View) {
            val entry = roots.getOrPut(top) {
                val owners = ComposeOwners()
                owners.install(top)
                owners.onStart(); owners.onResume()
                Entry(owners, 0)
            }
            entry.count++
        }

        private fun release(top: View) {
            val entry = roots[top] ?: return
            if (--entry.count > 0) return
            roots.remove(top)
            entry.owners.onDestroy()
            top.setViewTreeLifecycleOwner(null)
            top.setViewTreeSavedStateRegistryOwner(null)
            top.setViewTreeViewModelStoreOwner(null)
        }
    }
}
