package com.mycompany.app.script

import org.json.JSONObject

@Suppress("deprecation")
class WebViewXmlHttpResponse(@JvmField var json: JSONObject?) {
    @JvmField
    var readyState: Byte = 0
    @JvmField
    var responseHeaders: String? = null
    @JvmField
    var responseText: String? = null
    @JvmField
    var status: Int = 0
    @JvmField
    var statusText: String? = null
    @JvmField
    var finalUrl: String? = null
    @JvmField
    var lengthComputable: Boolean = false
    @JvmField
    var loaded: Long = 0
    @JvmField
    var total: Long = 0

    fun toJson(): String {
        var json: JSONObject? = JSONObject()
        try {
            json!!.put("readyState", this.readyState.toInt())
            json.put("responseHeaders", this.responseHeaders)
            json.put("responseText", this.responseText)
            json.put("status", this.status)
            json.put("statusText", this.statusText)
            json.put("finalUrl", this.finalUrl)
            json.put("lengthComputable", this.lengthComputable)
            json.put("loaded", this.loaded)
            json.put("total", this.total)
            val contextJson = this.json
            if (contextJson != null) {
                json.put("context", contextJson)
            }
        } catch (unused: Exception) {
            json = null
        }
        if (json == null) {
            return ""
        }
        try {
            return JSONObject.quote(json.toString())
        } catch (unused2: Exception) {
            return ""
        }
    }
}
