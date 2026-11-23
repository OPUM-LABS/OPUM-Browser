package com.mycompany.app.script

@Suppress("deprecation")
open class ScriptCriteria(
    name: String?,
    namespace: String?,
    @JvmField var excludes: Array<String?>?,
    @JvmField var includes: Array<String?>?,
    @JvmField var matches: Array<String?>?
) : ScriptId(name, namespace)
