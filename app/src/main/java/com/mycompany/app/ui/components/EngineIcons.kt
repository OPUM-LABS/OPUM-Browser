package com.mycompany.app.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * search engine list and the address-bar suggestions (2026-11-23). Fetched once from the engine's site with
 * [QuickIcons.fromSite] and kept in `no_backup/engine_icons/`, so later uses need no network; asked at most once per app
 * run when it fails (e.g. offline).
 */
object EngineIcons {
    private val memory = ConcurrentHashMap<Int, Bitmap>()
    private val asked = java.util.Collections.synchronizedSet(HashSet<Int>())

    private fun file(context: Context, index: Int) = File(context.noBackupFilesDir, "engine_icons/$index.png")

    /** Already in memory (no disk or network). */
    fun inMemory(index: Int): Bitmap? = memory[index]

    /** Blocking: call off the main thread. From memory, the stored file or (once per run) the engine's site. */
    fun load(context: Context, index: Int): Bitmap? {
        memory[index]?.let { return it }
        if (index !in MainConst.engineUrls.indices) return null
        val stored = file(context, index)
        if (stored.isFile) {
            val bitmap = try { BitmapFactory.decodeFile(stored.path) } catch (_: Exception) { null }
            if (MainUtil.isBitmapOk(bitmap)) {
                memory[index] = bitmap!!
                return bitmap
            }
        }
        if (!asked.add(index)) return null
        val bitmap = try { QuickIcons.fromSite(context, MainConst.engineUrls[index]) } catch (_: Exception) { null }
        if (!MainUtil.isBitmapOk(bitmap)) return null
        memory[index] = bitmap!!
        try {
            stored.parentFile?.mkdirs()
            stored.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        } catch (_: Exception) {
        }
        return bitmap
    }
}

/** The engine's site icon in a [size] square with rounded corners, else [fallback] (e.g. while offline). */
@Composable
fun EngineLogo(index: Int, size: Dp, fallback: @Composable () -> Unit) {
    val context = LocalContext.current.applicationContext
    var icon by remember(index) { mutableStateOf(EngineIcons.inMemory(index)) }
    LaunchedEffect(index) {
        if (icon == null) icon = kotlinx.coroutines.withContext(QuickIcons.loads) { EngineIcons.load(context, index) }
    }
    val bitmap = icon
    if (bitmap != null && MainUtil.isBitmapOk(bitmap)) {
        Image(bitmap.asImageBitmap(), null, Modifier.size(size).clip(RoundedCornerShape(size * 0.2f)))
    } else {
        fallback()
    }
}
