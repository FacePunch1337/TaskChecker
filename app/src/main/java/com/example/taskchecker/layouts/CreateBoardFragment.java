package com.example.taskchecker.layouts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.taskchecker.R;
import com.example.taskchecker.activities.WorkSpaceActivity;
import com.example.taskchecker.services.UserApiService;

import org.json.JSONException;
import org.json.JSONObject;

public class CreateBoardFragment extends Fragment {

    private ImageButton btnCreateBoard;
    private EditText editTextBoardTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.create_board_panel, container, false);

        btnCreateBoard = rootView.findViewById(R.id.btnCreateBoard);
        editTextBoardTitle = rootView.findViewById(R.id.editTextBoardTitle);

        btnCreateBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCreateBoardClick(v);
            }
        });

        return rootView;
    }

    public void onCreateBoardClick(View view) {
        // Вставьте здесь вашу логику обработки нажатия на кнопку
        String boardTitle = editTextBoardTitle.getText().toString();


       // String boardId = BoardButton.getId();
        // Проверяем, что поле не пустое
        if (!boardTitle.isEmpty()) {
            UserApiService.createNewBoard(requireContext(), boardTitle, new UserApiService.Callback() {
                @Override
                public void onSuccess(JSONObject boardData) throws JSONException {
                    //String boardId = boardData.getString("_id");

                    WorkSpaceActivity.addNewBoardButton(boardTitle);
                    Toast.makeText(requireContext(), "Доска успешно создана", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Вставьте здесь вашу логику для обработки ошибок
                    Toast.makeText(requireContext(), "Ошибка при создании доски: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Поле пустое, выводим Toast с сообщением
            Toast.makeText(requireContext(), "Пожалуйста, введите название доски", Toast.LENGTH_SHORT).show();
        }
    }
}
