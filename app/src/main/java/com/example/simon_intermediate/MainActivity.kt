package com.example.simon_intermediate

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.simon_intermediate.data.AppDatabase
import com.example.simon_intermediate.data.Match
import com.example.simon_intermediate.ui.theme.SimonIntermediateTheme

// Tag per il logger di debug di MainActivity
const val tagMainActivity = "MainActivity"
const val MATCHEXTRA = "MATCH_ID"

class MainActivity : ComponentActivity() {

    // Inizializzo il ViewModel utilizzando la factory
    private val mainViewModel: MainViewModel by viewModels {
        val dao = AppDatabase.getDatabaseDao(this.applicationContext)
        MainViewModelFactory(this.application, dao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tagMainActivity, "onCreate Activity 1")

        enableEdgeToEdge()

        setContent {
            SimonIntermediateTheme {

                // https://developer.android.com/develop/ui/compose/state?hl=it
                // https://developer.android.com/reference/kotlin/androidx/compose/runtime/collectAsState.composable#(kotlinx.coroutines.flow.StateFlow).collectAsState(kotlin.coroutines.CoroutineContext)˙

                // Osservo il dato esposto dal ViewModel sotto forma di State
                // Quando la mia lista nel DB cambia, historyList si aggiornerà
                // in automatico scatenando una ricomposizione della HomeScreen
                val historyList by mainViewModel.historyData.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HomeScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        historyList = historyList,
                        goToGameScreen = {
                            val myIntent = Intent(this, GameActivity::class.java)
                            Log.d(tagMainActivity, "startActivity of GameActivity")
                            startActivity(myIntent)
                        },
                        goToDetailScreen = { matchID ->
                            val myIntent = Intent(this, MatchDetail::class.java)
                            // Reference: https://developer.android.com/reference/android/content/Intent#putExtra(java.lang.String,%20android.os.Parcelable)
                            myIntent.putExtra(
                                MATCHEXTRA,
                                matchID
                            )
                            Log.d(tagMainActivity, "startActivity of MatchDetail")
                            startActivity(myIntent)
                        },


                        // DA RIMUOVERE ALLA FINE DEL PROGETTO (SOLO DEV)!!!!!!
                        /*deleteAll = {
                            // Chiamo la funzione del ViewModel per pulire il database
                            mainViewModel.deleteAllMatches()
                        }*/
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
    goToGameScreen: () -> Unit,
    goToDetailScreen: (Int) -> Unit,
    // deleteAll: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Titolo della schermata
        Text(
            text = stringResource(R.string.history_title_text),
            style = MaterialTheme.typography.headlineMedium,
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
                GameCard(historyList[i], goToDetailScreen)
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
            // PULSANTE DA TOGLIERE PRIMA DI CONDIVIDERE IL PROGETTO COME FINITO!!!!!
            /*Button(
                onClick = deleteAll,
                colors = ButtonDefaults.filledTonalButtonColors(MaterialTheme.colorScheme.primary),
            ) {
                Text(
                    "Delete ALL",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }*/
        }
    }
}

// Card della singola partita
@Composable
fun GameCard(gameInfo: Match, detailFun: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .height(25.dp)
            .clickable {
                detailFun(gameInfo.id)
            },
        horizontalArrangement = Arrangement.Start
    ) {
        // Conteggio del punteggio (lunghezza massima completata correttamente)
        Text(
            text = gameInfo.maxLengthCompleted.toString(),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )

        Spacer(modifier = Modifier.padding(horizontal = 8.dp))

        // Sequenza dei pulsanti cliccati
        Text(
            modifier = Modifier.weight(1f),
            text = formatStringColored(gameInfo.finalSequence, gameInfo.errorIndex),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1, // Forza il testo su una sola riga
            overflow = TextOverflow.Ellipsis, // Aggiunge i "..." se il testo non ci sta
        )
    }
}

@Composable
fun formatStringColored(sequenceString: String, errIndex: Int): AnnotatedString {
    val fSequence = sequenceString.split(", ")

    // prendo la parte di sequenza corretta (dall'inizio fino all'indice dell'errore escluso)
    val parteCorretta = fSequence
        .subList(0, errIndex)
        .joinToString(", ")

    // prendo la parte di sequenza sbagliata (dall'indice dell'errore fino alla fine)
    val parteErrata = fSequence
        .subList(errIndex, fSequence.size)
        .joinToString(", ")

    val sequenzeColorata = buildAnnotatedString {
        // inserisco la parte corretta con il colore di default
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
            append(parteCorretta)
        }

        // congiungo le due metà solo nel caso in cui nessuna delle due sia vuota
        if (parteErrata.count() != 0 && parteCorretta.count() != 0)
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append(", ")
            }

        // aggiungo la parte dall'errore in poi con un SpanStyle di colore diverso
        withStyle(style = SpanStyle(color = Color.Red)) { append(parteErrata) }
    }

    return sequenzeColorata
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SimonIntermediateTheme {
        HomeScreen(
            historyList = arrayListOf(
                Match(finalSequence = "Y, G", errorIndex = 1, maxLengthCompleted = 1),
                Match(finalSequence = "G, G, G, R", errorIndex = 1, maxLengthCompleted = 3),
                Match(
                    finalSequence = "M, Y, G, B, R, R, R, Y",
                    errorIndex = 1,
                    maxLengthCompleted = 7
                )
            ),
            goToGameScreen = {},
            goToDetailScreen = {},
            // deleteAll = {}
        )
    }
}
