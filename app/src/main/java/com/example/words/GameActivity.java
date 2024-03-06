package com.example.words;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {
    private final Random random = new Random();
    private Chronometer gameChronometer;
    private TextView wordToGuess;
    private EditText inputWord;
    private FrameLayout gameField;
    private final String[] wordsToGuess = {"apple", "banana", "cherry", "date", "fig", "grape", "kiwi", "lemon", "melon", "orange"};
    private String currentWord;
    private StringBuilder currentGuess;
    private long gameStartTime;

    private GameSurfaceView gameSurfaceView;
    private TextView scoreCounter;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        gameChronometer = findViewById(R.id.gameChronometer);
        scoreCounter = findViewById(R.id.scoreCounter);
        wordToGuess = findViewById(R.id.wordToGuess);
        inputWord = findViewById(R.id.inputWord);
        gameField = findViewById(R.id.gameField);

        gameSurfaceView = new GameSurfaceView(this);
        gameField.addView(gameSurfaceView);

        initializeGame();
        setupTouchListener();
        gameChronometer.setBase(SystemClock.elapsedRealtime());
        gameChronometer.start();
        gameStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameSurfaceView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurfaceView.resume();
    }

    private void initializeGame() {
        currentWord = wordsToGuess[random.nextInt(wordsToGuess.length)];
        wordToGuess.setText(currentWord);
        currentGuess = new StringBuilder();
        for (int i = 0; i < currentWord.length(); i++) {
            currentGuess.append("_");
        }
        inputWord.setText(currentGuess.toString());
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListener() {
        gameField.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float x = event.getX();
                    float y = event.getY();
                    char selectedLetter = gameSurfaceView.getSelectedLetter(x, y);
                    onLetterSelected(selectedLetter);
                    return true;
                }
                return false;
            }
        });
    }

    private void onLetterSelected(char letter) {
        if (letter == ' ') {
            return;
        }
        if (currentWord.indexOf(letter) != -1) {
            boolean isGuessUpdated = false;
            for (int i = 0; i < currentWord.length(); i++) {
                if (currentWord.charAt(i) == letter && currentGuess.charAt(i) == '_') {
                    currentGuess.setCharAt(i, letter);
                    score += 50;
                    isGuessUpdated = true;
                }
            }
            scoreCounter.setText(String.valueOf(score));
            inputWord.setText(currentGuess.toString());
            if (currentGuess.toString().equals(currentWord)) {
                finishGame(true);
                return;
            }
            if (isGuessUpdated) {
                return;
            }
        }
        finishGame(false);
    }

    private void finishGame(boolean won) {
        gameChronometer.stop();
        long endTime = System.currentTimeMillis(); // Время окончания игры
        long timeElapsed = endTime - gameStartTime; // Вычисляем прошедшее время в миллисекундах

        Intent scoreIntent = new Intent(this, ScoreActivity.class);
        scoreIntent.putExtra("won", won);
        scoreIntent.putExtra("score", score);
        scoreIntent.putExtra("startTime", getCurrentTimestamp(gameStartTime)); // Время начала игры
        scoreIntent.putExtra("endTime", getCurrentTimestamp(endTime)); // Время окончания игры
        scoreIntent.putExtra("timeElapsed", timeElapsed); // Передаем прошедшее время

        startActivity(scoreIntent);
        finish();
    }

    private String getCurrentTimestamp(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return dateFormat.format(new Date(time));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameSurfaceView.pause();
    }
}