package com.example.oscar.realnews;

/**
 * Created by oscar on 23/05/2017.
 */

public class Friend {

    String userId;
    String ImageUri;

    public Friend(String userId) {
        this.userId = userId;
    }

    public Friend(String userId, String imageUri) {
        this.userId = userId;
        ImageUri = imageUri;
    }

    public Friend() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUri() {
        return ImageUri;
    }

    public void setImageUri(String imageUri) {
        ImageUri = imageUri;
    }

}
