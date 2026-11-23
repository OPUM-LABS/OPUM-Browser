package com.mycompany.app.main

import androidx.recyclerview.widget.LinearLayoutManager
import java.util.ArrayList
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.db.DbUtil.insert
import com.mycompany.app.db.DbUtil.queryById
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefSet.putInt
import com.mycompany.app.pref.PrefSet.putString
import com.mycompany.app.pref.PrefUtil.itemMode
import com.mycompany.app.pref.PrefUtil.reversed
import ch.opumlabs.opumbrowser.R

/**
 * Holder for the types other code still uses. The rest of the class (a legacy View-based adapter / dialog) was unreachable
 * after the Compose port and was removed in T5 (2026-11-16).
 */
class MenuIconAdapter {
    class MainMenuItem {
        @JvmField
        var id: Int = 0
    }
}
