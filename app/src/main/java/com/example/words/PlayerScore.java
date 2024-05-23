package com.example.words;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlayerScore {
    private String nickname; // Никнейм игрока
    private int score;       // Количество очков игрока
    private String timeElapsed;     // Форматированная строка времени

    public PlayerScore() {
        // Пустой конструктор необходим для Firebase
    }

    public PlayerScore(String nickname, int score, String time) {
        this.nickname = nickname;
        this.score = score;
        this.timeElapsed = time;
    }

    // Геттеры и сеттеры
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(String timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

}