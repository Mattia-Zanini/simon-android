package com.example.simon_intermediate

import android.content.Intent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.simon_intermediate.ui.theme.SimonIntermediateTheme

// Tag per il logger di debug di MainActivity
const val tagMainD = "MainActivity"

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tagMainD, "onCreate Activity 1")

        enableEdgeToEdge()

        setContent {
            SimonIntermediateTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    // MainScreen utilizza gli "insets" (presenti in 'innerPadding') per mantenere
                    // l'interfaccia utente lontana dalla UI di sistema e dai ritagli del display (come il notch)
                    MainScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        goToScreen2 = { updatedHistory -> // <-- Ricevo lo storico dal composable

                            val myIntent = Intent(this, GamesHistory::class.java)

                            // Reference: https://developer.android.com/reference/android/content/Intent#putExtra(java.lang.String,%20android.os.Parcelable)
                            // Trasformo la List in ArrayList, che android sa trattare, per passarla come "StringArrayListExtra"
                            myIntent.putStringArrayListExtra(
                                "GAMES_HISTORY",
                                ArrayList(updatedHistory.reversed()) // "reversed" così in questo modo la partita appena fatta è in cima alla lista
                            )

                            Log.d(tagMainD, "Inserted the history inside the intent")

                            Log.d(tagMainD, "startActivity of Screen 2")

                            // Avvio l'Activity, non verranno ricevute informazioni quando l'Activity termina
                            startActivity(myIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier, goToScreen2: (List<String>) -> Unit) {
    // Recupero l'orientamento attuale del dispositivo
    val orientation = LocalConfiguration.current.orientation
    val isPortrait: Boolean = orientation == Configuration.ORIENTATION_PORTRAIT

    // Contiene lo storico di tutte le partite
    var history by rememberSaveable { mutableStateOf(listOf<String>()) }

    // Stato per memorizzare la sequenza di colori cliccati
    var txt by rememberSaveable { mutableStateOf("") }

    // Lista dei colori per la griglia
    val colors = listOf(Color.Red, Color.Green, Color.Blue, Color.Magenta, Color.Yellow, Color.Cyan)
    // Lettere associate ai colori
    val btnStrings = listOf("R", "G", "B", "M", "Y", "C")

    // Assegno il numero di colonne e di righe
    val cols = 3
    val rows = 2

    // Altezza variabile per la TextBox a seconda dell'orientamento
    val textBoxHeight = if (isPortrait) 180.dp else 200.dp

    // Queste sono le lambda che vengono utilizzate in entrambe le situazioni (sia portrait che landscape)
    val onColorClick: (String) -> Unit = { color ->
        txt += if (txt.isEmpty()) color else ", $color"
        Log.d(tagMainD, "BTN '$color' clicked")
    }

    val onDeleteClick: () -> Unit = {
        txt = "" // ripulisco la text
        Log.d(tagMainD, "BTN 'Delete' clicked")
    }

    val onEndClick: () -> Unit = {
        Log.d(tagMainD, "BTN 'End Game' clicked")

        // NON controllo che txt != empty perchè salvo anche le partite con 0 pulsanti cliccati
        history += txt // aggiungo la sequenza allo storico
        txt = ""

        goToScreen2(history)
    }

    if (isPortrait) {
        // ----- LAYOUT PORTRAIT -----
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp) // Margine esterno
        ) {
            // Matrice di pulsanti colorati
            ColorGrid(Modifier.weight(1f), rows, cols, colors, btnStrings, onColorClick)

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
            ColorGrid(Modifier.weight(0.55f), rows, cols, colors, btnStrings, onColorClick)

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

// Matrice dei pulsanti colorati
@Composable
fun ColorGrid(
    modifier: Modifier = Modifier,
    rows: Int,
    cols: Int,
    colors: List<Color>,
    colorsStrings: List<String>,
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

                    Button(
                        modifier = Modifier
                            .weight(1f) // Distribuisco equamente lo spazio orizzontale tra i pulsanti
                            .fillMaxHeight()
                            .padding(vertical = 4.dp), // Spazio tra le righe
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(colors[i]),
                        onClick = { onButtonClick(colorsStrings[i]) }
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
        Button(onClick = onDelete) { Text(stringResource(R.string.delete_btn)) }
        Button(onClick = onEnd) { Text(stringResource(R.string.end_game_btn)) }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    MainScreen(goToScreen2 = {})
}