package com.example.simon_intermediate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// Mettendo "suspend" alle funzioni, permetto a Room di gestire le operazioni in modo non bloccante
@Dao
interface MatchDao {
    // Inserisco una nuova riga nel database
    @Insert
    suspend fun insert(currentMatch: Match)

    // Recupero tutte le righe dalla tabella; ho messo l'ordine DECRESCENTE
    // così mostro la partita più recente in cima alla lista
    @Query("SELECT * FROM gameHistory ORDER BY id DESC")
    fun getAll(): Flow<List<Match>>

    // Recupero le informazioni di una singola riga tramite il suo ID
    @Query("SELECT * FROM gameHistory WHERE id = :matchID")
    suspend fun getMatchInfo(matchID: Int): Match
}
