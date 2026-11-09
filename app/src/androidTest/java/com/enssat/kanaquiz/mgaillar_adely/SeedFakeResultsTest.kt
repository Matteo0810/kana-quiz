package com.enssat.kanaquiz.mgaillar_adely

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.platform.app.InstrumentationRegistry
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.CharacterType
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.QuestionType
import com.enssat.kanaquiz.mgaillar_adely.business.stats.GradeCalculator
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import com.enssat.kanaquiz.mgaillar_adely.data.db.QuizDatabase
import com.enssat.kanaquiz.mgaillar_adely.ui.progress.ProgressChart
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SeedFakeResultsTest {
    @get:Rule val compose = createComposeRule()

    private val dao = QuizDatabase
        .getInstance(InstrumentationRegistry.getInstrumentation().targetContext)
        .quizResultDao()

    private fun fake(index: Int, grade: Int, score: Int, help: Boolean = false) = QuizResult(
        id = "fake-seed-$index",
        timestamp = 1_700_000_000_000L + index,
        characterType = CharacterType.KANJI,
        questionType = QuestionType.CHARACTER_TO_MEANING,
        quizGrade = grade,
        withHelp = help,
        score = score,
        isFake = true,
    )

    private val seed = listOf(
        fake(1, grade = 1, score = 9),
        fake(2, grade = 1, score = 9),
        fake(3, grade = 1, score = 9),
        fake(4, grade = 2, score = 8),
        fake(5, grade = 2, score = 10, help = true),
    )

    @Test fun seededResultsGiveExpectedGradeAndChart() {
        val fakes = runBlocking {
            dao.insertAll(seed)
            dao.getFake()
        }
        assertEquals(seed, fakes)

        val progress = GradeCalculator.calculate(fakes, CharacterType.KANJI)
        assertEquals(1, progress.validatedGrade)
        assertEquals(2, progress.nextGrade)
        assertEquals(1 + 2.0 / 3, progress.grade, 0.0001)

        val history = GradeCalculator.history(fakes, CharacterType.KANJI)
        compose.setContent { MaterialTheme { ProgressChart(history, 8) } }
        compose.onNodeWithTag("progress-chart").assertIsDisplayed()
        compose.onNodeWithContentDescription("Progression : 5 points, grade final 1,67").assertExists()
    }
}
