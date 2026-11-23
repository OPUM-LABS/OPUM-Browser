package com.mycompany.app.dialog

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.async.MyAsyncTask
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainUtil
import com.mycompany.app.ui.browser.ComposeBottomDialog
import java.lang.ref.WeakReference
import java.util.Collections

/**
 * "Open with" / app picker: the apps that can take [fileUrl] (a link, or for the settings pickers only a MIME type [mimeType]),
 * found with the old intent queries in [DialogTask] (web search, view and send, by address and by type; this app and
 * one download manager left out; when the first app found is Chrome, a "Chrome Custom Tab" entry is added for the
 * 8-argument constructor), sorted by name. A 4-column grid of icons and names; tap calls [SetDownListener.onPick] with
 * (name, package, activity) and closes; "No apps" when nothing was found. Compose now (on [ComposeBottomDialog]); same
 * constructors and listener, shows itself once the list is loading.
 */
@Suppress("deprecation")
class DialogSetDown : ComposeBottomDialog {
    @JvmField var hostActivity: Activity? = null
    @JvmField var appContext: Context? = null
    @JvmField var chooserMode: Boolean
    @JvmField var downListener: SetDownListener? = null
    @JvmField var loadTask: DialogTask? = null
    @JvmField var fileUrl: String? = null
    @JvmField var mimeType: String? = null

    private var loading by mutableStateOf(true)
    private var apps by mutableStateOf<List<Pair<ChildItem, Bitmap?>>>(emptyList())

    constructor(
        activity: Activity?,
        str: String?,
        z: Boolean,
        setDownListener: SetDownListener?,
    ) : super(activity) {
        this.chooserMode = false
        setup(activity, null, str, setDownListener)
    }

    constructor(
        activity: Activity?,
        str: String?,
        str2: String?,
        z: Boolean,
        z2: Boolean,
        z3: Boolean,
        i: Int,
        setDownListener: SetDownListener?,
    ) : super(activity) {
        this.chooserMode = true
        setup(activity, str, str2, setDownListener)
    }

    private fun setup(activity: Activity?, url: String?, action: String?, setDownListener: SetDownListener?) {
        hostActivity = activity
        appContext = context
        downListener = setDownListener
        fileUrl = url
        mimeType = action
        setSheet {
            Box(Modifier.fillMaxWidth().heightIn(min = 100.dp), contentAlignment = Alignment.Center) {
                when {
                    loading -> CircularProgressIndicator(Modifier.padding(24.dp))
                    apps.isEmpty() -> Text(
                        stringResource(AppR.string.apps_none),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                    )
                    else -> Grid()
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { dismiss() }) { Text(stringResource(AppR.string.close)) }
            }
        }
        show()
        loadTask?.cancelled = true
        val task = DialogTask(this, fileUrl, mimeType)
        loadTask = task
        task.start(appContext)
    }

    @androidx.compose.runtime.Composable
    private fun Grid() {
        Column(Modifier.fillMaxWidth().heightIn(max = 420.dp).verticalScroll(rememberScrollState()).padding(horizontal = 8.dp, vertical = 8.dp)) {
            for (row in apps.chunked(4)) {
                Row(Modifier.fillMaxWidth()) {
                    for ((item, icon) in row) {
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { pick(item) }.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            if (icon != null) {
                                Image(icon.asImageBitmap(), null, Modifier.size(48.dp))
                            } else {
                                Box(Modifier.size(48.dp))
                            }
                            Text(
                                item.name ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                        }
                    }
                    repeat(4 - row.size) { Box(Modifier.weight(1f)) }
                }
            }
        }
    }

    /** report (name, package, activity) and close. */
    private fun pick(item: ChildItem) {
        val listener = downListener ?: return
        listener.onPick(item.name, item.path, item.subText)
        dismiss()
    }

    class DialogTask(dialogSetDown: DialogSetDown?, name: String?, path: String?) : MyAsyncTask() {
        @JvmField var dialogRef: WeakReference<*>? = WeakReference<Any?>(dialogSetDown)
        @JvmField var name: String? = name
        @JvmField var path: String? = path
        @JvmField var items: MutableList<ChildItem?>? = null
        private var icons: List<Bitmap?> = emptyList()

