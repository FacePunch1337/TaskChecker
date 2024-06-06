package com.example.taskchecker.services;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;


public class MessagingService extends Service {



    public void getDeviceToken() {
        FirebaseApp.initializeApp(MessagingService.this);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if (!task.isSuccessful()) {
                    Log.e("FireBaseLogs", " Fetching Toke Failed" + task.getException());
                    return;
                }

                //Get Device Token
                String token = task.getResult();
                Log.v("FireBaseLogs", "Device Token: " + token);
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}