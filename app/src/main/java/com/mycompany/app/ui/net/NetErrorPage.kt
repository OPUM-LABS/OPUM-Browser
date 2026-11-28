package com.mycompany.app.ui.net

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.net.NetCause
import com.mycompany.app.net.NetDiagnosis
import com.mycompany.app.net.NetEntry

/** The name of a network kind ([NetEntry.WIFI] ...). */
@Composable
fun netKindName(kind: Int): String = stringResource(
    when (kind) {
        NetEntry.WIFI -> R.string.net_kind_wifi
        NetEntry.MOBILE -> R.string.net_kind_mobile
        NetEntry.ETHERNET -> R.string.net_kind_eth
        NetEntry.VPN -> R.string.net_kind_vpn
        else -> R.string.net_kind_other
    }
)

/** The main button of a cause. */
enum class NetAction { RETRY, ALLOW_LOCAL, WIFI, INTERNET, CHECK }

fun primaryAction(cause: NetCause?): NetAction = when (cause) {
    NetCause.LOCAL_PERMISSION -> NetAction.ALLOW_LOCAL
    NetCause.LOCAL_MOBILE, NetCause.LOCAL_NO_WIFI, NetCause.CAPTIVE -> NetAction.WIFI
    NetCause.OFFLINE, NetCause.NO_INTERNET -> NetAction.INTERNET
    else -> NetAction.RETRY
}

/**
 * Full-page "this page could not be opened" for network errors (2026-11-28), over WebView's own error page: the
 * likely cause in plain words (worked out by `NetDiag`), the network in use, the error code, and what helps.
 * [diagnosis] is null while the check runs.
 */
@Composable
fun NetErrorPage(
    url: String?,
    errorText: String?,
    diagnosis: NetDiagnosis?,
    onAction: (NetAction) -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.baseline_warning_red_24), contentDescription = null, modifier = Modifier.size(32.dp))
                Text(
                    stringResource(R.string.net_err_title), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp).weight(1f),
                )
            }
            if (!url.isNullOrEmpty()) {
                Text(url, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            if (diagnosis == null) {
                CircularProgressIndicator(Modifier.padding(top = 8.dp).size(28.dp))
            } else {
                Text(causeText(diagnosis), style = MaterialTheme.typography.bodyLarge)
                val active = diagnosis.snapshot.active
                if (active != null) {
                    Text(
                        stringResource(R.string.net_in_use, netKindName(active.kind)),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (!errorText.isNullOrEmpty()) {
                Text(errorText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
            }
            val primary = primaryAction(diagnosis?.cause)
            Button(onClick = { onAction(primary) }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(actionLabel(primary))
            }
            if (primary != NetAction.RETRY) {
                OutlinedButton(onClick = { onAction(NetAction.RETRY) }, modifier = Modifier.fillMaxWidth()) { Text(actionLabel(NetAction.RETRY)) }
            }
            TextButton(onClick = { onAction(NetAction.CHECK) }, modifier = Modifier.fillMaxWidth()) { Text(actionLabel(NetAction.CHECK)) }
        }
    }
}

@Composable
private fun actionLabel(action: NetAction): String = stringResource(
    when (action) {
        NetAction.RETRY -> R.string.retry
        NetAction.ALLOW_LOCAL -> R.string.net_allow_local
        NetAction.WIFI -> R.string.net_wifi_settings
        NetAction.INTERNET -> R.string.net_settings
        NetAction.CHECK -> R.string.net_check
    }
)

@Composable
private fun causeText(d: NetDiagnosis): String {
    val main = stringResource(
        when (d.cause) {
            NetCause.OFFLINE -> R.string.net_cause_offline
            NetCause.LOCAL_PERMISSION -> R.string.net_cause_local_perm
            NetCause.LOCAL_MOBILE -> R.string.net_cause_local_mobile
            NetCause.LOCAL_NO_WIFI -> R.string.net_cause_local_no_wifi
            NetCause.LOCAL_VPN -> R.string.net_cause_local_vpn
            NetCause.LOCAL_NO_ANSWER -> R.string.net_cause_local_no_answer
            NetCause.CAPTIVE -> R.string.net_cause_captive
            NetCause.NO_INTERNET -> R.string.net_cause_no_internet
            NetCause.DNS -> R.string.net_cause_dns
            NetCause.SITE -> R.string.net_cause_site
        }
    )
    val active = d.snapshot.active
    // only a chosen server (strict mode) can make every lookup fail; "Automatic" falls back to the network's DNS
    val strict = active?.privateDnsName
    if (d.cause == NetCause.DNS && active != null && active.privateDnsActive && !strict.isNullOrEmpty()) {
        return main + "\n\n" + stringResource(R.string.net_private_dns, strict)
    }
    return main
}
