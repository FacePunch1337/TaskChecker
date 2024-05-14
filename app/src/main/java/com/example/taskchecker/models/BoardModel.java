package com.example.taskchecker.models;

import org.json.JSONException;
import org.json.JSONObject;

public class BoardModel {
    private String _id;
    private String _title;
    private String _owner;
    private JSONObject _boardData;

    public BoardModel() {}
    public BoardModel(JSONObject boardData) throws JSONException {
        this._boardData = boardData;
        this._id = boardData.getString("_id");
        this._title = boardData.getString("title");
        this._owner = boardData.getString("owner");
    }

    public void setBoardData(JSONObject boardData){
        _boardData = boardData;
    }
    public JSONObject getBoardData(){
        return _boardData;
    }
    public String getId() {
        return _id;
    }
    public void setId(String boardId){
        _id = boardId;
    }
    public String getTitle() {
        return _title;
    }
    public void setTitle(String title){
        _title = title;
    }
    public String getOwner() {
        return _owner;
    }
    public void setOwner(String owner){
        _owner = owner;
    }
}
