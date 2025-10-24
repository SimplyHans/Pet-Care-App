package ca.gbc.petcareapp.auth.domain


import ca.gbc.petcareapp.auth.data.UserDao
import ca.gbc.petcareapp.auth.data.User
import ca.gbc.petcareapp.auth.security.PasswordHasher




class UserRepository(private val userDao: UserDao) {


    suspend fun register(fullName: String, email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        validateRegister(fullName, trimmedEmail, password)?.let { return Result.failure(IllegalArgumentException(it)) }


// Check existing
        val exists = userDao.findByEmail(trimmedEmail)
        if (exists != null) return Result.failure(IllegalStateException("Email is already registered."))


        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        val user = User(fullName = fullName.trim(), email = trimmedEmail, passwordHash = hash, salt = salt)
        val id = userDao.insert(user)
        return Result.success(user.copy(id = id))
    }


    suspend fun login(email: String, password: String): Result<User> {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isEmpty() || password.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email and password are required."))
        }
        val user = userDao.findByEmail(trimmedEmail)
            ?: return Result.failure(IllegalArgumentException("No account found for this email."))


        val hash = PasswordHasher.hash(password, user.salt)
        return if (hash == user.passwordHash) Result.success(user)
        else Result.failure(IllegalArgumentException("Incorrect password."))
    }


    private fun validateRegister(fullName: String, email: String, password: String): String? {
        if (fullName.isBlank()) return "Full name is required."
        if (!email.contains('@') || !email.contains('.')) return "Enter a valid email."
        if (password.length < 6) return "Password must be at least 6 characters."
        return null
    }
}