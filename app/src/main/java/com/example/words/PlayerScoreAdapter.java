package com.example.words;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PlayerScoreAdapter extends RecyclerView.Adapter<PlayerScoreAdapter.ViewHolder> {

    private final List<PlayerScore> playerScores;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView nicknameTextView;
        public final TextView scoreTextView;
        public final TextView timeTextView;

        public ViewHolder(View view) {
            super(view);
            nicknameTextView = itemView.findViewById(R.id.nicknameTextView);
            scoreTextView = itemView.findViewById(R.id.scoreTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }

    public PlayerScoreAdapter(List<PlayerScore> playerScores) {
        this.playerScores = playerScores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_player_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlayerScore playerScore = playerScores.get(position);
        holder.nicknameTextView.setText(playerScore.getNickname());
        String scoreText = "Очки: " + playerScore.getScore();
        holder.scoreTextView.setText(scoreText);
        String timeText = "Время: " + playerScore.getTimeElapsed();
        holder.timeTextView.setText(timeText);
    }

    @Override
    public int getItemCount() {
        return playerScores.size();
    }
}
