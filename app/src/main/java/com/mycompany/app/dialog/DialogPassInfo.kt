package com.mycompany.app.dialog

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ch.opumlabs.opumbrowser.R as AppR
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.book.DbBookPass
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainItem.ChildItem
import com.mycompany.app.main.MainListLoader
import com.mycompany.app.main.MainListLoader.ListLoadListener
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.ui.browser.ComposeBottomDialog

/**
 * A site password. New ([itemIdParam] = 0, from a page's login form): "Save password?", the site [siteUrlParam], Name [userTextParam] and Password
 * [passTextParam] fields and Save (stores with the page icon in `DbBookPass`). Saved ([itemIdParam] = row id, from the password list):
 * header with the site icon and name, the site / name / password with copy buttons and a show-password toggle; Edit
 * makes name and password editable with Save (updates the row). [PassInfoListener] hears a save (`onFailed`) and the close
 * (`onLoaded`). Saving work is the old code. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogPassInfo(
    activity: Activity?,
    itemIdParam: Long,
    siteUrlParam: String?,
    userTextParam: String?,
    passTextParam: String?,
    siteKey: String?,
    passInfoListener: PassInfoListener?,
) : ComposeBottomDialog(activity) {
    @JvmField
    var saveIcon: Bitmap? = null
    @JvmField
    var saveName: String? = null
    @JvmField
    var saveWord: String? = null
    @JvmField
    var appContext: Context? = context
    @JvmField
    var infoListener: PassInfoListener? = passInfoListener
    @JvmField
    var itemId: Long = itemIdParam
    @JvmField
    var siteUrl: String? = siteUrlParam
    @JvmField
    var userText: String? = userTextParam
    @JvmField
    var passText: String? = passTextParam
    @JvmField
    var siteKey: String? = siteKey
    @JvmField
    var listLoader: MainListLoader? = null
    @JvmField
    var saving: Boolean = false
    @JvmField
    var doneListener: PassInfoListener? = null

    @JvmField
    val mainHandler = Handler(Looper.getMainLooper())

    private var editing by mutableStateOf(itemIdParam == 0L)
    private var user by mutableStateOf(userTextParam ?: "")
    private var pass by mutableStateOf(passTextParam ?: "")
    private var showPass by mutableStateOf(false)
    private var busy by mutableStateOf(false)
    private var iconBmp by mutableStateOf<Bitmap?>(null)

    /** Background work. */
    private var executor: java.util.concurrent.ExecutorService? = null
    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    interface PassInfoListener {
        fun onSaved(name: String?, password: String?)
        fun onDelete(name: String?)
        val icon: Bitmap?
    }

    init {
        if (itemIdParam != 0L) loadIcon()
        setSheet {
            if (itemId != 0L) {
                Row(
                    Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 4.dp),
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
                        siteUrl ?: "", maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f).padding(start = 16.dp),
                    )
                    if (!editing) {
                        IconButton(onClick = { editing = true }) {
                            Icon(painterResource(AppR.drawable.outline_edit_black_24), contentDescription = stringResource(AppR.string.edit))
                        }
                    }
                }
            } else {
                Text(
                    stringResource(AppR.string.password_confirm), style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 4.dp),
                )
            }
            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(AppR.string.site), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(siteUrl ?: "", style = MaterialTheme.typography.bodyLarge)
                    }
                    if (itemId != 0L && !editing) CopyButton { copy("Copied host", siteUrl) }
                }
                Field(stringResource(AppR.string.name), user, { user = it }, password = false) {
                    if (itemId != 0L && !editing) CopyButton { copy("Copied username", userText) }
                }
                Field(stringResource(AppR.string.password), pass, { pass = it }, password = true) {
                    IconButton(onClick = { showPass = !showPass }) {
                        Icon(
                            painterResource(if (showPass) AppR.drawable.outline_visibility_black_24 else AppR.drawable.outline_visibility_off_black_24),
                            contentDescription = null,
                        )
                    }
                    if (itemId != 0L && !editing) CopyButton { copy("Copied password", passText) }
                }
            }
            if (busy) {
                LinearProgressIndicator(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp))
            }
            if (editing) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { save() }, enabled = !busy) { Text(stringResource(AppR.string.save)) }
                }
            } else {
                Box(Modifier.padding(bottom = 16.dp))
            }
        }
        show()
    }

    @Composable
    private fun CopyButton(onClick: () -> Unit) {
        IconButton(onClick = onClick) {
            Icon(painterResource(AppR.drawable.outline_content_copy_black_24), contentDescription = null)
        }
    }

    @Composable
    private fun Field(label: String, value: String, onChange: (String) -> Unit, password: Boolean, trailing: @Composable () -> Unit) {
        Row(Modifier.fillMaxWidth().padding(start = 24.dp, end = 12.dp, top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = value,
                onValueChange = onChange,
                label = { Text(label) },
                singleLine = true,
                readOnly = !editing,
                visualTransformation = if (password && !showPass) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (password) KeyboardType.Password else KeyboardType.Text,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { save() }),
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) { trailing() }
        }
    }

    private fun copy(label: String, siteUrl: String?) {
        MainUtil.copyTextToast(appContext, AppR.string.copied_clipboard, label, siteUrl)
    }

    /** Saved entry: the site icon (cached, or the list loader into a throw-away view). */
    private fun loadIcon() {
        val host = siteUrl
        if (TextUtils.isEmpty(host)) return
        val item = ChildItem()
        item.listType = 33
        item.itemType = 11.toByte()
        item.path = host
        val cached = MainListLoader.cachedIconFor(item)
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
                if (MainUtil.isBitmapOk(bitmap)) iconBmp = bitmap
            }
        })
        listLoader = loader
        loader.enqueue(item, target)
    }

    private fun save() {
        if (!editing || saving || busy) return
        saving = true
        mainHandler.post { savePass() }
    }

    /** Save: checks name and password, then stores (new) or updates (saved entry) in the background. */
    fun savePass() {
        if (infoListener == null) {
            saving = false
            return
        }
        var name: String? = user.trim { it <= ' ' }
        var word: String? = pass.trim { it <= ' ' }
        val icon: Bitmap?
        if (itemId == 0L) {
            icon = infoListener!!.icon
        } else {
            name = if (TextUtils.isEmpty(name)) null else name!!.replace(",", "")
            word = if (TextUtils.isEmpty(word)) null else word!!.replace(",", "")
            icon = null
        }
        if (TextUtils.isEmpty(name)) {
            showToast(appContext, AppR.string.input_name)
            saving = false
            return
        }
        if (TextUtils.isEmpty(word)) {
            showToast(appContext, AppR.string.input_password)
            saving = false
            return
        }
        doneListener = infoListener
        saveIcon = icon
        saveName = name
        saveWord = word
        infoListener = null
        busy = true
        setCanceledOnTouchOutside(false)
        runAsync(Runnable {
            val ctx = appContext
            if (itemId == 0L) {
                DbBookPass.saveItem(ctx, siteUrl, saveIcon!!, saveName, saveWord, siteKey)
            } else {
                val saveNameNow = saveName
                val saveWordNow = saveWord
                if (ctx != null && itemId > 0 && !TextUtils.isEmpty(saveNameNow) && !TextUtils.isEmpty(saveWordNow)) {
                    DbUtil.updateById(
                        DbBookPass.get(ctx).getWritableDatabase(),
                        "DbBookPass_table",
                        android.content.ContentValues().also { cv -> cv.put("_user_val", saveNameNow); cv.put("_pass_val", saveWordNow) },
                        itemId,
                    )
                }
            }
            mainHandler.post {
                val listener = doneListener
                doneListener = null
                listener?.onSaved(saveName, saveWord)
                saveIcon = null
                saveName = null
                saveWord = null
                saving = false
                busy = false
                if (isShowing) dismiss()
            }
        })
    }

    override fun canCancelFromScrim(): Boolean = !busy

    override fun dismiss() {
        if (appContext == null) {
            super.dismiss()
            return
        }
        val listener = infoListener
        infoListener = null
        listener?.onDelete(siteUrl)
        listLoader?.release()
        listLoader = null
        appContext = null
        super.dismiss()
    }

    companion object {
    }
}
