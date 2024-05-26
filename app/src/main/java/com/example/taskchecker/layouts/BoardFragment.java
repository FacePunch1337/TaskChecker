package com.example.taskchecker.layouts;

import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.taskchecker.R;
import com.example.taskchecker.activities.WorkSpaceActivity;
import com.example.taskchecker.models.BoardModel;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BoardFragment extends Fragment {

    private ImageButton btnBack;
    private Fragment boardFragment;
    private LinearLayout boardLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.board_panel, container, false);
        btnBack = rootView.findViewById(R.id.btnBack);
        boardLayout = rootView.findViewById(R.id.boardLayout);
        // Получение данных о доске из полученного JSON
        WorkSpaceActivity activity = (WorkSpaceActivity) getActivity();
        if (activity != null) {
            boardFragment = activity.getSupportFragmentManager().findFragmentById(R.id.fragmentBoardView);
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (activity != null) {


                    activity.getSupportFragmentManager().beginTransaction().hide(boardFragment).commit();
                    WorkSpaceActivity.titleTextView.setText("Boards");
                }
            }
        });


        return rootView;
    }

    private void createColumns(JSONObject boardData) throws JSONException {
        // Перевод dp в пиксели
        final float scale = getResources().getDisplayMetrics().density;
        int marginPixels = (int) (10 * scale + 0.5f); // 8dp в пикселях

        JSONArray columnsArray = boardData.getJSONArray("columns");
        for (int i = 0; i < columnsArray.length(); i++) {
            JSONObject columnObj = columnsArray.getJSONObject(i);
            String columnTitle = columnObj.getString("title");

            // Проверяем, содержится ли колонка с таким же заголовком уже в boardLayout
            boolean columnExists = false;
            for (int j = 0; j < boardLayout.getChildCount(); j++) {
                View child = boardLayout.getChildAt(j);
                if (child instanceof LinearLayout) {
                    TextView columnNameTextView = (TextView) ((LinearLayout) child).getChildAt(0); // Первый дочерний элемент - TextView с названием колонки
                    if (columnNameTextView.getText().toString().equals(columnTitle)) {
                        columnExists = true;
                        break;
                    }
                }
            }

            // Если колонка не существует, то создаем её
            if (!columnExists) {
                LinearLayout columnLayout = createColumnLayout(columnObj);

                // Устанавливаем маржу для столбца
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(marginPixels, 0, marginPixels, 0); // Устанавливаем маржу для столбца
                columnLayout.setLayoutParams(params);

                boardLayout.addView(columnLayout);
            }
        }
    }





    private LinearLayout createColumnLayout(JSONObject columnObj) throws JSONException {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        LinearLayout columnLayout = (LinearLayout) inflater.inflate(R.layout.column_layout, null);

        TextView columnNameTextView = columnLayout.findViewById(R.id.columnNameTextView);

        columnNameTextView.setText(columnObj.getString("title"));

        createCards(columnObj.getJSONArray("cards"), (LinearLayout) columnLayout.findViewById(R.id.cardsLayout));

        return columnLayout;
    }
    private TextView createColumnNameTextView(String columnName) {
        TextView columnNameTextView = new TextView(getContext());
        columnNameTextView.setText(columnName);
        return columnNameTextView;
    }

    private void createCards(JSONArray cardsArray, LinearLayout columnLayout) throws JSONException {
        // Перевод dp в пиксели
        final float scale = getResources().getDisplayMetrics().density;
        int verticalMarginPixels = (int) (5 * scale + 0.5f); // Вертикальный отступ в пикселях

        for (int j = 0; j < cardsArray.length(); j++) {
            JSONObject cardObj = cardsArray.getJSONObject(j);
            TextView cardButton = createCardButton(cardObj.getString("title"));

            // Установка параметров макета для каждой карточки
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, verticalMarginPixels, 0, verticalMarginPixels); // Устанавливаем вертикальный отступ
            cardButton.setLayoutParams(params);

            columnLayout.addView(cardButton);
        }
    }


    private TextView createCardButton(String cardTitle) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        TextView cardButton = (TextView) inflater.inflate(R.layout.card_layout, null);
        cardButton.setText(cardTitle);
        return cardButton;
    }


    public void GetBoardData(JSONObject boardData) {
        boardLayout.removeAllViews();
            if (boardData != null) {

               // String boardDataJson = boardData.toString();
                //boardDataJson = StringEscapeUtils.unescapeJava(boardDataJson);
                Log.d("BoardData", "JSONObject: " + boardData);
                try {
                    //JSONObject boardData = new JSONObject(boardDataJson);
                    // Ваши действия с данными о доске здесь

                    createColumns(boardData);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                // Обработка ситуации, когда данные доски недоступны
                Log.e("BoardError", "Board data is NULL: " + boardData);

            }



    }


}
