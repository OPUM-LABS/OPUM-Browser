package com.mycompany.app.lock

import androidx.compose.foundation.ExperimentalFoundationApi
import com.mycompany.app.ui.state.AppState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.lock.LockComposeActivity
import com.mycompany.app.ui.lock.LockLayout
import com.mycompany.app.ui.lock.LockSecondaryButton
import kotlinx.coroutines.delay

/**
 * PIN lock (4 digits). Unlocking checks the PIN as soon as the fourth digit is in; setting a new one asks twice
 * (Continue, then Apply). The last digit is shown for a moment, the others as stars. Backspace removes a digit
 * (long press clears). Intent contract and behaviour: [LockComposeActivity].
 */
class PinActivity : LockComposeActivity() {

    @Composable
    override fun Content() {
        LockBack()
        androidx.compose.runtime.key(generation) { PinScreen() }
    }

    @Composable
    private fun PinScreen() {
        var pin by remember { mutableStateOf("") }
        var message by remember { mutableStateOf<Int?>(if (isSetting) R.string.input else null) }
        var confirming by remember { mutableStateOf(false) }
        var first by remember { mutableStateOf<String?>(null) }
        var showLast by remember { mutableIntStateOf(0) }     // counts key presses, drives the "show last digit" timer
        var wrong by remember { mutableStateOf(false) }       // wrong PIN entered, clears after a short moment

        LaunchedEffect(showLast) {
            if (showLast > 0) { delay(200); showLast = 0 }
        }
        LaunchedEffect(wrong) {
            if (wrong) {
                delay(200)
                pin = ""
                message = R.string.wrong_input
                wrong = false
            }
        }

        fun onPin(value: String) {
            if (wrong) return
            pin = value
            showLast = if (value.isEmpty()) 0 else value.length
            if (!isSetting) {
                message = null
                if (value.length == 4) {
                    if (value == expectedSecret()) unlocked() else wrong = true
                }
                return
            }
            message = when {
                value.isEmpty() -> if (confirming) R.string.reinput else R.string.input
                else -> null
            }
            if (value.length == 4 && confirming && value != first) wrong = true
        }

        fun digit(digitValue: Int) {
            if (pin.length < 4) onPin(pin + digitValue)
        }

        fun retry() {
            confirming = false; first = null; pin = ""; showLast = 0
            message = R.string.input
        }

        fun next() {
            if (pin.length != 4) return
            if (!confirming) {
                confirming = true
                first = pin
                pin = ""
                showLast = 0
                message = R.string.reinput
            } else if (pin != first) {
                wrong = true
            } else {
                storeSecret(2, first)
            }
        }

        LockLayout(message?.let { stringResource(it) }) {
            // the four digit boxes
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                repeat(4) { index ->
                    val shown = when {
                        wrong && index < pin.length -> if (index == 3) pin.substring(3) else "*"
                        index < pin.length -> if (index == pin.length - 1 && showLast > 0) pin[index].toString() else "*"
                        else -> ""
                    }
                    OutlinedCard(Modifier.size(width = 52.dp, height = 64.dp), shape = RoundedCornerShape(12.dp)) {
                        Box(Modifier.size(width = 52.dp, height = 64.dp), contentAlignment = Alignment.Center) {
                            Text(shown, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
            Keypad(onDigit = ::digit, onBackspace = { if (pin.isNotEmpty()) onPin(pin.dropLast(1)) }, onClear = { if (pin.isNotEmpty()) onPin("") })
            if (isSetting) {
                Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { retry() }, enabled = pin.isNotEmpty() || confirming) { Text(stringResource(R.string.retry)) }
                    Button(onClick = { next() }, enabled = pin.length == 4 && !wrong) {
                        Text(stringResource(if (confirming) R.string.apply else R.string.continue_input))
                    }
                }
            } else {
                LockSecondaryButton(secondaryLabel()) { onSecondary() }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Keypad(onDigit: (Int) -> Unit, onBackspace: () -> Unit, onClear: () -> Unit) {
    val rows = listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9), listOf(-1, 0, -2))
    rows.forEach { row ->
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            row.forEach { key ->
                when (key) {
                    -1 -> Box(Modifier.size(72.dp))
                    -2 -> Box(
                        Modifier.size(72.dp).combinedClickable(onClick = onBackspace, onLongClick = onClear),
                        contentAlignment = Alignment.Center,
                    ) { Icon(painterResource(if (com.mycompany.app.ui.state.AppState.darkUi) R.drawable.outline_backspace_dark_24 else R.drawable.outline_backspace_black_24), contentDescription = null, tint = androidx.compose.ui.graphics.Color.Unspecified) }
                    else -> Surface(
                        onClick = { onDigit(key) },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(72.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(key.toString(), style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
