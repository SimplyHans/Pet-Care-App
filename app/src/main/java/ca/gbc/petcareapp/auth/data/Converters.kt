package ca.gbc.petcareapp.data

import androidx.room.TypeConverter
import java.time.Instant

class Converters {
    @TypeConverter
    fun fromInstant(v: Instant?): Long? = v?.toEpochMilli()

    @TypeConverter
    fun toInstant(v: Long?): Instant? = v?.let { Instant.ofEpochMilli(it) }
}
