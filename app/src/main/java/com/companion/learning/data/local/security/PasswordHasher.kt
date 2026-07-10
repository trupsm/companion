package com.companion.learning.data.local.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object PasswordHasher {

    fun generateSalt(): String {
        val sr = SecureRandom()
        val saltBytes = ByteArray(16)
        sr.nextBytes(saltBytes)
        return Base64.getEncoder().encodeToString(saltBytes)
    }

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = password + salt
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(hashBytes)
    }

    fun verifyPassword(password: String, salt: String, passwordHash: String): Boolean {
        val computedHash = hashPassword(password, salt)
        return computedHash == passwordHash
    }
}
