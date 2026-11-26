package com.mycompany.app.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Whether the reader (the reading view over the page) is open in full: the bars show it by filling the reader button
 * and putting [label] ("Reader mode") in front of the address. Set by the reader when it opens, closes or turns into
 * the mini window.
 */
object ReaderState {
    var on by mutableStateOf(false)

    /** The shown name, in the app's language (set when the reader opens). */
    var label: String = ""
}
