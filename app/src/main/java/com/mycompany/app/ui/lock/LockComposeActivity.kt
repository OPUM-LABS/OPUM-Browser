package com.mycompany.app.ui.lock

import android.content.Intent
import com.mycompany.app.ui.state.AppState
import com.mycompany.app.pref.store.SyncSettings
import com.mycompany.app.pref.store.SecretSettings
import com.mycompany.app.pref.store.MainSettings
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.dialog.DialogLockReset
import com.mycompany.app.dialog.DialogSetFull.DialogApplyListener
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.pref.PrefSync
import com.mycompany.app.ui.OpumComposeActivity
import com.mycompany.app.widget.WidgetSearchActivity

/**
 * Shared behaviour of the lock screens (password, PIN, pattern). They are started with these extras:
 * `EXTRA_PASS` which lock (0 secret mode / app, 1, 2, 3 the other locks), `EXTRA_TYPE` what to do
 * (0 unlock then open the browser at `EXTRA_PATH`, 1 set a new secret, 2 and 3 verify for a settings screen,
 * 4 unlock for the voice-search widget with `EXTRA_VOICE`). A verified unlock closes with RESULT_OK or opens the
 * browser; leaving the screen closes it again (so it never stays in the background unlocked).
 */
abstract class LockComposeActivity : OpumComposeActivity() {

    /** Which stored lock this screen is about (`EXTRA_PASS`). */
    protected var pass = 0
    /** What it is for (`EXTRA_TYPE`). */
    protected var type = 0
    protected var path: String? = null
    protected var voice = false
    private var resetDialog: DialogLockReset? = null
    /** Bumped when the activity gets a new intent, so the screens restart their input state. */
    protected var generation by mutableIntStateOf(0)
        private set

    protected val isSetting get() = type == 1

    override fun onCreate(savedInstanceState: Bundle?) {
        readIntent(intent)
        applyOrientation()
        super.onCreate(savedInstanceState)
        if (type == 3) hideSystemBars()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readIntent(intent)
        generation++
    }

