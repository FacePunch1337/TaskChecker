package com.example.taskchecker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.taskchecker.R;
import com.example.taskchecker.layouts.SignInFragment;
import com.example.taskchecker.layouts.SignUpFragment;


public class AuthActivity extends AppCompatActivity {

    private ImageButton signUpPageButton;
    private ImageButton signInPageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);
        signInPageButton = findViewById(R.id.signInPageButton);
        signUpPageButton = findViewById(R.id.signUpPageButton);

        signUpPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                loadFragment(new SignUpFragment());
                signInPageButton.setBackgroundResource(android.R.color.transparent);
                signUpPageButton.setBackgroundResource(R.drawable.rounded_border_shadow);
            }
        });

        signInPageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new SignInFragment());
                signInPageButton.setBackgroundResource(R.drawable.rounded_border_shadow);
                signUpPageButton.setBackgroundResource(android.R.color.transparent);
            }
        });
    }

    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentView, fragment)
                .addToBackStack(null)
                .commit();
    }


}

