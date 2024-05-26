package com.example.taskchecker.models;

public class TaskModel {
    private String description;
    private boolean checked;
    private String id;

    public TaskModel(String description, boolean checked) {
        this.description = description;
        this.checked = checked;

    }

    public String getDescription() {
        return description;
    }

    public boolean isChecked() {
        return checked;
    }

    public String getTaskId() {
        return id;
    }

    public void setTaskId(String id) {
        this.id = id;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}
