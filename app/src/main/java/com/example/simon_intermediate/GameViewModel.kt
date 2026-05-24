package com.example.simon_intermediate

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simon_intermediate.data.Match
import com.example.simon_intermediate.data.MatchDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ho creato un altro ViewModel per la GameActivity in quanto è consigliato creare ViewModel
// differenti per diverse Activity, soprattutto se devono gestire dati e compiti differenti
// qui infatti verrà gestita la logica del computer (da fare) e il slvataggio delle partite
class GameViewModel(application: Application, private val matchDao: MatchDao) :
    AndroidViewModel(application) {

    // contiene la sequenza che il computer genera e mostra all'utente
    var gameSequence by mutableStateOf("")
        private set // incapsulamento: in questo modo la variabile è SOLO visibile all'esterno, ma non modificabile

    // contiene la sequenza che l'utente ha cliccato
    var userSequence by mutableStateOf("")
        private set

    // false se il computer sta riproducendo la sequenza, true se tocca all'utente
    var isPlayerTurn by mutableStateOf(false)
        private set

    // Contiene la label del colore che il computer sta mostrando (es. "R")
    var activeColorLabel by mutableStateOf<String?>(null)
        private set

    // Rappresenta l'index della sequenza che il computer ha appena mostrato
    var computerIndex by mutableIntStateOf(0)
        private set

    // indica dove è arrivato l'utente nel riprodurre la sequenza
    var playerIndex by mutableIntStateOf(0)
        private set

    // mantiene lo stato in cui si trova il gioco, se in pausa oppure no
    var isGamePaused by mutableStateOf(false)
        private set

    // tiene lo stato di gioco di fine partita o partita in corso
    var isGameOver by mutableStateOf(false)
        private set

    // solo il giocare può decidere quando iniziare la partita
    var isGameStarted by mutableStateOf(false)
        private set

    // tiene traccia se l'applicazione è in background (stato: paused)
    var isAppPaused by mutableStateOf(false)
        private set

    // Eseguo la riproduzione della sequenza da parte del sistema
    fun playComputerSequence(simonBtns: List<SimonButton>) {
        viewModelScope.launch {
            // turno del computer, il gioco NON è in pausa E non è finito E se l'app non è in background
            if (isGameStarted && !isPlayerTurn && !isGamePaused && !isGameOver && !isAppPaused) {
                if (gameSequence.isEmpty()) {
                    // inizializzo la sequenza di gioco se è vuota (inizio partita)
                    gameSequence = simonBtns.random().label
                    Log.d(tagGameActivity, "Initial sequence set")
                }

                Log.d(tagGameActivity, "Current sequence: $gameSequence")

                delay(800) // delay iniziale, giusto per non avere il bottone subito accesso
                while (computerIndex < gameSequence.length) {
                    val currentLabel = gameSequence[computerIndex].toString()
                    activeColorLabel = currentLabel
                    delay(1000) // acceso
                    activeColorLabel = null
                    delay(500) // pausa tra i colori

                    if (isGamePaused) break // se l'utente preme pausa, esce dal loop
                    if (isAppPaused) break // se l'activity è in background allora blocco la riproduzione

                    Log.d(tagGameActivity, "Computer sequence index: [$computerIndex]")
                    computerIndex++
                }

                if (computerIndex >= gameSequence.length) {
                    isPlayerTurn = true // ora tocca all'utente
                    computerIndex = 0  // reset per la prossima sequenza
                    Log.d(tagGameActivity, "Computer finished sequence and reset variables")
                }
            }
        }
    }

    // gestisce la fine del gioco
    private fun handleGameOver(onSaved: () -> Unit) {
        viewModelScope.launch {
            if (isGameOver) {
                // il gioco è fermo e i tasti diventano rossi
                delay(1000)

                val seqNum = gameSequence.length

                // non registro la partita se interrotta durante la presentazione della prima sequenza
                if (seqNum == 1 && !isPlayerTurn) {
                    Log.d(
                        tagGameActivity,
                        "Match not saved: ended during first sequence presentation"
                    )
                    onSaved()

                    // serve per uscire in modo anticipato da una specifica espressione
                    // lambda, in questo caso, dal blocco della Coroutine avviata con launch
                    return@launch
                }

                // nel caso in cui la stringa non sia vuota allora la salvo nel modo standard
                var gameSequenceToSave = ""
                if (seqNum > 0) {
                    gameSequenceToSave = gameSequence[0].toString()
                    repeat(seqNum - 1) { i ->
                        gameSequenceToSave += ", ${gameSequence[i + 1]}"
                    }
                }

                // Creazione dell'oggetto Match con la sequenza attuale
                val currentMatch = Match(
                    finalSequence = gameSequenceToSave,
                    errorIndex = playerIndex,
                    maxLengthCompleted = seqNum - 1
                )

                // effettuo il salvataggio della partita nel database
                saveMatch(currentMatch, onSaved)
            }
        }
    }

    val onStartClick: (List<SimonButton>) -> Unit = { simonBtns ->
        Log.d(tagGameActivity, "BTN 'Start' clicked")

        isPlayerTurn = false
        isGameStarted = true
        playComputerSequence(simonBtns)
    }

    val onPauseClick: () -> Unit = {
        Log.d(tagGameActivity, "BTN 'Pause' clicked")

        isGamePaused = true
    }

    // riprendo la partita dopo la pausa
    val onContinueClick: (List<SimonButton>) -> Unit = { simonBtns ->
        Log.d(tagGameActivity, "BTN 'Continue' clicked")

        isGamePaused = false
        playComputerSequence(simonBtns)
    }

    fun onEndClick(onSaved: () -> Unit) {
        // evito chiamate multiple se la partita è già conclusa
        if (isGameOver) return

        // Se la partita non è iniziata, chiudo semplicemente l'Activity
        if (!isGameStarted) {
            onSaved()
            return
        }

        Log.d(tagGameActivity, "BTN 'End Game' clicked")

        isGameOver = true
        isPlayerTurn = false
        handleGameOver(onSaved)
    }

    // Logica di controllo di vittoria, colore corretto e sconfitta dell'utente
    val onColorClick: (String, List<SimonButton>, () -> Unit) -> Unit = { color, simonBtns, onSaved ->
            Log.d(tagGameActivity, "BTN '$color' clicked")

            if (color == gameSequence[playerIndex].toString()) {
                Log.d(tagGameActivity, "User clicked the correct color")

                playerIndex++
                userSequence += if (userSequence.isEmpty()) color else ", $color"

                if (playerIndex == gameSequence.length) {
                    Log.d(
                        tagGameActivity,
                        "User completed sequence correctly: $gameSequence"
                    )
                    isPlayerTurn = false
                    gameSequence += simonBtns.random().label // aggiungo un colore addizionale random
                    playerIndex = 0
                    userSequence = "" // ripulisco la sequenza dell'utente
                    playComputerSequence(simonBtns)
                }
            } else {
                Log.d(tagGameActivity, "User clicked the wrong color")

                onEndClick(onSaved)
            }
        }

    // salvo la partita nel DB
    private fun saveMatch(match: Match, onSaved: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            matchDao.insert(match)

            // Una volta salvato, invoco la callback sul thread principale
            launch(Dispatchers.Main) {
                onSaved()
            }
        }
    }

    fun setAppVisibility(isVisible: Boolean) {
        // isVisible = true -> app running
        // isVisible = false -> app paused
        isAppPaused = !isVisible
    }
}

// Factory per inizializzare il GameViewModel con il DAO
class GameViewModelFactory(
    private val application: Application,
    private val matchDao: MatchDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return modelClass.cast(GameViewModel(application, matchDao))!!
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
