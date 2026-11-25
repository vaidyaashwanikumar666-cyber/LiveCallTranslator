package com.livetranslator.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale
import java.util.UUID

/** Text-to-speech: bolta hai translated text sunne wale ki bhasha me. */
class Speaker(
    ctx: Context,
    private val onSpeakingChanged: (Boolean) -> Unit,
    private val onError: (String) -> Unit,
) : TextToSpeech.OnInitListener {

    private val main = Handler(Looper.getMainLooper())
    private val tts = TextToSpeech(ctx.applicationContext, this)
    private var ready = false
    private val pending = mutableListOf<Pair<String, Lang>>()
    private var queued = 0

    val isSpeaking get() = queued > 0

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            onError("Text-to-speech engine start nahi hua. Google TTS install karein.")
            return
        }
        ready = true
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {}
            override fun onDone(id: String?) {
                main.post { finished() }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                main.post { finished() }
            }
        })
        pending.forEach { speak(it.first, it.second) }
        pending.clear()
    }

    private fun finished() {
        if (queued > 0) queued--
        if (queued == 0) onSpeakingChanged(false)
    }

    fun speak(text: String, lang: Lang) {
        if (text.isBlank()) return
        if (!ready) { pending += text to lang; return }
        val r = tts.setLanguage(Locale.forLanguageTag(lang.locale))
        if (r == TextToSpeech.LANG_MISSING_DATA || r == TextToSpeech.LANG_NOT_SUPPORTED) {
            onError("${lang.name} ki awaaz phone me nahi hai. Settings → Text-to-speech → Google → Install voice data.")
            return
        }
        if (queued == 0) onSpeakingChanged(true)
        queued++
        tts.speak(text, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString())
    }

    fun stop() {
        tts.stop()
        if (queued > 0) { queued = 0; onSpeakingChanged(false) }
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
