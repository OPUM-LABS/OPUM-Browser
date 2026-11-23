package com.mycompany.app.ui.settings

import com.mycompany.app.ui.components.AppListItem
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsScaffold

/** Settings > Information > Open source licenses. */
class LicenseListActivity : OpumComposeActivity() {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val entries = LicenseData.entries
        SettingsScaffold(title = stringResource(R.string.license), onBack = { finish() }) {
            items(entries) { entry ->
                val licenseLabel = licenseName(entry.licenseId)
                AppListItem(
                    headlineContent = { Text(entry.name) },
                    supportingContent = {
                        Column {
                            Text(entry.author)
                            Row {
                                TextButton(onClick = { openWebsite(entry.url) }) { Text(stringResource(R.string.site)) }
                                TextButton(onClick = {
                                    startActivity(TextAssetActivity.intent(context, entry.name, "license/${entry.licenseId}.txt"))
                                }) { Text(licenseLabel) }
                            }
                        }
                    },
                )
            }
        }
    }

    private fun openWebsite(url: String) {
        val intent = MainUtil.browserIntent(this)
        intent.putExtra("EXTRA_PATH", url)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun licenseName(id: String): String = when (id) {
        "Apache2" -> "Apache License 2.0"
        "MIT" -> "MIT License"
        "MPL" -> "Mozilla Public License 1.1"
        "MPL2" -> "Mozilla Public License 2.0"
        "GPL3" -> "GNU General Public License 3.0"
        "Unicode" -> "Unicode License v3"
        "Glide" -> "Glide license"
        "NanoHttpd" -> "NanoHttpd license"
        "XZ" -> "XZ for Java license"
        else -> id
    }
}
