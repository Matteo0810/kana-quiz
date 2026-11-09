package com.enssat.kanaquiz.mgaillar_adely

import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import org.junit.Assert.*
import org.junit.Test

class QuizSessionTest {
    private fun session() = QuizSession(Quiz(
        QuizParams(CharacterType.HIRAGANA, QuestionType.CHARACTER_TO_ROMAJI),
        List(QUESTION_COUNT) { QuizQuestion("あ", listOf("a", "i", "u", "e"), 0) }
    ))

    @Test fun doubleTapCannotChangeAnAnswerOrScore() {
        val answered = session().answer(0)
        assertEquals(answered, answered.answer(1))
        assertEquals(1, answered.score)
    }

    @Test fun cannotAdvanceWithoutAnswering() {
        val initial = session()
        assertEquals(initial, initial.advance())
        assertEquals(initial, initial.answer(-1))
        assertEquals(initial, initial.answer(4))
    }

    @Test fun incompleteQuizCannotProduceAResult() {
        assertThrows(IllegalStateException::class.java) { session().answer(0).result(1) }
    }

    @Test fun fullQuizCountsAnswersAndKeepsStableResultIdentity() {
        var current = session()
        repeat(QUESTION_COUNT) { index ->
            current = current.answer(if (index < 7) 0 else 1).advance()
        }
        assertTrue(current.complete)
        assertEquals(7, current.score)
        assertEquals(9, current.index)
        assertEquals(current, current.advance())
        val result = current.result(42)
        assertEquals(current.id, result.id)
        assertEquals(result.id, current.result(43).id)
        assertEquals(42L, result.timestamp)
        assertEquals(7, result.score)
        assertEquals(CharacterType.HIRAGANA, result.characterType)
        assertFalse(result.isFake)
    }

    @Test fun newSessionsHaveIndependentIdentities() {
        assertNotEquals(session().id, session().id)
    }
}
