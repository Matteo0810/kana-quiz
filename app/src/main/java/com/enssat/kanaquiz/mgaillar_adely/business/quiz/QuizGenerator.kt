package com.enssat.kanaquiz.mgaillar_adely.business.quiz

import com.enssat.kanaquiz.mgaillar_adely.data.Kana
import com.enssat.kanaquiz.mgaillar_adely.data.Kanji
import kotlin.random.Random

private const val HIRAGANA_START = 'ぁ'
private const val HIRAGANA_END = 'ゖ'
private const val KATAKANA_START = 'ァ'
private const val KATAKANA_END = 'ヶ'
private const val KANA_OFFSET = KATAKANA_START - HIRAGANA_START

/** Un quiz de grade g porte sur les kanji de grade inférieur ou égal à g. */
internal fun isKanjiEligible(kanjiGrade: Int, quizGrade: Int): Boolean = kanjiGrade <= quizGrade

internal fun isHiragana(text: String): Boolean = text.first() in HIRAGANA_START..HIRAGANA_END

/** Convertit chaque caractère vers l'autre syllabaire (か <-> カ). */
internal fun toOtherSyllabary(text: String): String = text.map { c ->
    when (c) {
        in HIRAGANA_START..HIRAGANA_END -> c + KANA_OFFSET
        in KATAKANA_START..KATAKANA_END -> c - KANA_OFFSET
        else -> c
    }
}.joinToString("")

/** Nettoie les marqueurs du jeu de données : "ひと.つ" -> "ひとつ", "ひと-" -> "ひと". */
internal fun cleanReading(reading: String): String = reading
    .filterNot { it == '.' || it == '-' }
    .map { if (it in KATAKANA_START..KATAKANA_END) it - KANA_OFFSET else it }
    .joinToString("")

/** Une entrée interrogeable, indépendante du type de caractère. */
private data class Fact(
    val character: String,
    val value: String,
    val acceptedValues: Set<String>,
    val help: String,
    val reading: String,
)

private fun Kana.toFact() = Fact(
    character = char,
    value = romaji,
    acceptedValues = setOf(romaji),
    help = toOtherSyllabary(char),
    reading = char,
)

private fun Kanji.readings(): List<String> = (readingsOn + readingsKun).map(::cleanReading).distinct()

private fun Kanji.toFact(questionType: QuestionType): Fact {
    val readings = readings()
    val askMeaning = questionType == QuestionType.CHARACTER_TO_MEANING ||
        questionType == QuestionType.MEANING_TO_CHARACTER
    return Fact(
        character = char,
        value = if (askMeaning) meanings.first() else readings.first(),
        acceptedValues = if (askMeaning) meanings.map { it.lowercase() }.toSet() else readings.toSet(),
        help = if (askMeaning) readings.first() else meanings.first(),
        reading = readings.first(),
    )
}

class QuizGenerator(
    private val kanas: List<Kana>,
    private val kanjis: List<Kanji>,
) {

    fun generate(params: QuizParams, random: Random = Random.Default): Quiz {
        require(params.questionType.supports(params.characterType)) {
            "${params.questionType} n'est pas disponible pour ${params.characterType}"
        }
        val pool = buildPool(params)
        // distinctBy : deux kanji lus "こう" donneraient deux fois le même énoncé.
        val targets = pool.shuffled(random)
            .distinctBy { it.value }
            .take(QUESTION_COUNT)
        require(targets.size == QUESTION_COUNT) { "Pas assez de caractères pour $params" }

        val questions = targets.map { buildQuestion(it, pool, params, random) }
        return Quiz(params, questions)
    }

    private fun buildPool(params: QuizParams): List<Fact> = when (params.characterType) {
        CharacterType.HIRAGANA -> kanas.filter { isHiragana(it.char) }.map { it.toFact() }
        CharacterType.KATAKANA -> kanas.filterNot { isHiragana(it.char) }.map { it.toFact() }
        CharacterType.KANJI -> kanjis
            .filter { isKanjiEligible(it.grade, params.grade) }
            .map { it.toFact(params.questionType) }
    }

    private fun buildQuestion(
        target: Fact,
        pool: List<Fact>,
        params: QuizParams,
        random: Random,
    ): QuizQuestion {
        // Un distracteur ne doit jamais être, lui aussi, une bonne réponse.
        val distractors = pool.shuffled(random)
            .filter { it.character != target.character }
            .filter { it.acceptedValues.none(target.acceptedValues::contains) }
            .distinctBy { it.value }
            .take(CHOICE_COUNT - 1)
        check(distractors.size == CHOICE_COUNT - 1) { "Pas assez de distracteurs pour ${target.character}" }

        val asksCharacter = when (params.questionType) {
            QuestionType.ROMAJI_TO_CHARACTER,
            QuestionType.MEANING_TO_CHARACTER,
            QuestionType.READING_TO_CHARACTER -> true
            else -> false
        }
        val answerOf: (Fact) -> String = if (asksCharacter) Fact::character else Fact::value
        val correct = answerOf(target)
        val choices = (distractors.map(answerOf) + correct).shuffled(random)

        return QuizQuestion(
            prompt = if (asksCharacter) target.value else target.character,
            choices = choices,
            correctIndex = choices.indexOf(correct),
            help = target.help.takeIf { params.helpEnabled },
            speechBeforeAnswer = speechBefore(target, params),
            speechAfterAnswer = target.reading.takeIf { params.ttsEnabled },
        )
    }

    private fun speechBefore(target: Fact, params: QuizParams): String? {
        if (!params.ttsEnabled) return null
        return when (params.questionType) {
            // La question porte déjà sur le son : le dire ne révèle rien.
            QuestionType.ROMAJI_TO_CHARACTER,
            QuestionType.READING_TO_CHARACTER,
            QuestionType.CHARACTER_TO_MEANING -> target.reading
            // Ici, la voix donnerait la réponse ou le moyen de la déduire.
            QuestionType.CHARACTER_TO_ROMAJI,
            QuestionType.CHARACTER_TO_READING,
            QuestionType.MEANING_TO_CHARACTER -> null
        }
    }
}
