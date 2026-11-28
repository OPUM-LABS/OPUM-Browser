package com.mycompany.app.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.db.book.DbBookAddr
import com.mycompany.app.db.book.SavedAddress
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.ConfirmDialog
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingSwitchRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.rememberPref
import com.mycompany.app.web.PassManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Saved addresses (2026-11-28): the switch for offering to save and fill them on pages (AddressFill), the list of the
 * workspace's addresses, and adding, changing and deleting one. Opened from Security & passwords, behind the same lock
 * as the password page. With another password manager the switch is off: that app fills addresses.
 */
class AddressSettingsActivity : OpumComposeActivity() {

    @Composable
    override fun Content() {
        val context: Context = this
        val on = rememberPref({ ZtriSettings.addrSave.value }) { ZtriSettings.edit(context) { set(ZtriSettings.addrSave, it) } }
        val builtIn = PassManager.builtIn
        var addresses by remember { mutableStateOf<List<SavedAddress>?>(null) }
        var reloadKey by remember { mutableIntStateOf(0) }
        var editing by remember { mutableStateOf<SavedAddress?>(null) }
        var resetting by remember { mutableStateOf(false) }

        LaunchedEffect(reloadKey) {
            addresses = withContext(Dispatchers.IO) { DbBookAddr.all(context) }
        }

        SettingsScaffold(
            title = stringResource(R.string.addresses),
            onBack = { finish() },
            onReset = { resetting = true },
        ) {
            item {
                SettingSwitchRow(
                    title = stringResource(R.string.addr_save),
                    summary = stringResource(R.string.addr_save_sum),
                    checked = builtIn && on.value,
                    enabled = builtIn,
                    onCheckedChange = on::set,
                )
            }
            if (!builtIn) settingsFooter(getString(R.string.addr_other_app))
            settingsHeader(getString(R.string.hd_saved_addresses))
            val list = addresses.orEmpty()
            if (addresses != null && list.isEmpty()) settingsFooter(getString(R.string.addr_none))
            for (address in list) {
                item {
                    SettingRow(
                        title = address.title(),
                        summary = address.summary().takeIf { it != address.title() },
                        onClick = { editing = address },
                    )
                }
            }
            item {
                SettingRow(
                    title = stringResource(R.string.addr_add),
                    onClick = { editing = SavedAddress() },
                )
            }
            settingsFooter(getString(R.string.addr_info))
        }

        editing?.let { address ->
            AddressDialog(
                address = address,
                onSave = { changed ->
                    editing = null
                    Thread { DbBookAddr.save(context, changed); runOnUiThread { reloadKey++ } }.start()
                },
                onDelete = if (address.id > 0) ({
                    editing = null
                    Thread { DbBookAddr.delete(context, address.id); runOnUiThread { reloadKey++ } }.start()
                }) else null,
                onDismiss = { editing = null },
            )
        }
        if (resetting) {
            ConfirmDialog(
                message = stringResource(R.string.reset_setting),
                onConfirm = { resetToDefaults(context); on.reload(ZtriSettings.addrSave.value) },
                onDismiss = { resetting = false },
            )
        }
    }

    companion object {
        /** Restores the defaults of this page (the saved addresses stay). */
        @JvmStatic
        fun resetToDefaults(context: Context) {
            if (!ZtriSettings.addrSave.isDefault) ZtriSettings.reset(context, listOf(ZtriSettings.addrSave))
        }
    }
}

/** One field of the address dialog: its label, keyboard and how it reads and writes the address. */
private class AddressField(
    val labelRes: Int,
    val type: KeyboardType,
    val caps: KeyboardCapitalization,
    val get: (SavedAddress) -> String,
    val set: (SavedAddress, String) -> SavedAddress,
)

private val addressFields = listOf(
    AddressField(R.string.addr_label, KeyboardType.Text, KeyboardCapitalization.Sentences, { it.label }, { a, v -> a.copy(label = v) }),
    AddressField(R.string.name, KeyboardType.Text, KeyboardCapitalization.Words, { it.name }, { a, v -> a.copy(name = v) }),
    AddressField(R.string.addr_org, KeyboardType.Text, KeyboardCapitalization.Words, { it.org }, { a, v -> a.copy(org = v) }),
    AddressField(R.string.addr_street, KeyboardType.Text, KeyboardCapitalization.Words, { it.street }, { a, v -> a.copy(street = v) }),
    AddressField(R.string.addr_line2, KeyboardType.Text, KeyboardCapitalization.Sentences, { it.line2 }, { a, v -> a.copy(line2 = v) }),
    AddressField(R.string.addr_postcode, KeyboardType.Text, KeyboardCapitalization.Characters, { it.postcode }, { a, v -> a.copy(postcode = v) }),
    AddressField(R.string.addr_city, KeyboardType.Text, KeyboardCapitalization.Words, { it.city }, { a, v -> a.copy(city = v) }),
    AddressField(R.string.addr_region, KeyboardType.Text, KeyboardCapitalization.Words, { it.region }, { a, v -> a.copy(region = v) }),
    AddressField(R.string.addr_country, KeyboardType.Text, KeyboardCapitalization.Words, { it.country.ifEmpty { it.countryCode } }, { a, v -> a.copy(country = v, countryCode = if (v.length == 2) v.uppercase() else "") }),
    AddressField(R.string.addr_tel, KeyboardType.Phone, KeyboardCapitalization.None, { it.tel }, { a, v -> a.copy(tel = v) }),
    AddressField(R.string.addr_email, KeyboardType.Email, KeyboardCapitalization.None, { it.email }, { a, v -> a.copy(email = v) }),
)

/** Adding or changing an address: one field per line, Next moves to the following one, Done on the last saves. */
@Composable
private fun AddressDialog(address: SavedAddress, onSave: (SavedAddress) -> Unit, onDelete: (() -> Unit)?, onDismiss: () -> Unit) {
    val values = remember { mutableStateMapOf<Int, String>().apply { addressFields.forEachIndexed { i, f -> put(i, f.get(address)) } } }
    val focus = LocalFocusManager.current
    fun result(): SavedAddress {
        var a = address
        addressFields.forEachIndexed { i, f -> a = f.set(a, (values[i] ?: "").trim().take(SavedAddress.MAX_FIELD)) }
        return a
    }
    val ready = !result().copy(label = "").isEmpty
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (address.id > 0) R.string.edit else R.string.addr_add)) },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                addressFields.forEachIndexed { i, f ->
                    val last = i == addressFields.lastIndex
                    OutlinedTextField(
                        value = values[i] ?: "",
                        onValueChange = { values[i] = it },
                        singleLine = true,
                        label = { Text(stringResource(f.labelRes)) },
                        keyboardOptions = KeyboardOptions(capitalization = f.caps, keyboardType = f.type, imeAction = if (last) ImeAction.Done else ImeAction.Next),
                        keyboardActions = KeyboardActions(
                            onNext = { focus.moveFocus(FocusDirection.Down) },
                            onDone = { if (ready) onSave(result()) },
                        ),
                        modifier = Modifier.fillMaxWidth().padding(top = if (i == 0) 0.dp else 4.dp),
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = { onSave(result()) }, enabled = ready) { Text(stringResource(R.string.save)) } },
        dismissButton = {
            androidx.compose.foundation.layout.Row {
                if (onDelete != null) TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}
