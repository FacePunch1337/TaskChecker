package com.example.taskchecker.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class CubeView extends View {
    private Paint paint = new Paint();
    private float cubeSize = 200f;
    private float rotationAngleY = 0;
    public CubeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        // Настройка кисти
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);

        // Верхняя грань
        drawLine(canvas, centerX - cubeSize, centerY - cubeSize / 2, centerX, centerY - cubeSize);
        drawLine(canvas, centerX, centerY - cubeSize, centerX + cubeSize, centerY - cubeSize / 2);
        drawLine(canvas, centerX, centerY + cubeSize / centerX, centerX + cubeSize, centerY - cubeSize / 2);
        drawLine(canvas, centerX, centerY + cubeSize / centerX, centerX - cubeSize, centerY - cubeSize / 2);


        // Боковые грани
        drawLine(canvas, centerX - cubeSize, centerY - cubeSize / 2, centerX - cubeSize, centerY + cubeSize / 2);
        drawLine(canvas, centerX + cubeSize, centerY - cubeSize / 2, centerX + cubeSize, centerY + cubeSize / 2);
        drawLine(canvas, centerX + cubeSize / centerX, centerY - cubeSize , centerX , centerY + cubeSize);

        // Нижняя грань

        drawLine(canvas, centerX - cubeSize, centerY + cubeSize / 2, centerX, centerY + cubeSize);
        drawLine(canvas, centerX, centerY + cubeSize, centerX + cubeSize, centerY + cubeSize / 2);
        drawLine(canvas, centerX - cubeSize, centerY + cubeSize / 2, centerX, centerY + cubeSize / centerY);
        drawLine(canvas, centerX + cubeSize, centerY + cubeSize / 2, centerX, centerY + cubeSize / centerY);
    }

    // Рисует линию на холсте
    private void drawLine(Canvas canvas, float startX, float startY, float endX, float endY) {
        canvas.drawLine(startX, startY, endX, endY, paint);

    }

    // Поворачивает координаты X вокруг центра экрана
    private float rotateX(float x, float y, float rotationAngle) {
        float angle = (float) Math.toRadians(rotationAngle);
        return (float) (getWidth() / 2f + (x - getWidth() / 2f) * Math.cos(angle) - (y - getHeight() / 2f) * Math.sin(angle));
    }

    // Поворачивает координаты Y вокруг центра экрана
    private float rotateY(float x, float y, float rotationAngle) {
        float angle = (float) Math.toRadians(rotationAngle);
        return (float) (getHeight() / 2f + (x - getWidth() / 2f) * Math.sin(angle) + (y - getHeight() / 2f) * Math.cos(angle));
    }

    // Устанавливает угол вращения по оси Y
    public void setRotationAngleY(float angleY) {
         rotationAngleY = angleY;
        invalidate(); // Перерисовать представление с новым углом
    }

    // Начать анимацию вращения
    private void startRotationAnimation() {
        post(new Runnable() {
            @Override
            public void run() {
                rotationAngleY += 1; // Увеличиваем угол вращения
                if (rotationAngleY > 360) {
                    rotationAngleY -= 360; // Циклическое вращение в пределах 360 градусов
                }
                invalidate(); // Перерисовываем представление
                postDelayed(this, 20); // Повторяем через 20 миллисекунд
            }
        });
    }
}