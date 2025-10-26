package ca.gbc.petcareapp.pets

import androidx.room.*
import ca.gbc.petcareapp.auth.data.User

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "userId") val userId: Long,
    @ColumnInfo(name = "petType") val petType: String,
    @ColumnInfo(name = "petName") val petName: String,
    @ColumnInfo(name = "breed") val breed: String,
    @ColumnInfo(name = "age") val age: Int,
    @ColumnInfo(name = "desc") val desc: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
