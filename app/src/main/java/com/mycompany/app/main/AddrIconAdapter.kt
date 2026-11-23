package com.mycompany.app.main

import com.mycompany.app.pref.PrefSet
import com.mycompany.app.pref.store.ZoneSettings
import com.mycompany.app.pref.store.MainSettings
import android.view.View.OnLongClickListener
import com.mycompany.app.main.MenuIconAdapter.MainMenuItem
import java.util.ArrayList
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView.ScaleType
import androidx.recyclerview.widget.RecyclerView
import com.mycompany.app.compress.CompressUtil.extractedPaths
import com.mycompany.app.data.DataUtil.addUri
import com.mycompany.app.db.book.DbBookAlbum.Companion.cached; import com.mycompany.app.db.book.DbBookAlbum.Companion.get
import com.mycompany.app.db.book.DbBookCmp.Companion.cached; import com.mycompany.app.db.book.DbBookCmp.Companion.get
import com.mycompany.app.db.book.DbBookLocale.Companion.cached; import com.mycompany.app.db.book.DbBookLocale.Companion.get
import com.mycompany.app.db.book.DbBookPage.Companion.cached; import com.mycompany.app.db.book.DbBookPage.Companion.get
import com.mycompany.app.db.book.DbBookPdf.Companion.cached; import com.mycompany.app.db.book.DbBookPdf.Companion.get
import com.mycompany.app.down.DownParseKakao.parseItemText
import com.mycompany.app.down.DownParseReddit.parseRedditItem
import com.mycompany.app.down.DownParseTsfile.findSegments
import com.mycompany.app.down.DownSaveAudio.saveAudio
import com.mycompany.app.down.DownSaveImage.saveImageAt
import com.mycompany.app.down.DownSavePath.savePathPart
import com.mycompany.app.down.DownSaveSplit.saveSegment
import com.mycompany.app.pref.PrefMain
import com.mycompany.app.pref.PrefSet.putBool
import com.mycompany.app.pref.PrefUtil.showDetail
import com.mycompany.app.pref.PrefZone

