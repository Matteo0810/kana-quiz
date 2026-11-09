package com.enssat.kanaquiz.mgaillar_adely.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enssat.kanaquiz.mgaillar_adely.ui.components.*

@Composable
fun QuizScreen(state: QuizUiState, onAnswer: (Int) -> Unit, onNext: () -> Unit, onSpeak: () -> Unit) {
    val session = state.session ?: return
    val question = session.question
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Question ${session.index + 1} / ${session.quiz.questions.size}", style = MaterialTheme.typography.titleLarge)
        AnswerIndicators(session)
        SectionCard {
            Text(session.quiz.params.questionType.label(), color = MaterialTheme.colorScheme.primary)
            Text(question.prompt, modifier = Modifier.fillMaxWidth(), fontSize = if (question.prompt.length <= 3) 72.sp else 32.sp, textAlign = TextAlign.Center)
            question.help?.let { Text("Indice : $it", style = MaterialTheme.typography.bodyLarge) }
            val speech = if (session.answered) question.speechAfterAnswer else question.speechBeforeAnswer
            if (speech != null) TextButton(onSpeak) { Text("Écouter") }
        }
        question.choices.forEachIndexed { index, choice ->
            val selected = session.answers.getOrNull(session.index) == index
            val correct = session.answered && index == question.correctIndex
            val wrong = selected && !correct
            val color = when { correct -> MaterialTheme.colorScheme.primaryContainer
                wrong -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceContainer }
            OutlinedButton(
                onClick = { onAnswer(index) }, enabled = !session.answered,
                modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag("answer-$index"),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = color,
                    disabledContainerColor = color, disabledContentColor = MaterialTheme.colorScheme.onSurface)
            ) { Text(choice + if (correct) "  ✓" else if (wrong) "  ×" else "", style = MaterialTheme.typography.titleMedium) }
        }
        if (session.answered) {
            Text(if (session.answers[session.index] == question.correctIndex) "Bonne réponse !" else "Bonne réponse : ${question.choices[question.correctIndex]}", style = MaterialTheme.typography.titleMedium)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(onNext, enabled = !state.saving, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.saving) "Enregistrement…" else if (session.complete) "Voir mon résultat" else "Question suivante")
            }
        }
    }
}
