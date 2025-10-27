package ca.gbc.petcareapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(booking: Booking)

    @Query("SELECT * FROM bookings ORDER BY startTimeUtc DESC")
    fun getAll(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): Booking?
}
