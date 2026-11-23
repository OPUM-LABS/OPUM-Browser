package com.mycompany.app.main

import android.widget.TextView
import java.util.ArrayList
import android.animation.AnimatorSet
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.db.DbUtil.deleteById
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.db.DbUtil.updateWhere
import com.mycompany.app.down.DownParseDzen.parseItemText
import com.mycompany.app.down.DownParseDzen.segmentUrls
import com.mycompany.app.down.DownParseKakao.segmentUrls
import com.mycompany.app.down.DownParseReddit.redditSegments
import com.mycompany.app.down.DownParseVimeo.vimeoItem
import com.mycompany.app.down.DownSaveSplit.encItemAt
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefUtil.showRecent
import com.mycompany.app.pref.PrefUtil.singleLine
import com.mycompany.app.pref.PrefUtil.reversed
import com.mycompany.app.pref.PrefUtil.setSingleLine
import ch.opumlabs.opumbrowser.R
import java.util.regex.Pattern

/**
 * Holder for the types other code still uses. The rest of the class (a legacy View-based adapter / dialog) was unreachable
 * after the Compose port and was removed in T5 (2026-11-16).
 */
class MainSelectAdapter {
    interface MainSelectListener {
        fun onPick(id: Int)
    }

    class MainSelectItem {
        constructor() : super()

        @JvmField
        var id: Int = 0
        @JvmField
        var iconRes: Int = 0
        @JvmField
        var matchText: String? = null
        @JvmField
        var lowerName: String? = null
        @JvmField
        var isChecked: Boolean = false
        @JvmField
        var isDisabled: Boolean = false

        constructor(str: String?, i: Int, i2: Int, str2: String?) {
            this.id = i
            this.iconRes = i2
            this.matchText = str
            this.lowerName = str2
        }

        constructor(i: Int, str: String?) {
            this.id = i
            this.matchText = str
        }

        constructor(i: Int, str: String?, i2: Int) {
            this.id = 0
        }

        constructor(i: Int, i2: Int, i3: Int) {
            this.id = i
            this.iconRes = i2
        }

        constructor(i: Int, i2: Int, str: String?) {
            this.id = i
            this.iconRes = i2
            this.matchText = str
        }

        constructor(i: Int, i2: Int) {
            this.id = i
        }
    }
}
