package com.example.simon_intermediate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
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
import com.example.simon_intermediate.data.Match
import com.example.simon_intermediate.data.MatchDao
import com.example.simon_intermediate.data.AppDatabase
import com.example.simon_intermediate.ui.theme.SimonIntermediateTheme
import kotlin.collections.arrayListOf

// Tag per il logger di debug di MainActivity
const val tagHistoryD = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var historyData: List<Match>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tagHistoryD, "onCreate Activity 1")

        enableEdgeToEdge()

        // Ottengo direttamente il DAO tramite il Singleton
        val dao = AppDatabase.getDatabaseDao(this)

        // Recupero la lista delle partite
        historyData = dao.getAll()
        Log.d(tagHistoryD, "Numero di elementi nel DB: ${historyData.size}")

        setContent {
            SimonIntermediateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        historyList = historyData, // Qui passo lo storico delle partite al composable della Home Screen
                        goToGameScreen = {
                            val myIntent = Intent(this, GameActivity::class.java)
                            Log.d(tagMainD, "startActivity of GameActivity")
                            startActivity(myIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    historyList: List<Match>,
    goToGameScreen: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Titolo della schermata
        Text(
            text = stringResource(R.string.history_title_text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // LazyColumn dedicata per contenere tutte le cards delle partite fatte e poterle scrollare
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(historyList.size) { i ->
                GameCard(historyList[i])
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Button(
                onClick = goToGameScreen,
                colors = ButtonDefaults.filledTonalButtonColors(MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    stringResource(R.string.start_game),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// Card della singola partita
@Composable
fun GameCard(gameInfo: Match) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .height(25.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        val colorsSequence = gameInfo.finalSequence.split(", ")
        var numSequence = colorsSequence.count()

        // Controllo che la stringa sia vuota e correggo il valore di numSequence
        if (numSequence == 1 && colorsSequence[0].isEmpty())
            numSequence = 0

        // Conteggio dei pulsanti cliccati
        Text(
            text = numSequence.toString(),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.padding(horizontal = 8.dp))

        // Sequenza dei pulsanti cliccati
        Text(
            modifier = Modifier.weight(1f),
            text = gameInfo.finalSequence,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1, // Forza il testo su una sola riga
            overflow = TextOverflow.Ellipsis, // Aggiunge i "..." se il testo non ci sta
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SimonIntermediateTheme {
        HomeScreen(
            historyList = arrayListOf(
                Match(maxLength = 0, finalSequence = "Y, G", errorIndex = 1),
                Match(maxLength = 0, finalSequence = "G, G, G, R", errorIndex = 1),
                Match(maxLength = 0, finalSequence = "M, Y, G, B, R, R, R, Y", errorIndex = 1)
            ),
            goToGameScreen = {}
        )
    }
}