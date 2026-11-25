package com.livetranslator.app

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.widget.ScrollView
import android.widget.TextView

class Transcript(private val tv: TextView, private val scroll: ScrollView) {
    fun add(who: String, color: Int, original: String, translated: String? = null) {
        val sb = SpannableStringBuilder()
        val s = sb.length
        sb.append(who).append(": ")
        sb.setSpan(StyleSpan(Typeface.BOLD), s, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.setSpan(ForegroundColorSpan(color), s, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        sb.append(original).append('\n')
        if (translated != null) {
            val t = sb.length
            sb.append("   ➜ ").append(translated).append('\n')
            sb.setSpan(StyleSpan(Typeface.ITALIC), t, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        sb.append('\n')
        tv.append(sb)
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    fun note(text: String) {
        tv.append("ℹ $text\n\n")
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }
}
