package com.example.simon_intermediate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.simon_intermediate.data.Match
import com.example.simon_intermediate.data.MatchDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// ho creato un altro ViewModel per la GameActivity in quanto è consigliato creare ViewModel
// differenti per diverse Activity, soprattutto se devono gestire dati e compiti differenti
// qui infatti verrà gestita la logica del computer (da fare) e il slvataggio delle partite
class GameViewModel(application: Application, private val matchDao: MatchDao) :
    AndroidViewModel(application) {

    // Inserisco i dati della partita nel database
    fun saveMatch(match: Match, onSaved: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            matchDao.insert(match)

            // Una volta salvato, invoco la callback sul thread principale
            launch(Dispatchers.Main) {
                onSaved()
            }
        }
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
