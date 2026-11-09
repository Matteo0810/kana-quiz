package com.enssat.kanaquiz.mgaillar_adely.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enssat.kanaquiz.mgaillar_adely.business.stats.GradeCalculator
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import com.enssat.kanaquiz.mgaillar_adely.ui.components.*

@Composable
fun ResultScreen(state: QuizUiState, results: List<QuizResult>, onReplay: () -> Unit, onHome: () -> Unit, onProgress: () -> Unit) {
    val session = state.session ?: return
    val grade = GradeCalculator.calculate(results, session.quiz.params.characterType)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quiz terminé !", style = MaterialTheme.typography.headlineLarge)
        SectionCard {
            Text("${session.score} / ${session.quiz.questions.size}", style = MaterialTheme.typography.displayLarge)
            Text(if (session.score >= 8) "Bien joué, continue comme ça." else "Chaque essai te fait avancer.")
            Text("${session.quiz.params.characterType.label()} · Grade ${grade.grade.gradeLabel()}", style = MaterialTheme.typography.titleLarge)
            if (state.saved) Text("Résultat enregistré", color = MaterialTheme.colorScheme.primary)
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onReplay, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text(if (state.loading) "Préparation…" else "Rejouer") }
        OutlinedButton(onProgress, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) { Text("Voir ma progression") }
        TextButton(onHome, enabled = !state.loading) { Text("Revenir à l’accueil") }
    }
}
