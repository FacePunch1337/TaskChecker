package com.example.taskchecker.activities;



import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.taskchecker.R;
import com.example.taskchecker.models.CommentModel;
import com.example.taskchecker.models.MemberModel;
import com.example.taskchecker.models.TaskModel;
import com.example.taskchecker.models.UserModel;
import com.example.taskchecker.services.TaskAdapter;
import com.example.taskchecker.services.UserApiService;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import io.socket.client.Socket;

public class CardMenuActivity extends AppCompatActivity {


    private String cardTitle;
    private TextView taskTitleTextView;

    private EditText descriptionEditText;
    private MemberModel currentExecuter;
    private LinearLayout includedExecutersView;
    private LinearLayout parentLayout;
    private View dimView;
    private ScrollView scrollView;
    private ScrollView executorsScrollView;

    private ImageButton btnBack;
    private ImageButton btnCloseWindow;
    private String boardId;
    private String columnId;
    private String cardId;

    private TextView btnSelectExecuter;
    private TextView currentDateAndTimeTextView;
    private TextView deadLineTextView;
    private RecyclerView recyclerViewTasks;
    private ProgressBar progressBarTasks;
    private TaskAdapter taskAdapter;
    private CommentAdapter commentAdapter;
    private List<TaskModel> taskList = new ArrayList<>();
    private List<CommentModel> commentList = new ArrayList<>();

