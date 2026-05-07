package com.example.simon_intermediate.data

import androidx.room.Room
import androidx.room.RoomDatabase
import kotlin.concurrent.Volatile
import android.content.Context
import android.util.Log
import androidx.room.Database

// Specifico le entità (tabelle) e la versione del DB
@Database(entities = [Match::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    // Espongo il DAO per poterlo usare nel resto dell'app
    abstract fun matchDao(): MatchDao

    companion object {
        // @Volatile garantisce che le modifiche a dbInstance siano subito visibili a tutti i thread, evitando cache locali incoerenti
        @Volatile
        private var dbInstance: AppDatabase? = null
        private const val dbTAG: String = "AppDatabase"

        // Metodo per ottenere direttamente il DAO legato al database (Singleton)
        fun getDatabaseDao(context: Context): MatchDao {
            Log.d(dbTAG, "Invocato metodo per ottenere il Dao del database, contesto: $context")

            // Primo controllo: se il database non esiste, lo creo in modo sicuro
            if (dbInstance == null) {
                // Se è null, sincronizzo l'accesso per creare l'istanza in modo sicuro
                synchronized(this) {
                    // Secondo controllo: un altro thread potrebbe aver creato l'istanza proprio mentre aspettavo il lock
                    if (dbInstance == null) {
                        dbInstance = Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "simon-db"
                        )
                        .fallbackToDestructiveMigration(dropAllTables = true) // LA TENGO PER IL DEVELOP, al momento di distruggere il database per un cambio di versione non gestito, Room elimina tutte le tabelle presenti nel db
                        .allowMainThreadQueries() // Permette di accedere al database sul thread principale
                        .build()
                    }
                }
            }

            // Restituisco direttamente il DAO richiamando la funzione astratta della classe
            return dbInstance!!.matchDao()
        }
    }
}
