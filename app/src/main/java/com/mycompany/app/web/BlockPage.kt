package com.mycompany.app.web

import android.content.Context
import androidx.core.net.toUri
import androidx.core.text.htmlEncode
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.store.BrowserSettings
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.ui.theme.AppColor

/**
 * The page shown instead of a page the Web Filter blocks (2026-11-27; was a plain white "Ads Blocked" / "Link Blocked"
 * page in English): the shield, why it is blocked, and what can be done, in the app's language and colours (light or
 * dark like the pages). The buttons send `sb:` messages through `alert()`, handled by the page's chrome client:
 * `sb:go_back`, `sb:ads_open` (this once), `sb:ads_allow` (add the site to the allowed sites), `sb:filter_settings`,
 * `sb:link_setting` (the blocked links).
 */
object BlockPage {
    /** Blocked by a filter list (ads and trackers). */
    const val LIST = 0

    /** On the user's blocked links. */
    const val LINK = 1

    fun html(context: Context, url: String?, kind: Int): String {
        val address = url.orEmpty()
        val host = hostOf(address)
        val dark = AppState.darkWeb
        val choice = AppColor.choiceFor(BrowserSettings.colorTheme.value, BrowserSettings.themeColor.value, BrowserSettings.systemColors())
        val seed = choice?.seed ?: systemAccent(context) ?: AppColor.NEUTRAL_SEED
        val roles = AppColor.roles(seed, dark, choice?.style ?: AppColor.Style.TRUE_COLOR)
        fun c(role: AppColor.Role) = css(roles[role])
        val reason = context.getString(if (kind == LINK) R.string.block_reason_link else R.string.block_reason_list)
        val buttons = StringBuilder()
        buttons.append(button("sb:go_back", context.getString(R.string.block_go_back), "primary"))
        if (kind == LINK) {
            buttons.append(button("sb:link_setting", context.getString(R.string.block_edit_links), "text"))
        } else {
            buttons.append(button("sb:ads_open", context.getString(R.string.block_open_once), "tonal"))
            buttons.append(button("sb:ads_allow", context.getString(R.string.block_allow_site), "text"))
            buttons.append(button("sb:filter_settings", context.getString(R.string.block_settings), "text"))
        }
        return """<!DOCTYPE html><html><head><meta charset="utf-8"/>
<meta name="viewport" content="width=device-width,initial-scale=1.0"/>
<meta name="color-scheme" content="${if (dark) "dark" else "light"}"/>
<title>${esc(host.ifEmpty { address })}</title>
<style>
html,body{margin:0;background:${c(AppColor.Role.SURFACE)};color:${c(AppColor.Role.ON_SURFACE)};font-family:sans-serif;}
.wrap{min-height:100vh;box-sizing:border-box;display:flex;flex-direction:column;justify-content:center;max-width:480px;margin:0 auto;padding:32px 24px;}
.shield{width:72px;height:72px;border-radius:50%;background:${c(AppColor.Role.PRIMARY_CONTAINER)};display:flex;align-items:center;justify-content:center;margin-bottom:24px;}
.shield svg{width:40px;height:40px;fill:${c(AppColor.Role.ON_PRIMARY_CONTAINER)};}
h1{font-size:24px;line-height:1.3;font-weight:500;margin:0 0 8px;}
.host{font-size:18px;font-weight:600;margin:0 0 16px;word-break:break-all;}
.reason{font-size:16px;line-height:1.5;color:${c(AppColor.Role.ON_SURFACE_VARIANT)};margin:0 0 8px;}
.url{font-size:13px;line-height:1.4;color:${c(AppColor.Role.ON_SURFACE_VARIANT)};margin:0 0 32px;word-break:break-all;display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden;}
button{display:block;width:100%;height:48px;border:none;border-radius:24px;font-size:15px;font-weight:500;margin:0 0 12px;font-family:inherit;}
.primary{background:${c(AppColor.Role.PRIMARY)};color:${c(AppColor.Role.ON_PRIMARY)};}
.tonal{background:${c(AppColor.Role.SECONDARY_CONTAINER)};color:${c(AppColor.Role.ON_SECONDARY_CONTAINER)};}
.text{background:transparent;color:${c(AppColor.Role.PRIMARY)};height:44px;margin:0 0 4px;}
</style></head><body><div class="wrap">
<div class="shield"><svg viewBox="0 -960 960 960"><path d="M480-80q-139-35-229.5-159.5T160-516v-244l320-120 320 120v244q0 152-90.5 276.5T480-80Z"/></svg></div>
<h1>${esc(context.getString(R.string.block_title))}</h1>
<p class="host">${esc(host.ifEmpty { address })}</p>
<p class="reason">${esc(reason)}</p>
<p class="url">${esc(address)}</p>
$buttons
</div></body></html>"""
    }

    private fun button(message: String, label: String, style: String) =
        "<button class=\"$style\" onclick=\"alert('$message')\">${esc(label)}</button>\n"

    private fun esc(text: String): String = text.htmlEncode()

    private fun css(argb: Int): String = String.format("#%06X", argb and 0xFFFFFF)

    /** The wallpaper colour (Android 12+) for "System" colours. */
    private fun systemAccent(context: Context): Int? =
        try { context.getColor(android.R.color.system_accent1_500) } catch (_: Exception) { null }

    /** The host of an address, without "www.". */
    private fun hostOf(url: String): String =
        try { url.toUri().host?.removePrefix("www.").orEmpty() } catch (_: Exception) { "" }
}
