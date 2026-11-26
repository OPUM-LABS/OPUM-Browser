package com.mycompany.app.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsHeader

/**
 * Settings > About (was "Information"; rate, share and feedback moved in from the settings start page on 2026-11-19,
 * logo header and source code added 2026-11-26): the app logo with name, version and author (seven quick taps on the
 * logo open the ibex game, the last four counted down in a toast), support (rate / coffee / share / feedback), open
 * source (the code on GitHub, the licenses) and the legal texts.
 */
class InfoSettingsActivity : OpumComposeActivity() {
    private var logoTaps = 0
    private var lastLogoTap = 0L
    private var countdown: Toast? = null

    /** Opens [url] in a new tab of the browser. */
    private fun openInBrowser(url: String) {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, url.toUri())
                    .setClass(this, com.mycompany.app.web.WebViewActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            )
        } catch (_: Exception) {
        }
    }

    /** Seven quick taps on the logo open the ibex game; from the third tap a toast counts the rest down. */
    private fun logoTapped() {
        val now = android.os.SystemClock.elapsedRealtime()
        logoTaps = if (now - lastLogoTap < 1500) logoTaps + 1 else 1
        lastLogoTap = now
        countdown?.cancel()
        countdown = null
        if (logoTaps >= TAPS) {
            logoTaps = 0
            com.mycompany.app.ui.game.IbexGameActivity.open(this)
        } else if (logoTaps >= 3) {
            countdown = Toast.makeText(this, getString(R.string.taps_to_go, TAPS - logoTaps), Toast.LENGTH_SHORT).also { it.show() }
        }
    }

    override fun onPause() {
        countdown?.cancel()
        super.onPause()
    }

    /** The launcher icon's two layers, cut like the home screen does (the middle 72 of 108 dp). */
    @Composable
    private fun Logo(onClick: () -> Unit) {
        val size = 96.dp
        Box(
            modifier = Modifier.size(size).clip(RoundedCornerShape(26.dp)).clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(painterResource(R.drawable.ic_launcher_background), null, Modifier.requiredSize(size * 1.5f))
            Image(painterResource(R.drawable.ic_launcher_foreground), null, Modifier.requiredSize(size * 1.5f))
        }
    }

    @Composable
    override fun Content() {
        val context = LocalContext.current
        val language = "en" // the legal texts exist in English only (see TextAssetActivity.legalIntent)
        val tosTitle = stringResource(R.string.notice_tos)
        val privacyTitle = stringResource(R.string.notice_privacy)
        SettingsScaffold(title = stringResource(R.string.set_about), onBack = { finish() }) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Logo(onClick = { logoTapped() })
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleLarge)
                    Text(
                        stringResource(R.string.version_n, MainUtil.appVersion(context).orEmpty()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.made_by, AUTHOR),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            settingsHeader(getString(R.string.hd_support))
            item {
                SettingRow(
                    title = stringResource(R.string.rate_app),
                    onClick = { MainUtil.openPlayStore(this@InfoSettingsActivity, packageName) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.buy_coffee),
                    summary = stringResource(R.string.buy_coffee_info),
                    onClick = { openInBrowser(COFFEE_URL) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.share_app),
                    onClick = {
                        MainUtil.shareText(
                            this@InfoSettingsActivity,
                            "https://play.google.com/store/apps/details?id=$packageName",
                            getString(R.string.app_name),
                        )
                    },
                )
            }
            item {
                SettingRow(title = stringResource(R.string.feedback), onClick = { sendFeedback() })
            }
            settingsHeader(getString(R.string.hd_open_source))
            item {
                SettingRow(
                    title = stringResource(R.string.source_code),
                    summary = stringResource(R.string.source_code_sum) + " · " + SOURCE_URL.removePrefix("https://"),
                    onClick = { openInBrowser(SOURCE_URL) },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.license),
                    onClick = { startActivity(Intent(context, LicenseListActivity::class.java)) },
                )
            }
            settingsHeader(getString(R.string.hd_legal))
            item {
                SettingRow(
                    title = tosTitle,
                    onClick = { startActivity(TextAssetActivity.intent(context, tosTitle, "notice/tos_$language.txt")) },
                )
            }
            item {
                SettingRow(
                    title = privacyTitle,
                    onClick = { startActivity(TextAssetActivity.intent(context, privacyTitle, "notice/privacy_$language.txt")) },
                )
            }
        }
    }

    private fun sendFeedback() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = android.net.Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("app@opum.ch"))
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback))
            intent.putExtra(Intent.EXTRA_TEXT, MainUtil.feedbackText(this, this.intent.getStringExtra(EXTRA_PATH)))
            startActivity(intent)
        } catch (unused: Exception) {
            MainUtil.showToast(this, R.string.apps_none)
        }
    }

    companion object {
        const val COFFEE_URL = "https://buymeacoffee.com/opum_labs"
        const val SOURCE_URL = "https://github.com/OPUM-LABS/OPUM-Browser"
        /** The person behind OPUM Labs; a name, so the same in every language. */
        const val AUTHOR = "Michel Perrin"
        private const val TAPS = 7
    }
}
