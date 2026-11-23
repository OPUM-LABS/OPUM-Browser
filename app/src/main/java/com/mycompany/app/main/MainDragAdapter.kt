package com.mycompany.app.main

import android.widget.ArrayAdapter
import java.util.ArrayList
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatTextView
import com.mycompany.app.compress.CompressUtil.extractedPaths
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.db.DbUtil.deleteById
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.down.DownParseDzen.parseItemText
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseKakao.segmentUrls
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.down.DownParseReddit.redditSegments
import com.mycompany.app.down.DownParseTsfile.findSegments
import com.mycompany.app.down.DownParseVimeo.vimeoItem
import com.mycompany.app.down.DownSaveAudio.saveAudio
import com.mycompany.app.down.DownSaveImage.saveImageAt
import com.mycompany.app.down.DownSavePath.savePathPart
import com.mycompany.app.down.DownSaveSplit.saveSegment
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefUtil.showRecent
import ch.opumlabs.opumbrowser.R
import com.mycompany.app.web.WebViewActivity

/**
 * Holder for the types other code still uses. The rest of the class (a legacy View-based adapter / dialog) was unreachable
 * after the Compose port and was removed in T5 (2026-11-16).
 */
class MainDragAdapter {
    class MainDragItem(@JvmField var id: Int, @JvmField var iconRes: Int, @JvmField var titleRes: Int, @JvmField var isOn: Boolean)
}
