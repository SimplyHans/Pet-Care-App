package ca.gbc.petcareapp.auth.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val fullName: String,
    @ColumnInfo(collate = ColumnInfo.NOCASE) val email: String,
    val passwordHash: String,
    val salt: String,
    val role: String = "consumer",
    val createdAt: Long = System.currentTimeMillis()
)
