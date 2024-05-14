package com.example.taskchecker.services;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.taskchecker.R;
import com.example.taskchecker.activities.WorkSpaceActivity;
import com.example.taskchecker.models.BoardButtonModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class BoardButtonAdapter extends ArrayAdapter<BoardButtonModel> {
    private Context mContext;
    private ArrayList<BoardButtonModel> mBoardButtons;

    public BoardButtonAdapter(Context context, ArrayList<BoardButtonModel> boardButtons) {
        super(context, 0, boardButtons);
        this.mContext = context;
        this.mBoardButtons = boardButtons;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.board_button_item, parent, false);
        }

        final BoardButtonModel currentButton = mBoardButtons.get(position);

        TextView title = listItem.findViewById(R.id.buttonTitle);
        title.setText(currentButton.getTitle());

        // Добавляем обработчик нажатия
        listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String boardId = currentButton.getId(); // Получаем идентификатор доски
                Log.e("BoardId", "BoardId: " + boardId);
                // Вызываем fetchBoard для получения данных о доске
                UserApiService.fetchBoard(mContext, boardId, new UserApiService.Callback() {
                    @Override
                    public void onSuccess(JSONObject boardData) throws JSONException {
                        // Обработка успешного получения данных о доске
                        ((WorkSpaceActivity) mContext).OpenBoard(boardData);

                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Обработка ошибки при получении данных о доске
                        Log.e("BoardButtonAdapter", "Failed to fetch board: " + errorMessage);
                    }
                });
            }
        });


        return listItem;
    }
}
