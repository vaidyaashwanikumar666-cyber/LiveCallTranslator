package com.livetranslator.app

import android.content.Context

object Prefs {
    const val DEFAULT_SERVER = "ws://10.0.2.2:8080"

    private fun sp(c: Context) = c.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun myLang(c: Context) = Languages.byLocale(sp(c).getString("my", "hi-IN"))
    fun otherLang(c: Context) = Languages.byLocale(sp(c).getString("other", "mr-IN"))
    fun name(c: Context) = sp(c).getString("name", "")!!.ifBlank { "Dost" }
    fun rawName(c: Context) = sp(c).getString("name", "")!!
    fun server(c: Context) = sp(c).getString("server", DEFAULT_SERVER)!!
    fun sarvamKey(c: Context) = sp(c).getString("sarvam", "")!!.trim()

    fun save(c: Context, my: Lang, other: Lang, name: String, server: String, sarvam: String) {
        sp(c).edit()
            .putString("my", my.locale)
            .putString("other", other.locale)
            .putString("name", name.trim())
            .putString("server", server.trim())
            .putString("sarvam", sarvam.trim())
            .apply()
    }
}
