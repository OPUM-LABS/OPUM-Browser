package com.mycompany.app.dialog

import com.mycompany.app.ui.settings.CleanSettingsActivity
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.ZtriSettings
import com.mycompany.app.pref.store.EditorSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.db.book.DbBookQuick
import com.mycompany.app.data.book.DataBookLink
import ch.opumlabs.opumbrowser.R as AppR
import android.app.Activity
import android.content.Context
import android.webkit.WebViewDatabase
import com.mycompany.app.data.book.DataBookLink.Companion.instance
import com.mycompany.app.db.DbUtil
import com.mycompany.app.db.DbUtil.deleteWhere
import com.mycompany.app.db.book.DbBookDown
import com.mycompany.app.db.book.DbBookHistory
import com.mycompany.app.db.book.DbBookHistory.Companion.clearSecret
import com.mycompany.app.db.book.DbBookIcon
import com.mycompany.app.db.book.DbBookLink
import com.mycompany.app.db.book.DbBookPass
import com.mycompany.app.db.book.DbBookQuick.Companion.get
import com.mycompany.app.db.book.DbBookRecent
import com.mycompany.app.db.book.DbBookRecent.Companion.clearSecret
import com.mycompany.app.db.book.DbBookTab
import com.mycompany.app.db.book.DbBookTab.Companion.clearSecret
import com.mycompany.app.db.book.DbBookWeb
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst
import com.mycompany.app.main.MainUri.findChild
import com.mycompany.app.main.MainUri.externalVolumes
import com.mycompany.app.main.MainUtil
import com.mycompany.app.main.MainUtil.clearBrowsingData
import com.mycompany.app.main.MainUtil.dpToPx
import com.mycompany.app.main.MainUtil.clearCookies
import com.mycompany.app.main.MainUtil.showToast
import com.mycompany.app.main.MainUtil.compareStrings
import com.mycompany.app.main.MainUtil.compareStringsIgnoreCase
import com.mycompany.app.main.MainUtil.sameObject
import com.mycompany.app.main.MainUtil.noOp
import com.mycompany.app.pref.PrefEditor
import com.mycompany.app.pref.PrefEditor.Companion.withAlpha
import com.mycompany.app.pref.PrefEditor.Companion.get
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.pref.PrefZtri
import kotlin.Boolean
import kotlin.Exception
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.arrayOf
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mycompany.app.ui.browser.ComposeBottomDialog
import com.mycompany.app.ui.browser.SheetButtons

/**
 * "Forgot the lock?" reset ([resetTypeParam]: 0 = app lock (with the secret-mode note when it is on), 1 = saved passwords' lock,
 * 2 = link lock): the explanation and Reset, which clears that lock and the data it protected in the background
 * (old `SameObjectTask` / `7` / `8`, unchanged: lock prefs, secret quick access / bookmarks / downloads / recent /
 * history / tabs, saved passwords and their pass-icon / editor prefs, link list, HTTP auth), then says "Reset" and
 * calls [DialogApplyListener.onApply]. Compose now (on [ComposeBottomDialog]); same constructor, shows itself.
 */
@Suppress("deprecation")
class DialogLockReset(activity: Activity?, resetTypeParam: Int, dialogApplyListener: DialogApplyListener?) : ComposeBottomDialog(activity) {
    @JvmField val mainHandler: Handler? = Handler(Looper.getMainLooper())
    @JvmField var appContext: Context? = context
    @JvmField var applyListener: DialogApplyListener? = dialogApplyListener
    @JvmField var resetType: Int = resetTypeParam
    @JvmField var resetting: Boolean = false
    /** The app lock reset cleared the locked workspace in use: the browser opens Main now ([WorkspaceData.clearLocked]). */
    @Volatile @JvmField var leftSpace: Boolean = false

    private var busy by mutableStateOf(false)
    private var executor: java.util.concurrent.ExecutorService? = null

    init {
        setSheet {
            val text = when {
                resetType == 2 -> stringResource(AppR.string.link_reset_guide)
                resetType != 0 -> stringResource(AppR.string.password_reset_guide)
                SecretSettings.lockSecret.value -> stringResource(AppR.string.lock_reset_guide) + "\n" + stringResource(AppR.string.lock_spaces_guide)
                else -> stringResource(AppR.string.lock_reset_guide)
            }
            Text(
                text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()).fillMaxWidth()
                    .heightIn(min = 72.dp).padding(24.dp),
            )
            if (busy) {
                Box(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            }
            SheetButtons({ reset() }, mainLabel = AppR.string.reset, mainEnabled = !busy)
        }
        show()
    }

    override fun canCancelFromScrim(): Boolean = !busy

    /** Reset. */
    private fun reset() {
        if (resetting) return
        resetting = true
        busy = true
        setCanceledOnTouchOutside(false)
        runAsync(SameObjectTask())
    }

