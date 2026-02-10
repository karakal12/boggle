package com.amibar.boggle.data;

@SuppressWarnings("unused")
public class User {
    private String displayName;
    private String email;
    private String profileImageBase64;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String displayName, String email, String profileImageBase64) {
        this.displayName = displayName;
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }
}
