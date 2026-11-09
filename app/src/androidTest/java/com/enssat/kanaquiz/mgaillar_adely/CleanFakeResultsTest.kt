package com.enssat.kanaquiz.mgaillar_adely

import androidx.test.platform.app.InstrumentationRegistry
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.CharacterType
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.QuestionType
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import com.enssat.kanaquiz.mgaillar_adely.data.db.QuizDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class CleanFakeResultsTest {
    private val dao = QuizDatabase
        .getInstance(InstrumentationRegistry.getInstrumentation().targetContext)
        .quizResultDao()

    private fun result(id: String, fake: Boolean) = QuizResult(
        id, System.currentTimeMillis(), CharacterType.HIRAGANA,
        QuestionType.CHARACTER_TO_ROMAJI, 1, false, 7, fake,
    )

    @Test fun removesFakeResultsAndKeepsRealOnes() = runBlocking {
        val realId = "clean-test-real-${UUID.randomUUID()}"
        dao.insertAll(listOf(result(realId, fake = false), result("clean-test-fake", fake = true)))
        try {
            dao.deleteFake()
            assertTrue(dao.getFake().isEmpty())
            assertTrue(dao.getAll().any { it.id == realId })
        } finally {
            dao.deleteById(realId)
        }
    }
}
