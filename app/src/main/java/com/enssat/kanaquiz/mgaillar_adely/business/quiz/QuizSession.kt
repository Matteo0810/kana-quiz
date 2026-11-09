package com.enssat.kanaquiz.mgaillar_adely.business.quiz

import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import java.util.UUID

data class QuizSession(
    val quiz: Quiz,
    val id: String = UUID.randomUUID().toString(),
    val index: Int = 0,
    val answers: List<Int> = emptyList(),
) {
    val question: QuizQuestion get() = quiz.questions[index]
    val answered: Boolean get() = answers.size > index
    val complete: Boolean get() = answers.size == quiz.questions.size
    val score: Int get() = answers.indices.count { answers[it] == quiz.questions[it].correctIndex }

    fun answer(choice: Int): QuizSession =
        if (answered || choice !in question.choices.indices) this else copy(answers = answers + choice)

    fun advance(): QuizSession = if (answered && !complete) copy(index = index + 1) else this

    fun result(timestamp: Long): QuizResult {
        check(complete)
        return QuizResult(id, timestamp, quiz.params.characterType, quiz.params.questionType,
            quiz.params.grade, quiz.params.helpEnabled, score)
    }
}
