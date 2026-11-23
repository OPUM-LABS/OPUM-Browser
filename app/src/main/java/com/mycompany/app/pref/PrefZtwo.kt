package com.mycompany.app.pref

import android.content.Context
import android.text.TextUtils
import com.mycompany.app.main.MainApp
import com.mycompany.app.main.MainConst

@Suppress("deprecation")
class PrefZtwo(context: Context?) : PrefCore(context, "PrefZtwo") {
    companion object {
        @JvmField
        var loadedTag: String? = null
        // the values live in pref/store/ZtwoSettings since 2026-11-17

        @JvmStatic
        fun get(context: Context?, reload: Boolean): PrefZtwo? {
            var reload = reload
            val mainAppO = MainApp.from(context)
            if (mainAppO == null) {
                return PrefZtwo(null)
            }
            if (!reload && TextUtils.isEmpty(loadedTag)) {
                reload = true
            }
            val prefZtwo = mainAppO.prefZtwo
            if (prefZtwo == null) {
                synchronized(PrefZtwo::class.java) {
                    if (mainAppO.prefZtwo == null) {
                        mainAppO.prefZtwo = PrefZtwo(context)
                        reload = false
                    }
                }
            } else if (TextUtils.isEmpty(prefZtwo.filePath)) {
                synchronized(PrefZtwo::class.java) {
                    mainAppO.prefZtwo!!.init(context, "PrefZtwo")
                }
                reload = false
            }
            if (reload || mainAppO.prefZtwo!!.notLoaded()) {
                mainAppO.prefZtwo!!.load()
            }
            return mainAppO.prefZtwo
        }

        /** Loads the settings ([reload] = read the file again first, e.g. after a backup restore). */
        @JvmStatic
        fun load(context: Context?, reload: Boolean) {
            if (context == null) return
            if (reload) get(context, true)
            com.mycompany.app.pref.store.ZtwoSettings.load(context)
        }

        /** Saves the user agent choice (type and custom text). */
        @JvmStatic
        fun saveAgent(context: Context?) {
            val st = com.mycompany.app.pref.store.ZtwoSettings
            st.save(context, listOf(st.agentType, st.agentUser))
        }

        /** Saves the news topic and search. */
        @JvmStatic
        fun saveNews(context: Context?) {
            val st = com.mycompany.app.pref.store.ZtwoSettings
            st.save(context, listOf(st.newsTopic, st.newsSearch))
        }

        /** Works out whether the custom search address takes the query (`%s`) and saves the search choice. */
        @JvmStatic
        fun saveSearch(context: Context?) {
            if (context == null) return
            val st = com.mycompany.app.pref.store.ZtwoSettings
            val user = st.searchUser.value
            st.editLocal { set(st.searchForm, st.searchType.value >= 10 && !TextUtils.isEmpty(user) && user!!.contains("%s")) }
            st.save(context, listOf(st.searchType, st.searchUser, st.searchForm, st.searchColor))
        }
    }
}
