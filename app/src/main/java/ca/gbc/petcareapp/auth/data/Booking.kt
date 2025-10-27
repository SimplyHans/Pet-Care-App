package ca.gbc.petcareapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.Instant
import java.util.UUID

@TypeConverters(Converters::class)               // <— add this (entity-level)
@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val caregiverId: String,
    val caregiverName: String,
    val serviceType: String,
    val notes: String? = null,
    val startTimeUtc: Instant,                   // <— Instant needs converter
    val createdAtUtc: Instant = Instant.now()    // <— Instant needs converter
)
