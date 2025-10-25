package ca.gbc.petcareapp.auth.data

import androidx.room.*

@Dao
interface PetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: Pet): Long

    @Query("SELECT * FROM pets WHERE userId = :userId")
    suspend fun getPetsForUser(userId: Long): List<Pet>

    @Query("SELECT * FROM pets WHERE id = :petId LIMIT 1")
    suspend fun getPetById(petId: Long): Pet?

    @Delete
    suspend fun delete(pet: Pet)
}
