package com.mycompany.app.ui.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider

/**
 * Settings > About (was "Information"; rate, share and feedback moved in from the settings start page on 2026-11-19):
 * app version, rate / share / feedback, legal texts and open source licenses.
 */
class InfoSettingsActivity : OpumComposeActivity() {
    private var versionTaps = 0
    private var lastVersionTap = 0L

    /** Seven quick taps on the version open the ibex game. */
    private fun versionTapped() {
        val now = android.os.SystemClock.elapsedRealtime()
        versionTaps = if (now - lastVersionTap < 1500) versionTaps + 1 else 1
        lastVersionTap = now
        if (versionTaps >= 7) {
            versionTaps = 0
            com.mycompany.app.ui.game.IbexGameActivity.open(this)
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
                SettingRow(
                    title = stringResource(R.string.app_ver),
                    summary = MainUtil.appVersion(context).orEmpty(),
                    onClick = { versionTapped() },
                )
            }
            item {
                SettingRow(
                    title = stringResource(R.string.rate_app),
                    onClick = { MainUtil.openPlayStore(this@InfoSettingsActivity, packageName) },
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
            item {
                SettingRow(
                    title = stringResource(R.string.license),
                    onClick = { startActivity(Intent(context, LicenseListActivity::class.java)) },
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
}
