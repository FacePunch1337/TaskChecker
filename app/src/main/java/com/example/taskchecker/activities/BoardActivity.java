package com.example.taskchecker.activities;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.taskchecker.R;
import com.example.taskchecker.models.UserModel;
import com.example.taskchecker.services.UserApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

public class BoardActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private ImageButton btnBoardSettings;
    private ImageButton btnDeleteCard;
    private LinearLayout boardLayout;
    private String currentBoardId;
    private String[] columnsId;
    private String draggedFromColumnId;

    private HorizontalScrollView horizontalScrollView;
    private ScrollView verticalScrollView;
    private ImageView executorAvatarImageView;
    private boolean isOverDeleteButton = false;
    private String boardData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_panel);


        btnBack = findViewById(R.id.btnBack);

        btnBoardSettings = findViewById(R.id.btnBoardSettings);
        btnDeleteCard = findViewById(R.id.btnDeleteCard);
        boardLayout = findViewById(R.id.boardLayout);
        horizontalScrollView = findViewById(R.id.horizontalScrollView);
        verticalScrollView = findViewById(R.id.verticalScrollView);

        Intent intent = getIntent();
        if (intent != null) {
            fetchBoardData();

            String boardTitle = intent.getStringExtra("boardTitle");
            // Обновляем TextView
            TextView titleTextView = findViewById(R.id.boardTitle);
            titleTextView.setText(boardTitle);

            // Прочие действия с boardData...
        } else {
            Log.e("BoardActivity", "Intent is null");
            // Обработка ошибки
        }

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем Intent для перехода на BoardActivity
                Intent intent = new Intent(BoardActivity.this, WorkSpaceActivity.class);

                // Запускаем активность
                startActivity(intent);
                // Завершаем текущую активность, если необходимо
                finish();
            }
        });
        btnBoardSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BoardActivity.this, BoardSettingsActivity.class);

                // Если вам нужно передать какие-либо данные в активити настроек доски,
                // используйте методы putExtra() или другие методы Intent для передачи данных
                intent.putExtra("boardId", currentBoardId);
                // Запускаем активити
                startActivity(intent);
            }


        });




    }

    private void fetchBoardData() {

        String boardDataString = getIntent().getStringExtra("boardData");
        if (boardDataString != null) {
            try {
                JSONObject boardData = new JSONObject(boardDataString);
                currentBoardId = boardData.getString("_id");

                JSONArray columnsArray = boardData.getJSONArray("columns");
                columnsId = new String[columnsArray.length()];
                createColumns(boardData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e("BoardError", "Board data is NULL: " + boardDataString);
        }

    }

    private LinearLayout findColumnLayoutById(String columnId) {
        for (int i = 0; i < boardLayout.getChildCount(); i++) {
            View child = boardLayout.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout columnLayout = (LinearLayout) child;
                String tag = (String) columnLayout.getTag();
                if (tag != null && tag.equals(columnId)) {
                    return columnLayout;
                }
            }
        }
        return null;
    }

    private void createColumns(JSONObject boardData) throws JSONException {
        final float scale = getResources().getDisplayMetrics().density;
        int marginPixels = (int) (25 * scale + 0.5f);

        JSONArray columnsArray = boardData.getJSONArray("columns");
        columnsId = new String[columnsArray.length()];
        for (int i = 0; i < columnsArray.length(); i++) {
            JSONObject columnObj = columnsArray.getJSONObject(i);
            String columnTitle = columnObj.getString("title");
            columnsId[i] = columnObj.getString("_id");

            boolean columnExists = false;
            for (int j = 0; j < boardLayout.getChildCount(); j++) {
                View child = boardLayout.getChildAt(j);
                if (child instanceof LinearLayout) {
                    TextView columnNameTextView = (TextView) ((LinearLayout) child).getChildAt(0);
                    if (columnNameTextView.getText().toString().equals(columnTitle)) {
                        columnExists = true;
                        break;
                    }
                }
            }

            if (!columnExists) {
                LinearLayout columnLayout = createColumnLayout(columnObj, i);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(marginPixels, 0, marginPixels, 0);
                columnLayout.setLayoutParams(params);
                boardLayout.addView(columnLayout);
            }
        }
    }

    private LinearLayout createColumnLayout(JSONObject columnObj, final int columnIndex) throws JSONException {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout columnLayout = (LinearLayout) inflater.inflate(R.layout.column_layout, null);

        TextView columnNameTextView = columnLayout.findViewById(R.id.columnNameTextView);
        final EditText addCardEditText = columnLayout.findViewById(R.id.addCardEditText);

        columnNameTextView.setText(columnObj.getString("title"));
        String columnId = columnsId[columnIndex];
        columnLayout.setTag(columnId); // Установка тега колонки
        Log.d("CreateColumnLayout", "Column tag set: " + columnId);
        addCardEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (addCardEditText.getRight() - addCardEditText.getCompoundDrawables()[2].getBounds().width())) {
                        String cardTitle = addCardEditText.getText().toString().trim();
                        if (!cardTitle.isEmpty()) {
                            String boardId = currentBoardId;
                            String columnId = columnsId[columnIndex];

                            UserApiService.createNewCard(BoardActivity.this, boardId, columnId, cardTitle, new UserApiService.Callback() {
                                @Override
                                public void onSuccess(JSONObject response) throws JSONException {

                                    Log.d("CreateCard", "Card created successfully: " + response.toString());
                                    String cardId = response.getJSONObject("newCard").getString("_id");
                                    RelativeLayout newCardButton = createCardButton(cardTitle, cardId);
                                    LinearLayout columnLayout = findColumnLayoutById(columnId);
                                    if (columnLayout != null) {
                                        LinearLayout cardsLayout = columnLayout.findViewById(R.id.cardsLayout);
                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                                LinearLayout.LayoutParams.MATCH_PARENT,
                                                LinearLayout.LayoutParams.WRAP_CONTENT
                                        );
                                        final float scale = getResources().getDisplayMetrics().density;
                                        int verticalMarginPixels = (int) (5 * scale + 0.5f);
                                        params.setMargins(0, verticalMarginPixels, 0, verticalMarginPixels);
                                        newCardButton.setLayoutParams(params);

                                        // Установка прогресс-бара
                                        ProgressBar progressBar = newCardButton.findViewById(R.id.progressBar);
                                        progressBar.setProgress(0); // Здесь установите прогресс по вашим требованиям

                                        cardsLayout.addView(newCardButton);
                                        addCardEditText.setText("");
                                        fetchBoardData();
                                    } else {
                                        fetchBoardData();
                                        Log.e("CreateCard", "Column layout not found for columnId: " + columnId);
                                        Toast.makeText(BoardActivity.this, "Failed to add card: Column not found", Toast.LENGTH_SHORT).show();
                                    }
                                }


                                @Override
                                public void onFailure(String errorMessage) {
                                    Log.e("CreateCard", "Failed to create card: " + errorMessage);
                                    Toast.makeText(BoardActivity.this, "Failed to create card: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(BoardActivity.this, "Please enter a title for the card", Toast.LENGTH_SHORT).show();
                        }

                        return true;
                    }
                }
                return false;
            }
        });

        createCards(columnObj.getJSONArray("cards"), (LinearLayout) columnLayout.findViewById(R.id.cardsLayout));

        columnLayout.setOnDragListener(new OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_LOCATION:
                        float x = event.getX();
                        float y = event.getY();

                        // Получаем размеры ScrollView
                        int scrollViewWidth = verticalScrollView.getWidth();
                        int scrollViewHeight = verticalScrollView.getHeight();

                        // Вычисляем допустимый порог для скроллинга
                        int scrollHorizontalThreshold = scrollViewWidth / 10;
                        int scrollVerticaThreshold = scrollViewHeight / 10;

                        // Определяем, нужно ли скроллить вверх или вниз
                        if (y < scrollVerticaThreshold) {
                            // Скроллим вверх
                            verticalScrollView.smoothScrollBy(0, -100);
                        } else if (y > scrollViewHeight - scrollVerticaThreshold) {
                            // Скроллим вниз
                            verticalScrollView.smoothScrollBy(0, 100);
                        } else if (x < scrollHorizontalThreshold) {
                            // Скроллим вверх
                            verticalScrollView.smoothScrollBy(-100, 0);
                        } else if (x > scrollViewWidth - scrollHorizontalThreshold) {
                            // Скроллим вниз
                            verticalScrollView.smoothScrollBy(100, 0);
                        }
                        return true;
                    case DragEvent.ACTION_DRAG_STARTED:
                        btnDeleteCard.setOnDragListener(new DeleteButtonDragListenerService(currentBoardId, draggedFromColumnId));
                        return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);

                    case DragEvent.ACTION_DRAG_ENTERED:
                        // v.setBackgroundColor(0x30000000);  // Change background color to indicate entering
                        v.invalidate();
                        return true;

                    case DragEvent.ACTION_DRAG_EXITED:
                        // v.setBackgroundColor(0x00000000);  // Revert background color to original
                        v.invalidate();
                        return true;

                    case DragEvent.ACTION_DROP:
                        ClipData.Item item = event.getClipData().getItemAt(0);
                        String cardId = item.getText().toString();

                        View draggedView = (View) event.getLocalState();
                        ViewGroup parent = (ViewGroup) draggedView.getParent();

                        if (parent == null) {
                            Log.e("DragDrop", "Parent view is null");
                            return false;
                        }

                        String toColumnId = (String) v.getTag();

                        if (draggedFromColumnId == null || toColumnId == null) {
                            Log.e("DragDrop", "Column ID is null");
                            return false;
                        }

                        if (!draggedFromColumnId.equals(toColumnId)) {
                            moveCard(cardId, draggedFromColumnId, toColumnId);
                        }
                        parent.removeView(draggedView);
                        LinearLayout container = (LinearLayout) v.findViewById(R.id.cardsLayout);
                        container.addView(draggedView);

                        draggedView.setTag(cardId);

                        //  v.setBackgroundColor(0x00000000);  // Revert background color to original
                        v.invalidate();
                        return true;

                    case DragEvent.ACTION_DRAG_ENDED:
                        if (!event.getResult()) {
                            View draggedViewEnded = (View) event.getLocalState();
                            draggedViewEnded.setVisibility(View.VISIBLE);
                        }
                        //   v.setBackgroundColor(0x00000000);  // Revert background color to original
                        v.invalidate();
                        return true;

                    default:
                        break;
                }
                return true;
            }
        });

        return columnLayout;
    }

    private void moveCard(String cardId, String fromColumnId, String toColumnId) {
        try {
            Log.d("MoveCard", "Attempting to move card with ID: " + cardId + " from column: " + fromColumnId + " to column: " + toColumnId);

            UserApiService.moveCard(BoardActivity.this, currentBoardId, fromColumnId, cardId, toColumnId, new UserApiService.Callback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Log.d("MoveCard", "Card moved successfully: " + response.toString());
                    //Toast.makeText(BoardActivity.this, "Card moved successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("MoveCard", "Failed to move card: " + errorMessage);
                    //Toast.makeText(BoardActivity.this, "Failed to move card: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e("MoveCardException", "Exception occurred while moving card", e);
            Toast.makeText(BoardActivity.this, "An error occurred while moving the card. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Изменим метод createCards
    private void createCards(JSONArray cardsArray, LinearLayout cardsLayout) throws JSONException {
        for (int i = 0; i < cardsArray.length(); i++) {
            JSONObject cardObj = cardsArray.getJSONObject(i);
            String cardTitle = cardObj.getString("title");
            String cardId = cardObj.getString("_id");

            RelativeLayout cardButton = createCardButton(cardTitle, cardId);
            cardButton.setTag(cardId);

            // Получаем id доски и колонки для текущей карточки
            String boardId = currentBoardId;
            LinearLayout parentColumn = (LinearLayout) cardsLayout.getParent();
            String columnId = (String) parentColumn.getTag();

            // Делаем запрос для получения деталей карточки
            UserApiService.fetchCardDetails(BoardActivity.this, boardId, columnId, cardId, new UserApiService.Callback() {
                @Override
                public void onSuccess(JSONObject userData) throws JSONException {
                    JSONObject card = userData.getJSONObject("card");
                    String executorId = card.getString("executor");
                    String startDate = card.getString("startDate");
                    String endDate = card.getString("endDate");

                    // Находим ImageView для аватарки текущей карточки
                    ImageView executorAvatarImageView = cardButton.findViewById(R.id.executorAvatarImageView);
                    TextView startDateTextView = cardButton.findViewById(R.id.startDateTextView);
                    TextView endDateTextView = cardButton.findViewById(R.id.endDateTextView);
                    loadDate(columnId, cardId, startDate, endDate, startDateTextView, endDateTextView);
                    // Загружаем аватарку исполнителя
                    loadExecutorAvatar(executorId, executorAvatarImageView);
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Обработка ошибки
                }
            });

            // Получаем список задач для текущей карточки
            UserApiService.getTaskList(BoardActivity.this, boardId, columnId, cardId, new UserApiService.TaskIdCallback() {
                @Override
                public void onSuccessWithTaskIds(JSONObject response, List<String> taskIds) {
                    try {
                        int totalTasks = taskIds.size();
                        int completedTasks = 0;

                        // Подсчет выполненных задач
                        JSONArray tasksArray = response.getJSONArray("tasks");
                        for (int i = 0; i < tasksArray.length(); i++) {
                            JSONObject taskObject = tasksArray.getJSONObject(i);
                            if (taskObject.getBoolean("checked")) {
                                completedTasks++;
                            }
                        }

                        // Вычисление прогресса
                        int progress = totalTasks > 0 ? (completedTasks * 100 / totalTasks) : 0;

                        // Обновление прогресс-бара
                        ProgressBar progressBar = cardButton.findViewById(R.id.progressBar);
                        progressBar.setProgress(progress);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(BoardActivity.this, "Error processing task list", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e("GetTaskList", "Failed to get task list: " + errorMessage);
                    Toast.makeText(BoardActivity.this, "Failed to get task list: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

            // Устанавливаем параметры и добавляем карточку в layout
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            final float scale = getResources().getDisplayMetrics().density;
            int verticalMarginPixels = (int) (5 * scale + 0.5f);
            params.setMargins(0, verticalMarginPixels, 0, verticalMarginPixels);
            cardButton.setLayoutParams(params);

            cardsLayout.addView(cardButton);
        }
    }

    private void loadDate(String columnId, String cardId, String startDate, String endDate, TextView startDateTextView, TextView endDateTextView){
        UserApiService.updateDeadline(BoardActivity.this, currentBoardId, columnId, cardId, startDate, endDate, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject userData) throws JSONException {
                JSONObject cardData = userData.getJSONObject("card");
                String startDate = cardData.getString("startDate");
                String endDate = cardData.getString("endDate");
                startDateTextView.setText(startDate);
                endDateTextView.setText(endDate);
            }

            @Override
            public void onFailure(String errorMessage) {

            }
        });
    }
    // Метод для загрузки аватарки исполнителя
    private void loadExecutorAvatar(String executorId, ImageView avatarImageView) {
        UserApiService.fetchUserById(BoardActivity.this, executorId, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject userData) throws JSONException {
                String avatarUrl = userData.getString("avatarURL");
                Glide.with(BoardActivity.this).load(avatarUrl).into(avatarImageView);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Обработка ошибки
            }
        });
    }



    private RelativeLayout createCardButton(String cardTitle, String cardId) {
        LayoutInflater inflater = getLayoutInflater();
        //TextView cardButton = (TextView) inflater.inflate(R.layout.card_layout, null);
        RelativeLayout cardButton = (RelativeLayout) inflater.inflate(R.layout.card_layout,null);
        TextView cardText = cardButton.findViewById(R.id.cardTitleTextView);
        executorAvatarImageView = cardButton.findViewById(R.id.executorAvatarImageView);
        executorAvatarImageView = cardButton.findViewById(R.id.executorAvatarImageView);
        cardText.setText(cardTitle);
        cardButton.setTag(cardId);

        cardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the parent column layout
                LinearLayout parentColumn = (LinearLayout) v.getParent().getParent();
                // Fetch the columnId from the parent column's tag
                String columnId = (String) parentColumn.getTag();

                // Create an intent to start the CardMenuActivity
                Intent intent = new Intent(BoardActivity.this, CardMenuActivity.class);
                intent.putExtra("cardId", cardId);
                intent.putExtra("columnId", columnId); // Pass the columnId
                intent.putExtra("boardId", currentBoardId);
                intent.putExtra("cardTitle", cardTitle);
                //intent.putExtra("boardData", boardData);
                startActivity(intent);
            }
        });

        cardButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Object tag = v.getTag();
                if (tag == null) {
                    Log.e("onLongClick", "View tag is null");
                    return false;
                }

                ClipData.Item item = new ClipData.Item((CharSequence) tag);
                String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                ClipData dragData = new ClipData(tag.toString(), mimeTypes, item);
                View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);

                LinearLayout parentColumn = (LinearLayout) v.getParent().getParent();
                draggedFromColumnId = (String) parentColumn.getTag();

                v.startDragAndDrop(dragData, myShadow, v, 0);
                btnDeleteCard.setVisibility(View.VISIBLE);
                return true;
            }
        });

        return cardButton;
    }


    private class DeleteButtonDragListenerService implements OnDragListener {
        private String boardId;
        private String columnId;

        public DeleteButtonDragListenerService(String boardId, String columnId) {
            this.boardId = boardId;
            this.columnId = columnId;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;

                case DragEvent.ACTION_DRAG_ENTERED:
                    ((ImageButton) v).setImageResource(R.drawable.bucketiconinteract);
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:
                    ((ImageButton) v).setImageResource(R.drawable.bucketicon);
                    return true;

                case DragEvent.ACTION_DROP:
                    View draggedView = (View) event.getLocalState();
                    String cardId = (String) draggedView.getTag();

                    UserApiService.deleteCard(BoardActivity.this, boardId, columnId, cardId, new UserApiService.Callback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            Log.d("DeleteCard", "Card deleted successfully: " + response.toString());
                            ((ViewGroup) draggedView.getParent()).removeView(draggedView);
                            //Toast.makeText(BoardActivity.this, "Card deleted successfully", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            Log.d("DeleteCardData", "DeleteCardData: " + boardId + "," + columnId);
                            Log.e("DeleteCard", "Failed to delete card: " + errorMessage);
                            //Toast.makeText(BoardActivity.this, "Failed to delete card: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });

                    return true;

                case DragEvent.ACTION_DRAG_ENDED:
                    ((ImageButton) v).setImageResource(R.drawable.bucketicon);
                    btnDeleteCard.setVisibility(View.GONE);
                    return true;

                default:
                    break;
            }
            return false;
        }
    }
}