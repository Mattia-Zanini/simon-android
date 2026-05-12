package com.example.simon_intermediate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

// Mettendo "suspend" alle funzioni, permette a Room di gestire le operazioni in modo non bloccante
@Dao
interface MatchDao {
    // Inserisce una nuova riga nel database
    @Insert
    suspend fun insert(currentMatch: Match)

    // Recupera tutte le righe dalla tabella
    @Query("SELECT * FROM gameHistory")
    suspend fun getAll(): List<Match>

    // Inserisce una nuova riga nel database
    @Query("SELECT * FROM gameHistory WHERE id = :matchID")
    suspend fun getMatchInfo(matchID: Int): Match

    // Rimuove tutte le righe dalla tabella (DEV)
    @Query("DELETE FROM gameHistory WHERE 1=1")
    suspend fun deleteAll()
}
