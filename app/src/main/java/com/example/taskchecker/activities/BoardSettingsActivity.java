package com.example.taskchecker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.taskchecker.R;
import com.example.taskchecker.models.MemberModel;
import com.example.taskchecker.models.UserModel;
import com.example.taskchecker.services.UserApiService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BoardSettingsActivity extends AppCompatActivity {



    private String boardId;
    private String owner;
    private LinearLayout parentLayout;
    private ScrollView scrollView;
    private RelativeLayout membersView;
    private TextView boardNameTextView;
    private static final int MAX_VISIBLE_MEMBERS = 5;
    private static final int REQUEST_CODE_ADD_MEMBER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_settings_view_layout);

        ImageButton btnBack = findViewById(R.id.btnBack);
        ImageButton btnAddMember = findViewById(R.id.btnAddMember);
        parentLayout = findViewById(R.id.parentLayout);
        scrollView = findViewById(R.id.scrollView);
        membersView = findViewById(R.id.membersView);
        boardNameTextView = findViewById(R.id.boardTitleEditText);
        // Получаем Intent, который запустил эту активность
        Intent intent = getIntent();

        // Извлекаем boardId из Intent
        boardId = intent.getStringExtra("boardId");
        owner = intent.getStringExtra("owner");
        boardNameTextView.setText(intent.getStringExtra("boardTitle"));
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if (UserModel.get_id().equals(owner)) {
            btnAddMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(BoardSettingsActivity.this, AddMembersActivity.class);
                    intent.putExtra("boardId", boardId);
                    startActivityForResult(intent, REQUEST_CODE_ADD_MEMBER);
                }
            });

        }
        else {
            btnAddMember.setVisibility(View.GONE);
        }
        fetchBoardMembers(boardId);
    }

    private void fetchBoardMembers(String boardId) {
        UserApiService.fetchBoardMembers(BoardSettingsActivity.this, boardId, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                JSONArray membersArray = response.getJSONArray("members");

                // Очистка текущего списка участников
                parentLayout.removeAllViews();

                // Создаем список участников
                List<MemberModel> membersList = new ArrayList<>();

                for (int i = 0; i < membersArray.length(); i++) {
                    JSONObject memberData = membersArray.getJSONObject(i);
                    String userId = memberData.getString("userId");

                    MemberModel member = new MemberModel(userId);
                    membersList.add(member);
                }

                // Сортируем список, чтобы владелец был первым
                Collections.sort(membersList, new Comparator<MemberModel>() {
                    @Override
                    public int compare(MemberModel m1, MemberModel m2) {
                        if (m1.getId().equals(owner)) {
                            return -1;
                        } else if (m2.getId().equals(owner)) {
                            return 1;
                        }
                        return 0;
                    }
                });

                // Добавляем участников в отображение
                for (MemberModel member : membersList) {
                    fetchUserDetailsAndUpdateMember(member);
                }

                adjustMembersViewHeight(membersArray.length());
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
        if (isDestroyed()) {
            return; // Если активность уничтожена, выходим из метода
        }

        // Создаем новое представление memberView
        View memberView = getLayoutInflater().inflate(R.layout.member_view_layout, null);

        // Находим элементы внутри memberView
        ImageView avatarImageView = memberView.findViewById(R.id.avatarImageView);
        TextView membernameTextView = memberView.findViewById(R.id.membernameTextView);
        ImageButton btnDeleteMember = memberView.findViewById(R.id.btnMember);

        // Устанавливаем данные пользователя
        Glide.with(BoardSettingsActivity.this).load(member.getAvatarUrl()).into(avatarImageView);
        membernameTextView.setText(member.getUsername());

        // Проверяем, является ли текущий член владельцем
        if (member.getId().equals(owner)) {
            btnDeleteMember.setImageResource(R.drawable.crown); // Устанавливаем иконку короны для владельца
            btnDeleteMember.setOnClickListener(null); // Убираем обработчик клика
            btnDeleteMember.setVisibility(View.VISIBLE); // Делаем кнопку видимой
        } else if (UserModel.get_id().equals(owner)) {
            btnDeleteMember.setImageResource(R.drawable.deletemembericon); // Устанавливаем иконку удаления для остальных участников
            btnDeleteMember.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeMember(member.getId());
                }
            });
            btnDeleteMember.setVisibility(View.VISIBLE);
        } else {
            btnDeleteMember.setVisibility(View.GONE); // Скрываем кнопку удаления для всех остальных участников
        }

        // Добавляем memberView в родительский контейнер
        if (member.getId().equals(owner)) {
            parentLayout.addView(memberView, 0); // добавляем владельца в начало
        } else {
            parentLayout.addView(memberView);
        }
    }

    private void removeMember(String memberId) {
        UserApiService.removeMemberFromBoard(BoardSettingsActivity.this, boardId, memberId, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) throws JSONException {
                // Обновляем список участников после успешного удаления
                fetchBoardMembers(boardId);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ApiError", errorMessage);
                Toast.makeText(BoardSettingsActivity.this, "Ошибка при удалении участника: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void adjustMembersViewHeight(int memberCount) {
        if (memberCount > MAX_VISIBLE_MEMBERS) {
            // Высота для 5 элементов, исходя из предположения, что каждый элемент имеет высоту 60dp
            int itemHeightInDp = 60;
            int maxHeightInDp = itemHeightInDp * MAX_VISIBLE_MEMBERS;

            // Преобразование dp в пиксели
            final float scale = getResources().getDisplayMetrics().density;
            int maxHeightInPixels = (int) (maxHeightInDp * scale + 0.5f);

            // Установка высоты ScrollView
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
            params.height = maxHeightInPixels;
            scrollView.setLayoutParams(params);
        } else {
            // Возвращаем высоту wrap_content если участников меньше или равно MAX_VISIBLE_MEMBERS
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) scrollView.getLayoutParams();
            params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            scrollView.setLayoutParams(params);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_MEMBER && resultCode == RESULT_OK) {
            // Обновляем список участников
            fetchBoardMembers(boardId);
        }
    }
}
