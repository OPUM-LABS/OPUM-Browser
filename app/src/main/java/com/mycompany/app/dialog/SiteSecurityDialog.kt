package com.mycompany.app.dialog

import android.net.http.SslCertificate
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import com.mycompany.app.ui.components.TickSwitch as Switch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.app.web.deleteSiteCookies
import com.mycompany.app.web.reloadPage
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebNestView
import com.mycompany.app.web.WebViewActivity
import java.text.DateFormat
import com.mycompany.app.ui.components.Sym

/**
 * Tap on the lock in the address bar (2026-11-20, owner's request): a short answer to "is this site secure?" (secure,
 * or a certificate error the user chose to continue past), who the certificate was issued to and by and until when,
 * and a button to the full certificate ([DialogWebCerti]). Below that, like Chrome's page info: the site's saved
 * permissions as switches (`DbBookPms`) with "Reset permissions", "Clear site data" (cookies and storage, after a confirmation)
 * and "Site settings" (the saved site permissions). Ads and pop-ups per site are in the Web Filter dialog (shield). The page reloads on close when something changed. Shows itself.
 */
class SiteSecurityDialog(
    activity: WebViewActivity,
    webView: WebView?,
    private val onDetails: () -> Unit,
) : ComposeBottomDialog(activity) {
    private val wa: WebViewActivity = activity
    private val pageUrl: String = webView?.url ?: ""
    /** Key the browser stores permissions under (the page host); [listHost] = the host without "www." for messages. */
    private val pmsHost: String? = activity.dialogsState.pageHost
    private val listHost: String? = com.mycompany.app.main.MainUtil.hostOfUrl(pageUrl, true)
    private var allow by mutableIntStateOf(0)
    private var block by mutableIntStateOf(0)
    private var confirmClear by mutableStateOf(false)
    private var changed = false

    init {
        val cert: SslCertificate? = try { webView?.certificate } catch (_: Exception) { null }
        val host = try { android.net.Uri.parse(webView?.url ?: "").host } catch (_: Exception) { null } ?: ""
        val bypassed = (webView as? WebNestView)?.sslBypassedHost?.let { it.isNotEmpty() && it == host } == true
        val issuedTo = cert?.issuedTo?.cName?.takeIf { it.isNotBlank() }
        val issuedBy = (cert?.issuedBy?.oName?.takeIf { it.isNotBlank() } ?: cert?.issuedBy?.cName)?.takeIf { it.isNotBlank() }
        val validUntil = cert?.validNotAfterDate?.let { DateFormat.getDateInstance(DateFormat.LONG).format(it) }
        setSheet {
            val warn = MaterialTheme.colorScheme.error
            val ok = if (MaterialTheme.colorScheme.background.luminanceIsDark()) Color(0xFF81C995) else Color(0xFF188038)
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
            Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (bypassed) Sym.Warning else Sym.Lock, null,
                        tint = if (bypassed) warn else ok, modifier = Modifier.size(24.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(if (bypassed) R.string.sec_bypassed else R.string.sec_secure),
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                if (host.isNotEmpty()) {
                    Text(
                        host, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 36.dp, top = 2.dp),
                    )
                }
                Text(
                    stringResource(if (bypassed) R.string.sec_bypassed_info else R.string.sec_secure_info),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 16.dp),
                )
                Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (cert == null) {
                        Text(stringResource(R.string.no_info), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Fact(R.string.sec_issued_to, issuedTo)
                        Fact(R.string.sec_issued_by, issuedBy)
                        Fact(R.string.sec_valid_until, validUntil)
                    }
                }
            }
            if (host.isNotEmpty()) SiteControls()
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                if (cert != null) TextButton(onClick = { dismiss(); onDetails() }) { Text(stringResource(R.string.view_certi)) }
                TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.close)) }
            }
        }
        if (host.isNotEmpty()) load()
        show()
    }

    /** Reads the site's saved permissions. */
    private fun load() {
        val ctx = wa.applicationContext
        Thread {
            var a = 0
            var b = 0
            val key = pmsHost
            if (!key.isNullOrEmpty()) {
                var cursor: android.database.Cursor? = null
                try {
                    cursor = com.mycompany.app.db.DbUtil.query(
                        com.mycompany.app.db.book.DbBookPms.get(ctx).writableDatabase, "DbBookPms_table",
                        arrayOf("_allow", "_block"), "_path=?", arrayOf(key), null,
                    )
                    if (cursor != null && cursor.moveToFirst()) {
                        a = cursor.getInt(0)
                        b = cursor.getInt(1)
                    }
                } catch (_: Exception) {
                }
                cursor?.close()
            }
            wa.runOnUiThread {
                allow = a
                block = b
            }
        }.start()
    }

    @Composable
    private fun SiteControls() {
        val perms = allow or block
        Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
            HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            if (perms != 0) {
                Header(R.string.site_info_permissions)
                for ((bit, label) in listOf(
                    DialogPermission.CAMERA to R.string.camera, DialogPermission.MIC to R.string.perm_mic,
                    DialogPermission.LOCATION to R.string.location, DialogPermission.PROTECTED to R.string.perm_protected,
                )) {
                    if ((perms and bit) == 0) continue
                    SwitchLine(stringResource(label), (allow and bit) != 0) { setPermission(bit, it) }
                }
                TextButton(onClick = { resetPermissions() }, modifier = Modifier.padding(start = 12.dp)) {
                    Text(stringResource(R.string.site_info_reset_perms))
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                TextButton(onClick = { confirmClear = true }) { Text(stringResource(R.string.site_info_clear)) }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { openSiteSettings() }) { Text(stringResource(R.string.pms_site_settings)) }
            }
        }
        if (confirmClear) {
            AlertDialog(
                onDismissRequest = { confirmClear = false },
                title = { Text(stringResource(R.string.site_info_clear)) },
                text = { Text(stringResource(R.string.site_info_clear_info, listHost ?: "")) },
                confirmButton = { TextButton(onClick = { confirmClear = false; clearSiteData() }) { Text(stringResource(R.string.site_info_clear_ok)) } },
                dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) } },
            )
        }
    }

    @Composable
    private fun Header(title: Int) {
        Text(
            stringResource(title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 4.dp),
        )
    }

    @Composable
    private fun SwitchLine(title: String, checked: Boolean, onChange: (Boolean) -> Unit) {
        Row(
            Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(horizontal = 24.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onChange)
        }
    }

    /** Allows ([on]) or blocks one saved permission of the site. */
    private fun setPermission(bit: Int, on: Boolean) {
        allow = if (on) allow or bit else allow and bit.inv()
        block = if (on) block and bit.inv() else block or bit
        changed = true
        val key = pmsHost ?: return
        val a = allow
        val b = block
        val ctx = wa.applicationContext
        Thread {
            val values = android.content.ContentValues()
            values.put("_allow", a)
            values.put("_block", b)
            values.put("_time", System.currentTimeMillis())
            com.mycompany.app.db.DbUtil.updateWhere(
                com.mycompany.app.db.book.DbBookPms.get(ctx).writableDatabase, "DbBookPms_table", values, "_path=?", arrayOf(key),
            )
        }.start()
    }

    /** Forgets the site's permission decisions: it asks again next time. */
    private fun resetPermissions() {
        allow = 0
        block = 0
        changed = true
        val key = pmsHost ?: return
        val ctx = wa.applicationContext
        Thread {
            com.mycompany.app.db.DbUtil.deleteWhere(
                com.mycompany.app.db.book.DbBookPms.get(ctx).writableDatabase, "DbBookPms_table", "_path=?", arrayOf(key),
            )
        }.start()
    }

    /** Cookies and web storage (local storage, IndexedDB, caches) of the site; the page reloads on close. */
    private fun clearSiteData() {
        val uri = try { android.net.Uri.parse(pageUrl) } catch (_: Exception) { null } ?: return
        val host = uri.host ?: return
        try { wa.deleteSiteCookies(pageUrl, host) } catch (_: Exception) {}
        val origin = uri.scheme + "://" + host + if (uri.port > 0) ":" + uri.port else ""
        try { com.mycompany.app.web.WebProfiles.webStorage()!!.deleteOrigin(origin) } catch (_: Exception) {}
        changed = true
        com.mycompany.app.main.MainUtil.showToast(wa, R.string.site_info_cleared)
    }

    private fun openSiteSettings() {
        com.mycompany.app.ui.settings.SiteSettingsActivity.open(wa, pmsHost ?: listHost)
    }

    override fun dismiss() {
        val reload = changed
        changed = false
        super.dismiss()
        if (reload) wa.reloadPage()
    }

    @Composable
    private fun Fact(label: Int, value: String?) {
        if (value.isNullOrBlank()) return
        Row {
            Text(
                stringResource(label), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                modifier = Modifier.width(112.dp),
            )
            Text(value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        }
    }

    private fun Color.luminanceIsDark(): Boolean = (0.299f * red + 0.587f * green + 0.114f * blue) < 0.5f
}
