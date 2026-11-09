package com.enssat.kanaquiz.mgaillar_adely.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*

@Entity(tableName = "quiz_results", indices = [Index("timestamp", "id")])
data class QuizResult(
    @PrimaryKey val id: String,
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
