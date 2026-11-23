package com.mycompany.app.dialog

import android.graphics.Bitmap
import android.text.TextUtils
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.main.MainActivity
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil

/**
 * Save page (main menu): header with the page icon ([bitmap], cached site icon or the list loader) and title [pageTitle],
 * the name (title + ".mht") and save location; Save hands URL [pageUrl], the file name and the icon to
 * [DownPageListener] and closes. The caller passes the folder picker result to [setFolderPath]. Compose now (on
 * [SaveFolderDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogDownPage(
    mainActivity: MainActivity?,
    pageUrl: String?,
    pageTitle: String?,
    bitmap: Bitmap?,
    downPageListener: DownPageListener?,
) : SaveFolderDialog(mainActivity, ".mht") {
    @JvmField
    var pageUrl: String? = pageUrl
    @JvmField
    var pageIcon: Bitmap? = bitmap
    @JvmField
    var pageTitle: String? = pageTitle
    @JvmField
    var pageListener: DownPageListener? = downPageListener
    @JvmField
    var listLoader: MainListLoader? = null

    private var iconBmp by mutableStateOf<Bitmap?>(null)

    interface DownPageListener {
        fun onSave(url: String?, path: String?, bitmap: Bitmap?)
    }

    init {
        loadIcon()
        start()
    }

    override fun prepare(): String? = MainUtil.safeTitleFileName(186, pageTitle, "Downpage")

    @Composable
    override fun ColumnScope.Header() {
        Row(
            Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val bmp = iconBmp
            val mod = Modifier.size(40.dp).clip(CircleShape)
            if (bmp != null) {
                Image(bmp.asImageBitmap(), contentDescription = null, modifier = mod)
            } else {
                Box(mod.background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
                    Icon(painterResource(AppR.drawable.outline_public_black_24), contentDescription = null, modifier = Modifier.size(24.dp))
                }
            }
            Text(
                pageTitle ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp),
            )
        }
    }

    private fun loadIcon() {
        if (MainUtil.isBitmapOk(pageIcon)) {
            iconBmp = pageIcon
            return
        }
        if (TextUtils.isEmpty(pageUrl)) return
        val item = ChildItem()
        item.listType = 18
        item.itemType = 11.toByte()
        item.path = pageUrl
        val cached = MainListLoader.cachedIconFor(item)
        pageIcon = cached
        if (MainUtil.isBitmapOk(cached)) {
            iconBmp = cached
            return
        }
        val ctx = appContext ?: return
        val target = View(ctx)
        target.setTag(0)
        val loader = MainListLoader(ctx, false, object : ListLoadListener {
            override fun onFailed(childItem: ChildItem?, view: View?) {}
            override fun onLoaded(childItem: ChildItem?, view: View?, bitmap: Bitmap?) {
                pageIcon = bitmap
                if (MainUtil.isBitmapOk(bitmap)) iconBmp = bitmap
            }
        })
        listLoader = loader
        loader.enqueue(item, target)
    }

    override fun onSave(fileName: String) {
        val listener = pageListener
        pageListener = null
        listener?.onSave(pageUrl, fileName, pageIcon)
        dismiss()
    }

    /** A late page icon. */
    fun setIcon(bitmap: Bitmap?) {
        pageIcon = bitmap
        if (MainUtil.isBitmapOk(bitmap)) iconBmp = bitmap
    }

    /** Folder picker result path. */
    fun setFolderPath(path: String?) {
        setFolder(path)
    }

    override fun dismiss() {
        listLoader?.release()
        listLoader = null
        pageListener = null
        super.dismiss()
    }

    companion object {
    }
}
