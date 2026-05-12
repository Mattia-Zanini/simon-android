package com.example.simon_intermediate

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
    private var matchInfo by mutableStateOf(Match(-1, "not available", -1, -1))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tagMatchDetail, "onCreate Activity 3")

        enableEdgeToEdge()

        // Imposto il valore di default così che posso gestire il caso in cui non venga passato nulla, anche se è una situazione che al 100% NON succederà
        val matchID = intent.getIntExtra(MATCHEXTRA, -1)

        val dao = AppDatabase.getDatabaseDao(this)

        lifecycleScope.launch {
            val data = withContext(Dispatchers.IO) {
                dao.getMatchInfo(matchID)
            }
            matchInfo = data
            Log.d(tagMatchDetail, "Recuperati i dati del match dal DB: $matchInfo")
        }

        setContent {
            SimonIntermediateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DetailScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        matchInformations = matchInfo,
                        goToHomeScreen = {
                            Log.d(tagMatchDetail, "going back to MainActivity")
                            finish() // ritorna all'activity precedente
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    matchInformations: Match,
    goToHomeScreen: () -> Unit
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

        Spacer(modifier = Modifier.weight(1f))

        // Bottone "Back"
        Button(
            onClick = goToHomeScreen,
            modifier = Modifier,
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
        ) {
            Text(stringResource(R.string.back))
        }
    }
}

@Composable
fun GameInfo(matchInformations: Match) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow(
            label = stringResource(R.string.final_sequence),
            value = matchInformations.finalSequence
        )
        InfoRow(
            label = stringResource(R.string.error_index),
            value = matchInformations.errorIndex.toString()
        )
        InfoRow(
            label = stringResource(R.string.max_length),
            value = matchInformations.maxLengthCompleted.toString()
        )
    }
}

// Composable per rendere i testi più leggibili
@Composable
fun InfoRow(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DetailScreenPreview() {
    SimonIntermediateTheme {
        DetailScreen(
            matchInformations = Match(
                -1,
                "Y, G, B, B, M, M, R, B, G, Y, C",
                5,
                10
            ),
            goToHomeScreen = { }
        )
    }
}