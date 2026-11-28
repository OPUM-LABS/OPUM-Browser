package com.mycompany.app.web

import com.mycompany.app.main.MainUtil.evalJs
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import org.json.JSONObject

/**
 * Filling a saved login (2026-11-28): only when the user picks one (the password button's menu or the fill bar), and
 * only that one. The values go in as escaped string literals inside a function that keeps nothing afterwards: the
 * page gets the login in its name and password fields, nothing more (before, every saved login of the site was put
 * into the page as script variables on every load).
 *
 * The password goes into the first visible password field; the name into the visible text, e-mail or phone field
 * just before it (or one marked as a user name), never a search or captcha field. A page that asks for the name
 * first (two-step logins) gets the name only. Values are set the way typing does, so pages built with frameworks see
 * them.
 */
fun WebViewActivity.fillLogin(user: String?, pass: String?) {
    val js = "(function(u,p){" +
        "function shown(e){return !e.disabled&&!e.readOnly&&e.offsetParent!==null&&e.type!=='hidden';}" +
        "function put(e,v){if(!e||v==null)return;e.focus();" +
        "var s=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;s.call(e,v);" +
        "e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));}" +
        "function plain(e){var k=((e.id||'')+' '+(e.name||'')+' '+(e.autocomplete||'')).toLowerCase();" +
        "return !/search|captcha|keyword|query/.test(k)&&/^(text|email|tel)$/.test(e.type);}" +
        "var all=Array.prototype.slice.call(document.querySelectorAll('input')).filter(shown);" +
        "var pw=all.filter(function(e){return e.type==='password';})[0]||null;" +
        "var named=all.filter(function(e){return plain(e)&&/user|mail|login|account|identifier|phone/.test(((e.id||'')+(e.name||'')+(e.autocomplete||'')).toLowerCase());})[0]||null;" +
        "var before=null;if(pw){for(var i=all.indexOf(pw)-1;i>=0;i--){if(plain(all[i])){before=all[i];break;}}}" +
        "var name=before||named||all.filter(plain)[0]||null;" +
        "put(name,u);if(pw)put(pw,p);" +
        "})(" + JSONObject.quote(user ?: "") + "," + JSONObject.quote(pass ?: "") + ");"
    evalJs(currentPage, js, true)
}

/**
 * What kind of form the page shows: "new" (a sign-up or change-password form: a field marked for a new password, or
 * two password fields), "login" (one password field), "none". Only fields the user can see and type in count.
 */
private const val FORM_KIND_JS =
    "(function(){var a=Array.prototype.slice.call(document.querySelectorAll(\"input[type='password']\"))" +
        ".filter(function(e){return !e.disabled&&!e.readOnly&&e.offsetParent!==null;});" +
        "if(!a.length)return 'none';" +
        "if(a.length>1||a.some(function(e){return (e.autocomplete||'').indexOf('new-password')>=0;}))return 'new';" +
        "return 'login';})()"

/** Fills [password] into the page's new-password fields (the password and its repeat), the way typing does. */
private fun WebViewActivity.fillNewPassword(password: String) {
    val js = "(function(p){var s=Object.getOwnPropertyDescriptor(HTMLInputElement.prototype,'value').set;" +
        "Array.prototype.slice.call(document.querySelectorAll(\"input[type='password']\"))" +
        ".filter(function(e){return !e.disabled&&!e.readOnly&&e.offsetParent!==null&&(e.autocomplete||'').indexOf('current-password')<0;})" +
        ".forEach(function(e){s.call(e,p);e.dispatchEvent(new Event('input',{bubbles:true}));e.dispatchEvent(new Event('change',{bubbles:true}));});" +
        "})(" + JSONObject.quote(password) + ");"
    evalJs(currentPage, js, true)
}

/**
 * After a page has loaded (unless turned off: `ZtriSettings.passBar`): on a sign-up or change-password form the bar
 * offers a strong password; on a login form of a site with saved [logins] it offers to fill one. Any bar of the page
 * before goes.
 */
