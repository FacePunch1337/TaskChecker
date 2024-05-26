    package com.example.taskchecker.services;

    import android.content.Context;
    import android.content.Intent;
    import android.util.Log;

    import com.android.volley.Request;
    import com.android.volley.RequestQueue;
    import com.android.volley.Response;
    import com.android.volley.VolleyError;
    import com.android.volley.toolbox.JsonArrayRequest;
    import com.android.volley.toolbox.JsonObjectRequest;
    import com.android.volley.toolbox.Volley;
    import com.example.taskchecker.models.TaskModel;
    import com.example.taskchecker.models.UserModel;
    import com.example.taskchecker.activities.AuthActivity;

    import org.json.JSONArray;
    import org.json.JSONException;
    import org.json.JSONObject;

    import java.util.ArrayList;
    import java.util.List;

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

        public static void fetchBoardButtons(final Context context, final Callback callback, final String clientId) {
            String url = BASE_URL + "boards/";

            JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                JSONArray filteredBoards = new JSONArray();

                                // Проходим по всем доскам в ответе
                                for (int i = 0; i < response.length(); i++) {
                                    JSONObject board = response.getJSONObject(i);
                                    JSONArray members = board.getJSONArray("members");

                                    // Проверяем, есть ли clientId в массиве members
                                    boolean isMember = false;
                                    for (int j = 0; j < members.length(); j++) {
                                        JSONObject member = members.getJSONObject(j);
                                        if (clientId.equals(member.getString("userId"))) {
                                            isMember = true;
                                            break;
                                        }
                                    }

                                    // Если клиент является членом доски, добавляем её в список filteredBoards
                                    if (isMember) {
                                        filteredBoards.put(board);
                                    }
                                }

                                // Преобразуем filteredBoards в JSONObject для передачи в колбэк
                                JSONObject jsonResponse = new JSONObject();
                                jsonResponse.put("boardButtons", filteredBoards);

                                // Логируем ответ и передаем его в колбэк onSuccess
                                Log.d("ApiResponse", "BoardsInfo: " + filteredBoards.toString());
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
                                //BoardModel boardModel = new BoardModel();
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

        public static void createNewCard(final Context context, final String boardId, final String columnId, final String cardTitle, final Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + columnId + "/cards";

            JSONObject requestData = new JSONObject();
            try {
                requestData.put("title", cardTitle);
                // Вы можете добавить другие поля карточки, если нужно
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

        public static void moveCard(final Context context, final String boardId, final String fromColumnId, final String cardId, final String toColumnId, final Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + fromColumnId + "/cards/" + cardId + "/move/" + toColumnId;

            Log.d("ApiRequest", "URL: " + url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, null,
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
                            if (error.networkResponse != null) {
                                Log.e("ApiError", "Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                                callback.onFailure("Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                            } else {
                                Log.e("ApiError", "Volley Error: " + error.getMessage());
                                callback.onFailure("Volley Error: " + error.getMessage());
                            }
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }


        public static void deleteCard(final Context context, final String boardId, final String columnId, final String cardId, final Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + columnId + "/cards/" + cardId;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
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
                            if (error.networkResponse != null) {
                                Log.e("ApiError", "Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                                callback.onFailure("Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                            } else {
                                Log.e("ApiError", "Volley Error: " + error.getMessage());
                                callback.onFailure("Volley Error: " + error.getMessage());
                            }
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }

        public static void fetchUserByUsername(Context context, String username, final Callback callback) {
            String url = BASE_URL + "users/findByUsername";

            JSONObject requestData = new JSONObject();
            try {
                requestData.put("username", username);
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
        public static void fetchBoardMembers(Context context, String boardId, final Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/members";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ApiResponse", "Board Members: " + response.toString());
                            try {
                                callback.onSuccess(response); // Передаем JSONObject в колбэк onSuccess
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



        public static void addMemberToBoard(Context context, String boardId, String memberId, final Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/members/" + memberId;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ApiResponse", "Response: " + response.toString());
                            try {
                                callback.onSuccess(response);
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

        public static void fetchUserById(Context context, String userId, final Callback callback) {
            String url = BASE_URL + "users/" + userId; // Убедитесь, что этот URL корректный

            Log.d("ApiRequest", "Fetching user by ID: " + url);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ApiResponse", "User Data: " + response.toString());
                            try {
                                callback.onSuccess(response); // Передаем JSONObject в колбэк onSuccess
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.onFailure("JSON Parsing Error: " + e.getMessage());
                            }
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("ApiError", "Volley Error: " + error.getMessage());
                            callback.onFailure("Volley Error: " + error.getMessage());
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }
        public static void updateCardExecutor(final Context context, final String boardId, final String columnId, final String cardId, final String executorId, final Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + columnId + "/cards/" + cardId + "/executor";

            JSONObject requestData = new JSONObject();
            try {
                requestData.put("executor", executorId);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestData,
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
                            if (error.networkResponse != null) {
                                Log.e("ApiError", "Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                                callback.onFailure("Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                            } else {
                                Log.e("ApiError", "Volley Error: " + error.getMessage());
                                callback.onFailure("Volley Error: " + error.getMessage());
                            }
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }
        public static void fetchCardDetails(Context context, String boardId, String columnId, String cardId, final Callback callback) {
            String url = BASE_URL+ "boards/" + boardId + "/columns/" + columnId + "/cards/" + cardId;

            RequestQueue requestQueue = Volley.newRequestQueue(context);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    url,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                callback.onSuccess(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.onFailure("JSON parsing error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("ApiError", "Error fetching card details: " + error.getMessage());
                            callback.onFailure("Error fetching card details: " + error.getMessage());
                        }
                    }
            );

            requestQueue.add(jsonObjectRequest);
        }


        public static void logout(Context context) {

            Intent intent = new Intent(context, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }

        public static void updateDeadline(Context context, String boardId, String columnId, String cardId, String startDate, String endDate, final Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + columnId + "/cards/" + cardId;

            JSONObject requestData = new JSONObject();
            try {
                requestData.put("startDate", startDate);
                requestData.put("endDate", endDate);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestData,
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
                            if (error.networkResponse != null) {
                                Log.e("ApiError", "Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                                callback.onFailure("Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                            } else {
                                Log.e("ApiError", "Volley Error: " + error.getMessage());
                                callback.onFailure("Volley Error: " + error.getMessage());
                            }
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }

        public static void addNewTask(Context context, String boardId, String columnId, String cardId, TaskModel task, Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + columnId + "/cards/" + cardId + "/tasks";

            // Convert the list of TaskModel objects to a JSONArray
            JSONArray tasksArray = new JSONArray();
            try {
               /* for (TaskModel task : tasks) {
                    JSONObject taskObject = new JSONObject();
                    taskObject.put("description", task.getDescription());
                    taskObject.put("checked", task.isChecked());
                    tasksArray.put(taskObject);
                }*/
                JSONObject taskObject = new JSONObject();

                taskObject.put("description", task.getDescription());
                taskObject.put("checked", task.isChecked());
                tasksArray.put(taskObject);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            // Construct the request data
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("tasks", tasksArray);
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            // Create the JsonObjectRequest
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestData,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ApiResponse", "Response: " + response.toString());
                            try {
                                JSONObject cardObject = response.getJSONObject("card"); // Получаем объект card
                                JSONArray tasksArray = cardObject.getJSONArray("tasks"); // Получаем массив tasks

// Проходим по всем таскам в массиве
                                for (int i = 0; i < tasksArray.length(); i++) {
                                    JSONObject taskObject = tasksArray.getJSONObject(i); // Получаем объект таска
                                    String taskId = taskObject.getString("_id"); // Получаем _id из объекта таска
                                    task.setTaskId(taskId);
                                    Log.d("!", "Task ID: " + taskId);
                                }


                                callback.onSuccess(response);
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }


                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e("ApiError", "Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                                callback.onFailure("Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                            } else {
                                Log.e("ApiError", "Volley Error: " + error.getMessage());
                                callback.onFailure("Volley Error: " + error.getMessage());
                            }
                        }
                    });

            // Add the request to the request queue
            Volley.newRequestQueue(context).add(request);
        }


        public static void updateTaskList(Context context, String boardId, String columnId, String cardId, TaskModel task, Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + columnId + "/cards/" + cardId + "/tasks";

            // Convert the task object to a JSONObject
            JSONObject taskObject = new JSONObject();
            try {
                taskObject.put("description", task.getDescription());
                taskObject.put("checked", task.isChecked());
                Log.d("?", "?" +  task.isChecked());
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            // Construct the request data
            JSONObject requestData = new JSONObject();
            try {
                requestData.put("tasks", new JSONArray().put(taskObject));
            } catch (JSONException e) {
                e.printStackTrace();
                callback.onFailure("JSON Error: " + e.getMessage());
                return;
            }

            // Create the JsonObjectRequest
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.PUT, url, requestData,
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
                            if (error.networkResponse != null) {
                                Log.e("ApiError", "Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                                callback.onFailure("Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                            } else {
                                Log.e("ApiError", "Volley Error: " + error.getMessage());
                                callback.onFailure("Volley Error: " + error.getMessage());
                            }
                        }
                    });

            // Add the request to the request queue
            Volley.newRequestQueue(context).add(request);
        }



        public static void getTaskList(Context context, String boardId, String columnId, String cardId, TaskIdCallback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + columnId + "/cards/" + cardId + "/tasks";

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("ApiResponse", "Response: " + response.toString());
                            try {
                                JSONArray tasksArray = response.getJSONArray("tasks");

                                List<String> taskIds = new ArrayList<>();
                                for (int i = 0; i < tasksArray.length(); i++) {
                                    JSONObject taskObject = tasksArray.getJSONObject(i);
                                    String taskId = taskObject.getString("_id");
                                    taskIds.add(taskId);
                                }

                                callback.onSuccessWithTaskIds(response, taskIds);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.onFailure("JSON Error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e("ApiError", "Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                                callback.onFailure("Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                            } else {
                                Log.e("ApiError", "Volley Error: " + error.getMessage());
                                callback.onFailure("Volley Error: " + error.getMessage());
                            }
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }





        public static void deleteTask(Context context, String boardId, String columnId, String cardId, String taskId, Callback callback) {
            String url = BASE_URL + "boards/" + boardId + "/columns/" + columnId + "/cards/" + cardId + "/tasks/" + taskId;

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                callback.onSuccess(response);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                callback.onFailure("JSON Error: " + e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (error.networkResponse != null) {
                                Log.e("ApiError", "Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                                callback.onFailure("Error: " + error.networkResponse.statusCode + ", " + new String(error.networkResponse.data));
                            } else {
                                Log.e("ApiError", "Volley Error: " + error.getMessage());
                                callback.onFailure("Volley Error: " + error.getMessage());
                            }
                        }
                    });

            Volley.newRequestQueue(context).add(request);
        }



        public interface TaskIdCallback {
            void onSuccessWithTaskIds(JSONObject userData, List<String> taskIds) throws JSONException;
            void onFailure(String errorMessage);
        }

        public interface Callback {
            void onSuccess(JSONObject userData) throws JSONException;
            void onFailure(String errorMessage);
        }



    }