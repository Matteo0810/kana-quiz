package com.enssat.kanaquiz.mgaillar_adely

import com.enssat.kanaquiz.mgaillar_adely.data.KanaRepository
import com.enssat.kanaquiz.mgaillar_adely.data.Kanji
import com.enssat.kanaquiz.mgaillar_adely.quiz.CHOICE_COUNT
import com.enssat.kanaquiz.mgaillar_adely.quiz.CharacterType
import com.enssat.kanaquiz.mgaillar_adely.quiz.KANJI_GRADES
import com.enssat.kanaquiz.mgaillar_adely.quiz.QUESTION_COUNT
import com.enssat.kanaquiz.mgaillar_adely.quiz.QuestionType
import com.enssat.kanaquiz.mgaillar_adely.quiz.QuizGenerator
import com.enssat.kanaquiz.mgaillar_adely.quiz.QuizParams
import com.enssat.kanaquiz.mgaillar_adely.quiz.cleanReading
import com.enssat.kanaquiz.mgaillar_adely.quiz.toOtherSyllabary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import kotlin.random.Random

class QuizGeneratorTest {
    private val repo = KanaRepository()
    private fun raw(name: String) = File("src/main/res/raw/$name").readText()
    private val kanas = repo.parseKana(raw("kana_data.json"))
    private val kanjis = repo.parseKanji(raw("kanji_jouyou.json"))
    private val kanjiByChar = kanjis.associateBy { it.char }
    private val generator = QuizGenerator(kanas, kanjis)

    private val allCombinations = CharacterType.entries.flatMap { type ->
        QuestionType.entries.filter { it.supports(type) }.map { type to it }
    }

    private fun Kanji.cleanReadings() = (readingsOn + readingsKun).map(::cleanReading)

    @Test
    fun everyCombinationHasTenWellFormedQuestions() {
        repeat(30) { seed ->
            allCombinations.forEach { (type, questionType) ->
                val quiz = generator.generate(QuizParams(type, questionType), Random(seed))
                assertEquals(QUESTION_COUNT, quiz.questions.size)
                assertEquals(QUESTION_COUNT, quiz.questions.map { it.prompt }.distinct().size)
                quiz.questions.forEach { q ->
                    assertEquals(CHOICE_COUNT, q.choices.distinct().size)
                    assertTrue(q.correctIndex in q.choices.indices)
                }
            }
        }
    }

    @Test
    fun readingQuestionsHaveOnlyOneValidAnswer() {
        repeat(50) { seed ->
            val quiz = generator.generate(
                QuizParams(CharacterType.KANJI, QuestionType.READING_TO_CHARACTER),
                Random(seed),
            )
            quiz.questions.forEach { q ->
                val valid = q.choices.count { kanjiByChar.getValue(it).cleanReadings().contains(q.prompt) }
                assertEquals(1, valid)
            }
        }
    }

    @Test
    fun meaningQuestionsHaveOnlyOneValidAnswer() {
        repeat(50) { seed ->
            val quiz = generator.generate(
                QuizParams(CharacterType.KANJI, QuestionType.CHARACTER_TO_MEANING),
                Random(seed),
            )
            quiz.questions.forEach { q ->
                val meanings = kanjiByChar.getValue(q.prompt).meanings.map { it.lowercase() }
                assertEquals(1, q.choices.count { it.lowercase() in meanings })
            }
        }
    }

    @Test
    fun duplicatedRomajiNeverAppearTwice() {
        repeat(200) { seed ->
            val quiz = generator.generate(
                QuizParams(CharacterType.HIRAGANA, QuestionType.ROMAJI_TO_CHARACTER),
                Random(seed),
            )
            val romajiOf = kanas.associate { it.char to it.romaji }
            quiz.questions.forEach { q ->
                assertEquals(CHOICE_COUNT, q.choices.map(romajiOf::getValue).distinct().size)
            }
        }
    }

    @Test
    fun kanjiRespectQuizGrade() {
        val kanjiQuestionTypes = QuestionType.entries.filter { it.supportsKanji }
        repeat(30) { seed ->
            KANJI_GRADES.forEach { grade ->
                kanjiQuestionTypes.forEach { questionType ->
                    val quiz = generator.generate(
                        QuizParams(CharacterType.KANJI, questionType, requestedGrade = grade),
                        Random(seed),
                    )
                    assertEquals(QUESTION_COUNT, quiz.questions.size)
                    quiz.questions.forEach { q ->
                        val asksCharacter = questionType == QuestionType.MEANING_TO_CHARACTER ||
                            questionType == QuestionType.READING_TO_CHARACTER
                        val character = if (asksCharacter) q.choices[q.correctIndex] else q.prompt
                        assertTrue(kanjiByChar.getValue(character).grade <= grade)
                        assertFalse(q.prompt.isBlank())
                    }
                }
            }
        }
    }

    @Test
    fun kanaQuizIsAlwaysGradeOneAndUsesRequestedSyllabary() {
        val params = QuizParams(CharacterType.KATAKANA, QuestionType.CHARACTER_TO_ROMAJI, requestedGrade = 5)
        assertEquals(1, params.grade)
        generator.generate(params, Random(1)).questions.forEach { q ->
            assertTrue(q.prompt.first() in 'ァ'..'ヶ')
        }
    }

    @Test
    fun helpOnlyWhenEnabled() {
        val questionType = QuestionType.CHARACTER_TO_READING
        generator.generate(QuizParams(CharacterType.KANJI, questionType), Random(1))
            .questions.forEach { assertNull(it.help) }
        generator.generate(QuizParams(CharacterType.KANJI, questionType, helpEnabled = true), Random(1))
            .questions.forEach { q ->
                assertEquals(kanjiByChar.getValue(q.prompt).meanings.first(), q.help)
            }
    }

    @Test
    fun speechDoesNotRevealThePronunciationAnswer() {
        val params = QuizParams(CharacterType.KANJI, QuestionType.CHARACTER_TO_READING, ttsEnabled = true)
        generator.generate(params, Random(1)).questions.forEach { q ->
            assertNull(q.speechBeforeAnswer)
            assertEquals(q.choices[q.correctIndex], q.speechAfterAnswer)
        }
    }

    @Test
    fun noSpeechWhenTtsDisabled() {
        generator.generate(QuizParams(CharacterType.KANJI, QuestionType.READING_TO_CHARACTER), Random(1))
            .questions.forEach {
                assertNull(it.speechBeforeAnswer)
                assertNull(it.speechAfterAnswer)
            }
    }

    @Test
    fun quizzesAreIndependent() {
        val params = QuizParams(CharacterType.KANJI, QuestionType.CHARACTER_TO_MEANING)
        assertNotEquals(generator.generate(params, Random(1)), generator.generate(params, Random(2)))
    }

    @Test
    fun rejectsIncompatibleQuestionType() {
        assertThrows(IllegalArgumentException::class.java) {
            generator.generate(QuizParams(CharacterType.HIRAGANA, QuestionType.CHARACTER_TO_MEANING))
        }
    }

    @Test
    fun cleansReadingsAndConvertsSyllabary() {
        assertEquals("ひとつ", cleanReading("ひと.つ"))
        assertEquals("ひと", cleanReading("ひと-"))
        assertEquals("カ", toOtherSyllabary("か"))
        assertEquals("きゃ", toOtherSyllabary("キャ"))
    }
}