        public override fun doInBackground() {
            val dialogSetDown = this.dialogRef!!.get() as DialogSetDown?
            if (dialogSetDown == null || this.cancelled) {
                return
            }
            var path = this.path
            val pathEmpty = TextUtils.isEmpty(path)
            if (pathEmpty) {
                path = "*/*"
            }
            var name = this.name
            var hasApp = true
            if (TextUtils.isEmpty(name)) {
                if (path!!.startsWith("image")) {
                    name = "https://test.com/test.jpg"
                } else if (path.startsWith("video")) {
                    name = "https://test.com/test.mp4"
                } else {
                    hasApp = false
                    name = "https://test.com"
                }
            }
            val intent = Intent("android.intent.action.WEB_SEARCH")
            intent.setData(Uri.parse(name))
            val listA = dialogSetDown.withExtraApps(intent, null)
            val openIntent = Intent("android.intent.action.VIEW")
            openIntent.setData(Uri.parse(name))
            val withExtraApps = dialogSetDown.withExtraApps(openIntent, listA)
            val sendIntent = Intent("android.intent.action.SEND")
            sendIntent.setData(Uri.parse(name))
            val withSend = dialogSetDown.withExtraApps(sendIntent, withExtraApps)
            val textIntent = Intent("android.intent.action.SEND")
            textIntent.setType("text/plain")
            textIntent.putExtra("android.intent.extra.TEXT", name)
            val withText = dialogSetDown.withExtraApps(textIntent, withSend)
            val searchIntent = Intent("android.intent.action.WEB_SEARCH")
            searchIntent.setType(path)
            val withSearch = dialogSetDown.withExtraApps(searchIntent, withText)
            val typedViewIntent = Intent("android.intent.action.VIEW")
            typedViewIntent.setType(path)
            val withTypedView = dialogSetDown.withExtraApps(typedViewIntent, withSearch)
            val typedSendIntent = Intent("android.intent.action.SEND")
            typedSendIntent.setType(path)
            var items = dialogSetDown.withExtraApps(
                viewIntent("android.intent.action.SEND", name, path), dialogSetDown.withExtraApps(
                    viewIntent("android.intent.action.VIEW", name, path),
                    dialogSetDown.withExtraApps(viewIntent("android.intent.action.WEB_SEARCH", name, path), dialogSetDown.withExtraApps(typedSendIntent, withTypedView)),
                ),
            )
            if (!pathEmpty && hasApp) {
                items = dialogSetDown.withExtraApps(
                    viewIntent("android.intent.action.SEND", "https://test.com", "*/*"), dialogSetDown.withExtraApps(
                        viewIntent("android.intent.action.VIEW", "https://test.com", "*/*"),
                        dialogSetDown.withExtraApps(viewIntent("android.intent.action.WEB_SEARCH", "https://test.com", "*/*"), items),
                    ),
                )
            }
            if (items != null && !items.isEmpty()) {
                try {
                    Collections.sort<ChildItem?>(items, SortApp())
                } catch (unused: Exception) {
                }
            }
            this.items = items
            // App icons (the old grid adapter loaded them per cell).
            val pm = dialogSetDown.appContext?.packageManager
            icons = items?.map { o ->
                if (this.cancelled || pm == null) null else try {
                    MainUtil.drawableToBitmap(MainApp.dp48, (o as ChildItem).resolveInfo?.loadIcon(pm))
                } catch (_: Exception) {
                    null
                }
            } ?: emptyList()
        }

        public override fun onCancelled() {
            val dialogSetDown = this.dialogRef!!.get() as DialogSetDown? ?: return
            dialogSetDown.loadTask = null
        }

        public override fun onPostExecute() {
            val dialogSetDown = this.dialogRef!!.get() as DialogSetDown? ?: return
            dialogSetDown.loadTask = null
            val list = this.items?.filterIsInstance<ChildItem>() ?: emptyList()
            dialogSetDown.apps = list.mapIndexed { position, item -> item to icons.getOrNull(position) }
            dialogSetDown.loading = false
        }
    }

    interface SetDownListener {
        fun onPick(label: String?, packageName: String?, className: String?)
    }

    private class SortApp : Comparator<ChildItem?> {
        override fun compare(first: ChildItem?, second: ChildItem?): Int {
            if (first == null) {
                return if (second == null) 0 else 1
            }
            if (second == null) {
                return -1
            }
            val nameOrder = MainUtil.compareStringsIgnoreCase(first.name, second.name, false)
            if (nameOrder != 0) {
                return nameOrder
            }
            val pathOrder = MainUtil.compareStringsIgnoreCase(first.path, second.path, false)
            return if (pathOrder != 0) pathOrder else MainUtil.compareStringsIgnoreCase(first.subText, second.subText, false)
        }
    }

