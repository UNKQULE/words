package com.example.words;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ScoreActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private ExecutorService executorService;
    private int user;
    private int finalScore;
    private String startTime;
    private String stopTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        TextView scoreMessage = findViewById(R.id.scoreMessage);


        sharedPreferences = getSharedPreferences("WordGamePreferences", MODE_PRIVATE);
        executorService = Executors.newSingleThreadExecutor();

        // Извлекаем данные из интента
        Intent intent = getIntent();
        String nickname = sharedPreferences.getString("nickname", "");
        finalScore = intent.getIntExtra("score", 0);
        startTime = intent.getStringExtra("startTime");
        stopTime = intent.getStringExtra("endTime");
        long timeElapsed = getIntent().getLongExtra("timeElapsed", 0);
        boolean won = getIntent().getBooleanExtra("won", false);

        // Отправляем никнейм и получаем userId
        sendNicknameAndGetId(nickname);

        int seconds = (int) (timeElapsed / 1000) % 60;
        int minutes = (int) ((timeElapsed / (1000 * 60)) % 60);
        String timeFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);

        if (won) {
            scoreMessage.setText(getString(R.string.win_message, finalScore, timeFormatted));
        } else {
            scoreMessage.setText(getString(R.string.lose_message, finalScore, timeFormatted));
        }
        // Настройка кнопки для начала новой игры
        Button playAgainButton = findViewById(R.id.playAgainButton);
        playAgainButton.setOnClickListener(v -> {
            Intent gameIntent = new Intent(ScoreActivity.this, GameActivity.class);
            startActivity(gameIntent);
            finish();
        });
    }

    private void sendNicknameAndGetId(String nickname) {
        executorService.submit(() -> {
            if(nickname == null){
                return;
            }
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://95.31.215.151:6969/user"); // Замените на ваш URL
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                // Строим JSON объект для отправки
                JSONObject nicknameJson = new JSONObject();
                nicknameJson.put("nickname", nickname);

                // Отправляем JSON
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = nicknameJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Чтение ответа от сервера
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                    String responseLine;
                    StringBuilder response = new StringBuilder();
                    while ((responseLine = reader.readLine()) != null) {
                        response.append(responseLine.trim());
                    }
                    JSONObject jsonResponse = new JSONObject(response.toString());

                    // Извлекаем userId из ответа и сохраняем в SharedPreferences
                    user = jsonResponse.getInt("id");
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("user", user);
                    editor.apply();

                    // После получения userId можно отправить счет на сервер
                    sendScoreToServer(finalScore, startTime, stopTime, user);
                } else {
                    Log.e("HTTP_ERROR", "Server responded with status code: " + responseCode);
                }
            } catch (Exception e) {
                Log.e("NETWORK_ERROR", "Failed to send nickname to server", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });

    }

    private void sendScoreToServer(int score, String startTime, String stopTime, Integer user) {
        executorService.submit(() -> {
            if (startTime == null || stopTime == null || user == null) {
                Log.e("ScoreActivity", "Cannot send score to server: One or more fields are null.");
                return; // Exit the method if any value is null
            }

            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://95.31.215.151:6969/game");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true);

                SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
                isoFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date parsedStartTime = isoFormat.parse(startTime);
                Date parsedStopTime = isoFormat.parse(stopTime);

                JSONObject scoreJson = new JSONObject();
                scoreJson.put("score", score);
                scoreJson.put("start_time", isoFormat.format(parsedStartTime));
                scoreJson.put("stop_time", isoFormat.format(parsedStopTime));
                scoreJson.put("user", user);

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = scoreJson.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    reader.lines().forEach(response::append);
                    Log.d("ScoreActivity", "Score submitted successfully: " + response);
                } else {
                    Log.e("HTTP_ERROR", "Server responded with status code: " + responseCode);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), "utf-8"));
                    StringBuilder response = new StringBuilder();
                    reader.lines().forEach(line -> {
                        response.append(line.trim());
                        Log.e("HTTP_ERROR_BODY", response.toString());
                    });
                }
            } catch (Exception e) {
                Log.e("NETWORK_ERROR", "Failed to send score to server", e);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}