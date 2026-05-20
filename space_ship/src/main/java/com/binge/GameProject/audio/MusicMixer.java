package com.binge.GameProject.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class MusicMixer {
    private double ambientVolume = 1.0;
    private final Map<String, Double> groupVolumes = new HashMap<>();
    private final Map<String, MediaPlayer> groupPlayers = new HashMap<>();

    public void fadeAmbient(double targetVolume) {
        ambientVolume += (targetVolume - ambientVolume) * 0.08;
    }

    public void setGroupSongVolume(String groupId, String songPath, double volume) {
        double clamped = Math.max(0, Math.min(1, volume));
        groupVolumes.put(groupId, clamped);

        MediaPlayer player = groupPlayers.computeIfAbsent(groupId, id -> createLoopingPlayer(songPath));
        if (player != null) {
            player.setVolume(clamped);
            if (clamped > 0.01 && player.getStatus() != MediaPlayer.Status.PLAYING) {
                player.play();
            } else if (clamped <= 0.01 && player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
            }
        }
    }

    public void setGroupSongVolume(String groupId, double volume) {
        setGroupSongVolume(groupId, "/music/supernova.mp3", volume);
    }

    private MediaPlayer createLoopingPlayer(String songPath) {
        var url = MusicMixer.class.getResource(songPath);
        if (url == null) return null;
        MediaPlayer player = new MediaPlayer(new Media(url.toExternalForm()));
        player.setCycleCount(MediaPlayer.INDEFINITE);
        player.setOnEndOfMedia(() -> player.seek(Duration.ZERO));
        player.setVolume(0);
        return player;
    }

    public double getAmbientVolume() { return ambientVolume; }
    public double getGroupSongVolume(String groupId) { return groupVolumes.getOrDefault(groupId, 0.0); }
}
