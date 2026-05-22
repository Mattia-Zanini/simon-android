package com.example.simon_intermediate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simon_intermediate.data.AppDatabase
import com.example.simon_intermediate.data.Match
import com.example.simon_intermediate.data.MatchDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Estendendo AndroidViewModel posso accedere all'Application context
class MainViewModel(application: Application, private val matchDao: MatchDao) :
    AndroidViewModel(application) {

    // Questo mi permette di far reagire la UI istantaneamente quando i dati cambiano
    // e WhileSubscribed serve per risparmiare risorse quando la UI non è visualizzata
    val historyData: StateFlow<List<Match>> = matchDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Esegue l'inserimento di una partita
    fun insertMatch(match: Match) {
        viewModelScope.launch(Dispatchers.IO) {
            matchDao.insert(match)
        }
    }

    // Esegue la cancellazione totale delle partite
    fun deleteAllMatches() {
        viewModelScope.launch(Dispatchers.IO) {
            matchDao.deleteAll()
        }
    }
}

// Definisco la Factory necessaria per passare i parametri al costruttore del ViewModel
class MainViewModelFactory(
    private val application: Application,
    private val matchDao: MatchDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            // Utilizzo .cast() per convertire in modo sicuro l'istanza nel tipo T richiesto
            return modelClass.cast(MainViewModel(application, matchDao))!!
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
