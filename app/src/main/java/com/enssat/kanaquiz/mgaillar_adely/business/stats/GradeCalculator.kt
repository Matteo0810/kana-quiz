package com.enssat.kanaquiz.mgaillar_adely.business.stats

import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult

data class GradePoint(val resultId: String, val timestamp: Long, val grade: Double)
data class GradeProgress(val grade: Double, val validatedGrade: Int, val nextGrade: Int?)

object GradeCalculator {
    fun calculate(results: List<QuizResult>, type: CharacterType): GradeProgress {
        val levels = if (type == CharacterType.KANJI) KANJI_GRADES else listOf(1)
        val window = ordered(results.filter { it.characterType == type }).takeLast(20)
        val validated = levels.lastOrNull { level ->
            val quizzes = window.filter { it.quizGrade == level }
            quizzes.size >= 3 && quizzes.map { it.score }.average() >= 8.0
        } ?: 0
        val next = levels.firstOrNull { it > validated }
        return GradeProgress(validated.toDouble(), validated, next)
    }

    fun history(results: List<QuizResult>, type: CharacterType): List<GradePoint> {
        val sorted = ordered(results.filter { it.characterType == type })
        return sorted.mapIndexed { index, result ->
            GradePoint(result.id, result.timestamp, calculate(sorted.subList((index - 19).coerceAtLeast(0), index + 1), type).grade)
        }
    }

    private fun ordered(results: List<QuizResult>) = results.sortedWith(
        compareBy<QuizResult> { it.timestamp }.thenBy { it.id }
    )
}
