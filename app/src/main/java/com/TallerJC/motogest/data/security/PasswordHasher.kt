package com.TallerJC.motogest.data.security

import java.security.MessageDigest

object PasswordHasher {

    fun hash(rawPassword: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(rawPassword.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun matches(rawPassword: String, storedHash: String): Boolean {
        if (storedHash.isBlank()) return false
        return hash(rawPassword) == storedHash
    }
}
