package com.enssat.kanaquiz.mgaillar_adely.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.enssat.kanaquiz.mgaillar_adely.business.quiz.CharacterType
import com.enssat.kanaquiz.mgaillar_adely.business.stats.*
import com.enssat.kanaquiz.mgaillar_adely.data.QuizResult
import com.enssat.kanaquiz.mgaillar_adely.ui.components.*
import java.text.DateFormat
import java.util.Date

@Composable
fun ProgressScreen(results: List<QuizResult>, initialType: CharacterType = CharacterType.HIRAGANA) {
    var type by rememberSaveable(initialType) { mutableStateOf(initialType) }
    val progress = remember(results, type) { GradeCalculator.calculate(results, type) }
    val history = remember(results, type) { GradeCalculator.history(results, type) }
    val recent = remember(results, type) { results.filter { it.characterType == type }.sortedByDescending { it.timestamp }.take(10) }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Text("Tes progrès", style = MaterialTheme.typography.headlineMedium)
        CharacterTypeSelector(type) { type = it }
        SectionCard {
            Text("Grade ${progress.grade.gradeLabel()}", style = MaterialTheme.typography.headlineLarge)
            Text("Meilleur grade : ${(history.maxOfOrNull { it.grade } ?: 0.0).gradeLabel()}")
            Text(progress.nextGrade?.let { "Prochain niveau : $it" } ?: "Tous les niveaux sont validés !")
            ProgressChart(history, if (type == CharacterType.KANJI) 8 else 1)
        }
        Text("Comment ça marche ?", style = MaterialTheme.typography.titleMedium)
        Text("Les 20 derniers quiz de ce type comptent. Un niveau est validé avec au moins 3 quiz à ce niveau et 80 % de moyenne. Un quiz avec aide compte pour 80 % de son score. Le grade est le plus haut niveau validé, et ses décimales montrent ton avancement vers le niveau suivant. Le grade peut baisser lorsque d’anciens quiz sortent de la fenêtre.", style = MaterialTheme.typography.bodySmall)
        if (recent.isNotEmpty()) {
            Text("Derniers quiz", style = MaterialTheme.typography.titleLarge)
            recent.forEach { result ->
                SectionCard {
                    Text("${result.score} / 10 · Grade ${result.quizGrade}", style = MaterialTheme.typography.titleMedium)
                    Text(result.questionType.label())
                    Text(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(result.timestamp)) + if (result.withHelp) " · Avec aide" else " · Sans aide", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun ProgressChart(points: List<GradePoint>, maximumGrade: Int, modifier: Modifier = Modifier) {
    if (points.isEmpty()) {
        Text("Ta courbe apparaîtra après ton premier quiz.", modifier = modifier.testTag("progress-empty"))
        return
    }
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val description = "Progression : ${points.size} points, grade final ${points.last().grade.gradeLabel()}"
    Column(modifier) {
        Text("Grade · 0 à $maximumGrade", style = MaterialTheme.typography.labelSmall)
        Canvas(Modifier.fillMaxWidth().height(180.dp).testTag("progress-chart").semantics { contentDescription = description }) {
            val inset = 8.dp.toPx()
            val width = (size.width - 2 * inset).coerceAtLeast(1f)
            val height = (size.height - 2 * inset).coerceAtLeast(1f)
            for (step in 0..4) {
                val y = inset + height * step / 4f
                drawLine(gridColor, Offset(inset, y), Offset(inset + width, y), 1.dp.toPx())
            }
            drawLine(gridColor, Offset(inset, inset), Offset(inset, inset + height), 1.dp.toPx())
            fun position(index: Int) = Offset(
                inset + if (points.size == 1) width / 2 else width * index / (points.size - 1),
                inset + height * (1f - (points[index].grade / maximumGrade).toFloat().coerceIn(0f, 1f))
            )
            val path = Path().apply {
                points.indices.forEach { index ->
                    val point = position(index)
                    if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
                }
            }
            drawPath(path, lineColor, style = Stroke(3.dp.toPx()))
            points.indices.forEach { drawCircle(lineColor, 3.dp.toPx(), position(it)) }
            drawCircle(lineColor, 6.dp.toPx(), position(points.lastIndex))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Quiz 1", style = MaterialTheme.typography.labelSmall)
            Text("Quiz ${points.size}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
