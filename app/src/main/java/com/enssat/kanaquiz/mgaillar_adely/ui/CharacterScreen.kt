package com.enssat.kanaquiz.mgaillar_adely.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.enssat.kanaquiz.mgaillar_adely.data.Kana
import com.enssat.kanaquiz.mgaillar_adely.data.KanaRepository
import com.enssat.kanaquiz.mgaillar_adely.data.Kanji
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CharacterScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var kanas by remember { mutableStateOf<List<Kana>>(emptyList()) }
    var kanjis by remember { mutableStateOf<List<Kanji>>(emptyList()) }

    // Chargement + parsing hors du thread UI
    LaunchedEffect(Unit) {
        val repo = KanaRepository()
        withContext(Dispatchers.IO) {
            kanas = repo.loadKanas(context)
            kanjis = repo.loadKanjis(context)
        }
    }

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var kanaIndex by rememberSaveable { mutableIntStateOf(0) }
    var kanjiIndex by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Kana") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Kanji") })
        }

        val size = if (tab == 0) kanas.size else kanjis.size
        if (size == 0) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (tab == 0) KanaCard(kanas[kanaIndex]) else KanjiCard(kanjis[kanjiIndex])
        }

        val index = if (tab == 0) kanaIndex else kanjiIndex
        fun setIndex(i: Int) {
            if (tab == 0) kanaIndex = i else kanjiIndex = i
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Modulo : on boucle du dernier au premier et inversement
            Button(onClick = { setIndex((index - 1 + size) % size) }) { Text("Précédent") }
            Text("${index + 1} / $size")
            Button(onClick = { setIndex((index + 1) % size) }) { Text("Suivant") }
        }
    }
}

@Composable
private fun KanaCard(kana: Kana) {
    Text(kana.char, fontSize = 120.sp)
    Spacer(Modifier.height(16.dp))
    Text(kana.romaji, style = MaterialTheme.typography.headlineMedium)
}

@Composable
private fun KanjiCard(kanji: Kanji) {
    Text(kanji.char, fontSize = 120.sp)
    Spacer(Modifier.height(16.dp))
    Text(
        kanji.meanings.joinToString(", "),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(16.dp))
    if (kanji.readingsOn.isNotEmpty()) {
        Text("On : ${kanji.readingsOn.joinToString("、")}", style = MaterialTheme.typography.bodyLarge)
    }
    if (kanji.readingsKun.isNotEmpty()) {
        Text("Kun : ${kanji.readingsKun.joinToString("、")}", style = MaterialTheme.typography.bodyLarge)
    }
    Spacer(Modifier.height(8.dp))
    Text("Grade ${kanji.grade}", style = MaterialTheme.typography.labelLarge)
}
