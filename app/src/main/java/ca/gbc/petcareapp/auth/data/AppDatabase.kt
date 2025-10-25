package ca.gbc.petcareapp.auth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ca.gbc.petcareapp.pets.Pet        // ✅ Import Pet entity
import ca.gbc.petcareapp.pets.PetDao     // ✅ Import PetDao

@Database(
    entities = [User::class, Pet::class], // Include both tables
    version = 2,                          // Increment version since schema changed
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao  // Add Pet DAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "petcare.db"
                )
                    .fallbackToDestructiveMigration() // Automatically reset if structure changes
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
