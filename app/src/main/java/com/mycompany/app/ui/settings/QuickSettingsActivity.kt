package com.mycompany.app.ui.settings

import android.app.Activity
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.PdfSettings
import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.pref.PrefPdf
import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.PrefZtri
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ColumnCountDialog
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsDivider
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.prefs.rememberPref

/** Settings > Shortcuts (quick access on the home page). */
class QuickSettingsActivity : OpumComposeActivity() {
    private var startSmall = false
    private var startPlus = false
    private var startPort = 0
    private var startLand = 0
    private var startUse = false

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        startUse = ZtriSettings.quickUse.value
        startSmall = PdfSettings.quickSmall.value
        startPlus = ZtriSettings.quickPlus.value
        startPort = ZtriSettings.quickPort.value
        startLand = ZtriSettings.quickLand.value
        super.onCreate(savedInstanceState)
    }

    override fun finish() {
        val changed = !(startSmall == PdfSettings.quickSmall.value && startPlus == ZtriSettings.quickPlus.value && startPort == ZtriSettings.quickPort.value && startLand == ZtriSettings.quickLand.value)
        val reload = startUse != ZtriSettings.quickUse.value
        if (changed || reload) {
            val result = Intent()
            if (changed) result.putExtra("change", true)
            if (reload) result.putExtra("reload", true)
            setResult(Activity.RESULT_OK, result)
        }
        super.finish()
    }

    @Composable
    override fun Content() {
        val context: Context = this
        val use = rememberPref({ ZtriSettings.quickUse.value }) { ZtriSettings.edit(context) { set(ZtriSettings.quickUse, it) } }
        // The switch shows "big icon", the stored preference is the opposite ("small").
        val bigIcon = rememberPref({ !PdfSettings.quickSmall.value }) { PdfSettings.edit(context) { set(PdfSettings.quickSmall, !it) } }
        val plus = rememberPref({ ZtriSettings.quickPlus.value }) { ZtriSettings.edit(context) { set(ZtriSettings.quickPlus, it) } }
        var showColumns by remember { mutableStateOf(false) }
        var resetting by remember { mutableStateOf(false) }

        SettingsScaffold(
            title = stringResource(R.string.quick_access),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item { SettingSwitchRow(stringResource(R.string.quick_home), use.value, use::set) }
            settingsDivider()
            item { SettingSwitchRow(stringResource(R.string.big_icon), bigIcon.value, bigIcon::set) }
            item { SettingSwitchRow(stringResource(R.string.add_icon), plus.value, plus::set) }
            item { SettingRow(title = stringResource(R.string.column_count), onClick = { showColumns = true }) }
            settingsFooter(getString(R.string.quick_guide_1) + "\n" + getString(R.string.quick_guide_2))
        }

        if (showColumns) {
            ColumnCountDialog(
                title = stringResource(R.string.column_count),
                labels = listOf(stringResource(R.string.view_port), stringResource(R.string.view_land)),
                values = listOf(ZtriSettings.quickPort.value, ZtriSettings.quickLand.value),
                options = listOf(2, 3, 4, 5, 6, 7),
                onApply = { (port, land) ->
                    if (ZtriSettings.quickPort.value != port || ZtriSettings.quickLand.value != land) {
                        ZtriSettings.editLocal { set(ZtriSettings.quickPort, port) }
                        ZtriSettings.editLocal { set(ZtriSettings.quickLand, land) }
                        val prefs = PrefZtri.get(context)!!
                        prefs.putInt(ZtriSettings.quickPort.value, "mQuickPort")
                        prefs.putInt(ZtriSettings.quickLand.value, "mQuickLand")
                        prefs.saveAsync()
                    }
                },
                onDismiss = { showColumns = false },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = {
                    resetToDefaults(context)
                    use.reload(ZtriSettings.quickUse.value)
                    bigIcon.reload(!PdfSettings.quickSmall.value)
                    plus.reload(ZtriSettings.quickPlus.value)
                },
                onDismiss = { resetting = false },
            )
        }
    }

    /** Restores the defaults of this page. */
    private fun resetToDefaults(context: Context) {
        if (!ZtriSettings.quickUse.value) ZtriSettings.reset(context, listOf(ZtriSettings.quickUse))
        if (!PdfSettings.quickSmall.value) PdfSettings.reset(context, listOf(PdfSettings.quickSmall))
        if (!ZtriSettings.quickPlus.value) ZtriSettings.reset(context, listOf(ZtriSettings.quickPlus))
        if (ZtriSettings.quickPort.value != 5 || ZtriSettings.quickLand.value != 5) {
            ZtriSettings.editLocal { set(ZtriSettings.quickPort, 5) }
            ZtriSettings.editLocal { set(ZtriSettings.quickLand, 5) }
            val prefs = PrefZtri.get(context)!!
            prefs.removeKey("mQuickPort")
            prefs.removeKey("mQuickLand")
            prefs.save()
        }
    }
}
