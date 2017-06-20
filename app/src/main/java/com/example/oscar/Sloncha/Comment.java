package com.example.oscar.Sloncha;

/**
 * Created by oscar on 06/05/2017.
 */

public class Comment {

    String comment;
    String date;
    String uid;
    String postId;

    public Comment(String comment, String date, String uid, String postId) {
        this.comment = comment;
        this.date = date;
        this.uid = uid;
        this.postId = postId;
    }

    public Comment(String comment, String date, String uid) {
        this.comment = comment;
        this.date = date;
        this.uid = uid;
        this.postId = postId;
    }

    public Comment() {
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
