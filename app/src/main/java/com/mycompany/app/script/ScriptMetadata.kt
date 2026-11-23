package com.mycompany.app.script

@Suppress("deprecation")
open class ScriptMetadata(
    name: String?,
    namespace: String?,
    strArr: Array<String?>?,
    strArr2: Array<String?>?,
    strArr3: Array<String?>?
) : ScriptCriteria(name, namespace, strArr, strArr2, strArr3) {
    @JvmField
    var description: String? = null
    @JvmField
    var downloadUrl: String? = null
    @JvmField
    var updateUrl: String? = null
    @JvmField
    var installUrl: String? = null
    @JvmField
    var iconText: String? = null
    @JvmField
    var runAt: String? = null
    @JvmField
    var flags: Int = 0
    @JvmField
    var version: String? = null
    @JvmField
    var requires: Array<ScriptRequire?>? = null
    @JvmField
    var resourceList: Array<ScriptResource?>? = null
}
