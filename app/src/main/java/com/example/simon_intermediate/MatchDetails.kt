package com.example.simon_intermediate

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.simon_intermediate.data.Match
import com.example.simon_intermediate.data.AppDatabase
import com.example.simon_intermediate.ui.theme.SimonIntermediateTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

// Tag per il logger di debug di MatchDetail
const val tagMatchDetail = "MatchDetail"

class MatchDetail : ComponentActivity() {
    private var matchInfo by mutableStateOf(Match(-1, "not available", 0, -1))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tagMatchDetail, "onCreate Activity 3")

        enableEdgeToEdge()

        // Imposto il valore di default così che posso gestire il caso in cui non venga passato nulla, anche se è una situazione che al 100% NON succederà
        val matchID = intent.getIntExtra(MATCHEXTRA, -1)
        Log.d(tagMatchDetail, "Match ID: $matchID")

        val dao = AppDatabase.getDatabaseDao(this)

        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) { dao.getMatchInfo(matchID) }
            withContext(Dispatchers.Main) {
                matchInfo = data
                Log.d(tagMatchDetail, "Match data retrieved from DB: $matchInfo")
            }
        }

        setContent {
            SimonIntermediateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DetailScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        matchInformations = matchInfo
                    )
                }
            }
        }
    }
}

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    matchInformations: Match
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Titolo della schermata
        Text(
            text = stringResource(R.string.match_info),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Info della singola partita
        GameInfo(matchInformations)
    }
}

@Composable
fun GameInfo(matchInformations: Match) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Ottengo la sequenza "formattata", ovvero colorata
        val sequenzeColorata = formatStringColored(matchInformations.finalSequence, matchInformations.errorIndex)

        InfoRow(
            label = stringResource(R.string.final_sequence),
            value = sequenzeColorata
        )
        InfoRow(
            label = stringResource(R.string.error_index),
            value = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(matchInformations.errorIndex.toString())
                }
            }
        )
        InfoRow(
            label = stringResource(R.string.max_length),
            value = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(matchInformations.maxLengthCompleted.toString())
                }
            }
        )
    }
}

// Composable per rendere i testi più leggibili
@Composable
fun InfoRow(label: String, value: AnnotatedString) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "$label:",
            // https://developer.android.com/develop/ui/compose/designsystems/material3#typography
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    SimonIntermediateTheme {
        val s = "Y, G, B, B, M, M, R"
        val m = Match(-1, s, 0, s.split(", ").count() - 1)

        val s2 = "not available"
        val m2 = Match(-1, s2, 0, s2.split(", ").count() - 1)

        DetailScreen(
            matchInformations = m
        )
    }
}
