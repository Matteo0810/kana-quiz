package com.enssat.kanaquiz.mgaillar_adely

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import com.enssat.kanaquiz.mgaillar_adely.data.db.QuizDatabase
import com.enssat.kanaquiz.mgaillar_adely.ui.quiz.Page
import com.enssat.kanaquiz.mgaillar_adely.ui.quiz.QuizViewModel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class QuizFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private var sessionId: String? = null

    private fun model() = ViewModelProvider(compose.activity)[QuizViewModel::class.java]

    @After fun cleanup() {
        sessionId?.let { id -> runBlocking { dao().deleteById(id) } }
    }

    private fun dao() = QuizDatabase.getInstance(compose.activity).quizResultDao()

    private fun startQuiz() {
        compose.onNodeWithText("Lancer un quiz").performScrollTo().performClick()
        compose.onNodeWithText("Commencer le quiz").performScrollTo().performClick()
        compose.waitUntil(10_000) { model().state.value.page == Page.QUIZ }
        sessionId = model().state.value.session!!.id
    }

    @Test fun quizSurvivesRecreationAndSavesExactlyOneResult() {
        startQuiz()
        compose.onNodeWithTag("answer-0").performScrollTo().performClick()
        compose.activityRule.scenario.recreate()
        compose.onNodeWithTag("answer-0").assertIsNotEnabled()
        assertEquals(sessionId, model().state.value.session!!.id)
        compose.onNodeWithText("Question suivante").performScrollTo().performClick()
        repeat(9) { index ->
            compose.onNodeWithTag("answer-0").performScrollTo().performClick()
            compose.onNodeWithText(if (index == 8) "Voir mon résultat" else "Question suivante")
                .performScrollTo().performClick()
        }
        compose.waitUntil(10_000) { model().state.value.page == Page.RESULT }
        compose.onNodeWithText("Résultat enregistré").assertIsDisplayed()
        val saved = runBlocking { dao().getAll() }.filter { it.id == sessionId }
        assertEquals(1, saved.size)
        assertEquals(model().state.value.session!!.score, saved.single().score)
        compose.onNodeWithText("Voir ma progression").performScrollTo().performClick()
        compose.onNodeWithTag("progress-chart").performScrollTo().assertIsDisplayed()
    }

    @Test fun abandoningQuizDoesNotSaveAResult() {
        startQuiz()
        compose.onNodeWithTag("answer-0").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Retour").performClick()
        compose.onNodeWithText("Quitter le quiz").performClick()
        compose.onNodeWithText("Lancer un quiz").assertExists()
        assertFalse(runBlocking { dao().getAll() }.any { it.id == sessionId })
    }
}
