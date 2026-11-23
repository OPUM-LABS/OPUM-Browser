package com.mycompany.app.view

@Suppress("deprecation")
interface MyFadeListener {
    fun onFadeEnd(shown: Boolean)

    fun onFadeStart(shown: Boolean, animate: Boolean)
}
