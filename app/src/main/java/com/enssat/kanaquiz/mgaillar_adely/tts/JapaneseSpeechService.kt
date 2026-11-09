package com.enssat.kanaquiz.mgaillar_adely.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

private const val SLOW_SPEECH_RATE = 0.5f
private const val NORMAL_SPEECH_RATE = 1.0f

class JapaneseSpeechService(context: Context) {

    private var tts: TextToSpeech? = null
    private var isReady = false

    private var isSlow = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.JAPANESE)

                isReady = result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED &&
                        result != null
            }
        }
    }


    fun speak(text: String, alternateSpeed: Boolean = true) {
        if (!isReady || text.isBlank()) return

        val speechRate = if (alternateSpeed && isSlow) SLOW_SPEECH_RATE else NORMAL_SPEECH_RATE

        tts?.setSpeechRate(speechRate)

        tts?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "kanaquiz_speech"
        )

        if (alternateSpeed) isSlow = !isSlow
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        isReady = false
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}