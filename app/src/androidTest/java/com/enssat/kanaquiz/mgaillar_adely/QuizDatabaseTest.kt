package com.enssat.kanaquiz.mgaillar_adely

import androidx.test.platform.app.InstrumentationRegistry
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import com.enssat.kanaquiz.mgaillar_adely.data.db.QuizDatabase
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class QuizDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "quiz-test-${UUID.randomUUID()}.db"
    private lateinit var database: QuizDatabase
    private val dao get() = database.quizResultDao()

    @Before fun setup() { database = QuizDatabase.create(context, name) }
    @After fun cleanup() { database.close(); context.deleteDatabase(name) }

    private fun result(id: String, timestamp: Long, fake: Boolean = false) = QuizResult(
        id, timestamp, CharacterType.KANJI, QuestionType.CHARACTER_TO_READING, 6, true, 9, fake
    )

    @Test fun resultsSurviveReopeningAndRemainChronological() = runBlocking {
        val earlier = result("a", 1)
        val later = result("b", 2)
        dao.insertAll(listOf(later, earlier))
        database.close()
        database = QuizDatabase.create(context, name)
        assertEquals(listOf(earlier, later), dao.getAll())
    }

    @Test fun savingSameSessionTwiceDoesNotDuplicateIt() = runBlocking {
        val result = result("session", 1)
        dao.insertAll(listOf(result))
        dao.insertAll(listOf(result))
        assertEquals(listOf(result), dao.getAll())
    }

    @Test fun cleanupDeletesOnlyFakeResults() = runBlocking {
        val real = result("real", 1)
        val fake = result("fake", 2, true)
        dao.insertAll(listOf(real, fake))
        assertEquals(listOf(fake), dao.getFake())
        dao.deleteFake()
        assertTrue(dao.getFake().isEmpty())
        assertEquals(listOf(real), dao.getAll())
    }
}
