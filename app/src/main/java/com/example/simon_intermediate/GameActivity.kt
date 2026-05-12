package com.example.simon_intermediate

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.simon_intermediate.ui.theme.SimonIntermediateTheme
import com.example.simon_intermediate.data.Match
import com.example.simon_intermediate.data.AppDatabase

// Tag per il logger di debug di GameActivity
const val tagGameActivity = "GameActivity"

data class SimonButton(
    val label: String,
    val color: Color,
    val colorShadowed: Color
)

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tagGameActivity, "onCreate Activity 2")

        enableEdgeToEdge()

        val dao = AppDatabase.getDatabaseDao(this)
        val simonButtons = listOf(
            SimonButton("R", Color.Red, Color(0xFFAA3333)),
            SimonButton("G", Color.Green, Color(0xFF338833)),
            SimonButton("B", Color.Blue, Color(0xFF3333AA)),
            SimonButton("M", Color.Magenta, Color(0xF9AA33AA)),
            SimonButton("Y", Color.Yellow, Color(0xFFAAAA33)),
            SimonButton("C", Color.Cyan, Color(0xFF33AAAA))
        )

        setContent {
            SimonIntermediateTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // MainScreen utilizza gli "insets" (presenti in 'innerPadding') per mantenere
                    // l'interfaccia utente lontana dalla UI di sistema e dai ritagli del display (come il notch)
                    GameScreen(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        simonButtons,
                        insertMatch = { matchToInsert: Match ->
                            //  esegue l'inserimento nel database e, solo al termine del salvataggio,
                            //  torna sul thread principale (Dispatchers.Main) per chiudere l'activity con finish().
                            lifecycleScope.launch(Dispatchers.IO) {
                                dao.insert(matchToInsert)
                                withContext(Dispatchers.Main) {
                                    Log.d(tagGameActivity, "going back to MainActivity")
                                    finish() // ritorna all'activity precedente
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    simonBtns: List<SimonButton>,
    insertMatch: (Match) -> Unit
) {
    // Recupero l'orientamento attuale del dispositivo
    val orientation = LocalConfiguration.current.orientation
    val isPortrait: Boolean = orientation == Configuration.ORIENTATION_PORTRAIT

    // Stato per memorizzare la sequenza di colori cliccati
    var txt by rememberSaveable { mutableStateOf("") }

    // false se il computer sta riproducendo la sequenza, true se tocca all'utente
    var isPlayerTurn by rememberSaveable { mutableStateOf(false) }

    // Contiene la label del colore che il computer sta mostrando (es. "R")
    var activeColorLabel by rememberSaveable { mutableStateOf<String?>(null) }

    // Rappresenta l'index della sequenza che il computer ha appena mostrato
    var computerIndex by rememberSaveable { mutableIntStateOf(-1) }

    // test dell'active color
    activeColorLabel = "R"
    computerIndex = 0

    // Assegno il numero di colonne e di righe
    val cols = 3
    val rows = 2

    // Altezza variabile per la TextBox a seconda dell'orientamento
    val textBoxHeight = if (isPortrait) 180.dp else 200.dp

    // Queste sono le lambda che vengono utilizzate in entrambe le situazioni (sia portrait che landscape)
    val onColorClick: (String) -> Unit = { color ->
        txt += if (txt.isEmpty()) color else ", $color"
        Log.d(tagGameActivity, "BTN '$color' clicked")
    }

    val onDeleteClick: () -> Unit = {
        txt = "" // ripulisco la text
        Log.d(tagGameActivity, "BTN 'Delete' clicked")
    }

    val onEndClick: () -> Unit = {
        Log.d(tagGameActivity, "BTN 'End Game' clicked")

        // Creazione dell'oggetto Match con la sequenza attuale
        val currentMatch = Match(
            finalSequence = txt,
            errorIndex = 0,
            maxLengthCompleted = txt.length - 1
        )

        txt = ""

        // Chiamo la lambda insertMatch che si occupa del salvataggio asincrono e della chiusura dell'Activity
        insertMatch(currentMatch)
    }

    if (isPortrait) {
        // ----- LAYOUT PORTRAIT -----
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp) // Margine esterno
        ) {
            // Matrice di pulsanti colorati
            ColorGrid(
                Modifier.weight(1f),
                rows,
                cols,
                simonBtns,
                isPlayerTurn,
                activeColorLabel,
                onColorClick
            )

            // Text per contenere la sequenza
            TextBox(
                Modifier
                    .fillMaxWidth()
                    .height(textBoxHeight)
                    .padding(vertical = 16.dp),
                txt
            )

            // Zona pulsanti di controllo
            ActionButtons(
                onDelete = onDeleteClick,
                onEnd = onEndClick
            )
        }
    } else {
        // ----- LAYOUT LANDSCAPE -----
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sinistra: Griglia (55% dello spazio)
            ColorGrid(
                Modifier.weight(0.55f),
                rows,
                cols,
                simonBtns,
                isPlayerTurn,
                activeColorLabel,
                onColorClick
            )

            // Destra: TextBox + Pulsanti (45% dello spazio)
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
            ) {
                TextBox(
                    Modifier
                        .fillMaxWidth()
                        .height(textBoxHeight),
                    txt
                )

                // Lo Spacer "mangia" tutto lo spazio che avanza tra la TextBox e l'ActionButtons
                Spacer(modifier = Modifier.weight(1f))

                ActionButtons(
                    onDelete = onDeleteClick,
                    onEnd = onEndClick
                )
            }
        }
    }
}

