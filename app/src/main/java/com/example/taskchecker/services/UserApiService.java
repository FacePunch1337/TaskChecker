    package com.example.taskchecker.services;

    import android.content.Context;
    import android.content.Intent;
    import android.util.Log;

    import com.android.volley.Request;
    import com.android.volley.Response;
    import com.android.volley.VolleyError;
    import com.android.volley.toolbox.JsonArrayRequest;
    import com.android.volley.toolbox.JsonObjectRequest;
    import com.android.volley.toolbox.Volley;
    import com.example.taskchecker.models.BoardButtonModel;
    import com.example.taskchecker.models.BoardModel;
    import com.example.taskchecker.models.UserModel;
    import com.example.taskchecker.activities.AuthActivity;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import okhttp3.OkHttpClient;
    import retrofit2.Retrofit;
    import retrofit2.converter.gson.GsonConverterFactory;

    public class UserApiService {

        public static final String BASE_URL = "https://task-cheker-api.vercel.app/";
        private static Retrofit retrofit;

        public static Retrofit getRetrofit() {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            if (retrofit == null) {
                retrofit = new Retrofit.Builder().baseUrl(BASE_URL).
                        addConverterFactory(GsonConverterFactory.create()).client(okHttpClient).build();
            }
            return retrofit;
        }


        public static void signIn(Context context, String username, String password, final Callback callback) {
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("username", username);
                requestData.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            String signInUrl = BASE_URL + "login";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, signInUrl, requestData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ApiResponse", "Response: " + response.toString());

                            try {
                                if (response.getString("message").equals("Успешная аутентификация")) {
                                    JSONObject userData = response.getJSONObject("user");
                                    UserModel userModel = new UserModel(userData);

                                    // Получаем строку userData
                                    String userDataString = String.valueOf(userModel.getUserData());

                                    // Убираем экранированные символы из URL
                                    String cleanUserData = userDataString.replace("\\/", "/");
                                    Log.d("UserData", "userData: " + cleanUserData);

                                    callback.onSuccess(userData);
                                } else {
                                    callback.onFailure("Authentication Failed");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.onFailure("JSON Parsing Error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onFailure("Volley Error: " + error.getMessage());
                        }
                    });

            Volley.newRequestQueue(context).add(jsonObjectRequest);
        }



        public static void signUp(Context context, String username, String email, String password, final Callback callback) {
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("username", username);
                requestData.put("email", email);
                requestData.put("password", password);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            String signUpUrl = BASE_URL + "users";
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, signUpUrl, requestData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ApiResponse", "Response: " + response.toString());

                            try {
                                JSONObject userData = response.getJSONObject("user");
                                callback.onSuccess(userData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.onFailure("JSON Parsing Error: " + e.getMessage());
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onFailure("Volley Error: " + error.getMessage());
                        }
                    });


            Volley.newRequestQueue(context).add(jsonObjectRequest);
        }

        public static void createNewBoard(final Context context, final String boardTitle, final Callback callback) {
            String url = BASE_URL + "boards";

            JSONObject requestData = new JSONObject();
            try {

                requestData.put("title", boardTitle);
                requestData.put("owner", UserModel.get_id());
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ApiResponse", "Response: " + response.toString());
                            try {
                                callback.onSuccess(response);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onFailure("Volley Error: " + error.getMessage());
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }

        public static void fetchBoardButtons(final Context context, final Callback callback) {
            String url = BASE_URL + "boards/"+UserModel.get_id();

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {


                                // Преобразуем JSONArray в JSONObject
                                JSONObject jsonResponse = new JSONObject();
                                jsonResponse.put("boardButtons", response);
                                // Передаем JSONObject в колбэк onSuccess
                                Log.d("ApiResponse", "BoardsInfo: " + response.toString());

                                callback.onSuccess(jsonResponse);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.onFailure("JSON Parsing Error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            callback.onFailure("Volley Error: " + error.getMessage());
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }

        public static void fetchBoard(Context context, String boardId, final Callback callback) {
            String url = BASE_URL + "board/" + boardId;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                // Передаем данные о доске в обратный вызов при успешном получении ответа
                                callback.onSuccess(response);
                            } catch (Exception e) {
                                e.printStackTrace();
                                // Обработка ошибок при парсинге ответа
                                callback.onFailure("Error parsing response: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // Обработка ошибок при запросе
                            callback.onFailure("Volley Error: " + error.getMessage());
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }



        public static void logout(Context context) {

            Intent intent = new Intent(context, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }



        public interface Callback {
            void onSuccess(JSONObject userData) throws JSONException;
            void onFailure(String errorMessage);
        }



    }