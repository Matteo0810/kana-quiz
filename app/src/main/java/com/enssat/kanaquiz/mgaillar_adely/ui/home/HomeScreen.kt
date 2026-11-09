package com.enssat.kanaquiz.mgaillar_adely.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.CharacterType
import com.enssat.kanaquiz.mgaillar_adely.business.stats.GradeCalculator
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import com.enssat.kanaquiz.mgaillar_adely.ui.components.*

@Composable
fun HomeScreen(results: List<QuizResult>, onQuiz: () -> Unit, onLearn: () -> Unit, onProgress: (CharacterType) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Un peu de japonais,\nchaque jour.", style = MaterialTheme.typography.headlineLarge)
        Text("Apprends les caractères et mets tes connaissances à l’épreuve.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        SectionCard {
            Text("Ta progression", style = MaterialTheme.typography.titleLarge)
            CharacterType.entries.forEach { type ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(type.label())
                    TextButton({ onProgress(type) }, contentPadding = PaddingValues(0.dp)) {
                        Text("Grade ${GradeCalculator.calculate(results, type).grade.gradeLabel()}")
                    }
                }
            }
            Text("${results.size} quiz terminé${if (results.size == 1) "" else "s"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton({ onProgress(CharacterType.HIRAGANA) }) { Text("Voir la progression →") }
        }
        Button(onQuiz, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Lancer un quiz") }
        OutlinedButton(onLearn, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Apprendre les caractères") }
    }
}
