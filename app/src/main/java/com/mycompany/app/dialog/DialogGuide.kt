package com.mycompany.app.dialog

import androidx.compose.foundation.Canvas
import com.mycompany.app.ui.state.AppState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainApp
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * The "Tip" sheet of the tab bar position settings (the old guide dialog had more variants, only this one was ever opened,
 * with `i == 5`): how to move tabs (press and hold) and make folders (overlap tabs), with a small sample bar: two tabs, a red
 * ring where the finger is held and an arrow for the move. Compose now (on [ComposeBottomDialog]); same constructor, it
 * shows itself.
 */
class DialogGuide(mainActivity: android.app.Activity?, guide: Int) : ComposeBottomDialog(mainActivity) {
    init {
        setSheet {
            val dark = AppState.darkUi
            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(if (dark) R.drawable.outline_wb_incandescent_2_dark_24 else R.drawable.outline_wb_incandescent_2_black_24),
                    contentDescription = null, modifier = Modifier.size(24.dp),
                )
                Text(stringResource(R.string.tip), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(start = 16.dp))
            }
            HorizontalDivider()
            androidx.compose.foundation.layout.Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 24.dp)) {
                Text(
                    stringResource(R.string.tab_guide_1) + "\n\n" + stringResource(R.string.tab_guide_2),
                    style = MaterialTheme.typography.bodyMedium,
                )
                SampleBar(dark)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.ok)) }
            }
        }
        show()
    }

    @androidx.compose.runtime.Composable
    private fun SampleBar(dark: Boolean) {
        val bar = if (dark) Color.Black else Color.White
        val pill = Color(if (dark) -14606047 else -460552)
        val ink = if (dark) Color(0xFFF8F8F8) else Color.Black
        val red = Color(0xFFFF0000)
        Box(Modifier.padding(top = 24.dp).fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Box(Modifier.background(bar, RoundedCornerShape(8.dp)).padding(start = 12.dp, end = 12.dp, bottom = 6.dp)) {
                Row(Modifier.padding(top = 40.dp).height(44.dp)) {
                    repeat(2) {
                        Row(
                            Modifier.width(144.dp).height(44.dp).padding(horizontal = 2.dp).clip(RoundedCornerShape(8.dp)).background(pill),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painterResource(if (dark) R.drawable.outline_home_dark_24 else R.drawable.outline_home_black_24),
                                contentDescription = null, modifier = Modifier.padding(start = 12.dp).size(20.dp),
                            )
                            Text("OPUM", color = ink, style = TextStyle(fontSize = 14.sp), modifier = Modifier.padding(start = 10.dp).weight(1f))
                            Image(
                                painterResource(if (dark) R.drawable.outline_close_dark_18 else R.drawable.outline_close_black_18),
                                contentDescription = null, modifier = Modifier.padding(end = 10.dp),
                            )
                        }
                    }
                }
                // the finger held on the second tab
                Box(Modifier.padding(top = 40.dp).offset(x = 144.dp + 38.dp).size(44.dp).clip(CircleShape).background(Color.Transparent)) {
                    Canvas(Modifier.size(44.dp)) { drawCircle(red, radius = size.minDimension / 2f - 2.dp.toPx(), style = Stroke(3.dp.toPx())) }
                }
                // the move: an arrow both ways above the tabs
                Canvas(Modifier.padding(top = 4.dp).width(118.dp).height(32.dp).offset(x = 70.dp)) {
                    val y = size.height / 2f
                    val head = 8.dp.toPx()
                    val stroke = 3.dp.toPx()
                    drawLine(red, Offset(0f, y), Offset(size.width, y), stroke)
                    drawLine(red, Offset(0f, y), Offset(head, y - head), stroke)
                    drawLine(red, Offset(0f, y), Offset(head, y + head), stroke)
                    drawLine(red, Offset(size.width, y), Offset(size.width - head, y - head), stroke)
                    drawLine(red, Offset(size.width, y), Offset(size.width - head, y + head), stroke)
                }
            }
        }
    }
}
