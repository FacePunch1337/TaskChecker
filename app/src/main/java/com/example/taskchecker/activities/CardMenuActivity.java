package com.example.taskchecker.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.taskchecker.R;
import com.example.taskchecker.models.MemberModel;
import com.example.taskchecker.models.TaskModel;
import com.example.taskchecker.services.TaskAdapter;
import com.example.taskchecker.services.UserApiService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CardMenuActivity extends AppCompatActivity {

    private String cardTitle;
    private TextView taskTitleTextView;
    private MemberModel currentExecuter;
    private LinearLayout includedExecutersView;
    private LinearLayout parentLayout;
    private View dimView;
    private ImageButton btnBack;
    private ImageButton btnCloseWindow;
    private String boardId;
    private String columnId;
    private String cardId;
    private TextView currentDateAndTimeTextView;
    private TextView deadLineTextView;
    private RecyclerView recyclerViewTasks;
    private ProgressBar progressBarTasks;
    private TaskAdapter taskAdapter;
    private List<TaskModel> taskList = new ArrayList<>();

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
        TextView btnSelectExecuter = findViewById(R.id.btnSelectExecuter);
        btnCloseWindow = includedExecutersView.findViewById(R.id.btnCloseWindow);
        currentDateAndTimeTextView = findViewById(R.id.currentDateAndTime);
        deadLineTextView = findViewById(R.id.deadLineTextView);
        recyclerViewTasks = findViewById(R.id.recyclerView);
        progressBarTasks = findViewById(R.id.progressBar);
        TextView btnAddTask = findViewById(R.id.btnAddTask);
        CheckBox checkBox = findViewById(R.id.checkBox);

        taskAdapter = new TaskAdapter(taskList);
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTasks.setAdapter(taskAdapter);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Создаем Intent для перехода на BoardActivity
                Intent intent = new Intent(CardMenuActivity.this, BoardActivity.class);
                UserApiService.fetchBoard(CardMenuActivity.this, boardId, new UserApiService.Callback() {
                    @Override
                    public void onSuccess(JSONObject boardData) throws JSONException {
                        Log.d("CardMenuActivity", "Board data: " + boardData);
                        intent.putExtra("boardData",  boardData.toString());
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {

                    }
                });




                // Передача данных boardData (пример)
               /*String boardDataString = getIntent().getStringExtra("boardData");
                if (boardDataString != null) {
                    Log.d("CardMenuActivity", "Board data: " + boardDataString);
                    intent.putExtra("boardData", boardDataString);
                } else {
                    Log.e("CardMenuActivity", "Board data is NULL");
                }*/


                //onBackPressed();
            }
        });


        taskAdapter.setOnDeleteTaskClickListener(position -> {
            TaskModel taskToDelete = taskList.get(position);
            UserApiService.deleteTask(CardMenuActivity.this, boardId, columnId, cardId, taskToDelete.getTaskId(), new UserApiService.Callback() {
                @Override
                public void onSuccess(JSONObject response) {
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




        /*checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Обновляем состояние "checked" в объекте TaskModel при изменении состояния чекбокса
                for (TaskModel task : taskList) {
                    task.setValue(isChecked);
                }
                // Передаем обновленный список задач на сервер
                UserApiService.addTask(CardMenuActivity.this, boardId, columnId, cardId, taskList, new UserApiService.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Обработка успешного добавления задачи
                        // Можно добавить необходимую логику здесь
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Обработка ошибки при добавлении задачи
                        Toast.makeText(CardMenuActivity.this, "Failed to add task: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/

        // Удаление задачи
        /* taskAdapter.setOnDeleteTaskClickListener(position -> {
            TaskModel deletedTask = taskList.get(position);
            UserApiService.deleteTask(CardMenuActivity.this, boardId, columnId, cardId, deletedTask.getId(), new UserApiService.Callback() {
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
        });*/
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

        fetchTaskList();
        fetchCardData();
        fetchBoardMembers(boardId);
    }


    private void fetchTaskList() {
        UserApiService.getTaskList(this, boardId, columnId, cardId, new UserApiService.TaskIdCallback() {


            @Override
            public void onSuccessWithTaskIds(JSONObject userData, List<String> taskIds) throws JSONException {
                JSONArray tasksArray = userData.getJSONArray("tasks");
                taskList.clear(); // Очистить текущий список задач
                for (int i = 0; i < tasksArray.length(); i++) {
                    JSONObject taskObject = tasksArray.getJSONObject(i);
                    String taskId = taskIds.get(i); // Получаем _id из списка taskIds
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

    private void showDateTimePicker(TextView currentDateAndTimeTextView, TextView deadLineTextView) {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        // Вывод текущего времени в формате "Start: {date} - {time}"
        String currentDateTime = "Start: " + sdf.format(calendar.getTime());
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
                                deadLineTextView.setText("Deadline: " + selectedDateTime);

                                // Обновление времени выполнения карточки на сервере
                                updateCardData(currentDate, currentTime, selectedDate, selectedTime);
                            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);

                    timePickerDialog.show();
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void updateCardData(String currentStartDate, String currentStartTime, String selectedEndDate, String selectedEndTime) {
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
    }

    private void fetchCardData() {
        UserApiService.fetchCardDetails(this, boardId, columnId, cardId, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                Log.d("Executor", "Response: " + response);

                JSONObject cardData = response.optJSONObject("card");
                if (cardData != null) {
                    // Проверяем, существует ли исполнитель карточки
                    if (!cardData.isNull("executor")) {
                        String executorId = cardData.optString("executor");
                        String startDate = cardData.optString("startDate");
                        String endDate = cardData.optString("endDate");
                        // Если исполнитель существует, получаем его данные
                        fetchExecutorDetails(executorId);
                        fetchTime(startDate, endDate);


                    } else {
                        Log.d("Executor", "No executor found for the card.");
                        // Если исполнитель не существует, не делаем ничего
                    }
                } else {
                    Log.e("Executor", "No 'card' object found in response");
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ApiError", errorMessage);
                Toast.makeText(CardMenuActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTime(String startDate, String endDate) {
        currentDateAndTimeTextView.setText(startDate);
        deadLineTextView.setText(endDate);
    }


    private void displayExecutor(MemberModel executor) {
        // Find the executor view and set it to visible


        RelativeLayout executorView = findViewById(R.id.executorView);

        executorView.removeAllViews();
        // Remove any existing views to avoid duplicates

        executorView.setVisibility(View.VISIBLE);
        // Inflate the member view layout and set the avatar and username
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View memberView = inflater.inflate(R.layout.member_view_layout, executorView, false);

        ImageView avatarImageView = memberView.findViewById(R.id.avatarImageView);
        TextView membernameTextView = memberView.findViewById(R.id.membernameTextView);

        // Use Glide to load the avatar image
        Glide.with(this).load(executor.getAvatarUrl()).into(avatarImageView);
        membernameTextView.setText(executor.getUsername());

        // Add the member view to the executor view
        executorView.addView(memberView);
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