    override fun onPause() {
        super.onPause()
        resetDialog?.dismiss()
        resetDialog = null
        finish()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && type == 3) hideSystemBars()
    }

    private fun readIntent(intent: Intent?) {
        if (intent == null) return
        pass = intent.getIntExtra("EXTRA_PASS", 0)
        type = intent.getIntExtra("EXTRA_TYPE", 0)
        path = if (type == 0) intent.getStringExtra("EXTRA_PATH") else null
        voice = type == 4 && intent.getBooleanExtra("EXTRA_VOICE", false)
    }

    /** Same rule as the legacy screens: phones follow the orientation preference, tablets and split screen are free. */
    private fun applyOrientation() {
        if (isInMultiWindowMode || !MainUtil.isSmallScreen(this)) {
            when (MainSettings.appRotation.value) {
                1 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                2 -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            }
        } else {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun hideSystemBars() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }

    /** The secret that unlocks this screen. */
    protected fun expectedSecret(): String? = when (pass) {
        1 -> SecretSettings.passLockCode.value
        2 -> SecretSettings.linkLockCode.value
        3 -> SecretSettings.touchLockCode.value
        else -> SecretSettings.lockCode.value
    }

    /** Stores a new secret of [kind] (1 pattern, 2 PIN, 3 password) for this lock and closes with RESULT_OK. */
    protected fun storeSecret(lockKind: Int, secret: String?) {
        when (pass) {
            1 -> { SecretSettings.editLocal { set(SecretSettings.passLockType, lockKind) }; SecretSettings.editLocal { set(SecretSettings.passLockCode, secret) }; PrefSecret.savePassLock(applicationContext) }
            2 -> { SecretSettings.editLocal { set(SecretSettings.linkLockType, lockKind) }; SecretSettings.editLocal { set(SecretSettings.linkLockCode, secret) }; PrefSecret.saveLinkLock(applicationContext) }
            3 -> { SecretSettings.editLocal { set(SecretSettings.touchLockType, lockKind) }; SecretSettings.editLocal { set(SecretSettings.touchLockCode, secret) }; PrefSecret.saveTouchLock(applicationContext) }
            else -> { SecretSettings.editLocal { set(SecretSettings.lockType, lockKind) }; SecretSettings.editLocal { set(SecretSettings.lockCode, secret) }; PrefSecret.saveLock(applicationContext) }
        }
        setResult(RESULT_OK)
        finish()
    }

    /** The unlock succeeded: open the voice-search widget, report back, or open the browser. */
    protected fun unlocked() {
        if (type == 4) {
            openVoiceSearch()
        } else if (TextUtils.isEmpty(path)) {
            setResult(RESULT_OK)
            finish()
        } else {
            startActivity(MainUtil.browserIntent(applicationContext).putExtra("EXTRA_PATH", path))
        }
    }

    private fun openVoiceSearch() {
        finishAffinity()
        startActivity(
            Intent(applicationContext, WidgetSearchActivity::class.java)
                .putExtra("EXTRA_LOCK", true)
                .putExtra("EXTRA_VOICE", voice)
        )
    }

    /** Text of the secondary button under the input (null in the "set a new secret" flow, which has its own buttons). */
    @Composable
    protected fun secondaryLabel(): String? = when {
        type == 1 -> null
        type == 2 -> stringResource(R.string.secret_reset)
        type == 3 -> stringResource(R.string.cancel)
        SecretSettings.lockSecret.value -> stringResource(R.string.normal_start)
        else -> stringResource(R.string.secret_reset)
    }

    protected fun onSecondary() {
        when {
            type == 3 -> finish()
            type == 2 -> showReset()
            SecretSettings.lockSecret.value -> {
                SyncSettings.editLocal { set(SyncSettings.secretMode, false) }
                PrefSync.saveNormalIndex(applicationContext)
                MainUtil.clearAppData(applicationContext)
                if (type == 4) openVoiceSearch() else MainUtil.restartTask(this, path, false)
            }
            else -> showReset()
        }
    }

    /** The reset dialog was closed without resetting. */
    protected open fun onResetCancelled() {}

    protected fun showReset() {
        if (resetDialog != null) return
        var applied = false
        val dialog = DialogLockReset(this, pass, object : DialogApplyListener {
            override fun onApply() {
                applied = true
                resetDialog?.dismiss()
                resetDialog = null
                when {
                    type == 4 -> openVoiceSearch()
                    type != 0 -> {
                        setResult(RESULT_OK, Intent().putExtra("EXTRA_LOAD", true))
                        finish()
                    }
                    else -> {
                        finishAffinity()
                        val intent = MainUtil.browserIntent(applicationContext)
                        if (!TextUtils.isEmpty(path)) intent.putExtra("EXTRA_PATH", path)
                        startActivity(intent)
                    }
                }
            }
        })
        resetDialog = dialog
        dialog.setOnDismissListener {
            resetDialog = null
            if (!applied) onResetCancelled()
        }
    }

    /** Back: the app lock only goes to the background (never back into the locked app); other uses just close. */
    @Composable
    protected fun LockBack() {
        BackHandler { if (type == 0) moveTaskToBack(true) else finish() }
    }
}

/** The big lock symbol, a message line and then [content], centred like the old screens. */
@Composable
fun LockLayout(message: String?, content: @Composable () -> Unit) {
    androidx.compose.material3.Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Column(
        Modifier.fillMaxSize().systemBarsPadding().imePadding().padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        androidx.compose.material3.Icon(
            painterResource(if (AppState.darkUi) R.drawable.outline_lock_dark_84 else R.drawable.outline_lock_black_84),
            contentDescription = null,
            tint = androidx.compose.ui.graphics.Color.Unspecified,
        )
        Text(
            message ?: " ",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 16.dp),
        )
        content()
    }
    }
}

@Composable
fun LockSecondaryButton(label: String?, onClick: () -> Unit) {
    if (label != null) TextButton(onClick = onClick) { Text(label) }
}
