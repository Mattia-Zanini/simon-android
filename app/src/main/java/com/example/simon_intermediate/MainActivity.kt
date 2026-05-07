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
import androidx.room.Room
import com.example.simon_intermediate.ui.theme.SimonIntermediateTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.collections.arrayListOf

// Tag per il logger di debug di MainActivity
const val tagHistoryD = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var historyData: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tagHistoryD, "onCreate Activity 1")

        enableEdgeToEdge()

        // Recupero la lista (se è null, creo una lista vuota)
        historyData = arrayListOf<String>()

        // --- TEST ROOM ---
        // Ottengo direttamente il DAO tramite il Singleton
        val dao = AppDatabase.getDatabaseDao(this)

        // Utilizzo il DAO
        dao.insert(Match(maxLength = 0, finalSequence = "Y, G", errorIndex = 1))
        val tutti = dao.getAll()
        Log.d("RoomTest", "Elementi nel DB: ${tutti.size}")
        tutti.forEach { 
            Log.d("RoomTest", it.toString())
        }
        /*
        dao.deleteAll()
        Log.d("RoomTest", "Eliminati tutti i records")
        tutti = dao.getAll()
        Log.d("RoomTest", "Elementi nel DB: ${tutti.size}")
        */

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
// Dichiaro historyList come una List<String> in quanto deve SOLO leggere l'ArrayList<String> e NON modificarla
fun HomeScreen(modifier: Modifier = Modifier, historyList: List<String>, goToGameScreen: () -> Unit) {
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
            Button(onClick = goToGameScreen) { Text(stringResource(R.string.start_game)) }
        }
    }
}

// Card della singola partita
@Composable
fun GameCard(gameString: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .height(25.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        val colorsSequence = gameString.split(", ")
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
            text = gameString,
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
        HomeScreen(historyList = arrayListOf("R, M, Y, G", "R, R, R, Y, G", ""), goToGameScreen = {})
    }
}