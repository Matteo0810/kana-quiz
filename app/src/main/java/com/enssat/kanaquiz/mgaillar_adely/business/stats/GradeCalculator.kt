package com.enssat.kanaquiz.mgaillar_adely.business.stats

import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import kotlin.math.min

private const val WINDOW_SIZE = 20
private const val MIN_QUIZZES = 3
private const val PASSING_AVERAGE = 8.0
private const val HELP_WEIGHT = 0.8
private const val EPSILON = 1e-9

data class GradePoint(val resultId: String, val timestamp: Long, val grade: Double)
data class GradeProgress(val grade: Double, val validatedGrade: Int, val nextGrade: Int?)

object GradeCalculator {
    fun calculate(results: List<QuizResult>, type: CharacterType): GradeProgress {
        val levels = if (type == CharacterType.KANJI) KANJI_GRADES else listOf(1)
        val window = ordered(results.filter { it.characterType == type }).takeLast(WINDOW_SIZE)
        val quizzesByLevel = window.groupBy { it.quizGrade }

        val validated = levels.lastOrNull { level ->
            val quizzes = quizzesByLevel[level].orEmpty()
            quizzes.size >= MIN_QUIZZES && average(quizzes) >= PASSING_AVERAGE - EPSILON
        } ?: 0
        val next = levels.firstOrNull { it > validated }

        val progress = next?.let { (it - validated) * fractionTowards(quizzesByLevel[it].orEmpty()) } ?: 0.0
        return GradeProgress(validated + progress, validated, next)
    }

    fun history(results: List<QuizResult>, type: CharacterType): List<GradePoint> {
        val sorted = ordered(results.filter { it.characterType == type })
        return sorted.mapIndexed { index, result ->
            val window = sorted.subList((index - WINDOW_SIZE + 1).coerceAtLeast(0), index + 1)
            GradePoint(result.id, result.timestamp, calculate(window, type).grade)
        }
    }

    private fun fractionTowards(quizzes: List<QuizResult>): Double {
        if (quizzes.isEmpty()) return 0.0
        return min(quizzes.size.toDouble() / MIN_QUIZZES, 1.0) * min(average(quizzes) / PASSING_AVERAGE, 1.0)
    }

    private fun average(quizzes: List<QuizResult>): Double =
        quizzes.map { if (it.withHelp) it.score * HELP_WEIGHT else it.score.toDouble() }.average()

    private fun ordered(results: List<QuizResult>) = results.sortedWith(
        compareBy<QuizResult> { it.timestamp }.thenBy { it.id }
    )
}
