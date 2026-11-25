package com.livetranslator.app

import com.google.mlkit.nl.translate.TranslateLanguage

/**
 * @param locale  Android speech / TTS locale tag
 * @param mlkit   ML Kit on-device code (null = offline not available, Sarvam AI needed)
 * @param sarvam  Sarvam AI language code
 */
data class Lang(
    val name: String,
    val native: String,
    val locale: String,
    val mlkit: String?,
    val sarvam: String,
) {
    val label get() = "$name ($native)"
}

object Languages {
    val ALL = listOf(
        Lang("Hindi", "हिन्दी", "hi-IN", TranslateLanguage.HINDI, "hi-IN"),
        Lang("Marathi", "मराठी", "mr-IN", TranslateLanguage.MARATHI, "mr-IN"),
        Lang("Bengali", "বাংলা", "bn-IN", TranslateLanguage.BENGALI, "bn-IN"),
        Lang("Gujarati", "ગુજરાતી", "gu-IN", TranslateLanguage.GUJARATI, "gu-IN"),
        Lang("Tamil", "தமிழ்", "ta-IN", TranslateLanguage.TAMIL, "ta-IN"),
        Lang("Telugu", "తెలుగు", "te-IN", TranslateLanguage.TELUGU, "te-IN"),
        Lang("Kannada", "ಕನ್ನಡ", "kn-IN", TranslateLanguage.KANNADA, "kn-IN"),
        Lang("Malayalam", "മലയാളം", "ml-IN", null, "ml-IN"),
        Lang("Punjabi", "ਪੰਜਾਬੀ", "pa-IN", null, "pa-IN"),
        Lang("Odia", "ଓଡ଼ିଆ", "or-IN", null, "od-IN"),
        Lang("Urdu", "اردو", "ur-IN", TranslateLanguage.URDU, "ur-IN"),
        Lang("Assamese", "অসমীয়া", "as-IN", null, "as-IN"),
        Lang("English", "English", "en-IN", TranslateLanguage.ENGLISH, "en-IN"),
        Lang("Nepali", "नेपाली", "ne-NP", null, "ne-IN"),
        Lang("Konkani", "कोंकणी", "kok-IN", null, "kok-IN"),
        Lang("Maithili", "मैथिली", "mai-IN", null, "mai-IN"),
        Lang("Sanskrit", "संस्कृतम्", "sa-IN", null, "sa-IN"),
        Lang("Sindhi", "سنڌي", "sd-IN", null, "sd-IN"),
        Lang("Kashmiri", "کٲشُر", "ks-IN", null, "ks-IN"),
        Lang("Dogri", "डोगरी", "doi-IN", null, "doi-IN"),
        Lang("Manipuri", "মৈতৈলোন্", "mni-IN", null, "mni-IN"),
        Lang("Bodo", "बड़ो", "brx-IN", null, "brx-IN"),
        Lang("Santali", "ᱥᱟᱱᱛᱟᱲᱤ", "sat-IN", null, "sat-IN"),
    )

    fun byLocale(tag: String?): Lang = ALL.firstOrNull { it.locale == tag } ?: ALL[0]
}