@Suppress("deprecation")
class AddrIconAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    @JvmField
    var hasBack: Boolean = false
    @JvmField
    var listView: View? = null
    @JvmField
    var itemListener: AddrListener? = null
    @JvmField
    var menuItems: ArrayList<Any?>? = null
    @JvmField
    var spareItems: ArrayList<Any?>? = null
    @JvmField
    var notiMode: Boolean = false
    @JvmField
    var lastCount: Int = 0

    internal inner class PutBoolClick : View.OnClickListener {
        override fun onClick(view: View?) {
            val tag = view!!.getTag()
            val addrHolder = if (tag == null || tag !is AddrHolder) null else tag
            if (addrHolder == null || addrHolder.myIconView == null) {
                return
            }
            val position = addrHolder.bindingAdapterPosition
            val addrIconAdapter = this@AddrIconAdapter
            if (addrIconAdapter.itemAt(position) != null) {
                if (addrIconAdapter.notiMode) {
                    if (ZoneSettings.notiAddr.value) {
                        ZoneSettings.editLocal { set(ZoneSettings.notiAddr, false) }
                        PrefSet.putBool(15, addrHolder.myIconView!!.getContext(), "mNotiAddr", ZoneSettings.notiAddr.value)
                    }
                    addrHolder.myIconView!!.showDot(false, true)
                }
                val addrListener = addrIconAdapter.itemListener
                if (addrListener != null) {
                    addrListener.onItemClick(position)
                }
            }
        }
    }

    internal inner class PutBoolLongClick : OnLongClickListener {
        override fun onLongClick(view: View?): Boolean {
            val tag = view!!.getTag()
            val addrHolder = if (tag == null || tag !is AddrHolder) null else tag
            if (addrHolder != null && addrHolder.myIconView != null) {
                val position = addrHolder.bindingAdapterPosition
                val addrIconAdapter = this@AddrIconAdapter
                if (addrIconAdapter.itemAt(position) != null) {
                    if (addrIconAdapter.notiMode) {
                        if (ZoneSettings.notiAddr.value) {
                            ZoneSettings.editLocal { set(ZoneSettings.notiAddr, false) }
                            PrefSet.putBool(15, addrHolder.myIconView!!.getContext(), "mNotiAddr", ZoneSettings.notiAddr.value)
                        }
                        addrHolder.myIconView!!.showDot(false, true)
                    }
                    val addrListener = addrIconAdapter.itemListener
                    if (addrListener != null) {
                        addrListener.onStartDrag(addrHolder)
                    }
                }
            }
            return true
        }
    }

    class AddrHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        @JvmField
        var myIconView: com.mycompany.app.ui.browser.BarIndicator? = null
    }

    interface AddrListener {
        fun onItemClick(position: Int)

        fun onStartDrag(addrHolder: AddrHolder?)
    }

    override fun getItemCount(): Int {
        val lastCount = this.lastCount
        if (lastCount != 0) {
            return lastCount
        }
        val arrayList = this.menuItems
        if (arrayList == null) {
            return 0
        }
        return arrayList.size
    }

    override fun getItemViewType(position: Int): Int {
        val mainMenuItemV: MainMenuItem? = itemAt(position)
        return if (mainMenuItemV != null && mainMenuItemV.id == 1234) 1 else 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val newWidth: Int
        val addrHolder = holder as AddrHolder
        val view = addrHolder.itemView
        val mainMenuItemV: MainMenuItem? = itemAt(position)
        if (mainMenuItemV == null) {
            return
        }
        val isAddr = mainMenuItemV.id == 1234
        val width = this.listView!!.getWidth()
        val itemCount = getItemCount()
        if (itemCount == 0) {
            newWidth = -1
        } else {
            newWidth = if (isAddr) width - ((itemCount - 1) * MainApp.Companion.dp48) else MainApp.Companion.dp48
        }
        val layoutParams: ViewGroup.LayoutParams? = if (view.getWidth() != newWidth) view.getLayoutParams() else null
        if (view.getWidth() != newWidth && layoutParams != null) {
            layoutParams.width = newWidth
            view.requestLayout()
        }
        if (addrHolder.myIconView == null) {
            view.setVisibility(View.INVISIBLE)
            return
        }
        view.setVisibility(View.VISIBLE)
        val tint = MainUtil.barTint(0, false)
        view.setTag(addrHolder)
        addrHolder.myIconView!!.setTag(addrHolder)
        addrHolder.myIconView!!.applyShape(false, true)
        addrHolder.myIconView!!.setBgPreColor(MainUtil.themeIconColor(tint, 0))
        addrHolder.myIconView!!.setImageResource(MainUtil.themeColorAt(mainMenuItemV.id, tint))
        if (this.notiMode) {
            addrHolder.myIconView!!.setSetting(true)
            addrHolder.myIconView!!.showDot(ZoneSettings.notiAddr.value, false)
        }
        addrHolder.myIconView!!.setOnClickListener(PutBoolClick())
        addrHolder.myIconView!!.setOnLongClickListener(PutBoolLongClick())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val width: Int
        val context = parent.getContext()
        if (context == null) {
            throw IllegalStateException("no view holder for view type")
        }
        val measuredWidth = parent.getMeasuredWidth()
        val itemCount = getItemCount()
        if (itemCount == 0) {
            width = -1
        } else if (viewType == 1) {
            width = measuredWidth - ((itemCount - 1) * MainApp.Companion.dp48)
        } else {
            width = MainApp.Companion.dp48
        }
        if (viewType == 1) {
            val view = View(context)
            view.setLayoutParams(ViewGroup.LayoutParams(width, -1))
            return AddrHolder(view)
        }
        val myIconView = com.mycompany.app.ui.browser.BarIndicator(context)
        myIconView.setScaleType(ScaleType.CENTER_INSIDE)
        myIconView.applyShape(false, true)
        myIconView.setLayoutParams(ViewGroup.LayoutParams(width, -1))
        val addrHolder = AddrHolder(myIconView)
        addrHolder.myIconView = myIconView
        return addrHolder
    }

    fun itemAt(pos: Int): MainMenuItem? {
        val arrayList = this.menuItems
        if (arrayList == null || pos < 0 || pos >= arrayList.size) {
            return null
        }
        return this.menuItems!!.get(pos) as MainMenuItem?
    }

    fun buildItems(wide: Boolean, withAddr: Boolean) {
        val itemIds: IntArray?
        val arrayList: ArrayList<Any?> = ArrayList<Any?>()
        this.menuItems = arrayList
        val mainMenuItem: MainMenuItem = MainMenuItem()
        mainMenuItem.id = 1234
        arrayList.add(mainMenuItem)
        if (wide) {
            itemIds = MainUtil.parseIntList("0,1,2")
            this.spareItems = null
        } else {
            itemIds = MainUtil.parseIntList(MainSettings.addrItems.value)
            this.spareItems = ArrayList<Any?>()
        }
        if (itemIds != null && itemIds.size != 0) {
            val length = itemIds.size
            for (id in itemIds) {
                val copy: MainMenuItem = MainMenuItem()
                copy.id = id
                this.menuItems!!.add(copy)
            }
            if (!wide) {
                for (newId in 0..2) {
                    var index = 0
                    while (true) {
                        if (index >= length) {
                            val otherCopy: MainMenuItem = MainMenuItem()
                            otherCopy.id = newId
                            this.spareItems!!.add(otherCopy)
                            break
                        } else if (itemIds[index] == newId) {
                            break
                        } else {
                            index++
                        }
                    }
                }
            }
        } else if (!wide) {
            for (id in 0..2) {
                val nextCopy: MainMenuItem = MainMenuItem()
                nextCopy.id = id
                this.spareItems!!.add(nextCopy)
            }
        }
        if (withAddr) {
            notifyDataSetChanged()
        }
    }
}
