package com.example.oscar.realnews;

/**
 * Created by oscar on 28/04/2017.
 */

public class Post {


    private String title;
    private String desc;
    private String image;
    private String userId;
    private String userPic;
    private String userName;
    private String date;

    public Post(String title, String desc, String image, String userId, String userPic, String userName, String date) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.userId = userId;
        this.userPic = userPic;
        this.userName = userName;
        this.date = date;
    }

    public Post() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPic() {
        return userPic;
    }

    public void setUserPic(String userPic) {
        this.userPic = userPic;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


}