package com.mycompany.app.dialog

import android.content.Context
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.web.WebViewActivity
import java.security.MessageDigest
import java.security.cert.X509Certificate
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.text.DateFormat
import java.util.Date

/**
 * The site's certificate (address bar > lock > View Certificate, or the address menu). Reworked 2026-11-20 (owner):
 * instead of `X509Certificate.toString()` as raw lines, the certificate in sections: issued to (with the other domains
 * it covers), issued by, validity with a status, SHA-256 / SHA-1 fingerprints and the technical details. The text is
 * selectable; Copy puts everything on the clipboard as plain text. Same constructor as before; shows itself.
 */
class DialogWebCerti(
    webViewActivity: WebViewActivity?,
    webView: WebView?,
    /** The certificate to show instead of the page's: the one a certificate warning is about (the page has none yet). */
    certificate: android.net.http.SslCertificate? = null,
) : ComposeBottomDialog(webViewActivity) {

    /** One labelled value; [mono] for fingerprints and serial numbers. */
    private class Fact(val label: Int, val value: String, val mono: Boolean = false, val full: String = value)
    private class Section(val title: Int, val facts: List<Fact>)

    init {
        val x509: X509Certificate? = try { (certificate ?: webView?.certificate)?.x509Certificate } catch (_: Exception) { null }
        val host = try { android.net.Uri.parse(webView?.url ?: "").host } catch (_: Exception) { null } ?: ""
        val ctx = context
        val sections = if (x509 != null) sectionsOf(x509) else emptyList()
        val status = x509?.let { statusOf(ctx, it) }
        setSheet {
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 20.dp, top = 20.dp)) {
                Text(stringResource(R.string.sec_certificate), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 4.dp))
                if (host.isNotEmpty()) {
                    Text(host, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
                if (status != null) {
                    val good = status.second
                    Text(
                        status.first, style = MaterialTheme.typography.labelLarge,
                        color = if (good) okColor() else MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                    )
                }
                if (sections.isEmpty()) {
                    Text(stringResource(R.string.no_info), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(4.dp, 24.dp))
                }
                SelectionContainer {
                    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (section in sections) SectionCard(section)
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                if (sections.isNotEmpty()) {
                    TextButton(onClick = { copyAll(host, status?.first, sections) }) { Text(stringResource(R.string.sec_copy)) }
                }
                TextButton(onClick = { dismiss() }) { Text(stringResource(R.string.close)) }
            }
        }
        show()
    }

    @Composable
    private fun SectionCard(section: Section) {
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(section.title), style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                for (fact in section.facts) {
                    if (fact.mono) {
                        // long values (fingerprints) under their label
                        Column {
                            Text(stringResource(fact.label), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(fact.value, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
                        }
                    } else {
                        Row {
                            Text(
                                stringResource(fact.label), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                                modifier = Modifier.width(120.dp),
                            )
                            Text(fact.value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun okColor(): Color {
        val bg = MaterialTheme.colorScheme.surface
        val dark = (0.299f * bg.red + 0.587f * bg.green + 0.114f * bg.blue) < 0.5f
        return if (dark) Color(0xFF81C995) else Color(0xFF188038)
    }

    private fun sectionsOf(c: X509Certificate): List<Section> {
        val subject = dn(c.subjectX500Principal.getName("RFC2253"))
        val issuer = dn(c.issuerX500Principal.getName("RFC2253"))
        val dates = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT)
        val altNames = try {
            c.subjectAlternativeNames?.filter { (it[0] as? Int) == 2 }?.mapNotNull { it[1] as? String }
        } catch (_: Exception) { null }.orEmpty()
        val out = ArrayList<Section>()
        out += Section(R.string.sec_issued_to, listOfNotNull(
            subject["CN"]?.let { Fact(R.string.sec_common_name, it) },
            subject["O"]?.let { Fact(R.string.sec_organization, it) },
            subject["OU"]?.let { Fact(R.string.sec_org_unit, it) },
            altNames.filter { it != subject["CN"] }.takeIf { it.isNotEmpty() }?.let { names ->
                // big sites list 100+ domains: the first 8 and "+N" here, all of them in Copy
                val shown = names.take(8).joinToString(", ") + if (names.size > 8) " +${names.size - 8}" else ""
                Fact(R.string.sec_alt_names, shown, full = names.joinToString(", "))
            },
        ))
        out += Section(R.string.sec_issued_by, listOfNotNull(
            issuer["CN"]?.let { Fact(R.string.sec_common_name, it) },
            issuer["O"]?.let { Fact(R.string.sec_organization, it) },
            issuer["C"]?.let { Fact(R.string.sec_country, it) },
        ))
        out += Section(R.string.sec_validity, listOf(
            Fact(R.string.sec_issued_on, dates.format(c.notBefore)),
            Fact(R.string.sec_expires_on, dates.format(c.notAfter)),
        ))
        out += Section(R.string.sec_fingerprints, listOf(
            Fact(R.string.sec_sha256, digest(c, "SHA-256"), mono = true),
            Fact(R.string.sec_sha1, digest(c, "SHA-1"), mono = true),
        ))
        out += Section(R.string.sec_details, listOfNotNull(
            Fact(R.string.sec_serial, c.serialNumber.toByteArray().dropWhile { it == 0.toByte() && c.serialNumber.signum() >= 0 }
                .joinToString(":") { "%02X".format(it) }.ifEmpty { "00" }, mono = true),
            Fact(R.string.sec_version, c.version.toString()),
            Fact(R.string.sec_sig_alg, c.sigAlgName),
            keyOf(c)?.let { Fact(R.string.sec_public_key, it) },
        ))
        return out.filter { it.facts.isNotEmpty() }
    }

    /** "Valid · 85 days left" / "Expired" / "Not yet valid", and whether that is good. */
    private fun statusOf(ctx: Context, c: X509Certificate): Pair<String, Boolean> {
        val now = Date()
        return when {
            now.after(c.notAfter) -> ctx.getString(R.string.sec_expired) to false
            now.before(c.notBefore) -> ctx.getString(R.string.sec_not_yet) to false
            else -> {
                val days = ((c.notAfter.time - now.time) / 86_400_000L).toInt()
                ctx.resources.getQuantityString(R.plurals.sec_valid_days, days, days) to true
            }
        }
    }

    private fun keyOf(c: X509Certificate): String? = try {
        when (val k = c.publicKey) {
            is RSAPublicKey -> "RSA · ${k.modulus.bitLength()} bit"
            is ECPublicKey -> "EC · ${k.params.order.bitLength()} bit"
            else -> k.algorithm
        }
    } catch (_: Exception) { null }

    private fun digest(c: X509Certificate, alg: String): String = try {
        MessageDigest.getInstance(alg).digest(c.encoded).joinToString(":") { "%02X".format(it) }
    } catch (_: Exception) { "" }

    /** "CN=a\,b,O=Org,C=US" → {CN: "a,b", O: "Org", C: "US"} (first value of each key). */
    private fun dn(name: String): Map<String, String> {
        val out = LinkedHashMap<String, String>()
        val part = StringBuilder()
        var i = 0
        fun flush() {
            val s = part.toString(); part.clear()
            val eq = s.indexOf('=')
            if (eq > 0) out.putIfAbsent(s.substring(0, eq).trim().uppercase(), s.substring(eq + 1).trim())
        }
        while (i < name.length) {
            val ch = name[i]
            when {
                ch == '\\' && i + 1 < name.length -> { part.append(name[i + 1]); i++ }
                ch == ',' || ch == '+' -> flush()
                else -> part.append(ch)
            }
            i++
        }
        flush()
        return out
    }

    private fun copyAll(host: String, status: String?, sections: List<Section>) {
        val ctx = context
        val sb = StringBuilder()
        sb.append(ctx.getString(R.string.sec_certificate))
        if (host.isNotEmpty()) sb.append(" – ").append(host)
        sb.append('\n')
        if (status != null) sb.append(status).append('\n')
        for (s in sections) {
            sb.append('\n').append(ctx.getString(s.title)).append('\n')
            for (f in s.facts) sb.append(ctx.getString(f.label)).append(": ").append(f.full).append('\n')
        }
        com.mycompany.app.main.MainUtil.copyTextToast(ctx, R.string.copied_clipboard, "Certificate", sb.toString().trim())
    }

    override fun dismiss() {
        super.dismiss()
    }
}
