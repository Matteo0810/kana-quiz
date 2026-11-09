package com.enssat.kanaquiz.mgaillar_adely.business.quiz

const val QUESTION_COUNT = 10
const val CHOICE_COUNT = 4

val KANJI_GRADES = listOf(1, 2, 3, 4, 5, 6, 8)

enum class CharacterType { HIRAGANA, KATAKANA, KANJI }

enum class QuestionType(val supportsKana: Boolean, val supportsKanji: Boolean) {
    CHARACTER_TO_ROMAJI(supportsKana = true, supportsKanji = false),
    ROMAJI_TO_CHARACTER(supportsKana = true, supportsKanji = false),
    CHARACTER_TO_MEANING(supportsKana = false, supportsKanji = true),
    MEANING_TO_CHARACTER(supportsKana = false, supportsKanji = true),
    CHARACTER_TO_READING(supportsKana = false, supportsKanji = true),
    READING_TO_CHARACTER(supportsKana = false, supportsKanji = true);

    fun supports(type: CharacterType): Boolean =
        if (type == CharacterType.KANJI) supportsKanji else supportsKana
}

data class QuizParams(
    val characterType: CharacterType,
    val questionType: QuestionType,
    val requestedGrade: Int = 1,
    val helpEnabled: Boolean = false,
    val ttsEnabled: Boolean = false,
) {
    val grade: Int
        get() = if (characterType == CharacterType.KANJI) requestedGrade else 1
}

data class QuizQuestion(
    val prompt: String,
    val choices: List<String>,
    val correctIndex: Int,
    val help: String? = null,
    val speechBeforeAnswer: String? = null,
    val speechAfterAnswer: String? = null,
)

data class Quiz(
    val params: QuizParams,
    val questions: List<QuizQuestion>,
)