    private LinearLayout commentsView;
    private EditText commentEditText;
    private ShapeableImageView profileAvatarCommentView;
    private Button sendHelloButton;
    private Socket socket;
    private String initialDescription;
    private ArrayList<TaskModel> initialTaskList;
    private String initialStartDateTime;
    private String initialDeadlineDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.card_menu_activity);

        // Получение данных из Intent
        Intent intent = getIntent();
        boardId = intent.getStringExtra("boardId");
        columnId = intent.getStringExtra("columnId");
        cardId = intent.getStringExtra("cardId");
        cardTitle = intent.getStringExtra("cardTitle");
        btnBack = findViewById(R.id.btnBack);
        // Инициализация UI элементов
        taskTitleTextView = findViewById(R.id.taskTitleTextView);
        taskTitleTextView.setText(cardTitle);
        includedExecutersView = findViewById(R.id.included_executers_view);
        parentLayout = findViewById(R.id.parentLayout);
        dimView = findViewById(R.id.dim_view);
        btnSelectExecuter = findViewById(R.id.btnSelectExecuter);
        btnCloseWindow = includedExecutersView.findViewById(R.id.btnCloseWindow);
        currentDateAndTimeTextView = findViewById(R.id.currentDateAndTime);
        deadLineTextView = findViewById(R.id.deadLineTextView);
        scrollView = findViewById(R.id.scrollView);
        executorsScrollView = findViewById(R.id.executorsScrollView);
        recyclerViewTasks = findViewById(R.id.recyclerView);
        progressBarTasks = findViewById(R.id.progressBar);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        TextView btnAddTask = findViewById(R.id.btnAddTask);
        CheckBox checkBox = findViewById(R.id.checkBox);

        taskAdapter = new TaskAdapter(taskList);
        commentAdapter = new CommentAdapter((Context) CardMenuActivity.this, (ArrayList<CommentModel>) commentList);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);
        commentsView = findViewById(R.id.commentsView);
        commentEditText = findViewById(R.id.commentEditText);
        profileAvatarCommentView = findViewById(R.id.avatarCommentView);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAndClose();
                //onBackPressed();
            }

        });

        taskAdapter.setOnCheckedChangeListener(task -> {
            // Передаем обновленное значение чекбокса на сервер
            UserApiService.updateTaskList(CardMenuActivity.this, boardId, columnId, cardId, task, new UserApiService.Callback() {
                @Override
                public void onSuccess(JSONObject response) {
                    // Обработка успешного обновления задач на сервере
                    updateProgressBar();
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Обработка ошибки при обновлении задач на сервере
                    Toast.makeText(CardMenuActivity.this, "Failed to update tasks: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        //taskAdapter.setOnCheckedChangeListener(() -> updateProgressBar());

        // Обработчик кнопки добавления задачи
        btnAddTask.setOnClickListener(v -> {
            // Создаем диалоговое окно для ввода имени задачи
            AlertDialog.Builder builder = new AlertDialog.Builder(CardMenuActivity.this);
            builder.setTitle("Enter task name");

            // Поле ввода имени задачи в диалоговом окне
            final EditText input = new EditText(CardMenuActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String taskName = input.getText().toString().trim();
                    if (!taskName.isEmpty()) {
                        // Проверяем, есть ли уже такая задача на карточке
                        boolean taskExists = false;
                        for (TaskModel task : taskList) {
                            if (task.getDescription().equals(taskName)) {
                                taskExists = true;
                                break;
                            }
                        }

                        if (!taskExists) {
                            // Если задачи с таким именем еще нет на карточке, добавляем ее
                            TaskModel newTask = new TaskModel(taskName, false);
                            List<TaskModel> tasks = new ArrayList<>(taskList); // Создаем копию списка задач
                            tasks.add(newTask);

                            // Отправляем запрос на сервер только для добавления новой задачи
                            UserApiService.addNewTask(CardMenuActivity.this, boardId, columnId, cardId, newTask, new UserApiService.Callback() {
                                @Override
                                public void onSuccess(JSONObject response) throws JSONException {
                                    // Обрабатываем успешное добавление задачи
                                    taskAdapter.addTask(newTask);
                                    updateProgressBar();
                                }

                                @Override
                                public void onFailure(String errorMessage) {
                                    // Обрабатываем ошибку при добавлении задачи
                                    Toast.makeText(CardMenuActivity.this, "Failed to add task: " + errorMessage, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            // Если задача с таким именем уже есть на карточке, выводим сообщение об ошибке
                            Toast.makeText(CardMenuActivity.this, "Task already exists", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(CardMenuActivity.this, "Please enter task name", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            // Обработчик нажатия на кнопку "Отмена"
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // Показываем диалоговое окно для ввода имени задачи
            builder.show();
        });

        // Удаление задачи
         taskAdapter.setOnDeleteTaskClickListener(position -> {
            TaskModel deletedTask = taskList.get(position);
            UserApiService.deleteTask(CardMenuActivity.this, boardId, columnId, cardId, deletedTask.getTaskId(), new UserApiService.Callback() {
                @Override
                public void onSuccess(JSONObject response) throws JSONException {
                    // Обработка успешного удаления задачи
                    taskList.remove(position);
                    taskAdapter.notifyItemRemoved(position);
                    updateProgressBar();
                }

                @Override
                public void onFailure(String errorMessage) {
                    // Обработка ошибки при удалении задачи
                    Toast.makeText(CardMenuActivity.this, "Failed to delete task: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });
        // Обработчики других UI элементов
        btnSelectExecuter.setOnClickListener(v -> {
            includedExecutersView.setVisibility(View.VISIBLE);
            dimView.setVisibility(View.VISIBLE);
        });
        btnCloseWindow.setOnClickListener(v -> {
            includedExecutersView.setVisibility(View.GONE);
            dimView.setVisibility(View.GONE);
        });
        View btnTimer = findViewById(R.id.btnTimer);
        btnTimer.setOnClickListener(v -> showDateTimePicker(currentDateAndTimeTextView, deadLineTextView));


        if(!isDestroyed()){
            Glide.with(this).load(UserModel.getAvatarURL()).into(profileAvatarCommentView);
            commentEditText.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        // Проверка, находится ли касание внутри drawableEnd
                        if (event.getRawX() >= (commentEditText.getRight() - commentEditText.getCompoundDrawables()[2].getBounds().width())) {
                            // Здесь вызываем метод добавления комментария
                            String commentText = commentEditText.getText().toString();
                            if (!commentText.isEmpty()) {
                                String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                                MemberModel memberModel = new MemberModel(UserModel.get_id(), UserModel.get_username(), UserModel.getAvatarURL());
                                addComment(memberModel, currentTime, commentText);
                                commentEditText.setText(""); // Очистить поле ввода

                                // Прокрутить ScrollView вниз с небольшой задержкой
                                scrollView.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        scrollView.fullScroll(View.FOCUS_DOWN);
                                    }
                                }, 100);

                                // Вызов метода отправки комментария на сервер

                                UserApiService.sendComment(CardMenuActivity.this, boardId, columnId, cardId, UserModel.get_id(), commentText, currentTime, new UserApiService.JSONArraysCallback() {
                                    @Override
                                    public void onSuccess(JSONArray response) {
                                        Log.d("COMMENT", "COMMENT" + response);
                                        // Обработка успешного ответа сервера, если необходимо
                                    }

                                    @Override
                                    public void onFailure(String errorMessage) {
                                        Log.d("COMMENT", "onFailure" + errorMessage);
                                    }
                                });
                            }
                            return true;
                        }
                    }
                    return false;
                }
            });

        }
        fetchTaskList();
        fetchComments();
        fetchCardData();
        fetchBoardMembers(boardId);



    }


    private void fetchTaskList() {
        UserApiService.getTaskList(this, boardId, columnId, cardId, new UserApiService.ListCallback() {


            @Override
            public void onSuccessWithItemIds(JSONObject userData, List<String> itemIds) throws JSONException {
                JSONArray tasksArray = userData.getJSONArray("tasks");
                taskList.clear(); // Очистить текущий список задач
                for (int i = 0; i < tasksArray.length(); i++) {
                    JSONObject taskObject = tasksArray.getJSONObject(i);
                    String taskId = itemIds.get(i); // Получаем _id из списка taskIds
                    TaskModel task = new TaskModel(
                            taskObject.getString("description"),
                            taskObject.getBoolean("checked")
                    );
                    task.setTaskId(taskId); // Устанавливаем _id для задачи
                    taskList.add(task);
                }
                // Обновить адаптер
                taskAdapter.notifyDataSetChanged();
                updateProgressBar();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CardMenuActivity.this, "Failed to load tasks: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    // Получаем комментарии с сервера
    private void fetchComments() {
        UserApiService.fetchComments(this, boardId, columnId, cardId, new UserApiService.ListCallback() {

            @Override
            public void onSuccessWithItemIds(JSONObject userData, List<String> itemIds) throws JSONException {
                JSONArray commentsArray = userData.getJSONArray("comments");
                commentList.clear();

                List<CommentModel> tempComments = new ArrayList<>();

                // Проходимся по всем комментариям
                for (int i = 0; i < commentsArray.length(); i++) {
                    JSONObject commentObject = commentsArray.getJSONObject(i);
                    String commentId = itemIds.get(i); // Получаем _id из списка taskIds
                    CommentModel comment = new CommentModel(
                            commentObject.getString("text"),
                            commentObject.getString("memberId"),
                            commentObject.getString("time"),
                            Integer.parseInt(commentObject.getString("index")) // Преобразуем индекс из строки в число
                    );
                    comment.setCommentId(commentId); // Устанавливаем _id для комментария
                    tempComments.add(comment);
                }

                // Сортируем комментарии по индексу
                Collections.sort(tempComments, (c1, c2) -> Integer.compare(c1.getIndex(), c2.getIndex()));

                // Создаём ConcurrentHashMap для хранения комментариев
                Map<Integer, View> commentViewsMap = new ConcurrentHashMap<>();

                // Счётчик завершённых запросов
                final int[] completedRequests = {0};
                final int totalRequests = tempComments.size();

                // После сортировки комментариев по индексу, мы вызываем метод для отображения каждого комментария
                for (CommentModel comment : tempComments) {
                    UserApiService.fetchUserById(CardMenuActivity.this, comment.getMemberId(), new UserApiService.Callback() {

                        @Override
                        public void onSuccess(JSONObject userData) throws JSONException {
                            // Создаём и добавляем представление комментария в Map
                            View commentView = createCommentView(userData.getString("username"), userData.getString("avatarURL"), comment.getTime(), comment.getText());
                            commentViewsMap.put(comment.getIndex(), commentView);

                            // Увеличиваем счётчик завершённых запросов
                            completedRequests[0]++;

                            // Проверяем, все ли запросы завершены
                            if (completedRequests[0] == totalRequests) {
                                // Отображаем все комментарии в правильном порядке
                                displayComments(commentViewsMap);
                            }
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            // Обработка ошибки
                            completedRequests[0]++;

                            // Проверяем, все ли запросы завершены
                            if (completedRequests[0] == totalRequests) {
                                // Отображаем все комментарии в правильном порядке
                                displayComments(commentViewsMap);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CardMenuActivity.this, "Failed to load comments: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод для создания представления комментария
    private View createCommentView(String username, String avatarURL, String time, String text) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View commentView = inflater.inflate(R.layout.comment_view, null, false);

        ShapeableImageView userCommentAvatarUrl = commentView.findViewById(R.id.avatarCommentImageView);
        TextView userCommentUsername = commentView.findViewById(R.id.userCommentUsername);
        TextView userCommentTime = commentView.findViewById(R.id.userCommentTime);
        TextView commentText = commentView.findViewById(R.id.commentText);

        Glide.with(this).load(avatarURL).into(userCommentAvatarUrl);
        userCommentUsername.setText(username);
        userCommentTime.setText(time);
        commentText.setText(text);

        // Устанавливаем ширину commentView на match_parent
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 20, 0, 20); // отступы сверху и снизу
        commentView.setLayoutParams(layoutParams);

        return commentView;
    }

    // Метод для отображения комментариев в правильном порядке
    private void displayComments(Map<Integer, View> commentViewsMap) {
        LinearLayout commentsView = findViewById(R.id.commentsView);
        commentsView.removeAllViews(); // Очищаем предыдущие комментарии

        // Сортируем ключи и добавляем представления в LinearLayout в правильном порядке
        List<Integer> sortedKeys = new ArrayList<>(commentViewsMap.keySet());
        Collections.sort(sortedKeys);

        for (Integer key : sortedKeys) {
            View commentView = commentViewsMap.get(key);
            commentsView.addView(commentView);
        }

        // Обновляем адаптер
        commentAdapter.notifyDataSetChanged();
    }





    private void showDateTimePicker(TextView currentDateAndTimeTextView, TextView deadLineTextView) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        // Вывод текущего времени в формате "Start: {date} - {time}"
        String currentDateTime = sdf.format(calendar.getTime());
        currentDateAndTimeTextView.setText(currentDateTime);

        DatePickerDialog datePickerDialog = new DatePickerDialog(CardMenuActivity.this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    TimePickerDialog timePickerDialog = new TimePickerDialog(CardMenuActivity.this,
                            (view1, hourOfDay, minute) -> {
                                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                calendar.set(Calendar.MINUTE, minute);

                                String selectedDateTime = sdf.format(calendar.getTime());

                                // Разделение на дату и время
                                String[] dateTimeParts = selectedDateTime.split(" ");
                                String selectedDate = dateTimeParts[0];
                                String selectedTime = dateTimeParts[1];

                                // Получение текущей даты и времени
                                Calendar currentCalendar = Calendar.getInstance();
                                String currentDateTime1 = sdf.format(currentCalendar.getTime());
                                String[] currentDateTimeParts = currentDateTime1.split(" ");
                                String currentDate = currentDateTimeParts[0];
                                String currentTime = currentDateTimeParts[1];

                                // Вывод выбранного пользователем времени в формате "Deadline: {date} - {time}"
                                deadLineTextView.setText(selectedDateTime);

                                // Вычисление разницы во времени
                                try {
                                    // Преобразование выбранного времени в объект Date
                                    Date endDateTime = sdf.parse(selectedDateTime);
                                    Date currentDateTimeObj = new Date();

                                    // Расчет разницы во времени в миллисекундах
                                    long diffInMillis = endDateTime.getTime() - currentDateTimeObj.getTime();

                                    // Перевод разницы из миллисекунд в дни
                                    long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

                                    // Установка цвета текста в зависимости от разницы во времени
                                    if (diffInDays > 1) {
                                        deadLineTextView.setTextColor(getResources().getColor(R.color.green));
                                    } else {
                                        deadLineTextView.setTextColor(getResources().getColor(R.color.red));
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    // Обработка ошибки при преобразовании времени
                                    deadLineTextView.setTextColor(getResources().getColor(R.color.black)); // Устанавливаем черный цвет в случае ошибки
                                }

                                // Обновление времени выполнения карточки на сервере
                                updateDeadline(currentDate, currentTime, selectedDate, selectedTime);
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }




    private void updateDeadline(String currentStartDate, String currentStartTime, String selectedEndDate, String selectedEndTime) {
        // Проверка наличия текущего исполнителя
       // String executorId = currentExecuter != null ? currentExecuter.getId() : null;

        // Склейка текущей даты и времени для startDate
        String startDate = currentStartDate + " " + currentStartTime;
        // Склейка выбранной даты и времени для endDate
        String endDate = selectedEndDate + " " + selectedEndTime;



        UserApiService.updateDeadline(this, boardId, columnId, cardId, startDate, endDate, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {

                // Обработка успешного обновления данных карточки
                //Toast.makeText(CardMenuActivity.this, "Данные карточки успешно обновлены", Toast.LENGTH_SHORT).show();
                Log.d("UpdateCardData", "Карточка успешно обновлена: " + response.toString());
            }

            @Override
            public void onFailure(String errorMessage) {
                // Обработка ошибки при обновлении данных карточки
                Toast.makeText(CardMenuActivity.this, "Не удалось обновить данные карточки: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("UpdateCardData", "Ошибка при обновлении данных карточки: " + errorMessage);
            }
        });
    }

    private void updateProgressBar() {
        int totalTasks = taskList.size();
        if (totalTasks == 0) {
            progressBarTasks.setProgress(0);
            return;
        }
        int completedTasks = 0;
        for (TaskModel task : taskList) {
            if (task.isChecked()) {
                completedTasks++;
            }
        }
        int progress = (int) ((completedTasks / (double) totalTasks) * 100);
        progressBarTasks.setProgress(progress);
    }


    private void AddExecutor(MemberModel member) {
        // Call the API to update the executor of the card
        UserApiService.updateCardExecutor(this, boardId, columnId, cardId, member.getId(), new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                btnSelectExecuter.setText("Task executor");
                btnSelectExecuter.setTextColor(getResources().getColor(R.color.black));

                //Toast.makeText(CardMenuActivity.this, "Executor updated successfully", Toast.LENGTH_SHORT).show();
                Log.d("UpdateExecutor", "Executor updated successfully: " + response.toString());
                displayExecutor(member);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(CardMenuActivity.this, "Failed to update executor: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("UpdateExecutor", "Failed to update executor: " + errorMessage);
            }
        });

        RelativeLayout executorView = findViewById(R.id.executorView);
        executorView.setVisibility(View.VISIBLE);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View memberView = inflater.inflate(R.layout.member_view_layout, executorView, false);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        memberView.setLayoutParams(params);
        executorView.addView(memberView);
        View btnDeleteTaskExecutor = inflater.inflate(R.layout.member_view_layout, executorView, false);
    }

    private void fetchCardData() {
        UserApiService.fetchCardDetails(this, boardId, columnId, cardId, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                Log.d("Executor", "Response: " + response);

                JSONObject cardData = response.optJSONObject("card");
                if (cardData != null) {
                    // Проверяем, существует ли исполнитель карточки
                    String executorId = cardData.optString("executor", null);
                    if (executorId != null && !executorId.isEmpty()) {
                        fetchExecutorDetails(executorId);

                    } else {
                        Log.d("Executor", "executor not set");
                        btnSelectExecuter.setText("Task executor...");
                        btnSelectExecuter.setTextColor(getResources().getColor(R.color.blackTransparent25));
                    }

                    String description = cardData.optString("description");
                    String startDate = cardData.optString("startDate");
                    String endDate = cardData.optString("endDate");

                    getDescription(description);

                    getTime(startDate, endDate);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ApiError", errorMessage);
                Toast.makeText(CardMenuActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void getTime(String startDate, String endDate) {
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            currentDateAndTimeTextView.setText(startDate);
            currentDateAndTimeTextView.setTextColor(getResources().getColor(R.color.black));

            deadLineTextView.setText(endDate);

            // Формат для преобразования даты из строки
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            try {
                // Преобразование строки даты в объект Date
                Date endDateTime = sdf.parse(endDate);
                Date currentDateTime = new Date();

                // Расчет разницы во времени в миллисекундах
                long diffInMillis = endDateTime.getTime() - currentDateTime.getTime();

                // Перевод разницы из миллисекунд в дни
                long diffInDays = diffInMillis / (24 * 60 * 60 * 1000);

                // Установка цвета текста в зависимости от разницы во времени
                if (diffInDays > 1) {
                    deadLineTextView.setTextColor(getResources().getColor(R.color.green));
                } else {
                    deadLineTextView.setTextColor(getResources().getColor(R.color.red));
                }
            } catch (Exception e) {
                e.printStackTrace();
                deadLineTextView.setTextColor(getResources().getColor(R.color.black)); // Устанавливаем черный цвет в случае ошибки
            }
        } else {
            // Если startDate и endDate пустые, устанавливаем текст и цвет
            currentDateAndTimeTextView.setText("Start");
            currentDateAndTimeTextView.setTextColor(getResources().getColor(R.color.blackTransparent25));
            deadLineTextView.setText("Deadline");
            deadLineTextView.setTextColor(getResources().getColor(R.color.blackTransparent25));
        }
    }


    private void getDescription(String description) {
        descriptionEditText.setText(description);
    }


    private void displayExecutor(MemberModel executor) {
        // Find the executor view and set it to visible

        if (!isDestroyed()) {
            RelativeLayout executorView = findViewById(R.id.executorView);

            executorView.removeAllViews();
            // Remove any existing views to avoid duplicates

            executorView.setVisibility(View.VISIBLE);
            // Inflate the member view layout and set the avatar and username
            LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View memberView = inflater.inflate(R.layout.member_view_layout, executorView, false);

            ImageView avatarImageView = memberView.findViewById(R.id.avatarImageView);
            TextView membernameTextView = memberView.findViewById(R.id.membernameTextView);
            ImageButton changeExecutor = memberView.findViewById(R.id.btnMember);
            changeExecutor.setImageResource(R.drawable.change_icon);
            changeExecutor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    includedExecutersView.setVisibility(View.VISIBLE);
                    dimView.setVisibility(View.VISIBLE);

                }

            });
            // Use Glide to load the avatar image
            Glide.with(this).load(executor.getAvatarUrl()).into(avatarImageView);
            membernameTextView.setText(executor.getUsername());

            // Add the member view to the executor view
            executorView.addView(memberView);

            btnSelectExecuter.setText("Task executor");
            btnSelectExecuter.setTextColor(getResources().getColor(R.color.black));
        }
    }

    private void fetchBoardMembers(String boardId) {
        UserApiService.fetchBoardMembers(this, boardId, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                JSONArray membersArray = response.getJSONArray("members");
                parentLayout.removeAllViews();

                for (int i = 0; i < membersArray.length(); i++) {
                    JSONObject memberData = membersArray.getJSONObject(i);
                    String userId = memberData.getString("userId");
                    MemberModel member = new MemberModel(userId);
                    fetchUserDetailsAndUpdateMember(member);
                }
                adjustMembersViewHeight(membersArray.length());
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ApiError", errorMessage);
                Toast.makeText(CardMenuActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetailsAndUpdateMember(MemberModel member) {
        UserApiService.fetchUserById(this, member.getId(), new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject userData) throws JSONException {
                String userId = userData.getString("_id");
                String username = userData.getString("username");
                String avatarUrl = userData.getString("avatarURL");
                member.setId(userId);
                member.setUsername(username);
                member.setAvatarUrl(avatarUrl);
                addMemberView(member);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ApiError", errorMessage);
                Toast.makeText(CardMenuActivity.this, "Ошибка при получении данных пользователя: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchExecutorDetails(String executorId) {
        UserApiService.fetchUserById(this, executorId, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject userData) throws JSONException {
                String userId = userData.getString("_id");
                String username = userData.getString("username");
                String avatarUrl = userData.getString("avatarURL");

                MemberModel executor = new MemberModel(userId, username, avatarUrl);
                displayExecutor(executor);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ApiError", errorMessage);
                Toast.makeText(CardMenuActivity.this, "Ошибка при получении данных пользователя: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMemberView(MemberModel member) {
        if (!isDestroyed()) {
            View memberView = getLayoutInflater().inflate(R.layout.member_view_layout, parentLayout, false);
            ImageView avatarImageView = memberView.findViewById(R.id.avatarImageView);
            TextView membernameTextView = memberView.findViewById(R.id.membernameTextView);

            // Загрузка изображения аватара с использованием Glide
            Glide.with(this).load(member.getAvatarUrl()).into(avatarImageView);
            membernameTextView.setText(member.getUsername());

            // Назначение обработчика событий нажатия
            memberView.setOnClickListener(v -> {
                // Добавление выбранного участника в качестве исполнителя
                AddExecutor(member);
                includedExecutersView.setVisibility(View.GONE);
                dimView.setVisibility(View.GONE);
            });

            // Добавление представления в родительский макет
            parentLayout.addView(memberView);
        }
    }

    private void addComment(MemberModel member, String time, String commentText) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View commentView = inflater.inflate(R.layout.comment_view, commentsView, false);

        // Установите данные в виджеты
        ShapeableImageView userCommentAvatar = commentView.findViewById(R.id.avatarCommentImageView);
        TextView usernameTextView = commentView.findViewById(R.id.userCommentUsername);
        TextView timeTextView = commentView.findViewById(R.id.userCommentTime);
        TextView commentTextView = commentView.findViewById(R.id.commentText);

        if(!isDestroyed()){
            Glide.with(this).load(member.getAvatarUrl()).into(userCommentAvatar);
        }
        usernameTextView.setText(member.getUsername());
        timeTextView.setText(time);
        commentTextView.setText(commentText);

        // Добавьте комментарий в LinearLayout
        commentsView.addView(commentView);
    }
    private void saveAndClose() {
        // Данные изменены, выполняем переход
        Intent intent = new Intent(CardMenuActivity.this, BoardActivity.class);

        String currentDescription = descriptionEditText.getText().toString();

        UserApiService.updateDescription(CardMenuActivity.this, boardId, columnId, cardId, currentDescription, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                UserApiService.fetchBoard(CardMenuActivity.this, boardId, new UserApiService.Callback() {
                    @Override
                    public void onSuccess(JSONObject boardData) throws JSONException {
                        Log.d("CardMenuActivity", "Board data: " + boardData);
                        intent.putExtra("boardData", boardData.toString());

                        String title = boardData.getString("title");
                        intent.putExtra("boardTitle", title);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Обработка ошибки
                    }
                });
            }

            @Override
            public void onFailure(final String errorMessage) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(CardMenuActivity.this, "Failed to update description: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void adjustMembersViewHeight(int memberCount) {
        if (memberCount > 5) {
            // Height for 5 items, assuming each item has a height of 60dp
            int itemHeightInDp = 60;
            int maxHeightInDp = itemHeightInDp * 5;

            // Convert dp to pixels
            final float scale = getResources().getDisplayMetrics().density;
            int maxHeightInPixels = (int) (maxHeightInDp * scale + 0.5f);

            // Set the height of the ScrollView
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) executorsScrollView.getLayoutParams();
            params.height = maxHeightInPixels;
            executorsScrollView.setLayoutParams(params);
        } else {
            // Set height to wrap_content if members are less than or equal to 5
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) executorsScrollView.getLayoutParams();
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            executorsScrollView.setLayoutParams(params);
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}