package com.enssat.kanaquiz.mgaillar_adely.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Kana(val char: String, val romaji: String)

@Serializable
data class KanjiJson(
    val grade: Int,
    val meanings: List<String>,
    @SerialName("readings_on") val readingsOn: List<String>,
    @SerialName("readings_kun") val readingsKun: List<String>,
)

data class Kanji(
    val char: String,
    val grade: Int,
    val meanings: List<String>,
    val readingsOn: List<String>,
    val readingsKun: List<String>,
)

typealias KanaFile = Map<String, Map<String, Map<String, List<Kana>>>>