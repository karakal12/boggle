package com.amibar.boggle.data;

import java.io.Serializable;

/**
 * Represents a user of the Boggle app.
 * This is a simple data class (POJO) used for storing user information, especially when
 * interacting with Firebase Realtime Database.
 */
@SuppressWarnings("unused")
public class User implements Serializable {
    /** The user's chosen display name. */
    private String displayName;
    /** The user's email address, used for authentication and identification. */
    private String email;
    /** A Base64 encoded string of the user's profile picture. */
    private String profileImageBase64;

    /**
     * Default constructor required for Firebase Realtime Database deserialization.
     * (e.g., for calls to DataSnapshot.getValue(User.class))
     */
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * Creates a new User object.
     * @param displayName The user's display name.
     * @param email The user's email address.
     * @param profileImageBase64 The Base64 string of the user's profile image.
     */
    public User(String displayName, String email, String profileImageBase64) {
        this.displayName = displayName;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
    }

    /**
     * @return The user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return The user's display name.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return The Base64 encoded profile image string.
     */
    public String getProfileImageBase64() {
        return profileImageBase64;
    }
}
