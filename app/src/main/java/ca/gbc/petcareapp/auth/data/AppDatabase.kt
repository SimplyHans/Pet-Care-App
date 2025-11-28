package ca.gbc.petcareapp.auth.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ca.gbc.petcareapp.data.Booking
import ca.gbc.petcareapp.data.BookingDao
import ca.gbc.petcareapp.data.Converters           // ← add this
import ca.gbc.petcareapp.pets.Pet
import ca.gbc.petcareapp.pets.PetDao

@Database(
    entities = [User::class, Pet::class, Booking::class],
    version = 5,                                   // ← bumped for serviceSpecialization field
    exportSchema = false
)
@TypeConverters(Converters::class)                 // ← make Room use Instant converters
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun BookingDao(): BookingDao          // ← lowerCamelCase

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "petcare.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
