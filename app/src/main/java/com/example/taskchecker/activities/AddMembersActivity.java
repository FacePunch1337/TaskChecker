package com.example.taskchecker.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.taskchecker.R;
import com.example.taskchecker.services.UserApiService;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AddMembersActivity extends AppCompatActivity {
    private JSONArray users;
    private LinearLayout linearLayout;
    private EditText userSearchEditText;
    private ImageButton stopSearchButton;
    private ImageButton btnCloseAddMember;
    private String boardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_members_layout);
        Intent intent = getIntent();
        linearLayout = findViewById(R.id.linearLayout);
        userSearchEditText = findViewById(R.id.UserSearchEditText);
        btnCloseAddMember = findViewById(R.id.btnCloseAddMember);
        boardId = intent.getStringExtra("boardId");

        UserApiService.fetchUsers(AddMembersActivity.this, new UserApiService.JSONArraysCallback() {
            @Override
            public void onSuccess(JSONArray response) {
                users = response;
                Log.d("ApiResponse", "Data: " + response);
                //displayUsers()
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("MainActivity", "Error: " + errorMessage);
            }
        });

        userSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterAndDisplayUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        btnCloseAddMember.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void filterAndDisplayUsers(String query) {
        if (users == null || query.length() < 1) {
            linearLayout.removeAllViews();
            return;
        }

        JSONArray filteredUsers = new JSONArray();
        for (int i = 0; i < users.length(); i++) {
            try {
                JSONObject user = users.getJSONObject(i);
                String username = user.getString("username");
                if (username.toLowerCase().contains(query.toLowerCase())) {
                    filteredUsers.put(user);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        sortUsers(filteredUsers);
        displayUsers(filteredUsers);
    }

    private void sortUsers(JSONArray usersArray) {
        ArrayList<JSONObject> userList = new ArrayList<>();
        for (int i = 0; i < usersArray.length(); i++) {
            try {
                userList.add(usersArray.getJSONObject(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Collections.sort(userList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                String userA = "", userB = "";
                try {
                    userA = a.getString("username");
                    userB = b.getString("username");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return userA.compareToIgnoreCase(userB);
            }
        });

        usersArray = new JSONArray(userList);
    }

    private void displayUsers(JSONArray usersArray) {
        linearLayout.removeAllViews();
        for (int i = 0; i < usersArray.length(); i++) {
            try {
                JSONObject user = usersArray.getJSONObject(i);
                String avatarUrl = user.getString("avatarURL");
                String username = user.getString("username");
                addUserButton(avatarUrl, username);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void addUserButton(String avatarUrl, String username) {
        if (!isDestroyed()) {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View memberView = inflater.inflate(R.layout.member_view_layout, null);

            ShapeableImageView userAvatar = memberView.findViewById(R.id.avatarImageView);
            TextView memberNameTextView = memberView.findViewById(R.id.membernameTextView);
            ImageButton check = memberView.findViewById(R.id.btnMember);
            check.setVisibility(View.GONE);

            // Используем Glide для загрузки аватара
            Glide.with(this)
                    .load(avatarUrl)
                    .into(userAvatar);

            memberNameTextView.setText(username);

            // Устанавливаем слушатель кликов для всей view
            memberView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addMember(username);
                }
            });

            linearLayout.addView(memberView);
        }
    }


    private void addMember(String username) {
        UserApiService.fetchUserByUsername(this, username, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject userData) throws JSONException {
                String memberId = userData.getString("_id");
                String username = userData.getString("username");
                String avatarUrl = userData.getString("avatarURL");
                UserApiService.addMemberToBoard(AddMembersActivity.this, boardId, memberId, new UserApiService.Callback() {
                    @Override
                    public void onSuccess(JSONObject response) throws JSONException {
                        Log.d("MemberAdded", "Member added: " + username);
                        Intent resultIntent = new Intent();
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e("ApiError", errorMessage);
                        Toast.makeText(AddMembersActivity.this, "Ошибка при добавлении участника в доску: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("ApiError", errorMessage);
                Toast.makeText(AddMembersActivity.this, "Ошибка: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }




}
