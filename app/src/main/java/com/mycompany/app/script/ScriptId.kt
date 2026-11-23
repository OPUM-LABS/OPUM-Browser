package com.mycompany.app.script

import android.text.TextUtils

@Suppress("deprecation")
open class ScriptId(@JvmField var name: String?, @JvmField var namespace: String?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val scriptId = other as ScriptId
        val name = scriptId.name
        val nameNow = this.name
        if (nameNow == null) {
            if (name != null) {
                return false
            }
        } else if (nameNow != name) {
            return false
        }
        val namespace = scriptId.namespace
        val namespaceNow = this.namespace
        if (namespaceNow == null) {
            if (namespace != null) {
                return false
            }
        } else if (namespaceNow != namespace) {
            return false
        }
        return true
    }

    override fun hashCode(): Int {
        val name = this.name
        val hash = ((if (name == null) 0 else name.hashCode()) + 31) * 31
        val namespace = this.namespace
        return hash + (if (namespace != null) namespace.hashCode() else 0)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(this.name)
        val namespace = this.namespace
        if (!TextUtils.isEmpty(namespace)) {
            sb.append(", ")
            sb.append(namespace)
        }
        return sb.toString()
    }
}
