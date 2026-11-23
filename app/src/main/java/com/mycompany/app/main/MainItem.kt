package com.mycompany.app.main

import android.content.pm.ResolveInfo
import com.mycompany.app.compress.Compress

@Suppress("deprecation")
class MainItem {
    class ChildItem {
        @JvmField
        var time: Long = 0
        @JvmField
        var size: Long = 0
        @JvmField
        var readSize: Long = 0
        @JvmField
        var parentId: Long = 0
        @JvmField
        var uid: Long = 0
        @JvmField
        var dateText: String? = null
        @JvmField
        var subText: String? = null
        @JvmField
        var groupIndex: Int = 0
        @JvmField
        var position: Int = 0
        @JvmField
        var isChecked: Boolean = false
        @JvmField
        var isMarked: Boolean = false
        @JvmField
        var isEnabled: Boolean = false
        @JvmField
        var isSecret: Boolean = false
        @JvmField
        var compress: Compress? = null
        @JvmField
        var mimeType: String? = null
        @JvmField
        var resolveInfo: ResolveInfo? = null
        @JvmField
        var blockPerms: Int = 0
        @JvmField
        var listType: Int = 0
        @JvmField
        var rowType: Byte = 0
        @JvmField
        var itemType: Byte = 0
        @JvmField
        var status: Int = 0
        @JvmField
        var dir: String? = null
        @JvmField
        var dirName: String? = null
        @JvmField
        var path: String? = null
        @JvmField
        var name: String? = null
        @JvmField
        var namespace: String? = null
        @JvmField
        var author: String? = null
        @JvmField
        var isDir: Boolean = false
        @JvmField
        var groupKey: String? = null
        @JvmField
        var sortName: String? = null
        @JvmField
        var lowerName: String? = null
        @JvmField
        var sortExt: String? = null
        @JvmField
        var order: Long = 0
        @JvmField
        var url: String? = null
        @JvmField
        var referer: String? = null
        @JvmField
        var count: Int = 0
        @JvmField
        var index: Int = 0
        @JvmField
        var page: Int = 0
        @JvmField
        var iconColor: Int = 0
        @JvmField
        var iconRes: Int = 0
        @JvmField
        var badge: String? = null
        @JvmField
        var id: Long = 0
        @JvmField
        var filePath: String? = null
    }

    class GroupItem {
        @JvmField
        var title: String? = null
        @JvmField
        var itemCount: Int = 0
        @JvmField
        var groupPos: Int = 0
        @JvmField
        var checkedCount: Int = 0
        @JvmField
        var allChecked: Boolean = false
    }

    class ViewItem {
        @JvmField
        var itemType: Byte = 0
        @JvmField
        var compress: Compress? = null
        @JvmField
        var position: Int = 0
        @JvmField
        var url: String? = null
        @JvmField
        var referer: String? = null
        @JvmField
        var filePath: String? = null
        @JvmField
        var cacheType: Int = 0
        @JvmField
        var iconUrl: String? = null
    }
}