    /** Adds the exported activities (other than this app and one download manager) that can handle [intent] to [list] (old code). */
    fun withExtraApps(intent: Intent?, list: MutableList<ChildItem?>?): MutableList<ChildItem?>? {
        var list = list
        val dialogTask: DialogTask?
        val activity = this.hostActivity
        if (activity == null || ((this.loadTask.also { dialogTask = it }) != null && dialogTask!!.cancelled)) {
            return list
        }
        try {
            val packageManager = activity.getPackageManager()
            val listQueryIntentActivities =
                if (Build.VERSION.SDK_INT >= 33) packageManager.queryIntentActivities(
                    intent!!,
                    PackageManager.ResolveInfoFlags.of(0L)
                ) else packageManager.queryIntentActivities(intent!!, 0)
            for (resolveInfo in listQueryIntentActivities) {
                val loadTask = this.loadTask
                var found = true
                if (loadTask != null && loadTask.cancelled) {
                    return list
                }
                val activityInfo: ActivityInfo? = if (resolveInfo != null) resolveInfo.activityInfo else null
                if (resolveInfo != null && activityInfo != null && activityInfo.exported) {
                    val packageName = activityInfo.packageName
                    if (!TextUtils.isEmpty(packageName) && packageName != "ch.opumlabs.opumbrowser") {
                        val name = resolveInfo.activityInfo.name
                        val charSequenceLoadLabel: CharSequence? = if (!TextUtils.isEmpty(name) && (name != "com.logiclooper.idm.activities.MainActivity")) resolveInfo.loadLabel(
                            packageManager
                        ) else null
                        if (!TextUtils.isEmpty(name) && (name != "com.logiclooper.idm.activities.MainActivity") && charSequenceLoadLabel != null) {
                            val newName = charSequenceLoadLabel.toString()
                            if (!TextUtils.isEmpty(newName)) {
                                val folderNames: Array<String?> = folders
                                if (list != null) {
                                    val it = list.iterator()
                                    while (true) {
                                        if (!it.hasNext()) {
                                            val childItem = ChildItem()
                                            childItem.resolveInfo = resolveInfo
                                            childItem.path = packageName
                                            childItem.name = newName
                                            childItem.subText = name
                                            list.add(childItem)
                                            if (!this.chooserMode) {
                                                break
                                            }
                                            if (TextUtils.isEmpty(packageName)) {
                                                found = false
                                                break
                                            }
                                            var index = 0
                                            while (true) {
                                                if (index >= 4) {
                                                    found = false
                                                    break
                                                }
                                                if (packageName == folderNames[index]) {
                                                    break
                                                }
                                                index++
                                            }
                                            if (!found) {
                                                break
                                            }
                                            this.chooserMode = false
                                            list.add(Companion.customTabItem(resolveInfo, packageName!!))
                                            break
                                        }
                                        val childItem = it.next()
                                        if (childItem != null && name == childItem.subText) {
                                            break
                                        }
                                    }
                                } else {
                                    val arrayList: ArrayList<ChildItem?> = ArrayList()
                                    try {
                                        val childItem = ChildItem()
                                        childItem.resolveInfo = resolveInfo
                                        childItem.path = packageName
                                        childItem.name = newName
                                        childItem.subText = name
                                        arrayList.add(childItem)
                                        if (this.chooserMode) {
                                            if (TextUtils.isEmpty(packageName)) {
                                                found = false
                                                break
                                            }
                                            var index = 0
                                            while (true) {
                                                if (index >= 4) {
                                                    found = false
                                                    break
                                                }
                                                if (packageName == folderNames[index]) {
                                                    break
                                                }
                                                index++
                                            }
                                            if (found) {
                                                this.chooserMode = false
                                                arrayList.add(Companion.customTabItem(resolveInfo, packageName!!))
                                            }
                                        }
                                        list = arrayList
                                    } catch (unused: Exception) {
                                        return arrayList
                                    }
                                }
                            } else {
                                continue
                            }
                        }
                    }
                }
            }
            return list
            return list
        } catch (unused2: Exception) {
            return list
        }
    }

    override fun dismiss() {
        loadTask?.cancelled = true
        loadTask = null
        hostActivity = null
        appContext = null
        downListener = null
        fileUrl = null
        mimeType = null
        super.dismiss()
    }

    companion object {
        @JvmField
        val folders: Array<String?> = arrayOf<String?>(
            "com.android.chrome",
            "com.chrome.beta",
            "com.chrome.dev",
            "com.google.android.apps.chrome"
        )

        @JvmStatic
        fun customTabItem(resolveInfo: ResolveInfo?, packageName: String?): ChildItem {
            val childItem = ChildItem()
            childItem.resolveInfo = resolveInfo
            childItem.path = "isCustomTab:" + packageName
            childItem.name = "Chrome Custom Tab"
            childItem.subText = null
            return childItem
        }

        @JvmStatic
        fun viewIntent(action: String?, input: String?, mimeType: String?): Intent {
            val intent = Intent(action)
            intent.setDataAndType(Uri.parse(input), mimeType)
            return intent
        }
    }
}
