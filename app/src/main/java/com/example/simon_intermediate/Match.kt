package com.example.simon_intermediate

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gameHistory")
data class Match(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // ID autogenerato
    val maxLength: Int,
    val finalSequence: String,
    val errorIndex: Int
)
