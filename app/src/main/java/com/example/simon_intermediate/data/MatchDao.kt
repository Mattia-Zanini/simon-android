package com.example.simon_intermediate.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface MatchDao {
    // Inserisce una nuova riga nel database
    @Insert
    fun insert(currentMatch: Match)

    // Recupera tutte le righe dalla tabella
    @Query("SELECT * FROM gameHistory")
    fun getAll(): List<Match>

    // Rimuove tutte le righe dalla tabella
    @Query("DELETE FROM gameHistory WHERE 1=1")
    fun deleteAll()
}
