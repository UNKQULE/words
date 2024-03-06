package com.example.words;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private EditText nicknameEditText;
    private SharedPreferences sharedPreferences;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        executorService = Executors.newSingleThreadExecutor();

        nicknameEditText = findViewById(R.id.nickname_edittext);
        Button playButton = findViewById(R.id.play_button);
        Button statisticsButton = findViewById(R.id.statistics_button);
        Button exitButton = findViewById(R.id.exit_button);

        sharedPreferences = getSharedPreferences("WordGamePreferences", MODE_PRIVATE);
        loadNickname();

        playButton.setOnClickListener(v -> {
            String nickname = nicknameEditText.getText().toString().trim();
            saveNickname();
            // Запускаем GameActivity
            Intent gameIntent = new Intent(MainActivity.this, GameActivity.class);
            gameIntent.putExtra("nickname", nickname);
            startActivity(gameIntent);
        });

        statisticsButton.setOnClickListener(v -> {
            saveNickname();
            // Запускаем StatisticsActivity
            Intent statisticsIntent = new Intent(MainActivity.this, StatisticsActivity.class);
            statisticsIntent.putExtra("nickname", nicknameEditText.getText().toString().trim());
            startActivity(statisticsIntent);
        });

        exitButton.setOnClickListener(v -> {
            saveNickname();
            finish();
        });
    }

    private void saveNickname() {
        String nickname = nicknameEditText.getText().toString().trim();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("nickname", nickname);
        editor.apply();
    }

    private void loadNickname() {
        String nickname = sharedPreferences.getString("nickname", "");
        nicknameEditText.setText(nickname);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown(); // Закрываем ExecutorService при уничтожении Activity
    }
}