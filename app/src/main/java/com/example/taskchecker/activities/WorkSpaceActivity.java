package com.example.taskchecker.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.taskchecker.R;
import com.example.taskchecker.layouts.BoardFragment;
import com.example.taskchecker.models.BoardButtonModel;
import com.example.taskchecker.models.BoardModel;
import com.example.taskchecker.models.UserModel;
import com.example.taskchecker.services.BoardButtonAdapter;
import com.example.taskchecker.services.UserApiService;
import com.google.android.material.imageview.ShapeableImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

// Интерфейс для обработки события добавления новой кнопки доски


public class WorkSpaceActivity extends AppCompatActivity {

    public static TextView titleTextView;
    public BoardModel boardModel;
    private LinearLayout sidePanel;
    private ShapeableImageView btnProfile;
    private ImageButton btnLogout;
    private ImageButton btnToggleSidePanel;
    private ImageButton btnToggleTheme;
    private ImageButton btnToggleCreate;

    private ListView listView;
    private View fragmentCreateView;
    private View fragmentBoardView;
    private Fragment boardFragment;
    private FragmentManager fragmentManager;
    private EditText editTextBoardTitle;
    private static ArrayList<BoardButtonModel> mBoardButtons = new ArrayList<>();

    private static BoardButtonAdapter mAdapter;
    private static OnBoardButtonAddedListener mListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences preferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        boolean isDarkTheme = preferences.getBoolean("is_dark_theme", false);
        if (isDarkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        setContentView(R.layout.activity_workspace);
        fetchBoardButtons();
        getSupportFragmentManager().beginTransaction()
                .hide(new BoardFragment())
                .commit();
        titleTextView = findViewById(R.id.titleTextView);        btnProfile = findViewById(R.id.btnProfile);
        btnLogout = findViewById(R.id.btnLogout);
        sidePanel = findViewById(R.id.sidePanel);
        btnToggleSidePanel = findViewById(R.id.btnToggleSidePanel);
        listView = findViewById(R.id.listView);
        btnToggleCreate = findViewById(R.id.btnToggleCreate);
        Glide.with(this).load(UserModel.getAvatarURL()).into(btnProfile);

        mBoardButtons = new ArrayList<>();
        mAdapter = new BoardButtonAdapter(this, mBoardButtons);
        listView.setAdapter(mAdapter);
        fragmentCreateView = findViewById(R.id.fragmentCreateView);
        fragmentBoardView = findViewById(R.id.fragmentBoardView);
        fragmentManager = getSupportFragmentManager();
        boardFragment = fragmentManager.findFragmentById(R.id.fragmentBoardView);
        fragmentCreateView.setVisibility(View.GONE);
        fragmentBoardView.setVisibility(View.GONE);
        btnToggleTheme = findViewById(R.id.btnToggleTheme);
        btnToggleTheme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleThemeClick();
            }
        });
        setOnBoardButtonAddedListener(new OnBoardButtonAddedListener() {
            @Override
            public void onBoardButtonAdded() {
                // Вызываем метод fetchBoardButtons при добавлении новой кнопки
                fetchBoardButtons();
            }
        });

        btnToggleCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onToggleCreateClick();
            }
        });

        btnProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openProfileActivity();
            }
        });

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });

    }

    private void openProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void onToggleThemeClick() {
        boolean isCurrentlyDark = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES;
        if (isCurrentlyDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            saveThemePreference(false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            saveThemePreference(true);
        }
    }

    private void saveThemePreference(boolean isDarkTheme) {
        SharedPreferences preferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_dark_theme", isDarkTheme);
        editor.apply();
    }

    public void onToggleCreateClick() {
        if (fragmentCreateView.getVisibility() == View.VISIBLE) {
            fragmentCreateView.setVisibility(View.GONE);
        } else {
            fragmentCreateView.setVisibility(View.VISIBLE);
        }
    }

    public static void addBoardButton(String boardId, String boardTitle) {
        BoardButtonModel newButton = new BoardButtonModel(boardId, boardTitle);
        mBoardButtons.add(newButton);
        mAdapter.notifyDataSetChanged();
    }

    public static void addNewBoardButton(String boardTitle) {
        BoardButtonModel newButton = new BoardButtonModel(boardTitle);
        mBoardButtons.add(newButton);
        mAdapter.notifyDataSetChanged();

        // Перезапускаем активити
       /* WorkSpaceActivity activity = (WorkSpaceActivity) mAdapter.getContext();
        if (activity != null) {
            activity.recreate();
        }*/

        // Вызываем событие добавления новой кнопки
        if (mListener != null) {
            mListener.onBoardButtonAdded();
        }
    }



    /*public void OpenBoard(JSONObject boardData) throws JSONException {
        if (boardData != null) {
            fragmentBoardView.setVisibility(View.VISIBLE);
            String boardTitle = boardData.getString("title");
            titleTextView.setText(boardTitle);

            // Создаем экземпляр модели доски и устанавливаем в него данные
            //boardModel = new BoardModel();
            //boardModel.setBoardData(boardData);
            // Передаем данные фрагменту BoardFragment
           // BoardFragment boardFragment = new BoardFragment();
            BoardFragment boardFragment = (BoardFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentBoardView);
            if (boardFragment != null) {
                boardFragment.GetBoardData(boardData);
            }

            fragmentManager.beginTransaction().show(boardFragment).commit();
        } else {
            Log.e("WorkSpaceActivity", "Получено пустое boardData");
            // При необходимости отображаем всплывающее сообщение или обрабатываем этот случай грациозно
        }
    }*/

    public void OpenBoard(JSONObject boardData) throws JSONException {
        if (boardData != null) {
            // Создаем намерение для запуска BoardActivity
            Intent intent = new Intent(this, BoardActivity.class);
            // Передаем данные о доске как строку JSON через Intent
            intent.putExtra("boardData", boardData.toString());

            // Передаем title
            String title = boardData.getString("title");
            intent.putExtra("boardTitle", title);

            // Запускаем активность
            startActivity(intent);
        } else {
            Log.e("WorkSpaceActivity", "Получено пустое boardData");
            // При необходимости отображаем всплывающее сообщение или обрабатываем этот случай грациозно
        }
    }





    public void onToggleSidePanelClick(View view) {
        if (sidePanel.getVisibility() == View.VISIBLE) {
            sidePanel.setVisibility(View.GONE);
        } else {
            sidePanel.setVisibility(View.VISIBLE);
        }
    }

    private void fetchBoardButtons() {
        // Очищаем список перед загрузкой новых кнопок
        mBoardButtons.clear();

        // Предположим, что идентификатор клиента доступен через какой-то метод или поле
        String clientId = UserModel.get_id();

        UserApiService.fetchBoardButtons(WorkSpaceActivity.this, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray buttonsArray = response.getJSONArray("boardButtons");
                    for (int i = 0; i < buttonsArray.length(); i++) {
                        JSONObject buttonObject = buttonsArray.getJSONObject(i);
                        String boardId = buttonObject.getString("_id");
                        String buttonTitle = buttonObject.getString("title");
                        addBoardButton(boardId, buttonTitle);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(WorkSpaceActivity.this, "Не удалось получить кнопки досок: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, clientId);
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentBoardView, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void logout() {
        Intent intent = new Intent(this, AuthActivity.class);
        startActivity(intent);
        finish();
    }

    public static void setOnBoardButtonAddedListener(OnBoardButtonAddedListener listener) {
        mListener = listener;
    }
    public interface OnBoardButtonAddedListener {
        void onBoardButtonAdded();
    }

}
