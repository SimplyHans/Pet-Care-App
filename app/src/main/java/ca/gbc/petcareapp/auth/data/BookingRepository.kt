package ca.gbc.petcareapp.data

import android.content.Context
import ca.gbc.petcareapp.auth.data.AppDatabase
import kotlinx.coroutines.flow.Flow

class BookingRepository(context: Context) {
    private val dao = AppDatabase.get(context).BookingDao()

    fun getAll(): Flow<List<Booking>> = dao.getAll()
    suspend fun save(booking: Booking) = dao.upsert(booking)
}
