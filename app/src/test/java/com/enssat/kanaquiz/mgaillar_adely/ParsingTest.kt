package com.enssat.kanaquiz.mgaillar_adely

import com.enssat.kanaquiz.mgaillar_adely.data.KanaRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ParsingTest {
    private val repo = KanaRepository()
    private fun raw(name: String) = File("src/main/res/raw/$name").readText()

    @Test
    fun parseKana() {
        val kanas = repo.parseKana(raw("kana_data.json"))
        assertEquals(208, kanas.size)
        assertEquals("あ", kanas.first().char)
        assertTrue(kanas.any { it.char == "や" && it.romaji == "ya" })
    }

    @Test
    fun parseKanji() {
        val kanjis = repo.parseKanji(raw("kanji_jouyou.json"))
        assertEquals(2136, kanjis.size)
        val ichi = kanjis.first { it.char == "一" }
        assertEquals(1, ichi.grade)
        assertEquals("One", ichi.meanings.first())
        assertEquals(listOf("いち", "いつ"), ichi.readingsOn)
    }
}
