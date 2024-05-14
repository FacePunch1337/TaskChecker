package com.example.taskchecker.layouts;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.example.taskchecker.R;
import com.example.taskchecker.services.UserApiService;
import com.example.taskchecker.activities.AuthActivity;

import org.json.JSONObject;

public class SignUpFragment extends Fragment {


    private TextView usernameLabel;
    private TextView emailLabel;
    private TextView passwordLabel;
    private TextView repeatPasswordLabel;

    private EditText usernameEditText, emailEditText, passwordEditText, repeatPasswordEditText;
    private Button signUpButton;
    private TextView alreadyHaveAccountButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.sign_up_panel, container, false);

        usernameLabel = rootView.findViewById(R.id.usernameTextView);
        emailLabel = rootView.findViewById(R.id.emailTextView);
        passwordLabel = rootView.findViewById(R.id.passwordTextView);
        repeatPasswordLabel = rootView.findViewById(R.id.repeatPasswordTextView);
        usernameEditText = rootView.findViewById(R.id.usernameEditText);
        emailEditText = rootView.findViewById(R.id.emailEditText);
        passwordEditText = rootView.findViewById(R.id.passwordEditText);
        repeatPasswordEditText = rootView.findViewById(R.id.repeatPasswordEditText);
        signUpButton = rootView.findViewById(R.id.signUpButton);
        alreadyHaveAccountButton = rootView.findViewById(R.id.alreadyHaveAccountButton);

        signUpButton.setOnClickListener(view -> signUp());
        alreadyHaveAccountButton.setOnClickListener(view -> {


        });

        usernameEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        emailEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        passwordEditText.setPadding(70, 0, 35, 10);
        repeatPasswordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);

        setEditTextTransparency(usernameEditText, usernameLabel, false);
        setEditTextTransparency(emailEditText, emailLabel,false);
        setEditTextTransparency(passwordEditText, passwordLabel, false);
        setEditTextTransparency(repeatPasswordEditText, repeatPasswordLabel,false);

        usernameEditText.setOnFocusChangeListener((v, hasFocus) -> setEditTextTransparency(usernameEditText, usernameLabel,hasFocus));
        emailEditText.setOnFocusChangeListener((v, hasFocus) -> setEditTextTransparency(emailEditText, emailLabel,hasFocus));
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> setEditTextTransparency(passwordEditText, passwordLabel,hasFocus));
        repeatPasswordEditText.setOnFocusChangeListener((v, hasFocus) -> setEditTextTransparency(repeatPasswordEditText, repeatPasswordLabel,hasFocus));

        passwordEditText.setOnTouchListener((v, event) -> {
            // Check if drawableEnd is clicked
            if (event.getAction() == MotionEvent.ACTION_UP && event.getRawX() >= (passwordEditText.getRight() - passwordEditText.getCompoundDrawables()[2].getBounds().width())) {
                // Toggle password visibility
                togglePasswordVisibility();
                return true;
            }
            return false;
        });




        return rootView;
    }

    private void setEditTextTransparency(EditText editText, TextView label, boolean hasFocus) {
        String fieldValue = editText.getText().toString();
        // Проверяем поле на валидность перед изменением прозрачности
        if (!hasFocus && editText != passwordEditText) {
            validateField(editText, label);
        }
        // Устанавливаем прозрачность в зависимости от фокуса и содержимого поля
        if (!hasFocus && fieldValue.isEmpty()) {
            editText.setAlpha(0.5f); // Устанавливаем прозрачность 0.5 при отсутствии фокуса и если поле пустое
        } else {
            editText.setAlpha(1.0f); // Возвращаем полную прозрачность при фокусе или если поле не пустое
        }
    }


    private void validateField(EditText field, TextView label) {
        String fieldValue = field.getText().toString();

        // Устанавливаем начальные стили и иконки
        field.setBackgroundResource(R.drawable.rounded_border_shadow);
        field.setPadding(70, 0, 35, 10);
        field.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        label.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        // Проверяем, заполнено ли поле
        if (!TextUtils.isEmpty(fieldValue)) {
            // Проверяем соответствие формату почты или длине пароля в зависимости от поля
            if ((field == emailEditText && !isValidEmail(fieldValue)) || (field == repeatPasswordEditText && !isValidPasswordLength(fieldValue))) {
                field.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.error), null);
                field.setBackgroundResource(R.drawable.rounded_error_border_shadow);
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                field.setPadding(70, 0, 35, 10);

            } else {
                field.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.correct), null);
                field.setBackgroundResource(R.drawable.rounded_border_shadow);
                label.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                field.setPadding(70, 0, 35, 10);
            }
        }
    }

    private void togglePasswordVisibility() {
        if (passwordEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
            // Change password input type to visible
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            // Change drawableEnd icon to hide_password_icon
            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.eyeopen), null);
            // Set font to Inter-Black
            passwordEditText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_black));
        } else {
            // Change password input type to hidden
            passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            // Change drawableEnd icon to show_password_icon
            passwordEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.eyeclose), null);
            // Remove font
            passwordEditText.setTypeface(ResourcesCompat.getFont(requireContext(), R.font.inter_black));
        }
        passwordEditText.setSelection(passwordEditText.length());
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPasswordLength(String password) {
        int minLength = 6; // Минимальная длина пароля
        int maxLength = 20; // Максимальная длина пароля
        return password.length() >= minLength && password.length() <= maxLength;
    }

    private boolean arePasswordsEqual(String password, String repeatPassword) {
        return password.equals(repeatPassword);
    }

    // Метод для отправки запроса на регистрацию
    private void signUp() {
        String username = usernameEditText.getText().toString();
        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String repeatPassword = repeatPasswordEditText.getText().toString();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(requireContext(), "Неверный формат почты", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPasswordLength(password)) {
            Toast.makeText(requireContext(), "Пароль должен быть от 6 до 20 символов", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!arePasswordsEqual(password, repeatPassword)) {
            Toast.makeText(requireContext(), "Пароли не совпадают", Toast.LENGTH_SHORT).show();
            return;
        }

        UserApiService.signUp(requireActivity(), username, email, password, new UserApiService.Callback() {
            @Override
            public void onSuccess(JSONObject userData) {
                if (userData != null) {
                    Intent intent = new Intent(getActivity(), AuthActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(requireContext(), "Пустые данные пользователя", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                usernameEditText.setBackgroundResource(R.drawable.rounded_error_border_shadow);
                emailEditText.setBackgroundResource(R.drawable.rounded_error_border_shadow);
                passwordEditText.setBackgroundResource(R.drawable.rounded_error_border_shadow);
                repeatPasswordEditText.setBackgroundResource(R.drawable.rounded_error_border_shadow);

                usernameEditText.setPadding(70, 0, 35, 10);
                emailEditText.setPadding(70, 0, 35, 10);
                passwordEditText.setPadding(70, 0, 35, 10);
                repeatPasswordEditText.setPadding(70, 0, 35, 10);
                usernameLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                emailLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                passwordLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));
                repeatPasswordLabel.setTextColor(ContextCompat.getColor(requireContext(), R.color.red));

                //usernameEditText.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, ContextCompat.getDrawable(requireContext(), R.drawable.error), null);
            }
        });
    }

}
