package com.enssat.kanaquiz.mgaillar_adely.data

import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*

data class QuizResult(
    val id: String,
    val timestamp: Long,
    val characterType: CharacterType,
    val questionType: QuestionType,
    val quizGrade: Int,
    val withHelp: Boolean,
    val score: Int,
    val isFake: Boolean = false,
) {
    init {
        require(score in 0..QUESTION_COUNT)
        require(questionType.supports(characterType))
        require(if (characterType == CharacterType.KANJI) quizGrade in KANJI_GRADES else quizGrade == 1)
    }
}
