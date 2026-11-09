package com.enssat.kanaquiz.mgaillar_adely.ui.quiz

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import com.enssat.kanaquiz.mgaillar_adely.ui.components.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuizSetupScreen(state: QuizUiState, onParams: (QuizParams) -> Unit, onStart: () -> Unit) {
    val params = state.params
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("À toi de jouer", style = MaterialTheme.typography.headlineMedium)
        Text("10 questions pour progresser à ton rythme.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard {
            Text("Caractères", style = MaterialTheme.typography.titleMedium)
            CharacterTypeSelector(params.characterType) { onParams(params.copy(characterType = it)) }
            Text("Type de question", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuestionType.entries.filter { it.supports(params.characterType) }.forEach { type ->
                    FilterChip(params.questionType == type, { onParams(params.copy(questionType = type)) }, label = { Text(type.label()) })
                }
            }
            if (params.characterType == CharacterType.KANJI) {
                Text("Grade des kanji", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    KANJI_GRADES.forEach { grade ->
                        FilterChip(params.grade == grade, { onParams(params.copy(requestedGrade = grade)) }, label = { Text("$grade") })
                    }
                }
                Text("Inclut les caractères des grades inférieurs.", style = MaterialTheme.typography.bodySmall)
            }
        }
        SectionCard {
            SettingSwitch("Aide", "Un indice par question. Score de progression pondéré à 80 %.", params.helpEnabled) {
                onParams(params.copy(helpEnabled = it))
            }
            SettingSwitch("Voix japonaise", "Lecture quand elle ne révèle pas la réponse. Nécessite une voix japonaise installée.", params.ttsEnabled) {
                onParams(params.copy(ttsEnabled = it))
            }
        }
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(onStart, enabled = !state.loading, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
            if (state.loading) CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
            else Text("Commencer le quiz")
        }
    }
}
