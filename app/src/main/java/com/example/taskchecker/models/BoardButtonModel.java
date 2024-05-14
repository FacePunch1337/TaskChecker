package com.example.taskchecker.models;

import org.json.JSONException;
import org.json.JSONObject;

public class BoardButtonModel {
    private String _id;
    private String _title;

    public BoardButtonModel(String title) {
        this._title = title;
    }
    public BoardButtonModel(String id, String title) {
        this._id = id;
        this._title = title;
    }

    public String getId() {
        return _id;
    }

    public String getTitle() {
        return _title;
    }
}
