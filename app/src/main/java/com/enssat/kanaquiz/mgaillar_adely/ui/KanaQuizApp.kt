package com.enssat.kanaquiz.mgaillar_adely.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.enssat.kanaquiz.mgaillar_adely.ui.home.HomeScreen
import com.enssat.kanaquiz.mgaillar_adely.ui.learning.LearningScreen
import com.enssat.kanaquiz.mgaillar_adely.ui.progress.ProgressScreen
import com.enssat.kanaquiz.mgaillar_adely.ui.quiz.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KanaQuizApp(model: QuizViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    val results by model.results.collectAsStateWithLifecycle()
    BackHandler(enabled = state.page != Page.HOME) { model.back() }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("KanaQuiz") }, navigationIcon = {
                if (state.page != Page.HOME) {
                    IconButton(onClick = model::back, enabled = !state.loading && !state.saving) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            })
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.historyError != null && state.page in listOf(Page.HOME, Page.PROGRESS)) {
                Text(state.historyError!!, color = MaterialTheme.colorScheme.error)
                TextButton(model::refreshHistory) { Text("Réessayer") }
            }
            Box(Modifier.weight(1f)) {
                when (state.page) {
                    Page.HOME -> HomeScreen(results, { model.navigate(Page.SETUP) }, { model.navigate(Page.LEARNING) }, model::showProgress)
                    Page.LEARNING -> LearningScreen()
                    Page.SETUP -> QuizSetupScreen(state, model::setParams, model::start)
                    Page.QUIZ -> QuizScreen(state, model::answer, model::next, model::replaySpeech)
                    Page.RESULT -> ResultScreen(state, results, model::start, { model.navigate(Page.HOME) }, { model.showProgress(state.params.characterType) })
                    Page.PROGRESS -> ProgressScreen(results, state.progressType)
                }
            }
        }
    }
    if (state.confirmExit) {
        AlertDialog(onDismissRequest = model::dismissExit,
            title = { Text("Quitter ce quiz ?") },
            text = { Text("Ce quiz ne sera pas enregistré dans ta progression.") },
            confirmButton = { TextButton(model::abandon) { Text("Quitter le quiz") } },
            dismissButton = { TextButton(model::dismissExit) { Text("Continuer") } })
    }
}
