package com.example.taskchecker.models;

public class CommentModel {


    private String avatarURL;
    private String username;
    private String text;
    private String memberId;
    private String time;
    private Integer index;
    private String id;


    public CommentModel(String text, String memberId, String time, Integer index) {
        this.text = text;
        this.time = time;
        this.memberId = memberId;
        this.index = index;

    }

    public String getText() {
        return text;
    }
    public String getMemberId() {
        return memberId;
    }
    public String getTime() {
        return time;
    }
    public void setText(String text) {
        this.text = text;
    }
    public void setMemberId(String id) {
        this.id = id;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getCommentId() {
        return id;
    }
    public void setCommentId(String id) {
        this.id = id;
    }

    public Integer getIndex() {
        return index;
    }
    public void setIndex(Integer index) {
        this.index = index;
    }

    public void setAvatarURL(String avatarURL) {
        this.avatarURL = avatarURL;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatarURL() {
       return avatarURL;
    }
    public String getUsername() {
        return username;
    }
}
