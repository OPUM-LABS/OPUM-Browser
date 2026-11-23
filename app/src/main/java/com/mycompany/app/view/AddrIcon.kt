package com.mycompany.app.view

import android.graphics.Bitmap
import com.mycompany.app.pref.store.ZtwoSettings
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.mycompany.app.db.book.DbBookSearch
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefZtwo

/**
 * One icon button of the address editor ([EditorBar]) as plain state: the engine icon, clear, copy and delete. It keeps
 * the method names `WebViewActivity` used on the old `MyIconView` widgets (`setImageResource`, `setMaxAlpha`,
 * `setBgPreColor`, `setVisibility`, `setOnClickListener`, `applyShape`, `updateEngineIcon`, `release`), so the activity code that themes and wires them
 * did not change; the bar reads this state and draws it.
 */
class AddrIcon(private val owner: EditorBar) {
    var res by mutableIntStateOf(0)
    var bitmap by mutableStateOf<Bitmap?>(null)
    var visible by mutableStateOf(true)
    var alpha by mutableFloatStateOf(1f)
    var press by mutableIntStateOf(0)
    var click: View.OnClickListener? = null

    fun setImageResource(resource: Int) { res = resource; bitmap = null }
    fun setImageBitmap(image: Bitmap?) { bitmap = image }
    fun setMaxAlpha(value: Float) { alpha = value }
    fun setBgPreColor(color: Int) { press = color }
    fun setVisibility(value: Int) { visible = value == View.VISIBLE }
    fun setOnClickListener(listener: View.OnClickListener?) { click = listener }

    /** Old style flags of `MyIconView` (circle highlight); the Material icon needs none. */
    @Suppress("UNUSED_PARAMETER")
    fun applyShape(rounded: Boolean, onStart: Boolean) {}

    /** Releases the icon (the old widget released its resources here). */
    fun release() { click = null; bitmap = null }

    /** Shows the current search engine: built-in logo, custom colour dot, or the saved icon (loaded off the main thread). */
    fun updateEngineIcon() {
        if (ZtwoSettings.searchType.value < 10) {
            setImageResource(MainConst.engineIcons[ZtwoSettings.searchType.value])
            return
        }
        val colour = ZtwoSettings.searchColor.value
        if (colour != 0) {
            setImageResource(DbBookSearch.iconRes(colour))
            return
        }
        val cached = DbBookSearch.cachedIcon(ZtwoSettings.searchUser.value)
        if (MainUtil.isBitmapOk(cached)) {
            setImageBitmap(cached)
            return
        }
        val context = owner.context
        val url = ZtwoSettings.searchUser.value
        val id = (ZtwoSettings.searchType.value - 100).toLong()
        MainApp.runInBackground(context, Runnable {
            val loaded = DbBookSearch.iconById(context, id)
            DbBookSearch.cacheEngineIcon(url, loaded)
            owner.post {
                if (MainUtil.isBitmapOk(loaded)) setImageBitmap(loaded) else setImageResource(DbBookSearch.iconRes(-65536))
            }
        })
    }

    /** Called by the bar when this icon is tapped; the listeners get the bar as the `View` (their anchor). */
    fun performClick() { click?.onClick(owner) }
}
