package com.livetranslator.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/** Speech-to-text: sunta hai boli hui bhasha. Continuous mode me apne aap dobara shuru hota hai. */
class Listener(private val ctx: Context, private val cb: Callback) {

    interface Callback {
        fun onPartial(text: String)
        fun onFinal(text: String)
        fun onListening(active: Boolean)
        fun onListenError(msg: String)
    }

    private val main = Handler(Looper.getMainLooper())
    private var sr: SpeechRecognizer? = null
    private var lang: Lang? = null
    private var continuous = false
    private var wanted = false
    private var paused = false
    var currentLang: Lang? = null
        private set

    fun start(l: Lang, continuous: Boolean) {
        lang = l
        currentLang = l
        this.continuous = continuous
        wanted = true
        restart(0)
    }

    fun stop() {
        wanted = false
        main.removeCallbacksAndMessages(null)
        sr?.cancel()
        cb.onListening(false)
    }

    /** Pause while our own TTS is speaking (so we don't translate ourselves) or when muted. */
    fun setPaused(p: Boolean) {
        if (paused == p) return
        paused = p
        if (p) {
            main.removeCallbacksAndMessages(null)
            sr?.cancel()
            cb.onListening(false)
        } else if (wanted) {
            restart(300)
        }
    }

    fun destroy() {
        stop()
        sr?.destroy()
        sr = null
    }

    private fun restart(delay: Long) {
        main.removeCallbacksAndMessages(null)
        main.postDelayed({ begin() }, delay)
    }

    private fun begin() {
        if (!wanted || paused) return
        val l = lang ?: return
        val r = sr ?: SpeechRecognizer.createSpeechRecognizer(ctx).also {
            it.setRecognitionListener(listener)
            sr = it
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, l.locale)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, l.locale)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, ctx.packageName)
        }
        r.startListening(intent)
        cb.onListening(true)
    }

    private fun first(b: Bundle?): String? =
        b?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.trim()

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onEvent(eventType: Int, params: Bundle?) {}

        override fun onPartialResults(b: Bundle?) {
            first(b)?.takeIf { it.isNotEmpty() }?.let { cb.onPartial(it) }
        }

        override fun onResults(b: Bundle?) {
            cb.onListening(false)
            first(b)?.takeIf { it.isNotEmpty() }?.let { cb.onFinal(it) }
            if (continuous && wanted) restart(150) else wanted = false
        }

        override fun onError(error: Int) {
            cb.onListening(false)
            if (!wanted || paused) return
            when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    wanted = false
                    cb.onListenError("Microphone permission nahi mili")
                }
                SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED,
                SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE -> {
                    wanted = false
                    cb.onListenError("${lang?.name} speech recognition is phone par available nahi hai (Google app update/offline speech pack install karein)")
                }
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                    if (continuous) restart(150) else { wanted = false; cb.onListenError("Kuch sunai nahi diya, dobara boliye") }
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                SpeechRecognizer.ERROR_SERVER -> {
                    cb.onListenError("Network problem – dobara koshish…")
                    if (continuous) restart(1500) else wanted = false
                }
                else -> if (continuous) restart(600) else wanted = false
            }
        }
    }
}
