package com.example.taskchecker.layouts;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.taskchecker.R;
import com.example.taskchecker.activities.WorkSpaceActivity;
import com.example.taskchecker.services.UserApiService;
import com.example.taskchecker.activities.ProfileActivity;

import org.json.JSONObject;

public class SignInFragment extends Fragment {

    private TextView usernameLabel;
    private TextView passwordLabel;
    private EditText usernameEditText, passwordEditText;
    private Button signInButton;
    private TextView signUpLink;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.sign_in_panel, container, false);

        usernameLabel = rootView.findViewById(R.id.usernameTextView);
        passwordLabel = rootView.findViewById(R.id.passwordTextView);
        usernameEditText = rootView.findViewById(R.id.usernameEditText);
        passwordEditText = rootView.findViewById(R.id.passwordEditText);
        signInButton = rootView.findViewById(R.id.signInButton);

        setEditTextTransparency(usernameEditText, usernameLabel, false);
        setEditTextTransparency(passwordEditText, passwordLabel, false);

        usernameEditText.setOnFocusChangeListener((v, hasFocus) -> setEditTextTransparency(usernameEditText, usernameLabel, hasFocus));
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> setEditTextTransparency(passwordEditText, passwordLabel, hasFocus));

        signInButton.setOnClickListener(view -> signIn());

        passwordEditText.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP && event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width())) {
                togglePasswordVisibility();
                return true;
            }
            return false;
        });

        return rootView;
    }

    private void setEditTextTransparency(EditText editText, TextView label, boolean hasFocus) {
        String fieldValue = editText.getText().toString();
        if (!hasFocus && editText != passwordEditText) {
            validateField(editText, label);
        }
        if (!hasFocus && fieldValue.isEmpty()) {
            editText.setAlpha(0.5f);
        } else {
            editText.setAlpha(1.0f);
        }
    }

    private void validateField(EditText field, TextView label) {
        String fieldValue = field.getText().toString();

        field.setBackgroundResource(R.drawable.rounded_border_shadow);
        field.setPadding(70, 0, 35, 10);
        field.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        label.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        if (!TextUtils.isEmpty(fieldValue)) {
            if (field == usernameEditText && !field.isFocused()) {
                field.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.correct), null);
            } else if (field == passwordEditText && !field.isFocused()) {
                // Specific password validation if needed
            }
        }
    }

    private void togglePasswordVisibility() {
        if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.eyeopen), null);
            passwordEditText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_black));
        } else {
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.eyeclose), null);
            passwordEditText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_black));
        }
        passwordEditText.setSelection(passwordEditText.length());
    }

    private void signIn() {
        String username = usernameEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        UserApiService.signIn(requireActivity(), username, password, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject userData) {
                Intent intent = new Intent(getActivity(), WorkSpaceActivity.class);
                startActivity(intent);
                Toast.makeText(requireContext(), "Welcome!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(requireContext(), "No!", Toast.LENGTH_SHORT).show();
                usernameEditText.setBackgroundResource(R.drawable.rounded_error_border_shadow);
                passwordEditText.setBackgroundResource(R.drawable.rounded_error_border_shadow);
                usernameEditText.setPadding(70, 0, 35, 10);
                passwordEditText.setPadding(70, 0, 35, 10);
                usernameLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                passwordLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                usernameEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.error), null);
            }
        });
    }
}

