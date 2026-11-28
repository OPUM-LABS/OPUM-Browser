package com.mycompany.app.ui.net

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.net.LocalNet
import com.mycompany.app.net.NetEntry
import com.mycompany.app.net.NetSnapshot
import com.mycompany.app.net.NetTools
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.ui.components.SettingRow
import com.mycompany.app.ui.components.SettingsScaffold
import com.mycompany.app.ui.components.settingsFooter
import com.mycompany.app.ui.components.settingsHeader
import com.mycompany.app.ui.prefs.OnScreenResume

/**
 * Network check (2026-11-28, from the network error page and Site settings): the phone's networks as Android sees
 * them (which one is in use, whether it reaches the internet, addresses, gateway, DNS, Private DNS), the local network
 * permission, and DNS lookup / port check / ping / traceroute for one host. See [NetTools] for what each tool does.
 */
class NetworkCheckActivity : OpumComposeActivity() {
    companion object {
        private const val EXTRA_URL = "EXTRA_URL"

        fun open(context: Context, url: String?) {
            val intent = Intent(context, NetworkCheckActivity::class.java)
            if (!url.isNullOrEmpty()) intent.putExtra(EXTRA_URL, url)
            if (context !is android.app.Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try { context.startActivity(intent) } catch (_: Exception) {}
        }

        fun kindRes(kind: Int): Int = when (kind) {
            NetEntry.WIFI -> R.string.net_kind_wifi
            NetEntry.MOBILE -> R.string.net_kind_mobile
            NetEntry.ETHERNET -> R.string.net_kind_eth
            NetEntry.VPN -> R.string.net_kind_vpn
            else -> R.string.net_kind_other
        }
    }

    private var snapshot by mutableStateOf(NetSnapshot(emptyList()))
    private var localOk by mutableStateOf(true)
    private var host by mutableStateOf("")
    private var port by mutableStateOf("")
    private var dnsServer by mutableStateOf("")
    private var running by mutableStateOf(false)
    private val output = mutableStateListOf<String>()
    private var run: NetTools.Run? = null
    private var callback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val url = intent.getStringExtra(EXTRA_URL)
        if (!url.isNullOrEmpty()) {
            try {
                val uri = url.toUri()
                host = uri.host.orEmpty()
                port = (if (uri.port > 0) uri.port else if (uri.scheme == "http") 80 else 443).toString()
            } catch (_: Exception) {
            }
        }
        if (port.isEmpty()) port = "443"
        super.onCreate(savedInstanceState)
        refresh()
        // networks come and go while the page is open (Wi-Fi switched, mobile data on or off)
        val cm = getSystemService(ConnectivityManager::class.java)
        val cb = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) = post()
            override fun onLost(network: Network) = post()
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = post()
            private fun post() { runOnUiThread { refresh() } }
        }
        try {
            cm?.registerNetworkCallback(NetworkRequest.Builder().build(), cb)
            callback = cb
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        run?.cancel()
        val cb = callback
        if (cb != null) try { getSystemService(ConnectivityManager::class.java)?.unregisterNetworkCallback(cb) } catch (_: Exception) {}
        super.onDestroy()
    }

    private fun refresh() {
        snapshot = NetSnapshot.read(this)
        localOk = LocalNet.granted(this)
    }

    @Composable
    override fun Content() {
        OnScreenResume { refresh() }
        val ctx = this
        SettingsScaffold(title = stringResource(R.string.net_check), onBack = { finish() }) {
            if (LocalNet.applies()) {
                item {
                    SettingRow(
                        title = stringResource(R.string.net_local_perm),
                        summary = stringResource(if (localOk) R.string.ss_allowed else R.string.ss_blocked),
                        onClick = { if (!LocalNet.request(ctx)) LocalNet.openAppSettings(ctx) },
                    )
                }
            }
            settingsHeader(getString(R.string.net_networks))
            val entries = snapshot.entries
            if (entries.isEmpty()) {
                item { SettingRow(title = stringResource(R.string.net_none)) }
            }
            for (e in entries) {
                item(key = "net" + e.network.toString()) { NetworkCard(e) }
            }
            settingsHeader(getString(R.string.net_check))
            item { Tools() }
            settingsFooter(getString(R.string.net_limits))
        }
    }

    @Composable
    private fun NetworkCard(e: NetEntry) {
        val lines = ArrayList<String>()
        val state = ArrayList<String>()
        if (e.active) state.add(stringResource(R.string.net_active))
        state.add(stringResource(if (e.captivePortal) R.string.net_captive else if (e.validated) R.string.net_validated else R.string.net_not_validated))
        lines.add(state.joinToString(" · "))
        if (e.addresses.isNotEmpty()) lines.add(stringResource(R.string.net_addresses) + ": " + e.addresses.joinToString(", "))
        if (e.gateways.isNotEmpty()) lines.add(stringResource(R.string.net_gateway) + ": " + e.gateways.joinToString(", "))
        if (e.dns.isNotEmpty()) lines.add(stringResource(R.string.net_dns_servers) + ": " + e.dns.joinToString(", "))
        val pdns = when {
            !e.privateDnsName.isNullOrEmpty() -> e.privateDnsName
            e.privateDnsActive -> stringResource(R.string.mem_mode_auto)
            else -> stringResource(R.string.btn_off)
        }
        lines.add(stringResource(R.string.net_private_dns_label) + ": " + pdns)
        val title = stringResource(kindRes(e.kind)) + (e.iface?.let { "  ($it)" } ?: "")
        SettingRow(title = title, summary = lines.joinToString("\n"))
    }

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    private fun Tools() {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = host, onValueChange = { host = it.trim() }, singleLine = true,
                label = { Text(stringResource(R.string.net_host)) }, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = port, onValueChange = { v -> port = v.filter { it.isDigit() }.take(5) }, singleLine = true,
                    label = { Text(stringResource(R.string.net_port)) }, modifier = Modifier.width(110.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                )
                OutlinedTextField(
                    value = dnsServer, onValueChange = { dnsServer = it.trim() }, singleLine = true,
                    label = { Text(stringResource(R.string.net_dns_server)) }, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Done),
                )
            }
            val canRun = host.isNotEmpty() && !running
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(onClick = { start(1) }, enabled = canRun) { Text(stringResource(R.string.net_lookup)) }
                Button(onClick = { start(2) }, enabled = canRun && port.isNotEmpty()) { Text(stringResource(R.string.net_port_check)) }
                if (NetTools.pingAvailable()) {
                    Button(onClick = { start(3) }, enabled = canRun) { Text(stringResource(R.string.net_ping)) }
                    Button(onClick = { start(4) }, enabled = canRun) { Text(stringResource(R.string.net_trace)) }
                }
                if (running) OutlinedButton(onClick = { run?.cancel() }) { Text(stringResource(R.string.stop)) }
            }
            if (!NetTools.pingAvailable()) {
                Text(stringResource(R.string.net_no_ping), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (output.isNotEmpty()) {
                SelectionContainer {
                    Text(
                        output.joinToString("\n"), fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall, modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
                TextButton(onClick = { copy() }) { Text(stringResource(R.string.sec_copy)) }
            }
        }
    }

    private fun copy() {
        val cm = getSystemService(ClipboardManager::class.java) ?: return
        cm.setPrimaryClip(ClipData.newPlainText(getString(R.string.net_check), output.joinToString("\n")))
    }

    /** Networks worth trying one by one: the default first, then each Wi-Fi / mobile / Ethernet network. */
    private fun targets(): List<Pair<String, Network?>> {
        val list = ArrayList<Pair<String, Network?>>()
        val snap = NetSnapshot.read(this)
        val active = snap.active
        list.add(getString(R.string.net_default) + (active?.let { " (" + getString(kindRes(it.kind)) + ")" } ?: "") to null)
        for (e in snap.entries) {
            if (e.active || e.kind == NetEntry.VPN || e.kind == NetEntry.OTHER) continue
            list.add(getString(kindRes(e.kind)) to e.network)
        }
        return list
    }

    private fun start(tool: Int) {
        val h = host.removePrefix("http://").removePrefix("https://").substringBefore('/')
        if (h.isEmpty()) return
        val p = port.toIntOrNull() ?: 443
        val server = dnsServer
        output.clear()
        running = true
        val r = NetTools.Run()
        run = r
        val out: (String) -> Unit = { line -> runOnUiThread { output.add(line) } }
        val nets = targets()
        Thread {
            try {
                when (tool) {
                    1 -> {
                        if (server.isNotEmpty()) {
                            out("▸ ${getString(R.string.net_lookup)} $h @ $server")
                            NetTools.dnsServer(h, server, null, out)
                        } else {
                            for ((label, net) in nets) {
                                if (r.stop.get()) break
                                out("▸ ${getString(R.string.net_lookup)} $h · $label")
                                NetTools.dnsSystem(h, net, out)
                            }
                        }
                    }
                    2 -> for ((label, net) in nets) {
                        if (r.stop.get()) break
                        out("▸ ${getString(R.string.net_port_check)} $h:$p · $label")
                        NetTools.port(h, p, net, out)
                    }
                    3 -> {
                        out("▸ ${getString(R.string.net_ping)} $h")
                        NetTools.ping(h, r, out)
                    }
                    4 -> {
                        out("▸ ${getString(R.string.net_trace)} $h")
                        NetTools.traceroute(h, r, out)
                    }
                }
            } finally {
                runOnUiThread { running = false; if (run === r) run = null }
            }
        }.start()
    }
}