@Composable
fun ColorGrid(
    modifier: Modifier = Modifier,
    rows: Int,
    cols: Int,
    buttonsList: List<SimonButton>,
    isPlayerTurn: Boolean,
    activeColorLabel: String?,
    onButtonClick: (String) -> Unit
) {
    Column(modifier = modifier) {
        var index = 0

        repeat(cols) {
            Row(
                modifier = Modifier
                    .weight(1f) // Distribuisco equamente lo spazio verticale tra le righe
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Spazio tra colonne
            ) {
                repeat(rows) {
                    val i = index

                    val activeColor = buttonsList[i].color
                    val disabledColor = buttonsList[i].colorShadowed
                    val btnLabel = buttonsList[i].label

                    val isThisButtonActive = btnLabel == activeColorLabel

                    Button(
                        modifier = Modifier
                            .weight(1f) // Distribuisco equamente lo spazio orizzontale tra i pulsanti
                            .fillMaxHeight()
                            .padding(vertical = 4.dp), // Spazio tra le righe
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = activeColor,
                            disabledContainerColor = if (isThisButtonActive) activeColor else disabledColor
                        ),
                        onClick = { onButtonClick(btnLabel) },
                        enabled = isPlayerTurn
                    ) { }

                    // Incremento l'indice per scorrere le liste dei colori e delle stringhe
                    index++
                }
            }
        }
    }
}

// TextBox per contenere la sequenza dei pulsanti cliccati
@Composable
fun TextBox(modifier: Modifier = Modifier, txt: String) {
    // Reference: https://developer.android.com/reference/kotlin/androidx/compose/foundation/rememberScrollState.composable
    // Crea e "ricorda" un oggetto che mantiene traccia della posizione attuale dello scorrimento.
    val scrollState = rememberScrollState()

    // Reference: https://developer.android.com/develop/ui/compose/designsystems/material3
    // Reference: https://m3.material.io/styles/color/roles
    // Utilizzo il colorScheme di Material Design perchè cambia il colore
    // del testo e dello sfondo a seconda del tema del telefono automaticamente
    Text(
        text = txt,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))

            // Padding interno al testo (è interno perchè applico il padding DOPO il background)
            .padding(16.dp)

            // Abilito lo scorrimento verticale se il testo eccede lo spazio
            .verticalScroll(scrollState)
    )
}

// Zona dei pulsanti che gestiscono la pulizia della sequenza o il salvataggio della partita corrente allo storico
@Composable
fun ActionButtons(onDelete: () -> Unit, onEnd: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),

        // Allineo i pulsanti ai lati opposti della riga lasciando, quindi rimane uno spazio in mezzo tra di loro
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onDelete,
            colors = ButtonDefaults.filledTonalButtonColors(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                stringResource(R.string.delete_btn),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Button(
            onClick = onEnd,
            colors = ButtonDefaults.filledTonalButtonColors(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                stringResource(R.string.end_game_btn),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val demoButtons = listOf(
        SimonButton("R", Color.Red, Color(0xFFAA3333)),
        SimonButton("G", Color.Green, Color(0xFF338833)),
        SimonButton("B", Color.Blue, Color(0xFF3333AA)),
        SimonButton("M", Color.Magenta, Color(0xF9AA33AA)),
        SimonButton("Y", Color.Yellow, Color(0xFFAAAA33)),
        SimonButton("C", Color.Cyan, Color(0xFF33AAAA))
    )

    GameScreen(simonBtns = demoButtons, insertMatch = {})
}