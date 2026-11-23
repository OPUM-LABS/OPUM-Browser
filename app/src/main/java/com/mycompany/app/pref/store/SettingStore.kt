package com.mycompany.app.pref.store

import android.content.Context
import com.mycompany.app.pref.PrefCore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Typed settings over the existing preference files (`files/.pref/<name>`, read and written through [PrefCore]). The
 * keys and defaults stay exactly as before, so existing installs and backups keep working. Each [Setting] holds its
 * current value as a [StateFlow]; code reads [Setting.value] (or collects [Setting.flow]) and changes values only
 * through the store: [edit] (value and file together), [editLocal] + [save] (value now, file later), [remove] (drop
 * the stored key, keep the value) and [reset] (drop the key, back to the default).
 */
abstract class SettingStore {
    private val settings = mutableListOf<Setting<*>>()

    /** The group's preference file (the legacy `Pref*.r(context)` singleton). */
    protected abstract fun open(context: Context): PrefCore?

    /** Fixes up loaded values (e.g. a "-1 = compute" default); runs after every [load]. */
    protected open fun afterLoad() {}

    /** Reads every setting from the file (app start, and again after a backup is restored). */
    fun load(context: Context) {
        val core = open(context) ?: return
        for (s in settings) s.load(core)
        pinUnset(context, core)
        afterLoad()
    }

    /**
     * Settings whose factory default changed (they carry their old default, see [bool] `legacy`) and that are not in
     * the file yet: an install that already existed (the first-start wizard is done) keeps the old value, a new
     * install takes the new one. Either way the value is written, so it never changes behind the user's back later.
     */
    private fun pinUnset(context: Context, core: PrefCore) {
        val unset = settings.filter { it.hasLegacy && !core.has(it.key) }
        if (unset.isEmpty()) return
        val existing = isExistingInstall(context)
        for (s in unset) {
            if (existing) s.useLegacy()
            s.store(core)
        }
        core.saveAsync()
    }

    /** The app was set up before (the first-start wizard was finished or skipped; `SyncSettings.splash`). */
    private fun isExistingInstall(context: Context): Boolean =
        com.mycompany.app.pref.PrefSync.get(context, false)?.readBool("mSplash", true) == false

    /**
     * Changes values with [Editor.set]; the changed keys are written to the file in one go. Without a context (or a
     * file) only the values change, as the old field writes did.
     */
    fun edit(context: Context?, block: Editor.() -> Unit) {
        val editor = Editor()
        editor.block()
        if (editor.changed.isEmpty() || context == null) return
        val core = open(context) ?: return
        for (s in editor.changed) s.store(core)
        core.saveAsync()
    }

    /** Changes values in memory only; [save] writes them later (e.g. after a drag ends). */
    fun editLocal(block: Editor.() -> Unit) {
        Editor().block()
    }

    /** Writes the current values of [which] to the file. */
    fun save(context: Context?, which: List<Setting<*>>) {
        val core = open(context ?: return) ?: return
        for (s in which) s.store(core)
        core.saveAsync()
    }

    /** Removes [which] from the file but keeps their current values (the next start reads the defaults). */
    fun remove(context: Context?, which: List<Setting<*>>) {
        val core = open(context ?: return) ?: return
        for (s in which) core.removeKey(s.key)
        core.saveAsync()
    }

    /** Removes [which] from the file and puts each back to its default. */
    fun reset(context: Context?, which: List<Setting<*>>) {
        val core = open(context ?: return) ?: return
        for (s in which) {
            s.toDefault()
            // a setting whose factory default changed is written, else an existing install would read its old default
            if (s.hasLegacy) s.store(core) else core.removeKey(s.key)
        }
        core.saveAsync()
    }

    inner class Editor internal constructor() {
        internal val changed = LinkedHashSet<Setting<*>>()

        fun <T> set(setting: Setting<T>, value: T) {
            setting.put(value)
            changed.add(setting)
        }
    }

    /** [legacy]: the factory default before it changed, kept by installs that already existed ([pinUnset]). */
    protected fun bool(key: String, legacy: Boolean? = null, default: () -> Boolean) =
        add(Setting(key, default, { c, k, d -> c.readBool(k, d) }, { c, k, v -> c.putBool(k, v) }, legacy))

    protected fun int(key: String, legacy: Int? = null, default: () -> Int) =
        add(Setting(key, default, { c, k, d -> c.readInt(d, k) }, { c, k, v -> c.putInt(v, k) }, legacy))

    protected fun float(key: String, default: () -> Float) =
        add(Setting(key, default, { c, k, d -> c.readFloat(k, d) }, { c, k, v -> c.putFloat(k, v) }))

    protected fun long(key: String, default: () -> Long) =
        add(Setting(key, default, { c, k, d -> c.readLong(d, k) }, { c, k, v -> c.putLong(v, k) }))

    protected fun string(key: String, legacy: String? = null, default: () -> String?) =
        add(Setting(key, default, { c, k, d -> c.readString(k, d) }, { c, k, v -> c.putString(k, v) }, legacy))

    private fun <T> add(setting: Setting<T>): Setting<T> {
        settings.add(setting)
        return setting
    }
}

/** One stored value: its key in the group's file, its default, and its current value. */
class Setting<T> internal constructor(
    val key: String,
    private val default: () -> T,
    private val read: (PrefCore, String, T) -> T,
    private val write: (PrefCore, String, T) -> Unit,
    private val legacy: T? = null,
) {
    /** The factory default changed; [legacy] is the old one. */
    internal val hasLegacy: Boolean get() = legacy != null

    internal fun useLegacy() {
        if (legacy != null) state.value = legacy
    }

    private val state = MutableStateFlow(default())
    val flow: StateFlow<T> = state.asStateFlow()
    val value: T get() = state.value
    /** The value equals the default (stored or not). */
    val isDefault: Boolean get() = state.value == default()

    internal fun load(core: PrefCore) {
        state.value = read(core, key, default())
    }

    internal fun put(texCoords: T) {
        state.value = texCoords
    }

    internal fun store(core: PrefCore) = write(core, key, state.value)

    internal fun toDefault() {
        state.value = default()
    }

    /** For [SettingStore.afterLoad] fix-ups: changes the value without writing the file. */
    internal fun fix(t: T) {
        state.value = t
    }
}
