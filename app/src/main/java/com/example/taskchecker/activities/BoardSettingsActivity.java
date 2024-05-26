package com.example.taskchecker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.taskchecker.R;
import com.example.taskchecker.models.MemberModel;
import com.example.taskchecker.services.UserApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BoardSettingsActivity extends AppCompatActivity {

    private ImageButton btnAddMember;
    private EditText findByUsernameEditText;
    private String boardId;
    private LinearLayout parentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.members_view_layout);

        btnAddMember = findViewById(R.id.btnAddMember);
        findByUsernameEditText = findViewById(R.id.findByUsernameEditText);
        parentLayout = findViewById(R.id.parentLayout);

        // Получаем Intent, который запустил эту активность
        Intent intent = getIntent();

        // Извлекаем boardId из Intent
        boardId = intent.getStringExtra("boardId");
        btnAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = findByUsernameEditText.getText().toString();
                if (!username.isEmpty()) {
                    addMember(username);
                } else {
                    Toast.makeText(BoardSettingsActivity.this, "Введите имя пользователя", Toast.LENGTH_SHORT).show();
                }
            }
        });
        fetchBoardMembers(boardId);
    }

    private void addMember(String username) {
        UserApiService.fetchUserByUsername(this, username, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject userData) throws JSONException {
                String memberId = userData.getString("_id");
                String username = userData.getString("username");
                String avatarUrl = userData.getString("avatarURL");
                UserApiService.addMemberToBoard(BoardSettingsActivity.this, boardId, memberId, new UserApiService.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) throws JSONException {
                        // Обработка успешного добавления участника в доску
                        Log.d("MemberAdded", "Member added: " + username);
                        // После успешного добавления участника обновляем список участников
                        fetchBoardMembers(boardId);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        // Обработка ошибки при добавлении участника в доску
                        Log.e("ApiError", errorMessage);
                        Toast.makeText(BoardSettingsActivity.this, "Ошибка при добавлении участника в доску: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ApiError", errorMessage);
                Toast.makeText(BoardSettingsActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBoardMembers(String boardId) {
        UserApiService.fetchBoardMembers(BoardSettingsActivity.this, boardId, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                JSONArray membersArray = response.getJSONArray("members");

                // Очистка текущего списка участников
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
                Toast.makeText(BoardSettingsActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetailsAndUpdateMember(MemberModel member) {
        UserApiService.fetchUserById(BoardSettingsActivity.this, member.getId(), new UserApiService.Callback() {
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
                Toast.makeText(BoardSettingsActivity.this, "Ошибка при получении данных пользователя: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMemberView(MemberModel member) {
        // Создаем новое представление memberView
        View memberView = getLayoutInflater().inflate(R.layout.member_view_layout, null);

        // Находим элементы внутри memberView
        ImageView avatarImageView = memberView.findViewById(R.id.avatarImageView);
        TextView membernameTextView = memberView.findViewById(R.id.membernameTextView);

        // Устанавливаем данные пользователя
        Glide.with(BoardSettingsActivity.this).load(member.getAvatarUrl()).into(avatarImageView);
        membernameTextView.setText(member.getUsername());

        // Добавляем memberView в родительский контейнер
        parentLayout.addView(memberView);
    }
}
