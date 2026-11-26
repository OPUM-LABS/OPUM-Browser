package com.mycompany.app.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.AppListItem
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsHeader

/**
 * Settings > Information > Open source licenses: one row per library ("author · license"); the row opens the
 * license text, the globe the library's website. The app's own libraries first, then the ad blocker's.
 */
class LicenseListActivity : OpumComposeActivity() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val appTitle = stringResource(R.string.license_app_libs)
        val adTitle = stringResource(R.string.license_ad_blocker)
        val siteLabel = stringResource(R.string.site)
        SettingsScaffold(title = stringResource(R.string.license), onBack = { finish() }) {
            for ((adBlock, group) in LicenseData.entries.groupBy { it.adBlock }) {
                settingsHeader(if (adBlock) adTitle else appTitle)
                items(group, key = { it.name }) { entry ->
                    AppListItem(
                        headlineContent = { Text(entry.name) },
                        supportingContent = { Text("${entry.author} · ${LicenseData.licenseName(entry.licenseId)}") },
                        trailingContent = {
                            IconButton(onClick = { openWebsite(entry.url) }) {
                                Icon(painterResource(R.drawable.outline_language_black_24), contentDescription = siteLabel)
                            }
                        },
                        modifier = Modifier.clickable {
                            startActivity(TextAssetActivity.intent(context, entry.name, "license/${entry.licenseId}.txt"))
                        },
                    )
                }
            }
        }
    }

    private fun openWebsite(url: String) {
        val intent = MainUtil.browserIntent(this)
        intent.putExtra("EXTRA_PATH", url)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }
}
