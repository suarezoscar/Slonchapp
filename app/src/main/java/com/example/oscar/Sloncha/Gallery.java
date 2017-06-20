package com.example.oscar.Sloncha;

/**
 * Created by oscar on 22/05/2017.
 */

public class Gallery {

    private String Desc;
    private String Email;
    private String Image;
    private String PostId;
    private String UserId;

    public Gallery(String postId, String image, String userId) {
        UserId = userId;
        Image = image;
        PostId = postId;
    }

    public Gallery() {
    }


    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }
}
