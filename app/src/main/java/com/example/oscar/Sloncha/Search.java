package com.example.oscar.Sloncha;

/**
 * Created by oscar on 13/06/2017.
 */

public class Search {
    private String Email;
    private String Image;
    private String Name;
    private String UserId;
    private String Desc;

    public Search() {
    }

    public Search(String email, String image, String name, String userId, String desc) {
        Email = email;
        Image = image;
        Name = name;
        UserId = userId;
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

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }
}
