package com.mycompany.app.ui.settings

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingsTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Shows a bundled text file (terms of service, privacy policy, a license text) from `assets/`. */
class TextAssetActivity : OpumComposeActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        val path = intent.getStringExtra(EXTRA_ASSET).orEmpty()
        val content by produceState(initialValue = "", path) {
            value = withContext(Dispatchers.IO) {
                runCatching { assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() } }.getOrDefault("")
            }
        }
        Scaffold(topBar = { SettingsTopBar(title = title, onBack = { finish() }) }) { padding ->
            SelectionContainer {
                Text(
                    text = content.replace("\r\n", "\n"),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                )
            }
        }
    }

    companion object {
        private const val EXTRA_TITLE = "EXTRA_TITLE"
        private const val EXTRA_ASSET = "EXTRA_ASSET"

        /**
         * The terms of use or the privacy policy (OPUM-Labs). English for every app language for now.
         */
        fun legalIntent(context: Context, termsOfService: Boolean): Intent {
            val language = "en"
            val titleRes = if (termsOfService) R.string.notice_tos else R.string.notice_privacy
            val file = if (termsOfService) "tos" else "privacy"
            return intent(context, context.getString(titleRes), "notice/${file}_$language.txt")
        }

        fun intent(context: Context, title: String, assetPath: String): Intent =
            Intent(context, TextAssetActivity::class.java)
                .putExtra(EXTRA_TITLE, title)
                .putExtra(EXTRA_ASSET, assetPath)
    }
}
