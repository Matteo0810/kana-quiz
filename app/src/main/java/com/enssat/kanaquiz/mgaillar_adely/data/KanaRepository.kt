package com.enssat.kanaquiz.mgaillar_adely.data

import android.content.Context
import androidx.annotation.RawRes
import com.enssat.kanaquiz.mgaillar_adely.R
import kotlinx.serialization.json.Json

fun loadJsonFromRaw(context: Context, @RawRes rawResId: Int): String {
    return context.resources.openRawResource(rawResId).bufferedReader().use {
        it.readText()
    }
}

class KanaRepository {
    private val json = Json { ignoreUnknownKeys = true }

    fun parseKana(raw: String): List<Kana> =
        json.decodeFromString<KanaFile>(raw)
            .values
            .flatMap { it.values }
            .flatMap { it.values }
            .flatten()

    fun parseKanji(raw: String): List<Kanji> =
        json.decodeFromString<Map<String, KanjiJson>>(raw)
            .map { (char, k) -> Kanji(char, k.grade, k.meanings, k.readingsOn, k.readingsKun) }

    fun loadKanas(context: Context): List<Kana> =
        parseKana(loadJsonFromRaw(context, R.raw.kana_data))

    fun loadKanjis(context: Context): List<Kanji> =
        parseKanji(loadJsonFromRaw(context, R.raw.kanji_jouyou))
}
