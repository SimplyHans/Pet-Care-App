package ca.gbc.petcareapp.auth.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE role = :role")
    suspend fun findByRole(role: String): List<User>
    
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): User?
    
    @Query("UPDATE users SET fullName = :fullName WHERE id = :id")
    suspend fun updateName(id: Long, fullName: String)
    
    @Query("UPDATE users SET email = :email WHERE id = :id")
    suspend fun updateEmail(id: Long, email: String)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteById(id: Long)
}
