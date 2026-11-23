package com.mycompany.app.ui.browser

import android.net.http.SslError
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R

/** What the old SSL page showed: error code line + sentence per `SslError` primary error (English, as before). */
fun sslErrorInfo(error: SslError?): Pair<String?, String> = when (error?.primaryError) {
    null -> null to "SSL Certificate error."
    SslError.SSL_NOTYETVALID -> "ERR: SSL_NOTYETVALID" to "The certificate is not yet valid."
    SslError.SSL_EXPIRED -> "ERR: SSL_EXPIRED" to "The certificate has expired."
    SslError.SSL_IDMISMATCH -> "ERR: SSL_IDMISMATCH" to "Hostname mismatch."
    SslError.SSL_UNTRUSTED -> "ERR: SSL_UNTRUSTED" to "The certificate authority is not trusted."
    SslError.SSL_DATE_INVALID -> "ERR: SSL_DATE_INVALID" to "The date of the certificate is invalid."
    SslError.SSL_INVALID -> "ERR: SSL_INVALID" to "A generic error occurred."
    else -> null to "SSL Certificate error."
}

/** The certificate details shown in the scrolling area: the certificate text, then the URL. */
fun sslErrorDetails(error: SslError?): String {
    if (error == null) return ""
    val sb = StringBuilder()
    val certificate = error.certificate?.toString()
    if (!certificate.isNullOrEmpty()) sb.append(certificate)
    val url = error.url
    if (!url.isNullOrEmpty()) {
        if (sb.isNotEmpty() && !sb.endsWith("\n")) sb.append("\n")
        sb.append("URL: ").append(url)
    }
    return sb.toString()
}

/** Full-page SSL certificate warning: leave to a safe page (default action), continue anyway, or open the site settings. */
@Composable
fun SslErrorPage(
    error: SslError?,
    onSettings: () -> Unit,
    onContinue: () -> Unit,
    onExit: () -> Unit,
) {
    val (code, sentence) = sslErrorInfo(error)
    val details = sslErrorDetails(error)
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.baseline_warning_red_24), contentDescription = null, modifier = Modifier.size(32.dp))
                Text(
                    stringResource(R.string.site_warning), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 16.dp).weight(1f),
                )
                IconButton(onClick = onSettings) {
                    Icon(painterResource(R.drawable.outline_settings_black_20), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(Modifier.weight(1f).fillMaxWidth().padding(top = 16.dp)) {
                if (code != null) Text(code, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                Column(Modifier.weight(1f).fillMaxWidth().padding(vertical = 8.dp).verticalScroll(rememberScrollState())) {
                    Text(details, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(sentence, style = MaterialTheme.typography.bodyLarge)
            }
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.site_continue)) }
            Spacer(Modifier.height(8.dp))
            Button(onClick = onExit, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.site_exit)) }
        }
    }
}

/** Full-page "the page could not be displayed" (renderer crashed): refresh, and the three things that help. */
@Composable
fun CrashPage(
    url: String?,
    onRefresh: () -> Unit,
    onMemorySave: () -> Unit,
    onCleanMode: () -> Unit,
    onClearData: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.crash_guide_1), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            if (!url.isNullOrEmpty()) {
                Text(url, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) { Text(stringResource(R.string.refresh)) }
            Text(stringResource(R.string.crash_guide_2), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 24.dp))
            OutlinedButton(onClick = onMemorySave, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.mem_save)) }
            OutlinedButton(onClick = onCleanMode, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.clean_mode)) }
            OutlinedButton(onClick = onClearData, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.clear_data)) }
        }
    }
}
