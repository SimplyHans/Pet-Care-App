package ca.gbc.petcareapp.auth.security


import java.security.MessageDigest
import java.security.SecureRandom


object PasswordHasher {
    private val random = SecureRandom()


    fun generateSalt(bytes: Int = 16): String {
        val arr = ByteArray(bytes)
        random.nextBytes(arr)
        return arr.joinToString("") { String.format("%02x", it) }
    }


    fun hash(password: String, saltHex: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val salted = (saltHex + password).encodeToByteArray()
        val digest = md.digest(salted)
        return digest.joinToString("") { String.format("%02x", it) }
    }
}