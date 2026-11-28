package com.mycompany.app.net

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.InetAddress

/**
 * Android 17's local network protection (2026-11-28): an app that targets SDK 37 may only reach addresses in the local
 * network (a router, a NAS, a Proxmox server at 10.0.0.2) with the runtime permission ACCESS_LOCAL_NETWORK, and the
 * pages in its WebView inherit that. Without it, local pages fail with a timeout or "access denied".
 */
object LocalNet {
    const val PERMISSION = "android.permission.ACCESS_LOCAL_NETWORK"
    /** Request code of the permission prompt (WebViewActivity reloads the page when it is granted). */
    const val REQUEST_CODE = 41
    private const val PREFS = "local_net"
    private const val KEY_ASKED = "asked"

    /** The rule applies to this phone (Android 17 or later). */
    fun applies(): Boolean = Build.VERSION.SDK_INT >= 37

    fun granted(context: Context): Boolean =
        !applies() || ContextCompat.checkSelfPermission(context, PERMISSION) == PackageManager.PERMISSION_GRANTED

    /**
     * Asks for the permission, or opens the app's Android settings when Android no longer shows the prompt (the user
     * said no before and Android remembers it). Returns false when there was nothing to ask.
     */
    fun request(activity: Activity): Boolean {
        if (granted(activity)) return false
        val prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val askedBefore = prefs.getBoolean(KEY_ASKED, false)
        if (askedBefore && !ActivityCompat.shouldShowRequestPermissionRationale(activity, PERMISSION)) {
            openAppSettings(activity)
            return true
        }
        prefs.edit { putBoolean(KEY_ASKED, true) }
        ActivityCompat.requestPermissions(activity, arrayOf(PERMISSION), REQUEST_CODE)
        return true
    }

    fun openAppSettings(context: Context) {
        try {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (_: Exception) {
        }
    }

    /** The host is a name that only a local network knows (`nas.local`, `proxmox`, `router.lan`). */
    fun isLocalName(host: String): Boolean {
        val h = host.lowercase().trimEnd('.')
        if (h.isEmpty() || h == "localhost") return false
        if (isIpLiteral(h)) return false
        if (!h.contains('.')) return true
        return LOCAL_SUFFIXES.any { h.endsWith(it) }
    }

    /** [address] is in a private, link-local or unique-local range (not loopback: that is the phone itself). */
    fun isLocalAddress(address: InetAddress): Boolean {
        if (address.isLoopbackAddress) return false
        if (address.isSiteLocalAddress || address.isLinkLocalAddress) return true
        if (address is Inet6Address) {
            val first = address.address[0].toInt() and 0xff
            return (first and 0xfe) == 0xfc // fc00::/7
        }
        return false
    }

    /** The host written as an IP address, parsed without a DNS lookup (null for names). */
    fun literal(host: String): InetAddress? {
        val h = host.trim().removePrefix("[").removeSuffix("]")
        if (!isIpLiteral(h)) return null
        return try { InetAddress.getByName(h) } catch (_: Exception) { null }
    }

    /** A host that is local without asking DNS: a private IP address or a local name. */
    fun isLocalHost(host: String?): Boolean {
        if (host.isNullOrEmpty()) return false
        val ip = literal(host)
        return if (ip != null) isLocalAddress(ip) else isLocalName(host)
    }

    private fun isIpLiteral(h: String): Boolean {
        val s = h.removePrefix("[").removeSuffix("]")
        if (s.contains(':')) return s.all { it.isDigit() || it in 'a'..'f' || it in 'A'..'F' || it == ':' || it == '.' || it == '%' }
        val parts = s.split('.')
        return parts.size == 4 && parts.all { p -> p.isNotEmpty() && p.length <= 3 && p.all { it.isDigit() } && p.toInt() <= 255 }
    }

    private val LOCAL_SUFFIXES = listOf(".local", ".lan", ".home", ".home.arpa", ".internal", ".localdomain", ".intranet", ".corp", ".fritz.box")

    /** Short form of an address for the screens. */
    fun show(address: InetAddress): String = when (address) {
        is Inet4Address -> address.hostAddress ?: ""
        else -> (address.hostAddress ?: "").substringBefore('%')
    }
}
