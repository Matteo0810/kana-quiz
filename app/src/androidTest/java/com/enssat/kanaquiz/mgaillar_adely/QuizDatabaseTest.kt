package com.enssat.kanaquiz.mgaillar_adely

import androidx.test.platform.app.InstrumentationRegistry
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import com.enssat.kanaquiz.mgaillar_adely.data.db.QuizDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.UUID

class QuizDatabaseTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "quiz-test-${UUID.randomUUID()}.db"
    private lateinit var database: QuizDatabase

    @Before fun setup() { database = QuizDatabase(context, name) }
    @After fun cleanup() { database.close(); context.deleteDatabase(name) }

    private fun result(id: String, timestamp: Long, fake: Boolean = false) = QuizResult(
        id, timestamp, CharacterType.KANJI, QuestionType.CHARACTER_TO_READING, 6, true, 9, fake
    )

    @Test fun resultsSurviveReopeningAndRemainChronological() {
        val earlier = result("a", 1)
        val later = result("b", 2)
        database.insertAll(listOf(later, earlier))
        database.close()
        database = QuizDatabase(context, name)
        assertEquals(listOf(earlier, later), database.readAll())
    }

    @Test fun savingSameSessionTwiceDoesNotDuplicateIt() {
        val result = result("session", 1)
        database.insert(result)
        database.insert(result)
        assertEquals(listOf(result), database.readAll())
    }

    @Test fun cleanupDeletesOnlyFakeResults() {
        val real = result("real", 1)
        val fake = result("fake", 2, true)
        database.insertAll(listOf(real, fake))
        assertEquals(listOf(fake), database.readAll(fakeOnly = true))
        database.deleteFake()
        assertTrue(database.readAll(fakeOnly = true).isEmpty())
        assertEquals(listOf(real), database.readAll())
    }
}
