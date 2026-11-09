package com.enssat.kanaquiz.mgaillar_adely

import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import com.enssat.kanaquiz.mgaillar_adely.business.stats.GradeCalculator
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import org.junit.Assert.*
import org.junit.Test

class GradeCalculatorTest {
    private fun result(index: Int, grade: Int = 1, score: Int = 8, help: Boolean = false,
        type: CharacterType = CharacterType.KANJI) = QuizResult(
        index.toString(), index.toLong(), type,
        if (type == CharacterType.KANJI) QuestionType.CHARACTER_TO_MEANING else QuestionType.CHARACTER_TO_ROMAJI,
        grade, help, score
    )
    private fun grade(results: List<QuizResult>) = GradeCalculator.calculate(results, CharacterType.KANJI)

    @Test fun emptyHistoryStartsAtZero() {
        assertEquals(0.0, grade(emptyList()).grade, 0.0001)
        assertEquals(1, grade(emptyList()).nextGrade)
    }

    @Test fun threePassingQuizzesValidateALevel() {
        assertEquals(0, grade((1..2).map { result(it) }).validatedGrade)
        assertEquals(1, grade((1..3).map { result(it) }).validatedGrade)
        assertEquals(1.0, grade((1..3).map { result(it) }).grade, 0.0001)
    }

    @Test fun belowThresholdDoesNotValidateALevel() {
        assertEquals(0, grade(listOf(result(1, score = 4))).validatedGrade)
        assertEquals(1.0 / 6, grade(listOf(result(1, score = 4))).grade, 0.0001)
        assertEquals(0, grade((1..3).map { result(it, score = 7) }).validatedGrade)
        assertEquals(7.0 / 8, grade((1..3).map { result(it, score = 7) }).grade, 0.0001)
    }

    @Test fun decimalPartGrowsWithEachQuizTowardsNextLevel() {
        assertEquals(1.0 / 3, grade(listOf(result(1))).grade, 0.0001)
        assertEquals(2.0 / 3, grade((1..2).map { result(it) }).grade, 0.0001)
        val validatedThenNext = (1..3).map { result(it) } + result(4, grade = 2, score = 8)
        assertEquals(1 + 1.0 / 3, grade(validatedThenNext).grade, 0.0001)
    }

    @Test fun validationUsesTheAverageRatherThanIndividualScores() {
        assertEquals(1, grade(listOf(result(1, score = 6), result(2, score = 8), result(3, score = 10))).validatedGrade)
    }

    @Test fun helpWeighsScoreAtEightyPercent() {
        val withHelp = grade((1..3).map { result(it, score = 8, help = true) })
        assertEquals(0, withHelp.validatedGrade)
        assertEquals(0.8, withHelp.grade, 0.0001)
        assertEquals(1, grade((1..3).map { result(it, score = 10, help = true) }).validatedGrade)
    }

    @Test fun otherCharacterTypesDoNotCountOrEvictResults() {
        val kanji = (1..3).map { result(it) }
        val kana = (4..30).map { result(it, type = CharacterType.HIRAGANA) }
        assertEquals(1.0, grade(kanji + kana).grade, 0.0001)
        assertEquals(0.0, GradeCalculator.calculate(kanji, CharacterType.KATAKANA).grade, 0.0001)
        assertEquals(3, GradeCalculator.history(kanji + kana, CharacterType.KANJI).size)
    }

    @Test fun onlyTwentyMostRecentQuizzesCount() {
        val history = (1..3).map { result(it, grade = 6) } + (4..23).map { result(it) }
        assertEquals(1, grade(history).validatedGrade)
        assertEquals(grade(history).grade, grade(history.reversed()).grade, 0.0001)
    }

    @Test fun nextLevelAfterSixIsEight() {
        val history = (1..3).map { result(it, grade = 6) } + result(4, grade = 8)
        assertEquals(6 + 2.0 / 3, grade(history).grade, 0.0001)
        assertEquals(8, grade(history).nextGrade)
    }

    @Test fun highestLevelCanBeValidatedDirectlyAndIsCapped() {
        val progress = grade((1..4).map { result(it, grade = 8, score = 10) })
        assertEquals(8.0, progress.grade, 0.0001)
        assertNull(progress.nextGrade)
    }

    @Test fun historyReplaysEachPrefixInChronologicalOrder() {
        val results = (1..3).map { result(it) }
        val history = GradeCalculator.history(results.reversed(), CharacterType.KANJI)
        assertEquals(listOf("1", "2", "3"), history.map { it.resultId })
        assertEquals(1.0 / 3, history.first().grade, 0.0001)
        assertEquals(1.0, history.last().grade, 0.0001)
    }

    @Test fun kanaProgressStopsAtOne() {
        val results = (1..10).map { result(it, score = 10, type = CharacterType.KATAKANA) }
        val progress = GradeCalculator.calculate(results, CharacterType.KATAKANA)
        assertEquals(1.0, progress.grade, 0.0001)
        assertNull(progress.nextGrade)
    }
}
