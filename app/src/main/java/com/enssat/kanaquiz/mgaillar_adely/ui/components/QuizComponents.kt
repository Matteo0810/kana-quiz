package com.enssat.kanaquiz.mgaillar_adely.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.*
import java.util.Locale

fun CharacterType.label() = when (this) {
    CharacterType.HIRAGANA -> "Hiragana"
    CharacterType.KATAKANA -> "Katakana"
    CharacterType.KANJI -> "Kanji"
}

fun QuestionType.label() = when (this) {
    QuestionType.CHARACTER_TO_ROMAJI -> "Caractère → romaji"
    QuestionType.ROMAJI_TO_CHARACTER -> "Romaji → caractère"
    QuestionType.CHARACTER_TO_MEANING -> "Caractère → sens"
    QuestionType.MEANING_TO_CHARACTER -> "Sens → caractère"
    QuestionType.CHARACTER_TO_READING -> "Caractère → lecture"
    QuestionType.READING_TO_CHARACTER -> "Lecture → caractère"
}

fun Double.gradeLabel(): String = String.format(Locale.FRANCE, "%.2f", this)

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), content = content)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CharacterTypeSelector(selected: CharacterType, onSelect: (CharacterType) -> Unit) {
    FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        CharacterType.entries.forEach { type ->
            FilterChip(selected == type, { onSelect(type) }, label = { Text(type.label()) })
        }
    }
}

@Composable
fun SettingSwitch(title: String, description: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked, onChange, modifier = Modifier.semantics { contentDescription = title })
    }
}

@Composable
fun AnswerIndicators(session: QuizSession) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        session.quiz.questions.forEachIndexed { index, question ->
            val answer = session.answers.getOrNull(index)
            val correct = answer == question.correctIndex
            val label = if (answer == null) "En attente" else if (correct) "Correcte" else "Incorrecte"
            Surface(
                modifier = Modifier.weight(1f).height(32.dp).semantics { contentDescription = "Question ${index + 1} : $label" },
                shape = RoundedCornerShape(8.dp),
                color = when { answer == null -> MaterialTheme.colorScheme.surfaceContainerHighest
                    correct -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.errorContainer }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(if (answer == null) "${index + 1}" else if (correct) "✓" else "×")
                }
            }
        }
    }
}
