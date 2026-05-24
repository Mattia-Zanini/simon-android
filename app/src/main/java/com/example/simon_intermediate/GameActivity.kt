package com.example.simon_intermediate

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.simon_intermediate.ui.theme.SimonIntermediateTheme
import com.example.simon_intermediate.data.AppDatabase

// Tag per il logger di debug di GameActivity
const val tagGameActivity = "GameActivity"

data class SimonButton(
    val label: String,
    val color: Color,
    val colorShadowed: Color,
    val sound: SimonSound
)

class GameActivity : ComponentActivity() {
    private lateinit var simonButtons: List<SimonButton>

    private val gameViewModel: GameViewModel by viewModels {
        val dao = AppDatabase.getDatabaseDao(this.applicationContext)
        GameViewModelFactory(this.application, dao)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(tagGameActivity, "onCreate Activity 2")

        enableEdgeToEdge()

        simonButtons = listOf(
            SimonButton("R", Color.Red, Color(0xFFAA3333), SimonSound(261.63)), // Do
            SimonButton("G", Color.Green, Color(0xFF338833), SimonSound(293.66)), // Re
            SimonButton("B", Color.Blue, Color(0xFF3333AA), SimonSound(329.63)), // Mi
            SimonButton("M", Color.Magenta, Color(0xF9AA33AA), SimonSound(349.23)), // Fa
            SimonButton("Y", Color.Yellow, Color(0xFFAAAA33), SimonSound(392.00)), // Sol
            SimonButton("C", Color.Cyan, Color(0xFF33AAAA), SimonSound(440.00)) // La
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
                        gameViewModel,
                        systemFinish = { finish() }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        Log.d(tagGameActivity, "onResume called")

        simonButtons.forEach { it.sound.prepare() }

        gameViewModel.setAppVisibility(true)
        gameViewModel.playComputerSequence(simonButtons)
    }

    override fun onPause() {
        super.onPause()

        Log.d(tagGameActivity, "onPause called")

        gameViewModel.setAppVisibility(false)
        simonButtons.forEach { it.sound.release() }
    }
}

@Composable
fun GameScreen(
    modifier: Modifier = Modifier,
    simonBtns: List<SimonButton>,
    viewModel: GameViewModel,
    systemFinish: () -> Unit
) {
    // Recupero l'orientamento attuale del dispositivo
    val orientation = LocalConfiguration.current.orientation
    val isPortrait: Boolean = orientation == Configuration.ORIENTATION_PORTRAIT

    // sostituzione del tasto "Back" del sistema
    BackHandler(enabled = true) {
        // Se la partita è in corso, clicco virtualmente Fine Partita
        if (viewModel.isGameStarted && !viewModel.isGameOver) {
            Log.d(tagGameActivity, "Back pressed: triggering end game")
            viewModel.onEndClick(systemFinish)
        } else {
            // se non è iniziata la partita allora chiudo l'activity
            systemFinish()
        }
    }

    // Assegno il numero di colonne e di righe
    val cols = 3
    val rows = 2

    // Altezza variabile per la TextBox a seconda dell'orientamento
    val textBoxHeight = if (isPortrait) 180.dp else 200.dp

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
                viewModel.isPlayerTurn,
                viewModel.isGamePaused,
                viewModel.isGameOver,
                viewModel.activeColorLabel,
                onButtonClick = { color ->
                    simonBtns.find { it.label == color }?.sound?.play()
                    viewModel.onColorClick(color, simonBtns, systemFinish)
                }
            )

            // Text per contenere la sequenza
            TextBox(
                Modifier
                    .fillMaxWidth()
                    .height(textBoxHeight)
                    .padding(vertical = 16.dp),
                viewModel.userSequence
            )

            // Zona pulsanti di controllo
            ActionButtons(
                isComputerTurn = (!viewModel.isPlayerTurn && viewModel.isGameStarted && !viewModel.isGameOver),
                isStarted = viewModel.isGameStarted,
                isPaused = viewModel.isGamePaused,
                onStart = { viewModel.onStartClick(simonBtns) },
                onPause = { viewModel.onPauseClick() },
                onContinue = { viewModel.onContinueClick(simonBtns) },
                onEnd = { viewModel.onEndClick(onSaved = systemFinish) }
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
                viewModel.isPlayerTurn,
                viewModel.isGamePaused,
                viewModel.isGameOver,
                viewModel.activeColorLabel,
                onButtonClick = { color ->
                    simonBtns.find { it.label == color }?.sound?.play()
                    viewModel.onColorClick(color, simonBtns, systemFinish)
                }
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
                    viewModel.userSequence
                )

                // Lo Spacer "mangia" tutto lo spazio che avanza tra la TextBox e l'ActionButtons
                Spacer(modifier = Modifier.weight(1f))

                ActionButtons(
                    isComputerTurn = (!viewModel.isPlayerTurn && viewModel.isGameStarted && !viewModel.isGameOver),
                    isStarted = viewModel.isGameStarted,
                    isPaused = viewModel.isGamePaused,
                    onStart = { viewModel.onStartClick(simonBtns) },
                    onPause = { viewModel.onPauseClick() },
                    onContinue = { viewModel.onContinueClick(simonBtns) },
                    onEnd = { viewModel.onEndClick(onSaved = systemFinish) }
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
    isPaused: Boolean,
    isOver: Boolean,
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

                    val grayColor = Color.LightGray
                    val errorColor = Color(0xFFCE0000)
                    val btnLabel = buttonsList[i].label

                    // colori di default
                    var activeColor = buttonsList[i].color
                    var disabledColor = buttonsList[i].colorShadowed

                    // assegno i colori dei pulsanti a seconda dello stato del gioco
                    if (isPaused) {
                        activeColor = grayColor
                        disabledColor = grayColor
                    }
                    if (isOver) {
                        activeColor = errorColor
                        disabledColor = errorColor
                    }

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
                        enabled = isPlayerTurn && !isPaused && !isOver
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
fun ActionButtons(
    isComputerTurn: Boolean,
    isStarted: Boolean,
    isPaused: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onContinue: () -> Unit,
    onEnd: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),

        // Allineo i pulsanti ai lati opposti della riga lasciando, quindi rimane uno spazio in mezzo tra di loro
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = onStart,
            enabled = !isStarted,
            colors = ButtonDefaults.filledTonalButtonColors(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                stringResource(R.string.start_game),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Button(
            onClick = if (!isPaused) onPause else onContinue,
            enabled = isComputerTurn,
            colors = ButtonDefaults.filledTonalButtonColors(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                text = if (!isPaused) stringResource(R.string.pause_game) else stringResource(R.string.continue_game),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
        Button(
            onClick = onEnd,
            enabled = isStarted,
            colors = ButtonDefaults.filledTonalButtonColors(MaterialTheme.colorScheme.primary),
        ) {
            Text(
                stringResource(R.string.end_game_btn),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}
