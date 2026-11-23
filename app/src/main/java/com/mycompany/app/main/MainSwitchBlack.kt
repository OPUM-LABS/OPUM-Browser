package com.mycompany.app.main

import android.os.Bundle
import com.mycompany.app.pref.store.TtsSettings
import android.text.TextUtils
import com.mycompany.app.pref.PrefTts

@Suppress("deprecation")
class MainSwitchBlack : MainActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        this.ownBackHandling = true
        super.onCreate(savedInstanceState)
        val stringExtra = getIntent().getStringExtra("EXTRA_PATH")
        try {
            val intentL = MainUtil.browserIntent(this)
            if (!TextUtils.isEmpty(stringExtra)) {
                intentL.putExtra("EXTRA_PATH", stringExtra)
                if (TtsSettings.appKeypad.value && stringExtra == "short_new_tab") {
                    intentL.putExtra("EXTRA_KEYPAD", true)
                }
            }
            startActivity(intentL)
        } catch (unused: Exception) {
        }
        finish()
    }
}
