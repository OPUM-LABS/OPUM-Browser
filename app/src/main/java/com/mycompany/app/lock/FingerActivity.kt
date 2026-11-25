package com.mycompany.app.lock

import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.Bundle
import android.os.CancellationSignal
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.main.MainUtil
import com.mycompany.app.pref.PrefSecret
import com.mycompany.app.ui.lock.LockComposeActivity
import com.mycompany.app.ui.lock.LockLayout
import com.mycompany.app.ui.lock.LockSecondaryButton

/**
 * Fingerprint lock. The system biometric prompt opens on its own; its negative button is the same action as the
 * secondary button of the other lock screens (reset password / run in normal mode / cancel). A recognised
 * fingerprint unlocks like a correct password; any other end of the prompt leaves the screen (the app lock goes to
 * the background). Where the device cannot do biometrics, the screen shows buttons instead of the prompt. The
 * intent contract and shared behaviour are in [LockComposeActivity].
 */
class FingerActivity : LockComposeActivity() {

    private var cancel: CancellationSignal? = null
    private var hasFocus = false
    private var leaveWhenFocused = false
    private var paused = false

    private val available get() = MainUtil.canUseBiometric(applicationContext, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (available) startPrompt()
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        leaveWhenFocused = false
    }

    override fun onPause() {
        paused = true
        cancel?.cancel()
        cancel = null
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        this.hasFocus = hasFocus
        if (leaveWhenFocused && hasFocus) leave()
        leaveWhenFocused = false
    }

    override fun onResetCancelled() = leave()

    /** Leaves the lock screen without unlocking: the app lock goes to the background, other uses close. */
    private fun leave() {
        if (!available) return
        if (type == 0) moveTaskToBack(true) else finish()
    }

    private fun startPrompt() {
        if (cancel != null) return
        try {
            val negative = secondaryText() ?: getString(R.string.cancel)
            val signal = CancellationSignal()
            cancel = signal
            leaveWhenFocused = false
            BiometricPrompt.Builder(this)
                .setTitle(getString(R.string.finger_print))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                .setNegativeButton(negative, mainExecutor) { _, _ -> onSecondary() }
                .build()
                .authenticate(signal, mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                        cancel = null
                        if (paused || errorCode == NEGATIVE_BUTTON) return
                        if (errString.isNullOrEmpty()) MainUtil.showToast(this@FingerActivity, R.string.cancelled)
                        else MainUtil.showToastText(this@FingerActivity, errString)
                        leaveWhenFocused = true
                        if (hasFocus) leave()
                    }

                    override fun onAuthenticationFailed() {
                        leaveWhenFocused = true
                        if (hasFocus) leave()
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                        cancel = null
                        leaveWhenFocused = false
                        unlocked()
                    }
                })
        } catch (_: Exception) {
            cancel = null
        }
    }

    @Composable
    override fun Content() {
        LockBack()
        androidx.compose.runtime.key(generation) {
            val ready = available
            Surface(
                Modifier.fillMaxSize().clickable { leave() },
                color = MaterialTheme.colorScheme.background,
            ) {
                // with a working fingerprint sensor and a secret to set/verify, only the system prompt is shown
                if (!ready || type == 0) {
                    LockLayout(null) {
                        if (!ready) {
                            Button(onClick = {
                                if (cancel == null) startPrompt()
                                if (cancel == null) MainUtil.canUseBiometric(applicationContext, true)
                            }) { Text(stringResource(R.string.finger_print)) }
                            LockSecondaryButton(secondaryLabel()) { onSecondary() }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        /** `BiometricPrompt.BIOMETRIC_ERROR_NEGATIVE_BUTTON` of the biometric library. */
        const val NEGATIVE_BUTTON = 13
    }
}
