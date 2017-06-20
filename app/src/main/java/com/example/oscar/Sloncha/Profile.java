package com.example.oscar.Sloncha;

/**
 * Created by oscar on 22/05/2017.
 */

public class Profile {

    private String Desc;
    private String Email;
    private String Image;
    private String Name;
    private String UserId;

    public Profile(String desc, String email, String image, String name) {
        Desc = desc;
        Email = email;
        Image = image;
        Name = name;
    }

    public Profile() {
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
}
