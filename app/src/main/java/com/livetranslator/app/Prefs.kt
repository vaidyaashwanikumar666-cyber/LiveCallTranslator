package com.livetranslator.app

import android.content.Context

object Prefs {
    const val DEFAULT_SERVER = "wss://livecalltranslator-relay.onrender.com"

    private fun sp(c: Context) = c.getSharedPreferences("settings", Context.MODE_PRIVATE)

    fun myLang(c: Context) = Languages.byLocale(sp(c).getString("my", "hi-IN"))
    fun otherLang(c: Context) = Languages.byLocale(sp(c).getString("other", "mr-IN"))
    fun name(c: Context) = sp(c).getString("name", "")!!.ifBlank { "Dost" }
    fun rawName(c: Context) = sp(c).getString("name", "")!!
    fun server(c: Context) = normalize(sp(c).getString("server", DEFAULT_SERVER)!!.ifBlank { DEFAULT_SERVER })

    /** "https://abc.onrender.com" ya "abc.onrender.com" ko "wss://abc.onrender.com" banata hai. */
    fun normalize(url: String): String {
        val u = url.trim().trimEnd('/')
        return when {
            u.startsWith("https://") -> "wss://" + u.removePrefix("https://")
            u.startsWith("http://") -> "ws://" + u.removePrefix("http://")
            u.startsWith("ws://") || u.startsWith("wss://") -> u
            else -> "wss://$u"
        }
    }
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
