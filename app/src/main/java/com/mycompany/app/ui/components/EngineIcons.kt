package com.mycompany.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.quick.QuickIcons
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * The built-in search engines' own site icons (index into [MainConst.engineUrls]), shared by the setup wizard, the
 * search engine list, the address bar's engine button and menu, the widget and the suggestions (2026-11-23).
 *
 * They ship with the app (`assets/engine_icons/<index>.png`), so they show at once and offline. A fresh copy is
 * fetched from the engine's site ([QuickIcons.fromSite]) in the background at most every [REFRESH_DAYS] days, when
 * an icon is used, and kept in `no_backup/engine_icons/`; it takes the place of the shipped one from then on. A failed
 * fetch keeps what is there and is tried again on the next app run.
 */
object EngineIcons {
    const val REFRESH_DAYS = 30L

    private val memory = ConcurrentHashMap<Int, Bitmap>()
    /** Engines whose refresh was checked in this app run. */
    private val checked = java.util.Collections.synchronizedSet(HashSet<Int>())
    private val refresher by lazy { java.util.concurrent.Executors.newSingleThreadExecutor() }

    private fun file(context: Context, index: Int) = File(context.noBackupFilesDir, "engine_icons/$index.png")

    /**
     * The icon of engine [index]: from memory, the refreshed file or the shipped one (small PNGs, fast enough for the
     * main thread). Starts the background refresh when it is due. Null for an unknown index.
     */
    fun get(context: Context, index: Int): Bitmap? {
        if (index !in MainConst.engineUrls.indices) return null
        val app = context.applicationContext
        refreshIfDue(app, index)
        memory[index]?.let { return it }
        val bitmap = decode(file(app, index)) ?: try {
            app.assets.open("engine_icons/$index.png").use { BitmapFactory.decodeStream(it) }
        } catch (_: Exception) {
            null
        }
        if (!MainUtil.isBitmapOk(bitmap)) return null
        memory[index] = bitmap!!
        return bitmap
    }

    private fun decode(stored: File): Bitmap? =
        if (!stored.isFile) null else try { BitmapFactory.decodeFile(stored.path)?.takeIf { MainUtil.isBitmapOk(it) } } catch (_: Exception) { null }

    private fun refreshIfDue(context: Context, index: Int) {
        if (!checked.add(index)) return
        val stored = file(context, index)
        val age = System.currentTimeMillis() - stored.lastModified()
        if (stored.isFile && age in 0 until java.util.concurrent.TimeUnit.DAYS.toMillis(REFRESH_DAYS)) return
        try {
            refresher.execute {
                val bitmap = try { QuickIcons.fromSite(context, MainConst.engineUrls[index]) } catch (_: Exception) { null }
                if (!MainUtil.isBitmapOk(bitmap)) return@execute
                try {
                    stored.parentFile?.mkdirs()
                    val temp = File(stored.parentFile, "$index.tmp")
                    temp.outputStream().use { bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, it) }
                    if (temp.renameTo(stored)) memory[index] = bitmap!!
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }
    }
}

/** The engine's site icon in a [size] square with rounded corners, else [fallback]. */
@Composable
fun EngineLogo(index: Int, size: Dp, fallback: @Composable () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember(index) { EngineIcons.get(context, index) }
    if (bitmap != null && MainUtil.isBitmapOk(bitmap)) {
        Image(bitmap.asImageBitmap(), null, Modifier.size(size).clip(RoundedCornerShape(size * 0.2f)))
    } else {
        fallback()
    }
}
