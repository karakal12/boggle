package com.amibar.boggle.data;

public class User {
    private String uid;
    private String displayName;
    private String email;
    private String profileImageBase64;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String uid, String displayName, String email, String profileImageBase64) {
        this.uid = uid;
        this.displayName = displayName;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
    }

    public String getUid() {
        return uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }
}
