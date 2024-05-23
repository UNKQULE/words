package com.example.words;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Locale;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.widget.Button;

public class ScoreActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private SharedPreferences sharedPreferences;
    private int finalScore;
    private String timeFormatted;
    private boolean won;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        // Инициализируем Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        TextView scoreMessage = findViewById(R.id.scoreMessage);
        sharedPreferences = getSharedPreferences("WordGamePreferences", MODE_PRIVATE);

        Intent intent = getIntent();
        String nickname = sharedPreferences.getString("nickname", "");
        finalScore = intent.getIntExtra("score", 0);
        long timeElapsed = intent.getLongExtra("timeElapsed", 0);
        won = intent.getBooleanExtra("won", false);

        // Форматируем время
        timeFormatted = formatTime(timeElapsed);

        if (won) {
            scoreMessage.setText(getString(R.string.win_message, finalScore, timeFormatted));
        } else {
            scoreMessage.setText(getString(R.string.lose_message, finalScore, timeFormatted));
        }

        // Сохраняем данные пользователя
        if (!nickname.isEmpty()) {
            savePlayerScore(nickname, finalScore, timeFormatted);
        }

        // Настройка кнопки для начала новой игры
        Button playAgainButton = findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(v -> {
            Intent gameIntent = new Intent(ScoreActivity.this, GameActivity.class);
            startActivity(gameIntent);
            finish();
        });
    }

    private String formatTime(long timeElapsed) {
        int seconds = (int) (timeElapsed / 1000) % 60;
        int minutes = (int) (timeElapsed / (1000 * 60)) % 60;
        int hours = (int) (timeElapsed / (1000 * 60 * 60));
        return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
    }

    private void savePlayerScore(String nickname, int score, String timeFormatted) {
        String key = mDatabase.child("scores").push().getKey();
        if (key != null) {
            PlayerScore playerScore = new PlayerScore(nickname, score, timeFormatted);
            mDatabase.child("scores").child(key).setValue(playerScore)
                    .addOnSuccessListener(aVoid -> Log.d("Firebase", "Score saved successfully!"))
                    .addOnFailureListener(e -> Log.e("Firebase", "Failed to save score", e));
        }
    }

    static class PlayerScore {
        public String nickname;
        public int score;
        public String timeElapsed; // Тип изменен на String

        public PlayerScore(String nickname, int score, String timeElapsed) {
            this.nickname = nickname;
            this.score = score;
            this.timeElapsed = timeElapsed;
        }
    }
}