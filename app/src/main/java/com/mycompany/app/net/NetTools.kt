package com.mycompany.app.net

import android.net.Network
import android.os.SystemClock
import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

/**
 * The tools of the Network check page (2026-11-28). Everything here works without root:
 *  - DNS lookup with the system resolver (on one network or the default one), or a direct query to a chosen server,
 *  - port check: a TCP connection to host:port, on one network,
 *  - ping and traceroute through Android's own `/system/bin/ping` (unprivileged ICMP sockets; traceroute sends pings
 *    with a growing time-to-live and reads who answers "time to live exceeded").
 * Each call runs on the caller's thread, prints through [out] and stops when [stop] is set.
 */
object NetTools {
    private const val PING = "/system/bin/ping"

    class Run {
        val stop = AtomicBoolean(false)
        @Volatile var process: Process? = null
        fun cancel() {
            stop.set(true)
            try { process?.destroy() } catch (_: Exception) {}
        }
    }

    /** System resolver: on [network] or (null) the default network. */
    fun dnsSystem(host: String, network: Network?, out: (String) -> Unit): Boolean {
        val start = SystemClock.elapsedRealtime()
        return try {
            val list = (network?.getAllByName(host) ?: InetAddress.getAllByName(host)).toList()
            val ms = SystemClock.elapsedRealtime() - start
            for (a in list) out("  " + LocalNet.show(a) + if (LocalNet.isLocalAddress(a)) "  (local)" else "")
            out("  $ms ms")
            list.isNotEmpty()
        } catch (e: Exception) {
            out("  ✗ " + (e.message ?: e.javaClass.simpleName))
            false
        }
    }

    /** A plain DNS query (A and AAAA, UDP port 53) to [server], sent over [network] when given. */
    fun dnsServer(host: String, server: String, network: Network?, out: (String) -> Unit): Boolean {
        val serverAddr = try { InetAddress.getByName(server) } catch (e: Exception) {
            out("  ✗ $server: " + (e.message ?: "?")); return false
        }
        var any = false
        for (type in intArrayOf(1, 28)) {
            val id = Random.nextInt(0, 0xffff)
            val query = buildQuery(id, host, type)
            val start = SystemClock.elapsedRealtime()
            try {
                DatagramSocket().use { sock ->
                    network?.bindSocket(sock)
                    sock.soTimeout = 3000
                    sock.send(DatagramPacket(query, query.size, serverAddr, 53))
                    val buf = ByteArray(1500)
                    val reply = DatagramPacket(buf, buf.size)
                    sock.receive(reply)
                    val ms = SystemClock.elapsedRealtime() - start
                    val answers = parseAnswers(buf, reply.length, id)
                    val label = if (type == 1) "A" else "AAAA"
                    if (answers == null) {
                        out("  $label: ✗ (${rcodeName(buf)})  $ms ms")
                    } else if (answers.isEmpty()) {
                        out("  $label: —  $ms ms")
                    } else {
                        any = true
                        for (a in answers) out("  $label: $a" + if (LocalNet.isLocalAddress(InetAddress.getByName(a))) "  (local)" else "")
                        out("  $ms ms")
                    }
                }
            } catch (e: Exception) {
                out("  ✗ " + (e.message ?: e.javaClass.simpleName))
                return any
            }
        }
        return any
    }

    /** A TCP connection to [host]:[port] over [network] (null: the default network). */
    fun port(host: String, port: Int, network: Network?, out: (String) -> Unit): Boolean {
        val addrs = try { (network?.getAllByName(host) ?: InetAddress.getAllByName(host)).toList() } catch (e: Exception) {
            out("  ✗ DNS: " + (e.message ?: "?")); return false
        }
        for (a in addrs.take(2)) {
            val start = SystemClock.elapsedRealtime()
            try {
                Socket().use { s ->
                    network?.bindSocket(s)
                    s.connect(InetSocketAddress(a, port), 5000)
                    out("  ✓ ${LocalNet.show(a)}:$port  ${SystemClock.elapsedRealtime() - start} ms")
                }
                return true
            } catch (e: Exception) {
                out("  ✗ ${LocalNet.show(a)}:$port  " + (e.message ?: e.javaClass.simpleName))
            }
        }
        return false
    }

    fun pingAvailable(): Boolean = try { java.io.File(PING).canExecute() } catch (_: Exception) { false }

    fun ping(host: String, run: Run, out: (String) -> Unit): Boolean {
        val v6 = isV6(host)
        val cmd = listOf(if (v6) "/system/bin/ping6" else PING, "-n", "-c", "4", "-W", "2", host)
        return exec(cmd, run) { line -> if (line.isNotBlank()) out("  $line") } == 0
    }

