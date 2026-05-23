package com.binge.GameProject.ui;

import com.binge.GameProject.audio.AudioSystem;
import com.binge.GameProject.gameplay.ScoreResult;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class ResultSequenceManager {
    private final VBox statsContainer;
    private final Text ratingText;
    private Timeline activeTimeline;
    private long lastTickNanos;

    public ResultSequenceManager(VBox statsContainer, Text ratingText) {
        this.statsContainer = statsContainer;
        this.ratingText = ratingText;
    }

    public void start(ScoreResult result) {
        if (activeTimeline != null) activeTimeline.stop();
        statsContainer.getChildren().clear();
        ratingText.setText("RANK: -");
        ratingText.setOpacity(0);
        lastTickNanos = 0;
        animateRow(result, 0);
    }

    private void animateRow(ScoreResult result, int index) {
        if (index >= 7) {
            PauseTransition pause = new PauseTransition(Duration.seconds(0.3));
            pause.setOnFinished(e -> revealRating(result.getRating()));
            pause.play();
            return;
        }

        Text value = addRow(labelFor(index), "");
        double target = targetFor(result, index);
        activeTimeline = new Timeline();
        activeTimeline.setCycleCount(12);
        activeTimeline.getKeyFrames().add(new KeyFrame(Duration.seconds(0.05), e -> {
            double progress = (double) activeTimeline.getCurrentTime().toMillis() / 600.0;
            progress = Math.max(0, Math.min(1, progress));
            value.setText(formatValue(result, index, target * progress));
            playTickThrottled();
        }));
        activeTimeline.setOnFinished(e -> {
            value.setText(formatValue(result, index, target));
            animateRow(result, index + 1);
        });
        activeTimeline.playFromStart();
    }

    private Text addRow(String labelText, String valueText) {
        HBox row = new HBox(20);
        Text label = new Text(labelText);
        label.setFont(new Font("Consolas", 20));
        label.setFill(Color.GRAY);

        Text value = new Text(valueText);
        value.setFont(new Font("Consolas", 20));
        value.setFill(Color.WHITE);

        row.getChildren().addAll(label, value);
        statsContainer.getChildren().add(row);
        return value;
    }

    private String labelFor(int index) {
        return switch (index) {
            case 0 -> "RESCUED:";
            case 1 -> "RESCUE RATE:";
            case 2 -> "ALIVE TEAM:";
            case 3 -> "LOST:";
            case 4 -> "ENEMY KILLS:";
            case 5 -> "HP LEFT:";
            default -> "TOTAL SCORE:";
        };
    }

    private double targetFor(ScoreResult result, int index) {
        return switch (index) {
            case 0 -> result.getRescuedCount();
            case 1 -> result.getRescueRate() * 100.0;
            case 2 -> result.getAliveRescuedCount();
            case 3 -> result.getLostRescueCount();
            case 4 -> result.getEnemyKillCount();
            case 5 -> result.getPlayerHp();
            default -> result.getTotalScore();
        };
    }

    private String formatValue(ScoreResult result, int index, double value) {
        return switch (index) {
            case 0 -> String.format("%d / %d", Math.round(value), result.getTotalRescueTargetCount());
            case 1 -> String.format("%d%%", Math.round(value));
            case 6 -> String.format("%d / 100", Math.round(value));
            default -> String.valueOf(Math.round(value));
        };
    }

    private void playTickThrottled() {
        long now = System.nanoTime();
        if (now - lastTickNanos < 55_000_000L) return;
        lastTickNanos = now;
        if (AudioSystem.getInstance() != null) {
            AudioSystem.getInstance().playResultTick();
        }
    }

    private void revealRating(String rating) {
        ratingText.setText("RANK: " + rating);
        ratingText.setOpacity(1);
        if (AudioSystem.getInstance() != null) {
            AudioSystem.getInstance().playRatingReveal(rating);
        }
    }
}
