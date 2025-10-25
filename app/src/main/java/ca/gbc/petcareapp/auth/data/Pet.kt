package ca.gbc.petcareapp.auth.data

import androidx.room.*

@Entity(
    tableName = "pets",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE // Deletes pet(s) if user is deleted
        )
    ],
    indices = [Index(value = ["userId"])] // improves query performance
)
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,

    @ColumnInfo(name = "userId") val userId: Long, // Foreign key to User

    @ColumnInfo(name = "petType") val petType: String,
    @ColumnInfo(name = "petName") val petName: String,
    @ColumnInfo(name = "breed") val breed: String,
    @ColumnInfo(name = "age") val age: Int,
    @ColumnInfo(name = "desc") val desc: String? = null, // Optional description

    val createdAt: Long = System.currentTimeMillis()
)
