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
        // Inflate the layout for this fragment
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

       //String boardDataJson = "{\"_id\":\"66427985621e1e0bc857ea2d\",\"title\":\"BoardName\",\"owner\":\"663c227dc9d78e74f6fa0e42\",\"columns\":[{\"title\":\"Backlog\",\"cards\":[{\"title\":\"NewTask\",\"_id\":\"66427a56621e1e0bc857ea4c\"},{\"title\":\"NewTask2\",\"_id\":\"66427a68621e1e0bc857ea5b\"},{\"title\":\"NewTask3\",\"_id\":\"66427a77621e1e0bc857ea6c\"}],\"_id\":\"66427985621e1e0bc857ea2e\"},{\"title\":\"InProgress\",\"cards\":[],\"_id\":\"66427985621e1e0bc857ea2f\"},{\"title\":\"Release\",\"cards\":[],\"_id\":\"66427985621e1e0bc857ea30\"}],\"__v\":3}";

        return rootView;
    }

    private void createColumns(JSONObject boardData) throws JSONException {
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
                boardLayout.addView(columnLayout);
            }
        }
    }




    private LinearLayout createColumnLayout(JSONObject columnObj) throws JSONException {
        LinearLayout columnLayout = new LinearLayout(getContext());
        columnLayout.setOrientation(LinearLayout.VERTICAL);
        TextView columnNameTextView = createColumnNameTextView(columnObj.getString("title"));
        columnLayout.addView(columnNameTextView);
        createCards(columnObj.getJSONArray("cards"), columnLayout);
        return columnLayout;
    }

    private TextView createColumnNameTextView(String columnName) {
        TextView columnNameTextView = new TextView(getContext());
        columnNameTextView.setText(columnName);
        return columnNameTextView;
    }

    private void createCards(JSONArray cardsArray, LinearLayout columnLayout) throws JSONException {
        for (int j = 0; j < cardsArray.length(); j++) {
            JSONObject cardObj = cardsArray.getJSONObject(j);
            Button cardButton = createCardButton(cardObj.getString("title"));
            columnLayout.addView(cardButton);
        }
    }

    private Button createCardButton(String cardTitle) {
        Button cardButton = new Button(getContext());
        cardButton.setText(cardTitle);
        return cardButton;
    }

    public void GetBoardData(JSONObject boardData) {

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
