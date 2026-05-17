package com.amibar.boggle.data

import java.io.Serializable
import java.util.Objects

/**
 * Represents a user of the Boggle app.
 * This is a data class used for storing user information in Firebase Realtime Database.
 */
class User(
    var uid: String = "",
    var displayName: String = "",
    var email: String = "",
    var profileImageBase64: String? = null,
    @Suppress("unused") var fcmToken: String? = null
) : Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        return uid == other.uid
    }

    override fun hashCode(): Int {
        return Objects.hash(uid)
    }
}
