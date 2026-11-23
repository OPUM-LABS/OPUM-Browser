package com.mycompany.app.main

/**
 * Holder for the download-list row model ([DownListItem]) and its click callbacks ([MainDownListener]: a = row,
 * play, info). The RecyclerView adapter itself is gone: the lists draw their rows with
 * `ui.browser.DownRow` (DialogVideoList, and DialogDownUrl since 2026-11-17).
 */
class MainDownAdapter private constructor() {
    class DownListItem {
        constructor() : super()

        @JvmField
        var index: Int = 0
        @JvmField
        var url: String? = null
        @JvmField
        var title: String? = null
        @JvmField
        var fmt: String? = null
        @JvmField
        var checkState: Byte = 0
        @JvmField
        var thumb: String? = null
        @JvmField
        var isLive: Boolean = false
        @JvmField
        var isHidden: Boolean = false
        @JvmField
        var audioUrl: String? = null
        @JvmField
        var quality: Int = 0
        @JvmField
        var fileSize: Long = 0
        @JvmField
        var group: Int = 0

        constructor(i: Int, str: String?, str2: String?) {
            this.index = i
            this.title = str
            this.fmt = str2
        }
    }

    interface MainDownListener {
        fun onDownload(position: Int)

        fun onPlay(position: Int)

        fun onInfo(position: Int)
    }
}
