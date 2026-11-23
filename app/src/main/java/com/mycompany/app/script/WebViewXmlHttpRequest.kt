package com.mycompany.app.script

import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.webkit.WebView
import com.google.android.gms.common.internal.ImagesContract
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainUtil
import org.json.JSONObject
import java.io.IOException
import java.io.OutputStream
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

@Suppress("deprecation")
class WebViewXmlHttpRequest(context: Context?, webView: WebView?, detailsJson: String?) {
    @JvmField
    var appContext: Context? = null
    @JvmField
    var targetWeb: WebView? = null
    @JvmField
    var requestUrl: String? = null
    @JvmField
    var requestMethod: String? = null
    @JvmField
    var contextJson: JSONObject? = null
    @JvmField
    var headersJson: JSONObject? = null
    @JvmField
    var uploadJson: JSONObject? = null
    @JvmField
    var bodyData: String? = null
    @JvmField
    var onLoadFn: String? = null
    @JvmField
    var onErrorFn: String? = null
    @JvmField
    var onProgressFn: String? = null
    @JvmField
    var onStateFn: String? = null
    @JvmField
    var mimeTypeOverride: String? = null
    @JvmField
    var authUser: String? = null
    @JvmField
    var authPassword: String? = null
    @JvmField
    var onTimeoutFn: String? = null
    @JvmField
    var timeoutMs: Int = 0
    @JvmField
    var synchronous: Boolean = false

    internal inner class SendNowTask : Runnable {
        override fun run() {
            this@WebViewXmlHttpRequest.sendNow()
        }
    }

    init {
        if (!TextUtils.isEmpty(detailsJson)) {
            this.appContext = context
            this.targetWeb = webView
            try {
                val details = JSONObject(detailsJson!!)
                val string = details.getString(ImagesContract.URL)
                this.requestUrl = string
                if (!TextUtils.isEmpty(string)) {
                    val requestMethod = details.getString("method")
                    this.requestMethod = requestMethod
                    if (!TextUtils.isEmpty(requestMethod)) {
                        this.contextJson = details.optJSONObject("context")
                        this.headersJson = details.optJSONObject("headers")
                        this.uploadJson = details.optJSONObject("upload")
                        this.bodyData = details.optString("data")
                        this.onLoadFn = details.optString("onload")
                        this.onErrorFn = details.optString("onerror")
                        this.onProgressFn = details.optString("onprogress")
                        this.onStateFn = details.optString("onreadystatechange")
                        this.mimeTypeOverride = details.optString("overrideMimeType")
                        this.authUser = details.optString("user")
                        this.authPassword = details.optString("password")
                        this.onTimeoutFn = details.optString("ontimeout")
                        this.timeoutMs = details.optInt("timeout")
                        this.synchronous = details.optBoolean("synchronous")
                    }
                }
            } catch (unused: Exception) {
            }
        }
    }

    fun send(): WebViewXmlHttpResponse {
        if (this.synchronous) {
            return sendNow()
        }
        val webViewXmlHttpResponse = WebViewXmlHttpResponse(this.contextJson)
        val context = this.appContext
        if (context == null) {
            return webViewXmlHttpResponse
        }
        MainApp.runInBackground(context, SendNowTask())
        return webViewXmlHttpResponse
    }

