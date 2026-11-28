package com.mycompany.app.net

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import java.net.InetAddress
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** One network the phone has (Wi-Fi, mobile data, VPN, Ethernet), as Android reports it. */
class NetEntry(
    val network: Network,
    val kind: Int,
    val active: Boolean,
    /** Android checked that this network reaches the internet. */
    val validated: Boolean,
    val captivePortal: Boolean,
    val iface: String?,
    val addresses: List<String>,
    val gateways: List<String>,
    val dns: List<String>,
    val privateDnsActive: Boolean,
    val privateDnsName: String?,
) {
    companion object {
        const val WIFI = 1
        const val MOBILE = 2
        const val ETHERNET = 3
        const val VPN = 4
        const val OTHER = 5
    }

    /** A network that can lead into a home or office network. */
    val isLan: Boolean get() = kind == WIFI || kind == ETHERNET
}

/** What the phone's networks look like right now. */
class NetSnapshot(val entries: List<NetEntry>) {
    val active: NetEntry? get() = entries.firstOrNull { it.active }
    val wifi: NetEntry? get() = entries.firstOrNull { it.kind == NetEntry.WIFI } ?: entries.firstOrNull { it.kind == NetEntry.ETHERNET }
    val mobile: NetEntry? get() = entries.firstOrNull { it.kind == NetEntry.MOBILE }
    /** The network the browser's traffic really uses (a VPN counts when it is the default). */
    val vpnActive: Boolean get() = entries.any { it.kind == NetEntry.VPN && it.active } || (active?.kind == NetEntry.VPN)

    companion object {
        @Suppress("DEPRECATION") // allNetworks: still the only call that lists every network without a callback
        fun read(context: Context): NetSnapshot {
            val cm = context.getSystemService(ConnectivityManager::class.java) ?: return NetSnapshot(emptyList())
            val activeNet = cm.activeNetwork
            val list = ArrayList<NetEntry>()
            val networks = try { cm.allNetworks } catch (_: Exception) { emptyArray() }
            for (n in networks) {
                val caps = cm.getNetworkCapabilities(n) ?: continue
                val lp: LinkProperties? = cm.getLinkProperties(n)
                val kind = when {
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> NetEntry.VPN
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetEntry.WIFI
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetEntry.MOBILE
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetEntry.ETHERNET
                    else -> NetEntry.OTHER
                }
                list.add(
                    NetEntry(
                        network = n,
                        kind = kind,
                        active = n == activeNet,
                        validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED),
                        captivePortal = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL),
                        iface = lp?.interfaceName,
                        addresses = lp?.linkAddresses?.map { it.address.let(LocalNet::show) + "/" + it.prefixLength }.orEmpty(),
                        gateways = lp?.routes?.filter { it.isDefaultRoute && it.gateway != null && !it.gateway!!.isAnyLocalAddress }
                            ?.mapNotNull { it.gateway?.let(LocalNet::show) }?.distinct().orEmpty(),
                        dns = lp?.dnsServers?.map(LocalNet::show).orEmpty(),
                        privateDnsActive = lp?.isPrivateDnsActive == true,
                        privateDnsName = lp?.privateDnsServerName,
                    )
                )
            }
            // the active network first, then Wi-Fi, mobile, the rest
            list.sortWith(compareBy({ !it.active }, { it.kind }))
            return NetSnapshot(list)
        }
    }
}

/** Why a page could not be opened, worked out from the error and the networks. */
enum class NetCause {
    OFFLINE,
    LOCAL_PERMISSION,
    LOCAL_MOBILE,
    LOCAL_NO_WIFI,
    LOCAL_VPN,
    LOCAL_NO_ANSWER,
    CAPTIVE,
    NO_INTERNET,
    DNS,
    SITE,
}

class NetDiagnosis(val cause: NetCause, val snapshot: NetSnapshot, val resolved: List<String>)

object NetDiag {
    /** WebViewClient error codes for "the network did not get the page" (not bad URLs, files or certificates). */
    fun isNetworkError(code: Int, description: String?): Boolean {
        return when (code) {
            -2, -6, -7, -8 -> true // HOST_LOOKUP, CONNECT, IO, TIMEOUT
            -1 -> {
                val d = description ?: return false
                NET_WORDS.any { d.contains(it) }
            }
            else -> false
        }
    }

    private val NET_WORDS = listOf(
        "ADDRESS_UNREACHABLE", "INTERNET_DISCONNECTED", "NETWORK_CHANGED", "NETWORK_ACCESS_DENIED", "ERR_ACCESS_DENIED",
        "LOCAL_NETWORK", "PRIVATE_NETWORK", "CONNECTION_", "NAME_NOT_RESOLVED", "NAME_RESOLUTION", "DNS",
    )

    /** Works the cause out; does a short DNS lookup, so call it off the main thread. */
    fun diagnose(context: Context, host: String?, code: Int): NetDiagnosis {
        val snap = NetSnapshot.read(context)
        val h = host.orEmpty()
        var resolved: List<InetAddress> = emptyList()
        var local = LocalNet.isLocalHost(h)
        if (!local && h.isNotEmpty() && LocalNet.literal(h) == null && code != -2) {
            resolved = lookup(h, 2500)
            local = resolved.isNotEmpty() && resolved.all { LocalNet.isLocalAddress(it) }
        }
        val active = snap.active
        val cause = when {
            active == null -> NetCause.OFFLINE
            local && !LocalNet.granted(context) -> NetCause.LOCAL_PERMISSION
            local && snap.vpnActive -> NetCause.LOCAL_VPN
            local && !active.isLan && snap.wifi != null -> NetCause.LOCAL_MOBILE
            local && snap.wifi == null && !active.isLan -> NetCause.LOCAL_NO_WIFI
            local -> if (code == -2) NetCause.DNS else NetCause.LOCAL_NO_ANSWER
            active.captivePortal -> NetCause.CAPTIVE
            !active.validated -> NetCause.NO_INTERNET
            code == -2 -> NetCause.DNS
            else -> NetCause.SITE
        }
        return NetDiagnosis(cause, snap, resolved.map(LocalNet::show))
    }

    /** System DNS lookup with a time limit (getaddrinfo itself can hang for a long time). */
    fun lookup(host: String, timeoutMs: Long, network: Network? = null): List<InetAddress> {
        val pool = Executors.newSingleThreadExecutor()
        return try {
            pool.submit(Callable { (network?.getAllByName(host) ?: InetAddress.getAllByName(host)).toList() })
                .get(timeoutMs, TimeUnit.MILLISECONDS)
        } catch (_: Exception) {
            emptyList()
        } finally {
            pool.shutdownNow()
        }
    }
}
