package com.livetranslator.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var spMy: Spinner
    private lateinit var spOther: Spinner
    private lateinit var etName: EditText
    private lateinit var etServer: EditText
    private lateinit var etSarvam: EditText

    private var afterPermission: (() -> Unit)? = null
    private val micPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
            if (ok) afterPermission?.invoke()
            else toast("Translator ke liye microphone permission zaroori hai")
            afterPermission = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarPadding()

        spMy = findViewById(R.id.spMy)
        spOther = findViewById(R.id.spOther)
        etName = findViewById(R.id.etName)
        etServer = findViewById(R.id.etServer)
        etSarvam = findViewById(R.id.etSarvam)

        val labels = Languages.ALL.map { it.label }
        spMy.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        spOther.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        spMy.setSelection(Languages.ALL.indexOf(Prefs.myLang(this)))
        spOther.setSelection(Languages.ALL.indexOf(Prefs.otherLang(this)))
        etName.setText(Prefs.rawName(this))
        etServer.setText(Prefs.server(this))
        etSarvam.setText(Prefs.sarvamKey(this))

        findViewById<Button>(R.id.btnInterpreter).setOnClickListener {
            save()
            withMic { startActivity(Intent(this, InterpreterActivity::class.java)) }
        }
        findViewById<Button>(R.id.btnCall).setOnClickListener {
            save()
            withMic { startActivity(Intent(this, CallActivity::class.java)) }
        }
        findViewById<Button>(R.id.btnDownload).setOnClickListener {
            save()
            downloadPacks()
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            toast("Is phone me speech recognition nahi hai. Google app install/update karein.")
        }
    }

    private fun selected(sp: Spinner) = Languages.ALL[sp.selectedItemPosition.coerceAtLeast(0)]

    private fun save() {
        Prefs.save(
            this, selected(spMy), selected(spOther),
            etName.text.toString(), etServer.text.toString(), etSarvam.text.toString()
        )
    }

    override fun onPause() {
        super.onPause()
        save()
    }

    private fun downloadPacks() {
        val my = selected(spMy)
        val other = selected(spOther)
        if (my.mlkit == null || other.mlkit == null) {
            toast("${my.name}/${other.name} ka offline pack nahi hai – Sarvam AI key se online chalega")
            return
        }
        toast("Download ho raha hai… (~30MB per bhasha)")
        val engine = TranslationEngine(this)
        lifecycleScope.launch {
            try {
                engine.prepare(my, other)
                toast("✅ ${my.name} ↔ ${other.name} offline ready!")
            } catch (e: Exception) {
                toast("Download fail: ${e.message}")
            } finally {
                engine.close()
            }
        }
    }

    private fun withMic(block: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        ) block()
        else {
            afterPermission = block
            micPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
