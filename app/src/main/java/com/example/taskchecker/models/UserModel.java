package com.example.taskchecker.models;

import org.json.JSONException;
import org.json.JSONObject;

public class UserModel {
    private static String _id;
    private static String _username;
    private static String _email;
    public static String _avatarURL;
    public static String _avatarFilename;
    public static String _defaultAvatar = "https://firebasestorage.googleapis.com/v0/b/taskcheker-39fd8.appspot.com/o/avatars%2FdefaultAvatar.png?alt=media&token=2dc441da-b359-4293-9796-81c838d2c2be";


    private final JSONObject userData;


    public UserModel(JSONObject userData) throws JSONException {
        this.userData = userData;
        _id = userData.getString("_id");
        _username = userData.getString("username");
        _email = userData.getString("email");
        _avatarURL = userData.optString("avatarURL", _defaultAvatar); // Используем значение по умолчанию, если ключ отсутствует
        _avatarFilename = userData.optString("avatarFilename"); // Используем пустую строку, если ключ отсутствует

    }

    public static String getDefaultAvatarUrl() {
        return _defaultAvatar;
    }

    public JSONObject getUserData() {
        return userData;
    }
    public static String get_id() {
        return _id;
    }

    public static String get_username() {
        return _username;
    }
    public static String get_email() {
        return _email;
    }
    public static String getAvatarURL() {
        return _avatarURL;
    }
    public static void setAvatarURL(String avatarURL) {
        _avatarURL = avatarURL;
    }
    public static String getAvatarFilename() {
        return _avatarFilename;
    }
    public static void setAvatarFilename(String avatarFilename) {
        _avatarFilename = avatarFilename;
    }


}

