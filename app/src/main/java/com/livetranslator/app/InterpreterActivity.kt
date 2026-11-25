package com.livetranslator.app

import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

/**
 * Mode 1 – Interpreter: normal SIM call speaker par rakhein (ya aamne-saamne baat).
 * Neela button = main bol raha hoon, hara button = saamne wala bol raha hai.
 */
class InterpreterActivity : AppCompatActivity(), Listener.Callback {

    private lateinit var my: Lang
    private lateinit var other: Lang
    private lateinit var engine: TranslationEngine
    private lateinit var speaker: Speaker
    private lateinit var listener: Listener
    private lateinit var transcript: Transcript
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_interpreter)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        my = Prefs.myLang(this)
        other = Prefs.otherLang(this)
        title = "${my.name} ⇄ ${other.name}"

        engine = TranslationEngine(this)
        speaker = Speaker(this, { speaking ->
            if (!speaking) tvStatus.text = "Tayyar – button dabaa kar boliye"
            else tvStatus.text = "🔊 Translation bol raha hoon…"
        }, { err -> tvStatus.text = err; transcript.note(err) })
        listener = Listener(this, this)
        transcript = Transcript(findViewById(R.id.tvTranscript), findViewById<ScrollView>(R.id.scroll))
        tvStatus = findViewById(R.id.tvStatus)

        findViewById<Button>(R.id.btnMe).apply {
            text = "🎤 Main bolunga – ${my.native}"
            setOnClickListener { turn(my) }
        }
        findViewById<Button>(R.id.btnOther).apply {
            text = "🎤 Saamne wala – ${other.native}"
            setOnClickListener { turn(other) }
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            listener.stop()
            speaker.stop()
            tvStatus.text = "Ruk gaya"
        }

        lifecycleScope.launch {
            try {
                if (engine.prepare(my, other)) transcript.note("Offline language pack ready ✅")
            } catch (e: Exception) {
                transcript.note("Offline pack download nahi hua: ${e.message}")
            }
        }
    }

    private fun turn(lang: Lang) {
        speaker.stop()
        listener.start(lang, continuous = false)
        tvStatus.text = "👂 Sun raha hoon (${lang.name})…"
    }

    override fun onPartial(text: String) { tvStatus.text = "👂 $text" }

    override fun onFinal(text: String) {
        val from = listener.currentLang ?: return
        val to = if (from == my) other else my
        val color = ContextCompat.getColor(this, if (from == my) R.color.me else R.color.other)
        tvStatus.text = "🔄 Translate ho raha hai…"
        lifecycleScope.launch {
            try {
                val out = engine.translate(text, from, to)
                transcript.add(from.name, color, text, out)
                speaker.speak(out, to)
            } catch (e: Exception) {
                transcript.add(from.name, color, text)
                transcript.note("Translation error: ${e.message}")
                tvStatus.text = "Error"
            }
        }
    }

    override fun onListening(active: Boolean) {}

    override fun onListenError(msg: String) { tvStatus.text = msg }

    override fun onDestroy() {
        listener.destroy()
        speaker.shutdown()
        engine.close()
        super.onDestroy()
    }
}