    /**
     * Performs the GM_xmlhttpRequest synchronously, reporting readyState 1-4 through onreadystatechange, download
     * progress through onprogress, then onload; failures go to onerror / ontimeout (upload.onerror when sending
     * the body fails). Only status 200 bodies are read.
     */
    fun sendNow(): WebViewXmlHttpResponse {
        val mimeOverride = this.mimeTypeOverride
        val onState = this.onStateFn
        val resp = WebViewXmlHttpResponse(this.contextJson)
        if (TextUtils.isEmpty(this.requestUrl) || TextUtils.isEmpty(this.requestMethod)) {
            return resp
        }
        var url: URL?
        try {
            url = URL(this.requestUrl)
        } catch (exception: Exception) {
            url = null
        }
        val onError = this.onErrorFn
        if (url == null) {
            callBack(resp, onError)
            return resp
        }
        var conn: HttpURLConnection? = null
        var out: OutputStream? = null
        var reader: UnicodeReader? = null
        var uploading = false
        try {
            conn = url.openConnection() as HttpURLConnection?
            resp.readyState = 1
            callBack(resp, onState)
            val body = bodyBytes()
            if (body != null) {
                conn!!.setDoOutput(true)
                conn.setRequestProperty("Content-Length", body.size.toString())
            }
            conn!!.setRequestMethod(this.requestMethod)
            if (this.headersJson != null) {
                applyHeaders(conn)
            }
            if (!TextUtils.isEmpty(this.authUser) && !TextUtils.isEmpty(this.authPassword)) {
                applyAuth(conn)
            }
            if (!TextUtils.isEmpty(mimeOverride)) {
                conn.setRequestProperty("Content-Type", mimeOverride)
            }
            if (this.timeoutMs > 0) {
                conn.setConnectTimeout(this.timeoutMs)
                conn.setReadTimeout(this.timeoutMs)
            }
            conn.connect()
            if (body != null) {
                uploading = true
                out = conn.getOutputStream()
                out.write(body)
                out.flush()
                out.close()
                callUploadLoad(resp)
                uploading = false
            }
            val status = conn.getResponseCode()
            resp.status = status
            resp.statusText = conn.getResponseMessage()
            resp.finalUrl = conn.getURL().toString()
            resp.readyState = 2
            callBack(resp, onState)
            if (status != 200) {
                conn.disconnect()
                callBack(resp, onError)
                return resp
            }
            val headers: String? = Companion.headerText(conn)
            if (headers != null) {
                resp.responseHeaders = headers
            }
            val length = conn.getContentLength()
            if (length > 0) {
                resp.lengthComputable = true
                resp.total = length.toLong()
            }
            resp.readyState = 3
            callBack(resp, onState)
            reader = UnicodeReader(conn.getInputStream(), conn.getContentEncoding())
            val chars = CharArray(4096)
            var sb: StringBuilder? = null
            var loaded = 0
            var read: Int
            while ((reader.reader!!.read(chars, 0, 4096).also { read = it }) != -1 && read > 0) {
                if (loaded > 0 && length > 0) {
                    resp.loaded = loaded.toLong()
                    callBack(resp, this.onProgressFn)
                }
                if (sb == null) {
                    sb = StringBuilder()
                }
                sb.append(chars, 0, read)
                loaded += read
            }
            reader.close()
            reader = null
            conn.disconnect()
            if (sb != null) {
                resp.responseText = sb.toString()
            }
            resp.readyState = 4
            callBack(resp, onState)
            callBack(resp, this.onLoadFn)
            conn = null
        } catch (exception: SocketTimeoutException) {
            callBack(resp, this.onTimeoutFn)
        } catch (exception: UnsupportedEncodingException) {
            callBack(resp, onError)
        } catch (exception: IOException) {
            if (!uploading) {
                callBack(resp, onError)
            } else if (this.uploadJson != null && this.uploadJson!!.length() != 0) {
                callBack(resp, this.uploadJson!!.optString("onerror"))
            }
        } catch (exception: Exception) {
            callBack(resp, onError)
        }
        if (out != null) {
            try {
                out.close()
            } catch (ignored: Exception) {
            }
        }
        if (reader != null) {
            try {
                reader.close()
            } catch (ignored: Exception) {
            }
        }
        if (conn != null) {
            try {
                conn.disconnect()
            } catch (ignored: Exception) {
            }
        }
        return resp
    }

    fun callBack(webViewXmlHttpResponse: WebViewXmlHttpResponse, onError: String?) {
        val webView: WebView? = this.targetWeb
        if (TextUtils.isEmpty(onError) || webView == null) {
            return
        }
        MainUtil.evalJs(
            webView,
            "(function(){unsafeWindow.$onError(JSON.parse(${webViewXmlHttpResponse.toJson()}));})();",
            true
        )
    }

    fun callUploadLoad(webViewXmlHttpResponse: WebViewXmlHttpResponse) {
        val upload = this.uploadJson
        if (upload == null || upload.length() == 0) {
            return
        }
        callBack(webViewXmlHttpResponse, upload.optString("onload"))
    }

    fun bodyBytes(): ByteArray? {
        val bodyData = this.bodyData
        if (TextUtils.isEmpty(bodyData)) {
            return null
        }
        try {
            return bodyData!!.toByteArray(charset("UTF-8"))
        } catch (unused: Exception) {
            return null
        }
    }

    fun applyAuth(httpURLConnection: HttpURLConnection) {
        try {
            val credentials = Base64.encodeToString(
                (this.authUser + ":" + this.authPassword).toByteArray(
                    charset("UTF-8")
                ), 0
            )
            val sb = StringBuilder("Basic ")
            sb.append(credentials)
            httpURLConnection.setRequestProperty("Authorization", sb.toString())
        } catch (unused: Exception) {
        }
    }

    fun applyHeaders(httpURLConnection: HttpURLConnection) {
        val headers = this.headersJson
        if (headers != null) {
            try {
                val itKeys = headers.keys()
                while (itKeys.hasNext()) {
                    val next = itKeys.next()
                    val valueText = headers.getString(next)
                    if (!TextUtils.isEmpty(next) && !TextUtils.isEmpty(valueText)) {
                        httpURLConnection.setRequestProperty(next, valueText)
                    }
                }
            } catch (unused: Exception) {
            }
        }
    }

    companion object {
        @JvmStatic
        fun headerText(httpURLConnection: HttpURLConnection): String? {
            try {
                val setEntrySet = httpURLConnection.getHeaderFields().entries
                var sb: StringBuilder? = null
                for (entry in setEntrySet) {
                    val key = entry.key
                    val value: MutableList<String?>? = entry.value
                    if (!TextUtils.isEmpty(key) && value != null && !value.isEmpty()) {
                        if (sb == null) {
                            sb = StringBuilder()
                        }
                        sb.append(key!!.lowercase())
                        sb.append(": ")
                        val size = value.size
                        if (size == 1) {
                            sb.append(value.get(0))
                        } else {
                            for (index in 0..<size) {
                                sb.append(value.get(index))
                                if (index != size - 1) {
                                    sb.append("; ")
                                }
                            }
                        }
                    }
                }
                if (sb != null) {
                    return sb.toString()
                }
            } catch (unused: Exception) {
            }
            return null
        }
    }
}
