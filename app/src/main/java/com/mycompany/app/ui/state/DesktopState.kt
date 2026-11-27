package com.mycompany.app.ui.state

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Whether the current tab shows the desktop site (2026-11-27): the menu's Desktop switch is drawn filled while it is.
 * Set when the menu opens and when the switch is used.
 */
object DesktopState {
    var on by mutableStateOf(false)
}
