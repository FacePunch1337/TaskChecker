package com.example.taskchecker.services;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskchecker.R;
import com.example.taskchecker.models.TaskModel;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskModel> taskList;
    private OnCheckedChangeListener onCheckedChangeListener;
    private OnDeleteTaskClickListener onDeleteTaskClickListener;

    public TaskAdapter(List<TaskModel> taskList) {
        this.taskList = taskList;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCheckBox;
        ImageButton btnDeleteTask; // Добавлено поле для кнопки удаления задачи

        public TaskViewHolder(View itemView) {
            super(itemView);
            taskCheckBox = itemView.findViewById(R.id.checkBox);
            btnDeleteTask = itemView.findViewById(R.id.btnDeleteTask); // Инициализация кнопки удаления задачи
        }
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {
        TaskModel task = taskList.get(position);
        holder.taskCheckBox.setText(task.getDescription());
        holder.taskCheckBox.setChecked(task.isChecked());

        holder.taskCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setChecked(isChecked);
            if (onCheckedChangeListener != null) {
                onCheckedChangeListener.onCheckedChange(task);
            }
        });

        // Обработчик нажатия на кнопку удаления задачи
        holder.btnDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onDeleteTaskClickListener != null) {
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onDeleteTaskClickListener.onDeleteTaskClick(position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public void addTask(TaskModel task) {
        taskList.add(task);
        notifyItemInserted(taskList.size() - 1);
    }

    public List<TaskModel> getTasks() {
        return taskList;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.onCheckedChangeListener = listener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(TaskModel task);
    }

    public TaskModel getItem(int position) {
        if (position >= 0 && position < taskList.size()) {
            return taskList.get(position);
        }
        return null;
    }


    /*public void removeTask(int position) {
        taskList.remove(position);
        notifyItemRemoved(position);
        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChange();
        }
    }*/

    public void setOnDeleteTaskClickListener(OnDeleteTaskClickListener listener) {
        this.onDeleteTaskClickListener = listener;
    }

    public interface OnDeleteTaskClickListener {
        void onDeleteTaskClick(int position);
    }



}