    /** Hop by hop: one ping per time-to-live, until the host itself answers. */
    fun traceroute(host: String, run: Run, out: (String) -> Unit) {
        val target = try { InetAddress.getByName(host) } catch (e: Exception) {
            out("  ✗ DNS: " + (e.message ?: "?")); return
        }
        val ip = LocalNet.show(target)
        out("  → $ip")
        val bin = if (target is java.net.Inet6Address) "/system/bin/ping6" else PING
        var silent = 0
        for (ttl in 1..30) {
            if (run.stop.get()) return
            val start = SystemClock.elapsedRealtime()
            var from: String? = null
            var reached = false
            exec(listOf(bin, "-n", "-c", "1", "-W", "2", "-t", "$ttl", ip), run) { line ->
                val m = FROM.find(line)
                if (m != null) from = m.groupValues[1].trimEnd(':')
                val r = REPLY.find(line)
                if (r != null) { from = r.groupValues[1].trimEnd(':'); reached = true }
            }
            val ms = SystemClock.elapsedRealtime() - start
            val hop = ttl.toString().padStart(2)
            if (from == null) {
                out("  $hop  *")
                if (++silent >= 5) { out("  …"); return }
            } else {
                silent = 0
                out("  $hop  $from  " + (if (reached) "$ms ms" else "~$ms ms"))
            }
            if (reached || from == ip) return
        }
    }

    private val FROM = Regex("""[Ff]rom ([0-9a-fA-F.:]+)""")
    private val REPLY = Regex("""bytes from ([0-9a-fA-F.:]+)""")

    private fun isV6(host: String) = host.contains(':')

    private fun exec(cmd: List<String>, run: Run, line: (String) -> Unit): Int {
        return try {
            val p = ProcessBuilder(cmd).redirectErrorStream(true).start()
            run.process = p
            p.inputStream.bufferedReader().useLines { seq ->
                for (l in seq) {
                    if (run.stop.get()) break
                    line(l)
                }
            }
            if (run.stop.get()) { p.destroy(); -1 } else p.waitFor()
        } catch (e: Exception) {
            line("✗ " + (e.message ?: e.javaClass.simpleName))
            -1
        } finally {
            run.process = null
        }
    }

    private fun buildQuery(id: Int, host: String, type: Int): ByteArray {
        val b = ByteArrayOutputStream()
        b.write(id shr 8); b.write(id and 0xff)
        b.write(0x01); b.write(0x00) // recursion desired
        b.write(0); b.write(1) // one question
        repeat(6) { b.write(0) }
        for (label in host.trimEnd('.').split('.')) {
            val bytes = label.toByteArray(Charsets.UTF_8)
            b.write(bytes.size); b.write(bytes)
        }
        b.write(0)
        b.write(type shr 8); b.write(type and 0xff)
        b.write(0); b.write(1) // IN
        return b.toByteArray()
    }

    private fun rcodeName(buf: ByteArray): String = when (buf[3].toInt() and 0x0f) {
        1 -> "FORMERR"; 2 -> "SERVFAIL"; 3 -> "NXDOMAIN"; 4 -> "NOTIMP"; 5 -> "REFUSED"; else -> "RCODE " + (buf[3].toInt() and 0x0f)
    }

    /** Addresses in a DNS reply, null when the server reported an error. */
    private fun parseAnswers(buf: ByteArray, len: Int, id: Int): List<String>? {
        if (len < 12) return null
        fun u16(i: Int) = ((buf[i].toInt() and 0xff) shl 8) or (buf[i + 1].toInt() and 0xff)
        if (u16(0) != id) return null
        if ((buf[3].toInt() and 0x0f) != 0) return null
        val qd = u16(4)
        val an = u16(6)
        var i = 12
        fun skipName() {
            while (i < len) {
                val l = buf[i].toInt() and 0xff
                if (l == 0) { i++; return }
                if ((l and 0xc0) == 0xc0) { i += 2; return }
                i += l + 1
            }
        }
        repeat(qd) { skipName(); i += 4 }
        val result = ArrayList<String>()
        repeat(an) {
            if (i >= len) return result
            skipName()
            if (i + 10 > len) return result
            val type = u16(i)
            val rdLen = u16(i + 8)
            i += 10
            if (i + rdLen > len) return result
            if ((type == 1 && rdLen == 4) || (type == 28 && rdLen == 16)) {
                val addr = InetAddress.getByAddress(buf.copyOfRange(i, i + rdLen))
                result.add(LocalNet.show(addr))
            }
            i += rdLen
        }
        return result
    }
}
