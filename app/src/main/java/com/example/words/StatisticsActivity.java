package com.example.words;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.words.PlayerScore;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PlayerScoreAdapter adapter;
    private DatabaseReference databaseReference;
    private List<PlayerScore> playerScores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        playerScores = new ArrayList<>();
        adapter = new PlayerScoreAdapter(playerScores);
        recyclerView.setAdapter(adapter);

        // Установка Firebase Database path
        databaseReference = FirebaseDatabase.getInstance().getReference("scores");

        // Получение данных из Firebase и их отображение
        databaseReference.orderByChild("score").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                playerScores.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    PlayerScore playerScore = snapshot.getValue(PlayerScore.class);
                    if (playerScore != null) {
                        playerScores.add(playerScore);
                    }
                }
                // Отсортировать список по убыванию количества очков
                Collections.sort(playerScores, (p1, p2) -> Integer.compare(p2.getScore(), p1.getScore()));
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Обработка ошибок, если они возникают
            }
        });

    }
}
