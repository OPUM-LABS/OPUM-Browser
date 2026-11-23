package com.mycompany.app.dialog

import android.app.Activity
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainDownSize
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * "Estimated size" of a download ([url] = URL, [extra] = referer / user agent as before): shows the known size [fileSize] or asks
 * [MainDownSize] in the background (spinner, Cancel), then the size or "Unknown" with Retry. A found size goes to
 * [DownSizeListener]. Compose now (on [ComposeBottomDialog]); same constructor and listener, shows itself.
 */
class DialogDownSize(
    activity: Activity?,
    url: String?,
    extra: String?,
    fileSize: Long,
    downSizeListener: DownSizeListener?,
) : ComposeBottomDialog(activity) {
    private var listener: DownSizeListener? = downSizeListener
    private val url = url
    private val extra = extra
    private val ui = Handler(Looper.getMainLooper())

    private var size by mutableLongStateOf(fileSize)
    private var loading by mutableStateOf(false)
    private var task: MainDownSize? = null

    interface DownSizeListener {
        fun onSize(size: Long)
    }

    init {
        setSheet {
            Text(
                stringResource(R.string.estimated_size),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 24.dp, end = 24.dp),
            )
            Box(Modifier.fillMaxWidth().height(64.dp), contentAlignment = Alignment.Center) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(32.dp))
                } else {
                    Text(
                        if (size > 0) MainUtil.sizeLabel(size) else stringResource(R.string.unknown),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
            }
            Text(
                stringResource(R.string.estimated_info),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(onClick = { onButton() }) {
                    Text(
                        stringResource(
                            when {
                                loading -> R.string.cancel
                                size > 0 -> R.string.ok
                                else -> R.string.retry
                            }
                        )
                    )
                }
            }
        }
        if (fileSize <= 0) loadSize()
        show()
    }

    private fun onButton() {
        if (loading || size > 0) dismiss() else loadSize()
    }

    /** Starts the size lookup (old name). */
    fun loadSize() {
        if (loading) return
        loading = true
        val mainDownSize = MainDownSize()
        task = mainDownSize
        val ctx = context
        val runnable = Runnable {
            mainDownSize.start(ctx, url, extra, object : DownSizeListener {
                override fun onSize(size: Long) {
                    ui.post { finish(mainDownSize, size) }
                }
            })
        }
        val ex = MainApp.executor(ctx)
        try {
            if (ex != null) ex.execute(runnable) else Thread(runnable).start()
        } catch (unused: Exception) {
            loading = false
            task = null
        }
    }

    private fun finish(mainDownSize: MainDownSize, newSize: Long) {
        if (task !== mainDownSize) return
        task = null
        loading = false
        size = newSize
        if (newSize > 0) listener?.onSize(newSize)
    }

    override fun dismiss() {
        val taskNow = task
        task = null
        if (taskNow != null) {
            try { taskNow.stop() } catch (unused: Exception) { }
        }
        loading = false
        listener = null
        super.dismiss()
    }

    companion object {
    }
}
