package com.example.words;

import android.util.Log;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import androidx.annotation.NonNull;
import java.util.Random;

public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Random random = new Random();
    private final SurfaceHolder holder;
    private Thread drawingThread;
    private volatile boolean running = false;
    private char currentLetter = ' ';
    private float letterX, letterY; // Координаты текущей буквы
    private final int letterSize = 100; // Предположим, что это размер буквы

    public GameSurfaceView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        paint.setColor(Color.BLACK);
        paint.setTextSize(letterSize); // Установите размер текста
        paint.setTextAlign(Paint.Align.CENTER); // Выравнивание текста по центру
    }

    public void startDrawing() {
        if (drawingThread == null || !drawingThread.isAlive()) {
            drawingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (running) {
                        Canvas canvas = holder.lockCanvas();
                        if (canvas != null) {
                            synchronized (holder) {
                                drawLetter(canvas);
                                holder.unlockCanvasAndPost(canvas);
                            }
                        }
                        try {
                            Thread.sleep(1500); // Пауза перед следующей отрисовкой
                        } catch (InterruptedException e) {
                            // Если поток прерван, выходим из цикла
                            running = false;
                        }
                    }
                }
            });
            running = true;
            drawingThread.start();
        }
    }

    public void stopDrawing() {
        running = false;
        if (drawingThread != null) {
            try {
                drawingThread.join();
            } catch (InterruptedException e) {
                // Обработка исключения
            }
        }
    }

    public void pause() {
        stopDrawing();
    }

    public void resume() {
        startDrawing();
    }

    private void drawLetter(Canvas canvas) {
        // Очищаем холст
        canvas.drawColor(Color.WHITE);

        // Выбираем случайную букву
        currentLetter = (char) ('a' + random.nextInt(26));

        // Рассчитываем координаты для отрисовки буквы
        letterX = getWidth() / 2f;
        letterY = getHeight() / 2f + letterSize / 2f; // Смещение вниз, чтобы выровнять по центру

        // Отрисовываем букву на холсте
        canvas.drawText(String.valueOf(currentLetter), letterX, letterY, paint);
    }

    public char getSelectedLetter(float x, float y) {
        // Рассчитываем размер области вокруг буквы для определения выбора
        float letterHitSize = letterSize * 10f; // Размер вокруг буквы

        // Логируем координаты касания и положение буквы
        Log.d("GameSurfaceView", "Touch coordinates: (" + x + ", " + y + ")");
        Log.d("GameSurfaceView", "Letter position: (" + letterX + ", " + letterY + "), hit size: " + letterHitSize);

        // Проверяем, находятся ли координаты касания в пределах области буквы
        if (x >= letterX - letterHitSize && x <= letterX + letterHitSize &&
                y >= letterY - letterHitSize && y <= letterY + letterHitSize) {
            Log.d("GameSurfaceView", "Letter selected: " + currentLetter);
            return currentLetter;
        } else {
            Log.d("GameSurfaceView", "No letter selected");
            return ' '; // Возвращаем пробел, указывая на то, что касание было вне области буквы
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        resume(); // Запускаем отрисовку при создании Surface
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        // Метод для обработки изменений размера SurfaceView, если это необходимо
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        boolean retry = true;
        running = false;
        while (retry) {
            try {
                drawingThread.join(); // Wait for the drawing thread to finish
                retry = false;
            } catch (InterruptedException e) {
                // If the current thread is interrupted while waiting, try again
            }
        }
    }
}