    fun runAsync(runnable: Runnable?) {
        var ex = executor
        if (ex == null || ex.isShutdown() || ex.isTerminated()) {
            ex = MainApp.executor(context) ?: return
            executor = ex
        }
        try { ex.execute(runnable) } catch (unused: Exception) { }
    }

    internal inner class SameObjectTask : Runnable {
        override fun run() {
            val found: Boolean
            val dialogLockReset = this@DialogLockReset
            val resetType = dialogLockReset.resetType
            var notFound = true
            if (resetType != 0) {
                val context = dialogLockReset.appContext
                if (resetType == 2) {
                    val labels = CleanSettingsActivity.overLabels
                    if (context != null) {
                        if (SecretSettings.linkLockType.value != 0 || !MainUtil.sameObject(SecretSettings.linkLockCode.value, "")) {
                            SecretSettings.editLocal { set(SecretSettings.linkLockType, 0) }
                            SecretSettings.editLocal { set(SecretSettings.linkLockCode, "") }
                            val prefSecretR = PrefSecret.get(context, false)
                            prefSecretR!!.removeKey("mLinkLockType")
                            prefSecretR.removeKey("mLinkLockCode")
                            prefSecretR.save()
                        }
                        DataBookLink.instance(context).links = null
                        DataBookLink.instance(context).clear()
                        deleteWhere(
                            DbBookLink.get(context).getWritableDatabase(),
                            "DbBookLink_table",
                            null,
                            null
                        )
                    }
                    dialogLockReset.postDone()
                    return
                }
                if (context != null) {
                    if (SecretSettings.savePass.value || SecretSettings.loginLock.value || SecretSettings.passLockType.value != 0 || !MainUtil.sameObject(
                            SecretSettings.passLockCode.value,
                            ""
                        )
                    ) {
                        SecretSettings.editLocal { set(SecretSettings.savePass, false) }
                        SecretSettings.editLocal { set(SecretSettings.loginLock, false) }
                        SecretSettings.editLocal { set(SecretSettings.passLockType, 0) }
                        SecretSettings.editLocal { set(SecretSettings.passLockCode, "") }
                        val prefSecretR = PrefSecret.get(context, false)
                        prefSecretR!!.putBool("mSavePass2", false) // written: an absent key would read as on (new default)
                        prefSecretR.removeKey("mLoginLock")
                        prefSecretR.removeKey("mPassLockType2")
                        prefSecretR.removeKey("mPassLockCode2")
                        prefSecretR.save()
                    }
                    if (!ZtriSettings.passIcon.value || (-1.0f).compareTo(ZtriSettings.passLtX.value) != 0 || (-1.0f).compareTo(ZtriSettings.passRtX.value) != 0 || (-1.0f).compareTo(ZtriSettings.passUpY.value) != 0 || (-1.0f).compareTo(ZtriSettings.passDnY.value) != 0
                    ) {
                        ZtriSettings.editLocal { set(ZtriSettings.passIcon, true) }
                        ZtriSettings.editLocal { set(ZtriSettings.passLtX, -1.0f) }
                        ZtriSettings.editLocal { set(ZtriSettings.passRtX, -1.0f) }
                        ZtriSettings.editLocal { set(ZtriSettings.passUpY, -1.0f) }
                        ZtriSettings.editLocal { set(ZtriSettings.passDnY, -1.0f) }
                        val prefZtriR = PrefZtri.get(context)
                        prefZtriR!!.removeKey("mPassIcon")
                        prefZtriR.removeKey("mPassLtX")
                        prefZtriR.removeKey("mPassRtX")
                        prefZtriR.removeKey("mPassUpY")
                        prefZtriR.removeKey("mPassDnY")
                        prefZtriR.save()
                    }
                    val color = MainConst.gradientColors[5]
                    val passPos = MainConst.presetPositions[5]
                    if (EditorSettings.passAlpha.value != MainConst.BUTTON_ALPHA || EditorSettings.passColor.value != color || (EditorSettings.passPos.value).compareTo(passPos) != 0
                    ) {
                        EditorSettings.editLocal { set(EditorSettings.passAlpha, MainConst.BUTTON_ALPHA) }
                        EditorSettings.editLocal { set(EditorSettings.passColor, color) }
                        EditorSettings.editLocal { set(EditorSettings.passPos, passPos) }
                        PrefEditor.passArgb = withAlpha(color, MainConst.BUTTON_ALPHA)
                        val prefEditorS = PrefEditor.get(context)
                        prefEditorS!!.removeKey("mPassAlpha")
                        prefEditorS.removeKey("mPassColor")
                        prefEditorS.removeKey("mPassPos")
                        prefEditorS.save()
                    }
                    deleteWhere(DbBookPass.get(context).getWritableDatabase(), "DbBookPass_table", null, null)
                }
                dialogLockReset.postDone()
                return
            }
            val context = dialogLockReset.appContext
            if (context != null) {
                SecretSettings.editLocal { set(SecretSettings.lockType, 0) }
                SecretSettings.editLocal { set(SecretSettings.lockCode, "") }
                val prefSecretR = PrefSecret.get(context, false)
                prefSecretR!!.removeKey("mLockType2")
                prefSecretR.removeKey("mLockCode2")
                prefSecretR.save()
                // only locked workspaces: just those are cleared (what "Initialization target" lists is for the whole app)
                if (SecretSettings.lockSecret.value) {
                    dialogLockReset.leftSpace = com.mycompany.app.web.WorkspaceData.clearLocked(context)
                    dialogLockReset.postDone()
                    return
                }
                val lockReset = SecretSettings.lockReset.value
                if (lockReset != 0) {
                    val resetBit = lockReset and 2
                    if (resetBit == 2) {
                        deleteWhere(DbBookQuick.get(context).getWritableDatabase(), "DbBookQuick_table", null, null)
                    }
                    if ((SecretSettings.lockReset.value and 4) == 4) {
                        deleteWhere(
                            DbBookWeb.get(context).getWritableDatabase(),
                            "DbBookWeb_table",
                            null,
                            null
                        )
                    }
                    if ((SecretSettings.lockReset.value and 8) == 8) {
                        deleteWhere(
                            DbBookDown.get(context).getWritableDatabase(),
                            "DbBookDown_table",
                            null,
                            null
                        )
                    }
                    if ((SecretSettings.lockReset.value and 16) == 16) {
                        DbBookRecent.clearAll(context)
                    }
                    if ((SecretSettings.lockReset.value and 32) == 32) {
                        DbBookHistory.clearAll(context)
                        found = true
                    } else {
                        found = false
                    }
                    if ((SecretSettings.lockReset.value and 64) == 64) {
                        if (SyncSettings.normalIndex.value != 0 || PrefSync.currentSecretIndex != 0) {
                            SyncSettings.editLocal { set(SyncSettings.normalIndex, 0) }
                            PrefSync.currentSecretIndex = 0
                            PrefSync.normalIndex = 0
                            PrefSync.savedIndex = 0
                            val prefSyncR = PrefSync.get(context, false)
                            prefSyncR!!.removeKey("mNormalIndex")
                            prefSyncR.removeKey("mSecretIndex")
                            prefSyncR.save()
                        }
                        DbBookTab.clearAll(context)
                    } else {
                        notFound = false
                    }
                    if (found && notFound) {
                        deleteWhere(
                            DbBookIcon.get(context).getWritableDatabase(),
                            "DbBookIcon_table",
                            null,
                            null
                        )
                    }
                }
                val lockResetValue = SecretSettings.lockReset.value
                if (lockResetValue == 0) {
                    dialogLockReset.postDone()
                    return
                }
                if ((lockResetValue and 32) != 32) {
                    dialogLockReset.postDone()
                    return
                }
                val handler = dialogLockReset.mainHandler
                if (handler == null) {
                    return
                }
                handler.post(dialogLockReset.ClearBrowsingDataTask())
            }
        }
    }
    internal inner class ApplyTask : Runnable {
        override fun run() {
            val dialogLockReset = this@DialogLockReset
            showToast(dialogLockReset.appContext, AppR.string.reset_noti)
            val dialogApplyListener = dialogLockReset.applyListener
            if (dialogApplyListener != null) {
                dialogApplyListener.onApply()
            }
        }
    }
    internal inner class ClearBrowsingDataTask : Runnable {
        override fun run() {
            val dialogLockReset = this@DialogLockReset
            val context = dialogLockReset.appContext
            if (context == null) {
                return
            }
            MainUtil.clearWebData(context)
            clearBrowsingData(dialogLockReset.mainHandler, dialogLockReset.ClearCookiesTask())
        }
    }
    internal inner class ClearCookiesTask : Runnable {
        internal inner class PostDoneTask : Runnable {
            internal inner class ClearCookiesDoneStep : Runnable {
                override fun run() {
                    val clearCookiesTask = this@ClearCookiesTask
                    val context = this@DialogLockReset.appContext
                    if (context == null) {
                        return
                    }
                    clearCookies(context)
                    this@DialogLockReset.postDone()
                }
            }
            override fun run() {
                val dialogLockReset = this@DialogLockReset
                val context = dialogLockReset.appContext
                if (context != null) {
                    try {
                        WebViewDatabase.getInstance(context).clearHttpAuthUsernamePassword()
                    } catch (unused: Exception) {
                    } catch (unused: NoSuchMethodError) {
                    }
                    val handler = dialogLockReset.mainHandler
                    if (handler == null) {
                        return
                    }
                    handler.post(ClearCookiesDoneStep())
                }
            }
        }
        override fun run() {
            val handler = this@DialogLockReset.mainHandler
            if (handler == null) {
                return
            }
            handler.post(PostDoneTask())
        }
    }


    fun postDone() {
        mainHandler?.post(ApplyTask())
    }

    override fun dismiss() {
        appContext = null
        applyListener = null
        super.dismiss()
    }

    companion object {
    }
}
