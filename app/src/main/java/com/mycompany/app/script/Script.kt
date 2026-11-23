package com.mycompany.app.script

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainUtil
import java.io.ByteArrayInputStream
import java.util.Scanner
import java.util.UUID
import java.util.regex.Pattern

@Suppress("deprecation")
class Script(
    name: String?,
    namespace: String?,
    strArr: Array<String?>?,
    strArr2: Array<String?>?,
    strArr3: Array<String?>?,
    description: String?,
    downloadUrl: String?,
    updateUrl: String?,
    installUrl: String?,
    iconText: String?,
    runAt: String?,
    flags: Int,
    version: String?,
    scriptRequireArr: Array<ScriptRequire?>?,
    scriptResourceArr: Array<ScriptResource?>?,
    iconUrlParam: String?,
    locCodeParam: String?,
    locNameParam: String?,
    locDescParam: String?,
    authorNameParam: String?
) : ScriptMetadata(name, namespace, strArr, strArr2, strArr3) {
    @JvmField
    var iconUrl: String?
    @JvmField
    var rowId: Long = 0
    @JvmField
    var updatedAt: Long = 0
    @JvmField
    var enabled: Boolean = false
    @JvmField
    var locCode: String?
    @JvmField
    var locName: String?
    @JvmField
    var locDesc: String?
    @JvmField
    var authorName: String?

    init {
        this.description = description
        this.downloadUrl = downloadUrl
        this.updateUrl = updateUrl
        this.installUrl = installUrl
        this.iconText = iconText
        this.runAt = runAt
        this.flags = flags
        this.version = version
        this.requires = scriptRequireArr
        this.resourceList = scriptResourceArr
        this.iconUrl = iconUrlParam
        this.locCode = locCodeParam
        this.locName = locNameParam
        this.locDesc = locDescParam
        this.authorName = authorNameParam
    }

    companion object {
        @JvmField
        var pendingText: String? = null

        /** Downloads an @require url and wraps its text; null when it cannot be fetched or is empty.  */
        @JvmStatic
        fun loadRequire(url: String?): ScriptRequire? {
            if (TextUtils.isEmpty(url)) {
                return null
            }
            var body: String? = null
            val data = DownloadHelper.download(url)
            if (data != null && data.bytes != null) {
                var reader: UnicodeReader? = null
                try {
                    reader = UnicodeReader(ByteArrayInputStream(data.bytes), data.encoding)
                    val chars = CharArray(4096)
                    var sb: StringBuilder? = null
                    var count: Int
                    while ((reader.reader!!.read(chars, 0, 4096).also { count = it }) != -1) {
                        if (count > 0) {
                            if (sb == null) {
                                sb = StringBuilder()
                            }
                            sb.append(chars, 0, count)
                        }
                    }
                    body = if (sb == null) null else sb.toString()
                } catch (exception: Exception) {
                    body = null
                }
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (ignored: Exception) {
                    }
                }
            }
            return if (body == null) null else ScriptRequire(url, body)
        }

        @JvmStatic
        fun buildScript(script: Script, scriptCacheUrl: String?): String? {
            try {
                val namespace = script.namespace
                val name = script.name
                val scriptArgs = "\"" + name!!.replace("\"", "\\\"") + "\", \"" + namespace!!.replace(
                    "\"",
                    "\\\""
                ) + "\", \"" + scriptCacheUrl + "\""
                val apiPrefix = ("GM_" + name + namespace + UUID.randomUUID()
                    .toString()).replace("[^0-9a-zA-Z_]".toRegex(), "")
                val sb = StringBuilder()
                sb.append("unsafeWindow = (function() { var el = document.createElement('p'); el.setAttribute('onclick', 'return window;'); return el.onclick(); }()); window.wrappedJSObject = unsafeWindow;\n")
                sb.append("var GM_listValues = function() { return ")
                sb.append("android.onUsListValues(")
                sb.append(scriptArgs)
                sb.append(").split(\",\"); };\n")
                sb.append("var GM_getValue = function(key, defaultValue) { ")
                sb.append("if (defaultValue === undefined) {defaultValue = null;} ")
                sb.append("defaultValue = JSON.stringify(defaultValue); ")
                sb.append("return JSON.parse(android.onUsGetValue(")
                sb.append(scriptArgs)
                sb.append(", key, defaultValue)); };\n")
                sb.append("var GM_setValue = function(key, value) { ")
                sb.append("if (value === undefined) {value = null;} ")
                sb.append("value = JSON.stringify(value); ")
                sb.append("android.onUsSetValue(")
                sb.append(scriptArgs)
                sb.append(", key, value); };\n")
                sb.append("var GM_deleteValue = function(key) { ")
                sb.append("android.onUsDeleteValue(")
                sb.append(scriptArgs)
                sb.append(", key); };\n")
                sb.append("var GM_addStyle = function(css) { ")
                sb.append("var style = document.createElement(\"style\"); ")
                sb.append("style.type = \"text/css\"; style.innerHTML = css; ")
                sb.append("document.getElementsByTagName('head')[0].appendChild(style); };\n")
                sb.append("var GM_log = function(message) { ")
                sb.append("android.onUsLog(")
                sb.append(scriptArgs)
                sb.append(", message); };\n")
                sb.append("var GM_openInTab = function(url,option) { ")
                sb.append("android.onUsOpenInTab(")
                sb.append(scriptArgs)
                sb.append(", url); };\n")
                sb.append("var GM_setClipboard = function(data,type) { ")
                sb.append("android.onUsSetClipboard(")
                sb.append(scriptArgs)
                sb.append(", data); };\n")
                sb.append("var GM_getResourceURL = function(resourceName) { return ")
                sb.append("android.onUsGetResourceURL(")
                sb.append(scriptArgs)
                sb.append(", resourceName); };\n")
                sb.append("var GM_getResourceText = function(resourceName) { return ")
                sb.append("android.onUsGetResourceText(")
                sb.append(scriptArgs)
                sb.append(", resourceName); };\n")
                sb.append("var GM_xmlhttpRequest = function(details) { \n")
                sb.append("if (details.onabort) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_onAbortCallback = details.onabort;\n")
                sb.append("details.onabort = '")
                sb.append(apiPrefix)
                sb.append("GM_onAbortCallback'; }\n")
                sb.append("if (details.onerror) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_onErrorCallback = details.onerror;\n")
                sb.append("details.onerror = '")
                sb.append(apiPrefix)
                sb.append("GM_onErrorCallback'; }\n")
                sb.append("if (details.onload) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_onLoadCallback = details.onload;\n")
                sb.append("details.onload = '")
                sb.append(apiPrefix)
                sb.append("GM_onLoadCallback'; }\n")
                sb.append("if (details.onprogress) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_onProgressCallback = details.onprogress;\n")
                sb.append("details.onprogress = '")
                sb.append(apiPrefix)
                sb.append("GM_onProgressCallback'; }\n")
                sb.append("if (details.onreadystatechange) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_onReadyStateChange = details.onreadystatechange;\n")
                sb.append("details.onreadystatechange = '")
                sb.append(apiPrefix)
                sb.append("GM_onReadyStateChange'; }\n")
                sb.append("if (details.ontimeout) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_onTimeoutCallback = details.ontimeout;\n")
                sb.append("details.ontimeout = '")
                sb.append(apiPrefix)
                sb.append("GM_onTimeoutCallback'; }\n")
                sb.append("if (details.upload) {\n")
                sb.append("if (details.upload.onabort) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_uploadOnAbortCallback = details.upload.onabort;\n")
                sb.append("details.upload.onabort = '")
                sb.append(apiPrefix)
                sb.append("GM_uploadOnAbortCallback'; }\n")
                sb.append("if (details.upload.onerror) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_uploadOnErrorCallback = details.upload.onerror;\n")
                sb.append("details.upload.onerror = '")
                sb.append(apiPrefix)
                sb.append("GM_uploadOnErrorCallback'; }\n")
                sb.append("if (details.upload.onload) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_uploadOnLoadCallback = details.upload.onload;\n")
                sb.append("details.upload.onload = '")
                sb.append(apiPrefix)
                sb.append("GM_uploadOnLoadCallback'; }\n")
                sb.append("if (details.upload.onprogress) { unsafeWindow.")
                sb.append(apiPrefix)
                sb.append("GM_uploadOnProgressCallback = details.upload.onprogress;\n")
                sb.append("details.upload.onprogress = '")
                sb.append(apiPrefix)
                sb.append("GM_uploadOnProgressCallback'; }\n")
                sb.append("}\n")
                sb.append("return JSON.parse(")
                sb.append("android.onUsXmlHttpRequest(")
                sb.append(scriptArgs)
                sb.append(", JSON.stringify(details))); };\n")
                sb.append("var GM_info = ")
                sb.append("function() {};\n")
                sb.append("var GM_registerMenuCommand = ")
                sb.append("function() {};\n")
                sb.append("var GM={};\n")
                sb.append("GM.listValues=GM_listValues;\n")
                sb.append("GM.getValue=GM_getValue;\n")
                sb.append("GM.setValue=GM_setValue;\n")
                sb.append("GM.deleteValue=GM_deleteValue;\n")
                sb.append("GM.addStyle=GM_addStyle;\n")
                sb.append("GM.log=GM_log;\n")
                sb.append("GM.openInTab=GM_openInTab;\n")
                sb.append("GM.setClipboard=GM_setClipboard;\n")
                sb.append("GM.getResourceURL=GM_getResourceURL;\n")
                sb.append("GM.getResourceText=GM_getResourceText;\n")
                sb.append("GM.xmlhttpRequest=GM_xmlhttpRequest;\n")
                sb.append("GM.info=GM_info;\n")
                sb.append("GM.registerMenuCommand=GM_registerMenuCommand;\n")
                val scriptRequireArr = script.requires
                if (scriptRequireArr != null) {
                    for (scriptRequire in scriptRequireArr) {
                        sb.append(scriptRequire!!.content)
                        sb.append("\n")
                    }
                }
                sb.append(script.iconUrl)
                if ((script.flags and 1) != 1) {
                    sb.insert(0, "(function(){")
                    sb.append("})();")
                }
                return sb.toString()
            } catch (unused: Exception) {
                return null
            }
        }

        /**
         * Parses a userscript's "// ==UserScript==" header. url is where it came from (default namespace, download and
         * update url, base for relative @icon/@require/@resource), name the fallback name (else derived from url),
         * lang/subLang pick localized "@name:xx" / "@description:xx". @require and @resource are downloaded here.
         * Returns null on a missing header, @name or @namespace; with report set, x then holds the reason.
         */
        @JvmStatic
        fun parse(
            context: Context?,
            url: String?,
            name: String?,
            content: String?,
            lang: String?,
            subLang: String?,
            report: Boolean
        ): Script? {
            var name = name
            pendingText = null
            if (TextUtils.isEmpty(content)) {
                return null
            }
            try {
                var namespace: String? = null
                var downloadUrl: String? = null
                var updateUrl: String? = null
                if (url != null) {
                    if (TextUtils.isEmpty(name)) {
                        name = MainUtil.scriptNameFor(context, url)
                    }
                    namespace = url
                    downloadUrl = url
                    updateUrl = url
                }
                val excludes = HashSet<String?>()
                val includes = HashSet<String?>()
                val matches = HashSet<String?>()
                val requires = HashSet<ScriptRequire?>()
                val resources = HashSet<ScriptResource?>()
                var description: String? = null
                var installUrl: String? = null
                var icon: String? = null
                var runAt: String? = null
                var version: String? = null
                var author: String? = null
                var localeTag: String? = null
                var localeName: String? = null
                var localeDescription: String? = null
                var flags = 0
                var inHeader = false
                var closed = false
                var scanner: Scanner? = null
                try {
                    scanner = Scanner(content)
                    val meta = Pattern.compile("// @(\\S+)(?:\\s+(.*))?")
                    while (scanner.hasNextLine()) {
                        var line = scanner.nextLine()
                        if (TextUtils.isEmpty(line)) {
                            continue
                        }
                        line = line!!.trim { it <= ' ' }
                        if (TextUtils.isEmpty(line)) {
                            continue
                        }
                        if (!inHeader) {
                            if (line.startsWith("// ==UserScript==")) {
                                inHeader = true
                            }
                            continue
                        }
                        if (line.startsWith("// ==/UserScript==")) {
                            closed = true
                            break
                        }
                        val m = meta.matcher(line)
                        if (!m.matches()) {
                            continue
                        }
                        var key = m.group(1)
                        if (TextUtils.isEmpty(key)) {
                            continue
                        }
                        key = key!!.trim { it <= ' ' }
                        if (TextUtils.isEmpty(key)) {
                            continue
                        }
                        if (key == "unwrap") {
                            flags = flags or 1
                            continue
                        }
                        var value = m.group(2)
                        if (TextUtils.isEmpty(value)) {
                            continue
                        }
                        value = value!!.trim { it <= ' ' }
                        if (TextUtils.isEmpty(value)) {
                            continue
                        }
                        if (key.startsWith("name")) {
                            val len = key.length
                            if (len == 4) {
                                name = value
                            } else if (len > 5) {
                                if (len == 9 && key.startsWith("space", 4)) {
                                    namespace = value
                                } else if (!TextUtils.isEmpty(lang) && key.startsWith(":", 4)) {
                                    if (!TextUtils.isEmpty(subLang) && key.startsWith(
                                            subLang!!,
                                            5
                                        )
                                    ) {
                                        localeTag = subLang
                                        localeName = value
                                    } else if (TextUtils.isEmpty(localeName) && key.startsWith(
                                            lang!!,
                                            5
                                        )
                                    ) {
                                        localeTag = lang
                                        localeName = value
                                    }
                                }
                            }
                        } else if (key.startsWith("description")) {
                            val len = key.length
                            if (len == 11) {
                                description = value
                            } else if (len > 12 && !TextUtils.isEmpty(lang) && key.startsWith(
                                    ":",
                                    11
                                )
                            ) {
                                if (!TextUtils.isEmpty(subLang) && key.startsWith(subLang!!, 12)) {
                                    localeTag = subLang
                                    localeDescription = value
                                } else if (TextUtils.isEmpty(localeDescription) && key.startsWith(
                                        lang!!,
                                        12
                                    )
                                ) {
                                    localeTag = lang
                                    localeDescription = value
                                }
                            }
                        } else if (key == "author") {
                            author = value
                        } else if (key == "downloadURL") {
                            downloadUrl = value
                        } else if (key == "updateURL") {
                            updateUrl = value
                        } else if (key == "installURL") {
                            installUrl = value
                        } else if (key == "icon") {
                            icon = DownloadHelper.resolveUrl(value, url)
                        } else if (key == "run-at") {
                            if (value == "document-start" || value == "document-end") {
                                runAt = value
                            }
                        } else if (key == "version") {
                            version = value
                        } else if (key == "require") {
                            val require: ScriptRequire? = loadRequire(DownloadHelper.resolveUrl(value, url))
                            if (require != null) {
                                requires.add(require)
                            }
                        } else if (key == "resource") {
                            val r = Pattern.compile("(\\S+)\\s+(.*)").matcher(value)
                            if (r.matches()) {
                                val resUrl = DownloadHelper.resolveUrl(r.group(2), url)
                                val resName = r.group(1)
                                var bytes: ByteArray? = null
                                if (!TextUtils.isEmpty(resUrl)) {
                                    val data = DownloadHelper.download(resUrl)
                                    bytes = if (data == null) null else data.bytes
                                }
                                if (bytes != null) {
                                    resources.add(ScriptResource(bytes, resName, resUrl))
                                }
                            }
                        } else if (key == "exclude") {
                            excludes.add(value)
                        } else if (key == "include") {
                            includes.add(value)
                        } else if (key == "match") {
                            matches.add(value)
                        } else if (key == "flag") {
                            flags = runAtFlags(flags, value)
                        } else if (key == "flags") {
                            val parts: Array<String?> =
                                value.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                                    .toTypedArray()
                            for (part in parts) {
                                flags = runAtFlags(flags, part)
                            }
                        } else if (key == "grant") {
                            if ("none" == value) {
                                flags = flags or 2
                            }
                        }
                    }
                } catch (exception: Exception) {
                    closed = false
                }
                if (scanner != null) {
                    try {
                        scanner.close()
                    } catch (ignored: Exception) {
                    }
                }
                if (!closed) {
                    if (report) {
                        pendingText = "UserScript not found"
                    }
                    return null
                }
                if (TextUtils.isEmpty(name)) {
                    if (report) {
                        pendingText = "@name not found"
                    }
                    return null
                }
                if (TextUtils.isEmpty(namespace)) {
                    if (report) {
                        pendingText = "@namespace not found"
                    }
                    return null
                }
                return Script(
                    name, namespace,
                    if (excludes.isEmpty()) null else excludes.toTypedArray<String?>(),
                    if (includes.isEmpty()) null else includes.toTypedArray<String?>(),
                    if (matches.isEmpty()) null else matches.toTypedArray<String?>(),
                    description, downloadUrl, updateUrl, installUrl, icon, runAt, flags, version,
                    if (requires.isEmpty()) null else requires.toTypedArray<ScriptRequire?>(),
                    if (resources.isEmpty()) null else resources.toTypedArray<ScriptResource?>(),
                    content, localeTag, localeName, localeDescription, author
                )
            } catch (exception: Exception) {
                return null
            }
        }

        @JvmStatic
        fun runAtFlags(flags: Int, part: String?): Int {
            if (!TextUtils.isEmpty(part)) {
                val flag = part!!.trim { it <= ' ' }
                if (!TextUtils.isEmpty(flag)) {
                    val lowerCase = flag.lowercase()
                    lowerCase.javaClass
                    if (lowerCase == "nojsclosure") {
                        return flags or 1
                    }
                    if (lowerCase == "nojssandbox") {
                        return flags or 2
                    }
                }
            }
            return flags
        }
    }
}
