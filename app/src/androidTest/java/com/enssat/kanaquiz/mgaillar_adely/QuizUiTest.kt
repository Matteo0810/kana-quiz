package com.enssat.kanaquiz.mgaillar_adely

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import com.enssat.kanaquiz.mgaillar_adely.business.stats.GradePoint
import com.enssat.kanaquiz.mgaillar_adely.ui.progress.ProgressChart
import com.enssat.kanaquiz.mgaillar_adely.ui.quiz.*
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class QuizUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun emptyProgressExplainsHowToStart() {
        compose.setContent { MaterialTheme { ProgressChart(emptyList(), 8) } }
        compose.onNodeWithTag("progress-empty").assertIsDisplayed()
        compose.onNodeWithTag("progress-chart").assertDoesNotExist()
    }

    @Test fun chartExposesItsFinalGrade() {
        compose.setContent { MaterialTheme { ProgressChart(listOf(GradePoint("a", 1, 0.5)), 1) } }
        compose.onNodeWithTag("progress-chart").assertIsDisplayed()
        compose.onNodeWithContentDescription("Progression : 1 points, grade final 0,50").assertExists()
    }

    @Test fun answeredQuestionShowsCorrectionAndLocksChoices() {
        val quiz = Quiz(QuizParams(CharacterType.HIRAGANA, QuestionType.CHARACTER_TO_ROMAJI),
            List(10) { QuizQuestion("あ", listOf("a", "i", "u", "e"), 0) })
        var nextClicks = 0
        compose.setContent { MaterialTheme {
            QuizScreen(QuizUiState(session = QuizSession(quiz).answer(1)), {}, { nextClicks++ }, {})
        } }
        compose.onNodeWithText("i  ×").assertIsNotEnabled()
        compose.onNodeWithText("a  ✓").assertIsNotEnabled()
        compose.onNodeWithText("Bonne réponse : a").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Question suivante").performScrollTo().performClick()
        compose.runOnIdle { assertEquals(1, nextClicks) }
    }
}
