package com.example.taskchecker.activities;

import static com.example.taskchecker.services.UserApiService.BASE_URL;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.taskchecker.R;
import com.example.taskchecker.models.UserModel;
import com.example.taskchecker.services.UploadService;
import com.example.taskchecker.services.UserApiService;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.File;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameTextView, emailTextView;
    private ImageView profileImageView;
    private Button closeProfileButton;
    private String filePath;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_PERMISSIONS = 1;
    private static final String[] PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES

    };

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            p = PERMISSIONS;
        } else {
            p = PERMISSIONS;
        }
        return p;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        profileImageView = findViewById(R.id.profileImageView);
        closeProfileButton = findViewById(R.id.closeProfileButton);





        String username = UserModel.get_username();
        String email = UserModel.get_email();
        Glide.with(ProfileActivity.this).load(UserModel.getAvatarURL()).into(profileImageView);

        usernameTextView.setText(username);
        emailTextView.setText(email);
        closeProfileButton.setOnClickListener(v -> closeProfile());

        // Обработчик нажатия на ImageView для выбора изображения
        profileImageView.setOnClickListener(v -> requestStoragePermissionsAndOpenGallery());
    }

    private void requestStoragePermissionsAndOpenGallery() {
        // Проверяем разрешения перед запросом
        if (!hasPermissions()) {
            // Если разрешения не предоставлены, запросим их
            ActivityCompat.requestPermissions(ProfileActivity.this,  permissions(), REQUEST_PERMISSIONS);
        } else {
            // Если разрешения уже есть, открываем галерею для выбора изображения
            openGallery();
        }
    }

    private boolean hasPermissions() {
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение получено, открываем галерею для выбора изображения
                openGallery();
            } else {
                // Разрешение не предоставлено, выводим сообщение об ошибке
                Toast.makeText(this, "Permission denied for reading external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri selectedImageUri = data.getData();

            // Получаем путь к изображению из URI
            filePath = getRealPathFromURI(selectedImageUri);

            // Вызываем метод загрузки изображения на сервер
            uploadImage(filePath);
        }
    }

    private String getRealPathFromURI(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String filePath = cursor.getString(columnIndex);
        Log.d("FilePathDebug", "File path: " + filePath); // Отладочная информация
        cursor.close();
        return filePath;
    }

    private void uploadImage(String filePath) {
        Log.d("UploadDebug", "Starting image upload");

        File file = new File(filePath);

        Retrofit retrofit = UserApiService.getRetrofit();
        UploadService uploadService = retrofit.create(UploadService.class);

        RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), file);
        MultipartBody.Part parts = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

        uploadService.uploadImage(parts).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String message = jsonObject.getString("message");
                        String imageUrl = jsonObject.getString("imageUrl");
                        String avatarFilename = jsonObject.getString("fileName");

                        String oldAvatarFilename = UserModel.getAvatarFilename();
                        deleteOldAvatar(oldAvatarFilename);

                        Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.e("onResponse", "Image URL: " + imageUrl + "Filename: " + avatarFilename);

                        // Обновляем UI новым изображением
                        Glide.with(ProfileActivity.this).load(imageUrl).into(profileImageView);
                        updateUserData(imageUrl, avatarFilename);
                        // Удаляем старый аватар

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("UploadError", "Error parsing response: " + e.getMessage());
                        Toast.makeText(ProfileActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.e("UploadError", "Error parsing JSON: " + e.getMessage());
                        Toast.makeText(ProfileActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("UploadError", "Upload failed: " + response.message());
                    Toast.makeText(ProfileActivity.this, "Upload failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("UploadError", "Upload failed: " + t.getMessage());
                Toast.makeText(ProfileActivity.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteOldAvatar(String avatarFilename) {
        // Отправляем запрос на удаление старого аватара
        // Например, если сервер поддерживает удаление аватара по его URL:
        // Создаем URL для удаления
        String deleteFilename = BASE_URL + "deleteAvatar?avatarFilename=" + avatarFilename;

        // Создаем запрос на сервер для удаления
        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, deleteFilename, null,
                response -> {
                    // Обработка успешного удаления старого аватара
                },
                error -> {
                    // Обработка ошибки при удалении старого аватара
                });

        // Добавляем запрос на удаление в очередь для выполнения
        Volley.newRequestQueue(this).add(deleteRequest);
    }

    private void updateUserData(String imageUrl, String fileName) {


        UserModel.setAvatarURL(imageUrl);
        UserModel.setAvatarFilename(fileName);
        // Создаем JSON объект для отправки на сервер
        JSONObject jsonBody = new JSONObject();
        String url = BASE_URL + "users/" + UserModel.get_id();
        try {
            jsonBody.put("avatarUrl", imageUrl);
            jsonBody.put("avatarFilename", fileName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Создаем запрос на сервер
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, jsonBody,
                response -> {
                    Log.e("UpdateSuccesful", "Update Succesful: " + response);
                },
                error -> {
                    Log.e("UpdateError", "Update failed: " + error);
                });

        // Добавляем запрос в очередь для выполнения
        Volley.newRequestQueue(this).add(request);
    }


    /*private void logout() {
        UserApiService.logout(this);
        finish();
    }*/

     private void closeProfile() {
         Intent intent = new Intent(this, WorkSpaceActivity.class);
         startActivity(intent);
     }

}
