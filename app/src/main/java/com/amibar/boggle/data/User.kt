package com.amibar.boggle.data

import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

/**
 * Represents a user of the Boggle app.
 * This is a data class used for storing user information in Firebase Realtime Database.
 */
@IgnoreExtraProperties
data class User(
    var uid: String = "",
    var displayName: String = "",
    var email: String = "",
    var profileImageBase64: String? = null,
    @Suppress("unused") var fcmToken: String? = null
) : Serializable
