package com.livetranslator.app

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Mode 2 – App-to-App live translated call (hands-free).
 * Aap Hindi bolte ho -> text server se jata hai -> dusre phone par Marathi me translate + bola jata hai.
 * Woh Marathi bolte hain -> aapke phone par Hindi me sunai deta hai.
 */
class CallActivity : AppCompatActivity(), Listener.Callback, CallClient.Events {

    private lateinit var my: Lang
    private lateinit var engine: TranslationEngine
    private lateinit var speaker: Speaker
    private lateinit var listener: Listener
    private lateinit var client: CallClient
    private lateinit var transcript: Transcript
    private lateinit var tvStatus: TextView
    private lateinit var tvLive: TextView
    private lateinit var btnMute: Button
    private var muted = false
    private var inCall = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        my = Prefs.myLang(this)
        title = "Translated Call – ${my.name}"
        tvStatus = findViewById(R.id.tvStatus)
        tvLive = findViewById(R.id.tvLive)
        btnMute = findViewById(R.id.btnMute)
        transcript = Transcript(findViewById(R.id.tvTranscript), findViewById<ScrollView>(R.id.scroll))

        engine = TranslationEngine(this)
        listener = Listener(this, this)
        speaker = Speaker(this, { speaking -> listener.setPaused(speaking || muted) },
            { err -> transcript.note(err) })
        client = CallClient(this)

        val etRoom = findViewById<EditText>(R.id.etRoom)
        findViewById<Button>(R.id.btnJoin).setOnClickListener {
            val room = etRoom.text.toString().trim()
            if (room.length < 3) { etRoom.error = "Kam se kam 3 digit"; return@setOnClickListener }
            tvStatus.text = "Connect ho raha hai…"
            client.connect(Prefs.server(this), room, Prefs.name(this), my)
        }
        btnMute.setOnClickListener {
            muted = !muted
            btnMute.text = if (muted) "🔇 Unmute" else "🎤 Mute"
            listener.setPaused(muted || speaker.isSpeaking)
        }
        findViewById<Button>(R.id.btnEnd).setOnClickListener { finish() }
    }

    // ---- CallClient.Events ----
    override fun onJoined(peers: List<String>) {
        inCall = true
        findViewById<View>(R.id.joinBox).visibility = View.GONE
        btnMute.isEnabled = true
        tvStatus.text = if (peers.isEmpty()) "Room me aa gaye – dusre vyakti ka intezaar…"
        else "✅ Connected: ${peers.joinToString()}"
        listener.start(my, continuous = true)
    }

    override fun onPeerJoined(name: String, lang: Lang) {
        tvStatus.text = "✅ $name juda (${lang.name}) – ab boliye!"
        transcript.note("$name call me aaye – woh ${lang.name} bolenge, aap ${my.name} me sunenge")
    }

    override fun onPeerLeft(name: String) {
        tvStatus.text = "$name ne call chhod di"
        transcript.note("$name chale gaye")
    }

    override fun onMessage(name: String, text: String, lang: Lang) {
        val color = ContextCompat.getColor(this, R.color.other)
        lifecycleScope.launch {
            try {
                val out = engine.translate(text, lang, my)
                transcript.add(name, color, text, out)
                speaker.speak(out, my)
            } catch (e: Exception) {
                transcript.add(name, color, text)
                transcript.note("Translation error: ${e.message}")
            }
        }
    }

    override fun onDisconnected(reason: String) {
        inCall = false
        listener.stop()
        tvStatus.text = reason
        findViewById<View>(R.id.joinBox).visibility = View.VISIBLE
        btnMute.isEnabled = false
    }

    // ---- Listener.Callback ----
    override fun onPartial(text: String) { tvLive.text = "🗣 $text" }

    override fun onFinal(text: String) {
        tvLive.text = ""
        if (!inCall) return
        transcript.add("Aap", ContextCompat.getColor(this, R.color.me), text)
        client.send(text)
    }

    override fun onListening(active: Boolean) {
        if (inCall && !muted) tvLive.hint = if (active) "👂 sun raha hoon…" else ""
    }

    override fun onListenError(msg: String) { transcript.note(msg) }

    override fun onDestroy() {
        client.close()
        listener.destroy()
        speaker.shutdown()
        engine.close()
        super.onDestroy()
    }
}
