package com.amibar.boggle.data;

import java.io.Serializable;

/**
 * Represents a user of the Boggle app.
 * This is a simple data class (POJO) used for storing user information, especially when
 * interacting with Firebase Realtime Database.
 */
@SuppressWarnings("unused")
public class User implements Serializable {
    /** The user's unique ID. */
    private String uid;
    /** The user's chosen display name. */
    private String displayName;
    /** The user's email address, used for authentication and identification. */
    private String email;
    /** A Base64 encoded string of the user's profile picture. */
    private String profileImageBase64;
    /** The User's current device's Firebase Cloud Messaging (FCM) token. */
    private String fcmToken;

    /**
     * Default constructor required for Firebase Realtime Database deserialization.
     */
    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * Creates a new User object.
     * @param uid The user's unique ID.
     * @param displayName The user's display name.
     * @param email The user's email address.
     * @param profileImageBase64 The Base64 string of the user's profile image.
     * @param fcmToken The user's Firebase Cloud Messaging (FCM) token.
     */
    public User(String uid, String displayName, String email, String profileImageBase64, String fcmToken) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
        this.fcmToken = fcmToken;
    }

    /**
     * @return The user's unique ID.
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid The user's unique ID.
     */
    public void setUid(String uid) {
        this.uid = uid;
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
    /**
     * @return The user's Firebase Cloud Messaging (FCM) token.
     */
    public String getFcmToken() {
        return fcmToken;
    }
}
