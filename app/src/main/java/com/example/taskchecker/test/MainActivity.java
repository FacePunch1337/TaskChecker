package com.example.taskchecker.test;

import android.os.Bundle;

import android.view.MotionEvent;

import androidx.appcompat.app.AppCompatActivity;

import com.example.taskchecker.R;
import com.example.taskchecker.test.CubeView;

public class MainActivity extends AppCompatActivity {

    private CubeView cubeView;
    private float startX, startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Найдите CubeView в вашем макете по его идентификатору
        cubeView = findViewById(R.id.cubeView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Обработайте события касания пользователем экрана
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // При нажатии на экран сохраните начальные координаты
                startX = event.getX();
                startY = event.getY();

                break;
            case MotionEvent.ACTION_MOVE:
                // При перемещении пальца по экрану вычислите разницу с предыдущими координатами
                float deltaX = event.getX() - startX;
                float deltaY = event.getY() - startY;
                // Поверните куб на основе этой разницы
                // cubeView.rotateCube(deltaX, deltaY);
                break;
        }
        return true;
    }
}