fun WebViewActivity.offerLoginFill(logins: ArrayList<Any?>?) {
    // the site check runs in the background; the page and the bar live on the main thread
    if (android.os.Looper.myLooper() != android.os.Looper.getMainLooper()) {
        runOnUiThread { offerLoginFill(logins) }
        return
    }
    closeLoginFillBar()
    if (!com.mycompany.app.pref.store.ZtriSettings.passBar.value || !PassManager.saving) return
    val list = logins?.mapNotNull { it as? com.mycompany.app.main.MainItem.ChildItem }?.filter { !it.url.isNullOrEmpty() && it.subText != null }.orEmpty()
    val page = currentPage ?: return
    val host = dialogsState.pageHost ?: return
    page.evalJsRaw(FORM_KIND_JS) { result ->
        if (currentPage !== page || dialogsState.pageHost != host) return@evalJsRaw
        val bar = when (result) {
            "\"new\"" -> LoginFillBar(this, host, emptyList(), PasswordMaker.make())
            "\"login\"" -> if (list.isEmpty()) return@evalJsRaw else LoginFillBar(this, host, list.map { (it.url ?: "") to (it.subText ?: "") }, null)
            else -> return@evalJsRaw
        }
        val overlays = floatingState.overlays ?: return@evalJsRaw
        floatingState.loginFillBar = bar
        val decorHeight = window?.decorView?.height ?: 0
        val params = com.mycompany.app.ui.browser.OverlayParams(-1, -2)
        params.gravity = android.view.Gravity.BOTTOM
        params.bottomMargin = (decorHeight - pageVisibleBottom().coerceAtMost(decorHeight)).coerceAtLeast(0) + com.mycompany.app.main.MainApp.dp8
        try { overlays.addCoordView(bar, params) } catch (_: Exception) { floatingState.loginFillBar = null }
    }
}

fun WebViewActivity.closeLoginFillBar() {
    val bar = floatingState.loginFillBar ?: return
    floatingState.loginFillBar = null
    try { floatingState.overlays?.removeView(bar) } catch (_: Exception) {}
}

/**
 * The bar: "Saved login for <site>" with Fill and close. With one login Fill fills it; with several the bar opens to
 * list them by name, and a tap on one fills that one. Filling closes the bar.
 */
@android.annotation.SuppressLint("ViewConstructor")
class LoginFillBar(
    private val host: WebViewActivity, private val site: String, private val logins: List<Pair<String, String>>,
    /** Set on a sign-up or change-password form: the strong password the bar offers (instead of saved logins). */
    private val strong: String?,
) : com.mycompany.app.ui.browser.ComposeOverlay(host) {
    init {
        setPage({ com.mycompany.app.ui.state.AppState.darkUi }) { Bar() }
    }

    private fun useStrong() {
        val password = strong ?: return
        host.closeLoginFillBar()
        host.fillNewPassword(password)
        // also on the clipboard (marked sensitive, cleared after a minute) for a repeat field the page builds later;
        // it is offered for saving when the form is sent, like a typed one
        com.mycompany.app.main.SecretClip.copyPassword(host, password)
    }

    private fun fill(login: Pair<String, String>) {
        host.closeLoginFillBar()
        host.fillLogin(login.first, login.second)
    }

    @androidx.compose.runtime.Composable
    private fun Bar() {
        var open by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 6.dp,
            modifier = androidx.compose.ui.Modifier.fillMaxWidth().padding(horizontal = 12.dp),
        ) {
            androidx.compose.foundation.layout.Column {
                androidx.compose.foundation.layout.Row(
                    androidx.compose.ui.Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(
                        androidx.compose.ui.res.painterResource(ch.opumlabs.opumbrowser.R.drawable.outline_vpn_key_black_24), null,
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    )
                    if (strong != null) {
                        androidx.compose.foundation.layout.Column(androidx.compose.ui.Modifier.weight(1f).padding(start = 12.dp)) {
                            androidx.compose.material3.Text(
                                androidx.compose.ui.res.stringResource(ch.opumlabs.opumbrowser.R.string.strong_bar_title),
                                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, maxLines = 2,
                            )
                            androidx.compose.material3.Text(
                                strong, style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace, maxLines = 1,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        androidx.compose.material3.TextButton(onClick = { useStrong() }) {
                            androidx.compose.material3.Text(androidx.compose.ui.res.stringResource(ch.opumlabs.opumbrowser.R.string.strong_use))
                        }
                    } else {
                        androidx.compose.material3.Text(
                            androidx.compose.ui.res.stringResource(ch.opumlabs.opumbrowser.R.string.fill_bar_title, site),
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium, maxLines = 2,
                            modifier = androidx.compose.ui.Modifier.weight(1f).padding(start = 12.dp),
                        )
                        androidx.compose.material3.TextButton(onClick = { if (logins.size == 1) fill(logins[0]) else open = !open }) {
                            androidx.compose.material3.Text(androidx.compose.ui.res.stringResource(ch.opumlabs.opumbrowser.R.string.fill_login))
                        }
                    }
                    androidx.compose.material3.IconButton(onClick = { host.closeLoginFillBar() }) {
                        androidx.compose.material3.Icon(com.mycompany.app.ui.components.Sym.Close, androidx.compose.ui.res.stringResource(ch.opumlabs.opumbrowser.R.string.close))
                    }
                }
                if (open) {
                    for (login in logins) {
                        androidx.compose.material3.Text(
                            login.first, style = androidx.compose.material3.MaterialTheme.typography.bodyLarge, maxLines = 1,
                            modifier = androidx.compose.ui.Modifier.fillMaxWidth().clickable { fill(login) }.padding(horizontal = 52.dp, vertical = 12.dp),
                        )
                    }
                }
            }
        }
    }
}
