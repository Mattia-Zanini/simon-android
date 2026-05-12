package com.example.simon_intermediate.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gameHistory")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID autogenerato
    val finalSequence: String, // Stringa finale che l'utente ha sbagliato
    val errorIndex: Int, // Indice della stringa su cui l'utente ha sbagliato
    val maxLengthCompleted: Int // La lunghezza della sequenza completata con successo dall'utente
                                // (non è necessario salvarla in quanto è finalSequence.length - 1, ma ho deciso comunque di salvarla)
)