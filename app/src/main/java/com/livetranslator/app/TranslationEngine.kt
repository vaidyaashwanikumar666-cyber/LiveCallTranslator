package com.livetranslator.app

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Translation "brain" of the agent.
 *  1. Sarvam AI (cloud) if an API key is set  -> all 22 Indian languages
 *  2. Google ML Kit (on-device, offline, free) -> hi, mr, bn, gu, ta, te, kn, ur, en
 */
class TranslationEngine(private val ctx: Context) {

    private val http = OkHttpClient.Builder().callTimeout(20, TimeUnit.SECONDS).build()
    private val clients = mutableMapOf<String, Translator>()

    suspend fun translate(text: String, from: Lang, to: Lang): String {
        if (text.isBlank() || from.locale == to.locale) return text
        val key = Prefs.sarvamKey(ctx)
        val offlineOk = from.mlkit != null && to.mlkit != null

        if (key.isNotEmpty()) {
            try {
                return sarvam(text, from, to, key)
            } catch (e: Exception) {
                if (!offlineOk) throw e   // fall back to offline when possible
            }
        }
        if (offlineOk) return mlkit(text, from.mlkit!!, to.mlkit!!)
        throw IllegalStateException(
            "${from.name} ↔ ${to.name} offline support nahi hai. Settings me Sarvam AI API key daalein."
        )
    }

    /** Pre-download offline models so calls work without delay. */
    suspend fun prepare(a: Lang, b: Lang): Boolean {
        if (a.mlkit == null || b.mlkit == null || a == b) return false
        client(a.mlkit, b.mlkit).downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
        client(b.mlkit, a.mlkit).downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
        return true
    }

    private fun client(src: String, dst: String): Translator = clients.getOrPut("$src>$dst") {
        Translation.getClient(
            TranslatorOptions.Builder().setSourceLanguage(src).setTargetLanguage(dst).build()
        )
    }

    private suspend fun mlkit(text: String, src: String, dst: String): String {
        val c = client(src, dst)
        c.downloadModelIfNeeded(DownloadConditions.Builder().build()).await()
        return c.translate(text).await()
    }

    private suspend fun sarvam(text: String, from: Lang, to: Lang, key: String): String =
        withContext(Dispatchers.IO) {
            val body = JSONObject()
                .put("input", text)
                .put("source_language_code", from.sarvam)
                .put("target_language_code", to.sarvam)
                .put("model", "sarvam-translate:v1")
                .toString()
                .toRequestBody("application/json".toMediaType())
            val req = Request.Builder()
                .url("https://api.sarvam.ai/translate")
                .header("api-subscription-key", key)
                .post(body)
                .build()
            http.newCall(req).execute().use { r ->
                val s = r.body?.string().orEmpty()
                if (!r.isSuccessful) throw IOException("Sarvam AI error ${r.code}: $s")
                JSONObject(s).getString("translated_text")
            }
        }

    fun close() {
        clients.values.forEach { it.close() }
        clients.clear()
    }
}
