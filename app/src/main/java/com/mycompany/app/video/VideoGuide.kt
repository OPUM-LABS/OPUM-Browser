package com.mycompany.app.video

import android.content.Context
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.ComposeOverlay
import com.mycompany.app.view.MyFadeFrame

/**
 * Gesture guides shown over the video players and the preview dialogs. The callers still get a [MyFadeFrame] (it
 * fades the guide in and out and closes it on touch); the content inside is now one Compose page instead of the
 * hand-built View tree (2026-11-16).
 */
object VideoGuide {
    /** Full-screen video: drag up / down on the left for brightness, on the right for volume, sideways to seek. */
    @JvmStatic
    fun updownGuide(context: Context?): MyFadeFrame? {
        context ?: return null
        val frame = MyFadeFrame(context)
        frame.setBackgroundColor(0xA1000000.toInt())
        frame.addView(Page(context) { UpDown() }, FrameLayout.LayoutParams(-1, -1))
        return frame
    }

    private class Page(context: Context, content: @Composable () -> Unit) : ComposeOverlay(context) {
        init { setPage({ true }, content) }
    }

    private val outlined = TextStyle(
        color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center,
        shadow = Shadow(Color.Black, Offset(0f, 0f), blurRadius = 6f),
    )

    @Composable
    private fun Hint(icon: Int, controlBright: Int, modifier: Modifier = Modifier) {
        Column(modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painterResource(icon), contentDescription = null, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(controlBright), style = outlined)
        }
    }

    @Composable
    private fun UpDown() {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth()) {
                Hint(R.drawable.outline_swap_vert_white_48, R.string.control_bright, Modifier.weight(1f))
                Hint(R.drawable.outline_swap_vert_white_48, R.string.control_volume, Modifier.weight(1f))
            }
            Hint(R.drawable.outline_swap_horiz_white_48, R.string.control_seek)
        }
    }

}
